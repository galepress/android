package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.database_models.L_Statistic;

public class MuPDFReaderView extends ReaderView {
	enum Mode {Viewing, Selecting, Drawing}
	private final Context mContext;
	private boolean mLinksEnabled = true;
	private Mode mMode = Mode.Viewing;
	private boolean tapDisabled = false;
	private int tapPageMargin;

	protected void onTapMainDocArea() {}
	protected void onDocMotion() {}
	protected void onHit(Hit item) {};

	public void setLinksEnabled(boolean b) {
		mLinksEnabled = b;
		resetupChildren();
	}

	public void setMode(Mode m) {
		mMode = m;
	}

	public MuPDFReaderView(Activity act) {
		super(act);
		mContext = act;
		// Get the screen size etc to customise tap margins.
		// We calculate the size of 1 inch of the screen for tapping.
		// On some devices the dpi values returned are wrong, so we
		// sanity check it: we first restrict it so that we are never
		// less than 100 pixels (the smallest Android device screen
		// dimension I've seen is 480 pixels or so). Then we check
		// to ensure we are never more than 1/5 of the screen width.
		DisplayMetrics dm = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(dm);
		tapPageMargin = (int)dm.xdpi;
		if (tapPageMargin < 100)
			tapPageMargin = 100;
		if (tapPageMargin > dm.widthPixels/5)
			tapPageMargin = dm.widthPixels/5;
	}

	public boolean onSingleTapUp(MotionEvent e) {
		LinkInfo link = null;

		if (mMode == Mode.Viewing && !tapDisabled) {
			final MuPDFView pageView = (MuPDFView) getDisplayedView();
			Hit item = pageView.passClickEvent(e.getX(), e.getY());
			onHit(item);
			if (item == Hit.Nothing) {
				if (mLinksEnabled && pageView != null && (link = pageView.hitLink(e.getX(), e.getY())) != null) {
                    link.acceptVisitor(new LinkInfoVisitor() {
						@Override
						public void visitInternal(LinkInfoInternal li) {
							// Clicked on an internal (GoTo) link c
                            // MuPDF'in kodu burada sacmaliyordu. Bir sonraki sayfaya verilen pagelinkler calismiyordu. Iki sayfa sonrasina gidiyordu.
                            // Asagidaki sekilde bir sonraki sayfa olup olmadigini kontrol ederek bunu cozdum. (Adem)
                            // PageLinklerde ki mantik PageView icine tasindi. Ustuste binen durumlarda page link eziliyordu ve calismiyordu. View ekleyerek bunu engelliyorum.
                            /*
                            MuPDFCore core =((MuPDFActivity)mContext).core;
                            if(getDisplayedViewIndex() + 1 == li.pageNumber){
                                moveToNext();
                            }
                            else if (getDisplayedViewIndex() - 1 == li.pageNumber){
                                moveToPrevious();
                            }
                            else {
                                setDisplayedViewIndex(core.convertIndexesForLandscape2Page(li.pageNumber));
                            }
                            */
						}

						@Override
						public void visitExternal(LinkInfoExternal li) {
                            /*
                             Linklerde ki mantik PageView icine tasindi. Ustuste binen durumlarda page link eziliyordu ve calismiyordu. View ekleyerek bunu engelliyorum.
                            if(li.componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_WEBLINK){
                                Intent intent = new Intent(mContext, ExtraWebViewActivity.class);
                                intent.putExtra("url",li.url);
                                mContext.startActivity(intent);
                            }
                            */
						}

						@Override
						public void visitRemote(LinkInfoRemote li) {
							// Clicked on a remote (GoToR) link
						}
					});
				} else if (e.getX() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getX() > super.getWidth() - tapPageMargin) {
					super.smartMoveForwards();
				} else if (e.getY() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getY() > super.getHeight() - tapPageMargin) {
					super.smartMoveForwards();
				} else {
					onTapMainDocArea();
				}
			}
		}
		return super.onSingleTapUp(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		return super.onDown(e);
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		MuPDFView pageView = (MuPDFView)getDisplayedView();
		switch (mMode) {
		case Viewing:
			if (!tapDisabled)
				onDocMotion();

			return super.onScroll(e1, e2, distanceX, distanceY);
		case Selecting:
			if (pageView != null)
				pageView.selectText(e1.getX(), e1.getY(), e2.getX(), e2.getY());
			return true;
		default:
			return true;
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		switch (mMode) {
		case Viewing:
			return super.onFling(e1, e2, velocityX, velocityY);
		default:
			return true;
		}
	}

	public boolean onScaleBegin(ScaleGestureDetector d) {
		// Disabled showing the buttons until next touch.
		// Not sure why this is needed, but without it
		// pinch zoom can make the buttons appear
		tapDisabled = true;
		return super.onScaleBegin(d);
	}

	public boolean onTouchEvent(MotionEvent event) {

		if ( mMode == Mode.Drawing )
		{
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					touch_start(x, y);
					break;
				case MotionEvent.ACTION_MOVE:
					touch_move(x, y);
					break;
				case MotionEvent.ACTION_UP:
					touch_up();
					break;
			}
		}

		if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN)
		{
			tapDisabled = false;
		}

		return super.onTouchEvent(event);
	}

	private float mX, mY;

	private static final float TOUCH_TOLERANCE = 2;

