package jp.co.kifkeeper.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.kifkeeper.ApplicationConstants;
import jp.co.kifkeeper.code.AppType;
import jp.co.kifkeeper.code.GameResult;
import jp.co.kifkeeper.code.Grade;
import jp.co.kifkeeper.code.TimeRule;
import jp.co.kifkeeper.model.KifuData;
import jp.co.kifkeeper.model.KifuFetchCond;
import jp.co.kifkeeper.model.table.TtAppTarget;
import jp.co.kifkeeper.model.table.TtKifu;
import jp.co.kifkeeper.model.table.TtKifuDetail;
import jp.co.kifkeeper.repository.AppTargetCrudRepository;
import jp.co.kifkeeper.repository.KifuCrudRepository;
import jp.co.kifkeeper.repository.KifuCustomRepository;
import jp.co.kifkeeper.repository.KifuDetailCrudRepository;
import jp.co.kifkeeper.util.DateTimeUtil;
@Service
public class KifuManageService {
	
	@Autowired
	private KifuCrudRepository kifuCrudRepository;
	@Autowired
	private KifuDetailCrudRepository kifuDetailCrudRepository;
	@Autowired
	private KifuCustomRepository kifuCustomRepository;
	@Autowired
	private AppTargetCrudRepository appTargetCrudRepository;
	
	private final Logger logger = LoggerFactory.getLogger("");

