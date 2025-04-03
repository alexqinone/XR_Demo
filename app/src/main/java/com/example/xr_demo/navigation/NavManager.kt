package com.example.xr_demo.navigation

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.setFullSpaceMode
import com.example.xr_demo.MainActivity
import com.example.xr_demo.arcore.ARCoreMainActivity
import com.example.xr_demo.gallery.GalleryActivity
import com.example.xr_demo.museum.ModelLoadActivity
import com.example.xr_demo.rendering.RenderingActivity
import com.example.xr_demo.museum.MuseumPOCActivity

object NavManager {

    private const val TAG = "NavigationManager"

    private val activities = listOf(
        ActivityInfo(
            title = "Hello World Screen",
            activityClass = MainActivity::class.java,
            description = "Default screen with Hello World text"
        ),

        ActivityInfo(
            title = "ARCore Main Screen",
            activityClass = ARCoreMainActivity::class.java,
            description = "The ARCore main screen from google sample"
        ),

        ActivityInfo(
            title = "Gallery Screen",
            activityClass = GalleryActivity::class.java,
            description = "The Gallery sample"
        ),

        ActivityInfo(
            title = "Rendering Screen",
            activityClass = RenderingActivity::class.java,
            description = "3d model rendering and interaction",
            isFullSpace = true
        ),

        ActivityInfo(
            title = "Museum Demo",
            activityClass = MuseumPOCActivity::class.java,
            description = "Demo for showing interaction with 3d models",
            isFullSpace = true
        ),

        ActivityInfo(
            title = "Model Loading",
            activityClass = ModelLoadActivity::class.java,
            description = "Loading 3d models from the internet with url",
            isFullSpace = true
        ),

    )

    fun getActivities(): List<ActivityInfo> = activities

    fun start(context: Context, info : ActivityInfo, session : Session?) {

        val intent = Intent(context, info.activityClass)

        if (info.isFullSpace) {
            Log.i(TAG, "Starting new activity(${info.activityClass.name}) in Full space")
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            var bundle = Bundle()
            var startActivityBundle = session?.setFullSpaceMode(bundle) ?: bundle
            context.startActivity(intent, startActivityBundle)
        } else {
            Log.i(TAG, "Starting new activity(${info.activityClass.name}) in Home space")
            context.startActivity(intent)
        }
    }

}