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
                    resData = numberToUpperCaseLetter(item.page) ?: ""
                }
            }

            val resIntent = Intent("ods.NFC_FUNCTION_RESPONSE")
            resIntent.putExtra("response", resData)
            context.sendBroadcast(resIntent)
        }
    }

    private fun numberToUpperCaseLetter(number: Int): String? {
        if (number < 0 || number > 25) {
            return null // 1から26の範囲外の場合はnullを返すかエラー処理を行うこともできます。
        }
        // 'A'のASCIIコードは65です。そこからnumber分足した数字を文字に変換
        val charValue = 'A'.toInt() + number
        // Char型に変換して返す
        return charValue.toChar().toString()
    }

}