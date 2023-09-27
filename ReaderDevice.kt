package com.incommsys.android.composefolderscanner.cs108library

import androidx.annotation.Keep

@Keep
class ReaderDevice : Comparable<ReaderDevice?> {
   var name: String
   var address: String
   var selected: Boolean
   private var details: String?
   var extra1Bank = 0
   var extra2Bank = 0
   var extra1Offset = 0
   var extra2Offset = 0
   var pc: String? = null
   var xpc: String? = null
   var strCrc16: String? = null
   var mdid: String? = null
   var strExtra1: String? = null
   var strExtra2: String? = null
   var count: Int
   var rssi: Double
   var serviceUUID2p1 = 0
      private set
   var phase = 0
   var channel = 0
   var port = 0
   val INVALID_STATUS = -1
   var status = INVALID_STATUS
   val INVALID_CODETEMPC = -300f
   var backport1: Int = INVALID_BACKPORT
   var backport2: Int = INVALID_BACKPORT
   var codeSensor: Int = INVALID_CODESENSOR
   var codeSensorMax: Int = INVALID_CODESENSOR
   var codeRssi: Int = INVALID_CODERSSI
   var sensorData: Int = INVALID_SENSORDATA
   var codeTempC = INVALID_CODETEMPC
   var brand: String? = null
   var isConnected = false
   var timeOfRead: String? = null
      private set
   var timeZone: String? = null
      private set
   var location: String? = null
   var compass: String? = null
      private set

   constructor(
      name: String,
      address: String,
      selected: Boolean,
      details: String?,
      strPc: String?,
      strXpc: String?,
      strCrc16: String?,
      strMdid: String?,
      strExtra1: String?,
      extra1Bank: Int,
      extra1Offset: Int,
      strExtra2: String?,
      extra2Bank: Int,
      extra2Offset: Int,
      strTimeOfRead: String?,
      strTimeZone: String?,
      strLocation: String?,
      strCompass: String?,
      count: Int,
      rssi: Double,
      phase: Int,
      channel: Int,
      port: Int,
      status: Int,
      backPort1: Int,
      backPort2: Int,
      codeSensor: Int,
      codeRssi: Int,
      codeTempC: Float,
      brand: String?,
      sensorData: Int
   ) {
      this.name = name
      this.address = address
      this.selected = selected
      this.details = details
      pc = strPc
      xpc = strXpc
      this.strCrc16 = strCrc16
      mdid = strMdid
      this.strExtra1 = strExtra1
      this.extra1Bank = extra1Bank
      this.extra1Offset = extra1Offset
      this.strExtra2 = strExtra2
      this.extra2Bank = extra2Bank
      this.extra2Offset = extra2Offset
      timeOfRead = strTimeOfRead
      timeZone = strTimeZone
      location = strLocation
      compass = strCompass
      this.count = count
      this.rssi = rssi
      this.phase = phase
      this.channel = channel
      this.port = port
      this.status = status
      backport1 = backPort1
      backport2 = backPort2
      this.codeSensor = codeSensor
      this.codeRssi = codeRssi
      this.codeTempC = codeTempC
      this.brand = brand
      this.sensorData = sensorData
   }

   constructor(
      name: String,
      address: String,
      selected: Boolean,
      details: String?,
      count: Int,
      rssi: Double,
      serviceUUID2p1: Int
   ) {
      this.name = name
      this.address = address
      this.selected = selected
      this.details = details
      this.count = count
      this.rssi = rssi
      this.serviceUUID2p1 = serviceUUID2p1
   }

   constructor(
      name: String,
      address: String,
      selected: Boolean,
      details: String?,
      count: Int,
      rssi: Double
   ) {
      this.name = name
      this.address = address
      this.selected = selected
      this.details = details
      this.count = count
      this.rssi = rssi
   }


   fun getDetails(): String? {
      if (details == null) {
         var strDetail = "PC=" + pc + ", CRC16=" + strCrc16 // + ", Port=" + String.valueOf(port+1);
         if (xpc != null) strDetail += """
    
    XPC=${xpc}
    """.trimIndent()
         if (strExtra1 != null) {
            var strHeader: String? = null
            when (extra1Bank) {
               0 -> strHeader = "RES"
               1 -> strHeader = "EPC"
               2 -> strHeader = "TID"
               3 -> strHeader = "USER"
            }
            if (strHeader != null) strDetail += "\n$strHeader=$strExtra1"
         }
         if (strExtra2 != null) {
            var strHeader: String? = null
            when (extra2Bank) {
               0 -> strHeader = "RES"
               1 -> strHeader = "EPC"
               2 -> strHeader = "TID"
               3 -> strHeader = "USER"
            }
            if (strHeader != null) strDetail += "\n$strHeader=$strExtra2"
         }
         details = strDetail
      }
      return details
   }

   fun setDetails(details: String?) {
      this.details = details
   }

   val res: String?
      get() = if (extra1Bank == 0) strExtra1 else if (extra2Bank == 0) strExtra2 else null
   val res2: String?
      get() = if (extra2Bank == 0) strExtra2 else if (extra1Bank == 0) strExtra1 else null
   val epc: String?
      get() = if (extra1Bank == 1) strExtra1 else if (extra2Bank == 1) strExtra2 else null
   val tid: String?
      get() = if (extra1Bank == 2) strExtra1 else if (extra2Bank == 2) strExtra2 else null
   val user: String?
      get() = if (extra1Bank == 3) strExtra1 else if (extra2Bank == 3) strExtra2 else null

   fun getstrExtra1(): String? {
      return strExtra1
   }

   fun setExtra1(strExtra1: String?, extra1Bank: Int, extra1Offset: Int) {
      this.strExtra1 = strExtra1
      this.extra1Bank = extra1Bank
      this.extra1Offset = extra1Offset
   }

   fun getstrExtra2(): String? {
      return strExtra2
   }

   fun setExtra2(strExtra2: String?, extra2Bank: Int, extra2Offset: Int) {
      this.strExtra2 = strExtra2
      this.extra2Bank = extra2Bank
      this.extra2Offset = extra2Offset
   }

   fun setExtra(
      strExtra1: String?,
      extra1Bank: Int,
      extra1Offset: Int,
      strExtra2: String?,
      extra2Bank: Int,
      extra2Offset: Int
   ) {
      this.strExtra1 = strExtra1
      this.extra1Bank = extra1Bank
      this.extra1Offset = extra1Offset
      this.strExtra2 = strExtra2
      this.extra2Bank = extra2Bank
      this.extra2Offset = extra2Offset
      details = null
      getDetails()
   }

   fun setCcompass(compass: String?) {
      this.compass = compass
   }

   override fun equals(o: Any?): Boolean {
      if (o == null || o !is ReaderDevice) {
         return false
      }
      val readerDevice = o
      return address != null && readerDevice.address != null && readerDevice.address.equals(
         address, ignoreCase = true
      )
   }

   override fun hashCode(): Int {
      var hash = 4
      hash = 53 * hash + if (address != null) address.hashCode() else 0
      return hash
   }

   override operator fun compareTo(other: ReaderDevice?): Int {
      if (other != null) {
         return address.compareTo(other.address)
      }
      return 0
   }

   companion object {
      const val INVALID_BACKPORT = -1
      const val INVALID_CODESENSOR = -1
      const val INVALID_CODERSSI = -1
      const val INVALID_BRAND = -1
      const val INVALID_SENSORDATA = 0x1000
   }
}