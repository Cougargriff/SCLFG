package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(var name: String,
                 var timeCreated: Long,
                 var playerList: List<String>,
                 var ship: Ship,
                 var loc: Location,
                 var maxPlayers: Int,
                 var currCount: Int) : Parcelable
