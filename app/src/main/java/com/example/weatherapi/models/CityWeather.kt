package com.example.weatherapi.models

import java.io.Serializable
import java.util.*

data class CityWeather(
    var temp : String,
    var minTemp : String,
    var maxTemp : String,
    var description : String,
    val timeUpdated : Date,
    val place : String
) : Serializable