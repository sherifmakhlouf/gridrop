package com.novoda.gridrop;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.android.swipedismiss.SwipeDismissTouchListener;

public class MainActivity extends SherlockActivity {

    private LinearLayout container;
    private LayoutInflater inflater;
    private ScrollView scrollView;
    private View intro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (LinearLayout) findViewById(R.id.container);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        intro = (View) findViewById(R.id.intro);
        inflater = LayoutInflater.from(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_clear) {
            clear();
            return true;
        } else if (id == R.id.menu_share) {
            share();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void clear() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOut.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.removeAllViews();
                if (intro.getVisibility() == View.GONE) {
                    intro.setVisibility(View.VISIBLE);
                }
            }
        });
        container.startAnimation(fadeOut);

    }
    
    public void removeItem(View view) {
        container.removeView(view);
        if(container.getChildCount() == 0) {
            intro.setVisibility(View.VISIBLE);
        }
    }

    public void share() {
        new ScreenshotTask() {
            protected void onPostExecute(File result) {
                try {
                    if (result != null) {
                        launchGallery(Uri.fromFile(result));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.execute(this);
    }

    public void launch(View view) {
        share();

    }

    public void inflate(View view) {
        inflate(new int[] { R.layout.seekbar });
    }

    private void launchGallery(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/png");
        startActivity(intent);
    }

    public void inflate(int[] ids) {
        if (intro.getVisibility() == View.VISIBLE) {
            intro.setVisibility(View.GONE);
        }
        if (ids != null) {
            for (int id : ids) {
                View item = inflater.inflate(id, null);
                item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                container.addView(item);
                item.startAnimation(getViewAddedAnimation());

                // Create a generic swipe-to-dismiss touch listener.
                item.setOnTouchListener(new SwipeDismissTouchListener(item, null,
                        new SwipeDismissTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(View view, Object token) {
                                removeItem(view);
                            }
                        }));

            }
            scrollView.post(new Runnable() {

                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    private Animation getViewAddedAnimation() {
        Animation a = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        return a;
    }

    class ScreenshotTask extends AsyncTask<Activity, Void, File> {
        private static final String SAVE_FOLDER = "/DCIM/gridrop";
        private static final String PNG_SUFFIX = ".png";

        @Override
        protected File doInBackground(Activity... params) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + SAVE_FOLDER);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir.getAbsolutePath(), System.currentTimeMillis() + PNG_SUFFIX);
                FileOutputStream out = new FileOutputStream(file);

                Screenshot s = new Screenshot(params[0]);
                Bitmap b = s.snap();
                b.compress(CompressFormat.PNG, 100, out);

                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}