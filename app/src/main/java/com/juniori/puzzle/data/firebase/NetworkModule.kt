package com.juniori.puzzle.data.firebase

import com.google.gson.Gson
import com.google.gson.GsonBuilder
<<<<<<< HEAD
import com.juniori.puzzle.util.FIRESTORE_BASE_URL
=======
>>>>>>> 4fdae40 (feat: 로딩 dialog 화면 생성 #60)
import com.juniori.puzzle.util.STORAGE_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIME_OUT_MILLIS = 5000L

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT_MILLIS, TimeUnit.MILLISECONDS)
        .readTimeout(TIME_OUT_MILLIS, TimeUnit.MILLISECONDS)
        .writeTimeout(TIME_OUT_MILLIS, TimeUnit.MILLISECONDS)
        .addNetworkInterceptor {
            val request = it.request()
                .newBuilder()
                .build()
            it.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Singleton
    @Provides
<<<<<<< HEAD
    @Named("Firestore")
    fun provideFireStoreRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(FIRESTORE_BASE_URL)
            .build()
=======
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(STORAGE_BASE_URL)
        .build()
>>>>>>> 4fdae40 (feat: 로딩 dialog 화면 생성 #60)

    @Singleton
    @Provides
    @Named("Storage")
    fun provideStorageRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(STORAGE_BASE_URL)
            .build()

    @Singleton
    @Provides
    fun provideFirebaseService(@Named("Firestore") retrofit: Retrofit): FirestoreService =
        retrofit.create(FirestoreService::class.java)

    @Singleton
    @Provides
    fun provideFirebaseRepository(service: FirestoreService): FirestoreDataSource =
        FirestoreDataSource(service)

    @Singleton
    @Provides
    fun provideStorageService(@Named("Storage") retrofit: Retrofit): StorageService =
        retrofit.create(StorageService::class.java)

    @Singleton
    @Provides
    fun provideStorageRepository(service: StorageService): StorageDataSource =
        StorageDataSource(service)
}

