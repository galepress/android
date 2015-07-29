package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import ak.detaysoft.galepress.*;
import ak.detaysoft.galepress.util.CustomPulseProgress;


class PatchInfo {
	public Point patchViewSize;
	public Rect  patchArea;
	public boolean completeRedraw;

	public PatchInfo(Point aPatchViewSize, Rect aPatchArea, boolean aCompleteRedraw) {
		patchViewSize = aPatchViewSize;
		patchArea = aPatchArea;
		completeRedraw = aCompleteRedraw;
	}
}

// Make our ImageViews opaque to optimize redraw
class OpaqueImageView extends ImageView {

	public OpaqueImageView(Context context) {
		super(context);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}
}

interface TextProcessor {
	void onStartLine();
	void onWord(TextWord word);
	void onEndLine();
}

class TextSelector {
	final private TextWord[][] mText;
	final private RectF mSelectBox;

	public TextSelector(TextWord[][] text, RectF selectBox) {
		mText = text;
		mSelectBox = selectBox;
	}

	public void select(TextProcessor tp) {
		if (mText == null || mSelectBox == null)
			return;

		ArrayList<TextWord[]> lines = new ArrayList<TextWord[]>();
		for (TextWord[] line : mText)
			if (line[0].bottom > mSelectBox.top && line[0].top < mSelectBox.bottom)
				lines.add(line);

		Iterator<TextWord[]> it = lines.iterator();
		while (it.hasNext()) {
			TextWord[] line = it.next();
			boolean firstLine = line[0].top < mSelectBox.top;
			boolean lastLine = line[0].bottom > mSelectBox.bottom;
			float start = Float.NEGATIVE_INFINITY;
			float end = Float.POSITIVE_INFINITY;

			if (firstLine && lastLine) {
				start = Math.min(mSelectBox.left, mSelectBox.right);
				end = Math.max(mSelectBox.left, mSelectBox.right);
			} else if (firstLine) {
				start = mSelectBox.left;
			} else if (lastLine) {
				end = mSelectBox.right;
			}

			tp.onStartLine();

			for (TextWord word : line)
				if (word.right > start && word.left < end)
					tp.onWord(word);

			tp.onEndLine();
		}
	}
}

public abstract class PageView extends ViewGroup {
	private static final int HIGHLIGHT_COLOR = 0x802572AC;
	private static final int LINK_COLOR = 0x80AC7225;
	private static final int BOX_COLOR = 0xFF4444FF;
	private static final int INK_COLOR = 0xFFFF0000;
	private static final float INK_THICKNESS = 10.0f;
	private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
	private static final int PROGRESS_DIALOG_DELAY = 200;
	protected final Context   mContext;
	protected     int       mPageNumber;
	private       Point     mParentSize;
	protected     Point     mSize;   // Size of page at minimum zoom
	protected     float     mSourceScale;

	private       ImageView mEntire; // Image rendered at minimum zoom
	private       Bitmap    mEntireBm;
	private       Matrix    mEntireMat;
	private       AsyncTask<Void,Void,TextWord[][]> mGetText;
	public        AsyncTask<Void,Void,LinkInfo[]> mGetLinkInfo;
	private       AsyncTask<Void,Void,Void> mDrawEntire;

	private       Point     mPatchViewSize; // View size on the basis of which the patch was created
	private       Rect      mPatchArea;
	private       ImageView mPatch;
	private       Bitmap    mPatchBm;
	private       AsyncTask<PatchInfo,Void,PatchInfo> mDrawPatch;
	private       RectF     mSearchBoxes[];
	protected     LinkInfo  mLinks[];
	private       RectF     mSelectBox;
	private       TextWord  mText[][];
	private       RectF     mItemSelectBox;
	protected     ArrayList<ArrayList<PointF>> mDrawing;
	private       View      mSearchView;
	private       boolean   mIsBlank;
	private       boolean   mHighlightLinks;

	private       ProgressBar mBusyIndicator;
	private final Handler   mHandler = new Handler();
    private MuPDFCore core = null;
    AtomicInteger atomicInteger = new AtomicInteger();

