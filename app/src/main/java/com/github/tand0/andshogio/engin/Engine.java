package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** 将棋ソフトの思考エンジン。
 * 受信側は SendListener 側で行う。
 */
public abstract class Engine implements Runnable, SendListener {
    /** 待ちキュー */
    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(100);

    /** 最善手情報 */
    private EngineResponse now = null;

    /** engine 名を取得する
     * @return engine 名
     */
    public abstract String getEngineId();

    /** コンストラクタ */
    public Engine() {
    }

    /**
     * 合法手から１手指す
     * @param r エンジンへの要求
     */
    public abstract void createNow(EngineRequest r);

    /** スレッドの実装部 */
    @Override
    public final void run() {
        while (true) {
            try {
                // キューが空ならデータが入るまで待機する
                Object data = this.queue.take();
                if (data instanceof EngineRequest r) {
                    //
                    fail(r); // あらかじめ失敗を入れておく
                    //
                    createNow(r); // 合法手から１手指させる
                    //
                    r.forResult().send(this.getNow()); // 打ち返す
                    //
                } else if (data instanceof EngineStop) {
                    // 思考停止信号
                    this.setNow(null);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /** 手の情報を取得する(即値)
     * @param now エンジンの応答
     */
    public final void setNow(EngineResponse now) {
        this.now = now;
    }

    /** 手の情報を取得する(即値)
     * @return エンジンの応答
     */
    public final EngineResponse getNow() {
        return this.now;
    }

    /**
     * これを受けた時、ブロックキューに貯める
     * @param x オブジェクト
     * @throws InterruptedException 例外
     */
    @Override
    public final void send(Object x) throws InterruptedException {
        queue.put(x);
    }


    /** 学習を停止する
     * 要するに、キューになんか来たら
     * @return キューが空でないなら true
     */
    public final boolean isEnd() {
        return ! this.queue.isEmpty();
    }

    /**
     * まだ結果が分からないときのデータを返す
     * @param r リクエスト内容
     */
    public final void fail(EngineRequest r) {
        //
        // displayするか否か設定する
        List<TeTable> displayTable;
        int turn;
        int te = TableDefine.LOS;
        if (r == null) {
            turn = -1;
            displayTable = null;
        } else {
            turn = r.turn();
            if (r.display()) {
                displayTable = new ArrayList<>(); // display する
            } else {
                displayTable = null; // display しない
            }
        }
        //
        // 結果を出す
        this.setNow(new EngineResponse(
                turn, te, EngineMate.class,
                displayTable, 0.5f));
    }
}
