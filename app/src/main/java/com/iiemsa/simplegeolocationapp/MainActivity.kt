package com.iiemsa.simplegeolocationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
import android.location.Geocoder
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAddress: TextView

    // Register once; callback runs after user responds to the permission dialog
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLocation()
            } else {
                tvLatitude.text = "Permission denied"
                tvLongitude.text = "Permission denied"
                tvAddress.text = "Permission denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvLatitude = findViewById(R.id.tv_Latitude)
        tvLongitude = findViewById(R.id.tv_longitude)
        tvAddress  = findViewById(R.id.tv_Address)   // ✅ make sure this ID exists in your XML

        val btnGetLocation: Button = findViewById(R.id.btn_get_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnGetLocation.setOnClickListener {
            getLocation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getLocation() {
        val fineGranted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                tvLatitude.text = "Latitude: ${location.latitude}"
                tvLongitude.text = "Longitude: ${location.longitude}"

                // Geocoder can throw and can be slow; wrap in try/catch
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!results.isNullOrEmpty()) {
                        val adrs = results[0]
                        tvAddress.text = "Address: ${adrs.getAddressLine(0)}"
                    } else {
                        tvAddress.text = "Address not found"
                    }
                } catch (e: IOException) {
                    tvAddress.text = "Address lookup failed"
                } catch (e: Exception) {
                    tvAddress.text = "Address error"
                }
            } else {
                tvLatitude.text = "Latitude: Not available"
                tvLongitude.text = "Longitude: Not available"
                tvAddress.text = "Address: Not available"
            }
        }.addOnFailureListener {
            tvLatitude.text = "Error getting location"
            tvLongitude.text = "Error getting location"
            tvAddress.text = "Address: Unknown"
        }
    }
}
