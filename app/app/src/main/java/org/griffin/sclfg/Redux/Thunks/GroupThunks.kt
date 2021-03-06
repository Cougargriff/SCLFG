package org.griffin.sclfg.Redux.Thunks

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Thunk

val db = Firebase.firestore
val auth = FirebaseAuth.getInstance()
val grpRef = db.collection("groups")
val userRef = db.collection("users")

/*
    **** THUNKS ****
 */

fun sendMessage(gid : String, msg : Message) : Thunk<AppState> = {dispatch, getState, extraArg ->
    dispatch(Action.SEND_MESSAGE_REQUEST)
    try {
        GlobalScope.launch {
            grpRef.document(gid).collection("messages").add(
                Messages.messageToHash(msg)
            ).await()
            dispatch(Action.SEND_MESSAGE_SUCCESS)
        }

    } catch (e : Exception) {}
}


fun clearSelectedMessages() : Thunk<AppState> = {dispatch, getState, extraArg ->


    dispatch(Action.CLEAR_SELECTED_MESSAGES)
}

fun clearSelectedGroup() : Thunk<AppState> = {dispatch, getState, extraArg ->


    dispatch(Action.CLEAR_SELECTED_GROUP)
}

fun setMessageListener(view : Activity, gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    try {
        val query = grpRef.document(gid).collection("messages")
            .orderBy("time", Query.Direction.DESCENDING)
        query.addSnapshotListener(view) { querySnapshot, err ->
            GlobalScope.launch {
                val msgList = msgListFromDocs(querySnapshot!!.documents)
                var newList = ArrayList<Message>()
                msgList.forEach {
                    /* Change each author uid to screen name */
                    newList.add(it.copy(author = lookupUID(it.author)!!))
                }

                dispatch(Action.LOAD_MESSAGES_FROM_SNAP(
                    msgList
                ))
            }
        }
    } catch (e : Exception) {}
}

fun loadSelect(gid : String) : Thunk<AppState> = {dispatch, getState, extraArg ->
    dispatch(Action.SELECT_GROUP_REQUEST)
    try {
        GlobalScope.launch {


            var group = Groups.groupFromHash(grpRef.document(gid).get().await())
            group.playerList = ArrayList(group.playerList.map {
                lookupUID(it)!!
            })

            dispatch(Action.LOAD_SELECTED_GROUP_SUCCESS(group))
        }
    } catch (e : Exception) {}
}

fun getShips() : Thunk<AppState> = { dispatch, getState, extraArg ->
    try {
        dispatch(Action.LOAD_SHIPS_REQUEST)
        GlobalScope.launch {
            val ships = db.collection("ships").get().await()
            val arrShips = ArrayList(ships.documents.toList().map {
                val ship = it
                val name = ship["name"] as String
                val mass = ship["mass"] as String
                val manu = ship["manufacturer"] as String
                val price = ship["price"] as String
                val prod = ship["prod_state"] as String
                val role = ship["role"] as String
                val size = ship["size"] as String
                Ship(name, manu, mass, price, prod, role, size)
            })
            dispatch(Action.LOAD_SHIPS_SUCCESS(arrShips))
        }
    } catch (e : Exception) {}
}

fun getLocations() : Thunk<AppState> = { dispatch, getState, extraArg ->
    try {
       dispatch(Action.LOAD_LOCATIONS_REQUEST)
        GlobalScope.launch {
            val locs = db.collection("locations").get().await()
                dispatch(Action.LOAD_LOCATIONS_SUCCESS(ArrayList(locs.documents.toList().map {
                    Location(it["name"] as String)
                })))
        }
    } catch (e : Exception) {}
}

fun createGroup(group : Group, cb : () -> Unit) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.PUSH_NEW_GROUP_REQUEST)
    try {
        GlobalScope.launch {
            val id = grpRef.add(Groups.groupToHash(group, auth.uid!!)).await().id
            addGroupToUser(id, auth.uid!!)
            dispatch(Action.PUSH_NEW_GROUP_SUCCESS)
            cb()
        }

    } catch (e : Exception) {}
}

fun delete(gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.DELETE_GROUP_REQUEST)
    try {
        GlobalScope.launch {
            val group = Groups.groupFromHash(grpRef.document(gid).get().await())

            /*
                NOTE - Firestore doesn't support remote delete of collections
                Delete all messages in sub collection one by one
            */
            val grpMsgs = grpRef.document(gid).collection("messages").get().await()
            grpMsgs.documents.forEach {
                val currMsg = Messages.messageFromHash(it)
                grpRef.document(gid)
                    .collection("messages")
                    .document(currMsg.mid)
                    .delete().await()
            }

            grpRef.document(gid).delete().await()
            group.playerList.forEach {
                removeGroupFromUser(gid, it)
            }
            dispatch(Action.DELETE_GROUP_SUCCESS)
        }
    } catch (e : Exception) {}
}

