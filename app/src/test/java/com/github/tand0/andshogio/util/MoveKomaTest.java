package com.github.tand0.andshogio.util;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;


/** コマ移動のテスト */
public class MoveKomaTest {

	/** コンストラクタ */
    public MoveKomaTest() {
        
    }
	/** コマ移動のテスト */
	@Test
	public void moversTest() {
		Map<Integer,MoveKoma> moveKomaMap = MoveKoma.moverKomaMap;
		//
		assertEquals(14, moveKomaMap.size());
		//
		MoveKoma moveKoma = moveKomaMap.get((int)TableDefine.pK); // 王
		//
        Assert.assertNotNull(moveKoma);
        ArrayList<XYFlag> xYFlagList = moveKoma.getXYFlag();
		assertEquals(8, xYFlagList.size());
		//
		for (XYFlag xYFlag : xYFlagList) {
			System.out.println(
					"ou0 x=" + xYFlag.x() + " y=" + xYFlag.y(0) + " f=" + xYFlag.flag());
			System.out.println(
					"ou1 x=" + xYFlag.x() + " y=" + xYFlag.y(1) + " f=" + xYFlag.flag());
		}
	}
}
