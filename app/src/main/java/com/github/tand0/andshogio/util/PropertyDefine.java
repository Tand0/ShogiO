package com.github.tand0.andshogio.util;

/** プロパティの定義 */
public interface PropertyDefine {
    /** ボーダー(特定値以上の場合は定跡として計算する) */
    String APP_BORDER = "app.border";

    /** レート(CSAファイルから読み込む時の指し手のウマさの閾値)
     * default: 1500
     */
    String APP_RATE_MIN = "app.rate.min";

    /** レイヤ最大数 */
    String APP_CHANNEL_MAX = "app.channel.max";

    /** 大量の棋譜置き場 */
    String APP_LOAD_TARGET = "app.load.target";

    /** プライバシーポリシの URL */
    String URL_POLICY = "url.policy";

    /** レポートの URL */
    String URL_REPORT = "url.report";

    /** 棋譜を全て DB に織り込んだもの */
    String APP_DB_ALL = "app.db.all";

    /** 処理した DB を全てまとめたもの */
    String APP_DB_TOTAL = "app.db.total";

    /** 棋譜をボーダーで縮小したもの */
    String APP_DB_SMALL = "app.db.small";

    /** 棋譜をボーダーで縮小したもので学習かけたもの */
    String APP_DB_EVAL = "app.db.eval";

    /** 棋譜を小さくしたものの残り物 */
    String APP_DB_REMAIN = "app.db.remain";

    /** 評価用のテーブル */
    String APP_DB_ESTIMATE = "app.db.estimate";

    /** ダメな手を生成する */
    String APP_DB_BAD = "app.db.bad";

    /** 全てのデータセット */
    String APP_DATASET_ALL = "app.dataset.all";
}
