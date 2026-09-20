package com.github.tand0.andshogio.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** タイマー機能を備える */
public class MyTimer {

    /** 開始時間 */
    private long startTime = System.currentTimeMillis();

    /** カウント最大値 */
    private long countMax = 0;

    /** カウントした時刻 */
    private long countTime = 0;


    /** カウント最大値と開始時間のセット
     *
     * @param countMax カウント最大値
     */
    public void setCountMax(long countMax) {
        startTime = System.currentTimeMillis();
        this.countMax = countMax;
    }

    /** 実施した保存情報のカウンタ */
    private int printTimerCounter = 0;

    /** 何件存在したら commit するか？(あまり数が多いとJava heap space例外がでる) */
    private static final int printTimerCounterMax = 400000;

    /**
     * commit の実施
     * @param connection コネクション。表示するタイミングで commit してメモリから吐き出す
     * @param ps ステートメント、表示するタイミングで executeBatch してメモリから吐き出す
     * @throws SQLException SQL Exception
     */
    public void commit(Connection connection, PreparedStatement ps)
            throws SQLException {
        if (ps != null) {
            // 一定時間が経過したらバッチ処理
            ps.executeBatch();
        }
        if (connection != null) {
            // 一定時間が経過したら保存
            connection.commit();
        }
    }

    /**
     *  途中経過をx分おきに表示する
     * @param countNow カウント数
     * @param connection コネクション。表示するタイミングで commit してメモリから吐き出す
     * @param ps ステートメント、表示するタイミングで executeBatch してメモリから吐き出す
     * @throws SQLException SQL Exception
     */
    public void printTimer(long countNow, Connection connection, PreparedStatement ps)
            throws SQLException {
        // 途中経過をx分おきに表示する
        long now = System.currentTimeMillis();
        printTimerCounter++;
        if (printTimerCounterMax < printTimerCounter) {
            printTimerCounter = 0; // clear
            commit(connection,ps);

        }
        if (countTime + (1000 * 60) < now) {
            //
            countTime = now;
            countMax = Math.max(1, countMax);
            float pCent = 100f * countNow / countMax;
            long baseTime = countTime - startTime;
            long next = baseTime *  (countMax - countNow) / countNow;
            final String b = getTime(now) +
                    "[" + getTimeDiff(baseTime) + "]" +
                    "(" + String.format(Locale.getDefault(), "%.2f", pCent) + "%)" +
                    "->" +
                    "[" + getTimeDiff(next) + "] " +
                    String.format(Locale.JAPANESE,"%,d", countNow) + "/" +
                    String.format(Locale.JAPANESE,"%,d", countMax);
            System.out.println(b);
        }
    }

    /** 時間を文字列に変換する
     * @param currentTimeMillis 現在の時間
     * @return 現在の時間を文字列化したもの
     */
    public String getTime(long currentTimeMillis) {
        // long値をInstantオブジェクトに変換
        Instant instant = Instant.ofEpochMilli(currentTimeMillis);
        //
        // システムのデフォルトタイムゾーン（日本時間など）でLocalDateTimeに変換
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        //
        // 表示したい形式（フォーマット）を定義
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 文字列に変換して出力
        return "[" + dateTime.format(formatter) + "] ";
        // 出力例: [2026-05-30 17:55:00]
    }

    /**
     * 時間の差分を文字列に変換する
     * @param diff 時間の差分
     * @return 時間の差分を文字列に変換したもの
     */
    public String getTimeDiff(long diff)  {
        // 各単位に変換
        long day = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        //
        if (0 < day) {
            // 桁数を揃えて表示 (例: 2 day 00:06:12.300)
            return String.format(Locale.getDefault(), "%d day %02d:%02d:%02d", day, hours, minutes, seconds);
        }
        // 桁数を揃えて表示 (例: 00:06:12.300)
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }


    /**
     * テーブルを生成する
     * @param stmt ステートメント
     * @throws SQLException SQL Exception
     */
    public void createSQL(Statement stmt) throws SQLException {
        // SQL文に「IF NOT EXISTS」を付与する
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS mytable (
                    key1 INTEGER NOT NULL,
                    key2 INTEGER NOT NULL,
                    key3 INTEGER NOT NULL,
                    key4 INTEGER NOT NULL,
                    win INTEGER NOT NULL,
                    los INTEGER NOT NULL,
                    PRIMARY KEY (key1, key2, key3, key4)
                );""";
        //
        stmt.execute(createTableSql);
    }

    /** ファイルかフォルダが存在したらtrueを返す
     * @param path パス
     * @return true:存在、false:存在しない
     */
    public boolean fileExists(String path) {
        return (new File(path)).exists();
    }
}
