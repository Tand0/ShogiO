package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** Pointer の Test */
public class PointerTest {
    /** コンストラクタ：pos が 0 で初期化される */
    @Test
    public void testConstructor() {
        Pointer p = new Pointer();
        Assert.assertEquals(0, p.pos);
    }

    /** clear：pos が 0 に戻る */
    @Test
    public void testClear() {
        Pointer p = new Pointer();
        p.pos = 5;
        p.clear();
        Assert.assertEquals(0, p.pos);
    }

    /** checkSum：正常値なら例外が発生しない */
    @Test
    public void testCheckSumNormal() {
        Pointer p = new Pointer();

        p.pPSum = 10;
        p.pLSum = 2;
        p.pNSum = 3;
        p.pSSum = 4;
        p.pGSum = 4;
        p.pRSum = 1;
        p.pBSum = 2;
        p.pKSum = 1;

        p.checkSum(); // 例外がでない
    }

    /** checkSum：超過したら例外が発生する */
    @Test
    public void testCheckSumOverflow() {
        Pointer p = new Pointer();

        p.pPSum = 19; // 歩が 18 を超える → NG

        UnsupportedOperationException ex =
                Assert.assertThrows(UnsupportedOperationException.class, p::checkSum);

        String message = ex.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.contains("p=19"));
    }

    /** okSum：すべてのコマが規定数なら true */
    @Test
    public void testOkSumTrue() {
        Pointer p = new Pointer();

        p.pPSum = 18;
        p.pLSum = 4;
        p.pNSum = 4;
        p.pSSum = 4;
        p.pGSum = 4;
        p.pRSum = 2;
        p.pBSum = 2;
        p.pKSum = 2;

        Assert.assertTrue(p.okSum());
    }

    /** okSum：どれかが不足していれば false */
    @Test
    public void testOkSumFalse() {
        Pointer p = new Pointer();

        p.pPSum = 18;
        p.pLSum = 4;
        p.pNSum = 4;
        p.pSSum = 4;
        p.pGSum = 4;
        p.pRSum = 2;
        p.pBSum = 2;
        p.pKSum = 1; // ここだけ不足

        Assert.assertFalse(p.okSum());
    }
}
