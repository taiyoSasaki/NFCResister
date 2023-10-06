package jp.co.ods.nfcresister

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.opencsv.CSVWriter

import kotlinx.android.synthetic.main.activity_csv.*
import kotlinx.coroutines.*
import java.io.FileWriter
import java.lang.Exception

@Suppress("DEPRECATION")
class CsvActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val READ_REQUEST_CODE = 1000

    private lateinit var nfcCardDao: NfcCardDao
    private lateinit var cardList :List<NfcCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv)

        //databaseの準備
        nfcCardDao = NfcCardRoomDatabase.getDatabase(applicationContext).NfcCardDao()
        runBlocking { cardList = nfcCardDao.getAllNfcCard() }

        //戻るボタンの実装
        back_button.setOnClickListener {
            finish()
        }

        //csvアウトプットボタン
        csv_output_button.setOnClickListener {
            outputCsv()
        }

        //csvインポートボタン
        csv_import_button.setOnClickListener {
            //ダイアログの表示
            val builder = android.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("csvファイルの読み込み")
                .setMessage("csvファイルを読み込むと、現在保存されているカード情報がすべて削除されます。よろしいですか？")
                .setPositiveButton("OK") { _, _ ->
                    //パーミッションの状態確認
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //許可されている
                        openReadCsv()
                    } else {
                        //許可されていないのでダイアログを表示する
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_CODE
                        )
                    }
                }
                .setNeutralButton("キャンセル", null)
            builder.create()
            builder.show()
        }

    }

    private fun openReadCsv() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                importCsv(uri)
            }
        }
    }

    //csvインポート
    @SuppressLint("Recycle")
    private fun importCsv(uri: Uri) {
        val fileUri = Uri.parse(uri.toString())

        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            inputStream?.bufferedReader()?.useLines { lines ->
                //一行ずつ読み出す
                lines.forEach { line ->
                    //取り出した一行をカンマ(,)ごとに区切ってリストを作成する
                    val cells = line.split(",")
                    Log.d("importCsv", "cells -> $cells")

                    //databaseに保存
                    CoroutineScope(Dispatchers.Main).launch {
                        nfcCardDao.clear()

                        var errFlag = false
                        var nfcCard = NfcCard(0, 0, "", "")
                        try {
                            nfcCard = NfcCard(page = cells[0].toInt(), label = cells[1], idm = cells[2])
                        } catch (e : Exception) {
                            e.printStackTrace()
                            errFlag = true
                        } finally {
                            if (!errFlag) {
                                nfcCardDao.insert(nfcCard)
                            } else {
                                withContext(Dispatchers.Main) { showDialog() }
                            }
                        }
                    }

                }
            }
        } catch (e :Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SdCardPath")
    private fun outputCsv() {
        val csvFilePath = "/sdcard/Download/output.csv"
        val fileWriter = FileWriter(csvFilePath, false)
        val rowList = mutableListOf<Array<String>>()

        for (item in cardList) {
            val row = arrayOf(item.page.toString(), item.label, item.idm)
            rowList.add(row)
        }

        rowList.add(arrayOf("0", "ALSOK", "123456789"))
        Log.d("csvOutput", "rowList -> $rowList")

        //csvファイルを書き込む
        val csvWriter = CSVWriter(fileWriter)
        csvWriter.writeAll(rowList)

        csvWriter.close()
    }

    private fun showDialog() {
        //ダイアログの表示
        val builder = android.app.AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("csvファイルの読み込み")
            .setMessage("csvファイルを正しく読み込めませんでした。\ncsvファイルを正しい形式で作成してください")
            .setPositiveButton("OK", null)
        builder.create()
        builder.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //許可された
                    openReadCsv()
                } else {
                    //許可されなかった。
                }
            }
        }
    }

}