package com.ianhanniballake.meme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class RecognizerActivity extends Activity {

    private static final int RECOGNIZE_REQUEST_CODE = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your meme");
        startActivityForResult(intent, RECOGNIZE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(RecognizerActivity.class.getSimpleName(), "Result code: " + resultCode);
        if (requestCode == RECOGNIZE_REQUEST_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d(RecognizerActivity.class.getSimpleName(), "Results: " + results);
            if (results != null && results.size() > 0)
                recognize(results.get(0));
            else
                Toast.makeText(this, "Error recognizing your meme", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognize(String text) {
        String lowercaseText = text.toLowerCase();
        Intent intent = new Intent(this, ShareActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        int resId = 0;
        if (lowercaseText.contains(" simply ")){
            resId = R.drawable.one_does_not_simply;
            text = text.replaceFirst(" simply ", " simply\n");
        }
        else if (lowercaseText.startsWith("why ")){
            resId = R.drawable.y_u_no;
            text = text.replaceFirst("why ", "Y ");
            text = text.replaceFirst("Y you ", "Y u ");
            text = text.replaceFirst("Y u know ", "Y u no ");
        }
        else if (lowercaseText.startsWith("brace yourself")){
            resId = R.drawable.brace_yourselves;
            text = text.replaceFirst("brace yourself ", "Brace yourselves\n");
        }
        else if (lowercaseText.startsWith("brace yourselves")){
            resId = R.drawable.brace_yourselves;
            text = text.replaceFirst("brace yourselves ", "Brace yourselves\n");
        }
        else if (lowercaseText.endsWith("high")){
            resId = R.drawable.too_damn_high;
        }
        else if (lowercaseText.contains(" don't always ")){
            resId = R.drawable.i_dont_always;
            if (lowercaseText.contains(" but "))
                text.replaceFirst(" but ", "\nbut ");
        }
        else{
            resId = R.drawable.bad_luck_brian;
        }
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
        finish();
    }
}
