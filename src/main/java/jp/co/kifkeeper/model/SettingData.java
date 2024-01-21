package jp.co.kifkeeper.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SettingData {

	private List<String> warsIds = new ArrayList<>();
	private List<String> club24Ids = new ArrayList<>();
	private List<String> questIds = new ArrayList<>();
	
}
