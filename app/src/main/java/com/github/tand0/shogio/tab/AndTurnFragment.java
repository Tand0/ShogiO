package com.github.tand0.shogio.tab;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.tand0.shogio.MainActivity;
import com.github.tand0.shogio.MainViewModel;
import com.github.tand0.shogio.R;
import com.github.tand0.shogio.engin.SendListener;
import com.github.tand0.shogio.util.TeTable;

import java.util.List;

/** 指し手の一覧 */
public class AndTurnFragment extends Fragment {
    /** 一覧用の view */
    private ListView listView;

    /** アダプタ */
    AndTurnAdapter adapter;

    /** コンストラクタ */
    public AndTurnFragment() {
        this(R.layout.fragment_turn);
    }

    /** コンストラクタ
     * @param contentLayoutId content Layout Id
     */
    public AndTurnFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    /** 検索用 view id の取得
     *
     * @return 検索用 view id
     */
    public int getFindViewById() {
        return R.id.listview_turn;
    }

    /**
     * view モデルから表示すべきリストを得る
     * @param viewModel view モデル
     * @return 表示すべきリスト
     */
    public LiveData<List<? extends TeTable>> getTeTableList(MainViewModel viewModel) {
        return viewModel.getTeTableList();
    }

    /**
     * on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * on view create
     * @param view The View returned
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainViewModel viewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        getTeTableList(viewModel)
                .observe(getViewLifecycleOwner(), this::updateTeTableList);
        //
        listView = view.findViewById(getFindViewById());
        Context context = requireContext();
        adapter = new AndTurnAdapter(
                context, R.layout.item_turn);
        listView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //
        // 作ったオブザーバを解放する
        MainViewModel viewModel = new ViewModelProvider(
                requireActivity()).get(MainViewModel.class);
        getTeTableList(viewModel).removeObserver(this::updateTeTableList);
    }
    /** 将棋サーバからの局面情報の更新
     *
     * @param teTableList 将棋サーバからの局面情報
     */
    public void updateTeTableList(List<? extends TeTable> teTableList) {
        MainActivity act = (MainActivity)requireActivity();
        SendListener forGui = act.getForGui();
        //
        // 出力結果をリストビューに表示
        adapter.update(teTableList, forGui, listView);
    }
}
