package com.mipapadakis.canvas

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView


private const val ON_BACK_WAIT_TIME_SHORT = 2000L // FYI: Toast.LENGTH_SHORT = 2000ms
//private const val ON_BACK_WAIT_TIME_LONG = 3500L // Toast.LENGTH_LONG = 3500ms
private const val BACK_NOT_PRESSED = 200
private const val BACK_PRESSED_ONCE = 201

class MainActivity : AppCompatActivity(), InterfaceMainActivity{
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var backIsPressed = BACK_NOT_PRESSED
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        toast = Toast(this)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_gallery, R.id.nav_canvas, R.id.nav_about), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else{
            if(backIsPressed == BACK_NOT_PRESSED){
                backIsPressed = BACK_PRESSED_ONCE
                val timer = object: CountDownTimer(ON_BACK_WAIT_TIME_SHORT, ON_BACK_WAIT_TIME_SHORT) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() { backIsPressed = BACK_NOT_PRESSED }
                }
                showToast("Press back again to exit application")
                timer.start()
            }
            else{
                if (backIsPressed == BACK_PRESSED_ONCE)  toast.cancel()
//                val navController = findNavController(R.id.nav_host_fragment)
//                navController.popBackStack()
//                super.onBackPressed()
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
                else this.finishAffinity()
            }
        }
    }

    override fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}

interface InterfaceMainActivity{
    fun showToast(text: String)
}