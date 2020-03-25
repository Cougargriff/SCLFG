package org.griffin.sclfg.Login

import com.google.firebase.auth.FirebaseAuth

class LoginHandler(lPacket : User, cb : () -> Unit)
{
    data class User(var email : String, var password : String)

    private var mAuth  = FirebaseAuth.getInstance()
    private var email = lPacket.email
    private var psw = lPacket.password
    private var cb = cb

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