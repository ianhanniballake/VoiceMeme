package com.ianhanniballake.meme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ianlake on 11/16/13.
 */
public class TextDialog extends DialogFragment {
    private static final String TITLE = "TITLE_ARG";
    private static final String EXISTING_TEXT = "EXISTING_TEXT_ARG";
    private static final String ACTION = "ACTION_ARG";

    public static TextDialog createInstance(String action, String title, String existingText)
    {
        TextDialog dialog = new TextDialog();
        Bundle args = new Bundle();
        args.putString(ACTION, action);
        args.putString(TITLE, title);
        args.putString(EXISTING_TEXT, existingText);
        dialog.setArguments(args);
        return dialog;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_text, null);
        final TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString(TITLE));
        final EditText input = (EditText) view.findViewById(R.id.dialog_input);
        input.setText(getArguments().getString(EXISTING_TEXT));
        builder.setView(view);
        builder.setPositiveButton(R.string.input_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
                Intent intent = new Intent(getArguments().getString(ACTION));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, input.getText().toString());
                localBroadcastManager.sendBroadcast(intent);
            }
        });
        builder.setNegativeButton(R.string.input_cancel, null);
        return builder.create();
    }
}
