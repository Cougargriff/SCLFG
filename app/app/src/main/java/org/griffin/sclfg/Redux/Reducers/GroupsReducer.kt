package org.griffin.sclfg.Redux.Reducers

import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Reducer

val groupsReducer : Reducer<AppState> = { state, action ->
    when (action) {
        is Action.LOAD_GROUPS_SUCCESS -> state.copy(groups = action.groups, isLoadingGroups = false)
        is Action.LOAD_GROUPS_REQUEST -> state.copy(isLoadingGroups = true)
        is Action.UPDATE_GROUPS_FROM_SNAP -> state.copy(groups = action.groups)
        is Action.LOAD_USER_REQUEST -> state
        is Action.MAKE_GROUP_PUBLIC_SUCCESS -> state
        is Action.MAKE_GROUP_PRIVATE_SUCCESS -> state
        else -> state
    }
}
