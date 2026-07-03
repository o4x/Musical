package github.o4x.m2.network

/**
 * Generic class that holds the network state
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    object Loading : Result<Nothing>()
    data class Error(val error: Exception) : Result<Nothing>()
}