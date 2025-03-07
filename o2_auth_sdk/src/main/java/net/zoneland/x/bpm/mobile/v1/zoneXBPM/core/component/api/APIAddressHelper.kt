package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api

import android.text.TextUtils
import android.util.Log
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.APIDistributeTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIAssemblesData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIDataBean
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIDistributeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIWebServerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit

/**
 * Created by fancy on 2017/6/5.
 */

class APIAddressHelper private constructor() {

    companion object {
        private var INSTANCE: APIAddressHelper? = null
        fun instance(): APIAddressHelper {
            if (INSTANCE == null) {
                INSTANCE = APIAddressHelper()
                INSTANCE?.loadDistributeData()
            }
            return INSTANCE!!
        }
    }


    var webSocketHead = "ws://"
    var httpHead = "http://"

    val apiDistribute: HashMap<APIDistributeTypeEnum, APIDataBean> = HashMap()
    var webServerData: APIWebServerData? = null


    /**
     * 设置服务器协议
     * @param httpProtocol http https
     */
    fun setHttpProtocol(httpProtocol:String) {
        httpHead = "$httpProtocol://"
        if (httpProtocol == "https") {
            webSocketHead = "wss://"
        }else {
            webSocketHead = "ws://"
        }
    }

    fun getCenterUrl(host: String, context: String, port: Int): String {
        return if (context.contains("/")) {
            "$httpHead$host:$port$context/"
        } else {
            "$httpHead$host:$port/$context/"
        }
    }

    /**
     * 临时地址
     */
    fun getDownloadPuppy2018SkinUrl(): String {
//        return webServerData?.let {
//            "$httpHead${webServerData?.host}:${webServerData?.port}/"
//        } ?: ""
        return "http://dev.o2oa.io/"
    }

    fun getFaceppServerUrl(): String {
        return webServerData?.let {
            "http://${webServerData?.host}:8888/x_faceset_control/"
        } ?: ""
    }

    /**
     * 待办 待阅等任务的网页打开地址
     */
    fun getWorkUrlPre(): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/workmobilewithaction.html?workid=%s"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 已经完成的任务待办打开
     */
    fun getWorkCompletedUrl(): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/workmobilewithaction.html?workcompletedid=%s"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 流程草稿打开的url
     */
    fun getProcessDraftUrl(): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/workmobilewithaction.html?draft=%s"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 流程草稿打开的url
     */
    fun getProcessDraftWithIdUrl(): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/workmobilewithaction.html?draftid=%s"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 查看帖子的url
     * @param subjectId 帖子id
     * @param page 评论页数
     */
    fun getBBSWebViewUrl(subjectId: String, page: Int): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/forumdocMobile.html?id=$subjectId&page=$page"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * web 服务器地址
     * @param path 相对地址
     */
    fun getO2WebUrl(path: String): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/$path"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 论坛附件下载地址
     * jaxrs/attachment/download/${attachmentId}/stream/true
     */
    fun getBBSAttachmentURL(attachmentId: String): String {
        return webServerData?.let {
            getAPIDistribute(APIDistributeTypeEnum.x_bbs_assemble_control) + "jaxrs/attachment/download/$attachmentId/stream/true"
        } ?: ""
    }

    /**
     * cms 文章地址
     */
    fun getCMSWebViewUrl(docId: String): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/cmsdocMobile.html?id=$docId"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 带编辑按钮的 cms webview url
     */
    fun getCMSWebViewUrlWithAction(docId: String): String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/cmsdocmobilewithaction.html?id=$docId"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 热图图片地址
     * http://host:port/x_file_assemble_control/jaxrs/file/${pId}/download/stream
     */
    fun getHotPictureUrl(pid: String): String {
        return getAPIDistribute(APIDistributeTypeEnum.x_file_assemble_control) + "jaxrs/file/$pid/download/stream"
    }

    /**
     * 聊天消息 文件下载地址
     */
    fun getImFileDownloadUrl(fileId: String): String {
        return getAPIDistribute(APIDistributeTypeEnum.x_message_assemble_communicate) + "jaxrs/im/msg/download/$fileId"
    }

    /**
     * 通用的
     * 下载文件的地址
     * @param context: APIDistributeTypeEnum
     * @param urlPath : 例如：jaxrs/im/msg/download/12222233333
     */
    fun getCommonDownloadUrl(context: APIDistributeTypeEnum, urlPath: String): String {
        return getAPIDistribute(context) + urlPath
    }

    /**
     * 聊天消息
     */
    fun getImImageDownloadUrlWithWH(fileId: String, width: Int, height: Int): String {
        return getAPIDistribute(APIDistributeTypeEnum.x_message_assemble_communicate) + "jaxrs/im/msg/download/$fileId/image/width/$width/height/$height"
    }

