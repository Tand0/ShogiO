package com.github.tand0.andshogio.tab;


import androidx.lifecycle.LiveData;

import com.github.tand0.andshogio.MainViewModel;
import com.github.tand0.andshogio.R;
import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/** 指し手の一覧 (評価ディスプレイ用) */
public class AndEvalFragment extends AndTurnFragment {

    /** コンストラクタ */
    public AndEvalFragment() {
        this(R.layout.fragment_eval);
    }
    /** コンストラクタ
     * @param contentLayoutId content Layout Id
     */
    public AndEvalFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    /** 検索用 view id の取得
     *
     * @return 検索用 view id
     */
    @Override
    public int getFindViewById() {
        return R.id.listview_eval;
    }

    /**
     * view モデルから表示すべきリストを得る
     * @param viewModel view モデル
     * @return 表示すべきリスト
     */
    @Override
    public LiveData<List<TeTable>> getTeTableList(MainViewModel viewModel) {
        return viewModel.getEvalTeTableList();
    }
}
