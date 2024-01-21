package jp.co.kifkeeper.model;

import lombok.Data;

@Data
public class KifuData {
	
	private int kifuId;
	
	private String senteId;

	private String senteGrade;
	
	private String goteId;
	
	private String goteGrade;
	
	private int handCount;
	
	private String gameResult;
	
	private String gameDate;

	private String timeRule;
	
	private String showLink;
	
	private String detailLink;
	
	private String appType;
	
	private String kifuText;

}