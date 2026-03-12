package dev.aurakai.auraframefx.domains.genesis.network

import android.content.Context
import dev.aurakai.auraframefx.domains.genesis.network.api.GenesisBackendApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Service class for making API calls.
 * @param context Application context.
 */
class ApiService(context: Context) {

    private var apiToken: String? = null
    private var oauthToken: String? = null

    // Actual network client instance for Genesis backend.
    private var _networkService: GenesisBackendApi? = null

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            // Prefer OAuth token if available, otherwise use API token.
            val token = oauthToken ?: apiToken
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }

            chain.proceed(builder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _networkService = try {
            retrofit.create(GenesisBackendApi::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sets the API token for authentication.
     * @param token The API token.
     */
    fun setApiToken(token: String?) {
        this.apiToken = token
    }

    /**
     * Sets the OAuth token for authentication.
     * @param token The OAuth token.
     */
    fun setOAuthToken(token: String?) {
        this.oauthToken = token
    }

    /**
     * Creates (or retrieves) the actual network service client.
     * @return A network service client instance.
     */
    fun createService(): GenesisBackendApi? {
        return _networkService
    }
}
