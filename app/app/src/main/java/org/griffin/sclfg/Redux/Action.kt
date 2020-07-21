package org.griffin.sclfg.Redux

import org.griffin.sclfg.Models.*

class Action {
    data class LOAD_GROUPS_SUCCESS (val groups : ArrayList<Group>)
    data class LOAD_USER_SUCCESS(val user: User)
    data class UPDATE_GROUPS_FROM_SNAP (val groups : ArrayList<Group>)
    data class UPDATE_USER_FROM_SNAP(val user : User)
    object PUSH_NEW_GROUP_SUCCESS
    object PUSH_NEW_GROUP_REQUEST
    object MAKE_GROUP_PUBLIC_SUCCESS
    object MAKE_GROUP_PRIVATE_SUCCESS
    object MAKE_GROUP_PRIVATE_REQUEST
    object MAKE_GROUP_PUBLIC_REQUEST
    object LOAD_GROUPS_REQUEST
    object LOAD_USER_REQUEST
    object LEAVE_GROUP_SUCCESS
    object LEAVE_GROUP_REQUEST
    object JOIN_GROUP_SUCCESS
    object JOIN_GROUP_REQUEST
    object CLEAR_SELECTED_GROUP
    object SELECT_GROUP_REQUEST
    data class LOAD_SELECTED_GROUP(val group : Group)
    object DELETE_GROUP_REQUEST
    object DELETE_GROUP_SUCCESS
    object CHANGE_NAME_REQUEST
    object CHANGE_NAME_SUCCESS
    object LOAD_SHIPS_REQUEST
    data class LOAD_SHIPS_SUCCESS(val ships : ArrayList<Ship>)
    object LOAD_LOCATIONS_REQUEST
    data class LOAD_LOCATIONS_SUCCESS(val locs: ArrayList<Location>)
    object SEND_MESSAGE_REQUEST
    object SEND_MESSAGE_SUCCESS
    data class LOAD_MESSAGES_FROM_SNAP(val msgs : ArrayList<Message>)
    object CLEAR_SELECTED_MESSAGES
}
