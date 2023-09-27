package com.incommsys.android.composefolderscanner.cs108library

import android.content.Context
import android.widget.Toast
import com.incommsys.android.composefolderscanner.cs108library.Cs108Connector.Cs108ReadData

import android.widget.TextView
import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
class BluetoothConnector(var context: Context, mLogView: TextView) {
   var DEBUG_PKDATA: Boolean
   val DEBUG = false
   var userDebugEnable = false
   var mLogView: TextView
   var utility: Utility
   private fun byteArrayToString(packet: ByteArray?): String? {
      return utility.byteArrayToString(packet)
   }

   private fun compareArray(array1: ByteArray?, array2: ByteArray?, length: Int): Boolean {
      return utility.compareByteArray(array1, array2, length)
   }

   private fun appendToLog(s: String?) {
      utility.appendToLog(s)
   }

   private fun appendToLogView(s: String) {
      utility.appendToLogView(s)
   }

   private var icsModel = -1
   val csModel: Int
      get() {
         if (false) appendToLog("icsModel = $icsModel")
         return icsModel
      }

   enum class BluetoothIcPayloadEvents {
      GET_VERSION, SET_DEVICE_NAME, GET_DEVICE_NAME, FORCE_BT_DISCONNECT
   }

   class Cs108BluetoothIcData {
      var bluetoothIcPayloadEvent: BluetoothIcPayloadEvents? = null
      var dataValues: ByteArray? = null
   }

