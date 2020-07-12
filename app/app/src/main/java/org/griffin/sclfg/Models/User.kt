package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var screenName: String,
                var uid: String,
                var inGroups: ArrayList<String>,
                 var timeCreated: Long) : Parcelable

