package org.griffin.sclfg.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class MessageViewModel : ViewModel(), CoroutineScope {
    companion object {
        fun messageToHash(msg: Message): HashMap<String, Any> {
            return hashMapOf(
                "time" to msg.timeCreated,
                "author" to msg.author,
                "content" to msg.content
            )
        }

        fun messageFromHash(result: DocumentSnapshot): Message {
            val time = result["time"] as Long
            val author = result["author"].toString()
            var content = result["content"].toString()
            val mid = result.id

            return Message(author, time, content, mid)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val db = Firebase.firestore
    private lateinit var gid: String

    fun setGid(id: String, errCb: () -> Unit) {
        try {
            gid = id
        } catch (err: Exception) {
            errCb()
        }
    }

    private val msgs: MutableLiveData<List<Message>> by lazy {
        MutableLiveData<List<Message>>().also {
                initMsgs()
        }
    }

    fun getMsgs(): LiveData<List<Message>> {
        return msgs
    }

    fun sendMessage(msg: Message, cb: () -> Unit) {

        try {
            db.collection("groups")
                .document(gid)
                .collection("messages")
                .add(messageToHash(msg)).addOnSuccessListener {
                    cb()
                }
        } catch(e : Exception) {

        }
    }

    private fun msgListFromDocs(msgDocs: MutableList<DocumentSnapshot>): ArrayList<Message> {
        val msgList = ArrayList<Message>()
        msgDocs.forEach {
            msgList.add(
                messageFromHash(
                    it
                )
            )
        }
        return msgList
    }

    private fun initMsgs() {
        try {
            db.collection("groups").document(gid)
                .collection("messages")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    msgs.value = msgListFromDocs(querySnapshot!!.documents)
                }
        } catch(e : Exception) {

        }
    }
}