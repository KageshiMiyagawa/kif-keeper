package jp.co.kifkeeper.model.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_kifu_detail")
public class TtKifuDetail {
	@Id
	@Column(name = "kifu_id")
	private int kifuId;

	@Column(name = "hand_count")
	private int handCount;

	@Column(name = "kifu_text")
	private String kifuText;

	@Column(name = "time_rule")
	private String timeRule;
	
	@Column(name = "match_rule")
	private String matchRule;
	
	@Column(name = "style_1")
	private String style1;
	
	@Column(name = "style_2")
	private String style2;
	
	@Column(name = "style_3")
	private String style3;
	
	@Column(name = "style_4")
	private String style4;
	
	@Column(name = "style_5")
	private String style5;
	
	@Column(name = "add_info")
	private String addInfo;
	
	@Column(name = "regist_date")
	private String registDate;
	
	@OneToOne(mappedBy = "ttKifuDetail")
	private TtKifu ttKifu;

}