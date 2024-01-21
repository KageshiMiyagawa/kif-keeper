package jp.co.kifkeeper.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScrapingResult {

	private String appType;
	
	private String appId;
	
	private String result;
	
	private String count;
}
