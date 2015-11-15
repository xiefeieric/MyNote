package uk.co.feixie.mynote.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Fei on 11/11/2015.
 */
public class UIUtils {

    public static Toast mToast;

    public static void showToast(Context context, String msg) {

        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }
}
