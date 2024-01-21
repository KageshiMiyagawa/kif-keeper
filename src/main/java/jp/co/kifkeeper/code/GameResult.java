package jp.co.kifkeeper.code;

import org.apache.commons.lang3.StringUtils;

public enum GameResult {

	SNETE_WIN("1","先手勝ち"),
	GOTE_WIN("2","後手勝ち"),
	DRAW("3","引き分け");
	
	GameResult(String code, String name) {
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
	
	public static GameResult getGameResultByName (String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (GameResult gameResult : GameResult.values()) {
			if (StringUtils.equals(name, gameResult.getName())) {
				return gameResult;
			}
		}
		return null;
	}
	
	public static GameResult getGameResultByCode (String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		for (GameResult gameResult : GameResult.values()) {
			if (StringUtils.equals(code, gameResult.getCode())) {
				return gameResult;
			}
		}
		return null;		
	}
	
}
