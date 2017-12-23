package de.thegerman.sttt.di.modules

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import de.thegerman.sttt.data.apis.EthereumJsonRpcApi
import de.thegerman.sttt.data.apis.HexNumberAdapter
import de.thegerman.sttt.data.apis.WeiAdapter
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkingModule {

    @Provides
    @Singleton
    fun providesMoshi(): Moshi {
        return Moshi.Builder()
                .add(WeiAdapter())
                .add(HexNumberAdapter())
                .build()
    }

    @Provides
    @Singleton
    fun providesEthereumJsonRpcApi(moshi: Moshi, @Named(REST_CLIENT) client: OkHttpClient)
            : EthereumJsonRpcApi {
        val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(EthereumJsonRpcApi.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
        return retrofit.create(EthereumJsonRpcApi::class.java)
    }

    @Provides
    @Singleton
    @Named(REST_CLIENT)
    fun providesOkHttpClient(@Named(InterceptorsModule.REST_CLIENT_INTERCEPTORS) interceptors: @JvmSuppressWildcards List<Interceptor>): OkHttpClient {
        return OkHttpClient.Builder().apply {
            interceptors.forEach {
                addInterceptor(it)
            }
        }.build()
    }
    companion object {
        const val REST_CLIENT = "restClient"
    }

}