package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TableKey;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** 学習したテーブルを閾値によって２つに分離する */
public class A002DBSmall extends DBBase {
    /** 標的とする SQL 文 */
    private String selectSql;

    /** 標的とする count sql 文 */
    private String countSql;

    /** 保存先DB名 */
    private String saveDB;

    /** コンストラクタ
     */
    public A002DBSmall() {
    }

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException, IOException {
        A002DBSmall db = new A002DBSmall();
        int border = MyPropertiesAccess.getInt(PropertyDefine.APP_BORDER);
        //
        // 定跡として学習させる
        String where = " WHERE ("  + border + " < (win + los))";
        db.setSelectSql("SELECT key1, key2, key3, key4, win, los FROM mytable" + where);
        db.setCountSql("SELECT COUNT(*) AS total_count FROM mytable"  + where);
        db.setSaveDB(PropertyDefine.APP_DB_SMALL);
        db.run();
        //
        // 残りを保存する
        where = " WHERE ("  + border + " >= (win + los))";
        db.setSelectSql("SELECT key1, key2, key3, key4, win, los FROM mytable" + where);
        db.setCountSql("SELECT COUNT(*) AS total_count FROM mytable" + where);
        db.setSaveDB(PropertyDefine.APP_DB_REMAIN);
        db.run();
        //
        // 推論用に２個抽出する
        Table table = new Table(null,0);
        long[] key1 = (new TableKey(table)).getKey();
        where = String.format(" WHERE (key1=0x%x AND key2=0x%x AND key3=0x%x AND key4=0x%x) ",
                key1[0], key1[1], key1[2], key1[3]);
        table = new Table(table, TableDefine.changeTeStringToInt("+2726FU"));
        key1 = (new TableKey(table)).getKey();
        where += String.format(" OR (key1=0x%x AND key2=0x%x AND key3=0x%x AND key4=0x%x) ",
                key1[0], key1[1], key1[2], key1[3]);
        table = new Table(table, TableDefine.changeTeStringToInt("-8384FU"));
        key1 = (new TableKey(table)).getKey();
        where += String.format(" OR (key1=0x%x AND key2=0x%x AND key3=0x%x AND key4=0x%x) ",
                key1[0], key1[1], key1[2], key1[3]);
        db.setSelectSql("SELECT key1, key2, key3, key4, win, los FROM mytable" + where);
        db.setCountSql("SELECT COUNT(*) AS total_count FROM mytable" + where);
        db.setSaveDB(PropertyDefine.APP_DB_ESTIMATE);
        db.run();
    }

    @Override
    public String[] getTargetDb() {
        return new String[]{PropertyDefine.APP_DB_ALL};
    }

    @Override
    public String getSaveDb() {
        return this.saveDB;
    }

    @Override
    public String getSelectSql() {
        return this.selectSql;
    }

    @Override
    public String getCountSql() {
        return countSql; // "SELECT COUNT(*) AS total_count FROM mytable;";
    }

    @Override
    public void createSaveData(
            MainDatabase base,
            PreparedStatement ps,
            TableKey tableKey,
            long win, long los) throws SQLException {
        setSQL(tableKey.getKey(), win, los, ps);
    }


    /** 標的とする SQL 文の設定
     *
     * @param selectSql 標的とする SQL 文
     */
    private void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    /** 標的とする count SQL 文の設定
     *
     * @param countSql 標的とする count SQL 文
     */
    private void setCountSql(String countSql) {
        this.countSql = countSql;
    }

    /** 保存するDB名の設定
     *
     * @param saveDB 保存するDB名
     */
    private void setSaveDB(String saveDB) {
        this.saveDB = saveDB;
    }
}
