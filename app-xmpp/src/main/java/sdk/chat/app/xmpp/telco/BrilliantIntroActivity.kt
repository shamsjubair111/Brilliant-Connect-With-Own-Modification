package sdk.chat.app.xmpp.telco

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.rd.PageIndicatorView
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.activities.BaseActivity
import sdk.guru.common.RX

class BrilliantIntroActivity: BaseActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var pageIndicatorView: PageIndicatorView
    lateinit var adapter: BrilliantIntroPagerAdapter
    lateinit var progressBar: ProgressBar

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }
    override fun getLayout(): Int {
        return R.layout.activity_brilliant_intro
    }

    var tvNext: TextView? = null
    var ivNext: ImageView? = null
    private val PERMISSIONS_REQUEST_CODE = 100



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBar = findViewById(R.id.progressBar)

        viewPager = findViewById(R.id.viewPager)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)

        io.reactivex.plugins.RxJavaPlugins.setErrorHandler(this)

        adapter = BrilliantIntroPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageIndicatorView.setSelected(position)
                if (position == adapter.fragments.size - 1) {
                    tvNext?.visibility = View.VISIBLE
                    ivNext?.visibility = View.INVISIBLE
                } else {
                    tvNext?.visibility = View.INVISIBLE
                    ivNext?.visibility = View.VISIBLE
                }
            }
        })

        pageIndicatorView.selectedColor = getColor(R.color.textPrimary)
        pageIndicatorView.unselectedColor = getColor(R.color.gray_very_light_accent)
        pageIndicatorView.radius = 4

        pageIndicatorView.count = adapter.fragments.count()

       findViewById<ImageView>(R.id.ivBack)?.let {
            it.setOnClickListener {
                viewPager.currentItem = 0.coerceAtLeast(viewPager.currentItem - 1)
            }
        }

        tvNext = findViewById(R.id.tvNext)
        tvNext?.let {
            it.setOnClickListener {
                authenticate()
            }
        }

        ivNext = findViewById(R.id.ivForward)
        ivNext?.let {
            it.setOnClickListener {
                viewPager.currentItem = (adapter.fragments.count() - 1).coerceAtMost(viewPager.currentItem + 1)
//                if (viewPager.currentItem == adapter.fragments.count() - 1) {
//                    val intent = Intent(this, LoginActivity::class.java)
//                    ChatSDK.ui().startActivity(this, intent)
//                }
            }
        }
        viewPager.adapter = adapter

    }

    override fun onResume() {
        super.onResume()

        // TODO: Testing
        val pushAllowed = ChatSDK.shared().preferences.getBoolean("brilliant-push", false)
        if (!pushAllowed) {
            showPushAlert()
        }
        endAuthenticating()
    }

    fun authenticate() {
        if (ChatSDK.auth() != null) {
            if (ChatSDK.auth().isAuthenticatedThisSession) {
                ChatSDK.ui().startMainActivity(this)
                return
            } else if (ChatSDK.auth().cachedCredentialsAvailable()) {
                startAuthenticating()
                dm.add(ChatSDK.auth().authenticate()
                    .observeOn(RX.main())
                    .doFinally {
                        endAuthenticating()
                    }
                    .subscribe {
                        ChatSDK.ui().startMainActivity(this)
                    })
            }
        }
    }

    fun startAuthenticating() {
        progressBar.visibility = View.VISIBLE
        tvNext?.isEnabled = false
    }

    fun endAuthenticating() {
        progressBar.visibility = View.GONE
        tvNext?.isEnabled = true
    }

    fun showPushAlert() {
        // Inflate the custom layout
        val customView = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null)

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .create()

        customView.findViewById<TextView>(R.id.dialog_text)?.let {

        }
        customView.findViewById<Button>(R.id.button_positive)?.setOnClickListener {
            ChatSDK.shared().preferences.edit().putBoolean("brilliant-push", true).commit()
            enablePush()
            requestBothPermissions()
            dialog.dismiss()
        }
        customView.findViewById<Button>(R.id.button_negative)?.setOnClickListener {
            ChatSDK.shared().preferences.edit().putBoolean("brilliant-push", false).commit()
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    fun enablePush() {
//        FirebasePushModule.shared().activate(this)
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun requestBothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Check if both permissions are granted
            val contactsPermissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val recordPermissionGranted = grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED
            if (contactsPermissionGranted && recordPermissionGranted) {
                // Both permissions granted
                // Add your logic here
            } else {
                // Handle denied permissions
            }
        }
    }
}