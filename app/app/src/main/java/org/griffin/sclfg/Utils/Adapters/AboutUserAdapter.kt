package org.griffin.sclfg.Utils.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.user_cell_message.view.*
import org.griffin.sclfg.R

class AboutUserAdapter(
    var userList: ArrayList<String>,
    val lookUp: (uid: String, cb: (name: String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var vParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_cell_message,
            parent, false
        ) as LinearLayout

        vParent = parent

        return ViewHolder(
            cellView
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = userList[position]
        var item = holder.itemView

        item.animate()
            .alpha(0f)
            .setDuration(0L)
            .start()
        lookUp(curr) {
            item.userName.text = it
            item.animate()
                .alpha(1f)
                .setDuration(400L)
                .start()
        }

    }

    fun update(user_list: ArrayList<String>) {
        val diff = user_list.minus(userList)
        diff.forEach {
            if (user_list.contains(it)) {
                userList.add(it)
            } else {
                userList.remove(it)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}
