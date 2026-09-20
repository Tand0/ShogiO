package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** レイヤ用のテスト */
public class InputGenTest {
    /** テーブルを展開後、もとに戻せるか確認する */
    @Test
    public void basicTest() {
        Table table = new Table(null, 0);
        myCheck(table);
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+7776FU"));
        myCheck(table);
        //
        table = new Table(table,TableDefine.changeTeStringToInt("-3334FU"));
        myCheck(table);
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+8822KA"));
        myCheck(table);
    }

    /** テーブルを展開後、もとに戻せるか確認する
     * @param table 確認するテーブル
     */
    public void myCheck(Table table) {
        TableKey tableKey = new TableKey(table);
        long[] key = tableKey.getKey();
        //
        InputGen inputGen = new InputGen();
        inputGen.updateTable((new TableKey(key)).createTable());
        Table reverse = inputGen.get3DToTable();
        //
        // もしも後手番の時は反転させる
        reverse = (tableKey.getTeban() == 0)? reverse : reverse.flippingVertical();
        //
        if (! table.equals(reverse)) {
            // 一致しないときは左右反転させる
            reverse = reverse.flippingHorizontal();
            // それでも一致しないならおかしい
            Assert.assertEquals(table,reverse);
        }
        //
    }
    /** 手コマがきちんと置かれているかを確認する
     */
    @Test
    public void tegomaTest() {
        // コマをすべて片側の持ちコマにする
        Table table = new Table(null, 0);
        table.clearForCSAProtocol();
        table.endForCSAProtocol();
        myCheck(table);
        //
        // 持ちコマを反転させる
        table = new Table(null, 0);
        table.setTeban(1 - table.getTeban());
        table.endForCSAProtocol();
        myCheck(table);
    }

    /** ただで取られるコマがあるならtrueを返すのテスト
     *
     */
    @Test
    public void checkBadMoveTest() {
        Table table = new Table(null, 0);
        Assert.assertFalse(TableDefine.checkFreeKoma(table));
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+7776FU"));
        Assert.assertFalse(TableDefine.checkFreeKoma(table));
        //
        table = new Table(table,TableDefine.changeTeStringToInt("-3334FU"));
        Assert.assertFalse(TableDefine.checkFreeKoma(table));
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+8822KA"));
        Assert.assertTrue(TableDefine.checkFreeKoma(table));
    }

    /** 最終盤面のテスト */
    @Test
    public void testLast() {
        Table table = new Table(null, 0);
        InputGen inputGen = new InputGen();
        inputGen.updateTable(table); // 反映
        for (int x = 0 ; x < TableDefine.B_MAX; x++) {
            for (int y = 0 ; y < TableDefine.B_MAX; y++) {
                Assert.assertEquals(
                        1,
                        inputGen.get(x, y, InputGen.MAX_CHANNEL - 1),
                        0.01);
            }
        }
    }

    /** レイヤが生成するテーブルキーがあっているか？ */
    @Test
    public void testCreateInputGenKey() {
        InputGen inputGen = new InputGen();
        //
        Table table1 = new Table(null, 0);
        Table table2 = new Table(table1,TableDefine.changeTeStringToInt("+7776FU"));
        //
        Assert.assertNotEquals(table1, table2);
        //
        TableKey key1 = new TableKey(table1);
        TableKey key2 = new TableKey(table2);
        //
        TableKey key11 = inputGen.createInputGenKey(key1);
        checkTestCreateInputGenKey(key1 , key11);
        checkTestCreateInputGenKey(key11, key1 );
        //
        Table table21 = inputGen.createInputGenKey(key2)
                .createTable().flippingVertical();
        TableKey key21 =
                new TableKey(table21);
        checkTestCreateInputGenKey(key2, key21);
        checkTestCreateInputGenKey(key21, key2);
        //

    }

    /**
     * Key が左右反転するか確認する
     * @param key1 元となるキー
     * @param key11 左右反転したかもしれないキー
     */
    public void checkTestCreateInputGenKey(TableKey key1, TableKey key11) {
        if (! key1.equals(key11)) {
            // 左右反転
            TableKey key11New = new TableKey(key11.createTable().flippingHorizontal());
            // キーは一致するはず
            Assert.assertEquals(key1, key11New);
        }
    }

    /**
     * createInputGenKey のテスト (NGだったやつ)
     */
    @Test
    public void testCreateInputGenKeyString() {
        String keyString;
        //
        keyString = "c92f6800452800000004018f869dc005c0d2120072910004c3400092f7777bff";
        checkCreateInputGenKeyString(keyString);
        //
        keyString = "c9c3f80468cb60001000019fbc7cc700bd0524805e2ad3eb88f23b819a042cbf";
        checkCreateInputGenKeyString(keyString);
    }

    /** 持ち駒の正常反転テスト
     * @param keyString テーブルのキー
     */
    public void checkCreateInputGenKeyString(String keyString) {
        InputGen inputGen = new InputGen();
        TableKey tableKey = new TableKey(keyString);
        //
        // 上下反転
        Table table = tableKey.createTable();
        //
        if (table.getTeban() != 0) {
            Table reverseTable = table.flippingVertical();
            TableKey reverseTableKey = new TableKey(reverseTable);
            //
            TableKey inputKey = inputGen.createInputGenKey(reverseTableKey);
            Assert.assertNotNull(inputKey);
        }
        //
        TableKey inputKey = inputGen.createInputGenKey(tableKey);
        Assert.assertNotNull(inputKey);
    }
}
