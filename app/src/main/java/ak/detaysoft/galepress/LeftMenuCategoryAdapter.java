package ak.detaysoft.galepress;

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
        txtTitle.setText(mCategory.get(position).getName().toUpperCase());
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTypeface(ApplicationThemeColor.getInstance().getGothamBook(mContext));

        ImageView image = (ImageView)convertView.findViewById(R.id.category_icon);
        if(mCategory.get(position).getName().toLowerCase().contains("genel")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_stand));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_stand));
        } else if(mCategory.get(position).getName().toLowerCase().contains("alışveriş")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_alisveris));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_alisveris));
        }  else if(mCategory.get(position).getName().toLowerCase().contains("indirilenler")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
        } else if(mCategory.get(position).getName().toLowerCase().contains("çocuk")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_cocuk));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_cocuk));
        } else if(mCategory.get(position).getName().toLowerCase().contains("edebiyat")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_edebiyat));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_edebiyat));
        } else if(mCategory.get(position).getName().toLowerCase().contains("erkek")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_erkek));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_erkek));
        } else if(mCategory.get(position).getName().toLowerCase().contains("dekor")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_dekorasyon));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_dekorasyon));
        } else if(mCategory.get(position).getName().toLowerCase().contains("fotoğraf")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_fotograf));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_fotograf));
        } else if(mCategory.get(position).getName().toLowerCase().contains("gelin")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_gelin));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_gelin));
        } else if(mCategory.get(position).getName().toLowerCase().contains("gezi")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_gezi));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_gezi));
        } else if(mCategory.get(position).getName().toLowerCase().contains("haber")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_haber));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_haber));
        } else if(mCategory.get(position).getName().toLowerCase().contains("hayvan")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_hayvan));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_hayvan));
        } else if(mCategory.get(position).getName().toLowerCase().contains("hobi")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_hobi));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_hobi));
        } else if(mCategory.get(position).getName().toLowerCase().contains("indirilen")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_indirilenler));
        } else if(mCategory.get(position).getName().toLowerCase().contains("iş")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_is));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_is));
        } else if(mCategory.get(position).getName().toLowerCase().contains("kadın")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_kadin));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_kadin));
        } else if(mCategory.get(position).getName().toLowerCase().contains("moda")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_moda));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_moda));
        } else if(mCategory.get(position).getName().toLowerCase().contains("otomotiv")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_otomobil));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_otomobil));
        } else if(mCategory.get(position).getName().toLowerCase().contains("sağlık")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_saglik));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_saglik));
        } else if(mCategory.get(position).getName().toLowerCase().contains("sanat") || mCategory.get(position).getName().toLowerCase().contains("tasarım")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_sanat));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_sanat));
        } else if(mCategory.get(position).getName().toLowerCase().contains("sinema")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_sinema));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_sinema));
        } else if(mCategory.get(position).getName().toLowerCase().contains("spor")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_spor));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_spor));
        } else if(mCategory.get(position).getName().toLowerCase().contains("stand")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_stand));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_stand));
        } else if(mCategory.get(position).getName().toLowerCase().contains("tarih")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_tarih));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_tarih));
        } else if(mCategory.get(position).getName().toLowerCase().contains("teknoloji")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_teknoloji));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_teknoloji));
        } else if(mCategory.get(position).getName().toLowerCase().contains("yaşam")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_yasam));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_yasam));
        } else if(mCategory.get(position).getName().toLowerCase().contains("yemek")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_yemek));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_yemek));
        }  else if(mCategory.get(position).getName().toLowerCase().contains("müzik")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_muzik));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_muzik));
        }   else if(mCategory.get(position).getName().toLowerCase().contains("bilim")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_bilim));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_bilim));
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                image.setBackground(mContext.getResources().getDrawable(R.drawable.kategori_stand));
            else
                image.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.kategori_stand));
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
