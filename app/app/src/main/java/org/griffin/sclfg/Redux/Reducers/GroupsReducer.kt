package org.griffin.sclfg.Redux.Reducers

import org.griffin.sclfg.Redux.Actions
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Reducer

val groupsReducer : Reducer<AppState> = { state, action ->
    when (action) {
        is Actions.LOAD_GROUPS_SUCCESS -> state.copy(groups = action.groups, isLoadingGroups = false)
        is Actions.LOAD_GROUPS_REQUEST -> state.copy(isLoadingGroups = true)
        is Actions.UPDATE_GROUPS_FROM_SNAP -> state.copy(groups = action.groups)
        is Actions.LOAD_USER_REQUEST -> state
        is Actions.MAKE_GROUP_PUBLIC -> state
        is Actions.MAKE_GROUP_PRIVATE -> state
        else -> state
    }
}
