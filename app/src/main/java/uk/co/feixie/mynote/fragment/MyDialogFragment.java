package uk.co.feixie.mynote.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import uk.co.feixie.mynote.activity.EditNoteActivity;
import uk.co.feixie.mynote.activity.MainActivity;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.UIUtils;

/**
 * Created by Fei on 16/11/2015.
 */
public class MyDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new CharSequence[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                UIUtils.showToast(getActivity(), "clicked: " + which);
                MainActivity activity = (MainActivity) getActivity();
                Note note = activity.getClickedNote();
                if (which == 0) {
                    Intent intent = new Intent(getActivity(), EditNoteActivity.class);
                    intent.putExtra("note", note);
                    startActivity(intent);
                }

                if (which == 1) {
                    DbHelper dbHelper = new DbHelper(getActivity());
                    boolean delete = dbHelper.delete(note);
                    if (delete) {
                        UIUtils.showToast(getActivity(), "Delete Success.");
                    } else {
                        UIUtils.showToast(getActivity(), "Delete Fail.");
                    }

                }
            }
        });
        return builder.create();
    }
}
