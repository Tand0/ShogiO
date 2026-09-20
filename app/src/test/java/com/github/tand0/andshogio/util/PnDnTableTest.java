package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** PnDnTable の Test */
public class PnDnTableTest {

    /** equals() のテスト */
    @Test
    public void testEquals() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(t1,TableDefine.changeTeStringToInt("+7776FU"));
        TeTable teTable1a = new TeTable(t1, TableDefine.LOS);
        TeTable teTable1b = new TeTable(t1, TableDefine.LOS);
        TeTable teTable2 = new TeTable(t2, TableDefine.LOS);
        //
        Assert.assertEquals(teTable1a, teTable1b);
        Assert.assertNotEquals(teTable1a, teTable2);
        //
        PnDnTable p1a = new PnDnTable(teTable1a, 1);
        PnDnTable p1b = new PnDnTable(teTable1b, 1);
        PnDnTable p2 = new PnDnTable(teTable2, 1);
        //
        Assert.assertEquals(p1a, p1b);
        Assert.assertNotEquals(p1a, p2);
        //
        Assert.assertEquals(1, p1a.getPn(0));
        Assert.assertEquals(1, p1a.getDn(0));
        Assert.assertEquals(1, p1a.getPn(1));
        Assert.assertEquals(1, p1a.getDn(1));
        //
        p1a.setPn(0, 2);
        p1a.setDn(0, 3);
        //
        Assert.assertEquals(2, p1a.getPn(0));
        Assert.assertEquals(3, p1a.getDn(0));
        Assert.assertEquals(3, p1a.getPn(1));
        Assert.assertEquals(2, p1a.getDn(1));
        //
        p1a.setPn(1, 3);
        p1a.setDn(1, 2);
        //
        Assert.assertEquals(2, p1a.getPn(0));
        Assert.assertEquals(3, p1a.getDn(0));
        Assert.assertEquals(3, p1a.getPn(1));
        Assert.assertEquals(2, p1a.getDn(1));
        //
        String string = p1a.display();
        Assert.assertTrue(string.contains(":2"));
        Assert.assertTrue(string.contains(":3"));
    }
}
