package com.example.fogofwar.activities.friends_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R

class RecycleViewAdapterFriends(private var mDataset: List<String>?): RecyclerView.Adapter<RecycleViewAdapterFriends.MyViewHolder>() {



    class MyViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val buttonAdd: Button = v.findViewById(R.id.buttonAdd)


        fun bind(item: String?) {
            textView.text = item
            buttonAdd.text = "ADDED"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        val vh = MyViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataset!![position])
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}