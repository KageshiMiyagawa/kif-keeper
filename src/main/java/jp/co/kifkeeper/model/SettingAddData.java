package jp.co.kifkeeper.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SettingAddData {

	@NotEmpty(message = "アプリ種別が未選択です")
	private String appType;
	@NotEmpty(message = "ユーザIDが未設定です")
	private String appId;
	
}
