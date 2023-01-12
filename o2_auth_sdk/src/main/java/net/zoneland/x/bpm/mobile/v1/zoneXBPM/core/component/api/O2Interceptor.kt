package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api

import android.text.TextUtils
import android.util.Log
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.adapter.rxjava.Result.response
import android.R.string
import retrofit2.adapter.rxjava.Result.response





/**
 * Created by fancy on 2017/6/5.
 */

class O2Interceptor : Interceptor {
    val TAG = "O2Interceptor"
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        Log.d(TAG,"\n")
        Log.d(TAG,"----------Start----------------")
        Log.d(TAG, "| $original")

        val originalHttpUrl = original.url()
        val url = originalHttpUrl.newBuilder().addQueryParameter("o", (Math.random()*100).toString()).build()
        val xToken = O2SDKManager.instance().zToken
        val tokenName = O2SDKManager.instance().tokenName()
        val requestBuilder = original.newBuilder()
        Log.d(TAG, "tokenName:$tokenName, token: $xToken")
        if (!TextUtils.isEmpty(xToken)) {
            requestBuilder.addHeader(tokenName, xToken)
        }
        Log.d(TAG, "url: $url")
        val request = requestBuilder.addHeader("x-client", O2.DEVICE_TYPE)
                .method(original.method(), original.body())
                .url(url).build()
        Log.d(TAG,
            "发送请求: method：" + request.method()
                    + "\nurl：" + request.url()
                    + "\n请求头：" + request.headers()
                    )



//        return chain.proceed(request)
        val response = chain.proceed(request)
        val mediaType = response.body()?.contentType()
        return if (mediaType == null || !mediaType.toString().contains("application/json")) {
            chain.proceed(request)
        } else {
            val content = response.body()?.string()
            Log.d(TAG, "返回：$content")
            response.newBuilder()
                .body(okhttp3.ResponseBody.create(mediaType, content ?: ""))
                .build()
        }
    }
}