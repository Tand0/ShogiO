package com.github.tand0.shogio.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** ExplorerTable の Test */
public class ExplorerTableTest {
    /**"コンストラクタ：eval 初期値が設定される */
    @Test
    public void testConstructor() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));
        Assert.assertEquals(0.5f, et.getEval(), 0);
    }

    /**"eval の getter / setter */
    @Test
    public void testEvalGetterSetter() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));
        et.setEval(0.75f);
        Assert.assertEquals(0.75f, et.getEval(), 0);
    }

    /**"isExpand：リストが null なら false、設定後は true */
    @Test
    public void testIsExpand() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));

        Assert.assertFalse(et.isExpand());

        et.setList(new ArrayList<>());
        Assert.assertTrue(et.isExpand());
    }

    /**"setList / getList：リストが保持される */
    @Test
    public void testSetGetList() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));

        List<ExplorerTable> list = new ArrayList<>();
        list.add(new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS)));

        et.setList(list);

        Assert.assertEquals(1, et.getList().size());
        Assert.assertEquals(0, et.getList().getFirst().table.getTeban());
    }

    /**"clearList：リストがクリアされて null になる */
    @Test
    public void testClearList() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));

        List<ExplorerTable> list = new ArrayList<>();
        list.add(et);

        et.setList(list);
        Assert.assertTrue(et.isExpand());

        et.clearList();

        Assert.assertFalse(et.isExpand());
        Assert.assertNull(et.getList());
    }

    /**"equals：TeTable.table が一致すれば true */
    @Test
    public void testEquals() {
        ExplorerTable a = new ExplorerTable(
                new TeTable(new Table(null,0), 1));
        ExplorerTable b = new ExplorerTable(
                new TeTable(new Table(null,0), 2));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.table, b.table); // Table との比較
    }

    /**"compareTo：局面が同じなら 0 */
    @Test
    public void testCompareToSame() {
        ExplorerTable a = new ExplorerTable(
                new TeTable(new Table(null,0), 1));
        ExplorerTable b = new ExplorerTable(
                new TeTable(new Table(null,0), 1));

        Assert.assertEquals(0, a.compareTo(b));
    }

    /**"compareTo：評価値の差で比較（先手） */
    @Test
    public void testCompareToEvalSente() {
        Table table1 = new Table(null,0);
        int csa = TableDefine.changeTeStringToInt("+2726FU");
        Table table2 = new Table(table1, csa);
        csa = TableDefine.changeTeStringToInt("-1314FU");
        table2 = new Table(table2, csa);
        ExplorerTable a1 = new ExplorerTable(
                new TeTable(table1, TableDefine.LOS));
        ExplorerTable a2 = new ExplorerTable(
                new TeTable(table1, TableDefine.LOS));
        ExplorerTable b = new ExplorerTable(
                new TeTable(table2, TableDefine.LOS));

        a1.setEval(0.3f);
        b.setEval(0.5f);

        Assert.assertEquals( a1, a2);
        Assert.assertEquals(0, a1.compareTo(a2));
        Assert.assertTrue( a1.compareTo(b) < 0);
        Assert.assertTrue( 0 < b.compareTo(a1));
    }

    /**"display：評価値が文字列に含まれる */
    @Test
    public void testDisplay() {
        ExplorerTable et = new ExplorerTable(
                new TeTable(new Table(null,0),TableDefine.LOS));
        et.setEval(0.1234f);

        String s = et.display();

        Assert.assertTrue(s.contains("12.34%"));
    }
}
