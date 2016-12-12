package ak.detaysoft.graff;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ak.detaysoft.graff.database_models.L_Category;
import ak.detaysoft.graff.util.ApplicationThemeColor;

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
        txtTitle.setText(mCategory.get(position).getName().toUpperCase());
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(mContext));

        ImageView image = (ImageView)convertView.findViewById(R.id.category_icon);
        if(mCategory.get(position).getId().intValue() == -1){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
        } else if(mCategory.get(position).getId().intValue() == 1){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_raff));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_raff));
        } else if(mCategory.get(position).getId().intValue() == 2){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_yasam));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_yasam));
        }  else if(mCategory.get(position).getId().intValue() == 3){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_evdekokarsyon));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_evdekokarsyon));
        } else if(mCategory.get(position).getId().intValue() == 4){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_kadin));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_kadin));
        } else if(mCategory.get(position).getId().intValue() == 5){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_moda));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_moda));
        } else if(mCategory.get(position).getId().intValue() == 6){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_alisveris));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_alisveris));
        } else if(mCategory.get(position).getId().intValue() == 7){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_isdunyasi));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_isdunyasi));
        } else if(mCategory.get(position).getId().intValue() == 8){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_erkek));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_erkek));
        } else if(mCategory.get(position).getId().intValue() == 9){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_sektorel));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_sektorel));
        } else if(mCategory.get(position).getId().intValue() == 10){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_ailecocuk));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_ailecocuk));
        } else if(mCategory.get(position).getId().intValue() == 11){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_mizah));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_mizah));
        } else if(mCategory.get(position).getId().intValue() == 12){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_bilim));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_bilim));
        } else if(mCategory.get(position).getId().intValue() == 13){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_otomobil));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_otomobil));
        } else if(mCategory.get(position).getId().intValue() == 14){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_sanat));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_sanat));
        } else if(mCategory.get(position).getId().intValue() == 15){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_kultur));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_kultur));
        } else if(mCategory.get(position).getId().intValue() == 16){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_spor));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_spor));
        } else if(mCategory.get(position).getId().intValue() == 17){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_katolog));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_katolog));
        }  else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_raff));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_raff));
        }


        ((LinearLayout)image.getParent()).setBackgroundColor(Color.TRANSPARENT);
        /*
         * https://fabric.io/galepress/android/apps/ak.detaysoft.yeryuzudergidis/issues/56d3205ff5d3a7f76b2cef6d
         * Seklinde bi hata vardi. selectedCategories null olmasi ihtimaline karsi bende ilk createde oldugu gibi genel kategorisini set ettim
         * */
        if(((MainActivity)(mContext)).getApplicationFragment() != null) {
            if(((MainActivity)(mContext)).getApplicationFragment().selectedCategory == null) {
                ((MainActivity)(mContext)).getApplicationFragment().repairSelectedCategory();
            }

            if(((MainActivity)(mContext)).getApplicationFragment().selectedCategory.getId().compareTo(mCategory.get(position).id) == 0){
                ((LinearLayout)image.getParent()).setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());
            }
        }


         return convertView;
    }

    public void setmCategory(List<L_Category> mCategory){
        this.mCategory = mCategory;
    }
}
