package jp.co.kifkeeper.model;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class KifuFetchCond {
	
	private Integer userId;
	
	private String appId;
	
	private String appType;	
	
	private String gameResult;
	
	private String startGrade;
	
	private String endGrade;
	
	private String startDt;
	
	private String endDt;
	
	private String winnerId;
	
	private String loserId;
	
    public String toQueryString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(appId)) {
            sb.append("appId=").append(appId).append("&");
        }
        if (StringUtils.isNotEmpty(appType)) {
            sb.append("appType=").append(appType).append("&");
        }
        if (StringUtils.isNotEmpty(gameResult)) {
            sb.append("gameResult=").append(gameResult).append("&");
        }
        if (StringUtils.isNotEmpty(startGrade)) {
            sb.append("startGrade=").append(startGrade).append("&");
        }
        if (StringUtils.isNotEmpty(endGrade)) {
            sb.append("endGrade=").append(endGrade).append("&");
        }
        if (StringUtils.isNotEmpty(startDt)) {
            sb.append("startDt=").append(startDt).append("&");
        }
        if (StringUtils.isNotEmpty(endDt)) {
            sb.append("endDt=").append(endDt).append("&");
        }
        if (StringUtils.isNotEmpty(winnerId)) {
            sb.append("winnerId=").append(winnerId).append("&");
        }
        if (StringUtils.isNotEmpty(loserId)) {
            sb.append("loserId=").append(loserId).append("&");
        }
        // 最後の"&"を削除する
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
