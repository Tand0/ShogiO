package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.TableKey;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** DB生成の基底クラス */
public abstract class DBBase extends MyTimer {

    /** 重複した場合は上書きする */
    public static final String INSERT_SQL = """
                    INSERT INTO mytable (key1, key2, key3, key4, win, los)
                            VALUES (?, ?, ?, ?, ?, ?)
                            ON CONFLICT(key1, key2, key3, key4)
                            DO UPDATE SET win = excluded.win, los = excluded.los;
                    """;

    /** コンストラクタ
     */
    public DBBase() {
    }

    /** ターゲット DB の取得
     * @return 保存先
     */
    public abstract String[] getTargetDb();

    /** 保存先の取得
     * @return 保存先
     */
    public abstract String getSaveDb();

    /**
     * メイン処理
     * @throws IOException IO Exception
     * @throws SQLException sql exception
     */
    public void run() throws IOException, SQLException {
        String saveDb = MyPropertiesAccess.getString(getSaveDb());
        if (this.fileExists(saveDb)) {
            System.out.println("skip db=" + saveDb);
            return;
        }
        //
        // SQLを開いていく
        for (String targetDBString : getTargetDb()) {
        String targetDB = MyPropertiesAccess.getString(targetDBString);
        System.out.println("run start! db=" + targetDB);
        //
        try (Connection connTarget = DriverManager.getConnection(fToU(targetDB));
             Statement stmtTarget = connTarget.createStatement(
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {
            // WAL モードにする // ディスクへの書き込みを高速化
            stmtTarget.execute("PRAGMA journal_mode = WAL;");
            // OSのディスク書き込み完了を待たずに、次の処理へ進む
            stmtTarget.execute("PRAGMA synchronous = OFF;");
            // 一時的な作業データを、ディスクではなくメモリ（RAM）上で処理する
            stmtTarget.execute("PRAGMA temp_store = MEMORY;");
            //
            // 最大値の取得
            long countMax = getCountMax(stmtTarget);
            //
            runSearch(stmtTarget, countMax);
        } catch (SQLException e) {
            System.out.println("f=" + targetDB);
            System.out.println("e=" + e.getMessage());
        }
        }
        //
    }

    /**
     * カウント値の取得
     * @param stmtTarget Statement
     * @return カウント値
     * @throws SQLException 例外
     */
    public long getCountMax(Statement stmtTarget) throws SQLException {
        //
        // 最大値の取得
        long countMax;
        System.out.println("count table! start sql=" + this.getCountSql());
        try (ResultSet rs = stmtTarget.executeQuery(this.getCountSql())) {
            if (rs.next()) {
                countMax = rs.getLong("total_count");
            } else {
                throw new SQLException("rs.next() is false");
            }
        }
        System.out.println("count table! end countMax=" + countMax);
        return countMax;
    }

    /** SQLをselect する文の取得
     *
     * @return  SQLをselect する文
     */
    public abstract String getSelectSql();

    /** SQLを count する文の取得
     *
     * @return  SQLを count する文
     */
    public abstract String getCountSql();

    /**
     * 表に存在する子供を抽出する
     * @param stmtTarget ターゲットとなる DB
     * @param countMax カウント最大値
     * @throws IOException IO Exception
     * @throws SQLException SQL Exception
     */
    public void runSearch(Statement stmtTarget, long countMax) throws IOException,SQLException {
        String saveDb = MyPropertiesAccess.getString(getSaveDb());
        String allDb = MyPropertiesAccess.getString(PropertyDefine.APP_DB_ALL);
        try (MainDatabaseImpl allMainDataBase = new MainDatabaseImpl(fToU(allDb));
             Connection connSave = DriverManager.getConnection(fToU(saveDb))) {
            //
            // 保存一括用セッションの作成
            connSave.setAutoCommit(false);
            //
            // 処理数
            long countNow = 0;
            try (Statement stmtSave = connSave.createStatement()) {
                //
                //
                super.setCountMax(countMax); // カウント設定
                //
                // テーブルがなければ生成する
                System.out.println("create table! start f=" + saveDb);
                this.createSQL(stmtSave);
                System.out.println("create table! end f=" + saveDb);
            }

            try (PreparedStatement ps = connSave.prepareStatement(INSERT_SQL)) {
                //
                // 初めから削ったDBで処理しているので where はいらない
                final String selectSql = getSelectSql();
                System.out.println(selectSql);
                try (ResultSet rs = stmtTarget.executeQuery(selectSql)) {
                    while (rs.next()) {
                        //
                        // カウントアップ
                        countNow++;
                        //
                        long[] key = new long[4];
                        key[0] = rs.getLong("key1");
                        key[1] = rs.getLong("key2");
                        key[2] = rs.getLong("key3");
                        key[3] = rs.getLong("key4");
                        long win = rs.getLong("win");
                        long los = rs.getLong("los");
                        TableKey tableKey = new TableKey(key);
                        //
                        createSaveData(allMainDataBase, ps, tableKey, win, los);
                        //
                        printTimer(countNow, connSave, ps);
                    }
                } finally {
                    ps.executeBatch();
                }
            } finally {
                connSave.commit();
            }
        }
        //
        System.out.println("run over! sum=" + countMax);
    }

    /**
     * ファイル名からURLに変換する
     * @param fileName ファイル
     * @return url
     */
    public String fToU(String fileName) {
        if (fileName == null) {
            return null;
        }
        return "jdbc:sqlite:" + (new File(fileName)).getAbsolutePath();
    }

    /**
     * INSERT で行追加し、
     * もしも行が既にあるのであれば(= ON CONFLICT ならば)、
     * winとlosの値を「上書」きする
     * @param key キー値
     * @param win 先手勝ち数
     * @param los 後手負け数
     * @param ps SQL のセッション
     * @throws SQLException SQLの例外
     */
    public static void setSQL(long[] key, long win, long los, PreparedStatement ps)
            throws SQLException {
        ps.setLong(1, key[0]);
        ps.setLong(2, key[1]);
        ps.setLong(3, key[2]);
        ps.setLong(4, key[3]);
        ps.setLong(5, win);
        ps.setLong(6, los);
        ps.addBatch();
    }

    /** 元データから何かを作って保存する
     * @param base データ取得用全データベース
     * @param ps 保存用のデータベース
     * @param tableKey テーブルキー
     * @param win 先手勝ち数
     * @param los 先手負け数
     * @throws SQLException SQL Exception
     */
    public abstract void createSaveData(
            MainDatabase base, PreparedStatement ps,
            TableKey tableKey,
            long win, long los) throws SQLException;
}
