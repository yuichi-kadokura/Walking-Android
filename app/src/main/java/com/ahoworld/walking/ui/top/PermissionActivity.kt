package com.ahoworld.walking.ui.top

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.ahoworld.walking.R

class PermissionActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION = 1000
    }

    //------------------
    // Override Methods
    //------------------
    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // スプラッシュを消します。
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission()
        } else {
            startMapsActivity()
        }
    }

    /**
     * 権限の結果を受け取ります。
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMapsActivity()
        }
    }

    //-----------------
    // Private Methods
    //-----------------
    /**
     * 位置情報許可を確認します。
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startMapsActivity()
        } else {
            requestLocationPermission()
        }
    }

    /**
     * 位置情報の許可を求めます。
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this@PermissionActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION)
        } else {
            val toast = Toast.makeText(this,resources.getText(R.string.text_permission), Toast.LENGTH_SHORT)
            toast.show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION)
        }
    }

    /**
     * メイン画面に遷移します。
     */
    private fun startMapsActivity() {
        val intent = Intent(application, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
