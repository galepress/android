package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 31.08.2015.
 */
public class LeftMenuMembershipAdapter extends BaseAdapter{

    private Context mContext;
    public final static int LOGIN = 0;
    public final static int LOGOUT = 1;
    public final static int RESTORE = 2;
    public final static int SUBSCRIPTION = 3;

    public LeftMenuMembershipAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return GalePressApplication.getInstance().getMembershipMenuList().size();
    }

    @Override
    public Object getItem(int position) {
        return GalePressApplication.getInstance().getMembershipMenuList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.left_menu_membership_item, parent, false);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.membership_txt);

        if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LOGIN) {
            txtTitle.setText(mContext.getString(R.string.login));
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == RESTORE) {
            txtTitle.setText(mContext.getString(R.string.Restore));
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == SUBSCRIPTION) {
            txtTitle.setText(mContext.getString(R.string.Subscribe));
        }
        else if(GalePressApplication.getInstance().getMembershipMenuList().get(position) == LOGOUT) {
            txtTitle.setText(mContext.getString(R.string.logout));
        }
        txtTitle.setTextColor(ApplicationThemeColor.getInstance().leftmenuListViewColorStateList());
        txtTitle.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(mContext));

        return convertView;
    }

}
