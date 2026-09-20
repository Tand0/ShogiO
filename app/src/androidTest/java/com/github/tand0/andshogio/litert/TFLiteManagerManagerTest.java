package com.github.tand0.andshogio.litert;

import androidx.test.platform.app.InstrumentationRegistry;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.AssetManager;

import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;

import org.junit.Assert;
import org.junit.Test;


/** TFLiteManagerManager に対するテスト */
public class TFLiteManagerManagerTest {

    /** 普通のテスト
     */
    @Test
    public void normalTest() {
        // Context of the app under test.
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Assert.assertNotNull(appContext);
        assertEquals("com.github.tand0.andshogio", appContext.getPackageName());
        AssetManager am = appContext.getAssets();
        try (TFLiteManager manager = new TFLiteManager(am)) {
            //
            Table table = new Table(null, 0);
            this.check(manager, table, 0.54f);
            //
            table = new Table(table, TableDefine.changeTeStringToInt("+2726FU"));
            this.check(manager, table, 0.45f);
            //
            table = new Table(table, TableDefine.changeTeStringToInt("-8384FU"));
            this.check(manager, table, 0.5f);
            //
        }
    }

    /**
     * 値のチェック
     * @param manager TFLiteManager
     * @param table テーブル情報
     * @param expected 想定値
     */
    public void check(TFLiteManager manager, Table table, float expected) {
        Float result = manager.runCompiledModel(table);
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result, 0.1f);
        //
    }
}
