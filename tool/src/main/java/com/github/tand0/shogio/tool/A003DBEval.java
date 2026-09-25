package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.EvalMoveResult;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TableKey;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** データベース内でmin/max法を使って1段階学習する */
public class A003DBEval extends DBBase {

    /** コンストラクタ
     */
    public A003DBEval() {
    }

    @Override
    public String[] getTargetDb() {
        return new String[] {PropertyDefine.APP_DB_SMALL};
    }

    @Override
    public String getSaveDb() {
        return PropertyDefine.APP_DB_EVAL;
    }

    @Override
    public String getSelectSql() {
        return "SELECT key1, key2, key3, key4, win, los FROM mytable";
    }

    @Override
    public String getCountSql() {
        return "SELECT COUNT(*) AS total_count FROM mytable;";
    }

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException, IOException {
        // いまはスキップする
        //A003DBEval db = new A003DBEval();
        //db.run();
    }

    @Override
    public void createSaveData(
            MainDatabase base,
            PreparedStatement ps,
            TableKey tableKey,
            long win, long los) throws SQLException {
        EvalMoveResult result = TableDefine.getEvalMoveList(
                base, tableKey.createTable(), win, los);
        //
        // 書き込み先
        setSQL(tableKey.getKey(), result.win(), result.los(), ps);
    }
}
