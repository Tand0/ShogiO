package com.github.tand0.andshogio.util;


import org.junit.Assert;

import org.junit.Test;
/**
 * key値に対する評価
 */
public class TableKeyTest {

	/** コンストラクタ */
	public TableKeyTest() {
	}
	/** encode test
	 * 
	 */
	@Test
    public void pointTest() {
		Table only = new Table(null, 0); // デフォルト設定
		String q = (new TableKey(only)).toString(); // 16進出力
		Assert.assertEquals(64, q.length());
		//
		TableKey key = new TableKey(q); // もじから復元
		Assert.assertEquals(q,key.toString()); // 動作確認
		//
	}
	/**
	 * 64bitテスト
	 */
	@Test
    public void bitTest() {
		TableKey key = new TableKey("0000000000000000000000000000000000000000000000000000000000000000");
		Pointer p = new Pointer();
		//
		clearKey(key,p);
		for (int i = 0 ; i < 256;i++) {
			key.setData(p, 1, 1);
		}
		//
		clearKey(key,p);
		for (int i = 0 ; i < 256-3;i+=3) {
			key.setData(p, 3, 0b111);
		}
	}
	/**
     * 64bitテスト
     */
    @Test
    public void setBitTest() {
        TableKey key1 = new TableKey(new Table(null, 0));
        TableKey key2 = new TableKey(new Table(null, 0));
        Assert.assertEquals(key1,key2);
        //
        Pointer p = new Pointer();
        clearKey(key1,p);
        clearKey(key2,p);
        for (int i = 0 ; i < 256;i++) {
            Assert.assertEquals(0, key1.getBit(i));
            key1.setData(p, 1, 1);
            Assert.assertEquals(1, key1.getBit(i));
            //
            Assert.assertEquals(0, key2.getBit(i));
            key2.setBit(i, true);
            Assert.assertEquals(1, key2.getBit(i));
            //
            Assert.assertEquals(key1,key2);
        }
    }