    /**
     * 云盘图片地址
     * @param fileId 图片文件id
     * @param width 展现图片宽度
     * @param height 展现图片高度
     */
    fun getCloudDiskImageUrl(fileId: String, width: Int, height: Int) : String {
        val file = getAPIDistribute(APIDistributeTypeEnum.x_file_assemble_control)
        return "${file}jaxrs/attachment2/$fileId/download/image/width/$width/height/$height"
    }

    /**
     * 云盘文件下载地址
     * @param fileId 文件id
     */
    fun getCloudDiskFileUrl(fileId: String) : String {
        val file = getAPIDistribute(APIDistributeTypeEnum.x_file_assemble_control)
        return "${file}jaxrs/attachment2/$fileId/download"
    }

    /**
     * 新版用户头像地址
     */
    fun getPersonAvatarUrlWithId(id:String, withTimeSuffix:Boolean = false): String {
        var url =  getAPIDistribute(APIDistributeTypeEnum.x_organization_assemble_control) + "jaxrs/person/$id/icon"
        if (withTimeSuffix){
            url += "?" + DateHelper.nowByFormate("MMddHHmmss")
        }
        return url
    }

    /**
     * 用户头像地址 没有权限的
     */
    fun getPersonAvatarUrlWithoutPermission(id: String, withTimeSuffix:Boolean = false): String {
        var url = getAPIDistribute(APIDistributeTypeEnum.x_organization_assemble_personal) + "jaxrs/icon/$id"
        if (withTimeSuffix){
            url += "?" + DateHelper.nowByFormate("MMddHHmmss")
        }
        return url
    }

    /**
     * 门户打开地址
     */
    fun getPortalWebViewUrl(portalId:String, pageId: String? = null): String {
        val url = webServerData?.let {
            if (TextUtils.isEmpty(pageId)) {
                "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/portalmobile.html?id=$portalId"
            } else {
                "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/portalmobile.html?id=$portalId&page=$pageId"
            }
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * 门户icon地址
     */
    fun getPortalIconUrl(portalId: String, withTimeSuffix:Boolean = false): String {
        var url = getAPIDistribute(APIDistributeTypeEnum.x_portal_assemble_surface) + "jaxrs/portal/$portalId/icon"
        if (withTimeSuffix){
            url += "?" + DateHelper.nowByFormate("MMddHHmmss")
        }
        return url
    }

    /**
     * 自助打包 下载apk文件的url
     */
    fun getPackingClientAppInnerDownloadUrl(id: String): String {
        return getAPIDistribute(APIDistributeTypeEnum.x_app_packaging_client_assemble_control) + "jaxrs/apppackanony/file/download/$id"
    }

    fun getWebViewHost(): String {
        return webServerData?.let { webServerData?.host } ?: ""
    }

    /**
     * 前端config.json 配置文件地址
     */
    fun getConfigJsonUrl():String {
        val url = webServerData?.let {
            "$httpHead${webServerData?.host}:${webServerData?.port}/x_desktop/res/config/config.json"
        } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }

    /**
     * web服务器地址 如：http://dev.o2oa.io:80
     */
    fun getWebServerUrl():String {
        return "$httpHead${webServerData?.host}:${webServerData?.port}/"
    }

    fun setDistributeData(distributeData: APIDistributeData) {
        val standalone = distributeData.standalone ?: false // 是否单端口服务器
        val data = distributeData.assembles
        val webData = distributeData.webServer
        if (standalone) {
            Log.i("APIAddress", " 单端口服务器，开始设置统一的端口.")
            val centerPort = O2SDKManager.instance().prefs().getInt(O2.PRE_CENTER_PORT_KEY, 80)
            webData.port = centerPort
            webData.proxyPort = centerPort
            data.updatePort(centerPort)
            Log.i("APIAddress", " 统一的端口设置完成：$centerPort .")
        }
        val tokenName = if (TextUtils.isEmpty(distributeData.tokenName)) {"x-token"} else {distributeData.tokenName}
        if (data == null || webData == null) {
            throw RuntimeException("Assembles or webServer is null")
        }
        // 添加tokenName
        O2SDKManager.instance().prefs().edit {
            putString(O2.PRE_TOKEN_NAME_KEY, tokenName)
        }
        val dataJson = O2SDKManager.instance().gson.toJson(data)
        val webDataJson = O2SDKManager.instance().gson.toJson(webData)
        if (TextUtils.isEmpty(dataJson) || TextUtils.isEmpty(webDataJson)) {
            throw RuntimeException("Assembles or webServer parse json error")
        }
        Log.d("APIAddress", webDataJson)
        Log.d("APIAddress", dataJson)
        val oldDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_ASSEMBLESJSON_KEY, "")
        val oldWebDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_WEBSERVERJSON_KEY, "")
        if (dataJson != oldDataJson || webDataJson != oldWebDataJson) {
            O2SDKManager.instance().prefs().edit {
                putString(O2.PRE_ASSEMBLESJSON_KEY, dataJson)
                putString(O2.PRE_WEBSERVERJSON_KEY, webDataJson)
            }
        }

        setData(data, webData)
    }

