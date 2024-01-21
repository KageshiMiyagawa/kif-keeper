package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum ScrapingOption {

	ALL("1","全件"),
	FAILED_SCHEDULE("2","失敗スケジュールのみ");
	
	ScrapingOption(String code, String name) {
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
	
	public static ScrapingOption getkifkeeperExecOptionByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (ScrapingOption appExecOption : ScrapingOption.values()) {
			if (StringUtils.equals(name, appExecOption.getName())) {
				return appExecOption;
			}
		}
		return null;
	}
	
	public static ScrapingOption getkifkeeperExecOptionByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (ScrapingOption appExecOption : ScrapingOption.values()) {
			if (StringUtils.equals(code, appExecOption.getCode())) {
				return appExecOption;
			}
		}
		return null;		
	}
}
