package com.incommsys.android.composefolderscanner.cs108library

import androidx.annotation.Keep

@Keep
class Rx000pkgData {
   var responseType: HostCmdResponseTypes? = null
   var flags = 0
   lateinit var dataValues: ByteArray
   var decodedTime: Long = 0
   var decodedRssi = 0.0
   var decodedPhase = 0
   var decodedChidx = 0
   var decodedPort = 0
   var decodedPc: ByteArray? = null
   var decodedEpc: ByteArray? = null
   var decodedCrc: ByteArray? = null
   var decodedData1: ByteArray? = null
   var decodedData2: ByteArray? = null
   var decodedResult: String? = null
   var decodedError: String? = null
}