fun joinGroup(gid : String, cb: () -> Unit) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.JOIN_GROUP_REQUEST)
    try {
        GlobalScope.launch {
            var group = findGroup(gid)!!
            if(group.playerList.size + 1 <= group.maxPlayers) {
                group.playerList.add(auth.uid!!)
                addGroupToUser(gid, auth.uid!!)
                grpRef.document(group.gid).set(hashMapOf(
                    "playerList" to group.playerList
                ), SetOptions.merge()).await()
                dispatch(Action.JOIN_GROUP_SUCCESS)
            } else {
                cb()
            }
        }
    } catch(e : Exception) {
        cb()
    }
}


fun leaveGroup(gid: String, cb: () -> Unit) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.LEAVE_GROUP_REQUEST)
    try {
       GlobalScope.launch {
           var group = findGroup(gid)!!
           if(group.playerList.size - 1 >= 0) {
               group.playerList.remove(getState().user.uid)
               removeGroupFromUser(gid, auth.uid!!)
               grpRef.document(group.gid).set(hashMapOf(
                   "playerList" to group.playerList
               ), SetOptions.merge()).await()
               dispatch(Action.LEAVE_GROUP_SUCCESS)
           } else {
               cb()
           }
       }
    } catch (e : Exception) {
        cb()
    }
}

fun setPublic(gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.MAKE_GROUP_PUBLIC_REQUEST)
    makePublic(gid) {
       dispatch(Action.MAKE_GROUP_PUBLIC_SUCCESS)
    }
}

fun setPrivate(gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.MAKE_GROUP_PRIVATE_REQUEST)
    makePrivate(gid) {
        dispatch(Action.MAKE_GROUP_PRIVATE_SUCCESS)
    }
}

fun listenToGroups() : Thunk<AppState> = { dispatch, getState, extraArg ->
    /* Listens to db changes and reloads groups */
    grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            /* Listen for changes and update groups list */
            GlobalScope.launch {
                val docs = querySnapshot!!.documents
                var grpList = groupListFromDocs(docs)
                grpList.forEach {
                    it.playerList = ArrayList(it.playerList.map {
                        lookupUID(it)!!
                    })
                }
                dispatch(Action.UPDATE_GROUPS_FROM_SNAP(grpList))
            }
        }
}
fun getGroups() : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.LOAD_GROUPS_REQUEST)

    GlobalScope.launch {
        loadGroups {
            dispatch(Action.LOAD_GROUPS_SUCCESS(it))
        }
    }
}

/*
    **** Async and Helper Functions ****
 */

private fun msgListFromDocs(msgDocs : MutableList<DocumentSnapshot>) : ArrayList<Message> {
    return ArrayList(msgDocs.map {
        Messages.messageFromHash(it)
    })
}

private suspend fun addGroupToUser(gid: String, uid : String) {
    try {
        val user = Groups.userFromHash(userRef.document(uid).get().await())
        var currIngroups = user.inGroups
        currIngroups.add(gid)
        userRef.document(uid)
            .set(
                hashMapOf(
                    "inGroups" to currIngroups
                ), SetOptions.merge()
            ).await()
    } catch (e : Exception) {}

}

private suspend fun removeGroupFromUser(gid: String, uid : String) {
    try {
        val user = Groups.userFromHash(userRef.document(uid).get().await())
        var newIngroups = user.inGroups
        newIngroups.forEach {
            if (it.compareTo(gid) == 0) {
                newIngroups.remove(it)
            }
        }
        userRef.document(user.uid)
            .set(
                hashMapOf(
                    "inGroups" to newIngroups
                ), SetOptions.merge()
            ).await()
    } catch (e : Exception) {}
}

private suspend fun findGroup(gid : String) : Group? {
    try {
        val group = grpRef.document(gid).get().await()
        return Groups.groupFromHash(group)
    } catch (e : Exception) {
        return null
    }
}

private fun makePublic(gid: String, cb : () -> Unit) {
    try {
        val hash = hashMapOf(
            "active" to true
        )
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                cb()
            }
    } catch (e : Exception) {}
}

private fun makePrivate(gid: String, cb : () -> Unit) {
    try {
        grpRef.document(gid)
            .set(hashMapOf(
                "active" to false
            ), SetOptions.merge())
            .addOnSuccessListener {
                cb()
            }
    } catch (e : Exception) {}
}

private suspend fun lookupUID(uid: String) : String? {
    try {
        val user = userRef.document(uid).get().await()
        return Groups.userFromHash(user).screenName
    } catch(e : Exception) {
        return null
    }
}

private fun groupListFromDocs(grpDocs: MutableList<DocumentSnapshot>): ArrayList<Group> {
    var grpList = ArrayList<Group>()
    for (grp in grpDocs) {
        grpList.add(Groups.groupFromHash(grp))
    }
    return grpList
}

/* loads oldest first */
private suspend fun loadGroups(cb: (ArrayList<Group>) -> Unit) {
    try {
        val grps = grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
            .get()
            .await()

        var groupList = groupListFromDocs(grps.documents)
        groupList.forEach {
            it.playerList = ArrayList(it.playerList.map {
                lookupUID(it)!!
            })
        }
        cb(groupList)
    } catch (e : Exception) {}
}
