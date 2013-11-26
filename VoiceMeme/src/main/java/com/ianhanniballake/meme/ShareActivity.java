package com.ianhanniballake.meme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShareActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String ACTION_TOP_TEXT = "ACTION_TOP_TEXT";
    private static final String ACTION_BOTTOM_TEXT = "ACTION_BOTTOM_TEXT";
    public static final String AUTHORITY = "com.ianhanniballake.meme.fileprovider";
    public static final int DEFAULT_TEXT_SIZE = 70;
    private ImageView mImageView;
    private IntentChooserAdapter mAdapter;
    private String mTopText = "";
    private String mBottomText = "";
    private Bitmap mSourceImage;
    private Bitmap mCurrentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        mImageView = (ImageView) findViewById(R.id.image);
        Button topText = (Button) findViewById(R.id.top_text);
        topText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTopTextDialog();
            }
        });
        Button bottomText = (Button) findViewById(R.id.bottom_text);
        bottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomTextDialog();
            }
        });
        GridView mGridView = (GridView) findViewById(R.id.share_intent_grid);
        mAdapter = new IntentChooserAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (TextUtils.equals(action, Intent.ACTION_SEND) && type != null && type.startsWith("image/"))
            handleSendImage(intent); // Handle single image being sent

    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            InputStream input = null;
            try {
                input = getContentResolver().openInputStream(imageUri);
                mSourceImage = BitmapFactory.decodeStream(input);
                mImageView.setImageBitmap(mSourceImage);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType(intent.getType());
                mAdapter.setIntent(shareIntent);
                if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String[] splitText = text.split("\n", 2);
                    if (splitText.length == 2) {
                        mTopText = splitText[0];
                        mBottomText = splitText[1];
                    } else {
                        int middle = text.length() / 2;
                        int spaceBefore = text.substring(0, middle).lastIndexOf(' ');
                        int spaceAfter = text.indexOf(' ', middle);
                        int diffBefore = middle - spaceBefore;
                        int diffAfter = spaceAfter - middle;
                        int splitPoint = spaceBefore;
                        if (diffAfter < diffBefore)
                            splitPoint = spaceAfter;
                        mTopText = text.substring(0, splitPoint);
                        mBottomText = text.substring(splitPoint + 1, text.length());
                    }
                    Log.d(ShareActivity.class.getSimpleName(), "Top: \'" + mTopText + "\', Bottom: \'" + mBottomText + "\'");
                    redrawImage();
                }
            } catch (FileNotFoundException e) {
                Log.e(ShareActivity.class.getSimpleName(), "Error reading image " + imageUri, e);
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e(ShareActivity.class.getSimpleName(), "Error closing image " + imageUri, e);
                    }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File directory = new File(getFilesDir(), "images/");
        directory.mkdir();
        try {
            File file = File.createTempFile("image", ".png", directory);
            FileOutputStream out = new FileOutputStream(file);
            mCurrentImage.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, AUTHORITY, file));
            shareIntent.setType("image/png");
            mAdapter.setIntent(shareIntent);
            Intent launchIntent = (Intent) mAdapter.getItem(position);
            startActivity(launchIntent);
        } catch (IOException e) {
            Log.e(ShareActivity.class.getSimpleName(), "Error writing file", e);
        }
    }

    BroadcastReceiver mTopTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mTopText = intent.getStringExtra(Intent.EXTRA_TEXT);
            redrawImage();
        }
    };

    BroadcastReceiver mBottomTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBottomText = intent.getStringExtra(Intent.EXTRA_TEXT);
            redrawImage();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mTopTextReceiver,
                IntentFilter.create(ACTION_TOP_TEXT, "text/plain"));
        localBroadcastManager.registerReceiver(mBottomTextReceiver,
                IntentFilter.create(ACTION_BOTTOM_TEXT, "text/plain"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(mTopTextReceiver);
        localBroadcastManager.unregisterReceiver(mBottomTextReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_top_text:
                showTopTextDialog();
                return true;
            case R.id.action_bottom_text:
                showBottomTextDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showBottomTextDialog() {
        TextDialog bottomDialog = TextDialog.createInstance(ACTION_BOTTOM_TEXT, "Bottom Text", mBottomText);
        bottomDialog.show(getFragmentManager(), "dialog");
    }

    private void showTopTextDialog() {
        TextDialog topDialog = TextDialog.createInstance(ACTION_TOP_TEXT, "Top Text", mTopText);
        topDialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem topText = menu.findItem(R.id.action_top_text);
        if (TextUtils.isEmpty(mTopText))
            topText.setTitle(R.string.action_add_top_text);
        else
            topText.setTitle(R.string.action_edit_top_text);
        MenuItem bottomText = menu.findItem(R.id.action_bottom_text);
        if (TextUtils.isEmpty(mBottomText))
            bottomText.setTitle(R.string.action_add_bottom_text);
        else
            bottomText.setTitle(R.string.action_edit_bottom_text);
        return true;
    }

    private void redrawImage() {
        invalidateOptionsMenu();
        int w = mSourceImage.getWidth();
        int h = mSourceImage.getHeight();
        mCurrentImage = Bitmap.createBitmap(w, h, mSourceImage.getConfig());

        Canvas canvas = new Canvas(mCurrentImage);
        canvas.drawBitmap(mSourceImage, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(3f, 0f, 3f, Color.DKGRAY);
        paint.setAntiAlias(true);
        paint.setTextSize(DEFAULT_TEXT_SIZE);

        float topSize = paint.measureText(mTopText);
        float bottomSize = paint.measureText(mBottomText);

        int idealSize;
        if (topSize > bottomSize)
            idealSize = findIdealSize(paint, w, mTopText);
        else
            idealSize = findIdealSize(paint, w, mBottomText);
        paint.setTextSize(idealSize);
        Rect rect = new Rect();
        paint.getTextBounds(mTopText, 0, mTopText.length(), rect);
        canvas.drawText(mTopText, (w - rect.width()) / 2, rect.height() + 16, paint);

        paint.getTextBounds(mBottomText, 0, mBottomText.length(), rect);
        canvas.drawText(mBottomText, (w - rect.width()) / 2, h - 16, paint);

        mImageView.setImageBitmap(mCurrentImage);
    }

    private int findIdealSize(Paint paint, int targetWidth, String text) {
        int currentSize = DEFAULT_TEXT_SIZE;
        boolean done = false;
        while (!done) {
            paint.setTextSize(currentSize);
            float width = paint.measureText(text);
            if (width * 2 < targetWidth || currentSize > 100) {
                Log.v(ShareActivity.class.getSimpleName(), currentSize + " Too small: " + width + " vs " + targetWidth);
                currentSize += 10;
            } else if (width < targetWidth) {
                Log.v(ShareActivity.class.getSimpleName(), currentSize + " Just right: " + width + " vs " + targetWidth);
                done = true;
            } else if (width * 2 < targetWidth * 3) {
                Log.v(ShareActivity.class.getSimpleName(), currentSize + " Too big: " + width + " vs " + targetWidth);
                currentSize -= 10;
            } else if (width < targetWidth * 2) {
                Log.v(ShareActivity.class.getSimpleName(), currentSize + " Just right (two lines): " + width + " vs " + targetWidth);
                currentSize -= 10;
            } else {
                Log.v(ShareActivity.class.getSimpleName(), currentSize + " Way too big: " + width + " vs " + targetWidth);
                currentSize -= 10;
            }
        }
        return currentSize;
    }

    private class IntentChooserAdapter extends BaseAdapter {
        private final ActivityChooserModel mDataModel;
        private final PackageManager mPackageManager;
        private final LayoutInflater mLayoutInflater;

        private IntentChooserAdapter() {
            mPackageManager = getPackageManager();
            mLayoutInflater = getLayoutInflater();
            mDataModel = ActivityChooserModel.get(ShareActivity.this, "meme_share.xml");
        }

        @Override
        public int getCount() {
            Log.v(ShareActivity.class.getSimpleName(), "Found " + mDataModel.getActivityCount() + " activities");
            return mDataModel.getActivityCount();
        }

        public void setIntent(Intent intent) {
            mDataModel.setIntent(intent);
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return mDataModel.chooseActivity(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    mLayoutInflater.inflate(R.layout.item_intent, parent, false);
            ResolveInfo activity = mDataModel.getActivity(position);
            ImageView icon = (ImageView) view.findViewById(R.id.share_icon);
            icon.setImageDrawable(activity.loadIcon(mPackageManager));
            TextView label = (TextView) view.findViewById(R.id.share_label);
            label.setText(activity.loadLabel(mPackageManager));
            return view;
        }
    }
}
