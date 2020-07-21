package org.griffin.sclfg.Redux.Reducers

import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.griffin.sclfg.Redux.configureStore
import org.griffin.sclfg.Redux.initialGroup
import org.reduxkotlin.Reducer

val groupsReducer : Reducer<AppState> = { state, action ->
    when (action) {
        is Action.LOAD_GROUPS_SUCCESS -> state.copy(groups = action.groups, isLoadingGroups = false)
        is Action.LOAD_GROUPS_REQUEST -> state.copy(isLoadingGroups = true)
        is Action.UPDATE_GROUPS_FROM_SNAP -> state.copy(groups = action.groups)
        is Action.LOAD_SHIPS_SUCCESS -> state.copy(ships = action.ships)
        is Action.LOAD_LOCATIONS_SUCCESS -> state.copy(locations = action.locs)
        is Action.CLEAR_SELECTED_GROUP -> state.copy(selectedGroup = initialGroup)
        is Action.LOAD_SELECTED_GROUP -> state.copy(selectedGroup = action.group)
        is Action.LOAD_MESSAGES_FROM_SNAP -> state.copy(selectedMsgs = action.msgs)
        is Action.CLEAR_SELECTED_MESSAGES -> state.copy(selectedMsgs = ArrayList())
        else -> state
    }
}
