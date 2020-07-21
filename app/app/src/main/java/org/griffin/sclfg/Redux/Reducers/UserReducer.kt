package org.griffin.sclfg.Redux.Reducers


import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Reducer

val userReducer : Reducer<AppState> = { state, action ->
    when (action) {
        is Action.LOAD_USER_SUCCESS -> state.copy(user = action.user)
        is Action.UPDATE_USER_FROM_SNAP -> state.copy(user = action.user)
        else -> state
    }
}
