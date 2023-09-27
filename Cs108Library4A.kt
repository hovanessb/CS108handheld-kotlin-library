package com.incommsys.android.composefolderscanner.cs108library

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.incommsys.android.ComposeFolderScanner.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections

@SuppressLint("MissingPermission")
class Cs108Library4A @Keep constructor(context: Context, mLogView: TextView?) :
   Cs108Connector(
      context, mLogView
   ) {
   private val mHandler = Handler()
   var mLeScanCallback: BluetoothAdapter.LeScanCallback? = null
   var mScanCallback: ScanCallback? = null
   fun getlibraryVersion(): String {
      return BuildConfig.VERSION_NAME
   }

   override fun byteArrayToString(packet: ByteArray?): String? {
      return super.byteArrayToString(packet)
   }

   override fun appendToLog(s: String?) {
      super.appendToLog(s)
   }

   override fun appendToLogView(s: String) {
      super.appendToLogView(s)
   }

   @get:Keep
   override var isBleScanning: Boolean
      get() = super.isBleScanning
      set(isBleScanning) {
         super.isBleScanning = isBleScanning
      }


   var mScanResultList: ArrayList<Cs108ScanData>? = ArrayList()
   @SuppressLint("MissingPermission")
   @Keep
   fun scanLeDevice(enable: Boolean): Boolean {
      val DEBUG = false
      if (enable) mHandler.removeCallbacks(connectRunnable)
      if (DEBUG_SCAN) appendToLog("scanLeDevice[$enable]")
      if (bluetoothDeviceConnectOld != null) if (DEBUG) appendToLog(
         "bluetoothDeviceConnectOld connection state = " + mBluetoothManager!!.getConnectionState(
            bluetoothDeviceConnectOld,
            BluetoothProfile.GATT
         )
      )
      val bValue = super.scanLeDevice(enable, mLeScanCallback, mScanCallback)
      if (DEBUG_SCAN) appendToLog("isScanning = " + isBleScanning)
      return bValue
   }

   var check9800_serviceUUID2p1 = 0
   fun check9800(scanResultA: Cs108ScanData): Boolean {
      var found98 = false
      val DEBUG = false
      if (DEBUG) appendToLog("decoded data size = " + scanResultA.decoded_scanRecord!!.size)
      var iNewADLength = 0
      var newAD = ByteArray(0)
      var iNewADIndex = 0
      check9800_serviceUUID2p1 = -1
      if (isBLUETOOTH_CONNECTinvalid) return true
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
         ) != PackageManager.PERMISSION_GRANTED
      ) return true
      val strTemp = scanResultA.device.name
      if (strTemp != null && DEBUG) appendToLog("Found name = " + strTemp + ", length = " + strTemp.length)
      for (bdata in scanResultA.scanRecord) {
         if (iNewADIndex >= iNewADLength && iNewADLength != 0) {
            scanResultA.decoded_scanRecord!!.add(newAD)
            iNewADIndex = 0
            iNewADLength = 0
            if (DEBUG) appendToLog(
               "Size = " + scanResultA.decoded_scanRecord!!.size + ", " + byteArrayToString(
                  newAD
               )
            )
         }
         if (iNewADLength == 0) {
            iNewADLength = bdata.toInt()
            newAD = ByteArray(iNewADLength)
            iNewADIndex = 0
         } else newAD[iNewADIndex++] = bdata
      }
      if (DEBUG) appendToLog("decoded data size = " + scanResultA.decoded_scanRecord!!.size)
      var i = 0
      while (scanResultA.device.type == BluetoothDevice.DEVICE_TYPE_LE && i < scanResultA.decoded_scanRecord!!.size) {
         val currentAD = scanResultA.decoded_scanRecord!![i]
         if (DEBUG) appendToLog("Processing decoded data = " + byteArrayToString(currentAD))
         if (currentAD[0].toInt() == 2) {
            if (DEBUG) appendToLog("Processing UUIDs")
            if (currentAD[1].toInt() == 0 && currentAD[2] == 0x98.toByte()) {
               if (DEBUG) appendToLog("Found 9800")
               found98 = true
               check9800_serviceUUID2p1 = currentAD[1].toInt()
               if (DEBUG) appendToLog("serviceUD1D2p1 = $check9800_serviceUUID2p1")
               break
            }
         }
         i++
      }
      if (!found98 && DEBUG) appendToLog(
         "No 9800: with scanData = " + byteArrayToString(
            scanResultA.scanRecord
         )
      ) else if (DEBUG_SCAN) appendToLog(
         "Found 9800: with scanData = " + byteArrayToString(
            scanResultA.scanRecord
         )
      )
      return found98
   }

   @get:Keep
   val bluetoothDeviceName: String?
      get() = if (getmBluetoothDevice() == null) null else getmBluetoothDevice()!!.name

   @get:Keep
   val bluetoothDeviceAddress: String?
      get() = if (getmBluetoothDevice() == null) null else getmBluetoothDevice()!!.address
   var bleConnection = false
   var file: File? = null

   @get:Keep
   override val isBleConnected: Boolean
      get() {
         val DEBUG = false
         val bleConnectionNew = super.isBleConnected
         if (bleConnectionNew) {
            if (!bleConnection) {
               bleConnection = bleConnectionNew
               if (DEBUG_CONNECT) appendToLog("Newly connected")
               cs108ConnectorDataInit()
               setRfidOn(true)
               setBarcodeOn(true)
               hostProcessorICGetFirmwareVersion()
               bluetoothICFirmwareVersion
               channelOrderType = -1
               run {
                  this.barcodePreSuffix
                  this.barcodeReadingMode
                  this.barcodeSerial
                  //getBarcodeNoDuplicateReading();
                  //getBarcodeDelayTimeOfEachReading();
                  //getBarcodeEnable2dBarCodes();
                  //getBarcodePrefixOrder();
                  //getBarcodeVersion();
                  //barcodeSendCommandLoadUserDefault();
                  //barcodeSendQuerySystem();
                  setBatteryAutoReport(true) //0xA003
               }
               abortOperation()
               hostProcessorICSerialNumber //0xb004 (but access Oem as bluetooth version is not got)
               macVer
               run {
                  //following two instructions seems not used
                  val iValue =
                     mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.diagnosticConfiguration
                  if (DEBUG) appendToLog("diagnostic data = $iValue")
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(0xC08, 0x100)
                  mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.versionCode
               }
               regionCode = null
               countryCode
               run {
                  mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.freqModifyCode
                  mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.specialCountryVersion
               }
               serialNumber
               if (DEBUG_CONNECT) appendToLog("Start checkVersionRunnable")
               mHandler.postDelayed(checkVersionRunnable, 500)
            } else if (bFirmware_reset_before) {
               bFirmware_reset_before = false
               mHandler.postDelayed(reinitaliseDataRunnable, 500)
            }
         } else if (bleConnection) {
            bleConnection = bleConnectionNew
            if (DEBUG) appendToLog("Newly disconnected")
         }
         return bleConnection
      }
   var bNeedReconnect = false
   var iConnectStateTimer = 0
   val connectRunnable: Runnable = object : Runnable {
      val DEBUG = false
      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun run() {
         if (DEBUG_CONNECT) appendToLog("0 connectRunnable: mBluetoothConnectionState = $mBluetoothConnectionState, bNeedReconnect = $bNeedReconnect")
         if (isBleScanning) {
            if (DEBUG) appendToLog("connectRunnable: still scanning. Stop scanning first")
            scanLeDevice(false)
         } else if (bNeedReconnect) {
            if (mBluetoothGatt != null) {
               if (DEBUG) appendToLog("connectRunnable: mBluetoothGatt is null before connect. disconnect first")
               disconnect()
            } else if (readerDeviceConnect == null) {
               if (DEBUG) appendToLog("connectRunnable: exit with null readerDeviceConnect")
               return
            } else if (mBluetoothGatt == null) {
               if (DEBUG_CONNECT) appendToLog("4 connectRunnable: connect1 starts")
               connect1(null)
               bNeedReconnect = false
            }
         } else if (mBluetoothConnectionState == BluetoothProfile.STATE_DISCONNECTED) { //mReaderStreamOutCharacteristic valid around 1500ms
            iConnectStateTimer = 0
            if (DEBUG) appendToLog("connectRunnable: disconnect as disconnected connectionState is received")
            bNeedReconnect = true
            if (mBluetoothGatt != null) {
               if (DEBUG) appendToLog("disconnect F")
               disconnect()
            }
         } else if (mReaderStreamOutCharacteristic == null) {
            if (DEBUG_CONNECT) appendToLog("6 connectRunnable: wait as not yet discovery, with iConnectStateTimer = $iConnectStateTimer")
            if (++iConnectStateTimer > 10) {
            }
         } else {
            if (DEBUG_CONNECT) appendToLog("7 connectRunnable: end of ConnectRunnable")
            return
         }
         mHandler.postDelayed(this, 500)
      }
   }
   var readerDeviceConnect: ReaderDevice? = null
   @Keep
   fun connect(readerDevice: ReaderDevice?) {
      if (isBleConnected) return
      if (mBluetoothGatt != null) disconnect()
      if (readerDevice != null) readerDeviceConnect = readerDevice
      mHandler.removeCallbacks(connectRunnable)
      bNeedReconnect = true
      mHandler.post(connectRunnable)
      if (DEBUG_CONNECT) appendToLog("Start ConnectRunnable")
   }

   @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
   fun connect1(readerDevice: ReaderDevice?): Boolean {
      var readerDevice = readerDevice
      val DEBUG = false
      if (DEBUG_CONNECT) appendToLog("Connect with NULLreaderDevice = " + (readerDevice == null) + ", NULLreaderDeviceConnect = " + (readerDeviceConnect == null))
      if (readerDevice == null && readerDeviceConnect != null) readerDevice = readerDeviceConnect
      var result = false
      if (readerDevice != null) {
         bNeedReconnect = false
         iConnectStateTimer = 0
         bDiscoverStarted = false
         setServiceUUIDType(readerDevice.serviceUUID2p1)
         result = connectBle(readerDevice)
      }
      if (DEBUG_CONNECT) appendToLog("Result = $result")
      return result
   }

   @Keep
   fun disconnect(tempDisconnect: Boolean) {
      appendToLog("abcc tempDisconnect: getBarcodeOnStatus = " + if (barcodeOnStatus) "on" else "off")
      if (DEBUG) appendToLog("tempDisconnect = $tempDisconnect")
      mHandler.removeCallbacks(checkVersionRunnable)
      mHandler.removeCallbacks(runnableToggleConnection)
      if (barcodeOnStatus) {
         appendToLog("tempDisconnect: setBarcodeOn(false)")
         if (mBarcodeDevice!!.mBarcodeToWrite.size != 0) {
            appendToLog(
               "going to disconnectRunnable with remaining mBarcodeToWrite.size = " + mBarcodeDevice!!.mBarcodeToWrite.size + ", data = " + byteArrayToString(
                  mBarcodeDevice!!.mBarcodeToWrite[0]!!.dataValues
               )
            )
         }
         mBarcodeDevice!!.mBarcodeToWrite.clear()
         setBarcodeOn(false)
      } else appendToLog("tempDisconnect: getBarcodeOnStatus is false")
      mHandler.postDelayed(disconnectRunnable, 100)
      appendToLog("done with tempDisconnect = $tempDisconnect")
      if (!tempDisconnect) {
         mHandler.removeCallbacks(connectRunnable)
         bluetoothDeviceConnectOld =
            mBluetoothAdapter!!.getRemoteDevice(readerDeviceConnect!!.address)
         readerDeviceConnect = null
      }
   }

   override fun disconnect() {
      super.disconnect()
   }

   val disconnectRunnable: Runnable = object : Runnable {
      override fun run() {
         appendToLog("abcc disconnectRunnable with mBarcodeToWrite.size = " + mBarcodeDevice!!.mBarcodeToWrite.size)
         if (mBarcodeDevice!!.mBarcodeToWrite.size != 0) mHandler.postDelayed(this, 100) else {
            appendToLog("disconnect G")
            disconnect()
         }
      }
   }

   fun checkVersion(): String {
      val macVersion = macVer
      val hostVersion = hostProcessorICGetFirmwareVersion()
      val bluetoothVersion = bluetoothICFirmwareVersion
      val strVersionRFID = "2.6.44"
      val strRFIDVersions = strVersionRFID.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
         .toTypedArray()
      val strVersionBT = "1.0.17"
      val strBTVersions = strVersionBT.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
         .toTypedArray()
      val strVersionHost = "1.0.16"
      val strHostVersions = strVersionHost.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
         .toTypedArray()
      var stringPopup = ""
      val icsModel = getcsModel()
      if (!isRfidFailure && !checkHostProcessorVersion(
            macVersion,
            strRFIDVersions[0].trim { it <= ' ' }
               .toInt(),
            strRFIDVersions[1].trim { it <= ' ' }
               .toInt(),
            strRFIDVersions[2].trim { it <= ' ' }.toInt()
         )
      ) stringPopup += "\nRFID processor firmware: V$strVersionRFID"
      if (icsModel == 108) if (!checkHostProcessorVersion(
            hostVersion,
            strHostVersions[0].trim { it <= ' ' }
               .toInt(),
            strHostVersions[1].trim { it <= ' ' }
               .toInt(),
            strHostVersions[2].trim { it <= ' ' }.toInt()
         )
      ) stringPopup += "\nSiliconLab firmware: V$strVersionHost"
      if (icsModel == 108) if (!checkHostProcessorVersion(
            bluetoothVersion,
            strBTVersions[0].trim { it <= ' ' }
               .toInt(),
            strBTVersions[1].trim { it <= ' ' }.toInt(),
            strBTVersions[2].trim { it <= ' ' }
               .toInt())
      ) stringPopup += "\nBluetooth firmware: V$strVersionBT"
      return stringPopup
   }

   override var rssi: Int
      get() = super.rssi
      set(rssi) {
         super.rssi = rssi
      }

   val tagRate: Long  = -1

   fun setRfidOn(onStatus: Boolean): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.turnOn(onStatus)
   }

   @get:Keep
   val isBarcodeFailure: Boolean = mBarcodeDevice!!.barcodeFailure

   val isRfidFailure: Boolean = mRfidDevice!!.rfidFailure

   @Keep
   fun setReaderDefault() {
      setPowerLevel(300)
      setTagGroup(0, 0, 2)
      setPopulation(60)
      setInvAlgoNoSave(true)
      setCurrentLinkProfile(1)
      var string = getmBluetoothDevice()!!.address
      string = string.replace("[^a-zA-Z0-9]".toRegex(), "")
      string = string.substring(string.length - 6)
      setBluetoothICFirmwareName("CS108Reader$string")
   }

   private val reinitaliseDataRunnable: Runnable = object : Runnable {
      override fun run() {
         appendToLog("reset before: reinitaliseDataRunnable starts with inventoring=" + mRfidDevice!!.inventoring + ", mrfidToWriteSize=" + mrfidToWriteSize())
         if (mRfidDevice!!.inventoring || mrfidToWriteSize() != 0) {
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, 500)
         } else {
            if (DEBUG_CONNECT) appendToLog("reinitaliseDataRunnable: Start checkVersionRunnable")
            mHandler.postDelayed(checkVersionRunnable, 500)
         }
      }
   }
   private val checkVersionRunnable: Runnable = object : Runnable {
      val DEBUG = false
      override fun run() {
         if (DEBUG_CONNECT) appendToLog("0 checkVersionRunnable with getFreqChannelConfig = " + mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.freqChannelConfig + ", isBarcodeFailure = " + isBarcodeFailure + ", bBarcodeTriggerMode = " + mBarcodeDevice!!.bBarcodeTriggerMode)
         //if (false && (mRfidDevice.mRfidReaderChip.mRx000Setting.getFreqChannelConfig() < 0 || (isBarcodeFailure() == false && mBarcodeDevice.bBarcodeTriggerMode == (byte)0xFF))) {
         if (mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.freqChannelConfig < 0 || !isBarcodeFailure && mBarcodeDevice!!.bBarcodeTriggerMode == 0xFF.toByte()) {
            if (DEBUG) appendToLog("checkVersionRunnable: RESTART with FreqChannelConfig = " + mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.freqChannelConfig + ", bBarcodeTriggerMode = " + mBarcodeDevice!!.bBarcodeTriggerMode)
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, 500)
         } else {
            sameCheck = false
            if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 1 with BarcodeFailure = " + isBarcodeFailure)
            if (!isBarcodeFailure) {
               if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 2")
               if (!mBarcodeDevice!!.checkPreSuffix(
                     prefixRef,
                     suffixRef
                  )
               ) barcodeSendCommandSetPreSuffix()
               if (mBarcodeDevice!!.bBarcodeTriggerMode.toInt() != 0x30) barcodeSendCommandTrigger()
               autoRFIDAbort
               autoBarStartSTop //setAutoRFIDAbort(false); setAutoBarStartSTop(true);
            }
            if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 3")
            setAntennaCycle(0xffff)
            if (mBluetoothConnector!!.csModel == 463) {
               if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 4")
               setAntennaDwell(2000)
               setAntennaInvCount(0)
            } else if (mBluetoothConnector!!.csModel == 108) {
               if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 5")
               setAntennaDwell(0)
               setAntennaInvCount(0xfffffffeL)
            }
            if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 6")
            //mRfidDevice.mRfidReaderChip.mRx000Setting.setDiagnosticConfiguration(false);
            if (loadSetting1File()) loadSetting1File()
            if (DEBUG) appendToLog("checkVersionRunnable: macVersion  = " + macVer)
            if (checkHostProcessorVersion(macVer, 2, 6, 8)) {
               if (DEBUG) appendToLog("checkVersionRunnable: macVersion >= 2.6.8")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(tagDelay.toInt())
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelaySetting)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(true)
            } else {
               if (DEBUG) appendToLog("checkVersionRunnable: macVersion < 2.6.8")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(
                  tagDelayDefaultNormalSetting.toInt()
               )
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelaySetting)
            }
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setDiagnosticConfiguration(true)
            if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 10")
            sameCheck = true
         }
      }
   }

   fun loadSetting1File(): Boolean {
      val DEBUG = false
      if (DEBUG) appendToLog("start")
      val path = context.filesDir
      var fileName = getmBluetoothDevice()!!.address
      fileName = "cs108A_" + fileName.replace(":".toRegex(), "")
      file = File(path, fileName)
      var bNeedDefault = true
      if (DEBUG) appendToLogView("FileName = " + fileName + ".exits = " + file!!.exists() + ", with beepEnable = " + inventoryBeep)
      if (file!!.exists()) {
         val length = file!!.length().toInt()
         val bytes = ByteArray(length)
         try {
            val instream: InputStream = FileInputStream(file)
            if (instream != null) {
               val inputStreamReader = InputStreamReader(instream)
               val bufferedReader = BufferedReader(inputStreamReader)
               var line: String
               var queryTarget = -1
               var querySession = -1
               val querySelect = -1
               val startQValue = -1
               val maxQValue = -1
               val minQValue = -1
               val retryCount = -1
               val fixedQValue = -1
               val fixedRetryCount = -1
               var population = -1
               var invAlgo = true
               var retry = -1
               preFilterData = PreFilterData()
               while (bufferedReader.readLine().also { line = it } != null) {
                  if (DEBUG) appendToLog("Data read = $line")
                  val dataArray = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                     .toTypedArray()
                  if (dataArray.size == 2) {
                     if (dataArray[0].matches("appVersion".toRegex())) {
                        if (dataArray[1].matches(getlibraryVersion().toRegex())) bNeedDefault =
                           false
                        if (DEBUG) appendToLog("PowerLevel: appVersion data = " + dataArray[1] + ", libraryVersion = " + getlibraryVersion() + ", bNeedDefault = " + bNeedDefault)
                     } else if (bNeedDefault) {
                     } else if (dataArray[0].matches("countryInList".toRegex())) {
                        regionList
                        val countryInListNew = Integer.valueOf(dataArray[1])
                        if (countryNumberInList != countryInListNew && countryInListNew >= 0) setCountryInList(
                           countryInListNew
                        )
                        channelOrderType = -1
                     } else if (dataArray[0].matches("channel".toRegex())) {
                        val channelNew = Integer.valueOf(dataArray[1])
                        if (!channelHoppingStatus && channelNew >= 0) setChannel(channelNew)
                     } else if (dataArray[0].matches("antennaPower".toRegex())) {
                        if (DEBUG) appendToLog("PowerLevel set")
                        val lValue = java.lang.Long.valueOf(dataArray[1])
                        if (lValue >= 0) setPowerLevel(lValue)
                     } else if (dataArray[0].matches("population".toRegex())) {
                        population = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("querySession".toRegex())) {
                        val iValue = Integer.valueOf(dataArray[1])
                        if (iValue >= 0) querySession = iValue
                     } else if (dataArray[0].matches("queryTarget".toRegex())) {
                        queryTarget = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("tagFocus".toRegex())) {
                        val iValue = Integer.valueOf(dataArray[1])
                        if (iValue >= 0) tagFocus = iValue
                     } else if (dataArray[0].matches("fastId".toRegex())) {
                        val iValue = Integer.valueOf(dataArray[1])
                        if (iValue >= 0) fastId = iValue
                     } else if (dataArray[0].matches("invAlgo".toRegex())) {
                        invAlgo = if (dataArray[1].matches("true".toRegex())) true else false
                     } else if (dataArray[0].matches("retry".toRegex())) {
                        retry = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("currentProfile".toRegex())) {
                        val iValue = Integer.valueOf(dataArray[1])
                        if (iValue >= 0) setCurrentLinkProfile(iValue)
                     } else if (dataArray[0].matches("rxGain".toRegex())) {
                        setRxGain(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("deviceName".toRegex())) {
                        mBluetoothConnector!!.mBluetoothIcDevice.deviceName =
                           dataArray[1].toByteArray()
                     } else if (dataArray[0].matches("batteryDisplay".toRegex())) {
                        setBatteryDisplaySetting(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("rssiDisplay".toRegex())) {
                        setRssiDisplaySetting(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("tagDelay".toRegex())) {
                        setTagDelay(java.lang.Byte.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("cycleDelay".toRegex())) {
                        setCycleDelay(java.lang.Long.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("intraPkDelay".toRegex())) {
                        setIntraPkDelay(java.lang.Byte.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("dupDelay".toRegex())) {
                        setDupDelay(java.lang.Byte.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("triggerReporting".toRegex())) {
                        setTriggerReporting(if (dataArray[1].matches("true".toRegex())) true else false)
                     } else if (dataArray[0].matches("triggerReportingCount".toRegex())) {
                        setTriggerReportingCount(dataArray[1].toShort())
                     } else if (dataArray[0].matches("inventoryBeep".toRegex())) {
                        setInventoryBeep(if (dataArray[1].matches("true".toRegex())) true else false)
                     } else if (dataArray[0].matches("inventoryBeepCount".toRegex())) {
                        setBeepCount(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("inventoryVibrate".toRegex())) {
                        setInventoryVibrate(if (dataArray[1].matches("true".toRegex())) true else false)
                     } else if (dataArray[0].matches("inventoryVibrateTime".toRegex())) {
                        setVibrateTime(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("inventoryVibrateMode".toRegex())) {
                        setVibrateModeSetting(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("savingFormat".toRegex())) {
                        setSavingFormatSetting(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("csvColumnSelect".toRegex())) {
                        setCsvColumnSelectSetting(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("inventoryVibrateWindow".toRegex())) {
                        setVibrateWindow(Integer.valueOf(dataArray[1]))
                     } else if (dataArray[0].matches("saveFileEnable".toRegex())) {
                        saveFileEnable = if (dataArray[1].matches("true".toRegex())) true else false
                     } else if (dataArray[0].matches("saveCloudEnable".toRegex())) {
                        saveCloudEnable =
                           if (dataArray[1].matches("true".toRegex())) true else false
                     } else if (dataArray[0].matches("saveNewCloudEnable".toRegex())) {
                        saveNewCloudEnable =
                           if (dataArray[1].matches("true".toRegex())) true else false
                     } else if (dataArray[0].matches("saveAllCloudEnable".toRegex())) {
                        saveAllCloudEnable =
                           if (dataArray[1].matches("true".toRegex())) true else false
                     } else if (dataArray[0].matches("serverLocation".toRegex())) {
                        serverLocation = dataArray[1]
                     } else if (dataArray[0].matches("serverTimeout".toRegex())) {
                        serverTimeout = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("barcode2TriggerMode".toRegex())) {
                        barcode2TriggerMode = dataArray[1].matches("true".toRegex())
                        /*
                            } else if (dataArray[0].matches("wedgePrefix")) {
                                setWedgePrefix(dataArray[1]);
                            } else if (dataArray[0].matches("wedgeSuffix")) {
                                setWedgeSuffix(dataArray[1]);
                            } else if (dataArray[0].matches("wedgeDelimiter")) {
                                setWedgeDelimiter(Integer.valueOf(dataArray[1]));
*/
                     } else if (dataArray[0].matches("preFilterData.enable".toRegex())) {
                        preFilterData!!.enable = dataArray[1].matches("true".toRegex())
                     } else if (dataArray[0].matches("preFilterData.target".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.target = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("preFilterData.action".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.action = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("preFilterData.bank".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.bank = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("preFilterData.offset".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.offset = Integer.valueOf(dataArray[1])
                     } else if (dataArray[0].matches("preFilterData.mask".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.mask = dataArray[1]
                     } else if (dataArray[0].matches("preFilterData.maskbit".toRegex())) {
                        if (preFilterData == null) preFilterData = PreFilterData()
                        preFilterData!!.maskbit = dataArray[1].matches("true".toRegex())
                     } else if (dataArray[0].matches("userDebugEnable".toRegex())) {
                        mBluetoothConnector!!.userDebugEnable =
                           if (dataArray[1].matches("true".toRegex())) true else false
                     }
                  }
               }
               setInvAlgo(invAlgo)
               setPopulation(population)
               setRetryCount(retry)
               setTagGroup(querySelect, querySession, queryTarget)
               setTagFocus(if (tagFocus > 0) true else false)
               if (DEBUG) appendToLog("Going to setSelectCriteria with preFilterData.enable = " + if (preFilterData == null) "NULL" else preFilterData!!.enable)
               if (preFilterData != null && preFilterData!!.enable) setSelectCriteria(
                  0,
                  preFilterData!!.enable,
                  preFilterData!!.target,
                  preFilterData!!.action,
                  preFilterData!!.bank,
                  preFilterData!!.offset,
                  preFilterData!!.mask,
                  preFilterData!!.maskbit
               ) else {
                  if (DEBUG) appendToLog("Going to setSelectCriteriaDisable")
                  setSelectCriteriaDisable(0)
               }
            }
            instream.close()
            if (DEBUG) appendToLog("Data is read from FILE.")
         } catch (ex: Exception) {
            //
         }
      }
      if (bNeedDefault) {
         if (DEBUG) appendToLog("saveSetting2File default")
         setReaderDefault()
         saveSetting2File()
      }
      return bNeedDefault
   }

   @Keep
   fun saveSetting2File() {
      appendToLog("Start")
      val stream: FileOutputStream
      try {
         stream = FileOutputStream(file)
         stream.write("Start of data\n".toByteArray())
         var outData = """
              appVersion,${getlibraryVersion()}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              countryInList,${countryNumberInList}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         if (!channelHoppingStatus) outData = """
    channel,${channel}
    
    """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              antennaPower,${mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAntennaPower(0)}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              population,${population}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              querySession,${querySession}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              queryTarget,${queryTarget}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              tagFocus,${tagFocus}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              fastId,${fastId}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              invAlgo,${invAlgo}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              retry,${retryCount}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              currentProfile,${currentProfile}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              rxGain,${rxGain}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              deviceName,${bluetoothICFirmwareName}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              batteryDisplay,${batteryDisplaySetting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              rssiDisplay,${rssiDisplaySetting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              tagDelay,${tagDelay}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              cycleDelay,${cycleDelay}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              intraPkDelay,${intraPkDelay}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              dupDelay,${dupDelay}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              triggerReporting,${triggerReporting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              triggerReportingCount,${triggerReportingCount}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryBeep,${inventoryBeep}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryBeepCount,${beepCount}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryVibrate,${inventoryVibrate}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryVibrateTime,${vibrateTime}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryVibrateMode,${vibrateModeSetting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              inventoryVibrateWindow,${vibrateWindow}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              savingFormat,${savingFormatSetting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              csvColumnSelect,${csvColumnSelectSetting}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              saveFileEnable,${saveFileEnable}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              saveCloudEnable,${saveCloudEnable}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              saveNewCloudEnable,${saveNewCloudEnable}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              saveAllCloudEnable,${saveAllCloudEnable}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              serverLocation,${serverLocation}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = """
              serverTimeout,${serverTimeout}
              
              """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         outData = "barcode2TriggerMode,$barcode2TriggerMode\n"
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         /*
            outData = "wedgePrefix," + getWedgePrefix() + "\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "wedgeSuffix," + getWedgeSuffix() + "\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "wedgeDelimiter," + String.valueOf(getWedgeDelimiter() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
*/outData = """
   userDebugEnable,${userDebugEnable}
   
   """.trimIndent()
         stream.write(outData.toByteArray())
         appendToLog("outData = $outData")
         if (preFilterData != null) {
            outData = """
                  preFilterData.enable,${preFilterData!!.enable}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.target,${preFilterData!!.target}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.action,${preFilterData!!.action}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.bank,${preFilterData!!.bank}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.offset,${preFilterData!!.offset}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.mask,${preFilterData!!.mask}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
            outData = """
                  preFilterData.maskbit,${preFilterData!!.maskbit}
                  
                  """.trimIndent()
            stream.write(outData.toByteArray())
            appendToLog("outData = $outData")
         }
         stream.write("End of data\n".toByteArray())
         appendToLog("outData = $outData")
         stream.close()
      } catch (ex: Exception) {
         //
      }
   }

   val macVer: String? = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.macVer

   fun getcsModel(): Int {
      return mBluetoothConnector!!.csModel
   }

   val antennaCycle: Int
      //Configuration Calls: RFID
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.antennaCycle

   fun setAntennaCycle(antennaCycle: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaCycle(antennaCycle)
   }

   fun setAntennaInvCount(antennaInvCount: Long): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaInvCount(antennaInvCount)
   }

   val portNumber: Int
      get() = if (mBluetoothConnector!!.csModel == 463) 4 else 1
   val antennaSelect: Int
      get() {
         var iValue =  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.antennaSelect
         appendToLog("AntennaSelect = $iValue")
         return iValue
      }

   val antennaEnable: Boolean
      get() {
         val iValue: Int
         iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.antennaEnable
         appendToLog("AntennaEnable = $iValue")
         return iValue > 0
      }

   fun setAntennaEnable(enable: Boolean): Boolean {
      var iEnable = 0
      if (enable) iEnable = 1
      var bValue = false
      bValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaEnable(iEnable)
      appendToLog("AntennaEnable = $iEnable returning $bValue")
      return bValue
   }

   val antennaDwell: Long
      get() {
         var lValue: Long = 0
         lValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.antennaDwell
         appendToLog("AntennaDwell = $lValue")
         return lValue
      }

   fun setAntennaDwell(antennaDwell: Long): Boolean {
      var bValue = false
      bValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaDwell(antennaDwell)
      if (false) appendToLog("AntennaDwell = $antennaDwell returning $bValue")
      return bValue
   }

   @get:Keep
   val pwrlevel: Long
      get() {
         var lValue: Long = 0
         lValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAntennaPower(-1)
         return lValue
      }
   var pwrlevelSetting: Long = 0
   fun setPowerLevel(pwrlevel: Long): Boolean {
      pwrlevelSetting = pwrlevel
      var bValue = false
      bValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaPower(pwrlevel)
      if (false) appendToLog("PowerLevel = $pwrlevel returning $bValue")
      return bValue
   }

   fun setOnlyPowerLevel(pwrlevel: Long): Boolean {
      appendToLog("start")
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaPower(pwrlevel)
   }

   @get:Keep
   val queryTarget: Int
      get() {
         var iValue: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoAbFlip(1)
         return if (iValue > 0) 2 else {
            iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.queryTarget
            if (iValue > 0) 1 else 0
         }
      }

   @get:Keep
   val querySession: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.querySession

   @get:Keep
   val querySelect: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.querySelect

   @Keep
   fun setTagGroup(sL: Int, session: Int, target1: Int): Boolean {
      if (false) appendToLog("Hello6: invAlgo = " + mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo)
      if (false) appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = " + mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo)
      mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoSelect(mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo) //Must not delete this line
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQueryTarget(target1, session, sL)
   }

   var tagFocus = -1
      get(): Int {
         field = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.impinjExtension
         if (field > 0) {
            field = field and 0x10 shr 4
         }
         return field
      }

   fun setTagFocus(tagFocusNew: Boolean): Boolean {
      val bRetValue: Boolean
      bRetValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setImpinjExtension(
         tagFocusNew,
         if (fastId > 0) true else false
      )
      if (bRetValue) tagFocus = if (tagFocusNew) 1 else 0
      return bRetValue
   }

   var fastId = -1
    get(): Int {
       field = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.impinjExtension
      if (field > 0) field = field and 0x20 shr 5
      return field
   }

   fun setFastId(fastIdNew: Boolean): Boolean {
      val bRetValue: Boolean
      bRetValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setImpinjExtension(
         if (tagFocus > 0) true else false,
         fastIdNew
      )
      if (bRetValue) fastId = if (fastIdNew) 1 else 0
      return bRetValue
   }

   var invAlgoSetting = true

   @get:Keep
   var invAlgo: Boolean = invAlgoSetting

   @Keep
   fun setInvAlgo(dynamicAlgo: Boolean): Boolean {
      invAlgoSetting = dynamicAlgo
      if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = $dynamicAlgo")
      return setInvAlgo1(dynamicAlgo)
   }

   fun setInvAlgoNoSave(dynamicAlgo: Boolean): Boolean {
      appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = $dynamicAlgo")
      return setInvAlgo1(dynamicAlgo)
   }

   val invAlgo1: Boolean
      get() {
         val iValue: Int
         iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo
         return if (iValue < 0) {
            true
         } else {
            if (iValue != 0) true else false
         }
      }

   fun setInvAlgo1(dynamicAlgo: Boolean): Boolean {
      var bValue = true
      val DEBUG = false
      val iAlgo = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo
      val iRetry = retryCount
      val iAbFlip = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.algoAbFlip
      if (DEBUG) appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = $dynamicAlgo, iAlgo = $iAlgo, iRetry = $iRetry, iabFlip = $iAbFlip")
      if (dynamicAlgo && iAlgo == 0 || !dynamicAlgo && iAlgo == 3) {
         bValue =
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvAlgo(if (dynamicAlgo) 3 else 0)
         if (DEBUG) appendToLog("After setInvAlgo, bValue = $bValue")
         if (bValue) bValue = setPopulation(population)
         if (DEBUG) appendToLog("After setPopulation, bValue = $bValue")
         if (bValue) bValue = setRetryCount(iRetry)
         if (DEBUG) appendToLog("After setRetryCount, bValue = $bValue")
         if (bValue) bValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoAbFlip(iAbFlip)
         if (DEBUG) appendToLog("After setAlgoAbFlip, bValue = $bValue")
      }
      return bValue
   }

   @get:Keep
   val currentProfile: Int
      get() {
         val iValue: Int
         iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.currentProfile
         return iValue
      }

   @Keep
   fun setCurrentLinkProfile(profile: Int): Boolean {
      if (profile == currentProfile) return true
      var result: Boolean
      result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCurrentProfile(profile)
      if (result) {
         mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
         result =
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_UPDATELINKPROFILE)
      }
      if (result && profile == 3) {
         appendToLog("It is profile3")
         if (tagDelay < 2) result = setTagDelay(2.toByte())
      }
      return result
   }

   fun resetEnvironmentalRSSI() {
      mRfidDevice!!.mRfidReaderChip!!.mRx000EngSetting.resetRSSI()
   }

   val environmentalRSSI: String?
      get() {
         appendToLog("Hello123: getEnvironmentalRSSI")
         mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
         val iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000EngSetting.getwideRSSI()
         if (iValue < 0) return null
         if (iValue > 255) return "Invalid data"
         val dValue = mRfidDevice!!.mRfidReaderChip!!.decodeNarrowBandRSSI(iValue.toByte())
         return String.format("%.2f dB", dValue)
      }
   val highCompression: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.highCompression
   val rflnaGain: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.rflnaGain
   val iflnaGain: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.iflnaGain
   val agcGain: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.agcGain
   val rxGain: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.rxGain

   fun setRxGain(highCompression: Int, rflnagain: Int, iflnagain: Int, agcgain: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.setRxGain(
         highCompression,
         rflnagain,
         iflnagain,
         agcgain
      )
   }

   fun setRxGain(rxGain: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000MbpSetting.setRxGain(rxGain)
   }

   fun starAuthOperation(): Boolean {
      mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
      return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_18K6CAUTHENTICATE)
   }

   private val FCC_CHN_CNT = 50
   private val FCCTableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25, 904.75, 905.25, 905.75, 906.25, 906.75, 907.25,  //10
      907.75, 908.25, 908.75, 909.25, 909.75, 910.25, 910.75, 911.25, 911.75, 912.25,  //20
      912.75, 913.25, 913.75, 914.25, 914.75, 915.25, 915.75, 916.25, 916.75, 917.25,
      917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
      922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25
   )
   private val FCCTableOfFreq0 = doubleArrayOf(
      903.75, 912.25, 907.75, 910.25, 922.75, 923.25, 923.75, 915.25, 909.25, 912.75,
      910.75, 913.75, 909.75, 905.25, 911.75, 902.75, 914.25, 918.25, 926.25, 925.75,
      920.75, 920.25, 907.25, 914.75, 919.75, 922.25, 903.25, 906.25, 905.75, 926.75,
      924.25, 904.75, 925.25, 924.75, 919.25, 916.75, 911.25, 921.25, 908.25, 908.75,
      913.25, 916.25, 904.25, 906.75, 917.75, 921.75, 917.25, 927.25, 918.75, 915.75
   )
   private var fccFreqSortedIdx0: IntArray = IntArray(50)
   private val FCCTableOfFreq1 = doubleArrayOf(
      915.25, 920.75, 909.25, 912.25, 918.25, 920.25, 909.75, 910.25, 919.75, 922.75,
      908.75, 913.75, 903.75, 919.25, 922.25, 907.75, 911.75, 923.75, 916.75, 926.25,
      908.25, 912.75, 924.25, 916.25, 927.25, 907.25, 910.75, 903.25, 917.75, 926.75,
      905.25, 911.25, 924.75, 917.25, 925.75, 906.75, 914.25, 904.75, 918.75, 923.25,
      902.75, 914.75, 905.75, 915.75, 925.25, 906.25, 921.25, 913.25, 921.75, 904.25
   )
   private var fccFreqSortedIdx1: IntArray = IntArray(50)
   private val fccFreqTable = intArrayOf(
      0x00180E4F,  /*915.75 MHz   */
      0x00180E4D,  /*915.25 MHz   */
      0x00180E1D,  /*903.25 MHz   */
      0x00180E7B,  /*926.75 MHz   */
      0x00180E79,  /*926.25 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E7D,  /*927.25 MHz   */
      0x00180E61,  /*920.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */
      0x00180E35,  /*909.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E57,  /*917.75 MHz   */
      0x00180E25,  /*905.25 MHz   */
      0x00180E23,  /*904.75 MHz   */
      0x00180E75,  /*925.25 MHz   */
      0x00180E67,  /*921.75 MHz   */
      0x00180E4B,  /*914.75 MHz   */
      0x00180E2B,  /*906.75 MHz   */
      0x00180E47,  /*913.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E3D,  /*911.25 MHz   */
      0x00180E3F,  /*911.75 MHz   */
      0x00180E1F,  /*903.75 MHz   */
      0x00180E33,  /*908.75 MHz   */
      0x00180E27,  /*905.75 MHz   */
      0x00180E41,  /*912.25 MHz   */
      0x00180E29,  /*906.25 MHz   */
      0x00180E55,  /*917.25 MHz   */
      0x00180E49,  /*914.25 MHz   */
      0x00180E2D,  /*907.25 MHz   */
      0x00180E59,  /*918.25 MHz   */
      0x00180E51,  /*916.25 MHz   */
      0x00180E39,  /*910.25 MHz   */
      0x00180E3B,  /*910.75 MHz   */
      0x00180E2F,  /*907.75 MHz   */
      0x00180E73,  /*924.75 MHz   */
      0x00180E37,  /*909.75 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E53,  /*916.75 MHz   */
      0x00180E45,  /*913.25 MHz   */
      0x00180E6F,  /*923.75 MHz   */
      0x00180E31,  /*908.25 MHz   */
      0x00180E77,  /*925.75 MHz   */
      0x00180E43,  /*912.75 MHz   */
      0x00180E71,  /*924.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63,  /*920.75 MHz   */
      0x00180E6B,  /*922.75 MHz   */
      0x00180E1B,  /*902.75 MHz   */
      0x00180E6D
   )
   private val fccFreqTableIdx: IntArray
   private val fccFreqSortedIdx = intArrayOf(
      26, 25, 1, 48, 47,
      3, 49, 35, 33, 13,
      32, 30, 5, 4, 45,
      38, 24, 8, 22, 39,
      17, 18, 2, 12, 6,
      19, 7, 29, 23, 9,
      31, 27, 15, 16, 10,
      44, 14, 34, 28, 21,
      42, 11, 46, 20, 43,
      37, 36, 40, 0, 41
   )
   private val AUS_CHN_CNT = 10
   private val AUSTableOfFreq = doubleArrayOf(
      920.75, 921.25, 921.75, 922.25, 922.75,
      923.25, 923.75, 924.25, 924.75, 925.25
   )
   private val AusFreqTable = intArrayOf(
      0x00180E63,  /* 920.75MHz   */
      0x00180E69,  /* 922.25MHz   */
      0x00180E6F,  /* 923.75MHz   */
      0x00180E73,  /* 924.75MHz   */
      0x00180E65,  /* 921.25MHz   */
      0x00180E6B,  /* 922.75MHz   */
      0x00180E71,  /* 924.25MHz   */
      0x00180E75,  /* 925.25MHz   */
      0x00180E67,  /* 921.75MHz   */
      0x00180E6D
   )
   private val ausFreqSortedIdx = intArrayOf(
      0, 3, 6, 8, 1,
      4, 7, 9, 2, 5
   )
   private val PRTableOfFreq = doubleArrayOf(
      915.25, 915.75, 916.25, 916.75, 917.25,
      917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
      922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25
   )
   private val freqTable: IntArray? = null
   private var freqSortedIdx: IntArray? = null
   private val VZ_CHN_CNT = 10
   private val VZTableOfFreq = doubleArrayOf(
      922.75, 923.25, 923.75, 924.25, 924.75,
      925.25, 925.75, 926.25, 926.75, 927.25
   )
   private val vzFreqTable = intArrayOf(
      0x00180E77,  /* 925.75 MHz   */
      0x00180E6B,  /* 922.75MHz   */
      0x00180E7D,  /* 927.25 MHz   */
      0x00180E75,  /* 925.25MHz   */
      0x00180E6D,  /* 923.25MHz   */
      0x00180E7B,  /* 926.75 MHz   */
      0x00180E73,  /* 924.75MHz   */
      0x00180E6F,  /* 923.75MHz   */
      0x00180E79,  /* 926.25 MHz   */
      0x00180E71
   )
   private val vzFreqSortedIdx = intArrayOf(
      6, 0, 9, 5, 1,
      8, 4, 2, 7, 3
   )
   private val BR1_CHN_CNT = 24
   private val BR1TableOfFreq = doubleArrayOf( /*902.75, 903.25, 903.75, 904.25, 904.75,
            905.25, 905.75, 906.25, 906.75, 907.25,
            907.75, 908.25, 908.75, 909.25, 909.75,
            910.25, 910.75, 911.25, 911.75, 912.25,
            912.75, 913.25, 913.75, 914.25, 914.75,
            915.25,*/
      915.75, 916.25, 916.75, 917.25, 917.75,
      918.25, 918.75, 919.25, 919.75, 920.25,
      920.75, 921.25, 921.75, 922.25, 922.75,
      923.25, 923.75, 924.25, 924.75, 925.25,
      925.75, 926.25, 926.75, 927.25
   )
   private val br1FreqTable = intArrayOf(
      0x00180E4F,  /*915.75 MHz   */ //0x00180E4D, /*915.25 MHz   */
      //0x00180E1D, /*903.25 MHz   */
      0x00180E7B,  /*926.75 MHz   */
      0x00180E79,  /*926.25 MHz   */ //0x00180E21, /*904.25 MHz   */
      0x00180E7D,  /*927.25 MHz   */
      0x00180E61,  /*920.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */ //0x00180E35, /*909.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E57,  /*917.75 MHz   */ //0x00180E25, /*905.25 MHz   */
      //0x00180E23, /*904.75 MHz   */
      0x00180E75,  /*925.25 MHz   */
      0x00180E67,  /*921.75 MHz   */ //0x00180E4B, /*914.75 MHz   */
      //0x00180E2B, /*906.75 MHz   */
      //0x00180E47, /*913.75 MHz   */
      0x00180E69,  /*922.25 MHz   */ //0x00180E3D, /*911.25 MHz   */
      //0x00180E3F, /*911.75 MHz   */
      //0x00180E1F, /*903.75 MHz   */
      //0x00180E33, /*908.75 MHz   */
      //0x00180E27, /*905.75 MHz   */
      //0x00180E41, /*912.25 MHz   */
      //0x00180E29, /*906.25 MHz   */
      0x00180E55,  /*917.25 MHz   */ //0x00180E49, /*914.25 MHz   */
      //0x00180E2D, /*907.25 MHz   */
      0x00180E59,  /*918.25 MHz   */
      0x00180E51,  /*916.25 MHz   */ //0x00180E39, /*910.25 MHz   */
      //0x00180E3B, /*910.75 MHz   */
      //0x00180E2F, /*907.75 MHz   */
      0x00180E73,  /*924.75 MHz   */ //0x00180E37, /*909.75 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E53,  /*916.75 MHz   */ //0x00180E45, /*913.25 MHz   */
      0x00180E6F,  /*923.75 MHz   */ //0x00180E31, /*908.25 MHz   */
      0x00180E77,  /*925.75 MHz   */ //0x00180E43, /*912.75 MHz   */
      0x00180E71,  /*924.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63,  /*920.75 MHz   */
      0x00180E6B,  /*922.75 MHz   */ //0x00180E1B, /*902.75 MHz   */
      0x00180E6D
   )
   private val br1FreqSortedIdx = intArrayOf(
      0, 22, 21, 23, 9,
      7, 6, 4, 19, 12,
      13, 3, 5, 1, 18,
      8, 2, 16, 20, 17,
      11, 10, 14, 15
   )
   private val BR2_CHN_CNT = 33
   private val BR2TableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25, 904.75,
      905.25, 905.75, 906.25, 906.75,  /*907.25, 907.75, 908.25, 908.75, 909.25,
            909.75, 910.25, 910.75, 911.25, 911.75,
            912.25, 912.75, 913.25, 913.75, 914.25,
            914.75, 915.25,*/
      915.75, 916.25, 916.75, 917.25, 917.75,
      918.25, 918.75, 919.25, 919.75, 920.25,
      920.75, 921.25, 921.75, 922.25, 922.75,
      923.25, 923.75, 924.25, 924.75, 925.25,
      925.75, 926.25, 926.75, 927.25
   )
   private val br2FreqTable = intArrayOf(
      0x00180E4F,  /*915.75 MHz   */ //0x00180E4D, /*915.25 MHz   */
      0x00180E1D,  /*903.25 MHz   */
      0x00180E7B,  /*926.75 MHz   */
      0x00180E79,  /*926.25 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E7D,  /*927.25 MHz   */
      0x00180E61,  /*920.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */ //0x00180E35, /*909.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E57,  /*917.75 MHz   */
      0x00180E25,  /*905.25 MHz   */
      0x00180E23,  /*904.75 MHz   */
      0x00180E75,  /*925.25 MHz   */
      0x00180E67,  /*921.75 MHz   */ //0x00180E4B, /*914.75 MHz   */
      0x00180E2B,  /*906.75 MHz   */ //0x00180E47, /*913.75 MHz   */
      0x00180E69,  /*922.25 MHz   */ //0x00180E3D, /*911.25 MHz   */
      //0x00180E3F, /*911.75 MHz   */
      0x00180E1F,  /*903.75 MHz   */ //0x00180E33, /*908.75 MHz   */
      0x00180E27,  /*905.75 MHz   */ //0x00180E41, /*912.25 MHz   */
      0x00180E29,  /*906.25 MHz   */
      0x00180E55,  /*917.25 MHz   */ //0x00180E49, /*914.25 MHz   */
      //0x00180E2D, /*907.25 MHz   */
      0x00180E59,  /*918.25 MHz   */
      0x00180E51,  /*916.25 MHz   */ //0x00180E39, /*910.25 MHz   */
      //0x00180E3B, /*910.75 MHz   */
      //0x00180E2F, /*907.75 MHz   */
      0x00180E73,  /*924.75 MHz   */ //0x00180E37, /*909.75 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E53,  /*916.75 MHz   */ //0x00180E45, /*913.25 MHz   */
      0x00180E6F,  /*923.75 MHz   */ //0x00180E31, /*908.25 MHz   */
      0x00180E77,  /*925.75 MHz   */ //0x00180E43, /*912.75 MHz   */
      0x00180E71,  /*924.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63,  /*920.75 MHz   */
      0x00180E6B,  /*922.75 MHz   */
      0x00180E1B,  /*902.75 MHz   */
      0x00180E6D
   )
   private val br2FreqSortedIdx = intArrayOf(
      9, 1, 31, 30, 3,
      32, 18, 16, 15, 13,
      5, 4, 28, 21, 8,
      22, 2, 6, 7, 12,
      14, 10, 27, 17, 11,
      25, 29, 26, 20, 19,
      23, 0, 24
   )
   private val BR3_CHN_CNT = 9
   private val BR3TableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25, 904.75,  // 4
      905.25, 905.75, 906.25, 906.75
   )
   private val br3FreqTable = intArrayOf(
      0x00180E1D,  /*903.25 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E25,  /*905.25 MHz   */
      0x00180E23,  /*904.75 MHz   */
      0x00180E2B,  /*906.75 MHz   */
      0x00180E1F,  /*903.75 MHz   */
      0x00180E27,  /*905.75 MHz   */
      0x00180E29,  /*906.25 MHz   */
      0x00180E1B
   )
   private val br3FreqSortedIdx = intArrayOf(
      1, 3, 5, 4, 8,
      2, 6, 7, 0
   )
   private val BR4_CHN_CNT = 4
   private val BR4TableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25
   )
   private val br4FreqTable = intArrayOf(
      0x00180E1D,  /*903.25 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E1F,  /*903.75 MHz   */
      0x00180E1B
   )
   private val br4FreqSortedIdx = intArrayOf(
      1, 3, 2, 0
   )
   private val BR5_CHN_CNT = 14
   private val BR5TableOfFreq = doubleArrayOf(
      917.75, 918.25, 918.75, 919.25, 919.75,  // 4
      920.25, 920.75, 921.25, 921.75, 922.25,  // 9
      922.75, 923.25, 923.75, 924.25
   )
   private val br5FreqTable = intArrayOf(
      0x00180E61,  /*920.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E57,  /*917.75 MHz   */
      0x00180E67,  /*921.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E59,  /*918.25 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E6F,  /*923.75 MHz   */
      0x00180E71,  /*924.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63,  /*920.75 MHz   */
      0x00180E6B,  /*922.75 MHz   */
      0x00180E6D
   )
   private val br5FreqSortedIdx = intArrayOf(
      5, 3, 2, 0, 8,
      9, 1, 4, 12, 13,
      7, 6, 10, 11
   )
   private val HK_CHN_CNT = 8
   private val HKTableOfFreq = doubleArrayOf(
      920.75, 921.25, 921.75, 922.25, 922.75,
      923.25, 923.75, 924.25
   )
   private val hkFreqTable = intArrayOf(
      0x00180E63,  /*920.75MHz   */
      0x00180E69,  /*922.25MHz   */
      0x00180E71,  /*924.25MHz   */
      0x00180E65,  /*921.25MHz   */
      0x00180E6B,  /*922.75MHz   */
      0x00180E6D,  /*923.25MHz   */
      0x00180E6F,  /*923.75MHz   */
      0x00180E67
   )
   private val hkFreqSortedIdx = intArrayOf(
      0, 3, 7, 1, 4,
      5, 6, 2
   )
   private val BD_CHN_CNT = 4
   private val BDTableOfFreq = doubleArrayOf(
      925.25, 925.75, 926.25, 926.75
   )
   private val bdFreqTable = intArrayOf(
      0x00180E75,  /*925.25MHz   */
      0x00180E77,  /*925.75MHz   */
      0x00180E79,  /*926.25MHz   */
      0x00180E7B
   )
   private val bdFreqSortedIdx = intArrayOf(
      0, 3, 1, 2
   )
   private val TW_CHN_CNT = 12
   private val TWTableOfFreq = doubleArrayOf(
      922.25, 922.75, 923.25, 923.75, 924.25,
      924.75, 925.25, 925.75, 926.25, 926.75,
      927.25, 927.75
   )
   private val twFreqTable = intArrayOf(
      0x00180E7D,  /*927.25MHz   10*/
      0x00180E73,  /*924.75MHz   5*/
      0x00180E6B,  /*922.75MHz   1*/
      0x00180E75,  /*925.25MHz   6*/
      0x00180E7F,  /*927.75MHz   11*/
      0x00180E71,  /*924.25MHz   4*/
      0x00180E79,  /*926.25MHz   8*/
      0x00180E6D,  /*923.25MHz   2*/
      0x00180E7B,  /*926.75MHz   9*/
      0x00180E69,  /*922.25MHz   0*/
      0x00180E77,  /*925.75MHz   7*/
      0x00180E6F
   )
   private val twFreqSortedIdx = intArrayOf(
      10, 5, 1, 6, 11,
      4, 8, 2, 9, 0,
      7, 3
   )
   private val MYS_CHN_CNT = 8
   private val MYSTableOfFreq = doubleArrayOf(
      919.75, 920.25, 920.75, 921.25, 921.75,
      922.25, 922.75, 923.25
   )
   private val mysFreqTable = intArrayOf(
      0x00180E5F,  /*919.75MHz   */
      0x00180E65,  /*921.25MHz   */
      0x00180E6B,  /*922.75MHz   */
      0x00180E61,  /*920.25MHz   */
      0x00180E67,  /*921.75MHz   */
      0x00180E6D,  /*923.25MHz   */
      0x00180E63,  /*920.75MHz   */
      0x00180E69
   )
   private val mysFreqSortedIdx = intArrayOf(
      0, 3, 6, 1, 4,
      7, 2, 5
   )
   private val ZA_CHN_CNT = 16
   private val ZATableOfFreq = doubleArrayOf(
      915.7, 915.9, 916.1, 916.3, 916.5,
      916.7, 916.9, 917.1, 917.3, 917.5,
      917.7, 917.9, 918.1, 918.3, 918.5,
      918.7
   )
   private val zaFreqTable = intArrayOf(
      0x003C23C5,  /*915.7 MHz   */
      0x003C23C7,  /*915.9 MHz   */
      0x003C23C9,  /*916.1 MHz   */
      0x003C23CB,  /*916.3 MHz   */
      0x003C23CD,  /*916.5 MHz   */
      0x003C23CF,  /*916.7 MHz   */
      0x003C23D1,  /*916.9 MHz   */
      0x003C23D3,  /*917.1 MHz   */
      0x003C23D5,  /*917.3 MHz   */
      0x003C23D7,  /*917.5 MHz   */
      0x003C23D9,  /*917.7 MHz   */
      0x003C23DB,  /*917.9 MHz   */
      0x003C23DD,  /*918.1 MHz   */
      0x003C23DF,  /*918.3 MHz   */
      0x003C23E1,  /*918.5 MHz   */
      0x003C23E3
   )
   private val zaFreqSortedIdx = intArrayOf(
      0, 1, 2, 3, 4,
      5, 6, 7, 8, 9,
      10, 11, 12, 13, 14,
      15
   )
   private val ID_CHN_CNT = 4
   private val IDTableOfFreq = doubleArrayOf(
      923.25, 923.75, 924.25, 924.75
   )
   private val indonesiaFreqTable = intArrayOf(
      0x00180E6D,  /*923.25 MHz    */
      0x00180E6F,  /*923.75 MHz    */
      0x00180E71,  /*924.25 MHz    */
      0x00180E73
   )
   private val indonesiaFreqSortedIdx = intArrayOf(
      0, 1, 2, 3
   )
   private val IL_CHN_CNT = 7
   private val ILTableOfFreq = doubleArrayOf(
      915.25, 915.5, 915.75, 916.0, 916.25,  // 4
      916.5, 916.75
   )
   private val ilFreqTable = intArrayOf(
      0x00180E4D,  /*915.25 MHz   */
      0x00180E51,  /*916.25 MHz   */
      0x00180E4E,  /*915.5 MHz   */
      0x00180E52,  /*916.5 MHz   */
      0x00180E4F,  /*915.75 MHz   */
      0x00180E53,  /*916.75 MHz   */
      0x00180E50
   )
   private val ilFreqSortedIdx = intArrayOf(
      0, 4, 1, 5, 2, 6, 3
   )
   private val IL2019RW_CHN_CNT = 5
   private val IL2019RWTableOfFreq = doubleArrayOf(
      915.9, 916.025, 916.15, 916.275, 916.4
   )
   private val il2019RwFreqTable = intArrayOf(
      0x003C23C7,  /*915.9 MHz   */
      0x003C23C8,  /*916.025 MHz   */
      0x003C23C9,  /*916.15 MHz   */
      0x003C23CA,  /*916.275 MHz   */
      0x003C23CB
   )
   private val il2019RwFreqSortedIdx = intArrayOf(
      0, 4, 1, 2, 3
   )
   private val PH_CHN_CNT = 8
   private val PHTableOfFreq = doubleArrayOf(
      918.125, 918.375, 918.625, 918.875, 919.125,  // 5
      919.375, 919.625, 919.875
   )
   private val phFreqTable = intArrayOf(
      0x00301CB1,  /*918.125MHz   Channel 0*/
      0x00301CBB,  /*919.375MHz   Channel 5*/
      0x00301CB7,  /*918.875MHz   Channel 3*/
      0x00301CBF,  /*919.875MHz   Channel 7*/
      0x00301CB3,  /*918.375MHz   Channel 1*/
      0x00301CBD,  /*919.625MHz   Channel 6*/
      0x00301CB5,  /*918.625MHz   Channel 2*/
      0x00301CB9
   )
   private val phFreqSortedIdx = intArrayOf(
      0, 5, 3, 7, 1, 6, 2, 4
   )
   private val NZ_CHN_CNT = 11
   private val NZTableOfFreq = doubleArrayOf(
      922.25, 922.75, 923.25, 923.75, 924.25,  // 4
      924.75, 925.25, 925.75, 926.25, 926.75,  // 9
      927.25
   )
   private val nzFreqTable = intArrayOf(
      0x00180E71,  /*924.25 MHz   */
      0x00180E77,  /*925.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E7B,  /*926.75 MHz   */
      0x00180E6D,  /*923.25 MHz   */
      0x00180E7D,  /*927.25 MHz   */
      0x00180E75,  /*925.25 MHz   */
      0x00180E6B,  /*922.75 MHz   */
      0x00180E79,  /*926.25 MHz   */
      0x00180E6F,  /*923.75 MHz   */
      0x00180E73
   )
   private val nzFreqSortedIdx = intArrayOf(
      4, 7, 0, 9, 2, 10, 6, 1, 8, 3, 5
   )
   private val CN_CHN_CNT = 16
   private val CHNTableOfFreq = doubleArrayOf(
      920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375, 922.625, 922.875,
      923.125, 923.375, 923.625, 923.875, 924.125, 924.375
   )
   private val cnFreqTable = intArrayOf(
      0x00301CD3,  /*922.375MHz   */
      0x00301CD1,  /*922.125MHz   */
      0x00301CCD,  /*921.625MHz   */
      0x00301CC5,  /*920.625MHz   */
      0x00301CD9,  /*923.125MHz   */
      0x00301CE1,  /*924.125MHz   */
      0x00301CCB,  /*921.375MHz   */
      0x00301CC7,  /*920.875MHz   */
      0x00301CD7,  /*922.875MHz   */
      0x00301CD5,  /*922.625MHz   */
      0x00301CC9,  /*921.125MHz   */
      0x00301CDF,  /*923.875MHz   */
      0x00301CDD,  /*923.625MHz   */
      0x00301CDB,  /*923.375MHz   */
      0x00301CCF,  /*921.875MHz   */
      0x00301CE3
   )
   private val cnFreqSortedIdx = intArrayOf(
      7, 6, 4, 0, 10,
      14, 3, 1, 9, 8,
      2, 13, 12, 11, 5,
      15
   )
   private val UH1_CHN_CNT = 10
   private val UH1TableOfFreq = doubleArrayOf(
      915.25, 915.75, 916.25, 916.75, 917.25,
      917.75, 918.25, 918.75, 919.25, 919.75
   )
   private val uh1FreqTable = intArrayOf(
      0x00180E4F,  /*915.75 MHz   */
      0x00180E4D,  /*915.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E57,  /*917.75 MHz   */
      0x00180E55,  /*917.25 MHz   */
      0x00180E59,  /*918.25 MHz   */
      0x00180E51,  /*916.25 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E53
   )
   private val uh1FreqSortedIdx = intArrayOf(
      1, 0, 8, 7, 5,
      4, 6, 2, 9, 3
   )
   private val UH2_CHN_CNT = 15
   private val UH2TableOfFreq = doubleArrayOf(
      920.25, 920.75, 921.25, 921.75, 922.25,  // 4
      922.75, 923.25, 923.75, 924.25, 924.75,  // 9
      925.25, 925.75, 926.25, 926.75, 927.25
   )
   private val uh2FreqTable = intArrayOf(
      0x00180E7B,  /*926.75 MHz   */
      0x00180E79,  /*926.25 MHz   */
      0x00180E7D,  /*927.25 MHz   */
      0x00180E61,  /*920.25 MHz   */
      0x00180E75,  /*925.25 MHz   */
      0x00180E67,  /*921.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E73,  /*924.75 MHz   */
      0x00180E6F,  /*923.75 MHz   */
      0x00180E77,  /*925.75 MHz   */
      0x00180E71,  /*924.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63,  /*920.75 MHz   */
      0x00180E6B,  /*922.75 MHz   */
      0x00180E6D
   )
   private val uh2FreqSortedIdx = intArrayOf(
      13, 12, 14, 0, 10,
      3, 4, 9, 7, 11,
      8, 2, 1, 5, 6
   )
   private val LH_CHN_CNT = 26
   private val LHTableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25, 904.75,  // 4
      905.25, 905.75, 906.25, 906.75, 907.25,  // 9
      907.75, 908.25, 908.75, 909.25, 909.75,  // 14
      910.25, 910.75, 911.25, 911.75, 912.25,  // 19
      912.75, 913.25, 913.75, 914.25, 914.75,  // 24
      915.25
   )
   private val lhFreqTable = intArrayOf(
      0x00180E1B,  /*902.75 MHz   */
      0x00180E35,  /*909.25 MHz   */
      0x00180E1D,  /*903.25 MHz   */
      0x00180E37,  /*909.75 MHz   */
      0x00180E1F,  /*903.75 MHz   */
      0x00180E39,  /*910.25 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E3B,  /*910.75 MHz   */
      0x00180E23,  /*904.75 MHz   */
      0x00180E3D,  /*911.25 MHz   */
      0x00180E25,  /*905.25 MHz   */
      0x00180E3F,  /*911.75 MHz   */
      0x00180E27,  /*905.75 MHz   */
      0x00180E41,  /*912.25 MHz   */
      0x00180E29,  /*906.25 MHz   */
      0x00180E43,  /*912.75 MHz   */
      0x00180E2B,  /*906.75 MHz   */
      0x00180E45,  /*913.25 MHz   */
      0x00180E2D,  /*907.25 MHz   */
      0x00180E47,  /*913.75 MHz   */
      0x00180E2F,  /*907.75 MHz   */
      0x00180E49,  /*914.25 MHz   */
      0x00180E31,  /*908.25 MHz   */
      0x00180E4B,  /*914.75 MHz   */
      0x00180E33,  /*908.75 MHz   */
      0x00180E4D
   )
   private val lhFreqSortedIdx = intArrayOf(
      0, 13, 1, 14, 2,
      15, 3, 16, 4, 17,
      5, 18, 6, 19, 7,
      20, 8, 21, 9, 22,
      10, 23, 11, 24, 12,
      25
   )
   private val LH1_CHN_CNT = 14
   private val LH1TableOfFreq = doubleArrayOf(
      902.75, 903.25, 903.75, 904.25, 904.75,  // 4
      905.25, 905.75, 906.25, 906.75, 907.25,  // 9
      907.75, 908.25, 908.75, 909.25
   )
   private val lh1FreqTable = intArrayOf(
      0x00180E1B,  /*902.75 MHz   */
      0x00180E35,  /*909.25 MHz   */
      0x00180E1D,  /*903.25 MHz   */
      0x00180E1F,  /*903.75 MHz   */
      0x00180E21,  /*904.25 MHz   */
      0x00180E23,  /*904.75 MHz   */
      0x00180E25,  /*905.25 MHz   */
      0x00180E27,  /*905.75 MHz   */
      0x00180E29,  /*906.25 MHz   */
      0x00180E2B,  /*906.75 MHz   */
      0x00180E2D,  /*907.25 MHz   */
      0x00180E2F,  /*907.75 MHz   */
      0x00180E31,  /*908.25 MHz   */
      0x00180E33
   )
   private val lh1FreqSortedIdx = intArrayOf(
      0, 13, 1, 2, 3,
      4, 5, 6, 7, 8,
      9, 10, 11, 12
   )
   private val LH2_CHN_CNT = 11
   private val LH2TableOfFreq = doubleArrayOf(
      909.75, 910.25, 910.75, 911.25, 911.75,  // 4
      912.25, 912.75, 913.25, 913.75, 914.25,  // 9
      914.75
   )
   private val lh2FreqTable = intArrayOf(
      0x00180E37,  /*909.75 MHz   */
      0x00180E39,  /*910.25 MHz   */
      0x00180E3B,  /*910.75 MHz   */
      0x00180E3D,  /*911.25 MHz   */
      0x00180E3F,  /*911.75 MHz   */
      0x00180E41,  /*912.25 MHz   */
      0x00180E43,  /*912.75 MHz   */
      0x00180E45,  /*913.25 MHz   */
      0x00180E47,  /*913.75 MHz   */
      0x00180E49,  /*914.25 MHz   */
      0x00180E4B
   )
   private val lh2FreqSortedIdx = intArrayOf(
      0, 1, 2, 3, 4,
      5, 6, 7, 8, 9,
      10
   )
   private val ETSI_CHN_CNT = 4
   private val ETSITableOfFreq = doubleArrayOf(
      865.70, 866.30, 866.90, 867.50
   )
   private val etsiFreqTable = intArrayOf(
      0x003C21D1,  /*865.700MHz   */
      0x003C21D7,  /*866.300MHz   */
      0x003C21DD,  /*866.900MHz   */
      0x003C21E3
   )
   private val etsiFreqSortedIdx = intArrayOf(
      0, 1, 2, 3
   )
   private val IDA_CHN_CNT = 3
   private val IDATableOfFreq = doubleArrayOf(
      865.70, 866.30, 866.90
   )
   private val indiaFreqTable = intArrayOf(
      0x003C21D1,  /*865.700MHz   */
      0x003C21D7,  /*866.300MHz   */
      0x003C21DD
   )
   private val indiaFreqSortedIdx = intArrayOf(
      0, 1, 2
   )
   private val KR_CHN_CNT = 19
   private val KRTableOfFreq = doubleArrayOf(
      910.20, 910.40, 910.60, 910.80, 911.00, 911.20, 911.40, 911.60, 911.80, 912.00,
      912.20, 912.40, 912.60, 912.80, 913.00, 913.20, 913.40, 913.60, 913.80
   )
   private val krFreqTable = intArrayOf(
      0x003C23A8,  /*912.8MHz   13*/
      0x003C23A0,  /*912.0MHz   9*/
      0x003C23AC,  /*913.2MHz   15*/
      0x003C239E,  /*911.8MHz   8*/
      0x003C23A4,  /*912.4MHz   11*/
      0x003C23B2,  /*913.8MHz   18*/
      0x003C2392,  /*910.6MHz   2*/
      0x003C23B0,  /*913.6MHz   17*/
      0x003C2390,  /*910.4MHz   1*/
      0x003C239C,  /*911.6MHz   7*/
      0x003C2396,  /*911.0MHz   4*/
      0x003C23A2,  /*912.2MHz   10*/
      0x003C238E,  /*910.2MHz   0*/
      0x003C23A6,  /*912.6MHz   12*/
      0x003C2398,  /*911.2MHz   5*/
      0x003C2394,  /*910.8MHz   3*/
      0x003C23AE,  /*913.4MHz   16*/
      0x003C239A,  /*911.4MHz   6*/
      0x003C23AA
   )
   private val krFreqSortedIdx = intArrayOf(
      13, 9, 15, 8, 11,
      18, 2, 17, 1, 7,
      4, 10, 0, 12, 5,
      3, 16, 6, 14
   )
   private val KR2017RW_CHN_CNT = 6
   private val KR2017RwTableOfFreq = doubleArrayOf(
      917.30, 917.90, 918.50, 919.10, 919.70, 920.30
   )
   private val kr2017RwFreqTable = intArrayOf(
      0x003C23D5,  /* 917.3 -> 917.25  MHz Channel 1*/
      0x003C23DB,  /*917.9 -> 918 MHz Channel 2*/
      0x003C23E1,  /*918.5 MHz Channel 3*/
      0x003C23E7,  /*919.1 -> 919  MHz Channel 4*/
      0x003C23ED,  /*919.7 -> 919.75 MHz Channel 5*/
      0x003C23F3 /* 920.3 -> 920.25 MHz Channel 6*/
   )
   private val kr2017RwFreqSortedIdx = intArrayOf(
      3, 0, 5, 1, 4, 2
   )
   private val JPN2012_CHN_CNT = 4
   private val JPN2012TableOfFreq = doubleArrayOf(
      916.80, 918.00, 919.20, 920.40
   )
   private val jpn2012FreqTable = intArrayOf(
      0x003C23D0,  /*916.800MHz   Channel 1*/
      0x003C23DC,  /*918.000MHz   Channel 2*/
      0x003C23E8,  /*919.200MHz   Channel 3*/
      0x003C23F4
   )
   private val jpn2012FreqSortedIdx = intArrayOf(
      0, 1, 2, 3
   )
   private val JPN2012A_CHN_CNT = 6
   private val JPN2012ATableOfFreq = doubleArrayOf(
      916.80, 918.00, 919.20, 920.40, 920.60, 920.80
   )
   private val jpn2012AFreqTable = intArrayOf(
      0x003C23D0,  /*916.800MHz   Channel 1*/
      0x003C23DC,  /*918.000MHz   Channel 2*/
      0x003C23E8,  /*919.200MHz   Channel 3*/
      0x003C23F4,  /*920.400MHz   Channel 4*/
      0x003C23F6,  /*920.600MHz   Channel 5*/
      0x003C23F8
   )
   private val jpn2012AFreqSortedIdx = intArrayOf(
      0, 1, 2, 3, 4, 5
   )
   private val ETSIUPPERBAND_CHN_CNT = 4
   private val ETSIUPPERBANDTableOfFreq = doubleArrayOf(
      916.3, 917.5, 918.7, 919.9
   )
   private val etsiupperbandFreqTable = intArrayOf(
      0x003C23CB,  /*916.3 MHz   */
      0x003C23D7,  /*917.5 MHz   */
      0x003C23E3,  /*918.7 MHz   */
      0x003C23EF
   )
   private val etsiupperbandFreqSortedIdx = intArrayOf(
      0, 1, 2, 3
   )
   private val VN1_CHN_CNT = 3
   private val VN1TableOfFreq = doubleArrayOf(
      866.30, 866.90, 867.50
   )
   private val vietnam1FreqTable = intArrayOf(
      0x003C21D7,  /*866.300MHz   */
      0x003C21DD,  /*866.900MHz   */
      0x003C21E3
   )
   private val vietnam1FreqSortedIdx = intArrayOf(
      0, 1, 2
   )
   private val VN2_CHN_CNT = 8
   private val VN2TableOfFreq = doubleArrayOf(
      918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25
   )
   private val vietnam2FreqTable = intArrayOf(
      0x00180E61,  /*920.25 MHz   */
      0x00180E5D,  /*919.25 MHz   */
      0x00180E5B,  /*918.75 MHz   */
      0x00180E67,  /*921.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E5F,  /*919.75 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63
   )
   private val vietnam2FreqSortedIdx = intArrayOf(
      3, 1, 0, 6, 7, 2, 5, 4
   )
   private val VN3_CHN_CNT = 4
   private val VN3TableOfFreq = doubleArrayOf(
      920.75, 921.25, 921.75, 922.25
   )
   private val vietnam3FreqTable = intArrayOf(
      0x00180E67,  /*921.75 MHz   */
      0x00180E69,  /*922.25 MHz   */
      0x00180E65,  /*921.25 MHz   */
      0x00180E63
   )
   private val vietnam3FreqSortedIdx = intArrayOf(
      2, 3, 1, 0
   )

   fun setChannelData(regionCode: RegionCodes?): Boolean {
      return true
   }

   /*
    private void SetFrequencyBand (UInt32 frequencySelector, BandState config, UInt32 multdiv, UInt32 pllcc)
    {
        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_SEL, frequencySelector);

        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_CFG, (uint)config);

        if (config == BandState.ENABLE)
        {
            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDIVMULT, multdiv);

            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDACCTL, pllcc);
        }
    }*/
   @Keep
   fun FreqChnCnt(): Int {
      return FreqChnCnt(regionCode)
   }

   @Keep
   fun FreqChnCnt(regionCode: RegionCodes?): Int {
      return when (regionCode) {
         RegionCodes.FCC, RegionCodes.AG, RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR, RegionCodes.MX, RegionCodes.PM, RegionCodes.UG -> FCC_CHN_CNT
         RegionCodes.PR -> PRTableOfFreq.size
         RegionCodes.VZ -> VZ_CHN_CNT
         RegionCodes.AU -> AUS_CHN_CNT
         RegionCodes.BR1 -> BR1_CHN_CNT
         RegionCodes.BR2 -> BR2_CHN_CNT
         RegionCodes.BR3 -> BR3_CHN_CNT
         RegionCodes.BR4 -> BR4_CHN_CNT
         RegionCodes.BR5 -> BR5_CHN_CNT
         RegionCodes.HK, RegionCodes.SG, RegionCodes.TH, RegionCodes.VN -> HK_CHN_CNT
         RegionCodes.VN1 -> VN1_CHN_CNT
         RegionCodes.VN2 -> VN2_CHN_CNT
         RegionCodes.VN3 -> VN3_CHN_CNT
         RegionCodes.BD -> BD_CHN_CNT
         RegionCodes.TW -> TW_CHN_CNT
         RegionCodes.MY -> MYS_CHN_CNT
         RegionCodes.ZA -> ZA_CHN_CNT
         RegionCodes.ID -> ID_CHN_CNT
         RegionCodes.IL -> IL_CHN_CNT
         RegionCodes.IL2019RW -> IL2019RW_CHN_CNT
         RegionCodes.PH -> PH_CHN_CNT
         RegionCodes.NZ -> NZ_CHN_CNT
         RegionCodes.CN -> CN_CHN_CNT
         RegionCodes.UH1 -> UH1_CHN_CNT
         RegionCodes.UH2 -> UH2_CHN_CNT
         RegionCodes.LH -> LH_CHN_CNT
         RegionCodes.LH1 -> LH1_CHN_CNT
         RegionCodes.LH2 -> LH2_CHN_CNT
         RegionCodes.ETSI -> ETSI_CHN_CNT
         RegionCodes.IN -> IDA_CHN_CNT
         RegionCodes.KR -> KR_CHN_CNT
         RegionCodes.KR2017RW -> KR2017RW_CHN_CNT
         RegionCodes.JP -> JPN2012_CHN_CNT
         RegionCodes.JP6 -> JPN2012A_CHN_CNT
         RegionCodes.ETSIUPPERBAND -> ETSIUPPERBAND_CHN_CNT
         else -> 0
      }
   }

   fun GetAvailableFrequencyTable(regionCode: RegionCodes?): DoubleArray {
      var freqText: DoubleArray
      return when (regionCode) {
         RegionCodes.FCC, RegionCodes.AG, RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR, RegionCodes.MX, RegionCodes.PM, RegionCodes.UG ->                 /*switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
                    case 0:
                        return FCCTableOfFreq0;
                    case 1:
                        return FCCTableOfFreq1;
                    default:
                        return FCCTableOfFreq;
                }*/FCCTableOfFreq

         RegionCodes.PR -> PRTableOfFreq
         RegionCodes.VZ -> VZTableOfFreq
         RegionCodes.AU -> AUSTableOfFreq
         RegionCodes.BR1 -> BR1TableOfFreq
         RegionCodes.BR2 -> BR2TableOfFreq
         RegionCodes.BR3 -> BR3TableOfFreq
         RegionCodes.BR4 -> BR4TableOfFreq
         RegionCodes.BR5 -> BR5TableOfFreq
         RegionCodes.HK, RegionCodes.SG, RegionCodes.TH, RegionCodes.VN -> HKTableOfFreq
         RegionCodes.VN1 -> VN1TableOfFreq
         RegionCodes.VN2 -> VN2TableOfFreq
         RegionCodes.VN3 -> VN3TableOfFreq
         RegionCodes.BD -> BDTableOfFreq
         RegionCodes.TW -> TWTableOfFreq
         RegionCodes.MY -> MYSTableOfFreq
         RegionCodes.ZA -> ZATableOfFreq
         RegionCodes.ID -> IDTableOfFreq
         RegionCodes.IL -> ILTableOfFreq
         RegionCodes.IL2019RW -> IL2019RWTableOfFreq
         RegionCodes.PH -> PHTableOfFreq
         RegionCodes.NZ -> NZTableOfFreq
         RegionCodes.CN -> CHNTableOfFreq
         RegionCodes.UH1 -> UH1TableOfFreq
         RegionCodes.UH2 -> UH2TableOfFreq
         RegionCodes.LH -> LHTableOfFreq
         RegionCodes.LH1 -> LH1TableOfFreq
         RegionCodes.LH2 -> LH2TableOfFreq
         RegionCodes.ETSI -> {
            appendToLog("Got ETSI Table of Frequencies")
            ETSITableOfFreq
         }

         RegionCodes.IN -> IDATableOfFreq
         RegionCodes.KR -> KRTableOfFreq
         RegionCodes.KR2017RW -> KR2017RwTableOfFreq
         RegionCodes.JP -> JPN2012TableOfFreq
         RegionCodes.JP6 -> JPN2012ATableOfFreq
         RegionCodes.ETSIUPPERBAND -> ETSIUPPERBANDTableOfFreq
         else -> DoubleArray(0)
      }
   }

   @Keep
   private fun FreqIndex(regionCode: RegionCodes?): IntArray? {
      return when (regionCode) {
         RegionCodes.FCC, RegionCodes.AG, RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR, RegionCodes.MX, RegionCodes.PM, RegionCodes.UG ->                 /*switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
                    case 0:
                        return fccFreqSortedIdx0;
                    case 1:
                        return fccFreqSortedIdx1;
                    default:
                        return fccFreqSortedIdx;
                }*/fccFreqSortedIdx

         RegionCodes.PR -> {
            if (freqSortedIdx == null) {
               freqSortedIdx = IntArray(PRTableOfFreq.size)
               if (DEBUG) appendToLog("PR: freqSortedIdx size = " + freqSortedIdx!!.size)
               val list = ArrayList<Int>()
               run {
                  var i = 0
                  while (i < freqSortedIdx!!.size) {
                     list.add(Integer.valueOf(i))
                     i++
                  }
               }
               Collections.shuffle(list)
               var i = 0
               while (i < freqSortedIdx!!.size) {
                  freqSortedIdx!![i] = list[i]
                  if (DEBUG) appendToLog("PR: Random Value = " + freqSortedIdx!![i])
                  i++
               }
            }
            freqSortedIdx
         }

         RegionCodes.VZ -> vzFreqSortedIdx
         RegionCodes.AU -> ausFreqSortedIdx
         RegionCodes.BR1 -> br1FreqSortedIdx
         RegionCodes.BR2 -> br2FreqSortedIdx
         RegionCodes.BR3 -> br3FreqSortedIdx
         RegionCodes.BR4 -> br4FreqSortedIdx
         RegionCodes.BR5 -> br5FreqSortedIdx
         RegionCodes.HK, RegionCodes.SG, RegionCodes.TH, RegionCodes.VN -> hkFreqSortedIdx
         RegionCodes.VN1 -> vietnam1FreqSortedIdx
         RegionCodes.VN2 -> vietnam2FreqSortedIdx
         RegionCodes.VN3 -> vietnam3FreqSortedIdx
         RegionCodes.BD -> bdFreqSortedIdx
         RegionCodes.TW -> twFreqSortedIdx
         RegionCodes.MY -> mysFreqSortedIdx
         RegionCodes.ZA -> zaFreqSortedIdx
         RegionCodes.ID -> indonesiaFreqSortedIdx
         RegionCodes.IL -> ilFreqSortedIdx
         RegionCodes.IL2019RW -> il2019RwFreqSortedIdx
         RegionCodes.PH -> phFreqSortedIdx
         RegionCodes.NZ -> nzFreqSortedIdx
         RegionCodes.CN -> cnFreqSortedIdx
         RegionCodes.UH1 -> uh1FreqSortedIdx
         RegionCodes.UH2 -> uh2FreqSortedIdx
         RegionCodes.LH -> lhFreqSortedIdx
         RegionCodes.LH1 -> lh1FreqSortedIdx
         RegionCodes.LH2 -> lh2FreqSortedIdx
         RegionCodes.ETSI -> etsiFreqSortedIdx
         RegionCodes.IN -> indiaFreqSortedIdx
         RegionCodes.KR -> krFreqSortedIdx
         RegionCodes.KR2017RW -> kr2017RwFreqSortedIdx
         RegionCodes.JP -> jpn2012FreqSortedIdx
         RegionCodes.JP6 -> jpn2012AFreqSortedIdx
         RegionCodes.ETSIUPPERBAND -> etsiupperbandFreqSortedIdx
         else -> null
      }
   }

   fun FreqTable(regionCode: RegionCodes?): IntArray? {
      return when (regionCode) {
         RegionCodes.FCC, RegionCodes.AG, RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR, RegionCodes.MX, RegionCodes.PM, RegionCodes.UG -> /*                int[] freqTableIdx = fccFreqTableIdx;
                int[] freqSortedIdx;
                int[] freqTable = new int[50];
                if (DEBUG) appendToLog("gerVersionCode = " + mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode());
                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
                    case 0:
                        freqSortedIdx = fccFreqSortedIdx0;
                        break;
                    case 1:
                        freqSortedIdx = fccFreqSortedIdx1;
                        break;
                    default:
                        freqSortedIdx = fccFreqSortedIdx;
                        break;
                }
                for (int i = 0; i < 50; i++) {
                    freqTable[i] = fccFreqTable[fccFreqTableIdx[freqSortedIdx[i]]];
                    if (DEBUG) appendToLog("i = " + i + ", freqSortedIdx = " + freqSortedIdx[i] + ", fccFreqTableIdx = " + fccFreqTableIdx[freqSortedIdx[i]] + ", freqTable[" + i + "] = " + freqTable[i]);
                }
                return freqTable;*/fccFreqTable

         RegionCodes.PR -> {
            val freqSortedIndex = FreqIndex(regionCode)
            var freqTable: IntArray? = null
            if (freqSortedIndex != null) {
               freqTable = IntArray(freqSortedIndex.size)
               var i = 0
               while (i < freqSortedIndex.size) {
                  var j = 0
                  while (j < FCCTableOfFreq.size) {
                     if (FCCTableOfFreq[j] == PRTableOfFreq[freqSortedIndex[i]]) break
                     j++
                  }
                  freqTable[i] = fccFreqTable[fccFreqTableIdx[j]]
                  i++
               }
            } else if (DEBUG) appendToLog("NULL freqSortedIndex")
            freqTable // return prFreqTable;
         }

         RegionCodes.VZ -> vzFreqTable
         RegionCodes.AU -> AusFreqTable
         RegionCodes.BR1 -> br1FreqTable
         RegionCodes.BR2 -> br2FreqTable
         RegionCodes.BR3 -> br3FreqTable
         RegionCodes.BR4 -> br4FreqTable
         RegionCodes.BR5 -> br5FreqTable
         RegionCodes.HK, RegionCodes.SG, RegionCodes.TH, RegionCodes.VN -> hkFreqTable
         RegionCodes.VN1 -> vietnam1FreqTable
         RegionCodes.VN2 -> vietnam2FreqTable
         RegionCodes.VN3 -> vietnam3FreqTable
         RegionCodes.BD -> bdFreqTable
         RegionCodes.TW -> twFreqTable
         RegionCodes.MY -> mysFreqTable
         RegionCodes.ZA -> zaFreqTable
         RegionCodes.ID -> indonesiaFreqTable
         RegionCodes.IL -> ilFreqTable
         RegionCodes.IL2019RW -> il2019RwFreqTable
         RegionCodes.PH -> phFreqTable
         RegionCodes.NZ -> nzFreqTable
         RegionCodes.CN -> cnFreqTable
         RegionCodes.UH1 -> uh1FreqTable
         RegionCodes.UH2 -> uh2FreqTable
         RegionCodes.LH -> lhFreqTable
         RegionCodes.LH1 -> lh1FreqTable
         RegionCodes.LH2 -> lh2FreqTable
         RegionCodes.ETSI -> etsiFreqTable
         RegionCodes.IN -> indiaFreqTable
         RegionCodes.KR -> krFreqTable
         RegionCodes.KR2017RW -> kr2017RwFreqTable
         RegionCodes.JP -> jpn2012FreqTable
         RegionCodes.JP6 -> jpn2012AFreqTable
         RegionCodes.ETSIUPPERBAND -> etsiupperbandFreqTable
         else -> null
      }
   }

   private fun GetPllcc(regionCode: RegionCodes): Long {
      when (regionCode) {
         RegionCodes.ETSI, RegionCodes.IN -> return 0x14070400 //Notice: the read value is 0x14040400
         else -> {}
      }
      return 0x14070200 //Notice: the read value is 0x14020200
   }

   @Keep
   fun getLogicalChannel2PhysicalFreq(channel: Int): Double {
      countryList //  used to set up possibly regionCode
      val TotalCnt = FreqChnCnt(regionCode)
      val freqIndex = FreqIndex(regionCode)
      val freqTable = GetAvailableFrequencyTable(regionCode)
      return if (freqIndex!!.size != TotalCnt || freqTable.size != TotalCnt || channel >= TotalCnt) (-1).toDouble() else freqTable[freqIndex[channel]]
   }

   private fun FreqChnWithinRange(Channel: Int, regionCode: RegionCodes): Boolean {
      val TotalCnt = FreqChnCnt(regionCode)
      return if (TotalCnt <= 0) false else Channel >= 0 && Channel < TotalCnt
   }

   private fun FreqSortedIdxTbls(regionCode: RegionCodes, Channel: Int): Int {
      val TotalCnt = FreqChnCnt(regionCode)
      val freqIndex = FreqIndex(regionCode)
      if (!FreqChnWithinRange(Channel, regionCode) || freqIndex == null) return -1
      for (i in 0 until TotalCnt) {
         if (freqIndex[i] == Channel) return i
      }
      return -1
   }

   val connectionHSpeed: Boolean = connectionHSpeedA

   fun setConnectionHSpeed(on: Boolean): Boolean {
      return setConnectionHSpeedA(on)
   }

   var tagDelayDefaultCompactSetting: Byte = 0
   var tagDelayDefaultNormalSetting: Byte = 30

   @get:Keep
   var tagDelay = tagDelayDefaultCompactSetting
   @Keep
   fun setTagDelay(tagDelay: Byte): Boolean {
      this.tagDelay = tagDelay
      return true
   }

   @get:Keep
   val intraPkDelay: Byte
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.intraPacketDelay

   @Keep
   fun setIntraPkDelay(intraPkDelay: Byte): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setIntraPacketDelay(intraPkDelay)
   }

   @get:Keep
   val dupDelay: Byte
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.dupElimRollWindow

   @Keep
   fun setDupDelay(dupElim: Byte): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setDupElimRollWindow(dupElim)
   }

   var cycleDelaySetting: Long = 0

   @get:Keep
   val cycleDelay: Long = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.cycleDelay

   @Keep
   fun setCycleDelay(cycleDelay: Long): Boolean {
      cycleDelaySetting = cycleDelay
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelay)
   }

   @get:Keep
   val authenticateReplyLength: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.authenticateReplyLength

   @Keep
   fun setTam1Configuration(keyId: Int, matchData: String): Boolean {
      var matchData = matchData
      if (keyId > 255) return false
      if (matchData.length != 20) return false
      var retValue = false
      var preChallenge = "00"
      preChallenge += String.format("%02X", keyId)
      matchData = preChallenge + matchData
      retValue = setAuthMatchData(matchData)
      if (retValue) {
         retValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_AUTHENTICATE_CFG(
            true,
            true,
            0,
            matchData.length * 4
         )
      }
      return retValue
   }

   @Keep
   fun setTam2Configuration(
      keyId: Int,
      matchData: String,
      profile: Int,
      offset: Int,
      blockId: Int,
      protMode: Int
   ): Boolean {
      var matchData = matchData
      if (keyId > 255) return false
      if (matchData.length != 20) return false
      if (profile > 15) return false
      if (offset > 0xFFF) return false
      if (blockId > 15) return false
      if (protMode > 15) return false
      var retValue = false
      var preChallenge = "20"
      var postChallenge: String
      preChallenge += String.format("%02X", keyId)
      postChallenge = profile.toString()
      postChallenge += String.format("%03X", offset)
      postChallenge += blockId.toString()
      postChallenge += protMode.toString()
      matchData = preChallenge + matchData + postChallenge
      retValue = setAuthMatchData(matchData)
      if (retValue) {
         retValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_AUTHENTICATE_CFG(
            true,
            true,
            0,
            matchData.length * 4
         )
      }
      return retValue
   }

   @get:Keep
   val authMatchData: String?
      get() {
         val iValue1 = 96
         val strValue: String? = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.authMatchData
         if (strValue == null) return null
         var strLength = iValue1 / 4
         if (strLength * 4 != iValue1) strLength++
         return strValue.substring(0, strLength)
      }

   @Keep
   fun setAuthMatchData(mask: String?): Boolean {
      var result = false
      if (mask != null) {
         result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAuthMatchData(mask)
      }
      return result
   }

   @get:Keep
   val untraceableEpcLength: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.untraceableEpcLength

   @Keep
   fun setUntraceable(
      bHideEpc: Boolean,
      ishowEpcSize: Int,
      iHideTid: Int,
      bHideUser: Boolean,
      bHideRange: Boolean
   ): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_UNTRACEABLE_CFG(
         if (bHideRange) 2 else 0,
         bHideUser,
         iHideTid,
         ishowEpcSize,
         bHideEpc,
         false
      )
   }

   @Keep
   fun setUntraceable(
      range: Int,
      user: Boolean,
      tid: Int,
      epcLength: Int,
      epc: Boolean,
      uxpc: Boolean
   ): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_UNTRACEABLE_CFG(
         range,
         user,
         tid,
         epcLength,
         epc,
         uxpc
      )
   }

   @Keep
   fun setAuthenticateConfiguration(): Boolean {
      appendToLog("setAuthenuateConfiguration0 Started")
      var bValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_AUTHENTICATE_CFG(
         true,
         true,
         1,
         48
      ) //setAuthenticateConfig((48 << 10) | (1 << 2) | 0x03);
      appendToLog("setAuthenuateConfiguration 1: bValue = " + if (bValue) "true" else "false")
      if (bValue) {
         bValue =
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAuthMatchData("049CA53E55EA") //setAuthenticateMessage(new byte[] { 0x04, (byte)0x9C, (byte)0xA5, 0x3E, 0x55, (byte)0xEA } );
         appendToLog("setAuthenuateConfiguration 2: bValue = " + if (bValue) "true" else "false")
      }
      /*if (bValue) {
            bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.setAuthenticateResponseLen(16 * 8);
            appendToLog("setAuthenuateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
        }*/return false //bValue;
   }

   @get:Keep
   var beepCount = 8
   @Keep
   fun setBeepCount(beepCount: Int): Boolean {
      this.beepCount = beepCount
      return true
   }

   @get:Keep
   var inventoryBeep = true
   @Keep
   fun setInventoryBeep(inventoryBeep: Boolean): Boolean {
      val DEBUG = false
      if (DEBUG) appendToLog("this.inventoryBeep = " + this.inventoryBeep + ", inventoryBeep = " + inventoryBeep)
      this.inventoryBeep = inventoryBeep
      if (DEBUG) appendToLog("this.inventoryBeep = " + this.inventoryBeep + ", inventoryBeep = " + inventoryBeep)
      return true
   }

   @get:Keep
   var inventoryVibrate = false
   @Keep
   fun setInventoryVibrate(inventoryVibrate: Boolean): Boolean {
      val DEBUG = false
      if (DEBUG) appendToLog("this.inventoryVibrate = " + this.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate)
      this.inventoryVibrate = inventoryVibrate
      if (DEBUG) appendToLog("this.inventoryVibrate = " + this.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate)
      return true
   }

   @get:Keep
   var vibrateTime = 300
   @Keep
   fun setVibrateTime(vibrateTime: Int): Boolean {
      this.vibrateTime = vibrateTime
      return true
   }

   @get:Keep
   var vibrateWindow = 2
   @Keep
   fun setVibrateWindow(vibrateWindow: Int): Boolean {
      this.vibrateWindow = vibrateWindow
      return true
   }

   @get:Keep
   var saveFileEnable = true
   @Keep
   fun setSaveFileEnable(saveFileEnable: Boolean): Boolean {
      appendToLog("this.saveFileEnable = " + this.saveFileEnable + ", saveFileEnable = " + saveFileEnable)
      this.saveFileEnable = saveFileEnable
      appendToLog("this.saveFileEnable = " + this.saveFileEnable + ", saveFileEnable = " + saveFileEnable)
      return true
   }

   @get:Keep
   var saveCloudEnable = false
   @Keep
   fun setSaveCloudEnable(saveCloudEnable: Boolean): Boolean {
      this.saveCloudEnable = saveCloudEnable
      return true
   }

   @get:Keep
   var saveNewCloudEnable = false
   @Keep
   fun setSaveNewCloudEnable(saveNewCloudEnable: Boolean): Boolean {
      this.saveNewCloudEnable = saveNewCloudEnable
      return true
   }

   @get:Keep
   var saveAllCloudEnable = false
   @Keep
   fun setSaveAllCloudEnable(saveAllCloudEnable: Boolean): Boolean {
      this.saveAllCloudEnable = saveAllCloudEnable
      return true
   }

   @get:Keep
   val userDebugEnable: Boolean
      get() {
         val bValue = mBluetoothConnector!!.userDebugEnable
         appendToLog("bValue = $bValue")
         return bValue
      }

   @Keep
   fun setUserDebugEnable(userDebugEnable: Boolean): Boolean {
      appendToLog("new userDebug = $userDebugEnable")
      mBluetoothConnector!!.userDebugEnable = userDebugEnable
      return true
   }

   //    String serverLocation = "https://" + "www.convergence.com.hk:" + "29090/WebServiceRESTs/1.0/req/" + "create-update-delete/update-entity/" + "tagdata";
   //    String serverLocation = "http://ptsv2.com/t/10i1t-1519143332/post";
   @get:Keep
   var serverLocation = ""
   @Keep
   fun setServerLocation(serverLocation: String): Boolean {
      this.serverLocation = serverLocation
      return true
   }

   @get:Keep
   var serverTimeout = 6
   @Keep
   fun setServerTimeout(serverTimeout: Int): Boolean {
      this.serverTimeout = serverTimeout
      return true
   }

   @get:Keep
   val startQValue: Int
      //    aetOperationMode(continuous, antennaSequenceMode, result)
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoStartQ(3)

   @get:Keep
   val maxQValue: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoMaxQ(3)

   @get:Keep
   val minQValue: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoMinQ(3)
   val retryCount: Int
      get() {
         val algoSelect: Int
         algoSelect = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.algoSelect
         return if (algoSelect == 0 || algoSelect == 3) {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoRetry(algoSelect)
         } else -1
      }

   fun setRetryCount(retryCount: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoRetry(retryCount)
   }

   @Keep
   fun setDynamicQParms(
      startQValue: Int,
      minQValue: Int,
      maxQValue: Int,
      retryCount: Int
   ): Boolean {
      appendToLog("setTagGroup: going to setAlgoSelect with input as 3")
      var result: Boolean
      result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoSelect(3)
      if (result) {
         result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoStartQ(
            startQValue,
            maxQValue,
            minQValue,
            -1,
            -1,
            -1
         )
      }
      if (result) result = setRetryCount(retryCount)
      return result
   }

   @get:Keep
   val fixedQValue: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoStartQ(0)

   @get:Keep
   val fixedRetryCount: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoRetry(0)

   @get:Keep
   val repeatUnitNoTags: Boolean
      get() = if (mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoRunTilZero(0) == 1) true else false

   @Keep
   fun setFixedQParms(qValue: Int, retryCount: Int, repeatUnitNoTags: Boolean): Boolean {
      if (DEBUG) appendToLog("qValue=$qValue, retryCount = $retryCount, repeatUntilNoTags = $repeatUnitNoTags")
      var result: Boolean
      appendToLog("setTagGroup: going to setAlgoSelect with input as 0")
      result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoSelect(0)
      appendToLog("Hello6: invAlgo = 0 ")
      if (qValue == fixedQValue && retryCount == fixedRetryCount && repeatUnitNoTags == this.repeatUnitNoTags) return true
      appendToLog("Hello6: new invAlgo parameters are set")
      if (result) {
         result =
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoStartQ(qValue, -1, -1, -1, -1, -1)
      }
      if (result) result = setRetryCount(retryCount)
      if (result) {
         result =
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoRunTilZero(if (repeatUnitNoTags) 1 else 0)
      }
      return result
   }

   @get:Keep
   val invSelectIndex: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invSelectIndex

   @get:Keep
   val selectEnable: Boolean
      get() {
         val iValue: Int
         iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectEnable
         if (iValue < 0) return false
         return if (iValue != 0) true else false
      }

   @get:Keep
   val selectTarget: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectTarget

   @get:Keep
   val selectAction: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectAction

   @get:Keep
   val selectMaskBank: Int  = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectMaskBank

   @get:Keep
   val selectMaskOffset: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectMaskOffset

   @get:Keep
   val selectMaskData: String?
      get() {
         val iValue1: Int = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectMaskLength
         if (iValue1 < 0) return null
         val strValue: String?  = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.selectMaskData
         if (strValue == null) return null
         var strLength = iValue1 / 4
         if (strLength * 4 != iValue1) strLength++
         return strValue.substring(0, strLength)
      }

   @Keep
   fun setInvSelectIndex(invSelect: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvSelectIndex(invSelect)
   }

   inner class PreFilterData {
      var enable = false
      var target = 0
      var action = 0
      var bank = 0
      var offset = 0
      var mask: String? = null
      var maskbit = false

      constructor()
      constructor(
         enable: Boolean,
         target: Int,
         action: Int,
         bank: Int,
         offset: Int,
         mask: String?,
         maskbit: Boolean
      ) {
         this.enable = enable
         this.target = target
         this.action = action
         this.bank = bank
         this.offset = offset
         this.mask = mask
         this.maskbit = maskbit
      }
   }

   var preFilterData: PreFilterData? = null

   inner class PreMatchData(
      var enable: Boolean,
      var target: Int,
      var action: Int,
      var bank: Int,
      var offset: Int,
      var mask: String?,
      var maskblen: Int,
      var querySelect: Int,
      var pwrlevel: Long,
      var invAlgo: Boolean,
      var qValue: Int
   )

   var preMatchData: PreMatchData? = null
   fun setSelectCriteriaDisable(index: Int): Boolean {
      return setSelectCriteria(index, false, 0, 0, 0, 0, 0, "")
   }

   fun setSelectCriteria(
      index: Int,
      enable: Boolean,
      target: Int,
      action: Int,
      bank: Int,
      offset: Int,
      mask: String?,
      maskbit: Boolean
   ): Boolean {
      var mask = mask
      if (index == 0) preFilterData =
         PreFilterData(enable, target, action, bank, offset, mask, maskbit)
      var maskblen = mask!!.length * 4
      var maskHex = ""
      var iHex = 0
      if (maskbit) {
         for (i in 0 until mask.length) {
            iHex = iHex shl 1
            iHex = if (mask.substring(i, i + 1)
                  .matches("0".toRegex())
            ) iHex and 0xFE else if (mask.substring(i, i + 1)
                  .matches("1".toRegex())
            ) iHex or 0x01 else return false
            if ((i + 1) % 4 == 0) maskHex += String.format("%1X", iHex and 0x0F)
         }
         appendToLog("Hello8: 0 mask = $mask, maskHex = $maskHex, iHex = $iHex")
         val iBitRemain = mask.length % 4
         if (iBitRemain != 0) {
            iHex = iHex shl 4 - iBitRemain
            maskHex += String.format("%1X", iHex and 0x0F)
         }
         appendToLog("Hello8: 1 mask = $mask, maskHex = $maskHex, iHex = $iHex")
         maskblen = mask.length
         mask = maskHex
      }
      return setSelectCriteria(index, enable, target, action, 0, bank, offset, mask, maskblen)
   }

   fun setSelectCriteria(
      index: Int,
      enable: Boolean,
      target: Int,
      action: Int,
      delay: Int,
      bank: Int,
      offset: Int,
      mask: String?
   ): Boolean {
      var mask = mask
      if (index == 0) preFilterData =
         PreFilterData(enable, target, action, bank, offset, mask, false)
      if (mask!!.length > 64) mask = mask.substring(0, 64)
      if (index == 0) preMatchData = PreMatchData(
         enable,
         target,
         action,
         bank,
         offset,
         mask,
         mask.length * 4,
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.querySelect,
         pwrlevel,
         invAlgo,
         qValue.toInt()
      )
      var result = true
      if (index != mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invSelectIndex) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvSelectIndex(index)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectEnable(
         if (enable) 1 else 0,
         target,
         action,
         delay
      )
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskBank(bank)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskOffset(offset)
      if (mask == null) return false
      if (result) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskLength(mask.length * 4)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskData(mask)
      if (result) {
         if (enable) {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagSelect(1)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQuerySelect(3)
         } else {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagSelect(0)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQuerySelect(0)
         }
      }
      return result
   }

   fun setSelectCriteria(
      index: Int,
      enable: Boolean,
      target: Int,
      action: Int,
      delay: Int,
      bank: Int,
      offset: Int,
      mask: String?,
      maskblen: Int
   ): Boolean {
      var mask = mask
      appendToLog("settingUpdate: index = $index, enable = $enable, target = $target, action = $action, delay = $delay, bank = $bank, offset = $offset, mask = $mask, maskbitlen = $maskblen")
      var maskbytelen = maskblen / 4
      if (maskblen % 4 != 0) maskbytelen++
      if (maskbytelen > 64) maskbytelen = 64
      if (mask!!.length > maskbytelen) mask = mask.substring(0, maskbytelen)
      if (index == 0) preMatchData = PreMatchData(
         enable,
         target,
         action,
         bank,
         offset,
         mask,
         maskblen,
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.querySelect,
         pwrlevel,
         invAlgo,
         qValue.toInt()
      )
      var result = true
      if (index != mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invSelectIndex) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvSelectIndex(index)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectEnable(
         if (enable) 1 else 0,
         target,
         action,
         delay
      )
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskBank(bank)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskOffset(offset)
      if (mask == null) return false
      if (result) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskLength(maskblen)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setSelectMaskData(mask)
      if (result) {
         if (enable) {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagSelect(1)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQuerySelect(3)
         } else {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagSelect(0)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQuerySelect(0)
         }
      }
      return result
   }

   @get:Keep
   val rssiFilterEnable: Boolean
      get() {
         var iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterType
         if (iValue < 0) return false
         iValue = iValue and 0xF
         return if (iValue > 0) true else false
      }

   @get:Keep
   val rssiFilterType: Int
      get() {
         var iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterType
         if (iValue < 0) return 0
         iValue = iValue and 0xF
         return if (iValue < 2) 0 else iValue - 1
      }

   @get:Keep
   val rssiFilterOption: Int
      get() {
         var iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterOption
         if (iValue < 0) return 0
         iValue = iValue and 0xF
         return iValue
      }

   @Keep
   fun setRssiFilterConfig(enable: Boolean, rssiFilterType: Int, rssiFilterOption: Int): Boolean {
      var iValue = 0
      iValue = if (!enable) 0 else rssiFilterType + 1
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_INV_RSSI_FILTERING_CONFIG(
         iValue,
         rssiFilterOption
      )
   }

   @get:Keep
   val rssiFilterThreshold1: Double
      get() {
         val iValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterThreshold1
         appendToLog("iValue = $iValue")
         val byteValue = (iValue and 0xFF).toByte()
         appendToLog("byteValue = $byteValue")
         val dValue = mRfidDevice!!.mRfidReaderChip!!.decodeNarrowBandRSSI(byteValue)
         appendToLog("dValue = $dValue")
         return dValue
      }

   @get:Keep
   val rssiFilterThreshold2: Double
      get() {
         val iValue =
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterThreshold2
         appendToLog("iValue = $iValue")
         val byteValue = (iValue and 0xFF).toByte()
         return mRfidDevice!!.mRfidReaderChip!!.decodeNarrowBandRSSI(byteValue)
      }

   @Keep
   fun setRssiFilterThreshold(rssiFilterThreshold1: Double, rssiFilterThreshold2: Double): Boolean {
      appendToLog("rssiFilterThreshold = $rssiFilterThreshold1, $rssiFilterThreshold2")
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_INV_RSSI_FILTERING_THRESHOLD(
         mRfidDevice!!.mRfidReaderChip!!.encodeNarrowBandRSSI(rssiFilterThreshold1),
         mRfidDevice!!.mRfidReaderChip!!.encodeNarrowBandRSSI(rssiFilterThreshold2)
      )
   }

   @get:Keep
   val rssiFilterCount: Long
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.rssiFilterCount

   @Keep
   fun setRssiFilterCount(rssiFilterCount: Long): Boolean {
      appendToLog("rssiFilterCount = $rssiFilterCount")
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setHST_INV_RSSI_FILTERING_COUNT(
         rssiFilterCount
      )
   }

   @get:Keep
   val invMatchEnable: Boolean = if (mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invMatchEnable > 0) true else false

   @get:Keep
   val invMatchType: Boolean  = if (mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invMatchType > 0) true else false

   @get:Keep
   val invMatchOffset: Int  = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invMatchOffset

   @get:Keep
   val invMatchData: String?
      get() {
         val iValue1 = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invMatchLength
         if (iValue1 < 0) return null
         val strValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invMatchData
         var strLength = iValue1 / 4
         if (strLength * 4 != iValue1) strLength++
         return strValue!!.substring(0, strLength)
      }

   inner class PostMatchData(
      var enable: Boolean,
      var target: Boolean,
      var offset: Int,
      var mask: String?,
      antennaCycle: Int,
      var pwrlevel: Long,
      var invAlgo: Boolean,
      var qValue: Int
   )

   var postMatchData: PostMatchData? = null
   @Keep
   fun setPostMatchCriteria(enable: Boolean, target: Boolean, offset: Int, mask: String?): Boolean {
      postMatchData = PostMatchData(
         enable,
         target,
         offset,
         mask,
         antennaCycle,
         pwrlevel,
         invAlgo,
         qValue.toInt()
      )
      var result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvMatchEnable(
         if (enable) 1 else 0,
         if (target) 1 else 0,
         if (mask == null) -1 else mask.length * 4,
         offset
      )
      if (result && mask != null) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvMatchData(mask)
      return result
   }

   @Keep
   fun mrfidToWriteSize(): Int {
      return mRfidDevice!!.mRfidToWrite.size
   }

   @Keep
   fun mrfidToWritePrint() {
      for (i in mRfidDevice!!.mRfidToWrite.indices) {
         appendToLog(byteArrayToString(mRfidDevice!!.mRfidToWrite[i]!!.dataValues))
      }
   }

   @Keep
   fun startOperation(operationTypes: OperationTypes): Boolean {
      var retValue = false
      when (operationTypes) {
         OperationTypes.TAG_INVENTORY_COMPACT, OperationTypes.TAG_INVENTORY, OperationTypes.TAG_SEARCHING -> {
            if (operationTypes == OperationTypes.TAG_INVENTORY_COMPACT) {
               if (false) {
                  setTagGroup(-1, 1, 0) //Set Session S1, Target A
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(0)
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaDwell(2000)
               }
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(true)
            } else {
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(
                  tagDelayDefaultNormalSetting.toInt()
               )
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelaySetting)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(false)
            }
            autoRFIDAbort
            setAutoRFIDAbort(true)
            autoRFIDAbort
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            appendToLog("going to sendHostRegRequestHST_CMD(Cs108Library4A.HostCommands.CMD_18K6CINV)")
            retValue = true
            val hostCommand = HostCommands.CMD_18K6CINV
            retValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(hostCommand)
         }

         else -> {}
      }
      return retValue
   }

   fun resetSiliconLab(): Boolean {
      var bRetValue = false
      if (mSiliconLabIcDevice != null) {
         bRetValue = mSiliconLabIcDevice!!.mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.RESET)
      }
      mRfidDevice!!.inventoring = false
      return bRetValue
   }

   @Keep
   fun abortOperation(): Boolean {
      var bRetValue = false
      if (mRfidDevice!!.mRfidReaderChip != null) {
         bRetValue = mRfidDevice!!.mRfidReaderChip!!.sendControlCommand(ControlCommands.ABORT)
      }
      mRfidDevice!!.inventoring = false
      return bRetValue
   }

   @Keep
   fun restoreAfterTagSelect() {
      appendToLog("Start")
      loadSetting1File()
      if (checkHostProcessorVersion(macVer, 2, 6, 8)) {
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMatchRep(0)
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(tagDelay.toInt())
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelaySetting)
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(true)
      }
      if (postMatchDataChanged) {
         postMatchDataChanged = false
         setPostMatchCriteria(
            postMatchDataOld!!.enable,
            postMatchDataOld!!.target,
            postMatchDataOld!!.offset,
            postMatchDataOld!!.mask
         )
         appendToLog("PowerLevel")
         setPowerLevel(postMatchDataOld!!.pwrlevel)
         appendToLog("writeBleStreamOut: invAlgo = " + postMatchDataOld!!.invAlgo)
         setInvAlgo1(postMatchDataOld!!.invAlgo)
         setQValue1(postMatchDataOld!!.qValue)
      }
      if (false) {
         preMatchDataChanged = false
         appendToLog("preMatchDataChanged is reset")
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setQuerySelect(preMatchDataOld!!.querySelect)
         appendToLog("PowerLevel")
         setPowerLevel(preMatchDataOld!!.pwrlevel)
         appendToLog("writeBleStreamOut: invAlgo = " + preMatchDataOld!!.invAlgo)
         setInvAlgo1(preMatchDataOld!!.invAlgo)
         setQValue1(preMatchDataOld!!.qValue)
         setSelectCriteria(
            0,
            preMatchDataOld!!.enable,
            preMatchDataOld!!.target,
            preMatchDataOld!!.action,
            0,
            preMatchDataOld!!.bank,
            preMatchDataOld!!.offset,
            preMatchDataOld!!.mask,
            preMatchDataOld!!.maskblen
         )
      }
   }

   @Keep
   fun setSelectedTagByTID(strTagId: String?, pwrlevel: Long): Boolean {
      var pwrlevel = pwrlevel
      if (pwrlevel < 0) pwrlevel = pwrlevelSetting
      return setSelectedTag1(strTagId, 2, 0, 0, pwrlevel, 0, 0)
   }

   @Keep
   fun setSelectedTag(strTagId: String, selectBank: Int, pwrlevel: Long): Boolean {
      var isValid = false
      appendToLog("strTagId = $strTagId, selectBank = $selectBank")
      if (selectBank < 0 || selectBank > 3) return false
      val selectOffset = if (selectBank == 1) 32 else 0
      isValid = setSelectedTag1(strTagId, selectBank, selectOffset, 0, pwrlevel, 0, 0)
      return isValid
   }

   fun setMatchRep(matchRep: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMatchRep(matchRep)
   }

   @Keep
   fun setSelectedTag(
      selectMask: String,
      selectBank: Int,
      selectOffset: Int,
      pwrlevel: Long,
      qValue: Int,
      matchRep: Int
   ): Boolean {
      var isValid = false
      appendToLog("strTagId = $selectMask, selectBank = $selectBank, selectOffset = $selectOffset")
      isValid = setSelectedTag1(selectMask, selectBank, selectOffset, 0, pwrlevel, qValue, matchRep)
      return isValid
   }

   var postMatchDataOld: PostMatchData? = null
   var postMatchDataChanged = false
   var preMatchDataOld: PreMatchData? = null
   var preMatchDataChanged = false
   val tagSelectByMatching = false
   @Keep
   fun setSelectedTag1(
      selectMask: String?,
      selectBank: Int,
      selectOffset: Int,
      delay: Int,
      pwrlevel: Long,
      qValue: Int,
      matchRep: Int
   ): Boolean {
      var selectMask = selectMask
      var setSuccess = true
      if (selectMask == null) selectMask = ""
      //if (selectMask.length() == 0) return false;
      if (tagSelectByMatching) {
         if (!postMatchDataChanged) {
            postMatchDataChanged = true
            if (postMatchData == null) {
               postMatchData = PostMatchData(
                  false,
                  false,
                  0,
                  "",
                  antennaCycle,
                  this.pwrlevel,
                  invAlgo,
                  this.qValue.toInt()
               )
            }
            postMatchDataOld = postMatchData
         }
         setSuccess = setPostMatchCriteria(true, false, 0, selectMask)
      } else {
         appendToLogView("Setting setSelectedTag1")
         if (!preMatchDataChanged) {
            preMatchDataChanged = true
            appendToLog("preMatchDataChanged is SET")
            if (preMatchData == null) {
               preMatchData = PreMatchData(
                  false,
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.queryTarget,
                  0,
                  0,
                  0,
                  "",
                  0,
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.querySelect,
                  this.pwrlevel,
                  invAlgo,
                  this.qValue.toInt()
               )
            }
            preMatchDataOld = preMatchData
         }
         setSuccess = setSelectCriteria(
            0,
            true,
            4,
            0,
            delay,
            selectBank,
            selectOffset,
            selectMask,
            selectMask.length * 4
         )
      }
      if (setSuccess) setSuccess = setOnlyPowerLevel(pwrlevel)
      appendToLog("Hello6: going to do setFixedQParms with setSuccess = $setSuccess")
      /*if (setSuccess) setSuccess = setFixedQParms(qValue, 5, false);
        mRfidDevice.mRfidReaderChip.mRx000Setting.setAlgoAbFlip(1);
        if (setSuccess) {
            appendToLog("writeBleStreamOut: invAlgo = false");
            setSuccess = setInvAlgo1(false);
        }*/if (setSuccess) setSuccess =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMatchRep(matchRep)
      if (setSuccess) setSuccess =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagDelay(tagDelayDefaultNormalSetting.toInt())
      if (setSuccess) setSuccess =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setCycleDelay(cycleDelaySetting)
      if (setSuccess) setSuccess =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(false)
      return setSuccess
   }

   private val modifyCodeAA = 0xAA

   @Keep
   enum class RegionCodes {
      NULL, AG, BD, CL, CO, CR, DR, MX, PM, UG, BR1, BR2, BR3, BR4, BR5, IL, IL2019RW, PR, PH, SG, ZA, VZ, AU, NZ, HK, MY, VN, VN1, VN2, VN3, CN, TW, KR, KR2017RW, JP, JP6, TH, IN, FCC, UH1, UH2, LH, LH1, LH2, ETSI, ID, ETSIUPPERBAND, Albania1, Albania2, Algeria1, Algeria2, Algeria3, Algeria4, Argentina, Armenia, Australia1, Australia2, Austria1, Austria2, Azerbaijan, Bahrain, Bangladesh, Belarus, Belgium1, Belgium2, Bolivia, Bosnia, Botswana, Brazil1, Brazil2, Brunei1, Brunei2, Bulgaria1, Bulgaria2, Cambodia, Cameroon, Canada, Chile1, Chile2, Chile3, China, Colombia, Congo, CostaRica, Cotedlvoire, Croatia, Cuba, Cyprus1, Cyprus2, Czech1, Czech2, Denmark1, Denmark2, Dominican, Ecuador, Egypt, ElSalvador, Estonia, Finland1, Finland2, France, Georgia, Germany, Ghana, Greece, Guatemala, HongKong1, HongKong2, Hungary1, Hungary2, Iceland, India, Indonesia, Iran, Ireland1, Ireland2, Israel, Italy, Jamaica, Japan4, Japan6, Jordan, Kazakhstan, Kenya, Korea, KoreaDPR, Kuwait, Kyrgyz, Latvia, Lebanon, Libya, Liechtenstein1, Liechtenstein2, Lithuania1, Lithuania2, Luxembourg1, Luxembourg2, Macao, Macedonia, Malaysia, Malta1, Malta2, Mauritius, Mexico, Moldova1, Moldova2, Mongolia, Montenegro, Morocco, Netherlands, NewZealand1, NewZealand2, Nicaragua, Nigeria, Norway1, Norway2, Oman, Pakistan, Panama, Paraguay, Peru, Philippines, Poland, Portugal, Romania, Russia1, Russia3, Senegal, Serbia, Singapore1, Singapore2, Slovak1, Slovak2, Slovenia1, Solvenia2, SAfrica1, SAfrica2, Spain, SriLanka, Sudan, Sweden1, Sweden2, Switzerland1, Switzerland2, Syria, Taiwan1, Taiwan2, Tajikistan, Tanzania, Thailand, Trinidad, Tunisia, Turkey, Turkmenistan, Uganda, Ukraine, UAE, UK1, UK2, USA, Uruguay, Venezuela, Vietnam1, Vietnam2, Yemen, Zimbabwe
   }

   fun regionCode2StringArray(region: RegionCodes): String {
      return when (region) {
         RegionCodes.AG -> "Argentina"
         RegionCodes.CL -> "Chile"
         RegionCodes.CO -> "Columbia"
         RegionCodes.CR -> "Costa Rica"
         RegionCodes.DR -> "Dominican Republic"
         RegionCodes.MX -> "Mexico"
         RegionCodes.PM -> "Panama"
         RegionCodes.UG -> "Uruguay"
         RegionCodes.BR1 -> "Brazil 915-927"
         RegionCodes.BR2 -> "Brazil 902-906, 915-927"
         RegionCodes.BR3 -> "Brazil 902-906"
         RegionCodes.BR4 -> "Brazil 902-904"
         RegionCodes.BR5 -> "Brazil 917-924"
         RegionCodes.IL, RegionCodes.IL2019RW -> "Israel"
         RegionCodes.PR -> "Peru"
         RegionCodes.PH -> "Philippines"
         RegionCodes.SG -> "Singapore"
         RegionCodes.ZA -> "South Africa"
         RegionCodes.VZ -> "Venezuela"
         RegionCodes.AU -> "Australia"
         RegionCodes.NZ -> "New Zealand"
         RegionCodes.HK -> "Hong Kong"
         RegionCodes.MY -> "Malaysia"
         RegionCodes.VN -> "Vietnam"
         RegionCodes.VN1 -> "Vietnam1"
         RegionCodes.VN2 -> "Vietnam2"
         RegionCodes.VN3 -> "Vietnam3"
         RegionCodes.BD -> "Bangladesh"
         RegionCodes.CN -> "China"
         RegionCodes.TW -> "Taiwan"
         RegionCodes.KR, RegionCodes.KR2017RW -> "Korea"
         RegionCodes.JP -> "Japan"
         RegionCodes.JP6 -> "Japan"
         RegionCodes.TH -> "Thailand"
         RegionCodes.ID -> "Indonesia"
         RegionCodes.FCC -> {
            if (freqModifyCode == modifyCodeAA) "FCC" else "USA/Canada"
         }

         RegionCodes.UH1 -> "UH1"
         RegionCodes.UH2 -> "UH2"
         RegionCodes.LH -> "LH"
         RegionCodes.LH1 -> "LH1"
         RegionCodes.LH2 -> "LH2"
         RegionCodes.ETSI -> "Europe"
         RegionCodes.IN -> "India"
         RegionCodes.ETSIUPPERBAND -> "ETSI Upper Band"
         else -> region.toString()
      }
   }

   var regionCode: RegionCodes? = null
   var countryNumberInList = -1
   val countryList: Array<String?>?
      get() {
         var strCountryList: Array<String?>? = null
         val regionList = regionList
         if (regionList != null) {
            strCountryList = arrayOfNulls(regionList.size)
            for (i in regionList.indices) {
               strCountryList[i] = regionCode2StringArray(regionList[i])
            }
         }
         return strCountryList
      }
   val regionCodeDefault4Country2 = RegionCodes.FCC
   val regionList: Array<RegionCodes>?
      get() {
         val DEBUG = false
         var regionList: Array<RegionCodes>? = null
         run {
            when (this.countryCode) {
               1 -> {
                  if (regionCode == null) regionCode = RegionCodes.ETSI
                  regionList = arrayOf(RegionCodes.ETSI, RegionCodes.IN, RegionCodes.VN1)
               }

               2 -> {
                  val modifyCode = this.freqModifyCode
                  if (modifyCode != modifyCodeAA) {
                     if (regionCode == null) regionCode = regionCodeDefault4Country2
                     regionList = arrayOf(
                        RegionCodes.AG,
                        RegionCodes.AU,
                        RegionCodes.BD,
                        RegionCodes.BR1,
                        RegionCodes.BR2,
                        RegionCodes.BR3,
                        RegionCodes.BR4,
                        RegionCodes.BR5,
                        RegionCodes.CL,
                        RegionCodes.CO,
                        RegionCodes.CR,
                        RegionCodes.DR,
                        RegionCodes.HK,
                        RegionCodes.ID,
                        RegionCodes.IL2019RW,
                        RegionCodes.KR2017RW,
                        RegionCodes.LH1,
                        RegionCodes.LH2,
                        RegionCodes.MY,
                        RegionCodes.MX,
                        RegionCodes.PM,
                        RegionCodes.PR,
                        RegionCodes.PH,
                        RegionCodes.SG,
                        RegionCodes.ZA,
                        RegionCodes.TH,
                        RegionCodes.UH1,
                        RegionCodes.UH2,
                        RegionCodes.UG,
                        RegionCodes.FCC,
                        RegionCodes.VZ,
                        RegionCodes.VN
                     )
                  } else {
                     val strSpecialCountryVersion =
                        mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.specialCountryVersion
                     if (strSpecialCountryVersion!!.contains("OFCA")) {
                        regionCode = RegionCodes.HK
                        regionList = arrayOf(RegionCodes.HK)
                     } else if (strSpecialCountryVersion!!.contains("SG")) {
                        regionCode = RegionCodes.SG
                        regionList = arrayOf(RegionCodes.SG)
                     } else if (strSpecialCountryVersion!!.contains("AS")) {
                        regionCode = RegionCodes.AU
                        regionList = arrayOf(RegionCodes.AU)
                     } else if (strSpecialCountryVersion!!.contains("NZ")) {
                        regionCode = RegionCodes.NZ
                        regionList = arrayOf(RegionCodes.NZ)
                     } else if (strSpecialCountryVersion!!.contains("ZA")) {
                        regionCode = RegionCodes.ZA
                        regionList = arrayOf(RegionCodes.ZA)
                     } else if (strSpecialCountryVersion!!.contains("TH")) {
                        regionCode = RegionCodes.TH
                        regionList = arrayOf(RegionCodes.TH)
                     } else {    //if (strSpecialCountryVersion.contains("*USA")) {
                        regionCode = regionCodeDefault4Country2
                        regionList = arrayOf(RegionCodes.FCC)
                     }
                  }
               }

               3, 4 -> {
                  if (regionCode == null) regionCode = RegionCodes.TW
                  regionList = arrayOf(
                     RegionCodes.TW, RegionCodes.AU, RegionCodes.MY,
                     RegionCodes.HK, RegionCodes.SG, RegionCodes.ID, RegionCodes.CN
                  )
               }

               5 -> {
                  regionCode = RegionCodes.KR
                  regionList = arrayOf(RegionCodes.KR)
               }

               6 -> {
                  regionCode = RegionCodes.KR2017RW
                  regionList = arrayOf(RegionCodes.KR2017RW)
               }

               7 -> {
                  if (regionCode == null) regionCode = RegionCodes.CN
                  regionList = arrayOf(
                     RegionCodes.CN,
                     RegionCodes.AU,
                     RegionCodes.HK,
                     RegionCodes.TH,
                     RegionCodes.SG,
                     RegionCodes.MY,
                     RegionCodes.ID,
                     RegionCodes.VN2,
                     RegionCodes.VN3
                  )
               }

               8 -> {
                  val strSpecialCountryVersion =
                     mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.specialCountryVersion
                  if (strSpecialCountryVersion!!.contains("6")) {
                     regionCode = RegionCodes.JP6
                     regionList = arrayOf(RegionCodes.JP6)
                  } else {
                     regionCode = RegionCodes.JP
                     regionList = arrayOf(RegionCodes.JP)
                  }
               }

               9 -> {
                  regionCode = RegionCodes.ETSIUPPERBAND
                  regionList = arrayOf(RegionCodes.ETSIUPPERBAND)
               }

               else -> {
                  val modifyCode = this.freqModifyCode
                  if (modifyCode != modifyCodeAA) {
                     if (regionCode == null) regionCode = regionCodeDefault4Country2
                     regionList = arrayOf(
                        RegionCodes.AG,
                        RegionCodes.AU,
                        RegionCodes.BD,
                        RegionCodes.BR1,
                        RegionCodes.BR2,
                        RegionCodes.BR3,
                        RegionCodes.BR4,
                        RegionCodes.BR5,
                        RegionCodes.CL,
                        RegionCodes.CO,
                        RegionCodes.CR,
                        RegionCodes.DR,
                        RegionCodes.HK,
                        RegionCodes.ID,
                        RegionCodes.IL2019RW,
                        RegionCodes.KR2017RW,
                        RegionCodes.LH1,
                        RegionCodes.LH2,
                        RegionCodes.MY,
                        RegionCodes.MX,
                        RegionCodes.PM,
                        RegionCodes.PR,
                        RegionCodes.PH,
                        RegionCodes.SG,
                        RegionCodes.ZA,
                        RegionCodes.TH,
                        RegionCodes.UH1,
                        RegionCodes.UH2,
                        RegionCodes.UG,
                        RegionCodes.FCC,
                        RegionCodes.VZ,
                        RegionCodes.VN
                     )
                  } else {
                     val strSpecialCountryVersion =
                        mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.specialCountryVersion
                     if (strSpecialCountryVersion!!.contains("OFCA")) {
                        regionCode = RegionCodes.HK
                        regionList = arrayOf(RegionCodes.HK)
                     } else if (strSpecialCountryVersion!!.contains("SG")) {
                        regionCode = RegionCodes.SG
                        regionList = arrayOf(RegionCodes.SG)
                     } else if (strSpecialCountryVersion!!.contains("AS")) {
                        regionCode = RegionCodes.AU
                        regionList = arrayOf(RegionCodes.AU)
                     } else if (strSpecialCountryVersion!!.contains("NZ")) {
                        regionCode = RegionCodes.NZ
                        regionList = arrayOf(RegionCodes.NZ)
                     } else if (strSpecialCountryVersion!!.contains("ZA")) {
                        regionCode = RegionCodes.ZA
                        regionList = arrayOf(RegionCodes.ZA)
                     } else if (strSpecialCountryVersion!!.contains("TH")) {
                        regionCode = RegionCodes.TH
                        regionList = arrayOf(RegionCodes.TH)
                     } else {
                        regionCode = regionCodeDefault4Country2
                        regionList = arrayOf(RegionCodes.FCC)
                     }
                  }
               }
            }
         }
         countryNumberInList = 0
         if (DEBUG) appendToLog("saveSetting2File testpoint 1")
         for (i in regionList!!.indices) {
            if (regionCode == regionList!![i]) {
               countryNumberInList = i
               if (DEBUG) appendToLog("saveSetting2File testpoint 2")
               break
            }
         }
         return regionList
      }
   var toggledConnection = false
   var runnableToggleConnection: Runnable = object : Runnable {
      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun run() {
         if (DEBUG) appendToLog("runnableToggleConnection(): toggledConnection = " + toggledConnection + ", isBleConnected() = " + isBleConnected)
         if (!isBleConnected) toggledConnection = true
         if (toggledConnection) {
            if (!isBleConnected) {
               if (!connect1(null)) return
            } else return
         } else {
            appendToLog("disconnect H")
            disconnect()
         }
         mHandler.postDelayed(this, 500)
      }
   }

   fun setCountryInList(countryInList: Int): Boolean {
      val DEBUG = true
      if (DEBUG) appendToLog("this.countryInList =" + countryNumberInList + ", countryInList = " + countryInList)
      if (countryNumberInList == countryInList) return true
      val regionList = regionList
      if (DEBUG) appendToLog("regionList length =" + (regionList?.size ?: "NULL"))
      if (regionList == null) return false
      if (countryInList < 0 || countryInList >= regionList.size) return false
      val freqDataTableOld = FreqTable(regionCode)
      if (DEBUG) appendToLog(
         "regionCode =" + regionCode + ", freqDataTableOld length = " + (freqDataTableOld?.size
            ?: "NULL")
      )
      if (freqDataTableOld == null) return false
      val regionCodeNew = regionList[countryInList]
      val freqDataTable = FreqTable(regionCodeNew)
      if (DEBUG) appendToLog(
         "regionCodeNew =" + regionCodeNew + ", freqDataTable length = " + (freqDataTable?.size
            ?: "NULL")
      )
      if (freqDataTable == null) return false
      countryNumberInList = countryInList
      appendToLog("saveSetting2File testpoint 4")
      regionCode = regionCodeNew
      if (DEBUG) appendToLog("getChannel =" + channel + ", FreqChnCnt = " + FreqChnCnt())
      appendToLog("X channel = ")
      if (channel >= FreqChnCnt()) setChannel(0)
      when (countryCode) {
         1, 5, 8, 9 -> {}
         2 -> {
            if (false) {
               if (DEBUG) appendToLog("FCC Region is set")
               toggledConnection = false
               mHandler.removeCallbacks(runnableToggleConnection)
               mHandler.postDelayed(runnableToggleConnection, 500)
               return true
            }
            if (freqDataTable.size == freqDataTableOld.size) {
               var i = 0
               while (i < freqDataTable.size) {
                  if (freqDataTable[i] != freqDataTableOld[i]) break
                  i++
               }
               if (i == freqDataTable.size) {
                  if (DEBUG) appendToLog("Break as same freqDataTable")
               }
            }
            if (DEBUG) appendToLog("Finish as different freqDataTable")
            var k = 0
            while (k < freqDataTable.size) {
               if (DEBUG) appendToLog("Setting channel = $k")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(k)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(true)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqPllMultiplier(freqDataTable[k])
               k++
            }
            while (k < 50) {
               if (DEBUG) appendToLog("Resetting channel = $k")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(k)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(false)
               k++
            }
         }

         else -> {
            if (freqDataTable.size == freqDataTableOld.size) {
               var i = 0
               while (i < freqDataTable.size) {
                  if (freqDataTable[i] != freqDataTableOld[i]) break
                  i++
               }
               if (i == freqDataTable.size) {
                  if (DEBUG) appendToLog("Break as same freqDataTable")
                  return false
               }
            }
            if (DEBUG) appendToLog("Finish as different freqDataTable")
            var k = 0
            while (k < freqDataTable.size) {
               if (DEBUG) appendToLog("Setting channel = $k")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(k)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(true)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqPllMultiplier(freqDataTable[k])
               k++
            }
            while (k < 50) {
               if (DEBUG) appendToLog("Resetting channel = $k")
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(k)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(false)
               k++
            }
         }
      }
      if (DEBUG) appendToLog("New regionCode = " + regionCode.toString() + ", channel = " + channel + ", FreqChnCnt = " + FreqChnCnt())
      return true
   }

   val channelHoppingDefault: Boolean
      get() {
         val countryCode = countryCode
         appendToLog("getChannelHoppingDefault: countryCode (for channelOrderType) = $countryCode")
         run { return countryCode != 1 && countryCode != 8 && countryCode != 9 }
      }
   var channelOrderType = 0 // 0 for frequency hopping / agile, 1 for fixed frequencey
   val channelHoppingStatus: Boolean
      get() {
         appendToLog("countryCode with channelOrderType = $channelOrderType")
         if (channelOrderType < 0) {
            channelOrderType = if (channelHoppingDefault) 0 else 1
         }
         return if (channelOrderType == 0) true else false
      }

   fun setChannelHoppingStatus(channelOrderHopping: Boolean): Boolean {
      if (channelOrderType != (if (channelOrderHopping) 0 else 1)) {
         var result = true
         if (!channelHoppingDefault) {
            result =
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAntennaFreqAgile(if (channelOrderHopping) 1 else 0)
         }
         val freqcnt = FreqChnCnt()
         appendToLog("FrequencyA Count = $freqcnt")
         val channel = channel
         appendToLog(" FrequencyA Channel = $channel")
         for (i in 0 until freqcnt) {
            if (result) mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(i)
            if (result) mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(
               channelOrderHopping
            )
         }
         if (result) mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(channel)
         if (result) mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(true)
         appendToLog(" FrequencyA: end of setting")
         channelOrderType = if (channelOrderHopping) 0 else 1
         appendToLog("setChannelHoppingStatus: channelOrderType = $channelOrderType")
      }
      return true
   }

   val channelFrequencyList: Array<String?>
      get() {
         val DEBUG = true
         appendToLog("regionCode is " + regionCode.toString())
         val table = GetAvailableFrequencyTable(regionCode)
         appendToLog("table length = " + table.size)
         for (i in table.indices) appendToLog("table[" + i + "] = " + table[i])
         val strChannnelFrequencyList = arrayOfNulls<String>(table.size)
         for (i in table.indices) {
            strChannnelFrequencyList[i] = String.format("%.2f MHz", table[i])
            appendToLog("strChannnelFrequencyList[" + i + "] = " + strChannnelFrequencyList[i])
         }
         return strChannnelFrequencyList
      }
   val channel: Int
      get() {
         var channel = -1
         appendToLog("loadSetting1File: getChannel")
         if (mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.freqChannelConfig != 0) {
            channel = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.freqChannelSelect
            appendToLog("loadSetting1File: getting channel = $channel")
         }
         if (channelHoppingStatus) {
            appendToLog("loadSetting1File: got hoppingStatus: channel = $channel")
            channel = 0
         }
         appendToLog("loadSetting1File: channel = $channel")
         return channel
      }

   fun setChannel(channelSelect: Int): Boolean {
      var result = true
      appendToLog("loadSetting1File: channelSelect = $channelSelect")
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(false)
      if (result) result =
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelSelect(channelSelect)
      if (result) result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setFreqChannelConfig(true)
      return result
   }

   val countryCode: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.countryCode
   val freqModifyCode: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.freqModifyCode

   fun getPopulation2Q(population: Int): Byte {
      var dValue = 1 + Math.log10((population * 2).toDouble()) / Math.log10(2.0)
      if (dValue < 0) dValue = 0.0
      if (dValue > 15) dValue = 15.0
      val iValue = dValue.toInt().toByte()
      if (false) appendToLog("getPopulation2Q($population): log dValue = $dValue, iValue = $iValue")
      return iValue
   }

   var population = 30
   fun setPopulation(population: Int): Boolean {
      if (false) appendToLog("Stream population = $population")
      val iValue = getPopulation2Q(population)
      this.population = population
      return setQValue(iValue)
   }

   var qValue: Byte = -1
   fun setQValue(byteValue: Byte): Boolean {
      qValue = byteValue
      if (false) appendToLog("Stream population qValue = " + qValue)
      return setQValue1(byteValue.toInt())
   }

   val qValue1: Int
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.algoStartQ

   fun setQValue1(iValue: Int): Boolean {
      var result = true
      run {
         val invAlgo = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.invAlgo
         if (iValue != mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getAlgoStartQ(invAlgo)) {
            if (false) appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = $invAlgo")
            result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoSelect(invAlgo)
         }
      }
      if (result) {
         result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoStartQ(iValue)
      }
      return result
   }

   @get:Keep
   val radioSerial: String?
      get() {
         var strValue: String?
         strValue = serialNumber
         if (strValue != null) {
            appendToLog("strValue length = " + strValue.length)
            if (strValue.length > 13) strValue = strValue.substring(0, 13)
         } else appendToLog("BBB")
         return strValue
      }

   @get:Keep
   val radioBoardVersion: String?
      get() {
         val str = mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.getSerialNumber()
         if (str != null) {
            if (str.length == 16) {
               var strOut: String
               strOut = if (str.substring(13, 14).matches("0".toRegex())) str.substring(
                  14,
                  15
               ) else str.substring(13, 15)
               strOut += "." + str.substring(15)
               return strOut
            }
         }
         return str
      }

   @get:Keep
   val barcodeOnStatus: Boolean  = mBarcodeDevice?.onStatus == true

   @get:Keep
   val barcodePreSuffix: Unit
      get() {
         if (mBarcodeDevice?.prefix == null || mBarcodeDevice?.suffix == null) barcodeSendQuerySelfPreSuffix()
      }

   @get:Keep
   val barcodeReadingMode: Unit
      get() {
         barcodeSendQueryReadingMode()
      }
   val barcodeEnable2dBarCodes: Unit
      get() {
         barcodeSendQueryEnable2dBarCodes()
      }
   val barcodePrefixOrder: Unit
      get() {
         barcodeSendQueryPrefixOrder()
      }
   val barcodeDelayTimeOfEachReading: Unit
      get() {
         barcodeSendQueryDelayTimeOfEachReading()
      }
   val barcodeNoDuplicateReading: Unit
      get() {
         barcodeSendQueryNoDuplicateReading()
      }

   @Keep
   fun setBarcodeOn(on: Boolean): Boolean {
      var retValue: Boolean
      val cs108BarcodeData = Cs108BarcodeData()
      if (on) cs108BarcodeData.barcodePayloadEvent =
         BarcodePayloadEvents.BARCODE_POWER_ON else cs108BarcodeData.barcodePayloadEvent =
         BarcodePayloadEvents.BARCODE_POWER_OFF
      cs108BarcodeData.waitUplinkResponse = false
      retValue = mBarcodeDevice!!.mBarcodeToWrite.add(cs108BarcodeData)
      val continuousAfterOn = false
      if (retValue && on && continuousAfterOn) {
         retValue = if (checkHostProcessorVersion(bluetoothICFirmwareVersion, 1, 0, 2)) {
            if (DEBUG) appendToLog("to barcodeSendCommandConinuous()")
            barcodeSendCommandConinuous()
         } else false
      }
      if (DEBUG) appendToLog("mBarcodeToWrite size = " + mBarcodeDevice!!.mBarcodeToWrite.size)
      return retValue
   }

   var iModeSet = -1
   var iVibratieTimeSet = -1
   @Keep
   fun setVibrateOn(mode: Int): Boolean {
      val retValue: Boolean
      val cs108BarcodeData = Cs108BarcodeData()
      if (mode > 0) cs108BarcodeData.barcodePayloadEvent =
         BarcodePayloadEvents.BARCODE_VIBRATE_ON else cs108BarcodeData.barcodePayloadEvent =
         BarcodePayloadEvents.BARCODE_VIBRATE_OFF
      cs108BarcodeData.waitUplinkResponse = false
      if (iModeSet == mode && iVibratieTimeSet == vibrateTime) {
         appendToLog("writeBleStreamOut: A7B3: Skip saving vibration data")
         return true
      }
      if (mode > 0) {
         val barcodeCommandData = ByteArray(3)
         barcodeCommandData[0] = (mode - 1).toByte()
         barcodeCommandData[1] = (vibrateTime / 256).toByte()
         barcodeCommandData[2] = (vibrateTime % 256).toByte()
         cs108BarcodeData.dataValues = barcodeCommandData
      }
      retValue = mBarcodeDevice!!.mBarcodeToWrite.add(cs108BarcodeData)
      if (DEBUG) appendToLog("mBarcodeToWrite size = " + mBarcodeDevice!!.mBarcodeToWrite.size)
      if (retValue) {
         iModeSet = mode
         iVibratieTimeSet = vibrateTime
      }
      return retValue
   }

   //            MainActivity.mCs108Library4a.barcodeReadTriggerStart();
   fun barcodeReadTriggerStart(): Boolean {
      val cs108BarcodeData = Cs108BarcodeData()
      cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_SCAN_START
      cs108BarcodeData.waitUplinkResponse = false
      barcode2TriggerMode = false
      return mBarcodeDevice!!.mBarcodeToWrite.add(cs108BarcodeData)
   }

   var barcode2TriggerMode = true
   @Keep
   fun barcodeSendCommandTrigger(): Boolean {
      var retValue = true
      appendToLog("BarStream: Set trigger mode")
      barcode2TriggerMode = true
      mBarcodeDevice!!.bBarcodeTriggerMode = 0x30
      appendToLog("Reading mode is SET to TRIGGER")
      if (retValue) retValue = barcodeSendCommand("nls0006010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0302000;".toByteArray())
      if (retValue) retValue =
         barcodeSendCommand("nls0313000=3000;nls0313010=1000;nls0313040=1000;nls0302000;nls0007010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0001150;nls0006000;".toByteArray())
      return retValue
   }

   var prefixRef = byteArrayOf(0x02, 0x00, 0x07, 0x10, 0x17, 0x13)
   var suffixRef = byteArrayOf(0x05, 0x01, 0x11, 0x16, 0x03, 0x04)
   @Keep
   fun barcodeSendCommandSetPreSuffix(): Boolean {
      var retValue = true
      appendToLog("BarStream: BarcodePrefix BarcodeSuffix are SET")
      if (retValue) retValue = barcodeSendCommand("nls0006010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0311010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0317040;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0305010;".toByteArray())
      var string = "nls0300000=0x" + byteArrayToString(prefixRef) + ";"
      appendToLog("Set Prefix string = $string")
      if (retValue) retValue = barcodeSendCommand(string.toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0306010;".toByteArray())
      string = "nls0301000=0x" + byteArrayToString(suffixRef) + ";"
      appendToLog("Set Suffix string = $string")
      if (retValue) retValue = barcodeSendCommand(string.toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0308030;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0307010;".toByteArray())
      if (retValue) retValue =
         barcodeSendCommand("nls0309010;nls0310010;".toByteArray()) //enable terminator, set terminator as 0x0D
      if (retValue) retValue = barcodeSendCommand("nls0502110;".toByteArray())
      if (retValue) barcodeSendCommand("nls0001150;nls0006000;".toByteArray())
      if (retValue) {
         mBarcodeDevice?.prefix = prefixRef
         mBarcodeDevice?.suffix = suffixRef
      }
      return retValue
   }

   @Keep
   fun barcodeSendCommandResetPreSuffix(): Boolean {
      var retValue = true
      if (retValue) barcodeSendCommand("nls0006010;".toByteArray())
      if (retValue) barcodeSendCommand("nls0311000;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0300000=;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0301000=;".toByteArray())
      if (retValue) barcodeSendCommand("nls0006000;".toByteArray())
      if (retValue) {
         mBarcodeDevice?.prefix = null
         mBarcodeDevice?.suffix = null
      }
      return retValue
   }

   fun barcodeSendCommandLoadUserDefault(): Boolean {
      var retValue = barcodeSendCommand("nls0006010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0001160;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0006000;".toByteArray())
      return retValue
   }

   @Keep
   fun barcodeSendCommandConinuous(): Boolean {
      var retValue = barcodeSendCommand("nls0006010;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0302020;".toByteArray())
      if (retValue) retValue = barcodeSendCommand("nls0006000;".toByteArray())
      return retValue
   }

   fun barcodeSendQuerySystem(): Boolean {
      val datatt =
         byteArrayOf(0x7E, 0x01, 0x30, 0x30, 0x30, 0x30, 0x40, 0x5F, 0x5F, 0x5F, 0x3F, 0x3B, 0x03)
      barcodeSendCommand(datatt)
      val datat = byteArrayOf(
         0x7E,
         0x01,
         0x30,
         0x30,
         0x30,
         0x30,
         0x40,
         0x51,
         0x52,
         0x59,
         0x53,
         0x59,
         0x53,
         0x2C,
         0x50,
         0x44,
         0x4E,
         0x2C,
         0x50,
         0x53,
         0x4E,
         0x3B,
         0X03
      )
      //        return barcodeSendQuery(datat);
      return barcodeSendCommand(datat)
   }

   val barcodeVersion: String?
      get() {
         val strValue = mBarcodeDevice?.version
         if (strValue == null) barcodeSendQueryVersion()
         return strValue
      }

   fun barcodeSendQueryVersion(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x02,
         0x33, 0x47,
         0
      )
      return barcodeSendQuery(data)
   }

   val barcodeESN: String?
      get() {
         val strValue = mBarcodeDevice?.eSN
         if (strValue == null) barcodeSendQueryESN()
         return strValue
      }

   fun barcodeSendQueryESN(): Boolean {
      val datat = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x48, 0x30, 0x32, 0x30, 0xb2.toByte()
      )
      return barcodeSendQuery(datat)
   }

   @get:Keep
   val barcodeSerial: String?
      get() {
         val strValue = mBarcodeDevice?.serialNumber
         if (strValue == null) barcodeSendQuerySerialNumber()
         return strValue
      }

   fun barcodeSendQuerySerialNumber(): Boolean {
      val datat = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x48, 0x30, 0x33, 0x30, 0xb2.toByte()
      )
      return barcodeSendQuery(datat)
   }

   val barcodeDate: String?
      get() {
         var strValue = mBarcodeDevice?.date
         if (strValue == null) barcodeSendQueryDate()
         val strValue1 = barcodeESN
         if (strValue1 != null && strValue1.length != 0) strValue += ", $strValue1"
         return strValue
      }

   fun barcodeSendQueryDate(): Boolean {
      val datat = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x48, 0x30, 0x34, 0x30, 0xb2.toByte()
      )
      return barcodeSendQuery(datat)
   }

   fun barcodeSendQuerySelfPreSuffix(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x02,
         0x33, 0x37, 0xf9.toByte()
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQueryReadingMode(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x44, 0x30, 0x30, 0x30, 0xbd.toByte()
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQueryEnable2dBarCodes(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x02,
         0x33, 0x33,
         0
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQueryPrefixOrder(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x02,
         0x33, 0x42,
         0
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQueryDelayTimeOfEachReading(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x44, 0x30, 0x33, 0x30,
         0
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQueryNoDuplicateReading(): Boolean {
      val data = byteArrayOf(
         0x7E, 0x00,
         0x00, 0x05,
         0x33, 0x44, 0x30, 0x33, 0x31,
         0
      )
      return barcodeSendQuery(data)
   }

   fun barcodeSendQuery(data: ByteArray): Boolean {
      var bytelrc = 0xff.toByte()
      for (i in 2 until data.size - 1) {
         bytelrc = (bytelrc.toInt() xor data[i].toInt()).toByte()
      }
      if (false) appendToLog(
         String.format(
            "BarStream: bytelrc = %02X, last = %02X",
            bytelrc,
            data[data.size - 1]
         )
      )
      data[data.size - 1] = bytelrc
      return barcodeSendCommand(data)
   }

   private fun barcodeSendCommand(barcodeCommandData: ByteArray): Boolean {
      val cs108BarcodeData = Cs108BarcodeData()
      cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_COMMAND
      cs108BarcodeData.waitUplinkResponse = true
      cs108BarcodeData.dataValues = barcodeCommandData
      mBarcodeDevice!!.mBarcodeToWrite.add(cs108BarcodeData)
      return true
   }

   var barcodeAutoStarted = false
   @Keep
   fun barcodeInventory(start: Boolean): Boolean {
      var result = true
      appendToLog("TTestPoint 0: $start")
      if (start) {
         mBarcodeDevice!!.mBarcodeToRead.clear()
         barcodeDataStore = null
         if (!barcodeOnStatus) {
            result = setBarcodeOn(true)
            appendToLog("TTestPoint 1")
         }
         if (barcode2TriggerMode && result) {
            if (triggerButtonStatus && autoBarStartSTop) {
               appendToLog("TTestPoint 2")
               barcodeAutoStarted = true
               result = true
            } else {
               appendToLog("TTestPoint 3")
               result = barcodeSendCommand(byteArrayOf(0x1b, 0x33))
            }
         } else appendToLog("TTestPoint 4")
         appendToLog("TTestPoint 5")
      } else {
         if (!barcode2TriggerMode) {
            appendToLog("TTestPoint 6")
            result = setBarcodeOn(false)
         } else if (!barcodeOnStatus && result) {
            appendToLog("TTestPoint 7")
            result = setBarcodeOn(true)
         }
         if (barcode2TriggerMode && result) {
            if (barcodeAutoStarted && result) {
               appendToLog("TTestPoint 8")
               barcodeAutoStarted = false
               result = true
            } else {
               appendToLog("TTestPoint 9")
               result = barcodeSendCommand(byteArrayOf(0x1b, 0x30))
            }
         } else appendToLog("TTestPoint 10")
      }
      return result
   }

   @get:Keep
   val bluetoothICFirmwareVersion: String
      //Configuration Calls: System
      get() = mBluetoothConnector!!.mBluetoothIcDevice.bluetoothIcVersion

   @get:Keep
   val bluetoothICFirmwareName: String
      get() = mBluetoothConnector!!.mBluetoothIcDevice.bluetoothIcName

   @Keep
   fun setBluetoothICFirmwareName(name: String?): Boolean {
      return mBluetoothConnector!!.mBluetoothIcDevice.setBluetoothIcName(name)
   }

   @Keep
   fun forceBTdisconnect(): Boolean {
      return mBluetoothConnector!!.mBluetoothIcDevice.forceBTdisconnect()
   }

   @Keep
   fun hostProcessorICGetFirmwareVersion(): String? {
      return mSiliconLabIcDevice?.siliconLabIcVersion
   }

   @get:Keep
   val hostProcessorICSerialNumber: String?
      get() {
         val str: String?
         str =
            if (mBluetoothConnector!!.csModel == 108) mSiliconLabIcDevice!!.getSerialNumber() else mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.productSerialNumber
         if (str != null) {
            if (str.length > 13) return str.substring(0, 13)
         }
         return null
      }

   @get:Keep
   val hostProcessorICBoardVersion: String?
      get() {
         val str: String?
         str =
            if (mBluetoothConnector!!.csModel == 108) mSiliconLabIcDevice!!.getSerialNumber() else mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.productSerialNumber
         if (str != null) {
            if (str.length == 16) {
               var strOut = ""
               if (!str.substring(13, 14).matches("0".toRegex())) strOut = str.substring(13, 14)
               strOut += (if (strOut.length != 0) "." else "") + str[14]
               if (!str.substring(15, 16)
                     .matches("0".toRegex()) || strOut.length < 3
               ) strOut += (if (strOut.length < 3) "." else "") + str[15]
               return strOut
            }
         }
         return null
      }

   /*    UpdateHostProcessorFirmwareApplication(filename,result)
    UpdateHostProcssorFirmwareBootloader(filename,result)
    UpdateBluetoothProcessorFirmwareApplication(filename,result)
    UpdateBluetoothProcessorFirmwareBootloader(filename,result)
    */
   @Keep
   fun batteryLevelRequest(): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE
      if (mRfidDevice!!.inventoring) {
         appendToLog("Skip batteryLevelREquest as inventoring !!!")
         return true
      }
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   fun triggerButtoneStatusRequest(): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   @Keep
   fun setBatteryAutoReport(on: Boolean): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         if (on) NotificationPayloadEvents.NOTIFICATION_AUTO_BATTERY_VOLTAGE else NotificationPayloadEvents.NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   @Keep
   fun setAutoRFIDAbort(enable: Boolean): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_AUTO_RFIDINV_ABORT
      cs108NotificatiionData.dataValues = ByteArray(1)
      mNotificationDevice!!.autoRfidAbortStatus = enable
      cs108NotificatiionData.dataValues!![0] = if (enable) 1.toByte() else 0
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   @get:Keep
   val autoRFIDAbort: Boolean = mNotificationDevice!!.autoRfidAbortStatus

   @Keep
   fun setAutoBarStartSTop(enable: Boolean): Boolean {
      val autoBarStartStopStatus = autoBarStartSTop
      if (enable and autoBarStartStopStatus) return true else if (!enable && !autoBarStartStopStatus) return true
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_AUTO_BARINV_STARTSTOP
      cs108NotificatiionData.dataValues = ByteArray(1)
      mNotificationDevice!!.autoBarStartStopStatus = enable
      cs108NotificatiionData.dataValues!![0] = if (enable) 1.toByte() else 0
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   @get:Keep
   val autoBarStartSTop: Boolean
      get() = mNotificationDevice!!.autoBarStartStopStatus
   var triggerReporting = true
   fun setTriggerReporting(triggerReporting: Boolean): Boolean {
      var bValue = false
      //if (this.triggerReporting == triggerReporting) return true;
      bValue = if (triggerReporting) {
         setAutoTriggerReporting(triggerReportingCountSetting.toByte())
      } else stopAutoTriggerReporting()
      if (bValue) this.triggerReporting = triggerReporting
      return bValue
   }

   val iNO_SUCH_SETTING = 10000
   var triggerReportingCountSetting: Short = 1
   val triggerReportingCount: Short
      get() {
         var bValue = false
         if (getcsModel() == 108) bValue =
            checkHostProcessorVersion(hostProcessorICGetFirmwareVersion(), 1, 0, 16)
         return if (!bValue) iNO_SUCH_SETTING.toShort() else triggerReportingCountSetting
      }

   fun setTriggerReportingCount(triggerReportingCount: Short): Boolean {
      var bValue = false
      if (triggerReportingCount < 0 || triggerReportingCount > 255) return false
      bValue = if (triggerReporting) {
         if (triggerReportingCountSetting == triggerReportingCount) return true
         setAutoTriggerReporting((triggerReportingCount.toInt() and 0xFF).toByte())
      } else true
      if (bValue) triggerReportingCountSetting = triggerReportingCount
      return true
   }

   fun setAutoTriggerReporting(timeSecond: Byte): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_AUTO_TRIGGER_REPORT
      cs108NotificatiionData.dataValues = ByteArray(1)
      cs108NotificatiionData.dataValues!![0] = timeSecond
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   fun stopAutoTriggerReporting(): Boolean {
      val cs108NotificatiionData = Cs108NotificatiionData()
      cs108NotificatiionData.notificationPayloadEvent =
         NotificationPayloadEvents.NOTIFICATION_STOP_TRIGGER_REPORT
      return mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
   }

   @Keep
   fun getBatteryDisplay(voltageDisplay: Boolean): String {
      val floatValue = batteryLevel.toFloat() / 1000
      if (floatValue == 0f) return " "
      var retString: String? = null
      retString = if (voltageDisplay || batteryDisplaySetting == 0) String.format(
         "%.3f V",
         floatValue
      ) else String.format("%d", getBatteryValue2Percent(floatValue)) + "%"
      if (!voltageDisplay) retString += String.format("\r\n P=%d", pwrlevel)
      return retString
   }

   var strVersionMBoard = "1.8"
   var strMBoardVersions = strVersionMBoard.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
      .toTypedArray()
   var iBatteryNewCurveDelay = 0
   var bUsingInventoryBatteryCurve = false
   var fBatteryValueOld = 0f
   var iBatteryPercentOld = 0

   @get:Keep
   val isBatteryLow: String?
      get() {
         var batterylow = false
         val iValue = batteryLevel
         if (iValue == 0) return null
         val fValue = iValue.toFloat() / 1000
         val iPercent = getBatteryValue2Percent(fValue)
         if (checkHostProcessorVersion(
               hostProcessorICBoardVersion,
               strMBoardVersions[0].trim { it <= ' ' }
                  .toInt(),
               strMBoardVersions[1].trim { it <= ' ' }.toInt(),
               0
            )
         ) {
            if (mRfidDevice!!.inventoring) {
               if (fValue < 3.520) batterylow = true
            } else if (!bUsingInventoryBatteryCurve) {
               if (fValue < 3.626) batterylow = true
            }
         } else {
            if (mRfidDevice!!.inventoring) {
               if (fValue < 3.45) batterylow = true
            } else if (!bUsingInventoryBatteryCurve) {
               if (fValue < 3.6) batterylow = true
            }
         }
         return if (batterylow) iPercent.toString() else null
      }
   var iBatteryCount = 0
   fun getBatteryValue2Percent(floatValue: Float): Int {
      val DEBUG = false
      if (DEBUG) appendToLog("getHostProcessorICBoardVersion = " + hostProcessorICBoardVersion + ", strVersionMBoard = " + strVersionMBoard)
      return if (checkHostProcessorVersion(
            hostProcessorICBoardVersion,
            strMBoardVersions[0].trim { it <= ' ' }
               .toInt(),
            strMBoardVersions[1].trim { it <= ' ' }.toInt(),
            0
         )
      ) {
         val fValueStbyRef = floatArrayOf(
            4.212.toFloat(),
            4.175.toFloat(),
            4.154.toFloat(),
            4.133.toFloat(),
            4.112.toFloat(),
            4.085.toFloat(),
            4.069.toFloat(),
            4.054.toFloat(),
            4.032.toFloat(),
            4.011.toFloat(),
            3.990.toFloat(),
            3.969.toFloat(),
            3.953.toFloat(),
            3.937.toFloat(),
            3.922.toFloat(),
            3.901.toFloat(),
            3.885.toFloat(),
            3.869.toFloat(),
            3.853.toFloat(),
            3.837.toFloat(),
            3.821.toFloat(),
            3.806.toFloat(),
            3.790.toFloat(),
            3.774.toFloat(),
            3.769.toFloat(),
            3.763.toFloat(),
            3.758.toFloat(),
            3.753.toFloat(),
            3.747.toFloat(),
            3.742.toFloat(),
            3.732.toFloat(),
            3.721.toFloat(),
            3.705.toFloat(),
            3.684.toFloat(),
            3.668.toFloat(),
            3.652.toFloat(),
            3.642.toFloat(),
            3.626.toFloat(),
            3.615.toFloat(),
            3.605.toFloat(),
            3.594.toFloat(),
            3.584.toFloat(),
            3.568.toFloat(),
            3.557.toFloat(),
            3.542.toFloat(),
            3.531.toFloat(),
            3.510.toFloat(),
            3.494.toFloat(),
            3.473.toFloat(),
            3.457.toFloat(),
            3.436.toFloat(),
            3.410.toFloat(),
            3.362.toFloat(),
            3.235.toFloat(),
            2.987.toFloat(),
            2.982.toFloat()
         )
         val fPercentStbyRef = floatArrayOf(
            100f,
            98f,
            96f,
            95f,
            93f,
            91f,
            89f,
            87f,
            85f,
            84f,
            82f,
            80f,
            78f,
            76f,
            75f,
            73f,
            71f,
            69f,
            67f,
            65f,
            64f,
            62f,
            60f,
            58f,
            56f,
            55f,
            53f,
            51f,
            49f,
            47f,
            45f,
            44f,
            42f,
            40f,
            38f,
            36f,
            35f,
            33f,
            31f,
            29f,
            27f,
            25f,
            24f,
            22f,
            20f,
            18f,
            16f,
            15f,
            13f,
            11f,
            9f,
            7f,
            5f,
            4f,
            2f,
            0f
         )
         val fValueRunRef = floatArrayOf(
            4.106.toFloat(),
            4.017.toFloat(),
            3.98.toFloat(),
            3.937.toFloat(),
            3.895.toFloat(),
            3.853.toFloat(),
            3.816.toFloat(),
            3.779.toFloat(),
            3.742.toFloat(),
            3.711.toFloat(),
            3.679.toFloat(),
            3.658.toFloat(),
            3.637.toFloat(),
            3.626.toFloat(),
            3.61.toFloat(),
            3.584.toFloat(),
            3.547.toFloat(),
            3.515.toFloat(),
            3.484.toFloat(),
            3.457.toFloat(),
            3.431.toFloat(),
            3.399.toFloat(),
            3.362.toFloat(),
            3.32.toFloat(),
            3.251.toFloat(),
            3.135.toFloat()
         )
         val fPercentRunRef = floatArrayOf(
            100f,
            96f,
            92f,
            88f,
            84f,
            80f,
            76f,
            72f,
            67f,
            63f,
            59f,
            55f,
            51f,
            47f,
            43f,
            39f,
            35f,
            31f,
            27f,
            23f,
            19f,
            15f,
            11f,
            7f,
            2f,
            0f
         )
         var fValueRef = fValueStbyRef
         var fPercentRef = fPercentStbyRef
         if (iBatteryCount != batteryCount) {
            iBatteryCount = batteryCount
            iBatteryNewCurveDelay++
         }
         if (mRfidDevice!!.mRfidToWrite.size != 0) iBatteryNewCurveDelay =
            0 else if (mRfidDevice!!.inventoring) {
            if (!bUsingInventoryBatteryCurve) {
               if (iBatteryNewCurveDelay > 1) {
                  iBatteryNewCurveDelay = 0
                  bUsingInventoryBatteryCurve = true
               }
            } else iBatteryNewCurveDelay = 0
         } else if (bUsingInventoryBatteryCurve) {
            if (iBatteryNewCurveDelay > 2) {
               iBatteryNewCurveDelay = 0
               bUsingInventoryBatteryCurve = false
            }
         } else iBatteryNewCurveDelay = 0
         if (bUsingInventoryBatteryCurve) {
            fValueRef = fValueRunRef
            fPercentRef = fPercentRunRef
         }
         if (DEBUG) appendToLog("NEW Percentage cureve is USED with bUsingInventoryBatteryCurve = $bUsingInventoryBatteryCurve, iBatteryNewCurveDelay = $iBatteryNewCurveDelay")
         var index = 0
         while (index < fValueRef.size) {
            if (floatValue > fValueRef[index]) break
            index++
         }
         if (DEBUG) appendToLog("Index = $index")
         if (index == 0) return 100
         if (index == fValueRef.size) return 0
         var value = (fValueRef[index - 1] - floatValue) / (fValueRef[index - 1] - fValueRef[index])
         value *= fPercentRef[index - 1] - fPercentRef[index]
         value = fPercentRef[index - 1] - value
         value += 0.5.toFloat()
         var iValue = value.toInt()
         if (iBatteryNewCurveDelay != 0) iValue =
            iBatteryPercentOld else if (bUsingInventoryBatteryCurve && floatValue <= fBatteryValueOld && iValue >= iBatteryPercentOld) iValue =
            iBatteryPercentOld
         fBatteryValueOld = floatValue
         iBatteryPercentOld = iValue
         iValue
      } else {
         if (DEBUG) appendToLog("OLD Percentage cureve is USED")
         if (floatValue >= 4) 100 else if (floatValue < 3.4) 0 else {
            val result = 166.67.toFloat() * floatValue - 566.67.toFloat()
            result.toInt()
         }
      }
   }

   @get:Keep
   val batteryLevel: Int
      get() = mCs108ConnectorData!!.voltageMv

   @get:Keep
   val batteryCount: Int
      get() = mCs108ConnectorData!!.voltageCnt

   @get:Keep
   val triggerButtonStatus: Boolean
      get() = mNotificationDevice!!.triggerStatus
   val triggerCount: Int
      get() = mCs108ConnectorData!!.triggerCount

   @Keep
   fun setNotificationListener(listener: NotificationListener?) {
      mNotificationDevice!!.setNotificationListener0(listener)
   }

   @get:Keep
   var batteryDisplaySetting = 1
   @Keep
   fun setBatteryDisplaySetting(batteryDisplaySelect: Int): Boolean {
      if (batteryDisplaySelect < 0 || batteryDisplaySelect > 1) return false
      batteryDisplaySetting = batteryDisplaySelect
      return true
   }

   val dBuV_dBm_constant = 106.98

   @get:Keep
   var rssiDisplaySetting = 1
   @Keep
   fun setRssiDisplaySetting(rssiDisplaySelect: Int): Boolean {
      if (rssiDisplaySelect < 0 || rssiDisplaySelect > 1) return false
      rssiDisplaySetting = rssiDisplaySelect
      return true
   }

   @get:Keep
   var vibrateModeSetting = 0
   @Keep
   fun setVibrateModeSetting(vibrateModeSelect: Int): Boolean {
      if (vibrateModeSelect < 0 || vibrateModeSelect > 1) return false
      vibrateModeSetting = vibrateModeSelect
      return true
   }

   var savingFormatSetting = 0
   fun setSavingFormatSetting(savingFormatSelect: Int): Boolean {
      if (false) appendToLog("savingFormatSelect = $savingFormatSelect")
      if (savingFormatSelect < 0 || savingFormatSelect > 1) return false
      savingFormatSetting = savingFormatSelect
      return true
   }

   enum class CsvColumn {
      RESERVE_BANK, EPC_BANK, TID_BANK, USER_BANK, PHASE, CHANNEL, TIME, TIMEZONE, LOCATION, DIRECTION, OTHERS
   }

   var csvColumnSelectSetting = 0
   fun setCsvColumnSelectSetting(csvColumnSelect: Int): Boolean {
      if (false) appendToLog("csvColumnSelect = $csvColumnSelect")
      csvColumnSelectSetting = csvColumnSelect
      return true
   }

   @get:Keep
   val newDeviceScanned: Cs108ScanData?
      get() = if (mScanResultList!!.size != 0) {
         if (false) appendToLog("mScanResultList.size() = " + mScanResultList!!.size)
         val cs108ScanData = mScanResultList!![0]
         mScanResultList!!.removeAt(0)
         cs108ScanData
      } else null

   @Keep
   fun onRFIDEvent(): Rx000pkgData? {
      var rx000pkgData: Rx000pkgData? = null
      //if (mrfidToWriteSize() != 0) mRfidDevice.mRfidReaderChip.mRx000ToRead.clear();
      if (!mRfidDevice!!.mRfidReaderChip!!.bRx000ToReading && mRfidDevice!!.mRfidReaderChip!!.mRx000ToRead?.size != 0) {
         mRfidDevice!!.mRfidReaderChip!!.bRx000ToReading = true
         val index = 0
         try {
            rx000pkgData = mRfidDevice!!.mRfidReaderChip!!.mRx000ToRead?.get(index)
            if (false) appendToLog("rx000pkgData.type = " + rx000pkgData?.responseType.toString())
            mRfidDevice!!.mRfidReaderChip!!.mRx000ToRead?.removeAt(index) //appendToLog("mRx000ToRead.remove");
         } catch (ex: Exception) {
            rx000pkgData = null
         }
         mRfidDevice!!.mRfidReaderChip!!.bRx000ToReading = false
      }
      return rx000pkgData
   }

   @Keep
   fun onNotificationEvent(): ByteArray? {
      var notificationData: ByteArray? = null
      if (mNotificationDevice!!.mNotificationToRead.size != 0) {
         val cs108NotificatiionData = mNotificationDevice!!.mNotificationToRead[0]
         mNotificationDevice!!.mNotificationToRead.removeAt(0)
         if (cs108NotificatiionData != null) notificationData = cs108NotificatiionData.dataValues
      }
      return notificationData
   }

   var barcodeDataStore: ByteArray? = null
   var timeBarcodeData: Long = 0
   @Keep
   fun onBarcodeEvent(): ByteArray? {
      var barcodeData: ByteArray? = null
      if (mBarcodeDevice!!.mBarcodeToRead.size != 0) {
         val cs108BarcodeData = mBarcodeDevice!!.mBarcodeToRead[0]
         mBarcodeDevice!!.mBarcodeToRead.removeAt(0)
         if (cs108BarcodeData != null) {
            if (cs108BarcodeData.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_GOOD_READ) {
               if (false) barcodeData = "<GR>".toByteArray()
            } else if (cs108BarcodeData.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_DATA_READ) {
               barcodeData = cs108BarcodeData.dataValues
            }
         }
      }
      var barcodeCombined: ByteArray? = null
      if (barcodeData != null) {
         appendToLog(
            "BarStream: barcodeData = " + byteArrayToString(barcodeData) + ", barcodeDataStore = " + byteArrayToString(
               barcodeDataStore
            )
         )
         var barcodeDataStoreIndex = 0
         var length = barcodeData.size
         if (barcodeDataStore != null) {
            barcodeDataStoreIndex = barcodeDataStore!!.size
            length += barcodeDataStoreIndex
         }
         barcodeCombined = ByteArray(length)
         if (barcodeDataStore != null) System.arraycopy(
            barcodeDataStore,
            0,
            barcodeCombined,
            0,
            barcodeDataStore!!.size
         )
         System.arraycopy(barcodeData, 0, barcodeCombined, barcodeDataStoreIndex, barcodeData.size)
         barcodeDataStore = barcodeCombined
         timeBarcodeData = System.currentTimeMillis()
         barcodeCombined = ByteArray(0)
      }
      if (barcodeDataStore != null) {
         barcodeCombined = ByteArray(barcodeDataStore!!.size)
         System.arraycopy(barcodeDataStore, 0, barcodeCombined, 0, barcodeCombined.size)
         if (System.currentTimeMillis() - timeBarcodeData < 300) barcodeCombined =
            null else barcodeDataStore = null
      }
      if (barcodeCombined != null && mBarcodeDevice?.prefix != null && mBarcodeDevice?.suffix != null) {
         if (barcodeCombined.size == 0) barcodeCombined = null else {
            val prefixExpected = mBarcodeDevice?.prefix
            var prefixFound = false
            val suffixExpected = mBarcodeDevice?.suffix
            var suffixFound = false
            val codeTypeLength = 4
            appendToLog(
               "BarStream: barcodeCombined = " + byteArrayToString(barcodeCombined) + ", Expected Prefix = " + byteArrayToString(
                  prefixExpected
               ) + ", Expected Suffix = " + byteArrayToString(suffixExpected)
            )
            if (barcodeCombined.size > prefixExpected!!.size + suffixExpected!!.size + codeTypeLength) {
               var i = 0
               while (i <= barcodeCombined.size - prefixExpected!!.size - suffixExpected!!.size) {
                  var j = 0
                  while (j < prefixExpected!!.size) {
                     if (barcodeCombined[i + j] != prefixExpected!![j]) break
                     j++
                  }
                  if (j == prefixExpected!!.size) {
                     prefixFound = true
                     break
                  }
                  i++
               }
               var k = i + prefixExpected!!.size
               while (k <= barcodeCombined.size - suffixExpected!!.size) {
                  var j = 0
                  while (j < suffixExpected!!.size) {
                     if (barcodeCombined[k + j] != suffixExpected!![j]) break
                     j++
                  }
                  if (j == suffixExpected!!.size) {
                     suffixFound = true
                     break
                  }
                  k++
               }
               appendToLog("BarStream: iPrefix = $i, iSuffix = $k, with prefixFound = $prefixFound, suffixFound = $suffixFound")
               if (prefixFound && suffixFound) {
                  var barcodeCombinedNew = ByteArray(k - i - prefixExpected!!.size - codeTypeLength)
                  System.arraycopy(
                     barcodeCombined,
                     i + prefixExpected!!.size + codeTypeLength,
                     barcodeCombinedNew,
                     0,
                     barcodeCombinedNew.size
                  )
                  barcodeCombined = barcodeCombinedNew
                  appendToLog(
                     "BarStream: barcodeCombinedNew = " + byteArrayToString(
                        barcodeCombinedNew
                     )
                  )
                  val prefixExpected1 = byteArrayOf(0x5B, 0x29, 0x3E, 0x1E)
                  prefixFound = false
                  val suffixExpected1 = byteArrayOf(0x1E, 0x04)
                  suffixFound = false
                  appendToLog(
                     "BarStream: barcodeCombined = " + byteArrayToString(barcodeCombined) + ", Expected Prefix = " + byteArrayToString(
                        prefixExpected1
                     ) + ", Expected Suffix = " + byteArrayToString(suffixExpected1)
                  )
                  if (barcodeCombined.size > prefixExpected1.size + suffixExpected1.size) {
                     i = 0
                     while (i <= barcodeCombined.size - prefixExpected1.size - suffixExpected1.size) {
                        var j = 0
                        while (j < prefixExpected1.size) {
                           if (barcodeCombined[i + j] != prefixExpected1[j]) break
                           j++
                        }
                        if (j == prefixExpected1.size) {
                           prefixFound = true
                           break
                        }
                        i++
                     }
                     k = i + prefixExpected1.size
                     while (k <= barcodeCombined.size - suffixExpected1.size) {
                        var j = 0
                        while (j < suffixExpected1.size) {
                           if (barcodeCombined[k + j] != suffixExpected1[j]) break
                           j++
                        }
                        if (j == suffixExpected1.size) {
                           suffixFound = true
                           break
                        }
                        k++
                     }
                     appendToLog("BarStream: iPrefix = $i, iSuffix = $k, with prefixFound = $prefixFound, suffixFound = $suffixFound")
                     if (prefixFound && suffixFound) {
                        barcodeCombinedNew = ByteArray(k - i - prefixExpected1.size)
                        System.arraycopy(
                           barcodeCombined,
                           i + prefixExpected1.size,
                           barcodeCombinedNew,
                           0,
                           barcodeCombinedNew.size
                        )
                        barcodeCombined = barcodeCombinedNew
                        appendToLog(
                           "BarStream: barcodeCombinedNew = " + byteArrayToString(
                              barcodeCombinedNew
                           )
                        )
                     }
                  }
               }
            } else barcodeCombined = null
         }
      }
      return barcodeCombined
   }

   @get:Keep
   val modelNumber: String
      get() {
         val iCountryCode = countryCode
         var strCountryCode = ""
         val strModelName = modelName //"CS108";
         appendToLog("iCountryCode = $iCountryCode, strModelNumber = $strModelName")
         if (strModelName != null && strModelName.length != 0) {
            strCountryCode =
               if (iCountryCode > 0) strModelName + "-" + iCountryCode + " " + mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.specialCountryVersion else strModelName
         }
         return strCountryCode
      }

   @get:Keep
   val modelName: String?
      get() = mSiliconLabIcDevice!!.getModelName()

   @Keep
   fun setRx000KillPassword(password: String): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setRx000KillPassword(password)
   }

   @Keep
   fun setRx000AccessPassword(password: String?): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setRx000AccessPassword(password)
   }

   @Keep
   fun setAccessRetry(accessVerfiy: Boolean, accessRetry: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessRetry(accessVerfiy, accessRetry)
   }

   @Keep
   fun setInvModeCompact(invModeCompact: Boolean): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvModeCompact(invModeCompact)
   }

   @Keep
   fun setAccessLockAction(accessLockAction: Int, accessLockMask: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessLockAction(
         accessLockAction,
         accessLockMask
      )
   }

   @Keep
   fun setAccessBank(accessBank: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessBank(accessBank)
   }

   @Keep
   fun setAccessBank(accessBank: Int, accessBank2: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessBank(accessBank, accessBank2)
   }

   @Keep
   fun setAccessOffset(accessOffset: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessOffset(accessOffset)
   }

   @Keep
   fun setAccessOffset(accessOffset: Int, accessOffset2: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessOffset(
         accessOffset,
         accessOffset2
      )
   }

   @Keep
   fun setAccessCount(accessCount: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessCount(accessCount)
   }

   @Keep
   fun setAccessCount(accessCount: Int, accessCount2: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessCount(accessCount, accessCount2)
   }

   @Keep
   fun setAccessWriteData(dataInput: String): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAccessWriteData(dataInput)
   }

   @Keep
   fun setTagRead(tagRead: Int): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setTagRead(tagRead)
   }

   @Keep
   fun sendHostRegRequestHST_CMD(hostCommand: HostCommands?): Boolean {
      mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
      return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(hostCommand)
   }

   fun setPwrManagementMode(bLowPowerStandby: Boolean): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(bLowPowerStandby)
   }

   @get:Keep
   val serialNumber: String?
      get() = mRfidDevice!!.mRfidReaderChip!!.mRx000OemSetting.getSerialNumber()

   @Keep
   fun setInvBrandId(invBrandId: Boolean): Boolean {
      return mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setInvBrandId(invBrandId)
   }

   @Keep
   fun macRead(address: Int) {
      mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.readMAC(address)
   }

   fun macWrite(address: Int, value: Long) {
      mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(address, value)
   }

   fun set_fdCmdCfg(value: Int) {
      macWrite(0x117, value.toLong())
   }

   fun set_fdRegAddr(addr: Int) {
      macWrite(0x118, addr.toLong())
   }

   fun set_fdWrite(addr: Int, value: Long) {
      macWrite(0x118, addr.toLong())
      macWrite(0x119, value)
   }

   fun set_fdPwd(value: Int) {
      macWrite(0x11A, value.toLong())
   }

   fun set_fdBlockAddr4GetTemperature(addr: Int) {
      macWrite(0x11b, addr.toLong())
   }

   fun set_fdReadMem(addr: Int, len: Long) {
      macWrite(0x11c, addr.toLong())
      macWrite(0x11d, len)
   }

   fun set_fdWriteMem(addr: Int, len: Int, value: Long) {
      set_fdReadMem(addr, len.toLong())
      macWrite(0x11e, value)
   }

   fun setImpinJExtension(tagFocus: Boolean, fastId: Boolean) {
      var iValue = 0
      if (tagFocus) iValue = iValue or 0x10
      if (fastId) iValue = iValue or 0x20
      macWrite(0x203, iValue.toLong())
   }

   var fTemperature_old = -500f

   init {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         mScanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult>) {
               if (DEBUG) appendToLog("onBatchScanResults()")
            }

            override fun onScanFailed(errorCode: Int) {
               if (DEBUG) appendToLog("onScanFailed()")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult) {
               val DEBUG = false
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                  val scanResultA = Cs108ScanData(
                     result.device, result.rssi, result.scanRecord!!
                        .bytes
                  )
                  var found98 = true
                  found98 = check9800(scanResultA)
                  if (DEBUG) appendToLog("found98 = " + found98 + ", mScanResultList 0 = " + if (mScanResultList != null) "VALID" else "NULL")
                  if (mScanResultList != null && found98) {
                     scanResultA.serviceUUID2p2 = check9800_serviceUUID2p1
                     mScanResultList!!.add(scanResultA)
                     if (DEBUG) appendToLog("mScanResultList 0 = " + mScanResultList!!.size)
                  }
               }
            }
         }
      } else {
         mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            appendToLog("onLeScan()")
            val scanResultA = Cs108ScanData(device, rssi, scanRecord)
            var found98 = true
            found98 = check9800(scanResultA)
            appendToLog("found98 = " + found98 + ", mScanResultList 1 = " + if (mScanResultList != null) "VALID" else "NULL")
            if (mScanResultList != null && found98) {
               scanResultA.serviceUUID2p2 = check9800_serviceUUID2p1
               mScanResultList!!.add(scanResultA)
               appendToLog("mScanResultList 1 = " + mScanResultList!!.size)
            }
         }
      }
      val path = context.filesDir
      val fileArray = path.listFiles()
      val deleteFiles = false
      if (DEBUG) appendToLog("Number of file in data storage sub-directory = " + fileArray.size)
      for (i in fileArray.indices) {
         val fileName = fileArray[i].toString()
         if (DEBUG) appendToLog("Stored file ($i) = $fileName")
         val file = File(fileName)
         if (deleteFiles) file.delete()
      }
      if (deleteFiles) if (DEBUG) appendToLog("Stored file size after DELETE = " + path.listFiles().size)
      if (false) {
         val tableFreq = FCCTableOfFreq
         val tableFreq0 = FCCTableOfFreq0
         fccFreqSortedIdx0 = IntArray(50)
         for (i in 0..49) {
            for (j in 0..49) {
               if (FCCTableOfFreq0[i] == FCCTableOfFreq[j]) {
                  fccFreqSortedIdx0[i] = j
                  if (DEBUG) appendToLog("fccFreqSortedIdx0[$i] = $j")
                  break
               }
            }
         }
         val tableFreq1 = FCCTableOfFreq1
         fccFreqSortedIdx1 = IntArray(50)
         for (i in 0..49) {
            for (j in 0..49) {
               if (FCCTableOfFreq1[i] == FCCTableOfFreq[j]) {
                  fccFreqSortedIdx1[i] = j
                  if (DEBUG) appendToLog("fccFreqSortedIdx1[$i] = $j")
                  break
               }
            }
         }
      }
      fccFreqTableIdx = IntArray(50)
      val freqSortedINx = fccFreqSortedIdx
      for (i in 0..49) {
         fccFreqTableIdx[fccFreqSortedIdx[i]] = i
      }
      for (i in 0..49) {
         if (DEBUG) appendToLog("fccFreqTableIdx[" + i + "] = " + fccFreqTableIdx[i])
      }
      if (false) {    //for testing
         var fValue: Float
         fValue = (3.124 - (3.124 - 2.517) / 2).toFloat()
         appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue))
         fValue = (3.394 - (3.394 - 3.124) / 2).toFloat()
         appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue))
         fValue = (3.504 - (3.504 - 3.394) / 2).toFloat()
         appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue))
         fValue = (3.552 - (3.552 - 3.504) / 2).toFloat()
         appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue))
      }
   }

   fun decodeCtesiusTemperature(strActData: String, strCalData: String): Float {
      var fTemperature = -500f
      var invalid = false
      appendToLog("Hello9: strActData = $strActData, strCalData = $strCalData")
      if (strActData.length != 8 || strCalData.length != 8) {
         if (strActData.length != 8) appendToLogView("Warning: Invalid length of sensing data = $strActData") else appendToLogView(
            "Warning: Invalid length of calibration data = $strCalData"
         )
         invalid = true
      } else if (!(strActData.substring(0, 1).matches("F".toRegex()) && strActData.substring(4, 5)
            .matches("F".toRegex()))
      ) {
         appendToLogView("Warning: Not F header of sensing data = $strActData")
         invalid = true
      } else {
         val strTemp = strActData.substring(4, 8)
         var iTemp = strTemp.toInt(16)
         var iChecksum = 0
         var i = 0
         while (i < 5) {
            iChecksum = iChecksum xor (iTemp and 0x7)
            i++
            iTemp = iTemp shr 3
         }
         if (iChecksum != 0) {
            appendToLogView("Warning: Invalid checksum($iChecksum) for strActData = $strActData")
            invalid = true
         }
      }
      var iDelta1 = strCalData.substring(0, 4).toInt(16)
      if (iDelta1 and 0x8000 != 0) {
         iDelta1 = iDelta1 xor 0xFFFF
         iDelta1++
         iDelta1 *= -1
      }
      appendToLog(String.format("iDelta1 = %d", iDelta1))
      val iVersion = strCalData.substring(4, 5).toInt(16)
      appendToLog("Hello9: " + String.format("iDelta1 = %X, iVersion = %X", iDelta1, iVersion))
      val fDelta2 = iDelta1.toFloat() / 100 - 101
      val strTemp = strActData.substring(1, 4) + strActData.substring(5, 8)
      val iTemp = strTemp.toInt(16)
      val iD1 = iTemp and 0xF80000 shr 19
      val iD2 = iTemp and 0x7FFF8 shr 3
      if (iVersion == 0 || iVersion == 1) fTemperature =
         (11984.47 / (21.25 + iD1 + iD2 / 2752 + fDelta2) - 301.57).toFloat() else if (iVersion == 2) {
         fTemperature = (11109.6 / (24 + (iD2 + iDelta1) / 375.3) - 290).toFloat()
         if (fTemperature >= 125) fTemperature = (fTemperature * 1.2 - 25).toFloat()
      } else appendToLogView("Warning: Invalid version $iVersion")
      if (invalid) appendToLogView(String.format("Temperature = %f", fTemperature))
      if (fTemperature != -1f) fTemperature_old = fTemperature
      return fTemperature
   }

   fun decodeMicronTemperature(iTag35: Int, strActData: String?, strCalData: String?): Float {
      var fTemperature = -1f
      if (strActData == null || strCalData == null) {
      } else if (strActData.length != 4 || strCalData.length != 16) {
      } else if (strActData.matches("0000".toRegex())) {
         fTemperature = fTemperature_old
      } else if (iTag35 == 3) {
         val calCode1: Int
         var calTemp1: Int
         var calCode2: Int
         var calTemp2: Int
         val crc = strCalData.substring(0, 4).toInt(16)
         calCode1 = strCalData.substring(4, 7).toInt(16)
         calTemp1 = strCalData.substring(7, 10).toInt(16)
         calTemp1 = calTemp1 shr 1
         calCode2 = strCalData.substring(9, 13).toInt(16)
         calCode2 = calCode2 shr 1
         calCode2 = calCode2 and 0xFFF
         calTemp2 = strCalData.substring(12, 16).toInt(16)
         calTemp2 = calTemp2 shr 2
         calTemp2 = calTemp2 and 0x7FF
         fTemperature = strActData.toInt(16).toFloat()
         fTemperature =
            (calTemp2.toFloat() - calTemp1.toFloat()) * (fTemperature - calCode1.toFloat())
         fTemperature /= calCode2.toFloat() - calCode1.toFloat()
         fTemperature += calTemp1.toFloat()
         fTemperature -= 800f
         fTemperature /= 10f
      } else if (iTag35 == 5) {
         var iTemp: Int
         var calCode2 = strCalData.substring(0, 4).toInt(16).toFloat()
         calCode2 /= 16f
         iTemp = strCalData.substring(4, 8).toInt(16)
         iTemp = iTemp and 0x7FF
         var calTemp2 = iTemp.toFloat()
         calTemp2 -= 600f
         calTemp2 /= 10f
         var calCode1 = strCalData.substring(8, 12).toInt(16).toFloat()
         calCode1 /= 16f
         iTemp = strCalData.substring(12, 16).toInt(16)
         iTemp = iTemp and 0x7FF
         var calTemp1 = iTemp.toFloat()
         calTemp1 -= 600f
         calTemp1 /= 10f
         fTemperature = strActData.toInt(16).toFloat()
         fTemperature -= calCode1
         fTemperature *= calTemp2 - calTemp1
         fTemperature /= calCode2 - calCode1
         fTemperature += calTemp1
      }
      if (fTemperature != -1f) fTemperature_old = fTemperature
      return fTemperature
   }

   fun decodeAsygnTemperature(string: String): Float {
      return utility.decodeAsygnTemperature(string)
   } //4278

   fun float16toFloat32(strData: String): Float {
      var fValue = -1f
      if (strData.length == 4) {
         val iValue = strData.toInt(16)
         var iSign = iValue and 0x8000
         if (iSign != 0) iSign = 1
         val iExp = iValue and 0x7C00 shr 10
         val iMant = iValue and 0x3FF
         if (iExp == 15) {
            fValue = if (iSign == 0) Float.POSITIVE_INFINITY else Float.NEGATIVE_INFINITY
         } else if (iExp == 0) {
            fValue = (iMant / 1024 * 2 xor -14).toFloat()
            if (iSign != 0) fValue *= -1f
         } else {
            fValue = Math.pow(2.0, (iExp - 15).toDouble()).toFloat()
            fValue *= 1 + iMant.toFloat() / 1024
            if (iSign != 0) fValue *= -1f
         }
         if (DEBUG) appendToLog("strData = $strData, iValue = $iValue, iSign = $iSign, iExp = $iExp, iMant = $iMant, fValue = $fValue")
      }
      return fValue
   }

   fun strFloat16toFloat32(strData: String): String? {
      val strValue: String? = null
      val fTemperature = float16toFloat32(strData)
      return if (fTemperature > -400) String.format("%.1f", fTemperature) else strValue
   }

   fun str2float16(strData: String): String {
      var strValue = ""
      val fValue0 = Math.pow(2.0, -14.0).toFloat()
      val fValueMax = 2 * Math.pow(2.0, 30.0).toFloat()
      val fValue = strData.toFloat()
      var fValuePos = if (fValue > 0) fValue else -fValue
      val bSign = fValue < 0
      var iExp: Int
      val iMant: Int
      if (fValuePos < fValueMax) {
         if (fValuePos < fValue0) {
            iExp = 0
            iMant = (fValuePos / fValue0 * 1024).toInt()
         } else {
            iExp = 1
            while (iExp < 31) {
               if (fValuePos < 2 * Math.pow(2.0, (iExp - 15).toDouble()).toFloat()) break
               iExp++
            }
            fValuePos /= Math.pow(2.0, (iExp - 15).toDouble()).toFloat()
            fValuePos -= 1f
            fValuePos *= 1024f
            iMant = fValuePos.toInt()
         }
         val iValue = (if (bSign) 0x8000 else 0) + (iExp shl 10) + iMant
         strValue = String.format("%04X", iValue)
         if (DEBUG) appendToLog("bSign = $bSign, iExp = $iExp, iMant = $iMant, iValue = $iValue, strValue = $strValue")
      }
      return strValue
   }

   fun temperatureC2F(fTemp: Float): Float {
      return (32 + fTemp * 1.8).toFloat()
   }

   fun temperatureC2F(strValue: String): String {
      try {
         var fValue = strValue.toFloat()
         fValue = temperatureC2F(fValue)
         return String.format("%.1f", fValue)
      } catch (ex: Exception) {
      }
      return ""
   }

   fun temperatureF2C(fTemp: Float): Float {
      return ((fTemp - 32) * 0.5556).toFloat()
   }

   fun temperatureF2C(strValue: String): String {
      try {
         var fValue = strValue.toFloat()
         fValue = temperatureF2C(fValue)
         return String.format("%.1f", fValue)
      } catch (ex: Exception) {
      }
      return ""
   }

   fun get98XX(): Int {
      return 0
   } /*
    private String wedgePrefix = null, wedgeSuffix = null;
    private int wedgeDelimiter = 0x0a;
    public String getWedgePrefix() { return wedgePrefix; }
    public String getWedgeSuffix() { return wedgeSuffix; }
    public int getWedgeDelimiter() { return wedgeDelimiter; }
    public void setWedgePrefix(String string) { wedgePrefix = string; }
    public void setWedgeSuffix(String string) { wedgeSuffix = string; }
    public void setWedgeDelimiter(int iValue) { wedgeDelimiter = iValue; }
*/
}