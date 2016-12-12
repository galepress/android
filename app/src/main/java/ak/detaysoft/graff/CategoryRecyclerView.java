package ak.detaysoft.graff;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by p1025 on 18.11.2016.
 */

public class CategoryRecyclerView extends RecyclerView {

    public CategoryRecyclerView(Context context) {
        super(context);
    }

    public CategoryRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    @Override
    public void stopScroll() {
        super.stopScroll();
    }
}
