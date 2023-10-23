package jp.co.ods.nfcresister

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_nfc_card.*
import kotlinx.coroutines.*

class NfcCardFragment: Fragment() {

    private val nfcCardAdapter by lazy { NfcCardAdapter(requireContext()) }
    private lateinit var nfcCardDao:NfcCardDao

    private var itemList = arrayListOf<NfcCard>()
    private var pageNumber: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageNumber = arguments?.getInt("pageNumber")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_nfc_card, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //削除アイコンが押されたら、そのままActivityへ通知
        nfcCardAdapter.onClickRemove = {
            //削除確認ダイアログを表示する
            val builder = android.app.AlertDialog.Builder(requireContext())
                .setTitle("カードの削除")
                .setMessage("${it.label}を削除しますか？")
                .setPositiveButton("削除") { _, _ ->
                    //まずはビューから削除
                    itemList.remove(it)
                    recyclerView.adapter?.notifyDataSetChanged()
                    //databaseから削除
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Default) { nfcCardDao.delete(it) }
                    }

                }
                .setNeutralButton("キャンセル", null)
            builder.create()
            builder.show()

        }

        //RecyclerViewの初期化
        recyclerView.apply {
            adapter = nfcCardAdapter
            layoutManager = LinearLayoutManager(requireContext()) //1列ずつ表示
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        Log.d("Fragment_onResume", "FragmentのonResume")

        var list: List<NfcCard> = listOf()
        itemList = arrayListOf()
        //databaseの準備
        nfcCardDao = NfcCardRoomDatabase.getDatabase(requireContext().applicationContext).NfcCardDao()
        runBlocking { list = nfcCardDao.getAllNfcCard() }
        for (item in list) {
            if (item.page == pageNumber) { itemList.add(item) }
        }

        nfcCardAdapter.setItemList(itemList)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(pageNumber :Int): NfcCardFragment {
            val fragment = NfcCardFragment()
            val page = Bundle()
            page.putInt("pageNumber", pageNumber)
            fragment.arguments = page
            return fragment
        }
    }

}