/*
 * This is a weather forecast application made for educational purposes,
 * The app has been developed in a compact manner to provide essential elements displayed in general weather forecast apps.
 * Powering this App's data is OpenWeatherMap API -> https://openweathermap.org/
 *
 * Please be aware that you will need to replace the API keys which can be found in the URL_Helper class inside utils folder.
 * Currently the first time default starting locations are fixed on Muscat,London,Tokyo for experimentation purposes,
 * whatever location you select next via Google Maps API will be saved and reused until changed.
 *
 * Current App Features:
 * - Select cities/location via Google Map API (Change via menu or long hold location button)
 * - Shows current weather condition.
 * - Shows daily forecast for the upcoming 10 days.
 * - Shows weekly forecast.
 * - Refresh data, press the floating action button at the bottom.
 * - Data persistence, latest API data fetched will be stored in shared preferences to be used as a backup when there's no network connection.
 *
 * This project utilizes the following libraries and resources:
 * - Anko. (SDK, coroutines and commons)
 * - Iconics-core.
 * - Weather-Icons.
 * - Font-Awesome.
 * - Google gson for easy JSON parsing.
 * - MPAndroidChart for charts and graphs.
 * - Google Maps API.
 * - OpenWeatherMap API.
 *
 * Created on 27th Feb 2018
 *
 * @Author -> Fahad Al Shidhani (NoCakeNoCode)
 *
import shibe.doge.*
░░░░░░░░░▄░░░░░░░░░░░░░░▄░░░░
░░░░░░░░▌▒█░░░░░░░░░░░▄▀▒▌░░░
░░░░░░░░▌▒▒█░░░░░░░░▄▀▒▒▒▐░░░
░░░░░░░▐▄▀▒▒▀▀▀▀▄▄▄▀▒▒▒▒▒▐░░░
░░░░░▄▄▀▒░▒▒▒▒▒▒▒▒▒█▒▒▄█▒▐░░░
░░░▄▀▒▒▒░░░▒▒▒░░░▒▒▒▀██▀▒▌░░░
░░▐▒▒▒▄▄▒▒▒▒░░░▒▒▒▒▒▒▒▀▄▒▒▌░░
░░▌░░▌█▀▒▒▒▒▒▄▀█▄▒▒▒▒▒▒▒█▒▐░░
░▐░░░▒▒▒▒▒▒▒▒▌██▀▒▒░░░▒▒▒▀▄▌░
░▌░▒▄██▄▒▒▒▒▒▒▒▒▒░░░░░░▒▒▒▒▌░
▀▒▀▐▄█▄█▌▄░▀▒▒░░░░░░░░░░▒▒▒▐░
▐▒▒▐▀▐▀▒░▄▄▒▄▒▒▒▒▒▒░▒░▒░▒▒▒▒▌
▐▒▒▒▀▀▄▄▒▒▒▄▒▒▒▒▒▒▒▒░▒░▒░▒▒▐░
░▌▒▒▒▒▒▒▀▀▀▒▒▒▒▒▒░▒░▒░▒░▒▒▒▌░
░▐▒▒▒▒▒▒▒▒▒▒▒▒▒▒░▒░▒░▒▒▄▒▒▐░░
░░▀▄▒▒▒▒▒▒▒▒▒▒▒░▒░▒░▒▄▒▒▒▒▌░░
░░░░▀▄▒▒▒▒▒▒▒▒▒▒▄▄▄▀▒▒▒▒▄▀░░░
░░░░░░▀▄▄▄▄▄▄▀▀▀▒▒▒▒▒▄▄▀░░░░░
░░░░░░░░░▒▒▒▒▒▒▒▒▒▒▀▀░░░░░░░░
wer am I !?
wow
much code violence
such programming
Omae wa mou.. Shindeiru..................NANIIII???!!!!!o_O?

*/

