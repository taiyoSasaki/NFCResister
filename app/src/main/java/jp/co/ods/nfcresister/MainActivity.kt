package jp.co.ods.nfcresister

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {

    private val viewPagerAdapter by lazy { ViewPagerAdapter(this) }
    private var page = 0

    private val receiver = MyBroadcastReceiver()

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // BroadcastReceiverを登録
        val filter = IntentFilter("ods.NFC_ID_CHECK")
        registerReceiver(receiver, filter)

        // ViewPager2の初期化
        viewPager2.apply {
            adapter = viewPagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL // スワイプの向き横（ORIENTATION_VERTICAL を指定すれば縦スワイプで実装可能です）
        }

        //databaseに保存されているものから最大ページ数を調べる
        var maxPage: Int
        val nfcCardDao = NfcCardRoomDatabase.getDatabase(applicationContext).NfcCardDao()
        runBlocking { maxPage = nfcCardDao.getMaxPage() ?: 0}

        //最大ページ分を追加
        for (i in 0 until maxPage+1) {
            val fragment = NfcCardFragment(i)
            viewPagerAdapter.addFragment(fragment)
        }

        // TabLayoutの初期化
        // TabLayoutとViewPager2を紐づける
        // TabLayoutのTextを指定する
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = "機能${numberToUpperCaseLetter(position)}"
        }.attach()

        //ページが変わったことを通知するコールバックを登録
        val pageCallback = object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("CommitPrefEdits")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                page = position
                Log.d("onPageSelected", "page = $page")
            }
        }
        viewPager2.registerOnPageChangeCallback(pageCallback)

        //カード追加ボタン
        add_mark.apply {
            setOnClickListener {
                val intent = Intent(applicationContext, ResisterActivity::class.java)
                intent.putExtra("page", page)
                startActivity(intent)
            }
            setOnLongClickListener {
                val intent = Intent(applicationContext, CsvActivity::class.java)
                startActivity(intent)
                true
            }
        }

        //ページ追加ボタン
        tab_page_add.setOnClickListener {
            val newPage = viewPager2.adapter!!.itemCount
            Log.d("tab_page_add", "newPage = $newPage")
            if (newPage < 10) { //10ページまで追加できるようにする
                val newFragment = NfcCardFragment(newPage)
                viewPagerAdapter.addFragment(newFragment)

                //新しいタブを追加
                tabLayout.addTab(
                    tabLayout.newTab().setText("機能${numberToUpperCaseLetter(newPage)}")
                )

                //タブが追加されたことを通知
                viewPager2.adapter!!.notifyDataSetChanged()

                //ページが変わったことを通知するコールバックを削除し、新たにコールバックを登録
                viewPager2.unregisterOnPageChangeCallback(pageCallback)
                viewPager2.registerOnPageChangeCallback(pageCallback)

            } else {
                //ダイアログの表示
                val builder = android.app.AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("機能の追加")
                    .setMessage("これ以上機能を追加することはできません")
                    .setPositiveButton("OK", null)
                builder.create()
                builder.show()
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを閉じる
        // InputMethodManagerを取得
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //背景のLayoutを取得
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraint_layout)
        inputMethodManager.hideSoftInputFromWindow(constraintLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiverを解除
        unregisterReceiver(receiver)
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