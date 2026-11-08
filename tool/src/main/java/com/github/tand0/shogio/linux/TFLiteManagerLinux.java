package com.github.tand0.shogio.linux;

import com.github.tand0.shogio.tool.MyPropertiesAccess;
import com.github.tand0.shogio.util.InputGen;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TensoInterface;

import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

import java.io.File;

/**
 * Tenso Flow とのアクセスを管理するクラス
 */
public class TFLiteManagerLinux implements AutoCloseable, TensoInterface {

    /** モデル */
    private SavedModelBundle model;

    /** input gen */
    public final InputGen inputGen = new InputGen();


    /** コンストラクタ */
    public TFLiteManagerLinux() {
        try {
            // SavedModel 形式のモデルをロード
            this.model = SavedModelBundle.load(
                    new File(MyPropertiesAccess.getString(PropertyDefine.APP_DB_ALL))
                            .getAbsoluteFile().getParentFile().toString(),
                    "model_savedmodel");
        } catch (Exception e) {
            close();
        }
    }
    @Override
    public synchronized Float runCompiledModel(Table table) throws RuntimeException {
        if (model == null) {
            return null;
        }
        // 入力データの準備
        inputGen.updateTable(table);
        try (Tensor inputTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(inputGen.getInput()))) {
            try (Result result = model.session()
                    .runner()
                    .feed("serving_default_input_name", inputTensor)
                    .fetch("statefulpartitionedcall_output_name")
                    .run()) {
                Tensor outputTensor = result.get(0);
                // 結果の抽出
                TFloat32 output = (TFloat32) outputTensor;
                System.out.println("推論成功: " + output.getFloat(0));
                return output.getFloat(0);
            }
        }
    }
    @Override
    public void close() {
        if (model == null) {
            return;
        }
        // インタープリターのクローズ
        model.close();
        model = null;
    }
}