	/**
	 * 棋譜を検索する
	 * @param kifuFetchCond 検索条件
	 * @return スクレイピング結果
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<KifuData> findKifu(KifuFetchCond kifuFetchCond) {
		// 日時のフォーマット
		if (StringUtils.isNotEmpty(kifuFetchCond.getStartDt())) {
			kifuFetchCond.setStartDt(
					DateTimeUtil.convertDateStrFormat(kifuFetchCond.getStartDt(),
							ApplicationConstants.DATETIME_FORMAT_SEARCH_INPUT,
							ApplicationConstants.DATETIME_FORMAT_SYSTEM));
		}
		if (StringUtils.isNotEmpty(kifuFetchCond.getEndDt())) {
			kifuFetchCond.setEndDt(
					DateTimeUtil.convertDateStrFormat(kifuFetchCond.getEndDt(),
							ApplicationConstants.DATETIME_FORMAT_SEARCH_INPUT,
							ApplicationConstants.DATETIME_FORMAT_SYSTEM));
		}
		List<TtAppTarget> appTargetList = appTargetCrudRepository.findAll();
		List<String> appIds = appTargetList.stream()
				.map(userApp -> userApp.getAppId()).toList();
		// 棋譜検索
		List<TtKifu> ttKifuList = kifuCustomRepository.findKifuByFetchCond(kifuFetchCond, appIds);
		
		// 個別条件の絞り込み
		if (StringUtils.isNotBlank(kifuFetchCond.getStartGrade()) || StringUtils.isNotBlank(kifuFetchCond.getEndGrade())) {
			// アプリ種別による絞り込み
			if (StringUtils.isNotEmpty(kifuFetchCond.getAppType())) {
				appTargetList = appTargetList.stream()
						.filter(target -> kifuFetchCond.getAppType().equals(target.getAppType())).toList();
			}
			List<TtKifu> filterTtKifuList = new ArrayList<>();
			for (TtKifu ttKifu : ttKifuList) {
				if (appIds.contains(ttKifu.getSenteId())) {
					// 先手が自分の場合、後手の段位を判定
					if (StringUtils.isNotBlank(kifuFetchCond.getStartGrade())) {
						if (ttKifu.getGoteGrade().compareTo(kifuFetchCond.getStartGrade()) >= 0) {
							filterTtKifuList.add(ttKifu);
						}
					} else {
						if (ttKifu.getGoteGrade().compareTo(kifuFetchCond.getEndGrade()) < 0) {
							filterTtKifuList.add(ttKifu);
						}
					}
				} else {
					// 後手が自分の場合、先手の段位を判定
					if (StringUtils.isNotBlank(kifuFetchCond.getStartGrade())) {
						if (ttKifu.getSenteGrade().compareTo(kifuFetchCond.getStartGrade()) >= 0) {
							filterTtKifuList.add(ttKifu);
						}
					} else {
						if (ttKifu.getSenteGrade().compareTo(kifuFetchCond.getEndGrade()) < 0) {
							filterTtKifuList.add(ttKifu);
						}
					}					
				}
			}
			ttKifuList = filterTtKifuList;
		}
		
		if (StringUtils.isNotBlank(kifuFetchCond.getWinnerId())) {
			List<TtKifu> filterTtKifuList = new ArrayList<>();
			for (TtKifu kifu : ttKifuList) {
				if (kifu.getGameResult().equals(GameResult.SNETE_WIN.getCode())
						&& kifu.getSenteId().equals(kifuFetchCond.getWinnerId())) {
					filterTtKifuList.add(kifu);
					continue;
				}
				
				if (kifu.getGameResult().equals(GameResult.GOTE_WIN.getCode())
						&& kifu.getGoteId().equals(kifuFetchCond.getWinnerId())) {
					filterTtKifuList.add(kifu);
				}
			}
			ttKifuList = filterTtKifuList;
		}
		
		if (StringUtils.isNotBlank(kifuFetchCond.getLoserId())) {
			List<TtKifu> filterTtKifuList = new ArrayList<>();
			for (TtKifu kifu : ttKifuList) {
				if (kifu.getGameResult().equals(GameResult.SNETE_WIN.getCode())
						&& kifu.getGoteId().equals(kifuFetchCond.getLoserId())) {
					filterTtKifuList.add(kifu);
					continue;
				}
				
				if (kifu.getGameResult().equals(GameResult.GOTE_WIN.getCode())
						&& kifu.getSenteId().equals(kifuFetchCond.getLoserId())) {
					filterTtKifuList.add(kifu);
				}
			}
			ttKifuList = filterTtKifuList;
		}
		
		
		List<KifuData> kifuDataList = ttKifuList.stream().map(t -> convertUserDataForDisp(t)).collect(Collectors.toList());
		return kifuDataList;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public TtKifu findKifu(int kifuId) {
		Optional<TtKifu> ttKifu = kifuCrudRepository.findById(kifuId);
		if(ttKifu.isPresent()) {
			return ttKifu.get();
		}
		return null;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public String getKifuText(int kifuId) {
		Optional<TtKifu> ttKifu = kifuCrudRepository.findById(kifuId);
		if(ttKifu.isPresent()) {
			return ttKifu.get().getTtKifuDetail().getKifuText();
		}
		return null;
	}
	
	@Transactional
	public void deleteKifuByAppId (String appId) {
		List<TtKifu> ttKifuList = kifuCrudRepository.findByAppId(appId);
		kifuCrudRepository.deleteAllInBatch(ttKifuList);
		logger.info("appId: " + appId + " の棋譜情報を削除しました。{" + ttKifuList.size() + "}件");
		// カスケード削除が効かないので、別途棋譜詳細も削除する。
		List<Integer> kifuIds = ttKifuList.stream().map(kifu -> kifu.getKifuId()).toList();
		List<TtKifuDetail> ttKifuDetailList = kifuDetailCrudRepository.findAllById(kifuIds);
		kifuDetailCrudRepository.deleteAllInBatch(ttKifuDetailList);
		logger.info("appId: " + appId + " の棋譜詳細情報を削除しました。{" + ttKifuDetailList.size() + "}件");
	}
	
	private KifuData convertUserDataForDisp (TtKifu ttKifu) {
		KifuData kifuData = new KifuData();
		BeanUtils.copyProperties(ttKifu, kifuData);
		kifuData.setSenteGrade(Grade.getGradeByCode(ttKifu.getSenteGrade()).getName());
		kifuData.setGoteGrade(Grade.getGradeByCode(ttKifu.getGoteGrade()).getName());
		kifuData.setGameResult(GameResult.getGameResultByCode(ttKifu.getGameResult()).getName());
		kifuData.setAppType(AppType.getAppTypeByCode(ttKifu.getAppType()).getName());
		kifuData.setGameDate(
				DateTimeUtil.convertDateStrFormat(kifuData.getGameDate(), ApplicationConstants.DATETIME_FORMAT_SYSTEM,
						ApplicationConstants.DATETIME_FORMAT_DISP));
		kifuData.setHandCount(ttKifu.getTtKifuDetail().getHandCount());
		kifuData.setTimeRule(TimeRule.getTimeRuleByCode(ttKifu.getTtKifuDetail().getTimeRule()).getName());
		kifuData.setKifuText(ttKifu.getTtKifuDetail().getKifuText());
		return kifuData;
	}
}