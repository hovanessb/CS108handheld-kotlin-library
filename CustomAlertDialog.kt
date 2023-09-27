package com.incommsys.android.composefolderscanner.cs108library

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface

class CustomAlertDialog {
   var ans_true: Runnable? = null
   var ans_false: Runnable? = null
   fun Confirm(
      act: Activity?, Title: String?, ConfirmText: String?,
      CancelBtn: String?, OkBtn: String?, aProcedure: Runnable?, bProcedure: Runnable?
   ): Boolean {
      ans_true = aProcedure
      ans_false = bProcedure
      val dialog = AlertDialog.Builder(act).create()
      dialog.setTitle(Title)
      dialog.setMessage(ConfirmText)
      dialog.setCancelable(false)
      dialog.setButton(
         DialogInterface.BUTTON_POSITIVE, OkBtn
      ) { dialog, buttonId -> ans_true!!.run() }
      dialog.setButton(
         DialogInterface.BUTTON_NEGATIVE, CancelBtn
      ) { dialog, buttonId -> ans_false!!.run() }
      dialog.setIcon(android.R.drawable.ic_dialog_alert)
      dialog.show()
      return true
   }
}