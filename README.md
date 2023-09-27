# cs108-Library-2.8.22-5 in Kotlin

This library was taken from the [CS108 Android Java Bluetooth Demo App](https://github.com/cslrfid/CS108-Android-Java-App) and SDK

Its a Kotlin implementation to use with modern Android Compose. 
It has also been slightly refactored for better readability.

## Usage
Rough Example Implementation Files

### Scaning US Example
```kotlin

internal fun WriteScreen(
   scanModel: ScanViewModel,
   convergenceModel: ConvergenceHandgunViewModel
) {
   val connectedGun by convergenceModel.uiState.collectAsStateWithLifecycle()

   if (connectedGun is ConvergenceHandgunState.Scanning) {
      val rfidData = (connectedGun as ConvergenceHandgunState.Scanning).rfidData
      if(rfidData?.rawRead != null){
         if (activeField.value) {
            readBarcode.value = rfidData.rawRead!!
            if(rfidData.seriesId != null){
               readBarcodeFolderType.longValue = rfidData.seriesId!!
               selecteReadFolderSeries.value = convergenceModel.folderSeriesIdToName(rfidData.seriesId!!.toInt())
            }
         } else {
            writeBarcode.value = rfidData.rawRead!!.toString(Charsets.US_ASCII).split("-").first().toByteArray(Charsets.US_ASCII)
            if(rfidData.seriesId != null){
               writeBarcodeFolderType.longValue = rfidData.seriesId!!
               selecteWriteFolderSeries.value = convergenceModel.folderSeriesIdToName(rfidData.seriesId!!.toInt())
            }
         }
      } 
   }
}
```



### UI Connect Bluetooth Device Compose File
```kotlin
package com.android.scanner.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
importcom.android.scanner.ui.components.*
importcom.android.scanner.ui.theme.IgniteFolderScannerTheme
importcom.android.scanner.ui.viewmodels.ConvergenceHandgunState
importcom.android.scanner.ui.viewmodels.ConvergenceHandgunViewModel
importcom.android.scanner.ui.viewmodels.DeviceUiState
importcom.android.scanner.ui.viewmodels.DeviceViewModel
import kotlinx.coroutines.withContext

private const val TAG = "DeviceScanCompose"

@Composable
internal fun DeviceRoute(modifier: Modifier = Modifier,
                         viewModel: DeviceViewModel = hiltViewModel(),
                         convergenceModel : ConvergenceHandgunViewModel = hiltViewModel(),) {
   val deviceState by viewModel.viewState.collectAsStateWithLifecycle()
   DeviceScreen(deviceState = deviceState, modifier = modifier, viewModel =viewModel, convergenceModel = convergenceModel)
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
internal fun DeviceScreen(
   deviceState: DeviceUiState,
   modifier: Modifier = Modifier,
   viewModel: DeviceViewModel = hiltViewModel(),
   convergenceModel : ConvergenceHandgunViewModel = hiltViewModel(),
) {
   val connectedGun by convergenceModel.uiState.collectAsStateWithLifecycle()
   val current = LocalContext.current
   val permissionState = rememberMultiplePermissionsState(permissions = listOf(
         Manifest.permission.ACCESS_COARSE_LOCATION,
         Manifest.permission.ACCESS_FINE_LOCATION,
         Manifest.permission.BLUETOOTH,
         Manifest.permission.BLUETOOTH_ADMIN
      )
   )

   LaunchedEffect(key1 = 1){
      viewModel.initAdapter(current)
   }

   Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {

      Text("Bluetooth Connections", modifier.padding(16.dp),style = MaterialTheme.typography.titleLarge)

      Divider(modifier = Modifier.padding(horizontal = 20.dp))

      when (deviceState) {
         DeviceUiState.Ready -> {
            if(!permissionState.allPermissionsGranted){
               Button(modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                     onClick = {permissionState.launchMultiplePermissionRequest()},
                     content={ Text(text = "Grant Permissions") })
            } else {
               Scanning(viewModel)
            }
         }

         is DeviceUiState.BluetoothNotSupported -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
               Text(
                  text = "Bluetooth is not supported on this device",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Light
               )
            }
         }

         is DeviceUiState.Scanning -> {
            Loading()
            Spacer(modifier = Modifier.height(15.dp))
            Text(
               text = "Scanning for devices",
               fontSize = 15.sp,
               fontWeight = FontWeight.Light
            )
         }

         is DeviceUiState.Results -> if (deviceState.scanResults.isNotEmpty()) {
            val disconnected = connectedGun is ConvergenceHandgunState.Disconnected || connectedGun is ConvergenceHandgunState.NeverConnected
            val busy = connectedGun is ConvergenceHandgunState.Busy
            val currentContainerColor = if (disconnected)  {
               MaterialTheme.colorScheme.background
            } else if(busy) {
               MaterialTheme.colorScheme.secondaryContainer
            }  else MaterialTheme.colorScheme.primary
            val currentColor  = if (disconnected)  {
               MaterialTheme.colorScheme.onBackground
            } else if(busy) {
               MaterialTheme.colorScheme.onSecondaryContainer
            } else {
               MaterialTheme.colorScheme.onPrimary
            }

            val view = LocalView.current

            LazyColumn(
               modifier = Modifier
                  .padding(10.dp)
                  .fillMaxWidth()
            ) {
               itemsIndexed(deviceState.scanResults.keys.toList()) { _, key ->
                  val name = deviceState.scanResults[key]?.name ?: "Unknown Device"
                  val title = if (disconnected) name else if(busy) "Connecting..." else "Connected "
                  val device: BluetoothDevice = deviceState.scanResults.get(key = key)!!
                  Column(modifier = modifier) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween){
                        Column(modifier = Modifier
                           .clickable {
                              if (convergenceModel.currentDevice?.address != device.address) {
                                 convergenceModel.setCurrentConnection(device = device)
                                 view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                              } else {
                                 convergenceModel.destroy()
                              }
                           }
                           .background(
                              currentContainerColor,
                              shape = RoundedCornerShape(10.dp)
                           )
                           .fillMaxWidth()
                           .border(1.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                           .padding(5.dp)
                        ) {
                           Text(text=title, color=currentColor)
                           Text( text = deviceState.scanResults[key]?.address ?: "",
                                 fontWeight = FontWeight.Light,
                                 color=currentColor)
                        }
                     }
                  }
               }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally){
               if (busy) {
                  FolderLoadingWheel(contentDesc = "Please wait while this fully connects.")
               } else if(!disconnected) {
                  val isRfidOnFlow by convergenceModel.isRfidOn.collectAsStateWithLifecycle(initialValue = false,lifecycle = LocalLifecycleOwner.current.lifecycle)
                  val isBarcodeOnFlow by convergenceModel.isBarcodeOn.collectAsStateWithLifecycle(initialValue = false,lifecycle =LocalLifecycleOwner.current.lifecycle)
                  val batteryLevelFlow by convergenceModel.getBatteryLevel.collectAsStateWithLifecycle(initialValue = (0).toFloat(),lifecycle =LocalLifecycleOwner.current.lifecycle)
                  val currentPowerLevel = remember{ mutableIntStateOf(convergenceModel.getRfidPowerLevel().toInt()) }
                  val tagPopulation = remember{ mutableIntStateOf(convergenceModel.getPopulation()) }
                  val barcodeModuleActive = remember{mutableStateOf(false)}
                  val batteryLevelMut = remember{ mutableFloatStateOf(0.0F)}
                  val rfidModuleActive = remember{mutableStateOf(false)}
                  val batteryLevel : Float by animateFloatAsState(targetValue = batteryLevelFlow / 100 ,
                     animationSpec = tween(300,100,FastOutSlowInEasing),
                     label = ""
                  )

                  batteryLevelMut.value = batteryLevelFlow
                  barcodeModuleActive.value = isBarcodeOnFlow
                  rfidModuleActive.value = isRfidOnFlow

                  Text(
                     text = "Battery Level: ${(batteryLevelMut.value ).toInt()}%",
                     style = MaterialTheme.typography.titleMedium
                  )
                  Spacer(modifier=Modifier.padding(8.dp))
                  LinearProgressIndicator(
                     modifier= Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                     progress = batteryLevel)
                  Spacer(modifier=Modifier.padding(8.dp))
                  Text(
                     text = "RFID Power Level: ${currentPowerLevel.value}",
                     style = MaterialTheme.typography.titleMedium
                  )
                  Spacer(modifier=Modifier.padding(4.dp))
                  Slider(convergenceModel.getRfidPowerLevel(),
                         onValueChange = {currentPowerLevel.value=it.toInt()},
                         valueRange = (0).toFloat()..(300).toFloat(),
                         modifier= Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                         onValueChangeFinished = {convergenceModel.setRfidPowerLevel(currentPowerLevel.value)})
                  Spacer(modifier=Modifier.padding(8.dp))
                  Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                     Text(
                        text = "RFID Module ",
                        style = MaterialTheme.typography.titleSmall
                     )
                     Switch(checked = rfidModuleActive.value, onCheckedChange =  {convergenceModel.setRfidModule(it)})
                     Spacer(modifier=Modifier.padding(8.dp))
                     Text(
                        text = "Barcode Module ",
                        style = MaterialTheme.typography.titleSmall
                     )
                     Switch(checked = barcodeModuleActive.value, onCheckedChange = {convergenceModel.setBarcodeModule(it)})
                  }

                  Spacer(modifier=Modifier.padding(16.dp))
                  Text(
                     text = "Average Number of RFIDs to Read Per Scan: ${tagPopulation.value}",
                     style = MaterialTheme.typography.titleSmall
                  )
                  Spacer(modifier=Modifier.padding(4.dp))
                  Column(modifier=Modifier.padding(horizontal = 16.dp)){
                     Text(
                        text = "Current RFID Search Mode: ",
                        style = MaterialTheme.typography.titleSmall
                     )
                     Spacer(modifier=Modifier.padding(4.dp))
                     Text(text=convergenceModel.getInvAlgo(), style=MaterialTheme.typography.bodySmall)
                  }
               }
            }
         }
         is DeviceUiState.Error -> {
            Text(text = deviceState.message)
         } else -> {
            Empty(modifier)
         }
      }
   }
}

@Composable
private fun Scanning(viewModel : DeviceViewModel){
   Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Button(modifier = Modifier
         .fillMaxWidth()
         .padding(16.dp), onClick = {viewModel.startScan()}, content={ Text(text = "Scan Devices")})
   }
}

@Preview
@Composable
private fun LoadingStatePreview() {
   IgniteFolderScannerTheme {
      Loading()
   }
}


@Preview
@Composable
private fun DeviceScanScreenPreview() {
   IgniteFolderScannerTheme {
      Loading()
   }
}
```

### Service File
```kotlin
package com.android.scanner.ui.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
importcom.android.scanner..cs108library.Cs108Library4A
importcom.android.scanner..cs108library.Cs108Connector
importcom.android.scanner..cs108library.HostCmdResponseTypes
importcom.android.scanner..cs108library.ReaderDevice
importcom.android.scanner..cs108library.OperationTypes
importcom.android.scanner..cs108library.HostCommands
importcom.android.scanner..cs108library.Rx000pkgData
importcom.android.scanner..models.ScanModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Start RFID Tag/Label Inventory using Non-Default Configuration
 * 1. abortOperation(): to stop any previous RFID inventory and check RFID module health
   2. setPowerLevel (300): Set Power = 300
   3. setAntennaDwell(0): Set Antenna Dwell=0
   4. setCurrentLinkProfile (1): Set Link Profile = 1
   5. setTagGroup(0, 0, 0): Set select = all, Session = S0, target = A
   6. setInvAlgo(3): Set Algorithm = Dynamic
   7. setDynamicQParms(8, 0, 15, 0): Set Q = 8 for dynamic algorithm. setFixedQParms(8, 0, false): Q=8 for fixed algorithm
   8. startOperation(OperationTypes.TAG_INVENTORY): start inventory
   9. onRFIDEvent(): to get possible inventoried package data
   10. abortOperation(): to stop RFID inventory (do this when you are done reading RFID tags)

   Search an RFID Tag/Label (Geiger Search)
   1. abortOperation(): to stop any previous RFID inventory and check RFID module health
   2. setSelectedTag(“201700000000000000000001”, 300): set target tag ID, such as 201700000000000000000001, and power = 300
   3. setCurrentLinkProfile (1): Set Link Profile = 1
   4. setTagGroup(0, 1, 2): Set select = all, Session = S1, target = A/B toggle
   5. setInvAlgo(0): Set Algorithm = Fixed
   6. setFixedQParms(0, 0, false): Q=0 for fixed algorithm
   7. startOperation(OperationTypes.TAG_INVENTORY): start inventory
   8. onRFIDEvent(): to get possible inventoried package data
   9. abortOperation(): to stop RFID inventory (do this when you are done reading RFID tags)
 */
sealed interface ConvergenceHandgunState {
   class  Connect(val device: BluetoothDevice) : ConvergenceHandgunState
   object NeverConnected : ConvergenceHandgunState
   object Disconnected : ConvergenceHandgunState
   object Busy : ConvergenceHandgunState
   object Ready : ConvergenceHandgunState
   class Scanning (val rfidData : ScanModel?) : ConvergenceHandgunState
   object Writing : ConvergenceHandgunState
}

private fun deviceState(): ConvergenceHandgunState {
   return ConvergenceHandgunState.NeverConnected
}

class ConvergenceHandgunViewModel : ViewModel(){
   private var bleLibrary: Cs108Library4A? = null
   var currentDevice: BluetoothDevice? = null
   private var lastRssi = 0.0
   private var searching : Boolean = false
   private val mutableState = MutableStateFlow(deviceState())
   val uiState : StateFlow<ConvergenceHandgunState> = mutableState.asStateFlow()

   suspend fun startBleLibrary(app: Context) = withContext(Dispatchers.Main){
      Log.i("ConvergenceHandgun","Starting the BLE Library")
      bleLibrary =  Cs108Library4A(app, TextView(app))
   }


   fun setCurrentConnection(device: BluetoothDevice) {
      viewModelScope.launch {
         destroy()
         currentDevice = device
         if(uiState.value is ConvergenceHandgunState.Disconnected || uiState.value is ConvergenceHandgunState.NeverConnected){
            connectToHandgunDevice(device)
         }
      }
   }

   fun destroy(){
      try{
         if(uiState.value !is ConvergenceHandgunState.NeverConnected && uiState.value !is ConvergenceHandgunState.Disconnected){
            bleLibrary?.sameCheck = true;
            bleLibrary?.setInvBrandId(false)
            bleLibrary?.restoreAfterTagSelect()
            bleLibrary?.disconnect(true)
         }
      } finally {
         mutableState.update{ ConvergenceHandgunState.Disconnected }
         currentDevice = null
      }
   }


   @SuppressLint("MissingPermission")
   private suspend fun connectToHandgunDevice(device: BluetoothDevice) {
      if (!bleLibrary?.isBleConnected!!) {
         mutableState.update{ ConvergenceHandgunState.Connect(device) }
         mutableState.update{ ConvergenceHandgunState.Busy }
         val newDevice = ReaderDevice(
            device.name,
            device.address,
            false,
            "",
            1,
            1.0,
            0
         )
         bleLibrary?.connect(newDevice)
         var waitTime = 30
         var ready = false
         while(--waitTime > 0) {
            if (bleLibrary?.isBleConnected!!) {
               ready = true
               break
            }
            delay(500)
         }
         bleLibrary?.setReaderDefault()

         while (bleLibrary?.mrfidToWriteSize() != 0 ) {
            //bleLibrary?.mrfidToWritePrint()
            delay(200)
         }

         bleLibrary?.setNotificationListener( object : Cs108Connector.NotificationListener{
               override fun onChange() {
                  triggerPressed()
               }
            }
         )

         if(ready){
            newDevice.isConnected = true
            mutableState.update{ ConvergenceHandgunState.Ready }
            spoolGun()
         } else {
            mutableState.update{ ConvergenceHandgunState.Disconnected }
         }
      }
   }

   private fun triggerPressed() {
      val triggered  = bleLibrary?.triggerButtonStatus!!
      val bleReady  = bleLibrary?.isBleConnected!!
      val rfidFail = bleLibrary?.isRfidFailure!!
      if(!bleReady || rfidFail){
         mutableState.update { ConvergenceHandgunState.Disconnected }
         currentDevice = null
      } else if(triggered && mutableState.value is ConvergenceHandgunState.Ready){
         if(searching) {
            bleLibrary?.startOperation(OperationTypes.TAG_SEARCHING)
         } else {
            startInventory()
         }
      } else if(!triggered && mutableState.value is ConvergenceHandgunState.Scanning){
         mutableState.update { ConvergenceHandgunState.Ready }
         abort()
      }
   }
   private suspend fun spoolGun() = withContext(Dispatchers.IO){
      if(bleLibrary?.isBleConnected!! && !bleLibrary?.isRfidFailure!!){
         while (bleLibrary?.isBleConnected!! == true &&
            mutableState.value !is ConvergenceHandgunState.Disconnected &&
            mutableState.value !is ConvergenceHandgunState.NeverConnected) {
            if(bleLibrary?.mrfidToWriteSize() == 0){
               val rx000pkgData: Rx000pkgData? = bleLibrary?.onRFIDEvent()
               bleLibrary?.mRfidDevice
               if (rx000pkgData?.responseType != null && rx000pkgData.decodedError == null){
                  try {
                     when (rx000pkgData.responseType) {
                        in setOf(
                           HostCmdResponseTypes.TYPE_18K6C_INVENTORY,
                           HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) -> {
                           if(bleLibrary?.triggerButtonStatus!!){
                              mutableState.update { ConvergenceHandgunState.Scanning(parseData(rx000pkgData))}
                           }
                        }
                        HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS -> {
                           if(lastRssi != rx000pkgData.decodedRssi){
                              if(bleLibrary?.triggerButtonStatus!!){
                                 mutableState.update { ConvergenceHandgunState.Scanning(parseData(rx000pkgData))}
                              }
                           }
                           lastRssi = rx000pkgData.decodedRssi
                        }
                         HostCmdResponseTypes.TYPE_COMMAND_END -> {
                           if(bleLibrary?.triggerButtonStatus!!){
                              if(searching){
                                 bleLibrary?.startOperation(OperationTypes.TAG_SEARCHING)
                              } else {
                                 bleLibrary?.startOperation(OperationTypes.TAG_INVENTORY)
                              }
                           }
                        } else -> {
                           Log.i("ConvergenceHandgunViewModel","Ending Trigger Scanning")
                        }
                     }
                  } catch (myException : Exception ){
                     Log.w("ConvergenceHandgunViewModel", myException.stackTraceToString())
                  }
               }
            }
         }
         mutableState.update { ConvergenceHandgunState.Disconnected }
         currentDevice = null
      }
   }

   fun startSearch (aSelectedTag : ByteArray) {
      bytesToHex(aSelectedTag)?.let { bleLibrary?.setSelectedTag(it, 1, 300) }
      bleLibrary?.startOperation(OperationTypes.TAG_SEARCHING)
   }

   fun startInventory () {
      if (!bleLibrary?.mRfidDevice!!.inventoring) {
         bleLibrary?.startOperation(OperationTypes.TAG_INVENTORY)
      }

   }

   private fun abort () {
      bleLibrary?.abortOperation()
   }
   private fun bytesToHex(bytes: ByteArray): String? {
      val hex: ByteArray = "0123456789ABCDEF".toByteArray(Charsets.US_ASCII)
      val hexChars = ByteArray(bytes.size * 2)

      for (j in bytes.indices) {
         val v = bytes[j].toInt() and 0xFF
         hexChars[j * 2] = hex[v ushr 4]
         hexChars[j * 2 + 1] = hex[v and 0x0F]
      }

      return String(hexChars, Charsets.US_ASCII)
   } 
   
   fun folderSeriesIdToName(aSeriesId : Int) : String {
      val seriesName: String = when(aSeriesId) {
         0 -> {
            "Warehouse"
         }
          1 -> {
             "Stocks"
         }

         3 -> {
            "Store"
         }
      }
      return seriesName
   }
    

   suspend fun writeRfidData ( aCurrentTag : ByteArray , aWriteData : ByteArray , aWriteFolderType : String): String {
      var ending = false
      val iTimeOut = 5000
      var runTimeMillis: Long = System.currentTimeMillis()
      var endingMessage = "Success!"
      var retries = 0
      var readBytes  = bytesToHex(aCurrentTag)
      var maxSize = aWriteData.lastIndex + 1
      var minSize = 16

      if(maxSize > 32){
         maxSize = 32
      }

      if(maxSize <= 16){
         minSize = 0
      }

      var writeBytes = bytesToHex(aWriteData.copyOfRange(minSize, maxSize).plus(("-${folderSeriesToId(aWriteFolderType)}").toByteArray(Charsets.US_ASCII)))
      val repeat = 7

      while(writeBytes!!.length < 32){
         writeBytes += "0"
      }
      //33343536373893031322D31
      val accessCount = writeBytes.length / 4
      Log.d("debug",aWriteData.size.toString())
      bleLibrary?.abortOperation()
      bleLibrary?.setAccessBank(1)
      bleLibrary?.setAccessOffset(2)
      bleLibrary?.setAccessCount(accessCount)
      bleLibrary?.setRx000AccessPassword("0000")
      bleLibrary?.setAccessRetry(true,repeat)
      bleLibrary?.setAccessWriteData(writeBytes)
      if (readBytes != null) {
         bleLibrary?.setSelectedTag(readBytes,1,50)
      }
      setWriteModeDefaults()
      bleLibrary?.sendHostRegRequestHST_CMD(HostCommands.CMD_18K6CWRITE)

      while (bleLibrary?.isBleConnected == true && ending == false) {
         mutableState.update { ConvergenceHandgunState.Writing }
         if (System.currentTimeMillis() > runTimeMillis + 1000) {
            runTimeMillis += 1000
         }
         val rx000pkgData: Rx000pkgData? = bleLibrary?.onRFIDEvent()

         if (rx000pkgData?.responseType != null) {
            if (rx000pkgData.responseType == HostCmdResponseTypes.TYPE_COMMAND_END) {
               if (rx000pkgData.decodedData1 != null) {
                  endingMessage = rx000pkgData.decodedError.toString()
                  ending = true
               } else if (retries < repeat) {
                  bleLibrary?.sendHostRegRequestHST_CMD(HostCommands.CMD_18K6CWRITE)
                  retries += 1
               } else {
                  ending = true
               }
            }
         }

         if (runTimeMillis > iTimeOut){
            ending = true
         }

         delay(250)
      }
      bleLibrary?.abortOperation()
      setScanModeDefaults()
      bleLibrary?.abortOperation()
      mutableState.update {ConvergenceHandgunState.Ready}
      return endingMessage
   }



   private fun parseData(rx000pkgData: Rx000pkgData): ScanModel {
      var extraLength = 0
      if (rx000pkgData.decodedData1 != null) {
         extraLength += rx000pkgData.decodedData1!!.size
      }
      if (rx000pkgData.decodedData2 != null) {
         extraLength += rx000pkgData.decodedData2!!.size
      }
      var strEpc: List<String> = listOf("")
      if(rx000pkgData.decodedEpc != null){
         if (extraLength != 0 ) {
            val decodedEpcNew = ByteArray(rx000pkgData.decodedEpc!!.size - extraLength)
            System.arraycopy(rx000pkgData.decodedEpc, 0, decodedEpcNew, 0, decodedEpcNew.size)
            rx000pkgData.decodedEpc = decodedEpcNew
         }
         strEpc  = rx000pkgData.decodedEpc!!.toString(Charsets.US_ASCII).split("-")
      }

      // My RFIDs have a barcode in UTF-8 "0001000003-10" seperated by a seriesId number for categorization, i.e. "10"
      return if(strEpc.size >= 2 && strEpc[1].isNotEmpty() && strEpc[1].filter { it.isDigit() }.isDigitsOnly()){
         Log.i("ConvergenceHandgunViewModel","Found -- $strEpc[0]")
         ScanModel(
            barcodeId = strEpc[0], phase = rx000pkgData.decodedPhase.toFloat(), rssi = rx000pkgData.decodedRssi.toFloat(),
                   rawRead = rx000pkgData.decodedEpc,
                   seriesId = strEpc[1].filter { it.isDigit() }.toLong())
      } else {
         ScanModel(
                   barcodeId = strEpc[0], phase = rx000pkgData.decodedPhase.toFloat(), rssi = rx000pkgData.decodedRssi.toFloat(),
                   rawRead = rx000pkgData.decodedEpc,
                   seriesId = null)
      }
   }

   fun getRfidPowerLevel(): Float {
      return if(bleLibrary != null){
         (bleLibrary!!.pwrlevel).toFloat()
      } else {
         0F
      }
   }

   val getBatteryLevel:  Flow<Float> = flow{
      while(true){
         if(bleLibrary != null){
            emit(bleLibrary!!.getBatteryValue2Percent(bleLibrary!!.batteryLevel.toFloat() / 1000).toFloat())
         } else {
            emit((0).toFloat())
         }
         delay(5000)
      }
   }.distinctUntilChanged().conflate()

   fun setRfidPowerLevel(aChange : Number) {
      bleLibrary?.setPowerLevel(aChange.toLong())
   }

   fun getPopulation(): Int {
      return if(bleLibrary != null){
         bleLibrary!!.population
      }else {
         0
      }
   }

   val isRfidOn: Flow<Boolean> = flow{
      while(true){
         emit(bleLibrary!!.mRfidDevice?.onStatus!!)
         delay(500)
      }
   }.distinctUntilChanged().conflate()

   val isBarcodeOn: Flow<Boolean> = flow {
      while(true){
         emit(bleLibrary!!.mBarcodeDevice?.onStatus!!)
         delay(500)
      }
   }.distinctUntilChanged().conflate()

   fun setBarcodeModule(aSetValue: Boolean) {
      bleLibrary!!.setBarcodeOn(aSetValue)
   }

   fun setRfidModule(aSetValue: Boolean) {
      bleLibrary!!.setRfidOn(aSetValue)
   }

   fun getInvAlgo(): String {
      return if(bleLibrary!!.invAlgo){"DYNAMIC: Better adapts to different groups of RFID amounts per scan"}else{"FIXED: Efficiently searches for groups of ${getPopulation().toString()} RFIDs at a time"}
   }

   fun getProfile(): Int {
      return bleLibrary?.currentProfile ?: -1
   }


    fun setWriteModeDefaults() {
      val bleReady  = bleLibrary?.isBleConnected!!
      val rfidFail = bleLibrary?.isRfidFailure!!
      if(bleReady && !rfidFail){
         bleLibrary?.sameCheck = true;
         bleLibrary?.setInvBrandId(false)
         bleLibrary?.restoreAfterTagSelect()
         bleLibrary?.setTagFocus(true)
         bleLibrary?.setTagGroup(3, 1, 2)
         bleLibrary?.population = 60
         bleLibrary?.invAlgo = false
         bleLibrary?.setCurrentLinkProfile(1)
         bleLibrary?.setFixedQParms(0,0,false)
      }
   }

   fun setScanModeDefaults() {
      val bleReady  = bleLibrary?.isBleConnected
      val rfidFail = bleLibrary?.isRfidFailure
      if(bleReady == true && rfidFail == false){
         bleLibrary?.sameCheck = true
         bleLibrary?.setInvBrandId(false)
         bleLibrary?.restoreAfterTagSelect()
         bleLibrary?.setTagFocus(false)
         bleLibrary?.setTagGroup(0, 0, 2)
         bleLibrary?.population = 60
         bleLibrary?.invAlgo = true
         bleLibrary?.setCurrentLinkProfile(2)
      }
   }

   fun setGeigerModeDefaults() {
      val bleReady  = bleLibrary?.isBleConnected
      val rfidFail = bleLibrary?.isRfidFailure
      if(bleReady == true && rfidFail == false) {
         bleLibrary?.setPowerLevel(300)
         bleLibrary?.setTagGroup(0, 1, 2)
         bleLibrary?.population = 60
         bleLibrary?.invAlgo = false
         bleLibrary?.setFixedQParms(0, 0, false)
         bleLibrary?.setCurrentLinkProfile(1)
      }
   }

}
```
