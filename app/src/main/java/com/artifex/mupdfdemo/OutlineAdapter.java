package com.artifex.mupdfdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

public class OutlineAdapter extends BaseAdapter {
	private final OutlineItem    mItems[];
	private final LayoutInflater mInflater;
    private Context mContext;
	public OutlineAdapter(Context context, LayoutInflater inflater, OutlineItem items[]) {
		mContext = context;
        mInflater = inflater;
		mItems    = items;
	}

	public int getCount() {
		return mItems.length;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.outline_entry, parent, false);
		} else {
			v = convertView;
		}
		int level = mItems[position].level;
		if (level > 8) level = 8;
		String space = "";
		for (int i=0; i<level;i++)
			space += "   ";
		((TextView)v.findViewById(R.id.title)).setText(space+mItems[position].title);
        ((TextView)v.findViewById(R.id.title)).setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        ((TextView)v.findViewById(R.id.title)).setTypeface(ApplicationThemeColor.getInstance().getRubikLight(mContext));

		((TextView)v.findViewById(R.id.page)).setText(String.valueOf(mItems[position].page + 1));
        ((TextView)v.findViewById(R.id.page)).setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        ((TextView)v.findViewById(R.id.page)).setTypeface(ApplicationThemeColor.getInstance().getRubikLight(mContext));
		return v;
	}

}
