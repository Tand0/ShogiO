package com.github.tand0.andshogio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.tand0.andshogio.util.MainDatabase;
import com.github.tand0.andshogio.engin.SendListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * SQLiteOpenHelper を継承してDBアクセスできるようにする
 */
public class MainDatabaseHelper extends SQLiteOpenHelper implements MainDatabase {

    /** バージョンの定義 */
    private static final int DATABASE_VERSION = 18;

    /** データベース名のasset側のzipのファイル名 **/
    private static final String DATABASE_ZIP_NAME = "mytable_small.zip";

    /** データベース名 **/
    private static final String DATABASE_NAME = "mytable_small.db";

    /** ファイルの解凍を別スレッドで実施するためのクラス */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**  テーブル名とカラム名の定義 */
    public static final String TABLE_NAME = "mytable";
    /** 主キーその1 */
    public static final String KEY1 = "key1";
    /** 主キーその2 */
    public static final String KEY2 = "key2";
    /** 主キーその3 */
    public static final String KEY3 = "key3";
    /** 主キーその4 */
    public static final String KEY4 = "key4";
    /** パラメータ(win値) */
    public static final String WIN = "win";
    /** パラメータ(los値) */
    public static final String LOS = "los";

    /** OK牧場フラグ
     * True: DBはあるので先に進めてよい
     * False: DBはないのでデータ読みに行ったときはエラー
     */
    private boolean okFlag = false;

    /** GUI へメッセージを渡す用 */
    private final SendListener forGui;

    /** コンストラクタ
     * @param context context
     * @param forGui GUIスレッドにメッセージを送るためのリスナ
     */
    public MainDatabaseHelper(Context context, SendListener forGui) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.getWritableDatabaseWithAssets(context);
        this.forGui = forGui;
    }
    private static final String PREF_NAME = "AppConfig";
    private static final String KEY_VERSION = "version";

    /** データベースを利用可能にする（読み書き用に開く）
     * @param context context
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void getWritableDatabaseWithAssets(Context context) {
        // 内部ストレージのデータベースファイルを特定
        final File dbFile = context.getDatabasePath(DATABASE_NAME);
        // バージョンが古いかどうかチェック
        boolean versionFlag = checkVersion(context);
        // アセットマネージャを取得しておく
        AssetManager assetManager = context.getAssets();
        //
        //
        // 重い処理なのでGUIスレッドの外側で実行
        executor.execute(() -> {
            try {
                //
                // 存在しない場合またはバージョンが古いのとき、 assets からコピーを実行
                if ((!dbFile.exists() || versionFlag)) {
                    forGui.send("Copy data base to inner storage");
                    // 親の databases フォルダを強制的に作成
                    File parentDir = dbFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    // ストリームコピーを実行
                    copyDatabaseFromAssets(assetManager, dbFile);
                } else {
                    forGui.send("Loading database now ...");
                }
                //
                // コピー完了後（または既存時）、親クラスのメソッドを呼んで安全にDBを開く
                // これにより、必要に応じて onUpgrade() なども正しく動作します
                // SQLiteDatabase db = super.getWritableDatabase();
                // ↑ 直前まで呼ばなくて良いのでは？
                //
                okFlag = true; // OK 牧場は true に変換して読み込みOKにする
                // OK 牧場はカウントより先にやらないと、カウント側がNGと見なされて失敗する
                //
                // いくつあるかカウントする
                forGui.send("Loading database complete!");
                //
            } catch (IOException|InterruptedException|android.database.sqlite.SQLiteException e) {
                dbFile.delete(); // 初期化失敗＝DB壊れているので削除する
                okFlag = false; // 初期処理中に例外が出たら定跡は使わない
                //
                try {
                    forGui.send("DB NG! // please re install");
                    forGui.send(e);
                } catch (InterruptedException ex) {
                    // EMPTY
                }
            }
        });

    }

    /**
     * InputStream と FileOutputStream によるストリームコピー
     */
    private void copyDatabaseFromAssets(AssetManager assetManager, File destFile) throws IOException {
        Path destDir = Objects.requireNonNull(destFile.getParentFile()).toPath();
        try (InputStream fis = assetManager.open(DATABASE_ZIP_NAME);
             ZipInputStream zis = new ZipInputStream(fis)) {
            //
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = destDir.resolve(entry.getName());
                //
                if (entry.isDirectory()) {
                    // ディレクトリの場合は作成
                    Files.createDirectories(filePath);
                } else {
                    // 親ディレクトリが存在しない場合（念のため）作成
                    Files.createDirectories(filePath.getParent());
                    //
                    // ファイルを解凍して書き出し
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * バージョンのチェック
     * 過去に設定したバージョンと異なる場合、trueを返す
     *
     * @param context context
     * @return if oldVersion < newVersion then true.
     */
    private boolean checkVersion(Context context) {
        // MODE_PRIVATEで他のアプリからアクセスできないようにする
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        //
        // 第2引数はデータが存在しなかった場合のデフォルト値
        int oldVersion = pref.getInt(KEY_VERSION, 0);
        //
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_VERSION, MainDatabaseHelper.DATABASE_VERSION);
        //
        // apply()は非同期で保存するため、UIスレッドを止めず推奨される
        editor.apply();
        //
        return oldVersion < MainDatabaseHelper.DATABASE_VERSION;
    }

    /** データベースが初めて作成される時に実行される
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreateでDBを消すとassetの内容が飛ぶのでやりません
    }

    /**データベースのバージョンが上がった時に実行される
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onCreateでDBを消すとassetの内容が飛ぶのでやりません
    }

    /** データ読み込み (Select) のメソッド
     * @param key key
     * @return win, los
     */
    @Override
    public long[] getData(long[] key) {
        if (!okFlag) {
            return null; // 正常起動に失敗している
        }
        SQLiteDatabase db;
        // 読み込みモードでデータベースを開く
        try {
            db = this.getReadableDatabase();
        } catch (Exception e) {
            okFlag = false;
            return null; // なんらかの読み込み失敗が出た
        }

        // 取得したいカラムの指定
        String[] projection = {WIN, LOS};
        String selection = KEY1 + " = ? AND " + KEY2 + " = ? AND " + KEY3 + " = ? AND " + KEY4 + " = ?";
        String[] selectionArgs = {
            String.valueOf(key[0]),
            String.valueOf(key[1]),
            String.valueOf(key[2]),
            String.valueOf(key[3])
        };

        long win = 0L;
        long los = 0L;
        boolean hit = false;
        // データの検索 (SELECT * FROM users)
        try (Cursor cursor = db.query(
                TABLE_NAME,   // テーブル名
                projection,                  // 取得するカラム
                selection,                   // WHERE句
                selectionArgs,               // WHERE句の引数
                null,                        // GROUP BY
                null,                        // HAVING
                null)) {                     // ORDER BY

            // データの取り出し
            if (cursor.moveToFirst()) {
                win = cursor.getLong(cursor.getColumnIndexOrThrow(WIN));
                los = cursor.getLong(cursor.getColumnIndexOrThrow(LOS));
                hit = true;
            }
        }
        return hit ? new long[]{win, los} : null;
    }
}
