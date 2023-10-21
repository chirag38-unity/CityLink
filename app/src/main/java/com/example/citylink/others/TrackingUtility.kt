package com.example.citylink.others

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.citylink.others.Constants.ALL_PERMISSIONS
import com.example.citylink.others.Constants.BLUETOOTHPERMISSIONS
import com.example.citylink.others.Constants.LOCATION_PERMISSION
import com.example.citylink.others.Constants.PERMISSIONS
import com.example.citylink.services.Poly_line
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasNotifPermission (context: Context) : Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
      } else return true
    }

    fun hasPermissions(context: Context) : Boolean = LOCATION_PERMISSION.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // for other devices, this will include checking for Ethernet
            else -> false
        }
    }

    fun hasBluetoothPermissions(context: Context) : Boolean = BLUETOOTHPERMISSIONS.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(context: Context) : Boolean = ALL_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun appSettingOpen(context: Context){
        context.startActivity(Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun warningPermissionDialog(context: Context){
        MaterialAlertDialogBuilder(context)
            .setTitle("Permissions Denied")
            .setMessage("All Permission are Required for this app. App may not work properly if permissions are denied.")
            .setCancelable(false)
            .setPositiveButton("Grant"){ d,_ ->
                d.cancel()
                appSettingOpen(context)
            }
            .setNegativeButton("Cancel"){ d,_ ->
                d.dismiss()
            }
            .create()
            .show()
    }

    fun calculatePolylineLength(polyLine: Poly_line) : Float{
        var distance = 0f
        for(i in 0..polyLine.size-2){
            val pos1 = polyLine[ i ]
            val pos2 = polyLine[ i + 1 ]
            val result = FloatArray(1)
            Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, result)
            distance += result[0]
        }
        return distance
    }

    fun getCurrentTimeStampString() : String {
        val instant: Instant = Instant.now()
        val zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return instant.atZone(zoneId).format(formatter)
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }
    fun getFormattedTransactionTimeStamp(timeStamp : String) : String {

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a")

        // Specify the expected format for parsing
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Parse the input string with the specified time zone
        val inputDateTime = ZonedDateTime.parse(timeStamp, inputFormatter.withZone(ZoneId.of("Asia/Kolkata")))

        val now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))

        // Calculate the time difference
        val daysDiff = ChronoUnit.DAYS.between(inputDateTime, now)

        return when {
            daysDiff < 1 -> "Today, " + inputDateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            daysDiff < 2 -> "Yesterday, " + inputDateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            daysDiff < 7 -> "on " + inputDateTime.format(DateTimeFormatter.ofPattern("EEEE, h:mm a"))
            daysDiff < 365 -> "on " + inputDateTime.format(formatter)
            else -> inputDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"))
        }

    }

}