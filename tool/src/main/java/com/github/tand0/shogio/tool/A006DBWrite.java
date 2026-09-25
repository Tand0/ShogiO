package com.github.tand0.shogio.tool;

import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.InputGen;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TableKey;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DB情報を InputGen 化してバイナリデータを出力する。
 * このバイナリデータを使って
 */
public class A006DBWrite extends DBBase {
    /** 標的となるDB名 */
    private String target;

    /** 標的となる出力先フォルダ名 */
    private String folderPathString;

    /** レイヤ情報 */
    private final InputGen inputGen = new InputGen();

    /** 入力データの生成用バッファ
     * 最初にループの外側で生成することで処理速度を上げる
     * Python は LITTLE_ENDIAN なのでそれに変更。
     **/
    private final ByteBuffer bufferInp = ByteBuffer.allocate(
            TableDefine.B_MAX * TableDefine.B_MAX * InputGen.MAX_CHANNEL * 4)
            .order(ByteOrder.LITTLE_ENDIAN);

    /** 教師信号の生成用バッファ
     * 最初にループの外側で生成することで処理速度を上げる
     * Python は LITTLE_ENDIAN なのでそれに変更。
     **/
    private final ByteBuffer bufferOut = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN);

    /** 入出力先 */
    private ZipOutputStream zFosInp = null;

    /** 出力出力先 */
    private ZipOutputStream zFosOut = null;

    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException, IOException {
        A006DBWrite db = new A006DBWrite();
        //
        db.setTargetDb(PropertyDefine.APP_DB_ESTIMATE);
        db.run();
        //
        db.setTargetDb(PropertyDefine.APP_DB_TOTAL);
        db.run();
    }

    /** ターゲットDBを設定する
     * @param target ターゲット DB名
     * @throws IOException IOException
     */
    public void setTargetDb(String target) throws IOException{
        this.target = target;
        //
        // 対象のファイルパス
        Path path = Paths.get(MyPropertiesAccess.getString(target)).toAbsolutePath();
        //
        // フォルダ名（親ディレクトリのパス）を取得
        Path parentPath = path.getParent();
        String folderName = (parentPath != null) ? parentPath.toString() : "";
        //
        // ファイル名（拡張子を含む全体）を取得
        Path fileNamePath = path.getFileName();
        String fullFileName = (fileNamePath != null) ? fileNamePath.toString() : "";
        //
        // 拡張子と、拡張子なしのファイル名に分離する
        String baseName = fullFileName;
        int dotIndex = fullFileName.lastIndexOf('.');
        // ドットが存在し、かつドットが先頭（隠しファイル等）や末尾ではない場合
        if (dotIndex > 0 && dotIndex < fullFileName.length() - 1) {
            baseName = fullFileName.substring(0, dotIndex);
        }
        //
        this.folderPathString = folderName + "/bin_" + baseName;
    }

    @Override
    public String[] getTargetDb() {
        return new String[] {this.target};
    }

    @Override
    public String getSaveDb() {
        return this.folderPathString;
    }

    @Override
    public String getSelectSql() {
        return "SELECT key1, key2, key3, key4, win, los FROM mytable ORDER BY RANDOM()";
    }

    @Override
    public String getCountSql() {
        return "SELECT COUNT(*) AS total_count FROM mytable";
    }

    /** レイヤ―に変換して保存する
     *
     * @param stmtTarget ターゲットとなる DB
     * @param countMax カウント最大値
     * @throws IOException IO Exception
     * @throws SQLException SQL Exception
     */
    public void runSearch(Statement stmtTarget, long countMax)
            throws IOException, SQLException {
        try {
            Files.createDirectories(Paths.get(getSaveDb()));
        } catch (IOException e) {
            // EMPTY (既にファイルが作られている場合を許す)
        }
        //
        // 初めから削ったDBで処理しているので where はいらない
        final String selectSql = getSelectSql();
        System.out.println(selectSql);
        long countNow = 0;
        super.setCountMax(countMax); // カウント設定
        try (ResultSet rs = stmtTarget.executeQuery(selectSql)) {
            while (rs.next()) {
                if ((countNow % (1024 * 128)) == 0) {
                    //
                    // 先にクローズする
                    close();
                    final String zipEntryName = "raw_data.bin";
                    //
                    // ファイル名をつくる
                    String saveInpName = folderPathString
                            + "/inp_" +
                            String.format(Locale.JAPANESE,"%07d", countNow) + ".zip";
                    Path zipFileInp = Paths.get(saveInpName);
                    String saveOutName = folderPathString
                            + "/out_" +
                            String.format(Locale.JAPANESE,"%07d", countNow) + ".zip";
                    Path zipFileOut = Paths.get(saveOutName);
                    System.out.println(
                            getTime(System.currentTimeMillis()) + saveInpName);
                    zFosInp = new ZipOutputStream(Files.newOutputStream(zipFileInp));
                    ZipEntry entry = new ZipEntry(zipEntryName);
                    zFosInp.putNextEntry(entry);
                    zFosOut = new ZipOutputStream(Files.newOutputStream(zipFileOut));
                    entry = new ZipEntry(zipEntryName);
                    zFosOut.putNextEntry(entry);
                }
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
                createSaveData(null,  null, tableKey,win, los);
                //
                printTimer(countNow, null, null);
                //
            }
        } finally {
            close();
        }
    }

    /**
     * 終了処理
     */
    public void close() {
        if (zFosInp != null) {
            try {
                zFosInp.closeEntry();
            } catch (IOException e) {
                // EMPTY
            }
            try {
                zFosInp.close();
            } catch (IOException e) {
                // EMPTY
            }
            zFosInp = null;
        }
        if (zFosOut != null) {
            try {
                zFosOut.closeEntry();
            } catch (IOException e) {
                // EMPTY
            }
            try {
                zFosOut.close();
            } catch (IOException e) {
                // EMPTY
            }
            zFosOut = null;
        }
    }

    @Override
    public void createSaveData(
            MainDatabase ignore1,
            PreparedStatement ignore2,
            TableKey tableKey,
            long win, long los)
            throws SQLException {
        try {
            inputGen.updateTable(tableKey.createTable());
            float[] input = inputGen.getInput();
            //
            bufferInp.clear();
            for (float oneData : input) {
                bufferInp.putFloat(oneData);
            }
            zFosInp.write(bufferInp.array());
            //
            //inputGen.print(input); // 表示
            //
            bufferOut.clear();
            float output = EvalTeTable.getWinLosToRate(win, los);
            if (tableKey.getTeban() != 0) {
                output = 1.0f - output; // 反転する
            }
            bufferOut.putFloat(output);
            zFosOut.write(bufferOut.array());
            //
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
