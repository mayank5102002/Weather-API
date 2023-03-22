package com.example.weatherapi.models

import java.io.Serializable
import java.util.*

data class Weather(
    var temp : String,
    var minTemp : String,
    var maxTemp : String,
    var description : String,
    val timeUpdated : Date
) : Serializable