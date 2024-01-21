package jp.co.kifkeeper.code;

public enum SetType {

	USER("1","ユーザー"),
	PW("2","パスワード");
	
	SetType(String code, String name) {
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
}
