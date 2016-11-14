package ak.detaysoft.galepress;

import android.graphics.Bitmap;
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

import ak.detaysoft.galepress.database_models.L_ApplicationCategory;
import ak.detaysoft.galepress.database_models.L_CustomerApplication;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.CustomPulseProgress;

/**
 * Created by p1025 on 08.11.2016.
 */

public class ApplicationHolderAdapter extends BaseAdapter {

    private ApplicationFragment applicationFragment;

    public ApplicationHolderAdapter(ApplicationFragment applicationFragment){
        this.applicationFragment = applicationFragment;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public int getCount() {
        return applicationFragment.getApplications().size();
    }

    public Object getItem(int position) {
        return applicationFragment.getApplications().get(position);
    }

    public long getItemId(int position) {
        return Long.parseLong(((L_ApplicationCategory)applicationFragment.getApplications().get(position)).getApplication().getId());
    }

    public class ViewHolder
    {
        public LinearLayout detailLayout;
        public ImageView coverImageView;
        public TextView nameLabel;
        public ImageView overlay;
        public RelativeLayout downloadStatus;
        public TextView downloadPercentage;
        public CustomPulseProgress loading;
        public L_ApplicationCategory application;

    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {


        final ApplicationHolderAdapter.ViewHolder viewHolder;
        final L_ApplicationCategory application = (L_ApplicationCategory) applicationFragment.getApplications().get(position);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ApplicationHolderAdapter.ViewHolder();
            convertView = applicationFragment.getLayoutInflater().inflate(R.layout.grid_cell, null);
            viewHolder.detailLayout = (LinearLayout)convertView.findViewById(R.id.detailLayout);
            viewHolder.coverImageView= (ImageView)convertView.findViewById(R.id.coverImage);
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.downloadStatus = (RelativeLayout) convertView.findViewById(R.id.grid_download_status);
            viewHolder.downloadPercentage = (TextView) convertView.findViewById(R.id.grid_download_percentage);
            viewHolder.loading = (CustomPulseProgress)convertView.findViewById(R.id.grid_image_loading);
            viewHolder.overlay = (ImageView) convertView.findViewById(R.id.grid_download_overlay);
            viewHolder.loading.startAnim();
            //viewHolder.loading.setIndeterminate(true);
            //viewHolder.loading.getIndeterminateDrawable().setColorFilter(ApplicationThemeColor.getInstance().getForegroundColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ApplicationHolderAdapter.ViewHolder)convertView.getTag();
        }
        viewHolder.detailLayout.setBackgroundColor(ApplicationThemeColor.getInstance().getHolderDetailBackround());
        viewHolder.nameLabel.setTextColor(ApplicationThemeColor.getInstance().getGridItemNameLabelColor());
        viewHolder.downloadPercentage.setTextColor(ApplicationThemeColor.getInstance().getGridItemDetailLabelColor());

        if(applicationFragment.isDownloaded) {
            /*
            * Uygulama kategorilerinden ilk cover image gosteriliyor.
            * */
            File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), application.getApplication().getId()+"_"+application.getCategory().getId());
            if(application.isUpdated()){
                displayImage(true, viewHolder.coverImageView, viewHolder.loading, application.getCoverImageUrl(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
            } else {
                if(coverImageFile.exists()){
                    displayImage(false, viewHolder.coverImageView, viewHolder.loading, "file://"+coverImageFile.getPath(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
                } else if (application.getCoverImageUrl() != null){
                    displayImage(true, viewHolder.coverImageView, viewHolder.loading, application.getCoverImageUrl(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
                } else {
                    Log.e("imageDisplayed", "noimage");
                }
            }
        } else {
            File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), application.getApplication().getId()+"_"+application.getCategory().getId());
            if(application.isUpdated()){
                displayImage(true, viewHolder.coverImageView, viewHolder.loading, application.getCoverImageUrl(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
            } else {
                if(coverImageFile.exists()){
                    displayImage(false, viewHolder.coverImageView, viewHolder.loading, "file://"+coverImageFile.getPath(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
                } else if (application.getCoverImageUrl() != null){
                    displayImage(true, viewHolder.coverImageView, viewHolder.loading, application.getCoverImageUrl(), application.getApplication().getId()+"_"+application.getCategory().getId(), application);
                } else {
                    Log.e("imageDisplayed", "noimage");
                }
            }
        }


        viewHolder.nameLabel.setText(application.getApplication().getAppName());
        viewHolder.nameLabel.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(applicationFragment.getActivity()));
        viewHolder.downloadPercentage.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(applicationFragment.getActivity()));

        viewHolder.downloadStatus.setVisibility(View.GONE);
        viewHolder.overlay.setVisibility(View.GONE);
        viewHolder.application = application;

        return convertView;
    }

    public void displayImage(final boolean isDownload, final ImageView image, final CustomPulseProgress loading, final String imagePath, final String fileName, final L_ApplicationCategory application){
        DisplayImageOptions displayConfig;
        if (isDownload) {
            displayConfig = new DisplayImageOptions.Builder()
                    .showImageOnFail(ApplicationThemeColor.getInstance().paintIcons(applicationFragment.getActivity(), ApplicationThemeColor.INTERNET_CONNECTION_ERROR))
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
                    GalePressApplication.getInstance().getDataApi().saveApplicationCoverImage(bitmap, fileName, application);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                loading.setVisibility(View.GONE);
            }
        });
    }
}
