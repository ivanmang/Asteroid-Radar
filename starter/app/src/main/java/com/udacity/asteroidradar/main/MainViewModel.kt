package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

enum class AsteroidApiStatus {LOADING, ERROR, DONE}
enum class ApiFilter { WEEK, DAY, SAVED }

class MainViewModel(application: Application) :
    AndroidViewModel(application) {

    private val _image = MutableLiveData<PictureOfDay>()

    val image: LiveData<PictureOfDay>
        get() = _image

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()

    val navigateToSelectedAsteroid : LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<AsteroidApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<AsteroidApiStatus>
        get() = _status


    init {
        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            try {
                asteroidRepository.refreshAsteroids()
                _status.value = AsteroidApiStatus.DONE
                getImageOfTheDay()
            } catch (e :Exception){
                _status.value = AsteroidApiStatus.ERROR
            }
        }

    }
    private val apiFilter = MutableLiveData<ApiFilter>(ApiFilter.WEEK)

    val asteroidsList = Transformations.switchMap(apiFilter) {
            when(it) {
                ApiFilter.DAY -> asteroidRepository.todayAsteroids
                ApiFilter.SAVED -> asteroidRepository.savedAsteroids
                else -> asteroidRepository.weekAsteroids
            }

    }


    private fun getImageOfTheDay() {
        viewModelScope.launch {
            try {
                val image = AsteroidApi.retrofitImageService.getPictureOfDay(Constants.API_KEY)
                _image.value = image
            } catch (e: Exception) {
                _image.value = PictureOfDay("","","")
            }
        }
    }

    fun updateFilter(filter: ApiFilter) {
        apiFilter.value = filter
    }


    fun displayPropertyDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayPropertyDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }
}