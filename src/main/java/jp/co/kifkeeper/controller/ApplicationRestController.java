package jp.co.kifkeeper.controller;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.model.table.TtKifu;
import jp.co.kifkeeper.service.AppTargetManageService;
import jp.co.kifkeeper.service.KifuManageService;

@RestController
public class ApplicationRestController {

	@Autowired
	private KifuManageService kifuManageService;
	@Autowired
	private AppTargetManageService appTargetManageService;
	
	@PostMapping("/setting/{appId}")
	public ResponseEntity<?> deleteSetting(@PathVariable String appId, @RequestParam String appType, @RequestParam String clearKifu) {
		if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(appType)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("入力パラメータが不正です。appType: " + appType + "appId: " + appId);
		}
		
		appTargetManageService.deleteAppTarget(appType, appId);
		boolean isClearKifu = Boolean.valueOf(clearKifu);
		if (isClearKifu) {
			kifuManageService.deleteKifuByAppId(appId);
		}
		
		return ResponseEntity.ok("OK");
	}
	
	@GetMapping("/kifu/{kifuId}")
	public ResponseEntity<?> findKifu(@PathVariable String kifuId) {
		String kifuText = kifuManageService.getKifuText(Integer.parseInt(kifuId));
		if (StringUtils.isEmpty(kifuText)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("存在しない棋譜が読み取りされました。");
		}
		Map<String, String> res = new HashMap<>();
		res.put("kifuText", kifuText);
		return ResponseEntity.ok(res);
	}
	
	@GetMapping("/download/{kifuId}")
    public ResponseEntity<Resource> downloadKifu(@PathVariable String kifuId) throws Exception {
		TtKifu ttKifu = kifuManageService.findKifu(Integer.parseInt(kifuId));
		StringJoiner join = new StringJoiner("-");
		join.add(AppType.getAppTypeByCode(ttKifu.getAppType()).getName());
		join.add(String.valueOf(ttKifu.getKifuId()));
		join.add(ttKifu.getGameDate());
		join.add(ttKifu.getSenteId());
		join.add(ttKifu.getGoteId());
		
		String fileName;
		Path tempFilePath;
		Path renamedFilePath;
//		if (AppType.QUEST.getCode().equals(ttKifu.getAppType())) {
//			fileName = join.toString() + ".csa";
//			tempFilePath = Files.createTempFile(fileName, ".csa");
//			renamedFilePath = tempFilePath.resolveSibling(fileName + ".csa");
//		} else {
//			fileName = join.toString() + ".kif";
//			tempFilePath = Files.createTempFile(fileName, ".kif");
//			renamedFilePath = tempFilePath.resolveSibling(fileName + ".kif");
//		}
		fileName = join.toString() + ".kif";
		tempFilePath = Files.createTempFile(fileName, ".kif");
		renamedFilePath = tempFilePath.resolveSibling(fileName + ".kif");
		
        if (Files.exists(renamedFilePath)) Files.delete(renamedFilePath);
        Files.move(tempFilePath, renamedFilePath);
        Charset charset = Charset.forName("Shift_JIS");
        try (BufferedWriter writer = Files.newBufferedWriter(renamedFilePath, charset, StandardOpenOption.WRITE)) {
        	writer.write(ttKifu.getTtKifuDetail().getKifuText());
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(renamedFilePath.toFile()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        try {
        	Files.delete(renamedFilePath);
        } catch (FileSystemException e) {
        	e.printStackTrace();
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}