package jp.co.ods.nfcresister

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onReceive", "Broadcastが発動しました。")
        if (intent?.action == "ods.NFC_ID_CHECK") {
            val requestData = intent.getStringExtra("request_id")

            var list: List<NfcCard> = listOf()
            var resData = ""

            val nfcCardDao = NfcCardRoomDatabase.getDatabase(context!!.applicationContext).NfcCardDao()
            runBlocking { list = nfcCardDao.getAllNfcCard() }
            for (item in list) {
                if (item.idm == requestData) {
                    resData = when(item.page) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> ""
                    }
                }
            }

            val resIntent = Intent("ods.NFC_FUNCTION_RESPONSE")
            resIntent.putExtra("response", resData)
            context.sendBroadcast(resIntent)

        }
    }


}