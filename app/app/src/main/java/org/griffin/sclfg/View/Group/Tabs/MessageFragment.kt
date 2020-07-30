package org.griffin.sclfg.View.Group.Tabs

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.tab_message.*
import kotlinx.android.synthetic.main.tab_message.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.Thunks.clearSelectedMessages
import org.griffin.sclfg.Redux.Thunks.db
import org.griffin.sclfg.Redux.Thunks.sendMessage
import org.griffin.sclfg.Redux.Thunks.setMessageListener
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Adapters.MessagesAdapter
import org.reduxkotlin.StoreSubscription

class MessageFragment(val gid: String) : Fragment() {
    private var msgs = ArrayList<Message>()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var unsub : StoreSubscription

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
        setupSendButton()
    }

    override fun onDetach() {
        super.onDetach()
        store.dispatch(clearSelectedMessages())
       unsub()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        SetupRedux()
    }

    private fun SetupRedux() {
        unsub = store.subscribe {
            try {
                render(store.getState().selectedMsgs!!)
                render(store.getState().user)
            } catch (e : Exception) {}
        }
        /* Passing activity should detach listener automatically */
        store.dispatch(setMessageListener(requireActivity(), gid))
    }

    private fun render(newMsgs : ArrayList<Message>) {
        try {
            requireActivity().runOnUiThread {
                newMsgs.minus(msgs).reversed().forEach {
                    (rv.adapter as MessagesAdapter).addItem(it)
                }
                msgs = newMsgs

                rv.smoothScrollToPosition(0)
            }
        } catch (e : Exception) {}
    }

    private fun render(newUser : User) {
        try {
            requireActivity().runOnUiThread {
                user = newUser
                (rv.adapter as MessagesAdapter).apply {
                    authUser = user
                    notifyDataSetChanged()
                }
            }
        } catch (e : Exception) {}
    }

    private val retrieveName =
        fun(uid: String, cb: (name: String) -> Unit) {
            try {
                GlobalScope.launch {
                    val user = db.collection("users").document(uid)
                        .get().await()
                    requireActivity().runOnUiThread {
                        cb(Groups.userFromHash(user).screenName)
                    }
                }
            } catch (e : Exception) {}
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
                store.dispatch(sendMessage(gid,
                    Message(user.uid, Timestamp.now().seconds, txt, "")))
            }
        }
    }
}

