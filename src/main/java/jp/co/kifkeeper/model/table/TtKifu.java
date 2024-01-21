package jp.co.kifkeeper.model.table;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tt_kifu")
public class TtKifu {
	@Id
	@Column(name = "kifu_id")
	private int kifuId;

	@Column(name = "app_id")
	private String appId;

	@Column(name = "sente_id")
	private String senteId;

	@Column(name = "sente_grade")
	private String senteGrade;

	@Column(name = "sente_rate")
	private int senteRate;

	@Column(name = "gote_id")
	private String goteId;

	@Column(name = "gote_grade")
	private String goteGrade;

	@Column(name = "gote_rate")
	private int goteRate;
	
	@Column(name = "game_result")
	private String gameResult;
	
	@Column(name = "game_date")
	private String gameDate;

	@Column(name = "app_type")
	private String appType;

	@Column(name = "show_link")
	private String showLink;
	
	@Column(name = "detail_link")
	private String detailLink;

	@Column(name = "regist_date")
	private String registDate;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "kifu_id", referencedColumnName = "kifu_id", insertable = false, updatable = false)
	private TtKifuDetail ttKifuDetail;
}