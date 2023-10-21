package com.example.citylink.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.example.citylink.R
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur


class LoadingDialog(context: Context) : Dialog(context) {

    private var blurView: BlurView
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog)
        setCancelable(false)
        blurView = findViewById(R.id.loading_blurView)

    }

    private fun blurBackground() {
        val radius = 20f
        val decorView: View = window!!.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background
        blurView.setupWith(rootView, RenderScriptBlur(context))
            .setBlurAutoUpdate(true)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(10F)
    }

    fun setMessage(message: String) {
        val messageTextView = findViewById<TextView>(R.id.loading_message)
        messageTextView.text = message
        blurBackground()
    }

}