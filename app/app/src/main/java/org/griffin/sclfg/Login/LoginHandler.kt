package org.griffin.sclfg.Login

import com.google.firebase.auth.FirebaseAuth


/* added success and fail anon callback functions to do ui stuff after async call */
class LoginHandler(lPacket : User, val successCb : () -> Unit, val failCb : () -> Unit)
{
    data class User(var email : String, var password : String)

    private var mAuth  = FirebaseAuth.getInstance()
    private var email = lPacket.email
    private var psw = lPacket.password

    fun login()
    {
        mAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                successCb()
            }
            else
            {
                failCb()
            }
        }
    }

    fun register()
    {
        mAuth.createUserWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                successCb()
            }
            else
            {
                failCb()
            }
        }
    }
}