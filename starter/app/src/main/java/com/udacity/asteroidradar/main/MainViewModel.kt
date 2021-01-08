package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabaseDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

enum class AsteroidApiStatus {LOADING, ERROR, DONE}

class MainViewModel(val database: AsteroidDatabaseDao, application: Application) :
    AndroidViewModel(application) {
/*    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }*/

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<AsteroidApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    private val _response = MutableLiveData<ArrayList<Asteroid>>()

    val response: LiveData<ArrayList<Asteroid>>
        get() = _response

    private var asteroid = MutableLiveData<Asteroid?>()

    private var asteroids = database.getAllNights()

    init {
        getAsteroidData()
    }

    private fun getAsteroidData() {

        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            try {
                val response = AsteroidApi.retrofitService.getAsteroid(
                    getToday(),
                    getDaysLater(Constants.DEFAULT_END_DATE_DAYS),
                    Constants.API_KEY
                )
                _response.value = parseAsteroidsJsonResult(JSONObject(response))
                _status.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                _status.value = AsteroidApiStatus.ERROR
                _response.value = ArrayList()
            }
        }

    }


/*    fun getAsteroidsList() : ArrayList<Asteroid>{
        return parseAsteroidsJsonResult(JSONObject(_response.value!!))
    }*/

    private fun getToday(): String {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    private fun getDaysLater(later: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, later)
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

/*    private fun initializeAsteroid() {
        // open a thread to do something
        viewModelScope.launch {
            //asteroids.value = getAsteroidsFromDatabase()
        }
    }*/
}