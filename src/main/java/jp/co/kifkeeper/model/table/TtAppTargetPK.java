package jp.co.kifkeeper.model.table;


import java.io.Serializable;

import lombok.Data;

@Data
public class TtAppTargetPK implements Serializable {

	private String appType;

	private String appId;
	
}