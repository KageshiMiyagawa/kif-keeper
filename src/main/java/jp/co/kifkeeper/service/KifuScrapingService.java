package jp.co.kifkeeper.service;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.github.bonigarcia.wdm.WebDriverManager;
import jp.co.kifkeeper.ApplicationConstants;
import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.code.GameResult;
import jp.co.kifkeeper.code.Grade;
import jp.co.kifkeeper.code.ScrapingResultCode;
import jp.co.kifkeeper.code.SetType;
import jp.co.kifkeeper.code.TimeRule;
import jp.co.kifkeeper.model.ScrapingCond;
import jp.co.kifkeeper.model.ScrapingResult;
import jp.co.kifkeeper.model.table.TtAppTarget;
import jp.co.kifkeeper.model.table.TtKifu;
import jp.co.kifkeeper.model.table.TtKifuDetail;
import jp.co.kifkeeper.model.table.TtUserSetting;
import jp.co.kifkeeper.repository.AppTargetCrudRepository;
import jp.co.kifkeeper.repository.KifuCrudRepository;
import jp.co.kifkeeper.repository.KifuCustomRepository;
import jp.co.kifkeeper.repository.UserSettingCrudRepository;
import jp.co.kifkeeper.util.DateTimeUtil;
import lombok.Data;

@Service
public class KifuScrapingService {

	@Autowired
	private KifuCrudRepository kifuCrudRepository;
	@Autowired
	private KifuCustomRepository kifuCustomRepository;
	@Autowired
	private AppTargetCrudRepository appTargetCrudRepository;
	@Autowired
	private UserSettingCrudRepository userSettingCrudRepository;
	private final Logger logger = LoggerFactory.getLogger("");
	
	private static final String WARS_SEARCH_URL = "https://www.shogi-extend.com/swars/search?query=";
	private static final String WARS_DETAIL_URL = "https://shogiwars.heroz.jp/games/";
	private static final String WARS_KIFU_DETAIL_SENTE_SELECTOR = "_1BZYofEe8BhdN91NSXrX5p";
	private static final String WARS_KIFU_OPTIONS_SELECTOR = "_1evzo2kTEXnRnBVTuGar_l";
	private static final String WARS_TIME_SELECTOR = "GsE3HtqJGq0Gck0JoNC6Y";
	private static final String WARS_KIFU_BASE_URL = "https://www.shogi-extend.com/w/";
	private static final int WARS_SEARCH_DISP_COUNT = 10;

	private static final String CLUB24_SEARCH_URL = "https://www.shogidojo.net/shogi24kifu/";
	private static final String CLUB24_WIN_SELECTOR = "far fa-circle";

	private static final String QUEST_SEARCH_URL = "https://c-loft.com/shogi/quest/";

	private static final int retryCount = 3;

