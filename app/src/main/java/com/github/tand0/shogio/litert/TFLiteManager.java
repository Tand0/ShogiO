package com.github.tand0.shogio.litert;

import android.content.res.AssetManager;

import com.github.tand0.shogio.util.InputGen;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TensoInterface;

import com.google.ai.edge.litert.Accelerator;
import com.google.ai.edge.litert.CompiledModel;
import com.google.ai.edge.litert.LiteRtException;
import com.google.ai.edge.litert.TensorBuffer;
import java.util.List;

/**
 * TensoFlow Lite とのアクセスを管理するクラス
 */
public class TFLiteManager implements AutoCloseable, TensoInterface {

    /** モデルパス */
    public static final String MODEL_PATH = "model.tflite";

    /** input gen */
    public InputGen inputGen = new InputGen();

    /** model */
    public CompiledModel compiledModel = null;

    List<TensorBuffer> inputTensors = null;

    List<TensorBuffer> outputTensors = null;

    /** コンストラクタ
     * @param am Asset Manager
     */
    public TFLiteManager(AssetManager am) {
        createInterpreter(am);
    }

    /** インタプリタの生成
     * @param am Asset Manager
     */
    public void createInterpreter(AssetManager am) {
        // CompiledModel を直接作成
        CompiledModel.Options options = new CompiledModel.Options(Accelerator.CPU);
        try {
            this.compiledModel =
                    CompiledModel.create(
                            am,
                            MODEL_PATH,
                            options);
            this.inputTensors = this.compiledModel.createInputBuffers();
            this.outputTensors = this.compiledModel.createOutputBuffers();
        } catch (LiteRtException e) {
            this.compiledModel = null;
            this.inputTensors = null;
            this.outputTensors = null;
        }
    }

    @Override
    public synchronized Float runCompiledModel(Table table) throws RuntimeException {
        if (this.compiledModel == null) {
            return null;
        }
        try {
            //
            // インプットとして書き込む
            inputGen.updateTable(table);
            inputTensors.getFirst().writeFloat(inputGen.getInput());
            //
            // 推論する
            compiledModel.run(inputTensors, outputTensors);
            //
            // 1次元の結果を得る
            float[] flatOutputData = outputTensors.getFirst().readFloat();
            //
            // 1次元から１つのスカラーを得る
            float result = flatOutputData[0];
            //
            if (table.getTeban() != 0) {
                result = 1.0f - result; // 反転する
            }
            return result;
        } catch (LiteRtException e) {
            return null;
        }
    }
    /** AutoCloseable を使って try 文でクローズできるようにする
     */
    @Override
    public void close() {
        if (compiledModel != null) {
            compiledModel.close();
            compiledModel = null;
        }
    }
}