	private void touch_start(float x, float y) {

		MuPDFView pageView = (MuPDFView)getDisplayedView();
		if (pageView != null)
		{
			pageView.startDraw(x, y);
		}
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {

		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
		{
			MuPDFView pageView = (MuPDFView)getDisplayedView();
			if (pageView != null)
			{
				pageView.continueDraw(x, y);
			}
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {

		// NOOP
	}

	protected void onChildSetup(int i, View v) {
		if (SearchTaskResult.get() != null
				&& SearchTaskResult.get().pageNumber == i)
			((MuPDFView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
		else
			((MuPDFView) v).setSearchBoxes(null);

		((MuPDFView) v).setLinkHighlighting(mLinksEnabled);

		((MuPDFView) v).setChangeReporter(new Runnable() {
			public void run() {
				applyToChildren(new ViewMapper() {
					@Override
					void applyToView(View view) {
						((MuPDFView) view).update();
					}
				});
			}
		});
	}

	protected void onMoveToChild(final int i) {

        MuPDFCore core = ((MuPDFActivity)mContext).core;

        int[] pages = getDisplayedPageNumbers(i);
        for(int j=0; j < pages.length; j++){
            if(pages[j]!=-1){
                Logout.e("Adem","Goruntulenen sayfa : "+pages[j]);

                Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                String udid = UUID.randomUUID().toString();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                dateFormat .setTimeZone(TimeZone.getTimeZone("GMT"));
                Location location = GalePressApplication.getInstance().location;
                L_Statistic statistic;

                if(core.getDisplayPages() ==1){
                    // Portrait
                    statistic = new L_Statistic(udid, core.content.getId(), location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, pages[j], dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_pageOpenedPortrait, null,null,null);
                }
                else{
                    //landscape
                    statistic = new L_Statistic(udid, core.content.getId(), location!=null?location.getLatitude():null,location!=null?location.getLongitude():null, pages[j], dateFormat.format(cal.getTime()),L_Statistic.STATISTIC_pageOpenedLandscape, null,null,null);
                }

                GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);


            }
        }

        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
            SearchTaskResult.set(null);
            resetupChildren();
        }

	}

    public int[] getDisplayedPageNumbers(int index){
        MuPDFCore core = ((MuPDFActivity)this.mContext).core;
        int[] pages = {-1, -1};

        if(core.getDisplayPages() == 1 || index == 0){
            // portrait mode
            pages[0] = index+1;
        }
        else{
            // landscape mode
            pages[0] = index*2;
            pages[1] = (index*2)+1;
            if(core.getNumPages()%2 == 0 && pages[1] > core.getNumPages()){
                // last page is double
                pages[1] = -1;
            }
        }
        return pages;
    }

	@Override
	protected void onMoveOffChild(int i) {
		View v = getView(i);
		if (v != null)
			((MuPDFView)v).deselectAnnotation();
	}

	protected void onSettle(View v) {
		// When the layout has settled ask the page to render
		// in HQ
		((MuPDFView) v).updateHq(false);
	}

	protected void onUnsettle(View v) {
		// When something changes making the previous settled view
		// no longer appropriate, tell the page to remove HQ
		((MuPDFView) v).removeHq();
	}

	@Override
	protected void onNotInUse(View v) {
		((MuPDFView) v).releaseResources();
	}

	@Override
	protected void onScaleChild(View v, Float scale) {
		((MuPDFView) v).setScale(scale);
        MuPDFPageView pageView = (MuPDFPageView)v;
        for(int i=0; i < pageView.getChildCount(); i++){
            View view = pageView.getChildAt(i);

            if(view instanceof WebView){
                float original_x = -1;
                float original_y = -1;
                WebView webView = (WebView) view;
                LinkInfo[] links = pageView.mLinks;
                if (links!=null){
                    for (LinkInfo link : links) {
                        if (link instanceof LinkInfoExternal){
                            if(((LinkInfoExternal) link).webViewId == webView.getId()){
                                original_x = link.rect.left * pageView.mSourceScale;
                                original_y = link.rect.top * pageView.mSourceScale;
                                webView.setPivotX(0);
                                webView.setPivotY(0);
                                webView.setX(original_x*scale);
                                webView.setY(original_y*scale);
                                webView.setScaleX(scale);
                                webView.setScaleY(scale);
                            }
                        }
                    }
                }
            } else if(view instanceof com.mogoweb.chrome.WebView){ //Chromium webview kontrol
                float original_x = -1;
                float original_y = -1;
                com.mogoweb.chrome.WebView webView = (com.mogoweb.chrome.WebView) view;
                LinkInfo[] links = pageView.mLinks;
                if (links!=null){
                    for (LinkInfo link : links) {
                        if (link instanceof LinkInfoExternal){
                            if(((LinkInfoExternal) link).webViewId == webView.getId()){
                                original_x = link.rect.left * pageView.mSourceScale;
                                original_y = link.rect.top * pageView.mSourceScale;
                                webView.setPivotX(0);
                                webView.setPivotY(0);
                                webView.setX(original_x * scale);
                                webView.setY(original_y * scale);
                                webView.setScaleX(scale);
                                webView.setScaleY(scale);
                                webView.invalidate();
                            }
                        }
                    }
                }
            }
        }

	}
}
