package com.jeandealmeida_dev.billortest.commons.ui.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Int?.formatInSeconds(): String {
    if(this == null || this <= 0) return "0:00"
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun Long.formatInHoursMinutes(): String {
    val date = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}