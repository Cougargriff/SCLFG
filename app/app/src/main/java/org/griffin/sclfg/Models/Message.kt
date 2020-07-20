package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message(
    var author: String,
    var timeCreated: Long,
    var content: String,
    var mid: String
) : Parcelable

