package org.griffin.sclfg.View.GroupView.Messaging

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_message.view.*
import kotlinx.android.synthetic.main.message_cell_lb.view.*
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.R

class MessageFragment(val gid : String) : Fragment() {

    private val vm : ViewModel by activityViewModels()
    private val msgVm : MessageViewModel by activityViewModels()

    private var msgs = ArrayList<Message>()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group : Group

    private lateinit var rv : RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter : RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_message, container, false)

        /* Setup Message View */
        rv = view.messageView
        rvManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        rvAdapter = MessageListAdapter(ArrayList(), user, retrieveName)
        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }

        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.bottom_up)
        rv.layoutAnimation = controller
        rv.scheduleLayoutAnimation()

        rv.smoothScrollToPosition(0)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()
        setupSendButton()
    }

    private val retrieveName =
        fun (uid : String, cb: (name : String) -> Unit) {
            vm.lookupUID(uid) {
                cb(it)
            }
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            if(message_box.text.isNotBlank()) {
                val txt = message_box.text.toString()
                message_box.text.clear()
                vm.groupExists(gid, {}, {
                    msgVm.sendMessage( Message(user.uid, Timestamp.now().seconds, txt, ""))
                })
            }

        }
    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            user = it
            (rv.adapter as MessageListAdapter).authUser = user

            msgVm.getMsgs().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                var temp = ArrayList<Message>()
                it.forEach{
                    temp.add(it)
                }

                var diff  = temp.minus(msgs)

                diff.reversed().forEach {
                    (rv.adapter as MessageListAdapter).addItem(it)
                }
                msgs = temp

                /* TODO not scrolling to bottom of messages nicely... */
               rv.smoothScrollToPosition(0)
            })
    })
    }
}

class MessageListAdapter(
    val messageList: ArrayList<Message>, var authUser:  User,
    val retrieveName : (uid : String, cb : (name : String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private val TYPE_ME = 1
    private val TYPE_OTHER = 2

    private lateinit var vParent: ViewGroup

    override fun getItemViewType(position: Int): Int {
        if(messageList.get(position).author.compareTo(authUser.uid) == 0){
            return TYPE_ME
        }
        else
        {
            return TYPE_OTHER
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var cellView = LayoutInflater.from(parent.context).inflate((
                R.layout.message_cell_rg),
            parent, false
        ) as LinearLayout

        when(viewType) {
            TYPE_ME -> {
                cellView = LayoutInflater.from(parent.context).inflate(
                    R.layout.message_cell_lb,
                    parent, false
                ) as LinearLayout
            }
            TYPE_OTHER -> {
                cellView = LayoutInflater.from(parent.context).inflate((
                        R.layout.message_cell_rg),
                    parent, false
                        ) as LinearLayout
            }
        }

        vParent = parent

        return ViewHolder(cellView)
    }

    fun addItem(msg: Message)
    {
        messageList.add(0, msg)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = messageList[position]
        var item = holder.itemView

        item.content_box.text = curr.content

        /* TODO slow on updates */
        retrieveName(curr.author) {
            item.author_box.text = it
        }
    }



    override fun getItemCount(): Int {
        return messageList.size
    }

}