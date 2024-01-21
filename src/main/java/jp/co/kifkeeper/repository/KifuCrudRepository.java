package jp.co.kifkeeper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.kifkeeper.model.table.TtKifu;

public interface KifuCrudRepository extends JpaRepository<TtKifu, Integer> {

	List<TtKifu> findByAppId(String appId);
}
