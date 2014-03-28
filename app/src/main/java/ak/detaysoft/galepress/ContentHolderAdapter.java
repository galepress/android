package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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
    private Activity activity;
    public List contents;

    public ContentHolderAdapter(Activity activity, List contents) {
        this.activity = activity;
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


                if(v == downloadButton){
                    if(DataApi.isConnectedToInternet()){
                        v.setEnabled(false);
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


        }
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final L_Content content = (L_Content) contents.get(position);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ViewHolder();
            convertView = activity.getLayoutInflater().inflate(R.layout.grid_cell, null);
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
        task.execute(new File(GalePressApplication.getInstance().getFilesDir(), content.getCoverImageFileName()));
        viewHolder.coverImageView.setTag(task);

        viewHolder.nameLabel.setText(content.getName());
        viewHolder.detailLabel.setText(content.getDetail());
        viewHolder.monthLabel.setText(content.getMonthlyName());

        viewHolder.progressBar.setVisibility(View.INVISIBLE);
        viewHolder.progressLabel.setVisibility(View.INVISIBLE);
        viewHolder.viewButton.setVisibility(View.INVISIBLE);
        viewHolder.deleteButton.setVisibility(View.INVISIBLE);


        if(content.isPdfDownloaded()){
            viewHolder.downloadButton.setVisibility(View.INVISIBLE);
            viewHolder.viewButton.setVisibility(View.VISIBLE);
            viewHolder.viewButton.setEnabled(true);
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
            viewHolder.deleteButton.setEnabled(true);
            viewHolder.deleteButton.setOnClickListener(viewHolder);
        }
        else{
            viewHolder.downloadButton.setVisibility(View.VISIBLE);
            viewHolder.downloadButton.setOnClickListener(viewHolder);
            if(content.isPdfDownloading() && GalePressApplication.getInstance().getDataApi().downloadPdfTask !=null)
                viewHolder.downloadButton.setEnabled(false);
            else
                viewHolder.downloadButton.setEnabled(true);
        }

        if(content.isPdfUpdateAvailable()){
            viewHolder.updateButton.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.updateButton.setVisibility(View.INVISIBLE);
        }

        viewHolder.cancelButton.setVisibility(View.INVISIBLE);
        viewHolder.cancelButton.setOnClickListener(viewHolder);
        viewHolder.content = content;

        return convertView;
    }
}