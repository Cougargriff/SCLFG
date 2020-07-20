package org.griffin.sclfg.Utils.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.message_cell_lb.view.*
import org.griffin.sclfg.Models.Message
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R

class MessagesAdapter(
    val messageList: ArrayList<Message>, var authUser: User,
    val retrieveName: (uid: String, cb: (name: String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private val TYPE_ME = 1
    private val TYPE_OTHER = 2

    private lateinit var vParent: ViewGroup

    override fun getItemViewType(position: Int): Int {
        if (messageList.get(position).author.compareTo(authUser.uid) == 0) {
            return TYPE_ME
        } else {
            return TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var cellView = LayoutInflater.from(parent.context).inflate(
            (
                    R.layout.message_cell_lb),
            parent, false
        ) as LinearLayout

        when (viewType) {
            TYPE_ME -> {
                cellView = LayoutInflater.from(parent.context).inflate(
                    R.layout.message_cell_rg,
                    parent, false
                ) as LinearLayout
            }
            TYPE_OTHER -> {
                cellView = LayoutInflater.from(parent.context).inflate(
                    (
                            R.layout.message_cell_lb),
                    parent, false
                ) as LinearLayout
            }
        }

        vParent = parent

        return ViewHolder(cellView)
    }

    fun addItem(msg: Message) {
        messageList.add(0, msg)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = messageList[position]
        var item = holder.itemView
        item.content_box.text = curr.content
        retrieveName(curr.author) {
            item.author_box.text = it
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }

}
