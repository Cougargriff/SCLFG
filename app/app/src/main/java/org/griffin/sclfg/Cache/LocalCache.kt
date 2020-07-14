package org.griffin.sclfg.Cache

import android.content.Context
import android.content.SharedPreferences
import org.griffin.sclfg.Login.LoginHandler

private val USER_PROP = "USER_FIELD"
private val PSW_PROP = "PSW_FIELD"

class LocalCache(context: Context) {
    val sharedPref: SharedPreferences by lazy {
        context.getSharedPreferences("SCLFG", Context.MODE_PRIVATE)
    }

    fun cacheCredentials(lpacket: LoginHandler.User) {
        sharedPref.edit().apply {
            putString(USER_PROP, lpacket.email)
            putString(PSW_PROP, lpacket.password)
            commit()
        }
    }

    fun clearCache(cb: () -> Unit) {
        sharedPref.edit().apply {
            clear()
            commit()
        }.also {
            cb()
        }
    }

    fun retrieveCredentials(
        cb: (user: String, psw: String) -> Unit,
        errCb: () -> Unit
    ) {
        val user = sharedPref.getString(USER_PROP, "")
        val psw = sharedPref.getString(PSW_PROP, "")
        if (!user!!.isBlank() && !psw!!.isBlank()) {
            cb(user, psw)
        } else {
            errCb()
        }
    }
}