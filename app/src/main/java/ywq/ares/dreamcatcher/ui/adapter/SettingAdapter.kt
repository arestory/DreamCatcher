package ywq.ares.dreamcatcher.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.matchParent
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.ui.bean.SettingItem

class SettingAdapter : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private var list = ArrayList<SettingItem>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)


        val holder = ViewHolder(rootView)

        holder.iv = rootView.findViewById(R.id.iv)
        holder.titleTv = rootView.findViewById(R.id.tvTitle)


        return holder

    }

    private var itemClickListener:((item: SettingItem,position: Int)->Unit)?=null


    fun setOnItemClickListener(itemClickListener:(item: SettingItem,position: Int)->Unit){
        this.itemClickListener=itemClickListener
    }

    fun setNewData(list:ArrayList<SettingItem>){
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun addData(item:SettingItem){

        this.list.add(item)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {

        return list.size
     }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.iv.setBackgroundResource(item.drawableId)
        holder.titleTv.text=item.title
        holder.itemView.setOnClickListener {

            itemClickListener?.invoke(item,position)
        }
     }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var titleTv: TextView
        lateinit var iv: ImageView
    }
}