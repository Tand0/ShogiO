package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.EvalTeTableMinMax;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;
import com.github.tand0.shogio.util.TensoInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;



/** 探索エンジン */
public class EngineExplorerMinMax extends Engine {

    /** 先手詰み評価 */
    public static final float MAX_VALUE = 10.0f;

    /** 先手詰まされ評価 */
    public static final float MIN_VALUE = -10.0f;

    /** テーブル最大サイズ */
    private long maxTableFactory = 100000;

    /** レイヤインタフェース */
    private final TensoInterface tensoInterface;

    /** 想定する指し手 */
    private EvalTeTableMinMax bestMove = null;

    /** ダメなテーブルのリスト */
    private final HashMap<Table, EvalTeTableMinMax> factory = new HashMap<>();

    /** 読みの深さ */
    private int maxLevel = 5;

    /** 読みの最小幅 */
    private int maxSon = 10;

    /** 指定回数変更がなければ終了 */
    private int maxNoChange = 10;

    /** 学習する最大局面数 の設定
     * @param maxTableFactory 学習する最大局面数
     */
    public void maxTableFactory(long maxTableFactory) {
        this.maxTableFactory = maxTableFactory;
    }
    /** 学習する最大局面数 の取得
     * @return maxTableFactory 学習する最大局面数
     */
    public long maxTableFactory() {
        return this.maxTableFactory;
    }

    /** 指定回数 の設定
     * @param maxNoChange 指定回数
     */
    public void maxNoChange(int maxNoChange) {
        this.maxNoChange = maxNoChange;
    }

    /** 指定回数 の取得
     * @return maxNoChange 指定回数
     */
    public int maxNoChange() {
        return this.maxNoChange;
    }

    /** 変更なしフラグ */
    public int nowNoChange = 0;

    /** 読みの深さ の設定
     * @param maxLevel 読みの深さ (0以下にはできません)
     */
    public void maxLevel(int maxLevel) {
        this.maxLevel = Math.max(1,maxLevel);
    }

    /** 読みの深さ の取得
     * @return maxLevel 読みの深さ (0以下にはできません)
     */
    public int maxLevel() {
        return this.maxLevel;
    }

    /** 読みの最小幅 の設定
     * @param maxSon 読みの最小幅 (0以下にはできません)
     */
    public void maxSon(int maxSon) {
        this.maxSon = Math.max(1,maxSon);
    }

