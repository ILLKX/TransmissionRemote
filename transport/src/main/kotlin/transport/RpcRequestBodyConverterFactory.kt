package transport

import com.squareup.moshi.Moshi
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import transport.rpc.RpcBody
import transport.rpc.RpcMethod
import java.lang.reflect.Type

class RpcRequestBodyConverterFactory(private val moshi: Moshi) : Converter.Factory() {

    companion object {
        fun create(moshi: Moshi) = RpcRequestBodyConverterFactory(moshi)
    }

    override fun requestBodyConverter(
            type: Type?, parameterAnnotations: Array<out Annotation>?,
            methodAnnotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {

        val rpcMethod = findRpcMethod(methodAnnotations) ?: return null

        val adapter = moshi.adapter<RpcBody>(RpcBody::class.java)
        return RpcRequestBodyConverter(rpcMethod, adapter)
    }

    private fun findRpcMethod(annotations: Array<out Annotation>?): String? {
        val methodAnnotation = annotations?.find {
            it.annotationClass == RpcMethod::class
        } as? RpcMethod ?: return null
        return methodAnnotation.name
    }
}
