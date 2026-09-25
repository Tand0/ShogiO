package com.github.tand0.shogio.engin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.PnDnTable;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 詰みエンジンが正常に動くかテスト */
public class EngineMateTest {
    /** 詰みエンジン */
    private static EngineMate mate;

    /**
     * クラスのテストが実行される前に１度だけ実行されるもの。
     * ここではエンジンをインスタンス化する
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        mate = new EngineMate();
    }

    /** セットアップができているかテストする */
    @Test
    public void setupTest() {
        // mateがインスタンス化されているか？
        assertNotNull(mate);
        //
        // createNow に null を渡しても問題なく動くか？
        mate.createNow(null);
        Assert.assertNotNull(mate.getNow());
        Assert.assertEquals(TableDefine.LOS, mate.getNow().te());
        //
    }
    /**
     * 初期画面から不詰めが得られること
     */
    @Test
    public void firstTest() {
        Table table = new Table(null, 0);
        //
        List<TeTable> teTableList = new ArrayList<>();
        teTableList.add(new TeTable(table, 0));
        EngineRequest req = new EngineRequest(null, -1, teTableList, false);
        mate.fail(req);
        mate.createNow(req);
        // 不詰めが取れる
        EngineResponse res = mate.getNow();
        Assert.assertNotNull(res);
        Assert.assertEquals(TableDefine.LOS, res.te());
        //
        String teString = "+4736FU"; // 一手進める
        int te = TableDefine.changeTeStringToInt(teString);
        teTableList.clear();
        teTableList.add(new TeTable(table,te));
        req = new EngineRequest(null, -1, teTableList, false);
        mate.createNow(req);
        // 不詰めが取れる
        res = mate.getNow();
        Assert.assertNotNull(res);
        Assert.assertEquals(TableDefine.LOS, res.te());
    }

    /**
     * 実践データを使って詰みを検出できるかの評価
     * @throws IOException 例外
     */
    @Test
    public void mateShogiTest() throws IOException {
        //
        mateEngine("27点法先手勝ち.csa", false, false); // 先手負け宣言
        mateEngine("27点法後手勝ち.csa", true, true);
        //
        mateEngine("0手詰め後手-.csa", false, true);
        mateEngine("0手詰め後手+.csa", false, false);
        mateEngine("0手詰め先手-.csa", false, false);
        mateEngine("0手詰め先手+.csa", false, true);
        //
        mateEngine("test_01_tume.csa", false, true);
        mateEngine("test_02_tume.csa", false, true);
        mateEngine("test_03_tume.csa", false, true);
        mateEngine("test_04_tume.csa", false, true);
        mateEngine("test_05_tume.csa", false, true);
        mateEngine("test_06_tume.csa", false, true);
        mateEngine("test_07_tume.csa", false, true);
        //
    }

    /**
	 * mate エンジンが正しく動いているか確認する
	 * @param fileNameString 詰めろファイル
     * @param teWin true: 入玉勝ちのとき
     * @param tumi true: 詰み、 false: 不詰み(自分が詰んでいるなど)
	 * @throws IOException 例外パターン
	 */
    protected void mateEngine(String fileNameString,boolean teWin, boolean tumi) throws IOException {
        //
        // ファイル名確定
        File file = new File(".").getAbsoluteFile();
        file = new File(file, "src/test/assets/mate_engine");
        file = new File(file, fileNameString);
        List<EvalTeTable> eval = new ArrayList<>();
        //
        // データ構築
        try (FileInputStream fr = new FileInputStream(file)) {
            TableDefine.runFile(file.getAbsolutePath(), fr, eval, false, 0);
        }
        assertNotNull(eval);
        assertFalse(eval.isEmpty());
        Table table = eval.getLast().table;
        //
        // 学習
        List<TeTable> teTableList = new ArrayList<>();
        teTableList.add(new TeTable(table, 0));
        EngineRequest req = new EngineRequest(null, -1, teTableList, true);
        mate.fail(req); // まず失敗状態にする
        mate.createNow(req); // 詰めがあるかチェックする
        //
        // 詰めが取れる
        Assert.assertNotNull(mate.getNow());
        Assert.assertNotNull(mate.getNow().displayList());
        if (tumi) {
            // 詰んでいる
            Assert.assertFalse(mate.getNow().displayList().isEmpty());
            Assert.assertTrue(mate.getNow().displayList().getFirst() instanceof PnDnTable);
            PnDnTable pnDnTable = (PnDnTable) mate.getNow().displayList().getFirst();
            Assert.assertNotNull(pnDnTable);
            if (teWin) {
                // mateで確認する
                Assert.assertEquals(TableDefine.WIN, mate.getNow().te());
            } else {
                // LOSでない＝詰み手を得られている
                Assert.assertNotEquals(TableDefine.LOS, mate.getNow().te());
            }
        } else {
            // 詰んでいない
            if (! mate.getNow().displayList().isEmpty()) {
                // 一個も取れないのは正常なので条件で弾く
                // 一個でも取れた場合は負け宣言か確認する
                int te = mate.getNow().te();
                System.out.println("te=" + TableDefine.changeTeIntToKangiString(te));
                Assert.assertEquals(TableDefine.LOS, te);
            }
        }
    }

}
