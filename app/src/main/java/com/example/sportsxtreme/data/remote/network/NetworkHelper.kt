package com.example.sportsxtreme.data.remote.network

import com.example.sportsxtreme.common.Resource
import java.io.IOException
import retrofit2.HttpException

object NetworkHelper {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return try {
            Resource.Success(apiCall())
        } catch (exception: HttpException) {
            Resource.Error(exception.message())
        } catch (exception: IOException) {
            Resource.Error(exception.message ?: "Network error")
        } catch (exception: Exception) {
            Resource.Error(exception.message ?: "Unexpected error")
        }
    }
}
