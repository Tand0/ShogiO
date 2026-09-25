package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.MainDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** データベース実装 */
public class MainDatabaseImpl implements MainDatabase, AutoCloseable {
    /** Connection */
    private Connection connSecondSelect;

    /** ステートメント */
    private PreparedStatement preparedStatement;

    /** コンストラクタ
     * @param url url
     */
    public MainDatabaseImpl(String url) {
        try {
            connSecondSelect = DriverManager.getConnection(url);
            String selectSql = "SELECT win,los FROM mytable " +
                    "WHERE key1=? and key2=? and key3=? and key4=?;";
            preparedStatement = connSecondSelect.prepareStatement(selectSql);
        } catch(SQLException e) {
            preparedStatement = null;
        }

    }
    /** データ読み込み (Select) のメソッド
     * @param key key
     * @return win, los
     */
    @Override
    public long[] getData(long[] key) {
        if (preparedStatement == null) {
            return null;
        }
        try {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                preparedStatement.setLong(1, key[0]);
                preparedStatement.setLong(2, key[1]);
                preparedStatement.setLong(3, key[2]);
                preparedStatement.setLong(4, key[3]);
                if (rs.next()) {
                    long[] result;
                    long win = rs.getLong("win");
                    long los = rs.getLong("los");
                    result = new long[2];
                    result[0] = win;
                    result[1] = los;
                    return result;
                }
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /** 終了処理 */
    @Override
    public void close() throws SQLException {
        if (preparedStatement == null) {
            return;
        }
        preparedStatement.close();
        preparedStatement = null;
        //
        if (connSecondSelect == null) {
            return;
        }
        connSecondSelect.close();
        connSecondSelect = null;
    }
}
