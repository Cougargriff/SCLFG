package org.griffin.sclfg.Login

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.griffin.sclfg.Models.Groups
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Cache.LocalCache
import org.griffin.sclfg.View.Home.HomeActivity
import kotlin.coroutines.CoroutineContext

class RegisterActivity : AppCompatActivity(), CoroutineScope {
    private val BUTTON_ELEVATION by lazy {
        applicationContext.resources.displayMetrics.density * 8
    }
    private lateinit var localCache: LocalCache
    private lateinit var display_name: String

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reg_anim_ship.apply {
            setAnimation("reg_ship.json")
            speed = 0.25f
            loop(true)
            playAnimation()
        }

        localCache = LocalCache(this)
        setupRegisterOnclick()
    }

    private var err_cb = fun() {
        /* reset button heights on failed attempt */
        signup_button.elevation = BUTTON_ELEVATION
    }

    private fun hideUi() {
        register_container.visibility = View.GONE
    }

    private fun startAnimation(cb_end: () -> Unit, cb_start: () -> Unit) {
        reg_anim.visibility = View.VISIBLE
        reg_anim.apply {
            setAnimation("register_loading.json")
            playAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    cb_end()
                }
                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) {
                    cb_start()
                }
            })
        }
    }

    var cache_register_cb = fun(user: EmailPasswordLoginHandler.User, uid: String) {
        hideUi()
        localCache.cacheCredentials(user)
        startAnimation({
            register_cb(uid)
        }, {
            reg_anim_ship.visibility = View.GONE
            signup_button.visibility = View.GONE
        })
    }

    var register_cb = fun(uid: String) {
        /* INIT USER w/ display name */
        launch {
            initUser(uid) {
                var intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                intent.putExtra("display_name", display_name)
                ContextCompat.startActivity(this@RegisterActivity, intent, null)
            }
        }
    }

    private suspend fun initUser(uid: String, gotoMain: () -> Unit) {
        Groups().initUser(display_name) {
            gotoMain()
        }
    }

    private fun validateFields(rpacket : EmailPasswordLoginHandler.User) : Boolean {
        var err = false
        /*
            Check all fields for non empty.
            Check passwords match
         */
        if (emailR.text.isNotBlank() &&
            (passwordR.text.isNotBlank() && passwordR_confirm.text.isNotBlank()) &&
            passwordR.length() >= 6 && passwordR_confirm.length() >= 6 &&
            (passwordR.text.toString().compareTo(passwordR_confirm.text.toString()) == 0) &&
            screen_name.text.isNotBlank() && screen_name.text.length <= 20
        ) {
            rpacket.email = emailR.text.toString()
            rpacket.password = passwordR.text.toString()
            display_name = screen_name.text.toString()
        } else {
            err = true
        }
        if (err) {
            return false
        }
            return true
    }

    private fun setupRegisterOnclick() {
        signup_button.setOnClickListener {
            signup_button.elevation = 0f
            var rpacket = EmailPasswordLoginHandler.User(email = "", password = "")
            if(validateFields(rpacket)) {
                Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()
                EmailPasswordLoginHandler(rpacket, err_cb).apply {
                    register(cache_register_cb)
                }
            } else {
                Toast.makeText(
                    this,
                    "Valid Email?\nPassword Length >= 6\nDisplay Name Length <= 20", Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}