package jp.co.ods.nfcresister

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NfcCardAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //登録してあるカードのデータを格納するリスト
    private var items = mutableListOf<NfcCard>()

    //削除ボタンのコールバック
    var onClickRemove: ((NfcCard) -> Unit)? = null

    override fun getItemCount(): Int {
        return if(items.isEmpty()) 1 else items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            //カードが登録されていないとき
            VIEW_TYPE_EMPTY -> EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_not_card, parent, false))
            //カードが登録されているとき
            else -> NfcCardViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_nfc_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NfcCardViewHolder) {
            //生成されたViewHolderがNfcCardViewHolderだったら
            updateNfcCardViewHolder(holder, position)
        }
    }

    private fun updateNfcCardViewHolder(holder: NfcCardViewHolder, position: Int) {
        //生成されたViewHolderの位置情報を指定し、オブジェクトを代入
        val data = items[position]

        holder.apply {
            // 偶数番目と奇数番目で背景色を変更させる
            rootView.setBackgroundColor(ContextCompat.getColor(context, if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray))
            nameTextView.text = data.label
            cardNumberView.text = data.idm
            removeIcon.setOnClickListener { onClickRemove?.invoke(data) }
        }

    }

    fun setItemList(list: ArrayList<NfcCard>) {
        items = list
    }

    // ViewHolderを継承したNfcCardViewHolderクラスの定義
    class NfcCardViewHolder(view: View) :RecyclerView.ViewHolder(view) {
        // レイアウトファイルからidがrootViewのConstraintLayoutオブジェクトを取得し、代入
        val rootView: ConstraintLayout = view.findViewById(R.id.rootView)
        val nameTextView: TextView = view.findViewById(R.id.name)
        val cardNumberView: TextView = view.findViewById(R.id.card_number)
        val removeIcon: ImageView = view.findViewById(R.id.remove_icon)
    }

    //カードが登録されていないとき
    class EmptyViewHolder(view: View): RecyclerView.ViewHolder(view)

    companion object {
        // Viewの種類を定義する定数
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_EMPTY = 1
    }

}