package org.griffin.sclfg.Login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieComposition
import kotlinx.android.synthetic.main.activity_login.*
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.Utils.Cache.LocalCache
import org.griffin.sclfg.R
import org.griffin.sclfg.View.Home.MainActivity


class LoginActivity : AppCompatActivity() {

    private val vm : ViewModel  by viewModels()

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
//        textView2.visibility = View.INVISIBLE
//        email.visibility = View.GONE
//        password.visibility = View.GONE
        login_button.visibility = View.GONE
        register_button.visibility = View.GONE
    }

    private fun unhideUI() {
        /* un-hide sign in tools */
        login_container.visibility = View.VISIBLE
        space_stars.visibility = View.VISIBLE
        space_stars.setAnimation("space.json")
        space_stars.speed = 0.3f
        space_stars.playAnimation()
        space_stars.loop(true)
//        textView2.visibility = View.VISIBLE
//        email.visibility = View.VISIBLE
//        password.visibility = View.VISIBLE
        login_button.visibility = View.VISIBLE
        register_button.visibility = View.VISIBLE

        login_bar.visibility = View.INVISIBLE
    }

    private fun getCache(): LocalCache {
        return LocalCache(this)
    }

    var cachedLogin = fun(user: String, psw: String) {
        LoginHandler(LoginHandler.User(user, psw), err_cb).apply {
            login(already_cached_cb)
        }
    }

    /*
        Callback functions to pass to login_handler.
        Decouples the context logic with logging in for intent purposes
     */
    var login_cb = fun() {

        vm.getUser().observe(this, Observer {
            var intent = Intent(this@LoginActivity, MainActivity::class.java)
            ContextCompat.startActivity(this@LoginActivity, intent, null)
        })


    }

    var cache_login_cb = fun(user: LoginHandler.User) {
        localCache.cacheCredentials(user)
        login_cb()
    }

    var already_cached_cb = fun(user: LoginHandler.User) {
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

    private fun setupLoginOnclick() {
        /* Setup Button OnClick Listeners */

        login_button.setOnClickListener {
            login_button.elevation = 0f
            var lpacket = LoginHandler.User(email = "", password = "")
            var err = false

            if (email.text.isNotBlank() && password.text.isNotBlank()) {
                lpacket.email = email.text.toString()
                lpacket.password = password.text.toString()
            } else {
                err = true
            }

            if (err) {
                Toast.makeText(
                    this,
                    "Either email or password was incorrect!", Toast.LENGTH_SHORT
                ).show()
                err_cb()
            } else /* Handle Login Request */ {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

                LoginHandler(lpacket, err_cb).apply {
                    login(cache_login_cb)
                }
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
