package ak.detaysoft.galepress;

import android.content.Intent;
import android.graphics.Bitmap;
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
        public ImageView overlay;
        public RelativeLayout downloadStatus;
        public TextView downloadPercentage;
        public CustomPulseProgress loading;
        public L_Content content;

        public void refreshImageLoading(){
            displayImage(true, coverImageView, loading, content.getSmallCoverImageDownloadPath(), content);
        }

    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder viewHolder;
        final L_Content content = (L_Content) libraryFragment.getContents().get(position);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ViewHolder();
            convertView = libraryFragment.getLayoutInflater().inflate(R.layout.grid_cell, null);
            viewHolder.detailLayout = (LinearLayout)convertView.findViewById(R.id.detailLayout);
            viewHolder.coverImageView= (ImageView)convertView.findViewById(R.id.coverImage);
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.detailLabel = (TextView)convertView.findViewById(R.id.detailLabel);
            viewHolder.downloadStatus = (RelativeLayout) convertView.findViewById(R.id.grid_download_status);
            viewHolder.downloadPercentage = (TextView) convertView.findViewById(R.id.grid_download_percentage);
            viewHolder.loading = (CustomPulseProgress)convertView.findViewById(R.id.grid_image_loading);
            viewHolder.overlay = (ImageView) convertView.findViewById(R.id.grid_download_overlay);
            viewHolder.loading.startAnim();
            //viewHolder.loading.setIndeterminate(true);
            //viewHolder.loading.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.downloadStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalePressApplication.getInstance().getDataApi().cancelDownload(false, libraryFragment.getActivity(), content);
            }
        });


        /*viewHolder.overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(libraryFragment.getActivity(), ContentDetailPopupActivity.class);
                intent.putExtra("content", content);
                intent.putExtra("animationStartX", 0.5f);
                intent.putExtra("animationStartY", 0.5f);
                libraryFragment.getActivity().startActivityForResult(intent, 103);
            }
        });*/
        viewHolder.detailLayout.setBackgroundColor(ApplicationThemeColor.getInstance().getHolderDetailBackround());
        viewHolder.nameLabel.setTextColor(ApplicationThemeColor.getInstance().getGridItemNameLabelColor());
        viewHolder.detailLabel.setTextColor(ApplicationThemeColor.getInstance().getGridItemDetailLabelColor());
        viewHolder.downloadPercentage.setTextColor(ApplicationThemeColor.getInstance().getGridItemDetailLabelColor());

        File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());
        if(coverImageFile.exists()){
            displayImage(false, viewHolder.coverImageView, viewHolder.loading, "file://"+coverImageFile.getPath(), content);
        } else if (content.getSmallCoverImageDownloadPath() != null){
            displayImage(true, viewHolder.coverImageView, viewHolder.loading, content.getSmallCoverImageDownloadPath(), content);
        } else {
            Log.e("imageDisplayed", "noimage");
        }

        viewHolder.nameLabel.setText(content.getName());
        viewHolder.nameLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(libraryFragment.getActivity()));
        viewHolder.detailLabel.setText(content.getMonthlyName());
        viewHolder.detailLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(libraryFragment.getActivity()));
        viewHolder.downloadPercentage.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(libraryFragment.getActivity()));

        viewHolder.downloadStatus.setVisibility(View.GONE);
        viewHolder.overlay.setVisibility(View.GONE);

        boolean downloaded = content.isPdfDownloaded();
        boolean updateAvailable = content.isPdfUpdateAvailable();
        boolean downloading = content.isPdfDownloading()
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask !=null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content !=null
                && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content.getId().compareTo(content.getId()) == 0;

        if(downloaded){
            // Content is downloaded and ready to view.
            if(updateAvailable){

                if(downloading){
                    // update downloading
                    viewHolder.downloadStatus.setVisibility(View.VISIBLE);
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

                viewHolder.downloadStatus.setVisibility(View.VISIBLE);
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