package com.github.tand0.andshogio.util;

import static com.github.tand0.andshogio.util.TableDefine.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.List;


/** コマの移動のテスト */
public class KomaMoveTest {
    /** コンストラクタ */
    public KomaMoveTest() {
        
    }
    /** 移動先が合法手である手を探す用。
     * サイズが18個あるか確認する
     */
    @Test
	public void moversTest() {
		List<KomaMove> movers = KomaMove.movers;
		
		assertEquals(18, movers.size());
		
	}

    /** コンストラクタが XYFlag と koma を正しく保持する */
    @Test
    public void testConstructor() {
        int[] koma = { pG, pS };
        KomaMove km = new KomaMove(1, -1, koma, true);

        assertEquals(1, km.xYFlag.x());
        assertEquals(-1, km.xYFlag.y(0));
        assertEquals(1, km.xYFlag.y(1));
        assertTrue(km.xYFlag.flag());
        assertArrayEquals(koma, km.koma);
    }

    /** movers のサイズ確認 */
    @Test
    public void testMoversSize() {
        // 8方向 + 桂馬2 + 複数移動8 = 18
        assertEquals(18, KomaMove.movers.size());
    }

    /** movers の内容確認（例：最初の8方向）*/
    @Test
    public void testMoversFirstElements() {
        KomaMove m0 = KomaMove.movers.getFirst();
        assertEquals(1, m0.xYFlag.x());
        assertEquals(1, m0.xYFlag.y(0));
        assertFalse(m0.xYFlag.flag());
        assertArrayEquals(KomaMove.CHECKLIST_downYoko, m0.koma);

        KomaMove m4 = KomaMove.movers.get(4);
        assertEquals(0, m4.xYFlag.x());
        assertEquals(-1, m4.xYFlag.y(0));
        assertFalse(m4.xYFlag.flag());
        assertArrayEquals(KomaMove.CHECKLIST_upUp, m4.koma);
    }

    /** 桂馬チェックの mover が正しい */
    @Test
    public void testKnightMovers() {
        KomaMove m8 = KomaMove.movers.get(8);
        assertEquals(1, m8.xYFlag.x());
        assertEquals(-2, m8.xYFlag.y(0));
        assertArrayEquals(KomaMove.CHECKLIST_kei, m8.koma);
        assertFalse(m8.xYFlag.flag());

        KomaMove m9 = KomaMove.movers.get(9);
        assertEquals(-1, m9.xYFlag.x());
        assertEquals(-2, m9.xYFlag.y(0));
        assertArrayEquals(KomaMove.CHECKLIST_kei, m9.koma);
    }

    /** 複数移動チェック（香・飛・角）が flag=true で定義されている */
    @Test
    public void testLongRangeMovers() {
        // 香車
        KomaMove kyo = KomaMove.movers.get(10);
        assertEquals(0, kyo.xYFlag.x());
        assertEquals(-1, kyo.xYFlag.y(0));
        assertTrue(kyo.xYFlag.flag());
        assertArrayEquals(KomaMove.CHECKLIST_kyo, kyo.koma);

        // 飛車
        KomaMove rook = KomaMove.movers.get(11);
        assertEquals(0, rook.xYFlag.x());
        assertEquals(1, rook.xYFlag.y(0));
        assertTrue(rook.xYFlag.flag());
        assertArrayEquals(KomaMove.CHECKLIST_rook, rook.koma);

        // 角
        KomaMove kaku = KomaMove.movers.get(14);
        assertEquals(1, kaku.xYFlag.x());
        assertEquals(-1, kaku.xYFlag.y(0));
        assertTrue(kaku.xYFlag.flag());
        assertArrayEquals(KomaMove.CHECKLIST_kaku, kaku.koma);
    }

    /** CHECKLIST 配列の内容確認（例：CHECKLIST_up）*/
    @Test
    public void testCheckListArrays() {
        assertArrayEquals(
                new int[]{ pG, pS, ppS, ppL, ppN, ppP, ppR, pK },
                KomaMove.CHECKLIST_up
        );

        assertArrayEquals(
                new int[]{ pN },
                KomaMove.CHECKLIST_kei
        );
    }
}
