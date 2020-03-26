package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
    Data class for in game locations obtained from FireStore
 */
@Parcelize
data class Location(var name: String) : Parcelable