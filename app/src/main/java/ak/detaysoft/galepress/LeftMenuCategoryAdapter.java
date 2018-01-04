package ak.detaysoft.galepress;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 02.04.2015.
 */
public class LeftMenuCategoryAdapter extends BaseAdapter {

    private Context mContext;
    private List<L_Category> mCategory;

    public LeftMenuCategoryAdapter(Context context, List<L_Category> category){
        mContext = context;
        mCategory = category;
    }
    @Override
    public int getCount() {
        return mCategory.size();
    }

    @Override
    public Object getItem(int position) {
        return mCategory.get(position);
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
            convertView = mInflater.inflate(R.layout.left_menu_category_item, parent, false);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.category_title);
        txtTitle.setText(mCategory.get(position).getCategoryName());
        txtTitle.setTextColor(ApplicationThemeColor.getInstance().getThemeColorWithAlpha(50));
        txtTitle.setTypeface(ApplicationThemeColor.getInstance().getRubikLight(mContext));


        if(((MainActivity)(mContext)).getLibraryFragment() != null) {
            if(((MainActivity)(mContext)).getLibraryFragment().selectedCategories == null) {
                ((MainActivity)(mContext)).getLibraryFragment().repairSelectedCategories();
            }

            for (int i = 0; i < ((MainActivity)(mContext)).getLibraryFragment().selectedCategories.size(); i++){
                L_Category item = ((MainActivity)(mContext)).getLibraryFragment().selectedCategories.get(i);
                if(item.getCategoryID().compareTo(mCategory.get(position).categoryID) == 0){
                    txtTitle.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
                }
            }
        }


         return convertView;
    }

    public void setmCategory(List<L_Category> mCategory){
        this.mCategory = mCategory;
    }
}
