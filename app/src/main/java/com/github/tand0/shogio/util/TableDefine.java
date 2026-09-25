package com.github.tand0.shogio.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** テーブル定義 */
public interface TableDefine {

    /** 投了の文字列 */
    String STRING_LOS = "%TORYO";

    /** 勝った場合の文字列 */
    String STRING_WIN = "%KACHI";

    /** 引き分けの文字列 */
    String STRING_DRAW = "%HIKIWAKE";

    /** zip形式ファイル */
    String EXTENSION_ZIP = "zip";

    /** CSA形式ファイル */
    String EXTENSION_CSA = "csa";

    /** 定跡ファイル */
    String EXTENSION_DB = "db";

    /** 成りフラグ */
    byte NARI = 0x10; // 0b00010000

    /** 後手フラグ */
    byte ENEMY = 0x20; // 0b00100000

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
    /** と */
    byte ppP = (byte) (NARI | pP); // と
    /** 竜 */
    byte ppR = (byte) (NARI | pR); // 竜
    /** 馬 */
    byte ppB = (byte) (NARI | pB); // 馬
    /** 成り銀 */
    byte ppS = (byte) (NARI | pS); // 銀成
    /** 成り桂 */
    byte ppN = (byte) (NARI | pN); // 桂成
    /** 成り香 */
    byte ppL = (byte) (NARI | pL); // 香成

    /** 後手 歩 */
    byte pp = (byte) (ENEMY | pP); // 歩
    /** 後手 香車 */
    byte pl = (byte) (ENEMY | pL); // 香
    /** 後手 桂馬 */
    byte pn = (byte) (ENEMY | pN); // 桂
    /** 後手 銀 */
    byte ps = (byte) (ENEMY | pS); // 銀
    /** 後手 角 */
    byte pb = (byte) (ENEMY | pB); // 角
    /** 後手 飛車 */
    byte pr = (byte) (ENEMY | pR); // 飛
    /** 後手 金 */
    byte pg = (byte) (ENEMY | pG); // 金
    /** 後手 玉 */
    byte pk = (byte) (ENEMY | pK); // 玉

    /** 升の最大値 */
    int B_MAX = 9;

    /** moveで持ちコマを打った時の oldXY 値 */
    int BEAT = 9;

    /** コマを CSA文字に変換
     *
     * @param koma コマ
     * @return CSA文字
     */
    static String getKomaToString(int koma) {
        koma = koma & (~ENEMY);
        return switch (koma) {
            case pP -> "FU";// 歩
            case pL -> "KY";// 香
            case pN -> "KE";// 桂
            case pS -> "GI";// 銀
            case pG -> "KI";// 金
            case pB -> "KA";// 角
            case pR -> "HI";// 飛
            case pK -> "OU";// 玉
            case ppP -> "TO";// と
            case ppL -> "NY";// 香成
            case ppN -> "NK";// 桂成
            case ppS -> "NG";// 銀成
            case ppB -> "UM";// 馬
            case ppR -> "RY";// 竜
            default -> "* ";// 空白
        };
    }

    /** コマを 漢字に変換
     *
     * @param koma コマ
     * @return CSA文字
     */
    static String getKomaToKanji(int koma) {
        koma = koma & (~ENEMY);
        return switch (koma) {
            case pP -> "歩";// 歩
            case pL -> "香";// 香
            case pN -> "桂";// 桂
            case pS -> "銀";// 銀
            case pG -> "金";// 金
            case pB -> "角";// 角
            case pR -> "飛";// 飛
            case pK -> "王";// 玉
            case ppP -> "と";// と
            case ppL -> "香成";// 香成
            case ppN -> "桂成";// 桂成
            case ppS -> "銀成";// 銀成
            case ppB -> "馬";// 馬
            case ppR -> "竜";// 竜
            default -> "＊";// 空白
        };
    }

    /** CSA文字をコマに変換
     *
     * @param komaString CSA文字
     * @return コマ
     */
    static byte getStringToKoma(String komaString) {
        return switch (komaString.toUpperCase()) {
            case "FU" -> pP;
            case "KY" -> pL;// 香
            case "KE" -> pN;// 桂
            case "GI" -> pS;// 銀
            case "KI" -> pG;// 金
            case "KA" -> pB;// 角
            case "HI" -> pR;// 飛
            case "OU" -> pK;// 玉
            case "TO" -> ppP;// と
            case "NY" -> ppL;// 香成
            case "NK" -> ppN;// 桂成
            case "NG" -> ppS;// 銀成
            case "UM" -> ppB;// 馬
            case "RY" -> ppR;// 竜
            default -> pNull;
        };
    }

