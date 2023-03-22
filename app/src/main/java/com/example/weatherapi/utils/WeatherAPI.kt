package com.example.weatherapi.utils

import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.example.weatherapi.models.CityWeather
import com.example.weatherapi.models.Weather
import java.util.*

object WeatherAPI {

    val weather = MutableLiveData<Weather>()
    val weatherUpdated = MutableLiveData<Boolean>()

    var place :String? = null
    var placeFetched = MutableLiveData<Boolean>()

    val cityData = arrayListOf<CityWeather>()
    var cityDataFetched = MutableLiveData<Boolean>()

    fun getWeatherData(lat : Double, lon : Double, queue: RequestQueue) {

        //Parameters of the location
        val baseUrl = "https://api.openweathermap.org/data/3.0/onecall?lat=${lat}&lon=${lon}&exclude=minutely,hourly&units=metric&appid=37e4653c7e2d491670486f527875c4e8"

        val w = Weather("20", "10", "30", "Sunny", Date())

        //Creating the Volley request
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            baseUrl,
            null,
            { response ->
                //On success
//                println("response -> $response")

                if(response.isNull("cod")) {
                    val current = response.getJSONObject("current")
                    w.temp = current.getString("temp")
                    val desc =
                        current.getJSONArray("weather").getJSONObject(0).getString("description")
                    val descBuiler = StringBuilder(desc)
                    descBuiler.setCharAt(0, descBuiler[0].uppercaseChar())
                    w.description = descBuiler.toString()

                    val daily = response.getJSONArray("daily").getJSONObject(0)
                    val min = daily.getJSONObject("temp").getString("min")
                    val max = daily.getJSONObject("temp").getString("max")

                    w.minTemp = min
                    w.maxTemp = max

                    weather.postValue(w)
                    weatherUpdated.postValue(true)
                }
            },
            { error ->
                //On failure
                // Handle error
                println("error -> $error")
                throw error
            }) {
        }

        //Adding the request to Volley request queue
        queue.add(jsonObjectRequest)
    }

    fun getPlace(lat : Double, lon : Double, queue: RequestQueue) {
        val baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lon}&key=AIzaSyB0zaBTkirrkosyejNU-trwmZ5Wnohh4bk"

        //Creating the Volley request
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            baseUrl,
            null,
            { response ->
                //On success
                println("response -> $response")

                if(response.getString("status") == "OK") {
                    val result = response.getJSONArray("results").getJSONObject(0)
                    val address = result.getString("formatted_address")
                    place = address
                    placeFetched.postValue(true)
                }
            },
            { error ->
                //On failure
                // Handle error
                println("error -> $error")
                throw error
            }) {
        }

        //Adding the request to Volley request queue
        queue.add(jsonObjectRequest)
    }

    fun getCityData(cityNames : List<String>, queue : RequestQueue) {
        for(city in cityNames) {
            val baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=${city}&key=AIzaSyB0zaBTkirrkosyejNU-trwmZ5Wnohh4bk"

            //Creating the Volley request
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                baseUrl,
                null,
                { response ->
                    //On success
                    println("response -> $response")

                    if(response.getString("status") == "OK") {
                        val result = response.getJSONArray("results").getJSONObject(0)
                        val lat = result.getJSONObject("geometry").getJSONObject("location").getString("lat")
                        val lon = result.getJSONObject("geometry").getJSONObject("location").getString("lng")

                        getCityData(lat.toDouble(), lon.toDouble(), queue, city, cityNames.indexOf(city), cityNames.size)
                    }
                },
                { error ->
                    //On failure
                    // Handle error
                    println("error -> $error")
                    throw error
                }) {
            }

            //Adding the request to Volley request queue
            queue.add(jsonObjectRequest)
        }
    }

    private fun getCityData(lat : Double, lon : Double, queue: RequestQueue, city : String, index : Int, size : Int) {
        val baseUrl = "https://api.openweathermap.org/data/3.0/onecall?lat=${lat}&lon=${lon}&exclude=minutely,hourly&units=metric&appid=37e4653c7e2d491670486f527875c4e8"

        //Creating the Volley request
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            baseUrl,
            null,
            { response ->
                //On success
                println("response -> $response")

                if(response.isNull("cod")) {
                    val current = response.getJSONObject("current")
                    val temp = current.getString("temp")
                    val desc =
                        current.getJSONArray("weather").getJSONObject(0).getString("description")
                    val descBuiler = StringBuilder(desc)
                    descBuiler.setCharAt(0, descBuiler[0].uppercaseChar())
                    val description = descBuiler.toString()

                    val daily = response.getJSONArray("daily").getJSONObject(0)
                    val min = daily.getJSONObject("temp").getString("min")
                    val max = daily.getJSONObject("temp").getString("max")

                    val cityWeather = CityWeather(temp, min, max, description, Date(), city)
                    println("cityWeather -> $cityWeather")
                    cityData.add(cityWeather)
                }

                if(index == size - 1) {
                    println("cityWeather -> final")
                    cityDataFetched.postValue(true)
                }
            },
            { error ->
                //On failure
                // Handle error
                println("error -> $error")
            }) {
        }

        //Adding the request to Volley request queue
        queue.add(jsonObjectRequest)
    }

}