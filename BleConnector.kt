package com.incommsys.android.composefolderscanner.cs108library

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.UUID

@SuppressLint("MissingPermission")
open class BleConnector(context: Context, mLogView: TextView?) : BluetoothGattCallback() {
   var DEBUG_PKDATA: Boolean
   var DEBUG_APDATA: Boolean
   val DEBUG_SCAN = false
   val DEBUG_CONNECT = false
   val DEBUG_BTDATA = false
   val DEBUG_FILE = false
   open val DEBUG = true
   val DEBUG_BTOP = false
   private val mHandler = Handler()
   private var mBluetoothDevice: ReaderDevice? = null
   fun getmBluetoothDevice(): ReaderDevice? {
      return mBluetoothDevice
   }

   var mBluetoothManager: BluetoothManager? = null
   var mBluetoothAdapter: BluetoothAdapter? = null
   var mBluetoothGatt: BluetoothGatt? = null
   private var mleScanner: BluetoothLeScanner? = null
   var mBluetoothConnectionState = 0
   open val isBleConnected: Boolean
      get() = mBluetoothConnectionState == BluetoothProfile.STATE_CONNECTED && mReaderStreamOutCharacteristic != null
   open var isBleScanning = false
   var serviceUUID2p1 = 0
   fun setServiceUUIDType(serviceUUID2p1: Int) {
      this.serviceUUID2p1 = serviceUUID2p1
   }

   private val UUID_READER_STREAM_OUT_CHARACTERISTIC =
      UUID.fromString("00009900-0000-1000-8000-00805f9b34fb")
   private val UUID_READER_STREAM_IN_CHARACTERISTIC =
      UUID.fromString("00009901-0000-1000-8000-00805f9b34fb")
   open var rssi = 0
   var isCharacteristicListRead = false
      private set
   var mReaderStreamOutCharacteristic: BluetoothGattCharacteristic? = null
   private var mReaderStreamInCharacteristic: BluetoothGattCharacteristic? = null
   private var mStreamWriteCount: Long = 0
   private var mStreamWriteCountOld: Long = 0
   private var _readCharacteristic_in_progress = false
   private var _writeCharacteristic_in_progress = false
   private val mBluetoothGattCharacteristicToRead = ArrayList<BluetoothGattCharacteristic>()
   private val STREAM_IN_BUFFER_MAX = 0x100000 //0xC00;  //0x800;  //0x400;
   private val streamInBuffer = ByteArray(STREAM_IN_BUFFER_MAX)
   private var streamInBufferHead = 0
   private var streamInBufferTail = 0
   var streamInBufferSize = 0
      private set
   var streamInOverflowTime: Long = 0
      private set

   var streamInBytesMissing = 0

   var streamInTotalCounter = 0
      private set
   var streamInAddCounter = 0
      private set
   var streamInAddTime: Long = 0
      private set
   var connectionHSpeedA = true
      private set

   fun setConnectionHSpeedA(connectionHSpeed: Boolean): Boolean {
      connectionHSpeedA = connectionHSpeed
      return true
   }

