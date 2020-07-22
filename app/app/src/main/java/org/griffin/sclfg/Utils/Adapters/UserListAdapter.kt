package org.griffin.sclfg.Utils.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.user_cell.view.*
import org.griffin.sclfg.R

class UserListAdapter(
    val userList: ArrayList<String>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_cell,
            parent, false
        ) as LinearLayout
        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = userList[position]
        val item = holder.itemView

        item.userName.text = curr

        /* other setup for user list cell components */
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


