package com.example.weatherapi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.weatherapi.databinding.ActivityMainBinding
import com.example.weatherapi.models.CityWeather
import com.example.weatherapi.models.Weather
import com.example.weatherapi.utils.AppPermissions
import com.example.weatherapi.utils.GoogleGPS
import com.example.weatherapi.utils.WeatherAPI
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    private lateinit var sharedPref : SharedPreferences

    private lateinit var viewModel : MainVM

    private val citiesList = listOf("New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne")

    private lateinit var layoutManager: LayoutManager
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter: ForecastAdapter
    private var cityData = arrayListOf<CityWeather>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainVM::class.java]

        init()
        initObservers()
        initPermissions()

        setContentView(binding.root)
    }

    private fun init() {
        sharedPref = getSharedPreferences(resources.getString(R.string.WEATHER_PREFS), Context.MODE_PRIVATE)

        val res = sharedPref.getString(resources.getString(R.string.WEATHER_DATA), null)
        val place = sharedPref.getString(resources.getString(R.string.PLACE_DATA), null)
        if(res != null) {
            val weather = Gson().fromJson(res, Weather::class.java)
            viewModel.updateData(weather)
        }
        if(place != null) {
            viewModel.updatePlace(place)
        }

        for(city in citiesList) {
            val data = sharedPref.getString(city, null)
            if(data != null) {
                val cityWeather = Gson().fromJson(data, CityWeather::class.java)
                cityData.add(cityWeather)
            }
        }

        initAdapter()
    }

    private fun initAdapter() {
        recyclerView = binding.rvForecast
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = ForecastAdapter(cityData)
        recyclerView.adapter = adapter
    }

    private fun initPermissions() {
        if (AppPermissions.checkGpsPermission(this)) {
            locationProvided()
        } else {
            AppPermissions.requestGpsPermission(this)
        }
    }

    private fun checkForInternet() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try{
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    viewModel.getWeatherData(application)
                    viewModel.getPlace(application)
                    viewModel.getCityData(application, citiesList)
                    viewModel.afterLocationFetched()
                }

                override fun onLost(network: Network) {
                    Toast.makeText(this@MainActivity, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun initObservers() {
        GoogleGPS.locationEnabled.observe(this) {
            if(it == true) {
                viewModel.getLocation(this)
                GoogleGPS.afterLocationEnabled()
            }
        }
        viewModel.isLocationFetched.observe(this) {
            if (it) {
                checkForInternet()
            }
        }
        WeatherAPI.weatherUpdated.observe(this) {
            if (it) {
                viewModel.updateData(WeatherAPI.weather.value!!)
            }
        }
        WeatherAPI.placeFetched.observe(this) {
            if (it) {
                viewModel.updatePlace(WeatherAPI.place!!)
            }
        }
        WeatherAPI.cityDataFetched.observe(this) {
            if (it) {
                processCityData(WeatherAPI.cityData)
                adapter.cityData = cityData
                adapter.notifyDataSetChanged()

                for(cityWeather in cityData) {
                    val editor = sharedPref.edit()
                    editor.putString(cityWeather.place, Gson().toJson(cityWeather))
                    editor.apply()
                }
            }
        }
        viewModel.weather.observe(this) {
            updateUI(it)
        }
        viewModel.place.observe(this) {
            updateLocationUI(it)
        }
    }

    private fun processCityData(newCityData : ArrayList<CityWeather>) {
        val map = HashMap<String, CityWeather>()

        for(cityWeather in cityData) {
            map[cityWeather.place] = cityWeather
        }

        for(cityWeather in newCityData) {
            map[cityWeather.place] = cityWeather
        }

        cityData.clear()
        for(cityWeather in map.values) {
            cityData.add(cityWeather)
        }
    }

    private fun updateLocationUI(it : String) {
        binding.tvLocation.text = it

        val editor = sharedPref.edit()
        editor.putString(resources.getString(R.string.PLACE_DATA), it)
        editor.apply()
    }

    private fun updateUI(it : Weather) {
        var temp = it.temp
        temp = temp.substring(0, temp.indexOf("."))
        val desc = it.description
        var minTemp = it.maxTemp
        minTemp = minTemp.substring(0, minTemp.indexOf(".")) + "°C"
        var maxTemp = it.minTemp
        maxTemp = maxTemp.substring(0, maxTemp.indexOf(".")) + "°C"

        val hours = it.timeUpdated.hours
        val minutes = it.timeUpdated.minutes

        val hoursString = if(hours < 10) "0$hours" else hours.toString()
        val minutesString = if(minutes < 10) "0$minutes" else minutes.toString()

        val time = "Updated at ${hoursString}:${minutesString}"
        binding.tvUpdateTime.text = time
        binding.minTemp.text = minTemp
        binding.maxTemp.text = maxTemp
        binding.tvTempValue.text = temp
        binding.desc.text = desc

        val editor = sharedPref.edit()
        editor.putString(resources.getString(R.string.WEATHER_DATA), Gson().toJson(it))
        editor.apply()
    }

    private fun locationProvided() {
        if(GoogleGPS.isLocationEnabled(this)) {
            viewModel.getLocation(this)
        } else {
            GoogleGPS.createGoogleApiClient(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == AppPermissions.GPS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationProvided()
            } else {
                Log.d("Sms Messages Read Permission", "Permission not granted")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleGPS.REQUEST_ENABLE_LOCATION) {
            if (resultCode == RESULT_OK) {
                // Location services are now enabled, start location updates
                GoogleGPS.locationEnabled.value = true
            } else {
                // Location services are still disabled, show an error message or take other action
            }
        }
    }
}