package org.griffin.sclfg.Redux.Reducers

import org.griffin.sclfg.Redux.Actions
import org.griffin.sclfg.Redux.GroupsState
import org.reduxkotlin.Reducer

val groupsReducer : Reducer<GroupsState> = { state, action ->
    when (action) {
        is Actions.LOAD_GROUPS_SUCCESS -> state.copy(groups = action.groups, isLoadingGroups = false)
        is Actions.LOAD_GROUPS_REQUEST -> state.copy(isLoadingGroups = true)
        is Actions.UPDATE_GROUPS_FROM_SNAP -> state.copy(groups = action.groups)
        is Actions.LOAD_USER_SUCCESS -> state.copy(user = action.user)
        is Actions.UPDATE_USER_FROM_SNAP -> state.copy(user = action.user)
        is Actions.LOAD_USER_REQUEST -> state
        is Actions.MAKE_GROUP_PUBLIC -> state
        is Actions.MAKE_GROUP_PRIVATE -> state
        else -> state
    }
}
