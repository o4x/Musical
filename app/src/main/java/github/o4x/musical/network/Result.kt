package github.o4x.musical.network

/**
 * Generic class that holds the network state
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    object Loading : Result<Nothing>()
    data class Error(val error: Exception) : Result<Nothing>()
}