package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.BadMoveResult;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TableKey;
import com.github.tand0.shogio.util.TeTable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** 悪手を学習させる */
public class A004DBBadMove extends DBBase {
    /** コンストラクタ
     */
    public A004DBBadMove() {
    }

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException, IOException {
        A004DBBadMove db = new A004DBBadMove();
        db.run();
    }

    @Override
    public String[] getTargetDb() {
        return new String[] {PropertyDefine.APP_DB_ALL};
    }

    @Override
    public String getSaveDb() {
        return PropertyDefine.APP_DB_BAD;
    }

    /** border の取得
     *
     * @return border
     */
    public String getBorder() {
        int border;
        try {
            // データが大きすぎるのでボーダーを x 倍にする
            int x = 10;
            border = MyPropertiesAccess.getInt(PropertyDefine.APP_BORDER) * x;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return " WHERE ("  + border + " < (win + los)) and (win != 0) and (los != 0)";
    }

    @Override
    public String getSelectSql() {
        return "SELECT key1, key2, key3, key4, win, los FROM mytable" + getBorder();
    }
    @Override
    public String getCountSql() {
        return "SELECT COUNT(*) AS total_count FROM mytable" + getBorder();
    }

    @Override
    public void createSaveData(
            MainDatabase base,
            PreparedStatement ps,
            TableKey tableKey,
            long win, long los) throws SQLException {
        Table table = tableKey.createTable();
        BadMoveResult result = TableDefine.getBadMoveList(base, table);
        if (result == null) {
            return;
        }
        //
        for (TeTable teTable : result.teTableList()) {
            // 悪手を保存する
            tableKey = new TableKey(teTable.table);
            setSQL(tableKey.getKey(), result.win(), result.los(), ps);
        }
    }
}
