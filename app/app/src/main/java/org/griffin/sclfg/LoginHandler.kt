package org.griffin.sclfg

import com.google.firebase.auth.FirebaseAuth

class LoginHandler(lPacket : User, cb : () -> Unit)
{
    data class User(var email : String, var password : String)

    var mAuth  = FirebaseAuth.getInstance()
    var email = lPacket.email
    var psw = lPacket.password
    var cb = cb

    fun login()
    {
        mAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                cb()
            }
        }
    }

    fun register()
    {
        mAuth.createUserWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                cb()
            }
        }
    }
}