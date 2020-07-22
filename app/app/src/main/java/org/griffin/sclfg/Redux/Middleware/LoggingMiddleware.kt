package org.griffin.sclfg.Redux.Middleware

import android.util.Log
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Middleware

private fun log(action : Action) : String {
    val type = "\n{\n" +  action.toString() + "\n}\n"
    return type
}

val loggingMiddleware: Middleware<AppState> =
    { store ->
        { next ->
            { action ->
                Log.i("*************** REDUX-STORE", action.toString())
                next(action)
            }
        }
    }