   override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
      val DEBUG = false
      super.onConnectionStateChange(gatt, status, newState)
      if (DEBUG_CONNECT) appendToLog("newState = $newState")
      if (gatt != mBluetoothGatt) {
         if (DEBUG) appendToLog("abcc mismatched mBluetoothGatt = " + (gatt != mBluetoothGatt) + ", status = " + status)
      } else {
         mBluetoothConnectionState = newState
         when (newState) {
            BluetoothProfile.STATE_DISCONNECTED -> {
               if (DEBUG_CONNECT) appendToLog("state=Disconnected with status = $status")
               if (!disconnectRunning) {
                  if (DEBUG) appendToLog("disconnect b")
                  disconnect()
               }
            }

            BluetoothProfile.STATE_CONNECTED -> {
               if (DEBUG_CONNECT) appendToLog("state=Connected with status = $status")
               if (disconnectRunning) {
                  if (DEBUG) appendToLog("abcc disconnectRunning !!!")
                  return
               }
               run {
                  mStreamWriteCountOld = 0
                  mStreamWriteCount = mStreamWriteCountOld
               }
               run {
                  _writeCharacteristic_in_progress = false
                  _readCharacteristic_in_progress = _writeCharacteristic_in_progress
               }
               if (bDiscoverStarted) {
                  if (DEBUG) appendToLog("abc discovery has been started before")
                  return
               }
               if (DEBUG_CONNECT) appendToLog("Start discoverServices")
               if (discoverServices()) {
                  bDiscoverStarted = true
                  if (DEBUG_CONNECT) appendToLog("state=Connected. discoverServices starts with status = $status")
               } else {
                  if (DEBUG) appendToLog("state=Connected. discoverServices FAIL")
               }
               utility.setReferenceTimeMs()
               mHandler.removeCallbacks(mReadRssiRunnable)
               mHandler.post(mReadRssiRunnable)
            }

            else -> if (DEBUG) appendToLog("state=$newState")
         }
      }
   }

   var bDiscoverStarted = false
   @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
   override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
      val DEBUG = false
      super.onServicesDiscovered(gatt, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) appendToLog("INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) appendToLog("status=$status. restart discoverServices")
         discoverServices()
      } else {
         val UUID_READER_SERVICE = UUID.fromString("00009800-0000-1000-8000-00805f9b34fb")
         mReaderStreamOutCharacteristic =
            getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_OUT_CHARACTERISTIC)
         mReaderStreamInCharacteristic =
            getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_IN_CHARACTERISTIC)
         if (DEBUG_BTOP) appendToLog("mReaderStreamOutCharacteristic flag = " + mReaderStreamOutCharacteristic!!.properties)
         if (DEBUG_BTOP) appendToLog("mReaderStreamInCharacteristic flag = " + mReaderStreamInCharacteristic!!.properties)
         if (mReaderStreamInCharacteristic == null || mReaderStreamOutCharacteristic == null) {
            if (DEBUG_BTOP) appendToLog("restart discoverServices")
            discoverServices()
            return
         }
         if (!checkSelfPermissionBLUETOOTH()) return
         if (!mBluetoothGatt!!.setCharacteristicNotification(mReaderStreamInCharacteristic, true)) {
            if (DEBUG) appendToLog("setCharacteristicNotification() FAIL")
         } else {
            val mtu_requested = 255
            val bValue = gatt.requestMtu(mtu_requested)
            if (DEBUG_BTOP) appendToLog("requestMtu[$mtu_requested] with result=$bValue")
            if (DEBUG_BTOP) appendToLog("characteristicListRead = " + isCharacteristicListRead)
            if (!isCharacteristicListRead) {
               if (DEBUG) appendToLog("with services")
               mBluetoothGattCharacteristicToRead.clear()
               val ss = mBluetoothGatt!!.services
               for (service in ss) {
                  val uuid = service.uuid.toString().substring(4, 8) //substring(0, 8)
                  val cc = service.characteristics
                  for (characteristic in cc) {
                     val characteristicUuid =
                        characteristic.uuid.toString().substring(4, 8) //substring(0, 8)
                     val properties = characteristic.properties
                     var do_something = false
                     if (properties and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                        if (DEBUG) appendToLog("service=$uuid, characteristic=$characteristicUuid, property=read")
                        mBluetoothGattCharacteristicToRead.add(characteristic)
                        do_something = true
                     }
                     if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                        if (DEBUG) appendToLog("service=$uuid, characteristic=$characteristicUuid, property=write")
                        do_something = true
                     }
                     if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        if (DEBUG) appendToLog("service=$uuid, characteristic=$characteristicUuid, property=notify")
                        do_something = true
                     }
                     if (!do_something) {
                        if (DEBUG) appendToLog(
                           "service=" + uuid + ", characteristic=" + characteristicUuid + ", property=" + String.format(
                              "%X ",
                              properties
                           )
                        )
                     }
                  }
               }
               if (true) mBluetoothGattCharacteristicToRead.clear()
               mHandler.removeCallbacks(mReadCharacteristicRunnable)
               if (DEBUG) appendToLog("starts in onServicesDiscovered")
               mHandler.postDelayed(mReadCharacteristicRunnable, 500)
            }
         }
      }
   }

   override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
      val DEBUG = false
      super.onReadRemoteRssi(gatt, rssi, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) utility.appendToLogRunnable("onReadRemoteRssi: INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) utility.appendToLogRunnable("onReadRemoteRssi: NOT GATT_SUCCESS")
      } else {
         if (DEBUG_BTOP) utility.appendToLogRunnable("onReadRemoteRssi: rssi=$rssi")
         this.rssi = rssi
      }
   }

   private val mReadRssiRunnable: Runnable = object : Runnable {
      val DEBUG = false
      override fun run() {
         if (!checkSelfPermissionBLUETOOTH()) return
         if (mBluetoothGatt == null) {
            if (DEBUG) appendToLog("mReadRssiRunnable: readRemoteRssi with null mBluetoothGatt")
         } else if (mBluetoothGatt!!.readRemoteRssi()) {
            if (DEBUG_BTOP) appendToLog("mReadRssiRunnable: readRemoteRssi starts")
         } else {
            if (DEBUG) appendToLog("mReadRssiRunnable: readRemoteRssi FAIL")
         }
      }
   }

   override fun onDescriptorWrite(
      gatt: BluetoothGatt,
      descriptor: BluetoothGattDescriptor,
      status: Int
   ) {
      super.onDescriptorWrite(gatt, descriptor, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) appendToLog("INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) appendToLog("status=$status")
      } else {
         if (DEBUG) appendToLog("descriptor=" + descriptor.uuid.toString().substring(4, 8))
      }
   }

   private fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean {
      descriptor.value = value
      return if (!checkSelfPermissionBLUETOOTH()) false else mBluetoothGatt!!.writeDescriptor(
         descriptor
      )
   }

   override fun onDescriptorRead(
      gatt: BluetoothGatt,
      descriptor: BluetoothGattDescriptor,
      status: Int
   ) {
      super.onDescriptorRead(gatt, descriptor, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) utility.appendToLogRunnable("onDescriptorRead(): INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) utility.appendToLogRunnable("onDescriptorRead(): status=$status")
      } else {
         if (DEBUG) utility.appendToLogRunnable(
            "onDescriptorRead(): descriptor=" + descriptor.uuid.toString().substring(4, 8)
         )
      }
   }

   override fun onCharacteristicRead(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic,
      status: Int
   ) {
      super.onCharacteristicRead(gatt, characteristic, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) appendToLog("INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) appendToLog("status=$status")
      } else {
         _readCharacteristic_in_progress = false
         val serviceUuidd = characteristic.service.uuid.toString().substring(4, 8)
         val characteristicUuid = characteristic.uuid.toString().substring(4, 8)
         val v = characteristic.value
         val t = utility.referencedCurrentTimeMs
         mHandler.removeCallbacks(mReadCharacteristicRunnable)
         val stringBuilder = StringBuilder()
         if (v != null && v.size > 0) {
            stringBuilder.ensureCapacity(v.size * 3)
            for (b in v) stringBuilder.append(String.format("%02X ", b))
         }
         if (DEBUG) appendToLog(
            "$serviceUuidd, $characteristicUuid = $stringBuilder = $v"
         )
         if (DEBUG) appendToLog("starts in onCharacteristicRead")
         mReadCharacteristicRunnable.run()
      }
   }

   private val mReadCharacteristicRunnable: Runnable = object : Runnable {
      override fun run() {
         if (mBluetoothGattCharacteristicToRead.size == 0) {
            if (DEBUG) appendToLog("mReadCharacteristicRunnable(): read finish")
            isCharacteristicListRead = true
         } else if (isBleBusy) {
            if (DEBUG) appendToLog("mReadCharacteristicRunnable(): PortBusy")
            mHandler.postDelayed(this, 100)
         } else if (!readCharacteristic(mBluetoothGattCharacteristicToRead[0])) {
            if (DEBUG) appendToLog("mReadCharacteristicRunnable(): Read FAIL")
            mHandler.postDelayed(this, 100)
         } else {
            mBluetoothGattCharacteristicToRead.removeAt(0)
            if (DEBUG) appendToLog("mReadCharacteristicRunnable(): starts in mReadCharacteristicRunnable")
            mHandler.postDelayed(this, 10000)
         }
      }
   }

   @SuppressLint("MissingPermission")
   private fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
      if (!checkSelfPermissionBLUETOOTH()) return false
      if (mBluetoothGatt!!.readCharacteristic(characteristic)) {
         _readCharacteristic_in_progress = true
         return true
      }
      return false
   }

   override fun onCharacteristicWrite(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic,
      status: Int
   ) {
      val DEBUG = false
      super.onCharacteristicWrite(gatt, characteristic, status)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) appendToLog("INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         onCharacteristicWriteFailue++
         if (DEBUG) appendToLog("status=$status")
      } else {
         onCharacteristicWriteFailue = 0
         if (DEBUG) appendToLog(
            "characteristic=" + characteristic.uuid.toString()
               .substring(4, 8) + ", sent " + (mStreamWriteCount - mStreamWriteCountOld) + " bytes"
         )
         _writeCharacteristic_in_progress = false
      }
   }

   private var writeBleFailure = 0
   private var onCharacteristicWriteFailue = 0
   @SuppressLint("MissingPermission")

   fun writeBleStreamOut(value: ByteArray): Boolean {
      if (mBluetoothGatt == null) {
         if (DEBUG) appendToLog("ERROR with NULL mBluetoothGatt")
      } else if (mReaderStreamOutCharacteristic == null) {
         if (DEBUG) appendToLog("ERROR with NULL mReaderStreamOutCharacteristic")
      } else if (isBleBusy || !isCharacteristicListRead) {
         if (true) appendToLog("isBleBusy()  = " + isBleBusy + ", characteristicListRead = " + isCharacteristicListRead)
      } else {
         mReaderStreamOutCharacteristic!!.value = value
         if (!checkSelfPermissionBLUETOOTH()) return false
         val bValue = mBluetoothGatt!!.writeCharacteristic(mReaderStreamOutCharacteristic)
         if (!bValue) writeBleFailure++ else {
            writeBleFailure = 0
            if (true) appendToLogView("BtData: " + byteArrayToString(value))
            _writeCharacteristic_in_progress = true
            mStreamWriteCountOld = mStreamWriteCount
            mStreamWriteCount += value.size.toLong()
            return true
         }
         if (false) {
            appendToLogView("failure in writeCharacteristic(" + byteArrayToString(value) + "), writeBleFailure = " + writeBleFailure + ", onCharacteristicWriteFailue = " + onCharacteristicWriteFailue)
            if (writeBleFailure > 5 || onCharacteristicWriteFailue > 5) {
               appendToLogView("writeBleFailure is too much. start disconnect !!!")
               appendToLog("disconnect C")
               disconnect() //mReaderStreamOutCharacteristic = null;
            }
         }
      }
      return false
   }

   private var streamInRequest = false
   override fun onCharacteristicChanged(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic
   ) {
      super.onCharacteristicChanged(gatt, characteristic)
      if (gatt != mBluetoothGatt) {
         if (DEBUG) {
            val v = characteristic.value
            utility.appendToLogRunnable(
               "onCharacteristicChanged(): INVALID mBluetoothGatt, with address = " + gatt.device.address + ", values =" + byteArrayToString(
                  v
               )
            )
         }
      } else if (characteristic != mReaderStreamInCharacteristic) {
         if (DEBUG) utility.appendToLogRunnable("onCharacteristicChanged(): characteristic is not ReaderSteamIn")
      } else if (mBluetoothConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
         streamInBufferHead = 0
         streamInBufferTail = 0
         streamInBufferSize = 0
      } else {
         val v = characteristic.value
         if (false) utility.appendToLogRunnable(
            "onCharacteristicChanged(): VALID mBluetoothGatt, values =" + byteArrayToString(
               v
            )
         )
         synchronized(arrayListStreamIn) {
            if (v.size != 0) {
               streamInTotalCounter++
            }
            if (streamInBufferReseting) {
               if (DEBUG) utility.appendToLogRunnable("onCharacteristicChanged(): RESET.")
               streamInBufferReseting = false
               streamInBufferSize = 0
               streamInBytesMissing = 0
            }
            if (streamInBufferSize + v.size > streamInBuffer.size) {
               utility.writeDebug2File("A, " + System.currentTimeMillis() + ", Overflow")
               Log.i(TAG, ".Hello: missing data  = " + byteArrayToString(v))
               if (streamInBytesMissing == 0) {
                  streamInOverflowTime = utility.referencedCurrentTimeMs
               }
               streamInBytesMissing += v.size
            } else {
               utility.writeDebug2File("A, " + System.currentTimeMillis())
               if (DEBUG_BTDATA) Log.i(TAG, "BtData = " + byteArrayToString(v))
               if (isStreamInBufferRing) {
                  streamInBufferPush(v, 0, v.size)
               } else {
                  System.arraycopy(v, 0, streamInBuffer, streamInBufferSize, v.size)
               }
               streamInBufferSize += v.size
               streamInAddCounter++
               streamInAddTime = utility.referencedCurrentTimeMs
               if (!streamInRequest) {
                  streamInRequest = true
                  mHandler.removeCallbacks(runnableProcessBleStreamInData)
                  mHandler.post(runnableProcessBleStreamInData)
               }
            }
         }
      }
   }

   private var streamInBufferReseting = false
   fun setStreamInBufferReseting() {
      streamInBufferReseting = true
   }

   open fun processBleStreamInData() {}
   val intervalProcessBleStreamInData = 100 //50;
   val runnableProcessBleStreamInData: Runnable = object : Runnable {
      override fun run() {
         streamInRequest = false
         processBleStreamInData()
         mHandler.postDelayed(this, intervalProcessBleStreamInData.toLong())
      }
   }

   override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
      super.onMtuChanged(gatt, mtu, status)
      Log.i(TAG, "onMtuChanged starts")
      if (gatt != mBluetoothGatt) {
         if (DEBUG) utility.appendToLogRunnable("onMtuChanged: INVALID mBluetoothGatt")
      } else if (status != BluetoothGatt.GATT_SUCCESS) {
         if (DEBUG) utility.appendToLogRunnable("onMtuChanged: status=$status")
      } else {
         if (DEBUG_BTOP) utility.appendToLogRunnable("onMtuChanged: mtu=$mtu")
      }
   }

   override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
      super.onReliableWriteCompleted(gatt, status)
      if (gatt != mBluetoothGatt) {
         if (true) utility.appendToLogRunnable("INVALID mBluetoothGatt")
      } else {
         if (true) utility.appendToLogRunnable("onReliableWriteCompleted(): status=$status")
         //mBluetoothGatt.abortReliableWrite();
      }
   }

   private val mContext: Context
   private val activity: Activity
   private var popupWindow: PopupWindow? = null
   private var bleEnableRequestShown0 = false
   private var bleEnableRequestShown = false
   private var isLocationAccepted = false
   var appdialog: CustomAlertDialog? = null
   var bAlerting = false
   fun scanLeDevice(
      enable: Boolean,
      mLeScanCallback: LeScanCallback?,
      mScanCallBack: ScanCallback?
   ): Boolean {
      val DEBUG = false
      if (DEBUG) appendToLog("StreamOut: enable = $enable")
      var result = false
      val locationReady = true
      if (enable && isBleConnected) return true
      if (!enable && !isBleScanning) return true
      if (enable) {
         val locationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
         if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(
               LocationManager.NETWORK_PROVIDER
            )
         ) isLocationAccepted = false
      }
      if (DEBUG_SCAN) appendToLog("isLocationAccepted = $isLocationAccepted, bAlerting = $bAlerting, bleEnableRequestShown = $bleEnableRequestShown0")
      if (false) {
         if (!bAlerting && !bleEnableRequestShown0) {
            bAlerting = true
            if (DEBUG) appendToLog("StreamOut: new AlertDialog")
            popupAlert()
         }
         return false
      }
      if (DEBUG) appendToLog("StreamOut: Passed AlertDialog")
      if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         if (DEBUG) appendToLog("Checking permission and grant !!!")
         val locationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
         if (DEBUG_SCAN) appendToLog(
            "locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) = " + locationManager.isProviderEnabled(
               LocationManager.GPS_PROVIDER
            )
         )
         if (DEBUG_SCAN) appendToLog(
            "locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) = " + locationManager.isProviderEnabled(
               LocationManager.NETWORK_PROVIDER
            )
         )
         if (DEBUG_SCAN) appendToLog(
            "ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) = " + ActivityCompat.checkSelfPermission(
               activity,
               Manifest.permission.ACCESS_FINE_LOCATION
            )
         )
         if (DEBUG_SCAN) appendToLog(
            "ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)  = " + ActivityCompat.checkSelfPermission(
               activity,
               Manifest.permission.ACCESS_COARSE_LOCATION
            )
         )
         if (false) {
            var isShowing = false
            if (popupWindow != null) isShowing = popupWindow!!.isShowing
            if (!isShowing) {
               if (DEBUG) appendToLog("Setting grant")
            }
            return false
         } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(
               LocationManager.NETWORK_PROVIDER
            ) || ActivityCompat.checkSelfPermission(
               activity,
               Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
               activity,
               Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
         ) {
            if (true) {
               if (bAlerting || bleEnableRequestShown0) return false
               bAlerting = true
               popupAlert()
               return false
            }
         }
      }
      if (!locationReady) {
         if (DEBUG) appendToLog("AccessCoarseLocatin is NOT granted")
      } else if (mBluetoothAdapter == null) {
         if (DEBUG) appendToLog("scanLeDevice($enable) with NULL mBluetoothAdapter")
      } else if (!mBluetoothAdapter!!.isEnabled) {
         if (DEBUG) appendToLog("StreamOut: bleEnableRequestShown = $bleEnableRequestShown")
         if (!bleEnableRequestShown) {
            if (DEBUG) appendToLog("scanLeDevice($enable) with DISABLED mBluetoothAdapter")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, 1)
            if (DEBUG) appendToLog("StreamOut: bleEnableRequestShown is set")
            bleEnableRequestShown = true
            mHandler.postDelayed(mRquestAllowRunnable, 60000)
         }
      } else {
         bleEnableRequestShown = false
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mleScanner = mBluetoothAdapter!!.bluetoothLeScanner
            if (mleScanner == null) {
               if (DEBUG) appendToLog("scanLeDevice($enable) with NULL BluetoothLeScanner")
               return false
            }
         }
         if (!enable) {
            if (DEBUG) appendToLog("abcc scanLeDevice(" + enable + ") with mScanCallBack is " + if (mScanCallBack != null) "VALID" else "INVALID")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               if (mScanCallBack != null) mleScanner!!.stopScan(mScanCallBack)
            } else {
               if (mLeScanCallback != null) mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            }
            isBleScanning = false
            result = true
         } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               if (DEBUG) appendToLog(
                  "scanLeDevice(" + enable + "): START with mleScanner. ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) = " + ActivityCompat.checkSelfPermission(
                     mContext,
                     Manifest.permission.BLUETOOTH_SCAN
                  )
               )
               if (isBLUETOOTH_CONNECTinvalid) return true else mleScanner!!.startScan(mScanCallBack)
            } else {
               if (DEBUG) appendToLog("scanLeDevice($enable): START with mBluetoothAdapter")
               mBluetoothAdapter!!.startLeScan(mLeScanCallback)
            }
            isBleScanning = true
            result = true
         }
      }
      return result
   }

   private val mRquestAllowRunnable = Runnable {
      bleEnableRequestShown0 = false
      bleEnableRequestShown = false
   }

   fun popupAlert() {
      appdialog = CustomAlertDialog()
      appdialog!!.Confirm(mContext as Activity, "Use your location",
         "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
         "No thanks", "Turn on",
         object : Runnable {
            override fun run() {
               isLocationAccepted = true
               appendToLog("StreamOut: This from FALSE proc")
               if (ActivityCompat.checkSelfPermission(
                     activity,
                     Manifest.permission.ACCESS_FINE_LOCATION
                  ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                     activity,
                     Manifest.permission.ACCESS_COARSE_LOCATION
                  ) != PackageManager.PERMISSION_GRANTED
               ) {
                  appendToLog("requestPermissions ACCESS_FINE_LOCATION 123")
                  ActivityCompat.requestPermissions(
                     activity,
                     arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                     123
                  )
               }
               run {
                  val locationManager =
                     mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                  if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                     )
                  ) {
                     appendToLog("StreamOut: start activity ACTION_LOCATION_SOURCE_SETTINGS")
                     val intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                     mContext.startActivity(intent1)
                  }
               }
               bleEnableRequestShown0 = true
               mHandler.postDelayed(mRquestAllowRunnable, 60000)
               bAlerting = false
            }
         }
      ) {
         appendToLog("StreamOut: This from FALSE proc")
         bAlerting = false
         bleEnableRequestShown0 = true
         mHandler.postDelayed(mRquestAllowRunnable, 60000)
      }
   }

   /*
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            appendToLog("action = " + action);
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // CONNECT
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Discover new device
            }
        }
    };
*/
   @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
   open fun connectBle(readerDevice: ReaderDevice?): Boolean {
      val DEBUG = false
      if (DEBUG) appendToLog("abcc: start connecting " + readerDevice!!.name)
      if (readerDevice == null) {
         if (DEBUG) appendToLog("with NULL readerDevice")
      } else {
         val address = readerDevice.address
         if (mBluetoothAdapter == null) {
            if (DEBUG) appendToLog("connectBle[$address] with NULL mBluetoothAdapter")
         } else if (!mBluetoothAdapter!!.isEnabled) {
            if (DEBUG) appendToLog("connectBle[$address] with DISABLED mBluetoothAdapter")
         } else {
            utility.debugFileSetup()
            utility.setReferenceTimeMs()
            if (DEBUG_CONNECT) appendToLog("connectBle[$address]: connectGatt starts")
            mBluetoothConnectionState = -1
            if (!checkSelfPermissionBLUETOOTH()) return false
            mBluetoothGatt  = mBluetoothAdapter!!.getRemoteDevice(address).connectGatt(mContext, false, this)

            if (mBluetoothGatt != null) {
               mBluetoothGattActive = true
               if (false) {
                  if (true) {
                     mBluetoothGatt!!.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                     if (DEBUG) appendToLog("Stream Set to HIGH")
                  } else {
                     mBluetoothGatt!!.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                     if (DEBUG) appendToLog("Stream Set to BALANCED")
                  }
               }
            }

            mBluetoothDevice = readerDevice
            isCharacteristicListRead =
               true //skip in case there is problem in completing reading characteristic features, causing endless reading 0706 and 0C02
            return true
         }
      }
      return false
   }

   open fun disconnect() {
      appendToLog("abcc: start disconnect ")
      if (mBluetoothGatt == null) {
         if (DEBUG) appendToLog("NULL mBluetoothGatt")
      } else {
         utility.debugFileClose()
         mReaderStreamOutCharacteristic = null
         mHandler.removeCallbacks(mDisconnectRunnable)
         mHandler.post(mDisconnectRunnable)
         disconnectRunning = true
         if (DEBUG) appendToLog("abcc done and start mDisconnectRunnable")
      }
   }

   var mBluetoothGattActive = false
   fun forcedDisconnect1(): Boolean {
      mHandler.removeCallbacks(mReadRssiRunnable)
      mHandler.removeCallbacks(mReadCharacteristicRunnable)
      if (mBluetoothGatt != null) {
         if (mBluetoothGattActive) {
            appendToLog("abcc mDisconnectRunnable(): close mBluetoothGatt")
            if (!checkSelfPermissionBLUETOOTH()) return false
            mBluetoothGatt!!.close()
            mBluetoothGattActive = false
         } else {
            appendToLog("abcc mDisconnectRunnable(): Null mBluetoothGatt")
            mBluetoothGatt = null
            return true
         }
      }
      return false
      //mBluetoothConnectionState = -1;
   }

   private var disconnectRunning = false
   var bluetoothDeviceConnectOld: BluetoothDevice? = null
   private val mDisconnectRunnable: Runnable = object : Runnable {
      override fun run() {
         var done = false
         var bGattConnection = -1
         if (!checkSelfPermissionBLUETOOTH()) return
         if (bluetoothDeviceConnectOld != null) bGattConnection =
            mBluetoothManager!!.getConnectionState(bluetoothDeviceConnectOld, BluetoothProfile.GATT)
         if (DEBUG) appendToLog("abcc DisconnectRunnable(): disconnect with mBluetoothConnectionState = $mBluetoothConnectionState, gattConnection = $bGattConnection")
         if (mBluetoothConnectionState < 0) {
            appendToLog("abcc DisconnectRunnable(): start mBluetoothGatt.disconnect")
            mBluetoothGatt!!.disconnect()
            mBluetoothConnectionState = BluetoothProfile.STATE_DISCONNECTED
         } else if (mBluetoothConnectionState != BluetoothProfile.STATE_DISCONNECTED) {
            appendToLog("abcc 2 DisconnectRunnable(): start mBluetoothGatt.disconnect")
            if (checkSelfPermissionBLUETOOTH()) {
               mBluetoothGatt!!.disconnect() //forcedDisconnect(true);
               mBluetoothConnectionState = BluetoothProfile.STATE_DISCONNECTED
            }
         } else if (forcedDisconnect1()) {
            if (DEBUG) appendToLog("abcc mDisconnectRunnable(): END")
            disconnectRunning = false
            if (false) mBluetoothAdapter!!.disable()
            done = true
         }
         if (!done) mHandler.postDelayed(this, 100)
      }
   }
   val isBleBusy: Boolean
      get() = mBluetoothConnectionState != BluetoothProfile.STATE_CONNECTED || _readCharacteristic_in_progress /*|| _writeCharacteristic_in_progress*/

   private fun getCharacteristic(
      service: UUID,
      characteristic: UUID
   ): BluetoothGattCharacteristic? {
      val s = mBluetoothGatt!!.getService(service) ?: return null
      return s.getCharacteristic(characteristic)
   }

   var streamInDataMilliSecond: Long = 0
      private set

   fun readBleSteamIn(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
      var byteOffset = byteOffset
      var byteCount = byteCount
      synchronized(arrayListStreamIn) {
         if (0 == streamInBufferSize) return 0
         if (isArrayListStreamInBuffering) {
            var byteGot = 0
            val length1 = arrayListStreamIn[0].data.size
            if (arrayListStreamIn.size != 0 && buffer.size - byteOffset > length1) {
               System.arraycopy(arrayListStreamIn[0].data, 0, buffer, byteOffset, length1)
               streamInDataMilliSecond = arrayListStreamIn[0].milliseconds
               arrayListStreamIn.removeAt(0)
               byteOffset += length1
               byteGot += length1
            }
            byteCount = byteGot
         } else {
            if (byteCount > streamInBufferSize) byteCount = streamInBufferSize
            if (byteOffset + byteCount > buffer.size) {
               byteCount = buffer.size - byteOffset
            }
            if (byteCount <= 0) return 0
            if (isStreamInBufferRing) {
               streamInBufferPull(buffer, byteOffset, byteCount)
            } else {
               System.arraycopy(streamInBuffer, 0, buffer, byteOffset, byteCount)
               System.arraycopy(
                  streamInBuffer,
                  byteCount,
                  streamInBuffer,
                  0,
                  streamInBufferSize - byteCount
               )
            }
         }
         streamInBufferSize -= byteCount
         return byteCount
      }
   }

   private var totalTemp = 0
   private var totalReceived = 0
   private var firstTime: Long = 0
   private var totalTime: Long = 0
   open val streamInRate: Long
      get() = if (totalTime == 0L || totalReceived == 0) 0 else totalReceived * 1000L / totalTime

   private inner class StreamInData {
      lateinit var data: ByteArray
      var milliseconds: Long = 0
   }

   private val arrayListStreamIn = ArrayList<StreamInData>()
   private val isArrayListStreamInBuffering = true
   private val isStreamInBufferRing = true
   private fun streamInBufferPush(inData: ByteArray, inDataOffset: Int, length: Int) {
      var inDataOffset = inDataOffset
      var length = length
      val length1 = streamInBuffer.size - streamInBufferTail
      var totalCopy = 0
      if (isArrayListStreamInBuffering) {
         val streamInData = StreamInData()
         streamInData.data = inData
         streamInData.milliseconds = System.currentTimeMillis()
         arrayListStreamIn.add(streamInData)
         totalCopy = length
      } else {
         if (length > length1) {
            totalCopy = length1
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length1)
            length -= length1
            inDataOffset += length1
            streamInBufferTail = 0
         }
         if (length != 0) {
            totalCopy += length
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length)
            streamInBufferTail += length
         }
      }
      if (totalCopy != 0) {
         totalTemp += totalCopy
         val timeDifference = System.currentTimeMillis() - firstTime
         if (totalTemp > 17 && timeDifference > 1000) {
            totalReceived = totalTemp
            totalTime = timeDifference
            firstTime = System.currentTimeMillis()
            totalTemp = 0
         }
      }
   }

   private fun streamInBufferPull(buffer: ByteArray, byteOffset: Int, length: Int) {
      var byteOffset = byteOffset
      var length = length
      synchronized(arrayListStreamIn) {
         val length1 = streamInBuffer.size - streamInBufferHead
         if (length > length1) {
            System.arraycopy(streamInBuffer, streamInBufferHead, buffer, byteOffset, length1)
            length -= length1
            byteOffset += length1
            streamInBufferHead = 0
         }
         if (length != 0) {
            System.arraycopy(streamInBuffer, streamInBufferHead, buffer, byteOffset, length)
            streamInBufferHead += length
         }
      }
   }

   val isBLUETOOTH_CONNECTinvalid: Boolean
      get() {
         var bValue = false
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (ActivityCompat.checkSelfPermission(
               mContext,
               Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
               mContext,
               Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED)
         ) {
            appendToLog("requestPermissions BLUETOOTH_CONNECT & BLUETOOTH_CONNECT 123")
            ActivityCompat.requestPermissions(
               (mContext as Activity), arrayOf(
                  Manifest.permission.BLUETOOTH_SCAN,
                  Manifest.permission.BLUETOOTH_CONNECT
               ), 123
            )
            bValue = true
         }
         return bValue
      }
   var utility: Utility

   init {
      val DEBUG = false
      mContext = context
      activity = mContext as Activity
      utility = Utility(context, mLogView)
      DEBUG_PKDATA = utility.DEBUG_PKDATA
      DEBUG_APDATA = utility.DEBUG_APDATA

//        BluetoothConfigManager mConfigManager;
//        mConfigManager = BluetoothConfigManager.getInstance();
//        appendToLog("BluetoothConfigManager.getIoCapability = " + mConfigManager.getIoCapability());
      val mPackageManager = mContext.getPackageManager()
      if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
         mBluetoothManager =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
         mBluetoothAdapter = mBluetoothManager!!.adapter
         if (mBluetoothAdapter != null  && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isBle5 = mBluetoothAdapter!!.isLeCodedPhySupported
            val isAdvertising5 = mBluetoothAdapter!!.isLeExtendedAdvertisingSupported
            if (DEBUG) appendToLog("isBle5 = $isBle5, isAdvertising5 = $isAdvertising5")
         }
      } else {
         mBluetoothAdapter = null
         if (DEBUG) appendToLog("NO BLUETOOTH_LE")
      }
      if (DEBUG) {
         val locationManager =
            mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
         if (ActivityCompat.checkSelfPermission(
               mContext,
               Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
         ) appendToLog("permitted ACCESS_FINE_LOCATION")
         if (ActivityCompat.checkSelfPermission(
               mContext,
               Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
         ) appendToLog("permitted ACCESS_COARSE_LOCATION")
         val stringProviderList = locationManager.allProviders
         for (stringProvider in stringProviderList) appendToLog("Provider = $stringProvider")
         if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) appendToLog("ProviderEnabled GPS_PROVIDER")
         if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) appendToLog("ProviderEnabled NETWORK_PROVIDER")
         if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) appendToLog("ProviderEnabled PASSIVE_PROVIDER")
      }
   }

   fun byteArray2DisplayString(byteData: ByteArray?): String? {
      return utility.byteArray2DisplayString(byteData)
   }

   open fun byteArrayToString(packet: ByteArray?): String? {
      return utility.byteArrayToString(packet)
   }

   fun byteArrayToInt(bytes: ByteArray): Int {
      return utility.byteArrayToInt(bytes)
   }

   open fun appendToLog(s: String?) {
      utility.appendToLog(s)
   }

   open fun appendToLogView(s: String) {
      utility.appendToLogView(s)
   }

   fun writeDebug2File(stringDebug: String?) {
      utility.writeDebug2File(stringDebug)
   }

   fun compareArray(array1: ByteArray?, array2: ByteArray?, length: Int): Boolean {
      return utility.compareByteArray(array1, array2, length)
   }

   fun debugFileEnable(enable: Boolean) {
      utility.debugFileEnable(enable)
   }

   fun getlast3digitVersion(str: String?): String? {
      return utility.getlast3digitVersion(str)
   }

   fun isVersionGreaterEqual(
      version: String?,
      majorVersion: Int,
      minorVersion: Int,
      buildVersion: Int
   ): Boolean {
      return utility.isVersionGreaterEqual(version, majorVersion, minorVersion, buildVersion)
   }

   fun get2BytesOfRssi(bytes: ByteArray, index: Int): Double {
      return utility.get2BytesOfRssi(bytes, index)
   }

   fun getConnectionState(bluetoothDevice: BluetoothDevice?): Int {
      return if (!checkSelfPermissionBLUETOOTH()) -1 else mBluetoothManager!!.getConnectionState(
         bluetoothDevice,
         BluetoothProfile.GATT
      )
   }

   fun discoverServices(): Boolean {
      return if (!checkSelfPermissionBLUETOOTH()) false else mBluetoothGatt!!.discoverServices()
   }

   fun checkSelfPermissionBLUETOOTH(): Boolean {
      var bValue = false
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         if (ActivityCompat.checkSelfPermission(
               mContext,
               Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
         ) bValue = true
      } else if (ActivityCompat.checkSelfPermission(
            mContext.applicationContext,
            Manifest.permission.BLUETOOTH
         ) == PackageManager.PERMISSION_GRANTED
      ) bValue = true
      if (false) Log.i("Hello3", "checkSelfPermissionBLUETOOTH bValue = $bValue")
      return bValue
   }

   companion object {
      const val TAG = "Hello"
   }
}