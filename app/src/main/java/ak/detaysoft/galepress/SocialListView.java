package ak.detaysoft.galepress;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by p1025 on 14.12.2016.
 */

public class SocialListView extends RecyclerView {
    public SocialListView(Context context) {
        super(context);
    }

    public SocialListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SocialListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void stopScroll() {
        super.stopScroll();
    }
}
