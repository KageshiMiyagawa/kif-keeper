package jp.co.kifkeeper.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.code.SetType;
import jp.co.kifkeeper.model.UserSettingData;
import jp.co.kifkeeper.model.table.TtUserSetting;
import jp.co.kifkeeper.repository.UserSettingCrudRepository;

@Service
public class UserSettingManageService {

	@Autowired
	private UserSettingCrudRepository userSettingCrudRepository;

	@Transactional(readOnly=true)
	public UserSettingData findUserSetting() {
		List<TtUserSetting> ttUserSettingList = userSettingCrudRepository.findAll();
		
		Map<String, String> userSettingMap = ttUserSettingList.stream()
				.collect(Collectors.toMap(u -> u.getAppType() + "-" + u.getSetType(), u -> u.getSetData()));
		
		UserSettingData userSettingData = new UserSettingData();
		String club24UserIdKey = AppType.CLUB24.getCode() + "-" + SetType.USER.getCode();
		userSettingData.setClub24Id(userSettingMap.get(club24UserIdKey));
		String questIdKey = AppType.CLUB24.getCode() + "-" + SetType.PW.getCode();
		userSettingData.setClub24Pw(userSettingMap.get(questIdKey));
		
		return userSettingData;
	}
	
	@Transactional
	public void saveUserSettingClub24(UserSettingData appSettingData) {
		saveUserSetting(AppType.CLUB24, SetType.USER, appSettingData.getClub24Id());
		saveUserSetting(AppType.CLUB24, SetType.PW, appSettingData.getClub24Pw());
	}
	
	private void saveUserSetting(AppType appType, SetType setType, String setData) {
		TtUserSetting TtAppSetting = new TtUserSetting();
		TtAppSetting.setAppType(appType.getCode());
		TtAppSetting.setSetType(setType.getCode());
		TtAppSetting.setSetData(setData);
		TtAppSetting.setValidFlg("1");
		userSettingCrudRepository.save(TtAppSetting);
	}

}