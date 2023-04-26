package com.example.faceliveness

import android.content.Context
import android.view.View
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val ANIMATION_FAST_MILLIS = 50L

fun View.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

fun getFile(context : Context?): File {
    val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    val date : String = formatter.format(Date()).toString()
    return File(context?.filesDir,"$date.png")
}