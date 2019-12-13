package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager{

    fun existDeniedPermission(context: Context, permissions: Array<out String>) : Boolean
            = deniedPermListOf(context, permissions).isNotEmpty()

    fun deniedPermListOf(context: Context, permissions: Array<out String>): Array<String>
            = permissions.filter {UShort
        (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, it))
                && existOnThisAPILevel(it)
    }.toTypedArray()

    private fun existOnThisAPILevel(permission: String): Boolean
            = when(permission){
        Manifest.permission.ACCESS_BACKGROUND_LOCATION ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        else -> true
    }

    fun showRequestWithShutdownSelection(activity: Activity
                                         , permissions: Array<out String>
                                         , permissionCode: Int
                                         , message: String)
    {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(message)
        builder.setPositiveButton("예") { dialog, id ->
            ActivityCompat.requestPermissions(activity, permissions, permissionCode)
        }
        builder.setNegativeButton("아니오(종료)"
        ) { _, _ ->
            activity.finishActivity(0)
            activity.finish()
            System.runFinalization()
            android.os.Process.killProcess(android.os.Process.myPid() )
        }

        builder.show()
    }

    fun showOnlyRequestAnd(activity: Activity
                           , permissions: Array<out String>
                           , permissionCode: Int
                           , message: String
                           , doingIfNegative: ((DialogInterface, Int) -> Unit)? = null){
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(message)
        builder.setPositiveButton("예") { dialog, id ->
            ActivityCompat.requestPermissions(activity, permissions, permissionCode)
        }
        builder.setNegativeButton("아니오", doingIfNegative)

        builder.show()
    }
}