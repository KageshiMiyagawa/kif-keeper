package jp.co.kifkeeper.controller;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jp.co.kifkeeper.ApplicationConstants;
import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.model.AppTargetData;
import jp.co.kifkeeper.model.KifuData;
import jp.co.kifkeeper.model.KifuFetchCond;
import jp.co.kifkeeper.model.ScrapingCond;
import jp.co.kifkeeper.model.ScrapingResult;
import jp.co.kifkeeper.model.SettingAddData;
import jp.co.kifkeeper.model.SettingData;
import jp.co.kifkeeper.model.UserSettingData;
import jp.co.kifkeeper.service.AppTargetManageService;
import jp.co.kifkeeper.service.KifuManageService;
import jp.co.kifkeeper.service.KifuScrapingService;
import jp.co.kifkeeper.service.UserSettingManageService;
import jp.co.kifkeeper.util.DateTimeUtil;

@Controller
public class ApplicationController {

	@Autowired
	private KifuScrapingService kifuScrapingService;
	@Autowired
	private KifuManageService kifuManageService;
	@Autowired
	private AppTargetManageService appTargetManageService;
	@Autowired
	private UserSettingManageService userSettingManageService;

    @GetMapping({"/", "/index"})
    public String getDefaultAccess(Model model) {
    	return "index";
    }
    
    @GetMapping("/setting") 
    public String getAppSetting (@ModelAttribute SettingData settingData, Model model){
    	setSettingDispData(model);
    	setUserSettingDispData(model);
    	model.addAttribute("settingAddData", new SettingAddData());
    	
    	return "setting";
    }
    
    @PostMapping("/setting") 
    public String postAppSetting (@Valid @ModelAttribute SettingAddData settingAddData, BindingResult result, Model model){
    	if (result.hasErrors()) {
    		setSettingDispData(model);
    		return "setting";
    	}
    	appTargetManageService.saveAppTarget(settingAddData);
    	setSettingDispData(model);
    	setUserSettingDispData(model);
		String dispText = String.format(ApplicationConstants.MESSAGE_SETTING,
				AppType.getAppTypeByCode(settingAddData.getAppType()).getName(), settingAddData.getAppId());
		;
    	setDialogDispData(model, dispText);
    	return "setting";
    }
    
    @GetMapping("/user-setting") 
	public String getUserSetting(@ModelAttribute UserSettingData userSettingData, Model model) {
		setUserSettingDispData(model);
		return "userSetting";
	}
    
    @PostMapping("/user-setting") 
	public String postUserSetting(@ModelAttribute UserSettingData userSettingData, Model model) {
		userSettingManageService.saveUserSettingClub24(userSettingData);
		setUserSettingDispData(model);
		String dispText = String.format(ApplicationConstants.MESSAGE_SETTING_CLUB24, userSettingData.getClub24Id());
		setDialogDispData(model, dispText);
		return "userSetting";
	}
    