	public PageView(Context c, Point parentSize, Bitmap sharedHqBm) {
		super(c);
		mContext    = c;
		mParentSize = parentSize;
		setBackgroundColor(BACKGROUND_COLOR);
		mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Config.ARGB_8888);
		mPatchBm = sharedHqBm;
		mEntireMat = new Matrix();
	}
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Logout.e("Adem","Intercepted touch event : "+ev.getAction());
//        return true;
//    }



	protected abstract void drawPage(Bitmap bm, int sizeX, int sizeY, int patchX, int patchY, int patchWidth, int patchHeight);
	protected abstract void updatePage(Bitmap bm, int sizeX, int sizeY, int patchX, int patchY, int patchWidth, int patchHeight);
	protected abstract LinkInfo[] getLinkInfo();
	protected abstract TextWord[][] getText();
	protected abstract void addMarkup(PointF[] quadPoints, Annotation.Type type);

	private void reinit() {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

		if (mGetLinkInfo != null) {
			mGetLinkInfo.cancel(true);
			mGetLinkInfo = null;
		}

		if (mGetText != null) {
			mGetText.cancel(true);
			mGetText = null;
		}

		mIsBlank = true;
		mPageNumber = 0;

		if (mSize == null)
			mSize = mParentSize;

		if (mEntire != null) {
			mEntire.setImageBitmap(null);
			mEntire.invalidate();
		}

		if (mPatch != null) {
			mPatch.setImageBitmap(null);
			mPatch.invalidate();
		}

		mPatchViewSize = null;
		mPatchArea = null;

		mSearchBoxes = null;
		mLinks = null;
		mSelectBox = null;
		mText = null;
		mItemSelectBox = null;
	}

	public void releaseResources() {
		reinit();

		if (mBusyIndicator != null) {
			removeView(mBusyIndicator);
			mBusyIndicator = null;
		}
	}

	public void releaseBitmaps() {
		reinit();
        // Outofmemory hatasi aliyordum. Onun cozumu icin ekledim. (ak_)
        mEntireBm.recycle();
        mPatchBm.recycle();
        //
		mEntireBm = null;
		mPatchBm = null;
	}

	public void blank(int page) {
		reinit();
		mPageNumber = page;

		if (mBusyIndicator == null) {
			mBusyIndicator = new ProgressBar(mContext);
			mBusyIndicator.setIndeterminate(true);
//			mBusyIndicator.setBackgroundResource(R.drawable.busy);
//            mBusyIndicator.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.loading1));
//
//            0xFFFF0000
            mBusyIndicator.getIndeterminateDrawable().setColorFilter(0xFF00D0FF, android.graphics.PorterDuff.Mode.MULTIPLY);
			addView(mBusyIndicator);
		}

		setBackgroundColor(BACKGROUND_COLOR);
	}

    public void clearCustomProgress(PageView pageView){
        ArrayList<View> gpAnnotations = getGPCustomProgress(pageView);
        for(int i=0; i < gpAnnotations.size(); i++){
            View view = gpAnnotations.get(i);
            pageView.removeView(view);
            pageView.invalidate();
        }
    }

    public void clearWebAnnotations(PageView pageView){
        // pageView icindeki webView'leri kaldirir. PageView'ler tekrar ettigi icin eski webView ler yeni sayfalara biniyordu.
        // Bu method MuPDFPageView icinden de cagiriliyor. Sadece buradan cagrilmasi butun webviewleri kaldirmiyordu.
//        pageView.is
        ArrayList<View> gpAnnotations = getGPAnnotations(pageView);
        for(int i=0; i < gpAnnotations.size(); i++){

            View view = gpAnnotations.get(i);

            if(view instanceof WebView){
                WebView webView = (WebView)view;
                webView.loadUrl("");
                webView.stopLoading();

                try {
                    Class.forName("android.webkit.WebView")
                            .getMethod("onPause", (Class[]) null)
                            .invoke(webView, (Object[]) null);

                } catch(Exception cnfe) {
                    Log.e("onPause", cnfe.toString());
                }

                webView.destroy();
                pageView.removeView(view);
                pageView.invalidate();
            } if(view instanceof com.mogoweb.chrome.WebView){

                try {
                    final com.mogoweb.chrome.WebView webView = (com.mogoweb.chrome.WebView)view;
                    webView.stopLoading();

                    try {
                        Class.forName("com.mogoweb.chrome.WebView")
                                .getMethod("onPause", (Class[]) null)
                                .invoke(webView, (Object[]) null);

                    } catch(Exception cnfe) {
                        Log.e("onPause", cnfe.toString());
                    }

                    webView.destroy(); //Fatal-signal when destroy webview
                    pageView.removeView(view);
                    pageView.invalidate();
                } catch (Exception e){
                    Log.e("ChromeView destroy", e.toString());
                }

            }


        }

        //webview/mediaplayer durdurmak için
        ((AudioManager)mContext.getSystemService(
                Context.AUDIO_SERVICE)).requestAudioFocus(
                new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {}
                }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public ArrayList<View> getGPAnnotations(PageView pageView){
        ArrayList<View> gpAnnotations = new ArrayList<View>();
        for(int i=0; i < pageView.getChildCount(); i++){
            View view = (View)pageView.getChildAt(i);
            if(view instanceof WebView){
                gpAnnotations.add(view);
            } else if(view instanceof com.mogoweb.chrome.WebView) {
                gpAnnotations.add(view);
            }
        }
        return gpAnnotations;
    }

    public ArrayList<View> getGPCustomProgress(PageView pageView){
        ArrayList<View> gpprogress = new ArrayList<View>();
        for(int i=0; i < pageView.getChildCount(); i++){
            View view = (View)pageView.getChildAt(i);
            if(view instanceof CustomPulseProgress){
                gpprogress.add(view);
            } else if(view instanceof com.mogoweb.chrome.WebView) {
                gpprogress.add(view);
            }
        }
        return gpprogress;
    }

	public void setPage(final int page, PointF size) {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		mIsBlank = false;
		// Highlights may be missing because mIsBlank was true on last draw
		if (mSearchView != null)
			mSearchView.invalidate();

		mPageNumber = page;
		if (mEntire == null) {
			mEntire = new OpaqueImageView(mContext);
			mEntire.setScaleType(ImageView.ScaleType.MATRIX);
			addView(mEntire);
		}

		// Calculate scaled size that fits within the screen limits
		// This is the size at minimum zoom
		mSourceScale = Math.min(mParentSize.x/size.x, mParentSize.y/size.y);
		Point newSize = new Point((int)(size.x*mSourceScale), (int)(size.y*mSourceScale));
		mSize = newSize;

		mEntire.setImageBitmap(null);
		mEntire.invalidate();

// Render the page in the background
        mDrawEntire = new AsyncTask<Void,Void,Void>() {
            protected Void doInBackground(Void... v) {
                drawPage(mEntireBm, mSize.x, mSize.y, 0, 0, mSize.x, mSize.y);
                return null;
            }

            protected void onPreExecute() {
                setBackgroundColor(BACKGROUND_COLOR);
                mEntire.setImageBitmap(null);
                mEntire.invalidate();

                if (mBusyIndicator == null) {
                    mBusyIndicator = new ProgressBar(mContext);
                    mBusyIndicator.setIndeterminate(true);
                    mBusyIndicator.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.loading1));
//                    mBusyIndicator.setBackgroundResource(R.drawable.busy);
                    addView(mBusyIndicator);
                    mBusyIndicator.setVisibility(INVISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (mBusyIndicator != null)
                                mBusyIndicator.setVisibility(VISIBLE);
                        }
                    }, PROGRESS_DIALOG_DELAY);
                }
            }

            protected void onPostExecute(Void v) {
                removeView(mBusyIndicator);
                mBusyIndicator = null;
                mEntire.setImageBitmap(mEntireBm);
                mEntire.invalidate();
                setBackgroundColor(Color.TRANSPARENT);
            }
        };

        mDrawEntire.execute();
        getNewLinkInfoTask();

        //mGetLinkInfo.execute();

		if (mSearchView == null) {
			mSearchView = new ViewGroup(mContext) {
				@Override
				protected void onDraw(final Canvas canvas) {
					super.onDraw(canvas);
					// Work out current total scale factor
					// from source to view
					final float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
					final Paint paint = new Paint();

					if (!mIsBlank && mSearchBoxes != null) {
						paint.setColor(HIGHLIGHT_COLOR);
						for (RectF rect : mSearchBoxes)
							canvas.drawRect(rect.left*scale, rect.top*scale,
									        rect.right*scale, rect.bottom*scale,
									        paint);
					}

					if (!mIsBlank && mLinks != null && mHighlightLinks) {
						paint.setColor(LINK_COLOR);
						for (LinkInfo link : mLinks){
                            boolean insOf = link instanceof LinkInfoExternal;
                            boolean insOf2 = link.getClass().isAssignableFrom(LinkInfoExternal.class);
                            if(link instanceof LinkInfoExternal){
                                canvas.drawRect(link.rect.left*scale, link.rect.top*scale,link.rect.right*scale, link.rect.bottom*scale,paint);
                            }

                        }
					}

					if (mSelectBox != null && mText != null) {
						paint.setColor(HIGHLIGHT_COLOR);
						processSelectedText(new TextProcessor() {
							RectF rect;

							public void onStartLine() {
								rect = new RectF();
							}

							public void onWord(TextWord word) {
								rect.union(word);
							}

							public void onEndLine() {
								if (!rect.isEmpty())
									canvas.drawRect(rect.left*scale, rect.top*scale, rect.right*scale, rect.bottom*scale, paint);
							}
						});
					}

					if (mItemSelectBox != null) {
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(BOX_COLOR);
						canvas.drawRect(mItemSelectBox.left*scale, mItemSelectBox.top*scale, mItemSelectBox.right*scale, mItemSelectBox.bottom*scale, paint);
					}

					if (mDrawing != null) {
						Path path = new Path();
						PointF p;

						paint.setAntiAlias(true);
						paint.setDither(true);
						paint.setStrokeJoin(Paint.Join.ROUND);
						paint.setStrokeCap(Paint.Cap.ROUND);

						paint.setStyle(Paint.Style.FILL);
						paint.setStrokeWidth(INK_THICKNESS * scale);
						paint.setColor(INK_COLOR);

						Iterator<ArrayList<PointF>> it = mDrawing.iterator();
						while (it.hasNext()) {
							ArrayList<PointF> arc = it.next();
							if (arc.size() >= 2) {
								Iterator<PointF> iit = arc.iterator();
								p = iit.next();
								float mX = p.x * scale;
								float mY = p.y * scale;
								path.moveTo(mX, mY);
								while (iit.hasNext()) {
									p = iit.next();
									float x = p.x * scale;
									float y = p.y * scale;
									path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
									mX = x;
									mY = y;
								}
								path.lineTo(mX, mY);
							} else {
								p = arc.get(0);
								canvas.drawCircle(p.x * scale, p.y * scale, INK_THICKNESS * scale / 2, paint);
							}
						}

						paint.setStyle(Paint.Style.STROKE);
						canvas.drawPath(path, paint);
					}
				}

                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {

                }
            };

			addView(mSearchView);
		}
		requestLayout();
	}

    public AsyncTask<Void,Void,LinkInfo[]> getNewLinkInfoTask(){
        // Get the link info in the background
        mGetLinkInfo = new AsyncTask<Void,Void,LinkInfo[]>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                clearWebAnnotations(PageView.this);
                clearCustomProgress(PageView.this);
            }

            protected LinkInfo[] doInBackground(Void... v) {
                return getLinkInfo();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            protected void onPostExecute(LinkInfo[] v) {

                mLinks = v;
                final float scale = mSourceScale*(float)getWidth()/(float)mSize.x;//
                for (LinkInfo link : mLinks){
                    if(link instanceof LinkInfoExternal ){
                        final LinkInfoExternal linkInfoExternal = (LinkInfoExternal)link;
                        final int left = (int)(linkInfoExternal.rect.left * scale);
                        final int top = (int) (linkInfoExternal.rect.top * scale);
                        int right = (int) (linkInfoExternal.rect.right * scale);
                        int bottom = (int) (linkInfoExternal.rect.bottom * scale);


                        CustomPulseProgress progressBar;
                        if(!linkInfoExternal.isInternal && !linkInfoExternal.isModal) {
                            int progressSize = 40;
                            progressBar = new CustomPulseProgress(mContext);
                            progressBar.layout((left+right)/2 - progressSize/2, (top+bottom)/2 - progressSize/2, (left+right)/2+progressSize, (top+bottom)/2+progressSize);
                        }
                        else
                            progressBar = null;

                        if((linkInfoExternal.isWebAnnotation())){
                            if(linkInfoExternal.isModal){
                                Button modalButton = new Button(mContext);
                                modalButton.layout(left,top,right,bottom);
                                modalButton.setBackgroundColor(Color.TRANSPARENT);
                                modalButton.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(mContext, ExtraWebViewActivity.class);
                                        intent.putExtra("url",linkInfoExternal.getSourceUrlPath(mContext)); // daha once linkInfoExternal.sourceurl vardi o nedenle modal acilmiyordu
                                        intent.putExtra("isModal", true);
                                        mContext.startActivity(intent);
                                    }
                                });
                                addView(modalButton);
                            }
                            else{
                                final int LOLLIPOP = 21; // Android 5.0
                                if (android.os.Build.VERSION.SDK_INT >= LOLLIPOP) {
                                    String url = linkInfoExternal.getSourceUrlPath(mContext);
                                    // Web Annotations
                                    final WebViewAnnotation web = new WebViewAnnotation(mContext, linkInfoExternal, progressBar);
                                    web.layout(left,top,right,bottom);
                                    web.readerView = ((MuPDFActivity) mContext).mDocView;
                                    web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                    final String url2 = linkInfoExternal.getSourceUrlPath(mContext);

                                    web.setId(atomicInteger.incrementAndGet());
                                    linkInfoExternal.webViewId = web.getId();

                                    if(linkInfoExternal.isWebAnnotation()){
                                        web.loadUrl(url);
                                    }
                                    addView(web);
                                } else {
                                    String url = linkInfoExternal.getSourceUrlPath(mContext);
                                    // Web Annotations
                                    final WebViewAnnotationWithChromium web = new WebViewAnnotationWithChromium(mContext, linkInfoExternal, progressBar);
                                    web.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
                                    web.layout(left,top,right,bottom);
                                    web.readerView = ((MuPDFActivity) mContext).mDocView;
                                    web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                    final String url2 = linkInfoExternal.getSourceUrlPath(mContext);

                                    web.setId(atomicInteger.incrementAndGet());
                                    linkInfoExternal.webViewId = web.getId();

                                    if(linkInfoExternal.isWebAnnotation()){
                                        web.loadUrl(url);
                                    }

                                    addView(web);
                                }

                            }
                        }
                        else if((((LinkInfoExternal) link).componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_HARİTA) ){
//                            Map Annotations
//                            http://adem.me/map/index.html?lat=41.033621&lon=28.952785&zoom=16&w=400&h=300&mapType=0
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("http");
                            builder.authority("www.galepress.com");
                            builder.appendPath("files");
                            builder.appendPath("map_html");
                            builder.appendPath("index.html");
                            builder.appendQueryParameter("lat",String.valueOf(linkInfoExternal.location.getLatitude()));
                            builder.appendQueryParameter("lon",String.valueOf(linkInfoExternal.location.getLongitude()));
                            builder.appendQueryParameter("zoom",String.valueOf(linkInfoExternal.zoom));
                            builder.appendQueryParameter("w",String.valueOf(right-left));
                            builder.appendQueryParameter("h",String.valueOf(bottom-top));
                            builder.appendQueryParameter("mapType",String.valueOf(linkInfoExternal.mapType));
                            String mapUrl = builder.build().toString();


                            final int LOLLIPOP = 21; // Android 5.0
                            if (android.os.Build.VERSION.SDK_INT >= LOLLIPOP) {
                                // Web Annotations
                                final WebViewAnnotation web = new WebViewAnnotation(mContext, linkInfoExternal, progressBar);
                                web.layout(left,top,right,bottom);
                                web.readerView = ((MuPDFActivity) mContext).mDocView;
                                web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

                                web.setId(atomicInteger.incrementAndGet());
                                linkInfoExternal.webViewId = web.getId();
                                web.loadUrl(mapUrl);
                                addView(web);
                            } else {
                                // Web Annotations
                                final WebViewAnnotationWithChromium web = new WebViewAnnotationWithChromium(mContext, linkInfoExternal, progressBar);
                                web.layout(left,top,right,bottom);
                                web.readerView = ((MuPDFActivity) mContext).mDocView;
                                web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

                                web.setId(atomicInteger.incrementAndGet());
                                linkInfoExternal.webViewId = web.getId();
                                web.loadUrl(mapUrl);

                                addView(web);
                            }
                        }
                        else if(((LinkInfoExternal) link).componentAnnotationTypeId == LinkInfoExternal.COMPONENT_TYPE_ID_WEBLINK){
                            View view = new View(mContext);
                            view.layout(left,top,right,bottom);
                            view.setBackgroundColor(Color.TRANSPARENT);
                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ExtraWebViewActivity.class);
                                    intent.putExtra("url",linkInfoExternal.url);
                                    intent.putExtra("isModal", true);
                                    mContext.startActivity(intent);
                                }
                            });
                            addView(view);
                        }

                        if(!linkInfoExternal.isInternal && !linkInfoExternal.isModal) {
                            addView(progressBar);
                        }
                    }
                    else{
                        // LinkInfo Internal - Burada pagelinkler icin webView olusturacagiz.
                        final LinkInfoInternal linkInfoInternal = (LinkInfoInternal)link;
                        final int left = (int)(linkInfoInternal.rect.left * scale);
                        final int top = (int) (linkInfoInternal.rect.top * scale);
                        int right = (int) (linkInfoInternal.rect.right * scale);
                        int bottom = (int) (linkInfoInternal.rect.bottom * scale);
                        View view = new View(mContext);
                        view.layout(left,top,right,bottom);
                        view.setBackgroundColor(Color.TRANSPARENT);
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MuPDFCore core =((MuPDFActivity)mContext).core;
                                MuPDFReaderView readerView = ((MuPDFActivity) mContext).mDocView;
                                if(readerView.getDisplayedViewIndex() + 1 == linkInfoInternal.pageNumber){
                                    readerView.moveToNext(true);
                                }
                                else if (readerView.getDisplayedViewIndex() - 1 == linkInfoInternal.pageNumber){
                                    readerView.moveToPrevious(true);
                                }
                                else {
                                    readerView.setDisplayedViewIndex(core.convertIndexesForLandscape2Page(linkInfoInternal.pageNumber));
                                    ((MuPDFActivity)mContext).scrollToLastThumnail(core.convertIndexesForLandscape2Page(linkInfoInternal.pageNumber));
                                }
                            }
                        });
                        addView(view);
                    }
                }

                if (mSearchView != null)
                    mSearchView.invalidate();
            }
        };

        return mGetLinkInfo;
    }

	public void setSearchBoxes(RectF searchBoxes[]) {
		mSearchBoxes = searchBoxes;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	public void setLinkHighlighting(boolean f) {
		mHighlightLinks = f;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	public void deselectText() {
		mSelectBox = null;
		mSearchView.invalidate();
	}

	public void selectText(float x0, float y0, float x1, float y1) {
		float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
		float docRelX0 = (x0 - getLeft())/scale;
		float docRelY0 = (y0 - getTop())/scale;
		float docRelX1 = (x1 - getLeft())/scale;
		float docRelY1 = (y1 - getTop())/scale;
		// Order on Y but maintain the point grouping
		if (docRelY0 <= docRelY1)
			mSelectBox = new RectF(docRelX0, docRelY0, docRelX1, docRelY1);
		else
			mSelectBox = new RectF(docRelX1, docRelY1, docRelX0, docRelY0);

		mSearchView.invalidate();

		if (mGetText == null) {
			mGetText = new AsyncTask<Void,Void,TextWord[][]>() {
				@Override
				protected TextWord[][] doInBackground(Void... params) {
					return getText();
				}
				@Override
				protected void onPostExecute(TextWord[][] result) {
					mText = result;
					mSearchView.invalidate();
				}
			};

			mGetText.execute();
		}
	}

	public void startDraw(float x, float y) {
		float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
		float docRelX = (x - getLeft())/scale;
		float docRelY = (y - getTop())/scale;
		if (mDrawing == null)
			mDrawing = new ArrayList<ArrayList<PointF>>();

		ArrayList<PointF> arc = new ArrayList<PointF>();
		arc.add(new PointF(docRelX, docRelY));
		mDrawing.add(arc);
		mSearchView.invalidate();
	}

	public void continueDraw(float x, float y) {
		float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
		float docRelX = (x - getLeft())/scale;
		float docRelY = (y - getTop())/scale;

		if (mDrawing != null && mDrawing.size() > 0) {
			ArrayList<PointF> arc = mDrawing.get(mDrawing.size() - 1);
			arc.add(new PointF(docRelX, docRelY));
			mSearchView.invalidate();
		}
	}

	public void cancelDraw() {
		mDrawing = null;
		mSearchView.invalidate();
	}

	protected PointF[][] getDraw() {
		if (mDrawing == null)
			return null;

		PointF[][] path = new PointF[mDrawing.size()][];

		for (int i = 0; i < mDrawing.size(); i++) {
			ArrayList<PointF> arc = mDrawing.get(i);
			path[i] = arc.toArray(new PointF[arc.size()]);
		}

		return path;
	}

	protected void processSelectedText(TextProcessor tp) {
		(new TextSelector(mText, mSelectBox)).select(tp);
	}

	public void setItemSelectBox(RectF rect) {
		mItemSelectBox = rect;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int x, y;
		switch(MeasureSpec.getMode(widthMeasureSpec)) {
		case MeasureSpec.UNSPECIFIED:
			x = mSize.x;
			break;
		default:
			x = MeasureSpec.getSize(widthMeasureSpec);
		}
		switch(MeasureSpec.getMode(heightMeasureSpec)) {
		case MeasureSpec.UNSPECIFIED:
			y = mSize.y;
			break;
		default:
			y = MeasureSpec.getSize(heightMeasureSpec);
		}

		setMeasuredDimension(x, y);

		if (mBusyIndicator != null) {
			int limit = Math.min(mParentSize.x, mParentSize.y)/2;
			mBusyIndicator.measure(MeasureSpec.AT_MOST | limit, MeasureSpec.AT_MOST | limit);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int w  = right-left;
		int h = bottom-top;

		if (mEntire != null) {
			if (mEntire.getWidth() != w || mEntire.getHeight() != h) {
				mEntireMat.setScale(w/(float)mSize.x, h/(float)mSize.y);
				mEntire.setImageMatrix(mEntireMat);
				mEntire.invalidate();
			}
			mEntire.layout(0, 0, w, h);
		}

		if (mSearchView != null) {
			mSearchView.layout(0, 0, w, h);
		}

		if (mPatchViewSize != null) {
			if (mPatchViewSize.x != w || mPatchViewSize.y != h) {
				// Zoomed since patch was created
				mPatchViewSize = null;
				mPatchArea     = null;
				if (mPatch != null) {
					mPatch.setImageBitmap(null);
					mPatch.invalidate();
				}
			} else {
				mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
			}
		}

		if (mBusyIndicator != null) {
			int bw = mBusyIndicator.getMeasuredWidth();
			int bh = mBusyIndicator.getMeasuredHeight();

			mBusyIndicator.layout((w-bw)/2, (h-bh)/2, (w+bw)/2, (h+bh)/2);
		}
	}

	public void updateHq(boolean update) {
		Rect viewArea = new Rect(getLeft(),getTop(),getRight(),getBottom());
		if (viewArea.width() == mSize.x || viewArea.height() == mSize.y) {
			// If the viewArea's size matches the unzoomed size, there is no need for an hq patch
			if (mPatch != null) {
				mPatch.setImageBitmap(null);
				mPatch.invalidate();
			}
		} else {
			Point patchViewSize = new Point(viewArea.width(), viewArea.height());
			Rect patchArea = new Rect(0, 0, mParentSize.x, mParentSize.y);

			// Intersect and test that there is an intersection
			if (!patchArea.intersect(viewArea))
				return;

			// Offset patch area to be relative to the view top left
			patchArea.offset(-viewArea.left, -viewArea.top);

			boolean area_unchanged = patchArea.equals(mPatchArea) && patchViewSize.equals(mPatchViewSize);

			// If being asked for the same area as last time and not because of an update then nothing to do
			if (area_unchanged && !update)
				return;

			boolean completeRedraw = !(area_unchanged && update);

			// Stop the drawing of previous patch if still going
			if (mDrawPatch != null) {
				mDrawPatch.cancel(true);
				mDrawPatch = null;
			}

			// Create and add the image view if not already done
			if (mPatch == null) {
				mPatch = new OpaqueImageView(mContext);
				mPatch.setScaleType(ImageView.ScaleType.MATRIX);
				addView(mPatch);
				mSearchView.bringToFront();
			}

			mDrawPatch = new AsyncTask<PatchInfo,Void,PatchInfo>() {
				protected PatchInfo doInBackground(PatchInfo... v) {
					if (v[0].completeRedraw) {
						drawPage(mPatchBm, v[0].patchViewSize.x, v[0].patchViewSize.y,
									v[0].patchArea.left, v[0].patchArea.top,
									v[0].patchArea.width(), v[0].patchArea.height());
					} else {
						updatePage(mPatchBm, v[0].patchViewSize.x, v[0].patchViewSize.y,
									v[0].patchArea.left, v[0].patchArea.top,
									v[0].patchArea.width(), v[0].patchArea.height());
					}

					return v[0];
				}

				protected void onPostExecute(PatchInfo v) {
					mPatchViewSize = v.patchViewSize;
					mPatchArea     = v.patchArea;
					mPatch.setImageBitmap(mPatchBm);
					mPatch.invalidate();
					//requestLayout();
					// Calling requestLayout here doesn't lead to a later call to layout. No idea
					// why, but apparently others have run into the problem.

					mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
                    PageView pageView = PageView.this;
                    ArrayList<View> gpAnnotations = pageView.getGPAnnotations(pageView);
                    for (View view : gpAnnotations){
                        view.bringToFront();
                    }
				}
			};

			mDrawPatch.execute(new PatchInfo(patchViewSize, patchArea, completeRedraw));
		}
	}

	public void update() {

        // Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

		// Render the page in the background
		mDrawEntire = new AsyncTask<Void,Void,Void>() {
			protected Void doInBackground(Void... v) {
                updatePage(mEntireBm, mSize.x, mSize.y, 0, 0, mSize.x, mSize.y);
				return null;
			}

			protected void onPostExecute(Void v) {
				mEntire.setImageBitmap(mEntireBm);
				mEntire.invalidate();
			}
		};

		mDrawEntire.execute();

		updateHq(true);
	}

	public void removeHq() {
			// Stop the drawing of the patch if still going
			if (mDrawPatch != null) {
				mDrawPatch.cancel(true);
				mDrawPatch = null;
			}

			// And get rid of it
			mPatchViewSize = null;
			mPatchArea = null;
			if (mPatch != null) {
				mPatch.setImageBitmap(null);
				mPatch.invalidate();
			}
	}

	public int getPage() {
		return mPageNumber;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}


}
