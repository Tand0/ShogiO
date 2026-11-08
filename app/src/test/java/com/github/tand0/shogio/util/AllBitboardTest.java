package com.github.tand0.shogio.util;


import org.junit.Assert;
import org.junit.Test;

/** 全てのビットテーブルのテスト */
public class AllBitboardTest {
    /** コンストラクタのテスト */
    @Test
    public void testConstructor() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        Assert.assertNotNull(a);
    }
    /** クリアのテスト */
    @Test
    public void testClear() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        Assert.assertNotNull(a);
        //
        a.clear(table); // エラーなく呼び出せるか？
    }
    /** GetBitboard のテスト */
    @Test
    public void testGetBitboard() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        Assert.assertNotNull(a);
        //
        long[] result;
        long[] expected = new long[2];
        //
        result = a.getBitboard(0);
        expected[0] = 0b0111111111000000000000000000000000000000000000000000000000000000L;
        expected[1] = 0b11111111101000001L;
        Assert.assertArrayEquals(expected, result);
        //
        result = a.getBitboard(1);
        expected[0] = 0b111111111010000010111111111L;
        expected[1] = 0L;
        Assert.assertArrayEquals(expected, result);
    }

    /** get bitのテスト */
    @Test
    public void testGetBit() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        long[] spaceX = a.getSpaceBitboard();
        long data;
        data = AllBitboard.getBit(spaceX,82);
        Assert.assertEquals(0, data);
        data = AllBitboard.getBit(spaceX,-1);
        Assert.assertEquals(0, data);
        data = AllBitboard.getBit(spaceX,130);
        Assert.assertEquals(0, data);
    }

    /** getEffect のテスト */
    @Test
    public void testGetEffect() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        Assert.assertNotNull(a);
        //
        long[] result;
        long[] expected = new long[2];
        //
        result = a.getEffect(0);
        expected[0] = 0b1101000111111111111000000000000000000000000000000000000000000000L;
        expected[1] = 0b10111111011111111L;
        Assert.assertArrayEquals(expected, result);
        //
        result = a.getEffect(1);
        expected[0] = 0b111111111111000101111111111011111101L;
        expected[1] = 0L;
        Assert.assertArrayEquals(expected, result);
    }
    /**
     * 王手の効きを乗せるテスト
     */
    @Test
    public void testGetEffectOute() {
        Table table = new Table(null, 0);
        table.moveKoma(TableDefine.pB, 7,7, 2, 2); // 無理やり王手
        table.setSide(0);  // 先手番に戻す
        //
        AllBitboard a = new AllBitboard(table);
        Assert.assertNotNull(a);
        //
        long[] result;
        long[] expected;
        //
        a.clear(table);
        result = a.getEffectOute(0);
        expected = createTargetBitboard(
                0b000000000,
                0b000001000,
                0b000000100,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000);
        Assert.assertArrayEquals(expected, result);
        //
        a.clear(table);
        result = a.getEffectOute(1);
        expected = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000);
        Assert.assertArrayEquals(expected, result);
        //
        Assert.assertEquals(1, a.getEffectNum(0));
        Assert.assertEquals(0, a.getEffectNum(1));
        //
        table.moveKoma(TableDefine.pR, 1,7, 4, 2); // 無理やり王手
        table.setSide(0);  // 先手番に戻す
        a.clear(table);  // 動いたので一旦消す
        Assert.assertEquals(2, a.getEffectNum(0));
        Assert.assertEquals(0, a.getEffectNum(1));
    }

    /**
     * ビットのテスト
     */
    @Test
    public void testBit() {
        long[] target = new long[2];
        int pos;
        //
        pos = 0;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setBit(target,pos);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 1;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setBit(target,pos);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 63;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setBit(target,pos);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 64;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setBit(target,pos);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 80;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setBit(target,pos);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        target[0] = 0L;
        target[1] = 0L;
        //
        pos = 0;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,1);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 1;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,1);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 63;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,1);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 64;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,1);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        pos = 80;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,1);
        Assert.assertEquals(1, AllBitboard.getBit(target,pos));
        //
        target[0] = 0L;
        target[1] = 0L;
        //
        pos = 0;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,0);
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        //
        pos = 1;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,0);
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        //
        pos = 63;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,0);
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        //
        pos = 64;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,0);
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        //
        pos = 80;
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        AllBitboard.setOrBit(target, pos,0);
        Assert.assertEquals(0, AllBitboard.getBit(target,pos));
        //
    }

    /**
     * shiftBit()のテスト
     */
    @Test
    public void testShiftBit() {
        long[] target = new long[2];
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        long low = 0b1111111111111111111111111111111111111111111111111111111111111111L;
        long hi = 0b11111111111111111L;
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -123);
        Assert.assertEquals(0, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -82);
        Assert.assertEquals(0, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -81);
        Assert.assertEquals(0, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, +81);
        Assert.assertEquals(0, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -80);
        Assert.assertEquals(1L, target[0]);
        Assert.assertEquals(0L, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, +80);
        Assert.assertEquals(0L, target[0]);
        Assert.assertEquals(0b10000000000000000L, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, 0);
        Assert.assertEquals(low, target[0]);
        Assert.assertEquals(hi, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, 64);
        Assert.assertEquals(0, target[0]);
        Assert.assertEquals(hi, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, 63);
        Assert.assertEquals(low << 63, target[0]);
        Assert.assertEquals(((hi << 1) & 0x1FFFFL) + 1L, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -64);
        Assert.assertEquals(hi, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = low;
        target[1] = hi;
        AllBitboard.shiftBit(target, -63);
        Assert.assertEquals(0x3FFFFL, target[0]);
        Assert.assertEquals(0, target[1]);
        //
        target[0] = a.getBitboard(0)[0] | a.getBitboard(1)[0];
        target[0] = ~target[0];
        target[1] = ~(a.getBitboard(0)[1] | a.getBitboard(1)[1]);
        target[1] = target[1] & 0x1FFFFL;  // 空白マスを作る
        //
        Assert.assertEquals(
                0b1000000000111111111111111111111111111000000000101111101000000000L,
                target[0]);
        Assert.assertEquals(
                0b10111110L,
                target[1]);
        AllBitboard.shiftBit(target, -9);
        Assert.assertEquals(
                0b0101111101000000000111111111111111111111111111000000000101111101L,
                target[0]);
        Assert.assertEquals(
                0L,
                target[1]);
    }

    /**
     * koma 用のbitboardを取得する
     */
    @Test
    public void testGetKomaBitboard() {
        Table table = new Table(null, 0);
        table.moveKoma(TableDefine.pB,7,7, 8,7);
        table.setSide(0);
        table.moveKoma(TableDefine.pP,7,6, 7,5);
        table.setSide(0);
        AllBitboard a = new AllBitboard(table);
        //
        final long[] space = a.getSpaceBitboard();
        long[] result = a.createKomaBitboard(space, 0, TableDefine.pP, 0, 6);
        long[] target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000001,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboard(space, 0, TableDefine.pR, 1, 7);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b011111101, // 角を一つ左にずらしているのでこれであっている
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboard(space, 0, TableDefine.pB, 8, 7); // 角は上でズラした
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000001000,
                0b000010000,
                0b000100000,
                0b001000000,
                0b010000000,
                0b000000000, // 角を一つ左にずらしているのでこれであっている
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboard(
                space, 0, Table.addNariKoma(TableDefine.pB), 8, 7); // 角は上でズラした
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000001000,
                0b000010000,
                0b000100000,
                0b001000000,
                0b010000000,
                0b010000000, // 角を一つ左にずらしているのでこれであっている
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboard(space, 1, TableDefine.pL, 0, 0);
        target = createTargetBitboard(
                0b000000000,
                0b000000001,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboard(space, 0, TableDefine.pK, 4, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000111000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
    }
    /**
     * koma 用のbitboardを取得する
     */
    @Test
    public void testGetKomaBitboardEffect() {
        Table table = new Table(null, 0);
        AllBitboard a = new AllBitboard(table);
        long[] target;
        long[] result;
        final long[] space = a.getSpaceBitboard();
        result = a.createKomaBitboardEffect(space, 1, TableDefine.ps, 2, 0);
        target = createTargetBitboard(
                0b000000000,
                0b000001110,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pK, 4, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000111000,
                0b000101000
        );
        Assert.assertArrayEquals(target, result);
        //
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pK, 3,8);
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pK, 3, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000011100,
                0b000010100
        );
        Assert.assertArrayEquals(target, result);
        //
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pK, 2, 8);
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pK, 2, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000001110,
                0b000001010
        );
        Assert.assertArrayEquals(target, result);
        //
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pK, 1, 8);
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pK, 1, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000111,
                0b000000101
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pK, 0,8);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pK, 0, 8);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000011,
                0b000000010
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pG, 4,4);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pG, 4, 4);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000111000,
                0b000101000,
                0b000010000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pG, 4,0);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pG, 4, 0);
        target = createTargetBitboard(
                0b000101000,
                0b000010000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pG, 6,0);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pG, 6, 0);
        target = createTargetBitboard(
                0b010100000,
                0b001000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pG, 7,0);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pG, 7, 0);
        target = createTargetBitboard(
                0b101000000,
                0b010000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(TableDefine.pG, 8,0);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(space, 0, TableDefine.pG, 8, 0);
        target = createTargetBitboard(
                0b010000000,
                0b100000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);
        //
        table = Table.createNullTable();
        table.setKoma(Table.addNariKoma(TableDefine.pS), 4,4);
        a.clear(table); // 初期化
        result = a.createKomaBitboardEffect(
                space, 0, Table.addNariKoma(TableDefine.pS), 4, 4);
        target = createTargetBitboard(
                0b000000000,
                0b000000000,
                0b000000000,
                0b000111000,
                0b000101000,
                0b000010000,
                0b000000000,
                0b000000000,
                0b000000000
        );
        Assert.assertArrayEquals(target, result);

    }
    public long[] createTargetBitboard(long... x) {
        long[] result = new long[2];
        int y = 0;
        for (long xx : x) {
            for (int i = 0 ; i < TableDefine.B_MAX ; i++) {
                AllBitboard.setOrBit(result, y*TableDefine.B_MAX+ i,(xx>>>i) &0b1L);
            }
            y++;
        }
        return result;
    }

    /** getBitBoardNext のテスト */
    @Test
    public void testGetBitBoardNext() {
        long[] b = new long[2];
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, -1));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 0));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 1));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 63));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 64));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 65));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 66));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 80));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 81));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 82));
        b[0] = 1L;
        Assert.assertEquals(0, AllBitboard.getBitBoardNext(b, -1));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 0));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 1));
        Assert.assertEquals(81, AllBitboard.getBitBoardNext(b, 1));
        b[0] = 1L;
        b[1] = 0b10000000000000000L;
        Assert.assertEquals(0, AllBitboard.getBitBoardNext(b, -1));
        Assert.assertEquals(80, AllBitboard.getBitBoardNext(b, 0));
        Assert.assertEquals(80, AllBitboard.getBitBoardNext(b, 1));
        Assert.assertEquals(80, AllBitboard.getBitBoardNext(b, 1));
        b[0] = 0;
        b[1] = 0b1000000000000000L;
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, -1));
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, 0));
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, 1));
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, 63));
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, 64));
        Assert.assertEquals(79, AllBitboard.getBitBoardNext(b, 65));
    }
}
