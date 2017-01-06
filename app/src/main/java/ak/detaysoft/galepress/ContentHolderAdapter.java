package ak.detaysoft.galepress;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;
import ak.detaysoft.galepress.util.ProgressWheel;

/**
 * Created by adem on 13/01/14.
 */

public class ContentHolderAdapter extends BaseAdapter  {
    private LibraryFragment libraryFragment;

    public ContentHolderAdapter(LibraryFragment activity) {
        this.libraryFragment = activity;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public int getCount() {
        return libraryFragment.getContents().size();
    }

    public Object getItem(int position) {
        return libraryFragment.getContents().get(position);
    }

    public long getItemId(int position) {
        return ((L_Content)libraryFragment.getContents().get(position)).getId();
    }

    public class ViewHolder
    {
        public LinearLayout detailLayout;
        public ImageView coverImageView;
        public TextView nameLabel;
        public TextView detailLabel;
        public TextView monthLabel;
        public ProgressWheel progressBar;
        public ImageView overlay;
        public CustomPulseProgress loading;
        public L_Content content;

        public void refreshImageLoading(){
            displayImage(true, coverImageView, loading, content.getSmallCoverImageDownloadPath(), content);
        }

        public void progressUpdate(long total, long fileLength){
            if(progressBar.getVisibility() != View.VISIBLE){
                progressBar.setVisibility(View.VISIBLE);
            }

            if(overlay.getVisibility() != View.VISIBLE){
                overlay.setVisibility(View.VISIBLE);
            }
            progressBar.setProgress((int)((total*360)/fileLength));
            progressBar.setText("%"+(int) (total * 100 / fileLength));
        }

    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder viewHolder;
        final L_Content content = (L_Content) libraryFragment.getContents().get(position);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ViewHolder();
            convertView = libraryFragment.getLayoutInflater().inflate(R.layout.grid_cell, parent, false);
            viewHolder.detailLayout = (LinearLayout)convertView.findViewById(R.id.detailLayout);
            viewHolder.coverImageView= (ImageView)convertView.findViewById(R.id.coverImage);
            viewHolder.monthLabel = (TextView)convertView.findViewById(R.id.monthLabel);
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.detailLabel = (TextView)convertView.findViewById(R.id.detailLabel);
            viewHolder.progressBar = (ProgressWheel)convertView.findViewById(R.id.progress_bar);
            viewHolder.overlay = (ImageView) convertView.findViewById(R.id.grid_download_overlay);
            viewHolder.loading = (CustomPulseProgress)convertView.findViewById(R.id.grid_image_loading);
            viewHolder.loading.startAnim();
            //viewHolder.loading.setIndeterminate(true);
            //viewHolder.loading.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        ((RelativeLayout)viewHolder.detailLayout.getParent()).setBackgroundColor(ApplicationThemeColor.getInstance().getCoverImageBackgroundColor());
        viewHolder.nameLabel.setTextColor(ApplicationThemeColor.getInstance().getLibraryItemTextColor());
        viewHolder.detailLabel.setTextColor(ApplicationThemeColor.getInstance().getLibraryItemTextColor());
        viewHolder.monthLabel.setTextColor(ApplicationThemeColor.getInstance().getLibraryItemTextColorWithAlpha(50));
        viewHolder.overlay.setBackgroundColor(ApplicationThemeColor.getInstance().getDownloadProgressColorWithAlpha(70));

        viewHolder.progressBar.setTextColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        viewHolder.progressBar.setBarColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        viewHolder.progressBar.setRimColor(ApplicationThemeColor.getInstance().getReverseThemeColorWithAlpha(20));
        viewHolder.progressBar.setContourColor(Color.TRANSPARENT);

        File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());
        if(coverImageFile.exists()){
            displayImage(false, viewHolder.coverImageView, viewHolder.loading, "file://"+coverImageFile.getPath(), content);
        } else if (content.getSmallCoverImageDownloadPath() != null){
            displayImage(true, viewHolder.coverImageView, viewHolder.loading, content.getSmallCoverImageDownloadPath(), content);
        } else {
            Log.e("imageDisplayed", "noimage");
        }

        viewHolder.nameLabel.setText(content.getName());
        viewHolder.nameLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikMedium(libraryFragment.getActivity()));
        viewHolder.detailLabel.setText(content.getDetail());
        viewHolder.detailLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(libraryFragment.getActivity()));
        viewHolder.monthLabel.setText(content.getMonthlyName());
        viewHolder.monthLabel.setTypeface(ApplicationThemeColor.getInstance().getRubikRegular(libraryFragment.getActivity()));
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.overlay.setVisibility(View.GONE);

        boolean downloaded = content.isPdfDownloaded();
        boolean updateAvailable = content.isPdfUpdateAvailable();
        boolean downloading = content.isPdfDownloading() && GalePressApplication.getInstance().getDataApi().downloadPdfTask !=null && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content !=null && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content.getId().compareTo(content.getId()) == 0;

        if(downloaded){
            // Content is downloaded and ready to view.
            if(updateAvailable){

                if(downloading){
                    // update downloading
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.overlay.setVisibility(View.VISIBLE);

                }
            }
            else{
                // update not available
            }
        }
        else{
            // not downloaded
            if(downloading){
                // Content is not downloaded but downloading

                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.overlay.setVisibility(View.VISIBLE);
            }
            else{
                // Content Download edilmemis. ilk acildigi durum.
            }
        }
        viewHolder.content = content;

        return convertView;
    }

    public void displayImage(final boolean isDownload, final ImageView image, final CustomPulseProgress loading, String imagePath, final L_Content content){
        DisplayImageOptions displayConfig;
        if (isDownload) {
            displayConfig = new DisplayImageOptions.Builder()
                .showImageOnFail(ApplicationThemeColor.getInstance().paintIcons(libraryFragment.getActivity(), ApplicationThemeColor.INTERNET_CONNECTION_ERROR))
                    .cacheInMemory(true).build();
        } else {
            displayConfig = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).build();
        }

        ImageLoader.getInstance().displayImage(imagePath, image, displayConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                image.setImageBitmap(null);
                loading.setVisibility(View.VISIBLE);
                loading.startAnim();
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                loading.setVisibility(View.GONE);
                if (isDownload)
                    GalePressApplication.getInstance().getDataApi().saveImage(bitmap, content.getCoverImageFileName(), content.getId(), false);
                else if (content.getRemoteCoverImageVersion() < content.getCoverImageVersion())
                    GalePressApplication.getInstance().getDataApi().downloadUpdatedImage(content.getSmallCoverImageDownloadPath()
                            , content.getCoverImageFileName()
                            , content.getId(), false);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                loading.setVisibility(View.GONE);
            }
        });
    }


}