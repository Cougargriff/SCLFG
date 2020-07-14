package org.griffin.sclfg.View.GroupView.Messaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.griffin.sclfg.Models.Message

class MessageViewModel(var gid : String) : ViewModel() {
        companion object {
                fun messageToHash(msg: Message) : HashMap<String, Any> {
                        return hashMapOf(
                                "time" to msg.timeCreated,
                                "author" to msg.author,
                                "content" to msg.content
                        )
                }

                fun messageFromHash(result: DocumentSnapshot) : Message {
                        val time = result["timeCreated"] as Long
                        val author = result["author"].toString()
                        var content = result["content"].toString()
                        val mid = result.id

                        return Message(author, time, content, mid)
                }
        }

        private val db = Firebase.firestore

        private val msgs : MutableLiveData<List<Message>> by lazy {
                MutableLiveData<List<Message>>().also {
                        initMsgs()
                }
        }

        fun getMsgs() : LiveData<List<Message>> {
                return msgs
        }

        private fun msgListFromDocs(msgDocs : MutableList<DocumentSnapshot>) : ArrayList<Message> {
                val msgList = ArrayList<Message>()
                msgDocs.forEach {
                        msgList.add(messageFromHash(it))
                }
                return msgList
        }

        private fun initMsgs() {
                db.collection("groups")
                        .document(gid).collection("messages")
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                val docs = querySnapshot!!.documents
                                var initMsgs = msgListFromDocs(docs)
                                msgs.value = initMsgs
                        }
                db.collection("groups").document(gid)
                        .collection("messages").get().addOnSuccessListener {
                                val docs = it!!.documents
                                val initMsgs = msgListFromDocs(docs)
                                msgs.value = initMsgs
                        }
        }

}