package de.thegerman.sttt.data.apis

import de.thegerman.sttt.data.apis.models.JsonRpcRequest
import de.thegerman.sttt.data.apis.models.JsonRpcResult

open class BulkRequest {
    private val callMap = HashMap<Int, SubRequest<out Any?>>()

    constructor(vararg calls: SubRequest<out Any?>) {
        calls.forEach { callMapper(it) }
    }

    constructor(calls: List<SubRequest<out Any?>>) {
        calls.forEach { callMapper(it) }
    }

    private fun callMapper(subRequest: SubRequest<out Any?>) {
        val id = subRequest.params.id
        if (callMap.containsKey(id)) {
            throw IllegalStateException("Duplicate ids!")
        }
        callMap[id] = subRequest
    }

    fun body(): Collection<JsonRpcRequest> = callMap.map { it.value.params }

    fun parse(results: Collection<JsonRpcResult>) {
        results.forEach {
            callMap[it.id]?.parse(it)
        }
    }

    class SubRequest<D>(val params: JsonRpcRequest, private val adapter: (JsonRpcResult) -> D) {
        var value: D? = null

        fun parse(result: JsonRpcResult) {
            value = adapter(result)
        }
    }
}