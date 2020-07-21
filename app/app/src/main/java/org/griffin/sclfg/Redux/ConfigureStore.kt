package org.griffin.sclfg.Redux

import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Redux.Reducers.groupsReducer
import org.griffin.sclfg.Redux.Reducers.userReducer
import org.reduxkotlin.*



data class AppState(val user : User,
                    val groups : ArrayList<Group>,
                    val isLoadingGroups : Boolean)

val rootReducer = combineReducers(groupsReducer, userReducer)



fun configureStore() : Store<AppState> {
    return createThreadSafeStore(
        rootReducer,
        AppState(User("",
            "",
            ArrayList(),
            0),
        ArrayList(),
        false),
        applyMiddleware(createThunkMiddleware()))
}
