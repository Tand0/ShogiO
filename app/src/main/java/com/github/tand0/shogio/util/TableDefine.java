package com.github.tand0.shogio.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** テーブル定義 */
public interface TableDefine {

    /** 投了の文字列 */
    String STRING_LOS = "%TORYO";

    /** 勝った場合の文字列 */
    String STRING_WIN = "%KACHI";

    /** 引き分けの文字列 */
    String STRING_DRAW = "%HIKIWAKE";

    /** CSA形式ファイル */
    String EXTENSION_CSA = "csa";

    /** 定跡ファイル */
    String EXTENSION_DB = "db";

    /** 空白 */
    byte pNull = 0;
    /** 歩 */
    byte pP = 1; // 歩
    /** 香 */
    byte pL = 2; // 香
    /** 桂馬 */
    byte pN = 3; // 桂
    /** 銀 */
    byte pS = 4; // 銀
    /** 金 */
    byte pG = 5; // 金
    /** 角 */
    byte pB = 6; // 角
    /** 飛車 */
    byte pR = 7; // 飛
    /** 王 */
    byte pK = 8; // 玉
    //

    /** 後手 歩 */
    byte pp = Table.addSideKoma(pP, 1); // 歩

    /** 後手 香車 */
    byte pl = Table.addSideKoma(pL, 1); // 香

    /** 後手 桂馬 */
    byte pn = Table.addSideKoma(pN, 1); // 桂

    /** 後手 銀 */
    byte ps = Table.addSideKoma(pS, 1); // 銀

    /** 後手 角 */
    byte pb = Table.addSideKoma(pB, 1); // 角

    /** 後手 飛車 */
    byte pr = Table.addSideKoma(pR, 1); // 飛

    /** 後手 金 */
    byte pg = Table.addSideKoma(pG, 1); // 金

    /** 後手 玉 */
    byte pk = Table.addSideKoma(pK, 1); // 玉

    /** 升の最大値 */
    int B_MAX = 9;

    /** moveで持ちコマを打った時の oldXY 値 */
    int BEAT = 9;

    /**
     * 勝ったとき。１一空白はteとして置けないので、
     * この値を勝った時のキーとして使う。
     * 主にすでに詰ませている状態の te として使う。
     */
    int WIN = Table.changeTeStringToInt("+0000* ");

    /**
     * 負けたとき、0を負けた時のキーとして使う。
     * 主に合法手が０の時のte として使う。
     */
     int LOS = 0;


    /**
     * USIの文字をkomaに変換する
     * @param at USIの文字
     * @return コマ情報
     */
    static byte getUsiKomaToKoma(char at) {
        return switch (at) {
            // 先手
            case 'P' -> pP;
            case 'L' -> pL;// 香
            case 'N' -> pN;// 桂
            case 'S' -> pS;// 銀
            case 'G' -> pG;// 金
            case 'B' -> pB;// 角
            case 'R' -> pR;// 飛
            case 'K' -> pK;// 玉
            //
            // 後手
            case 'p' -> pp;
            case 'l' -> pl;// 香
            case 'n' -> pn;// 桂
            case 's' -> ps;// 銀
            case 'g' -> pg;// 金
            case 'b' -> pb;// 角
            case 'r' -> pr;// 飛
            case 'k' -> pk;// 玉
            //
            // 分からん
            default -> pNull;
        };
    }

    /** 指定されたデータの出力
     *
     * @param file ファイル
     * @param stream stream
     * @param eval セッション
     * @param learn true:学習用にデータを読み込む場合(最後勝者が分からないとデータ全捨てします)、
     *              false:データをとにかく吸出したいとき
     * @param rate 0:レート関係なし、1以上:レートを超えるものは計算しない
     * @throws IOException 例外
     */
    static void runFile(
            String file, InputStream stream, List<EvalTeTable> eval, boolean learn, int rate)
            throws IOException {
        String extension = getExtension(file);
        //
        if (extension.equals(EXTENSION_CSA)) {
            // ファイル１個分の処理
            TableDefine.runFileCsaStream(eval, learn, stream, rate);
        } else if (extension.equals(EXTENSION_DB)) {
            // ファイル１個分の処理
            TableDefine.runFileDbStream(stream, eval);
        }
        //
    }

