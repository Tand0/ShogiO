package com.github.tand0.andshogio;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.github.tand0.andshogio.util.InputGen;
import com.github.tand0.andshogio.util.PropertyDefine;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
// マニフェストに登録したカスタムApplicationクラス（MyApplication）を指定
public class ExampleInstrumentedTest {

    /** user app context */
    @Test
    public void testApplicationInitialization() {
        // 起動されたApplicationインスタンスを取得
        MainApplication app = ApplicationProvider.getApplicationContext();

        // インスタンスがnullでないか確認
        Assert.assertNotNull(app);
        //
        // レイヤ数が最大かチェック
        int layerMax = app.getInt(PropertyDefine.APP_CHANNEL_MAX,1);
        Assert.assertEquals(InputGen.MAX_CHANNEL, layerMax);
        //
        String[] keyword = {
                PropertyDefine.APP_LOAD_TARGET,
                PropertyDefine.APP_RATE_MIN,
                PropertyDefine.APP_DB_ALL,
                PropertyDefine.APP_DB_TOTAL,
                PropertyDefine.APP_DB_SMALL,
                PropertyDefine.APP_DB_EVAL,
                PropertyDefine.APP_DB_REMAIN,
                PropertyDefine.APP_DB_BAD,
                PropertyDefine.APP_DATASET_ALL,
                PropertyDefine.APP_DB_ESTIMATE
        };
        for (String key : keyword) {
            String x = app.getString(key, "x");
            Assert.assertNotNull(x);
        }
    }

    /** user app context */
    @Test
    public void useAppContextTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Assert.assertNotNull(appContext);
        assertEquals("com.github.tand0.andshogio", appContext.getPackageName());
    }
}