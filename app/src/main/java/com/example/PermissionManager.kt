package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

object PermissionManager{


    fun isExist_deniedPermission(context: Context, permissions: Array<out String>) : Boolean
            = deniedPermListOf(context, permissions).isNotEmpty()

    fun deniedPermListOf(context: Context, permissions: Array<out String>): Array<String>
        = permissions.filter {
            PackageManager.PERMISSION_GRANTED !=
            ContextCompat.checkSelfPermission(context, it)
        }.toTypedArray()

    fun showRequest(activity: Activity, permissions: Array<out String>, permissionCode: Int
                    , forFeature: String, permNameForUser: String)
    {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(forFeature + "기능을 위해 " + permNameForUser + "권한이 필요합니다.")
        builder.setPositiveButton("예",
            object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, id: Int)
                    = ActivityCompat.requestPermissions(activity, permissions, PERMISSIONCODE_Essential)
            })
        builder.setNegativeButton("아니오(종료)",
            object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int){
                    activity.finishActivity(0)
                    activity.finish()
                    System.runFinalization()
                    android.os.Process.killProcess(android.os.Process.myPid() );
                }
            })

        builder.show()
    }
}