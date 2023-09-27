package com.incommsys.android.composefolderscanner.cs108library

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")

open class Cs108Connector internal constructor(var context: Context, var mLogView: TextView?) :
   BleConnector(
      context, mLogView
   ) {
   val appendToLogViewDisable = false
   val DEBUGTHREAD = false
   open var sameCheck = true

   override fun connectBle(readerDevice: ReaderDevice?): Boolean {
      var result = false
      if (DEBUG_CONNECT) appendToLog("ConnectBle(" + readerDevice!!.compass + ")")
      result = super.connectBle(readerDevice)
      if (result) writeDataCount = 0
      return result
   }

   override val isBleConnected: Boolean
      get() = super.isBleConnected

   public override fun disconnect() {
      super.disconnect()
      appendToLog("abcc done")
      mRfidDevice!!.mRfidToWrite.clear()
      mRfidDevice!!.mRfidReaderChip!!.mRx000ToWrite.clear()
   }

   var writeDataCount = 0
   var btSendTimeOut = 0
   var btSendTime: Long = 0
   fun writeData(buffer: ByteArray?, timeout: Int): Boolean {
      val result = writeBleStreamOut(buffer!!)
      if (!result) appendToLog("!!! failure to writeData with previous btSendTimeout = $btSendTimeOut, btSendTime = $btSendTime")
      if (true) {
         btSendTime = System.currentTimeMillis()
         btSendTimeOut = timeout + 60
         if (!isCharacteristicListRead) btSendTimeOut += 3000
         if (false) appendToLog("btSendTimeOut = $btSendTimeOut")
      }
      return result
   }

   var crc_lookup_table = intArrayOf(
      0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
      0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
      0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
      0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
      0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
      0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
      0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
      0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
      0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
      0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
      0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
      0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
      0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
      0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
      0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
      0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
      0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
      0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
      0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
      0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
      0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
      0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
      0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
      0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
      0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
      0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
      0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
      0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
      0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
      0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
      0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
      0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
   )
   var dataRead = false
   var dataReadDisplayCount = 0
   var mCs108DataReadRequest = false
   var inventoryLength = 0
   var iSequenceNumber = 0
   var bDifferentSequence = false
   var bFirstSequence = true
   var invalidata = 0
   var invalidUpdata = 0
   var validata = 0
   var dataInBufferResetting = false
   public override fun processBleStreamInData() {
      val DEBUG = false
      var cs108DataReadStartOld = 0
      var cs108DataReadStart = 0
      var validHeader = false
      if (dataInBufferResetting) {
         if (DEBUG) appendToLog("RESET.")
         dataInBufferResetting = false
         /*cs108DataLeft = new byte[CS108DATALEFT_SIZE];*/cs108DataLeftOffset = 0
         mCs108DataRead!!.clear()
      }
      if (DEBUG) appendToLog("START, cs108DataLeftOffset=$cs108DataLeftOffset, streamInBufferSize=$streamInBufferSize")
      var bFirst = true
      val lTime = System.currentTimeMillis()
      while (true) {
         if (System.currentTimeMillis() - lTime > intervalProcessBleStreamInData / 2) {
            writeDebug2File("B" + intervalProcessBleStreamInData + ", " + System.currentTimeMillis() + ", Timeout")
            if (DEBUG) appendToLogView("processCs108DataIn_TIMEOUT")
            break
         }
         val streamInOverflowTime = streamInOverflowTime
         val streamInMissing = streamInBytesMissing
         if (streamInMissing != 0) appendToLogView("processCs108DataIn($streamInTotalCounter, $streamInAddCounter): len=0, getStreamInOverflowTime()=$streamInOverflowTime, MissBytes=$streamInMissing, Offset=$cs108DataLeftOffset")
         val len = readData(cs108DataLeft, cs108DataLeftOffset, cs108DataLeft.size)
         if (len != 0) {
            val debugData = ByteArray(len)
            System.arraycopy(cs108DataLeft, cs108DataLeftOffset, debugData, 0, len)
            if (DEBUG) appendToLog("DataIn = " + byteArrayToString(debugData))
         }
         if (len != 0 && bFirst) {
            bFirst = false
            writeDebug2File("B" + intervalProcessBleStreamInData + ", " + System.currentTimeMillis())
         }
         cs108DataLeftOffset += len
         if (len == 0) {
            if (!zeroLenDisplayed) {
               zeroLenDisplayed = true
               if (streamInTotalCounter != streamInAddCounter || streamInAddTime != 0L || cs108DataLeftOffset != 0) {
                  if (DEBUG) appendToLog("processCs108DataIn($streamInTotalCounter,$streamInAddCounter): len=0, getStreamInAddTime()=$streamInAddTime, Offset=$cs108DataLeftOffset")
               }
            }
            if (cs108DataLeftOffset == cs108DataLeft.size) {
               if (DEBUG) appendToLog(
                  "cs108DataLeftOffset=" + cs108DataLeftOffset + ", cs108DataLeft=" + byteArrayToString(
                     cs108DataLeft
                  )
               )
            }
            break
         } else {
            dataRead = true
            zeroLenDisplayed = false
            if (DEBUG) appendToLog("cs108DataLeftOffset = $cs108DataLeftOffset, cs108DataReadStart = $cs108DataReadStart")
            while (cs108DataLeftOffset >= cs108DataReadStart + 8) {
               validHeader = false
               val dataIn = cs108DataLeft
               val iPayloadLength = dataIn[cs108DataReadStart + 2].toInt() and 0xFF
               if (dataIn[cs108DataReadStart] == 0xA7.toByte() && dataIn[cs108DataReadStart + 1] == 0xB3.toByte()
                  && (dataIn[cs108DataReadStart + 3] == 0xC2.toByte() || dataIn[cs108DataReadStart + 3] == 0x6A.toByte() || dataIn[cs108DataReadStart + 3] == 0xD9.toByte() || dataIn[cs108DataReadStart + 3] == 0xE8.toByte() || dataIn[cs108DataReadStart + 3] == 0x5F.toByte())
                  && (dataIn[cs108DataReadStart + 4] == 0x82.toByte() || dataIn[cs108DataReadStart + 3] == 0xC2.toByte() && dataIn[cs108DataReadStart + 8] == 0x81.toByte()) && dataIn[cs108DataReadStart + 5] == 0x9E.toByte()
               ) {
                  if (cs108DataLeftOffset - cs108DataReadStart < iPayloadLength + 8) break
                  val bcheckChecksum = true
                  val checksum =
                     (dataIn[cs108DataReadStart + 6].toInt() and 0xFF) * 256 + (dataIn[cs108DataReadStart + 7].toInt() and 0xFF)
                  var checksum2 = 0
                  if (bcheckChecksum) {
                     for (i in cs108DataReadStart until cs108DataReadStart + 8 + iPayloadLength) {
                        if (i != cs108DataReadStart + 6 && i != cs108DataReadStart + 7) {
                           val index = checksum2 xor (dataIn[i].toInt() and 0x0FF) and 0x0FF
                           val table_value = crc_lookup_table[index]
                           checksum2 = checksum2 shr 8 xor table_value
                        }
                     }
                     if (DEBUG) appendToLog(
                        "checksum = " + String.format(
                           "%04X",
                           checksum
                        ) + ", checksum2 = " + String.format("%04X", checksum2)
                     )
                  }
                  if (bcheckChecksum && checksum != checksum2) {
                     if (iPayloadLength < 0) {
                        if (DEBUG) appendToLog(
                           "processCs108DataIn_ERROR, iPayloadLength=" + iPayloadLength + ", cs108DataLeftOffset=" + cs108DataLeftOffset + ", dataIn=" + byteArrayToString(
                              dataIn
                           )
                        )
                     }
                     if (true) {
                        val invalidPart = ByteArray(8 + iPayloadLength)
                        System.arraycopy(
                           dataIn,
                           cs108DataReadStart,
                           invalidPart,
                           0,
                           invalidPart.size
                        )
                        if (DEBUG) appendToLog(
                           "processCs108DataIn_ERROR, INCORRECT RevChecksum=" + Integer.toString(
                              checksum,
                              16
                           ) + ", CalChecksum2=" + Integer.toString(
                              checksum2,
                              16
                           ) + ",data=" + byteArrayToString(invalidPart)
                        )
                     }
                  } else {
                     validHeader = true
                     if (cs108DataReadStart > cs108DataReadStartOld) {
                        if (true) {
                           val invalidPart = ByteArray(cs108DataReadStart - cs108DataReadStartOld)
                           System.arraycopy(
                              dataIn,
                              cs108DataReadStartOld,
                              invalidPart,
                              0,
                              invalidPart.size
                           )
                           if (DEBUG) appendToLog(
                              "processCs108DataIn_ERROR, before valid data, invalid unused data: " + invalidPart.size + ", " + byteArrayToString(
                                 invalidPart
                              )
                           )
                        }
                     } else if (cs108DataReadStart < cs108DataReadStartOld) if (DEBUG) appendToLog(
                        "processCs108DataIn_ERROR, invalid cs108DataReadStartdata=$cs108DataReadStart < cs108DataReadStartOld=$cs108DataReadStartOld"
                     )
                     cs108DataReadStartOld = cs108DataReadStart
                     val cs108ReadData = Cs108ReadData()
                     val dataValues = ByteArray(iPayloadLength)
                     System.arraycopy(
                        dataIn,
                        cs108DataReadStart + 8,
                        dataValues,
                        0,
                        dataValues.size
                     )
                     cs108ReadData.dataValues = dataValues
                     cs108ReadData.milliseconds =
                        System.currentTimeMillis() //getStreamInDataMilliSecond(); //
                     if (DEBUG) appendToLog("current:" + System.currentTimeMillis() + ", streamInData:" + streamInDataMilliSecond)
                     if (false) {
                        val headerbytes = ByteArray(8)
                        System.arraycopy(
                           dataIn,
                           cs108DataReadStart,
                           headerbytes,
                           0,
                           headerbytes.size
                        )
                        if (DEBUG) appendToLog(
                           "processCs108DataIn: Got package=" + byteArrayToString(
                              headerbytes
                           ) + " " + byteArrayToString(dataValues)
                        )
                     }
                     var bRecdOldSequence = false
                     when (dataIn[cs108DataReadStart + 3]) {
                        0xC2.toByte() -> {
                           cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.RFID
                           if (dataIn[cs108DataReadStart + 8] == 0x81.toByte()) {
                              val iSequenceNumber = dataIn[cs108DataReadStart + 4].toInt() and 0xFF
                              var itemp = iSequenceNumber
                              if (itemp < this.iSequenceNumber) {
                                 itemp += 256
                              }
                              itemp -= this.iSequenceNumber + 1
                              if (itemp != 0) {
                                 cs108ReadData.invalidSequence = true
                                 if (!bFirstSequence) {
                                    if (itemp > 128) {
                                       bRecdOldSequence = true
                                       if (DEBUG) appendToLogView(
                                          String.format(
                                             "processCs108DataIn_ERROR: invalidata = %d, %X - %X = %d. Assume old package.",
                                             invalidata,
                                             iSequenceNumber,
                                             this.iSequenceNumber,
                                             itemp
                                          )
                                       )
                                    } else {
                                       invalidata += itemp
                                       if (true) {
                                          var stringSequenceList = ""
                                          var i = 0
                                          while (i < itemp) {
                                             var iMissedNumber = iSequenceNumber - i - 1
                                             if (iMissedNumber < 0) iMissedNumber += 256
                                             stringSequenceList += (if (i != 0) ", " else "") + String.format(
                                                "%X",
                                                iMissedNumber
                                             )
                                             i++
                                          }
                                          if (DEBUG) appendToLogView(
                                             String.format(
                                                "processCs108DataIn_ERROR: invalidata = %d, %X - %X, miss %d: ",
                                                invalidata,
                                                iSequenceNumber,
                                                this.iSequenceNumber,
                                                itemp
                                             ) + stringSequenceList
                                          )
                                       }
                                       if (DEBUG) appendToLog("New 1 sequence = " + iSequenceNumber + ", old = " + this.iSequenceNumber)
                                       this.iSequenceNumber = iSequenceNumber
                                    }
                                 }
                              }
                              bFirstSequence = false
                              if (!bRecdOldSequence) {
                                 if (DEBUG) appendToLog("New 2 sequence = " + iSequenceNumber + ", old = " + this.iSequenceNumber)
                                 this.iSequenceNumber = iSequenceNumber
                              }
                           }
                           if (DEBUG) appendToLogView(
                              "writeRfid, Rin: " + (if (cs108ReadData.invalidSequence) "invalid sequence" else "ok") + "," + byteArrayToString(
                                 cs108ReadData.dataValues
                              )
                           )
                           validata++
                        }

                        0x6A.toByte() -> {
                           if (DEBUG) {
                              appendToLog("BarStreamIn: " + byteArrayToString(cs108ReadData.dataValues))
                              appendToLogView("BIn: " + byteArrayToString(cs108ReadData.dataValues))
                           }
                           cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.BARCODE
                        }

                        0xD9.toByte() -> {
                           if (DEBUG) appendToLog(
                              "BARTRIGGER NotificationData = " + byteArrayToString(
                                 cs108ReadData.dataValues
                              )
                           )
                           cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.NOTIFICATION
                        }

                        0xE8.toByte() -> cs108ReadData.cs108ConnectedDevices =
                           Cs108ConnectedDevices.SILICON_LAB

                        0x5F.toByte() -> cs108ReadData.cs108ConnectedDevices =
                           Cs108ConnectedDevices.BLUETOOTH
                     }
                     mCs108DataRead!!.add(cs108ReadData)
                     cs108DataReadStart += 8 + iPayloadLength
                     val cs108DataLeftNew = ByteArray(CS108DATALEFT_SIZE)
                     System.arraycopy(
                        cs108DataLeft,
                        cs108DataReadStart,
                        cs108DataLeftNew,
                        0,
                        cs108DataLeftOffset - cs108DataReadStart
                     )
                     cs108DataLeft = cs108DataLeftNew
                     cs108DataLeftOffset -= cs108DataReadStart
                     cs108DataReadStart = 0
                     cs108DataReadStart = -1
                     if (!mCs108DataReadRequest) {
                        mCs108DataReadRequest = true
                        mHandler.removeCallbacks(mReadWriteRunnable)
                        mHandler.post(mReadWriteRunnable)
                     }
                  }
               }
               if (validHeader && cs108DataReadStart < 0) {
                  cs108DataReadStart = 0
                  cs108DataReadStartOld = 0
               } else {
                  cs108DataReadStart++
               }
            }
            if (cs108DataReadStart != 0 && cs108DataLeftOffset >= 8) {
               if (true) {
                  val invalidPart = ByteArray(cs108DataReadStart)
                  System.arraycopy(cs108DataLeft, 0, invalidPart, 0, invalidPart.size)
                  val validPart = ByteArray(cs108DataLeftOffset - cs108DataReadStart)
                  System.arraycopy(cs108DataLeft, cs108DataReadStart, validPart, 0, validPart.size)
                  if (DEBUG) appendToLog(
                     "processCs108DataIn_ERROR, ENDLOOP invalid unused data: " + invalidPart.size + ", " + byteArrayToString(
                        invalidPart
                     ) + ", with valid data length=" + validPart.size + ", " + byteArrayToString(
                        validPart
                     )
                  )
               }
               val cs108DataLeftNew = ByteArray(CS108DATALEFT_SIZE)
               System.arraycopy(
                  cs108DataLeft,
                  cs108DataReadStart,
                  cs108DataLeftNew,
                  0,
                  cs108DataLeftOffset - cs108DataReadStart
               )
               cs108DataLeft = cs108DataLeftNew
               cs108DataLeftOffset -= cs108DataReadStart
               cs108DataReadStart = 0
            }
         }
      }
      if (DEBUG) appendToLog("END, cs108DataLeftOffset=$cs108DataLeftOffset, streamInBufferSize=$streamInBufferSize")
   }

   private fun readData(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
      return readBleSteamIn(buffer, byteOffset, byteCount)
   }

   inner class Cs108ConnectorData {
      var voltageMv = 0
      var voltageCnt = 0
      var triggerButtonStatus = false
      var triggerCount = 0
      var timeStamp: Date? = null
      fun getTimeStamp(): String {
         val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
         return sdf.format(mCs108ConnectorData!!.timeStamp)
      }
   }

   var mCs108ConnectorData: Cs108ConnectorData? = null
   var mRfidDevice: RfidDevice? = null
   var mBarcodeDevice: BarcodeDevice? = null
   var mNotificationDevice: NotificationDevice? = null
   var mSiliconLabIcDevice: SiliconLabIcDevice? = null
   var mBluetoothConnector: BluetoothConnector? = null
   private val mHandler = Handler()
   fun cs108ConnectorDataInit() {
      mCs108DataRead = ArrayList()
      cs108DataLeft = ByteArray(CS108DATALEFT_SIZE)
      cs108DataLeftOffset = 0
      zeroLenDisplayed = false
      invalidata = 0
      validata = 0
      dataInBufferResetting = false
      writeDataCount = 0
      mCs108ConnectorData = Cs108ConnectorData()
      mRfidDevice = RfidDevice()
      mBarcodeDevice = BarcodeDevice()
      mNotificationDevice = NotificationDevice()
      mSiliconLabIcDevice = SiliconLabIcDevice()
      mBluetoothConnector = mLogView?.let { BluetoothConnector(context, it) }
      appendToLog("!!! all major classes are initialised")
   }

   enum class Cs108ConnectedDevices {
      RFID, BARCODE, NOTIFICATION, SILICON_LAB, BLUETOOTH, OTHER
   }

   inner class Cs108ReadData {
      var cs108ConnectedDevices: Cs108ConnectedDevices? = null
      lateinit var dataValues: ByteArray
      var invalidSequence = false
      var milliseconds: Long = 0
   }

   val CS108DATALEFT_SIZE = 300 //4000;    //100;
   private var mCs108DataRead: ArrayList<Cs108ReadData>? = null
   lateinit var cs108DataLeft: ByteArray
   var cs108DataLeftOffset = 0
   var zeroLenDisplayed = false

   /*
    boolean batteryReportRequest = false; boolean batteryReportOn = false;
    void setBatteryAutoReport(boolean on) {
        appendToLog("setBatteryAutoReport(" + on + ")");
        batteryReportRequest = true; batteryReportOn = on;
    }
    void setBatteryAutoReport() {
        if (batteryReportRequest) {
            batteryReportRequest = false;
            appendToLog("setBatteryAutoReport()");
            boolean retValue = false;
            if (checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 1, 0, 2)) {
                appendToLog("setBatteryAutoReport(): 111");
                if (batteryReportOn)
                    retValue = mNotificationDevice.mNotificationToWrite.add(NotificationPayloadEvents.NOTIFICATION_AUTO_BATTERY_VOLTAGE);
                else
                    retValue = mNotificationDevice.mNotificationToWrite.add(NotificationPayloadEvents.NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE);
            }
        }
    }*/
   fun checkHostProcessorVersion(
      version: String?,
      majorVersion: Int,
      minorVersion: Int,
      buildVersion: Int
   ): Boolean {
      if (version == null) return false
      if (version.length == 0) return false
      val versionPart = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
         .toTypedArray()
      if (versionPart == null) {
         appendToLog("NULL VersionPart")
         return false
      }
      return try {
         var value = Integer.valueOf(versionPart[0])
         if (value < majorVersion) return false
         if (value > majorVersion) return true
         if (versionPart.size < 2) return true
         value = Integer.valueOf(versionPart[1])
         if (value < minorVersion) return false
         if (value > minorVersion) return true
         if (versionPart.size < 3) return true
         value = Integer.valueOf(versionPart[2])
         value >= buildVersion
      } catch (ex: Exception) {
         false
      }
   }

   var rfidPowerOnTimeOut = 0
   var barcodePowerOnTimeOut = 0
   var timeReady: Long = 0
   var aborting = false
   var sendFailure = false
   private val mReadWriteRunnable: Runnable = object : Runnable {
      var ready2Write = false
      val DEBUG = false
      var timer2Write = 0
      var validBuffer = false
      override fun run() {
         if (DEBUGTHREAD) appendToLog("mReadWriteRunnable starts")
         if (timer2Write != 0 || streamInBufferSize != 0 || mRfidDevice!!.mRfidToRead.size != 0) {
            validBuffer = true
            if (DEBUG) appendToLog("mReadWriteRunnable(): START, timer2Write=" + timer2Write + ", streamInBufferSize = " + streamInBufferSize + ", mRfidToRead.size=" + mRfidDevice!!.mRfidToRead.size + ", mRx000ToRead.size=" + mRfidDevice!!.mRfidReaderChip!!.mRx000ToRead!!.size)
         } else validBuffer = false
         val intervalReadWrite = 250 //50;   //50;    //500;   //500, 100;
         if (rfidPowerOnTimeOut >= intervalReadWrite) {
            rfidPowerOnTimeOut -= intervalReadWrite
            if (rfidPowerOnTimeOut <= 0) {
               rfidPowerOnTimeOut = 0
            }
         }
         if (barcodePowerOnTimeOut >= intervalReadWrite) {
            barcodePowerOnTimeOut -= intervalReadWrite
            if (barcodePowerOnTimeOut <= 0) {
               barcodePowerOnTimeOut = 0
            }
         }
         if (barcodePowerOnTimeOut != 0) if (DEBUG) appendToLog("mReadWriteRunnable(): barcodePowerOnTimeOut = $barcodePowerOnTimeOut")
         val lTime = System.currentTimeMillis()
         mHandler.postDelayed(this, intervalReadWrite.toLong())
         if (mRfidDevice!!.mRfidReaderChip == null) return
         var bFirst = true
         mCs108DataReadRequest = false
         while (mCs108DataRead!!.size != 0) {
            if (!isBleConnected) {
               mCs108DataRead!!.clear()
            } else if (System.currentTimeMillis() - lTime > intervalRx000UplinkHandler / 2) {
               writeDebug2File("C" + intervalReadWrite + ", " + System.currentTimeMillis() + ", Timeout")
               appendToLogView("mReadWriteRunnable: TIMEOUT !!! mCs108DataRead.size() = " + mCs108DataRead!!.size)
               break
            } else {
               if (bFirst) {
                  bFirst = false
                  writeDebug2File("C" + intervalReadWrite + ", " + System.currentTimeMillis())
               }
               try {
                  val cs108ReadData = mCs108DataRead!![0]
                  mCs108DataRead!!.removeAt(0)
                  if (DEBUG) appendToLog(
                     "mReadWriteRunnable(): mCs108DataRead.dataValues = " + byteArrayToString(
                        cs108ReadData.dataValues
                     )
                  )
                  if (mRfidDevice!!.isMatchRfidToWrite(cs108ReadData)) {
                     if (writeDataCount > 0) writeDataCount--
                     ready2Write = true //btSendTime = 0; aborting = false;
                  } else if (mBarcodeDevice!!.isMatchBarcodeToWrite(cs108ReadData)) {
                     if (writeDataCount > 0) writeDataCount--
                     ready2Write = true //btSendTime = 0;
                  } else if (mNotificationDevice!!.isMatchNotificationToWrite(cs108ReadData)) {
                     if (writeDataCount > 0) writeDataCount--
                     ready2Write = true
                     btSendTime = 0
                  } else if (mSiliconLabIcDevice!!.isMatchSiliconLabIcToWrite(cs108ReadData)) {
                     if (writeDataCount > 0) writeDataCount--
                     ready2Write = true
                     btSendTime = 0
                  } else if (mBluetoothConnector!!.mBluetoothIcDevice.isMatchBluetoothIcToWrite(
                        cs108ReadData
                     )
                  ) {
                     if (writeDataCount > 0) writeDataCount--
                     ready2Write = true
                     btSendTime = 0
                  } else if (mRfidDevice!!.isRfidToRead(cs108ReadData)) {
                     mRfidDevice!!.rfidValid = true
                  } else if (mBarcodeDevice!!.isBarcodeToRead(cs108ReadData)) {
                  } else if (mNotificationDevice!!.isNotificationToRead(cs108ReadData)) {
                     /* if (mRfidDevice.mRfidToWrite.size() != 0 && mNotificationDevice.mNotificationToRead.size() != 0) {
                                mNotificationDevice.mNotificationToRead.remove(0);
                                mRfidDevice.mRfidToWrite.clear();
                                mSiliconLabIcDevice.mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.RESET);

                                timeReady = System.currentTimeMillis() - 1500;
                                appendToLog("mReadWriteRunnable: endingMessage: changed timeReady");
                            }*/
                  } else appendToLog(
                     "mReadWriteRunnable: !!! CANNOT process " + byteArrayToString(
                        cs108ReadData.dataValues
                     ) + " with mDataToWriteRemoved = " + mBarcodeDevice!!.mDataToWriteRemoved
                  )
                  if (mBarcodeDevice!!.mDataToWriteRemoved) {
                     mBarcodeDevice!!.mDataToWriteRemoved = false
                     ready2Write = true
                     btSendTime = 0
                     if (DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: processed barcode. btSendTime is set to 0 to allow new sending.")
                  }
               } catch (ex: Exception) {
               }
            }
         }
         if (mRfidDevice!!.mRfidToWriteRemoved) {
            mRfidDevice!!.mRfidToWriteRemoved = false
            ready2Write = true
            btSendTime = 0
            if (DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: processed Rfidcode. btSendTime is set to 0 to allow new sending.")
         }
         var timeout2Ready = 2000
         if (aborting || sendFailure) timeout2Ready = 200
         if (System.currentTimeMillis() > timeReady + timeout2Ready) ready2Write = true
         if (mBarcodeDevice!!.mBarcodeToWrite.size != 0 && DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeDevice!!.mBarcodeToWrite.size + ", ready2write = " + ready2Write)
         if (ready2Write) {
            timeReady = System.currentTimeMillis()
            timer2Write = 0
            if (mRfidDevice!!.rfidFailure) mRfidDevice!!.mRfidToWrite.clear()
            if (mBarcodeDevice!!.barcodeFailure) mBarcodeDevice!!.mBarcodeToWrite.clear()
            if (mRfidDevice!!.mRfidReaderChip!!.mRx000ToWrite.size != 0 && mRfidDevice!!.mRfidToWrite.size == 0) {
               if (DEBUG) appendToLog("mReadWriteRunnable(): mRx000ToWrite.size=" + mRfidDevice!!.mRfidReaderChip!!.mRx000ToWrite.size + ", mRfidToWrite.size=" + mRfidDevice!!.mRfidToWrite.size)
               mRfidDevice!!.mRfidReaderChip!!.addRfidToWrite(mRfidDevice!!.mRfidReaderChip!!.mRx000ToWrite[0])
            }
            var bisRfidCommandStop = false
            var bisRfidCommandExecute = false
            if (mRfidDevice!!.mRfidToWrite.size != 0 && DEBUG) appendToLog(
               "mRfidToWrite = " + mRfidDevice!!.mRfidToWrite[0]!!.rfidPayloadEvent.toString() + "." + byteArrayToString(
                  mRfidDevice!!.mRfidToWrite[0]!!.dataValues
               ) + ", ready2write = " + ready2Write
            )
            if (mRfidDevice!!.mRfidToWrite.size != 0) {
               val cs108RfidData = mRfidDevice!!.mRfidToWrite[0]
               if (cs108RfidData!!.rfidPayloadEvent == RfidPayloadEvents.RFID_COMMAND) {
                  var ii: Int
                  if (false) {
                     val byCommandExeccute = byteArrayOf(0x70, 1, 0, 0xF0.toByte())
                     ii = 0
                     while (ii < 4) {
                        if (byCommandExeccute[ii] != cs108RfidData.dataValues!![ii]) break
                        ii++
                     }
                     if (ii == 4) bisRfidCommandExecute = true
                  }
                  val byCommandStop = byteArrayOf(0x40.toByte(), 3, 0, 0, 0, 0, 0, 0)
                  ii = 0
                  while (ii < 4) {
                     if (byCommandStop[ii] != cs108RfidData.dataValues!![ii]) break
                     ii++
                  }
                  if (ii == 4) bisRfidCommandStop = true
                  if (DEBUG) appendToLog(
                     "mRfidToWrite(0).dataValues = " + byteArrayToString(
                        mRfidDevice!!.mRfidToWrite[0]!!.dataValues
                     ) + ", bisRfidCommandExecute = " + bisRfidCommandExecute + ", bisRfidCommandStop = " + bisRfidCommandStop
                  )
               }
            }
            if (mBarcodeDevice!!.mBarcodeToWrite.size != 0 && DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeDevice!!.mBarcodeToWrite.size + ", bisRfidCommandStop = " + bisRfidCommandStop)
            if (bisRfidCommandStop) {
               mRfidDevice!!.sendRfidToWrite()
               ready2Write = false //
            } else if (mSiliconLabIcDevice!!.sendSiliconLabIcToWrite()) { //SiliconLab version afffects Notification operation
               ready2Write = false //
            } else if (mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite.size != 0) {   //Bluetooth version affects Barcode operation
               if (!isBleConnected) mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite.clear() else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                  val dataOut = mBluetoothConnector!!.mBluetoothIcDevice.sendBluetoothIcToWrite()
                  var retValue = false
                  if (DEBUG_PKDATA && mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent != 0) appendToLog(
                     "!!! mBluetoothIcDevice.sendDataToWriteSent = " + mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent
                  )
                  if (DEBUG_PKDATA) appendToLog(
                     String.format(
                        "PkData: write mBluetoothIcDevice.%s.%s with mBluetoothIcDevice.sendDataToWriteSent = %d",
                        mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite[0].bluetoothIcPayloadEvent.toString(),
                        byteArrayToString(mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite[0].dataValues),
                        mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent
                     )
                  )
                  if (mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent != 0) appendToLog(
                     "!!! mBluetoothIcDevice.sendDataToWriteSent = " + mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent
                  )
                  if (dataOut != null) retValue = writeData(dataOut, 0)
                  if (retValue) {
                     mBluetoothConnector!!.mBluetoothIcDevice.sendDataToWriteSent++
                  } else {
                     if (DEBUG) appendToLogView("failure to send " + mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite[0].bluetoothIcPayloadEvent.toString())
                     mBluetoothConnector!!.mBluetoothIcDevice.mBluetoothIcToWrite.removeAt(0)
                  }
               }
               ready2Write = false
            } else if (mNotificationDevice!!.sendNotificationToWrite()) {
               ready2Write = false
            } else if (mBarcodeDevice!!.sendBarcodeToWrite()) {
               ready2Write = false
            } else if (mRfidDevice!!.sendRfidToWrite()) {
               ready2Write = false
            }
         }
         if (validBuffer) {
            if (DEBUG) appendToLog("mReadWriteRunnable: END, timer2Write=" + timer2Write + ", streamInBufferSize = " + streamInBufferSize + ", mRfidToRead.size=" + mRfidDevice!!.mRfidToRead.size + ", mRx000ToRead.size=" + mRfidDevice!!.mRfidReaderChip!!.mRx000ToRead!!.size)
         }
         mRfidDevice!!.mRfidReaderChip!!.mRx000UplinkHandler()
         if (DEBUGTHREAD) appendToLog("mReadWriteRunnable: mReadWriteRunnable ends")
      }
   }
   var intervalRx000UplinkHandler = 250
   private val runnableRx000UplinkHandler: Runnable = object : Runnable {
      override fun run() {
//            mRfidDevice.mRx000Device.mRx000UplinkHandler();
         mHandler.postDelayed(this, intervalRx000UplinkHandler.toLong())
      }
   }

   enum class RfidPayloadEvents {
      RFID_POWER_ON, RFID_POWER_OFF, RFID_COMMAND, RFID_DATA_READ
   }

   inner class Cs108RfidData {
      var waitUplinkResponse = false
      var downlinkResponsed = false
      var rfidPayloadEvent: RfidPayloadEvents? = null
      var dataValues: ByteArray? = null
      var invalidSequence = false
      var milliseconds: Long = 0
   }

   inner class RfidDevice {
      var onStatus = false
         private set
      var mRfidToWrite = ArrayList<Cs108RfidData?>()
      var mRfidToRead = ArrayList<Cs108RfidData>()
      var inventoring = false


      var mRfidReaderChip: RfidReaderChip? = RfidReaderChip()
      private fun arrayTypeSet(dataBuf: ByteArray, pos: Int, event: RfidPayloadEvents?): Boolean {
         var validEvent = false
         when (event) {
            RfidPayloadEvents.RFID_POWER_ON -> validEvent = true
            RfidPayloadEvents.RFID_POWER_OFF -> {
               dataBuf[pos] = 1
               validEvent = true
            }

            RfidPayloadEvents.RFID_COMMAND -> {
               dataBuf[pos] = 2
               validEvent = true
            }

            else -> {}
         }
         return validEvent
      }

      fun writeRfid(dataIn: Cs108RfidData?): Boolean {
         var dataOut = byteArrayOf(
            0xA7.toByte(),
            0xB3.toByte(),
            2,
            0xC2.toByte(),
            0x82.toByte(),
            0x37.toByte(),
            0,
            0,
            0x80.toByte(),
            0
         )
         if (dataIn!!.rfidPayloadEvent == RfidPayloadEvents.RFID_COMMAND) {
            if (dataIn.dataValues != null) {
               val dataOut1 = ByteArray(dataOut.size + dataIn.dataValues!!.size)
               System.arraycopy(dataOut, 0, dataOut1, 0, dataOut.size)
               dataOut1[2] = (dataOut1[2] + dataIn.dataValues!!.size).toByte()
               System.arraycopy(
                  dataIn.dataValues,
                  0,
                  dataOut1,
                  dataOut.size,
                  dataIn.dataValues!!.size
               )
               dataOut = dataOut1
            }
         }
         if (arrayTypeSet(dataOut, 9, dataIn.rfidPayloadEvent)) {
            if (false) appendToLogView(byteArrayToString(dataOut)!!)
            if (DEBUG_PKDATA) appendToLog(
               String.format(
                  "PkData: write Rfid.%s.%s with mRfidDevice.sendRfidToWriteSent = %d",
                  dataIn.rfidPayloadEvent.toString(),
                  byteArrayToString(
                     dataIn.dataValues
                  ),
                  sendRfidToWriteSent
               )
            )
            if (sendRfidToWriteSent != 0) appendToLog("!!! mRfidDevice.sendRfidToWriteSent = $sendRfidToWriteSent")
            return writeData(dataOut, if (dataIn.waitUplinkResponse) 500 else 0)
         }
         return false
      }

      fun isMatchRfidToWrite(cs108ReadData: Cs108ReadData): Boolean {
         var match = false
         val DEBUG = false
         if (mRfidToWrite.size != 0 && cs108ReadData.dataValues[0] == 0x80.toByte()) {
            val dataInCompare = byteArrayOf(0x80.toByte(), 0)
            if (arrayTypeSet(
                  dataInCompare,
                  1,
                  mRfidToWrite[0]!!.rfidPayloadEvent
               ) && cs108ReadData.dataValues.size == dataInCompare.size + 1
            ) {
               if (compareArray(
                     cs108ReadData.dataValues,
                     dataInCompare,
                     dataInCompare.size
                  ).also { match = it }
               ) {
                  if (DEBUG_PKDATA) appendToLog(
                     "PkData: matched Rfid.Reply with payload = " + byteArrayToString(cs108ReadData.dataValues) + " for writeData Rfid." + mRfidToWrite[0]!!.rfidPayloadEvent.toString() + "." + byteArrayToString(
                        mRfidToWrite[0]!!.dataValues
                     )
                  )
                  if (cs108ReadData.dataValues[2].toInt() != 0) {
                     if (DEBUG) appendToLog("Rfid.reply data is found with error")
                  } else {
                     if (mRfidToWrite[0]!!.rfidPayloadEvent == RfidPayloadEvents.RFID_POWER_ON) {
                        rfidPowerOnTimeOut = 3000
                        onStatus = true
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOn with result 0 and onStatus = $onStatus")
                     } else if (mRfidToWrite[0]!!.rfidPayloadEvent == RfidPayloadEvents.RFID_POWER_OFF) {
                        onStatus = false
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOff with result 0 and onStatus = $onStatus")
                     } else if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply." + mRfidToWrite[0]!!.rfidPayloadEvent.toString() + " with result 0")
                     val cs108RfidData = mRfidToWrite[0]
                     if (cs108RfidData!!.waitUplinkResponse) {
                        cs108RfidData.downlinkResponsed = true
                        mRfidToWrite[0] = cs108RfidData
                        if (DEBUG_PKDATA) appendToLog("PkData: mRfidToWrite.downlinkResponsed is set and waiting uplink data")
                        if (false) {
                           for (i in mRfidReaderChip!!.mRx000ToRead!!.indices) {
                              if (mRfidReaderChip!!.mRx000ToRead?.get(i)!!.responseType == HostCmdResponseTypes.TYPE_COMMAND_END) if (DEBUG) appendToLog(
                                 "mRx0000ToRead with COMMAND_END is removed"
                              )
                           }
                           if (DEBUG) appendToLog("mRx000ToRead.clear !!!")
                        }
                        mRfidReaderChip!!.mRx000ToRead?.clear()
                        if (DEBUG) appendToLog("mRx000ToRead.clear !!!")
                        return true
                     }
                     if (DEBUG) appendToLog("matched Rfid.reply data is found with mRfidToWrite.size=" + mRfidToWrite.size)
                  }
                  mRfidToWrite.removeAt(0)
                  sendRfidToWriteSent = 0
                  mRfidToWriteRemoved = true
                  if (DEBUG) appendToLog("mmRfidToWrite remove 1 with remained write size = " + mRfidToWrite.size)
                  if (DEBUG_PKDATA) appendToLog("PkData: new mRfidToWrite size = " + mRfidToWrite.size)
                  if (false) {
                     for (i in mRfidReaderChip!!.mRx000ToRead?.indices!!) {
                        when (mRfidReaderChip!!.mRx000ToRead?.get(i)!!.responseType ) {
                           HostCmdResponseTypes.TYPE_COMMAND_END ->{
                              if (DEBUG) {
                                 appendToLog("mRx0000ToRead with COMMAND_END is removed")
                              }
                           }

                           else -> {}
                        }
                     }
                     if (DEBUG) appendToLog("mRx000ToRead.clear !!!")
                  }
                  mRfidReaderChip!!.mRx000ToRead?.clear()
                  if (DEBUG) appendToLog("mRx000ToRead.clear !!!")
               }
            }
         }
         return match
      }

      private val logTime: Long = 0
      var sendRfidToWriteSent = 0
      var mRfidToWriteRemoved = false
      var rfidFailure = false
      var rfidValid = false
      fun sendRfidToWrite(): Boolean {
         val DEBUG = false
         if (rfidPowerOnTimeOut != 0) {
            if (DEBUG) appendToLog("rfidPowerOnTimeOut = " + rfidPowerOnTimeOut + ", mRfidToWrite.size() = " + mRfidToWrite.size)
            return false
         }
         if (!rfidFailure && mRfidToWrite.size != 0) {
            if (!isBleConnected) {
               mRfidToWrite.clear()
            } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
               if (DEBUG) appendToLog("Timeout: btSendTimeOut = $btSendTimeOut")
               val rfidPayloadEvents = mRfidToWrite[0]!!.rfidPayloadEvent
               if (sendRfidToWriteSent >= 4) {
                  mRfidToWrite.removeAt(0)
                  sendRfidToWriteSent = 0
                  mRfidToWriteRemoved = true
                  if (DEBUG) appendToLog("mmRfidToWrite remove 2")
                  if (DEBUG) appendToLog("Removed after sending count-out.")
                  if (true) {
                     appendToLog("Rfdid data transmission failure !!! clear mRfidToWrite buffer !!!")
                     rfidFailure = true
                     mRfidToWrite.clear()
                  } else if (!rfidValid) {
                     Toast.makeText(
                        context,
                        "Problem in sending data to Rfid Module. Rfid is disabled.",
                        Toast.LENGTH_SHORT
                     ).show()
                     rfidFailure = true
                  } else {
                     Toast.makeText(
                        context,
                        "Problem in Sending Commands to RFID Module.  Bluetooth Disconnected.  Please Reconnect",
                        Toast.LENGTH_SHORT
                     ).show()
                     appendToLog("disconnect d")
                     disconnect()
                  }
                  if (DEBUG) appendToLog("done")
               } else {
                  if (DEBUG) appendToLog(
                     "size = " + mRfidToWrite.size + ", PayloadEvents = " + rfidPayloadEvents.toString() + ", data=" + byteArrayToString(
                        mRfidToWrite[0]!!.dataValues
                     )
                  )
                  val retValue = writeRfid(mRfidToWrite[0])
                  sendRfidToWriteSent++
                  if (retValue) {
                     mRfidToWriteRemoved = false
                     if (DEBUG) appendToLog("writeRfid() with sendRfidToWriteSent = $sendRfidToWriteSent")
                     sendFailure = false
                  } else sendFailure = true
               }
            }
            return true
         }
         return false
      }

      fun isRfidToRead(cs108ReadData: Cs108ReadData): Boolean {
         var found = false
         val DEBUG = false
         if (cs108ReadData.dataValues[0] == 0x81.toByte()) {
            if (DEBUG_PKDATA) appendToLog(
               "PkData: found Rfid.Uplink with payload = " + byteArrayToString(
                  cs108ReadData.dataValues
               )
            )
            val cs108RfidReadData = Cs108RfidData()
            val dataValues = ByteArray(cs108ReadData.dataValues.size - 2)
            System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.size)
            if (DEBUG_PKDATA) appendToLog(
               "PkData: found Rfid.Uplink.DataRead with payload = " + byteArrayToString(
                  dataValues
               )
            )
            if (cs108ReadData.dataValues[1].toInt() == 0) {
               cs108RfidReadData.rfidPayloadEvent = RfidPayloadEvents.RFID_DATA_READ
               cs108RfidReadData.dataValues = dataValues
               cs108RfidReadData.invalidSequence = cs108ReadData.invalidSequence
               cs108RfidReadData.milliseconds = cs108ReadData.milliseconds
               mRfidToRead.add(cs108RfidReadData)
               if (DEBUG_PKDATA) appendToLog("PkData: uplink data Rfid.Uplink.DataRead is uploaded to mRfidToRead")
               found = true
            } else {
               invalidUpdata++
               appendToLog(
                  "!!! found INVALID Rfid.Uplink with payload = " + byteArrayToString(
                     cs108ReadData.dataValues
                  )
               )
            }
         }
         return found
      }
   }

   enum class BarcodePayloadEvents {
      BARCODE_NULL, BARCODE_POWER_ON, BARCODE_POWER_OFF, BARCODE_SCAN_START, BARCODE_COMMAND, BARCODE_VIBRATE_ON, BARCODE_VIBRATE_OFF, BARCODE_DATA_READ, BARCODE_GOOD_READ
   }

   internal enum class BarcodeCommendTypes {
      COMMAND_COMMON, COMMAND_SETTING, COMMAND_QUERY
   }

   inner class Cs108BarcodeData {
      var waitUplinkResponse = false
      var downlinkResponsed = false
      var barcodePayloadEvent: BarcodePayloadEvents? = null
      var dataValues: ByteArray? = null
   }

   inner class BarcodeDevice {
      var onStatus = false
         private set
      var vibrateStatus = false
         private set
      var version: String? = null
         private set
      var eSN: String? = null
         private set
      var serialNumber: String? = null
         private set
      var date: String? = null
         private set
      var prefix: ByteArray? = null
      var suffix: ByteArray? = null
      fun checkPreSuffix(prefix1: ByteArray?, suffix1: ByteArray?): Boolean {
         var result = false
         if (prefix1 != null && prefix != null && suffix1 != null && suffix != null) {
            result = Arrays.equals(prefix1, prefix)
            if (result) result = Arrays.equals(suffix1, suffix)
         }
         return result
      }

      var mBarcodeToWrite = ArrayList<Cs108BarcodeData?>()
      var mBarcodeToRead = ArrayList<Cs108BarcodeData?>()
      private fun arrayTypeSet(
         dataBuf: ByteArray,
         pos: Int,
         event: BarcodePayloadEvents?
      ): Boolean {
         var validEvent = false
         when (event) {
            BarcodePayloadEvents.BARCODE_POWER_ON -> validEvent = true
            BarcodePayloadEvents.BARCODE_POWER_OFF -> {
               dataBuf[pos] = 1
               validEvent = true
            }

            BarcodePayloadEvents.BARCODE_SCAN_START -> {
               dataBuf[pos] = 2
               validEvent = true
            }

            BarcodePayloadEvents.BARCODE_COMMAND -> {
               dataBuf[pos] = 3
               validEvent = true
            }

            BarcodePayloadEvents.BARCODE_VIBRATE_ON -> {
               dataBuf[pos] = 4
               validEvent = true
            }

            BarcodePayloadEvents.BARCODE_VIBRATE_OFF -> {
               dataBuf[pos] = 5
               validEvent = true
            }

            else -> {}
         }
         return validEvent
      }

      private fun writeBarcode(data: Cs108BarcodeData?): Boolean {
         var datalength = 0
         if (data!!.dataValues != null) datalength = data.dataValues!!.size
         val dataOutRef = byteArrayOf(
            0xA7.toByte(),
            0xB3.toByte(),
            2,
            0x6A.toByte(),
            0x82.toByte(),
            0x37.toByte(),
            0,
            0,
            0x90.toByte(),
            0
         )
         val dataOut = ByteArray(10 + datalength)
         if (datalength != 0) {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength)
            dataOutRef[2] = ( dataOutRef[2] + datalength).toByte()
         }
         System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.size)
         if (arrayTypeSet(dataOut, 9, data.barcodePayloadEvent)) {
            if (false) {
               appendToLog("BarStreamOut: " + byteArrayToString(dataOut))
               appendToLogView("BOut: " + byteArrayToString(dataOut))
            }
            if (DEBUG_PKDATA) appendToLog(
               String.format(
                  "PkData: write Barcode.%s.%s with mBarcodeDevice.sendDataToWriteSent = %d",
                  data.barcodePayloadEvent.toString(),
                  byteArrayToString(
                     data.dataValues
                  ),
                  sendDataToWriteSent
               )
            )
            if (sendDataToWriteSent != 0) appendToLog("!!! mBarcodeDevice.sendDataToWriteSent = $sendDataToWriteSent")
            return writeData(dataOut, if (data.waitUplinkResponse) 500 else 0)
         }
         return false
      }

      fun isMatchBarcodeToWrite(cs108ReadData: Cs108ReadData): Boolean {
         var match = false
         val DEBUG = false
         if (mBarcodeToWrite.size != 0 && cs108ReadData.dataValues[0] == 0x90.toByte()) {
            if (DEBUG) appendToLog("cs108ReadData = " + byteArrayToString(cs108ReadData.dataValues))
            if (DEBUG) appendToLog("tempDisconnect: icsModel = " + mBluetoothConnector!!.csModel + ", mBarcodeToWrite.size = " + mBarcodeToWrite.size)
            if (mBarcodeToWrite.size != 0) if (DEBUG) appendToLog(
               "mBarcodeToWrite(0) = " + mBarcodeToWrite[0]!!.barcodePayloadEvent.toString() + "," + byteArrayToString(
                  mBarcodeToWrite[0]!!.dataValues
               )
            )
            val dataInCompare = byteArrayOf(0x90.toByte(), 0)
            if (arrayTypeSet(
                  dataInCompare,
                  1,
                  mBarcodeToWrite[0]!!.barcodePayloadEvent
               ) && cs108ReadData.dataValues.size == dataInCompare.size + 1
            ) {
               if (compareArray(
                     cs108ReadData.dataValues,
                     dataInCompare,
                     dataInCompare.size
                  ).also { match = it }
               ) {
                  if (DEBUG_PKDATA) appendToLog(
                     "PkData: matched Barcode.Reply with payload = " + byteArrayToString(
                        cs108ReadData.dataValues
                     ) + " for writeData Barcode." + mBarcodeToWrite[0]!!.barcodePayloadEvent.toString()
                  )
                  if (cs108ReadData.dataValues[2].toInt() != 0) {
                     if (DEBUG) appendToLog("Barcode.reply data is found with error")
                  } else if (mBluetoothConnector!!.csModel == 108) {
                     if (mBarcodeToWrite[0]!!.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_ON) {
                        barcodePowerOnTimeOut = 1000
                        if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_ON")
                        onStatus = true
                        if (DEBUG_PKDATA or (cs108ReadData.dataValues[2].toInt() != 0)) appendToLog(
                           "PkData: matched Barcode.Reply.PowerOn with result = " + cs108ReadData.dataValues[2] + " and onStatus = " + onStatus
                        )
                     } else if (mBarcodeToWrite[0]!!.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_OFF) {
                        if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_OFF")
                        onStatus = false
                        if (DEBUG_PKDATA or (cs108ReadData.dataValues[2].toInt() != 0)) appendToLog(
                           "PkData: matched Barcode.Reply.PowerOff with result = " + cs108ReadData.dataValues[2] + " and onStatus = " + onStatus
                        )
                     } else if (mBarcodeToWrite[0]!!.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_ON) {
                        vibrateStatus = true
                        if (DEBUG_PKDATA or (cs108ReadData.dataValues[2].toInt() != 0)) appendToLog(
                           "PkData: matched Barcode.Reply.VibrateOn with result = " + cs108ReadData.dataValues[2] + " and vibrateStatus = " + vibrateStatus
                        )
                     } else if (mBarcodeToWrite[0]!!.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_OFF) {
                        vibrateStatus = false
                        if (DEBUG_PKDATA or (cs108ReadData.dataValues[2].toInt() != 0)) appendToLog(
                           "PkData: matched Barcode.Reply.VibrateOff with result = " + cs108ReadData.dataValues[2] + " and vibrateStatus = " + vibrateStatus
                        )
                     } else if (mBarcodeToWrite[0]!!.barcodePayloadEvent == BarcodePayloadEvents.BARCODE_COMMAND) {
                        barcodePowerOnTimeOut = 500
                        if (DEBUG_PKDATA or (cs108ReadData.dataValues[2].toInt() != 0)) appendToLog(
                           "PkData: matched Barcode.Reply.Command with result = " + cs108ReadData.dataValues[2] + " and barcodePowerOnTimeOut = " + barcodePowerOnTimeOut
                        )
                     } else appendToLog("Not matched Barcode.Reply")
                     val cs108BarcodeData = mBarcodeToWrite[0]
                     if (cs108BarcodeData!!.waitUplinkResponse) {
                        cs108BarcodeData.downlinkResponsed = true
                        mBarcodeToWrite[0] = cs108BarcodeData
                        if (DEBUG_PKDATA) appendToLog("PkData: mBarcodeToWrite.downlinkResponsed is set and waiting uplink data")
                        return true
                     }
                  } else {
                     barcodeFailure = true
                     appendToLog("Not matched Barcode.Reply")
                  }
                  mBarcodeToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  mDataToWriteRemoved = true
                  if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + mBarcodeToWrite.size)
               }
            }
         }
         return match
      }

      private var sendDataToWriteSent = 0
      var mDataToWriteRemoved = false
      var barcodeFailure = false
      fun sendBarcodeToWrite(): Boolean {
         val DEBUG = false
         if (barcodePowerOnTimeOut != 0) {
            if (DEBUG) appendToLog("barcodePowerOnTimeOut = " + barcodePowerOnTimeOut + ", mBarcodeToWrite.size() = " + mBarcodeToWrite.size)
            return false
         }
         if (mBarcodeToWrite.size != 0) {
            if (DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeToWrite.size)
            if (!isBleConnected) {
               mBarcodeToWrite.clear()
            } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
               val barcodePayloadEvents = mBarcodeToWrite[0]!!.barcodePayloadEvent
               if (DEBUG) appendToLog("barcodePayloadEvents = " + barcodePayloadEvents.toString())
               val isBarcodeData =
                  barcodePayloadEvents == BarcodePayloadEvents.BARCODE_SCAN_START || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_COMMAND
               if (barcodeFailure && isBarcodeData) {
                  mBarcodeToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  mDataToWriteRemoved = true
               } else if (sendDataToWriteSent >= 2 && isBarcodeData) {
                  mBarcodeToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  mDataToWriteRemoved = true
                  if (DEBUG) appendToLog("Removed after sending count-out.")
                  if (false) Toast.makeText(
                     context,
                     "Problem in sending data to BarCode Module. Barcode is disabled",
                     Toast.LENGTH_LONG
                  ).show() else if (mBluetoothConnector!!.csModel == 108) Toast.makeText(
                     context, "No barcode present on Reader", Toast.LENGTH_LONG
                  ).show()
                  barcodeFailure = true // disconnect(false);
               } else {
                  if (DEBUG) appendToLog("size = " + mBarcodeToWrite.size + ", PayloadEvents = " + mBarcodeToWrite[0]!!.barcodePayloadEvent.toString())
                  val retValue = writeBarcode(mBarcodeToWrite[0])
                  if (retValue) {
                     sendDataToWriteSent++
                     mDataToWriteRemoved = false
                  } else {
                     if (DEBUG) appendToLogView("failure to send " + mBarcodeToWrite[0]!!.barcodePayloadEvent.toString())
                     mBarcodeToWrite.removeAt(0)
                     sendDataToWriteSent = 0
                     mDataToWriteRemoved = true
                  }
                  return true
               }
            }
         }
         return false
      }

      var bBarcodeTriggerMode = 0xff.toByte()
      fun isBarcodeToRead(cs108ReadData: Cs108ReadData): Boolean {
         var found = false
         val DEBUG = false
         if (cs108ReadData.dataValues[0] == 0x91.toByte()) {
            if (DEBUG_PKDATA) appendToLog(
               "PkData: found Barcode.Uplink with payload = " + byteArrayToString(
                  cs108ReadData.dataValues
               )
            )
            val cs108BarcodeData = Cs108BarcodeData()
            when (cs108ReadData.dataValues[1].toInt()) {
               0 -> {
                  cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_DATA_READ
                  val dataValues = ByteArray(cs108ReadData.dataValues.size - 2)
                  System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.size)
                  if (DEBUG_PKDATA) appendToLog(
                     "PkData: found Barcode.Uplink.DataRead with payload = " + byteArrayToString(
                        dataValues
                     )
                  )
                  var commandType: BarcodeCommendTypes? = null
                  if (mBarcodeToWrite.size > 0) {
                     if (mBarcodeToWrite[0]!!.downlinkResponsed) {
                        var count = 0
                        var matched = true
                        if (mBarcodeToWrite[0]!!.dataValues!![0].toInt() == 0x1b) {
                           commandType = BarcodeCommendTypes.COMMAND_COMMON
                           count = 1
                           if (DEBUG) appendToLog("0x1b, Common response with  count = $count")
                        } else if (mBarcodeToWrite[0]!!.dataValues!![0].toInt() == 0x7E) {
                           if (DEBUG) appendToLog(
                              "0x7E, Barcode response with 0x7E mBarcodeToWrite.get(0).dataValues[0] and response data = " + byteArrayToString(
                                 dataValues
                              )
                           )
                           matched = true
                           commandType = BarcodeCommendTypes.COMMAND_QUERY
                           var index = 0
                           while (dataValues.size - index >= 5 + 1) {
                              if (dataValues[index].toInt() == 2 && dataValues[index + 1].toInt() == 0 && dataValues[index + 4].toInt() == 0x34) {
                                 val length = dataValues[index + 2] * 256 + dataValues[index + 3]
                                 if (dataValues.size - index >= length + 4 + 1) {
                                    matched = true
                                    val bytes = ByteArray(length - 1)
                                    System.arraycopy(dataValues, index + 5, bytes, 0, bytes.size)
                                    val requestBytes = ByteArray(
                                       mBarcodeToWrite[0]!!.dataValues!!.size - 6
                                    )
                                    System.arraycopy(
                                       mBarcodeDevice!!.mBarcodeToWrite[0]!!.dataValues,
                                       5,
                                       requestBytes,
                                       0,
                                       requestBytes.size
                                    )
                                    if (DEBUG_PKDATA) appendToLog(
                                       "PkData: found Barcode.Uplink.DataRead.QueryResponse with payload data1 = " + byteArrayToString(
                                          bytes
                                       ) + " for QueryInput data1 = " + byteArrayToString(
                                          requestBytes
                                       )
                                    )
                                    if (mBarcodeToWrite[0]!!.dataValues!![5].toInt() == 0x37 && length >= 5) {
                                       matched = true
                                       val prefixLength = dataValues[index + 6].toInt()
                                       var suffixLength = 0
                                       if (dataValues.size - index >= 5 + 2 + prefixLength + 2 + 1) {
                                          suffixLength =
                                             dataValues[index + 6 + prefixLength + 2].toInt()
                                       }
                                       if (dataValues.size - index >= 5 + 2 + prefixLength + 2 + suffixLength + 1) {
                                          prefix = null
                                          suffix = null
                                          if (dataValues[index + 5].toInt() == 1) {
                                             prefix = ByteArray(prefixLength)
                                             System.arraycopy(
                                                dataValues,
                                                index + 7,
                                                prefix,
                                                0,
                                                prefix!!.size
                                             )
                                          }
                                          if (dataValues[index + 6 + prefixLength + 1].toInt() == 1) {
                                             suffix = ByteArray(suffixLength)
                                             System.arraycopy(
                                                dataValues,
                                                index + 7 + prefixLength + 2,
                                                suffix,
                                                0,
                                                suffix!!.size
                                             )
                                          }
                                          if (DEBUG) appendToLog(
                                             "BarStream: BarcodePrefix = " + byteArrayToString(
                                                prefix
                                             ) + ", BarcodeSuffix = " + byteArrayToString(
                                                suffix
                                             )
                                          )
                                       }
                                       if (DEBUG_PKDATA) appendToLog(
                                          "PkData: Barcode.Uplink.DataRead.QueryResponse.SelfPrefix_SelfSuffix is processed as Barcode Prefix = " + byteArrayToString(
                                             prefix
                                          ) + ", Suffix = " + byteArrayToString(suffix)
                                       )
                                    } else if (mBarcodeToWrite[0]!!.dataValues!![5].toInt() == 0x47 && length > 1) {
                                       if (DEBUG) appendToLog("versionNumber is detected with length = $length")
                                       matched = true
                                       val byteVersion = ByteArray(length - 1)
                                       System.arraycopy(
                                          dataValues,
                                          index + 5,
                                          byteVersion,
                                          0,
                                          byteVersion.size
                                       )
                                       version =  byteVersion.toString(StandardCharsets.UTF_8)
                                    } else if (mBarcodeToWrite[0]!!.dataValues!![5].toInt() == 0x48 && length >= 5) {
                                       if (dataValues[index + 5] == mBarcodeToWrite[0]!!.dataValues!![6] && dataValues[index + 6] == mBarcodeToWrite[0]!!.dataValues!![7]) {
                                          matched = true //for ESN, S/N or Date
                                          val byteSN = ByteArray(length - 3)
                                          System.arraycopy(
                                             dataValues,
                                             index + 7,
                                             byteSN,
                                             0,
                                             byteSN.size
                                          )
                                          var serialNumber: String?
                                          try {
                                             serialNumber =byteSN.toString(StandardCharsets.UTF_8)
                                             val snLength = serialNumber.substring(0, 2).toInt()
                                             if (DEBUG) appendToLog("BarStream: serialNumber = " + serialNumber + ", snLength = " + snLength + ", serialNumber.length = " + serialNumber.length)
                                             serialNumber =
                                                if (snLength + 2 == serialNumber.length) {
                                                   serialNumber.substring(2)
                                                } else null
                                          } catch (e: Exception) {
                                             serialNumber = null
                                          }
                                          if (dataValues[index + 6] == 0x32.toByte()) eSN =
                                             serialNumber else if (dataValues[index + 6] == 0x33.toByte()) this.serialNumber =
                                             serialNumber else if (dataValues[index + 6] == 0x34.toByte()) date =
                                             serialNumber
                                          if (DEBUG) appendToLog(
                                             "BarStream: " + String.format(
                                                "%02x",
                                                dataValues[index + 6]
                                             ) + " serialNumber = " + serialNumber + ", length = " + serialNumber!!.length
                                          )
                                       }
                                    } else if (mBarcodeToWrite[0]!!.dataValues!![5].toInt() == 0x44 && length >= 3) {
                                       if (DEBUG) appendToLog(
                                          "BarStream: dataValue = " + byteArrayToString(dataValues) + ", writeDataValue = " + byteArrayToString(
                                             mBarcodeToWrite[0]!!.dataValues
                                          )
                                       )
                                       if (dataValues[index + 5] == mBarcodeToWrite[0]!!.dataValues!![6] && dataValues[index + 6] == mBarcodeToWrite[0]!!.dataValues!![7]) {
                                          matched = true
                                          if (mBarcodeToWrite[0]!!.dataValues!![6].toInt() == 0x30 && mBarcodeToWrite[0]!!.dataValues!![7].toInt() == 0x30 && mBarcodeToWrite[0]!!.dataValues!![8].toInt() == 0x30) {
                                             bBarcodeTriggerMode = dataValues[7]
                                             if (dataValues[index + 7].toInt() == 0x30) {
                                                if (DEBUG) appendToLog("BarStream: Reading mode is TRIGGER")
                                             } else if (DEBUG) appendToLog("BarStream: Reading mode = " + dataValues[7].toString())
                                             if (DEBUG) appendToLogView("BIn: Correct readingMode query response !!!")
                                          }
                                       } else if (DEBUG) {
                                          matched = true //for debugging to skip any wrong response
                                          if (DEBUG) appendToLog("BarStream: incorrect response !!!")
                                          if (DEBUG) appendToLogView("BIn: incorrect readingMode query response !!!")
                                       }
                                       if (DEBUG) appendToLog("matched = $matched")
                                    }
                                    index += length + 5
                                 } else break
                              } else index++
                           }
                           if (matched) {
                              if (DEBUG) appendToLog("Matched Query response")
                           } else {
                              if (DEBUG) appendToLog("Mis-matched Query response")
                           }
                        } else {
                           if (DEBUG) appendToLog("BarStream: Barcode response with mBarcodeToWrite.get(0).dataValues[0] =  Others")
                           var strData: String? = null
                           strData = mBarcodeToWrite[0]!!.dataValues?.toString( StandardCharsets.UTF_8)
                           val findStr = "nls"
                           var lastIndex = 0
                           while (lastIndex != -1) {
                              lastIndex = strData!!.indexOf(findStr, lastIndex)
                              if (lastIndex != -1) {
                                 count++
                                 lastIndex += findStr.length
                              }
                           }
                           if (DEBUG) appendToLog("Setting strData = $strData, count = $count")
                        }
                        if (count != 0) {
                           if (DEBUG) appendToLog(
                              "BarStream: count = " + count + ", data = " + byteArrayToString(
                                 dataValues
                              )
                           )
                           val dataValuesNew = ByteArray(dataValues.size - count)
                           matched = false
                           var iCount = 0
                           var iNewIndex = 0
                           var k = 0
                           while (k < dataValues.size) {
                              var match06 = false
                              if (!matched) {
                                 if (dataValues[k].toInt() == 0x06 || dataValues[k].toInt() == 0x15) {
                                    match06 = true
                                    if (++iCount == count) matched = true
                                    if (DEBUG) appendToLog("BarStream: WRONG PREFIX: matched with k = $k")
                                 }
                              }
                              if (!match06 && iNewIndex < dataValuesNew.size) {
                                 dataValuesNew[iNewIndex++] =
                                    dataValues[k] // java.lang.ArrayIndexOutOfBoundsException: length=0; index=0
                              }
                              k++
                           }
                           if (DEBUG) appendToLog(
                              "BarStream: matched = " + matched + ", new data = " + byteArrayToString(
                                 dataValuesNew
                              )
                           )
                           if (DEBUG) appendToLog(
                              "WRONG PREFIX: matched " + matched + ", iNewIndex = " + iNewIndex + ", dataValuesNew = " + byteArrayToString(
                                 dataValuesNew
                              )
                           )
                           if (!matched) cs108BarcodeData.dataValues =
                              dataValues else if (iNewIndex != 0) cs108BarcodeData.dataValues =
                              dataValuesNew else cs108BarcodeData.dataValues = null
                           if (cs108BarcodeData.dataValues != null) {
                              mBarcodeDevice!!.mBarcodeToRead.add(cs108BarcodeData)
                              if (DEBUG) appendToLog(
                                 "BarStream: mBarcodeToRead is added with mBarcodeToWrite.size() = " + mBarcodeToWrite.size + ", dataValues = " + byteArrayToString(
                                    dataValues
                                 )
                              )
                           }
                        }
                        if (matched) {
                           found = true
                           mBarcodeToWrite.removeAt(0)
                           sendDataToWriteSent = 0
                           mDataToWriteRemoved = true
                           if (DEBUG) appendToLog("BarStream: matched response command")
                        }
                     }
                  }
                  var i = 0
                  while (false) {
                     if (//  ( )
                        dataValues[i].toInt() == 0x28 || dataValues[i].toInt() == 0x29 || dataValues[i].toInt() == 0x5B || dataValues[i].toInt() == 0x5D || dataValues[i].toInt() == 0x5C || dataValues[i].toInt() == 0x7B || dataValues[i].toInt() == 0x7D) dataValues[i] =
                        0x20
                     i++
                  }
                  cs108BarcodeData.dataValues = dataValues
                  mBarcodeDevice!!.mBarcodeToRead.add(cs108BarcodeData)
                  if (DEBUG_PKDATA) appendToLog(
                     "PkData: uplink data Barcode.DataRead." + byteArrayToString(
                        dataValues
                     ) + " is added to mBarcodeToRead"
                  )
                  found = true
               }

               1 -> {
                  if (DEBUG) appendToLog("BarStream: matched Barcode.good data is found")
                  cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_GOOD_READ
                  cs108BarcodeData.dataValues = null
                  mBarcodeDevice!!.mBarcodeToRead.add(cs108BarcodeData)
                  found = true
               }
            }
         }
         if (DEBUG_BTDATA && found && DEBUG) appendToLog(
            "found Barcode.read data = " + byteArrayToString(
               cs108ReadData.dataValues
            )
         )
         return found
      }
   }

   enum class NotificationPayloadEvents {
      NOTIFICATION_GET_BATTERY_VOLTAGE, NOTIFICATION_GET_TRIGGER_STATUS, NOTIFICATION_AUTO_BATTERY_VOLTAGE, NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE, NOTIFICATION_AUTO_RFIDINV_ABORT, NOTIFICATION_GET_AUTO_RFIDINV_ABORT, NOTIFICATION_AUTO_BARINV_STARTSTOP, NOTIFICATION_GET_AUTO_BARINV_STARTSTOP, NOTIFICATION_AUTO_TRIGGER_REPORT, NOTIFICATION_STOP_TRIGGER_REPORT, NOTIFICATION_BATTERY_FAILED, NOTIFICATION_BATTERY_ERROR, NOTIFICATION_TRIGGER_PUSHED, NOTIFICATION_TRIGGER_RELEASED
   }

   inner class Cs108NotificatiionData {
      var notificationPayloadEvent: NotificationPayloadEvents? = null
      var dataValues: ByteArray? = null
   }

   interface NotificationListener {
      fun onChange()
   }

   inner class NotificationDevice {
      var listener: NotificationListener? = null
      fun setNotificationListener0(listener: NotificationListener?) {
         this.listener = listener
      }

      //NotificationListener getListener() { return listener; }
      var mTriggerStatus = false
      var triggerStatus: Boolean
         get() = mTriggerStatus
         set(mTriggerStatus) {
            this.mTriggerStatus = mTriggerStatus
            if (listener != null) listener!!.onChange()
         }
      var mAutoRfidAbortStatus = true
      var mAutoRfidAbortStatusUpdate = false
      var autoRfidAbortStatus: Boolean
         get() {
            if (true) {
               val cs108NotificatiionData = Cs108NotificatiionData()
               cs108NotificatiionData.notificationPayloadEvent =
                  NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT
               mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
            }
            return mAutoRfidAbortStatus
         }
         set(mAutoRfidAbortStatus) {
            this.mAutoRfidAbortStatus = mAutoRfidAbortStatus
            mAutoRfidAbortStatusUpdate = true
         }
      var mAutoBarStartStopStatus = false
      var mAutoBarStartStopStatusUpdated = false
      var autoBarStartStopStatus: Boolean
         get() {
            if (!mAutoBarStartStopStatusUpdated) {
               val cs108NotificatiionData = Cs108NotificatiionData()
               cs108NotificatiionData.notificationPayloadEvent =
                  NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP
               mNotificationDevice!!.mNotificationToWrite.add(cs108NotificatiionData)
            }
            return mAutoBarStartStopStatus
         }
         set(mAutoBarStartStopStatus) {
            this.mAutoBarStartStopStatus = mAutoBarStartStopStatus
            mAutoBarStartStopStatusUpdated = true
         }
      var mNotificationToWrite = ArrayList<Cs108NotificatiionData?>()
      var mNotificationToRead = ArrayList<Cs108NotificatiionData?>()
      private fun arrayTypeSet(
         dataBuf: ByteArray,
         pos: Int,
         event: NotificationPayloadEvents?
      ): Boolean {
         var validEvent = false
         when (event) {
            NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE -> validEvent = true
            NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS -> {
               dataBuf[pos] = 1
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_AUTO_BATTERY_VOLTAGE -> if (!checkHostProcessorVersion(
                  mSiliconLabIcDevice!!.siliconLabIcVersion, 1, 0, 2
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 2
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE -> if (!checkHostProcessorVersion(
                  mSiliconLabIcDevice!!.siliconLabIcVersion, 1, 0, 2
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 3
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_AUTO_RFIDINV_ABORT -> if (!checkHostProcessorVersion(
                  mBluetoothConnector!!.mBluetoothIcDevice.bluetoothIcVersion, 1, 0, 13
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 4
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT -> if (!checkHostProcessorVersion(
                  mBluetoothConnector!!.mBluetoothIcDevice.bluetoothIcVersion, 1, 0, 13
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 5
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_AUTO_BARINV_STARTSTOP -> if (!checkHostProcessorVersion(
                  mBluetoothConnector!!.mBluetoothIcDevice.bluetoothIcVersion, 1, 0, 14
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 6
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP -> if (false) {
               validEvent = false
            } else {
               dataBuf[pos] = 7
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_AUTO_TRIGGER_REPORT -> if (!checkHostProcessorVersion(
                  mSiliconLabIcDevice!!.siliconLabIcVersion, 1, 0, 16
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 8
               validEvent = true
            }

            NotificationPayloadEvents.NOTIFICATION_STOP_TRIGGER_REPORT -> if (!checkHostProcessorVersion(
                  mSiliconLabIcDevice!!.siliconLabIcVersion, 1, 0, 16
               )
            ) {
               validEvent = false
            } else {
               dataBuf[pos] = 9
               validEvent = true
            }

            else -> {}
         }
         return validEvent
      }

      private fun writeNotification(data: Cs108NotificatiionData?): Boolean {
         var datalength = 0
         val DEBUG = false
         if (data!!.dataValues != null) datalength = data.dataValues!!.size
         val dataOutRef = byteArrayOf(
            0xA7.toByte(),
            0xB3.toByte(),
            2,
            0xD9.toByte(),
            0x82.toByte(),
            0x37.toByte(),
            0,
            0,
            0xA0.toByte(),
            0
         )
         val dataOut = ByteArray(10 + datalength)
         if (DEBUG) appendToLog("event = " + data.notificationPayloadEvent.toString() + ", with datalength = " + datalength)
         if (datalength != 0) {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength)
            dataOutRef[2] = (dataOutRef[2] + datalength).toByte()
         }
         System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.size)
         if (arrayTypeSet(dataOut, 9, data.notificationPayloadEvent)) {
            if (DEBUG) appendToLogView("NOut: " + byteArrayToString(dataOut))
            return writeData(dataOut, 0)
         }
         return false
      }

      fun isMatchNotificationToWrite(cs108ReadData: Cs108ReadData): Boolean {
         var match = false
         if (mNotificationToWrite.size != 0) {
            val dataInCompare = byteArrayOf(0xA0.toByte(), 0)
            if (arrayTypeSet(
                  dataInCompare,
                  1,
                  mNotificationToWrite[0]!!.notificationPayloadEvent
               ) && cs108ReadData.dataValues.size >= dataInCompare.size + 1
            ) {
               if (compareArray(
                     cs108ReadData.dataValues,
                     dataInCompare,
                     dataInCompare.size
                  ).also { match = it }
               ) {
                  if (DEBUG_BTDATA) appendToLog(
                     "found Notification.read data = " + byteArrayToString(
                        cs108ReadData.dataValues
                     )
                  )
                  if (mNotificationToWrite[0]!!.notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE) {
                     if (cs108ReadData.dataValues.size >= dataInCompare.size + 2) {
                        mCs108ConnectorData!!.voltageMv =
                           (cs108ReadData.dataValues[2].toInt() and 0xFF) * 256 + (cs108ReadData.dataValues[3].toInt() and 0xFF)
                        mCs108ConnectorData!!.voltageCnt++
                     }
                  } else if (mNotificationToWrite[0]!!.notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS) {
                     if (cs108ReadData.dataValues[2].toInt() != 0) {
                        triggerStatus = true //mTriggerStatus = true;
                        mCs108ConnectorData!!.triggerButtonStatus = true
                     } else {
                        triggerStatus = false //mTriggerStatus = false;
                        mCs108ConnectorData!!.triggerButtonStatus = false
                     }
                     mCs108ConnectorData!!.triggerCount++
                     if (DEBUG) appendToLog("BARTRIGGER: isMatchNotificationToWrite finds trigger = " + triggerStatus)
                  } else if (mNotificationToWrite[0]!!.notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT) {
                     autoRfidAbortStatus = cs108ReadData.dataValues[2].toInt() != 0
                     if (DEBUG) appendToLog("AUTORFIDABORT: isMatchNotificationToWrite finds autoRfidAbort = " + autoRfidAbortStatus)
                  } else if (mNotificationToWrite[0]!!.notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP) {
                     autoBarStartStopStatus = cs108ReadData.dataValues[2].toInt() != 0
                     if (DEBUG) appendToLog("AUTOBARSTARTSTOP: isMatchNotificationToWrite finds autoBarStartStop = " + autoBarStartStopStatus)
                  } else {
                  }
                  mNotificationToWrite.removeAt(0)
                  sendDataToWriteSent = 0
               }
            }
         }
         return match
      }

      private var sendDataToWriteSent = 0
      fun sendNotificationToWrite(): Boolean {
         if (mNotificationToWrite.size != 0) {
            if (!isBleConnected) {
               mNotificationToWrite.clear()
            } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
               if (sendDataToWriteSent >= 5) {
                  val oldSize = mNotificationToWrite.size
                  val cs108NotificatiionData = mNotificationToWrite[0]
                  mNotificationToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mNotificationToWrite.size() = " + mNotificationToWrite.size)
                  if (DEBUG) appendToLog("Removed after sending count-out.")
                  val string =
                     "Problem in sending data to Notification Module. Removed data sending after count-out"
                  if (mBluetoothConnector!!.userDebugEnable) Toast.makeText(
                     context,
                     string,
                     Toast.LENGTH_SHORT
                  ).show() else appendToLogView(string)
                  if (true) Toast.makeText(
                     context,
                     cs108NotificatiionData!!.notificationPayloadEvent.toString(),
                     Toast.LENGTH_LONG
                  ).show()
               } else {
                  if (DEBUG) appendToLog("size = " + mNotificationToWrite.size)
                  val retValue = writeNotification(mNotificationToWrite[0])
                  if (retValue) {
                     sendDataToWriteSent++
                  } else {
                     if (DEBUG) appendToLog("failure to send " + mNotificationToWrite[0]!!.notificationPayloadEvent.toString())
                     mNotificationToWrite.removeAt(0)
                  }
               }
            }
            return true
         }
         return false
      }

      var timeTriggerRelease: Long = 0
      fun isNotificationToRead(cs108ReadData: Cs108ReadData): Boolean {
         var found = false
         val DEBUG = false
         if (cs108ReadData.dataValues[0] == 0xA0.toByte() && cs108ReadData.dataValues[1] == 0x00.toByte() && cs108ReadData.dataValues.size >= 4) {
            mCs108ConnectorData!!.voltageMv =
               (cs108ReadData.dataValues[2].toInt() and 0xFF) * 256 + (cs108ReadData.dataValues[3].toInt() and 0xFF)
            mCs108ConnectorData!!.voltageCnt++
            found = true
         } else if (cs108ReadData.dataValues[0] == 0xA0.toByte() && cs108ReadData.dataValues[1] == 0x01.toByte() && cs108ReadData.dataValues.size >= 3) {
            mCs108ConnectorData!!.triggerButtonStatus = cs108ReadData.dataValues[2].toInt() != 0
            mCs108ConnectorData!!.triggerCount++
            found = true
         } else if (cs108ReadData.dataValues[0] == 0xA1.toByte()) {
            val cs108NotificatiionData = Cs108NotificatiionData()
            when (cs108ReadData.dataValues[1].toInt()) {
               0 -> {
                  if (DEBUG) appendToLog("matched batteryFailed data is found.")
                  cs108NotificatiionData.notificationPayloadEvent =
                     NotificationPayloadEvents.NOTIFICATION_BATTERY_FAILED
                  cs108NotificatiionData.dataValues = null
                  if (false) mNotificationDevice!!.mNotificationToRead.add(cs108NotificatiionData)
                  found = true
               }

               1 -> {
                  if (DEBUG) appendToLog(
                     "matched Error data is found, " + byteArrayToString(
                        cs108ReadData.dataValues
                     )
                  )
                  cs108NotificatiionData.notificationPayloadEvent =
                     NotificationPayloadEvents.NOTIFICATION_BATTERY_ERROR
                  val dataValues = ByteArray(cs108ReadData.dataValues.size - 2)
                  System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.size)
                  cs108NotificatiionData.dataValues = dataValues
                  if (true) mNotificationDevice!!.mNotificationToRead.add(cs108NotificatiionData)
                  btSendTime = System.currentTimeMillis() - btSendTimeOut + 50
                  found = true
               }
               2 -> {
                  cs108NotificatiionData.notificationPayloadEvent =
                     NotificationPayloadEvents.NOTIFICATION_TRIGGER_PUSHED
                  cs108NotificatiionData.dataValues = null
                  triggerStatus = true //mTriggerStatus = true;
                  if (DEBUG) appendToLog("BARTRIGGER: isNotificationToRead finds trigger = " + triggerStatus)
                  if (false) mNotificationDevice!!.mNotificationToRead.add(cs108NotificatiionData)
                  found = true
               }

               3 -> {
                  cs108NotificatiionData.notificationPayloadEvent =
                     NotificationPayloadEvents.NOTIFICATION_TRIGGER_RELEASED
                  cs108NotificatiionData.dataValues = null
                  //if (System.currentTimeMillis() - timeTriggerRelease > 800) {
                  //    timeTriggerRelease = System.currentTimeMillis();
                  triggerStatus = false //mTriggerStatus = false;
                  //}
                  if (DEBUG) appendToLog("BARTRIGGER: isNotificationToRead finds trigger = " + triggerStatus)
                  if (false) mNotificationDevice!!.mNotificationToRead.add(cs108NotificatiionData)
                  found = true
               }
            }
         }
         if (DEBUG_BTDATA && found) appendToLog(
            "found Notification.read data = " + byteArrayToString(
               cs108ReadData.dataValues
            )
         )
         return found
      }
   }

   enum class SiliconLabIcPayloadEvents {
      GET_VERSION, GET_SERIALNUMBER, GET_MODELNAME, RESET
   }

   internal inner class Cs108SiliconLabIcReadData {
      var siliconLabIcPayloadEvent: SiliconLabIcPayloadEvents? = null
      lateinit var dataValues: ByteArray
   }

   inner class SiliconLabIcDevice {
      private val mSiliconLabIcVersion = byteArrayOf(-1, -1, -1)
      val siliconLabIcVersion: String
         get() = if (mSiliconLabIcVersion[0].toInt() == -1) {
            var repeatRequest = false
            if (mSiliconLabIcToWrite.size != 0) {
               if (mSiliconLabIcToWrite[mSiliconLabIcToWrite.size - 1] == SiliconLabIcPayloadEvents.GET_VERSION) {
                  repeatRequest = true
               }
            }
            if (!repeatRequest) {
               mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_VERSION)
            }
            ""
         } else {
            mSiliconLabIcVersion[0].toString() + "." + mSiliconLabIcVersion[1].toString() + "." + mSiliconLabIcVersion[2].toString()
         }
      private val serialNumber = ByteArray(16)
      fun getSerialNumber(): String? {
         return if (serialNumber[0].toInt() == 0) {
            var repeatRequest = false
            if (mSiliconLabIcToWrite.size != 0) {
               if (mSiliconLabIcToWrite[mSiliconLabIcToWrite.size - 1] == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
                  repeatRequest = true
               }
            }
            if (!repeatRequest) {
               mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_SERIALNUMBER)
            }
            ""
         } else {
            if (serialNumber[15].toInt() == 0) {
               serialNumber[15] = serialNumber[14]
               serialNumber[14] = serialNumber[13]
               serialNumber[13] = 0
            }
            for (i in 13..15) {
               if (serialNumber[i].toInt() == 0) serialNumber[i] = 0x30
            }
            if (true) return byteArray2DisplayString(serialNumber)
            var str: String? = serialNumber.toString(StandardCharsets.UTF_8).trim()
            str
         }
      }

      private val modelName = ByteArray(16)
      fun getModelName(): String? {
         appendToLog("modelName = " + byteArrayToString(modelName))
         return if (modelName[0].toInt() == 0) {
            var repeatRequest = false
            if (mSiliconLabIcToWrite.size != 0) {
               if (mSiliconLabIcToWrite[mSiliconLabIcToWrite.size - 1] == SiliconLabIcPayloadEvents.GET_MODELNAME) {
                  repeatRequest = true
               }
            }
            if (!repeatRequest) {
               mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_MODELNAME)
            }
            ""
         } else if (true) {
            byteArray2DisplayString(modelName)
         } else {
            modelName.toString(StandardCharsets.UTF_8)
         }
      }

      fun byteArray2DisplayString(byteData: ByteArray?): String? {
         if (false) appendToLog("String0 = " + byteArrayToString(byteData))
         var str = byteData?.toString(StandardCharsets.UTF_8)
         if (str != null) {
            str = str.replace("[^\\x00-\\x7F]".toRegex(), "")
            str = str.replace("[\\p{Cntrl}&&[^\r\n\t]]".toRegex(), "")
         }
         if (false) appendToLog("String1 = $str")
         return str
      }

      var mSiliconLabIcToWrite = ArrayList<SiliconLabIcPayloadEvents?>()
      private fun arrayTypeSet(
         dataBuf: ByteArray,
         pos: Int,
         event: SiliconLabIcPayloadEvents?
      ): Boolean {
         var validEvent = false
         when (event) {
            SiliconLabIcPayloadEvents.GET_VERSION -> validEvent = true
            SiliconLabIcPayloadEvents.GET_SERIALNUMBER -> {
               dataBuf[pos] = 4
               validEvent = true
            }

            SiliconLabIcPayloadEvents.GET_MODELNAME -> {
               dataBuf[pos] = 6
               validEvent = true
            }

            SiliconLabIcPayloadEvents.RESET -> {
               dataBuf[pos] = 12
               validEvent = true
            }

            else -> {}
         }
         return validEvent
      }

      private fun writeSiliconLabIc(event: SiliconLabIcPayloadEvents?): Boolean {
         var dataOut = byteArrayOf(
            0xA7.toByte(),
            0xB3.toByte(),
            2,
            0xE8.toByte(),
            0x82.toByte(),
            0x37.toByte(),
            0,
            0,
            0xB0.toByte(),
            0
         )
         if (event == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
            dataOut = byteArrayOf(
               0xA7.toByte(),
               0xB3.toByte(),
               3,
               0xE8.toByte(),
               0x82.toByte(),
               0x37.toByte(),
               0,
               0,
               0xB0.toByte(),
               4,
               0
            )
         } else if (event == SiliconLabIcPayloadEvents.GET_MODELNAME) {
            dataOut = byteArrayOf(
               0xA7.toByte(),
               0xB3.toByte(),
               2,
               0xE8.toByte(),
               0x82.toByte(),
               0x37.toByte(),
               0,
               0,
               0xB0.toByte(),
               6
            )
         } else if (event == SiliconLabIcPayloadEvents.RESET) {
            dataOut = byteArrayOf(
               0xA7.toByte(),
               0xB3.toByte(),
               2,
               0xE8.toByte(),
               0x82.toByte(),
               0x37.toByte(),
               0,
               0,
               0xB0.toByte(),
               12
            )
         }
         if (DEBUG) appendToLog(byteArrayToString(dataOut))
         return writeData(dataOut, 0)
      }

      fun isMatchSiliconLabIcToWrite(cs108ReadData: Cs108ReadData): Boolean {
         var match = false
         val DEBUG = false
         if (mSiliconLabIcToWrite.size != 0) {
            val dataInCompare = byteArrayOf(0xB0.toByte(), 0)
            if (arrayTypeSet(
                  dataInCompare,
                  1,
                  mSiliconLabIcToWrite[0]
               ) && cs108ReadData.dataValues.size >= dataInCompare.size + 1
            ) {
               if (compareArray(
                     cs108ReadData.dataValues,
                     dataInCompare,
                     dataInCompare.size
                  ).also { match = it }
               ) {
                  if (DEBUG) appendToLog(
                     "found SiliconLabIc.read data = " + byteArrayToString(
                        cs108ReadData.dataValues
                     )
                  )
                  if (mSiliconLabIcToWrite[0] == SiliconLabIcPayloadEvents.GET_VERSION) {
                     if (cs108ReadData.dataValues.size >= 2 + mSiliconLabIcVersion.size) {
                        System.arraycopy(
                           cs108ReadData.dataValues,
                           2,
                           mSiliconLabIcVersion,
                           0,
                           mSiliconLabIcVersion.size
                        )
                        if (DEBUG) appendToLog(
                           "matched mSiliconLabIc.GetVersion.reply data is found with mSiliconLabIcToWrite.size=" + mSiliconLabIcToWrite.size + ", version=" + byteArrayToString(
                              mSiliconLabIcVersion
                           )
                        )
                     }
                  } else if (mSiliconLabIcToWrite[0] == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
                     var length = cs108ReadData.dataValues.size - 2
                     if (length > serialNumber.size) length = serialNumber.size
                     System.arraycopy(cs108ReadData.dataValues, 2, serialNumber, 0, length)
                     if (DEBUG) appendToLog(
                        "matched mSiliconLabIc.GetSerialNumber.reply data is found: " + byteArrayToString(
                           cs108ReadData.dataValues
                        )
                     )
                  } else if (mSiliconLabIcToWrite[0] == SiliconLabIcPayloadEvents.GET_MODELNAME) {
                     var length = cs108ReadData.dataValues.size - 2
                     if (length > modelName.size) length = modelName.size
                     System.arraycopy(cs108ReadData.dataValues, 2, modelName, 0, length)
                     if (DEBUG) appendToLog(
                        "matched mSiliconLabIc.GetModelName.reply data is found: " + byteArrayToString(
                           cs108ReadData.dataValues
                        )
                     )
                  } else if (mSiliconLabIcToWrite[0] == SiliconLabIcPayloadEvents.RESET) {
                     if (cs108ReadData.dataValues[2].toInt() != 0) {
                        if (DEBUG) appendToLog("Silicon Lab RESET is found with error")
                     } else if (DEBUG) appendToLog("matched SiliconLab.reply data is found")
                  } else {
                     if (DEBUG) appendToLog("matched mSiliconLabIc.Other.reply data is found.")
                  }
                  mSiliconLabIcToWrite.removeAt(0)
                  sendDataToWriteSent = 0
               }
            }
         }
         return match
      }

      private var sendDataToWriteSent = 0
      fun sendSiliconLabIcToWrite(): Boolean {
         if (mSiliconLabIcToWrite.size != 0) {
            if (!isBleConnected) {
               mSiliconLabIcToWrite.clear()
            } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
               if (sendDataToWriteSent >= 5) {
                  val oldSize = mSiliconLabIcToWrite.size
                  mSiliconLabIcToWrite.removeAt(0)
                  sendDataToWriteSent = 0
                  if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mSiliconLabIcToWrite.size() = " + mSiliconLabIcToWrite.size)
                  if (DEBUG) appendToLog("Removed after sending count-out.")
                  val string =
                     "Problem in sending data to SiliconLabIc Module. Removed data sending after count-out"
                  if (mBluetoothConnector!!.userDebugEnable) Toast.makeText(
                     context,
                     string,
                     Toast.LENGTH_SHORT
                  ).show() else appendToLogView(string)
               } else {
                  if (DEBUG) appendToLog("size = " + mSiliconLabIcToWrite.size)
                  val retValue = writeSiliconLabIc(mSiliconLabIcToWrite[0])
                  if (retValue) {
                     sendDataToWriteSent++
                  } else {
                     appendToLogView("failure to send " + mSiliconLabIcToWrite[0].toString())
                     mSiliconLabIcToWrite.removeAt(0)
                  }
               }
            }
            return true
         }
         return false
      }
   }

   enum class ControlCommands {
      NULL, CANCEL, SOFTRESET, ABORT, PAUSE, RESUME, GETSERIALNUMBER, RESETTOBOOTLOADER
   }

   enum class HostRegRequests {
      MAC_OPERATION,  //MAC_VER, MAC_LAST_COMMAND_DURATION,

      //HST_CMNDIAGS,
      //HST_MBP_ADDR, HST_MBP_DATA,
      //HST_OEM_ADDR, HST_OEM_DATA,
      HST_ANT_CYCLES, HST_ANT_DESC_SEL, HST_ANT_DESC_CFG, MAC_ANT_DESC_STAT, HST_ANT_DESC_PORTDEF, HST_ANT_DESC_DWELL, HST_ANT_DESC_RFPOWER, HST_ANT_DESC_INV_CNT, HST_TAGMSK_DESC_SEL, HST_TAGMSK_DESC_CFG, HST_TAGMSK_BANK, HST_TAGMSK_PTR, HST_TAGMSK_LEN, HST_TAGMSK_0_3, HST_QUERY_CFG, HST_INV_CFG, HST_INV_SEL, HST_INV_ALG_PARM_0, HST_INV_ALG_PARM_1, HST_INV_ALG_PARM_2, HST_INV_ALG_PARM_3, HST_INV_RSSI_FILTERING_CONFIG, HST_INV_RSSI_FILTERING_THRESHOLD, HST_INV_RSSI_FILTERING_COUNT, HST_INV_EPC_MATCH_CFG, HST_INV_EPCDAT_0_3, HST_TAGACC_DESC_CFG, HST_TAGACC_BANK, HST_TAGACC_PTR, HST_TAGACC_CNT, HST_TAGACC_LOCKCFG, HST_TAGACC_ACCPWD, HST_TAGACC_KILLPWD, HST_TAGWRDAT_SEL, HST_TAGWRDAT_0, HST_RFTC_CURRENT_PROFILE, HST_RFTC_FRQCH_SEL, HST_RFTC_FRQCH_CFG, HST_RFTC_FRQCH_DESC_PLLDIVMULT, HST_RFTC_FRQCH_DESC_PLLDACCTL, HST_RFTC_FRQCH_CMDSTART, HST_AUTHENTICATE_CFG, HST_AUTHENTICATE_MSG, HST_READBUFFER_LEN, HST_UNTRACEABLE_CFG, HST_CMD
   }

   inner class Rx000Setting(set_default_setting: Boolean) {
      inner class Rx000Setting_default {
         var macVer: String? = null
         var diagnosticCfg = 0x210
         var mbpAddress = 0 // ?
         var mbpData = 0 // ?
         var oemAddress = 4 // ?
         var oemData = 0 // ?

         //RFTC block paramters
         var currentProfile = 1
         var freqChannelSelect = 0

         // Antenna block parameters
         var antennaCycle = 1
         var antennaFreqAgile = 0
         var antennaSelect = 0

         //Tag select block parameters
         var invSelectIndex = 0

         //Inventtory block paraameters
         var queryTarget = 0
         var querySession = 2
         var querySelect = 1
         var invAlgo = 3
         var matchRep = 0
         var tagSelect = 0
         var noInventory = 0
         var tagRead = 0
         var tagDelay = 0
         var tagJoin = 0
         var brandid = 0
         var algoSelect = 3
         var rssiFilterType = 0
         var rssiFilterOption = 0
         var rssiFilterThreshold = 0
         var rssiFilterCount: Long = 0
         var matchEnable = 0
         var matchType = 0
         var matchLength = 0
         var matchOffset = 0
         lateinit var invMatchData0_63: ByteArray
         var invMatchDataReady = 0

         //Tag access block parameters
         var accessRetry = 3
         var accessBank = 1
         var accessBank2 = 0
         var accessOffset = 2
         var accessOffset2 = 0
         var accessCount = 1
         var accessCount2 = 0
         var accessLockAction = 0
         var accessLockMask = 0

         //long accessPassword = 0;
         // long killPassword = 0;
         var accessWriteDataSelect = 0
         lateinit var accWriteData0_63: ByteArray
         var accWriteDataReady = 0
         lateinit var authMatchData: ByteArray
         var authMatchDataReady = 0
      }

      var mDefault = Rx000Setting_default()
      fun readMAC(address: Int): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0, 0, 0, 0, 0, 0)
         msgBuffer[2] = (address % 256).toByte()
         msgBuffer[3] = ((address shr 8) % 256).toByte()
         if (false) appendToLog("readMac buffer = " + byteArrayToString(msgBuffer))
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.MAC_OPERATION,
            false,
            msgBuffer
         )
      }

      fun writeMAC(address: Int, value: Long): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 0, 0, 0, 0, 0)
         msgBuffer[2] = (address % 256).toByte()
         msgBuffer[3] = ((address shr 8) % 256).toByte()
         msgBuffer[4] = (value % 256).toByte()
         msgBuffer[5] = ((value shr 8) % 256).toByte()
         msgBuffer[6] = ((value shr 16) % 256).toByte()
         msgBuffer[7] = ((value shr 24) % 256).toByte()
         if (false) appendToLog("writeMac buffer = " + byteArrayToString(msgBuffer))
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.MAC_OPERATION,
            true,
            msgBuffer
         )
      }

      var macVer: String? = null
         get (){
            if (field == null) {
               readMAC(0)
            }
            return  field
         }
      var macVerBuild = 0

      var mac_last_command_duration: Long = 0
      fun getMacLastCommandDuration(request: Boolean): Long {
         if (request) {
            if (true) readMAC(9)
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, 0, 0, 0, 0, 0};
            //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.MAC_LAST_COMMAND_DURATION, false, msgBuffer);
         }
         return mac_last_command_duration
      }

      val DIAGCFG_INVALID = -1
      val DIAGCFG_MIN = 0
      val DIAGCFG_MAX = 0x3FF
      var diagnosticCfg = DIAGCFG_INVALID
      val diagnosticConfiguration: Int
         get() {
            if (diagnosticCfg < DIAGCFG_MIN || diagnosticCfg > DIAGCFG_MAX) {
               if (true) readMAC(0x201)
               //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 2, 0, 0, 0, 0};
               //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, false, msgBuffer);
            }
            return diagnosticCfg
         }

      fun setDiagnosticConfiguration(bCommmandActive: Boolean): Boolean {
         var diagnosticCfgNew = diagnosticCfg
         diagnosticCfgNew = diagnosticCfgNew and 0x0200.inv()
         if (bCommmandActive) diagnosticCfgNew = diagnosticCfgNew or 0x200
         if (diagnosticCfg == diagnosticCfgNew && sameCheck) return true
         diagnosticCfg = diagnosticCfgNew
         return writeMAC(
            0x201,
            diagnosticCfgNew.toLong()
         ) //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, true, msgBuffer);
      }

      var impinjExtensionValue = -1
      val impinjExtension: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            run {
               if (impinjExtensionValue < 0) readMAC(0x203)
               return impinjExtensionValue
            }
         }

      fun setImpinjExtension(tagFocus: Boolean, fastId: Boolean): Boolean {
         val iValue = (if (tagFocus) 0x10 else 0) or if (fastId) 0x20 else 0
         val bRetValue = writeMAC(0x203, iValue.toLong())
         if (bRetValue) impinjExtensionValue = iValue
         return bRetValue
      }

      var pwrMgmtStatus = -1
      fun getPwrMgmtStatus() {
         if (false) appendToLog("pwrMgmtStatus: getPwrMgmtStatus ")
         pwrMgmtStatus = -1
         readMAC(0x204)
      }

      val MBPADDR_INVALID = -1
      val MBPADDR_MIN = 0
      val MBPADDR_MAX = 0x1FFF
      var mbpAddress = MBPADDR_INVALID.toLong()
      fun setMBPAddress(mbpAddress: Long): Boolean {
         //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 4, 0, 0, 0, 0};
         if (mbpAddress < MBPADDR_MIN || mbpAddress > MBPADDR_MAX) return false
         //mbpAddress = mDefault.mbpAddress;
         if (this.mbpAddress == mbpAddress && sameCheck) return true
         //msgBuffer[4] = (byte) (mbpAddress % 256);
         //msgBuffer[5] = (byte) ((mbpAddress >> 8) % 256);
         this.mbpAddress = mbpAddress
         if (false) appendToLog("Going to writeMAC")
         return writeMAC(
            0x400,
            mbpAddress.toInt().toLong()
         ) //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_ADDR, true, msgBuffer);
      }

      val MBPDATA_INVALID = -1
      val MBPDATA_MIN = 0
      val MBPDATA_MAX = 0x1FFF
      var mbpData = MBPDATA_INVALID.toLong()
      fun setMBPData(mbpData: Long): Boolean {
         //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 4, 0, 0, 0, 0};
         if (mbpData < MBPADDR_MIN || mbpData > MBPADDR_MAX) return false
         //mbpData = mDefault.mbpData;
         if (this.mbpData == mbpData && sameCheck) return true
         //msgBuffer[4] = (byte) (mbpData % 256);
         //msgBuffer[5] = (byte) ((mbpData >> 8) % 256);
         this.mbpData = mbpData
         return writeMAC(
            0x401,
            mbpData.toInt().toLong()
         ) //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_DATA, true, msgBuffer);
      }

      val OEMADDR_INVALID = -1
      val OEMADDR_MIN = 0
      val OEMADDR_MAX = 0x1FFF
      var oemAddress = OEMADDR_INVALID.toLong()
      fun setOEMAddress(oemAddress: Long): Boolean {
         //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 5, 0, 0, 0, 0};
         if (oemAddress < OEMADDR_MIN || oemAddress > OEMADDR_MAX) return false
         //oemAddress = mDefault.oemAddress;
         if (this.oemAddress == oemAddress && sameCheck) return true
         //msgBuffer[4] = (byte) (oemAddress % 256);
         //msgBuffer[5] = (byte) ((oemAddress >> 8) % 256);
         this.oemAddress = oemAddress
         return writeMAC(
            0x500,
            oemAddress.toInt().toLong()
         ) //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_ADDR, true, msgBuffer);
      }

      val OEMDATA_INVALID = -1
      val OEMDATA_MIN = 0
      val OEMDATA_MAX = 0x1FFF
      var oemData = OEMDATA_INVALID.toLong()
      fun setOEMData(oemData: Long): Boolean {
         //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 5, 0, 0, 0, 0};
         if (oemData < OEMADDR_MIN || oemData > OEMADDR_MAX) return false
         //oemData = mDefault.oemData;
         if (this.oemData == oemData && sameCheck) return true
         //msgBuffer[4] = (byte) (oemData % 256);
         //msgBuffer[5] = (byte) ((oemData >> 8) % 256);
         this.oemData = oemData
         return writeMAC(
            0x501,
            oemData.toInt().toLong()
         ) //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_DATA, true, msgBuffer);
      }

      // Antenna block parameters
      val ANTCYCLE_INVALID = -1
      val ANTCYCLE_MIN = 0
      val ANTCYCLE_MAX = 0xFFFF
      var antennaCycle = ANTCYCLE_INVALID
         get() {
         if (field < ANTCYCLE_MIN || field > ANTCYCLE_MAX) hST_ANT_CYCLES
         return field
      }

      fun setAntennaCycle(antennaCycle: Int): Boolean {
         return setAntennaCycle(antennaCycle, antennaFreqAgile)
      }

      fun setAntennaCycle(antennaCycle: Int, antennaFreqAgile: Int): Boolean {
         var antennaCycle = antennaCycle
         var antennaFreqAgile = antennaFreqAgile
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 7, 0, 0, 0, 0)
         if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) antennaCycle =
            mDefault.antennaCycle
         if (antennaFreqAgile < FREQAGILE_MIN || antennaFreqAgile > FREQAGILE_MAX) antennaFreqAgile =
            mDefault.antennaFreqAgile
         if (this.antennaCycle == antennaCycle && this.antennaFreqAgile == antennaFreqAgile && sameCheck) return true
         msgBuffer[4] = (antennaCycle % 256).toByte()
         msgBuffer[5] = ((antennaCycle shr 8) % 256).toByte()
         if (antennaFreqAgile != 0) {
            msgBuffer[7] = (msgBuffer[7].toInt() or 0x01).toByte()
         }
         this.antennaCycle = antennaCycle
         this.antennaFreqAgile = antennaFreqAgile
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_ANT_CYCLES,
            true,
            msgBuffer
         )
      }

      val FREQAGILE_INVALID = -1
      val FREQAGILE_MIN = 0
      val FREQAGILE_MAX = 1
      var antennaFreqAgile = FREQAGILE_INVALID
         get() {
         if (field < FREQAGILE_MIN || field > FREQAGILE_MAX) hST_ANT_CYCLES
         return field
      }

      fun setAntennaFreqAgile(freqAgile: Int): Boolean {
         return setAntennaCycle(antennaCycle, freqAgile)
      }

      private val hST_ANT_CYCLES: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0, 7, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_ANT_CYCLES,
               false,
               msgBuffer
            )
         }
      val ANTSELECT_INVALID = -1
      val ANTSLECT_MIN = 0
      val ANTSELECT_MAX = 15
      var antennaSelect = ANTSELECT_INVALID //default value = 0
         get() : Int {
            if (field < ANTSLECT_MIN || field > ANTSELECT_MAX) {
               appendToLog("AntennaSelect = $field")
               val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 7, 0, 0, 0, 0)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_ANT_DESC_SEL,
                  false,
                  msgBuffer
               )
            }
            return field
         }
         set( aAntennaSelect){
            val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 7, 0, 0, 0, 0)
            if (aAntennaSelect < ANTSLECT_MIN || aAntennaSelect > ANTSELECT_MAX) {
               field = mDefault.antennaSelect
            }
            if (aAntennaSelect != field && !sameCheck) {
               appendToLog("antennaSelect is set to $antennaSelect")
               msgBuffer[4] = aAntennaSelect.toByte()
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_ANT_DESC_SEL,
                  true,
                  msgBuffer
               )
            }
            field = aAntennaSelect
         }


      var antennaSelectedData: Array<AntennaSelectedData?>
      val antennaEnable: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaEnable
         }

      fun setAntennaEnable(antennaEnable: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaEnable(antennaEnable)
      }

      fun setAntennaEnable(
         antennaEnable: Int,
         antennaInventoryMode: Int,
         antennaLocalAlgo: Int,
         antennaLocalStartQ: Int,
         antennaProfileMode: Int,
         antennaLocalProfile: Int,
         antennaFrequencyMode: Int,
         antennaLocalFrequency: Int
      ): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val antennaInventoryMode: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaInventoryMode
         }

      fun setAntennaInventoryMode(antennaInventoryMode: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaInventoryMode(antennaInventoryMode)
      }

      val antennaLocalAlgo: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaLocalAlgo
         }

      fun setAntennaLocalAlgo(antennaLocalAlgo: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaLocalAlgo(antennaLocalAlgo)
      }

      val antennaLocalStartQ: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaLocalStartQ
         }

      fun setAntennaLocalStartQ(antennaLocalStartQ: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaLocalStartQ(antennaLocalStartQ)
      }

      val antennaProfileMode: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaProfileMode
         }

      fun setAntennaProfileMode(antennaProfileMode: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaProfileMode(antennaProfileMode)
      }

      val antennaLocalProfile: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaLocalProfile
         }

      fun setAntennaLocalProfile(antennaLocalProfile: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaLocalProfile(antennaLocalProfile)
      }

      val antennaFrequencyMode: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaFrequencyMode
         }

      fun setAntennaFrequencyMode(antennaFrequencyMode: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaFrequencyMode(antennaFrequencyMode)
      }

      val antennaLocalFrequency: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaLocalFrequency
         }

      fun setAntennaLocalFrequency(antennaLocalFrequency: Int): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaLocalFrequency(antennaLocalFrequency)
      }

      val antennaStatus: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaStatus
         }
      val antennaDefine: Int
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            antennaSelectedData[antennaSelect]!!.antennaDefine
         }
      val antennaDwell: Long
         get() = if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID.toLong()
         } else {
            antennaSelectedData[antennaSelect]!!.antennaDwell
         }

      fun setAntennaDwell(antennaDwell: Long): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaDwell(antennaDwell)
      }

      fun getAntennaPower(portNumber: Int): Long {
         var portNumber = portNumber
         return if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID.toLong()
         } else {
            if (portNumber < 0 || portNumber > 15) portNumber = antennaSelect
            val lValue: Long
            lValue = antennaSelectedData[portNumber]!!.antennaPower
            lValue
         }
      }

      fun setAntennaPower(antennaPower: Long): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaPower(antennaPower)
      }

      var antennaInvCount: Long = ANTSELECT_INVALID.toLong()
         get () {
            return if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
               ANTSELECT_INVALID.toLong()
            } else {
               antennaSelectedData[antennaSelect]!!.antennaInvCount
            }
         }

      fun setAntennaInvCount(antennaInvCount: Long): Boolean {
         if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            antennaSelect = mDefault.antennaSelect
            appendToLog("antennaSelect is set to $antennaSelect")
         }
         return antennaSelectedData[antennaSelect]!!.setAntennaInvCount(antennaInvCount)
      }

      //Tag select block parameters
      val INVSELECT_INVALID = -1
      val INVSELECT_MIN = 0
      val INVSELECT_MAX = 7
      var invSelectIndex = INVSELECT_INVALID
          get(): Int {
         if (field < INVSELECT_MIN || field > INVSELECT_MAX) {
            run {
               val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0, 8, 0, 0, 0, 0)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_TAGMSK_DESC_SEL,
                  false,
                  msgBuffer
               )
            }
         }
         return field
      }

      fun setInvSelectIndex(invSelect: Int): Boolean {
         var invSelect = invSelect
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 8, 0, 0, 0, 0)
         if (invSelect < INVSELECT_MIN || invSelect > INVSELECT_MAX) invSelect =
            mDefault.invSelectIndex
         if (invSelectIndex == invSelect && sameCheck) return true
         msgBuffer[4] = (invSelect and 0x07).toByte()
         invSelectIndex = invSelect
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_DESC_SEL,
            true,
            msgBuffer
         )
      }

      var invSelectData: Array<InvSelectData?>
      val selectEnable: Int
         get() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
               mDefault.invSelectIndex
            return invSelectData[invSelectIndex]!!.selectEnable
         }

      fun setSelectEnable(
         enable: Int,
         selectTarget: Int,
         selectAction: Int,
         selectDelay: Int
      ): Boolean {
         if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
            mDefault.invSelectIndex
         return invSelectData[invSelectIndex]!!
            .setRx000HostReg_HST_TAGMSK_DESC_CFG(enable, selectTarget, selectAction, selectDelay)
      }

      val selectTarget: Int
         get() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
               mDefault.invSelectIndex
            return invSelectData[invSelectIndex]!!.selectTarget
         }
      val selectAction: Int
         get() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
               mDefault.invSelectIndex
            return invSelectData[invSelectIndex]!!.selectAction
         }
      val selectMaskBank: Int
         get() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
               mDefault.invSelectIndex
            return invSelectData[invSelectIndex]!!.selectMaskBank
         }

      fun setSelectMaskBank(selectMaskBank: Int): Boolean {
         if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
            mDefault.invSelectIndex
         return invSelectData[invSelectIndex]!!.setSelectMaskBank(selectMaskBank)
      }

      val selectMaskOffset: Int
         get() {
            val dataIndex = invSelectIndex
            return if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
               INVSELECT_INVALID
            } else {
               invSelectData[dataIndex]!!.selectMaskOffset
            }
         }

      fun setSelectMaskOffset(selectMaskOffset: Int): Boolean {
         if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
            mDefault.invSelectIndex
         return invSelectData[invSelectIndex]!!.setSelectMaskOffset(selectMaskOffset)
      }

      val selectMaskLength: Int
         get() {
            val dataIndex = invSelectIndex
            return if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
               INVSELECT_INVALID
            } else {
               invSelectData[dataIndex]!!.selectMaskLength
            }
         }

      fun setSelectMaskLength(selectMaskLength: Int): Boolean {
         if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
            mDefault.invSelectIndex
         return invSelectData[invSelectIndex]!!.setSelectMaskLength(selectMaskLength)
      }

      val selectMaskData: String?
         get() {
            val dataIndex = invSelectIndex
            return if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
               null
            } else {
               invSelectData[dataIndex]!!.rx000SelectMaskData
            }
         }

      fun setSelectMaskData(maskData: String?): Boolean {
         if (maskData == null) return false
         if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex =
            mDefault.invSelectIndex
         if (invSelectData[invSelectIndex]!!.selectMaskDataReady.toInt() != 0) {
            val maskDataOld = selectMaskData
            if (maskData != null && maskDataOld != null) {
               if (maskData.matches(maskDataOld.toRegex()) && sameCheck) return true
            }
         }
         return invSelectData[invSelectIndex]!!.setRx000SelectMaskData(maskData)
      }

      //Inventtory block paraameters
      val QUERYTARGET_INVALID = -1
      val QUERYTARGET_MIN = 0
      val QUERYTARGET_MAX = 1
      var queryTarget = QUERYTARGET_INVALID
       get(): Int {
         if (field < QUERYTARGET_MIN || field > QUERYTARGET_MAX) hST_QUERY_CFG
         return field
      }

      fun setQueryTarget(queryTarget: Int): Boolean {
         return setQueryTarget(queryTarget, querySession, querySelect)
      }

      fun setQueryTarget(queryTarget: Int, querySession: Int, querySelect: Int): Boolean {
         var queryTarget = queryTarget
         var querySession = querySession
         var querySelect = querySelect
         if (queryTarget >= 2) {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoAbFlip(1)
         } else if (queryTarget >= 0) {
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setAlgoAbFlip(0)
         }
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 9, 0, 0, 0, 0)
         if (queryTarget != 2 && (queryTarget < QUERYTARGET_MIN || queryTarget > QUERYTARGET_MAX)) queryTarget =
            mDefault.queryTarget
         if (querySession < QUERYSESSION_MIN || querySession > QUERYSESSION_MAX) querySession =
            mDefault.querySession
         if (querySelect < QUERYSELECT_MIN || querySelect > QUERYSELECT_MAX) querySelect =
            mDefault.querySelect
         if (this.queryTarget == queryTarget && this.querySession == querySession && this.querySelect == querySelect && sameCheck) return true
         msgBuffer[4] =
            (msgBuffer[4].toInt() or ((if (queryTarget == 2) 0 else queryTarget) shl 4)).toByte()
         msgBuffer[4] = (msgBuffer[4].toInt() or (querySession shl 5).toByte().toInt()).toByte()
         if (querySelect and 0x01 != 0) {
            msgBuffer[4] = (msgBuffer[4].toInt() or 0x80.toByte().toInt()).toByte()
         }
         if (querySelect and 0x02 != 0) {
            msgBuffer[5] = (msgBuffer[5].toInt() or 0x01.toByte().toInt()).toByte()
         }
         this.queryTarget = queryTarget
         this.querySession = querySession
         this.querySelect = querySelect
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_QUERY_CFG,
            true,
            msgBuffer
         )
      }

      val QUERYSESSION_INVALID = -1
      val QUERYSESSION_MIN = 0
      val QUERYSESSION_MAX = 3
      var querySession = QUERYSESSION_INVALID
       get(): Int {
         return if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            run {
               if (field < QUERYSESSION_MIN || field > QUERYSESSION_MAX) this.hST_QUERY_CFG
               return field
            }
         }
      }

      fun setQuerySession(querySession: Int): Boolean {
         return setQueryTarget(queryTarget, querySession, querySelect)
      }

      val QUERYSELECT_INVALID = -1
      val QUERYSELECT_MIN = 0
      val QUERYSELECT_MAX = 3
      var querySelect = QUERYSELECT_INVALID
       get(): Int {
         if (field < QUERYSELECT_MIN || field > QUERYSELECT_MAX) hST_QUERY_CFG
         if (false) appendToLog("Stream querySelect = $field")
         return field
      }

      fun setQuerySelect(querySelect: Int): Boolean {
         return setQueryTarget(queryTarget, querySession, querySelect)
      }

      private val hST_QUERY_CFG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_QUERY_CFG,
               false,
               msgBuffer
            )
         }
      val INVALGO_INVALID = -1
      val INVALGO_MIN = 0
      val INVALGO_MAX = 3
      var invAlgo = INVALGO_INVALID
       get(): Int {
         if (field < INVALGO_MIN || field > INVALGO_MAX) hST_INV_CFG
         return field
      }

      fun setInvAlgo(invAlgo: Int): Boolean {
         if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      val MATCHREP_INVALID = -1
      val MATCHREP_MIN = 0
      val MATCHREP_MAX = 255
      var matchRep = MATCHREP_INVALID
       get(): Int {
         if (field < MATCHREP_MIN || field > MATCHREP_MAX) hST_INV_CFG
         return field
      }

      fun setMatchRep(matchRep: Int): Boolean {
         appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      val TAGSELECT_INVALID = -1
      val TAGSELECT_MIN = 0
      val TAGSELECT_MAX = 1
      var tagSelect = TAGSELECT_INVALID
       get(): Int {
         if (field < TAGSELECT_MIN || field > TAGSELECT_MAX) hST_INV_CFG
         return field
      }

      fun setTagSelect(tagSelect: Int): Boolean {
         if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      val NOINVENTORY_INVALID = -1
      val NOINVENTORY_MIN = 0
      val NOINVENTORY_MAX = 1
      var noInventory = NOINVENTORY_INVALID
       get(): Int {
         if (field < NOINVENTORY_MIN || field > NOINVENTORY_MAX) hST_INV_CFG
         return field
      }

      fun setNoInventory(noInventory: Int): Boolean {
         appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      val TAGREAD_INVALID = -1
      val TAGREAD_MIN = 0
      val TAGREAD_MAX = 2
      var tagRead = TAGREAD_INVALID
       get(): Int {
         if (field < TAGREAD_MIN || field > TAGREAD_MAX) hST_INV_CFG
         return field
      }

      fun setTagRead(tagRead: Int): Boolean {
         appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      val TAGDELAY_INVALID = -1
      val TAGDELAY_MIN = 0
      val TAGDELAY_MAX = 63
      var tagDelay = TAGDELAY_INVALID
       get(): Int {
         if (field < TAGDELAY_MIN || field > TAGDELAY_MAX) hST_INV_CFG
         return field
      }

      fun setTagDelay(tagDelay: Int): Boolean {
         if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            invBrandId
         )
      }

      var intraPacketDelay: Byte = 4
       get(): Byte {
         appendToLog("intraPacketDelay = $field")
         return field
      }

      fun setIntraPacketDelay(intraPkDelay: Byte): Boolean {
         if (intraPacketDelay == intraPkDelay && sameCheck) {
            appendToLog("!!! Skip sending repeated data with intraPkDelay = $intraPkDelay")
            return true
         }
         appendToLog("Skip setDupElim with intraPkDelay = $intraPkDelay")
         intraPacketDelay = intraPkDelay
         return true
      }

      var dupElimRollWindow: Byte = 0
       get(): Byte {
         appendToLog("dupElim = $field")
         return field
      }

      fun setDupElimRollWindow(dupElimDelay: Byte): Boolean {
         if (dupElimRollWindow == dupElimDelay && sameCheck) {
            appendToLog("!!! Skip sending repeated data with dupElimDelay = $dupElimDelay")
            return true
         }
         appendToLog("Skip setDupElim with dupElimDelay = $dupElimDelay")
         dupElimRollWindow = dupElimDelay
         return true
      }

      var cycleDelay: Long = 0
      fun setCycleDelay(cycleDelay: Long): Boolean {
         if (this.cycleDelay == cycleDelay && sameCheck) return true
         val msgBuffer =
            byteArrayOf(0x70.toByte(), 1, 0x0F.toByte(), 0x0F.toByte(), 0, 0, 0, 0)
         msgBuffer[4] = (msgBuffer[4].toLong() or (cycleDelay and 0xFFL)).toByte()
         msgBuffer[5] = (msgBuffer[5]
            .toInt() or (cycleDelay and 0xFF00L shr 8).toByte().toInt()).toByte()
         msgBuffer[6] = (msgBuffer[6]
            .toInt() or (cycleDelay and 0xFF0000L shr 16).toByte().toInt()).toByte()
         msgBuffer[7] = (msgBuffer[7]
            .toInt() or (cycleDelay and 0xFF000000L shr 24).toByte().toInt()).toByte()
         this.cycleDelay = cycleDelay
         //msgBuffer = new byte[]{(byte) 0x70, 0, (byte)0x0F, (byte)0x0F, 0, 0, 0, 0};
         //mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_CFG, false, msgBuffer);
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_CFG,
            true,
            msgBuffer
         )
      }

      val AUTHENTICATE_CFG_INVALID = -1
      val AUTHENTICATE_CFG_MIN = 0
      val AUTHENTICATE_CFG_MAX = 4095
      var authenticateSendReply = false
      var authenticateIncReplyLength = false
      var authenticateLength = AUTHENTICATE_CFG_INVALID
      val authenticateReplyLength: Int
         get() {
            if (authenticateLength < AUTHENTICATE_CFG_MIN || authenticateLength > AUTHENTICATE_CFG_MAX) hST_AUTHENTICATE_CFG
            return authenticateLength
         }
      private val hST_AUTHENTICATE_CFG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0, 0x0F.toByte(), 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_AUTHENTICATE_CFG,
               false,
               msgBuffer
            )
         }

      fun setHST_AUTHENTICATE_CFG(
         sendReply: Boolean,
         incReplyLenth: Boolean,
         csi: Int,
         length: Int
      ): Boolean {
         appendToLog("sendReply = $sendReply, incReplyLenth = $incReplyLenth, length = $length")
         if (length < 0 || length > 0x3FF) return false
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 0x0F.toByte(), 0, 0, 0, 0)
         if (sendReply) msgBuffer[4] = (msgBuffer[4].toInt() or 0x01).toByte()
         authenticateSendReply = sendReply
         if (incReplyLenth) msgBuffer[4] = (msgBuffer[4].toInt() or 0x02).toByte()
         authenticateIncReplyLength = incReplyLenth
         msgBuffer[4] = (msgBuffer[4].toInt() or (csi and 0x3F shl 2)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (csi shr 6 and 0x03)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (length and 0x3F shl 2)).toByte()
         msgBuffer[6] = (msgBuffer[6].toInt() or (length and 0xFC0 shr 6)).toByte()
         authenticateLength = length
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_AUTHENTICATE_CFG,
            true,
            msgBuffer
         )
      }

      var authMatchData0_63: ByteArray
      var authMatchDataReady = 0
      val authMatchData: String?
         get() {
            var length = 96
            var strValue: String? = ""
            for (i in 0..2) {
               if (length > 0) {
                  appendToLog("i = $i, authMatchDataReady = $authMatchDataReady")
                  if (authMatchDataReady and (0x01 shl i) == 0) {
                     val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 0x0F.toByte(), 0, 0, 0, 0)
                     msgBuffer[2] = (msgBuffer[2] + i).toByte()
                     mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                        HostRegRequests.HST_AUTHENTICATE_MSG,
                        false,
                        msgBuffer
                     )
                  } else {
                     for (j in 0..3) {
                        strValue += String.format("%02X", authMatchData0_63[i * 4 + j])
                     }
                  }
                  length -= 32
               }
            }
            if (strValue!!.length < 16) strValue = null
            return strValue
         }

      fun setAuthMatchData(matchData: String): Boolean {
         var length = matchData.length
         for (i in 0..5) {
            if (length > 0) {
               length -= 8
               val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 0x0F.toByte(), 0, 0, 0, 0)
               val hexString = "0123456789ABCDEF"
               for (j in 0..7) {
                  if (i * 8 + j + 1 <= matchData.length) {
                     val subString = matchData.substring(i * 8 + j, i * 8 + j + 1).uppercase(
                        Locale.getDefault()
                     )
                     var k = 0
                     k = 0
                     while (k < 16) {
                        if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                           break
                        }
                        k++
                     }
                     if (k == 16) return false
                     if (j / 2 * 2 == j) {
                        msgBuffer[7 - j / 2] = (msgBuffer[7 - j / 2].toInt() or (k shl 4).toByte()
                           .toInt()).toByte()
                     } else {
                        msgBuffer[7 - j / 2] =
                           (msgBuffer[7 - j / 2].toInt() or k.toByte().toInt()).toByte()
                     }
                  }
               }
               msgBuffer[2] = ((msgBuffer[2].toInt() and 0xFF) + i).toByte()
               if (!mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                     HostRegRequests.HST_AUTHENTICATE_MSG,
                     true,
                     msgBuffer
                  )
               ) return false else {
                  //authMatchDataReady |= (0x01 << i);
                  System.arraycopy(
                     msgBuffer,
                     4,
                     authMatchData0_63,
                     i * 4,
                     4
                  ) //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
                  //                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
               }
            }
         }
         return true
      }

      val UNTRACEABLE_CFG_INVALID = -1
      val UNTRACEABLE_CFG_MIN = 0
      val UNTRACEABLE_CFG_MAX = 3
      var untraceableRange = UNTRACEABLE_CFG_INVALID
      var untraceableUser = false
      var untraceableTid = UNTRACEABLE_CFG_INVALID
      var untraceableEpc = false
      var untraceableUXpc = false
      var untraceableEpcLength = UNTRACEABLE_CFG_INVALID
       get(): Int {
         if (field < UNTRACEABLE_CFG_MIN || field > UNTRACEABLE_CFG_MAX) hST_UNTRACEABLE_CFG
         return field
      }

      private val hST_UNTRACEABLE_CFG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 5, 0x0F.toByte(), 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_UNTRACEABLE_CFG,
               false,
               msgBuffer
            )
         }

      fun setHST_UNTRACEABLE_CFG(
         range: Int,
         user: Boolean,
         tid: Int,
         epcLength: Int,
         epc: Boolean,
         uxpc: Boolean
      ): Boolean {
         appendToLog("range = $range, user = $user, tid = $tid, epc = $epc, epcLength = $epcLength, xcpc = $uxpc")
         if (range < 0 || range > 3) return false
         if (tid < 0 || tid > 2) return false
         if (epcLength < 0 || epcLength > 31) return false
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 5, 0x0F.toByte(), 0, 0, 0, 0)
         msgBuffer[4] = (msgBuffer[4].toInt() or range).toByte()
         untraceableRange = range
         if (user) msgBuffer[4] = (msgBuffer[4].toInt() or 0x04).toByte()
         untraceableUser = user
         msgBuffer[4] = (msgBuffer[4].toInt() or (tid shl 3)).toByte()
         untraceableTid = tid
         msgBuffer[4] = (msgBuffer[4].toInt() or (epcLength and 0x7 shl 5)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (epcLength and 0x18 shr 3)).toByte()
         untraceableEpcLength = epcLength
         if (epc) msgBuffer[5] = (msgBuffer[5].toInt() or 0x04).toByte()
         untraceableEpc = epc
         if (uxpc) msgBuffer[5] = (msgBuffer[5].toInt() or 0x08).toByte()
         untraceableUXpc = uxpc
         appendToLog("going to do sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG,")
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_UNTRACEABLE_CFG,
            true,
            msgBuffer
         )
      }

      val TAGJOIN_INVALID = -1
      val TAGJOIN_MIN = 0
      val TAGJOIN_MAX = 1
      var invModeCompact = TAGJOIN_INVALID
      fun getInvModeCompact(): Boolean {
         if (invModeCompact < TAGDELAY_MIN || invModeCompact > TAGDELAY_MAX) {
            hST_INV_CFG
            return false
         }
         return if (invModeCompact == 1) true else false
      }

      fun setInvModeCompact(invModeCompact: Boolean): Boolean {
         if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with invAlgo = $invAlgo")
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            if (invModeCompact) 1 else 0,
            invBrandId
         )
      }

      val BRAND_INVALID = -1
      val BRANDID_MIN = 0
      val BRANDID_MAX = 1
      var invBrandId = BRAND_INVALID
      fun getInvBrandId(): Boolean {
         if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) {
            hST_INV_CFG
            return false
         }
         return if (invModeCompact == 1) true else false
      }

      fun setInvBrandId(invBrandId: Boolean): Boolean {
         return setInvAlgo(
            invAlgo,
            matchRep,
            tagSelect,
            noInventory,
            tagRead,
            tagDelay,
            invModeCompact,
            if (invBrandId) 1 else 0
         )
      }

      private val hST_INV_CFG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_CFG,
               false,
               msgBuffer
            )
         }

      fun setInvAlgo(
         invAlgo: Int,
         matchRep: Int,
         tagSelect: Int,
         noInventory: Int,
         tagRead: Int,
         tagDelay: Int,
         invModeCompact: Int,
         invBrandId: Int
      ): Boolean {
         var invAlgo = invAlgo
         var matchRep = matchRep
         var tagSelect = tagSelect
         var noInventory = noInventory
         var tagRead = tagRead
         var tagDelay = tagDelay
         var invModeCompact = invModeCompact
         var invBrandId = invBrandId
         val DEBUG = false
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 9, 0, 0, 0, 0)
         if (invAlgo < INVALGO_MIN || invAlgo > INVALGO_MAX) invAlgo = mDefault.invAlgo
         if (matchRep < MATCHREP_MIN || matchRep > MATCHREP_MAX) matchRep = mDefault.matchRep
         if (tagSelect < TAGSELECT_MIN || tagSelect > TAGSELECT_MAX) tagSelect = mDefault.tagSelect
         if (noInventory < NOINVENTORY_MIN || noInventory > NOINVENTORY_MAX) noInventory =
            mDefault.noInventory
         if (tagDelay < TAGDELAY_MIN || tagDelay > TAGDELAY_MAX) tagDelay = mDefault.tagDelay
         if (invModeCompact < TAGJOIN_MIN || invModeCompact > TAGJOIN_MAX) invModeCompact =
            mDefault.tagJoin
         if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) invBrandId = mDefault.brandid
         if (tagRead < TAGREAD_MIN || tagRead > TAGREAD_MAX) tagRead = mDefault.tagRead
         if (DEBUG) appendToLog("Old invAlgo = " + this.invAlgo + ", matchRep = " + this.matchRep + ", tagSelect =" + this.tagSelect + ", noInventory = " + this.noInventory + ", tagRead = " + this.tagRead + ", tagDelay = " + this.tagDelay + ", invModeCompact = " + this.invModeCompact + ", invBrandId = " + this.invBrandId)
         if (DEBUG) appendToLog("New invAlgo = $invAlgo, matchRep = $matchRep, tagSelect =$tagSelect, noInventory = $noInventory, tagRead = $tagRead, tagDelay = $tagDelay, invModeCompact = $invModeCompact, invBrandId = $invBrandId, sameCheck = $sameCheck")
         if (this.invAlgo == invAlgo && this.matchRep == matchRep && this.tagSelect == tagSelect && this.noInventory == noInventory && this.tagRead == tagRead && this.tagDelay == tagDelay && this.invModeCompact == invModeCompact && this.invBrandId == invBrandId && sameCheck) return true
         if (DEBUG) appendToLog("There is difference")
         msgBuffer[4] = (msgBuffer[4].toInt() or invAlgo).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (matchRep and 0x03 shl 6).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (matchRep shr 2).toByte().toInt()).toByte()
         if (tagSelect != 0) {
            msgBuffer[5] = (msgBuffer[5].toInt() or 0x40).toByte()
         }
         if (noInventory != 0) {
            msgBuffer[5] = (msgBuffer[5].toInt() or 0x80).toByte()
         }
         if (tagRead and 0x03 != 0) {
            msgBuffer[6] = (msgBuffer[6].toInt() or (tagRead and 0x03)).toByte()
         }
         if (tagDelay and 0x0F != 0) {
            msgBuffer[6] = (msgBuffer[6].toInt() or (tagDelay and 0x0F shl 4)).toByte()
         }
         if (tagDelay and 0x30 != 0) {
            msgBuffer[7] = (msgBuffer[7].toInt() or (tagDelay and 0x30 shr 4)).toByte()
         }
         if (invModeCompact == 1) {
            msgBuffer[7] = (msgBuffer[7].toInt() or 0x04).toByte()
         }
         if (invBrandId == 1) {
            msgBuffer[7] = (msgBuffer[7].toInt() or 0x08).toByte()
         }
         this.invAlgo = invAlgo
         if (DEBUG) appendToLog("Hello6: invAlgo = $invAlgo, queryTarget = $queryTarget")
         this.matchRep = matchRep
         this.tagSelect = tagSelect
         this.noInventory = noInventory
         this.tagRead = tagRead
         this.tagDelay = tagDelay
         this.invModeCompact = invModeCompact
         this.invBrandId = invBrandId
         if (DEBUG) appendToLog("Stored tagDelay = " + this.tagDelay)
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_CFG,
            true,
            msgBuffer
         )
      }

      val ALGOSELECT_INVALID = -1
      val ALGOSELECT_MIN = 0
      val ALGOSELECT_MAX = 3 //DataSheet says Max=1
      var algoSelect = ALGOSELECT_INVALID
       get(): Int {
         if (field < ALGOSELECT_MIN || field > ALGOSELECT_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 2, 9, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_SEL,
               false,
               msgBuffer
            )
         }
         return field
      }

      var dummyAlgoSelected = false
      fun setAlgoSelect(algoSelect: Int): Boolean {
         var algoSelect = algoSelect
         if (false) appendToLog("setTagGroup: algoSelect = " + algoSelect + ", this.algoSelct = " + this.algoSelect + ", dummyAlgoSelected = " + dummyAlgoSelected)
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 9, 0, 0, 0, 0)
         if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) algoSelect =
            mDefault.algoSelect
         if (algoSelect == this.algoSelect && !dummyAlgoSelected) return true
         msgBuffer[4] = (algoSelect and 0xFF).toByte()
         msgBuffer[5] = (algoSelect and 0xFF00 shr 8).toByte()
         msgBuffer[6] = (algoSelect and 0xFF0000 shr 16).toByte()
         msgBuffer[7] = (algoSelect and -0x1000000 shr 24).toByte()
         this.algoSelect = algoSelect
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_SEL,
            true,
            msgBuffer
         )
      }

      var algoSelectedData: Array<AlgoSelectedData?>
      fun getAlgoStartQ(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.getAlgoStartQ(false)
         }
      }

      val algoStartQ: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.getAlgoStartQ(true)
         }

      fun setAlgoStartQ(algoStartQ: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoStartQ(
            algoStartQ
         )
      }

      fun setAlgoStartQ(
         startQ: Int,
         algoMaxQ: Int,
         algoMinQ: Int,
         algoMaxRep: Int,
         algoHighThres: Int,
         algoLowThres: Int
      ): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!
            .setAlgoStartQ(startQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres)
      }

      fun getAlgoMaxQ(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoMaxQ
         }
      }

      val algoMaxQ: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoMaxQ
         }

      fun setAlgoMaxQ(algoMaxQ: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoMaxQ(
            algoMaxQ
         )
      }

      fun getAlgoMinQ(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoMaxQ
         }
      }

      val algoMinQ: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoMaxQ
         }

      fun setAlgoMinQ(algoMinQ: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoMinQ(
            algoMinQ
         )
      }

      val algoMaxRep: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoMaxRep
         }

      fun setAlgoMaxRep(algoMaxRep: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoMaxRep(
            algoMaxRep
         )
      }

      val algoHighThres: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoHighThres
         }

      fun setAlgoHighThres(algoHighThre: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoHighThres(
            algoHighThre
         )
      }

      val algoLowThres: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoLowThres
         }

      fun setAlgoLowThres(algoLowThre: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoLowThres(
            algoLowThre
         )
      }

      fun getAlgoRetry(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoRetry
         }
      }

      fun setAlgoRetry(algoRetry: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoRetry(
            algoRetry
         )
      }

      fun getAlgoAbFlip(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoAbFlip
         }
      }

      val algoAbFlip: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoAbFlip
         }

      fun setAlgoAbFlip(algoAbFlip: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!.setAlgoAbFlip(
            algoAbFlip
         )
      }

      fun setAlgoAbFlip(algoAbFlip: Int, algoRunTilZero: Int): Boolean {
         if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false
         appendToLog("algoSelect = $algoSelect, algoAbFlip = $algoAbFlip, algoRunTilZero = $algoRunTilZero")
         return algoSelectedData[algoSelect]!!.setAlgoAbFlip(algoAbFlip, algoRunTilZero)
      }

      fun getAlgoRunTilZero(algoSelect: Int): Int {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoRunTilZero
         }
      }

      val algoRunTilZero: Int
         get() = if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
            ALGOSELECT_INVALID
         } else {
            algoSelectedData[algoSelect]!!.algoRunTilZero
         }

      fun setAlgoRunTilZero(algoRunTilZero: Int): Boolean {
         return if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) false else algoSelectedData[algoSelect]!!
            .setAlgoRunTilZero(algoRunTilZero)
      }

      var rssiFilterConfig = -1
      val RSSIFILTERTYPE_INVALID = -1
      val RSSIFILTERTYPE_MIN = 0
      val RSSIFILTERTYPE_MAX = 2
      val RSSIFILTEROPTION_INVALID = -1
      val RSSIFILTEROPTION_MIN = 0
      val RSSIFILTEROPTION_MAX = 4
      var rssiFilterType = RSSIFILTERTYPE_INVALID
       get(): Int {
         if (field < 0) hST_INV_RSSI_FILTERING_CONFIG
         return field
      }
      var rssiFilterOption = RSSIFILTEROPTION_INVALID
       get(): Int {
         if (field < 0) hST_INV_RSSI_FILTERING_CONFIG
         return field
      }

      private val hST_INV_RSSI_FILTERING_CONFIG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 7, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG,
               false,
               msgBuffer
            )
         }

      fun setHST_INV_RSSI_FILTERING_CONFIG(rssiFilterType: Int, rssiFilterOption: Int): Boolean {
         var rssiFilterType = rssiFilterType
         var rssiFilterOption = rssiFilterOption
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 7, 9, 0, 0, 0, 0)
         if (rssiFilterType < RSSIFILTERTYPE_MIN || rssiFilterType > RSSIFILTERTYPE_MAX) rssiFilterType =
            mDefault.rssiFilterType
         if (rssiFilterOption < RSSIFILTEROPTION_MIN || matchType > RSSIFILTEROPTION_MAX) rssiFilterOption =
            mDefault.rssiFilterOption
         if (this.rssiFilterType == rssiFilterType && this.rssiFilterOption == rssiFilterOption && sameCheck) return true
         msgBuffer[4] = (msgBuffer[4].toInt() or (rssiFilterType and 0xF).toByte().toInt()).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (rssiFilterOption and 0xF shl 4).toByte().toInt()).toByte()
         this.rssiFilterType = rssiFilterType
         this.rssiFilterOption = rssiFilterOption
         val bValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG,
            true,
            msgBuffer
         )
         if (false) hST_INV_RSSI_FILTERING_CONFIG
         return bValue
      }

      val RSSIFILTERTHRESHOLD_INVALID = -1
      val RSSIFILTERTHRESHOLD_MIN = 0
      val RSSIFILTERTHRESHOLD_MAX = 0xFFFF
      var rssiFilterThreshold1 = RSSIFILTERTHRESHOLD_INVALID
      get(): Int {
         if (field < 0) {
            hST_INV_RSSI_FILTERING_THRESHOLD
         }
         return field
      }

      var rssiFilterThreshold2 = RSSIFILTERTHRESHOLD_INVALID
       get(): Int {
         if (field < 0) {
            hST_INV_RSSI_FILTERING_THRESHOLD
         }
         return field
      }

      private val hST_INV_RSSI_FILTERING_THRESHOLD: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 8, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD,
               false,
               msgBuffer
            )
         }

      fun setHST_INV_RSSI_FILTERING_THRESHOLD(
         rssiFilterThreshold1: Int,
         rssiFilterThreshold2: Int
      ): Boolean {
         var rssiFilterThreshold1 = rssiFilterThreshold1
         var rssiFilterThreshold2 = rssiFilterThreshold2
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 8, 9, 0, 0, 0, 0)
         if (rssiFilterThreshold1 < RSSIFILTERTHRESHOLD_MIN || rssiFilterThreshold1 > RSSIFILTERTHRESHOLD_MAX) rssiFilterThreshold1 =
            mDefault.rssiFilterThreshold
         if (rssiFilterThreshold2 < RSSIFILTERTHRESHOLD_MIN || rssiFilterThreshold2 > RSSIFILTERTHRESHOLD_MAX) rssiFilterThreshold2 =
            mDefault.rssiFilterThreshold
         if (this.rssiFilterThreshold1 == rssiFilterThreshold1 && this.rssiFilterThreshold2 == rssiFilterThreshold2 && sameCheck) return true
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (rssiFilterThreshold1 and 0xFF).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (rssiFilterThreshold1 shr 8 and 0xFF).toByte()
            .toInt()).toByte()
         msgBuffer[6] =
            (msgBuffer[6].toInt() or (rssiFilterThreshold2 and 0xFF).toByte().toInt()).toByte()
         msgBuffer[7] = (msgBuffer[7].toInt() or (rssiFilterThreshold2 shr 8 and 0xFF).toByte()
            .toInt()).toByte()
         this.rssiFilterThreshold1 = rssiFilterThreshold1
         this.rssiFilterThreshold2 = rssiFilterThreshold2
         val bValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD,
            true,
            msgBuffer
         )
         if (false) hST_INV_RSSI_FILTERING_THRESHOLD
         return bValue
      }

      val RSSIFILTERCOUNT_INVALID: Long = -1
      val RSSIFILTERCOUNT_MIN: Long = 0
      val RSSIFILTERCOUNT_MAX: Long = 1000000
      var rssiFilterCount = RSSIFILTERCOUNT_INVALID
       get(): Long {
         if (field < 0) hST_INV_RSSI_FILTERING_COUNT
         return field
      }

      private val hST_INV_RSSI_FILTERING_COUNT: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 9, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD,
               false,
               msgBuffer
            )
         }

      fun setHST_INV_RSSI_FILTERING_COUNT(rssiFilterCount: Long): Boolean {
         var rssiFilterCount = rssiFilterCount
         appendToLog("entry: rssiFilterCount = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount)
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 9, 9, 0, 0, 0, 0)
         if (rssiFilterCount < RSSIFILTERCOUNT_MIN || rssiFilterCount > RSSIFILTERCOUNT_MAX) rssiFilterCount =
            mDefault.rssiFilterCount
         appendToLog("rssiFilterCount 1 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount)
         if (this.rssiFilterCount == rssiFilterCount && sameCheck) return true
         appendToLog("rssiFilterCount 2 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount)
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (rssiFilterCount and 0xFFL).toByte().toInt()).toByte()
         msgBuffer[5] =
            (msgBuffer[5].toInt() or (rssiFilterCount shr 8 and 0xFFL).toByte().toInt()).toByte()
         msgBuffer[6] =
            (msgBuffer[6].toInt() or (rssiFilterCount shr 16 and 0xFFL).toByte().toInt()).toByte()
         msgBuffer[7] =
            (msgBuffer[7].toInt() or (rssiFilterCount shr 24 and 0xFFL).toByte().toInt()).toByte()
         this.rssiFilterCount = rssiFilterCount
         appendToLog("entering to sendHostRegRequest: rssiFilterCount = $rssiFilterCount")
         val bValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_RSSI_FILTERING_COUNT,
            true,
            msgBuffer
         )
         appendToLog("after sendHostRegRequest: rssiFilterCount = $rssiFilterCount")
         return bValue
      }

      val MATCHENABLE_INVALID = -1
      val MATCHENABLE_MIN = 0
      val MATCHENABLE_MAX = 1
      var matchEnable = MATCHENABLE_INVALID
      val invMatchEnable: Int
         get() {
            hST_INV_EPC_MATCH_CFG
            return matchEnable
         }

      fun setInvMatchEnable(matchEnable: Int): Boolean {
         return setHST_INV_EPC_MATCH_CFG(matchEnable, matchType, matchLength, matchOffset)
      }

      fun setInvMatchEnable(
         matchEnable: Int,
         matchType: Int,
         matchLength: Int,
         matchOffset: Int
      ): Boolean {
         return setHST_INV_EPC_MATCH_CFG(matchEnable, matchType, matchLength, matchOffset)
      }

      val MATCHTYPE_INVALID = -1
      val MATCHTYPE_MIN = 0
      val MATCHTYPE_MAX = 1
      var matchType = MATCHTYPE_INVALID
      val invMatchType: Int
         get() {
            hST_INV_EPC_MATCH_CFG
            return matchType
         }
      val MATCHLENGTH_INVALID = 0
      val MATCHLENGTH_MIN = 0
      val MATCHLENGTH_MAX = 496
      var matchLength = MATCHLENGTH_INVALID
      val invMatchLength: Int
         get() {
            hST_INV_EPC_MATCH_CFG
            return matchLength
         }
      val MATCHOFFSET_INVALID = -1
      val MATCHOFFSET_MIN = 0
      val MATCHOFFSET_MAX = 496
      var matchOffset = MATCHOFFSET_INVALID
      val invMatchOffset: Int
         get() {
            hST_INV_EPC_MATCH_CFG
            return matchOffset
         }
      private val hST_INV_EPC_MATCH_CFG: Boolean
         private get() = if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX || matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX || matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX || matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0x11, 9, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_EPC_MATCH_CFG,
               false,
               msgBuffer
            )
         } else {
            false
         }

      private fun setHST_INV_EPC_MATCH_CFG(
         matchEnable: Int,
         matchType: Int,
         matchLength: Int,
         matchOffset: Int
      ): Boolean {
         var matchEnable = matchEnable
         var matchType = matchType
         var matchLength = matchLength
         var matchOffset = matchOffset
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0x11, 9, 0, 0, 0, 0)
         if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX) matchEnable =
            mDefault.matchEnable
         if (matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX) matchType = mDefault.matchType
         if (matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX) matchLength =
            mDefault.matchLength
         if (matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX) matchOffset =
            mDefault.matchOffset
         if (this.matchEnable == matchEnable && this.matchType == matchType && this.matchLength == matchLength && this.matchOffset == matchOffset && sameCheck) return true
         if (matchEnable != 0) {
            msgBuffer[4] = (msgBuffer[4].toInt() or 0x01).toByte()
         }
         if (matchType != 0) {
            msgBuffer[4] = (msgBuffer[4].toInt() or 0x02).toByte()
         }
         msgBuffer[4] = (msgBuffer[4].toInt() or (matchLength % 64 shl 2).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (matchLength / 64).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (matchOffset % 32 shl 3).toByte().toInt()).toByte()
         msgBuffer[6] = (msgBuffer[6].toInt() or (matchOffset / 32).toByte().toInt()).toByte()
         this.matchEnable = matchEnable
         this.matchType = matchType
         this.matchLength = matchLength
         this.matchOffset = matchOffset
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_EPC_MATCH_CFG,
            true,
            msgBuffer
         )
      }

      var invMatchData0_63: ByteArray
      var invMatchDataReady = 0
      val invMatchData: String?
         get() {
            var length = matchLength
            var strValue: String? = ""
            for (i in 0..15) {
               if (length > 0) {
                  if (invMatchDataReady and (0x01 shl i) == 0) {
                     val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0x12, 9, 0, 0, 0, 0)
                     msgBuffer[2] = (msgBuffer[2] + i).toByte()
                     mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                        HostRegRequests.HST_INV_EPCDAT_0_3,
                        false,
                        msgBuffer
                     )
                     strValue = null
                     break
                  } else {
                     for (j in 0..3) {
                        strValue += String.format("%02X", invMatchData0_63[i * 4 + j])
                     }
                  }
                  length -= 32
               }
            }
            return strValue
         }

      fun setInvMatchData(matchData: String): Boolean {
         var length = matchData.length
         for (i in 0..15) {
            if (length > 0) {
               length -= 8
               val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0x12, 9, 0, 0, 0, 0)
               val hexString = "0123456789ABCDEF"
               for (j in 0..7) {
                  if (i * 8 + j + 1 <= matchData.length) {
                     val subString = matchData.substring(i * 8 + j, i * 8 + j + 1).uppercase(
                        Locale.getDefault()
                     )
                     var k = 0
                     k = 0
                     while (k < 16) {
                        if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                           break
                        }
                        k++
                     }
                     if (k == 16) return false
                     if (j / 2 * 2 == j) {
                        msgBuffer[4 + j / 2] = (msgBuffer[4 + j / 2].toInt() or (k shl 4).toByte()
                           .toInt()).toByte()
                     } else {
                        msgBuffer[4 + j / 2] =
                           (msgBuffer[4 + j / 2].toInt() or k.toByte().toInt()).toByte()
                     }
                  }
               }
               msgBuffer[2] = ((msgBuffer[2].toInt() and 0xFF) + i).toByte()
               if (!mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                     HostRegRequests.HST_INV_EPCDAT_0_3,
                     true,
                     msgBuffer
                  )
               ) return false else {
                  invMatchDataReady = invMatchDataReady or (0x01 shl i)
                  System.arraycopy(
                     msgBuffer,
                     4,
                     invMatchData0_63,
                     i * 4,
                     4
                  ) //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
                  //                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
               }
            }
         }
         return true
      }

      //Tag access block parameters
      var accessVerfiy = false
      val ACCRETRY_INVALID = -1
      val ACCRETRY_MIN = 0
      val ACCRETRY_MAX = 7
      var accessRetry = ACCRETRY_INVALID
       get(): Int {
         if (field < ACCRETRY_MIN || field > ACCRETRY_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 0x0A.toByte(), 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGACC_DESC_CFG,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAccessRetry(accessVerfiy: Boolean, accessRetry: Int): Boolean {
         var accessRetry = accessRetry
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 0x0A, 0, 0, 0, 0)
         if (accessRetry < ACCRETRY_MIN || accessRetry > ACCRETRY_MAX) accessRetry =
            mDefault.accessRetry
         if (this.accessVerfiy == accessVerfiy && this.accessRetry == accessRetry && sameCheck) return true
         msgBuffer[4] = (msgBuffer[4].toInt() or (accessRetry shl 1).toByte().toInt()).toByte()
         if (accessVerfiy) msgBuffer[4] = (msgBuffer[4].toInt() or 0x01).toByte()
         this.accessVerfiy = accessVerfiy
         this.accessRetry = accessRetry
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_DESC_CFG,
            true,
            msgBuffer
         )
      }

      val ACCBANK_INVALID = -1
      val ACCBANK_MIN = 0
      val ACCBANK_MAX = 3
      var accessBank2 = ACCBANK_INVALID

      var accessBank = ACCBANK_INVALID
         get() : Int {
         if (field < ACCBANK_MIN || field > ACCBANK_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 2, 0x0A.toByte(), 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGACC_BANK,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAccessBank(accessBank: Int): Boolean {
         var accessBank = accessBank
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 0x0A, 0, 0, 0, 0)
         if (accessBank < ACCBANK_MIN || accessBank > ACCBANK_MAX) accessBank = mDefault.accessBank
         if (this.accessBank == accessBank && accessBank2 == 0 && sameCheck) return true
         msgBuffer[4] = (accessBank and 0x03).toByte()
         this.accessBank = accessBank
         accessBank2 = 0
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_BANK,
            true,
            msgBuffer
         )
      }

      fun setAccessBank(accessBank: Int, accessBank2: Int): Boolean {
         var accessBank = accessBank
         var accessBank2 = accessBank2
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 0x0A, 0, 0, 0, 0)
         if (DEBUG) appendToLog("setAccessBank($accessBank, $accessBank2) with tagRead = $tagRead")
         if (tagRead != 2) accessBank2 = 0
         if (accessBank < ACCBANK_MIN || accessBank > ACCBANK_MAX) accessBank = mDefault.accessBank
         if (accessBank2 < ACCBANK_MIN || accessBank2 > ACCBANK_MAX) accessBank2 =
            mDefault.accessBank2
         if (this.accessBank == accessBank && this.accessBank2 == accessBank2 && sameCheck) return true
         msgBuffer[4] = (accessBank and 0x03).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (accessBank2 and 0x03 shl 2).toByte().toInt()).toByte()
         this.accessBank = accessBank
         this.accessBank2 = accessBank2
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_BANK,
            true,
            msgBuffer
         )
      }

      val ACCOFFSET_INVALID = -1
      val ACCOFFSET_MIN = 0
      val ACCOFFSET_MAX = 0xFFFF
      var accessOffset2 = ACCOFFSET_INVALID
      var accessOffset = ACCOFFSET_INVALID
       get(): Int {
         if (field < ACCOFFSET_MIN || field > ACCOFFSET_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 3, 0x0A.toByte(), 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGACC_PTR,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAccessOffset(accessOffset: Int): Boolean {
         var accessOffset = accessOffset
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 0x0A, 0, 0, 0, 0)
         if (accessOffset < ACCOFFSET_MIN || accessOffset > ACCOFFSET_MAX) accessOffset =
            mDefault.accessOffset
         if (this.accessOffset == accessOffset && accessOffset2 == 0 && sameCheck) return true
         msgBuffer[4] = (accessOffset and 0xFF).toByte()
         msgBuffer[5] = (accessOffset shr 8 and 0xFF).toByte()
         msgBuffer[6] = (accessOffset shr 16 and 0xFF).toByte()
         msgBuffer[7] = (accessOffset shr 24 and 0xFF).toByte()
         this.accessOffset = accessOffset
         accessOffset2 = 0
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_PTR,
            true,
            msgBuffer
         )
      }

      fun setAccessOffset(accessOffset: Int, accessOffset2: Int): Boolean {
         var accessOffset = accessOffset
         var accessOffset2 = accessOffset2
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 0x0A, 0, 0, 0, 0)
         if (tagRead != 2) accessOffset2 = 0
         if (accessOffset < ACCOFFSET_MIN || accessOffset > ACCOFFSET_MAX) accessOffset =
            mDefault.accessOffset
         if (accessOffset2 < ACCOFFSET_MIN || accessOffset2 > ACCOFFSET_MAX) accessOffset2 =
            mDefault.accessOffset2
         if (this.accessOffset == accessOffset && this.accessOffset2 == accessOffset2 && sameCheck) return true
         msgBuffer[4] = (accessOffset and 0xFF).toByte()
         msgBuffer[5] = (accessOffset shr 8 and 0xFF).toByte()
         msgBuffer[6] = (accessOffset2 and 0xFF).toByte()
         msgBuffer[7] = (accessOffset2 shr 8 and 0xFF).toByte()
         this.accessOffset = accessOffset
         this.accessOffset2 = accessOffset2
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_PTR,
            true,
            msgBuffer
         )
      }

      val ACCCOUNT_INVALID = -1
      val ACCCOUNT_MIN = 0
      val ACCCOUNT_MAX = 255
      var accessCount2 = ACCCOUNT_INVALID
      var accessCount = ACCCOUNT_INVALID
       get(): Int {
         if (field < ACCCOUNT_MIN || field > ACCCOUNT_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 4, 0x0A.toByte(), 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGACC_CNT,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAccessCount(accessCount: Int): Boolean {
         var accessCount = accessCount
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 4, 0x0A, 0, 0, 0, 0)
         if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX) accessCount =
            mDefault.accessCount
         if (this.accessCount == accessCount && accessCount2 == 0 && sameCheck) return true
         msgBuffer[4] = (accessCount and 0xFF).toByte()
         this.accessCount = accessCount
         accessCount2 = 0
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_CNT,
            true,
            msgBuffer
         )
      }

      fun setAccessCount(accessCount: Int, accessCount2: Int): Boolean {
         var accessCount = accessCount
         var accessCount2 = accessCount2
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 4, 0x0A, 0, 0, 0, 0)
         if (tagRead != 2) accessCount2 = 0
         if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX) accessCount =
            mDefault.accessCount
         if (accessCount2 < ACCCOUNT_MIN || accessCount2 > ACCCOUNT_MAX) accessCount2 =
            mDefault.accessCount2
         if (this.accessCount == accessCount && this.accessCount2 == accessCount2 && sameCheck) return true
         msgBuffer[4] = (accessCount and 0xFF).toByte()
         msgBuffer[5] = (accessCount2 and 0xFF).toByte()
         this.accessCount = accessCount
         this.accessCount2 = accessCount2
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_CNT,
            true,
            msgBuffer
         )
      }

      val ACCLOCKACTION_INVALID = -1
      val ACCLOCKACTION_MIN = 0
      val ACCLOCKACTION_MAX = 0x3FF
      var accessLockAction = ACCLOCKACTION_INVALID
       get(): Int {
         if (field < ACCLOCKACTION_MIN || field > ACCLOCKACTION_MAX) hST_TAGACC_LOCKCFG
         return field
      }

      fun setAccessLockAction(accessLockAction: Int): Boolean {
         return setAccessLockAction(accessLockAction, accessLockMask)
      }

      val ACCLOCKMASK_INVALID = -1
      val ACCLOCKMASK_MIN = 0
      val ACCLOCKMASK_MAX = 0x3FF
      var accessLockMask = ACCLOCKMASK_INVALID
       get(): Int {
         if (field < ACCLOCKMASK_MIN || field > ACCLOCKMASK_MAX) hST_TAGACC_LOCKCFG
         return field
      }

      fun setAccessLockMask(accessLockMask: Int): Boolean {
         return setAccessLockAction(accessLockAction, accessLockMask)
      }

      val hST_TAGACC_LOCKCFG: Boolean
         get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 5, 0x0A.toByte(), 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGACC_LOCKCFG,
               false,
               msgBuffer
            )
         }

      fun setAccessLockAction(accessLockAction: Int, accessLockMask: Int): Boolean {
         var accessLockAction = accessLockAction
         var accessLockMask = accessLockMask
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 5, 0x0A, 0, 0, 0, 0)
         if (accessLockAction < ACCLOCKACTION_MIN || accessLockAction > ACCLOCKACTION_MAX) accessLockAction =
            mDefault.accessLockAction
         if (accessLockMask < ACCLOCKMASK_MIN || accessLockMask > ACCLOCKMASK_MAX) accessLockMask =
            mDefault.accessLockMask
         if (this.accessLockAction == accessLockAction && this.accessLockMask == accessLockMask && sameCheck) return true
         msgBuffer[4] = (accessLockAction and 0xFF).toByte()
         msgBuffer[5] =
            (msgBuffer[5].toInt() or (accessLockAction and 0x3FF shr 8).toByte().toInt()).toByte()
         msgBuffer[5] =
            (msgBuffer[5].toInt() or (accessLockMask and 0x3F shl 2).toByte().toInt()).toByte()
         msgBuffer[6] =
            (msgBuffer[6].toInt() or (accessLockMask and 0x3FF shr 6).toByte().toInt()).toByte()
         this.accessLockAction = accessLockAction
         this.accessLockMask = accessLockMask
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_LOCKCFG,
            true,
            msgBuffer
         )
      }

      val ACCPWD_INVALID = 0
      val ACCPWD_MIN: Long = 0
      val ACCPWD_MAX: Long = -0x1
      fun setRx000AccessPassword(password: String?): Boolean {
         var password = password
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 6, 0x0A.toByte(), 0, 0, 0, 0)
         if (password == null) password = ""
         val hexString = "0123456789ABCDEF"
         for (j in 0..15) {
            if (j + 1 <= password.length) {
               val subString = password.substring(j, j + 1).uppercase(Locale.getDefault())
               var k = 0
               k = 0
               while (k < 16) {
                  if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                     break
                  }
                  k++
               }
               if (k == 16) return false
               if (j / 2 * 2 == j) {
                  msgBuffer[7 - j / 2] =
                     (msgBuffer[7 - j / 2].toInt() or (k shl 4).toByte().toInt()).toByte()
               } else {
                  msgBuffer[7 - j / 2] =
                     (msgBuffer[7 - j / 2].toInt() or k.toByte().toInt()).toByte()
               }
            }
         }
         val retValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_ACCPWD,
            true,
            msgBuffer
         )
         if (DEBUG) appendToLog("sendHostRegRequest(): retValue = $retValue")
         return retValue
      }

      fun setRx000KillPassword(password: String): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 7, 0x0A.toByte(), 0, 0, 0, 0)
         val hexString = "0123456789ABCDEF"
         for (j in 0..15) {
            if (j + 1 <= password.length) {
               val subString = password.substring(j, j + 1).uppercase(Locale.getDefault())
               var k = 0
               k = 0
               while (k < 16) {
                  if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                     break
                  }
                  k++
               }
               if (k == 16) return false
               if (j / 2 * 2 == j) {
                  msgBuffer[7 - j / 2] =
                     (msgBuffer[7 - j / 2].toInt() or (k shl 4).toByte().toInt()).toByte()
               } else {
                  msgBuffer[7 - j / 2] =
                     (msgBuffer[7 - j / 2].toInt() or k.toByte().toInt()).toByte()
               }
            }
         }
         val retValue = mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGACC_KILLPWD,
            true,
            msgBuffer
         )
         if (DEBUG) appendToLog("sendHostRegRequest(): retValue = $retValue")
         return retValue
      }

      val ACCWRITEDATSEL_INVALID = -1
      val ACCWRITEDATSEL_MIN = 0
      val ACCWRITEDATSEL_MAX = 7

      var accessWriteDataSelect = ACCWRITEDATSEL_INVALID
       get(): Int {
         if (field < ACCWRITEDATSEL_MIN || field > ACCWRITEDATSEL_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 8, 0x0A.toByte(), 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGWRDAT_SEL,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAccessWriteDataSelect(accessWriteDataSelect: Int): Boolean {
         var accessWriteDataSelect = accessWriteDataSelect
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 8, 0x0A, 0, 0, 0, 0)
         if (accessWriteDataSelect < ACCWRITEDATSEL_MIN || accessWriteDataSelect > ACCWRITEDATSEL_MAX) accessWriteDataSelect =
            mDefault.accessWriteDataSelect
         if (this.accessWriteDataSelect == accessWriteDataSelect && sameCheck) return true
         accWriteDataReady = 0
         msgBuffer[4] = (accessWriteDataSelect and 0x07).toByte()
         this.accessWriteDataSelect = accessWriteDataSelect
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGWRDAT_SEL,
            true,
            msgBuffer
         )
      }

      var accWriteData0_63: ByteArray
      var accWriteDataReady = 0
      val accessWriteData: String?
         get() {
            var length = accessCount
            if (length > 32) {
               length = 32
            }
            var strValue: String? = ""
            for (i in 0..31) {
               if (length > 0) {
                  if (accWriteDataReady and (0x01 shl i) == 0) {
                     val msgBuffer = byteArrayOf(0x70.toByte(), 0, 9, 0x0A.toByte(), 0, 0, 0, 0)
                     msgBuffer[2] = (msgBuffer[2] + i).toByte()
                     mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                        HostRegRequests.HST_TAGWRDAT_0,
                        false,
                        msgBuffer
                     )
                     strValue = null
                     break
                  } else {
                     for (j in 0..3) {
                        strValue += String.format("%02X", accWriteData0_63[i * 4 + j])
                     }
                  }
                  length -= 2
               }
            }
            return strValue
         }

      fun setAccessWriteData(dataInput: String): Boolean {
         var dataInput = dataInput
         dataInput = dataInput.trim { it <= ' ' }
         val writeBufLength = 16 * 2 //16
         val wrieByteSize = 4 //8
         var length = dataInput.length
         appendToLog("length = $length")
         if (length > wrieByteSize * writeBufLength) {
            appendToLog("1")
            return false
         }
         for (i in 0 until writeBufLength) {
            if (length > 0) {
               length -= wrieByteSize
               if (i / 16 * 16 == i) {
                  val msgBuffer = byteArrayOf(0x70.toByte(), 1, 8, 0x0A.toByte(), 0, 0, 0, 0)
                  msgBuffer[4] = (i / 16).toByte()
                  if (!mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                        HostRegRequests.HST_TAGWRDAT_SEL,
                        true,
                        msgBuffer
                     )
                  ) {
                     appendToLog("23")
                     return false
                  }
               }
               val msgBuffer = byteArrayOf(0x70.toByte(), 1, 9, 0x0A.toByte(), 0, 0, 0, 0)
               val hexString = "0123456789ABCDEF"
               for (j in 0 until wrieByteSize) {
//                        if (i * wrieByteSize + j + 1 <= dataInput.length()) {
                  appendToLog("dataInput = $dataInput, i = $i, wrieByteSize = $wrieByteSize, j = $j")
                  if (i * wrieByteSize + j >= dataInput.length) break
                  val subString =
                     dataInput.substring(i * wrieByteSize + j, i * wrieByteSize + j + 1).uppercase(
                        Locale.getDefault()
                     )
                  appendToLog("subString = $subString")
                  if (DEBUG) appendToLog(subString)
                  var k = 0
                  k = 0
                  while (k < 16) {
                     if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                        break
                     }
                     k++
                  }
                  if (k == 16) {
                     appendToLog("2: i= $i, j=$j, subString = $subString")
                     return false
                  }
                  if (j / 2 * 2 == j) {
                     msgBuffer[5 - j / 2] = (msgBuffer[5 - j / 2].toInt() or (k shl 4).toByte()
                        .toInt()).toByte()
                  } else {
                     msgBuffer[5 - j / 2] =
                        (msgBuffer[5 - j / 2].toInt() or k.toByte().toInt()).toByte()
                  }
                  //                        }
               }
               appendToLog("complete 4 bytes: " + byteArrayToString(msgBuffer))
               msgBuffer[2] = ((msgBuffer[2].toInt() and 0xFF) + i % 16).toByte()
               if (wrieByteSize == 4) {
                  msgBuffer[6] = i.toByte()
               }
               if (!mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                     HostRegRequests.HST_TAGWRDAT_0,
                     true,
                     msgBuffer
                  )
               ) {
                  appendToLog("3")
                  return false
               } else {
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.accWriteDataReady =
                     mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.accWriteDataReady or (0x01 shl i)
                  if (DEBUG) appendToLog("accWriteReady=$accWriteDataReady")
                  for (k in 0..3) {
                     accWriteData0_63[i * 4 + k] = msgBuffer[7 - k]
                  }
                  if (DEBUG) appendToLog("Data=" + byteArrayToString(accWriteData0_63))
               }
            } else break
         }
         return true
      }

      //RFTC block paramters
      val PROFILE_INVALID = -1
      val PROFILE_MIN = 0
      val PROFILE_MAX = 5 //profile 4 and 5 are custom profiles.
      var currentProfile = PROFILE_INVALID
       get(): Int {
         return if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
            ANTSELECT_INVALID
         } else {
            if (field < PROFILE_MIN || field > PROFILE_MAX) {
               val msgBuffer = byteArrayOf(0x70.toByte(), 0, 0x60, 0x0B, 0, 0, 0, 0)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_RFTC_CURRENT_PROFILE,
                  false,
                  msgBuffer
               )
            }
            field
         }
      }

      fun setCurrentProfile(currentProfile: Int): Boolean {
         var currentProfile = currentProfile
         return if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) false else {
            val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0x60, 0x0B, 0, 0, 0, 0)
            if (currentProfile < PROFILE_MIN || currentProfile > PROFILE_MAX) currentProfile =
               mDefault.currentProfile
            if (this.currentProfile == currentProfile && sameCheck) return true
            msgBuffer[4] = currentProfile.toByte()
            this.currentProfile = currentProfile
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_RFTC_CURRENT_PROFILE,
               true,
               msgBuffer
            )
         }
      }

      val COUNTRYENUM_INVALID = -1
      val COUNTRYENUM_MIN = 1
      val COUNTRYENUM_MAX = 109
      val COUNTRYCODE_INVALID = -1
      val COUNTRYCODE_MIN = 1
      val COUNTRYCODE_MAX = 9
      var countryEnumOem = COUNTRYENUM_INVALID
      var countryEnum = COUNTRYENUM_INVALID
      var countryCode = COUNTRYCODE_INVALID // OemAddress = 0x02
      var modelCode: String? = null
      val FREQCHANSEL_INVALID = -1
      val FREQCHANSEL_MIN = 0
      val FREQCHANSEL_MAX = 49
      var freqChannelSelect = FREQCHANSEL_INVALID
       get(): Int {
         appendToLog("freqChannelSelect = $field")
         if (field < FREQCHANSEL_MIN || field > FREQCHANSEL_MAX) {
            run {
               val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 0x0C, 0, 0, 0, 0)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_RFTC_FRQCH_SEL,
                  false,
                  msgBuffer
               )
            }
         }
         return field
      }

      fun setFreqChannelSelect(freqChannelSelect: Int): Boolean {
         var freqChannelSelect = freqChannelSelect
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 0x0C, 0, 0, 0, 0)
         if (freqChannelSelect < FREQCHANSEL_MIN || freqChannelSelect > FREQCHANSEL_MAX) freqChannelSelect =
            mDefault.freqChannelSelect
         //if (this.freqChannelSelect == freqChannelSelect && sameCheck)  return true;
         appendToLog("freqChannelSelect = $freqChannelSelect")
         msgBuffer[4] = freqChannelSelect.toByte()
         this.freqChannelSelect = freqChannelSelect
         freqChannelSelect = FREQCHANCONFIG_INVALID
         freqPllMultiplier = FREQPLLMULTIPLIER_INVALID
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_RFTC_FRQCH_SEL,
            true,
            msgBuffer
         )
      }

      val FREQCHANCONFIG_INVALID = -1
      val FREQCHANCONFIG_MIN = 0
      val FREQCHANCONFIG_MAX = 1
      var freqChannelConfig = FREQCHANCONFIG_INVALID
       get(): Int {
         if (field < FREQCHANCONFIG_MIN || field > FREQCHANCONFIG_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 2, 0x0C, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_RFTC_FRQCH_CFG,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setFreqChannelConfig(on: Boolean): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 0x0C, 0, 0, 0, 0)
         val onCurrent = freqChannelConfig != 0
         //            if (onCurrent == on && sameCheck)  return true;
         if (on) {
            msgBuffer[4] = 1
            freqChannelConfig = 1
         } else {
            freqChannelConfig = 0
         }
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_RFTC_FRQCH_CFG,
            true,
            msgBuffer
         )
      }

      val FREQPLLMULTIPLIER_INVALID = -1
      var freqPllMultiplier = FREQPLLMULTIPLIER_INVALID
       get(): Int {
         if (field == FREQPLLMULTIPLIER_INVALID) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 3, 0x0C, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setFreqPllMultiplier(freqPllMultiplier: Int): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 0x0C, 0, 0, 0, 0)
         msgBuffer[4] = (freqPllMultiplier and 0xFF).toByte()
         msgBuffer[5] = (freqPllMultiplier shr 8 and 0xFF).toByte()
         msgBuffer[6] = (freqPllMultiplier shr 16 and 0xFF).toByte()
         msgBuffer[7] = (freqPllMultiplier shr 24 and 0xFF).toByte()
         this.freqPllMultiplier = freqPllMultiplier
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT,
            true,
            msgBuffer
         )
      }

      val FREQPLLDAC_INVALID = -1

      init {
         if (set_default_setting) {
            macVer = mDefault.macVer
            //diagnosticCfg = mDefault.diagnosticCfg;
            oemAddress = mDefault.oemAddress.toLong()

            //RFTC block paramters
            currentProfile = mDefault.currentProfile

            // Antenna block parameters
            antennaCycle = mDefault.antennaCycle
            antennaFreqAgile = mDefault.antennaFreqAgile
            antennaSelect = mDefault.antennaSelect
         }
         antennaSelectedData = arrayOfNulls(ANTSELECT_MAX + 1)
         for (i in antennaSelectedData.indices) {
            var default_setting_type = 0
            if (set_default_setting) {
               default_setting_type =
                  if (i == 0) 1 else if (i >= 1 && i <= 3) 2 else if (i >= 4 && i <= 7) 3 else if (i >= 8 && i <= 11) 4 else 5
            }
            antennaSelectedData[i] = AntennaSelectedData(set_default_setting, default_setting_type)
         }

         //Tag select block parameters
         if (set_default_setting) invSelectIndex = 0
         invSelectData = arrayOfNulls(INVSELECT_MAX + 1)
         for (i in invSelectData.indices) {
            invSelectData[i] = InvSelectData(set_default_setting)
         }
         if (set_default_setting) {
            //Inventtory block paraameters
            queryTarget = mDefault.queryTarget
            querySession = mDefault.querySession
            querySelect = mDefault.querySelect
            invAlgo = mDefault.invAlgo
            matchRep = mDefault.matchRep
            tagSelect = mDefault.tagSelect
            noInventory = mDefault.noInventory
            tagDelay = mDefault.tagDelay
            invModeCompact = mDefault.tagJoin
            invBrandId = mDefault.brandid
         }
         if (set_default_setting) algoSelect = 3
         algoSelectedData = arrayOfNulls(ALGOSELECT_MAX + 1)
         for (i in algoSelectedData.indices) { //0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
            var default_setting_type = 0
            if (set_default_setting) {
               default_setting_type = i + 1
            }
            algoSelectedData[i] = AlgoSelectedData(set_default_setting, default_setting_type)
         }
         if (set_default_setting) {
            rssiFilterType = mDefault.rssiFilterType
            rssiFilterOption = mDefault.rssiFilterOption
            rssiFilterThreshold1 = mDefault.rssiFilterThreshold
            rssiFilterThreshold2 = mDefault.rssiFilterThreshold
            rssiFilterCount = mDefault.rssiFilterCount
            matchEnable = mDefault.matchEnable
            matchType = mDefault.matchType
            matchLength = mDefault.matchLength
            matchOffset = mDefault.matchOffset
            invMatchDataReady = mDefault.invMatchDataReady

            //Tag access block parameters
            accessRetry = mDefault.accessRetry
            accessBank = mDefault.accessBank
            accessBank2 = mDefault.accessBank2
            accessOffset = mDefault.accessOffset
            accessOffset2 = mDefault.accessOffset2
            accessCount = mDefault.accessCount
            accessCount2 = mDefault.accessCount2
            accessLockAction = mDefault.accessLockAction
            accessLockMask = mDefault.accessLockMask
            //long accessPassword = 0;
            //long killPassword = 0;
            accessWriteDataSelect = mDefault.accessWriteDataSelect
            accWriteDataReady = mDefault.accWriteDataReady
            authMatchDataReady = mDefault.authMatchDataReady
         }
         invMatchData0_63 = ByteArray(4 * 16)
         accWriteData0_63 = ByteArray(4 * 16 * 2)
         authMatchData0_63 = ByteArray(4 * 4)
      }

      var freqPllDac = FREQPLLDAC_INVALID
       get(): Int {
         if (field == FREQPLLDAC_INVALID) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 4, 0x0C, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDACCTL,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setFreqChannelOverride(freqStart: Int): Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 8, 0x0C, 0, 0, 0, 0)
         msgBuffer[4] = (freqStart and 0xFF).toByte()
         msgBuffer[5] = (freqStart shr 8 and 0xFF).toByte()
         msgBuffer[6] = (freqStart shr 16 and 0xFF).toByte()
         msgBuffer[7] = (freqStart shr 24 and 0xFF).toByte()
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_RFTC_FRQCH_CMDSTART,
            true,
            msgBuffer
         )
      }
   }

   val ANTINVCOUNT_INVALID: Long = -1
   val ANTINVCOUNT_MIN: Long = 0
   val ANTINVCOUNT_MAX = 0xFFFFFFFFL
   inner class AntennaSelectedData(
      set_default_setting: Boolean,
      default_setting_type: Int
   ) {
      inner class AntennaSelectedData_default(set_default_setting: Int) {
         var antennaEnable: Int
         var antennaInventoryMode: Int
         var antennaLocalAlgo: Int
         var antennaLocalStartQ: Int
         var antennaProfileMode: Int
         var antennaLocalProfile: Int
         var antennaFrequencyMode: Int
         var antennaLocalFrequency: Int
         var antennaStatus: Int
         var antennaDefine: Int
         var antennaDwell: Long
         var antennaPower: Long
         var antennaInvCount: Long

         init {
            antennaEnable = mDefaultArray.antennaEnable[set_default_setting]
            antennaInventoryMode = mDefaultArray.antennaInventoryMode[set_default_setting]
            antennaLocalAlgo = mDefaultArray.antennaLocalAlgo[set_default_setting]
            antennaLocalStartQ = mDefaultArray.antennaLocalStartQ[set_default_setting]
            antennaProfileMode = mDefaultArray.antennaProfileMode[set_default_setting]
            antennaLocalProfile = mDefaultArray.antennaLocalProfile[set_default_setting]
            antennaFrequencyMode = mDefaultArray.antennaFrequencyMode[set_default_setting]
            antennaLocalFrequency = mDefaultArray.antennaLocalFrequency[set_default_setting]
            antennaStatus = mDefaultArray.antennaStatus[set_default_setting]
            antennaDefine = mDefaultArray.antennaDefine[set_default_setting]
            antennaDwell = mDefaultArray.antennaDwell[set_default_setting]
            antennaPower = mDefaultArray.antennaPower[set_default_setting]
            antennaInvCount = mDefaultArray.antennaInvCount[set_default_setting]
         }
      }
      var antennaInvCount: Long = ANTINVCOUNT_INVALID
         get(){
            if (field < ANTINVCOUNT_MIN || field > ANTINVCOUNT_MAX) {
               val msgBuffer = byteArrayOf(0x70.toByte(), 0, 7, 7, 0, 0, 0, 0)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                  HostRegRequests.HST_ANT_DESC_INV_CNT,
                  false,
                  msgBuffer
               )
            }
            return field
         }
      var mDefault: AntennaSelectedData_default

      inner class AntennaSelectedData_defaultArray {
         //0 for invalid default,    1  for 0,       2 for 1 to 3,       3 for 4 to 7,       4 for 8 to   11,        5 for 12 to 15
         var antennaEnable = intArrayOf(-1, 1, 0, 0, 0, 0)
         var antennaInventoryMode = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaLocalAlgo = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaLocalStartQ = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaProfileMode = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaLocalProfile = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaFrequencyMode = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaLocalFrequency = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaStatus = intArrayOf(-1, 0, 0, 0, 0, 0)
         var antennaDefine = intArrayOf(-1, 0, 0, 1, 2, 3)
         var antennaDwell = longArrayOf(-1, 2000, 2000, 2000, 2000, 2000)
         var antennaPower = longArrayOf(-1, 300, 0, 0, 0, 0)
         var antennaInvCount = longArrayOf(-1, 8192, 8192, 8192, 8192, 8192)
      }

      var mDefaultArray = AntennaSelectedData_defaultArray()
      val ANTENABLE_INVALID = -1
      val ANTENABLE_MIN = 0
      val ANTENABLE_MAX = 1
      var antennaEnable = ANTENABLE_INVALID
       get(): Int {
         if (field < ANTENABLE_MIN || field > ANTENABLE_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaEnable(antennaEnable: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTINVMODE_INVALID = 0
      val ANTINVMODE_MIN = 0
      val ANTINVMODE_MAX = 1
      var antennaInventoryMode = ANTINVMODE_INVALID
       get(): Int {
         if (field < ANTPROFILEMODE_MIN || field > ANTPROFILEMODE_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaInventoryMode(antennaInventoryMode: Int): Boolean {
         return setAntennaEnable(
            antennaEnable,
            antennaInventoryMode,
            antennaLocalAlgo,
            antennaLocalStartQ,
            antennaProfileMode,
            antennaLocalProfile,
            antennaFrequencyMode,
            antennaLocalFrequency
         )
      }

      val ANTLOCALALGO_INVALID = 0
      val ANTLOCALALGO_MIN = 0
      val ANTLOCALALGO_MAX = 5
      var antennaLocalAlgo = ANTLOCALALGO_INVALID
       get(): Int {
         if (field < ANTLOCALALGO_MIN || field > ANTLOCALALGO_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaLocalAlgo(antennaLocalAlgo: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTLOCALSTARTQ_INVALID = 0
      val ANTLOCALSTARTQ_MIN = 0
      val ANTLOCALSTARTQ_MAX = 15
      var antennaLocalStartQ = ANTLOCALSTARTQ_INVALID
       get(): Int {
         if (field < ANTLOCALSTARTQ_MIN || field > ANTLOCALSTARTQ_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaLocalStartQ(antennaLocalStartQ: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTPROFILEMODE_INVALID = 0
      val ANTPROFILEMODE_MIN = 0
      val ANTPROFILEMODE_MAX = 1
      var antennaProfileMode = ANTPROFILEMODE_INVALID
       get(): Int {
         if (field < ANTPROFILEMODE_MIN || field > ANTPROFILEMODE_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaProfileMode(antennaProfileMode: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTLOCALPROFILE_INVALID = 0
      val ANTLOCALPROFILE_MIN = 0
      val ANTLOCALPROFILE_MAX = 5
      var antennaLocalProfile = ANTLOCALPROFILE_INVALID
       get(): Int {
         if (field < ANTLOCALPROFILE_MIN || field > ANTLOCALPROFILE_MIN) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaLocalProfile(antennaLocalProfile: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTFREQMODE_INVALID = 0
      val ANTFREQMODE_MIN = 0
      val ANTFREQMODE_MAX = 1
      var antennaFrequencyMode = ANTFREQMODE_INVALID
       get(): Int {
         if (field < ANTFREQMODE_MIN || field > ANTFREQMODE_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaFrequencyMode(antennaFrequencyMode: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      val ANTLOCALFREQ_INVALID = 0
      val ANTLOCALFREQ_MIN = 0
      val ANTLOCALFREQ_MAX = 49
      var antennaLocalFrequency = ANTLOCALFREQ_INVALID
       get(): Int {
         if (field < ANTLOCALFREQ_MIN || field > ANTLOCALFREQ_MAX) hST_ANT_DESC_CFG
         return field
      }

      fun setAntennaLocalFrequency(antennaLocalFrequency: Int): Boolean {
         return setAntennaEnable(
            antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
            antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency
         )
      }

      private val hST_ANT_DESC_CFG: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 2, 7, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_ANT_DESC_CFG,
               false,
               msgBuffer
            )
         }

      fun setAntennaEnable(
         antennaEnable: Int,
         antennaInventoryMode: Int,
         antennaLocalAlgo: Int,
         antennaLocalStartQ: Int,
         antennaProfileMode: Int,
         antennaLocalProfile: Int,
         antennaFrequencyMode: Int,
         antennaLocalFrequency: Int
      ): Boolean {
         var antennaEnable = antennaEnable
         var antennaInventoryMode = antennaInventoryMode
         var antennaLocalAlgo = antennaLocalAlgo
         var antennaLocalStartQ = antennaLocalStartQ
         var antennaProfileMode = antennaProfileMode
         var antennaLocalProfile = antennaLocalProfile
         var antennaFrequencyMode = antennaFrequencyMode
         var antennaLocalFrequency = antennaLocalFrequency
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 7, 0, 0, 0, 0)
         if (antennaEnable < ANTENABLE_MIN || antennaEnable > ANTENABLE_MAX) antennaEnable =
            mDefault.antennaEnable
         if (antennaInventoryMode < ANTINVMODE_MIN || antennaInventoryMode > ANTINVMODE_MAX) antennaInventoryMode =
            mDefault.antennaInventoryMode
         if (antennaLocalAlgo < ANTLOCALALGO_MIN || antennaLocalAlgo > ANTLOCALALGO_MAX) antennaLocalAlgo =
            mDefault.antennaLocalAlgo
         if (antennaLocalStartQ < ANTLOCALSTARTQ_MIN || antennaLocalStartQ > ANTLOCALSTARTQ_MAX) antennaLocalStartQ =
            mDefault.antennaLocalStartQ
         if (antennaProfileMode < ANTPROFILEMODE_MIN || antennaProfileMode > ANTPROFILEMODE_MAX) antennaProfileMode =
            mDefault.antennaProfileMode
         if (antennaLocalProfile < ANTLOCALPROFILE_MIN || antennaLocalProfile > ANTLOCALPROFILE_MAX) antennaLocalProfile =
            mDefault.antennaLocalProfile
         if (antennaFrequencyMode < ANTFREQMODE_MIN || antennaFrequencyMode > ANTFREQMODE_MAX) antennaFrequencyMode =
            mDefault.antennaFrequencyMode
         if (antennaLocalFrequency < ANTLOCALFREQ_MIN || antennaLocalFrequency > ANTLOCALFREQ_MAX) antennaLocalFrequency =
            mDefault.antennaLocalFrequency
         if (this.antennaEnable == antennaEnable && this.antennaInventoryMode == antennaInventoryMode && this.antennaLocalAlgo == antennaLocalAlgo && this.antennaLocalStartQ == antennaLocalStartQ && this.antennaProfileMode == antennaProfileMode && this.antennaLocalProfile == antennaLocalProfile && this.antennaFrequencyMode == antennaFrequencyMode && this.antennaLocalFrequency == antennaLocalFrequency && sameCheck) return true
         msgBuffer[4] = (msgBuffer[4].toInt() or antennaEnable).toByte()
         msgBuffer[4] = (msgBuffer[4].toInt() or (antennaInventoryMode shl 1)).toByte()
         msgBuffer[4] = (msgBuffer[4].toInt() or (antennaLocalAlgo shl 2)).toByte()
         msgBuffer[4] = (msgBuffer[4].toInt() or (antennaLocalStartQ shl 4)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or antennaProfileMode).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (antennaLocalProfile shl 1)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (antennaFrequencyMode shl 5)).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (antennaLocalFrequency and 0x03 shl 6)).toByte()
         msgBuffer[6] = (msgBuffer[6].toInt() or (antennaLocalFrequency shr 2)).toByte()
         this.antennaEnable = antennaEnable
         this.antennaInventoryMode = antennaInventoryMode
         this.antennaLocalAlgo = antennaLocalAlgo
         this.antennaLocalStartQ = antennaLocalStartQ
         this.antennaProfileMode = antennaProfileMode
         this.antennaLocalProfile = antennaLocalProfile
         this.antennaFrequencyMode = antennaFrequencyMode
         this.antennaLocalFrequency = antennaLocalFrequency
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_ANT_DESC_CFG,
            true,
            msgBuffer
         )
      }

      val ANTSTATUS_INVALID = -1
      val ANTSTATUS_MIN = 0
      val ANTSTATUS_MAX = 0xFFFFF
      var antennaStatus = ANTSTATUS_INVALID
       get(): Int {
         if (field < ANTSTATUS_MIN || field > ANTSTATUS_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 3, 7, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.MAC_ANT_DESC_STAT,
               false,
               msgBuffer
            )
         }
         return field
      }

      val ANTDEFINE_INVALID = -1
      val ANTDEFINE_MIN = 0
      val ANTDEFINE_MAX = 3
      var antennaDefine = ANTDEFINE_INVALID
       get(): Int {
         if (field < ANTDEFINE_MIN || field > ANTDEFINE_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 4, 7, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_ANT_DESC_PORTDEF,
               false,
               msgBuffer
            )
         }
         return field
      }

      val ANTDWELL_INVALID: Long = -1
      val ANTDWELL_MIN: Long = 0
      val ANTDWELL_MAX: Long = 0xFFFF
      var antennaDwell = ANTDWELL_INVALID
       get(): Long {
         if (field < ANTDWELL_MIN || field > ANTDWELL_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 5, 7, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_ANT_DESC_DWELL,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAntennaDwell(antennaDwell: Long): Boolean {
         var antennaDwell = antennaDwell
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 5, 7, 0, 0, 0, 0)
         if (antennaDwell < ANTDWELL_MIN || antennaDwell > ANTDWELL_MAX) antennaDwell =
            mDefault.antennaDwell
         if (this.antennaDwell == antennaDwell && sameCheck) return true
         msgBuffer[4] = (antennaDwell % 256).toByte()
         msgBuffer[5] = ((antennaDwell shr 8) % 256).toByte()
         msgBuffer[6] = ((antennaDwell shr 16) % 256).toByte()
         msgBuffer[7] = ((antennaDwell shr 24) % 256).toByte()
         this.antennaDwell = antennaDwell
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_ANT_DESC_DWELL,
            true,
            msgBuffer
         )
      }

      val ANTARGET_INVALID = -1
      val ANTARGET_MIN = 0
      val ANTARGET_MAX = 1
      var antennaTarget = ANTARGET_INVALID
      var antennaInventoryRoundControl: ByteArray? = null
      val ANTOGGLE_INVALID = -1
      val ANTOGGLE_MIN = 0
      val ANTOGGLE_MAX = 100
      var antennaToggle = ANTOGGLE_INVALID
      val ANTRFMODE_INVALID = -1
      val ANTRFMODE_MIN = 1
      val ANTRFMODE_MAX = 15
      var antennaRfMode = ANTRFMODE_INVALID
      val ANTPOWER_INVALID: Long = -1
      val ANTPOWER_MIN: Long = 0
      val ANTPOWER_MAX: Long = 330 //Maximum 330\
      var antennaPower = ANTPOWER_INVALID //default value = 300
       get(): Long {
         if (field < ANTPOWER_MIN || field > ANTPOWER_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 6, 7, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_ANT_DESC_RFPOWER,
               false,
               msgBuffer
            )
         }
         return field
      }

      var antennaPowerSet = false
      fun setAntennaPower(antennaPower: Long): Boolean {
         var antennaPower = antennaPower
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 6, 7, 0, 0, 0, 0)
         if (antennaPower < ANTPOWER_MIN || antennaPower > ANTPOWER_MAX) antennaPower =
            mDefault.antennaPower
         if (this.antennaPower == antennaPower && sameCheck) return true
         msgBuffer[4] = (antennaPower % 256).toByte()
         msgBuffer[5] = ((antennaPower shr 8) % 256).toByte()
         this.antennaPower = antennaPower
         antennaPowerSet = true
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_ANT_DESC_RFPOWER,
            true,
            msgBuffer
         )
      }

      init {
         var default_setting_type = default_setting_type
         if (default_setting_type < 0) default_setting_type = 0
         if (default_setting_type > 5) default_setting_type = 5
         mDefault = AntennaSelectedData_default(default_setting_type)


         if (false) {
            antennaEnable = mDefault.antennaEnable
            antennaInventoryMode = mDefault.antennaInventoryMode
            antennaLocalAlgo = mDefault.antennaLocalAlgo
            antennaLocalStartQ = mDefault.antennaLocalStartQ
            antennaProfileMode = mDefault.antennaProfileMode
            antennaLocalProfile = mDefault.antennaLocalProfile
            antennaFrequencyMode = mDefault.antennaFrequencyMode
            antennaLocalFrequency = mDefault.antennaLocalFrequency
            antennaStatus = mDefault.antennaStatus
            antennaDefine = mDefault.antennaDefine
            antennaDwell = mDefault.antennaDwell
            antennaPower = mDefault.antennaPower
            appendToLog("antennaPower is set to default $antennaPower")
            antennaInvCount = mDefault.antennaInvCount
         }
      }

      fun setAntennaInvCount(antennaInvCount: Long): Boolean {
         var antennaInvCount = antennaInvCount
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 7, 7, 0, 0, 0, 0)
         if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX) antennaInvCount =
            mDefault.antennaInvCount
         if (antennaInvCount == antennaInvCount && sameCheck) return true
         msgBuffer[4] = (antennaInvCount % 256).toByte()
         msgBuffer[5] = ((antennaInvCount shr 8) % 256).toByte()
         msgBuffer[6] = ((antennaInvCount shr 16) % 256).toByte()
         msgBuffer[7] = ((antennaInvCount shr 24) % 256).toByte()
         antennaInvCount = antennaInvCount
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_ANT_DESC_INV_CNT,
            true,
            msgBuffer
         )
      }
   }

   inner class InvSelectData(set_default_setting: Boolean) {
      inner class InvSelectData_default {
         var selectEnable = 0
         var selectTarget = 0
         var selectAction = 0
         var selectDelay = 0
         var selectMaskBank = 0
         var selectMaskOffset = 0
         var selectMaskLength = 0
         var selectMaskData0_31 = ByteArray(4 * 8)
         var selectMaskDataReady: Byte = 0
      }

      var mDefault = InvSelectData_default()
      val INVSELENABLE_INVALID = 0
      val INVSELENABLE_MIN = 0
      val INVSELENABLE_MAX = 1
      var selectEnable = INVSELENABLE_INVALID
       get(): Int {
          if (field < INVSELENABLE_MIN ||
              field > INVSELENABLE_MAX ) {
             rx000HostReg_HST_TAGMSK_DESC_CFG()
          }
         return field
      }

      fun setSelectEnable(selectEnable: Int): Boolean {
         return setRx000HostReg_HST_TAGMSK_DESC_CFG(
            selectEnable,
            selectTarget,
            selectAction,
            selectDelay
         )
      }

      val INVSELTARGET_INVALID = -1
      val INVSELTARGET_MIN = 0
      val INVSELTARGET_MAX = 7
      var selectTarget = INVSELTARGET_INVALID
       get(): Int {
          if(
             field < INVSELTARGET_MIN ||
             field > INVSELTARGET_MAX ){
             rx000HostReg_HST_TAGMSK_DESC_CFG()
          }
         return field
      }

      val INVSELACTION_INVALID = -1
      val INVSELACTION_MIN = 0
      val INVSELACTION_MAX = 7
      var selectAction = INVSELACTION_INVALID
       get(): Int {
          if(
             field < INVSELACTION_MIN ||
             field > INVSELACTION_MAX ){
             rx000HostReg_HST_TAGMSK_DESC_CFG()
          }
         return field
      }

      val INVSELDELAY_INVALID = -1
      val INVSELDELAY_MIN = 0
      val INVSELDELAY_MAX = 255
      var selectDelay = INVSELDELAY_INVALID
       get(): Int {
          if(field < INVSELDELAY_MIN ||
             field > INVSELDELAY_MAX){
            rx000HostReg_HST_TAGMSK_DESC_CFG()
          }
         return field
      }

      fun rx000HostReg_HST_TAGMSK_DESC_CFG() : Boolean {
         val msgBuffer = byteArrayOf(0x70.toByte(), 0, 1, 8, 0, 0, 0, 0)
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_DESC_CFG,
            false,
            msgBuffer
         )
      }

      fun setRx000HostReg_HST_TAGMSK_DESC_CFG(
         selectEnable: Int,
         selectTarget: Int,
         selectAction: Int,
         selectDelay: Int
      ): Boolean {
         var selectEnable = selectEnable
         var selectTarget = selectTarget
         var selectAction = selectAction
         var selectDelay = selectDelay
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 1, 8, 0, 0, 0, 0)
         if (selectEnable < INVSELENABLE_MIN || selectEnable > INVSELENABLE_MAX) selectEnable =
            mDefault.selectEnable
         if (selectTarget < INVSELTARGET_MIN || selectTarget > INVSELTARGET_MAX) selectTarget =
            mDefault.selectTarget
         if (selectAction < INVSELACTION_MIN || selectAction > INVSELACTION_MAX) selectAction =
            mDefault.selectAction
         val selectDalay0 = selectDelay
         if (selectDelay < INVSELDELAY_MIN || selectDelay > INVSELDELAY_MAX) selectDelay =
            mDefault.selectDelay
         if (this.selectEnable == selectEnable && this.selectTarget == selectTarget && this.selectAction == selectAction && this.selectDelay == selectDelay && sameCheck) return true
         msgBuffer[4] = (msgBuffer[4].toInt() or (selectEnable and 0x1).toByte().toInt()).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (selectTarget and 0x07 shl 1).toByte().toInt()).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (selectAction and 0x07 shl 4).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (selectDelay and 0xFF).toByte().toInt()).toByte()
         this.selectEnable = selectEnable
         this.selectTarget = selectTarget
         this.selectAction = selectAction
         this.selectDelay = selectDelay
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_DESC_CFG,
            true,
            msgBuffer
         )
      }

      val INVSELMBANK_INVALID = -1
      val INVSELMBANK_MIN = 0
      val INVSELMBANK_MAX = 3
      var selectMaskBank = INVSELMBANK_INVALID
       get(): Int {
         if (field < INVSELMBANK_MIN || field > INVSELMBANK_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 2, 8, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGMSK_BANK,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setSelectMaskBank(selectMaskBank: Int): Boolean {
         var selectMaskBank = selectMaskBank
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 2, 8, 0, 0, 0, 0)
         if (selectMaskBank < INVSELMBANK_MIN || selectMaskBank > INVSELMBANK_MAX) selectMaskBank =
            mDefault.selectMaskBank
         if (this.selectMaskBank == selectMaskBank && sameCheck) return true
         msgBuffer[4] = (msgBuffer[4].toInt() or (selectMaskBank and 0x3).toByte().toInt()).toByte()
         this.selectMaskBank = selectMaskBank
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_BANK,
            true,
            msgBuffer
         )
      }

      val INVSELMOFFSET_INVALID = -1
      val INVSELMOFFSET_MIN = 0
      val INVSELMOFFSET_MAX = 0xFFFF
      var selectMaskOffset = INVSELMOFFSET_INVALID
       get(): Int {
         if (field < INVSELMOFFSET_MIN || field > INVSELMOFFSET_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 3, 8, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGMSK_PTR,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setSelectMaskOffset(selectMaskOffset: Int): Boolean {
         var selectMaskOffset = selectMaskOffset
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 8, 0, 0, 0, 0)
         if (selectMaskOffset < INVSELMOFFSET_MIN || selectMaskOffset > INVSELMOFFSET_MAX) selectMaskOffset =
            mDefault.selectMaskOffset
         if (this.selectMaskOffset == selectMaskOffset && sameCheck) return true
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (selectMaskOffset and 0xFF).toByte().toInt()).toByte()
         msgBuffer[5] =
            (msgBuffer[5].toInt() or (selectMaskOffset shr 8 and 0xFF).toByte().toInt()).toByte()
         this.selectMaskOffset = selectMaskOffset
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_PTR,
            true,
            msgBuffer
         )
      }

      val INVSELMLENGTH_INVALID = -1
      val INVSELMLENGTH_MIN = 0
      val INVSELMLENGTH_MAX = 255
      var selectMaskLength = INVSELMLENGTH_INVALID
       get(): Int {
         appendToLog("getSelectMaskData with selectMaskLength = $field")
         if (field < INVSELMLENGTH_MIN || field > INVSELMLENGTH_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 4, 8, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_TAGMSK_LEN,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setSelectMaskLength(selectMaskLength: Int): Boolean {
         var selectMaskLength = selectMaskLength
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 4, 8, 0, 0, 0, 0)
         if (selectMaskLength < INVSELMLENGTH_MIN) selectMaskLength =
            INVSELMLENGTH_MIN else if (selectMaskLength > INVSELMLENGTH_MAX) selectMaskLength =
            INVSELMLENGTH_MAX
         if (this.selectMaskLength == selectMaskLength && sameCheck) return true
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (selectMaskLength and 0xFF).toByte().toInt()).toByte()
         if (selectMaskLength == INVSELMLENGTH_MAX) msgBuffer[5] = 1
         this.selectMaskLength = selectMaskLength
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_TAGMSK_PTR,
            true,
            msgBuffer
         )
      }

      var selectMaskData0_31 = ByteArray(4 * 8)
      var selectMaskDataReady: Byte = 0

      init {
         if (set_default_setting) {
            selectEnable = mDefault.selectEnable
            selectTarget = mDefault.selectTarget
            selectAction = mDefault.selectAction
            selectDelay = mDefault.selectDelay
            selectMaskBank = mDefault.selectMaskBank
            selectMaskOffset = mDefault.selectMaskOffset
            selectMaskLength = mDefault.selectMaskLength
            selectMaskDataReady = mDefault.selectMaskDataReady
         }
      }

      val rx000SelectMaskData: String?
         get() {
            appendToLog(
               "getSelectMaskData with selectMaskData0_31 = " + byteArrayToString(
                  selectMaskData0_31
               )
            )
            var length = selectMaskLength
            var strValue: String? = ""
            if (length < 0) {
               selectMaskLength
            } else {
               for (i in 0..7) {
                  if (length > 0) {
                     if (selectMaskDataReady.toInt() and (0x01 shl i) == 0) {
                        val msgBuffer = byteArrayOf(0x70.toByte(), 0, 5, 8, 0, 0, 0, 0)
                        msgBuffer[2] = (msgBuffer[2] + i).toByte()
                        mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                           HostRegRequests.HST_TAGMSK_0_3,
                           false,
                           msgBuffer
                        )
                        strValue = null
                        break
                     } else {
                        for (j in 0..3) {
                           if (DEBUG) appendToLog("i = " + i + ", j = " + j + ", selectMaskData0_31 = " + selectMaskData0_31[i * 4 + j])
                           strValue += String.format("%02X", selectMaskData0_31[i * 4 + j])
                        }
                     }
                     length -= 32
                  }
               }
            }
            return strValue
         }

      fun setRx000SelectMaskData(maskData: String): Boolean {
         var length = maskData.length
         for (i in 0..7) {
            if (length > 0) {
               length -= 8
               val msgBuffer = byteArrayOf(0x70.toByte(), 1, 5, 8, 0, 0, 0, 0)
               val hexString = "0123456789ABCDEF"
               for (j in 0..7) {
                  if (i * 8 + j + 1 <= maskData.length) {
                     val subString = maskData.substring(i * 8 + j, i * 8 + j + 1).uppercase(
                        Locale.getDefault()
                     )
                     var k = 0
                     k = 0
                     while (k < 16) {
                        if (subString.matches(hexString.substring(k, k + 1).toRegex())) {
                           break
                        }
                        k++
                     }
                     if (k == 16) return false
                     //                                appendToLog("setSelectMaskData(" + maskData +"): i=" + i + ", j=" + j + ", k=" + k);
                     if (j / 2 * 2 == j) {
                        msgBuffer[4 + j / 2] = (msgBuffer[4 + j / 2].toInt() or (k shl 4).toByte()
                           .toInt()).toByte()
                     } else {
                        msgBuffer[4 + j / 2] =
                           (msgBuffer[4 + j / 2].toInt() or k.toByte().toInt()).toByte()
                     }
                  }
               }
               msgBuffer[2] = ((msgBuffer[2].toInt() and 0xFF) + i).toByte()
               if (!mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
                     HostRegRequests.HST_TAGMSK_0_3,
                     true,
                     msgBuffer
                  )
               ) return false else {
                  selectMaskDataReady = (selectMaskDataReady.toInt() or (0x01 shl i)).toByte()
                  if (DEBUG) appendToLog(
                     "Old selectMaskData0_31 = " + byteArrayToString(
                        selectMaskData0_31
                     )
                  )
                  System.arraycopy(msgBuffer, 4, selectMaskData0_31, i * 4, 4)
                  if (DEBUG) appendToLog(
                     "New selectMaskData0_31 = " + byteArrayToString(
                        selectMaskData0_31
                     )
                  )
               }
            }
         }
         return true
      }
   }

   inner class AlgoSelectedData(set_default_setting: Boolean, default_setting_type: Int) {

      val mDefaultArray = AlgoSelectedData_defaultArray()
      var mDefault = AlgoSelectedData_default(1)
      val ALGOSTARTQ_INVALID = -1
      val ALGOSTARTQ_MIN = 0
      val ALGOSTARTQ_MAX = 15
      var algoStartQ = ALGOSTARTQ_INVALID
      inner class AlgoSelectedData_default(set_default_setting: Int) {
         var algoStartQ = ALGOSTARTQ_INVALID
         var algoMaxQ = -1
         var algoMinQ = -1
         var algoMaxRep = -1
         var algoHighThres = -1
         var algoLowThres = -1
         var algoRetry = -1
         var algoAbFlip = -1
         var algoRunTilZero = -1

         init {
            algoStartQ = mDefaultArray.algoStartQ[set_default_setting]
            algoMaxQ = mDefaultArray.algoMaxQ[set_default_setting]
            algoMinQ = mDefaultArray.algoMinQ[set_default_setting]
            algoMaxRep = mDefaultArray.algoMaxRep[set_default_setting]
            algoHighThres = mDefaultArray.algoHighThres[set_default_setting]
            algoLowThres = mDefaultArray.algoLowThres[set_default_setting]
            algoRetry = mDefaultArray.algoRetry[set_default_setting]
            algoAbFlip = mDefaultArray.algoAbFlip[set_default_setting]
            algoRunTilZero = mDefaultArray.algoRunTilZero[set_default_setting]
         }
      }

      inner class AlgoSelectedData_defaultArray {
         //0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
         var algoStartQ = intArrayOf(-1, 0, 0, 0, 4)
         var algoMaxQ = intArrayOf(-1, 0, 0, 0, 15)
         var algoMinQ = intArrayOf(-1, 0, 0, 0, 0)
         var algoMaxRep = intArrayOf(-1, 0, 0, 0, 4)
         var algoHighThres = intArrayOf(-1, 0, 5, 5, 5)
         var algoLowThres = intArrayOf(-1, 0, 3, 3, 3)
         var algoRetry = intArrayOf(-1, 0, 0, 0, 0)
         var algoAbFlip = intArrayOf(-1, 0, 1, 1, 1)
         var algoRunTilZero = intArrayOf(-1, 0, 0, 0, 0)
      }

      fun getAlgoStartQ(getInvalid: Boolean): Int {
         if (getInvalid && (algoStartQ < ALGOSTARTQ_MIN || algoStartQ > ALGOSTARTQ_MAX)) hST_INV_ALG_PARM_0
         return algoStartQ
      }

      fun setAlgoStartQ(algoStartQ: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      val ALGOMAXQ_INVALID = -1
      val ALGOMAXQ_MIN = 0
      val ALGOMAXQ_MAX = 15
      var algoMaxQ = ALGOMAXQ_INVALID
       get(): Int {
         if (field < ALGOMAXQ_MIN || field > ALGOMAXQ_MAX) hST_INV_ALG_PARM_0
         return field
      }

      fun setAlgoMaxQ(algoMaxQ: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      val ALGOMINQ_INVALID = -1
      val ALGOMINQ_MIN = 0
      val ALGOMINQ_MAX = 15
      var algoMinQ = ALGOMINQ_INVALID
       get(): Int {
         if (field < ALGOMINQ_MIN || field > ALGOMINQ_MAX) hST_INV_ALG_PARM_0
         return field
      }

      fun setAlgoMinQ(algoMinQ: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      val ALGOMAXREP_INVALID = -1
      val ALGOMAXREP_MIN = 0
      val ALGOMAXREP_MAX = 255
      var algoMaxRep = ALGOMAXREP_INVALID
       get(): Int {
         if (field < ALGOMAXREP_MIN || field > ALGOMAXREP_MAX) hST_INV_ALG_PARM_0
         return field
      }

      fun setAlgoMaxRep(algoMaxRep: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      val ALGOHIGHTHRES_INVALID = -1
      val ALGOHIGHTHRES_MIN = 0
      val ALGOHIGHTHRES_MAX = 15
      var algoHighThres = ALGOHIGHTHRES_INVALID
       get(): Int {
         if (field < ALGOHIGHTHRES_MIN || field > ALGOHIGHTHRES_MAX) hST_INV_ALG_PARM_0
         return field
      }

      fun setAlgoHighThres(algoHighThres: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      val ALGOLOWTHRES_INVALID = -1
      val ALGOLOWTHRES_MIN = 0
      val ALGOLOWTHRES_MAX = 15
      var algoLowThres = ALGOLOWTHRES_INVALID
       get(): Int {
         if (field < ALGOLOWTHRES_MIN || field > ALGOLOWTHRES_MAX) hST_INV_ALG_PARM_0
         return field
      }

      fun setAlgoLowThres(algoLowThres: Int): Boolean {
         return setAlgoStartQ(
            algoStartQ,
            algoMaxQ,
            algoMinQ,
            algoMaxRep,
            algoHighThres,
            algoLowThres
         )
      }

      private val hST_INV_ALG_PARM_0: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 3, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_ALG_PARM_0,
               false,
               msgBuffer
            )
         }

      fun setAlgoStartQ(
         startQ: Int,
         algoMaxQ: Int,
         algoMinQ: Int,
         algoMaxRep: Int,
         algoHighThres: Int,
         algoLowThres: Int
      ): Boolean {
         var startQ = startQ
         var algoMaxQ = algoMaxQ
         var algoMinQ = algoMinQ
         var algoMaxRep = algoMaxRep
         var algoHighThres = algoHighThres
         var algoLowThres = algoLowThres
         val DEBUG = false
         if (DEBUG) appendToLog("startQ = " + startQ + ", algoStartQ = " + algoStartQ)
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 9, 0, 0, 0, 0)
         if (startQ < ALGOSTARTQ_MIN || startQ > ALGOSTARTQ_MAX) startQ = mDefault.algoStartQ
         if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) algoMaxQ = mDefault.algoMaxQ
         if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) algoMinQ = mDefault.algoMinQ
         if (algoMaxRep < ALGOMAXREP_MIN || algoMaxRep > ALGOMAXREP_MAX) algoMaxRep =
            mDefault.algoMaxRep
         if (algoHighThres < ALGOHIGHTHRES_MIN || algoHighThres > ALGOHIGHTHRES_MAX) algoHighThres =
            mDefault.algoHighThres
         if (algoLowThres < ALGOLOWTHRES_MIN || algoLowThres > ALGOLOWTHRES_MAX) algoLowThres =
            mDefault.algoLowThres
         if (false) return true
         if (DEBUG) appendToLog("algoMaxRep = $algoMaxRep, algoMaxRep = $algoMaxRep, algoLowThres = $algoLowThres")
         msgBuffer[4] = (msgBuffer[4].toInt() or (startQ and 0x0F).toByte().toInt()).toByte()
         msgBuffer[4] =
            (msgBuffer[4].toInt() or (algoMaxQ and 0x0F shl 4).toByte().toInt()).toByte()
         msgBuffer[5] = (msgBuffer[5].toInt() or (algoMinQ and 0x0F).toByte().toInt()).toByte()
         msgBuffer[5] =
            (msgBuffer[5].toInt() or (algoMaxRep and 0xF shl 4).toByte().toInt()).toByte()
         msgBuffer[6] =
            (msgBuffer[6].toInt() or (algoMaxRep and 0xF0 shr 4).toByte().toInt()).toByte()
         msgBuffer[6] =
            (msgBuffer[6].toInt() or (algoHighThres and 0x0F shl 4).toByte().toInt()).toByte()
         msgBuffer[7] = (msgBuffer[7].toInt() or (algoLowThres and 0x0F).toByte().toInt()).toByte()
         algoStartQ = startQ
         this.algoMaxQ = algoMaxQ
         this.algoMinQ = algoMinQ
         this.algoMaxRep = algoMaxRep
         this.algoHighThres = algoHighThres
         this.algoLowThres = algoLowThres
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_ALG_PARM_0,
            true,
            msgBuffer
         )
      }

      val ALGORETRY_INVALID = -1
      val ALGORETRY_MIN = 0
      val ALGORETRY_MAX = 255
      var algoRetry = ALGORETRY_INVALID
      get(): Int {
         if (field < ALGORETRY_MIN || field > ALGORETRY_MAX) {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 4, 9, 0, 0, 0, 0)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_ALG_PARM_1,
               false,
               msgBuffer
            )
         }
         return field
      }

      fun setAlgoRetry(algoRetry: Int): Boolean {
         var algoRetry = algoRetry
         if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX) algoRetry = mDefault.algoRetry
         if (false) return true
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 4, 9, 0, 0, 0, 0)
         msgBuffer[4] = algoRetry.toByte()
         this.algoRetry = algoRetry
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_ALG_PARM_1,
            true,
            msgBuffer
         )
      }

      val ALGOABFLIP_INVALID = -1
      val ALGOABFLIP_MIN = 0
      val ALGOABFLIP_MAX = 1
      var algoAbFlip = ALGOABFLIP_INVALID
      get(): Int {
         if (field < ALGOABFLIP_MIN || field > ALGOABFLIP_MAX) hST_INV_ALG_PARM_2
         return field
      }

      fun setAlgoAbFlip(algoAbFlip: Int): Boolean {
         return setAlgoAbFlip(algoAbFlip, algoRunTilZero)
      }

      val ALGORUNTILZERO_INVALID = -1
      val ALGORUNTILZERO_MIN = 0
      val ALGORUNTILZERO_MAX = 1

      var algoRunTilZero = ALGORUNTILZERO_INVALID
         get(): Int {
            if (field < ALGORUNTILZERO_MIN || field > ALGORUNTILZERO_MAX) hST_INV_ALG_PARM_2
            return field
         }
      init {
         var default_setting_type = default_setting_type
         if (default_setting_type < 0) default_setting_type = 0
         if (default_setting_type > 4) default_setting_type = 4
         mDefault = AlgoSelectedData_default(default_setting_type)
         if (set_default_setting) {
            algoStartQ = mDefault.algoStartQ
            algoMaxQ = mDefault.algoMaxQ
            algoMinQ = mDefault.algoMinQ
            algoMaxRep = mDefault.algoMaxRep
            algoHighThres = mDefault.algoHighThres
            algoLowThres = mDefault.algoLowThres
            algoRetry = mDefault.algoRetry
            algoAbFlip = mDefault.algoAbFlip
            algoRunTilZero = mDefault.algoRunTilZero
         }
      }

      fun setAlgoRunTilZero(algoRunTilZero: Int): Boolean {
         return setAlgoAbFlip(algoAbFlip, algoRunTilZero)
      }

      private val hST_INV_ALG_PARM_2: Boolean
         private get() {
            val msgBuffer = byteArrayOf(0x70.toByte(), 0, 5, 9, 0, 0, 0, 0)
            return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
               HostRegRequests.HST_INV_ALG_PARM_2,
               false,
               msgBuffer
            )
         }

      fun setAlgoAbFlip(algoAbFlip: Int, algoRunTilZero: Int): Boolean {
         var algoAbFlip = algoAbFlip
         var algoRunTilZero = algoRunTilZero
         if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX) algoAbFlip =
            mDefault.algoAbFlip
         if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX) algoRunTilZero =
            mDefault.algoRunTilZero
         if (false) appendToLog("this.algoAbFlip  = " + this.algoAbFlip + ", algoAbFlip = " + algoAbFlip + ", this.algoRunTilZero = " + this.algoRunTilZero + ", algoRunTilZero = " + algoRunTilZero)
         if (false) return true
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 5, 9, 0, 0, 0, 0)
         if (algoAbFlip != 0) {
            msgBuffer[4] = (msgBuffer[4].toInt() or 0x01).toByte()
         }
         if (algoRunTilZero != 0) {
            msgBuffer[4] = (msgBuffer[4].toInt() or 0x02).toByte()
         }
         this.algoAbFlip = algoAbFlip
         this.algoRunTilZero = algoRunTilZero
         return mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequest(
            HostRegRequests.HST_INV_ALG_PARM_2,
            true,
            msgBuffer
         )
      }
   }

   inner class Rx000EngSetting {
      var narrowRSSI = -1
      var wideRSSI = -1
      fun getwideRSSI(): Int {
         if (wideRSSI < 0) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(
               0x100,
               0x05
            ) //sub-command: 0x05, Arg0: reserved
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(
               0x101,
               (3 + 0x20000).toLong()
            ) //Arg1: 15-0: number of RSSI sample
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_ENGTEST)
         } else appendToLog("Hello123: wideRSSI = $wideRSSI")
         return wideRSSI
      }

      fun getnarrowRSSI(): Int {
         if (narrowRSSI < 0) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(
               0x100,
               0x05
            ) //sub-command: 0x05, Arg0: reserved
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(
               0x101,
               (3 + 0x20000).toLong()
            ) //Arg1: 15-0: number of RSSI sample
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_ENGTEST)
         } else appendToLog("Hello123: narrowRSSI = $wideRSSI")
         return wideRSSI
      }

      fun resetRSSI() {
         narrowRSSI = -1
         wideRSSI = -1
      }
   }

   inner class Rx000MbpSetting {
      val RXGAIN_INVALID = -1
      val RXGAIN_MIN = 0
      val RXGAIN_MAX = 0x1FF
      val highCompression: Int
         get() {
            var iRetValue = -1
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
               appendToLog("70010004: getHighCompression")
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPRDREG)
            } else iRetValue = rxGain shr 8
            return iRetValue
         }
      val rflnaGain: Int
         get() {
            var iRetValue = -1
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
               appendToLog("70010004: getRflnaGain")
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPRDREG)
            } else iRetValue = rxGain and 0xC0 shr 6
            return iRetValue
         }
      val iflnaGain: Int
         get() {
            var iRetValue = -1
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
               appendToLog("70010004: getIflnaGain")
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPRDREG)
            } else iRetValue = rxGain and 0x38 shr 3
            return iRetValue
         }
      val agcGain: Int
         get() {
            var iRetValue = -1
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
               appendToLog("70010004: getAgcGain")
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPRDREG)
            } else iRetValue = rxGain and 0x07
            return iRetValue
         }
      var rxGain = RXGAIN_INVALID
       get(): Int {
         var iRetValue = -1
         if (field < RXGAIN_MIN || field > RXGAIN_MAX) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPRDREG)
         } else iRetValue = field
         return iRetValue
      }

      fun setRxGain(highCompression: Int, rflnagain: Int, iflnagain: Int, agcgain: Int): Boolean {
         val rxGain_new =
            highCompression and 0x01 shl 8 or (rflnagain and 0x3 shl 6) or (iflnagain and 0x7 shl 3) or (agcgain and 0x7)
         return setRxGain(rxGain_new)
      }

      fun setRxGain(rxGain_new: Int): Boolean {
         var bRetValue = true
         if (rxGain_new != rxGain || !sameCheck) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            bRetValue = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPAddress(0x450)
            if (bRetValue) bRetValue =
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setMBPData(rxGain_new.toLong())
            if (bRetValue) bRetValue =
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_MBPWRREG)
            if (bRetValue) rxGain = rxGain_new
         }
         return bRetValue
      }
   }

   inner class Rx000OemSetting {
      val COUNTRYCODE_INVALID = -1
      val COUNTRYCODE_MIN = 1
      val COUNTRYCODE_MAX = 9
      var countryCode = COUNTRYCODE_INVALID // OemAddress = 0x02
       get(): Int {
         if (field < COUNTRYCODE_MIN || field > COUNTRYCODE_MAX) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress(2)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
         }
         return field
      }

      val SERIALCODE_INVALID = -1
      var serialNumber = byteArrayOf(
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0
      )

      fun getSerialNumber(): String? {
         var invalid = false
         var length = serialNumber.size / 4
         if (serialNumber.size % 4 != 0) length++
         for (i in 0 until length) {
            if (serialNumber[4 * i].toInt() == SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
               invalid = true
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress((0x04 + i).toLong())
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
            }
         }
         if (invalid) return null
         appendToLog("retValue = " + byteArrayToString(serialNumber))
         val retValue = ByteArray(serialNumber.size)
         for (i in retValue.indices) {
            val j = i / 4 * 4 + 3 - i % 4
            if (j >= serialNumber.size) retValue[i] = serialNumber[i] else retValue[i] =
               serialNumber[j]
            if (retValue[i].toInt() == 0) retValue[i] = 0x30
         }
         appendToLog(
            "retValue = " + byteArrayToString(retValue) + ", String = " + retValue.toString()
         )
         return retValue.toString()
      }

      val PRODUCT_SERIALCODE_INVALID = -1
      var productserialNumber = byteArrayOf(
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0,
         SERIALCODE_INVALID.toByte(),
         0,
         0,
         0
      )
      val productSerialNumber: String?
         get() {
            var invalid = false
            var length = productserialNumber.size / 4
            if (productserialNumber.size % 4 != 0) length++
            for (i in 0 until length) {
               if (productserialNumber[4 * i].toInt() == PRODUCT_SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
                  invalid = true
                  mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
                  mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress((0x08 + i).toLong())
                  mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
               }
            }
            if (invalid) return null
            appendToLog("retValue = " + byteArrayToString(productserialNumber))
            val retValue = ByteArray(productserialNumber.size)
            for (i in retValue.indices) {
               val j = i / 4 * 4 + 3 - i % 4
               if (j >= productserialNumber.size) retValue[i] =
                  productserialNumber[i] else retValue[i] = productserialNumber[j]
               if (retValue[i].toInt() == 0) retValue[i] = 0x30
            }
            appendToLog(
               "retValue = " + byteArrayToString(retValue) + ", String = " + retValue.toString()
            )
            return retValue.toString()
         }
      val VERSIONCODE_INVALID = -1
      val VERSIONCODE_MIN = 1
      val VERSIONCODE_MAX = 9
      var versionCode = VERSIONCODE_INVALID // OemAddress = 0x02
       get(): Int {
         if (field < VERSIONCODE_MIN || field > VERSIONCODE_MAX) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress(0x0B)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
         }
         return field
      }

      var spcialCountryVersion: String? = null
      val specialCountryVersion: String
         get() {
            if (spcialCountryVersion == null) {
               mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
               mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress(0x8E)
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
               return ""
            }
            return spcialCountryVersion!!.replace("[^A-Za-z0-9]".toRegex(), "")
         }
      val FREQMODIFYCODE_INVALID = -1
      val FREQMODIFYCODE_MIN = 0
      val FREQMODIFYCODE_MAX = 0xAA
      var freqModifyCode = FREQMODIFYCODE_INVALID // OemAddress = 0x8A
       get(): Int {
         if (field < FREQMODIFYCODE_MIN || field > FREQMODIFYCODE_MAX) {
            mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress(0x8F)
            mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_RDOEM)
         }
         return field
      }

      fun writeOEM(address: Int, value: Int) {
         mRfidDevice!!.mRfidReaderChip!!.setPwrManagementMode(false)
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMAddress(address.toLong())
         mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.setOEMData(value.toLong())
         mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_WROEM)
      }
   }

   var bFirmware_reset_before = false
   val RFID_READING_BUFFERSIZE = 1024

   init {
      cs108ConnectorDataInit()
      mHandler.removeCallbacks(runnableProcessBleStreamInData)
      mHandler.post(runnableProcessBleStreamInData)
      mHandler.removeCallbacks(mReadWriteRunnable)
      mHandler.post(mReadWriteRunnable)
      mHandler.removeCallbacks(runnableRx000UplinkHandler)
      mHandler.post(runnableRx000UplinkHandler)
   }

   inner class RfidReaderChip {
      var mRfidToReading = ByteArray(RFID_READING_BUFFERSIZE)
      var mRfidToReadingOffset = 0
      var mRx000ToWrite = ArrayList<Cs108RfidData>()
      var mRx000Setting = Rx000Setting(true)
      var mRx000EngSetting = Rx000EngSetting()
      var mRx000MbpSetting = Rx000MbpSetting()
      var mRx000OemSetting = Rx000OemSetting()
      var mRx000ToRead : ArrayList<Rx000pkgData?>? = ArrayList<Rx000pkgData?>()
      private var clearTempDataIn_request = false
      var commandOperating = false
      fun decodeNarrowBandRSSI(byteRSSI: Byte): Double {
         var mantissa = byteRSSI
         mantissa = (mantissa.toInt() and 0x07).toByte()
         var exponent = byteRSSI
         exponent = (exponent.toInt() shr 3).toByte()
         val dValue = 20 * Math.log10(
            Math.pow(2.0, exponent.toDouble()) * (1 + mantissa / Math.pow(
               2.0,
               3.0
            ))
         )
         if (false) appendToLog(
            "byteRSSI = " + String.format(
               "%X",
               byteRSSI
            ) + ", mantissa = " + mantissa + ", exponent = " + exponent + "dValue = " + dValue
         )
         return dValue
      }

      fun encodeNarrowBandRSSI(dRSSI: Double): Int {
         var dValue = dRSSI / 20
         dValue = Math.pow(10.0, dValue)
         var exponent = 0
         if (false) appendToLog("exponent = $exponent, dValue = $dValue")
         while (dValue + 0.062 >= 2) {
            dValue /= 2.0
            exponent++
            if (false) appendToLog("exponent = $exponent, dValue = $dValue")
         }
         dValue--
         var mantissa = (dValue * 8 + 0.5).toInt()
         while (mantissa >= 8) {
            mantissa -= 8
            exponent++
         }
         val iValue = exponent and 0x1F shl 3 or (mantissa and 0x7)
         if (false) appendToLog(
            "dRssi = " + dRSSI + ", exponent = " + exponent + ", mantissa = " + mantissa + ", iValue = " + String.format(
               "%X",
               iValue
            )
         )
         return iValue
      }

      var firmware_ontime_ms: Long = 0
      var date_time_ms: Long = 0
      var bRx000ToReading = false
      fun mRx000UplinkHandler() {
         val DEBUG = false
         if (bRx000ToReading) return
         bRx000ToReading = true
         var startIndex = 0
         var startIndexOld = 0
         var startIndexNew = 0
         var packageFound = false
         var packageType = 0
         val lTime = System.currentTimeMillis()
         var bdebugging = false
         if (mRfidDevice!!.mRfidToRead.size != 0) {
            bdebugging = true
            if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): START")
         } else if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): START AAA")
         var bFirst = true
         while (mRfidDevice!!.mRfidToRead.size != 0) {
            if (!isBleConnected) {
               mRfidDevice!!.mRfidToRead.clear()
            } else if (System.currentTimeMillis() - lTime > intervalRx000UplinkHandler / 2) {
               writeDebug2File("D" + intervalRx000UplinkHandler + ", " + System.currentTimeMillis() + ", Timeout")
               if (DEBUG) appendToLogView("mRx000UplinkHandler_TIMEOUT !!! mRfidToRead.size() = " + mRfidDevice!!.mRfidToRead.size)
               break
            } else {
               if (bFirst) {
                  bFirst = false
                  writeDebug2File("D" + intervalRx000UplinkHandler + ", " + System.currentTimeMillis())
               }
               var dataIn = mRfidDevice!!.mRfidToRead[0].dataValues
               val tagMilliSeconds = mRfidDevice!!.mRfidToRead[0].milliseconds
               val invalidSequence = mRfidDevice!!.mRfidToRead[0].invalidSequence
               if (DEBUG) appendToLog(
                  "mRx000UplinkHandler(): invalidSequence = " + invalidSequence + ", Processing data = " + byteArrayToString(
                     dataIn
                  ) + ", length=" + dataIn!!.size + ", mRfidToReading.length=" + mRfidToReading.size + ", startIndex=" + startIndex + ", startIndexNew=" + startIndexNew + ", mRfidToReadingOffset=" + mRfidToReadingOffset
               )
               mRfidDevice!!.mRfidToRead.removeAt(0)
               if (dataIn!!.size >= mRfidToReading.size - mRfidToReadingOffset) {
                  val unhandledBytes = ByteArray(mRfidToReadingOffset)
                  System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.size)
                  if (true) appendToLogView(
                     "mRx000UplinkHandler(): ERROR insufficient buffer, mRfidToReadingOffset=" + mRfidToReadingOffset + ", dataIn.length=" + dataIn.size + ", clear mRfidToReading: " + byteArrayToString(
                        unhandledBytes
                     )
                  )
                  val mRfidToReadingNew = ByteArray(RFID_READING_BUFFERSIZE)
                  mRfidToReading = mRfidToReadingNew
                  mRfidToReadingOffset = 0
                  invalidUpdata++
               }
               if (mRfidToReadingOffset != 0 && invalidSequence) {
                  val unhandledBytes = ByteArray(mRfidToReadingOffset)
                  System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.size)
                  if (DEBUG) appendToLog(
                     "mRx000UplinkHandler(): ERROR invalidSequence with nonzero mRfidToReadingOffset=" + mRfidToReadingOffset + ", throw invalid unused data=" + unhandledBytes.size + ", " + byteArrayToString(
                        unhandledBytes
                     )
                  )
                  mRfidToReadingOffset = 0
                  startIndex = 0
                  startIndexNew = 0
               }
               System.arraycopy(dataIn, 0, mRfidToReading, mRfidToReadingOffset, dataIn.size)
               mRfidToReadingOffset += dataIn.size
               if (true) {
                  val bufferData = ByteArray(mRfidToReadingOffset)
                  System.arraycopy(mRfidToReading, 0, bufferData, 0, bufferData.size)
                  if (DEBUG) appendToLog(
                     "mRx000UplinkHandler(): mRfidToReadingOffset= " + mRfidToReadingOffset + ", mRfidToReading= " + byteArrayToString(
                        bufferData
                     )
                  )
               }
               if (DEBUG) appendToLog("mRfidToReadingOffset = $mRfidToReadingOffset, startIndex = $startIndex")
               val iPayloadSizeMin = 8
               while (mRfidToReadingOffset - startIndex >= iPayloadSizeMin) {
                  run {
                     val packageLengthRead =
                        (mRfidToReading[startIndex + 5].toInt() and 0xFF) * 256 + (mRfidToReading[startIndex + 4].toInt() and 0xFF)
                     var expectedLength = 8 + packageLengthRead * 4
                     if (mRfidToReading[startIndex].toInt() == 0x04) expectedLength =
                        8 + packageLengthRead
                     if (DEBUG) appendToLog("loop: mRfidToReading.length=" + mRfidToReading.size + ", 1Byte=" + mRfidToReading[startIndex] + ", mRfidToReadingOffset=" + mRfidToReadingOffset + ", startIndex=" + startIndex + ", expectedLength=" + expectedLength)
                     if (mRfidToReadingOffset - startIndex >= 8) {
                        if (mRfidToReading[startIndex] == 0x40.toByte()
                           && (mRfidToReading[startIndex + 1].toInt() == 2 || mRfidToReading[startIndex + 1].toInt() == 3 || mRfidToReading[startIndex + 1].toInt() == 7)
                        ) {   //input as Control Command Response
                           dataIn = mRfidToReading
                           if (DEBUG) appendToLog("decoding CONTROL data")
                           if (mRfidDevice!!.mRfidToWrite.size == 0) {
                              if (DEBUG) appendToLog("Control Response is received with null mRfidToWrite")
                           } else if (mRfidDevice!!.mRfidToWrite[0] == null) {
                              if (DEBUG) appendToLog("Control Response is received with null mRfidToWrite.get(0)")
                           } else if (mRfidDevice!!.mRfidToWrite[0]!!.dataValues == null) {
                              mRfidDevice!!.mRfidToWrite.removeAt(0)
                              if (DEBUG) appendToLog("mmRfidToWrite remove 5")
                              if (DEBUG) appendToLog("Control Response is received with null mRfidToWrite.dataValues")
                           } else if (!(mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![0] == dataIn!![startIndex] && mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![1] == dataIn!![startIndex + 1])) {
                              if (DEBUG) appendToLog(
                                 "Control Response is received with Mis-matched mRfidToWrite, " + startIndex + ", " + byteArrayToString(
                                    dataIn
                                 )
                              )
                           } else {
                              var dataInCompare: ByteArray? = null
                              when (mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![1].toInt()) {
                                 2 -> dataInCompare = byteArrayOf(
                                    0x40,
                                    0x02,
                                    0xbf.toByte(),
                                    0xfd.toByte(),
                                    0xbf.toByte(),
                                    0xfd.toByte(),
                                    0xbf.toByte(),
                                    0xfd.toByte()
                                 )

                                 3 -> dataInCompare = byteArrayOf(
                                    0x40,
                                    0x03,
                                    0xbf.toByte(),
                                    0xfc.toByte(),
                                    0xbf.toByte(),
                                    0xfc.toByte(),
                                    0xbf.toByte(),
                                    0xfc.toByte()
                                 )

                                 7 -> dataInCompare = byteArrayOf(
                                    0x40,
                                    0x07,
                                    0xbf.toByte(),
                                    0xf8.toByte(),
                                    0xbf.toByte(),
                                    0xf8.toByte(),
                                    0xbf.toByte(),
                                    0xf8.toByte()
                                 )
                              }
                              val dataIn8 = ByteArray(8)
                              System.arraycopy(dataIn, startIndex, dataIn8, 0, dataIn8.size)
                              if (!compareArray(dataInCompare, dataIn8, 8)) {
                                 if (DEBUG) appendToLog(
                                    "Control response with invalid data: " + byteArrayToString(
                                       dataIn8
                                    )
                                 )
                              } else {
                                 mRfidDevice!!.mRfidToWrite.removeAt(0)
                                 mRfidDevice!!.sendRfidToWriteSent = 0
                                 mRfidDevice!!.mRfidToWriteRemoved = true
                                 if (DEBUG) appendToLog("mmRfidToWrite remove 6")
                                 if (DEBUG) appendToLog("matched control command with mRfidToWrite.size=" + mRfidDevice!!.mRfidToWrite.size)
                              }
                           }
                           if (true) {
                              val dataIn8 = ByteArray(8)
                              System.arraycopy(dataIn, startIndex, dataIn8, 0, dataIn8.size)
                              val dataInCompare = byteArrayOf(
                                 0x40,
                                 0x03,
                                 0xbf.toByte(),
                                 0xfc.toByte(),
                                 0xbf.toByte(),
                                 0xfc.toByte(),
                                 0xbf.toByte(),
                                 0xfc.toByte()
                              )
                              if (compareArray(dataInCompare, dataIn8, 8)) {
                                 val dataA = Rx000pkgData()
                                 dataA.dataValues = dataIn8
                                 dataA.responseType = HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN

                                 val oldSize2 = mRx000ToRead?.size
                                 if (oldSize2 != null) {
                                    if(mRx000ToRead != null && oldSize2 >= 0){
                                       mRx000ToRead?.add(oldSize2!!, dataA)
                                       if (DEBUG) appendToLog("Abort Return data is found wth type = " + dataA.responseType.toString())
                                    }
                                 }
                                 mRfidDevice!!.inventoring = false
                              }
                           }
                           packageFound = true
                           packageType = 1
                           startIndexNew = startIndex + iPayloadSizeMin
                        } else if ((mRfidToReading[startIndex] == 0x00.toByte() || mRfidToReading[startIndex] == 0x70.toByte()) && mRfidToReading[startIndex + 1].toInt() == 0 && mRfidDevice!!.mRfidToWrite.size != 0 && mRfidDevice!!.mRfidToWrite[0]!!.dataValues != null && mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![0].toInt() == 0x70 && mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![1].toInt() == 0) {   //if input as HOST_REG_RESP
                           if (DEBUG) appendToLog(
                              "loop: decoding HOST_REG_RESP data with startIndex = " + startIndex + ", mRfidToReading=" + byteArrayToString(
                                 mRfidToReading
                              )
                           )
                           dataIn = mRfidToReading
                           val dataInPayload = ByteArray(4)
                           System.arraycopy(
                              dataIn,
                              startIndex + 4,
                              dataInPayload,
                              0,
                              dataInPayload.size
                           )
                           //if (mRfidDevice.mRfidToWrite.size() == 0) {
                           //    if (true) appendToLog("mRx000UplinkHandler(): HOST_REG_RESP is received with null mRfidToWrite: " + byteArrayToString(dataInPayload));
                           //} else if (mRfidDevice.mRfidToWrite.get(0).dataValues == null) {
                           //    if (true) appendToLog("mRx000UplinkHandler(): NULL mRfidToWrite.get(0).dataValues"); //.length = " + mRfidDevice.mRfidToWrite.get(0).dataValues.length);
                           //} else if (!(mRfidDevice.mRfidToWrite.get(0).dataValues[0] == 0x70 && mRfidDevice.mRfidToWrite.get(0).dataValues[1] == 0)) {
                           //    if (true) appendToLog("mRx000UplinkHandler(): HOST_REG_RESP is received with invalid mRfidDevice.mRfidToWrite.get(0).dataValues=" + byteArrayToString(mRfidDevice.mRfidToWrite.get(0).dataValues));
                           //} else
                           run {
                              val addressToWrite =
                                 mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![2] + mRfidDevice!!.mRfidToWrite[0]!!.dataValues!![3] * 256
                              val addressToRead =
                                 dataIn!![startIndex + 2] + dataIn!![startIndex + 3] * 256
                              if (addressToRead != addressToWrite) {
                                 if (DEBUG) appendToLog(
                                    "mRx000UplinkHandler(): HOST_REG_RESP is received with misMatch address: addressToRead=" + addressToRead + ", " + startIndex + ", " + byteArrayToString(
                                       dataInPayload
                                    ) + ", addressToWrite=" + addressToWrite
                                 )
                              } else {
                                 when (addressToRead) {
                                    0 -> {
                                       val patchVersion =
                                          dataIn!![startIndex + 4] + (dataIn!![startIndex + 5].toInt() and 0x0F) * 256
                                       val minorVersion =
                                          (dataIn!![startIndex + 5].toInt() shr 4) + dataIn!![startIndex + 6] * 256
                                       val majorVersion = dataIn!![startIndex + 7].toInt()
                                       mRx000Setting.macVer =
                                          "$majorVersion.$minorVersion.$patchVersion"
                                       if (DEBUG) appendToLog("found MacVer =" + mRx000Setting.macVer)
                                    }

                                    9 -> {
                                       mRx000Setting.mac_last_command_duration =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF).toLong() * 256 * 256 * 256
                                       if (DEBUG) appendToLog("found mac_last_command_duration =" + mRx000Setting.mac_last_command_duration)
                                    }

                                    0x0201 -> {
                                       mRx000Setting.diagnosticCfg =
                                          (dataIn!![startIndex + 4].toInt() and 0x0FF) + (dataIn!![startIndex + 5].toInt() and 0x03) * 256
                                       if (DEBUG) appendToLog(
                                          "found diagnostic configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", diagnosticCfg=" + mRx000Setting.diagnosticCfg
                                       )
                                    }

                                    0x0203 -> mRx000Setting.impinjExtensionValue =
                                       dataIn!![startIndex + 4].toInt() and 0x03F

                                    0x204 -> {
                                       mRx000Setting.pwrMgmtStatus =
                                          dataIn!![startIndex + 4].toInt() and 0x07
                                       if (DEBUG) appendToLog("pwrMgmtStatus = " + mRx000Setting.pwrMgmtStatus)
                                    }

                                    0x0700 -> {
                                       mRx000Setting.antennaCycle =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256
                                       mRx000Setting.antennaFreqAgile = 0
                                       if (dataIn!![startIndex + 7].toInt() and 0x01 != 0) mRx000Setting.antennaFreqAgile =
                                          1
                                       if (DEBUG) appendToLog(
                                          "found antenna cycle: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", cycle=" + mRx000Setting.antennaCycle + ", frequencyAgile=" + mRx000Setting.antennaFreqAgile
                                       )
                                    }

                                    0x0701 -> {
                                       mRx000Setting.antennaSelect =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF) * 256 * 256 * 256
                                       if (DEBUG) appendToLog("found antenna select, select=" + mRx000Setting.antennaSelect)
                                    }

                                    0x0702 -> {
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaEnable =
                                          dataIn!![startIndex + 4].toInt() and 0x01
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaInventoryMode =
                                          dataIn!![startIndex + 4].toInt() and 0x02 shr 1
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalAlgo =
                                          dataIn!![startIndex + 4].toInt() and 0x0C shr 2
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalStartQ =
                                          dataIn!![startIndex + 4].toInt() and 0xF0 shr 4
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaProfileMode =
                                          dataIn!![startIndex + 5].toInt() and 0x01
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalProfile =
                                          dataIn!![startIndex + 5].toInt() and 0x1E shr 1
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaFrequencyMode =
                                          dataIn!![startIndex + 5].toInt() and 0x20 shr 5
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalFrequency =
                                          (dataIn!![startIndex + 5].toInt() and 0x0F) * 4 + (dataIn!![startIndex + 5].toInt() and 0xC0 shr 6)
                                       if (DEBUG) appendToLog(
                                          "found antenna selectEnable: " + byteArrayToString(
                                             dataInPayload
                                          )
                                                + ", selectEnable=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaEnable
                                                + ", inventoryMode=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaInventoryMode
                                                + ", localAlgo=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalAlgo
                                                + ", localStartQ=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalStartQ
                                                + ", profileMode=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaProfileMode
                                                + ", localProfile=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalProfile
                                                + ", frequencyMode=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaFrequencyMode
                                                + ", localFrequency=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaLocalFrequency
                                       )
                                    }

                                    0x0703 -> {
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaStatus =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0x0F) * 256 * 256
                                       if (DEBUG) appendToLog(
                                          "found antenna status: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", status=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaStatus
                                       )
                                    }

                                    0x0704 -> {
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaDefine =
                                          dataIn!![startIndex + 4].toInt() and 0x3
                                       //      mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect].antennaRxDefine = (dataIn[startIndex + 6] & 0x3);
                                       if (DEBUG) appendToLog(
                                          "found antenna define: " + byteArrayToString(dataInPayload)
                                                + ", define=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaDefine //        + ", RxDefine=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect].antennaRxDefine
                                       )
                                    }

                                    0x0705 -> {
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaDwell =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF).toLong() * 256 * 256 * 256
                                       if (DEBUG) appendToLog("found antenna dwell=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaDwell)
                                    }

                                    0x0706 -> if (!mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaPowerSet) mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaPower =
                                       (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF).toLong() * 256 * 256 * 256

                                    0x0707 -> {
                                       mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaInvCount =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF).toLong() * 256 * 256 * 256
                                       if (DEBUG) appendToLog("found antenna InvCount=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect]!!.antennaInvCount)
                                    }

                                    0x0800 -> {
                                       mRx000Setting.invSelectIndex =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 6].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0xFF) * 256 * 256 * 256
                                       if (DEBUG) appendToLog(
                                          "found inventory select: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", select=" + mRx000Setting.invSelectIndex
                                       )
                                    }

                                    0x0801 -> {
                                       val dataIndex = mRx000Setting.invSelectIndex
                                       if (dataIndex < mRx000Setting.INVSELECT_MIN || dataIndex > mRx000Setting.INVSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory select configuration: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.invSelectData[dataIndex]!!.selectEnable =
                                             dataIn!![startIndex + 4].toInt() and 0x01
                                          mRx000Setting.invSelectData[dataIndex]!!.selectTarget =
                                             dataIn!![startIndex + 4].toInt() and 0x0E shr 1
                                          mRx000Setting.invSelectData[dataIndex]!!.selectAction =
                                             dataIn!![startIndex + 4].toInt() and 0x70 shr 4
                                          mRx000Setting.invSelectData[dataIndex]!!.selectDelay =
                                             dataIn!![startIndex + 5].toInt()
                                          if (DEBUG) appendToLog(
                                             "found inventory select configuration: " + byteArrayToString(
                                                dataInPayload
                                             )
                                                   + ", selectEnable=" + mRx000Setting.invSelectData[dataIndex]!!.selectEnable
                                                   + ", selectTarget=" + mRx000Setting.invSelectData[dataIndex]!!.selectTarget
                                                   + ", selectAction=" + mRx000Setting.invSelectData[dataIndex]!!.selectAction
                                                   + ", selectDelay=" + mRx000Setting.invSelectData[dataIndex]!!.selectDelay
                                          )
                                       }
                                    }

                                    0x0802 -> {
                                       val dataIndex = mRx000Setting.invSelectIndex
                                       if (dataIndex < mRx000Setting.INVSELECT_MIN || dataIndex > mRx000Setting.INVSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask bank: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.invSelectData[dataIndex]!!.selectMaskBank =
                                             dataIn!![startIndex + 4].toInt() and 0x03
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask bank: " + byteArrayToString(
                                                dataInPayload
                                             )
                                                   + ", selectMaskBank=" + mRx000Setting.invSelectData[dataIndex]!!.selectMaskBank
                                          )
                                       }
                                    }

                                    0x0803 -> {
                                       val dataIndex = mRx000Setting.invSelectIndex
                                       if (dataIndex < mRx000Setting.INVSELECT_MIN || dataIndex > mRx000Setting.INVSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask offset: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.invSelectData[dataIndex]!!.selectMaskOffset =
                                             (dataIn!![startIndex + 4].toInt() and 0x0FF) + (dataIn!![startIndex + 5].toInt() and 0x0FF) * 256 + (dataIn!![startIndex + 6].toInt() and 0x0FF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0x0FF) * 256 * 256 * 256
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask offset: " + byteArrayToString(
                                                dataInPayload
                                             )
                                                   + ", selectMaskOffset=" + mRx000Setting.invSelectData[dataIndex]!!.selectMaskOffset
                                          )
                                       }
                                    }

                                    0x0804 -> {
                                       val dataIndex = mRx000Setting.invSelectIndex
                                       if (dataIndex < mRx000Setting.INVSELECT_MIN || dataIndex > mRx000Setting.INVSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask length: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.invSelectData[dataIndex]!!.selectMaskLength =
                                             dataIn!![startIndex + 4].toInt() and 0x0FF
                                          if (DEBUG) appendToLog("getSelectMaskData with read selectMaskLength = " + mRx000Setting.invSelectData[dataIndex]!!.selectMaskLength)
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask length: " + byteArrayToString(
                                                dataInPayload
                                             )
                                                   + ", selectMaskLength=" + mRx000Setting.invSelectData[dataIndex]!!.selectMaskLength
                                          )
                                       }
                                    }

                                    0x0805, 0x0806, 0x0807, 0x0808, 0x0809, 0x080A, 0x080B, 0x080C -> {
                                       val dataIndex = mRx000Setting.invSelectIndex
                                       if (dataIndex < mRx000Setting.INVSELECT_MIN || dataIndex > mRx000Setting.INVSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask 0-3: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          val maskDataIndex = addressToRead - 0x0805
                                          if (DEBUG) appendToLog(
                                             "Old selectMaskData0_31 = " + byteArrayToString(
                                                mRx000Setting.invSelectData[dataIndex]!!.selectMaskData0_31
                                             )
                                          )
                                          System.arraycopy(
                                             dataIn,
                                             startIndex + 4,
                                             mRx000Setting.invSelectData[dataIndex]!!.selectMaskData0_31,
                                             maskDataIndex * 4,
                                             4
                                          )
                                          if (DEBUG) appendToLog(
                                             "Old selectMaskData0_31 = " + byteArrayToString(
                                                mRx000Setting.invSelectData[dataIndex]!!.selectMaskData0_31
                                             )
                                          )
                                          mRx000Setting.invSelectData[dataIndex]!!.selectMaskDataReady =
                                             (mRx000Setting.invSelectData[dataIndex]!!.selectMaskDataReady.toInt() or (0x01 shl maskDataIndex)).toByte()
                                          if (DEBUG) appendToLog(
                                             "found inventory select mask 0-3: " + byteArrayToString(
                                                dataInPayload
                                             )
                                          )
                                       }
                                    }

                                    0x0900 -> {
                                       if (mRx000Setting.queryTarget != 2) mRx000Setting.queryTarget =
                                          dataIn!![startIndex + 4].toInt() shr 4 and 0x01
                                       mRx000Setting.querySession =
                                          dataIn!![startIndex + 4].toInt() shr 5 and 0x03
                                       mRx000Setting.querySelect =
                                          dataIn!![startIndex + 4].toInt() shr 7 and 0x01 + (dataIn!![startIndex + 5].toInt() and 0x01) * 2
                                       if (DEBUG) appendToLog(
                                          "found query configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", target=" + mRx000Setting.queryTarget + ", session=" + mRx000Setting.querySession + ", select=" + mRx000Setting.querySelect
                                       )
                                    }

                                    0x0901 -> {
                                       mRx000Setting.invAlgo =
                                          dataIn!![startIndex + 4].toInt() and 0x3F
                                       mRx000Setting.matchRep =
                                          (dataIn!![startIndex + 4].toInt() and 0xC0 shr 6) + (dataIn!![startIndex + 5].toInt() and 0x3F) * 4
                                       mRx000Setting.tagSelect =
                                          dataIn!![startIndex + 5].toInt() and 0x40 shr 6
                                       mRx000Setting.noInventory =
                                          dataIn!![startIndex + 5].toInt() and 0x80 shr 7
                                       mRx000Setting.tagRead =
                                          dataIn!![startIndex + 6].toInt() and 0x03
                                       mRx000Setting.tagDelay =
                                          (dataIn!![startIndex + 7].toInt() and 0x03) * 16 + (dataIn!![startIndex + 6].toInt() and 0xF0 shr 4)
                                       mRx000Setting.invModeCompact =
                                          dataIn!![startIndex + 7].toInt() and 0x04
                                       if (DEBUG) appendToLog(
                                          "found inventory configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", algorithm=" + mRx000Setting.invAlgo + ", matchRep=" + mRx000Setting.matchRep + ", tagSelect=" + mRx000Setting.tagSelect + ", noInventory=" + mRx000Setting.noInventory + ", tagRead=" + mRx000Setting.tagRead + ", tagDelay=" + mRx000Setting.tagDelay
                                       )
                                    }

                                    0x0902 -> if (dataIn!![startIndex + 6].toInt() != 0 || dataIn!![startIndex + 7].toInt() != 0) {
                                       if (DEBUG) appendToLog(
                                          "found inventory select, but too big: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    } else {
                                       mRx000Setting.algoSelect =
                                          (dataIn!![startIndex + 4].toInt() and 0xFF) + (dataIn!![startIndex + 5].toInt() and 0xFF) * 256
                                       if (DEBUG) appendToLog("found inventory algorithm select=" + mRx000Setting.algoSelect)
                                    }

                                    0x0903 -> {
                                       val dataIndex = mRx000Setting.algoSelect
                                       if (dataIndex < mRx000Setting.ALGOSELECT_MIN || dataIndex > mRx000Setting.ALGOSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 0: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoStartQ =
                                             dataIn!![startIndex + 4].toInt() and 0x0F
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoMaxQ =
                                             dataIn!![startIndex + 4].toInt() and 0xF0 shr 4
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoMinQ =
                                             dataIn!![startIndex + 5].toInt() and 0x0F
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoMaxRep =
                                             (dataIn!![startIndex + 5].toInt() and 0xF0 shr 4) + (dataIn!![startIndex + 6].toInt() and 0x0F shl 4)
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoHighThres =
                                             dataIn!![startIndex + 6].toInt() and 0xF0 shr 4
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoLowThres =
                                             dataIn!![startIndex + 7].toInt() and 0x0F
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 0: " + byteArrayToString(
                                                dataInPayload
                                             )
                                                   + ", algoStartQ=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoStartQ
                                                   + ", algoMaxQ=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoMaxQ
                                                   + ", algoMinQ=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoMinQ
                                                   + ", algoMaxRep=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoMaxRep
                                                   + ", algoHighThres=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoHighThres
                                                   + ", algoLowThres=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoLowThres
                                          )
                                       }
                                    }

                                    0x0904 -> {
                                       val dataIndex = mRx000Setting.algoSelect
                                       if (dataIndex < mRx000Setting.ALGOSELECT_MIN || dataIndex > mRx000Setting.ALGOSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 1: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoRetry =
                                             dataIn!![startIndex + 4].toInt() and 0x0FF
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 1: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", algoRetry=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoRetry
                                          )
                                       }
                                    }

                                    0x0905 -> {
                                       val dataIndex = mRx000Setting.algoSelect
                                       if (dataIndex < mRx000Setting.ALGOSELECT_MIN || dataIndex > mRx000Setting.ALGOSELECT_MAX) {
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 2: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", but invalid index=" + dataIndex
                                          )
                                       } else {
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 2: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", dataIndex=" + dataIndex + ", algoAbFlip=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoAbFlip + ", algoRunTilZero=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoRunTilZero
                                          )
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoAbFlip =
                                             dataIn!![startIndex + 4].toInt() and 0x01
                                          mRx000Setting.algoSelectedData[dataIndex]!!.algoRunTilZero =
                                             dataIn!![startIndex + 4].toInt() and 0x02 shr 1
                                          if (DEBUG) appendToLog(
                                             "found inventory algo parameter 2: " + byteArrayToString(
                                                dataInPayload
                                             ) + ", algoAbFlip=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoAbFlip + ", algoRunTilZero=" + mRx000Setting.algoSelectedData[dataIndex]!!.algoRunTilZero
                                          )
                                       }
                                    }

                                    0x0907 -> {
                                       mRx000Setting.rssiFilterType =
                                          dataIn!![startIndex + 4].toInt() and 0xF
                                       mRx000Setting.rssiFilterOption =
                                          dataIn!![startIndex + 4].toInt() shr 4 and 0xF
                                    }

                                    0x0908 -> {
                                       mRx000Setting.rssiFilterThreshold1 =
                                          dataIn!![startIndex + 4].toInt()
                                       mRx000Setting.rssiFilterThreshold1 += dataIn!![startIndex + 5].toInt() shl 8
                                       mRx000Setting.rssiFilterThreshold2 =
                                          dataIn!![startIndex + 6].toInt()
                                       mRx000Setting.rssiFilterThreshold2 += dataIn!![startIndex + 7].toInt() shl 8
                                    }

                                    0x0909 -> {
                                       mRx000Setting.rssiFilterCount =
                                          dataIn!![startIndex + 4].toLong()
                                       mRx000Setting.rssiFilterCount += (dataIn!![startIndex + 5].toInt() shl 8).toLong()
                                       mRx000Setting.rssiFilterCount += (dataIn!![startIndex + 6].toInt() shl 16).toLong()
                                       mRx000Setting.rssiFilterCount += (dataIn!![startIndex + 7].toInt() shl 24).toLong()
                                    }

                                    0x0911 -> {
                                       mRx000Setting.matchEnable =
                                          dataIn!![startIndex + 4].toInt() and 0x01
                                       mRx000Setting.matchType =
                                          dataIn!![startIndex + 4].toInt() and 0x02 shr 1
                                       mRx000Setting.matchLength =
                                          (dataIn!![startIndex + 4].toInt() and 0x0FF shr 2) + (dataIn!![startIndex + 5].toInt() and 0x07) * 64
                                       mRx000Setting.matchOffset =
                                          (dataIn!![startIndex + 5].toInt() and 0x0FF shr 3) + (dataIn!![startIndex + 6].toInt() and 0x1F) * 32
                                       if (DEBUG) appendToLog(
                                          "found inventory match configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", selectEnable=" + mRx000Setting.matchEnable + ", matchType=" + mRx000Setting.matchType + ", matchLength=" + mRx000Setting.matchLength + ", matchOffset=" + mRx000Setting.matchOffset
                                       )
                                    }

                                    0x0912, 0x0913, 0x0914, 0x0915, 0x0916, 0x0917, 0x0918, 0x0919, 0x091A, 0x091B, 0x091C, 0x091D, 0x091E, 0x091F, 0x0920, 0x0921 -> {
                                       val maskDataIndex = addressToRead - 0x0912
                                       System.arraycopy(
                                          dataIn,
                                          startIndex + 4,
                                          mRx000Setting.invMatchData0_63,
                                          maskDataIndex * 4,
                                          4
                                       )
                                       mRx000Setting.invMatchDataReady =
                                          mRx000Setting.invMatchDataReady or (0x01 shl maskDataIndex)
                                       if (DEBUG) appendToLog(
                                          "found inventory match Data 0-3: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }

                                    0x0A01 -> {
                                       mRx000Setting.accessRetry =
                                          dataIn!![startIndex + 4].toInt() and 0x0E shr 1
                                       if (DEBUG) appendToLog(
                                          "found access algoRetry: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessRetry=" + mRx000Setting.accessRetry
                                       )
                                    }

                                    0x0A02 -> {
                                       mRx000Setting.accessBank =
                                          dataIn!![startIndex + 4].toInt() and 0x03
                                       mRx000Setting.accessBank2 =
                                          dataIn!![startIndex + 4].toInt() shr 2 and 0x03
                                       if (DEBUG) appendToLog(
                                          "found access bank: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessBank=" + mRx000Setting.accessBank + ", accessBank2=" + mRx000Setting.accessBank2
                                       )
                                    }

                                    0x0A03 -> {
                                       if (mRx000Setting.tagRead != 0) {
                                          mRx000Setting.accessOffset =
                                             (dataIn!![startIndex + 4].toInt() and 0x0FF) + (dataIn!![startIndex + 5].toInt() and 0x0FF) * 256 // + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                          mRx000Setting.accessOffset2 =
                                             (dataIn!![startIndex + 6].toInt() and 0x0FF) + (dataIn!![startIndex + 7].toInt() and 0x0FF) * 256 // + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                       } else {
                                          mRx000Setting.accessOffset =
                                             (dataIn!![startIndex + 4].toInt() and 0x0FF) + (dataIn!![startIndex + 5].toInt() and 0x0FF) * 256 + (dataIn!![startIndex + 6].toInt() and 0x0FF) * 256 * 256 + (dataIn!![startIndex + 7].toInt() and 0x0FF) * 256 * 256 * 256
                                       }
                                       if (DEBUG) appendToLog(
                                          "found access offset: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessOffset=" + mRx000Setting.accessOffset + ", accessOffset2=" + mRx000Setting.accessOffset2
                                       )
                                    }

                                    0x0A04 -> {
                                       mRx000Setting.accessCount =
                                          dataIn!![startIndex + 4].toInt() and 0x0FF
                                       mRx000Setting.accessCount2 =
                                          dataIn!![startIndex + 5].toInt() and 0x0FF
                                       if (DEBUG) appendToLog(
                                          "found access count: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessCount=" + mRx000Setting.accessCount + ", accessCount2=" + mRx000Setting.accessCount2
                                       )
                                    }

                                    0x0A05 -> {
                                       mRx000Setting.accessLockAction =
                                          (dataIn!![startIndex + 4].toInt() and 0x0FF) + (dataIn!![startIndex + 5].toInt() and 0x03) * 256
                                       mRx000Setting.accessLockMask =
                                          (dataIn!![startIndex + 5].toInt() and 0x0FF shr 2) + (dataIn!![startIndex + 6].toInt() and 0x0F) * 64
                                       if (DEBUG) appendToLog(
                                          "found access lock configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessLockAction=" + mRx000Setting.accessLockAction + ", accessLockMask=" + mRx000Setting.accessLockMask
                                       )
                                    }

                                    0x0A08 -> {
                                       mRx000Setting.accessWriteDataSelect =
                                          dataIn!![startIndex + 4].toInt() and 0x07
                                       if (DEBUG) appendToLog(
                                          "found write data select: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", accessWriteDataSelect=" + mRx000Setting.accessWriteDataSelect
                                       )
                                    }

                                    0x0A09, 0x0A0A, 0x0A0B, 0x0A0C, 0x0A0D, 0x0A0E, 0x0A0F, 0x0A10, 0x0A11, 0x0A12, 0x0A13, 0x0A14, 0x0A15, 0x0A16, 0x0A17, 0x0A18 -> {
                                       val maskDataIndex = addressToRead - 0x0A09
                                       var maskDataIndexH = 0
                                       if (mRx000Setting.accessWriteDataSelect != 0) maskDataIndexH =
                                          16
                                       var k = 0
                                       while (k < 4) {
                                          mRx000Setting.accWriteData0_63[(maskDataIndexH + maskDataIndex) * 4 + k] =
                                             dataIn!![startIndex + 7 - k]
                                          k++
                                       }
                                       mRx000Setting.accWriteDataReady =
                                          mRx000Setting.accWriteDataReady or (0x01 shl maskDataIndexH + maskDataIndex)
                                       if (DEBUG) appendToLog("accessWriteData=" + mRx000Setting.accWriteData0_63)
                                       if (DEBUG) appendToLog(
                                          "found access write data 0-3: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }

                                    0x0b60 -> {
                                       mRx000Setting.currentProfile =
                                          dataIn!![startIndex + 4].toInt()
                                       if (DEBUG) appendToLog(
                                          "found current profile: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", profile=" + mRx000Setting.currentProfile
                                       )
                                    }

                                    0x0c01 -> {
                                       mRx000Setting.freqChannelSelect =
                                          dataIn!![startIndex + 4].toInt()
                                       if (DEBUG) appendToLog(
                                          "setFreqChannelSelect: found frequency channel select: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", freqChannelSelect=" + mRx000Setting.freqChannelSelect
                                       )
                                    }

                                    0x0c02 -> {
                                       mRx000Setting.freqChannelConfig =
                                          dataIn!![startIndex + 4].toInt() and 0x01
                                       if (DEBUG) appendToLog(
                                          "found frequency channel configuration: " + byteArrayToString(
                                             dataInPayload
                                          ) + ", channelConfig=" + mRx000Setting.freqChannelConfig
                                       )
                                    }

                                    0x0f00 -> {
                                       mRx000Setting.authenticateSendReply =
                                          if (dataIn!![startIndex + 4].toInt() and 1 != 0) true else false
                                       mRx000Setting.authenticateIncReplyLength =
                                          if (dataIn!![startIndex + 4].toInt() and 2 != 0) true else false
                                       mRx000Setting.authenticateLength =
                                          (dataIn!![startIndex + 5].toInt() and 0xFC shr 3) + (dataIn!![startIndex + 6].toInt() and 0x3F)
                                       if (DEBUG) appendToLog(
                                          "found authenticate configuration: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }

                                    0x0f01, 0x0f02, 0x0f03, 0x0f04 -> {
                                       val maskDataIndex = addressToRead - 0x0f01
                                       System.arraycopy(
                                          dataIn,
                                          startIndex + 4,
                                          mRx000Setting.authMatchData0_63,
                                          maskDataIndex * 4,
                                          4
                                       )
                                       //mRx000Setting.authMatchDataReady |= (0x01 << maskDataIndex);
                                       if (DEBUG) appendToLog(
                                          "found authenticate match Data 0-3: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }

                                    0x0f05 -> {
                                       mRx000Setting.untraceableRange =
                                          dataIn!![startIndex + 4].toInt() and 0x03
                                       mRx000Setting.untraceableUser =
                                          if (dataIn!![startIndex + 4].toInt() and 0x04 != 0) true else false
                                       mRx000Setting.untraceableTid =
                                          dataIn!![startIndex + 4].toInt() and 0x18 shr 3
                                       mRx000Setting.untraceableEpcLength =
                                          (dataIn!![startIndex + 4].toInt() and 0xE0 shr 5) + (dataIn!![startIndex + 5].toInt() and 0x3 shl 3)
                                       mRx000Setting.untraceableEpc =
                                          if (dataIn!![startIndex + 5].toInt() and 4 != 0) true else false
                                       mRx000Setting.untraceableUXpc =
                                          if (dataIn!![startIndex + 5].toInt() and 8 != 0) true else false
                                       if (DEBUG) appendToLog(
                                          "found untraceable configuration: " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }

                                    else -> if (DEBUG) appendToLog(
                                       "found OTHERS with addressToWrite=" + addressToWrite + ", addressToRead=" + addressToRead + ", " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                 }
                                 mRfidDevice!!.mRfidToWrite.removeAt(0)
                                 mRfidDevice!!.sendRfidToWriteSent = 0
                                 mRfidDevice!!.mRfidToWriteRemoved = true
                                 if (DEBUG) appendToLog("mmRfidToWrite remove 7")
                              }
                           }
                           packageFound = true
                           packageType = 2
                           startIndexNew = startIndex + 8
                        } else if (mRfidToReading[startIndex] >= 1 && mRfidToReading[startIndex] <= 4 && expectedLength >= 0 && expectedLength < mRfidToReading.size
                           && (mRfidToReading[startIndex + 2].toInt() == 0 || mRfidToReading[startIndex + 2].toInt() == 1 || mRfidToReading[startIndex + 2] >= 5 && mRfidToReading[startIndex + 2] <= 14)
                           && (mRfidToReading[startIndex + 3].toInt() == 0 || mRfidToReading[startIndex + 3].toInt() == 0x30 || mRfidToReading[startIndex + 3] == 0x80.toByte()) && mRfidToReading[startIndex + 7].toInt() == 0
                        ) {  //if input as command response
                           run {
                              if (DEBUG) appendToLog("loop: decoding 1_4 data")
                              if (mRfidToReadingOffset - startIndex < expectedLength) {
                                 return
                              }
                              dataIn = mRfidToReading
                              val dataInPayload = ByteArray(expectedLength - 4)
                              System.arraycopy(
                                 dataIn,
                                 startIndex + 4,
                                 dataInPayload,
                                 0,
                                 dataInPayload.size
                              )
                              //if ((dataIn[startIndex + 3] == (byte) 0x80 && dataIn[startIndex + 6] == 0 && dataIn[startIndex + 7] == 0) == false) {
                              //    appendToLog("mRx000UplinkHandler(): invalid command response is received with incorrect byte3= " + dataIn[startIndex + 3] + ", byte6=" + dataIn[startIndex + 6] + ", byte7=" + dataIn[startIndex + 7]);
                              //}
                              val packageTypeRead =
                                 dataIn!![startIndex + 2] + (dataIn!![startIndex + 3].toInt() and 0xFF) * 256
                              var dataA = Rx000pkgData()
                              if (packageTypeRead == 6 && dataIn!![startIndex + 1].toInt() and 0x02 != 0 && dataIn!![startIndex + 13].toInt() == 0) {
                                 dataIn!![startIndex + 13] = 0xFF.toByte()
                              }
                              val padCount = dataIn!![startIndex + 1].toInt() and 0x0FF shr 6
                              if (packageTypeRead == 6) {
                                 dataA.dataValues = ByteArray(8 + packageLengthRead * 4 - padCount)
                                 System.arraycopy(
                                    dataIn,
                                    startIndex,
                                    dataA.dataValues,
                                    0,
                                    dataA.dataValues.size
                                 )
                              } else if (packageTypeRead == 0x8005 || packageTypeRead == 5) {
                                 if (dataIn!![startIndex].toInt() == 0x04) {
                                    dataA.dataValues = ByteArray(packageLengthRead)
                                    dataA.decodedPort = dataIn!![startIndex + 6].toInt()
                                 } else dataA.dataValues =
                                    ByteArray(packageLengthRead * 4 - padCount)
                                 System.arraycopy(
                                    dataIn,
                                    startIndex + 8,
                                    dataA.dataValues,
                                    0,
                                    dataA.dataValues.size
                                 )
                              } else {
                                 dataA.dataValues = ByteArray(packageLengthRead * 4)
                                 System.arraycopy(
                                    dataIn,
                                    startIndex + 8,
                                    dataA.dataValues,
                                    0,
                                    dataA.dataValues.size
                                 )
                              }
                              dataA.flags = dataIn!![startIndex + 1].toInt() and 0xFF
                              when (packageTypeRead) {
                                 0x0000, 0x8000 -> if (dataIn!![startIndex].toInt() != 1 && dataIn!![startIndex].toInt() != 2) {
                                    if (DEBUG) appendToLog(
                                       "command COMMAND_BEGIN is found without first byte as 0x01 or 0x02, " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                 } else if (mRfidDevice!!.mRfidToWrite.size == 0) {
                                    if (DEBUG) appendToLog("command COMMAND_BEGIN is found without mRfidToWrite")
                                 } else {
                                    val dataWritten = mRfidDevice!!.mRfidToWrite[0]!!.dataValues
                                    if (dataWritten == null) {
                                    } else if (!(dataWritten[0] == 0x70.toByte() && dataWritten[1].toInt() == 1 && dataWritten[2].toInt() == 0 && dataWritten[3] == 0xF0.toByte())) {
                                       if (DEBUG) appendToLog(
                                          "command COMMAND_BEGIN is found with invalid mRfidToWrite: " + byteArrayToString(
                                             dataWritten
                                          )
                                       )
                                    } else {
                                       var matched = true
                                       run {
                                          var i = 0
                                          while (i < 4) {
                                             if (dataWritten[4 + i] != dataIn!![startIndex + 8 + i]) {
                                                matched = false
                                                break
                                             }
                                             i++
                                          }
                                       }
                                       var lValue: Long = 0
                                       var multipler = 1
                                       var i = 0
                                       while (i < 4) {
                                          lValue += (dataIn!![startIndex + 12 + i].toInt() and 0xFF).toLong() * multipler
                                          multipler *= 256
                                          i++
                                       }
                                       if (!matched) {
                                          if (DEBUG) appendToLog(
                                             "command COMMAND_BEGIN is found with mis-matched command:" + byteArrayToString(
                                                dataWritten
                                             )
                                          )
                                       } else {
                                          mRfidDevice!!.mRfidToWrite.removeAt(0)
                                          mRfidDevice!!.sendRfidToWriteSent = 0
                                          mRfidDevice!!.mRfidToWriteRemoved = true
                                          if (DEBUG) appendToLog("mmRfidToWrite remove 8")
                                          mRfidDevice!!.inventoring = true
                                          val date = Date()
                                          val date_time = date.time
                                          var expected_firmware_ontime_ms = firmware_ontime_ms
                                          if (date_time_ms != 0L) {
                                             val firmware_ontime_ms_difference =
                                                date_time - date_time_ms
                                             if (firmware_ontime_ms_difference > 2000) {
                                                expected_firmware_ontime_ms += firmware_ontime_ms_difference - 2000
                                             }
                                          }
                                          if (lValue < expected_firmware_ontime_ms) {
                                             bFirmware_reset_before = true
                                             if (DEBUG) appendToLogView("command COMMAND_BEGIN --- Firmware reset before !!!")
                                          }
                                          firmware_ontime_ms = lValue
                                          date_time_ms = date_time
                                          if (DEBUG) appendToLog("command COMMAND_BEGIN is found with packageLength=$packageLengthRead, with firmware count=$lValue, date_time=$date_time, expected firmware count=$expected_firmware_ontime_ms")
                                       }
                                    }
                                 }

                                 0x0001, 0x8001 -> {
                                    if (dataIn!![startIndex].toInt() != 1 && dataIn!![startIndex].toInt() != 2) {
                                       if (DEBUG) appendToLog(
                                          "command COMMAND_END is found without first byte as 0x01 or 0x02, " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                       return
                                    } else {
                                       dataA.responseType = HostCmdResponseTypes.TYPE_COMMAND_END
                                       mRfidDevice!!.inventoring = false
                                       if (DEBUG) appendToLog(
                                          "command COMMAND_END is found with packageLength=" + packageLengthRead + ", length = " + dataA.dataValues.size + ", dataValues=" + byteArrayToString(
                                             dataA.dataValues
                                          )
                                       )
                                       if (dataA.dataValues.size >= 8) {
                                          val status =
                                             dataA.dataValues[12 - 8] + dataA.dataValues[13 - 8] * 256
                                          if (status != 0) dataA.decodedError =
                                             "Received COMMAND_END with status=" + String.format(
                                                "0x%X",
                                                status
                                             ) + ", error_port=" + dataA.dataValues[14 - 8]
                                          if (dataA.decodedError != null) if (DEBUG) appendToLog(
                                             dataA.decodedError
                                          )
                                       }
                                    }
                                    val oldSize = mRx000ToRead?.size
                                    mRx000ToRead?.add(dataA)
                                    if (DEBUG) appendToLog("oldSize = " + oldSize + ", after adding 8001 mRx000ToRead.size = " + mRx000ToRead!!.size)
                                    commandOperating = false
                                 }

                                 0x0005, 0x8005 -> if (dataIn!![startIndex].toInt() != 3 && dataIn!![startIndex].toInt() != 4) {
                                    if (DEBUG) appendToLog(
                                       "command 18K6C_INVENTORY is found without first byte as 0x03, 0x04, " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                    return
                                 } else {
                                    if (dataIn!![startIndex].toInt() == 3) {
                                       dataA.responseType =
                                          HostCmdResponseTypes.TYPE_18K6C_INVENTORY
                                       if (true) {
                                          var crcError: Boolean
                                          if (dataA.dataValues.size < 12 + 4) dataA.decodedError =
                                             "Received TYPE_18K6C_INVENTORY with length = " + dataA.dataValues.size + ", data = " + byteArrayToString(
                                                dataA.dataValues
                                             ) else {
                                             val epcLength =
                                                (dataA.dataValues[12].toInt() shr 3) * 2
                                             if (dataA.dataValues.size < 12 + 2 + epcLength + 2) dataA.decodedError =
                                                "Received TYPE_18K6C_INVENTORY with length = " + dataA.dataValues.size + ", data = " + byteArrayToString(
                                                   dataA.dataValues
                                                ) else {
                                                mRfidDevice!!.inventoring = true
                                                var time1 =
                                                   (dataA.dataValues[3].toInt() and 0x00FF).toLong()
                                                time1 = time1 shl 8
                                                time1 =
                                                   time1 or (dataA.dataValues[2].toInt() and 0x00FF).toLong()
                                                time1 = time1 shl 8
                                                time1 = time1
                                                time1 =
                                                   time1 or (dataA.dataValues[1].toInt() and 0x00FF).toLong()
                                                time1 = time1 shl 8
                                                time1 = time1
                                                time1 =
                                                   time1 or (dataA.dataValues[0].toInt() and 0x00FF).toLong()
                                                dataA.decodedTime = time1
                                                dataA.decodedRssi = decodeNarrowBandRSSI(
                                                   dataA.dataValues[13 - 8]
                                                )
                                                var bValue = dataA.dataValues[14 - 8]
                                                bValue = (bValue.toInt() and 0x7F).toByte()
                                                if (bValue.toInt() and 0x40 != 0) bValue =
                                                   (bValue.toInt() or 0x80).toByte()
                                                dataA.decodedPhase = bValue.toInt()
                                                if (true) {
                                                   var iValue =
                                                      dataA.dataValues[14 - 8].toInt() and 0x3F //0x7F;
                                                   val b7 =
                                                      dataA.dataValues[14 - 8].toInt() and 0x80 != 0
                                                   iValue *= 90
                                                   iValue /= 32
                                                   dataA.decodedPhase = iValue
                                                }
                                                dataA.decodedChidx =
                                                   dataA.dataValues[15 - 8].toInt()
                                                dataA.decodedPort = dataA.dataValues[18 - 8].toInt()
                                                var data1_count =
                                                   dataA.dataValues[16 - 8].toInt() and 0xFF
                                                data1_count *= 2
                                                var data2_count =
                                                   dataA.dataValues[17 - 8].toInt() and 0xFF
                                                data2_count *= 2
                                                if (dataA.dataValues.size >= 12 + 2) {
                                                   dataA.decodedPc = ByteArray(2)
                                                   System.arraycopy(
                                                      dataA.dataValues,
                                                      12,
                                                      dataA.decodedPc,
                                                      0,
                                                      dataA.decodedPc!!.size
                                                   )
                                                }
                                                if (dataA.dataValues.size >= 12 + 2 + 2) {
                                                   dataA.decodedEpc =
                                                      ByteArray(dataA.dataValues.size - 12 - 4)
                                                   System.arraycopy(
                                                      dataA.dataValues,
                                                      12 + 2,
                                                      dataA.decodedEpc,
                                                      0,
                                                      dataA.decodedEpc!!.size
                                                   )
                                                   dataA.decodedCrc = ByteArray(2)
                                                   System.arraycopy(
                                                      dataA.dataValues,
                                                      dataA.dataValues.size - 2,
                                                      dataA.decodedCrc,
                                                      0,
                                                      dataA.decodedCrc!!.size
                                                   )
                                                }
                                                if (data1_count != 0 && dataA.dataValues.size - 2 - data1_count - data2_count >= 0) {
                                                   dataA.decodedData1 = ByteArray(data1_count)
                                                   System.arraycopy(
                                                      dataA.dataValues,
                                                      dataA.dataValues.size - 2 - data1_count - data2_count,
                                                      dataA.decodedData1,
                                                      0,
                                                      dataA.decodedData1!!.size
                                                   )
                                                }
                                                if (data2_count != 0 && dataA.dataValues.size - 2 - data2_count >= 0) {
                                                   dataA.decodedData2 = ByteArray(data2_count)
                                                   System.arraycopy(
                                                      dataA.dataValues,
                                                      dataA.dataValues.size - 2 - data2_count,
                                                      dataA.decodedData2,
                                                      0,
                                                      dataA.decodedData2!!.size
                                                   )
                                                }
                                                if (DEBUG) appendToLog(
                                                   "dataValues = " + byteArrayToString(dataA.dataValues) + ", 1 decodedRssi = " + dataA.decodedRssi + ", decodedPhase = " + dataA.decodedPhase + ", decodedChidx = " + dataA.decodedChidx + ", decodedPort = " + dataA.decodedPort + ", decodedPc = " + byteArrayToString(
                                                      dataA.decodedPc
                                                   )
                                                         + ", decodedCrc = " + byteArrayToString(
                                                      dataA.decodedCrc
                                                   ) + ", decodedEpc = " + byteArrayToString(dataA.decodedEpc) + ", decodedData1 = " + byteArrayToString(
                                                      dataA.decodedData1
                                                   ) + ", decodedData2 = " + byteArrayToString(dataA.decodedData2)
                                                )
                                             }
                                          }
                                       }
                                       val oldSize2 = mRx000ToRead?.size
                                       if (oldSize2 != null) {
                                          if(mRx000ToRead != null && oldSize2 >= 0){
                                             mRx000ToRead?.add(oldSize2!!, dataA)
                                             if (DEBUG) appendToLog("oldSize = " + oldSize2 + ", after adding 8005 mRx000ToRead.size = " + mRx000ToRead!!.size)
                                          }
                                       }
                                    } else {
                                       dataA.responseType =
                                          HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT
                                       if (true) {
                                          if (dataA.dataValues.size < 3) dataA.decodedError =
                                             "Received TYPE_18K6C_INVENTORY with length = " + dataA.dataValues.size + ", data = " + byteArrayToString(
                                                dataA.dataValues
                                             ) else {
                                             var index = 0
                                             val dataValuesFull = dataA.dataValues
                                             while (index < dataValuesFull.size) {
                                                dataA.decodedTime = System.currentTimeMillis()
                                                if (dataValuesFull.size >= index + 2) {
                                                   dataA.decodedPc = ByteArray(2)
                                                   System.arraycopy(
                                                      dataValuesFull,
                                                      index,
                                                      dataA.decodedPc,
                                                      0,
                                                      dataA.decodedPc!!.size
                                                   )
                                                   index += 2
                                                } else {
                                                   break
                                                }
                                                val epcLength =
                                                   (dataA.decodedPc!![0].toInt() and 0xFF shr 3) * 2
                                                if (dataValuesFull.size >= index + epcLength) {
                                                   dataA.decodedEpc = ByteArray(epcLength)
                                                   System.arraycopy(
                                                      dataValuesFull,
                                                      index,
                                                      dataA.decodedEpc,
                                                      0,
                                                      epcLength
                                                   )
                                                   index += epcLength
                                                }
                                                if (dataValuesFull.size >= index + 1) {
                                                   dataA.decodedRssi = decodeNarrowBandRSSI(
                                                      dataValuesFull[index]
                                                   )
                                                   index++
                                                }
                                                if (DEBUG) appendToLog(
                                                   (if (dataA.dataValues != null) "mRfidToRead.size() = " + mRfidDevice!!.mRfidToRead.size + ", dataValues = " + byteArrayToString(
                                                      dataA.dataValues
                                                   ) + ", " else "") + "2 decodedRssi = " + dataA.decodedRssi + ", decodedPc = " + byteArrayToString(
                                                      dataA.decodedPc
                                                   ) + ", decodedEpc = " + byteArrayToString(dataA.decodedEpc)
                                                )
                                                if (dataValuesFull.size > index) {
                                                   mRx000ToRead?.add(dataA)
                                                   val iDecodedPortOld = dataA.decodedPort
                                                   dataA = Rx000pkgData()
                                                   dataA.decodedPort = iDecodedPortOld
                                                   dataA.responseType =
                                                      HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT
                                                }
                                             }
                                          }
                                       }
                                       val oldSize3 = mRx000ToRead?.size
                                       mRx000ToRead?.add(dataA)
                                       if (DEBUG) appendToLog("oldSize = " + oldSize3 + ", after adding 8005 mRx000ToRead.size = " + mRx000ToRead!!.size)
                                    }
                                    if (DEBUG) appendToLog(
                                       "command 18K6C_INVENTORY is found with data=" + byteArrayToString(
                                          dataA.dataValues
                                       )
                                    )
                                 }

                                 6 -> {
                                    if (dataIn!![startIndex].toInt() != 1) {
                                       if (DEBUG) appendToLog(
                                          "command 18K6C_TAG_ACCESS is found without first byte as 0x02, " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                       return
                                    } else {
                                       dataA.responseType =
                                          HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS
                                       if (true) {
                                          val dataInPayload_full = ByteArray(expectedLength)
                                          System.arraycopy(
                                             dataIn,
                                             startIndex,
                                             dataInPayload_full,
                                             0,
                                             dataInPayload_full.size
                                          )
                                          if (DEBUG) appendToLog(
                                             "command TYPE_18K6C_TAG_ACCESS is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(
                                                dataInPayload_full
                                             )
                                          )
                                       }
                                       if (true) {
                                          var accessError: Int
                                          var backscatterError: Int
                                          var timeoutError: Boolean
                                          var crcError: Boolean
                                          if (dataA.dataValues.size >= 8 + 12) {
                                             backscatterError = 0
                                             accessError = 0
                                             timeoutError = false
                                             crcError = false
                                             if (dataA.dataValues[1].toInt() and 8 != 0) crcError =
                                                true else if (dataA.dataValues[1].toInt() and 4 != 0) timeoutError =
                                                true else if (dataA.dataValues[1].toInt() and 2 != 0) backscatterError =
                                                dataA.dataValues[13].toInt() and 0xFF else if (dataA.dataValues[1].toInt() and 1 != 0 && dataA.dataValues.size >= 8 + 12 + 4) accessError =
                                                (dataA.dataValues[20].toInt() and 0xFF) + (dataA.dataValues[21].toInt() and 0xFF) * 256
                                             val dataRead = ByteArray(dataA.dataValues.size - 20)
                                             System.arraycopy(
                                                dataA.dataValues,
                                                20,
                                                dataRead,
                                                0,
                                                dataRead.size
                                             )
                                             if (backscatterError == 0 && accessError == 0 && !timeoutError && !crcError) {
                                                if (dataA.dataValues[12] == 0xC3.toByte() || dataA.dataValues[12] == 0xC4.toByte() || dataA.dataValues[12] == 0xC5.toByte() || dataA.dataValues[12] == 0xD5.toByte() || dataA.dataValues[12] == 0xE2.toByte()) dataA.decodedResult =
                                                   "" else if (dataA.dataValues[12] == 0xC2.toByte() || dataA.dataValues[12] == 0xE0.toByte()) dataA.decodedResult =
                                                   byteArrayToString(dataRead) else dataA.decodedError =
                                                   "Received TYPE_18K6C_TAG_ACCESS with unhandled command = " + dataA.dataValues[12].toString() + ", data = " + byteArrayToString(
                                                      dataA.dataValues
                                                   )
                                             } else {
                                                dataA.decodedError =
                                                   "Received TYPE_18K6C_TAG_ACCESS with Error "
                                                if (crcError) dataA.decodedError += "crcError=$crcError, "
                                                if (timeoutError) dataA.decodedError += "timeoutError=$timeoutError, "
                                                if (backscatterError != 0) {
                                                   dataA.decodedError += "backscatterError:"
                                                   var strErrorMessage = backscatterError.toString()
                                                   when (backscatterError) {
                                                      3 -> strErrorMessage =
                                                         "Specified memory location does not exist or the PC value is not supported by the tag"

                                                      4 -> strErrorMessage =
                                                         "Specified memory location is locked and/or permalocked and is not writeable"

                                                      0x0B -> strErrorMessage =
                                                         "Tag has insufficient power to perform the memory write"

                                                      0x0F -> strErrorMessage =
                                                         "Tag does not support error-specific codes"

                                                      else -> {}
                                                   }
                                                   dataA.decodedError += "$strErrorMessage, "
                                                }
                                                if (accessError != 0) {
                                                   dataA.decodedError += "accessError: "
                                                   var strErrorMessage = accessError.toString()
                                                   when (accessError) {
                                                      0x01 -> strErrorMessage =
                                                         "Read after write verify failed"

                                                      0x02 -> strErrorMessage =
                                                         "Problem transmitting tag command"

                                                      0x03 -> strErrorMessage =
                                                         "CRC error on tag response to a write"

                                                      0x04 -> strErrorMessage =
                                                         "CRC error on the read packet when verifying the write"

                                                      0x05 -> strErrorMessage =
                                                         "Maximum retries on the write exceeded"

                                                      0x06 -> strErrorMessage =
                                                         "Failed waiting for read data from tag, possible timeout"

                                                      0x07 -> strErrorMessage =
                                                         "Failure requesting a new tag handle"

                                                      0x09 -> strErrorMessage = "Out of retries"
                                                      0x0A -> strErrorMessage =
                                                         "Error waiting for tag response, possible timeout"

                                                      0x0B -> strErrorMessage =
                                                         "CRC error on tag response to a kill"

                                                      0x0C -> strErrorMessage =
                                                         "Problem transmitting 2nd half of tag kill"

                                                      0x0D -> strErrorMessage =
                                                         "Tag responded with an invalid handle on first kill command"

                                                      else -> {}
                                                   }
                                                   dataA.decodedError += "$strErrorMessage, "
                                                }
                                                dataA.decodedError += "data = " + byteArrayToString(
                                                   dataA.dataValues
                                                )
                                             }
                                          } else {
                                             dataA.decodedError =
                                                "Received TYPE_18K6C_TAG_ACCESS with length = " + dataA.dataValues.size + ", data = " + byteArrayToString(
                                                   dataA.dataValues
                                                )
                                          }
                                       }
                                    }
                                    val oldSize4 = mRx000ToRead?.size
                                    mRx000ToRead?.add(dataA)
                                    if (DEBUG) appendToLog("oldSize = " + oldSize4 + ", after adding 0006 mRx000ToRead.size = " + mRx000ToRead!!.size)
                                    if (DEBUG) {
                                       appendToLog(
                                          "mRx000UplinkHandler(): package read = " + byteArrayToString(
                                             dataA.dataValues
                                          )
                                       )
                                    }
                                 }

                                 0x0007, 0x8007 -> {
                                    if (dataIn!![startIndex].toInt() != 1 && dataIn!![startIndex].toInt() != 2) {
                                       if (DEBUG) appendToLog(
                                          "command TYPE_ANTENNA_CYCLE_END is found without first byte as 0x01 or 0x02, " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                       return
                                    } else {
                                       dataA.responseType =
                                          HostCmdResponseTypes.TYPE_ANTENNA_CYCLE_END
                                       if (DEBUG) appendToLog(
                                          "command TYPE_ANTENNA_CYCLE_END is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }
                                    mRx000ToRead?.add(dataA)
                                 }

                                 0x000E -> {
                                    if (dataIn!![startIndex].toInt() != 1 && dataIn!![startIndex].toInt() != 2) {
                                       if (DEBUG) appendToLog(
                                          "command TYPE_COMMAND_ACTIVE is found without first byte as 0x01 or 0x02, " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                       return
                                    } else {
                                       dataA.responseType = HostCmdResponseTypes.TYPE_COMMAND_ACTIVE
                                       if (DEBUG) appendToLog(
                                          "command TYPE_COMMAND_ACTIVE is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(
                                             dataInPayload
                                          )
                                       )
                                    }
                                    mRx000ToRead?.add(dataA)
                                 }

                                 0x3005 -> {
                                    var address =
                                       (dataIn!![startIndex + 8].toInt() and 0xFF) + (dataIn!![startIndex + 9].toInt() and 0xFF) * 256
                                    if (address == 0x450) {
                                       mRx000MbpSetting.rxGain =
                                          (dataIn!![startIndex + 10].toInt() and 0xFF) + (dataIn!![startIndex + 11].toInt() and 0xFF) * 256
                                    }
                                    address =
                                       (dataIn!![startIndex + 8].toInt() and 0xFF) + (dataIn!![startIndex + 9].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 10].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 11].toInt() and 0xFF) * 256 * 256 * 256
                                    when (address) {
                                       0x02 -> {
                                          //                                                    dataIn[startIndex + 12] = 3;
                                          mRx000OemSetting.countryCode =
                                             (dataIn!![startIndex + 12].toInt() and 0xFF) + (dataIn!![startIndex + 13].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 14].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256 * 256 * 256
                                          if (DEBUG) appendToLog("countryCode = " + mRx000OemSetting.countryCode)
                                       }

                                       0x04, 0x05, 0x06, 0x07 -> System.arraycopy(
                                          dataIn,
                                          startIndex + 12,
                                          mRx000OemSetting.serialNumber,
                                          4 * (address - 4),
                                          4
                                       )

                                       0x08, 0x09, 0x0A -> System.arraycopy(
                                          dataIn,
                                          startIndex + 12,
                                          mRx000OemSetting.productserialNumber,
                                          4 * (address - 8),
                                          4
                                       )

                                       0x0B -> {
                                          System.arraycopy(
                                             dataIn,
                                             startIndex + 12,
                                             mRx000OemSetting.productserialNumber,
                                             4 * (address - 8),
                                             4
                                          )
                                          if (dataIn!![startIndex + 12].toInt() == 0 && dataIn!![startIndex + 13].toInt() == 0 && dataIn!![startIndex + 14].toInt() == 0 && dataIn!![startIndex + 15].toInt() == 0) {
                                             mRx000OemSetting.versionCode = 0
                                          } else if (dataIn!![startIndex + 12].toInt() == 0x20 && dataIn!![startIndex + 13].toInt() == 0x17 && dataIn!![startIndex + 14].toInt() == 0) {
                                             mRx000OemSetting.versionCode =
                                                (dataIn!![startIndex + 14].toInt() and 0xFF) + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256
                                          }
                                          if (DEBUG) appendToLog("versionCode = " + mRx000OemSetting.versionCode)
                                       }

                                       0x8E -> {
                                          /*dataIn[startIndex + 12] = 0x2A; //0x4F;
                                                dataIn[startIndex + 13] = 0x2A; //0x46;
                                                dataIn[startIndex + 14] = 0x4E; //0x41; //0x43;
                                                dataIn[startIndex + 15] = 0x5A; //0x53; //0x41; */
                                          if (dataIn!![startIndex + 12].toInt() == 0 || dataIn!![startIndex + 13].toInt() == 0 || dataIn!![startIndex + 14].toInt() == 0 || dataIn!![startIndex + 15].toInt() == 0) {
                                             mRx000OemSetting.spcialCountryVersion = ""
                                          } else {
                                             mRx000OemSetting.spcialCountryVersion = Char(
                                                dataIn!![startIndex + 15].toUShort()
                                             ).toString() + Char(
                                                dataIn!![startIndex + 14].toUShort()
                                             ) + Char(
                                                dataIn!![startIndex + 13].toUShort()
                                             ) + Char(
                                                dataIn!![startIndex + 12].toUShort()
                                             )
                                          }
                                          val dataInPart = ByteArray(4)
                                          System.arraycopy(
                                             dataIn,
                                             startIndex + 12,
                                             dataInPart,
                                             0,
                                             dataInPart.size
                                          )
                                          if (DEBUG) appendToLog(
                                             "spcialCountryVersion = " + mRx000OemSetting.spcialCountryVersion + ", data = " + byteArrayToString(
                                                dataInPart
                                             )
                                          )
                                       }

                                       0x8F -> {
                                          //dataIn[startIndex + 12] = (byte)0xAA;
                                          mRx000OemSetting.freqModifyCode =
                                             (dataIn!![startIndex + 12].toInt() and 0xFF) + (dataIn!![startIndex + 13].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 14].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256 * 256 * 256
                                          if (DEBUG) appendToLog("freqModifyCode = " + mRx000OemSetting.freqModifyCode)
                                       }

                                       else -> {}
                                    }
                                    /*                                            if (address >= 4 && address <= 7) {
                                            for (int i = 0; i < 4; i++) {
                                                mRx000OemSetting.serialNumber[(address - 4) * 4 + i] = dataIn[startIndex + 12 + i];
                                            }
                                        }*/if (DEBUG) appendToLog(
                                       "command OEMCFG_READ is found with address = " + address + ", packageLength=" + packageLengthRead + ", " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                 }

                                 0x3007 -> {
                                    var address =
                                       (dataIn!![startIndex + 8].toInt() and 0xFF) + (dataIn!![startIndex + 9].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 10].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 11].toInt() and 0xFF) * 256 * 256 * 256
                                    when (address) {
                                       0x02 -> {
                                          mRx000OemSetting.countryCode =
                                             (dataIn!![startIndex + 12].toInt() and 0xFF) + (dataIn!![startIndex + 13].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 14].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256 * 256 * 256
                                          if (DEBUG) appendToLog("countryCode = " + mRx000OemSetting.countryCode)
                                       }

                                       0x04, 0x05, 0x06, 0x07 -> System.arraycopy(
                                          dataIn,
                                          startIndex + 12,
                                          mRx000OemSetting.serialNumber,
                                          4 * (address - 4),
                                          4
                                       )

                                       0x08, 0x09, 0x0A -> System.arraycopy(
                                          dataIn,
                                          startIndex + 12,
                                          mRx000OemSetting.productserialNumber,
                                          4 * (address - 8),
                                          4
                                       )

                                       0x0B -> {
                                          System.arraycopy(
                                             dataIn,
                                             startIndex + 12,
                                             mRx000OemSetting.productserialNumber,
                                             4 * (address - 8),
                                             4
                                          )
                                          if (dataIn!![startIndex + 12].toInt() == 0 && dataIn!![startIndex + 13].toInt() == 0 && dataIn!![startIndex + 14].toInt() == 0 && dataIn!![startIndex + 15].toInt() == 0) {
                                             mRx000OemSetting.versionCode = 0
                                          } else if (dataIn!![startIndex + 12].toInt() == 0x20 && dataIn!![startIndex + 13].toInt() == 0x17 && dataIn!![startIndex + 14].toInt() == 0) {
                                             mRx000OemSetting.versionCode =
                                                (dataIn!![startIndex + 14].toInt() and 0xFF) + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256
                                          }
                                          if (DEBUG) appendToLog("versionCode = " + mRx000OemSetting.versionCode)
                                       }

                                       0x8E -> {
                                          if (dataIn!![startIndex + 12].toInt() == 0 || dataIn!![startIndex + 13].toInt() == 0 || dataIn!![startIndex + 14].toInt() == 0 || dataIn!![startIndex + 15].toInt() == 0) {
                                             mRx000OemSetting.spcialCountryVersion = ""
                                          } else {
                                             mRx000OemSetting.spcialCountryVersion = Char(
                                                dataIn!![startIndex + 15].toUShort()
                                             ).toString() + Char(
                                                dataIn!![startIndex + 14].toUShort()
                                             ) + Char(
                                                dataIn!![startIndex + 13].toUShort()
                                             ) + Char(
                                                dataIn!![startIndex + 12].toUShort()
                                             )
                                          }
                                          val dataInPart = ByteArray(4)
                                          System.arraycopy(
                                             dataIn,
                                             startIndex + 12,
                                             dataInPart,
                                             0,
                                             dataInPart.size
                                          )
                                          if (DEBUG) appendToLog(
                                             "spcialCountryVersion = " + mRx000OemSetting.spcialCountryVersion + ", data = " + byteArrayToString(
                                                dataInPart
                                             )
                                          )
                                       }

                                       0x8F -> {
                                          mRx000OemSetting.freqModifyCode =
                                             (dataIn!![startIndex + 12].toInt() and 0xFF) + (dataIn!![startIndex + 13].toInt() and 0xFF) * 256 + (dataIn!![startIndex + 14].toInt() and 0xFF) * 256 * 256 + (dataIn!![startIndex + 15].toInt() and 0xFF) * 256 * 256 * 256
                                          if (DEBUG) appendToLog("freqModifyCode = " + mRx000OemSetting.freqModifyCode)
                                       }

                                       else -> {}
                                    }
                                    if (DEBUG) appendToLog(
                                       "command OEMCFG_READ is found with address = " + address + ", packageLength=" + packageLengthRead + ", " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                 }

                                 0x3008 -> {
                                    if (DEBUG) appendToLog(
                                       "Hello123: RFID_PACKET_TYPE_ENG_RSSI S is found: " + byteArrayToString(
                                          dataInPayload
                                       )
                                    )
                                    if (dataIn!![startIndex + 8].toInt() and 0x02 != 0) {
                                       mRx000EngSetting.narrowRSSI =
                                          (dataIn!![startIndex + 28].toInt() and 0xFF) + (dataIn!![startIndex + 29].toInt() and 0xFF) * 256
                                       mRx000EngSetting.wideRSSI =
                                          (dataIn!![startIndex + 30].toInt() and 0xFF) + (dataIn!![startIndex + 31].toInt() and 0xFF) * 256
                                       if (DEBUG) appendToLog(
                                          "Hello123: narrorRSSI = " + String.format(
                                             "%04X",
                                             mRx000EngSetting.narrowRSSI
                                          ) + ", wideRSSI = " + String.format(
                                             "%04X",
                                             mRx000EngSetting.wideRSSI
                                          )
                                       )
                                    }
                                 }

                                 else -> if (DEBUG) appendToLog(
                                    "command OTHERS is found: " + byteArrayToString(
                                       dataInPayload
                                    ) + ", with packagelength=" + packageLengthRead + ", packageTypeRead=" + packageTypeRead
                                 )
                              }
                              packageFound = true
                              packageType = 3
                              startIndexNew = startIndex + expectedLength
                           }
                        }
                     }
                  }
                  if (packageFound) {
                     packageFound = false
                     if (DEBUG) appendToLog("mRx000UplinkHandler(): packageFound $packageType with mRfidToReadingOffset=$mRfidToReadingOffset, startIndexOld= $startIndexOld, startIndex= $startIndex, startIndexNew=$startIndexNew")
                     if (startIndex != startIndexOld) {
                        val unhandledBytes = ByteArray(startIndex - startIndexOld)
                        System.arraycopy(
                           mRfidToReading,
                           startIndexOld,
                           unhandledBytes,
                           0,
                           unhandledBytes.size
                        )
                        if (DEBUG) appendToLog(
                           "mRx000UplinkHandler(): packageFound with invalid unused data: " + unhandledBytes.size + ", " + byteArrayToString(
                              unhandledBytes
                           )
                        )
                        invalidUpdata++
                     }
                     if (false) {
                        val usedBytes = ByteArray(startIndexNew - startIndex)
                        System.arraycopy(mRfidToReading, startIndex, usedBytes, 0, usedBytes.size)
                        if (DEBUG) appendToLog(
                           "mRx000UplinkHandler(): used data = " + usedBytes.size + ", " + byteArrayToString(
                              usedBytes
                           )
                        )
                     }
                     val mRfidToReadingNew = ByteArray(RFID_READING_BUFFERSIZE)
                     System.arraycopy(
                        mRfidToReading,
                        startIndexNew,
                        mRfidToReadingNew,
                        0,
                        mRfidToReadingOffset - startIndexNew
                     )
                     mRfidToReading = mRfidToReadingNew
                     mRfidToReadingOffset -= startIndexNew
                     startIndex = 0
                     startIndexNew = 0
                     startIndexOld = 0
                     if (mRfidToReadingOffset != 0) {
                        val remainedBytes = ByteArray(mRfidToReadingOffset)
                        System.arraycopy(mRfidToReading, 0, remainedBytes, 0, remainedBytes.size)
                        if (DEBUG) appendToLog(
                           "mRx000UplinkHandler(): moved with remained bytes=" + byteArrayToString(
                              remainedBytes
                           )
                        )
                     }
                     //}
                  } else {
                     startIndex++
                  }
               }
               if (startIndex != 0 && mRfidToReadingOffset != 0) if (DEBUG) appendToLog("mRx000UplinkHandler(): exit while(-8) loop with startIndex = " + startIndex + (if (startIndex == 0) "" else "(NON-ZERO)") + ", mRfidToReadingOffset=" + mRfidToReadingOffset)
            }
         }
         if (mRfidToReadingOffset == startIndexNew && mRfidToReadingOffset != 0) {
            val unusedData = ByteArray(mRfidToReadingOffset)
            System.arraycopy(mRfidToReading, 0, unusedData, 0, unusedData.size)
            if (DEBUG) appendToLog(
               "mRx000UplinkHandler(): Ending with invaid unused data: " + mRfidToReadingOffset + ", " + byteArrayToString(
                  unusedData
               )
            )
            mRfidToReading = ByteArray(RFID_READING_BUFFERSIZE)
            mRfidToReadingOffset = 0
         }
         if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): END")
         bRx000ToReading = false
      }

      fun turnOn(onStatus: Boolean): Boolean {
         val cs108RfidData = Cs108RfidData()
         return if (onStatus) {
            cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_POWER_ON
            cs108RfidData.waitUplinkResponse = false
            clearTempDataIn_request = true
            addRfidToWrite(cs108RfidData)
            true
         } else if (!onStatus) {
            cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_POWER_OFF
            cs108RfidData.waitUplinkResponse = false
            clearTempDataIn_request = true
            addRfidToWrite(cs108RfidData)
            true
         } else {
            false
         }
      }

      fun sendControlCommand(controlCommands: ControlCommands): Boolean {
         var msgBuffer: ByteArray? = byteArrayOf(0x40.toByte(), 6, 0, 0, 0, 0, 0, 0)
         var needResponse = false
         if (!isBleConnected) return false
         when (controlCommands) {
            ControlCommands.CANCEL -> {
               msgBuffer!![1] = 1
               commandOperating = false
            }

            ControlCommands.SOFTRESET -> {
               msgBuffer!![1] = 2
               needResponse = true
            }

            ControlCommands.ABORT -> {
               msgBuffer!![1] = 3
               needResponse = true
               commandOperating = false
            }

            ControlCommands.PAUSE -> msgBuffer!![1] = 4
            ControlCommands.RESUME -> msgBuffer!![1] = 5
            ControlCommands.GETSERIALNUMBER -> {
               msgBuffer = byteArrayOf(0xC0.toByte(), 0x06, 0, 0, 0, 0, 0, 0)
               needResponse = true
            }

            ControlCommands.RESETTOBOOTLOADER -> {
               msgBuffer!![1] = 7
               needResponse = true
            }

            else -> {
               msgBuffer!![1] = 1
               commandOperating = false
            }
         }
         return if (msgBuffer == null) {
            if (DEBUG) appendToLog("Invalid control commands")
            false
         } else {
            clearTempDataIn_request = true
            val cs108RfidData = Cs108RfidData()
            cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_COMMAND
            cs108RfidData.dataValues = msgBuffer
            if (needResponse) {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRx000ToWrite");
               cs108RfidData.waitUplinkResponse = needResponse
               addRfidToWrite(cs108RfidData)
               //                    mRx000ToWrite.add(cs108RfidData);
            } else {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRfidToWrite");
               cs108RfidData.waitUplinkResponse = needResponse
               addRfidToWrite(cs108RfidData)
            }
            if (controlCommands == ControlCommands.ABORT) aborting = true
            true
         }
      }

      fun sendHostRegRequestHST_RFTC_FRQCH_DESC_PLLDIVMULT(freqChannel: Int): Boolean {
         var freqChannel = freqChannel
         val fccFreqTable = longArrayOf(
            0x00180E4F,  //915.75 MHz
            0x00180E4D,  //915.25 MHz
            0x00180E1D,  //903.25 MHz
            0x00180E7B,  //926.75 MHz
            0x00180E79,  //926.25 MHz
            0x00180E21,  //904.25 MHz
            0x00180E7D,  //927.25 MHz
            0x00180E61,  //920.25 MHz
            0x00180E5D,  //919.25 MHz
            0x00180E35,  //909.25 MHz
            0x00180E5B,  //918.75 MHz
            0x00180E57,  //917.75 MHz
            0x00180E25,  //905.25 MHz
            0x00180E23,  //904.75 MHz
            0x00180E75,  //925.25 MHz
            0x00180E67,  //921.75 MHz
            0x00180E4B,  //914.75 MHz
            0x00180E2B,  //906.75 MHz
            0x00180E47,  //913.75 MHz
            0x00180E69,  //922.25 MHz
            0x00180E3D,  //911.25 MHz
            0x00180E3F,  //911.75 MHz
            0x00180E1F,  //903.75 MHz
            0x00180E33,  //908.75 MHz
            0x00180E27,  //905.75 MHz
            0x00180E41,  //912.25 MHz
            0x00180E29,  //906.25 MHz
            0x00180E55,  //917.25 MHz
            0x00180E49,  //914.25 MHz
            0x00180E2D,  //907.25 MHz
            0x00180E59,  //918.25 MHz
            0x00180E51,  //916.25 MHz
            0x00180E39,  //910.25 MHz
            0x00180E3B,  //910.75 MHz
            0x00180E2F,  //907.75 MHz
            0x00180E73,  //924.75 MHz
            0x00180E37,  //909.75 MHz
            0x00180E5F,  //919.75 MHz
            0x00180E53,  //916.75 MHz
            0x00180E45,  //913.25 MHz
            0x00180E6F,  //923.75 MHz
            0x00180E31,  //908.25 MHz
            0x00180E77,  //925.75 MHz
            0x00180E43,  //912.75 MHz
            0x00180E71,  //924.25 MHz
            0x00180E65,  //921.25 MHz
            0x00180E63,  //920.75 MHz
            0x00180E6B,  //922.75 MHz
            0x00180E1B,  //902.75 MHz
            0x00180E6D
         )
         val msgBuffer = byteArrayOf(0x70.toByte(), 1, 3, 0x0C, 0, 0, 0, 0)
         if (freqChannel >= 50) {
            freqChannel = 49
         }
         val freqData = fccFreqTable[freqChannel]
         msgBuffer[4] = (freqData % 256).toByte()
         msgBuffer[5] = ((freqData shr 8) % 256).toByte()
         msgBuffer[6] = ((freqData shr 16) % 256).toByte()
         msgBuffer[7] = ((freqData shr 24) % 256).toByte()
         return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, true, msgBuffer)
      }

      var bLowPowerStandby = false
      fun setPwrManagementMode(bLowPowerStandby: Boolean): Boolean {
         if (false) appendToLog("pwrMgmtStatus: setPwrManagementMode($bLowPowerStandby)")
         if (!bLowPowerStandby) return true //for testing if setPwrManagementMode(false) is needed
         if (this.bLowPowerStandby == bLowPowerStandby) return true
         var result = mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.writeMAC(
            0x200,
            (if (bLowPowerStandby) 1 else 0).toLong()
         )
         if (result) {
            result =
               mRfidDevice!!.mRfidReaderChip!!.sendHostRegRequestHST_CMD(HostCommands.CMD_SETPWRMGMTCFG)
            this.bLowPowerStandby = bLowPowerStandby
            mRfidDevice!!.mRfidReaderChip!!.mRx000Setting.getPwrMgmtStatus()
         }
         return result
      }

      fun sendHostRegRequestHST_CMD(hostCommand: HostCommands?): Boolean {
         var hostCommandData: Long = -1
         when (hostCommand) {
            HostCommands.CMD_WROEM -> hostCommandData = 0x02
            HostCommands.CMD_RDOEM -> hostCommandData = 0x03
            HostCommands.CMD_ENGTEST -> hostCommandData = 0x04
            HostCommands.CMD_MBPRDREG -> hostCommandData = 0x05
            HostCommands.CMD_MBPWRREG -> hostCommandData = 0x06
            HostCommands.CMD_18K6CINV -> hostCommandData = 0x0F
            HostCommands.CMD_18K6CREAD -> hostCommandData = 0x10
            HostCommands.CMD_18K6CWRITE -> hostCommandData = 0x11
            HostCommands.CMD_18K6CLOCK -> hostCommandData = 0x12
            HostCommands.CMD_18K6CKILL -> hostCommandData = 0x13
            HostCommands.CMD_SETPWRMGMTCFG -> hostCommandData = 0x14
            HostCommands.CMD_UPDATELINKPROFILE -> hostCommandData = 0x19
            HostCommands.CMD_18K6CBLOCKWRITE -> hostCommandData = 0x1F
            HostCommands.CMD_CHANGEEAS -> hostCommandData = 0x26
            HostCommands.CMD_GETSENSORDATA -> hostCommandData = 0x3b
            HostCommands.CMD_18K6CAUTHENTICATE -> hostCommandData = 0x50
            HostCommands.CMD_READBUFFER -> hostCommandData = 0x51
            HostCommands.CMD_UNTRACEABLE -> hostCommandData = 0x52
            HostCommands.CMD_FDM_RDMEM -> hostCommandData = 0x53
            HostCommands.CMD_FDM_WRMEM -> hostCommandData = 0x54
            HostCommands.CMD_FDM_AUTH -> hostCommandData = 0x55
            HostCommands.CMD_FDM_GET_TEMPERATURE -> hostCommandData = 0x56
            HostCommands.CMD_FDM_START_LOGGING -> hostCommandData = 0x57
            HostCommands.CMD_FDM_STOP_LOGGING -> hostCommandData = 0x58
            HostCommands.CMD_FDM_WRREG -> hostCommandData = 0x59
            HostCommands.CMD_FDM_RDREG -> hostCommandData = 0x5A
            HostCommands.CMD_FDM_DEEP_SLEEP -> hostCommandData = 0x5B
            HostCommands.CMD_FDM_OPMODE_CHECK -> hostCommandData = 0x5C
            HostCommands.CMD_FDM_INIT_REGFILE -> hostCommandData = 0x5d
            HostCommands.CMD_FDM_LED_CTRL -> hostCommandData = 0x5e
            else -> {}
         }
         return if (hostCommandData == -1L) {
            false
         } else {
            commandOperating = true
            val msgBuffer = byteArrayOf(0x70.toByte(), 1, 0, 0xf0.toByte(), 0, 0, 0, 0)
            msgBuffer[4] = (hostCommandData % 256).toByte()
            msgBuffer[5] = ((hostCommandData shr 8) % 256).toByte()
            msgBuffer[6] = ((hostCommandData shr 16) % 256).toByte()
            msgBuffer[7] = ((hostCommandData shr 24) % 256).toByte()
            sendHostRegRequest(HostRegRequests.HST_CMD, true, msgBuffer)
         }
      }

      var macAccessHistory = ArrayList<ByteArray?>()
      fun bifMacAccessHistoryData(msgBuffer: ByteArray): Boolean {
         if (!sameCheck) return false
         if (msgBuffer.size != 8) return false
         return if (msgBuffer[0] != 0x70.toByte() || msgBuffer[1].toInt() != 1) false else msgBuffer[2]
            .toInt() != 0 || msgBuffer[3] != 0xF0.toByte()
      }

      fun findMacAccessHistory(msgBuffer: ByteArray?): Int {
         var i = -1
         i = 0
         while (i < macAccessHistory.size) {

//                appendToLog("macAccessHistory(" + i + ")=" + byteArrayToString(macAccessHistory.get(i)));
            if (Arrays.equals(macAccessHistory[i], msgBuffer)) break
            i++
         }
         if (i == macAccessHistory.size) i = -1
         if (i >= 0) appendToLog(
            "macAccessHistory: returnValue = " + i + ", msgBuffer=" + byteArrayToString(
               msgBuffer
            )
         )
         return i
      }

      fun addMacAccessHistory(msgBuffer: ByteArray?) {
         val DEBUG = false
         val msgBuffer4 = Arrays.copyOf(msgBuffer, 4)
         for (i in macAccessHistory.indices) {
            val macAccessHistory4 = Arrays.copyOf(macAccessHistory[i], 4)
            if (Arrays.equals(msgBuffer4, macAccessHistory4)) {
               if (DEBUG) appendToLog(
                  "macAccessHistory: deleted old record=" + byteArrayToString(
                     macAccessHistory4
                  )
               )
               macAccessHistory.removeAt(i)
               break
            }
         }
         if (DEBUG) appendToLog("macAccessHistory: added msgbuffer=" + byteArrayToString(msgBuffer))
         macAccessHistory.add(msgBuffer)
      }

      fun sendHostRegRequest(
         hostRegRequests: HostRegRequests?,
         writeOperation: Boolean,
         msgBuffer: ByteArray?
      ): Boolean {
         var needResponse = false
         var validRequest = false
         if (!isBleConnected) return false
         addMacAccessHistory(msgBuffer)
         when (hostRegRequests) {
            HostRegRequests.MAC_OPERATION, HostRegRequests.HST_ANT_CYCLES, HostRegRequests.HST_ANT_DESC_SEL, HostRegRequests.HST_ANT_DESC_CFG, HostRegRequests.MAC_ANT_DESC_STAT, HostRegRequests.HST_ANT_DESC_PORTDEF, HostRegRequests.HST_ANT_DESC_DWELL, HostRegRequests.HST_ANT_DESC_RFPOWER, HostRegRequests.HST_ANT_DESC_INV_CNT -> validRequest =
               true

            HostRegRequests.HST_TAGMSK_DESC_SEL, HostRegRequests.HST_TAGMSK_DESC_CFG, HostRegRequests.HST_TAGMSK_BANK, HostRegRequests.HST_TAGMSK_PTR, HostRegRequests.HST_TAGMSK_LEN, HostRegRequests.HST_TAGMSK_0_3 -> validRequest =
               true

            HostRegRequests.HST_QUERY_CFG, HostRegRequests.HST_INV_CFG, HostRegRequests.HST_INV_SEL, HostRegRequests.HST_INV_ALG_PARM_0, HostRegRequests.HST_INV_ALG_PARM_1, HostRegRequests.HST_INV_ALG_PARM_2, HostRegRequests.HST_INV_ALG_PARM_3, HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG, HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, HostRegRequests.HST_INV_RSSI_FILTERING_COUNT, HostRegRequests.HST_INV_EPC_MATCH_CFG, HostRegRequests.HST_INV_EPCDAT_0_3 -> validRequest =
               true

            HostRegRequests.HST_TAGACC_DESC_CFG, HostRegRequests.HST_TAGACC_BANK, HostRegRequests.HST_TAGACC_PTR, HostRegRequests.HST_TAGACC_CNT, HostRegRequests.HST_TAGACC_LOCKCFG, HostRegRequests.HST_TAGACC_ACCPWD, HostRegRequests.HST_TAGACC_KILLPWD, HostRegRequests.HST_TAGWRDAT_SEL, HostRegRequests.HST_TAGWRDAT_0 -> validRequest =
               true

            HostRegRequests.HST_RFTC_CURRENT_PROFILE, HostRegRequests.HST_RFTC_FRQCH_SEL, HostRegRequests.HST_RFTC_FRQCH_CFG, HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDACCTL, HostRegRequests.HST_RFTC_FRQCH_CMDSTART -> validRequest =
               true

            HostRegRequests.HST_AUTHENTICATE_CFG, HostRegRequests.HST_AUTHENTICATE_MSG, HostRegRequests.HST_READBUFFER_LEN, HostRegRequests.HST_UNTRACEABLE_CFG -> validRequest =
               true

            HostRegRequests.HST_CMD -> {
               validRequest = true
               needResponse = true
            }

            else -> {}
         }
         val DEBUG = false
         if (DEBUG) appendToLog("checking msgbuffer = " + (if (msgBuffer == null) "NULL" else "Valid") + ", validRequst = " + validRequest)
         return if (msgBuffer == null || !validRequest) {
            if (DEBUG) appendToLog("Invalid HST_REQ_REQ or null message")
            false
         } else {
            if (DEBUG) appendToLog("True Ending 0")
            val cs108RfidData = Cs108RfidData()
            cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_COMMAND
            cs108RfidData.dataValues = msgBuffer
            if (needResponse || !writeOperation) {
               cs108RfidData.waitUplinkResponse = needResponse || !writeOperation
               //                    mRx000ToWrite.add(cs108RfidData);
               addRfidToWrite(cs108RfidData)
            } else {
               cs108RfidData.waitUplinkResponse = needResponse || !writeOperation
               addRfidToWrite(cs108RfidData)
            }
            if (DEBUG) appendToLog("True Ending")
            true
         }
      }

      fun addRfidToWrite(cs108RfidData: Cs108RfidData) {
         var repeatRequest = false
         if (mRfidDevice!!.mRfidToWrite.size != 0) {
            val cs108RfidData1 = mRfidDevice!!.mRfidToWrite[mRfidDevice!!.mRfidToWrite.size - 1]
            if (cs108RfidData.rfidPayloadEvent == cs108RfidData1!!.rfidPayloadEvent) {
               if (cs108RfidData.dataValues == null && cs108RfidData1.dataValues == null) {
                  repeatRequest = true
               } else if (cs108RfidData.dataValues != null && cs108RfidData1.dataValues != null) {
                  if (cs108RfidData.dataValues!!.size == cs108RfidData1.dataValues!!.size) {
                     if (compareArray(
                           cs108RfidData.dataValues,
                           cs108RfidData1.dataValues,
                           cs108RfidData.dataValues!!.size
                        )
                     ) {
                        repeatRequest = true
                     }
                  }
               }
            }
         }
         if (!repeatRequest) {
            if (false) appendToLog("add cs108RfidData to mRfidToWrite with rfidPayloadEvent = " + cs108RfidData.rfidPayloadEvent)
            mRfidDevice!!.mRfidToWrite.add(cs108RfidData)
         }
      }
   }
}