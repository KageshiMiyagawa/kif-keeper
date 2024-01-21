package jp.co.kifkeeper.model.table;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_app_target")
@IdClass(TtAppTargetPK.class)
public class TtAppTarget {
	@Id
	@Column(name = "app_type")
	private String appType;

	@Id
	@Column(name = "app_id")
	private String appId;
	
	@Column(name = "last_collect_date")
	private String lastCollectDate;

	@Column(name = "valid_flg")
	private String validFlg;
	
}