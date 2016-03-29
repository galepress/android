package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;

public class MuPDFPageAdapter extends BaseAdapter {
	private final Context mContext;
	private final FilePicker.FilePickerSupport mFilePickerSupport;
	private final MuPDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
	private       Bitmap mSharedHqBm;

	public MuPDFPageAdapter(Context c, FilePicker.FilePickerSupport filePickerSupport, MuPDFCore core) {
		mContext = c;
		mFilePickerSupport = filePickerSupport;
		mCore = core;
	}

	public int getCount() {
		return mCore.countPages();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public void releaseBitmaps()
	{
		//  recycle and release the shared bitmap.
		if (mSharedHqBm!=null)
			mSharedHqBm.recycle();
		mSharedHqBm = null;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final MuPDFPageView pageView;
		if (convertView == null) {
			if (mSharedHqBm == null || mSharedHqBm.getWidth() != parent.getWidth() || mSharedHqBm.getHeight() != parent.getHeight()) {

				/*
				* Eger parent view init edilmemisse(herzaman olmuyor) burada ekran boyutuna gore bitmap olusturuluyor. (MG)
				* (https://fabric.io/galepress/android/apps/ak.detaysoft.carrefoursa1/issues/56b278fcf5d3a7f76b8f6164)
				* (https://fabric.io/galepress/android/apps/ak.detaysoft.carrefoursa1/issues/56d219e3f5d3a7f76b28a8da)
				* (https://fabric.io/galepress/android/apps/ak.detaysoft.galepressviewer/issues/56f6ac6cffcdc0425028e988)
				* */
				if(parent.getWidth() <= 0 || parent.getHeight() <= 0) {
					Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					int width = size.x;
					int height = size.y;
					mSharedHqBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				} else {
					mSharedHqBm = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
				}
			}


			pageView = new MuPDFPageView(mContext, mFilePickerSupport, mCore, new Point(parent.getWidth(), parent.getHeight()), mSharedHqBm);
		} else {
			pageView = (MuPDFPageView) convertView;
		}

		PointF pageSize = mPageSizes.get(position);
		if (pageSize != null) {
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position);
			AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
				@Override
				protected PointF doInBackground(Void... arg0) {
					return mCore.getPageSize(position);
				}

				@Override
				protected void onPostExecute(PointF result) {
					super.onPostExecute(result);
					// We now know the page size
					mPageSizes.put(position, result);
					// Check that this view hasn't been reused for
					// another page since we started
					if (pageView.getPage() == position)
						pageView.setPage(position, result);
				}
			};

			sizingTask.execute((Void)null);
		}
		return pageView;
	}

}
