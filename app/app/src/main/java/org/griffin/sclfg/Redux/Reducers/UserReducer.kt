package org.griffin.sclfg.Redux.Reducers


import org.griffin.sclfg.Redux.Actions
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Reducer

val userReducer : Reducer<AppState> = { state, action ->
    when (action) {
        is Actions.LOAD_USER_SUCCESS -> state.copy(user = action.user)
        is Actions.UPDATE_USER_FROM_SNAP -> state.copy(user = action.user)
        else -> state
    }
}
