package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** EvalTable のテスト */
public class EvalTeTableTest {
    /** 反転のテスト */
    @Test
    public void flippingTest() {
        Table table = new Table(null, 0);
        float late = 2.0f /3.0f;
        EvalTeTable eT = new EvalTeTable(table, TableDefine.LOS, late, 2, 1);
        //
        // 上下反転すると勝率も反転する
        Assert.assertEquals(eT, eT.flippingVertical().flippingVertical());
        EvalTeTable flipping = eT.flippingVertical();
        Assert.assertEquals(flipping.eval,1 - eT.eval, 0.0f);
        Assert.assertEquals(flipping.win , eT.los);
        Assert.assertEquals(flipping.los , eT.win);
        //
        // 左右反転しても勝利は変わらない
        Assert.assertEquals(eT, eT.flippingHorizontal().flippingHorizontal());
        flipping = eT.flippingHorizontal();
        Assert.assertEquals(flipping.eval, eT.eval, 0.0f);
        Assert.assertEquals(flipping.win , eT.win);
        Assert.assertEquals(flipping.los , eT.los);
    }
    /** compareTo() のテスト */
    @Test
    public void testCompareTo() {
        float late1 = 0.1f;
        float late2 = 0.2f;
        Table table1 = new Table(null, 0);
        Table table2 = new Table(table1,TableDefine.changeTeStringToInt("+7776FU"));
        table2 = new Table(table2,TableDefine.changeTeStringToInt("-3334FU"));
        //
        EvalTeTable eT11 = new EvalTeTable(table1, TableDefine.LOS, late1, 2, 1);
        EvalTeTable eT12 = new EvalTeTable(table1, TableDefine.LOS, late2, 2, 1);
        EvalTeTable eT21 = new EvalTeTable(table2, TableDefine.LOS, late1, 2, 1);
        EvalTeTable eT22 = new EvalTeTable(table2, TableDefine.LOS, late2, 2, 1);
        Assert.assertEquals(eT11, eT12); // 同じ値
        Assert.assertEquals(eT21, eT22); // 同じ値
        Assert.assertEquals(0  , eT11.compareTo(eT12)); // 同じ値
        Assert.assertEquals(0  , eT21.compareTo(eT22)); // 同じ値
        Assert.assertEquals(-1 , eT11.compareTo(eT22));
        Assert.assertEquals(1  , eT22.compareTo(eT11));
    }

    /** 表示テスト */
    @Test
    public void testDisplay() {
        Table table1 = new Table(null, 0);
        EvalTeTable eT11 = new EvalTeTable(table1, TableDefine.LOS, 0.1f, 2, 3);
        String string = eT11.display();
        Assert.assertNotNull(string);
        Assert.assertTrue(string.contains("10"));
        Assert.assertTrue(string.contains("2"));
        Assert.assertTrue(string.contains("3"));
    }

}
