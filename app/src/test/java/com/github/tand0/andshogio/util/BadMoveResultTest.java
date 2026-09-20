package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** BadMoveResult の Test */
public class BadMoveResultTest {
    /** 全件テストする */
    @Test
    public void testFields() {
        List<TeTable> teTableList = new ArrayList<>();
        long win = 10L;
        long los= 5L;
        BadMoveResult b = new BadMoveResult(teTableList, win, los);
        //
        Assert.assertEquals(teTableList, b.teTableList());
        Assert.assertEquals(win, b.win());
        Assert.assertEquals(los, b.los());
    }
}
