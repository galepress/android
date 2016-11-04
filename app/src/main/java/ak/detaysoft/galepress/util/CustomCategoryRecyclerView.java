package ak.detaysoft.galepress.util;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by p1025 on 02.11.2016.
 */

public class CustomCategoryRecyclerView extends LinearLayoutManager {

    Context context;
    RecyclerView recyclerView;

    public CustomCategoryRecyclerView(Context context) {
        super(context);
        this.context = context;
    }

    public CustomCategoryRecyclerView(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomCategoryRecyclerView(RecyclerView recyclerView, Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.recyclerView = recyclerView;
        this.context = context;
    }

    public CustomCategoryRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


}
