package org.griffin.sclfg.Redux

import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User

class Actions {
    data class LOAD_GROUPS_SUCCESS (val groups : ArrayList<Group>)
    data class LOAD_USER_SUCCESS(val user: User)
    data class UPDATE_GROUPS_FROM_SNAP (val groups : ArrayList<Group>)
    data class UPDATE_USER_FROM_SNAP(val user : User)
    object PUSH_NEW_GROUP
    object MAKE_GROUP_PUBLIC
    object MAKE_GROUP_PRIVATE
    object LOAD_GROUPS_REQUEST
    object LOAD_USER_REQUEST
    object LEAVE_GROUP
    object JOIN_GROUP
}
