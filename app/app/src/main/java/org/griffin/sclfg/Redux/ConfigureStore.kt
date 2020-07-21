package org.griffin.sclfg.Redux

import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Redux.Middleware.loggingMiddleware
import org.griffin.sclfg.Redux.Reducers.groupsReducer
import org.griffin.sclfg.Redux.Reducers.userReducer
import org.griffin.sclfg.Redux.Thunks.listenToGroups
import org.griffin.sclfg.Redux.Thunks.listenToUser
import org.reduxkotlin.*

data class AppState(val user : User,
                    val groups : ArrayList<Group>,
                    val isLoadingGroups : Boolean)

val rootReducer = combineReducers(groupsReducer, userReducer)

fun configureStore() : Store<AppState> {
    val store = createThreadSafeStore(
        rootReducer,
        AppState(User("",
            "",
            ArrayList(),
            0),
        ArrayList(),
        false),
        applyMiddleware(createThunkMiddleware(), loggingMiddleware))

    /* call collection listeners once */
    store.dispatch(listenToGroups())
    store.dispatch(listenToUser())

    return store
}

val store = configureStore()
