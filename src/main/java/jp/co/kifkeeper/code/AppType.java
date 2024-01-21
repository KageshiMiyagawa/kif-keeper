package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum AppType {

	WARS("1","将棋ウォーズ"),
	CLUB24("2","将棋倶楽部24");
//	QUEST("3","将棋クエスト");
	
	AppType(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	private String code;
	private String name;
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	
	public static AppType getAppTypeByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (AppType AppType : AppType.values()) {
			if (StringUtils.equals(name, AppType.getName())) {
				return AppType;
			}
		}
		return null;
	}
	
	public static AppType getAppTypeByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (AppType AppType : AppType.values()) {
			if (StringUtils.equals(code, AppType.getCode())) {
				return AppType;
			}
		}
		return null;		
	}
}
