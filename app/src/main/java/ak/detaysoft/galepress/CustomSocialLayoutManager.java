package ak.detaysoft.galepress;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by p1025 on 14.12.2016.
 */

public class CustomSocialLayoutManager extends LinearLayoutManager{

    Context context;
    RecyclerView recyclerView;

    public CustomSocialLayoutManager(Context context) {
        super(context);
    }

    public CustomSocialLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomSocialLayoutManager(RecyclerView recyclerView, Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.recyclerView = recyclerView;
        this.context = context;
    }
}