    @GetMapping("/kifuList") 
	public String getKifuList(@PageableDefault(page = 0, size = 20) Pageable pageable, @ModelAttribute KifuFetchCond kifuFetchCond,
			Model model) {
    	String tmpStartDate = kifuFetchCond.getStartDt();
    	String tmpEndDate = kifuFetchCond.getEndDt();
    	List<KifuData> kifuDataList = kifuManageService.findKifu(kifuFetchCond);
    	
    	PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    	int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), kifuDataList.size());
        PageImpl<KifuData> pageImpl = new PageImpl<>(kifuDataList.subList(start, end), pageRequest, kifuDataList.size());
        model.addAttribute("page", pageImpl);
        model.addAttribute("kifuDataList", pageImpl.getContent());
        
        model.addAttribute("totalCount", kifuDataList.size());
        model.addAttribute("startCount", start);
        model.addAttribute("endCount", end);
        
    	kifuFetchCond.setStartDt(tmpStartDate);
    	kifuFetchCond.setEndDt(tmpEndDate);
    	model.addAttribute("kifuFetchCond", kifuFetchCond);
    	model.addAttribute("fetchQuery", kifuFetchCond.toQueryString());
    	setAppTargetDispData(model);
    	
    	return "kifuList";
    }
    
    @PostMapping("/downloadAll")
    public void downloadAll(HttpServletResponse response, @ModelAttribute KifuFetchCond kifuFetchCond) throws Exception {
    	
    	List<KifuData> kifuDataList = kifuManageService.findKifu(kifuFetchCond);
    	List<File> files = new ArrayList<>();
    	for (KifuData kifuData : kifuDataList) {
    		StringJoiner join = new StringJoiner("-");
    		join.add(kifuData.getAppType());
    		join.add(String.valueOf(kifuData.getKifuId()));
    		// gamedateは解析できるフォーマットに変換
    		String gameDate = DateTimeUtil.convertDateStrFormat(kifuData.getGameDate(),
    				ApplicationConstants.DATETIME_FORMAT_DISP, ApplicationConstants.DATETIME_FORMAT_SYSTEM);
    		join.add(gameDate);
    		join.add(kifuData.getSenteId());
    		join.add(kifuData.getGoteId());
    		String fileName = join.toString();
    		
    		Path tempFile;
    		Path renamedFile;
//    		String appType = AppType.getAppTypeByName(kifuData.getAppType()).getCode();
//    		if (AppType.QUEST.getCode().equals(appType)) {
//    			tempFile = Files.createTempFile(fileName, ".csa");
//    			renamedFile = tempFile.resolveSibling(fileName + ".csa");
//    		} else {
//    			tempFile = Files.createTempFile(fileName, ".kif");
//    			renamedFile = tempFile.resolveSibling(fileName + ".kif");
//    		}
			tempFile = Files.createTempFile(fileName, ".kif");
			renamedFile = tempFile.resolveSibling(fileName + ".kif");
            if (Files.exists(renamedFile)) {
            	// 基本的にファイルは処理後に削除する。しかしエラーがあってファイルが残ってしまった場合の考慮。
            	Files.delete(renamedFile);
            }
			Files.move(tempFile, renamedFile);
			Charset charset = Charset.forName("Shift_JIS");
			try (BufferedWriter writer = Files.newBufferedWriter(renamedFile, charset, StandardOpenOption.WRITE)) {
				writer.write(kifuData.getKifuText());
			}
            files.add(renamedFile.toFile());
    	}

    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	try (ZipOutputStream zos = new ZipOutputStream(baos)) {
    	    for (File file : files) {
    	        ZipEntry entry = new ZipEntry(file.getName());
    	        zos.putNextEntry(entry);

    	        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
    	            byte[] buffer = new byte[1024];
    	            int len;
    	            while ((len = bis.read(buffer)) > 0) {
    	                zos.write(buffer, 0, len);
    	            }
    	        }
    	        zos.closeEntry();
    	    }
    	    zos.finish();
    	}
    	
    	LocalDateTime now = LocalDateTime.now();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
    	String dateTimeStr = now.format(formatter);
    	String zipFileName = "kifkeeper-" + dateTimeStr + ".zip";
    	response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
    	response.setContentType("application/zip");
    	
    	// 出力ストリームへの書き込み
    	try (ServletOutputStream out = response.getOutputStream()) {
    	    out.write(baos.toByteArray());
    	    out.flush();
    	}
    	// 処理が完了したファイルは削除する
    	files.stream().forEach(file -> file.delete());
    }

    @GetMapping("/downloadCsv")
    public ResponseEntity<byte[]> downloadCsv(@ModelAttribute KifuFetchCond kifuFetchCond) throws Exception {
    	List<KifuData> kifuDataList = kifuManageService.findKifu(kifuFetchCond);
    	List<List<String>> kifuDataCsvList = new ArrayList<>();
    	for (KifuData kifuData : kifuDataList) {
    		List<String> kifuDataCsv = new ArrayList<>();
    		kifuDataCsv.add(kifuData.getAppType());
    		kifuDataCsv.add(kifuData.getTimeRule());
    		kifuDataCsv.add(kifuData.getSenteId());
    		kifuDataCsv.add(kifuData.getSenteGrade());
    		kifuDataCsv.add(kifuData.getGoteId());
    		kifuDataCsv.add(kifuData.getGoteGrade());
    		kifuDataCsv.add(String.valueOf(kifuData.getHandCount()));
    		kifuDataCsv.add(kifuData.getGameResult());
    		kifuDataCsv.add(kifuData.getGameDate());
    		kifuDataCsv.add(kifuData.getShowLink());
    		kifuDataCsvList.add(kifuDataCsv);
    	}
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("アプリ", "時間", "先手","先手段位","後手","後手段位","手数","結果","対局日","閲覧リンク"));
        for (List<String> kifuDataCsv : kifuDataCsvList) {
            csvPrinter.printRecord(kifuDataCsv);
        }
        
        csvPrinter.flush();
        csvPrinter.close();

        byte[] csvBytes = outputStream.toByteArray();

    	LocalDateTime now = LocalDateTime.now();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
    	String dateTimeStr = now.format(formatter);
    	String csvFileName = "kifkeeper-" + dateTimeStr + ".csv";
    	
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", csvFileName);

		return ResponseEntity
				.ok()
				.headers(headers)
				.body(csvBytes);
    }
    
    @GetMapping("/scraping")
    public String dispScraping(Model model){
    	ScrapingCond scrapingCond = new ScrapingCond();
    	model.addAttribute("scrapingCond", scrapingCond);
    	setAppTargetDispData(model);
    	return "scraping";
    }
    
    @PostMapping("/scraping")
    public String scraping(@ModelAttribute ScrapingCond scrapingCond, Model model){
    	// 収集対象の設定チェック
    	if(CollectionUtils.isEmpty(appTargetManageService.findAppTarget())) {
    		model.addAttribute("noAppTarget", "1");
    		setAppTargetDispData(model);
    		return "scraping";
    	}
    	
    	List<ScrapingResult> scrapingResultList = kifuScrapingService.scrapingKifu(scrapingCond);
    	model.addAttribute("scrapingResultList", scrapingResultList);
		model.addAttribute("scrapingCond", scrapingCond);
		setAppTargetDispData(model);
		setDialogDispData(model, ApplicationConstants.MESSAGE_SCRAPING);
    	return "scraping";
    }
    
    private void setAppTargetDispData(Model model) {
    	List<AppTargetData> appTargetList = appTargetManageService.findAppTarget();
    	model.addAttribute("appTargetList", appTargetList);
    }
    
    private void setSettingDispData(Model model) {
    	SettingData settingData = appTargetManageService.findSetting();
    	model.addAttribute("settingData", settingData);
    }
    
    private void setUserSettingDispData(Model model) {
		UserSettingData userSettingData = userSettingManageService.findUserSetting();
    	model.addAttribute("userSettingData", userSettingData);
    }
    
    private void setDialogDispData(Model model, String dialogText) {
    	model.addAttribute("dialogText", dialogText);
    }
     
}