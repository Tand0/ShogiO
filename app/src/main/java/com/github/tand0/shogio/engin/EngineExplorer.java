package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.ExplorerTable;
import com.github.tand0.shogio.util.TensoInterface;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** 探索エンジン */
public class EngineExplorer extends Engine {
    /** レイヤインタフェース */
    private final TensoInterface tensoInterface;

    /** ダメなテーブルのリスト */
    private final Set<TeTable> topOpBadTableList;

    /** 学習する最大局面数 */
    private int maxTableFactory = 100000;

    /** 指定回数変更がなければ終了 */
    private int maxNoChange = 10;

    /** 読みの深さ */
    private int maxLevel = 3;

    /** 現在の最大読み深さ */
    private int nowMaxLevel = 0;


    /** 読みの最小幅 */
    private int minSon = 3;

    /** 変更フラグ */
    private boolean changeFlag;

    /** 変更フラグの取得
     * @return 変更フラグ
     */
    public boolean changeFlag() {
        return this.changeFlag;
    }
    /** 変更フラグの設定
     * @param changeFlag 変更フラグ
     */
    public void changeFlag(boolean changeFlag) {
        this.changeFlag = changeFlag;
    }

    /** 学習する最大局面数 の設定
     * @param maxTableFactory 学習する最大局面数
     */
    public void maxTableFactory(int maxTableFactory) {
        this.maxTableFactory = maxTableFactory;
    }
    /** 指定回数 の設定
     * @param maxNoChange 指定回数
     */
    public void maxNoChange(int maxNoChange) {
        this.maxNoChange = maxNoChange;
    }
    /** 読みの深さ の設定
     * @param maxLevel 読みの深さ
     */
    public void maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /** 読みの最小幅 の設定
     * @param minSon 読みの最小幅
     */
    public void minSon(int minSon) {
        this.minSon = minSon;
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
    public EngineExplorer(TensoInterface tensoInterface, Set<TeTable> topOpBadTeableSet) {
        super();
        this.tensoInterface = tensoInterface;
        this.topOpBadTableList = topOpBadTeableSet;
    }

    /** 最大局数に到達した */
    public final static String DISPLAY_MAX_TABLE_FACTORY = "explorer maxTableFactory";

    /** 先手勝ち */
    public final static String DISPLAY_MATE_MAX = "explorer MATE_MAX";

    /** 先手負け */
    public final static String DISPLAY_MATE_MIN = "explorer MATE_MIN";

    /** 正常終了した */
    public final static String DISPLAY_NO_CHANGE = "explorer changeFlag";

    /** 中断した */
    public final static String DISPLAY_IS_END = "explorer isEnd";

    /** 変化なしがずっと続いた */
    public final static String DISPLAY_MAX_CHANGE = "explorer maxNoChange";

    @Override
    public void createNow(EngineRequest r) {
        //
        if ((r == null) || (r.teTableList().isEmpty())) {
            return; // r が入ってなければ終了
        }
        TeTable lastTable = r.teTableList().getLast();
        List<TeTable> displayList = r.display() ? new ArrayList<>() : null;
        ExplorerTable next = new ExplorerTable(lastTable);
        ExplorerTable target = null;
        //
        // 同一局面外し用の HashSet
        HashMap<Table, TeTable> factory = new HashMap<>();
        for (TeTable teTable : topOpBadTableList) {
            factory.put(teTable.table, teTable);
        }
        //
        // 同一手回避用の HashSet
        HashSet<Table> stack = new HashSet<>();
        //
        int noChange = 0;
        try {
            stack.add(next.table);
            //
            nowMaxLevel = 0;
            changeFlag = true;
            while ((factory.size() < maxTableFactory)
                    && (next.getEval() != ExplorerTable.MATE_MAX)
                    && (next.getEval() != ExplorerTable.MATE_MIN)
                    && (!isEnd())
                    && (noChange <= maxNoChange)
                    && changeFlag) {
                changeFlag = false;
                calculateNode(0, next, factory, stack);
                if ((next.getList() == null) || next.getList().isEmpty()) {
                    break;
                }
                ExplorerTable best = next.getList().getFirst();
                if (! best.equals(target)) {
                    // 最良の値に変化があるのでtimeoutに対応できるよに setNow() を差し替える
                    target = best;
                    if (r.display()) {
                        displayList.clear(); // 使いまわすのでクリア
                        // もとに戻す手を先頭の乗せる
                        displayList.addFirst(new TeTable(lastTable.table,TableDefine.LOS));
                        // リストを載せる
                        displayList.addAll(next.getList());
                    }
                    this.setNow(new EngineResponse(
                            r.turn(), target.te, EngineDatabase.class,
                            displayList, target.getEval()));
                    noChange = 0; // no change をクリアする
                } else {
                    noChange++;
                }

            }
        } finally {
            try {
                // 動作した原因出力
                if (factory.size() >= maxTableFactory) {
                    r.forResult().send(new GUIMessage(DISPLAY_MAX_TABLE_FACTORY
                            + " l=" + nowMaxLevel
                            + " f=" + factory.size()));
                }
                if (next.getEval() == ExplorerTable.MATE_MAX) {
                    r.forResult().send(new GUIMessage(DISPLAY_MATE_MAX));
                }
                if (next.getEval() == ExplorerTable.MATE_MIN) {
                    r.forResult().send(new GUIMessage(DISPLAY_MATE_MIN));
                }
                if (isEnd()) {
                    r.forResult().send(new GUIMessage(DISPLAY_IS_END));
                }
                if (noChange > maxNoChange) {
                    r.forResult().send(new GUIMessage(DISPLAY_MAX_CHANGE
                            + " l=" + nowMaxLevel
                            + " f=" + factory.size()));
                }
                if (!changeFlag) {
                    // 探索終了 (深さと数を表示)
                    r.forResult().send(new GUIMessage(DISPLAY_NO_CHANGE
                            + " l=" + nowMaxLevel
                            + " f=" + factory.size()));
                }
            } catch (InterruptedException e) {
                // 停止信号が出ているので終わる
                // EMPTY
            }
            for (TeTable work : factory.values()) {
                if (work instanceof ExplorerTable) {
                    ((ExplorerTable)work).clearList();
                }
            }
            factory.clear();
            stack.clear();
        }
    }

    /** PNDNの計算実際部分
     *
     * @param level 探索深さ
     * @param t 現局面
     * @param factory 工場。同一局面は同一の PnDnを使う
     * @param stack スタック。同一局面を処理するのを防ぐ
     */
    public void calculateNode(
            int level, ExplorerTable t,
            HashMap<Table, TeTable> factory,
            HashSet<Table> stack) {
        nowMaxLevel = Math.max(nowMaxLevel, level);
        if ((maxLevel <= level)
                || (t.getEval() == ExplorerTable.MATE_MIN)
                || (t.getEval() == ExplorerTable.MATE_MAX)
                || isEnd()
                || (t.isExpand() && t.getList().isEmpty())) {
            return; // これ以上の計算不要
        }
        //
        if (! t.isExpand()) {
            // 展開されていない
            calculateNodeExpand(level, t, factory);
        } else {
            // 展開している
            calculateNodeNotExpand(level, t, factory, stack);
        }
    }

    /** PNDNの計算実際部分(展開する場合)
     * @param level レベル
     * @param t 現局面
     * @param factory 工場。同一局面は同一の PnDnを使う
     */
    public void calculateNodeExpand(
            int level,
            ExplorerTable t,
            HashMap<Table, TeTable> factory) {
        //
        changeFlag(true); // 展開があるかぎりループする
        //
        List<TeTable> teTableList = t.table.createChild();
        if (teTableList.isEmpty()) {
            // target が null ということは、値が一つもない＝お前はもう詰んでいる
            t.setEval((t.table.getTeban() == 0)
                    ? ExplorerTable.MATE_MIN   // 先手の場合、後手勝ち
                    : ExplorerTable.MATE_MAX); // 後手の場合、先手勝ち
            // もう探索するものはないので空を渡す
            t.setList(Collections.emptyList());
            return;
        } else if (TableDefine.checkWinTeTableList(teTableList)) {
            // 既に詰ませている場合はこちら
            t.setEval((t.table.getTeban() == 0)
                    ? ExplorerTable.MATE_MAX   // 先手の場合、先手勝ち
                    : ExplorerTable.MATE_MIN); // 後手の場合、後手勝ち
            // 手を追加する
            t.setList(List.of(new ExplorerTable(teTableList.getFirst())));
            return;
        }
        // 展開されていないので以下で展開した葉に評価値を割り当てる
        List<ExplorerTable> inputList = new ArrayList<>();
        List<ExplorerTable> containList = new ArrayList<>();
        for (TeTable input : teTableList) {
            // factory にいたらfactoryのテーブルを使う
            TeTable targetTable = factory.get(input.table);
            //
            if (targetTable == null) {
                // factory にいなければ生成
                ExplorerTable explorerDnTable = new ExplorerTable(input);
                factory.put(input.table, explorerDnTable);
                inputList.add(explorerDnTable);
            } else if (targetTable instanceof ExplorerTable) {
                // factory にいて、かつ、悪手でなければ追加
                containList.add((ExplorerTable)targetTable);
            }
        }
        //
        // 評価値を計算する
        for (int i = 0; i < inputList.size(); i++) {
            ExplorerTable input = inputList.get(i);
            Float result = this.tensoInterface.runCompiledModel(input.table);
            //
            if (result != null) {
                input.setEval(result);
            }
        }
        // 合体させる
        containList.addAll(inputList);
        //
        if (! containList.isEmpty()) {
            // 枝を刈るためにソートする
            Collections.sort(containList);
            //
            // 最良値を与える
            t.setEval(containList.getFirst().getEval());
        }
        //
        int index = 0;
        boolean flag = true;
        Iterator<ExplorerTable> iterator = containList.iterator();
        int teban = t.table.getTeban();
        int oldSum = t.table.getTegomaNum(teban);
        while (iterator.hasNext()) {
            ExplorerTable et = iterator.next();
            index++;
            if ((minSon < index) //最小探索幅越で、かつ
                    && (oldSum == et.table.getTegomaNum(teban) //手コマが増えていない
                    && (!et.table.isOute(0)) // 先手に王手が掛かっていない
                    && (!et.table.isOute(1)))) { // 後手に王手が掛かっていない
                // 条件に合う場合に以降学習させない
                // 無視する場合でもfactoryには入るので入れる必要がある
                et.setList(Collections.emptyList());
                if (flag) {
                    flag = level <= 0; // 1こめはスキップ
                } else {
                    // 階層１以上の場合で１個覆えたらそれより前の評価は無視して良い
                    iterator.remove();
                }
            }
        }
        t.setList(containList); // 設定
        //
    }

    /** PNDNの計算実際部分(展開後の場合)
     *
     * @param level 探索深さ
     * @param t 現局面
     * @param factory 工場。同一局面は同一の PnDnを使う
     * @param stack スタック。同一局面を処理するのを防ぐ
     */
    public void calculateNodeNotExpand(
            int level,
            ExplorerTable t,
            HashMap<Table, TeTable> factory,
            HashSet<Table> stack) {
        //
        for (ExplorerTable target : t.getList()) {
            // 最高の target 順にチェックする
            stack.add(target.table); // スタックを詰む
            calculateNode(level + 1, target, factory, stack); // 子供を計算
            stack.remove(target.table); // スタックから外す
            if (changeFlag()) {
                break; // ヒットしたら終了
            }
        }
        //
        if (changeFlag()) {
            // changeFlag が立ったので、データが変わっている可能性がある
            // のでソートしなおす
            if (! t.getList().isEmpty()) {
                List<ExplorerTable> list = t.getList();
                Collections.sort(list);// 元のリストをソート（Comparable 実装済み前提）
                t.setList(list);// そのままセット
                // 最良値を更新する
                t.setEval(t.getList().getFirst().getEval());
            }
        } else if (0 < level) {
            // changeFlag が立っていない＝これ以上探索するものがないので
            // 調査する初期局面で無ければ
            // 自身に空を渡してこれ以上学習させない
            t.setList(Collections.emptyList());
        }
    }
}
