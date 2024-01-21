package jp.co.kifkeeper.model.table;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_user_setting")
@IdClass(TtUserSettingPK.class)
public class TtUserSetting {
	@Id
	@Column(name = "app_type")
	private String appType;

	@Id
	@Column(name = "set_type")
	private String setType;
	
	@Column(name = "set_data")
	private String setData;

	@Column(name = "valid_flg")
	private String validFlg;
	
}