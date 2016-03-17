package ak.detaysoft.galepress;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artifex.mupdfdemo.MuPDFActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 07.07.2015.
 */
public class HomeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_fragment, container, false);
        v.setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());

        if(!((MainActivity)getActivity()).isTabFirstInit) {
            // Home buttonu kullanilarak master pdf acildigi zaman reader sayfasinda cihaz rotate oldugunda bu metod tekrar cagrildigi icin master ikinci defa aciliyordu
            if (GalePressApplication.getInstance().getMuPDFActivity() == null)
                openMaster();
        } else
            ((MainActivity)getActivity()).isTabFirstInit = false;
        return v;
    }


    public void openMaster(){
        L_Content content = GalePressApplication.getInstance().getDataApi().getMasterContent();
        File samplePdfFile = new File(content.getPdfPath(),"file.pdf");
        if(content!=null && content.isPdfDownloaded() && samplePdfFile.exists()){

            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, content.getId(), location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, null, dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_contentOpened, null,null,null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

            Uri uri = Uri.parse(samplePdfFile.getAbsolutePath());
            Intent intent = new Intent(getActivity(), MuPDFActivity.class);
            intent.putExtra("content", content);
            intent.putExtra("isHomeOpen", true);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            getActivity().startActivityForResult(intent, 101);
        } else {

        }
    }
}
