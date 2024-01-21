package jp.co.kifkeeper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.kifkeeper.model.table.TtUserSetting;
import jp.co.kifkeeper.model.table.TtUserSettingPK;

public interface UserSettingCrudRepository extends JpaRepository<TtUserSetting, TtUserSettingPK> {

	List<TtUserSetting> findByAppType(String appType);
}
