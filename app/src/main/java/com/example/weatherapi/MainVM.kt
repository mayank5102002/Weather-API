package com.example.weatherapi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapi.models.Weather
import com.example.weatherapi.utils.VolleySingleton
import com.example.weatherapi.utils.WeatherAPI
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class MainVM : ViewModel() {

    val weather = MutableLiveData<Weather>()
    val place = MutableLiveData<String>()

    var currentLocation : Location? = null
    val isLocationFetched = MutableLiveData<Boolean>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun updateData(weatherData : Weather) {
        weather.postValue(weatherData)
    }

    fun updatePlace(placeData : String) {
        place.postValue(placeData)
    }

    @SuppressLint("MissingPermission")
    fun getLocation(activity: Activity) {
        viewModelScope.launch {
            try {
                // Create a new instance of FusedLocationProviderClient
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

                // Request location updates with the specified LocationRequest and LocationCallback
                val locationRequest = LocationRequest.Builder(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)

                fusedLocationClient.requestLocationUpdates(
                    locationRequest.build(),
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    fun afterLocationFetched() {
        isLocationFetched.postValue(false)
    }

    fun getWeatherData(application: Application) {
        viewModelScope.launch {
            if (currentLocation != null) {
                val queue = VolleySingleton.getInstance(application).requestQueue
                try{
                    WeatherAPI.getWeatherData(currentLocation!!.latitude, currentLocation!!.longitude, queue)
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getPlace(application: Application) {
        viewModelScope.launch {
            if (currentLocation != null) {
                val queue = VolleySingleton.getInstance(application).requestQueue
                try{
                    WeatherAPI.getPlace(currentLocation!!.latitude, currentLocation!!.longitude, queue)
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun destroyLocationProvider() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // Handle the location update
            val location: Location? = locationResult.lastLocation
            handleLocationUpdate(location)
        }
    }

    private fun handleLocationUpdate(location: Location?) {
        viewModelScope.launch {
            if (location != null) {
                currentLocation = location
                isLocationFetched.postValue(true)
                destroyLocationProvider()
            }
        }
    }

    fun getCityData(application: Application, cityList: List<String>) {
        viewModelScope.launch {
            val queue = VolleySingleton.getInstance(application).requestQueue
            try{
                WeatherAPI.getCityData(cityList, queue)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

}