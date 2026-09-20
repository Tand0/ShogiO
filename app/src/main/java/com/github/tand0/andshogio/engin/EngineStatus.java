package com.github.tand0.andshogio.engin;

/** エンジンの状態を保持するためのクラス */
public class EngineStatus {
    /** エンジン情報 */
    public final Engine engine;

    /** このフラグが立っていたら即決する */
    public final boolean decide;


    /** エンジン応答 */
    private EngineResponse engineResponse = null;

    /** コンストラクタ
     *
     * @param engine エンジン情報
     * @param decide このフラグが立っていたら即決する
     */
    public EngineStatus(Engine engine, boolean decide) {
        this.engine = engine;
        this.decide = decide;
    }

    /** エンジン応答の設定
     *
     * @param engineResponse エンジン応答
     */
    public void setEngineResponse(EngineResponse engineResponse) {
        this.engineResponse = engineResponse;
    }

    /** エンジン応答の取得
     *
     * @return エンジン応答
     */
    public EngineResponse getEngineResponse() {
        return engineResponse;
    }
}
