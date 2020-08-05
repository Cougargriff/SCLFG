package org.griffin.sclfg.Login

import com.google.firebase.auth.FirebaseAuth
import org.griffin.sclfg.Redux.Thunks.signInUser
import org.griffin.sclfg.Redux.store


/* added success and fail anon callback functions to do ui stuff after async call */
class EmailPasswordLoginHandler(var lPacket: User, val failCb: () -> Unit) {
    data class User(var email: String, var password: String)

    private var mAuth = FirebaseAuth.getInstance()
    private var email = lPacket.email
    private var psw = lPacket.password

    /* TODO move to redux */

    fun login(cacheCb: (user: User) -> Unit) {

        mAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener {
            if (it.isSuccessful) {
                cacheCb(lPacket)
            } else {
                failCb()
            }
        }
            .addOnFailureListener {

            }
    }

    fun register(cacheCb: (user: User, uid: String) -> Unit) {
        mAuth.createUserWithEmailAndPassword(email, psw).addOnCompleteListener {
            if (it.isSuccessful) {
                login {
                    cacheCb(lPacket, mAuth.currentUser!!.uid)
                }
            } else {
                failCb()
            }
        }
    }
}