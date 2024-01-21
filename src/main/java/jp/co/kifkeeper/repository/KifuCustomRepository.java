package jp.co.kifkeeper.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jp.co.kifkeeper.ApplicationConstants;
import jp.co.kifkeeper.model.KifuFetchCond;
import jp.co.kifkeeper.model.table.TtKifu;

@Repository
public class KifuCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public List<TtKifu> findKifuByFetchCond(KifuFetchCond kifuFetchCond, List<String> appIds) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<TtKifu> cq = cb.createQuery(TtKifu.class);
		Root<TtKifu> root = cq.from(TtKifu.class);

		List<Predicate> predicates = new ArrayList<>();

		if (StringUtils.isNotBlank(kifuFetchCond.getAppId())) {
			predicates.add(cb.equal(root.get("appId"), kifuFetchCond.getAppId()));
		} else {
			predicates.add(root.get("appId").in(appIds));
		}
		if (StringUtils.isNotBlank(kifuFetchCond.getAppType())) {
			predicates.add(cb.equal(root.get("appType"), kifuFetchCond.getAppType()));
		}
		if (StringUtils.isNotBlank(kifuFetchCond.getGameResult())) {
			predicates.add(cb.equal(root.get("gameResult"), kifuFetchCond.getGameResult()));
		}
		if (StringUtils.isNotBlank(kifuFetchCond.getAppId())) {
			predicates.add(cb.or(
					cb.equal(root.get("senteId"), kifuFetchCond.getAppId()),
					cb.equal(root.get("goteId"), kifuFetchCond.getAppId())));
		}
		if (StringUtils.isNotBlank(kifuFetchCond.getStartDt()) && StringUtils.isNotBlank(kifuFetchCond.getEndDt())) {
			predicates.add(cb.between(root.get("gameDate"), kifuFetchCond.getStartDt(), kifuFetchCond.getEndDt()));
		} else {
			if (StringUtils.isNotBlank(kifuFetchCond.getStartDt())) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("gameDate"), kifuFetchCond.getStartDt()));
			}
			if (StringUtils.isNotBlank(kifuFetchCond.getEndDt())) {
				predicates.add(cb.lessThanOrEqualTo(root.get("gameDate"), kifuFetchCond.getEndDt()));
			}
			
		}
		
		cq.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<TtKifu> query = entityManager.createQuery(cq);
		query.setMaxResults(ApplicationConstants.DEFAULT_MAX_FETCH_SIZE);
		return query.getResultList();
	}
	
	public List<TtKifu> findKifuAlreadyCollected(List<String> gameDateList) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<TtKifu> cq = cb.createQuery(TtKifu.class);
		Root<TtKifu> root = cq.from(TtKifu.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(root.get("gameDate").in(gameDateList));
		cq.where(predicates.toArray(new Predicate[predicates.size()]));

		TypedQuery<TtKifu> query = entityManager.createQuery(cq);
		return query.getResultList();
	}
	
	public int getMaxKifuId() {
		Object maxKifuId = entityManager.createNativeQuery("SELECT MAX(kifu_id) FROM tt_kifu;").getSingleResult();
		if (Objects.isNull(maxKifuId)) {
			return 0;
		}
		return (int) maxKifuId;
	}
}
