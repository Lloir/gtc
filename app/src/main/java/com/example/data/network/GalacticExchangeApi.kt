package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface GalacticExchangeApi {
    // 🌐 Public Open Endpoints (Subject to 100 units / 5 min rate limit)
    @GET("public/exchange/mat-prices")
    suspend fun getMaterialPrices(): MatPricesResponse

    @GET("public/exchange/mat-prices/{materialId}")
    suspend fun getSingleMaterialPrice(@Path("materialId") materialId: Int): MatPriceDto

    @GET("public/exchange/mat-details/{materialId}")
    suspend fun getMaterialDetails(@Path("materialId") materialId: Int): MatDetailsResponse

    // 🔑 Limited Endpoints (Requires In-Game Company API Key)
    @GET("public/company")
    suspend fun getMyCompany(@Header("Authorization") authHeader: String): PMyCompanyModel

    @GET("public/company/bases")
    suspend fun getMyBases(@Header("Authorization") authHeader: String): List<PBaseDetailResponseModel>

    @GET("public/company/warehouses")
    suspend fun getMyWarehouses(@Header("Authorization") authHeader: String): List<PWarehouseModel>
}

object ApiClient {
    private const val BASE_URL = "https://api.g2.galactictycoons.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val exchangeApi: GalacticExchangeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GalacticExchangeApi::class.java)
    }
}
