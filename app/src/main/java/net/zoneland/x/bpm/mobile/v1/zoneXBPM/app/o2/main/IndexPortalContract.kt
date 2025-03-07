package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSApplicationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSCategoryInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSDocumentInfoJson

/**
 * Created by fancyLou on 21/03/2018.
 * Copyright © 2018 O2. All rights reserved.
 */

object IndexPortalContract {
    interface View:BaseView {
//        fun loadCmsCategoryListByAppId(categoryList: List<CMSCategoryInfoJson>)
        fun cmsApplication(app: CMSApplicationInfoJson?)
        fun documentDraft(list: List<CMSDocumentInfoJson>)

        fun openCmsApplication(app: CMSApplicationInfoJson?)
    }
    interface Presenter:BasePresenter<View> {
//        fun loadCmsCategoryListByAppId(appId: String)

        // 能发布的 app
        fun loadCmsApplication(appId: String)
        fun findDocumentDraftWithCategory(categoryId: String)
        // 有权限查看的 app
        fun openCmsApplication(appId: String)

    }
}