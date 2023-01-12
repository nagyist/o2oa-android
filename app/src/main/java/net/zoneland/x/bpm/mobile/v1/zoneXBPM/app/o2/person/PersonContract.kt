package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.person

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.person.PersonJson


object PersonContract {
    interface View : BaseView {
        fun isUsuallyPerson(flag:Boolean)
        fun loadPersonInfo(personInfo: PersonJson, iSuperior: Boolean)
        fun loadPersonInfoFail()
        fun createConvSuccess(conv: IMConversationInfo)
        fun createConvFail(message: String)
    }

    interface Presenter : BasePresenter<View> {
        fun loadPersonInfo(name:String, iSuperior: Boolean)
        fun collectionUsuallyPerson(owner:String, person:String, ownerDisplay:String,personDisplay:String, gender:String, mobile:String)
        fun deleteUsuallyPerson(owner: String, person: String)
        fun isUsuallyPerson(owner: String, person: String)
        fun startSingleTalk(user: String)
    }
}
