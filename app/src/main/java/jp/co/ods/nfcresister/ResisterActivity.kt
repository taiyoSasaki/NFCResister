package jp.co.ods.nfcresister

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import kotlinx.android.synthetic.main.activity_resister.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*

class ResisterActivity : AppCompatActivity() {

    companion object {
        const val MSG_RESISTER_CARD = 1
        const val MSG_REJECT_CARD = 2
    }

    private lateinit var nfcAdapter: NfcAdapter
    private var isNFC = true
    private lateinit var nfcCardDao: NfcCardDao

    private lateinit var cardList :List<NfcCard>
    private var page = 0
    private var idm = ""
    private var mTag : Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resister)

        //databaseの準備
        nfcCardDao = NfcCardRoomDatabase.getDatabase(applicationContext).NfcCardDao()
        runBlocking { cardList = nfcCardDao.getAllNfcCard() }

        //MainActivityの時のページ番号を取得
        page = intent.getIntExtra("page", 0)

        //NFCの設定
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (e: Exception) {
            e.printStackTrace()
            isNFC = false

            //ダイアログの表示
            val builder = android.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("NFCの設定")
                .setMessage("この端末はNFCを搭載していません。\nNFCを搭載した端末でご使用ください")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
            builder.create()
            builder.show()
        }

        //戻るボタンの実装
        back_button.setOnClickListener { finish() }

    }

    override fun onResume() {
        super.onResume()

        if (isNFC) {
            if (!nfcAdapter.isEnabled) { //NFCがオフの場合は設定画面へ遷移
                //ダイアログの表示
                val builder = android.app.AlertDialog.Builder(this)
                    .setTitle("NFCの設定")
                    .setMessage("NFCの設定をONにしてください")
                    .setPositiveButton("OK") { _, _ ->
                        //設定画面に遷移
                        val intent = Intent()
                        intent.action = Settings.ACTION_NFC_SETTINGS
                        startActivity(intent)
                    }
                    .setNeutralButton("キャンセル") { _, _ ->
                        finish()
                    }
                builder.create()
                builder.show()
            } else {
                nfcAdapter.enableReaderMode(this,
                    { tag ->
                        Log.d("onTagDiscovered", "タグを検出しました　→　$tag")
                        if (tag != null) {
                            mTag = tag
                            doMethod()
                        }
                    },
                    NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    null
                )
            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (isNFC) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    private fun doMethod() {
        idm = bytesToHexString(mTag!!.id)
        Log.d("doMethod", "読み取ったID = $idm")

        var isReject = false
        for (card in cardList) {
            if (idm == card.idm) { //既に登録されている
                isReject = true
                break
            }
        }
        if (isReject) { mHandler.sendEmptyMessage(MSG_REJECT_CARD) }
        else { mHandler.sendEmptyMessage(MSG_RESISTER_CARD) } //新規登録 ラベルを入力するためのダイアログを表示
    }

    private fun showDialog() {
        // ダイアログ専用のレイアウトを読み込む
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.edit_text_dialog, null)
        val editText = dialogLayout.findViewById<AppCompatEditText>(R.id.editTextDialog)
        editText.hint = "例：カードA"

        val dialog = AlertDialog.Builder(this)
            .setTitle("カード登録")
            .setMessage("カードにラベルをつけてください\n空欄で登録した場合は「No.X」という形式で登録されます")
            .setView(dialogLayout)
            .setPositiveButton("登録") { _, _ ->
                //OKボタンを押したとき
                var label = ""
                var count = 1
                if (editText.text.isNullOrEmpty()) {
                    runBlocking {
                        for (item in nfcCardDao.getAllNfcCard() ) {
                            if (item.page == page && item.label == "No.$count") { count++ }
                        }
                    }
                    label = "No.$count"
                } else {
                    label = editText.text.toString()
                }
                val nfcCard = NfcCard(page = page, label = label, idm = idm)
                //databaseに保存
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.Default) { nfcCardDao.insert(nfcCard) }
                }
                finish()
            }
            .setNegativeButton("キャンセル", null)
            .create()

        dialog.show()
    }

    private fun showRejectDialog() {
        //ダイアログの表示
        val builder = android.app.AlertDialog.Builder(this)
            .setTitle("カード登録")
            .setMessage("このカードは既に登録されています")
            .setPositiveButton("OK", null)
        builder.create()
        builder.show()
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                MSG_RESISTER_CARD -> {
                    showDialog()
                }
                MSG_REJECT_CARD -> {
                    showRejectDialog()
                }
            }
        }
    }

    //bytesを16進数型文字列に変換用関数
    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        val formatter = Formatter(sb)
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        //大文字にして戻す（見た目の調整だけ）
        return sb.toString().toUpperCase()
    }
}