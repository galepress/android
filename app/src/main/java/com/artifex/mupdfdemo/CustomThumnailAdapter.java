package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 26.04.2015.
 */
public class CustomThumnailAdapter extends ArrayAdapter<Object> {
    private static final String TAG = ThumbnailListAdapter.class
            .getSimpleName();
    private Context mContext;
    private MuPDFCore mCore;
    private MuPDFReaderView mDocView;
    public PointF mPreviewSize;
    private PointF mPageSize;
    private final SparseArray<Bitmap> mBitmapCache = new SparseArray<Bitmap>();
    private String mPath;
    public ArrayList<PointF> itemSizeList;
    public ArrayList<TextView> pageNumberList;

    public CustomThumnailAdapter(Context context, MuPDFCore core, MuPDFReaderView docView) {
        super(context, android.R.layout.simple_list_item_1, core.countPages());
        mContext = context;
        mDocView = docView;
        mCore = core;

        itemSizeList = new ArrayList<PointF>();
        for(int i = 0; i < mCore.countPages(); i++){
            mPreviewSize = new PointF();
            int defaultWidth = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.reader_bottom_preview_width);
            int defaultHeight = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.reader_bottom_preview_height);

            mPageSize = mCore.getPageSize(i);
            float pageScale = mPageSize.x / mPageSize.y;
            mPreviewSize.y = defaultHeight;
            if(mPageSize.x > mPageSize.y){
                mPreviewSize.x = mPreviewSize.y*pageScale;
            } else {
                mPreviewSize.x = mPreviewSize.y*pageScale;
            }

            itemSizeList.add(mPreviewSize);
        }

        pageNumberList = new ArrayList<TextView>();
        for(int i = 0; i < mCore.countPages(); i++)
            pageNumberList.add(new TextView(mContext));

        if(mCore.getDisplayPages() == 1){
            mPath = GalePressApplication.getInstance().getFilesDir() + "/"+mCore.getContent().getId()+"/previewCache/" + mCore.getContent().getPdfFileName()+"/";
        } else {
            mPath = GalePressApplication.getInstance().getFilesDir() + "/"+mCore.getContent().getId()+"/previewCache/" + mCore.getContent().getPdfFileName()+"/land/";
        }

        File mCacheDirectory = new File(mPath);
        if (!mCacheDirectory.exists())
            mCacheDirectory.mkdirs();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int count = mCore.countPages();
        return count;
    }
    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int pPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int pPosition) {
        // TODO Auto-generated method stub
        if(mCore.getDisplayPages() == 1)
            return pPosition;
        else{
            if(pPosition > 0)
                return pPosition;
            else
                return 0;
        }
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final View pageView;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pageView = inflater.inflate(R.layout.preview_pager_item_layout,
                parent, false);
        final ImageView mPreviewPageImageView = (ImageView) pageView
                .findViewById(R.id.PreviewPageImageView);
        mPreviewPageImageView.setImageResource(R.drawable.darkdenim3);
        TextView pageNumber = (TextView) pageView
                .findViewById(R.id.PreviewPageNumber);
        pageNumber.setText(String.valueOf(position + 1));
        pageNumber.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        pageNumber.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(mContext));
        pageNumberList.set(position, pageNumber);
        if(position == mDocView.getDisplayedViewIndex())
            pageNumber.setVisibility(View.VISIBLE);
        else
            pageNumber.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            pageNumber.setBackground(ApplicationThemeColor.getInstance().paintIcons(mContext, ApplicationThemeColor.READER_UCGEN));
        else
            pageNumber.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(mContext, ApplicationThemeColor.READER_UCGEN));

        ImageView divider = (ImageView) pageView
                .findViewById(R.id.reader_thumnail_divider);
        if(position + 1 != mCore.countPages()){
            divider.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeTranperentForegroundColor());
        } else {
            divider.setBackgroundColor(Color.TRANSPARENT);
        }


        mPreviewPageImageView.setLayoutParams(new RelativeLayout.LayoutParams((int) itemSizeList.get(position).x,
                (int) itemSizeList.get(position).y));
        mPreviewPageImageView.invalidate();
        mPreviewPageImageView.requestLayout();

        drawPageImageView(mPreviewPageImageView, position);

        pageView.setLayoutParams(new AbsListView.LayoutParams((int)itemSizeList.get(position).x+1, (int)itemSizeList.get(position).y));

        pageView.setId(position);
        return pageView;
    }

    private void drawPageImageView(final ImageView v, final int position) {
        ThumbnailSafeAsyncTask<Void, Void, Bitmap> drawTask = new ThumbnailSafeAsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... pParams) {
                Bitmap lq = getCachedBitmap(position);
                mBitmapCache.put(position, lq);
                return lq;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                v.setImageBitmap(result);
            }

        };
        Bitmap bmp = mBitmapCache.get(position);
        if (bmp == null)
            drawTask.safeExecute((Void) null);
        else
            v.setImageBitmap(bmp);
    }

    private Bitmap getCachedBitmap(int position) {
        String mCachedBitmapFilePath = mPath  + position
                + ".jpg";
        File mCachedBitmapFile = new File(mCachedBitmapFilePath);
        Bitmap lq = null;
        try {
            if (mCachedBitmapFile.exists() && mCachedBitmapFile.canRead()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inJustDecodeBounds = true;
                lq = BitmapFactory.decodeFile(mCachedBitmapFilePath, options);
                return lq;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // some error with cached file,
            // delete the file and get rid of bitmap
            mCachedBitmapFile.delete();
            lq = null;
        }
        if (lq == null) {
            lq = Bitmap.createBitmap((int)itemSizeList.get(position).x, (int)itemSizeList.get(position).y,
                    Bitmap.Config.ARGB_8888);
            mCore.drawPage(lq, position, (int)(itemSizeList.get(position).x), (int)(itemSizeList.get(position).y), 0,0,(int)(itemSizeList.get(position).x), (int)(itemSizeList.get(position).y) );
            try {
                lq.compress(Bitmap.CompressFormat.JPEG, 50, new FileOutputStream(
                        mCachedBitmapFile));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mCachedBitmapFile.delete();
            }
        }
        return lq;
    }

    public MuPDFReaderView getmDocView() {
        return mDocView;
    }
}
