package com.github.tand0.andshogio.tab;
import static com.github.tand0.andshogio.util.TableDefine.BEAT;
import static com.github.tand0.andshogio.util.TableDefine.ENEMY;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.lifecycle.ViewTreeViewModelStoreOwner;

import com.github.tand0.andshogio.MainViewModel;
import com.github.tand0.andshogio.engin.EngineManual;
import com.github.tand0.andshogio.engin.EngineRequest;
import com.github.tand0.andshogio.engin.EngineResponse;
import com.github.tand0.andshogio.util.EvalTeTable;
import com.github.tand0.andshogio.util.ExplorerTable;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/**
 * 局面情報の view
 */
public class AndCanvasView extends View {

    /** ペイント情報 */
    private final Paint paint = new Paint();

    /** 入力番号 */
    private int dispMode = 0;

    /** 移動前の X 位置 */
    private float x0 = -200.0f;

    /** 移動前の Y 位置 */
    private float y0 = -200.0f;

    /** 移動後の X 位置 */
    private float x1 = -200.0f;

    /** 移動後の Y 位置 */
    private float y1 = -200.0f;

    /** 画面の大きさの最大値：x軸 */
    private float xMax;

    /** 画面の大きさの最大値：y軸 */
    private float yMax;

    /** X 軸の最大値 */
    private static float xRMax = 14f;

    /** Y 軸の最大値 */
    private static final float yRMax = 10f;

    /** 成るかフラグ */
    private boolean isPromoteFlag = true;

    /** エンジンへの要求 */
    private EngineRequest engineRequest = null;

    /** evalTeTable の指定ポート */
    private int evalTeTablePos = 0;

    /** evalTeTable 情報 */
    private List<TeTable> evalTeTableList = null;

    /**
     * コンストラクタ
     * @param context Context
     */
    public AndCanvasView(Context context) {
        this(context, null);
    }