	/**
	 * 棋譜収集処理
	 * @param scrapingCond スクレイピング条件
	 * @return スクレイピング結果
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<ScrapingResult> scrapingKifu(ScrapingCond scrapingCond) {
		List<TtAppTarget> appTargetList = appTargetCrudRepository.findAll().stream()
				.filter(tgt -> tgt.getValidFlg().equals("1")).toList();

		if (StringUtils.isNotEmpty(scrapingCond.getAppType())) {
			appTargetList = appTargetList.stream()
					.filter(t -> t.getAppType().equals(scrapingCond.getAppType())).toList();
		}
		if (StringUtils.isNotEmpty(scrapingCond.getAppId())) {
			appTargetList = appTargetList.stream()
					.filter(t -> t.getAppId().equals(scrapingCond.getAppId())).toList();
		}
		
		Map<String, List<TtAppTarget>> ttUserappTargetMap = appTargetList.stream()
				.collect(Collectors.groupingBy(TtAppTarget::getAppType));
		List<TtAppTarget> targetWars = ttUserappTargetMap.get(AppType.WARS.getCode());
		List<TtAppTarget> targetClub24 = ttUserappTargetMap.get(AppType.CLUB24.getCode());
//		List<TtAppTarget> targetQuest = ttUserappTargetMap.get(AppType.QUEST.getCode());

		// スクレイピング
		String nowDateStr = DateTimeUtil.getNowDateStr(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
		List<TtKifu> kifuList = new ArrayList<>();
		List<TtAppTarget> successTarget = new ArrayList<>();
		List<TtAppTarget> failureTarget = new ArrayList<>();
		List<TtAppTarget> noDataTarget = new ArrayList<>();
		Map<String, Integer> collectKifuCountMap = new LinkedHashMap<>();

		if (!CollectionUtils.isEmpty(targetWars)) {
			try {
				ScrapingResultData warsResult = scrapingWarsKifu(targetWars);
				kifuList.addAll(warsResult.getScrapingKifus());
				successTarget.addAll(warsResult.getSuccessTarget());
				failureTarget.addAll(warsResult.getFailerTarget());
				noDataTarget.addAll(warsResult.getNoDataTarget());
				collectKifuCountMap.putAll(warsResult.getCollectKifuCountMap());
			} catch (Exception e) {
				failureTarget.addAll(targetWars);
				e.printStackTrace();
			}
		}

		if (!CollectionUtils.isEmpty(targetClub24)) {
			try {
				ScrapingResultData club24Result = scrapingClub24Kifu(targetClub24);
				kifuList.addAll(club24Result.getScrapingKifus());
				successTarget.addAll(club24Result.getSuccessTarget());
				failureTarget.addAll(club24Result.getFailerTarget());
				noDataTarget.addAll(club24Result.getNoDataTarget());
				collectKifuCountMap.putAll(club24Result.getCollectKifuCountMap());
			} catch (Exception e) {
				failureTarget.addAll(targetClub24);
				e.printStackTrace();
			}
		}

//		if (!CollectionUtils.isEmpty(targetQuest)) {
//			try {
//				ScrapingResultData questResult = scrapingQuestKifu(targetQuest);
//				kifuList.addAll(questResult.getScrapingKifus());
//				successTarget.addAll(questResult.getSuccessTarget());
//				failureTarget.addAll(questResult.getFailerTarget());
//				noDataTarget.addAll(questResult.getNoDataTarget());
//				collectKifuCountMap.putAll(questResult.getCollectKifuCountMap());
//			} catch (Exception e) {
//				failureTarget.addAll(targetQuest);
//				e.printStackTrace();
//			}
//		}

		if (!CollectionUtils.isEmpty(kifuList)) {
			// 棋譜の共通設定
			int baseKifuId = kifuCustomRepository.getMaxKifuId();
			for (TtKifu kifu : kifuList) {
				// 連番設定
				baseKifuId++;
				kifu.setKifuId(baseKifuId);
				kifu.getTtKifuDetail().setKifuId(baseKifuId);
				// 登録日時
				kifu.setRegistDate(nowDateStr);
				kifu.getTtKifuDetail().setRegistDate(nowDateStr);
			}
			kifuCrudRepository.saveAll(kifuList);

			// 最終収集日時を更新
			successTarget.stream().forEach(tgt -> tgt.setLastCollectDate(nowDateStr));
			appTargetCrudRepository.saveAll(successTarget);
		}
		
		// 結果作成、ログ出力
		List<ScrapingResult> resultList = new ArrayList<>();
		
		logger.info("---収集結果サマリ---");
		logger.info("収集予定:{}件", appTargetList.size());
		logger.info("収集成功:{}件", successTarget.size());
		logger.info("収集対象なし:{}件", noDataTarget.size());
		logger.info("収集失敗:{}件", failureTarget.size());
		logger.info("---収集結果詳細---");
		if(!CollectionUtils.isEmpty(successTarget)) {
			logger.info("---◯収集成功◯---");
			for (TtAppTarget target : successTarget) {
				if (failureTarget.contains(target)) {
					continue; //暫定：失敗に含まれていたらそちらの出力を優先する。
				}
				logger.info("アプリ種別:{},アプリID:{}", target.getAppType(), target.getAppId());
				String prefix = target.getAppType() + "-" + target.getAppId();
				resultList.add(new ScrapingResult(AppType.getAppTypeByCode(target.getAppType()).getName(),
						target.getAppId(),
						ScrapingResultCode.SUCCESS.getName(), String.valueOf(collectKifuCountMap.get(prefix))));
			}
		}
		if(!CollectionUtils.isEmpty(noDataTarget)) {
			logger.info("---△収集対象なし△---");
			for (TtAppTarget target : noDataTarget) {
				if (failureTarget.contains(target)) {
					continue; //暫定：失敗に含まれていたらそちらの出力を優先する。
				}
				logger.info("アプリ種別:{},アプリID:{}", target.getAppType(), target.getAppId());
				resultList.add(new ScrapingResult(AppType.getAppTypeByCode(target.getAppType()).getName(),
						target.getAppId(), ScrapingResultCode.NODATA.getName(), "-"));
			}
		}
		if(!CollectionUtils.isEmpty(failureTarget)) {
			logger.info("---×収集失敗×---");
			for (TtAppTarget target : failureTarget) {
				logger.info("アプリ種別:{},アプリID:{}",target.getAppType(), target.getAppId());
				resultList.add(new ScrapingResult(AppType.getAppTypeByCode(target.getAppType()).getName(),
						target.getAppId(), ScrapingResultCode.FAILURE.getName(), "-"));
			}
		}
		return resultList;
	}

	/**
	 * 将棋倶楽部２４の棋譜を取得<br>
	 * スクレイピングの取得内容は下記の通り<br>
	 * [0] 自分のID
	 * [1] 相手のID
	 * [2] 対局日時（m/d)
	 * [3] KENTOのURL、詳細URL
	 * @param targetWars 将棋ウォーズ収集対象
	 * @return 将棋ウォーズのスクレイピング結果
	 * @throws InterruptedException 
	 */
	public ScrapingResultData scrapingWarsKifu(List<TtAppTarget> targetWars) throws InterruptedException {
		ScrapingResultData result = new ScrapingResultData(AppType.WARS);
		WebDriver driver = connectWebDriver(WARS_SEARCH_URL, AppType.WARS);
		WebDriverWait waitDriver = new WebDriverWait(driver, Duration.ofSeconds(10));
		if (Objects.isNull(driver)) {
			result.setFailerTarget(targetWars);
			return result;
		}
		for (TtAppTarget target : targetWars) {
			List<TtKifu> kifuList = new ArrayList<>();
			try {
				String warsUrl = generateUrlForWars(target.getAppId(), target.getLastCollectDate());
				driver.get(warsUrl);
				Thread.sleep(3000);
				// 件数判定
				String countElemStr = waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.className("info"))).getText();
				String[] splitCountElem = countElemStr.split("/");
				int kifuCount = Integer.parseInt(splitCountElem[1].trim());
				if (kifuCount == 0) {
					result.getNoDataTarget().add(target);
					continue;
				}

				List<WebElement> rows = driver.findElements(By.tagName("tr"));
				kifuList.addAll(setScrapingResultForWars(rows, waitDriver));
				int repeatCount = kifuCount / WARS_SEARCH_DISP_COUNT;
				int pageCount = 2;
				for (int i = 0; i < repeatCount; i++) {
					String warsUrlRepeat = warsUrl + "&page=" + pageCount;
					driver.get(warsUrlRepeat);
					Thread.sleep(3000); //検索結果を読み込むために一時停止
					List<WebElement> rowsRepeat = driver.findElements(By.tagName("tr"));
					kifuList.addAll(setScrapingResultForWars(rowsRepeat, waitDriver));
					pageCount++;
				}
				// 収集済みの棋譜を除外
				kifuList = excludeKifuAlreadyCollected(kifuList);
				if (CollectionUtils.isEmpty(kifuList)) {
					result.getNoDataTarget().add(target);
					continue;
				}
				complementKifuDataForWars(kifuList, driver);
				kifuList.stream().forEach(kifu -> kifu.setAppId(target.getAppId()));

			} catch (Exception e) {
				result.getFailerTarget().add(target);
				e.printStackTrace();
			}
			result.getScrapingKifus().addAll(kifuList);
			result.getSuccessTarget().add(target);
			String prefix = AppType.WARS.getCode() + "-" + target.getAppId();
			result.getCollectKifuCountMap().put(prefix, kifuList.size());
		}
		quitDriver(driver);
		return result;
	}

	/**
	 * 将棋倶楽部２４の棋譜を取得<br>
	 * スクレイピングの取得内容は下記の通り<br>
	 * [0] KifuForWebUrl
	 * [1] KifuTextUrl
	 * [2] 対局ルール
	 * [3] 時間ルール
	 * [4] 開始日時（yyyy/m/d hh:mm）
	 * [5] 先手レーティング
	 * [6] 先手ID
	 * [7] 勝敗結果
	 * [8] 後手レーティング
	 * [9] 後手ID
	 * [10] 手合い
	 * [11] 手数
	 * [12] 終了日時（yyyy/m/d hh:mm）
	 * [13] 追加情報
	 * [14] 対局ID
	 * @param targetClub24 将棋倶楽部24収集対象
	 * @return 将棋倶楽部２４のスクレイピング結果
	 * @throws InterruptedException 
	 */
	public ScrapingResultData scrapingClub24Kifu(List<TtAppTarget> targetClub24) throws InterruptedException {
		ScrapingResultData result = new ScrapingResultData(AppType.CLUB24);
		WebDriver driver = connectWebDriver(CLUB24_SEARCH_URL, AppType.CLUB24);
		WebDriverWait waitDriver = new WebDriverWait(driver, Duration.ofSeconds(10));
		if (Objects.isNull(driver)) {
			result.setFailerTarget(targetClub24);
			return result;
		}
		// ログイン
		waitDriver.until(ExpectedConditions.elementToBeClickable(By.id("sub")));
		WebElement inputName = driver.findElement(By.id("uname"));
		List<TtUserSetting> userSettingList = userSettingCrudRepository.findByAppType(AppType.CLUB24.getCode());
		if(CollectionUtils.isEmpty(userSettingList)) {
			// ログインユーザー未設定
			return result;
		}
		Map<String, String> userSettingMap = userSettingList.stream()
				.collect(Collectors.toMap(TtUserSetting::getSetType, TtUserSetting::getSetData));
		String club24UserId = userSettingMap.get(SetType.USER.getCode());
		String club24Pw = userSettingMap.get(SetType.PW.getCode());
		inputName.sendKeys(club24UserId);
		WebElement inputPwd = driver.findElement(By.id("pwd"));
		inputPwd.sendKeys(club24Pw);
		WebElement loginBtn = driver.findElement(By.id("sub"));
		loginBtn.click();
		
		for (TtAppTarget target : targetClub24) {
			List<TtKifu> kifuList = new ArrayList<>();
			try {
				execScrapingForClub24(driver, waitDriver, target.getAppId(), target.getLastCollectDate());

				List<WebElement> rows;
				try {
					WebElement kifuSearchTable = driver.findElement(By.id("kifuresultTable"));
					rows = kifuSearchTable.findElements(By.tagName("tr"));
				} catch (NoSuchElementException e) {
					result.getNoDataTarget().add(target);
					continue;
				}
				kifuList.addAll(setScrapingResultForClub24(rows, target.getAppId()));

				// 1行目はヘッダ行なので無視する
				kifuList.remove(0);
				// 収集済みの棋譜は除外
				kifuList = excludeKifuAlreadyCollected(kifuList);
				if(CollectionUtils.isEmpty(kifuList)) {
					result.getNoDataTarget().add(target);
					continue;
				}

				for (TtKifu kifu : kifuList) {
					driver.get(kifu.getDetailLink());
					kifu.getTtKifuDetail().setKifuText(driver.findElement(By.tagName("pre")).getText());
					kifu.setAppId(target.getAppId());
				}
			} catch (Exception e) {
				result.getFailerTarget().add(target);
				e.printStackTrace();
			}
			result.getScrapingKifus().addAll(kifuList);
			result.getSuccessTarget().add(target);
			String prefix = AppType.CLUB24.getCode() + "-" + target.getAppId();
			result.getCollectKifuCountMap().put(prefix, kifuList.size());
		}
		quitDriver(driver);
		return result;
	}

	/**
	 * 将棋クエストの棋譜を取得<br>
	 * スクレイピングの取得内容は下記の通り<br>
	 * [0] 日時 勝敗 手数 要因 手合
	 * [1] 先手のID、段位、レート
	 * [2] 後手のID、段位、レート
	 * [3] 閲覧リンク、棋譜リンク、CSAリンク、KENTOリンク
	 * @param targetQuest 将棋クエスト収集対象
	 * @return 将棋クエストのスクレイピング結果
	 * @throws InterruptedException 
	 */
