package com.github.tand0.andshogio.tab;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.tand0.andshogio.MainActivity;
import com.github.tand0.andshogio.MainViewModel;
import com.github.tand0.andshogio.R;
import com.github.tand0.andshogio.engin.MessageConnectCloseRequest;
import com.github.tand0.andshogio.engin.SendListener;
import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/** 指し手の一覧 */
public class AndTurnFragment extends Fragment {
    /** 一覧用の view */
    private ListView listView;

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
    public LiveData<List<TeTable>> getTeTableList(MainViewModel viewModel) {
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
        listView.setOnItemClickListener(
                (parent, view1, position, id) -> {
            //
            TeTable teTable = (TeTable) parent.getItemAtPosition(position);
            //
            MainActivity act = (MainActivity)requireActivity();
            SendListener forGui = act.getForGui();
            //
            try {
                viewModel.getStatus().send(new MessageConnectCloseRequest());
                forGui.send(teTable);
                //
            } catch (InterruptedException e) {
                // EMPTY
            }
        });
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
    public void updateTeTableList(List<TeTable> teTableList) {
        Context context = requireContext();
        //
        // 出力結果をリストビューに表示
        AndTurnAdapter adapter = new AndTurnAdapter(context, R.layout.item_turn, teTableList);
        listView.setAdapter(adapter);
    }
}
