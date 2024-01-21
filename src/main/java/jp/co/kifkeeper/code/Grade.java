package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum Grade {

	DAN_TEN("40","十段"),
	DAN_NINE("39","九段"),
	DAN_EIGHT("38","八段"),
	DAN_SEVEN("37","七段"),
	DAN_SIX("36","六段"),
	DAN_FIVE("35","五段"),
	DAN_FOUR("34","四段"),
	DAN_THREE("33","三段"),
	DAN_TWO("32","二段"),
	DAN_ONE("31","初段"),
	KYU_ONE("30","1級"),
	KYU_TWO("29","2級"),
	KYU_THREE("28","3級"),
	KYU_FOUR("27","4級"),
	KYU_FIVE("26","5級"),
	KYU_SIX("25","6級"),
	KYU_SEVEN("24","7級"),
	KYU_EIGHT("23","8級"),
	KYU_NINE("22","9級"),
	KYU_TEN("21","10級"),
	KYU_ELEVEN("20","11級"),
	KYU_TWELVE("19","12級"),
	KYU_THIRTEEN("18","13級"),
	KYU_FOURTEEN("17","14級"),
	KYU_FIFTEEN("16","15級"),
	KYU_SIXTEEN("15","16級"),
	KYU_SEVENTEEN("14","17級"),
	KYU_EIGHTEEN("13","18級"),
	KYU_NINETEEN("12","19級"),
	KYU_TWENTY("11","20級"),
	KYU_TWENTY_ONE("10","21級"),
	KYU_TWENTY_TWO("09","22級"),
	KYU_TWENTY_THREE("08","23級"),
	KYU_TWENTY_FOUR("07","24級"),
	KYU_TWENTY_FIVE("06","25級"),
	KYU_TWENTY_SIX("05","26級"),
	KYU_TWENTY_SEVEN("04","27級"),
	KYU_TWENTY_EIGHT("03","28級"),
	KYU_TWENTY_NINE("02","29級"),
	KYU_THIRTY("01","30級"),
	BEGINEER("00","初心者");
	
	Grade(String code, String name) {
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
	
	public static Grade getGradeByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (Grade grade : Grade.values()) {
			if (StringUtils.equals(code, grade.getCode())) {
				return grade;
			}
		}
		return null;
	}
	
	public static Grade getGradeByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (Grade grade : Grade.values()) {
			if (StringUtils.equals(name, grade.getName())) {
				return grade;
			}
		}
		return null;
	}
	
	public static Grade getGradeByClub24Rate(int rate) {
		if (rate >= 2900) return Grade.DAN_EIGHT;
		if (rate >= 2700) return Grade.DAN_SEVEN;
		if (rate >= 2500) return Grade.DAN_SIX;
		if (rate >= 2300) return Grade.DAN_FIVE;
		if (rate >= 2100) return Grade.DAN_FOUR;
		if (rate >= 1900) return Grade.DAN_THREE;
		if (rate >= 1700) return Grade.DAN_TWO;
		if (rate >= 1550) return Grade.DAN_ONE;
		if (rate >= 1450) return Grade.KYU_ONE;
		if (rate >= 1350) return Grade.KYU_TWO;
		if (rate >= 1250) return Grade.KYU_THREE;
		if (rate >= 1150) return Grade.KYU_FOUR;
		if (rate >= 1050) return Grade.KYU_FIVE;
		if (rate >= 950) return Grade.KYU_SIX;
		if (rate >= 850) return Grade.KYU_SEVEN;
		if (rate >= 750) return Grade.KYU_EIGHT;
		if (rate >= 650) return Grade.KYU_NINE;
		if (rate >= 550) return Grade.KYU_TEN;
		if (rate >= 450) return Grade.KYU_ELEVEN;
		if (rate >= 350) return Grade.KYU_TWELVE;
		if (rate >= 250) return Grade.KYU_THIRTEEN;
		if (rate >= 150) return Grade.KYU_FOURTEEN;
		if (rate >= 50) return Grade.KYU_FIFTEEN;
		return Grade.BEGINEER;
	}
	
	public static Grade convertQuestGrade(String grade) {
		switch (grade) {
			case "9段": return Grade.DAN_NINE;
			case "8段": return Grade.DAN_EIGHT;
			case "7段": return Grade.DAN_SEVEN;
			case "6段": return Grade.DAN_SIX;
			case "5段": return Grade.DAN_FIVE;
			case "4段": return Grade.DAN_FOUR;
			case "3段": return Grade.DAN_THREE;
			case "2段": return Grade.DAN_TWO;
			case "初段": return Grade.DAN_ONE;
			case "1級": return Grade.KYU_ONE;
			case "2級": return Grade.KYU_TWO;
			case "3級": return Grade.KYU_THREE;
			case "4級": return Grade.KYU_FOUR;
			case "5級": return Grade.KYU_FIVE;
			case "6級": return Grade.KYU_SIX;
			case "7級": return Grade.KYU_SEVEN;
			case "8級": return Grade.KYU_EIGHT;
			case "9級": return Grade.KYU_NINE;
			case "10級": return Grade.KYU_TEN;
			case "11級": return Grade.KYU_ELEVEN;
			case "12級": return Grade.KYU_TWELVE;
			case "13級": return Grade.KYU_THIRTEEN;
			case "14級": return Grade.KYU_FOURTEEN;
			case "15級": return Grade.KYU_FIFTEEN;
			case "16級": return Grade.KYU_SIXTEEN;
			case "17級": return Grade.KYU_SEVENTEEN;
			case "18級": return Grade.KYU_EIGHTEEN;
			case "19級": return Grade.KYU_NINETEEN;
			case "20級": return Grade.KYU_TWENTY;
			case "21級": return Grade.KYU_TWENTY_ONE;
			case "22級": return Grade.KYU_TWENTY_TWO;
			case "23級": return Grade.KYU_TWENTY_THREE;
			case "24級": return Grade.KYU_TWENTY_FOUR;
			case "25級": return Grade.KYU_TWENTY_FIVE;
			case "26級": return Grade.KYU_TWENTY_SIX;
			case "27級": return Grade.KYU_TWENTY_SEVEN;
			case "28級": return Grade.KYU_TWENTY_EIGHT;
			case "29級": return Grade.KYU_TWENTY_NINE;
			case "30級": 
			default: return Grade.KYU_THIRTY;
		}
	}
}
