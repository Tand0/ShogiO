package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * EngineExplorerMinMax の JUnit4 テスト
 */
public class EngineExplorerMinMaxTest {

    /** SendListener の mock */
    public SendListener forResult = x -> {
        if (x instanceof String resultString) {
            System.out.println(resultString);
        } else if (x instanceof GUIMessage(String message)) {
            System.out.println(message);
        }
    };

    private final TensoInterface mockTenso = new MockTensoInterface();
    private EngineExplorerMinMax engine;

    /** モックの TensoInterface（評価値を固定で返す） */
    private static class MockTensoInterface implements TensoInterface {
        @Override
        public Float runCompiledModel(Table table) {
            return 0.5f; // 固定評価
        }
    }

    @Before
    public void setUp() {
        // ダミーの悪手セット
        Set<TeTable> badSet = new HashSet<>();
        Table table = new Table(null, 0);
        int te =  Table.changeTeStringToInt("+9796FU");
        table = new Table(table, te);
        badSet.add(new TeTable(table, TableDefine.LOS));

        engine = new EngineExplorerMinMax(mockTenso, badSet);
        engine.maxTableFactory(1000000);
        engine.maxLevel(3);
        engine.maxSon(5);
        engine.maxNoChange(5);
    }

    /** 設定値の確認 */
    @Test
    public void testSettingValue() {
        final long maxTableFactory = 1000001;
        final int maxLevel = 2;
        final int maxSon = 4;
        final int maxNoChange = 3;
        //
        engine.maxTableFactory(maxTableFactory);
        engine.maxLevel(maxLevel);
        engine.maxSon(maxSon);
        engine.maxNoChange(maxNoChange);
        //
        Assert.assertEquals(maxTableFactory, engine.maxTableFactory());
        Assert.assertEquals(maxLevel, engine.maxLevel());
        Assert.assertEquals(maxSon, engine.maxSon());
        Assert.assertEquals(maxNoChange, engine.maxNoChange());
        //
        // 0以下は指定できない
        engine.maxLevel(0);
        Assert.assertEquals(1, engine.maxLevel());
        //
        // 0以下は指定できない
        engine.maxSon(0);
        Assert.assertEquals(1, engine.maxSon());
    }

    /** createNow() が例外なく動くか */
    @Test
    public void testCreateNow() {
        Table table = new Table(null, 0);
        TeTable teTable = new TeTable(table, TableDefine.LOS);
        int turn = -1;
        List<TeTable> teTableList = new ArrayList<>();
        teTableList.add(teTable);
        boolean display = false;
        EngineRequest req = new EngineRequest(forResult, turn, teTableList, display, 0);

        engine.createNow(req);

        EngineResponse res = engine.getNow();
        assertNotNull(res);
    }

    /** alphaBeta が値を返すか */
    @Test
    public void testAlphaBeta() {
        Table table = new Table(null, 0);
        EvalTeTableMinMax evalTable = new EvalTeTableMinMax(table, 0, 0.3f, 20);

        float alpha = EngineExplorerMinMax.MIN_VALUE;
        float beta = EngineExplorerMinMax.MAX_VALUE;
        float result = engine.alphaBeta(
                evalTable, 5, 3, alpha, beta, true,null);

        assertTrue(result >= EngineExplorerMinMax.MIN_VALUE);
        assertTrue(result <= EngineExplorerMinMax.MAX_VALUE);
    }

    /** findTable がキャッシュされるか */
    @Test
    public void testFindTable() {
        Table table = new Table(null, 0);
        //
        EvalTeTable e1 = engine.findTable(table, 0, 0);
        EvalTeTable e2 = engine.findTable(table, 1, 0);
        //
        assertSame("同じ局面はキャッシュされるべき", e1, e2);
    }
}