package com.nocakenocode.tenkiforecast.features.forecast

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.gson.Gson
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.nocakenocode.tenkiforecast.R
import com.nocakenocode.tenkiforecast.features.location.MapActivity
import com.nocakenocode.tenkiforecast.data.model.CurrentWeather
import com.nocakenocode.tenkiforecast.data.model.DailyWeather
import com.nocakenocode.tenkiforecast.utils.URL_Helper
import com.nocakenocode.tenkiforecast.utils.WeatherIconHelper
import kotlinx.android.synthetic.main.activity_app.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.longToast
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.uiThread
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class App : AppCompatActivity(), WeeklyForecastAdapter.ItemClickListener {

    // Store Current Info for Each Location
    private var location = arrayOfNulls<CurrentWeather>(3)

    // Store Daily Info to be displayed on the graph as shown on OWM website
    private var locationDaily = arrayOfNulls<DailyWeather>(3)

    // Current active location slot
    private var currentActiveLocation = 0

    // Floating Actions Buttons Array
    private var fabArr = arrayOfNulls<FloatingActionButton>(3)

    // Shared preferences instance, used to get previously stored locations
    private lateinit var sharedPref: SharedPreferences

    // 7 days Forecast View Adapter
    private lateinit var adapter: WeeklyForecastAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        fabArr = arrayOf(fab1, fab2, fab3)
        sharedPref = this@App.getSharedPreferences("Wasabi", Context.MODE_PRIVATE)


        // Initialize Current Weather Data
        initCurrentWeather()

        // Daily Forecast data for the Chart
        initDailyForecast()

        // FAB's actions to update all 3 locations
        fab1.onClick { onTapAction(0) }
        fab2.onClick { onTapAction(1) }
        fab3.onClick { onTapAction(2) }

        // change location on long click
        fab1.onLongClick { onLongAction(0) }
        fab2.onLongClick { onLongAction(1) }
        fab3.onLongClick { onLongAction(2) }

        // control the clicking action on the floating action button
        fab.setOnClickListener { view ->

            // Custom animation , Clockwise
            val animCWInf = loadAnimation(applicationContext, R.anim.clockwise_infinite)

            // animate floating action button
            fab.startAnimation(animCWInf)

            // refresh weather and forecast data
            initCurrentWeather()
            initDailyForecast()

            // bar displayed at the bottom of the app
            val snackBar = Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT)
            snackBar.setAction("Action", null).show()
            snackBar.setAction("Dismiss", {
                fab.clearAnimation()
                snackBar.dismiss()
            })
        }

    }

    private fun onTapAction(slot: Int) {
        setFabColorInactive()
        currentActiveLocation = slot
        setFabColorActive()
        updateCurrentWeatherInfo(slot)
        updateDailyForecast(slot)
    }

    private fun onLongAction(slot: Int) {
        if (isConnected(this@App)) {
            fabArr[slot]?.performClick()
            val intent = Intent(this@App, MapActivity::class.java)
            intent.putExtra("slot", slot)
            intent.putExtra("lon", location[slot]!!.coordData.coord_lon)
            intent.putExtra("lat", location[slot]!!.coordData.coord_lat)
            startActivityForResult(intent, 1)
        } else
            longToast(R.string.no_connectivity)
    }

    private fun setFabColorInactive() {
        fabArr[currentActiveLocation]?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_grey))
    }

    private fun setFabColorActive() {
        fabArr[currentActiveLocation]?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryDark))
    }

    // For "Current" Weather reports for each location, will be further optimized in the future.
    private fun initCurrentWeather() {
        populateCurrentWeather(0)
        populateCurrentWeather(1)
        populateCurrentWeather(2)
    }

    // Save data once to be reused for "Current" Weather reports
    private fun populateCurrentWeather(location_position: Int) {

        doAsync {

            // Using URL helper class to construct the proper URL by feeding it the location id
            val owmURL = when (location_position) {
                0 -> URL_Helper.getCurrentWeatherURLByID(sharedPref!!.getString(getString(R.string.location_id_1), getString(R.string.location_id_1)))
                1 -> URL_Helper.getCurrentWeatherURLByID(sharedPref!!.getString(getString(R.string.location_id_2), getString(R.string.location_id_2)))
                else -> URL_Helper.getCurrentWeatherURLByID(sharedPref!!.getString(getString(R.string.location_id_3), getString(R.string.location_id_3)))
            }

            // determine if there's no internet connection to decide to use sharedPreferences
            // Fetch Data from API
            val result: String

            if (!isConnected(this@App)) {
                result = setCurrentWeatherJSON(location_position, "location${location_position + 1}_CW_json")
            } else {
                result = URL(owmURL).readText()
                val json = Gson().fromJson(result, CurrentWeather::class.java)
                location[location_position] = json
            }

            // store json object using google gson
            val prefsEditor = sharedPref!!.edit()
            prefsEditor.putString("location${location_position + 1}_CW_json", result)

            prefsEditor.apply()


            uiThread {
                updateCurrentWeatherInfo(currentActiveLocation)
            }
        }
    }

    private fun setCurrentWeatherJSON(location_position: Int, location_string: String): String {
        val gson = Gson()
        val json = sharedPref!!.getString(location_string, "")
        val obj = gson.fromJson(json, CurrentWeather::class.java)
        location[location_position] = obj
        return gson.toJson(obj)
    }

    /*
        This function will update the UI elements with current weather results fetched earlier and stored in the API

        Argument -> location_position (Integer)

        The argument supplied should be between 0 and 2, the number supplied will denote the location.
     */

    @SuppressLint("PrivateResource")
    private fun updateCurrentWeatherInfo(location_position: Int) {

        // Make changes in the UI , Always check for null

        val data = location[location_position]

        if (data != null) {
            location_name.text = data.location_name.toUpperCase()

            // left to right sliding animation
            location_name.animation = loadAnimation(applicationContext, R.anim.left_to_right_slide)

            temperature.text = resources.getString(R.string.temperature_tv, data.main.temp)

            // grow fade in animation
            temperature.animation = loadAnimation(applicationContext, R.anim.abc_grow_fade_in_from_bottom)

            description.text = data.weather.firstOrNull()!!.weather_description.toUpperCase()

            weatherIcon.setImageDrawable(IconicsDrawable(applicationContext)
                    .icon(WeatherIconHelper.getNeutralWeatherIcon(data.weather.firstOrNull()!!.weather_condition_id))
                    .color(Color.WHITE)
                    .sizeDp(78))

            // right to left slide animation
            weatherIcon.animation = loadAnimation(applicationContext, R.anim.right_to_left_slide)

            low_high_temp.text =
                    resources.getString(
                            R.string.low_high_temp_tv,
                            data.main.temp_min,
                            data.main.temp_max
                    )


            humidity2.text =
                    resources.getString(
                            R.string.humidity_tv,
                            data.main.humidity
                    )

            wind_speed2.text =
                    resources.getString(
                            R.string.wind_speed_tv,
                            data.wind.wind_speed
                    )

            wind_direction.rotation = data.wind.wind_deg.toFloat()
            wind_direction2.text = resources.getString(R.string.wind_direction_tv, data.wind.wind_deg.toInt())

            //wind_direction2.text = "N/A"

            pressure2.text = resources.getString(R.string.pressure_tv, data.main.pressure)

            weather_visibility3.text = resources.getString(R.string.visibility_tv, (data.visibility / 1000))
        }
    }

    private fun initDailyForecast() {
        populateDailyForecast(0)
        populateDailyForecast(1)
        populateDailyForecast(2)
    }

    // Connect to API using ANKO library and Google GSON to retrieve and store data to be later used for daily forecasts
    private fun populateDailyForecast(location_position: Int) {

        doAsyncResult {

            // Using URL helper class to construct the proper URL by feeding it the location id
            val owmURL = when (location_position) {
                0 -> URL_Helper.getDailyForecastURLByID(
                        sharedPref!!.getString(getString(R.string.location_id_1), getString(R.string.location_id_1)), 10
                )
                1 -> URL_Helper.getDailyForecastURLByID(
                        sharedPref!!.getString(getString(R.string.location_id_2), getString(R.string.location_id_2)), 10
                )
                else -> URL_Helper.getDailyForecastURLByID(sharedPref!!.getString(
                        getString(R.string.location_id_3), getString(R.string.location_id_3)), 10
                )
            }

            // determine if there's no internet connection to decide to use sharedPreferences
            // debug version, will be optimized later
            // Fetch Data from API
            val result: String

            if (!isConnected(this@App)) {
                result = setDailyForecastJSON(location_position, "location${location_position + 1}_DF_obj")
            } else {
                result = URL(owmURL).readText()
                val json = Gson().fromJson(result, DailyWeather::class.java)
                locationDaily[location_position] = json
            }

            // store json object using google gson
            val prefsEditor = sharedPref!!.edit()//fab.clearAnimation()
            // end fab animation at the end of the last API call by reloading it with a finite animation
            prefsEditor.putString("location${location_position + 1}_DF_obj", result)

            prefsEditor.apply()

            uiThread {
                updateDailyForecast(currentActiveLocation)
                // end fab animation at the end of the last API call by reloading it with a finite animation
                if (location_position == 2) {
                    //fab.clearAnimation()
                    fab.animation = loadAnimation(applicationContext, R.anim.clockwise)
                }
            }
        }
    }

    private fun setDailyForecastJSON(location_position: Int, location_string: String): String {
        val gson = Gson()
        val json = sharedPref!!.getString(location_string, "")
        val obj = gson.fromJson(json, DailyWeather::class.java)
        locationDaily[location_position] = obj
        return gson.toJson(obj)
    }

    // function used to check for network connection
    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateDailyForecast(location_position: Int) {

        // in this example, a LineChart is initialized from xml
        val chart = findViewById<View>(R.id.chart) as BarChart
        var entries: List<BarEntry> = ArrayList()
        val data = locationDaily[location_position]

        if(data != null){
            chart.clear()

            val sdf = SimpleDateFormat("D")

            for (i in 0 until 10) {

                val unixSeconds: Long = data.infoDailyWeatherList[i].dt
                // convert seconds to milliseconds
                val date = Date(unixSeconds * 1000L)
                val temperature = data.infoDailyWeatherList[i].temp.day
                val dayInYear = sdf.format(date)
                // Add data
                entries += (BarEntry(dayInYear.toFloat(), temperature.toFloat()))

            }

            val dataSet = BarDataSet(entries, "DAILY TEMPERATURE") // add entries to dataset
            dataSet.color = Color.parseColor("#00b1f2")
            dataSet.valueTextColor = Color.WHITE
            chart.legend.textColor = Color.WHITE
            chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            chart.axisLeft.textColor = Color.WHITE
            chart.axisRight.textColor = Color.WHITE
            chart.xAxis.textColor = Color.WHITE

            val xAxisFormatter: IAxisValueFormatter = CustomXAxisValueFormatter()

            val xAxis: XAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f // only intervals of 1 day
            xAxis.labelCount = 10
            xAxis.valueFormatter = xAxisFormatter
            xAxis.labelRotationAngle = -45f

            val barData = BarData(dataSet)
            barData.barWidth = 0.9f // set custom bar width
            chart.data = barData
            chart.setFitBars(true) // make the x-axis fit exactly all bars
            chart.description.isEnabled = false
            chart.invalidate() // refresh

            // commence updating weekly forecast
            updateWeeklyForecast(location_position)
        }

    }

    private fun updateWeeklyForecast(location_position: Int) {
        // set up the RecyclerView
        val recyclerView = rvWeekly
        val horizontalLayoutManager = LinearLayoutManager(this@App, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = horizontalLayoutManager
        adapter = WeeklyForecastAdapter(this, locationDaily[location_position]!!)
        adapter!!.setClickListener(this)
        recyclerView.adapter = adapter
    }

    // perform actions when MapActivity is over
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                fab.performClick() // refresh
            }

            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    /* To be implemented later in case needed*/
    override fun onItemClick(view: View, position: Int) {
        //toast("""You clicked ${adapter!!.getItem(position)} on item position $position""")
    }

    // Inject into context to use Android Icons
    // More from https://github.com/mikepenz/Android-Iconics
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_app, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {

            return true
        } else if (id == R.id.action_edit_current) {

            when (currentActiveLocation) {
                0 -> fab1.performLongClick()
                1 -> fab2.performLongClick()
                2 -> fab3.performLongClick()
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
