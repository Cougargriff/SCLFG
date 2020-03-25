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
    }

    var login_cb = object : (() -> Unit) {
        override fun invoke() {
            val intent = Intent(this@LoginActivity, MainActivty::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        }
    }

}
