package ak.detaysoft.graff;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ak.detaysoft.graff.custom_models.ApplicationPlist;
import ak.detaysoft.graff.util.ApplicationThemeColor;

/**
 * Created by p1025 on 03.04.2015.
 */
public class LeftMenuSocialAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ApplicationPlist> mList;

    public LeftMenuSocialAdapter(Context context, ArrayList<ApplicationPlist> list){
        mContext = context;
        mList = list;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.left_menu_social_item, null);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.social_title);
        txtTitle.setText((mList.get(position).getKey()));
        txtTitle.setTextColor(ApplicationThemeColor.getInstance().leftmenuListViewColorStateList());
        txtTitle.setTypeface(ApplicationThemeColor.getInstance().getOpenSansLight(mContext));

        ImageView image = (ImageView)convertView.findViewById(R.id.social_icon);
        image.setVisibility(View.VISIBLE);
        if(mList.get(position).getKey().toLowerCase().contains("facebook")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("twitter")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("instagram") || mList.get(position).getKey().toLowerCase().contains("ınstagram")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("linkedin") || mList.get(position).getKey().toLowerCase().contains("linkedın")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("web")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("mail")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("google") && mList.get(position).getKey().toLowerCase().contains("plus")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("pinterest")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("tumblr")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("youtube")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
            else
                image.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
        } else
            image.setVisibility(View.INVISIBLE);

        return convertView;
    }
}
