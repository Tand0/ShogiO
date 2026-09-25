package com.github.tand0.shogio.util;

import org.junit.Assert;
import org.junit.Test;

/** TeTable の Test */
public class TeTableTest {
    /** コンストラクタ：table と te が正しく保持される */
    @Test
    public void testConstructor() {
        Table table = new Table(null, 0);
        TeTable tt = new TeTable(table, 5);

        Assert.assertEquals(table, tt.table);
        Assert.assertEquals(5, tt.te);
    }

    /** display：LOS の場合は ------- が返る **/
    @Test
    public void testDisplayLos() {
        Table table = new Table(null, 0);
        TeTable tt = new TeTable(table, TableDefine.LOS);

        Assert.assertEquals("-------", tt.display());
    }

    /** display のテスト */
    @Test
    public void testDisplayNormal() {
        Table table = new Table(null, 0);
        TeTable tt = new TeTable(table, TableDefine.changeTeStringToInt("+7776FU"));

        Assert.assertNotNull(tt.display());
    }

    /** equals：TeTable 同士で table が同じなら true */
    @Test
    public void testEqualsTeTable() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(null, 0);

        TeTable a = new TeTable(t1, TableDefine.changeTeStringToInt("+7776FU"));
        TeTable b = new TeTable(t2, TableDefine.changeTeStringToInt("+7776FU"));
        Assert.assertEquals(a, b);
    }

    /** equals：Table と比較しても table が同じなら true */
    @Test
    public void testEqualsTable() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(null, 0);

        TeTable a = new TeTable(t1, 3);

        Assert.assertEquals(a.table, t2);
    }

    /** equals：異なる table なら false */
    @Test
    public void testEqualsDifferent() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(t1, TableDefine.changeTeStringToInt("+7776FU"));
        //
        TeTable a = new TeTable(t1, TableDefine.LOS);
        TeTable b = new TeTable(t2, TableDefine.LOS);

        Assert.assertNotEquals(a, b);
    }

    /** equals：null は false */
    @Test
    public void testEqualsInvalid() {
        Table t1 = new Table(null, 0);
        TeTable a = new TeTable(t1,TableDefine.changeTeStringToInt("+7776FU"));

        Assert.assertNotEquals(null, a);
    }
}
