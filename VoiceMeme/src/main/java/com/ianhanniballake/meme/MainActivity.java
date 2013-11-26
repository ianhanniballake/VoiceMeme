package com.ianhanniballake.meme;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final int GET_PICTURE = 40;
    private MemeAdapter mAdapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_PICTURE && resultCode == RESULT_OK)
        {
            Intent intent = new Intent(this, ShareActivity.class);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/jpg");
            intent.putExtra(Intent.EXTRA_STREAM, data.getData());
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView memeGrid = (GridView) findViewById(R.id.meme_grid);
        mAdapter = new MemeAdapter();
        memeGrid.setAdapter(mAdapter);
        memeGrid.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_start_voice_input:
                startActivity(new Intent(this, RecognizerActivity.class));
                return true;
            case R.id.action_import_picture:
                Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
                getContent.addCategory(Intent.CATEGORY_OPENABLE);
                getContent.setType("image/");
                startActivityForResult(Intent.createChooser(getContent,
                        "Select Picture"), GET_PICTURE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + mAdapter.getItem(position));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(intent);
    }

    private class MemeAdapter extends BaseAdapter {
        private final LayoutInflater mLayoutInflater;

        MemeAdapter()
        {
            mLayoutInflater = getLayoutInflater();
        }
        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Object getItem(int position) {
            switch (position)
            {
                case 0:
                    return R.drawable.brace_yourselves;
                case 1:
                    return R.drawable.i_dont_always;
                case 2:
                    return R.drawable.one_does_not_simply;
                case 3:
                    return R.drawable.too_damn_high;
                case 4:
                    return R.drawable.y_u_no;
                case 5:
                    return R.drawable.bad_luck_brian;
                default:
                    return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
            {
                view = mLayoutInflater.inflate(R.layout.item_meme, parent, false);
            }
            ((ImageView)view).setImageResource((Integer)getItem(position));
            return view;
        }
    }
}