    /** 変更する
     *
     * @param te 手
     * @return CSA文字
     */
    static String changeTeIntToString(int te) {
        if (te == TableDefine.LOS) { // 次に指す手がない
            return STRING_LOS;
        } else if (te == TableDefine.WIN) { // 入玉勝ち
            return STRING_WIN; // 24点法を採用
        }
        StringBuilder buff = new StringBuilder();
        int koma = (te >> (6 * 4)) & 0x3F;
        buff.append(((koma & ENEMY) == 0) ? '+' : '-');
        int oldY = te & 0x1F;
        int oldX = (te >> 6) & 0x1F;
        if (oldY == BEAT) { // 打った
            buff.append('0');
            buff.append('0');
        } else {
            buff.append((char) ('1' + oldX));
            buff.append((char) ('1' + oldY));
        }
        int newY = (te >> (6 * 2)) & 0x1F;
        int newX = (te >> (6 * 3)) & 0x1F;
        buff.append((char) ('1' + newX));
        buff.append((char) ('1' + newY));
        buff.append(getKomaToString(koma));
        return buff.toString();
    }

    /** 変更する
     *
     * @param te 手
     * @return CSA文字
     */
    static String changeTeIntToKangiString(int te) {
        if (te == TableDefine.LOS) { // 次に指す手がない
            return "%TORYO";
        } else if (te == TableDefine.WIN) { // 入玉勝ち
            return "%KACHI";
        }
        int index;
        StringBuilder buff = new StringBuilder();
        int koma = (te >> (6 * 4)) & 0x3F;
        buff.append(((koma & ENEMY) == 0) ? "先手▲" : "後手△");
        int oldY = te & 0x1F;
        int newY = (te >> (6 * 2)) & 0x1F;
        int newX = (te >> (6 * 3)) & 0x1F;
        final String[] newXString = {"１","２","３","４","５","６","７","８","９","Ⅹ"};
        index = Math.min(newX, newXString.length - 1);
        buff.append(newXString[index]);
        final String[] newYString = {"一","二","三","四","五","六","七","八","九","Ⅹ"};
        index = Math.min(newY, newYString.length - 1);
        buff.append(newYString[index]);
        buff.append(getKomaToKanji(koma));
        if (oldY == BEAT) { // 打った
            buff.append("打");
        } else {
            buff.append("＿");
        }
        return buff.toString();
    }
    /** 変更する
     *
     * @param csaTe CSA文字
     * @return 手
     */
    static int changeTeStringToInt(String csaTe) {
        if (csaTe.length() < 7) {
            return 0;
        }
        int myTurn = csaTe.charAt(0) == '+' ? 0 : 1;
        String komaString = csaTe.substring(5, 7);
        int oldX = csaTe.charAt(1);
        int oldY = csaTe.charAt(2);
        if (oldY == '0') {
            oldX = BEAT;
            oldY = BEAT;
        } else {
            oldX = oldX - '1';
            oldY = oldY - '1';
        }
        int newX = csaTe.charAt(3) - '1';
        int newY = csaTe.charAt(4) - '1';
        byte koma = getStringToKoma(komaString);
        koma = (byte) (koma | (myTurn * ENEMY));
        return changeTeToInt(koma, oldX, oldY, newX, newY);
    }
    /** 変更する。
     * 入力を -3333FU,T12 としたとき、12秒になる。
     * @param csaTe CSA文字
     * @return 経過時間
     */
    static int changeTeStringToTime(String csaTe) {
        final Pattern pattern = Pattern.compile(",T(\\d+)");
        Matcher matcher = pattern.matcher(csaTe);
        if (! matcher.find()) {
            return 0;
        }
        String timeString = matcher.group(1);
        if (timeString == null) {
            return 0;
        }
        int time;
        try {
            time = Integer.parseInt(timeString);
        } catch(NumberFormatException e) {
            time = 0;
        }
        return time;
    }

    /** 変更する
     *
     * @param koma コマ
     * @param oldX 移動前x、打つ場合は BEAT
     * @param oldY 移動前y
     * @param newX 移動後x
     * @param newY 移動後y
     * @return 手
     */
    static int changeTeToInt(byte koma, int oldX, int oldY, int newX, int newY) {
        return oldY | (oldX << 6) | (newY << (6 * 2)) | (newX << (6 * 3)) | (koma << (6 * 4));
    }

