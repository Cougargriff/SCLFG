package org.griffin.sclfg.Redux.Thunks

import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.Groups
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Thunk


fun listenToUser() : Thunk<AppState> = { dispatch, getState, extraArg ->
    userRef.document(auth.uid!!)
        .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val user = Groups.userFromHash(documentSnapshot!!)
            dispatch(Action.UPDATE_USER_FROM_SNAP(user))
        }
}

fun getUser() : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.LOAD_USER_REQUEST)
    GlobalScope.launch {
        try {
            val user = userRef.document(auth.uid!!).get().await()
            dispatch(Action.LOAD_USER_SUCCESS(Groups.userFromHash(user)))
        } catch (e : Exception) {}
    }

}


fun changeName(name : String) : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Action.CHANGE_NAME_REQUEST)
    GlobalScope.launch {
        try {
            userRef.document(auth.uid!!).set(
                hashMapOf(
                    "screenName" to name
                ), SetOptions.merge()
            ).addOnSuccessListener {
                dispatch(getGroups())
                dispatch(Action.CHANGE_NAME_SUCCESS)
            }

        } catch (e : Exception) {}
    }
}