    /** コンストラクタ
     *
     * @param context Context
     * @param attrs Attribute Set
     */
    public AndCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * This is called when the view is attached to a window.  At this point it
     * has a Surface and will start drawing.  Note that this function is
     * guaranteed to be called before onDraw(android.graphics.Canvas),
     * however it may be called any time before the first onDraw -- including
     * before or after onMeasure(int, int).
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //
        LifecycleOwner lifecycleOwner = ViewTreeLifecycleOwner.get(this);
        if (lifecycleOwner == null) {
            return;
        }
        // ここでUIの更新を行う（例: invalidate() や setText() など）
        MainViewModel viewModel = getViewModel();
        if (viewModel != null) {
            viewModel.getCanvasTeTable().observe(lifecycleOwner, this::updateCanvasTeTable);
            viewModel.getEvalTeTableList().observe(lifecycleOwner, this::updateEvalTeTableList);
        }
    }

    /**
     * This is called when the view is detached from a window.  At this point it
     * no longer has a surface for drawing.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //
        MainViewModel viewModel = getViewModel();
        if (viewModel != null) {
            viewModel.getCanvasTeTable().removeObserver(this::updateCanvasTeTable);
            viewModel.getEvalTeTableList().removeObserver(this::updateEvalTeTableList);
        }
    }
    /**
     * View Modelの取得
     * @return View Model
     */
    protected MainViewModel getViewModel() {
        MainViewModel viewModel = null;
        // 親のActivityやFragmentのOwnerを取得
        ViewModelStoreOwner owner = ViewTreeViewModelStoreOwner.get(this);
        if (owner != null) {
            // ViewModelを取得
            viewModel = new ViewModelProvider(owner).get(MainViewModel.class);
        }
        return viewModel;
    }

    /**
     * 描画
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //
        xMax = (float)getWidth();
        yMax = (float)getHeight();
        xRMax = (dispMode != 1) ? 14.0f : 18.0f;
        if (xMax / xRMax < yMax / yRMax) {
            yMax = xMax * yRMax / xRMax;
        } else {
            xMax = yMax * xRMax / yRMax;
        }
        //
        this.onDrawTable(canvas);
        //
        if ((dispMode == 1) && (this.evalTeTableList != null)) {
            this.onDrawGraph(canvas);
        }
    }
    /**
     * メイン画面の表示
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDrawTable(@NonNull Canvas canvas) {
        paint.setStrokeWidth(2);
        //
        if ((-100 <= x0) && (-100 <= y0)) {
            // 打つ元を決める
            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.FILL);
            float xPos = xMax * (2 + (8 - x0)) / xRMax;
            float yPos = yMax * (1 + y0) / yRMax;
            canvas.drawRect(
                    xPos, yPos,
                    xPos + xMax / xRMax, yPos + yMax / yRMax, paint);
        }
        if ((-100 <= x1) && (-100 <= y1)) {
            // 打つ先を決める
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.FILL);
            float xPos = xMax * (2 + (8 - x1)) / xRMax;
            float yPos = yMax * (1 + y1) / yRMax;
            canvas.drawRect(
                    xPos, yPos,
                    xPos + xMax / xRMax, yPos + yMax / yRMax, paint);
        }
        //
        // 枠線を引く
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        for (int x = 2 ; x < 12 ; x++) {
            canvas.drawLine(
                    xMax * x / xRMax, yMax / yRMax,
                    xMax * x / xRMax, yMax, paint);
        }
        for (int y = 1 ; y < 11 ; y++) {
            canvas.drawLine(
                    xMax * 2 / xRMax, yMax * y / yRMax,
                    xMax * 11 / xRMax, yMax * y / yRMax, paint);
        }
        //
        float size = Math.min(xMax /xRMax , yMax /yRMax);
        paint.setColor(Color.BLACK);
        paint.setTextSize(size - 4);
        String[] num = {"９", "８", "７", "６", "５", "４", "３", "２", "１"};
        for (int x = 0 ; x < num.length ; x++) {
            canvas.drawText(num[x], (xMax * (x + 2)) / xRMax, yMax * 1 / yRMax -8, paint);
        }
        String[] kanStr = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        for (int y = 0 ; y < kanStr.length ; y++) {
            canvas.drawText(kanStr[y], (xMax * 11) / xRMax, yMax * (2+y) / yRMax -8, paint);
        }
        //
        canvas.drawText(isPromoteFlag ? "成" : "不", 0, yMax * 10 / yRMax -8, paint);
        //
        canvas.drawText("" + dispMode, xMax / xRMax, yMax * 10 / yRMax -8, paint);
        //
        MainViewModel viewModel = this.getViewModel();
        if (viewModel == null) {
            return; // view モデルが無いので処理できない
        }
        TeTable teTable = viewModel.getCanvasTeTable().getValue();
        if (teTable == null) {
            return; // tableが無いので置かなくてよい
        }
        canvas.drawText(teTable.table.getTeban() == 0 ? "先手" : "後手", 0, yMax * 9 / yRMax -8, paint);
        //
        //
        paint.setColor(Color.BLACK);
        for (byte y = TableDefine.pP ; y < TableDefine.pK ; y++) {
            int piece0 = teTable.table.getTegoma(y,0);
            int piece1 = teTable.table.getTegoma(y,1);
            // 先手側
            if (piece0 != 0) {
                canvas.drawText(TableDefine.getKomaToKanji(y) + piece0, (xMax * 12) / xRMax, yMax * (3 + y) / yRMax - 8, paint);
            }
            // 後手側
            if (piece1 != 0) {
                canvas.drawText(TableDefine.getKomaToKanji(y) + piece1, 0, yMax * (1 + y) / yRMax - 8, paint);
            }
        }
        // 先手の局面描写
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        onDrawTableOne(canvas, teTable, 0, size, xMax, yMax);
        // 後手の局面描写
        canvas.save(); // 画面の反転の保存
        canvas.rotate(180f, xMax/2, yMax/2); // 反転
        onDrawTableOne(canvas, teTable, 1, size, xMax, yMax);
        canvas.restore(); // 反転戻し
    }

    /** テーブルを描く
     *
     * @param canvas キャンバス
     * @param teTable 手テーブル
     * @param senGo 0:先手、1:後手
     * @param size フォントサイズ
     * @param xMax x最大
     * @param yMax y最大
     */
    protected void onDrawTableOne(Canvas canvas, TeTable teTable, int senGo, float size, float xMax, float yMax) {
        byte pieceMask = (senGo == 0) ? 0 : (byte)0x20;
        for (int x = 0 ; x < TableDefine.B_MAX ; x++) {
            for (int y = 0; y < TableDefine.B_MAX; y++) {
                byte piece = teTable.table.getKoma(x, y);
                if (((piece & 0x20) == pieceMask) && (piece != 0)) { // 後手で空白除く
                    if ((piece & 0x10) == 0) { // 成りか？
                        paint.setColor(Color.BLACK);
                    } else {
                        paint.setColor(Color.RED);
                    }
                    paint.setTextSize(size - 4);
                    String kanji = TableDefine.getKomaToKanji(piece & 0x1F);
                    onDrawTableKoma(canvas, kanji, senGo, x, y, size, xMax, yMax);
                }
            }
        }
    }

    /**
     * コマ１個分の描写
     * @param canvas キャンバス
     * @param kanji 出力メッセージ
     * @param x x位置
     * @param y y位置
     * @param senGo  0:先手、1:後手
     * @param size フォントサイズ
     * @param xMax x最大
     * @param yMax y最大
     */
    public void onDrawTableKoma(Canvas canvas, String kanji, int senGo, int x, int y, float size, float xMax, float yMax) {
        if (1 < kanji.length()) {
            paint.setTextSize((size / 2) - 4);
        } else {
            paint.setTextSize(size - 4);
        }
        if (senGo == 0) {
            canvas.drawText(kanji, xMax * (10 - x) / xRMax, yMax * (y + 2) / yRMax - 8, paint);
        } else {
            canvas.drawText(kanji, xMax * (x + xRMax - 11) / xRMax, yMax * (9 - y) / yRMax - 8, paint);
        }
    }

    /**
     * サブ画面の表示
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDrawGraph(@NonNull Canvas canvas) {
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE); // 塗りつぶす色
        paint.setStyle(Paint.Style.FILL); // 塗りつぶしモードに設定
        float x0 = xMax * 14 / xRMax;
        float xMax = (float) getWidth();
        float y0 = yMax / yRMax;
        float yMax = (float) getHeight();
        //
        // グラフを白く塗る
        canvas.drawRect(x0, 0f, xMax, yMax, paint);
        //
        // ボタン部分の線を轢く
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x0, y0, xMax, y0, paint);
        //
        // 中央の線を引く
        float xCenter = ((xMax - x0)/2) + x0;
        canvas.drawLine(xCenter, 0f, xCenter, yMax, paint);
        //
        // 現在の pos の横線を引く
        paint.setColor(Color.GREEN);
        int iMax = this.evalTeTableList.size();
        float yPos = (evalTeTablePos * (yMax - y0) / iMax) + y0;
        canvas.drawLine(x0, yPos, xMax, yPos, paint);
        //
        // 評価値を表示する
        float eval = 0.5f;
        float xOld = eval * (xMax - x0) + x0;
        float yOld = y0;
        paint.setColor(Color.RED);
        for (int i = 0 ; i < iMax; i++) {
            TeTable teTable = evalTeTableList.get(i);
            if (teTable instanceof EvalTeTable) {
                eval = ((EvalTeTable)teTable).eval;
            } else if (teTable instanceof ExplorerTable) {
                eval = ((ExplorerTable)teTable).getEval();
            }
            float x1 = eval * (xMax - x0) + x0;
            float y1 = (i * (xMax - y0) / iMax) + y0;
            //
            canvas.drawLine(xOld, yOld, x1, y1, paint);
            //
            xOld = x1;
            yOld = y1;
        }
    }

    /**
     * Call this view's OnClickListener, if it is defined.  Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @return True there was an assigned OnClickListener that was called, false
     *         otherwise is returned.
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Implement this method to handle pointer events.
     * <p>
     * This method is called to handle motion events where pointers are down on
     * the view. For example, this could include touchscreen touches, stylus
     * touches, or click-and-drag events from a mouse. However, it is not called
     * for motion events that do not involve pointers being down, such as hover
     * events or mouse scroll wheel movements.
     * <p>
     * If this method is used to detect click actions, it is recommended that
     * the actions be performed by implementing and calling
     * performClick(). This will ensure consistent system behavior,
     * including:
     * <ul>
     * <li>obeying click sound preferences
     * <li>dispatching OnClickListener calls
     * <li>handling AccessibilityNodeInfo#ACTION_CLICK ACTION_CLICK when
     * accessibility features are enabled
     * </ul>
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        MainViewModel viewModel = this.getViewModel();
        if (dispMode == 1) {
            float xL = ((int) (-event.getX() * xRMax / xMax)) + 2 + 8;
            float yL = ((int) (event.getY() * yRMax / yMax)) - 1;
            //
            if ((xL <= -4.0f)
                    && (this.evalTeTableList != null)
                    && (!this.evalTeTableList.isEmpty())) {
                int max = this.evalTeTableList.size();
                if (yL <= -1.0f) {
                    // ボタンを押した
                    if (-5.0f <= xL) {
                        evalTeTablePos = Math.max(0, evalTeTablePos - 1);
                    } else {
                        evalTeTablePos = Math.clamp(evalTeTablePos + 1, 0, max);
                    }
                } else {
                    // テーブル線に手を入れた
                    evalTeTablePos = (int)
                            (max * (event.getY() - (yMax / yRMax))
                                    / (getHeight() - (yMax / yRMax)));
                }
                viewModel.appendLog("pos=" + evalTeTablePos + " max=" + max);
                evalTeTablePos = max <= evalTeTablePos ? max - 1 : evalTeTablePos;
                TeTable teTable = this.evalTeTableList.get(evalTeTablePos);
                viewModel.setCanvasTeTable(teTable);
                invalidate();
                return true;
            }
        }
        TeTable teTable = viewModel.getCanvasTeTable().getValue();
        if (teTable == null) {
            return false;
        }
        int tableTurn = -1;
        List<TeTable> x = viewModel.getTeTableList().getValue();
        if (x != null) {
            tableTurn = x.size();
        }
        performClick();
        //
        if ((-100 < x0) && (-100 < x1)) {
            clearXY(); // 両方に値が入っていたらクリアする
        } else {
            // 値を取得する
            x1 = x0;
            y1 = y0;
            x0 = ((int) (-event.getX() * xRMax / xMax)) + 2 + 8;
            y0 = ((int) (event.getY() * yRMax / yMax)) - 1;
            //
            if ((x0 == 10) && (y0 == 8)) {
                isPromoteFlag = !isPromoteFlag;
                clearXY(); // クリアする
            } else if ((x0 == 9) && (y0 == 8)) {
                // 表示モードを替える
                dispMode = (dispMode + 1) % 2;
                clearXY(); // クリアする
            } else if (((x1 == x0) && (y1 == y0))
                    || (x0 == -1)
                    || (x0 == -3)
                    || (x0 == 9)
                    || (y0 == -1) ) {
                clearXY();
            } else if (x1 < -100) {
                // x0 のみ設定されているとき
                //
                int myTurn = teTable.table.getTeban();
                if (x0 == 10) { // 後手の手のコマ
                    byte piece = (byte)(y0 + 1);
                    if ((myTurn == 0) || (teTable.table.getTegoma(piece,myTurn) <= 0)) {
                        x0 = -200.0f;
                        y0 = -200.0f;
                    }
                } else if (x0 == -2) { // 先手の手のコマ
                    byte piece = (byte)(y0 - 1);
                    if ((myTurn != 0) || (teTable.table.getTegoma(piece,myTurn) <= 0)) {
                        x0 = -200.0f;
                        y0 = -200.0f;
                    }
                } else {
                    // 局面上から選択する
                    byte piece = teTable.table.getKoma((int)x0, (int)y0);
                    if ((piece == TableDefine.pNull)
                            || ((piece & 0x20) == ENEMY) == (myTurn == 0)) {
                        // 空白 or 持ちコマでない
                        x0 = -200.0f;
                        y0 = -200.0f;
                    }
                }
            } else {
                // x0, x1 とも設定されているとき
                if ((0 <= x0) && (x0 < 9) && (0 <= y0) && (y0 <= 9)) {
                    // 局面上から選択する
                    int myTurn = teTable.table.getTeban();
                    byte piece = teTable.table.getKoma((int)x0, (int)y0);
                    if ((piece != TableDefine.pNull)
                            && ((piece & 0x20) == ENEMY) != (myTurn == 0)) {
                        // 空白 or 持ちコマでない
                        x0 = -200.0f;
                        y0 = -200.0f;
                    } else {
                        //
                        int targetX1 = (int)x1;
                        int targetY1 = (int)y1;
                        if (x1 == 10) { // 後手の手のコマ
                            piece = (byte)(y1 + 1);
                            targetX1 = BEAT;
                            targetY1 = BEAT;
                        } else if (x1 == -2) { // 先手の手のコマ
                            piece = (byte) (y1 - 1);
                            targetX1 = BEAT;
                            targetY1 = BEAT;
                        } else {
                            piece = teTable.table.getKoma(targetX1,targetY1);
                        }
                        //
                        // 成りを考えないとまずい……
                        boolean promoteYes = false;
                        boolean promoteNot = false;
                        //
                        for (TeTable child : teTable.table.createChild()) {
                            int oldY = TableDefine.changeTeToOldY(child.te);
                            int oldX = TableDefine.changeTeToOldX(child.te);
                            int newY = TableDefine.changeTeToNewY(child.te) ;
                            int newX = TableDefine.changeTeToNewX(child.te);
                            byte oldPiece = TableDefine.changeTeToKoma(child.te);
                            if ((targetX1 == oldX)
                                    && (targetY1 == oldY)
                                    && ((int)x0 == newX)
                                    && ((int)y0 == newY)
                                    && ((oldPiece & 0xF) == (piece & 0xF))) {
                                if ((oldPiece & TableDefine.NARI) == 0) {
                                    promoteNot = true;
                                } else {
                                    promoteYes = true;
                                }
                            }
                            if (promoteNot && promoteYes) {
                                break;
                            }
                        }
                        if (promoteNot || promoteYes) {
                            // ヒットした
                            if (promoteNot && promoteYes) {
                                // 不成も成りも選択できる場合は 成るか？フラグにする
                                promoteYes = isPromoteFlag;
                            }
                            piece = promoteYes ? (byte) (piece | TableDefine.NARI) : piece;
                            piece = promoteYes ? (byte) (piece | TableDefine.NARI) : piece;
                            int te = TableDefine.changeTeToInt(piece, targetX1, targetY1, (int) x0, (int) y0);
                            if (engineRequest != null) {
                                EngineResponse res = new EngineResponse(
                                        tableTurn ,te, EngineManual.class,
                                        null, 0.5f);
                                try {
                                    engineRequest.forResult().send(res);
                                } catch (InterruptedException e) {
                                    // EMPTY
                                }
                                engineRequest = null;
                            } else {
                                // 手動でテーブル更新
                                viewModel.setCanvasTeTable(new TeTable(new Table(teTable.table, te), te));
                            }
                        } else {
                            clearXY();
                        }
                    }
                } else {
                    clearXY();
                }
            }
        }
        //
        viewModel.appendLog("now x0=" + x0 + " y0=" + y0 + " x1=" + x1 + " y1=" + y1);
        // 更新
        invalidate();
        //
        return true;
    }

    /** クリアする */
    public void clearXY() {
        x0 = -200.0f;
        y0 = -200.0f;
        x1 = -200.0f;
        y1 = -200.0f;
    }
    /** 将棋サーバからの局面情報の更新
     * @param teTable 表示するテーブル
     */
    public void updateCanvasTeTable(TeTable teTable) {
        //
        if (teTable == null) {
            return;
        }
        int te = teTable.te;
        if ((te == TableDefine.WIN) || (te == TableDefine.LOS)) {
            clearXY();
        } else {
            y1 = (int) (te & 0x1FL);
            x1 = (int) ((te >> 6) & 0x1FL);
            if (y1 == BEAT) { // 打った
                int piece = (te >> (6 * 4)) & 0x3F;
                int myTurn = ((piece & ENEMY) == 0) ? 0 : 1;
                piece = piece & 0xF; // ENEMY/成り フラグも取る
                x1 = (myTurn == 0) ? -2 : 10;
                y1 = (myTurn == 0) ? piece + 1: piece - 1;
            }
            y0 = (int) ((te >> (6 * 2)) & 0x1FL);
            x0 = (int) ((te >> (6 * 3)) & 0x1FL);
        }
        //
        // GUIを更新する
        this.invalidate();
    }

    /**
     * 評価関数のリストを取得する
     * @param evalTeTableList teTableList
     */
    public void updateEvalTeTableList(List<TeTable> evalTeTableList) {
        this.evalTeTablePos = 0;
        this.evalTeTableList = evalTeTableList;
        //
        dispMode = 1; // 表示をevalTeTable表示モードにする
        //
        // GUIを更新する
        this.invalidate();
    }

    /** 手動指しの設定
     *
     * @param engineRequest engine request
     */
    public void updateEngineRequest(EngineRequest engineRequest) {
        this.engineRequest = engineRequest;
    }
}
