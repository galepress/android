package ak.detaysoft.galepress.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by p1025 on 22.03.2016.
 */
public class LeftMenuStateList extends StateListDrawable {

    public LeftMenuStateList(Drawable res, Drawable selectedRes) {
        super();
        addState(new int[]{android.R.attr.state_pressed}, selectedRes);
        addState(new int[]{android.R.attr.state_selected}, selectedRes);
        addState(new int[]{android.R.attr.state_enabled}, res);
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean isClicked = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_selected || state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }
        if (isClicked) {
            setColorFilter(ApplicationThemeColor.getInstance().getReverseThemeColorFilterWithAlpha((float)0.5));
        }
        else {
            setColorFilter(ApplicationThemeColor.getInstance().getReverseThemeColorFilter());
        }
        return super.onStateChange(stateSet);
    }
}
