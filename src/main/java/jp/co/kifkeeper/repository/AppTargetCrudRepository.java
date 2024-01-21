package jp.co.kifkeeper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.kifkeeper.model.table.TtAppTarget;
import jp.co.kifkeeper.model.table.TtAppTargetPK;

public interface AppTargetCrudRepository extends JpaRepository<TtAppTarget, TtAppTargetPK> {

	@Modifying
	@Query(value = "DELETE FROM tt_user_app_target t WHERE t.user_id = :userId", nativeQuery = true)
	void deleteByUserId(@Param("userId") Integer userId);
	
	@Query(value = "SELECT * FROM tt_user_app_target t WHERE t.user_id = :userId ORDER BY t.app_type", nativeQuery = true)
	List<TtAppTarget> findByUserId(@Param ("userId") Integer userId);
}
