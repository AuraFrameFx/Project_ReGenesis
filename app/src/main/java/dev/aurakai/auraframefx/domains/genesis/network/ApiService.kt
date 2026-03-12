package dev.aurakai.auraframefx.domains.genesis.network

import android.content.Context
import dev.aurakai.auraframefx.domains.genesis.network.api.GenesisBackendApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Service class for making API calls.
 * @param context Application context.
 */
class ApiService(context: Context) {

    private var apiToken: String? = null
    private var oauthToken: String? = null

    // Retrofit service instance for Genesis Backend.
    private var _networkService: GenesisBackendApi? = null

    init {
        initializeService()
    }

    /**
     * Initializes the network client (Retrofit).
     */
    private fun initializeService() {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                apiToken?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
                oauthToken?.let { requestBuilder.addHeader("X-OAuth-Token", it) }
                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AuraApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _networkService = retrofit.create(GenesisBackendApi::class.java)
    }

    /**
     * Sets the API token for authentication.
     * @param token The API token.
     */
    fun setApiToken(token: String?) {
        this.apiToken = token
        initializeService() // Reconfigure network client with new token.
    }

    /**
     * Sets the OAuth token for authentication.
     * @param token The OAuth token.
     */
    fun setOAuthToken(token: String?) {
        this.oauthToken = token
        initializeService() // Reconfigure network client with new token.
    }

    /**
     * Creates (or retrieves) the actual network service client.
     * @return A network service client instance.
     */
    fun createService(): GenesisBackendApi? {
        return _networkService
    }
}
