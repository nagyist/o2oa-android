package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im


class IMMessageBody(
        var type: String?,
        var body: String?,
        var fileId: String? = null, //文件id
        var fileExtension: String? = null, //文件扩展
        var fileTempPath: String? = null, //本地临时文件地址
        var fileName: String? = null, // 文件名称
        var audioDuration: String? = null, // 音频文件时长
        var address: String? = null, //type=location的时候位置信息
        var addressDetail: String? = null,
        var latitude: Double? = null,//type=location的时候位置信息
        var longitude: Double? = null,//type=location的时候位置信息
        var title: String? = null, // 工作流程 信息等 标题字段
        var work: String? = null, // 工作流程 工作id
        var process: String? = null, // 工作流程 流程id
        var processName: String? = null, // 工作流程 流程名称
        var application: String? = null, // 工作流程 应用id
        var applicationName: String? = null, // 工作流程 应用名称
        var job: String? = null // 工作流程 jobId

)

