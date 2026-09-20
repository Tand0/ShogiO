package com.github.tand0.andshogio.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tand0.andshogio.R;
import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/**
 * 棋譜を表示用のアダプタ
 */
public class AndTurnAdapter extends ArrayAdapter<TeTable> {
    /** リソース ID */
    private final int mResource;

    /** リストビューの要素のリスト */
    private final List<TeTable> mItems;

    /** GUI部品のレイアウト情報 */
    private final LayoutInflater mInflater;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソース ID
     * @param items リストビューの要素
     */
    public AndTurnAdapter(Context context, int resource, List<TeTable> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        //
        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }
        // リストビューに表示する要素を取得
        String result;
        TeTable item = mItems.get(position);
        result = item.display();
        //
        // タイトルを設定
        TextView title = view.findViewById(R.id.turn_title);
        title.setText(result);
        //
        return view;
    }
}
