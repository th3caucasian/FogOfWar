package com.example.fogofwar.activities.marker_groups_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.additions.MarkerGroupDTO

class RecycleViewAdapterMarkerGroups(private var mDataset: List<String>?): RecyclerView.Adapter<RecycleViewAdapterMarkerGroups.MyViewHolder>() {



    class MyViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val button: ImageButton = v.findViewById(R.id.buttonSend)


        fun bind(item: String?) {
            textView.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.marker_groups_rec_view_item, parent, false)
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