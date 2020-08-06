package org.griffin.sclfg.Login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_login.*
import org.griffin.sclfg.Models.Groups
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Thunks.signInUser
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Cache.LocalCache
import org.griffin.sclfg.View.Home.HomeActivity


class LoginActivity : AppCompatActivity() {


    private val BUTTON_ELEVATION by lazy {
        applicationContext.resources.displayMetrics.density * 8
    }
    private lateinit var localCache: LocalCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        hideUI()
        login_bar.apply {
            imageAssetsFolder = "/assets/"
            setAnimation("spacecraft.json")
            speed = 0.75f
            loop(true)
            playAnimation()
        }

        /* if credentials exist in cache, login with cb method */
        localCache = getCache().apply {
            /* lambda function to update UI after checking cache */
            retrieveCredentials(cachedLogin, fun() {
                unhideUI()
            })
        }
        setupLoginOnclick()
    }

    private fun hideUI() {
        login_container.visibility = View.GONE
        space_stars.visibility = View.GONE
        login_button.visibility = View.GONE
        register_button.visibility = View.GONE
    }

    private fun unhideUI() {
        /* un-hide sign in tools */
        login_container.visibility = View.VISIBLE
        space_stars.visibility = View.VISIBLE
        if (!space_stars.isAnimating) {
            space_stars.setAnimation("reg_ship.json")
            space_stars.speed = 0.2f
            space_stars.playAnimation()
            space_stars.loop(true)
        }
        login_button.visibility = View.VISIBLE
        register_button.visibility = View.VISIBLE
        login_bar.visibility = View.INVISIBLE
    }

    private fun getCache(): LocalCache {
        return LocalCache(this)
    }

    var cachedLogin = fun(user: String, psw: String) {
        EmailPasswordLoginHandler(EmailPasswordLoginHandler.User(user, psw), err_cb).apply {
            login(already_cached_cb)
        }
    }

    /*
        Callback functions to pass to login_handler.
        Decouples the context logic with logging in for intent purposes
     */
    var login_cb = fun() {
            store.dispatch(signInUser())
            var intent = Intent(this@LoginActivity, HomeActivity::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
    }

    var cache_login_cb = fun(user: EmailPasswordLoginHandler.User) {
        localCache.cacheCredentials(user)
        login_cb()
    }

    var already_cached_cb = fun(user: EmailPasswordLoginHandler.User) {
        login_cb()
    }


    private var err_cb = fun() {
        /* reset button heights on failed attempt */
        LocalCache(this).apply {
            clearCache {
                unhideUI()
            }
        }
        login_button.elevation = BUTTON_ELEVATION
    }

    override fun onBackPressed() {
        /* dont go back on login screen */
    }

    private fun validateFields(lpacket : EmailPasswordLoginHandler.User) : Boolean {
        var err = false
        if (email.text!!.isNotBlank() && password.text.isNotBlank()) {
            lpacket.email = email.text.toString()
            lpacket.password = password.text.toString()
        } else {
            err = true
        }

        if (err) {
            return false
        }
        /* Handle Login Request */
        return true
    }

    private fun setupLoginOnclick() {
        /* Setup Button OnClick Listeners */

        login_button.setOnClickListener {
            login_button.elevation = 0f
            var lpacket = EmailPasswordLoginHandler.User(email = "", password = "")
            if(validateFields(lpacket)) {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

                EmailPasswordLoginHandler(lpacket, err_cb).apply {
                    login(cache_login_cb)
                }
            } else {
                Toast.makeText(
                    this,
                    "Either email or password was incorrect!", Toast.LENGTH_SHORT
                ).show()
                err_cb()
            }
        }

        /*
            Go To modal registration screen
         */
        register_button.setOnClickListener {
            var intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        }
    }

}
