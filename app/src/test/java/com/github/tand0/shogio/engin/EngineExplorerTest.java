package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.ExplorerTable;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;
import com.github.tand0.shogio.util.TensoInterface;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 探索エンジン用のテスト */
public class EngineExplorerTest {

    /** テスト用のエンジン */
    private EngineExplorer engine;

    /** 結果情報 */
    private String resultString;

    /** SendListener の mock */
    public SendListener forResult = x -> {
        if (x instanceof String) {
            this.resultString = (String)x;
            System.out.println(this.resultString);
        } else if (x instanceof GUIMessage) {
            this.resultString = ((GUIMessage)x).message();
            System.out.println(this.resultString);
        }
    };

    /** 起動時に必ず呼ばれる */
    @Before
    public void setUp() {
        TensoInterface tensoInterface = table -> 0.5f;
        Table table = new Table(null, 0);
        int te = TableDefine.changeTeStringToInt("+1716FU");
        TeTable teTable = new TeTable(new Table(table,te),te);
        Set<TeTable> badTableSet = new HashSet<>();
        badTableSet.add(teTable);
        engine = new EngineExplorer(tensoInterface, badTableSet);
    }

    /** id のチェック */
    @Test
    public void testGetEngineId() {
        Assert.assertEquals("engine_explorer", engine.getEngineId());
    }

    /** メソッド設定テスト */
    @Test
    public void testSetterMethods() {
        engine.maxTableFactory(500);
        engine.maxNoChange(3);
        engine.maxLevel(4);
        engine.minSon(2);

        // リフレクションで private 値を確認する例
        Assert.assertEquals(500, getPrivateInt(engine, "maxTableFactory"));
        Assert.assertEquals(3, getPrivateInt(engine, "maxNoChange"));
        Assert.assertEquals(4, getPrivateInt(engine, "maxLevel"));
        Assert.assertEquals(2, getPrivateInt(engine, "minSon"));
    }

    /** r == null → 何も起きない（例外が出ないことを確認） */
    @Test
    public void testCreateNowNullRequest() {
        engine.createNow(null);
    }

    /** 普通に計算させる */
    @Test
    public void testCreateNow() {
        engine.maxTableFactory(1);
        engine.maxNoChange(1);
        engine.maxLevel(3);
        engine.minSon(1);
        //

        //
        // 空なら終了
        engine.createNow(null);
        //
        // リクエストの設定
        int turn = -1;
        List<TeTable> teTableList = new ArrayList<>();
        boolean display = false;
        EngineRequest r = new EngineRequest(forResult, turn, teTableList, display);
        engine.createNow(r);
        //
        // リクエストの設定
        engine.maxTableFactory(1);
        engine.setNow(null);
        TeTable teTable = new TeTable(new Table(null, 0), TableDefine.LOS);
        teTableList.add(teTable);
        engine.createNow(r);
        EngineResponse res = engine.getNow();
        Assert.assertNull(res);
        Assert.assertTrue(this.resultString.contains(EngineExplorer.DISPLAY_MAX_TABLE_FACTORY));
        //
        // 正常処理
        engine.maxTableFactory(1000);
        engine.setNow(null);
        engine.createNow(r);
        res = engine.getNow();
        Assert.assertNotNull(res);
        Assert.assertNotEquals(TableDefine.LOS, res.te());
        Assert.assertTrue(this.resultString.contains(EngineExplorer.DISPLAY_NO_CHANGE));
        //
        // 先手詰み
        Table table = new Table("sfen lnsgk2nl/1r4g1b/2ppppppp/p1p3p2/9/9/9/9/9 b 2P 1"); // 詰み
        teTable = new TeTable(table, TableDefine.LOS);
        teTableList.clear();
        teTableList.add(teTable);
        engine.setNow(null);
        engine.createNow(r);
        res = engine.getNow();
        Assert.assertNull(res);
        Assert.assertEquals(EngineExplorer.DISPLAY_MATE_MIN, this.resultString);
        //
        // 後手詰み
        table = new Table("sfen 8k/8G/8G/9/9/9/9/9/K8 b - 1"); // 詰み
        teTable = new TeTable(table, TableDefine.LOS);
        teTableList.clear();
        teTableList.add(teTable);
        engine.setNow(null);
        engine.createNow(r);
        res = engine.getNow();
        Assert.assertNotNull(res);
        Assert.assertEquals(EngineExplorer.DISPLAY_MATE_MAX, this.resultString);
    }
    /** 普通に計算させる */
    @Test
    public void testCalculateNodeExpandBasic() {
        engine.maxTableFactory(500);
        engine.maxNoChange(3);
        engine.maxLevel(1);
        int minSon = 2;
        engine.minSon(minSon);
        //
        // ExplorerTable をモック化
        Table table = new Table(null, 0);

        ExplorerTable explorer = new ExplorerTable(new TeTable(table, TableDefine.LOS));

        engine.changeFlag(false);
        engine.calculateNodeExpand(0, explorer, new java.util.HashMap<>());

        Assert.assertEquals(0.5, explorer.getEval(), 0.0001);
        //
        // 初期局面は30ある
        Assert.assertEquals(30, explorer.getList().size());
        int sum = 0;
        for (ExplorerTable et : explorer.getList()) {
            if (et.getList() == null) {
                sum++;
            }
        }
        Assert.assertEquals(minSon, sum);
        Assert.assertTrue(engine.changeFlag());
        //
        engine.changeFlag(false);
        engine.changeFlag(false);
        engine.calculateNodeExpand(1, explorer, new java.util.HashMap<>());
        Assert.assertEquals(minSon + 1, explorer.getList().size());
    }

    /** calculateNodeNotExpand のテスト
     */
    @Test
    public void testCalculateNodeNotExpand() {
        engine.maxTableFactory(500);
        engine.maxNoChange(3);
        engine.maxLevel(1);
        engine.minSon(2);
        //
        // ExplorerTable をモック化
        Table table = new Table(null, 0);

        ExplorerTable explorer1 = new ExplorerTable(new TeTable(table, TableDefine.LOS));
        ExplorerTable explorer2 = new ExplorerTable(new TeTable(table, TableDefine.LOS));
        List<ExplorerTable> explorer2Array = new ArrayList<>();
        explorer2Array.add(explorer2);
        explorer1.setList(explorer2Array);
        explorer2.setList(Collections.emptyList());
        Assert.assertEquals(1, explorer1.getList().size());
        //
        int level = 0;
        engine.calculateNodeNotExpand(
                level, explorer1, new HashMap<>(), new HashSet<>());
        Assert.assertEquals(1, explorer1.getList().size());

        //
        level = 1;
        engine.calculateNodeNotExpand(
                level, explorer1, new HashMap<>(), new HashSet<>());
        Assert.assertEquals(0, explorer1.getList().size());
    }

    /** --- private フィールド取得用ヘルパー ---
     * @param obj オブジェクト
     * @param fieldName ファイル名
     * @return 結果
     */
    private int getPrivateInt(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.getInt(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
