package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.TestObject
import retrofit2.http.*
import rx.Observable


interface QueryAssembleSurfaceService {

    /**
     * 执行视图
     * 返回视图结果
     */
    @PUT("jaxrs/view/{id}/execute")
    fun excuteView(@Path("id") id: String) : Observable<ApiResponse<TestObject>>


    /**
     * 根据关键字搜索id列表
     */
    @GET("jaxrs/segment/key/{key}")
    fun segmentSearch(@Path("key") key: String) : Observable<ApiResponse<O2SearchIdsEntry>>

    /**
     * 根据id列表获取数据集
     * @param map O2SearchEntryForm
     */
    @POST("jaxrs/segment/list/entry")
    fun segmentListEntry(@Body map: O2SearchEntryForm) : Observable<ApiResponse<List<O2SearchEntry>>>


    /**
     * 搜索v2版本 从7.3版本开始
     */
    @POST("jaxrs/search")
    fun searchV2(@Body body: O2SearchV2Form): Observable<ApiResponse<O2SearchV2PageModel>>

}