package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.OrganizationPermissionManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.realm.RealmDataService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.IdentityLevelForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.unit.UnitJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.NewContactFragmentVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class NewContactPresenter : BasePresenterImpl<NewContactContract.View>(), NewContactContract.Presenter {

    override fun loadNewContact() {
        val service = getOrganizationAssembleControlApi(mView?.getContext())
        val personService = getAssemblePersonalApi(mView?.getContext())
        val expressService = getAssembleExpressApi(mView?.getContext())
        val identityListObservable = service?.identityListWithPerson(O2SDKManager.instance().distinguishedName)?.map { response ->
            val identityList = ArrayList<NewContactFragmentVO>()
            val list = response.data
            if (list != null && list.isNotEmpty()) {
                val header = NewContactFragmentVO.GroupHeader(mView?.getContext()?.getString(R.string.contact_my_department) ?: "我的部门", R.mipmap.icon_contact_my_company)
                identityList.add(header)
                list.filter { !TextUtils.isEmpty(it.unit) }.map {
                    identityList.add(it.copyToVO())
                }
            }
            identityList
        }

        val topUnitListObservable = personService?.getCurrentPersonInfo()?.flatMap { response ->
            val person = response.data
            if (person != null) {
                val identityList = person.woIdentityList
                if (identityList.isNotEmpty()) {
//                    val identity = identityList[0]
                    val list: List<Observable<ApiResponse<UnitJson>>> = identityList.mapNotNull { i ->
                        expressService?.unitByIdentityAndLevel(IdentityLevelForm(identity = i.distinguishedName, level = 1))
                    }
                    Observable.zip(list) { results ->
                        val myList = ArrayList<ApiResponse<UnitJson>>()
                        if (results != null) {
                            for (result in results) {
                                val s = result as? ApiResponse<UnitJson>
                                if (s != null) {
                                    myList.add(s)
                                }
                            }
                        }
                        myList
                    }
                } else {
                    Observable.just(ArrayList<ApiResponse<UnitJson>>())
                }
            } else {
                Observable.just(ArrayList<ApiResponse<UnitJson>>())
            }
        }?.map { response ->
            val topList = ArrayList<NewContactFragmentVO>()
            if (response != null) {
                val header = NewContactFragmentVO.GroupHeader(mView?.getContext()?.getString(R.string.contact_org_structure) ?: "组织结构", R.mipmap.icon_contact_my_department)
                topList.add(header)
                for (api in response) {
                    val unit = api.data
                    if (unit != null) {
                        val u = unit.copyToVO() as NewContactFragmentVO.MyDepartment
                        u.hasChildren = true
                        topList.add(u)
                    }
                }

            }
            topList
        }
//            val topUnitListObservable = service?.unitListTop()?.map { response ->
//                val topList = ArrayList<NewContactFragmentVO>()
//                val list = response.data
//                if (list != null && !list.isEmpty()) {
//                    val header = NewContactFragmentVO.GroupHeader("组织结构", R.mipmap.icon_contact_my_department)
//                    topList.add(header)
//                    list.map {
//                        topList.add(it.copyToVO())
//                    }
//                }
//                topList
//            }
        val usuallyObservable = RealmDataService().loadUsuallyPersonByOwner(O2SDKManager.instance().distinguishedName).map { usuallyPersons ->
            val usList = ArrayList<NewContactFragmentVO>()
            if (usuallyPersons.isNotEmpty()) {
                usList.add(NewContactFragmentVO.GroupHeader(mView?.getContext()?.getString(R.string.contact_common_use) ?: "常用联系人", R.mipmap.icon_contact_my_collect))
                usuallyPersons.map {
                    usList.add(NewContactFragmentVO.MyCollect(it.person ?: "",
                            it.personDisplay ?: "",
                            it.gender ?: "",
                            it.mobile ?: ""))
                }
            }
            usList
        }

        val isqueryAll = !OrganizationPermissionManager.instance().isCurrentPersonCannotQueryAll()
        XLog.debug("是否能查询外部门！$isqueryAll")
        val obs = if (isqueryAll) { // 当前用户不能查询通讯录
            val isqueryOut = !OrganizationPermissionManager.instance().isCurrentPersonCannotQueryOuter()
            XLog.debug("是否能查询外部门！$isqueryOut")
            if (isqueryOut) {
                Observable.zip(identityListObservable, topUnitListObservable, usuallyObservable) { t1, t2, t3 ->
                    val list = ArrayList<NewContactFragmentVO>()
                    list.addAll(t1)
                    list.addAll(t2)
                    list.addAll(t3)
                    list
                }
            } else {
                Observable.zip(identityListObservable, usuallyObservable) { t1, t2 ->
                    val list = ArrayList<NewContactFragmentVO>()
                    list.addAll(t1)
                    list.addAll(t2)
                    list
                }
            }
        } else {
            Observable.just(ArrayList<NewContactFragmentVO>())
        }

        obs.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { list ->
                        mView?.loadContact(list)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.loadContactFail()
                    }
                }
    }


}
