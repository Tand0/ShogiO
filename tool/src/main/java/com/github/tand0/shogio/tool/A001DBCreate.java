package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TableKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/** 棋譜を参照してDBを作る
 */
public class A001DBCreate extends MyTimer {
    /** 投入する SQL winとlosが1以外で同じなら加算する */
    public static final String INSERT_SQL = """
        INSERT INTO mytable (key1, key2, key3, key4, win, los)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(key1, key2, key3, key4)
                DO UPDATE SET
                    win = CASE
                              WHEN excluded.win = 1 THEN mytable.win
                              ELSE mytable.win + excluded.win
                          END,
                    los = CASE
                              WHEN excluded.los = 1 THEN mytable.los
                              ELSE mytable.los + excluded.los
                          END;
        """;

    /** カウントの現在地 */
    private long fileCountSum = 0;

    /** コンストラクタ
     */
    public A001DBCreate() {
    }

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException,IOException {
        A001DBCreate db = new A001DBCreate();
        db.run();
    }

    /** ログフォルダにあるファイルを読み込んでDBに押し込む
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public void run() throws SQLException, IOException {
        System.out.println("run start!");
        //
        String targetDb = MyPropertiesAccess.getString(PropertyDefine.APP_DB_ALL);
        String folder = Paths.get(targetDb).getParent().toString();
        int rate = MyPropertiesAccess.getInt(PropertyDefine.APP_RATE_MIN);
        //
        String targetZipsComma = MyPropertiesAccess.getString(PropertyDefine.APP_LOAD_TARGET);
        List<String> list = Arrays.stream(targetZipsComma.split("\\s*,\\s*"))
                .map(String::trim)
                .filter(targetZip ->{
                    if (checkSkipString(targetZip)) {
                        System.out.println("skipping f=" + targetZip);
                        return false;
                    }
                    return true;
                })
                .toList();

        for (String targetZip : list) {
            String targetZipDB = getTargetZipDB(folder,targetZip);
            if ((new File(targetZipDB)).exists()) {
                System.out.println("skipping f=" + targetZipDB);
                continue;
            }
            //
            String url = "jdbc:sqlite:" + (new File(targetZipDB)).getAbsolutePath();
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                //
                try (Statement stmt = conn.createStatement()) {
                    // テーブルがなければ生成する
                    this.createSQL(stmt);
                }
                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                    runTarget(targetZip, conn, ps, rate);
                }
                conn.commit();
            }
        }
        if ((new File(targetDb)).exists()) {
            System.out.println("skipping f=" + targetDb);
            return;
        }
        //
        // SQLを開いていく
        File dbFile = new File(targetDb);
        System.out.println(dbFile.getAbsoluteFile());
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                for (String targetZip : list) {
                    String targetZipDB = getTargetZipDB(folder,targetZip);
                    System.out.println("attach f=" + targetZipDB);
                    // テーブルがなければ生成する
                    this.createSQL(stmt);
                    // 統合元 DB を attach
                    stmt.execute("ATTACH DATABASE '" + targetZipDB + "' AS a");
                    // mytable をコピー
                    stmt.execute("""
                            INSERT INTO mytable (key1, key2, key3, key4, win, los)
                                SELECT key1, key2, key3, key4, win, los FROM a.mytable
                                WHERE true
                                ON CONFLICT(key1, key2, key3, key4)
                                DO UPDATE SET
                                    win = CASE
                                              WHEN excluded.win = 1 THEN mytable.win
                                              ELSE mytable.win + excluded.win
                                          END,
                                    los = CASE
                                              WHEN excluded.los = 1 THEN mytable.los
                                              ELSE mytable.los + excluded.los
                                          END;
                            """);
                    // detach
                    stmt.execute("DETACH DATABASE a");
                }
            }

        }
        System.out.println("run over!");
    }

    /**
     * zip からファイル名を得る
     * @param folder db フォルダ
     * @param targetZip zip ファイル名
     * @return db ファイル名
     */
    private String getTargetZipDB(String folder, String targetZip) {
        Path path = Paths.get(targetZip);
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        String name = filename.substring(0, dotIndex);
        return folder + "/" + name + ".db";
    }

