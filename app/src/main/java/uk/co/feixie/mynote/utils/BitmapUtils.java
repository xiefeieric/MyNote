package uk.co.feixie.mynote.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.Display;

import java.io.FileNotFoundException;

/**
 * Created by Fei on 15/11/2015.
 */
public class BitmapUtils {

    public static Bitmap getBitmapLocal(Activity activity,Uri uri) {
        Bitmap pic = null;
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        Display display = activity.getWindowManager().getDefaultDisplay();
        int dw = display.getWidth();
        int dh = display.getHeight();
        try {
            pic = BitmapFactory.decodeStream(activity.getContentResolver()
                    .openInputStream(uri), null, op);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int wRatio = (int) Math.ceil(op.outWidth / (float) dw);
        int hRatio = (int) Math.ceil(op.outHeight / (float) dh);
        if (wRatio > 1 && hRatio > 1) {
            op.inSampleSize = wRatio + hRatio;
        }
        op.inJustDecodeBounds = false;
        try {
            pic = BitmapFactory.decodeStream(activity.getContentResolver()
                    .openInputStream(uri), null, op);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return pic;
    }

}