	/** set のチェック */
	@Test
    public void setDataTest() {
		Table oldTable = new Table(null, 0);
		TableKey key = new TableKey(oldTable);
		Pointer p = new Pointer();
		//
		clearKey(key,p);
		Assert.assertEquals(
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		long data;
		data = 1;
		key.setData(p, 1, data);
		Assert.assertEquals(
                "8000000000000000" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		data = 0b10;
		key.setData(p, 2, data);
		Assert.assertEquals(
                "c000000000000000" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		clearKey(key,p);
		data = 0xc000000000000001L;
		key.setData(p, 64, data);
		Assert.assertEquals(
                "c000000000000001" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		data = 0x1L;
		key.setData(p, 1, data);
		Assert.assertEquals(
                "c000000000000001" +
                "8000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		data = 0x3L;
		key.setData(p, 2, data);
		Assert.assertEquals(
                "c000000000000001" +
                "e000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		clearKey(key,p);
		data = 0x4000000000000001L;
		key.setData(p, 63, data);
		Assert.assertEquals(
                "8000000000000002" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		data = 0x7L;
		key.setData(p, 3, data);
		Assert.assertEquals(
                "8000000000000003" +
                "c000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
		//
		clearKey(key,p);
		data = 0x1L;
		key.setData(p, 63, data);
		Assert.assertEquals(
                "0000000000000002" +
                "0000000000000000" +
                "0000000000000000" +
                "0000000000000000",
                key.toString());
	}

	/** set のチェック */
	@Test
    public void getDataTest() {
		Table oldTable = new Table(null, 0);
		TableKey key = new TableKey(oldTable);
		Pointer p = new Pointer();
		//
		clearKey(key,p);
		long data;
		long result;
		data = 0;
		result = key.getData(p, 64);
		Assert.assertEquals(data,result);
		//
		clearKey(key,p);
		data = 1;
		key.setData(p, 1, data);
		p.clear();
		result = key.getData(p, 1);
		Assert.assertEquals(data,result);
		//
		//
		clearKey(key,p);
		data = 0b10;
		key.setData(p, 2, data);
		p.clear();
		result = key.getData(p, 2);
		Assert.assertEquals(data,result);
		//
		clearKey(key,p);
		long data1 = 0xc000000000000001L;
		key.setData(p, 64, data1);
		System.out.println("p1:" + key);
		long data2 = 0x1L;
		key.setData(p, 1, data2);
		System.out.println("p2:" + key);
		long data3 = 0x3L;
		key.setData(p, 2, data3);
		System.out.println("p3:" + key);
		p.clear();
		result = key.getData(p, 64);
		Assert.assertEquals(data1,result);
		result = key.getData(p, 1);
		Assert.assertEquals(data2,result);
		result = key.getData(p, 2);
		Assert.assertEquals(data3,result);
		//
		//
		clearKey(key,p);
		key.setData(p, 63, data1);
		data1 = 0x4000000000000001L;
		System.out.println("d1:" + key);
		data2 = 0x7L;
		key.setData(p, 3, data2);
		System.out.println("d2:" + key);
		p.clear();
		result = key.getData(p, 63);
		Assert.assertEquals(data1,result);
		result = key.getData(p, 3);
		Assert.assertEquals(data2,result);
		//
		
		long sum = 2480048881256L;
		clearKey(key,p);
		key.setData(p, 39+2+2+2+2, sum);
		p.clear();
		result = key.getData(p, 39+2+2+2+2); 
		Assert.assertEquals(sum,result);
	}
	
	
	/**
     * 単純にキーから盤面読みだしてあっているかを確認する
     */
    @Test
    public void firstTest() {
        System.out.println("test start!");
        Table oldTable = new Table(null, 0);
        TableKey key1 = new TableKey(oldTable);
        Table newTable = key1.createTable();
        TableKey key2 = new TableKey(newTable);
        //
        System.out.println(oldTable);
        System.out.println(newTable.toString());
        System.out.println(key1);
        System.out.println(key2);
        System.out.println("test old!");
        System.out.println(oldTable);
        System.out.println("test new!");
        System.out.println(newTable);
        //
        Assert.assertEquals(oldTable, newTable);
        Assert.assertEquals(key1, key2);
    }
    
	/**
	 * clear key
	 * @param key key value
	 * @param p pointer
	 */
	void clearKey(TableKey key,Pointer p) {
		key.key[0] = 0L;
		key.key[1] = 0L;
		key.key[2] = 0L;
		key.key[3] = 0L;
		p.clear();
	}

	/** 持ちコマのテスト */
	@Test
    public void mochiKomaTest() {
		//
		for (int teban = 0; teban < 2 ; teban++) {	   
	        Table only = new Table(null, 0);
	        TableKey key = new TableKey(only);
	        mochiGomaNext(key);
            for (int j = 0; j < TableDefine.B_MAX; j++) {
                for (int i = 0; i < TableDefine.B_MAX; i++) {
	                byte koma = (byte)(0xF & only.getKoma(i, j));
	                if ((koma == TableDefine.pK) || (koma == TableDefine.pNull)) {
	                    continue;
	                }
	                only.setKoma(TableDefine.pNull, i, j);
	                only.setTegoma(koma,  teban, only.getTegoma(koma, teban) + 1);
	                //
	                key = new TableKey(only);
	                mochiGomaNext(key);
	                //
	            }
	        }
		}
	}
	
	/**
	 * 持ちコマの処理が TableKey で正常に動くのかを確認する
	 * @param key key value
	 */
	public void mochiGomaNext(TableKey key) {
		// 打ったあとにキー値を使って同じか確認する
		String keyString = key.toString(); // キーを文字にする
		TableKey tableKey = new TableKey(keyString); // 文字からキーを作る
        // 同じキーのはずだから、同じキーになるはずだ
        Assert.assertEquals(keyString,tableKey.toString());
        //
        Table tableOnly = tableKey.createTable(); // キーから盤面を作る
        TableKey tableKey2 = new TableKey(tableOnly); // onlyからキーを作る
		Assert.assertEquals(tableKey.toString(),tableKey2.toString());		
	}

	/** 64文字の16進文字列を作るヘルパー */
	private String makeHex64(String base) {
		return base.repeat(64 / base.length());
	}

	/**
	 * constructorFromString() のテスト
	 */
	@Test
	public void testConstructorFromString() {
		String hex = makeHex64(
						"8a67cc43e4556840" +
						"0000019af7863fef" +
						"1f29511010720ae0" +
						"a04e7c19a0dc5f8e");
		TableKey key = new TableKey(hex);
		//
		Assert.assertEquals(4, key.getKey().length);
		Assert.assertEquals(hex, key.toString());
	}

	/**
	 * 変な入力をしてエラーがでるか？
	 */
	@Test
	public void testConstructorFromStringInvalidLength() {
		Assert.assertThrows(UnsupportedOperationException.class, () -> new TableKey("1234"));
	}

	/** キーが正しく取れるか？
	 */
	@Test
	public void testConstructorFromLongArray() {
		long[] arr = {0x8a67cc43e64a7000L,
				0x00001d9af60f8e78L,
				0x24786ca00fd2aa40L,
				0xe3fa73e0cd783146L};
		TableKey key = new TableKey(arr);

		Assert.assertArrayEquals(arr, key.getKey());
	}

	/**
	 * キーが文字として処理されるか？
	 */
	@Test
	public void testToString() {
		long[] arr = {
				0x8a67ca03e6e56000L,
				0x0000119af0dc73ceL,
				0xf89009450003ea2aL,
				0xe139e0cd787f87c5L};
		TableKey key = new TableKey(arr);

		String s = key.toString();
		Assert.assertEquals(
				"8a67ca03e6e560000000119af0dc73cef89009450003ea2ae139e0cd787f87c5",
				s);
	}

	/** キー値が同一のとき、同一として処理されるか？
	 */
	@Test
	public void testEqualsAndHashCode() {
		long[] arr1 = {0x8a67cc43e64a7000L,
				0x00001d9af60f8e78L,
				0x24786ca00fd2aa40L,
				0xe3fa73e0cd783146L};
		long[] arr2 = {0x8a67cc43e64a7000L,
				0x00001d9af60f8e78L,
				0x24786ca00fd2aa40L,
				0xe3fa73e0cd783146L};
		long[] arr3 = {
				0x8a67ca03e6e56000L,
				0x0000119af0dc73ceL,
				0xf89009450003ea2aL,
				0xe139e0cd787f87c5L};

		TableKey k1 = new TableKey(arr1);
		TableKey k2 = new TableKey(arr2);
		TableKey k3 = new TableKey(arr3);

		Assert.assertEquals(k1, k2);
		Assert.assertEquals(k1.hashCode(), k2.hashCode());
		Assert.assertNotEquals(k1, k3);
	}

	/**
	 * キー値の compareTo() が正しく処理されるか？
	 */
	@Test
	public void testCompareTo() {
		TableKey k1 = new TableKey(new long[]{1, 2, 3, 4});
		TableKey k2 = new TableKey(new long[]{1, 2, 3, 5});
		TableKey k3 = new TableKey(new long[]{1, 2, 3, 4});

		Assert.assertTrue(k1.compareTo(k2) < 0);
		Assert.assertTrue(k2.compareTo(k1) > 0);
		Assert.assertEquals(0, k1.compareTo(k3));
	}

	/** bit 設定がうまくできるか？ */
	@Test
	public void testSetBitAndGetBit() {
		TableKey key = new TableKey(makeHex64("0"));

		key.setBit(0); // 最初のビットを立てる
		Assert.assertEquals(1, key.getBit(0));

		key.setBit(63); // 64bit目
		Assert.assertEquals(1, key.getBit(63));

		key.setBit(63, false);
		Assert.assertEquals(0, key.getBit(63));
	}

	/** bit len の設定がうまくできるか？ */
	@Test
	public void testSetBitLenAndGetBitLen() {
		TableKey key = new TableKey(makeHex64("0"));

		key.setBitLen(10, 4, 0b1010);
		Assert.assertEquals(0b1010, key.getBitLen(10, 4));
	}

	/** set data, get data の設定がうまくできるか？ */
	@Test
	public void testSetDataAndGetData() {
		TableKey key = new TableKey(makeHex64("0"));
		Pointer p = new Pointer();

		key.setData(p, 5, 0b10101);
		p.pos = 0;

		long result = key.getData(p, 5);
		Assert.assertEquals(0b10101, result);
	}

	/** peek data の設定がうまくできるか？ */
	@Test
	public void testPeakData() {
		TableKey key = new TableKey(makeHex64("0"));
		Pointer p = new Pointer();

		key.setData(p, 3, 0b110);
		Assert.assertEquals(0b110, key.peakData(new Pointer(), 3));
	}

	/** 手番の確認 */
	@Test
	public void testGetSetTeban() {
		Table table = new Table(null, 0);
		TableKey key = new TableKey(table);
		//
		// 先手が得られる
		Assert.assertEquals(0, key.getTeban());
		//
		int csa = TableDefine.changeTeStringToInt("+2726FU");
		table = new Table(table, csa); // 1手進める
		key = new TableKey(table);
		// 後手が得られる
		Assert.assertEquals(1, key.getTeban());
	}
}