package org.griffin.sclfg.Redux

import org.griffin.sclfg.Models.*
import org.griffin.sclfg.Redux.Middleware.loggingMiddleware
import org.griffin.sclfg.Redux.Reducers.groupsReducer
import org.griffin.sclfg.Redux.Reducers.userReducer
import org.griffin.sclfg.Redux.Thunks.listenToGroups
import org.griffin.sclfg.Redux.Thunks.listenToUser
import org.griffin.sclfg.Redux.Thunks.getShips
import org.griffin.sclfg.Redux.Thunks.getLocations
import org.reduxkotlin.*

data class AppState(val user : User,
                    val groups : ArrayList<Group>,
                    val isLoadingGroups : Boolean,
                    val ships : ArrayList<Ship>,
                    val locations : ArrayList<Location>,
                    val selectedGroup : Group,
                    val selectedMsgs  : ArrayList<Message>,
                    val isSelectedGrpLoaded : Boolean)

val rootReducer = combineReducers(groupsReducer, userReducer)


val initialGroup = Group("",
    "",
    0,
    ArrayList(),
    "","",
    0,
    false,
    "",
    "")

val initialUser = User("",
    "",
    ArrayList(),
    0)

private val initialState = AppState(
    initialUser,
    ArrayList(),
    false,
    ArrayList(),
    ArrayList(),
    initialGroup,
    ArrayList(),
    false
)

fun configureStore() : Store<AppState> {
    val store = createThreadSafeStore(
        rootReducer,
        initialState,
        applyMiddleware(createThunkMiddleware(), loggingMiddleware))

    store.dispatch(getShips())
    store.dispatch(getLocations())

    /* call collection listeners once */
    store.dispatch(listenToGroups())


    return store
}

val store = configureStore()