    fun loadDistributeData() {
        Log.i("APIAddress", "loadDistributeData.........................")
        val oldDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_ASSEMBLESJSON_KEY, "")
        val oldWebDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_WEBSERVERJSON_KEY, "")
        val httpProtocol = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "")
        if (!TextUtils.isEmpty(oldDataJson) && !TextUtils.isEmpty(oldWebDataJson)) {
            try {
                if (!TextUtils.isEmpty(httpProtocol)){
                    setHttpProtocol(httpProtocol!!)
                }
                val data = O2SDKManager.instance().gson.fromJson<APIAssemblesData>(oldDataJson, APIAssemblesData::class.java)
                val webData = O2SDKManager.instance().gson.fromJson<APIWebServerData>(oldWebDataJson, APIWebServerData::class.java)
                setData(data, webData)
            } catch (e: Exception) {
            }
        }
    }


    fun setData(data: APIAssemblesData, webData: APIWebServerData) {
        webServerData = webData
        if(data.x_file_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_file_assemble_control] = data.x_file_assemble_control
        }
        if(data.x_meeting_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_meeting_assemble_control] = data.x_meeting_assemble_control
        }
        if(data.x_attendance_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_attendance_assemble_control] = data.x_attendance_assemble_control
        }
        if(data.x_okr_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_okr_assemble_control] = data.x_okr_assemble_control
        }
        if(data.x_bbs_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_bbs_assemble_control] = data.x_bbs_assemble_control
        }
        if(data.x_cms_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_cms_assemble_control] = data.x_cms_assemble_control
        }
        if(data.x_hotpic_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_hotpic_assemble_control] = data.x_hotpic_assemble_control
        }
        if(data.x_organization_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_organization_assemble_control] = data.x_organization_assemble_control
        }
        if(data.x_organization_assemble_custom != null) {
            apiDistribute[APIDistributeTypeEnum.x_organization_assemble_custom] = data.x_organization_assemble_custom
        }
        if(data.x_processplatform_assemble_surface != null) {
            apiDistribute[APIDistributeTypeEnum.x_processplatform_assemble_surface] = data.x_processplatform_assemble_surface
        }
        if(data.x_organization_assemble_express != null) {
            apiDistribute[APIDistributeTypeEnum.x_organization_assemble_express] = data.x_organization_assemble_express
        }
        if(data.x_organization_assemble_personal != null) {
            apiDistribute[APIDistributeTypeEnum.x_organization_assemble_personal] = data.x_organization_assemble_personal
        }
        if(data.x_component_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_component_assemble_control] = data.x_component_assemble_control
        }
        if(data.x_organization_assemble_authentication != null) {
            apiDistribute[APIDistributeTypeEnum.x_organization_assemble_authentication] = data.x_organization_assemble_authentication
        }
        if(data.x_portal_assemble_surface != null) {
            apiDistribute[APIDistributeTypeEnum.x_portal_assemble_surface] = data.x_portal_assemble_surface
        }
        if(data.x_calendar_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_calendar_assemble_control] = data.x_calendar_assemble_control
        }
        if(data.x_mind_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_mind_assemble_control] = data.x_mind_assemble_control
        }
        if(data.x_jpush_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_jpush_assemble_control] = data.x_jpush_assemble_control
        }

        if(data.x_message_assemble_communicate != null) {
            apiDistribute[APIDistributeTypeEnum.x_message_assemble_communicate] = data.x_message_assemble_communicate
        }
        if (data.x_query_assemble_surface != null) {
            apiDistribute[APIDistributeTypeEnum.x_query_assemble_surface] = data.x_query_assemble_surface
        }
        if (data.x_organizationPermission != null) {
            apiDistribute[APIDistributeTypeEnum.x_organizationPermission] = data.x_organizationPermission
        }
        if (data.x_pan_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_pan_assemble_control] = data.x_pan_assemble_control
        }
        if (data.x_app_packaging_client_assemble_control != null) {
            apiDistribute[APIDistributeTypeEnum.x_app_packaging_client_assemble_control] = data.x_app_packaging_client_assemble_control
        }

    }

    fun getAPIDistribute(typeEnum: APIDistributeTypeEnum): String {
        val bean = apiDistribute[typeEnum]
        val url = bean?.let { "$httpHead${it.host}:${it.port}${it.context}/" } ?: ""
        return O2SDKManager.instance().urlTransfer2Mapping(url)
    }


    /**
     * websocket 地址
     */
    fun webSocketUrl(): String {
        val bean = apiDistribute[APIDistributeTypeEnum.x_message_assemble_communicate]
        val xToken = O2SDKManager.instance().zToken
        val tokenName = O2SDKManager.instance().tokenName()
        return bean?.let { "$webSocketHead${it.host}:${it.port}${it.context}/ws/collaboration?$tokenName=$xToken" } ?: ""
    }

}