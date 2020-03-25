package org.griffin.sclfg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setup_login_onclick()
    }

    /*
        Callback function to pass to login_handler.
        Decouples the context logic with logging in for intent purposes
     */
    var login_cb = object : (() -> Unit) {
        override fun invoke() {
            var intent = Intent(this@LoginActivity, MainActivty::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        }
    }

    fun setup_login_onclick()
    {
        /* Setup Button OnClick Listeners */

        login_button.setOnClickListener {
            var lpacket = LoginHandler.User(email = "", password = "")
            var err = false;

            if(email.text.isNotBlank() && password.text.isNotBlank())
            {
                lpacket.email = email.text.toString()
                lpacket.password = password.text.toString()
            }
            else
            {
                err = true;
            }

            if(err)
            {
                Toast.makeText(this,
                    "Either email or password was incorrect!", Toast.LENGTH_SHORT)
            }
            else /* Handle Login Request */
            {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT)

                var handler = LoginHandler(lpacket, login_cb)
                handler.login()
            }
        }

        register.setOnClickListener {
            var lpacket = LoginHandler.User(email = "", password = "")
            var err = false;

            if(email.text.isNotBlank() && password.text.isNotBlank())
            {
                lpacket.email = email.text.toString()
                lpacket.password = password.text.toString()
            }
            else
            {
                err = true;
            }

            if(err)
            {
                Toast.makeText(this,
                    "Either email or password was incorrect!", Toast.LENGTH_SHORT)
            }
            else /* Handle Login Request */
            {
                Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT)

                var handler = LoginHandler(lpacket, login_cb)
                handler.register()
            }
        }
    }

}
