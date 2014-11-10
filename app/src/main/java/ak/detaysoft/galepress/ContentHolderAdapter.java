package ak.detaysoft.galepress;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.File;
import java.util.List;

import ak.detaysoft.galepress.database_models.L_Content;

/**
 * Created by adem on 13/01/14.
 */

public class ContentHolderAdapter extends BaseAdapter  {
    private LibraryFragment libraryFragment;
    public List contents;

    public ContentHolderAdapter(LibraryFragment activity, List contents) {
        this.libraryFragment = activity;
        this.contents = contents;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public int getCount() {
        return contents.size();
    }

    public Object getItem(int position) {
        return contents.get(position);
    }

    public long getItemId(int position) {
        return ((L_Content)contents.get(position)).getId();
    }

    public static class ViewHolder implements View.OnClickListener
    {
        public ImageView coverImageView;
        public TextView nameLabel;
        public TextView detailLabel;
        public TextView monthLabel;
        public Button downloadButton;
        public Button updateButton;
        public Button cancelButton;
        public Button viewButton;
        public Button deleteButton;
        public ProgressBar progressBar;
        public TextView progressLabel;
        public L_Content content;

        @Override
        public void onClick(View v) {
            if(!GalePressApplication.getInstance().getDataApi().isBlockedFromWS){
                if(v == downloadButton){
                    if(DataApi.isConnectedToInternet()){
                        v.setEnabled(false);
                        GalePressApplication.getInstance().getDataApi().getPdf(content);
                    }
                }
                else if(v == updateButton){
                    if(DataApi.isConnectedToInternet()){
                        v.setEnabled(false);
                        v.setVisibility(View.INVISIBLE);
                        GalePressApplication.getInstance().getDataApi().getPdf(content);
                    }
                }
                else if(v == cancelButton){
                    v.setEnabled(false);
                    GalePressApplication.getInstance().getDataApi().cancelDownload(false);
                }
                else if(v == deleteButton){
                    v.setEnabled(false);
                    GalePressApplication.getInstance().getDataApi().deletePdf(content.getId());
                }
                else{
                    GalePressApplication.getInstance().getLibraryActivity().viewContent(content);
                }
            }
        }
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final L_Content content = (L_Content) contents.get(position);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ViewHolder();
            convertView = libraryFragment.getLayoutInflater().inflate(R.layout.grid_cell, null);
            viewHolder.coverImageView= (ImageView)convertView.findViewById(R.id.coverImage);
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.detailLabel = (TextView)convertView.findViewById(R.id.detailLabel);
            viewHolder.monthLabel = (TextView)convertView.findViewById(R.id.monthLabel);
            viewHolder.downloadButton = (Button)convertView.findViewById(R.id.download_button);
            viewHolder.updateButton = (Button)convertView.findViewById(R.id.update_button);
            viewHolder.cancelButton = (Button)convertView.findViewById(R.id.cancel_button);
            viewHolder.viewButton = (Button)convertView.findViewById(R.id.view_button);
            viewHolder.deleteButton = (Button)convertView.findViewById(R.id.delete_button);
            viewHolder.progressBar = (ProgressBar)convertView.findViewById(R.id.progress_bar);
            viewHolder.progressLabel = (TextView)convertView.findViewById(R.id.progress_label);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        if(viewHolder.coverImageView.getTag() != null) {
            ((ImageGetter) viewHolder.coverImageView.getTag()).cancel(true);
        }
        ImageGetter task = new ImageGetter(viewHolder.coverImageView) ;
        File coverImageFile = new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName());

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,coverImageFile);
        } else {
            task.execute(coverImageFile);
        }

        viewHolder.coverImageView.setTag(task);

        viewHolder.nameLabel.setText(content.getName());
        viewHolder.detailLabel.setText(content.getDetail());
        viewHolder.monthLabel.setText(content.getMonthlyName());

        viewHolder.progressBar.setVisibility(View.INVISIBLE);
        viewHolder.progressLabel.setVisibility(View.INVISIBLE);
        viewHolder.viewButton.setVisibility(View.INVISIBLE);
        viewHolder.deleteButton.setVisibility(View.INVISIBLE);
        viewHolder.cancelButton.setVisibility(View.INVISIBLE);
        viewHolder.cancelButton.setOnClickListener(viewHolder);

        boolean downloaded = content.isPdfDownloaded();
        boolean updateAvailable = content.isPdfUpdateAvailable();
        boolean downloading = content.isPdfDownloading() && GalePressApplication.getInstance().getDataApi().downloadPdfTask !=null && GalePressApplication.getInstance().getDataApi().downloadPdfTask.getStatus() == AsyncTask.Status.RUNNING && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content !=null && GalePressApplication.getInstance().getDataApi().downloadPdfTask.content.getId().compareTo(content.getId()) == 0;

        viewHolder.cancelButton.setOnClickListener(viewHolder);
        viewHolder.viewButton.setOnClickListener(viewHolder);
        viewHolder.deleteButton.setOnClickListener(viewHolder);
        viewHolder.downloadButton.setOnClickListener(viewHolder);
        viewHolder.updateButton.setOnClickListener(viewHolder);


        if(downloaded){
            // Content is downloaded and ready to view.
            viewHolder.downloadButton.setVisibility(View.INVISIBLE);

            viewHolder.viewButton.setVisibility(View.VISIBLE);
            viewHolder.viewButton.setEnabled(true);

            viewHolder.deleteButton.setVisibility(View.VISIBLE);
            viewHolder.deleteButton.setEnabled(true);

            viewHolder.cancelButton.setVisibility(View.INVISIBLE);
            convertView.setOnClickListener(viewHolder);

            if(updateAvailable){
                viewHolder.updateButton.setVisibility(View.VISIBLE);
                viewHolder.updateButton.setEnabled(true);

                if(downloading){
                    // update downloading
                    viewHolder.updateButton.setVisibility(View.INVISIBLE);
                    viewHolder.viewButton.setVisibility(View.INVISIBLE);
                    viewHolder.deleteButton.setVisibility(View.INVISIBLE);

                    viewHolder.cancelButton.setEnabled(true);
                    viewHolder.cancelButton.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.progressLabel.setVisibility(View.VISIBLE);

                }
            }
            else{
                // update not available
                viewHolder.updateButton.setVisibility(View.INVISIBLE);
            }
        }
        else{
            // not downloaded
            if(downloading){
                // Content is not downloaded but downloading
                viewHolder.cancelButton.setVisibility(View.VISIBLE);
                viewHolder.cancelButton.setEnabled(true);

                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.progressLabel.setVisibility(View.VISIBLE);

                viewHolder.downloadButton.setEnabled(false);
                viewHolder.downloadButton.setVisibility(View.INVISIBLE);

                viewHolder.viewButton.setVisibility(View.INVISIBLE);
            }
            else{
                // Content Download edilmemis. ilk acildigi durum.
                viewHolder.downloadButton.setVisibility(View.VISIBLE);
                viewHolder.downloadButton.setEnabled(true);

                viewHolder.deleteButton.setVisibility(View.INVISIBLE);
                viewHolder.updateButton.setVisibility(View.INVISIBLE);

                viewHolder.cancelButton.setVisibility(View.INVISIBLE);
            }
        }
        viewHolder.content = content;

        return convertView;
    }
}