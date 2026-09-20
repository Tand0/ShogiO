package com.github.tand0.andshogio.tool;

import com.github.tand0.andshogio.util.InputGen;
import com.github.tand0.andshogio.util.MainDatabase;
import com.github.tand0.andshogio.util.PropertyDefine;
import com.github.tand0.andshogio.util.TableKey;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** 学習したテーブルを合算する。
 * 重複したテーブルがある場合はここで消す。
 * 全て先手番に変更する
 */
public class A005DBTotal extends DBBase {
    /** 読み込み先DB名 */
    private String[] loadDB;

    /** レイヤ― */
    private final InputGen inputGen = new InputGen();

    /** 読み込みするDB名の設定
     *
     * @param loadDB ロードするDB名
     */
    private void setLoadDB(String[] loadDB) {
        this.loadDB = loadDB;
    }


    /** コンストラクタ
     */
    public A005DBTotal() {
    }

    @Override
    public boolean fileExists(String path) {
        return false; // ファイルのスキップを run() 側でさせない
    }

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException, IOException {
        A005DBTotal db = new A005DBTotal();
        String saveDb = MyPropertiesAccess.getString(db.getSaveDb());
        if ((new File(saveDb)).exists()) {
            System.out.println("skip db=" + saveDb);
            return;
        }
        //
        //
        // 実行は最後上書きされて良い順
        String[] dbNames = {PropertyDefine.APP_DB_BAD, PropertyDefine.APP_DB_ALL};
        db.setLoadDB(dbNames);
        db.run();
    }

    @Override
    public String[] getTargetDb() {
        return this.loadDB;
    }

    @Override
    public String getSaveDb() {
        return PropertyDefine.APP_DB_TOTAL;
    }

    @Override
    public String getSelectSql() {
        return "SELECT key1, key2, key3, key4, win, los FROM mytable";
    }

    @Override
    public String getCountSql() {
        return "SELECT COUNT(*) AS total_count FROM mytable;";
    }

    @Override
    public void createSaveData(
            MainDatabase base,
            PreparedStatement ps,
            TableKey tableKey,
            long win, long los) throws SQLException {
        // 保存する
        TableKey inputKey = inputGen.createInputGenKey(tableKey);
        if (tableKey.getTeban() == 0) {
            // 先手番
            setSQL(inputKey.getKey(), win, los, ps);
        } else {
            // 後手番の場合はレイヤが上下反転して先後も反転しているので
            // win, los も反転させて保存する
            setSQL(inputKey.getKey(), los, win, ps);
        }
    }
}
