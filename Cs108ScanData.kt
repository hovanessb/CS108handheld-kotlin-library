package com.incommsys.android.composefolderscanner.cs108library

import android.bluetooth.BluetoothDevice

class Cs108ScanData {

   lateinit var device: BluetoothDevice
   var name: String? = null
   var address: String? = null
   var rssi: Int
   var scanRecord: ByteArray
   var decoded_scanRecord: ArrayList<ByteArray>? = null
   var serviceUUID2p2 = 0

   internal constructor(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
      this.device = device
      this.rssi = rssi
      this.scanRecord = scanRecord
      decoded_scanRecord = ArrayList()
   }

   internal constructor(name: String?, address: String?, rssi: Int, scanRecord: ByteArray) {
      this.name = name
      this.address = address
      this.rssi = rssi
      this.scanRecord = scanRecord
   }
}