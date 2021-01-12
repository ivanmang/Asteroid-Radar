package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


enum class ApiFilter() { WEEK, DAY, SAVED }
class AsteroidRepository(private val database: AsteroidDatabase) {

    var weekAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAsteroidsFromThisWeek(getToday(), getDaysLater(7))
    var todayAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAsteroidToday(getToday())
    var savedAsteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAllAsteroids()

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
                val response = AsteroidApi.retrofitService.getAsteroid(getToday(), getDaysLater(Constants.DEFAULT_END_DATE_DAYS), Constants.API_KEY)
                val asteroids = parseAsteroidsJsonResult(JSONObject(response))
                insertToDatabase(asteroids)
        }
    }

    private suspend fun insertToDatabase(asteroids: ArrayList<Asteroid>) {
        for(asteroid in asteroids) {
            database.asteroidDatabaseDao.insert(asteroid)
        }
    }

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

/*    fun updateQuery(filter: ApiFilter){
        when(filter) {
            ApiFilter.DAY -> asteroids = database.asteroidDatabaseDao.getAsteroidToday(getToday())
            ApiFilter.SAVED -> asteroids = database.asteroidDatabaseDao.getAllAsteroids()
            else -> asteroids = database.asteroidDatabaseDao.getAsteroidsFromThisWeek(getToday(), getDaysLater(7))
        }
    }*/
}