package jp.co.kifkeeper.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.kifkeeper.model.table.TtKifuDetail;

public interface KifuDetailCrudRepository extends JpaRepository<TtKifuDetail, Integer> {

}
