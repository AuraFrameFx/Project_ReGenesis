package dev.aurakai.auraframefx.domains.genesis.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.aurakai.auraframefx.domains.genesis.network.api.GenesisBackendApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Service class for making API calls.
 * @param context Application context.
 */
class ApiService(context: Context) {

    private var apiToken: String? = null
    private var oauthToken: String? = null

    private var _networkService: GenesisBackendApi? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        synchronized(this) {
            apiToken?.let {
                builder.header("X-API-Token", it)
            }
            oauthToken?.let {
                builder.header("Authorization", "Bearer $it")
            }
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5000/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    init {
        // Initialize network client (Retrofit)
        _networkService = retrofit.create(GenesisBackendApi::class.java)
    }

    /**
     * Sets the API token for authentication.
     * @param token The API token.
     */
    fun setApiToken(token: String?) {
        synchronized(this) {
            this.apiToken = token
        }
    }

    /**
     * Sets the OAuth token for authentication.
     * @param token The OAuth token.
     */
    fun setOAuthToken(token: String?) {
        synchronized(this) {
            this.oauthToken = token
        }
    }

    /**
     * Creates (or retrieves) the actual network service client.
     * @return A network service client instance.
     */
    fun createService(): GenesisBackendApi? {
        return _networkService
    }
}
