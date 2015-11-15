package uk.co.feixie.mynote.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.UIUtils;

public class AddNoteActivity extends AppCompatActivity {

    private Toolbar mToolbarAddNote;
    private EditText etTitle, etContent;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private ImageView ivAddPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initViews();
        initListeners();
    }


    private void initViews() {
        mToolbarAddNote = (Toolbar) findViewById(R.id.toolbarAddNote);
        setSupportActionBar(mToolbarAddNote);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_black_24dp);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle("");

        etTitle = (EditText) findViewById(R.id.etTitle);
        ivAddPhoto = (ImageView) findViewById(R.id.ivAddPhoto);

        etContent = (EditText) findViewById(R.id.etContent);
        etContent.requestFocus();
        etContent.requestFocusFromTouch();
    }

    private void initListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                UIUtils.showToast(this, "save");
                if (TextUtils.isEmpty(etTitle.getText().toString()) && TextUtils.isEmpty(etContent.getText().toString())) {
                    UIUtils.showToast(this, "Cannot save an empty note");
                } else {
                    saveNote();
                }
                finish();
                break;
            case R.id.action_bar_voice:
                UIUtils.showToast(this, "voice");
                try {
                    displaySpeechRecognizer();
                } catch (ActivityNotFoundException e) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.google.android.voicesearch"));
                    startActivity(browserIntent);
                }
                break;
            case R.id.action_bar_photo:
                UIUtils.showToast(this, "photo");
                dispatchTakePictureIntent();
                break;
            case R.id.action_bar_video:
                UIUtils.showToast(this, "video");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //save note to database
    private void saveNote() {
        Note note = new Note();
        DbHelper dbHelper = new DbHelper(this);
//        List<Note> noteList = dbHelper.queryAll();
//        note.setId(noteList.size()+1);
        String title = etTitle.getText().toString();
        String capital = title;
        if (!TextUtils.isEmpty(title)){
            capital = title.subSequence(0,1).toString().toUpperCase()+title.substring(1);
        }
        note.setTitle(capital);
        String content = etContent.getText().toString();
        note.setContent(content);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String time = formatter.format(new Date());
        note.setTime(time);

        if (!TextUtils.isEmpty(mCurrentPhotoPath)) {
//            System.out.println(mCurrentPhotoPath);
            note.setImagePath(mCurrentPhotoPath);
        }

        dbHelper.add(note);
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                UIUtils.showToast(this,"Photo save error!");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    /**
     * 通过路径获取系统图片
     * @param uri
     * @return
     */
    private Bitmap getBitmap(Uri uri) {
        Bitmap pic = null;
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        Display display = getWindowManager().getDefaultDisplay();
        int dw = display.getWidth();
        int dh = display.getHeight();
        try {
            pic = BitmapFactory.decodeStream(getContentResolver()
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
            pic = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, op);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return pic;
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            etContent.setText(spokenText);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Uri uri = Uri.parse(mCurrentPhotoPath);
            Bitmap imageBitmap = getBitmap(uri);
//            mImageView.setImageBitmap(imageBitmap);
            //insertIntoEditText(getBitmapMime(imageBitmap,uri));
            ivAddPhoto.setImageBitmap(imageBitmap);
            ivAddPhoto.setVisibility(View.VISIBLE);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //add image into edittext
    private SpannableString getBitmapMime(Bitmap pic, Uri uri) {
//        int imgWidth = pic.getWidth();
//        int imgHeight = pic.getHeight();
//        float scalew = (float) 40 / imgWidth;
//        float scaleh = (float) 40 / imgHeight;
//        Matrix mx = new Matrix();
//        mx.setScale(scalew, scaleh);
//        pic = Bitmap.createBitmap(pic, 0, 0, imgWidth, imgHeight, mx, true);
//        String smile = uri.getPath();
        String smile = "-";
        SpannableString ss = new SpannableString(smile+"\n");
        ImageSpan span = new ImageSpan(this, pic);
        ss.setSpan(span, 0, smile.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        ss.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 这里是重点
     */
    private void insertIntoEditText(SpannableString ss) {
        Editable et = etContent.getText();// 先获取Edittext中的内容
        int start = etContent.getSelectionStart();
        et.insert(start, ss);// 设置ss要添加的位置
        etContent.setText(et);// 把et添加到Edittext中
        etContent.setSelection(start + ss.length());// 设置Edittext中光标在最后面显示
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        UIUtils.showToast(this,"back pressed");
        if (TextUtils.isEmpty(etTitle.getText().toString()) && TextUtils.isEmpty(etContent.getText().toString())) {
            UIUtils.showToast(this, "Cannot save an empty note");
        } else {
            saveNote();
        }
    }
}
