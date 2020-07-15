package org.griffin.sclfg.View.GroupView.Messaging

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_message.view.*
import kotlinx.android.synthetic.main.message_cell.*
import kotlinx.android.synthetic.main.message_cell.view.*
import kotlinx.android.synthetic.main.profile_group_cell.view.*
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.R
import org.griffin.sclfg.View.GroupView.ModalGroupActivity
import org.griffin.sclfg.View.Home.Tabs.GListAdapter
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Observer

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
        })


        msgVm.getMsgs().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            var temp = ArrayList<Message>()
            it.forEach{
                temp.add(it)
            }

            msgs = temp
            var newAdapter = MessageListAdapter(msgs, user, retrieveName)
                rv.adapter = newAdapter
            /* TODO not scrolling to bottom of messages nicely... */
            rv.smoothScrollToPosition(0)
        })
    }

}

class MessageListAdapter(
    val messageList: ArrayList<Message>, val authUser:  User,
    val retrieveName : (uid : String, cb : (name : String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var vParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.message_cell,
            parent, false
        ) as LinearLayout

        vParent = parent

        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = messageList[position]
        var item = holder.itemView

        var leftSet = ConstraintSet()
        var rightSet = ConstraintSet()

        var cellLayout = item.msg_cell
        leftSet.clone(cellLayout)
        rightSet.clone(cellLayout)
        rightSet.clear(R.id.author_box, ConstraintSet.LEFT)
        rightSet.apply {
            clear(R.id.author_card, ConstraintSet.LEFT)
            clear(R.id.content_card, ConstraintSet.LEFT)
            connect(R.id.content_card, ConstraintSet.RIGHT, R.id.msg_cell, ConstraintSet.RIGHT, 4)
            connect(R.id.author_card, ConstraintSet.RIGHT, R.id.msg_cell, ConstraintSet.RIGHT, 4)
        }

        if(curr.author.compareTo(authUser.uid) == 0)
        {
            leftSet.applyTo(item.msg_cell)
        }
        else {
            rightSet.applyTo(item.msg_cell)
        }
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