package org.griffin.sclfg.View.Group.Tabs

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.tab_message.*
import kotlinx.android.synthetic.main.tab_message.view.*
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Adapters.MessagesAdapter

class MessageFragment(val gid: String) : Fragment() {

    private val vm: GroupViewModel by activityViewModels()
    private val msgVm: MessageViewModel by activityViewModels()

    private var msgs = ArrayList<Message>()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group: Group

    private lateinit var rv: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_message, container, false)

        /* Setup Message View */
        rv = view.messageView
        rvManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        rvAdapter = MessagesAdapter(ArrayList(), user, retrieveName)
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
        fun(uid: String, cb: (name: String) -> Unit) {
            vm.lookupUID(uid) {
                cb(it)
            }
        }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            if (message_box.text.isNotBlank()) {
                sendButton.visibility = View.INVISIBLE
                confirm_lottie.apply {
                    imageAssetsFolder = "/assets/"
                    setAnimation("confirm_lottie.json")
                    visibility = View.VISIBLE
                    addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationEnd(animation: Animator?) {
                            confirm_lottie.visibility = View.GONE
                            it.visibility = View.VISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) = Unit
                        override fun onAnimationRepeat(animation: Animator?) = Unit
                        override fun onAnimationStart(animation: Animator?) = Unit
                    })
                    playAnimation()
                }
                val txt = message_box.text.toString()
                message_box.text.clear()
                vm.groupExists(gid, {}, {
                    msgVm.sendMessage(Message(user.uid, Timestamp.now().seconds, txt, "")) {
                    }
                })
            }

        }
    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            user = it
            (rv.adapter as MessagesAdapter).authUser = user

            msgVm.getMsgs().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                var temp = ArrayList<Message>()
                it.forEach {
                    temp.add(it)
                }

                var diff = temp.minus(msgs)

                diff.reversed().forEach {
                    (rv.adapter as MessagesAdapter).addItem(it)
                }
                msgs = temp

                rv.smoothScrollToPosition(0)
            })
        })
    }
}

