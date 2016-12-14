package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ak.detaysoft.galepress.custom_models.ApplicationPlist;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.web_views.ExtraWebViewActivity;

/**
 * Created by p1025 on 03.04.2015.
 */
public class LeftMenuSocialAdapter extends RecyclerView.Adapter<LeftMenuSocialAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ApplicationPlist> mList;

    public LeftMenuSocialAdapter(Context context, ArrayList<ApplicationPlist> list){
        mContext = context;
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.left_menu_social_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.position =position;
        if(mList.get(position).getKey().toLowerCase().contains("facebook")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FACEBOOK_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("twitter")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TWITTER_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("instagram") || mList.get(position).getKey().toLowerCase().contains("ınstagram")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.INSTAGRAM_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("linkedin") || mList.get(position).getKey().toLowerCase().contains("linkedın")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.LINKEDIN_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("web")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("mail")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.MAIL_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("google") && mList.get(position).getKey().toLowerCase().contains("plus")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.GOOGLE_PLUS_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("pinterest")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.PINTEREST_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("tumblr")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.TUMBLR_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("youtube")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.YOUTUBE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("behance")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.BEHANCE_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.BEHANCE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("flicker")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FLICKER_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FLICKER_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("foursquare")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FOURSQUARE_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.FOURSQUARE_ICON));
        } else if(mList.get(position).getKey().toLowerCase().contains("swarm")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.SWARM_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.SWARM_ICON));
        } else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                holder.icon.setBackground(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
            else
                holder.icon.setBackgroundDrawable(ApplicationThemeColor.getInstance().getLeftMenuIconDrawable(mContext, ApplicationThemeColor.WEB_ICON));
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public int position;

        public MyViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.social_icon);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) icon.getLayoutParams();
            if(position == 0 || position == mList.size()-1){
                params.setMargins(0, 30, 0, 30);
            } else {
                params.setMargins(30, 30, 0, 30);
            }
            icon.setLayoutParams(params);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mList.get(position).getKey().toString().toLowerCase().contains("mail")) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        //intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mList.get(position).getValue().toString()});
                        intent.putExtra(Intent.EXTRA_SUBJECT, " ");
                        intent.putExtra(Intent.EXTRA_TEXT, " ");
                        try {
                            mContext.startActivity(Intent.createChooser(intent, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent intent = new Intent(mContext, ExtraWebViewActivity.class);
                        intent.putExtra("url", mList.get(position).getValue().toString());
                        intent.putExtra("isMainActivitIntent", true);
                        mContext.startActivity(intent);
                        ((MainActivity)mContext).overridePendingTransition(R.animator.left_to_right_translate, 0);
                    }
                }
            });
        }
    }
}
