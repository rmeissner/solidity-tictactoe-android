package de.thegerman.sttt.data.apis

import de.thegerman.sttt.BuildConfig
import de.thegerman.sttt.data.apis.models.JsonRpcRequest
import de.thegerman.sttt.data.apis.models.JsonRpcResult
import de.thegerman.sttt.data.apis.models.JsonRpcTransactionReceiptResult
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface EthereumJsonRpcApi {
    companion object {
        const val BASE_URL: String = BuildConfig.BLOCKCHAIN_NET_URL
    }

    @POST("/")
    fun receipt(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcTransactionReceiptResult>

    @POST("/")
    fun receipt(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcTransactionReceiptResult>>

    @POST("/")
    fun post(@Body jsonRpcRequest: JsonRpcRequest): Observable<JsonRpcResult>

    @POST("/")
    fun post(@Body jsonRpcRequest: Collection<JsonRpcRequest>): Observable<Collection<JsonRpcResult>>
}
