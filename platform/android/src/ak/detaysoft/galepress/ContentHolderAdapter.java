package ak.detaysoft.galepress;

import com.artifex.mupdfdemo.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by adem on 13/01/14.
 */

public class ContentHolderAdapter extends BaseAdapter {
    private Context mContext;
    private Activity activity;

    public ContentHolderAdapter(Context c, Activity activity) {
        mContext = c;
        this.activity = activity;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder
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
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView;
        ViewHolder viewHolder;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            viewHolder = new ViewHolder();
            convertView = activity.getLayoutInflater().inflate(R.layout.grid_row, null);
            viewHolder.coverImageView= (ImageView)convertView.findViewById(R.id.coverImage);
            viewHolder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            viewHolder.detailLabel = (TextView)convertView.findViewById(R.id.detailLabel);
            viewHolder.monthLabel = (TextView)convertView.findViewById(R.id.monthLabel);
            viewHolder.downloadButton = (Button)convertView.findViewById(R.id.download_button);
            viewHolder.updateButton = (Button)convertView.findViewById(R.id.update_button);
            viewHolder.cancelButton = (Button)convertView.findViewById(R.id.cancel_button);
            viewHolder.viewButton = (Button)convertView.findViewById(R.id.view_button);
            viewHolder.deleteButton = (Button)convertView.findViewById(R.id.delete_button);
            convertView.setTag(viewHolder);

//            imageView = new ImageView(mContext);
//            imageView.setLayoutParams(new GridView.LayoutParams(110, 145));
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setPadding(0, 0, 0, 0);
//            imageView.setBackgroundColor(Color.BLUE);
        } else {
//            imageView = (ImageView) convertView;
            viewHolder = (ViewHolder)convertView.getTag();
        }

//        imageView.setImageResource(mThumbIds[position]);

        viewHolder.coverImageView.setImageResource(mThumbIds[position]);
        viewHolder.nameLabel.setText("Name Label");



        return convertView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,
            R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,
            R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1,R.drawable.sample1

    };
}