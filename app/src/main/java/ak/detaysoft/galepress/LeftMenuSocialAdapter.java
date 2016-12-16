package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import ak.detaysoft.galepress.custom_models.ApplicationPlist;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

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

        ImageView icon = (ImageView)convertView.findViewById(R.id.social_icon);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) icon.getLayoutParams();
        if(position == 0 || position == mList.size()){
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(20, 0, 0, 0);
        }
        icon.setLayoutParams(params);
        icon.setVisibility(View.VISIBLE);
        if(mList.get(position).getKey().toLowerCase().contains("facebook")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("twitter")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("instagram") || mList.get(position).getKey().toLowerCase().contains("ınstagram")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("linkedin") || mList.get(position).getKey().toLowerCase().contains("linkedın")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("web")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("mail")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("google") && mList.get(position).getKey().toLowerCase().contains("plus")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("pinterest")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("tumblr")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("youtube")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("behance")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.BEHANCE_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.BEHANCE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("flicker")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FLICKER_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FLICKER_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("foursquare")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FOURSQUARE_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FOURSQUARE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("swarm")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.SWARM_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.SWARM_ICON));
        } else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
            else
                icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
        }

        return convertView;
    }
}