    /**
     * 手から oldY を取得する
     * @param te 手
     * @return old Y
     */
    static int changeTeToOldY(int te) {
        return te & 0x1F;
    }

    /**
     * 手から oldX を取得する
     * @param te 手
     * @return old X
     */
    static int changeTeToOldX(int te) {
        return (te >>> 6) & 0x1F;
    }

    /**
     * 手から newY を取得する
     * @param te 手
     * @return new Y
     */
    static int changeTeToNewY(int te) {
        return (te >>> (6 * 2)) & 0x1F;
    }

    /**
     * 手から newX を取得する
     * @param te 手
     * @return new X
     */
    static int changeTeToNewX(int te) {
        return (te >>> (6 * 3)) & 0x1F;
    }

    /**
     * 手からコマを取得する
     * @param te 手
     * @return コマ
     */
    static byte changeTeToKoma(int te) {
        return (byte) ((te >>> (6 * 4)) & 0x3F);
    }

    /**
     * 勝ったとき。１一空白はteとして置けないので、
     * この値を勝った時のキーとして使う。
     * 主にすでに詰ませている状態の te として使う。
     */
    int WIN = changeTeStringToInt("+0000* ");

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
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String str;
            float blackRate = 0.0f;
            float whiteRate = 0.0f;
            //
            List<TeTable> nextList = new ArrayList<>();
            // 最初の局面
            TeTable teTable = new TeTable(new Table(null, 0),0);
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
                    int te = TableDefine.changeTeStringToInt(str);
                    Table nextTable = new Table(nextList.getLast().table,te);
                    TeTable nextTeTable = new TeTable(nextTable,te);
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
            //
            // 勝敗判定：
            boolean stopCheck = true;
            int winLos = -1; // -1:引き分け、0先手勝ち、1:後手勝ち
            for (int i = nextList.size() - 1;  0<= i; i--) {
                // 棋譜の後ろから前に渡っての検索
                TeTable item = nextList.get(i);
                if (stopCheck) {
                    // 入玉と勝ち負けチェック
                    //
                    // 手番
                    int teban = item.table.getTeban();
                    //
                    // 先手が入玉している
                    int res = item.table.isKingWin(
                            0, item.table.getSenteOuX(), item.table.getSenteOuY());
                    if (res == 0) {
                        // 先手は入玉していないので、後手が入玉しているかチェック
                        res = item.table.isKingWin(
                                1, item.table.getGoteOuX(), item.table.getGoteOuY());
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
                        List<TeTable> childList = item.table.createChild();
                        if (childList.isEmpty()) { // 詰み
                            winLos = teban;
                        } else if (TableDefine.checkWinTeTableList(childList)) { // 勝ち
                            winLos = 1 - teban;
                        } else { // 詰みか、勝ちの場合以外
                            if ((!learn) && (rate != 0)) {
                                // 学習モードでない、かつ、rateも0でない
                                // 学習せず skip
                                return;
                            }
                            winLos = -1;
                        }
                    }
                }
                int sum = (i == nextList.size() - 1) ? 1 : 2;
                long winLong = (winLos == -1) ? sum : (long) winLos * sum;
                long losLong = (winLos == -1) ? sum : (long) (1 - winLos) * sum;
                //
                eval.add(new EvalTeTable(item.table, item.te, winLong, losLong));
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
                    eval.add(new EvalTeTable(table,0,10L,10L));
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
        int myTurn = table.getTeban(); // 0:先手、1:後手
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
                if ((koma == 0) || (0 != (koma & TableDefine.ENEMY))) {
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
        int teban = table.getTeban();
        if (childList.isEmpty()) {
            win = teban; // 手番が0なら先手が詰み
            los = 1 - teban; // 手番が1なら後手が詰み
            return new EvalMoveResult(win, los);
        }
        if (TableDefine.checkWinTeTableList(childList)) {
            win = 1 - teban; // 手番が先手なら先手勝ち
            los = teban; // 手番が後手なら後手勝ち
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
    static boolean checkWinTeTableList(List<TeTable> teTableList) {
        return (teTableList.size() == 1) && (teTableList.getFirst().te == TableDefine.WIN);
    }
}
