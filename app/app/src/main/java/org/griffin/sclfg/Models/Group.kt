package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(
    var name: String,
    var gid: String,
    var timeCreated: Long,
    var playerList: ArrayList<String>,
    var ship: String,
    var loc: String,
    var maxPlayers: Int,
    var currCount: Int,
    var active: Boolean,
    var createdBy: String,
    var description: String
) : Parcelable
