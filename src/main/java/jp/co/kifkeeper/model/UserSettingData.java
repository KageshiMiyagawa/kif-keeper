package jp.co.kifkeeper.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserSettingData {

	@NotEmpty(message = "IDが未設定です")
	private String club24Id;
	@NotEmpty(message = "パスワードが未設定です")
	private String club24Pw;
	
}
