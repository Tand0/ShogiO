package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** Table Define のテスト */
public class TableDefineTest {

    /** 定義が存在するか？ */
    @Test
    public void testDefine() {
        String[] keyword = {
                TableDefine.STRING_LOS,
                TableDefine.STRING_WIN,
                TableDefine.STRING_DRAW,
                TableDefine.EXTENSION_ZIP,
                TableDefine.EXTENSION_CSA,
                TableDefine.EXTENSION_DB
        };
        for (String key : keyword) {
            Assert.assertNotNull(key);
        }
    }

    /** getRate の test */
    @Test
    public void testGetRate() {
        // 'black_rate:Kristallweizen-Core2Duo-P7450+81dc9bdb52d04dc20036dbd8313ed055:3780.0
        // 'white_rate:gikou2_1c+4849c06b4665cfdb26a86eaa9439dd53:3300.0
        float x;
        x = TableDefine.getRate(
                "'black_rate:Kristallweizen-Core2Duo-P7450+81dc6dbd8313ed055:3780.0");
        Assert.assertEquals(3780.0, x , 0.001f);
        x = TableDefine.getRate(
                "'white_rate:gikou2_1c+4849c06b4665cfdb26a86eaa9439dd53:3300.0");
        Assert.assertEquals(3300.0, x , 0.001f);
    }

    /** 子が空 → 詰み判定（先手） */
    @Test
    public void testSenteTsumi() {
        Table table = new Table(null, 0) {
            @SuppressWarnings("unused")
            public List<TeTable> createChild() {
                super.createChild();
                return new ArrayList<>();
            }
        }; // 子なし → 詰み
        EvalMoveResult result = TableDefine.getEvalMoveList(null, table, 0, 0);

        Assert.assertEquals(0, result.win());
        Assert.assertEquals(1, result.los());
    }

    /** 子が空 → 詰み判定（後手） */
    @Test
    public void testGoteTsumi() {
        Table table = new Table(null, 0) {
            @SuppressWarnings("unused")
            public List<TeTable> createChild() {
                return new ArrayList<>();
            }
        }; // 子なし → 詰み
        table.setTeban(1);
        EvalMoveResult result = TableDefine.getEvalMoveList(null, table, 0, 0);

        Assert.assertEquals(1, result.win());
        Assert.assertEquals(0, result.los());
    }

    /** heckWinTeTableList が true → 勝ち判定（先手） */
    @Test
    public void testCheckWinSente() {
        Table table = new Table(null, 0) {
            @SuppressWarnings("unused")
            public List<TeTable> createChild() {
                List<TeTable> list = new ArrayList<>();
                list.add(new TeTable(new Table(null,0), TableDefine.WIN));
                return list;
            }
        };
        EvalMoveResult result = TableDefine.getEvalMoveList(null, table, 0, 0);
        Assert.assertEquals(1, result.win());
        Assert.assertEquals(0, result.los());
    }

    /** データが全く無い → win/los をそのまま返す */
    @Test
    public void testNoData() {
        Table table = new Table(null, 0);

        MainDatabase base = (key -> null);
        EvalMoveResult result = TableDefine.getEvalMoveList(base, table, 7, 3);

        Assert.assertEquals(7, result.win());
        Assert.assertEquals(3, result.los());
    }

    /** 最良の EvalTeTable を選択する */
    @Test
    public void testBestEvalSelection() {
        // 子を2つ用意
        TeTable child1 = new TeTable(new Table(null, 0), 5);
        TeTable child2 = new TeTable(
                new Table(child1.table,
                        TableDefine.changeTeStringToInt("+2726FU")), 5);
        TableKey key2 = new TableKey(child2.table);

        Table table = new Table(null, 0);

        MainDatabase base = (key -> {
            long[] result = null;
            if ((new TableKey(key)).equals(key2)) {
                result = new long[2];
                result[0] = 9;
                result[1] = 1;
            }
            return result;
        });
        EvalMoveResult result = TableDefine.getEvalMoveList(base, table, 0, 0);
        //
        Assert.assertEquals(9, result.win());
        Assert.assertEquals(1, result.los());
    }

