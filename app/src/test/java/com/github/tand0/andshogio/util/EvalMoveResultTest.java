package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

/** EvalMoveResult の Test */
public class EvalMoveResultTest {
    /** 全件テストする */
    @Test
    public void testFields() {
        EvalMoveResult r = new EvalMoveResult(10L, 5L);

        Assert.assertEquals(10L, r.win());
        Assert.assertEquals(5L, r.los());

        EvalMoveResult r1 = new EvalMoveResult(3L, 1L);
        EvalMoveResult r2 = new EvalMoveResult(3L, 1L);

        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r1.hashCode(), r2.hashCode());

        EvalMoveResult r3 = new EvalMoveResult(7L, 2L);
        String s = r3.toString();

        Assert.assertTrue(s.contains("win=7"));
        Assert.assertTrue(s.contains("los=2"));
    }
}
