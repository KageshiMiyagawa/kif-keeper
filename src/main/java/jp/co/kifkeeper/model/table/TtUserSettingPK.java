package jp.co.kifkeeper.model.table;


import java.io.Serializable;

import lombok.Data;

@Data
public class TtUserSettingPK implements Serializable {

	private String appType;

	private String setType;
	
}