   inner class BluetoothIcDevice {
      private val mBluetoothIcVersion = byteArrayOf(-1, -1, -1)
      private var mBluetoothIcVersionUpdated = false
      val bluetoothIcVersion: String
         get() {
            val DEBUG = false
            return if (!mBluetoothIcVersionUpdated) {
               if (DEBUG) appendToLog("mBluetoothIcVersionUpdated is false")
               var repeatRequest = false
               if (mBluetoothIcToWrite.size != 0) {
                  if (mBluetoothIcToWrite[mBluetoothIcToWrite.size - 1].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_VERSION) {
                     repeatRequest = true
                  }
               }
               if (!repeatRequest) {
                  val cs108BluetoothIcData = Cs108BluetoothIcData()
                  cs108BluetoothIcData.bluetoothIcPayloadEvent =
                     BluetoothIcPayloadEvents.GET_VERSION
                  mBluetoothIcToWrite.add(cs108BluetoothIcData)
                  if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent + " to mBluetoothIcToWrite with length = " + mBluetoothIcToWrite.size)
               }
               ""
            } else {
               val retValue =
                  mBluetoothIcVersion[0].toString() + "." + mBluetoothIcVersion[1].toString() + "." + mBluetoothIcVersion[2].toString()
               if (DEBUG) appendToLog(
                  "mBluetoothIcVersionUpdated is true with data = " + byteArrayToString(
                     mBluetoothIcVersion
                  ) + ", icsModel = " + icsModel
               )
               retValue
            }
         }
      var deviceName: ByteArray? = null
      val bluetoothIcName: String
         get() {
            val DEBBUG = false
            if (DEBBUG) appendToLog(
               "3 deviceName = " + if (deviceName == null) "null" else byteArrayToString(
                  deviceName
               )
            )
            return if (deviceName == null) {
               var repeatRequest = false
               if (DEBBUG) appendToLog("3A mBluetoothIcToWrite.size = " + mBluetoothIcToWrite.size)
               if (mBluetoothIcToWrite.size != 0) {
                  if (mBluetoothIcToWrite[mBluetoothIcToWrite.size - 1].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_DEVICE_NAME) {
                     repeatRequest = true
                  }
               }
               if (DEBBUG) appendToLog("3b repeatRequest = $repeatRequest")
               if (!repeatRequest) {
                  val cs108BluetoothIcData = Cs108BluetoothIcData()
                  cs108BluetoothIcData.bluetoothIcPayloadEvent =
                     BluetoothIcPayloadEvents.GET_DEVICE_NAME
                  mBluetoothIcToWrite.add(cs108BluetoothIcData)
                  if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent + " to mBluetoothIcToWrite with length = " + mBluetoothIcToWrite.size)
               }
               ""
            } else {
               deviceName.toString().trim { it <= ' ' }
            }
         }

      fun setBluetoothIcName(name: String?): Boolean {
         if (name == null) return false
         if (name.length == 0) return false
         if (name.length > 20) return false
         val cs108BluetoothIcData = Cs108BluetoothIcData()
         cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.SET_DEVICE_NAME
         if (DEBUG) appendToLog(
            "deviceName.length = " + deviceName!!.size + ", name.getBytes = " + byteArrayToString(
               name.toByteArray()
            )
         )
         cs108BluetoothIcData.dataValues = name.toByteArray()
         if (!mBluetoothIcToWrite.add(cs108BluetoothIcData)) return false
         deviceName = name.toByteArray()
         return true
      }

      fun forceBTdisconnect(): Boolean {
         val cs108BluetoothIcData = Cs108BluetoothIcData()
         cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.FORCE_BT_DISCONNECT
         return mBluetoothIcToWrite.add(cs108BluetoothIcData)
      }

      var mBluetoothIcToWrite = ArrayList<Cs108BluetoothIcData>()
      private val mBluetoothIcToRead = ArrayList<Cs108BluetoothIcData>()
      private fun arrayTypeSet(
         dataBuf: ByteArray,
         pos: Int,
         event: BluetoothIcPayloadEvents?
      ): Boolean {
         var validEvent = false
         when (event) {
            BluetoothIcPayloadEvents.GET_VERSION -> validEvent = true
            BluetoothIcPayloadEvents.SET_DEVICE_NAME -> {
               dataBuf[pos] = 3
               validEvent = true
            }

            BluetoothIcPayloadEvents.GET_DEVICE_NAME -> {
               dataBuf[pos] = 4
               validEvent = true
            }

            BluetoothIcPayloadEvents.FORCE_BT_DISCONNECT -> {
               dataBuf[pos] = 5
               validEvent = true
            }

            else -> {}
         }
         return validEvent
      }

      private fun writeBluetoothIc(data: Cs108BluetoothIcData): ByteArray? {
         var datalength = 0
         if (DEBUG) appendToLog(
            "data.bluetoothIcPayloadEvent=" + data.bluetoothIcPayloadEvent.toString() + ", data.dataValues=" + byteArrayToString(
               data.dataValues
            )
         )
         if (data.dataValues != null) datalength = data.dataValues!!.size
         val dataOutRef = byteArrayOf(
            0xA7.toByte(),
            0xB3.toByte(),
            2,
            0x5F.toByte(),
            0x82.toByte(),
            0x37.toByte(),
            0,
            0,
            0xC0.toByte(),
            0
         )
         var dataOut = ByteArray(10 + datalength)
         if (datalength != 0) {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength)
            dataOutRef[2] = (dataOutRef[2] + datalength).toByte()
         }
         System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.size)
         if (DEBUG) appendToLog("dataOut=" + byteArrayToString(dataOut))
         if (arrayTypeSet(dataOut, 9, data.bluetoothIcPayloadEvent)) {
            if (data.bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.SET_DEVICE_NAME && data.dataValues!!.size < 21) {
               val dataOut1 = ByteArray(10 + 21)
               System.arraycopy(dataOut, 0, dataOut1, 0, dataOut.size)
               dataOut1[2] = 23
               dataOut = dataOut1
            }
            if (DEBUG) appendToLog(byteArrayToString(dataOut))
            return dataOut
         }
         return null
      }

      fun isMatchBluetoothIcToWrite(cs108ReadData: Cs108ReadData): Boolean {
         var match = false
         if (mBluetoothIcToWrite.size != 0 && cs108ReadData.dataValues[0] == 0xC0.toByte()) {
            val dataInCompare = byteArrayOf(0xC0.toByte(), 0)
            if (arrayTypeSet(
                  dataInCompare,
                  1,
                  mBluetoothIcToWrite[0].bluetoothIcPayloadEvent
               ) && cs108ReadData.dataValues.size >= dataInCompare.size + 1
            ) {
               if (compareArray(
                     cs108ReadData.dataValues,
                     dataInCompare,
                     dataInCompare.size
                  ).also { match = it }
               ) {
                  if (DEBUG_PKDATA) appendToLog(
                     "PkData: matched BluetoothIc.Reply with payload = " + byteArrayToString(
                        cs108ReadData.dataValues
                     ) + " for writeData BluetoothIc." + mBluetoothIcToWrite[0].bluetoothIcPayloadEvent.toString()
                  )
                  if (mBluetoothIcToWrite[0].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_VERSION) {
                     if (cs108ReadData.dataValues.size > 2) {
                        var length = mBluetoothIcVersion.size
                        if (cs108ReadData.dataValues.size - 2 < length) length =
                           cs108ReadData.dataValues.size - 2
                        System.arraycopy(
                           cs108ReadData.dataValues,
                           2,
                           mBluetoothIcVersion,
                           0,
                           length
                        )
                        if (mBluetoothIcVersion[0].toInt() == 3) icsModel =
                           463 else if (mBluetoothIcVersion[0].toInt() == 1) icsModel = 108
                        mBluetoothIcVersionUpdated = true
                        if (DEBUG) appendToLog("mBluetoothIcVersionUpdated is true")
                     }
                     if (DEBUG_PKDATA) appendToLog(
                        "PkData: matched BluetoothIc.Reply.GetVersion with version = " + byteArrayToString(
                           mBluetoothIcVersion
                        )
                     )
                  } else if (mBluetoothIcToWrite[0].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_DEVICE_NAME) {
                     if (cs108ReadData.dataValues.size > 2) {
                        val deviceName1 = ByteArray(cs108ReadData.dataValues.size - 2)
                        System.arraycopy(
                           cs108ReadData.dataValues,
                           2,
                           deviceName1,
                           0,
                           cs108ReadData.dataValues.size - 2
                        )
                        deviceName = deviceName1
                     }
                     if (DEBUG_PKDATA) appendToLog(
                        "PkData: matched mBluetoothIc.GetDeviceName.Reply data is found with name=" + byteArrayToString(
                           deviceName
                        ) + ", dataValues.length=" + cs108ReadData.dataValues.size + ", deviceName.length=" + deviceName!!.size
                     )
                  } else if (mBluetoothIcToWrite[0].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.SET_DEVICE_NAME) {
                     if (cs108ReadData.dataValues.size >= 3) {
                        if (cs108ReadData.dataValues[2].toInt() != 0) {
                           //do if false
                        }
                     }
                     if (DEBUG_PKDATA) appendToLog("PkData: matched mBluetoothIc.SetDeviceName.Reply data is found.")
                  } else if (mBluetoothIcToWrite[0].bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.FORCE_BT_DISCONNECT) {
                     if (cs108ReadData.dataValues.size >= 3) {
                        if (cs108ReadData.dataValues[2].toInt() != 0) {
                           //do if false
                        }
                     }
                     if (DEBUG_PKDATA) appendToLog("PkData: matched mBluetoothIc.ForceBTDisconnect.Reply data is found.")
                  } else {
                     if (DEBUG) appendToLog("matched mBluetoothIc.Other.Reply data is found.")
                  }
                  mBluetoothIcToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  if (DEBUG_PKDATA) appendToLog("PkData: new mBluetoothIcToWrite size = " + mBluetoothIcToWrite.size)
               }
            }
         }
         return match
      }

      var sendDataToWriteSent = 0
      fun sendBluetoothIcToWrite(): ByteArray? {
         if (sendDataToWriteSent >= 5) {
            val oldSize = mBluetoothIcToWrite.size
            mBluetoothIcToWrite.removeAt(0)
            sendDataToWriteSent = 0
            if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mBluetoothIcToWrite.size() = " + mBluetoothIcToWrite.size)
            if (DEBUG) appendToLog("Removed after sending count-out.")
            val string =
               "Problem in sending data to Bluetooth Module. Removed data sending after count-out"
            if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT)
               .show() else appendToLogView(string)
         } else {
            if (DEBUG) appendToLog("size = " + mBluetoothIcToWrite.size + ", PayloadEvents = " + mBluetoothIcToWrite[0].bluetoothIcPayloadEvent.toString())
            return writeBluetoothIc(mBluetoothIcToWrite[0])
         }
         return null
      }

      fun addBluetoothToWrite(cs108BluetoothIcData: Cs108BluetoothIcData) {
         var repeatRequest = false
         if (mBluetoothIcDevice.mBluetoothIcToWrite.size != 0) {
            val cs108BluetoothIcData1 =
               mBluetoothIcDevice.mBluetoothIcToWrite[mBluetoothIcDevice.mBluetoothIcToWrite.size - 1]
            if (cs108BluetoothIcData.bluetoothIcPayloadEvent == cs108BluetoothIcData1.bluetoothIcPayloadEvent) {
               if (cs108BluetoothIcData.dataValues == null && cs108BluetoothIcData1.dataValues == null) {
                  repeatRequest = true
               } else if (cs108BluetoothIcData.dataValues != null && cs108BluetoothIcData1.dataValues != null) {
                  if (cs108BluetoothIcData.dataValues!!.size == cs108BluetoothIcData1.dataValues!!.size) {
                     if (compareArray(
                           cs108BluetoothIcData.dataValues,
                           cs108BluetoothIcData1.dataValues,
                           cs108BluetoothIcData.dataValues!!.size
                        )
                     ) {
                        repeatRequest = true
                     }
                  }
               }
            }
         }
         if (!repeatRequest) {
            mBluetoothIcDevice.mBluetoothIcToWrite.add(cs108BluetoothIcData)
            appendToLog("2b GET_DEVICE_NAME")
            if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent.toString() + " to mBluetoothIcToWrite with length = " + mBluetoothIcToWrite.size)
         }
      }
   }

   var mBluetoothIcDevice: BluetoothIcDevice

   init {
      this.mLogView = mLogView
      utility = Utility(context, mLogView)
      DEBUG_PKDATA = utility.DEBUG_PKDATA
      mBluetoothIcDevice = BluetoothIcDevice()
   }
}