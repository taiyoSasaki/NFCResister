package jp.co.ods.nfcresister

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {

    private val viewPagerAdapter by lazy { ViewPagerAdapter(this) }
    private var page = 0

    private val receiver = MyBroadcastReceiver()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // BroadcastReceiverを登録
        val filter = IntentFilter("ods.NFC_ID_CHECK")
        registerReceiver(receiver, filter)

        //SharedPreference
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        function_edit.text = Editable.Factory.getInstance().newEditable((sp.getString("functionA", "") ?: ""))

        // ViewPager2の初期化
        viewPager2.apply {
            adapter = viewPagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL // スワイプの向き横（ORIENTATION_VERTICAL を指定すれば縦スワイプで実装可能です）
            offscreenPageLimit = viewPagerAdapter.itemCount // ViewPager2で保持する画面数
        }

        // TabLayoutの初期化
        // TabLayoutとViewPager2を紐づける
        // TabLayoutのTextを指定する
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.setText(viewPagerAdapter.titleIds[position])
        }.attach()

        //ページが変わったことを通知するリスナー
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("CommitPrefEdits")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                page = position
                Log.d("onPageSelected", "page = $page")

                when(page) {
                    0 -> {
                        function_edit.text = Editable.Factory.getInstance().newEditable((sp.getString("functionA", "") ?: ""))
                    }
                    1 -> {
                        function_edit.text = Editable.Factory.getInstance().newEditable((sp.getString("functionB", "") ?: ""))
                    }
                    2 -> {
                        function_edit.text = Editable.Factory.getInstance().newEditable((sp.getString("functionC", "") ?: ""))
                    }
                }
            }
        })

        //プラスボタン
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

        function_edit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // キーボードを閉じる
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                //テキストを保存
                when(page) {
                    0 -> {
                        val editor = sp.edit()
                        editor.putString("functionA", function_edit.text.toString())
                        editor.apply()
                    }
                    1 -> {
                        val editor = sp.edit()
                        editor.putString("functionB", function_edit.text.toString())
                        editor.apply()
                    }
                    2 -> {
                        val editor = sp.edit()
                        editor.putString("functionC", function_edit.text.toString())
                        editor.apply()
                    }
                }

            }
            false
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを閉じる
        // InputMethodManagerを取得
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //背景のLayoutを取得
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraint_layout)
        inputMethodManager.hideSoftInputFromWindow(constraintLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        when(page) {
            0 -> {
                val editor = sp.edit()
                editor.putString("functionA", function_edit.text.toString())
                editor.apply()
            }
            1 -> {
                val editor = sp.edit()
                editor.putString("functionB", function_edit.text.toString())
                editor.apply()
            }
            2 -> {
                val editor = sp.edit()
                editor.putString("functionC", function_edit.text.toString())
                editor.apply()
            }
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiverを解除
        unregisterReceiver(receiver)
    }

}