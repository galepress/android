package ak.detaysoft.galepress;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by gunes on 05/09/2017.
 */

public class UsedLibrariesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.used_libraries);
        findViewById(R.id.usedLibraries_header).setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());
        findViewById(R.id.usedLibraries_body).setBackgroundColor(ApplicationThemeColor.getInstance().getReverseThemeColor());

        TextView titleTextView = (TextView) findViewById(R.id.usedLibraries_title);
        titleTextView.setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        titleTextView.setTypeface(ApplicationThemeColor.getInstance().getRubikRegular(UsedLibrariesActivity.this));

        ImageButton closeButton = (ImageButton) findViewById(R.id.usedLibraries_close);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            closeButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        else
            closeButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.WEBVIEW_CLOSE));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView licenceText = (TextView) findViewById(R.id.usedLibraries_text);
        licenceText.setTextColor(ApplicationThemeColor.getInstance().getThemeColor());
        licenceText.setTypeface(ApplicationThemeColor.getInstance().getRubikRegular(UsedLibrariesActivity.this));
    }
}
