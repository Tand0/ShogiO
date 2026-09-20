package com.github.tand0.andshogio.tab;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.tand0.andshogio.MainViewModel;
import com.github.tand0.andshogio.R;

/**
 * ログ用のフラグメント
 */
public class AndLogFragment extends Fragment {


    /**
     * コンストラクタ
     */
    public AndLogFragment() {
        super(R.layout.fragment_log);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** ログオブザーバ */
    private Observer<String> logObserver;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.logObserver = message -> {
            //
            // テキストを設定する
            TextView textView = view.findViewById(R.id.text_log);
            textView.setText(message);
            //
            // 最下部までスクロールさせる
            ScrollView scrollView = view.findViewById(R.id.scroll_view);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        };
        viewModel.getLog().observe(getViewLifecycleOwner(), logObserver);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //
        // 作ったオブザーバを解放する
        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getLog().removeObserver(this.logObserver);
    }
}