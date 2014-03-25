package ak.detaysoft.galepress;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by adem on 04/03/14.
 */
public class ImageGetter extends AsyncTask<File, Void, Bitmap> {
    private ImageView iv;
    public ImageGetter(ImageView v) {
        iv = v;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        return decodeFile(params[0].getPath());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        iv.setImageBitmap(result);
    }
}