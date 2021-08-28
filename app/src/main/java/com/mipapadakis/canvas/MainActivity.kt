package com.mipapadakis.canvas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.tools.CvFileHelper
import com.mipapadakis.canvas.tools.ShowTipDialog
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment


private const val ON_BACK_WAIT_TIME_SHORT = 2000L // FYI: Toast.LENGTH_SHORT = 2000ms
//private const val ON_BACK_WAIT_TIME_LONG = 3500L // Toast.LENGTH_LONG = 3500ms
private const val BACK_NOT_PRESSED = 200
private const val BACK_PRESSED_ONCE = 201


class MainActivity : AppCompatActivity(), InterfaceMainActivity{
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var backIsPressed = BACK_NOT_PRESSED
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar)) // In app_bar_main.xml
        toast = Toast(this)
        drawerLayout = findViewById(R.id.drawer_layout) // In activity_main.xml
        navView = findViewById(R.id.nav_view) // In activity_main.xml
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment) // In content_main.xml
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration( setOf( //In menu activity_main_drawer
            R.id.nav_gallery,
            R.id.nav_canvas,
            R.id.nav_about,
            R.id.nav_feedback,
            R.id.nav_rate),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        ShowTipDialog.showTutorialDialog(this)

        navView.menu.findItem(R.id.nav_feedback).setOnMenuItemClickListener {
            val sharingIntent = Intent(Intent.ACTION_SENDTO).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("mipapadakis@uth.gr"))
                putExtra(Intent.EXTRA_SUBJECT, "Canvas App Feedback")
            }
            try { startActivity(Intent.createChooser(sharingIntent, "Choose email client..."))}
            catch(e:Exception){showToast(e.message.toString())}
            true
        }
        navView.menu.findItem(R.id.nav_rate).setOnMenuItemClickListener {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To be taken back to our application after pressing back button, we need to add following flags to intent:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            else goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try { startActivity(goToMarket) }
            catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
            true
        }

        //User wants to open an image using this app
        if(intent.action == Intent.ACTION_VIEW){
            //Receive the image and go to CanvasActivity:
            val cvImageUri = intent.data
            val cvImage = CvFileHelper(this).loadExternalCvImage(cvImageUri)
            if(cvImage==null) exitApp()
            else{
                CanvasActivityData.cvImage = CvImage(cvImage)
                launchCanvasActivity()
            }
        }
    }

    private fun launchCanvasActivity(){
        val intent = Intent(this, CanvasActivity::class.java)
        intent.putExtra(CreateCanvasFragment.IMPORT_CV_IMAGE_INTENT_KEY, "CanvasViewModel.cvImage contains the required cvImage.")
        startActivity(intent)
    }

    /**
    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    */

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
            if(drawerLayout[0].isSelected) showToast("Gallery is selected")
            else if(drawerLayout[1].isSelected) showToast("CreateCanvas is selected")
        }
        else{
            //If createCanvasFragment or AboutFragment are open, pressing back button opens Gallery:
            if(!navView.menu[0].isChecked) {
                super.onBackPressed()
                return
            }

            if(backIsPressed == BACK_NOT_PRESSED){
                backIsPressed = BACK_PRESSED_ONCE
                val timer = object: CountDownTimer(ON_BACK_WAIT_TIME_SHORT, ON_BACK_WAIT_TIME_SHORT) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() { backIsPressed = BACK_NOT_PRESSED }
                }
                showToast("Press back again to exit application")
                timer.start()
            }
            else exitApp()
        }
    }

    private fun exitApp(){
        toast.cancel()
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
        else this.finishAffinity()
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

//todo about
//todo tool menu icons
//todo set function of move-toolbar button to the whole toolbar?
//todo option to hide bottom toolbar? -> when app opens, it's closed
//todo on detach, save canvas as tmp file
//todo update brush patterns

/*

Canvas



Simple painting app



Canvas is a painting app that was created as a university diploma project.
It provides features of layers, brush drawing, eraser, shapes, text, and more.

Feedback is always appreciated!

*/