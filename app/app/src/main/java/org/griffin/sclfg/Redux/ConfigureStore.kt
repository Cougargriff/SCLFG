package org.griffin.sclfg.Redux

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.tasks.await
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import kotlinx.coroutines.launch
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Redux.Reducers.groupsReducer
import org.reduxkotlin.*



data class GroupsState(val user : User,
                       val groups : ArrayList<Group>,
                       val isLoadingGroups : Boolean)

val rootReducer = combineReducers(groupsReducer)



fun configureStore() : Store<GroupsState> {
    return createThreadSafeStore(
        rootReducer,
        GroupsState(User("",
            "",
            ArrayList(),
            0),
        ArrayList(),
        false),
        applyMiddleware(createThunkMiddleware()))
}