    /** 読みの最小幅 の取得
     * @return maxSon 読みの最小幅 (0以下にはできません)
     */
    public int maxSon() {
        return this.maxSon;
    }

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_explorer";
    }

    /** コンストラクタ
     * @param tensoInterface レイヤーのインタフェース
     * @param topOpBadTeableSet ダメなテーブルのセット
     */
    public EngineExplorerMinMax(TensoInterface tensoInterface, Set<TeTable> topOpBadTeableSet) {
        super();
        this.tensoInterface = tensoInterface;
        this.factory.clear();
        for (TeTable teTable : topOpBadTeableSet) {
            float eval = teTable.table.getSide() == 0 ?
                    EngineExplorerMinMax.MIN_VALUE : EngineExplorerMinMax.MAX_VALUE; // ハードルを上げる
            this.factory.put(
                    teTable.table,
                    new EvalTeTableMinMax(teTable.table, TableDefine.LOS, eval,Integer.MAX_VALUE));
        }
    }

    @Override
    public void createNow(EngineRequest r) {
        TeTable table = r.teTableList().getLast();
        //
        List<TeTable> displayList;
        if (r.display()) {
            // 表示モードの時はこちら
            // 先頭に前局面を乗せる
            displayList = new ArrayList<>();
            displayList.addFirst(r.teTableList().getFirst());
        } else {
            displayList = null;
        }
        //
        float alpha = EngineExplorerMinMax.MIN_VALUE;
        float beta = EngineExplorerMinMax.MAX_VALUE;
        EvalTeTableMinMax root =
                new EvalTeTableMinMax(table.table, table.te, 0.5f, r.turn());
        factory.put(root.table, root);
        //
        nowNoChange = 0;  // 変更なしフラグを立てる
        bestMove = null;  // 初めは消す
        for (int depth  = 1; depth <= this.maxLevel(); depth++) {
            float eval = alphaBeta(root, r.turn(), depth, alpha, beta, true, displayList);
            // ここが重要：浅い読みの結果で窓を絞る
            if (root.table.getSide() == 0) { // 先手
                alpha = Math.max(alpha, eval);
            } else { // 後手
                beta = Math.min(beta, eval);
            }
            //
            // 最後に走らせることで必ず１回は回す
            if (this.isInterruptOrTimeout()
                    || (maxNoChange() <= nowNoChange)) {
                break;
            }
        }
        //
        // 全消しする。
        // 過去の手を残しても良いが、
        // そうするとteが正しくなくなる問題が出たのでやめました
        factory.clear();
    }

    /**
     * αβ狩りをする
     * @param table テーブル
     * @param baseLevel 初期手数
     * @param depth 読み深さ
     * @param alpha α値
     * @param beta β値
     * @param isFirst true:最初、false:２手目以降
     * @param displayList GUIでの表示用リスト
     * @return 評価値
     */
    protected float alphaBeta(
            EvalTeTableMinMax table,
            int baseLevel,
            int depth,
            float alpha,
            float beta,
            boolean isFirst,
            List<TeTable> displayList) {
        //
        // 終端（勝敗 or 深さ制限）
        if ((depth <= 0)  // 深さ制限
                || (maxTableFactory() <= factory.size()))  {  // サイズ制限
            return table.eval;
        }
        //
        // 合法手を入手して評価値に替えたあと sorted してリストに返す
        List<EvalTeTableMinMax> minMaxList = table.table.createChild(
                (x,te)->
                    // cloneを使って変更されないようにコピーを取る
                    findTable(x, te, baseLevel))
                .stream() // streamを取る
                .sorted()  // 評価値でソート
                .toList(); // リストに戻す
        boolean orNode = table.table.getSide() == 0;  //true:先手, false:後手
        float lowValue = orNode ? EngineExplorerMinMax.MIN_VALUE : EngineExplorerMinMax.MAX_VALUE;
        //
        if (minMaxList.isEmpty()) {  // 詰んだ
            return lowValue;
        }
        if (TableDefine.checkWinTeTableList(minMaxList)) {  // 勝利した
            return lowValue;
        }
        //
        float value = lowValue;
        int nowSun = 0;
        for (EvalTeTableMinMax forTable : minMaxList) {
            nowSun++; // 幅チェック
            float score = alphaBeta(
                    forTable, baseLevel + 1, depth - 1,
                    alpha, beta, false, displayList);
            forTable.eval = score;
            checkNoChange(forTable, baseLevel, isFirst, orNode, displayList);
            if (orNode) { // orノード
                value = Math.max(value, score);
                alpha = Math.max(alpha, value);
                if (beta < alpha) {  // βカット
                    break;
                }
                if ((score == EngineExplorerMinMax.MAX_VALUE)) {
                    break;  // 勝利なので終了
                }
            } else {
                value = Math.min(value, score);
                beta = Math.min(beta, value);
                if (beta < alpha) {  // αカット
                    break;
                }
                if ((score == EngineExplorerMinMax.MIN_VALUE)) {
                    break;  // 勝利なので終了
                }
            }
            //
            // 割り込みチェックを最後にすることで、必ず１回は通させる
            if (this.isInterruptOrTimeout()
                    || (maxNoChange() <= nowNoChange)
                    || (maxSon() < nowSun)) {
                break;
            }
        }
        return value;
    }

    public void checkNoChange(
            EvalTeTableMinMax forTable, int baseLevel, boolean isFirst, boolean orNode,
            List<TeTable> displayList) {
        if (!isFirst) { // 次に指す手だけを考える
            return;
        }
        if ((bestMove == null)
                || (orNode && (bestMove.eval < forTable.eval))
                || (!orNode && (forTable.eval < bestMove.eval))) {
            nowNoChange = 0; // 変化がある
            bestMove = forTable; // best moveを差し替える
            //
            if (displayList != null) {
                displayList.add(bestMove);  // 選ばれた候補を順番に乗せる
            }
            //
            // 候補として途中結果を入れておく
            this.setNow(new EngineResponse(
                    baseLevel, bestMove.te, EngineEval.class,
                    displayList, bestMove.eval));
            //
        } else {
            nowNoChange++;  // 変化がない
        }
    }


    /**
     * テーブルの評価値を得る
     * @param table テーブル
     * @param te 手
     * @param nowLevel 現在手数
     * @return テーブルの評価値
     */
    public EvalTeTableMinMax findTable(Table table, int te, int nowLevel) {
        if (! this.queueIsEmpty()) {
            return null; // 新しい要求が来たのであれば以降処理はしない
        }
        EvalTeTableMinMax value = factory.get(table);  // 工場から取って来る
        if (value == null) {
            Table cloneTable = table.clone();  // factoryに突っ込む前に変更されないようcloneを取る
            float eval = this.tensoInterface.runCompiledModel(cloneTable);  // 評価値に替える
            value = new EvalTeTableMinMax(cloneTable, te, eval, nowLevel);
            factory.put(cloneTable, value);
        } else {
            if ((te == TableDefine.WIN) && (value.te != TableDefine.WIN)) {
                // factoryの値が想定と違うので差し替える
                // WIN値以外は、数手先で同じ盤面がきたときの指し手が違う場合がありえるので替えないこと！
                Table cloneTable = table.clone();  // factoryに突っ込む前に変更されないようcloneを取る
                value = new EvalTeTableMinMax(cloneTable, TableDefine.WIN, value.eval, nowLevel);
                factory.put(cloneTable, value);
            }
        }
        return value;
    }
}
