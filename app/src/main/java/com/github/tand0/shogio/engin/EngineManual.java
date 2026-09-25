package com.github.tand0.shogio.engin;

/** マニュアルで指す */
public class EngineManual extends Engine {

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_manual";
    }

    @Override
    public void createNow(EngineRequest r) {
        // EMPTY
    }

    /** コンストラクタ
     */
    public EngineManual() {
    }

}
