package jp.co.kifkeeper.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.model.AppTargetData;
import jp.co.kifkeeper.model.SettingAddData;
import jp.co.kifkeeper.model.SettingData;
import jp.co.kifkeeper.model.table.TtAppTarget;
import jp.co.kifkeeper.repository.AppTargetCrudRepository;

@Service
public class AppTargetManageService {

	@Autowired
	private AppTargetCrudRepository appTargetCrudRepository;


	@Transactional(readOnly = true)
	public SettingData findSetting() {
		List<TtAppTarget> ttAppTargetList = appTargetCrudRepository.findAll().stream()
				.sorted(Comparator.comparing(TtAppTarget::getAppId))
				.toList();
		return convertDispUserSetting(ttAppTargetList);
	}
	
	@Transactional(readOnly = true)
	public List<AppTargetData> findAppTarget() {
		List<TtAppTarget> ttAppTargetList = appTargetCrudRepository.findAll().stream()
				.sorted(Comparator.comparing(TtAppTarget::getAppId))
				.toList();
		return convertDispUserAppTarget(ttAppTargetList);
	}

	@Transactional
	public void saveAppTarget(SettingAddData settingAddData) {
		TtAppTarget appTarget = generateAppTarget(settingAddData);
		appTargetCrudRepository.save(appTarget);
	}
	
	@Transactional
	public void deleteAppTarget(String appType, String appId) {
		TtAppTarget appTarget = new TtAppTarget();
		appTarget.setAppId(appId);
		appTarget.setAppType(appType);
		appTargetCrudRepository.delete(appTarget);
	}

	private TtAppTarget generateAppTarget(SettingAddData settingAddData) {
		TtAppTarget ttAppTarget = new TtAppTarget();
		ttAppTarget.setAppType(settingAddData.getAppType());
		ttAppTarget.setAppId(settingAddData.getAppId());
		ttAppTarget.setValidFlg("1");
		ttAppTarget.setLastCollectDate(null);

		return ttAppTarget;
	}

	private List<AppTargetData> convertDispUserAppTarget(List<TtAppTarget> appTargetList) {
		List<AppTargetData> appTargetDataList = new ArrayList<>();
		for (TtAppTarget appTarget : appTargetList) {
			AppTargetData appTargetData = new AppTargetData();
			appTargetData.setAppId(appTarget.getAppId());
			appTargetData.setAppType(appTarget.getAppType());
			AppType appType = AppType.getAppTypeByCode(appTarget.getAppType());
			appTargetData.setDispAppId(appTargetData.getAppId() + "(" + appType.getName() + ")");

			appTargetDataList.add(appTargetData);
		}
		return appTargetDataList;
	}

	private SettingData convertDispUserSetting(List<TtAppTarget> appTargetList) {
		SettingData userSettingData = new SettingData();
		for (TtAppTarget userAppTarget : appTargetList) {
			String appType = userAppTarget.getAppType();
			if (AppType.WARS.getCode().equals(appType)) {
				userSettingData.getWarsIds().add(userAppTarget.getAppId());
			} else if (AppType.CLUB24.getCode().equals(appType)) {
				userSettingData.getClub24Ids().add(userAppTarget.getAppId());
			} 
//			else if (AppType.QUEST.getCode().equals(appType)) {
//				userSettingData.getQuestIds().add(userAppTarget.getAppId());
//			}
		}
		return userSettingData;
	}

}