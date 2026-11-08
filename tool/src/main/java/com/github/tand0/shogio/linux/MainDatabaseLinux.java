package com.github.tand0.shogio.linux;

import com.github.tand0.shogio.tool.MyPropertiesAccess;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** データベースのロード */
public class MainDatabaseLinux implements AutoCloseable, MainDatabase {
    /** コネクション */
    private Connection connSecondSelect;

    /** ステートメント */
    private PreparedStatement preparedStatement;

    /** コンストラクタ */
    public MainDatabaseLinux() {
        try {
            String targetDBString = MyPropertiesAccess.getString(PropertyDefine.APP_DB_SMALL);
            String targetDB = MyPropertiesAccess.getString(targetDBString);
            targetDB = "jdbc:sqlite:" + (new File(targetDB)).getAbsolutePath();
            connSecondSelect = DriverManager.getConnection(targetDB);
            final String selectSql = "SELECT win,los FROM mytable " +
                    "WHERE key1=? and key2=? and key3=? and key4=?;";
            preparedStatement = connSecondSelect.prepareStatement(selectSql);
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
            close();
        }
    }
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

    @Override
    public void close() {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                //EMPTY
            }
            preparedStatement = null;
        }
        if (connSecondSelect != null) {
            try {
                connSecondSelect.close();
            } catch (SQLException e) {
                //EMPTY
            }
            connSecondSelect = null;
        }
    }
}
