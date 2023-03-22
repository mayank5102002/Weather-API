package com.example.weatherapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.databinding.ItemForecastBinding
import com.example.weatherapi.models.CityWeather

class ForecastAdapter(var cityData : ArrayList<CityWeather>) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cityData[position])
    }

    class ViewHolder(val binding : ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item : CityWeather) {
            updateUI(item)
        }

        private fun updateUI(it : CityWeather) {
            var temp = it.temp
            if(temp.contains("."))
                temp = temp.substring(0, temp.indexOf("."))
            val desc = it.description
            var minTemp = it.maxTemp
            if (minTemp.contains("."))
                minTemp = minTemp.substring(0, minTemp.indexOf(".")) + "°C"
            var maxTemp = it.minTemp
            if(maxTemp.contains("."))
                maxTemp = maxTemp.substring(0, maxTemp.indexOf(".")) + "°C"

            val hours = it.timeUpdated.hours
            val minutes = it.timeUpdated.minutes

            val place = it.place

            val hoursString = if(hours < 10) "0$hours" else hours.toString()
            val minutesString = if(minutes < 10) "0$minutes" else minutes.toString()

            val time = "Updated at ${hoursString}:${minutesString}"
            binding.tvUpdateTime.text = time
            binding.minTemp.text = minTemp
            binding.maxTemp.text = maxTemp
            binding.tvTempValue.text = temp
            binding.desc.text = desc
            binding.tvLocation.text = place
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemForecastBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun getItemCount() = cityData.size

}