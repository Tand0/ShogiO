package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** XYFlag の Test */
public class XYFlagTest {

    /** コンストラクタ：x, y, flag が正しく保持される */
    @Test
    public void testConstructor() {
        XYFlag xy = new XYFlag(3, -2, true);

        Assert.assertEquals(3, xy.x());
        Assert.assertEquals(-2, xy.y(0));   // 手番0ならそのまま
        Assert.assertEquals(2, xy.y(1));    // 手番1なら反転
        Assert.assertTrue(xy.flag());
    }

    /** x(): x がそのまま返る */
    @Test
    public void testX() {
        XYFlag xy = new XYFlag(5, 1, false);
        Assert.assertEquals(5, xy.x());
    }

    /** y(teban): 手番0はそのまま、手番1は反転 */
    @Test
    public void testY() {
        XYFlag xy = new XYFlag(0, 4, false);

        Assert.assertEquals(4, xy.y(0));   // 先手
        Assert.assertEquals(-4, xy.y(1));  // 後手
    }

    /** flag(): 複数移動フラグが正しく返る */
    @Test
    public void testFlag() {
        XYFlag xy1 = new XYFlag(1, 1, true);
        XYFlag xy2 = new XYFlag(1, 1, false);

        Assert.assertTrue(xy1.flag());
        Assert.assertFalse(xy2.flag());
    }
}
