package org.griffin.sclfg.Redux.Thunks

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Redux.Actions
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Thunk


fun getUser() : Thunk<AppState> = { dispatch, getState, extraArg ->
    dispatch(Actions.LOAD_USER_REQUEST)

    userRef.document(auth.uid!!)
        .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val user = GroupViewModel.userFromHash(documentSnapshot!!)
            dispatch(Actions.UPDATE_USER_FROM_SNAP(user))
        }

    try {
        GlobalScope.launch {
            val user = userRef.document(auth.uid!!).get().await()
            dispatch(Actions.LOAD_USER_SUCCESS(GroupViewModel.userFromHash(user)))
        }
    } catch (e : Exception) {}
}
