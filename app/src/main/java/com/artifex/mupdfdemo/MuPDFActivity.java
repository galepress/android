package com.artifex.mupdfdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.artifex.mupdfdemo.ReaderView.ViewMapper;

import net.simonvt.menudrawer.MenuDrawer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.MainActivity;
import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.custom_models.TabbarItem;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_Statistic;
import ak.detaysoft.galepress.util.ApplicationThemeColor;
import ak.detaysoft.galepress.util.ReaderTabbarStateListDrawable;

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class MuPDFActivity extends Activity implements FilePicker.FilePickerSupport {
    /* The core rendering instance */
    enum TopBarMode {
        Main, Search, Annot, Delete, More, Accept
    }

    ;

    enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText}

    ;

    private final int OUTLINE_REQUEST = 0;
    private final int PRINT_REQUEST = 1;
    private final int FILEPICK_REQUEST = 2;
    public MuPDFCore core;
    private String mFileName;
    public MuPDFReaderView mDocView;
    private View mButtonsView;
    private boolean mButtonsVisible;
    private EditText mPasswordView;
    private TextView mFilenameView;
    //private SeekBar      mPageSlider;
    private int mPageSliderRes;
    //private TextView     mPageNumberView;
    private TextView mInfoView;
    private ImageButton mSearchButton;
    private ImageButton mOutlineButton;
    private ImageButton mailButton;
    private ImageButton mReflowButton;
    private ImageButton mMoreButton;
    private TextView mAnnotTypeText;
    private ImageButton mAnnotButton;
    private ViewAnimator mTopBarSwitcher;
    private ImageButton mLinkButton;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private AcceptMode mAcceptMode;
    private ImageButton mSearchBack;
    private ImageButton mSearchFwd;
    private EditText mSearchText;
    private SearchTask mSearchTask;
    private LinearLayout bottomTabBar;
    private AlertDialog.Builder mAlertBuilder;
    private boolean mLinkHighlight = true;
    private final Handler mHandler = new Handler();
    private boolean mAlertsActive = false;
    private boolean mReflow = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private FilePicker mFilePicker;
    private int mOrientation;
    public L_Content content;
    public Bundle savedInstanceState;
    private boolean isActivityActive = false;
    private MenuDrawer mDrawer;

    //private ThumnailHorizontalLayout mPreview;
    private ThumnailHorizontalListView mPreview;
    private RelativeLayout bottomButton;
    private ImageView bottomButtonImg1;
    private RelativeLayout bottomButtonImg2;
    private RelativeLayout mPreviewBarHolder;
    //private CustomThumnailAdapter thumnailAdapter;
    private ThumnailListAdapter thumnailAdapter;

    private Animation thumnailAnimVisible;
    private Animation thumnailAnimInvisible;
    private int thumnailAnimStartPoint;
    private int thumnailAnimEndPoint;
    private AnimationSet sInVisible;
    private AnimationSet sVisible;
    private boolean isHomeOpen = false;
    private int lastPortraitPageIndex = -1;


    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            core.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        int penultimateSlashPos = (path.substring(0, path.lastIndexOf("/"))).lastIndexOf('/');
        mFileName = new String((lastSlashPos == -1 ||penultimateSlashPos == -1)
                ? path
                : path.substring(penultimateSlashPos + 1, lastSlashPos));
        System.out.println("Trying to open " + path);
        try {
            core = new MuPDFCore(this, path, content);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[]) {
        System.out.println("Trying to open byte buffer");
        try {
            core = new MuPDFCore(this, buffer);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return core;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.content = (L_Content) intent.getSerializableExtra("content");

        mAlertBuilder = new AlertDialog.Builder(this);

        if (getIntent().hasExtra("isHomeOpen"))
            isHomeOpen = getIntent().getBooleanExtra("isHomeOpen", false);

        if (core == null) {
            core = (MuPDFCore) getLastNonConfigurationInstance();

            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName");
            }
        }
        if (core == null) {

            byte buffer[] = null;
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri.toString().startsWith("content://")) {
                    // Handle view requests from the Transformer Prime's file manager
                    // Hopefully other file managers will use this same scheme, if not
                    // using explicit paths.
                    Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
                    if (cursor.moveToFirst()) {
                        String str = cursor.getString(0);
                        String reason = null;
                        if (str == null) {
                            try {
                                InputStream is = getContentResolver().openInputStream(uri);
                                int len = is.available();
                                buffer = new byte[len];
                                is.read(buffer, 0, len);
                                is.close();
                            } catch (OutOfMemoryError e) {
                                System.out.println("Out of memory during buffer reading");
                                reason = e.toString();
                            } catch (Exception e) {
                                reason = e.toString();
                            }
                            if (reason != null) {
                                buffer = null;
                                Resources res = getResources();
                                AlertDialog alert = mAlertBuilder.create();
                                setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
                                alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                alert.show();
                                return;
                            }
                        } else {
                            uri = Uri.parse(str);
                        }
                    }
                }
                if (buffer != null) {
                    core = openBuffer(buffer);
                } else {
                    core = openFile(Uri.decode(uri.getEncodedPath()));
                }
                SearchTaskResult.set(null);
            }
            if (core != null && core.needsPassword()) {
                requestPassword(savedInstanceState);
                return;
            }
            if (core != null && core.countPages() == 0) {
                core = null;
            }
        }
        if (core == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.show();
            return;
        }

        if (this.content == null)
            this.content = (L_Content) getIntent().getSerializableExtra("content");

        mOrientation = getResources().getConfiguration().orientation;

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            core.setDisplayPages(2);
        } else {
            core.setDisplayPages(1);
        }

        isActivityActive = true;

        createUI(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (content != null) {
            if (this.content == null)
                this.content = (L_Content) getIntent().getSerializableExtra("content");

            if (this.content.getContentOrientation() == 1) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                mOrientation = Configuration.ORIENTATION_LANDSCAPE;

            } else if (this.content.getContentOrientation() == 2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                mOrientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                mOrientation = getResources().getConfiguration().orientation;
            }
        }

        //uygulamaya tekrar açıldığında
        if (!isActivityActive && (MuPDFPageView) mDocView.getChildAt(0) != null) {
            ((MuPDFPageView) mDocView.getChildAt(0)).resumeCurrentPageWebAnnotationsMedia();
            ((MuPDFPageView) mDocView.getChildAt(0)).resumeTimers();
            isActivityActive = true;
        }
    }

    public void requestPassword(final Bundle savedInstanceState) {
        mPasswordView = new EditText(this);
        mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog alert = mAlertBuilder.create();
        alert.setTitle(R.string.enter_password);
        alert.setView(mPasswordView);
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (core.authenticatePassword(mPasswordView.getText().toString())) {
                            createUI(savedInstanceState);
                        } else {
                            requestPassword(savedInstanceState);
                        }
                    }
                });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alert.show();
    }

    public void createUI(Bundle savedInstanceState) {
        if (core == null)
            return;

        // Now create the UI.
        // First create the document view
        mDocView = new MuPDFReaderView(this) {

            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;

                /*mPageNumberView.setText(String.format("%d / %d", i + 1,
                        core.countPages()));

                mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);*/


                final MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getDisplayedView();
                if (muPDFPageView != null && muPDFPageView.mGetLinkInfo != null) {
                    if (muPDFPageView.mGetLinkInfo.getStatus() != AsyncTask.Status.FINISHED) {
                        if (muPDFPageView.mGetLinkInfo.getStatus() == AsyncTask.Status.RUNNING) {
                            muPDFPageView.mGetLinkInfo.cancel(true);
                            muPDFPageView.mGetLinkInfo = muPDFPageView.getNewLinkInfoTask();
                        }
                        muPDFPageView.mGetLinkInfo.execute();
                    }
                } else {
                    // Eger muPDFpageView null ise (Content ilk acildigi durumlarda oluyor) 1 sn sonra webView'leri load ediyorum.

                    Runnable mMyRunnable = new Runnable() {
                        @Override
                        public void run() {
                            final MuPDFPageView muPDFPageView2 = (MuPDFPageView) mDocView.getDisplayedView();

                            if (muPDFPageView2 != null) {
                                //AsyncTask null oldugu icin ilk sayfanin annotationlar yuklenmiyordu (MG)
                                if (muPDFPageView2.mGetLinkInfo == null) ;
                                muPDFPageView2.mGetLinkInfo = muPDFPageView2.getNewLinkInfoTask();

                                if (muPDFPageView2.mGetLinkInfo.getStatus() != AsyncTask.Status.FINISHED) {
                                    if (muPDFPageView2.mGetLinkInfo.getStatus() == AsyncTask.Status.RUNNING) {
                                        muPDFPageView2.mGetLinkInfo.cancel(true);
                                        muPDFPageView2.mGetLinkInfo = muPDFPageView2.getNewLinkInfoTask();
                                    }
                                    muPDFPageView2.mGetLinkInfo.execute();
                                }
                            }
                        }
                    };
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(mMyRunnable, 500);
                }

                super.onMoveToChild(i);
            }

            @Override
            protected void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    if (mTopBarMode == TopBarMode.Main)
                        hideButtons();
                }
            }

            @Override
            protected void onDocMotion() {
                hideButtons();
            }

            @Override
            protected void onHit(Hit item) {
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        // fall through
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
                        if (pageView != null)
                            pageView.deselectAnnotation();
                        break;
                }
            }
        };
        mDocView.setBackgroundColor(ApplicationThemeColor.getInstance().getReverseThemeColor());
        mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                mDocView.setDisplayedViewIndex(result.pageNumber);

                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView.resetupChildren();
            }
        };

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView();

        // Set up the page slider
        int smax = Math.max(core.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        // Set the file-name text
        mFilenameView.setText(mFileName);

        // Activate the seekbar
        /*mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
            }
        });*/

        // Activate the search-preparing button
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOn();
            }
        });

        mailButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*Activity activity = MuPDFActivity.this;
                String filePath = Environment.getExternalStorageDirectory() + File.separator + "Pictures/screenshot.png";
                sendMail(activity, filePath);*/

                cropAndShareCurrentPage();
            }
        });

        // Activate the reflow button
        mReflowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleReflow();
            }
        });

        if (core.fileFormat().startsWith("PDF")) {
            mAnnotButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mTopBarMode = TopBarMode.Annot;
                    mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                }
            });
        } else {
            mAnnotButton.setVisibility(View.GONE);
        }

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
        mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

        // React to interaction with the text widget
        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                setButtonEnabled(mSearchBack, haveText);
                setButtonEnabled(mSearchFwd, haveText);

                // Remove any previous search results
                if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    mDocView.resetupChildren();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        //React to Done button on keyboard
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    search(1);
                return false;
            }
        });

        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                    search(1);
                return false;
            }
        });

        // Activate search invoking buttons
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(-1);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(1);
            }
        });

        mLinkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLinkHighlight(!mLinkHighlight);
            }
        });

        if (core.hasOutline()) {
            mOutlineButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mDrawer.isMenuVisible())
                        mDrawer.closeMenu(true);
                    else
                        mDrawer.openMenu(true);
                }
            });
        } else {
            mOutlineButton.setVisibility(View.GONE);
        }

        // Reenstate last state if it was recorded
        SharedPreferences prefs = getSharedPreferences("pages", Context.MODE_PRIVATE);
        int viewIndex = prefs.getInt("page" + mFileName, 0);
        lastPortraitPageIndex = prefs.getInt("lastPortraitPageIndex" + mFileName, viewIndex);


        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mDocView.setDisplayedViewIndex(core.convertIndexes(viewIndex, lastPortraitPageIndex, true));
        } else {
            lastPortraitPageIndex = viewIndex;
            mDocView.setDisplayedViewIndex(viewIndex);
        }


		/*if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
            showButtons();*/

        if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
            searchModeOn();

        if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
            reflowModeSet(true);


        if (core.hasOutline()) {
            mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
            RelativeLayout layout = new RelativeLayout(this);
            layout.addView(mDocView);
            layout.addView(mButtonsView);

            mDrawer.setContentView(layout);
            mDrawer.setMenuView(R.layout.reader_left_layout);
            mDrawer.setDropShadowColor(Color.LTGRAY);
            mDrawer.setDropShadowSize(2);
            mDrawer.setMenuSize((int) getResources().getDimension(R.dimen.reader_left_menu_size));

            ListView leftList = (ListView) findViewById(R.id.reader_left_menu_listView);
            leftList.setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());
            leftList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mDrawer.closeMenu();
                    int resultCode = core.getOutline()[position].page;
                    if (core.getDisplayPages() == 2) {
                        resultCode = (core.getOutline()[position].page + 1) / 2;
                    }
                    mDocView.setDisplayedViewIndex(resultCode);
                    scrollToLastThumnail(mDocView.getDisplayedViewIndex());
                }
            });
            leftList.setAdapter(new OutlineAdapter(this, getLayoutInflater(), core.getOutline()));

        } else {
            // Stick the document view and the buttons overlay into a parent view
            RelativeLayout layout = new RelativeLayout(this);
            layout.addView(mDocView);
            layout.addView(mButtonsView);
            setContentView(layout);
        }


        hideButtonsFast();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode >= 0) {
                    if (core.getDisplayPages() == 2) {
                        resultCode = (resultCode + 1) / 2;
                    }
                    mDocView.setDisplayedViewIndex(resultCode);
                }
                break;
            case PRINT_REQUEST:
                if (resultCode == RESULT_CANCELED)
                    showInfo(getString(R.string.print_failed));
                break;
            case FILEPICK_REQUEST:
                if (mFilePicker != null && resultCode == RESULT_OK)
                    mFilePicker.onPick(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Object onRetainNonConfigurationInstance() {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    private void reflowModeSet(boolean reflow) {
        mReflow = reflow;
        mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
        mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
        setButtonEnabled(mAnnotButton, !reflow);
        setButtonEnabled(mSearchButton, !reflow);
        if (reflow) setLinkHighlight(false);
        setButtonEnabled(mLinkButton, !reflow);
        setButtonEnabled(mMoreButton, !reflow);
        mDocView.refresh(mReflow);
    }

    private void toggleReflow() {
        reflowModeSet(!mReflow);
        showInfo(mReflow ? getString(R.string.entering_reflow_mode) : getString(R.string.leaving_reflow_mode));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mFileName != null && mDocView != null) {
            outState.putString("FileName", mFileName);

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            SharedPreferences prefs = getSharedPreferences("pages",Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            int viewIndex = mDocView.getDisplayedViewIndex();
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewIndex = core.convertIndexes(viewIndex, lastPortraitPageIndex, false);
            } else {
                lastPortraitPageIndex = viewIndex;
            }

            edit.putInt("page" + mFileName, viewIndex);
            edit.putInt("lastPortraitPageIndex" + mFileName, lastPortraitPageIndex);
            edit.commit();
        }

        if (!mButtonsVisible)
            outState.putBoolean("ButtonsHidden", true);

        if (mTopBarMode == TopBarMode.Search)
            outState.putBoolean("SearchMode", true);

        if (mReflow)
            outState.putBoolean("ReflowMode", true);
    }

    @Override
    protected void onPause() {
        try{
            Logout.e("Adem", "onPause");
            super.onPause();

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            boolean isScreenOn = powerManager.isScreenOn();

            if (!isScreenOn) {
                isActivityActive = false;
                /*if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null) {
                    ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                    ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                    ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
                }*/

                if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
                    ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                    ((MuPDFPageView) mDocView.getChildAt(0)).pauseTimers();
                }
            }

            if (mSearchTask != null)
                mSearchTask.stop();

            if (mFileName != null && mDocView != null) {
                SharedPreferences prefs = getSharedPreferences("pages", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();

                int viewIndex = mDocView.getDisplayedViewIndex();
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    viewIndex = core.convertIndexes(viewIndex, lastPortraitPageIndex, false);
                } else {
                    lastPortraitPageIndex = viewIndex;
                }

                edit.putInt("page" + mFileName, viewIndex);
                edit.putInt("lastPortraitPageIndex" + mFileName, lastPortraitPageIndex);
                edit.commit();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDestroy() {

        try{
            if (mDocView != null) {
                mDocView.applyToChildren(new ViewMapper() {
                    void applyToView(View view) {
                        ((MuPDFView) view).releaseBitmaps();
                    }
                });
            }

            if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null) {
                ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
            }


        /*for(int i =0; i < mDocView.getChildCount(); i++){
            MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getChildAt(i);
            muPDFPageView.clearWebAnnotations(muPDFPageView);
        }*/
            if (core != null)
                core.onDestroy();
            if (mAlertTask != null) {
                mAlertTask.cancel(true);
                mAlertTask = null;
            }
            core = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
            ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
            ((MuPDFPageView) mDocView.getChildAt(0)).pauseTimers();
        }

        isActivityActive = false;
        super.onUserLeaveHint();
    }

    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color.argb(255, 128, 128, 128));
    }

    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
        mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        mDocView.setLinksEnabled(highlight);
    }

    private void showButtons() {
        if (core == null)
            return;
        if (!mButtonsVisible && !content.isMaster()) {

            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            //mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
            //mPageSlider.setProgress(index*mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                mSearchText.requestFocus();
                showKeyboard();
            }

            Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);

                    /*anim = new TranslateAnimation(0, 0, mPageSlider.getHeight() + bottomTabBar.getHeight(), 0);
                    anim.setDuration(200);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                            mPageSlider.setVisibility(View.VISIBLE);
                        }
                        public void onAnimationRepeat(Animation animation) {}
                        public void onAnimationEnd(Animation animation) {
                            mPageNumberView.setVisibility(View.VISIBLE);
                        }
                    });
                    mPageSlider.startAnimation(anim);*/

            anim = new TranslateAnimation(0, 0, bottomTabBar.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomTabBar.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {

                }
            });
            bottomTabBar.startAnimation(anim);

            scrollToLastThumnail(mDocView.getDisplayedViewIndex());
            anim = new TranslateAnimation(0, 0, bottomTabBar.getHeight() + mPreviewBarHolder.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mPreviewBarHolder.startAnimation(anim);

            TranslateAnimation menuAnim = new TranslateAnimation(0, 0, 0, bottomButton.getHeight());
            menuAnim.setStartOffset(200);
            menuAnim.setDuration(250);
            menuAnim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomButton.setVisibility(View.GONE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {


                }
            });
            bottomButton.startAnimation(menuAnim);
        }
    }

    private void showButtonsFast() {
        if (core == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = mDocView.getDisplayedViewIndex();
            updatePageNumView(index);
            //mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
            //mPageSlider.setProgress(index*mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                mSearchText.requestFocus();
                showKeyboard();
            }

            Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            /*anim = new TranslateAnimation(0, 0, mPageSlider.getHeight() + bottomTabBar.getHeight(), 0);
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageSlider.setVisibility(View.VISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);*/

            anim = new TranslateAnimation(0, 0, bottomTabBar.getHeight(), 0);
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomTabBar.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {

                }
            });
            bottomTabBar.startAnimation(anim);


            scrollToLastThumnail(mDocView.getDisplayedViewIndex());
            anim = new TranslateAnimation(0, 0, bottomTabBar.getHeight() + mPreviewBarHolder.getHeight(), 0);
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mPreviewBarHolder.startAnimation(anim);

            TranslateAnimation menuAnim = new TranslateAnimation(0, 0, 0, bottomButton.getHeight());
            menuAnim.setDuration(250);
            menuAnim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomButton.setVisibility(View.GONE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            bottomButton.startAnimation(menuAnim);
        }
    }

    public void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            hideKeyboard();

            Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            /*anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight() + bottomTabBar.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    mPageSlider.setVisibility(View.INVISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);*/

            anim = new TranslateAnimation(0, 0, 0, bottomTabBar.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomTabBar.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {

                }
            });
            bottomTabBar.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, 0, bottomTabBar.getHeight() + this.mPreviewBarHolder.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mPreviewBarHolder.startAnimation(anim);

            TranslateAnimation menuAnim = new TranslateAnimation(0, 0, bottomButton.getHeight(), 0);
            menuAnim.setStartOffset(200);
            menuAnim.setDuration(250);
            menuAnim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomButton.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    scaleAnimation();
                }
            });
            bottomButton.startAnimation(menuAnim);
        }
    }

    private void scaleAnimation() {
        final int startTime = 0;
        final int durationTime = 600;

        ScaleAnimation s11 = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        s11.setFillAfter(true);
        s11.setStartOffset(startTime);
        s11.setDuration(durationTime);
        s11.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ScaleAnimation s12 = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                s12.setFillAfter(true);
                s12.setStartOffset(startTime);
                s12.setDuration(durationTime);
                s12.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ScaleAnimation s21 = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        s21.setFillAfter(true);
                        s21.setStartOffset(startTime);
                        s21.setDuration(durationTime);
                        s21.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ScaleAnimation s22 = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                s22.setFillAfter(true);
                                s22.setStartOffset(startTime);
                                s22.setDuration(durationTime);
                                s22.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        ScaleAnimation s31 = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        s31.setFillAfter(true);
                                        s31.setStartOffset(startTime);
                                        s31.setDuration(durationTime);
                                        s31.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                ScaleAnimation s32 = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                                s32.setFillAfter(true);
                                                s32.setStartOffset(startTime);
                                                s32.setDuration(durationTime);
                                                s32.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {

                                                    }
                                                });
                                                bottomButtonImg2.startAnimation(s32);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                        bottomButtonImg2.startAnimation(s31);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                bottomButtonImg2.startAnimation(s22);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        bottomButtonImg2.startAnimation(s21);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                bottomButtonImg2.startAnimation(s12);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomButtonImg2.startAnimation(s11);


    }

    private void hideButtonsFast() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            hideKeyboard();

            Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            /*anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight() + bottomTabBar.getHeight());
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    mPageSlider.setVisibility(View.INVISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);*/

            anim = new TranslateAnimation(0, 0, 0, bottomTabBar.getHeight());
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomTabBar.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {

                }
            });
            bottomTabBar.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, 0, bottomTabBar.getHeight() + this.mPreviewBarHolder.getHeight());
            anim.setDuration(0);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPreviewBarHolder.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mPreviewBarHolder.startAnimation(anim);
        }

        if (!content.isMaster()) {
            TranslateAnimation menuAnim = new TranslateAnimation(0, 0, bottomButton.getHeight(), 0);
            menuAnim.setStartOffset(500);
            menuAnim.setDuration(250);
            menuAnim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    bottomButton.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {

                    scaleAnimation();
                }
            });
            bottomButton.startAnimation(menuAnim);
        }
    }

    private void searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search;
            //Focus on EditTextWidget
            mSearchText.requestFocus();
            showKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        }
    }

    private static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                System.out.println("b is:" + b);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMail(Activity a, String b) {
        hideButtonsFast();
        savePic(takeScreenShot(a), b);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_SUBJECT, " ");
        intent.putExtra(Intent.EXTRA_TEXT, " ");
        Uri myUri = Uri.parse("file://" + b);
        intent.putExtra(Intent.EXTRA_STREAM, myUri);
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
        showButtonsFast();
    }

    public void cropAndShareCurrentPage(){

        hideButtonsFast();
        bottomButton.setVisibility(View.INVISIBLE);
        byte[] bytes;
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            takeScreenShot(MuPDFActivity.this).compress(Bitmap.CompressFormat.PNG, 100, stream);
            bytes =  stream.toByteArray();
            Intent intent = new Intent(MuPDFActivity.this, CropAndShareActivity.class);
            intent.putExtra("cropImage", bytes);
            int display_mode = getResources().getConfiguration().orientation;
            intent.putExtra("displayMode", display_mode);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(MuPDFActivity.this, MuPDFActivity.this.getResources().getString(R.string.cannot_open_crop), Toast.LENGTH_SHORT).show();
        }
        bottomButton.setVisibility(View.VISIBLE);
        showButtonsFast();

    }


    private static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }


    private void searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main;
            hideKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
            SearchTaskResult.set(null);
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            mDocView.resetupChildren();
        }
    }

    private void updatePageNumView(int index) {
        if (core == null)
            return;
        //mPageNumberView.setText(String.format("%d / %d", index+1, core.countPages()));
    }

    private void printDoc() {
        if (!core.fileFormat().startsWith("PDF")) {
            showInfo(getString(R.string.format_currently_not_supported));
            return;
        }

        Intent myIntent = getIntent();
        Uri docUri = myIntent != null ? myIntent.getData() : null;

        if (docUri == null) {
            showInfo(getString(R.string.print_failed));
        }

        if (docUri.getScheme() == null)
            docUri = Uri.parse("file://" + docUri.toString());

        Intent printIntent = new Intent(this, PrintDialogActivity.class);
        printIntent.setDataAndType(docUri, "aplication/pdf");
        printIntent.putExtra("title", mFileName);
        startActivityForResult(printIntent, PRINT_REQUEST);
    }

    private void showInfo(String message) {
        mInfoView.setText(message);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            SafeAnimatorInflater safe = new SafeAnimatorInflater((Activity) this, R.animator.info, (View) mInfoView);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mInfoView.setVisibility(View.INVISIBLE);
                }
            }, 500);
        }
    }

    public void scrollToLastThumnail(int displayIndex) {
        for (int i = 0; i < thumnailAdapter.getCount(); i++) {
            if (i == displayIndex)
                thumnailAdapter.pageNumberList.get(i).setVisibility(View.VISIBLE);
            else
                thumnailAdapter.pageNumberList.get(i).setVisibility(View.INVISIBLE);
        }
        mPreview.scrollToItem(displayIndex);
    }

    private void setInvisibleAllTextView(int selectedIndex) {
        for (int i = selectedIndex - 10; i < selectedIndex + 10; i++) {
            if (i >= 0 && i < thumnailAdapter.getCount())
                thumnailAdapter.pageNumberList.get(i).setVisibility(View.INVISIBLE);
        }
    }

    public void startThumnailLeftAnim(final int id) {

        final int currentIndex = mDocView.getDisplayedViewIndex();
        final int selectedIndex = (int) id;
        int durationTime = 100;

        try {

            sInVisible = new AnimationSet(false);
            thumnailAnimStartPoint = (((RelativeLayout) thumnailAdapter.pageNumberList.get(currentIndex).getParent()).getWidth()
                    - thumnailAdapter.pageNumberList.get(currentIndex).getWidth()) / 2;
            thumnailAnimEndPoint = -(thumnailAnimStartPoint + thumnailAdapter.pageNumberList.get(currentIndex).getWidth());

            thumnailAnimInvisible = new TranslateAnimation(0, thumnailAnimEndPoint, 0, 0);
            thumnailAnimInvisible.setDuration(100);
            thumnailAnimInvisible.setInterpolator(new AccelerateInterpolator());
            thumnailAnimInvisible.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    thumnailAdapter.pageNumberList.get(currentIndex).setVisibility(View.INVISIBLE);

                }
            });

            AlphaAnimation alphaAmin = new AlphaAnimation(1, 0);
            alphaAmin.setDuration(50);

            sInVisible.addAnimation(alphaAmin);
            sInVisible.addAnimation(thumnailAnimInvisible);

            thumnailAdapter.pageNumberList.get(currentIndex).startAnimation(sInVisible);
        } catch (Exception e) {
            durationTime = 0;
        }

        try {
            sVisible = new AnimationSet(false);

            thumnailAnimStartPoint = ((RelativeLayout) thumnailAdapter.pageNumberList.get(selectedIndex).getParent()).getWidth();
            thumnailAnimEndPoint = ((RelativeLayout) thumnailAdapter.pageNumberList.get(selectedIndex).getParent()).getWidth() / 2;

            thumnailAnimVisible = new TranslateAnimation(thumnailAnimStartPoint, 0, 0, 0);
            thumnailAnimVisible.setDuration(100);
            thumnailAnimVisible.setStartOffset(durationTime);
            thumnailAnimVisible.setInterpolator(new DecelerateInterpolator());

            AlphaAnimation alphaAmin = new AlphaAnimation(0, 1);
            alphaAmin.setDuration(150);
            alphaAmin.setStartOffset(thumnailAnimVisible.getStartOffset());
            alphaAmin.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mDocView.setDisplayedViewIndex(selectedIndex);
                    setInvisibleAllTextView(selectedIndex);
                    thumnailAdapter.pageNumberList.get(selectedIndex).setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            sVisible.addAnimation(alphaAmin);
            sVisible.addAnimation(thumnailAnimVisible);

            thumnailAdapter.pageNumberList.get(selectedIndex).startAnimation(sVisible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startThumnailRightAnim(final int id) {

        final int currentIndex = mDocView.getDisplayedViewIndex();
        final int selectedIndex = (int) id;
        int durationTime = 100;

        try {

            AnimationSet sInVisible = new AnimationSet(false);

            thumnailAnimStartPoint = (((RelativeLayout) thumnailAdapter.pageNumberList.get(currentIndex).getParent()).getWidth()
                    - thumnailAdapter.pageNumberList.get(currentIndex).getWidth()) / 2;
            thumnailAnimEndPoint = ((RelativeLayout) thumnailAdapter.pageNumberList.get(currentIndex).getParent()).getWidth();


            thumnailAnimInvisible = new TranslateAnimation(0, thumnailAnimEndPoint, 0, 0);
            thumnailAnimInvisible.setDuration(100);
            thumnailAnimInvisible.setInterpolator(new AccelerateInterpolator());
            thumnailAnimInvisible.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    thumnailAdapter.pageNumberList.get(currentIndex).setVisibility(View.INVISIBLE);
                }
            });

            AlphaAnimation alphaAmin = new AlphaAnimation(1, 0);
            alphaAmin.setDuration(50);

            sInVisible.addAnimation(alphaAmin);
            sInVisible.addAnimation(thumnailAnimInvisible);

            thumnailAdapter.pageNumberList.get(currentIndex).clearAnimation();
            thumnailAdapter.pageNumberList.get(currentIndex).startAnimation(sInVisible);
        } catch (Exception e) {
            durationTime = 0;
        }

        try {
            AnimationSet sVisible = new AnimationSet(false);

            thumnailAnimStartPoint = -((((RelativeLayout) thumnailAdapter.pageNumberList.get(selectedIndex).getParent()).getWidth()
                    - thumnailAdapter.pageNumberList.get(selectedIndex).getWidth()) / 2
                    + thumnailAdapter.pageNumberList.get(selectedIndex).getWidth());
            thumnailAnimEndPoint = (((RelativeLayout) thumnailAdapter.pageNumberList.get(selectedIndex).getParent()).getWidth()
                    - thumnailAdapter.pageNumberList.get(selectedIndex).getWidth()) / 2;

            thumnailAnimVisible = new TranslateAnimation(thumnailAnimStartPoint, 0, 0, 0);
            thumnailAnimVisible.setDuration(100);
            thumnailAnimVisible.setStartOffset(durationTime);
            thumnailAnimVisible.setInterpolator(new DecelerateInterpolator());

            AlphaAnimation alphaAmin = new AlphaAnimation(0, 1);
            alphaAmin.setDuration(150);
            alphaAmin.setStartOffset(durationTime);
            alphaAmin.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mDocView.setDisplayedViewIndex(selectedIndex);
                    setInvisibleAllTextView(selectedIndex);
                    thumnailAdapter.pageNumberList.get(selectedIndex).setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            sVisible.addAnimation(alphaAmin);
            sVisible.addAnimation(thumnailAnimVisible);

            thumnailAdapter.pageNumberList.get(selectedIndex).clearAnimation();
            thumnailAdapter.pageNumberList.get(selectedIndex).startAnimation(sVisible);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeButtonsView() {

        if (core == null)
            return;

        mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);
        mFilenameView = (TextView) mButtonsView.findViewById(R.id.docNameText);
        //mPageSlider = (SeekBar)mButtonsView.findViewById(R.id.pageSlider);
        mPreviewBarHolder = (RelativeLayout) mButtonsView.findViewById(R.id.PreviewBarHolder);
        ImageView divider = (ImageView) mButtonsView.findViewById(R.id.reader_preview_bar_divider);
        divider.setBackgroundColor(ApplicationThemeColor.getInstance().getForegroundColor());
        divider.setAlpha((float) 0.9);
        divider.bringToFront();
        mPreviewBarHolder.setBackgroundColor(Color.TRANSPARENT);

        bottomButton = (RelativeLayout) mButtonsView.findViewById(R.id.reader_bottom_page_button);
        bottomButton.setBackgroundColor(Color.TRANSPARENT);

        bottomButtonImg1 = (ImageView) mButtonsView.findViewById(R.id.reader_bottom_page_img1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            bottomButtonImg1.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN));
        else
            bottomButtonImg1.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN));
        bottomButtonImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButtons();
            }
        });

        bottomButtonImg2 = (RelativeLayout) mButtonsView.findViewById(R.id.reader_bottom_page_img2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            bottomButtonImg2.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN2));
        else
            bottomButtonImg2.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN2));
        bottomButtonImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButtons();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ImageView) mButtonsView.findViewById(R.id.reader_bottom_page_img_ok)).setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN_OK));
        else
            ((ImageView) mButtonsView.findViewById(R.id.reader_bottom_page_img_ok)).setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU_OPEN_OK));

        mPreview = (ThumnailHorizontalListView) mButtonsView.findViewById(R.id.reader_preview_bar_listView);
        mPreview.setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColor());
        thumnailAdapter = new ThumnailListAdapter(this, core, mDocView);
        mPreview.setAdapter(thumnailAdapter);
        //mPreview.setCenter(mDocView.getDisplayedViewIndex());
        mPreview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((int) id > mDocView.getDisplayedViewIndex())
                    startThumnailRightAnim((int) id);
                else if ((int) id < mDocView.getDisplayedViewIndex())
                    startThumnailLeftAnim((int) id);
            }
        });
        bottomTabBar = (LinearLayout) mButtonsView.findViewById(R.id.reader_tabbar);
        bottomTabBar.setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColor());


        //ANA SAYFA
        ((ImageView) mButtonsView.findViewById(R.id.reader_home)).setImageDrawable(createDrawable(false, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON_SELECTED),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.HOME_ICON)));

        TextView reader_home_txt = ((TextView) mButtonsView.findViewById(R.id.reader_home_txt));
        reader_home_txt.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
        reader_home_txt.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        reader_home_txt.setTextColor(createTabTitleColorStateList());
        reader_home_txt.setText(getResources().getString(R.string.HOME));

        ((LinearLayout) mButtonsView.findViewById(R.id.reader_home_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabItemClick(0);
            }
        });

        if (GalePressApplication.getInstance().getDataApi().getMasterContent() == null || !GalePressApplication.getInstance().getDataApi().getMasterContent().isPdfDownloaded())
            ((LinearLayout) mButtonsView.findViewById(R.id.reader_home_layout)).setVisibility(View.GONE);


        //KUTUPHANE
        TextView reader_library_txt = ((TextView) mButtonsView.findViewById(R.id.reader_library_txt));
        reader_library_txt.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
        reader_library_txt.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        reader_library_txt.setText(getResources().getString(R.string.LIBRARY));


        //icerik indirildigi zaman downloaded ekrani aciksa update etmek icin
        if(GalePressApplication.getInstance().getMainActivity() != null){
            MainActivity act = GalePressApplication.getInstance().getMainActivity();
            if(act.mTabHost.getCurrentTabTag().compareTo(MainActivity.LIBRARY_TAB_TAG) == 0){
                ((ImageView) mButtonsView.findViewById(R.id.reader_library)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON),
                        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON_SELECTED)));
                reader_library_txt.setTextColor(createSelectedTabTitleColorStateList());
            } else {
                ((ImageView) mButtonsView.findViewById(R.id.reader_library)).setImageDrawable(createDrawable(false, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON),
                        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.LIBRARY_ICON_SELECTED)));
                reader_library_txt.setTextColor(createTabTitleColorStateList());
            }
        }

        ((LinearLayout) mButtonsView.findViewById(R.id.reader_library_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabItemClick(1);
            }
        });


        //INDIRILENLER
        TextView reader_download_txt = ((TextView) mButtonsView.findViewById(R.id.reader_download_txt));
        reader_download_txt.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
        reader_download_txt.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        reader_download_txt.setText(getResources().getString(R.string.DOWNLOADED));

        //icerik indirildigi zaman downloaded ekrani aciksa update etmek icin
        if(GalePressApplication.getInstance().getMainActivity() != null){
            MainActivity act = GalePressApplication.getInstance().getMainActivity();
            if(act.mTabHost.getCurrentTabTag().compareTo(MainActivity.DOWNLOADED_LIBRARY_TAG) == 0){
                ((ImageView) mButtonsView.findViewById(R.id.reader_download)).setImageDrawable(createDrawable(true, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON),
                        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON_SELECTED)));
                reader_download_txt.setTextColor(createSelectedTabTitleColorStateList());
            } else {
                ((ImageView) mButtonsView.findViewById(R.id.reader_download)).setImageDrawable(createDrawable(false, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON),
                        ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.DOWNLOAD_ICON_SELECTED)));
                reader_download_txt.setTextColor(createTabTitleColorStateList());
            }
        }

        ((LinearLayout) mButtonsView.findViewById(R.id.reader_download_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabItemClick(2);
            }
        });


        //HAKKINDA
        ((ImageView) mButtonsView.findViewById(R.id.reader_info)).setImageDrawable(createDrawable(false, ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON_SELECTED),
                ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.INFO_ICON)));

        TextView reader_info_txt = ((TextView) mButtonsView.findViewById(R.id.reader_info_txt));
        reader_info_txt.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
        reader_info_txt.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        reader_info_txt.setTextColor(createTabTitleColorStateList());
        reader_info_txt.setText(getResources().getString(R.string.INFO));

        ((LinearLayout) mButtonsView.findViewById(R.id.reader_info_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabItemClick(3);
            }
        });

        ((LinearLayout) mButtonsView.findViewById(R.id.reader_info_layout)).setVisibility(View.GONE);

        //Custom tabbar buttons
        if (GalePressApplication.getInstance().getDataApi().isConnectedToInternet() && GalePressApplication.getInstance().getTabList() != null) {
            int index = 0;
            for (TabbarItem item : GalePressApplication.getInstance().getTabList()) {
                LinearLayout layout = new LinearLayout(this);
                layout.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())));
                layout.setGravity(Gravity.CENTER);
                layout.setId(100 + index);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tabItemClick(v.getId());
                    }
                });

                ImageView img = new ImageView(this);
                img.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics())));
                ApplicationThemeColor.getInstance().paintRemoteIcon(this, item, img);
                layout.addView(img);

                TextView txt = new TextView(this);
                txt.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics())));
                txt.setSingleLine();
                txt.setMaxLines(1);
                txt.setEllipsize(TextUtils.TruncateAt.END);
                txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.5f);
                txt.setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
                txt.setTextColor(createTabTitleColorStateList());
                txt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                txt.setText(item.getTitle());
                layout.addView(txt);

                ((LinearLayout) mButtonsView.findViewById(R.id.reader_tabbar)).addView(layout);
                index++;

            }
        }

        //mPageNumberView = (TextView)mButtonsView.findViewById(R.id.pageNumber);
        mInfoView = (TextView) mButtonsView.findViewById(R.id.info);
        mSearchButton = (ImageButton) mButtonsView.findViewById(R.id.searchButton);

        mOutlineButton = (ImageButton) mButtonsView.findViewById(R.id.outlineButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mOutlineButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU));
        else
            mOutlineButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MENU));

        mailButton = (ImageButton) mButtonsView.findViewById(R.id.mailButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mailButton.setBackground(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MAIL));
        else
            mailButton.setBackgroundDrawable(ApplicationThemeColor.getInstance().paintIcons(this, ApplicationThemeColor.READER_MAIL));

        mReflowButton = (ImageButton) mButtonsView.findViewById(R.id.reflowButton);
        mAnnotButton = (ImageButton) mButtonsView.findViewById(R.id.editAnnotButton);
        mAnnotTypeText = (TextView) mButtonsView.findViewById(R.id.annotType);

        mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
        mTopBarSwitcher.setBackgroundColor(ApplicationThemeColor.getInstance().getActionAndTabBarColor());

        ((TextView) mButtonsView.findViewById(R.id.reader_title)).setTextColor(ApplicationThemeColor.getInstance().getForegroundColor());
        ((TextView) mButtonsView.findViewById(R.id.reader_title)).setTypeface(ApplicationThemeColor.getInstance().getOpenSansRegular(this));
        ((TextView) mButtonsView.findViewById(R.id.reader_title)).setText(((TextView) mButtonsView.findViewById(R.id.reader_title)).getText().toString().toUpperCase());

        mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.searchBack);
        mSearchFwd = (ImageButton) mButtonsView.findViewById(R.id.searchForward);
        mSearchText = (EditText) mButtonsView.findViewById(R.id.searchText);
        mLinkButton = (ImageButton) mButtonsView.findViewById(R.id.linkButton);
        mMoreButton = (ImageButton) mButtonsView.findViewById(R.id.moreButton);
        mTopBarSwitcher.setVisibility(View.INVISIBLE);
        //mPageNumberView.setVisibility(View.INVISIBLE);
        mInfoView.setVisibility(View.INVISIBLE);
        //mPageSlider.setVisibility(View.INVISIBLE);
        bottomTabBar.setVisibility(View.INVISIBLE);
        mPreviewBarHolder.setVisibility(View.INVISIBLE);
    }

    private ColorStateList createTabTitleColorStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors = new int[]{
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50),
                ApplicationThemeColor.getInstance().getForegroundColor()
        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    private ColorStateList createSelectedTabTitleColorStateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };

        int[] colors = new int[]{
                ApplicationThemeColor.getInstance().getForegroundColor(),
                ApplicationThemeColor.getInstance().getForegroundColor(),
                ApplicationThemeColor.getInstance().getForegroundColor(),
                ApplicationThemeColor.getInstance().getForegroundColorWithAlpha(50)
        };

        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }

    private void tabItemClick(int type) {
        if (GalePressApplication.getInstance().getContentPopupActivity() != null)
            GalePressApplication.getInstance().getContentPopupActivity().finish();

        if (GalePressApplication.getInstance().getDataApi().isLibraryMustBeEnabled()) {
            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, this.content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentClosed, null, null, null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

            GalePressApplication.getInstance().setMuPDFActivity(null);
            Intent intent = getIntent();
            intent.putExtra("SelectedTab", type);
            setResult(101, intent);
            finish();
        }

    }

    private Drawable createDrawable(boolean isSelected, Drawable res, Drawable selectedRes) {
        ReaderTabbarStateListDrawable states = new ReaderTabbarStateListDrawable(isSelected, res, selectedRes);
        return states;
    }

    public void OnMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.More;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnPrintButtonClick(View v) {
        printDoc();
    }

    public void OnCopyTextButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.CopyText;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(getString(R.string.copy_text));
        showInfo(getString(R.string.select_text));
    }

    public void OnEditAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.More;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnHighlightButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Highlight;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.highlight);
        showInfo(getString(R.string.select_text));
    }

    public void OnUnderlineButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Underline;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.underline);
        showInfo(getString(R.string.select_text));
    }

    public void OnStrikeOutButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.StrikeOut;
        mDocView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.strike_out);
        showInfo(getString(R.string.select_text));
    }

    public void OnInkButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Ink;
        mDocView.setMode(MuPDFReaderView.Mode.Drawing);
        mAnnotTypeText.setText(R.string.ink);
        showInfo(getString(R.string.draw_annotation));
    }

    public void OnCancelAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null) {
            pageView.deselectText();
            pageView.cancelDraw();
        }
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
        switch (mAcceptMode) {
            case CopyText:
                mTopBarMode = TopBarMode.More;
                break;
            default:
                mTopBarMode = TopBarMode.Annot;
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        boolean success = false;
        switch (mAcceptMode) {
            case CopyText:
                if (pageView != null)
                    success = pageView.copySelection();
                mTopBarMode = TopBarMode.More;
                showInfo(success ? getString(R.string.copied_to_clipboard) : getString(R.string.no_text_selected));
                break;

            case Highlight:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case Underline:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case StrikeOut:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case Ink:
                if (pageView != null)
                    success = pageView.saveDraw();
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.nothing_to_save));
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mDocView.setMode(MuPDFReaderView.Mode.Viewing);
    }

    public void OnCancelSearchButtonClick(View v) {
        searchModeOff();
    }

    public void OnDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deleteSelectedAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    public void OnCancelDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deselectAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(mSearchText, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    private void search(int direction) {
        hideKeyboard();
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
    }

    @Override
    public boolean onSearchRequested() {
        if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
            hideButtons();
        } else {
            showButtons();
            searchModeOn();
        }
        return super.onSearchRequested();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
            hideButtons();
        } else {
            if (content != null && !content.isMaster())
                showButtons();
            searchModeOff();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        GalePressApplication.getInstance().setMuPDFActivity(this);
        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        GalePressApplication.getInstance().setMuPDFActivity(null);
        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {

        if (GalePressApplication.getInstance().getDataApi().isLibraryMustBeEnabled()) {
            Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String udid = UUID.randomUUID().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Location location = GalePressApplication.getInstance().location;
            L_Statistic statistic = new L_Statistic(udid, this.content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentClosed, null, null, null);
            GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);


            try {
                if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
                    ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                    ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                    ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
                }


                /*for(int i =0; i < mDocView.getChildCount(); i++){
                    MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getChildAt(i);
                    muPDFPageView.clearWebAnnotations(muPDFPageView);
                }*/
            } catch (Exception e) {
                Log.e("Webview clear", "" + e.toString());
            }

            if (content.isMaster() && isHomeOpen) {
                Intent intent = getIntent();
                intent.putExtra("SelectedTab", 1);
                setResult(101, intent);
                finish();
            } else {
                super.onBackPressed();
            }
        } else {
            if (content.isMaster() && isHomeOpen) {

                try {
                    if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
                        ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                        ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                        ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
                    }


                /*for(int i =0; i < mDocView.getChildCount(); i++){
                    MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getChildAt(i);
                    muPDFPageView.clearWebAnnotations(muPDFPageView);
                }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = getIntent();
                intent.putExtra("SelectedTab", 0);
                setResult(101, intent);
                finish();
            } else {
                return;
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (GalePressApplication.getInstance().getDataApi().isLibraryMustBeEnabled()) {
                        Settings.Secure.getString(GalePressApplication.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        String udid = UUID.randomUUID().toString();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Location location = GalePressApplication.getInstance().location;
                        L_Statistic statistic = new L_Statistic(udid, this.content.getId(), location != null ? location.getLatitude() : null, location != null ? location.getLongitude() : null, null, dateFormat.format(cal.getTime()), L_Statistic.STATISTIC_contentClosed, null, null, null);
                        GalePressApplication.getInstance().getDataApi().commitStatisticsToDB(statistic);

                        try {
                            if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
                                ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                                ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                                ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
                            }


                /*for(int i =0; i < mDocView.getChildCount(); i++){
                    MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getChildAt(i);
                    muPDFPageView.clearWebAnnotations(muPDFPageView);
                }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (content.isMaster() && isHomeOpen) {
                            Intent intent = getIntent();
                            intent.putExtra("SelectedTab", 1);
                            setResult(101, intent);
                            finish();
                        } else {
                            super.onBackPressed();
                        }
                    } else {
                        if (content.isMaster() && isHomeOpen) {

                            try {
                                if(mDocView != null && ((MuPDFPageView) mDocView.getChildAt(0)) != null ) {
                                    ((MuPDFPageView) mDocView.getChildAt(0)).stopAllWebAnnotationsMedia();
                                    ((MuPDFPageView) mDocView.getChildAt(0)).clearWebAnnotations(((MuPDFPageView) mDocView.getChildAt(0)));
                                    ((MuPDFPageView) mDocView.getChildAt(0)).destroyTimers();
                                }


                /*for(int i =0; i < mDocView.getChildCount(); i++){
                    MuPDFPageView muPDFPageView = (MuPDFPageView) mDocView.getChildAt(i);
                    muPDFPageView.clearWebAnnotations(muPDFPageView);
                }*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent intent = getIntent();
                            intent.putExtra("SelectedTab", 0);
                            setResult(101, intent);
                            finish();
                        } else {
                            return true;
                        }

                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void performPickFor(FilePicker picker) {
        mFilePicker = picker;
        Intent intent = new Intent(this, ChoosePDFActivity.class);
        intent.setAction(ChoosePDFActivity.PICK_KEY_FILE);
        startActivityForResult(intent, FILEPICK_REQUEST);
    }
}