//	public ScrapingResultData scrapingQuestKifu(List<TtAppTarget> targetQuest)
//			throws InterruptedException {
//		ScrapingResultData result = new ScrapingResultData(AppType.QUEST);
//		WebDriver driver = connectWebDriver(QUEST_SEARCH_URL, AppType.QUEST);
//		if (Objects.isNull(driver)) {
//			result.setFailerTarget(targetQuest);
//			return result;
//		}
//
//		for (TtAppTarget target : targetQuest) {
//			List<TtKifu> kifuList = new ArrayList<>();
//			try {
//				WebElement inputUser = driver.findElement(By.id("userId"));
//				inputUser.clear();
//				inputUser.sendKeys(target.getAppId());
//				kifuList.addAll(execScrapingForQuest(driver, TimeRule.QUEST_LIMIT_TEN, target.getLastCollectDate()));
//				kifuList.addAll(execScrapingForQuest(driver, TimeRule.QUEST_LIMIT_FIVE, target.getLastCollectDate()));
//				kifuList.addAll(execScrapingForQuest(driver, TimeRule.QUEST_LIMIT_TWO, target.getLastCollectDate()));
//				if (CollectionUtils.isEmpty(kifuList)) {
//					result.getNoDataTarget().add(target);
//					continue;
//				}
//				kifuList.stream().forEach(kifu -> kifu.setAppId(target.getAppId()));
//			} catch (Exception e) {
//				result.getFailerTarget().add(target);
//				e.printStackTrace();
//				continue;
//			}
//			// 最終収集日時でフィルタ
//			if(StringUtils.isNotEmpty(target.getLastCollectDate())) {
//				kifuList = kifuList.stream().filter(kifu -> target.getLastCollectDate().compareTo(kifu.getGameDate()) < 0)
//						.toList();
//			}
//			result.getScrapingKifus().addAll(kifuList);
//			result.getSuccessTarget().add(target);
//			String prefix = AppType.QUEST.getCode() + "-" + target.getAppId();
//			result.getCollectKifuCountMap().put(prefix, kifuList.size());
//		}
//		quitDriver(driver);
//		return result;
//	}

	/**
	 * WebDriverを生成する
	 * @return WebDriver
	 * @throws MalformedURLException 
	 */
	private WebDriver generateWebDriver() throws MalformedURLException {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
//		options.addArguments("--remote-allow-origins=*", "--window-size=1920,1080", "-ignore-certificate-errors",
//				"user-agenet=" + getRandomUserAgent(), "--headless");
		options.addArguments("--remote-allow-origins=*", "--window-size=1920,1080", "-ignore-certificate-errors",
				"user-agenet=" + getRandomUserAgent());
		return new ChromeDriver(options);
	}

	/**
	 * ランダムなユーザーエージェントを生成する
	 * @return ユーザーエージェント
	 */
	private String getRandomUserAgent() {
		String[] userAgents = {
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36" };
		int randomNumber = new Random().nextInt(userAgents.length);
		return userAgents[randomNumber];
	}

	/**
	 * WebDriverを終了する
	 * @param driver WebDriver
	 */
	private void quitDriver(WebDriver driver) {
		if (Objects.nonNull(driver)) {
			driver.quit();
		}
	}

	/**
	 * 将棋ウォーズのURLを生成する
	 * @param warsUserId 将棋ウォーズID
	 * @param lastCollectDate 最終収集日時
	 * @return 将棋ウォーズURL
	 */
	private String generateUrlForWars(String warsUserId, String lastCollectDate) {
		StringBuilder sb = new StringBuilder();
		sb.append(WARS_SEARCH_URL);
		sb.append(warsUserId);
		if (StringUtils.isNotEmpty(lastCollectDate)) {
			sb.append("%20日付%3A");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ApplicationConstants.DATETIME_FORMAT_WARS_QUERY);
			LocalDate now = LocalDate.now();
			String nowStr = now.format(formatter);
			sb.append(DateTimeUtil.convertDateStrFormat(lastCollectDate,
					ApplicationConstants.DATETIME_FORMAT_SYSTEM, ApplicationConstants.DATETIME_FORMAT_WARS_QUERY));
			sb.append("..");
			sb.append(nowStr);
		}
		return sb.toString();
	}

	/**
	 * スクレイピングした結果を設定する
	 * @param rows スクレイピングレコード（テーブル行）
	 * @param waitDriver waitDriver
	 * @return 棋譜リスト
	 * @throws Exception 例外が発生した場合
	 */
	private List<TtKifu> setScrapingResultForWars(List<WebElement> rows, WebDriverWait waitDriver) throws Exception {
		rows.remove(0); //ヘッダ行を除外
		List<TtKifu> kifuList = new ArrayList<>();
		for (WebElement row : rows) {
			TtKifu kifu = new TtKifu();
			TtKifuDetail kifuDetail = new TtKifuDetail();
			kifu.setAppType(AppType.WARS.getCode());
			List<WebElement> tds = row.findElements(By.tagName("td"));

			for (int i = 0; i < tds.size(); i++) {
				WebElement td = tds.get(i);
				if (i == 0) {
					kifu.setSenteId(td.findElement(By.className("mr-1")).getText());
					String senteGrade = td.findElement(By.className("ml-1")).getText();
					kifu.setSenteGrade(Grade.getGradeByName(senteGrade).getCode());
				} else if (i == 1) {
					kifu.setGoteId(td.findElement(By.className("mr-1")).getText());
					String goteGrade = td.findElement(By.className("ml-1")).getText();
					kifu.setGoteGrade(Grade.getGradeByName(goteGrade).getCode());
				} else if (i == 3) {
					// 1件目がKENTO、2件目がWarsの棋譜URLの元ネタ
					List<WebElement> links = td.findElements(By.tagName("a"));
					String[] urlArray = links.get(1).getAttribute("href").split("/");
					// URLを編集してWARSの詳細URLを設定
					String urlPrefix = urlArray[urlArray.length - 2];
					kifu.setShowLink(WARS_DETAIL_URL + urlPrefix);
					// URLから対局日時を設定
					String[] urlTextArray = kifu.getShowLink().split("-");
					String dateStr = urlTextArray[urlTextArray.length - 1];
					kifu.setGameDate(DateTimeUtil.convertDateStrFormat(dateStr,
							"yyyyMMdd_HHmmss",
							ApplicationConstants.DATETIME_FORMAT_SYSTEM));

					// 棋譜を取得
					String kifuUrl = WARS_KIFU_BASE_URL + urlPrefix + ".kif";
					WebDriver driver = connectWebDriver(kifuUrl, AppType.WARS);
					String kifuText = driver.findElement(By.tagName("pre")).getText();
					kifuDetail.setKifuText(kifuText);
				}
			}
			
			kifu.setTtKifuDetail(kifuDetail);
			kifuList.add(kifu);
		}
		return kifuList;
	}

	/**
	 * スクレイピングした結果を設定する
	 * @param rows スクレイピングレコード（テーブル行）
	 * @param club24Id 将棋倶楽部24のID
	 * @return 棋譜リスト
	 * @throws Exception 例外が発生した場合
	 */
	private List<TtKifu> setScrapingResultForClub24(List<WebElement> rows, String club24Id) throws Exception {
		List<TtKifu> kifuList = new ArrayList<>();
		for (WebElement row : rows) {
			TtKifu kifu = new TtKifu();
			TtKifuDetail kifuDetail = new TtKifuDetail();
			kifu.setAppType(AppType.CLUB24.getCode());
			List<WebElement> tds = row.findElements(By.tagName("td"));

			for (int i = 0; i < tds.size(); i++) {
				WebElement td = tds.get(i);
				if (i == 0) {
					List<WebElement> links = td.findElements(By.tagName("a"));
					kifu.setShowLink(links.get(1).getAttribute("href"));
				} else if (i == 1) {
					kifu.setDetailLink(td.findElement(By.tagName("a")).getAttribute("href"));
				} else if (i == 3) {
					kifuDetail.setTimeRule(TimeRule.getTimeRuleByClub24Code(td.getText()).getCode());
				} else if (i == 4) {
					kifu.setGameDate(
							DateTimeUtil.convertDateStrFormat(td.getText(),
									ApplicationConstants.DATETIME_FORMAT_CLUB24,
									ApplicationConstants.DATETIME_FORMAT_SYSTEM));
				} else if (i == 5) {
					kifu.setSenteRate(Integer.valueOf(td.getText()));
					kifu.setSenteGrade(Grade.getGradeByClub24Rate(kifu.getSenteRate()).getCode());
				} else if (i == 6) {
					kifu.setSenteId(td.getText());
				} else if (i == 7) {
					// 勝敗判定
					List<WebElement> resultElements = td.findElements(By.tagName("i"));
					if (CollectionUtils.isEmpty(resultElements)) {
						kifu.setGameResult(GameResult.DRAW.getCode());
					} else {
						WebElement resultSente = resultElements.get(0);
						WebElement resultGote = resultElements.get(1);
						boolean winSente = resultSente.getAttribute("class").equals(CLUB24_WIN_SELECTOR);
						boolean winGote = resultGote.getAttribute("class").equals(CLUB24_WIN_SELECTOR);
						if (winSente) {
							kifu.setGameResult(GameResult.SNETE_WIN.getCode());
						} else if (winGote) {
							kifu.setGameResult(GameResult.GOTE_WIN.getCode());
						}
					}
				} else if (i == 8) {
					kifu.setGoteRate(Integer.parseInt(td.getText()));
					kifu.setGoteGrade(Grade.getGradeByClub24Rate(kifu.getGoteRate()).getCode());
				} else if (i == 9) {
					kifu.setGoteId(td.getText());
				} else if (i == 10) {
					if (StringUtils.isEmpty(td.getText())) {
						kifuDetail.setMatchRule("平手");
					} else {
						kifuDetail.setMatchRule(td.getText());
					}
				} else if (i == 11) {
					kifuDetail.setHandCount(Integer.parseInt(td.getText()));
				} else if (i == 13) {
					if(td.getText().equals("対局中")) {
						continue; // 対局中の将棋は次回収集で取得する
					}
					kifuDetail.setAddInfo(td.getText());
				}
			}
			kifu.setTtKifuDetail(kifuDetail);
			kifuList.add(kifu);
		}
		return kifuList;
	}

	/**
	 * 将棋ウォーズのスクレイピング結果を補完する
	 * @param kifuList 棋譜リスト
	 * @param driver Webドライバー
	 */
	private void complementKifuDataForWars(List<TtKifu> kifuList, WebDriver driver) {
		for (TtKifu kifu : kifuList) {
			driver.get(kifu.getShowLink());
			// 先手、後手を正しく設定
			// 先手のユーザー名を取得　▲XXXX Y段
			String senteText = driver.findElement(By.className(WARS_KIFU_DETAIL_SENTE_SELECTOR))
					.findElement(By.tagName("a")).getText();
			String senteUserId = senteText.substring(1, senteText.indexOf(" "));
			if (!StringUtils.equals(kifu.getSenteId(), senteUserId)) {
				String gradeTmp = kifu.getGoteGrade();
				kifu.setGoteId(kifu.getSenteId());
				kifu.setGoteGrade(kifu.getSenteGrade());
				kifu.setSenteId(senteUserId);
				kifu.setSenteGrade(gradeTmp);
			}

			// 勝者判定
			WebElement selectElement = driver.findElement(By.className(WARS_KIFU_OPTIONS_SELECTOR));
			Select select = new Select(selectElement);
			WebElement lastOption = select.getOptions().get(select.getOptions().size() - 1);
			String resultText = lastOption.getText();
			if (resultText.contains("先手")) {
				kifu.setGameResult(GameResult.SNETE_WIN.getCode());
			} else if (resultText.contains("後手")) {
				kifu.setGameResult(GameResult.GOTE_WIN.getCode());
			} else {
				kifu.setGameResult(GameResult.DRAW.getCode());
			}

			// 手数取得
			WebElement preFromLastOption = select.getOptions().get(select.getOptions().size() - 2);
			kifu.getTtKifuDetail().setHandCount(Integer.parseInt(preFromLastOption.getAttribute("value")));

			// 時間ルール取得
			WebElement timeElement = driver.findElement(By.className(WARS_TIME_SELECTOR));
			kifu.getTtKifuDetail()
					.setTimeRule(TimeRule.getTimeRuleByWarsTimeValue(timeElement.getText()).getCode());
		}

	}

	/**
	 * 将棋倶楽部24のスクレイピング処理を実施する
	 * @param driver Webドライバー
	 * @param club24Id 将棋倶楽部24ID
	 * @param lastCollectDate 最終収集日時
	 * @throws InterruptedException 例外が発生した場合
	 */
	private void execScrapingForClub24(WebDriver driver, WebDriverWait waitDriver, String club24Id, String lastCollectDate)
			throws InterruptedException {
		// 検索実行
		waitDriver.until(ExpectedConditions.elementToBeClickable(By.id("searchBtn")));
		WebElement inputElement = driver.findElement(By.name("name1"));
		inputElement.clear();
		inputElement.sendKeys(club24Id);

		String fromDate = null;
		if (StringUtils.isEmpty(lastCollectDate)) {
			LocalDate ldt = LocalDate.now().minus(1, ChronoUnit.YEARS);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ApplicationConstants.DATETIME_FORMAT_CLUB24_QUERY);
			fromDate = ldt.format(dtf);
		} else {
			fromDate = DateTimeUtil.convertDateStrFormat(lastCollectDate,
					ApplicationConstants.DATETIME_FORMAT_SYSTEM, ApplicationConstants.DATETIME_FORMAT_CLUB24_QUERY);
		}
		WebElement inputElementDate = driver.findElement(By.name("fromdate"));
		inputElementDate.clear();
		inputElementDate.sendKeys(fromDate);

		WebElement buttonElement = driver.findElement(By.id("searchBtn"));
		buttonElement.click();
		Thread.sleep(3000); //検索結果を読み込むために一時停止
	}