    /**
     * skip 対象なら trueを返す
     * @param targetDir フォルダ名情報
     * @return true: skip対象、 false: skipしない
     */
    private boolean checkSkipString(String targetDir) {
        String checkString = targetDir.toLowerCase(Locale.JAPANESE);
        //
        return checkString.isEmpty()
                || checkString.equals("null")
                || checkString.equals("false")
                || checkString.equals("none")
                || checkString.equals("nil");
    }

    /**
     * ログフォルダにあるファイルを読み込んでDBに押し込む(フォルダ別)
     * @param targetZipFile 対象フォルダ
     * @param conn データベースのコネクション
     * @param ps データベースのセッション
     * @param rate 0:レートは考慮しない、1以上:レート以下は無視する
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public void runTarget(
            String targetZipFile, Connection conn, PreparedStatement ps, int rate)
            throws SQLException, IOException {
        System.out.println("targetZipFile=" + targetZipFile);
        if (checkSkipString(targetZipFile)) {
            return;
        }
        String extension = TableDefine.getExtension(targetZipFile);
        if (! TableDefine.EXTENSION_ZIP.equals(extension)) {
            throw new IOException("this is not zip file f=" + targetZipFile);
        }
        //
        File file = new File(targetZipFile);
        boolean learn = true; // 学習フラグを立てる
        try (ZipFile zipFile = new ZipFile(file)) {
            // ディレクトリも含めた総エントリ数
            super.setCountMax(zipFile.size()); // カウント設定
            fileCountSum = 0;
            //
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                fileCountSum++; // カウントアップ
                //
                // ディレクトリ（フォルダ）はスキップする
                if (entry.isDirectory()) {
                    continue;
                }
                String fileName = entry.getName();
                try (InputStream is = zipFile.getInputStream(entry)) {
                    //
                    // 学習のメイン部分
                    this.runFile(fileName, is, learn, conn, ps, rate);
                }
            }
        }
    }

    /** add batch のカウンターの最大値 */
    private static final int addBatchCounterMax = 300000;

    /** add batch のカウンター */
    private int addBatchCounter = 0;

    /** 指定されたデータの出力
     *
     * @param fileName フォルダ
     * @param stream ファイルのストリーム
     * @param learn true:学習用にデータを読み込む場合(最後勝者が分からないとデータ全捨てします)、
     *              false:データをとにかく吸出したいとき
     * @param conn コネクション
     * @param ps 学習情報
     * @param rate レート
     * @throws IOException 例外
     * @throws SQLException SQL例外
     */
    public void runFile(
            String fileName, InputStream stream, boolean learn, Connection conn, PreparedStatement ps, int rate)
            throws IOException,SQLException {
        //
        // ロードする
        final List<EvalTeTable> evalList = new ArrayList<>();
        TableDefine.runFile(fileName, stream, evalList, learn, rate);
        if (evalList.isEmpty()) {
            return; // 空なら処理しない
        }
        //
        // 学習する
        for (EvalTeTable eval : evalList) {
            //
            // テーブル更新
            long[] key = (new TableKey(eval.table)).getKey();
            ps.setLong(1, key[0]);
            ps.setLong(2, key[1]);
            ps.setLong(3, key[2]);
            ps.setLong(4, key[3]);
            ps.setLong(5, eval.win);
            ps.setLong(6, eval.los);
            ps.addBatch();
            addBatchCounter++;
            if (addBatchCounterMax < addBatchCounter) {
                commit(conn, ps); // ある程度いったらコミットする
                addBatchCounter = 0;
            }
        }
        //
        // 途中経過をx分おきに表示する
        printTimer(fileCountSum, conn, ps);
    }
}
