package org.griffin.sclfg.Redux.Thunks

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Redux.Actions
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Thunk


val db = Firebase.firestore
val auth = FirebaseAuth.getInstance()
val grpRef = db.collection("groups")
val userRef = db.collection("users")


fun createGroup(group : Group, cb : () -> Unit) {
    try {
        GlobalScope.launch {
            val id = grpRef.add(GroupViewModel.groupToHash(group, auth.uid!!)).addOnSuccessListener {
            }.await().id
            addGroupToUser(id, auth.uid!!)
        }

    } catch (e : Exception) {}
}

fun joinGroup(gid : String, cb: () -> Unit) : Thunk<AppState> = { dispatch, getState, extraArg ->
    try {
        GlobalScope.launch {
            var group = findGroup(gid)!!
            if(group.currCount + 1 <= group.maxPlayers) {
                group.playerList.add(auth.uid!!)
                addGroupToUser(gid, auth.uid!!)
                grpRef.document(group.gid).set(hashMapOf(
                    "playerList" to group.playerList,
                    "currCount" to group.currCount + 1
                ), SetOptions.merge()).await()
                dispatch(Actions.JOIN_GROUP)
            } else {
                cb()
            }
        }
    } catch(e : Exception) {}
}


fun leaveGroup(gid: String, cb: () -> Unit) : Thunk<AppState> = { dispatch, getState, extraArg ->
    try {
       GlobalScope.launch {
           var group = findGroup(gid)!!
           if(group.currCount - 1 >= 0) {
               group.playerList.remove(getState().user.uid)
               removeGroupFromUser(gid, auth.uid!!)
               grpRef.document(group.gid).set(hashMapOf(
                   "playerList" to group.playerList,
                   "currCount" to group.currCount - 1
               ), SetOptions.merge()).await()
               dispatch(Actions.LEAVE_GROUP)
           } else {
               cb()
           }
       }
    } catch (e : Exception) {
        cb()
    }
}

fun setPublic(gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    makePublic(gid) {
       dispatch(Actions.MAKE_GROUP_PUBLIC)
    }
}


fun setPrivate(gid : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    makePrivate(gid) {
        dispatch(Actions.MAKE_GROUP_PRIVATE)
    }
}



fun getGroups() : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Actions.LOAD_GROUPS_REQUEST)

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
                dispatch(Actions.UPDATE_GROUPS_FROM_SNAP(grpList))
            }
        }

    GlobalScope.launch {
        loadGroups {
            dispatch(Actions.LOAD_GROUPS_SUCCESS(it))
        }
    }
}

private suspend fun addGroupToUser(gid: String, uid : String) {
    try {
        val user = GroupViewModel.userFromHash(userRef.document(uid).get().await())
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
        val user = GroupViewModel.userFromHash(userRef.document(uid).get().await())
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
        return GroupViewModel.groupFromHash(group)
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
        return GroupViewModel.userFromHash(user).screenName
    } catch(e : Exception) {
        return null
    }
}

private fun groupListFromDocs(grpDocs: MutableList<DocumentSnapshot>): ArrayList<Group> {
    var grpList = ArrayList<Group>()
    for (grp in grpDocs) {
        grpList.add(GroupViewModel.groupFromHash(grp))
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