    /** 子が空 → null（詰み） */
    @Test
    public void testChildEmpty() {
        Table table = new Table(null, 0) {
            @SuppressWarnings("unused")
            public List<TeTable> createChild() {
                return new ArrayList<>();
            }
        };
        Assert.assertNull(TableDefine.getBadMoveList(null, table));
    }

    /** checkWinTeTableList が true → null（詰ませ） */
    @Test
    public void testCheckWinTrue() {
        Table table = new Table(null, 0)  {
            @SuppressWarnings("unused")
            public List<TeTable> createChild() {
                List<TeTable> list = new ArrayList<>();
                list.add(new TeTable(new Table(null,0), TableDefine.WIN));
                return list;
            }
        };
        Assert.assertNull(TableDefine.getBadMoveList(null, table));
    }

    /** データベースに評価値が一つもない → badTeTableList は空で学習しない" */
    @Test
    public void testNoDatabaseData() {
        Table table = new Table(null, 0);
        MainDatabase base = (key -> null);
        BadMoveResult result = TableDefine.getBadMoveList(base, table);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.teTableList().isEmpty());
    }

    /** データベースに評価値がある → 先手は最低 rate を選ぶ */
    @Test
    public void testSenteSelectMinRate() {
        Table table = new Table(null, 0);
        Table table1 = new Table(table,
                TableDefine.changeTeStringToInt("+2726FU"));
        TableKey next1 = new TableKey(table1);
        Table table2 = new Table(table,
                TableDefine.changeTeStringToInt("+1716FU"));
        TableKey next2 = new TableKey(table2);
        MainDatabase base = (key -> {
            long[] result = null;
            TableKey tableKey = new TableKey(key);
            if (tableKey.equals(next1)) {
                result = new long[2];
                result[0] = 8;
                result[1] = 2;
            } else if (tableKey.equals(next2)) {
                result = new long[2];
                result[0] = 2;
                result[1] = 8;
            }
            return result;
        });
        BadMoveResult result = TableDefine.getBadMoveList(base, table);
        //
        Assert.assertNotNull(result);
        Assert.assertEquals(
                0.05,
                (double) result.win() / (result.win() + result.los()),
                0.01);
    }

    /** 後手は最高 rate を選ぶ */
    @Test
    public void testGoteSelectMaxRate() {
        Table table = new Table(null, 0);
        Table table1 = new Table(table,
                TableDefine.changeTeStringToInt("+2726FU"))
                .flippingVertical();
        TableKey next1 = new TableKey(table1);
        Table table2 = new Table(table,
                TableDefine.changeTeStringToInt("+1716FU"))
                .flippingVertical();
        TableKey next2 = new TableKey(table2);
        //
        table = table.flippingVertical();
        //
        MainDatabase base = (key -> {
            long[] result = null;
            TableKey tableKey = new TableKey(key);
            if (tableKey.equals(next1)) {
                result = new long[2];
                result[0] = 8;
                result[1] = 2;
            } else if (tableKey.equals(next2)) {
                result = new long[2];
                result[0] = 2;
                result[1] = 8;
            }
            return result;
        });
        BadMoveResult result = TableDefine.getBadMoveList(base, table);
        //
        Assert.assertNotNull(result);
        Assert.assertEquals(
                0.95,
                (double) result.win() / (result.win() + result.los()),
                0.01);
    }

    /** 入力を -3333FU,T12 としたとき、12秒になる */
    @Test
    public void testChangeTeStringToTime() {
        Assert.assertEquals(
                12, TableDefine.changeTeStringToTime("-3333FU,T12"));
    }

    /** 手の変更が正しく行えたか？ */
    @Test
    public void testChangeTe() {
        int te = TableDefine.changeTeToInt(TableDefine.pP, 1, 2, 3, 4);
        Assert.assertEquals(TableDefine.pP,TableDefine.changeTeToKoma(te));
        Assert.assertEquals(1,TableDefine.changeTeToOldX(te));
        Assert.assertEquals(2,TableDefine.changeTeToOldY(te));
        Assert.assertEquals(3,TableDefine.changeTeToNewX(te));
        Assert.assertEquals(4,TableDefine.changeTeToNewY(te));

    }
}
