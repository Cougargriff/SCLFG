package org.griffin.sclfg.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
    Data class for in game ships obtained from FireStore
 */
@Parcelize
data class Ship(var name: String,
                var manuf: String,
                var mass: String,
                var price: String,
                var prod_state: String,
                var role: String,
                var size: String) : Parcelable