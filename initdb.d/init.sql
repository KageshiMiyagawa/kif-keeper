CREATE TABLE kifkeeper.tt_app_target(
    app_type CHAR(1) NOT NULL COMMENT 'アプリ種別',    
    app_id VARCHAR(32) NOT NULL COMMENT 'アプリID',    
    last_collect_date CHAR(12) COMMENT '最終収集日時',
    valid_flg CHAR(1) COMMENT '有効フラグ',
    PRIMARY KEY (app_type, app_id)
) COMMENT 'アプリ対象';

CREATE TABLE kifkeeper.tt_kifu(
    kifu_id INT NOT NULL COMMENT '棋譜ID',
    app_id VARCHAR(32) NOT NULL COMMENT 'アプリID',
    sente_id VARCHAR(32) NOT NULL COMMENT '先手ID',
    sente_grade CHAR(2) COMMENT '先手段位',
    sente_rate SMALLINT COMMENT '先手レート',
    gote_id VARCHAR(32) NOT NULL COMMENT '後手ID',
    gote_grade CHAR(2) COMMENT '後手段位',
    gote_rate SMALLINT COMMENT '後手レート',
    game_result CHAR(1) NOT NULL COMMENT '対局結果',
    game_date CHAR(12) NOT NULL COMMENT '対局日時',
    app_type CHAR(1) NOT NULL COMMENT 'アプリ種別',
    show_link VARCHAR(1024) COMMENT '閲覧リンク',
    detail_link VARCHAR(1024) COMMENT '詳細リンク',
    regist_date CHAR(12) NOT NULL COMMENT '登録日時',
    PRIMARY KEY (kifu_id)
) COMMENT '棋譜';

CREATE TABLE kifkeeper.tt_kifu_detail (
    kifu_id INT NOT NULL PRIMARY KEY COMMENT '棋譜ID',
    hand_count SMALLINT COMMENT '手数',
    kifu_text TEXT COMMENT '棋譜テキスト',
    time_rule CHAR(2) COMMENT '時間',
    match_rule VARCHAR(10) COMMENT '手合い',
    style_1 VARCHAR(20) COMMENT '戦型1',
    style_2 VARCHAR(20) COMMENT '戦型2',
    style_3 VARCHAR(20) COMMENT '戦型3',
    style_4 VARCHAR(20) COMMENT '戦型4',
    style_5 VARCHAR(20) COMMENT '戦型5',
    add_info VARCHAR(255) COMMENT '追加情報',
    regist_date CHAR(12) NOT NULL COMMENT '登録日時'
) COMMENT='棋譜詳細';

CREATE TABLE kifkeeper.tt_user_setting(
    app_type CHAR(1) NOT NULL COMMENT 'アプリ種別',
    set_type CHAR(1) NOT NULL COMMENT '設定種別',
    set_data VARCHAR(256) NOT NULL COMMENT '設定内容',
    valid_flg CHAR(1) COMMENT '有効フラグ',
    PRIMARY KEY (app_type, set_type)
) COMMENT 'ユーザ設定';