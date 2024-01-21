package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum TimeRule {

	WARS_LIMIT_THREE("11","3切れ"),
	WARS_LIMIT_TEN("12","10切れ"),
	WARS_TEN_SECOND("13","10秒将棋"),
	CLUB24_FIRST("21","早指し"),
	CLUB24_FIRST_TWO("22","早指し2"),
	CLUB24_FIFTEEN("23","15分"),
	CLUB24_LONG("24","長考"),
	CLUB24_FIRST_THREE("25","早指し3"),
	QUEST_LIMIT_TEN("31", "10分"),
	QUEST_LIMIT_FIVE("32", "5分"),
	QUEST_LIMIT_TWO("33", "2分");
	
	TimeRule(String code, String name) {
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
	
	public static TimeRule getTimeRuleByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (TimeRule timeRule : TimeRule.values()) {
			if (StringUtils.equals(code, timeRule.getCode())) {
				return timeRule;
			}
		}
		return null;
	}
	
	public static TimeRule getTimeRuleByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (TimeRule timeRule : TimeRule.values()) {
			if (StringUtils.equals(name, timeRule.getName())) {
				return timeRule;
			}
		}
		return null;
	}
	
	public static TimeRule getTimeRuleByClub24Code(String club24Code) {
		if (StringUtils.isEmpty(club24Code)) {
			return null;
		}
		switch(club24Code) {
		case "S1":
			return TimeRule.CLUB24_FIRST;
		case "S2":
			return TimeRule.CLUB24_FIRST_TWO;
		case "S3":
			return TimeRule.CLUB24_FIRST_THREE;
		case "15":
			return TimeRule.CLUB24_FIFTEEN;
		default:
			return TimeRule.CLUB24_LONG;
		}
	}
	
	public static TimeRule getTimeRuleByWarsTimeValue(String warsTimeValue) {
		if (StringUtils.isEmpty(warsTimeValue)) {
			return null;
		}
		switch(warsTimeValue) {
			case "10:00":
				return TimeRule.WARS_LIMIT_THREE;
			case "03:00":
				return TimeRule.WARS_LIMIT_TEN;
			default:
				return TimeRule.WARS_TEN_SECOND;
		}
	}
	
}