//	private List<TtKifu> execScrapingForQuest(WebDriver driver, TimeRule timeRule, String lastCollectDate) throws Exception {
//		WebDriverWait waitDriver = new WebDriverWait(driver, Duration.ofSeconds(10));
//		String year = String.valueOf(Year.now().getValue());
//		List<TtKifu> kifuList = new ArrayList<>();
//
//		WebElement timeRuleBtn;
//		if (TimeRule.QUEST_LIMIT_TEN.equals(timeRule)) {
//			timeRuleBtn = driver.findElement(By.id("rd0"));
//		} else if (TimeRule.QUEST_LIMIT_FIVE.equals(timeRule)) {
//			timeRuleBtn = driver.findElement(By.id("rd1"));
//		} else {
//			timeRuleBtn = driver.findElement(By.id("rd2"));
//		}
//
//		try {
//			waitDriver.until(ExpectedConditions.elementToBeClickable(timeRuleBtn)).click();
//		} catch (ElementClickInterceptedException e) {
//			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", timeRuleBtn);
//			timeRuleBtn.click();
//		}
//
//		WebElement getBtn = driver.findElement(By.id("btnGet"));
//		waitDriver.until(ExpectedConditions.elementToBeClickable(getBtn)).click();
//		Thread.sleep(3000);
//
//		List<WebElement> rows;
//		int retry = 0;
//		while (true) {
//			WebElement kifuSearchTable = driver.findElement(By.id("tblHistory-tbody"));
//			rows = kifuSearchTable.findElements(By.tagName("tr"));
//			if (!CollectionUtils.isEmpty(rows)) {
//				break;
//			}
//			retry++;
//			if (retry == retryCount) {
//				// 収集対象棋譜なし
//				return new ArrayList<>();
//			}
//			Thread.sleep(1000);
//		}
//
//		for (WebElement row : rows) {
//			TtKifu kifu = new TtKifu();
//			TtKifuDetail kifuDetail = new TtKifuDetail();
//			kifu.setAppType(AppType.QUEST.getCode());
//			kifuDetail.setTimeRule(timeRule.getCode());
//			List<WebElement> tds = row.findElements(By.tagName("td"));
//
//			String gameResultCode = "3"; //対象者目線で、1:勝ち 2:負け 3:引分
//			boolean alreadyCollected = false;
//			boolean removedKifu = false; //サーバーから削除された棋譜をあらわす
//			boolean isSente = false;
//			for (int i = 0; i < tds.size(); i++) {
//				WebElement td = tds.get(i);
//				if (i == 0) {
//					String[] dataArray = td.getText().split(" ");
//					String dateTime = year + "/" + dataArray[0] + " " + dataArray[1];
//					String[] dataArray2 = dataArray[2].split("\n");
//					String result = dataArray2[0];
//					String handCount = dataArray2[1];
//					try {
//						kifu.setGameDate(
//								DateTimeUtil.convertDateStrFormat(dateTime, ApplicationConstants.DATETIME_FORMAT_QUEST,
//										ApplicationConstants.DATETIME_FORMAT_SYSTEM));
//					} catch (RuntimeException e) {
//						// 年が付与せずにリトライ
//						dateTime = dataArray[0] + " " + dataArray[1];
//						kifu.setGameDate(
//								DateTimeUtil.convertDateStrFormat(dateTime, ApplicationConstants.DATETIME_FORMAT_QUEST,
//										ApplicationConstants.DATETIME_FORMAT_SYSTEM));
//					}
//					if(StringUtils.isNotEmpty(lastCollectDate) && lastCollectDate.compareTo(kifu.getGameDate()) > 0) {
//						alreadyCollected = true;
//						break;
//					}
//					kifuDetail.setHandCount(Integer.parseInt(handCount));
//					if (result.equals("○")) {
//						gameResultCode = "1";
//					} else if (result.equals("×")) {
//						gameResultCode = "2";
//					}
//
//				} else if (i == 1 || i == 2) {
//					WebElement grade;
//					try {
//						grade = td.findElement(By.className("target-0"));
//						if (i == 1)
//							isSente = true;
//					} catch (NoSuchElementException e) {
//						grade = td.findElement(By.className("target-1"));
//					}
//					String[] dataArray = grade.getText().split(" "); // [userName\n段位],[rate]
//					String[] dataArray2 = dataArray[0].split("\n");
//					if (i == 1) {
//						kifu.setSenteId(dataArray2[0]);
//						kifu.setSenteGrade(Grade.convertQuestGrade(dataArray2[1]).getCode());
//						kifu.setSenteRate(Integer.parseInt(dataArray[1]));
//					} else {
//						kifu.setGoteId(dataArray2[0]);
//						kifu.setGoteGrade(Grade.convertQuestGrade(dataArray2[1]).getCode());
//						kifu.setGoteRate(Integer.parseInt(dataArray[1]));
//					}
//
//				} else if (i == 3) {
//					List<WebElement> links = td.findElements(By.tagName("a"));
//					if (links.size() < 2) {
//						// サーバーから削除された棋譜はリンク1件のみ 取得対象外
//						removedKifu = true;
//						break;
//					}
//					WebElement showLink = links.get(0);
//					kifu.setShowLink(showLink.getAttribute("href"));
//
//					WebElement kifuWindow = links.get(1);
//					try {
//						waitDriver.until(ExpectedConditions.elementToBeClickable(kifuWindow)).click();
//					} catch (ElementClickInterceptedException e) {
//						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", kifuWindow);
//						kifuWindow.click();
//					}
//					WebElement btnKifCopy = driver.findElement(By.id("btnKifCopy"));
//					waitDriver.until(ExpectedConditions.elementToBeClickable(btnKifCopy)).click();
//					Toolkit kit = Toolkit.getDefaultToolkit();
//					Clipboard clip = kit.getSystemClipboard();
//					kifuDetail.setKifuText((String) clip.getData(DataFlavor.stringFlavor));
//					// コピーが終わったらウィンドウを閉じるui-id-1
//					WebElement btnClose = driver.findElement(By.className("ui-dialog-titlebar-close"));
//					waitDriver.until(ExpectedConditions.elementToBeClickable(btnClose)).click();
//				}
//			}
//			if (alreadyCollected || removedKifu) {
//				continue;
//			}
//			// 勝敗結果の判定
//			if (isSente) {
//				if (gameResultCode.equals("1"))
//					kifu.setGameResult(GameResult.SNETE_WIN.getCode());
//				else if (gameResultCode.equals("2"))
//					kifu.setGameResult(GameResult.GOTE_WIN.getCode());
//				else
//					kifu.setGameResult(GameResult.DRAW.getCode());
//			} else {
//				if (gameResultCode.equals("1"))
//					kifu.setGameResult(GameResult.GOTE_WIN.getCode());
//				else if (gameResultCode.equals("2"))
//					kifu.setGameResult(GameResult.SNETE_WIN.getCode());
//				else
//					kifu.setGameResult(GameResult.DRAW.getCode());
//			}
//			kifu.setTtKifuDetail(kifuDetail);
//			kifuList.add(kifu);
//		}
//		return kifuList;
//	}

	/**
	 * 収集済みの棋譜を除外する
	 * @param kifuList 棋譜リスト
	 * @return 棋譜リスト（収集済み棋譜を除外）
	 */
	private List<TtKifu> excludeKifuAlreadyCollected(List<TtKifu> kifuList) {
		LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)
				.withNano(0);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ApplicationConstants.DATETIME_FORMAT_SYSTEM);
		String tomorrowStr = tomorrow.format(formatter);
		// 当日収集分の棋譜を取得
		List<String> todayGameDate = kifuList.stream()
				.filter(t -> Objects.compare(t.getGameDate(), tomorrowStr, String::compareTo) == -1)
				.map(t -> t.getGameDate())
				.collect(Collectors.toList());
		List<TtKifu> ttKifuAlreadyCollected = kifuCustomRepository.findKifuAlreadyCollected(todayGameDate);
		List<String> gameDateAlreadyCollected = ttKifuAlreadyCollected.stream().map(t -> t.getGameDate())
				.collect(Collectors.toList());
		// 収集済みの棋譜は除外
		kifuList = kifuList.stream().filter(t -> !gameDateAlreadyCollected.contains(t.getGameDate()))
				.collect(Collectors.toList());
		return kifuList;
	}

	private WebDriver connectWebDriver(String url, AppType appType) throws InterruptedException {
		int retry = 0;
		while (true) {
			try {
				WebDriver driver = generateWebDriver();
				driver.get(url); //対象サイトが読込完了するまでSelniumが待機
				return driver;
			} catch (Exception e) {
				retry++;
				if (retry == retryCount) {
					logger.error("スクレイピング対象の接続に失敗しました。対象:{},URL:{}", appType.getName(), url);
					return null;
				}
				Thread.sleep(1000);
				logger.warn("接続失敗のためリトライします。リトライ{}回目,対象:{},URL:{}\"", retry, appType.getName(), url);
				continue;
			}
		}
	}
	
	@Data
	class ScrapingResultData {
		private AppType appType;
		private List<TtKifu> scrapingKifus = new ArrayList<>();
		private List<TtAppTarget> successTarget = new ArrayList<>();
		private List<TtAppTarget> failerTarget = new ArrayList<>();
		private List<TtAppTarget> noDataTarget = new ArrayList<>();
		private Map<String, Integer> collectKifuCountMap = new HashMap<>();
		
		public ScrapingResultData(AppType appType) {
			this.appType = appType;
		}
	}
}