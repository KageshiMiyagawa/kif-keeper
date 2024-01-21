package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum ScrapingResultCode {

	SUCCESS("成功"),
	FAILURE("失敗"),
	NODATA("対象なし");
	
	ScrapingResultCode(String name) {
		this.name = name;
	}
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public static ScrapingResultCode getScrapingResultByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (ScrapingResultCode scrapingResult : ScrapingResultCode.values()) {
			if (StringUtils.equals(name, scrapingResult.getName())) {
				return scrapingResult;
			}
		}
		return null;
	}

}