    /** 拡張子の取得
     * @param fileName もとになるファイル
     * @return 拡張子
     */
    static String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        // 最後のドットの位置を取得
        int lastDotPosition = fileName.lastIndexOf(".");
        // ドットが存在し、かつそれが文字列の先頭や末尾でない場合
        if (lastDotPosition != -1 && lastDotPosition < fileName.length() - 1) {
            return fileName.substring(lastDotPosition + 1);
        }
        return ""; // 拡張子がない場合
    }

    /**
     * ファイルストリーム全体の処理
     * @param eval 受信した学習情報
     * @param learn true:学習用にデータを読み込む場合(最後勝者が分からないとデータ全捨てします)、
     *              false:データをとにかく吸出したいとき
     * @param is ファイル情報
     * @param rate 0:レートは無視する、1以上:レート以下は無視する
     * @throws IOException IO 例外
     */
    static void runFileCsaStream(
            List<EvalTeTable> eval, boolean learn, InputStream is, int rate)
            throws IOException {
        int sameSum = 0;
        List<TeTable> nextList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String str;
            float blackRate = 0.0f;
            float whiteRate = 0.0f;
            // 最初の局面
            TeTable teTable = new TeTable(new Table(null, 0), 0);
            while ((str = br.readLine()) != null) {
                if (str.isEmpty()) {
                    continue;
                }
                char ch = str.charAt(0);
                if ((ch == 'V') || (ch == '$')
                        || (ch == 'T') || (ch == 'N')
                        || (ch == '\u0000')) {
                    continue;
                }
                if (ch == '%') {
                    // %TIME_UP
                    // %SENNICHITE
                    // %TORYO
                    // %KACHI
                    break;
                }
                if (0 == str.indexOf("'black_rate:")) {
                    blackRate = getRate(str);
                    continue;
                }
                if (0 == str.indexOf("'white_rate:")) {
                    whiteRate = getRate(str);
                    continue;
                }
                if (ch == '\'') {
                    continue;
                }
                if (teTable.table.setForCSAProtocol(str)) {
                    if ((ch == '+') || (ch == '-')) {
                        nextList.addLast(teTable);
                    }
                    continue;
                }
                if ((ch == '+') || (ch == '-')) {
                    int te = Table.changeTeStringToInt(str);
                    Table nextTable = new Table(nextList.getLast().table, te);
                    TeTable nextTeTable = new TeTable(nextTable, te);
                    nextList.addLast(nextTeTable); //打った手を保存
                    continue;
                }
                System.out.println("unkown=(" + str + ")");
            }
            // レートフィルタ
            if ((rate != 0) && (blackRate <= rate) && (whiteRate <= rate)) {
                // rate が指定されている場合で、
                // かつ、先後両方ともが rate が指定値以下の時は ignore する
                return;
            }
        }
        //
        // 勝敗判定：
        boolean stopCheck = true;
        int winLos = -1; // -1:引き分け、0先手勝ち、1:後手勝ち
        for (int i = nextList.size() - 1; 0 <= i; i--) {
            // 棋譜の後ろから前に渡っての検索
            TeTable item = nextList.get(i);
            final long[] memento = item.table.createUndo(); // cloneを作って戻せるようにする
            AllBitboard all = new AllBitboard(item.table);
            if (stopCheck) {
                // 入玉と勝ち負けチェック
                //
                // 手番
                int side = item.table.getSide();
                //
                // 先手が入玉している
                int res = item.table.isKingWin(0, all);
                if (res == 0) {
                    // 先手は入玉していないので、後手が入玉しているかチェック
                    res = item.table.isKingWin(1, all);
                }
                if (res != 0) {
                    // どちらが入玉勝ちを宣言したら後手勝ち扱いにする
                    winLos = 1;
                } else if (i != nextList.size() - 1) {
                    // 入玉勝ち宣言されず、かつ、
                    // 2回目以降のループなので winLos の確定は不要
                    stopCheck = false; // 遡り不要
                } else {
                    // 入玉勝ち宣言されず、かつ
                    // 最初のループなので winLos の確定が必要
                    stopCheck = false; // 遡り不要
                    //
                    // 詰み/勝ちのチェックが始まる
                    int te = LOS;
                    te = item.table.iterate(te, memento, all);
                    item.table.setUndo(memento); // 元に戻す
                    if (te == LOS) {
                        winLos = side;
                    } else if (te == WIN) {
                        winLos = 1 - side;
                    } else {
                        if ((!learn) && (rate != 0)) {
                            // 学習モードでない、かつ、rateも0でない
                            // 学習せず skip
                            return;
                        }
                        winLos = -1;
                    }
                }
            }
            long winMax = (i == nextList.size() - 1) ? 1L : 10L;
            long losMax = (i == nextList.size() - 1) ? 0L : 1L;
            long winLong = (winLos == -1) ? 0 : (winLos == 0) ? winMax : losMax;
            long losLong = (winLos == -1) ? 0 : (winLos == 0) ? losMax : winMax;
            //
            eval.addFirst(new EvalTeTable(item.table, item.te, winLong, losLong));
        }
        //
        if (winLos == -1) {
            return; // 引き分けの場合、同一盤面チェックはしない
        }
        //
        // 盤面が同じ局面がある場合、以降の勝率を下げる
        EvalTeTable x;
        int i = 0;
        for ( ; i < eval.size(); i++) {
            x = eval.get(i);
            if ((x.win == 1) || (x.los == 1)) {
                break;
            }
            for (int j = i + 1; j < eval.size(); j++) {
                EvalTeTable y = eval.get(j);
                if ((y.win == 1) || (y.los == 1)) {
                    break;
                }
                if (x.table.equalsTableOnly(y.table)) {
                    //
                    // まずはコマの増減をチェックする
                    for (byte koma = TableDefine.pR; TableDefine.pP <= koma; koma--) {
                        // 大ゴマから増減チェックする
                        int target = y.table.getTegoma(koma, 0)
                                - x.table.getTegoma(koma, 0);
                        if (target != 0) {
                            if (sameSum == 0) {
                                sameSum = target;
                            } else if (((sameSum < 0) && (0 < target))
                                    || ((0 < sameSum) && (target < 0))) {
                                sameSum = 0;
                                break;
                            }
                        }
                    }
                }
                if (sameSum != 0) {
                    break;  // hitしたのでブレークする
                }
            }
            if (sameSum != 0) {
                break;  // hitしたのでブレークする
            }
        }
        if (sameSum == 0) {
            return;  // 同じ盤面でかつ、一方敵にコマが減っているものがなかった
        }
        // hit した場合ここにくる
        //
        // 状況：
        //   sameSum が正のとき、先手のコマが増えた。⇒先手有利
        //   sameSum が負のとき、先手のコマが減った。⇒先手不利
        //
        // やりたいこと:
        // 同一局面で先手勝ち(y.win==3)のとき、先手より有利(0<sameSum)なら、同一局面以前のwin値を下げる
        // 同一局面で先手勝ち(y.win==3)のとき、先手より不利(sameSum<0)なら、同一局面以降のwin値を下げる
        // 同一局面で後手勝ち(y.los==3)のとき、先手より有利(0<sameSum)なら、同一局面以降のlos値を下げる
        // 同一局面で後手勝ち(y.los==3)のとき、先手より不利(sameSum<0)なら、同一局面以前のlos値を下げる
        boolean isAfter = (winLos == 0) ? 0 < sameSum : 0 > sameSum;
        List<EvalTeTable> evalOld = new ArrayList<>(eval);
        eval.clear();
        for (int j = 0; j < evalOld.size() ; j++) {
            EvalTeTable y = evalOld.get(j);
            if ((y.win == 1) || (y.los == 1)) {
                eval.add(y);
            } else if ((i + 1 <= j) ^ isAfter) {
                eval.add(y);
            } else {
                long win = y.win;
                win = 1 < win ? win - 1 : win;
                long los = y.los;
                los = 1 < los ? los - 1 : los;
                eval.add(new EvalTeTable(y.table, y.te, win, los));
            }
        }
    }


    /**
     * レートの文字列を数値に変更する
     * @param str レートの文字列
     * @return 数値化されたレート
     */
    static float getRate(String str) {
        // 'black_rate:Kristallweizen-Core2Duo-P7450+81dc9bdb52d04dc20036dbd8313ed055:3780.0
        // 'white_rate:gikou2_1c+4849c06b4665cfdb26a86eaa9439dd53:3300.0
        int index = str.lastIndexOf(':');
        float result = 0.0f;
        if (0 <= index) {
            str = str.substring(index + 1);
            result = Float.parseFloat(str);
        }
        return result;
    }

    /**
     * ファイル一つ分の処理。
     * @param eval 評価情報
     * @param is ファイル情報
     * @throws IOException 例外
     */
    static void runFileDbStream(InputStream is, List<EvalTeTable> eval) throws IOException {
        //
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            Table table = new Table(null, 0);
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // 空文
                }
                char ch = line.charAt(0);
                if ((ch == '#') || (ch == '\uFEFF')) {
                    continue; // コメント
                }
                if (line.contains("sfen")) {
                    table = new Table(line);
                    try {
                        //
                        new TableKey(table); // キーにできるか？
                        //
                    } catch (UnsupportedOperationException e) {
                        return; // 異常終了
                    }
                    //
                    // 定跡は勝ち負けありの５割とする
                    eval.add(new EvalTeTable(table,0,1000L,1000L));
                    //
                } else {
                    // 指し手チェック
                    runFileDbStream(table, line, eval);
                }
            }
        }
    }

    /**
     * 指し手チェック
     * @param table 初期局面
     * @param line 指し手
     * @param eval 結果
     */
    static void runFileDbStream(
            Table table, String line, List<EvalTeTable> eval) {
        for (String word : line.split("\\s+")) {
            if ((word.length() < 4) || word.equals("none")) {
                return; // 終了
            }
            // 手が指される
            int te = table.changeUsiTeToInt(word);
            if (te != 0) {
                table = new Table(table, te);
                try {
                    //
                    new TableKey(table); // キーにできるか？
                    //
                } catch (UnsupportedOperationException e) {
                    return; // 異常終了
                }
                // 定跡は勝ち負けなしの５割とする
                eval.add(new EvalTeTable(table,te,100L,100L));
            }
        }
    }

    /**
     * bad move のリストを作る
     * @param base データベース情報
     * @param table テーブル情報
     * @return オブジェクト
     */
    static BadMoveResult getBadMoveList(MainDatabase base, Table table) {
        int myTurn = table.getSide(); // 0:先手、1:後手
        List<TeTable> teTableList = table.createChild();
        if (teTableList.isEmpty()) {
            // 詰んでいる
            return null;
        }
        if (TableDefine.checkWinTeTableList(teTableList)) {
            // 詰ませている
            return null;
        }
        //
        // 合法手が詰みでも詰まされてもない
        List<TeTable> badTeTableList = new ArrayList<>();
        //
        float targetBadRate = myTurn == 0 ? Float.MAX_VALUE : Float.MIN_VALUE;
        boolean hitFlag = false;
        for (TeTable teTable : teTableList) {
            TableKey baseTableKey = new TableKey(teTable.table);
            long[] result = base.getData(baseTableKey.getKey());
            if (result != null) {
                // データベースに存在する場合
                long baseWin = result[0];
                long baseLos = result[1];
                float baseRate = EvalTeTable.getWinLosToRate(baseWin, baseLos);
                if ((baseRate <= 0.001f) || (0.999 <= baseRate)) {
                    continue; // 上限に近いものは他さしても同じようなものなので無視する
                }
                hitFlag = true;
                targetBadRate = (myTurn == 0)
                        ? Math.min(baseRate,targetBadRate) // 先手の場合、先手最低勝率を探す
                        : Math.max(baseRate,targetBadRate); // 後手の場合、先手最高勝率を探す
            } else {
                // データベースに存在しない場合は加算候補
                if (checkFreeKoma(teTable.table)) {
                    // ただで取られるコマがあるなら加算する
                    badTeTableList.add(teTable);
                }
            }
        }
        if (! hitFlag) { // データベースに参考となる評価値が一つもない
            badTeTableList.clear(); // 学習しなくて良い
        }
        float rate = 2.0f;
        if (myTurn == 0) {
            // 先手の場合、rate=1に近づくほど良い手、0に近づくほど悪手
            // 先手の場合、悪手として rate を 1/2 させたものを学習させる
            targetBadRate = targetBadRate / rate;
        } else {
            // 後手の場合、rate=0に近づくほど良い手、1に近づくほど悪手
            // 後手の場合、悪手として rate を 1/2 せたものを学習させる
            targetBadRate = ((rate - 1.0f)/rate) + (targetBadRate/ rate);
        }
        //
        // rateを適当に win losに変換する
        long targetWin = (long) (10000 * targetBadRate);
        long targetLos = (long) (10000 * (1 - targetBadRate));
        //
        return new BadMoveResult(badTeTableList, targetWin, targetLos);
    }

    /**
     * ただで取られるコマがあるならtrueを返す
     * @param table 局面
     * @return True: ただで取られるコマがある, False: ただで取られるコマはない
     */
     static boolean checkFreeKoma(Table table) {
        InputGen inputGen = new InputGen();
        inputGen.updateTable(table);
        //
        for (int x = 0 ; x < TableDefine.B_MAX ; x++ ) {
            for (int y = 0 ; y < TableDefine.B_MAX ; y++ ) {
                byte koma = table.getKoma(x,y);
                if ((koma == 0) || (0 != Table.isSideKoma(koma))) {
                    continue;
                }
                float ans = inputGen.get(x, y, InputGen.DANGER_CHANNEL)
                        - inputGen.get(x, y, InputGen.DANGER_CHANNEL + 1);
                if (ans < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * eval move のリストを作る
     * @param base データベース情報
     * @param table テーブル情報
     * @param win 先手勝ち数
     * @param los 後手負け数
     * @return オブジェクト
     */
    static EvalMoveResult getEvalMoveList(MainDatabase base, Table table, long win, long los) {
        //
        // 足切り倍率
        final long BORDER = 100;
        //
        List<TeTable> childList = table.createChild();
        int side = table.getSide();
        if (childList.isEmpty()) {
            win = side; // 手番が0なら先手が詰み
            los = 1 - side; // 手番が1なら後手が詰み
            return new EvalMoveResult(win, los);
        }
        if (TableDefine.checkWinTeTableList(childList)) {
            win = 1 - side; // 手番が先手なら先手勝ち
            los = side; // 手番が後手なら後手勝ち
            return new EvalMoveResult(win, los);
        }
        //
        long maxWinLos = 0;
        List<EvalTeTable> evalList = new ArrayList<>();
        for (TeTable teTable : childList) {
            TableKey teTableKey = new TableKey(teTable.table);
            long[] result = base.getData(teTableKey.getKey());
            if (result == null) {
                continue;
            }
            long w = result[0];
            long l = result[1];
            long wl = w + l;
            maxWinLos = Math.max(wl, maxWinLos);
            evalList.add(new EvalTeTable(teTable.table, teTable.te, result[0], result[1]));
        }
        if (evalList.isEmpty()) {
            // データがない場合は元の win/los を返す
            return new EvalMoveResult(win, los);
        }
        //
        // 足切り倍率で除算
        long border = maxWinLos / BORDER;
        EvalTeTable best = null;
        for (EvalTeTable eTable : evalList) {
            long wl = eTable.win + eTable.los;

            // 足切り
            if (wl < border) {
                continue;
            }

            // 最良値探索（Comparable を使わず自前で比較）
            if (best == null || eTable.compareTo(best) < 0) {
                best = eTable;
            }
        }
        if (best != null) {
            win = best.win;
            los = best.los;
        }
        return new EvalMoveResult(win, los);
    }

    /** 勝っているかをチェックする
     * @param teTableList teTable のリスト
     * @return True:勝っている, False:勝っていない
     */
    static boolean checkWinTeTableList(List<? extends TeTable> teTableList) {
        return (teTableList.size() == 1) && (teTableList.getFirst().te == TableDefine.WIN);
    }
}
