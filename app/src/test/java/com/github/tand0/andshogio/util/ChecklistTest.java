package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** Checklist の Test */
public class ChecklistTest {

    /** フィールド値が正しく保持される */
    @Test
    public void testRecordFields() {
        Checklist c = new Checklist(3, 2, true, false);

        Assert.assertEquals(3, c.forcedNari());
        Assert.assertEquals(2, c.beatKinshi());
        Assert.assertTrue(c.narazuOK());
        Assert.assertFalse(c.narenai());
    }

    /** getForcedNari: 手番0（先手）の場合 */
    @Test
    public void testGetForcedNariSente() {
        Checklist c = new Checklist(3, 0, false, false);

        Assert.assertTrue(c.getForcedNari(0, 1));  // y < 3 → true
        Assert.assertTrue(c.getForcedNari(0, 2));  // y < 3 → true
        Assert.assertFalse(c.getForcedNari(0, 3)); // y < 3 → false
        Assert.assertFalse(c.getForcedNari(0, 5));
    }

    /** getForcedNari: 手番1（後手）の場合 */
    @Test
    public void testGetForcedNariGote() {
        Checklist c = new Checklist(3, 0, false, false);

        // (8 - 3) = 5 より大きいと強制
        Assert.assertFalse(c.getForcedNari(1, 4));
        Assert.assertTrue(c.getForcedNari(1, 6));
        Assert.assertTrue(c.getForcedNari(1, 7));
    }

    /** isTekijin: 敵陣判定（先手） */
    @Test
    public void testIsTekijinSente() {
        Checklist c = new Checklist(3, 0, false, false);

        Assert.assertTrue(c.isTekijin(0, 0));
        Assert.assertTrue(c.isTekijin(0, 2));
        Assert.assertFalse(c.isTekijin(0, 3));
    }

    /** sTekijin: 敵陣判定（後手）*/
    @Test
    public void testIsTekijinGote() {
        Checklist c = new Checklist(3, 0, false, false);

        Assert.assertFalse(c.isTekijin(1, 5));
        Assert.assertTrue(c.isTekijin(1, 6));
        Assert.assertTrue(c.isTekijin(1, 7));
    }

    /** getUchiKinshi: beatKinshi = 0 の場合は常に false */
    @Test
    public void testUchiKinshiZero() {
        Checklist c = new Checklist(3, 0, false, false);

        Assert.assertFalse(c.getUchiKinshi(0, 1));
        Assert.assertFalse(c.getUchiKinshi(1, 7));
    }

    /** getUchiKinshi: 手番0（先手） */
    @Test
    public void testUchiKinshiSente() {
        Checklist c = new Checklist(3, 4, false, false);

        Assert.assertTrue(c.getUchiKinshi(0, 1));  // y < 4 → true
        Assert.assertTrue(c.getUchiKinshi(0, 3));  // y < 4 → true
        Assert.assertFalse(c.getUchiKinshi(0, 4));
    }

    /** getUchiKinshi: 手番1（後手）*/
    @Test
    public void testUchiKinshiGote() {
        Checklist c = new Checklist(3, 4, false, false);

        // (8 - 4) = 4 より大きいと禁止
        Assert.assertFalse(c.getUchiKinshi(1, 4));
        Assert.assertTrue(c.getUchiKinshi(1, 5));
        Assert.assertTrue(c.getUchiKinshi(1, 7));
    }

    /** narazuOK / narenai のオーバーライド確認 */
    @Test
    public void testNarazuAndNarenai() {
        Checklist c = new Checklist(3, 2, true, false);

        Assert.assertTrue(c.narazuOK());
        Assert.assertFalse(c.narenai());
    }
}
