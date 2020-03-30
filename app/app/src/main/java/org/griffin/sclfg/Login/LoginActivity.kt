package org.griffin.sclfg.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*
import org.griffin.sclfg.View.MainActivity
import org.griffin.sclfg.R

class LoginActivity : AppCompatActivity()
{
    private val BUTTON_ELEVATION = 20f

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
    private var login_cb = object : (() -> Unit) {
        override fun invoke()
        {
            var intent = Intent(this@LoginActivity, MainActivity::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        }
    }

    /* register screen to immediately set screen name */
    private var register_cb = object : (() -> Unit) {
        override fun invoke()
        {
            var intent = Intent(this@LoginActivity, MainActivity::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        }
    }

    private var err_cb = object : (() -> Unit) {
        override fun invoke()
        {
            /* reset button heights on failed attempt */
            login_button.elevation = BUTTON_ELEVATION
            register.elevation = BUTTON_ELEVATION
        }
    }


    private fun setup_login_onclick()
    {
        /* Setup Button OnClick Listeners */

        login_button.setOnClickListener {
            login_button.elevation = 0f
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

                var handler =
                    LoginHandler(lpacket, login_cb, err_cb)
                handler.login()
            }
        }

        register.setOnClickListener {
            register.elevation = 0f
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

                var handler =
                    LoginHandler(lpacket, register_cb, err_cb)
                handler.register()
            }
        }
    }

}
