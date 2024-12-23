package com.example.fogofwar.activities.marker_groups_activity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.activities.friends_activity.FriendsActivity

class RecycleViewAdapterMarkerGroups(private var mDataset: List<String>?, private var context: Context): RecyclerView.Adapter<RecycleViewAdapterMarkerGroups.MyViewHolder>() {



    class MyViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val buttonSend: ImageButton = v.findViewById(R.id.buttonSend)


        fun bind(item: String?, _context: Context) {
            textView.text = item
            buttonSend.setOnClickListener {
                val intent = Intent(_context, FriendsActivity::class.java)
                intent.putExtra("caller_activity", "MarkerGroupsActivity")
                intent.putExtra("marker_name", textView.text.toString())
                _context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.marker_groups_rec_view_item, parent, false)
        val vh = MyViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataset!![position], context)
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}