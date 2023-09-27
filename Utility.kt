package com.incommsys.android.composefolderscanner.cs108library


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class Utility(private val mContext: Context, private val mLogView: TextView?) {
   val DEBUG_PKDATA = true
   val DEBUG_APDATA = true
   fun setReferenceTimeMs() {
      mReferenceTimeMs = System.currentTimeMillis()
   }

   val referencedCurrentTimeMs: Long
      get() = System.currentTimeMillis() - mReferenceTimeMs

   fun compareByteArray(array1: ByteArray?, array2: ByteArray?, length: Int): Boolean {
      var i = 0
      if (array1 == null) return false
      if (array2 == null) return false
      if (array1.size < length || array2.size < length) {
         return false
      }
      while (i < length) {
         if (array1[i] != array2[i]) {
            break
         }
         i++
      }
      return i == length
   }

   fun byteArray2DisplayString(byteData: ByteArray?): String {
      if (false) appendToLog("String0 = " + byteArrayToString(byteData))
      var str = ""
      str = byteArrayToString(byteData)
      str = str.replace("[^\\x00-\\x7F]".toRegex(), "")
      str = str.replace("[\\p{Cntrl}&&[^\r\n\t]]".toRegex(), "")
      if (false) appendToLog("String1 = $str")
      return str
   }

   fun byteArrayToString(packet: ByteArray?): String {
      if (packet == null) return ""
      val sb = StringBuilder(packet.size * 2)
      for (b in packet) {
         sb.append(String.format("%02X", b))
      }
      return sb.toString()
   }

   fun byteArrayToInt(bytes: ByteArray): Int {
      var iValue = 0
      var length = bytes.size
      if (bytes.size > 4) length = 4
      for (i in 0 until length) {
         iValue = (iValue shl 8) + (bytes[i].toInt() and 0xFF)
      }
      return iValue
   }

   fun appendToLogRunnable(s: String?) {
      mHandler.post { appendToLog(s) }
   }

   fun appendToLog(s: String?): String {
      var TAG = ""
      val stacktrace = Thread.currentThread().stackTrace
      var foundMe = false
      for (i in stacktrace.indices) {
         val e = stacktrace[i]
         val methodName = e.methodName
         if (methodName.contains("appendToLog")) {
            foundMe = true
         } else if (foundMe) {
            if (!methodName.startsWith("access$")) {
               //TAG = String.format(Locale.US, "%s.%s", e.getClassName(), methodName);
               TAG = String.format(Locale.US, "%s", methodName)
               break
            }
         }
      }
      Log.i("$TAG.Hello", s!!)
      return """
         
         $referencedCurrentTimeMs.$s
         """.trimIndent()
   }

   fun appendToLogView(s: String) {
      appendToLog(s)
      val string = """
             
             ${referencedCurrentTimeMs}.$s
             """.trimIndent()
      if (Looper.myLooper() == Looper.getMainLooper() && mLogView != null && string != null) mLogView.append(
         string
      )
   }

   fun debugFileSetup() {
      var writeExtPermission = true
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            writeExtPermission = false
            appendToLog("requestPermissions WRITE_EXTERNAL_STORAGE 1")
            ActivityCompat.requestPermissions(
               (mContext as Activity),
               arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
               1
            )
            return
         }
      }
      var errorDisplay: String? = null
      if (!writeExtPermission) {
         errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!"
      } else if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) errorDisplay =
         "Error in mouting external storage !!!" else {
         val path =
            File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS + "/cs108Java")
         if (!path.exists()) path.mkdirs()
         if (!path.exists()) errorDisplay = "Error in making directory !!!" else {
            val dateTime = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
            val fileName = "cs108JavaDebug_$dateTime.txt"
            fileDebug = File(path, fileName)
            if (fileDebug == null) errorDisplay = "Error in making directory !!!"
         }
      }
      if (errorDisplay != null) appendToLog("Error in saving file with $errorDisplay")
   }

   fun debugFileClose() {
      if (fileDebug != null) {
         try {
            MediaScannerConnection.scanFile(mContext, arrayOf(fileDebug!!.absolutePath), null, null)
         } catch (ex: Exception) {
         }
      }
   }

   fun debugFileEnable(enable: Boolean) {
      enableFileDebug = enable
   }

   fun writeDebug2File(stringDebug: String?) {
      if (fileDebug != null && enableFileDebug) {
         try {
            val outputStreamDebug = FileOutputStream(fileDebug, true)
            val printWriterDebug = PrintWriter(
               OutputStreamWriter(
                  BufferedOutputStream(outputStreamDebug),
                  StandardCharsets.UTF_8
               )
            )
            printWriterDebug.println(stringDebug)
            printWriterDebug.flush()
            printWriterDebug.close()
            outputStreamDebug.close()
         } catch (ex: Exception) {
         }
      }
   }

   fun getlast3digitVersion(str: String?): String? {
      if (str != null) {
         val len = str.length
         if (len > 3) {
            var strOut = ""
            strOut = if (str.substring(len - 3, len - 2).matches("0".toRegex())) str.substring(
               len - 2,
               len - 1
            ) else str.substring(len - 3, len - 1)
            strOut += "." + str[len - 1]
            return strOut
         }
      }
      return null
   }

   fun isVersionGreaterEqual(
      version: String?,
      majorVersion: Int,
      minorVersion: Int,
      buildVersion: Int
   ): Boolean {
      if (version == null) return false
      if (version.length == 0) return false
      val versionPart = version.split("[ .,-]+".toRegex()).dropLastWhile { it.isEmpty() }
         .toTypedArray()
         ?: return false
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

   fun get2BytesOfRssi(bytes: ByteArray, index: Int): Double {
      var iValue = (bytes[index].toInt() and 0xFF) * 256 + (bytes[index + 1].toInt() and 0xFF)
      if (iValue and 0x8000 != 0) iValue = iValue xor 0xFFFF.inv()
      val dValue = iValue.toDouble()
      return dValue / 100
   }

   fun decodeAsygnTemperature(string: String): Float {
      val stringUser5 = string.substring(20, 24)
      val iUser5 = Integer.valueOf(stringUser5, 16)
      val stringUser6 = string.substring(24, 28)
      val iUser6 = Integer.valueOf(stringUser6, 16)
      val stringUser1 = string.substring(4, 8)
      var iUser1 = Integer.valueOf(stringUser1, 16)
      when (iUser1 and 0xC000) {
         0xc000 -> {
            iUser1 = iUser1 and 0x1FFF
            iUser1 /= 8
         }

         0x8000 -> {
            iUser1 = iUser1 and 0xFFF
            iUser1 /= 4
         }

         0x4000 -> {
            iUser1 = iUser1 and 0x7FF
            iUser1 /= 2
         }

         else -> iUser1 = iUser1 and 0x3FF
      }
      var temperature = -1f
      appendToLog("input string $string, user1 = $stringUser1, user5 = $stringUser5, user6 = $stringUser6")
      //iUser1 = 495; iUser6 = 3811;
      appendToLog("iUser1 = $iUser1, iUser5 = $iUser5, iUser6 = $iUser6")
      if (iUser5 == 3000) {
         val calibOffset = 3860.27.toFloat() - iUser6.toFloat()
         appendToLog("calibOffset = $calibOffset")
         val acqTempCorrected = iUser1.toFloat() + calibOffset / 8
         appendToLog("acqTempCorrected = $acqTempCorrected")
         temperature = 0.3378.toFloat() * acqTempCorrected - 133f
         appendToLog("temperature = $temperature")
      } else if (iUser5 == 1835) {
         var expAcqTemp = 398.54.toFloat() - iUser5.toFloat() / 100f
         appendToLog("expAcqTemp = $expAcqTemp")
         expAcqTemp /= 0.669162.toFloat()
         appendToLog("expAcqTemp = $expAcqTemp")
         val calibOffset = 8f * expAcqTemp - iUser6.toFloat()
         var acqTempCorrected = iUser1.toFloat() + calibOffset
         acqTempCorrected /= 8f
         temperature = -0.669162.toFloat() * acqTempCorrected
         temperature += 398.54.toFloat()
         appendToLog("expAcqTemp = $expAcqTemp. calibOffset = $calibOffset, acqTempCorrected = $acqTempCorrected, temperature = $temperature")
      }
      return temperature
   } //4278

   companion object {
      private var mReferenceTimeMs: Long = 0
      private val mHandler = Handler()
      private var fileDebug: File? = null
      private var enableFileDebug = false
   }
}