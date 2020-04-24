package org.griffin.sclfg.Login

import com.google.firebase.auth.FirebaseAuth


/* added success and fail anon callback functions to do ui stuff after async call */
class LoginHandler(var lPacket : User, val failCb : () -> Unit)
{
    data class User(var email : String, var password : String)

    private var mAuth  = FirebaseAuth.getInstance()
    private var email = lPacket.email
    private var psw = lPacket.password

    fun login(cache_cb : (user : User) -> Unit)
    {
        mAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                cache_cb(lPacket)
            }
            else
            {
                failCb()
            }
        }
    }

    fun register(cache_cb : (user : User) -> Unit)
    {
        mAuth.createUserWithEmailAndPassword(email, psw).addOnCompleteListener {
            if(it.isSuccessful)
            {
                cache_cb(lPacket)
            }
            else
            {
                failCb()
            }
        }
    }
}