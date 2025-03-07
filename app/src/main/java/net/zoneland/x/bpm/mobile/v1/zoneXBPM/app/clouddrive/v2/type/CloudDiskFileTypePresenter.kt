package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.type

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudDiskPageForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class CloudDiskFileTypePresenter : BasePresenterImpl<CloudDiskFileTypeContract.View>(), CloudDiskFileTypeContract.Presenter {
    override fun getPageItems(page: Int, type: String) {
        val service = if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.listFileByPage(page, O2.DEFAULT_PAGE_NUMBER, CloudDiskPageForm(type))
        } else {
            getCloudFileControlService(mView?.getContext())?.listFileByPage(page, O2.DEFAULT_PAGE_NUMBER, CloudDiskPageForm(type))
        }
        if (service == null) {
            mView?.error("服务为空！")
            return
        }
        service.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.pageItems(it.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }

}