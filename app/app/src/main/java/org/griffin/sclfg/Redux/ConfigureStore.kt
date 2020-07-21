package org.griffin.sclfg.Redux

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.reduxkotlin.*

data class State(val user : User, val groups : List<Group>)
val db = Firebase.firestore

data class UPDATE_GROUPS(val groups : List<Group>)
data class UPDATE_GROUPS_REQUEST()

fun getGroups() : Thunk<State> = {dispatch, getState, extraArg ->

}




val groupsReducer : Reducer<State> = { state, action ->
    when (action) {
        is UPDATE_GROUPS -> state.copy(groups = action.groups)
        else -> state
    }
}

val rootReducer = combineReducers(groupsReducer)



fun configureStore() {

}