package org.griffin.sclfg.Redux.Middleware

import android.util.Log
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.AppState
import org.reduxkotlin.Middleware

private val ACTION_TAG = "******* REDUX-STORE -> "

val loggingMiddleware: Middleware<AppState> =
    { store ->
        { next ->
            { action ->
                Log.i(ACTION_TAG, action.toString())
                next(action)
            }
        }
    }
