package uk.co.feixie.mynote.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lidroid.xutils.BitmapUtils;

import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.ImageFilePath;
import uk.co.feixie.mynote.utils.UIUtils;

public class EditNoteActivity extends AppCompatActivity {

    private Note mNote;
    private EditText etEditTitle, etEditContent;
    private ImageView ivEditPhoto;
    private VideoView vvEditVideo;
    private AutoCompleteTextView acEditCategory;

    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;

    private String mCurrentPhotoPath;
    private String mCurrentVideoPath;
    private DbHelper mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        mNote = (Note) getIntent().getSerializableExtra("note");
        mDbHelper = new DbHelper(this);

        x.Ext.init(getApplication());
        x.Ext.setDebug(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_black_24dp);
            supportActionBar.setTitle("");
        }
        initViews();
    }

    private void initViews() {
        etEditTitle = (EditText) findViewById(R.id.etEditTitle);
        etEditTitle.setText(mNote.getTitle());
        etEditContent = (EditText) findViewById(R.id.etEditContent);
        etEditContent.setFocusable(true);
        etEditContent.setFocusableInTouchMode(true);
        String content = mNote.getContent();
        if (TextUtils.isEmpty(content)) {
            etEditContent.setHint("Compose your note");
        } else {
            etEditContent.setText(content);
        }


        ivEditPhoto = (ImageView) findViewById(R.id.ivEditPhoto);
        String imagePath = mNote.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
            BitmapUtils bitmapUtils = new BitmapUtils(this);
            bitmapUtils.display(ivEditPhoto, imagePath);
            ivEditPhoto.setVisibility(View.VISIBLE);
        }

        vvEditVideo = (VideoView) findViewById(R.id.vvEditVideo);
        String videoPath = mNote.getVideoPath();
        if (!TextUtils.isEmpty(videoPath)) {
            vvEditVideo.setVideoURI(Uri.parse(videoPath));
            vvEditVideo.setVisibility(View.VISIBLE);
            vvEditVideo.start();
        }

        acEditCategory = (AutoCompleteTextView) findViewById(R.id.acEditCategory);
        acEditCategory.setText(mNote.getCategory());
        new Thread(){
            @Override
            public void run() {
                final List<String> listCategory = mDbHelper.queryAllCategory();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditNoteActivity.this, android.R.layout.simple_dropdown_item_1line,listCategory);
                        acEditCategory.setAdapter(adapter);
                    }
                });
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String newTitle;
    private String newContent;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                newTitle = etEditTitle.getText().toString();
                newContent = etEditContent.getText().toString();
                final String newCategory = acEditCategory.getText().toString();

                if (!TextUtils.equals(newTitle, mNote.getTitle()) || !TextUtils.equals(newContent, mNote.getContent())
                        || !TextUtils.isEmpty(mCurrentPhotoPath) || !TextUtils.isEmpty(mCurrentVideoPath) || !TextUtils.equals(newCategory, mNote.getCategory())) {

                    mNote.setTitle(newTitle);
                    mNote.setContent(newContent);
                    mNote.setCategory(newCategory);

                    new Thread(){
                        @Override
                        public void run() {
                            boolean inCategory = checkNameInCategory(newCategory);
                            if (!inCategory && !TextUtils.isEmpty(newCategory)) {
                                mDbHelper.addCategory(newCategory);
                            }
                        }
                    }.start();

                    if (!TextUtils.isEmpty(mCurrentPhotoPath) && !TextUtils.equals(mCurrentPhotoPath, mNote.getImagePath())) {
                        mNote.setImagePath(mCurrentPhotoPath);
                    }

                    if (!TextUtils.isEmpty(mCurrentVideoPath) && !TextUtils.equals(mCurrentVideoPath, mNote.getVideoPath())) {
                        mNote.setVideoPath(mCurrentVideoPath);
                    }


                    boolean isUpdate = saveEdit(mNote);
                    if (isUpdate) {

                        Intent intent = new Intent();
                        intent.putExtra("request_note", mNote);
                        setResult(RESULT_OK, intent);
                        UIUtils.showToast(EditNoteActivity.this, "Change saved");

                    } else {

                        UIUtils.showToast(EditNoteActivity.this, "Save Failed!");

                    }

                }
                finish();
                break;

            case R.id.action_bar_voice:
//                UIUtils.showToast(this, "voice");
                try {
                    displaySpeechRecognizer();
                } catch (ActivityNotFoundException e) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.google.android.googlequicksearchbox"));
                    startActivity(browserIntent);
                }
                break;

            case R.id.action_bar_photo:
//                UIUtils.showToast(this, "photo");
//                dispatchTakePictureIntent();
                takeSelectPicture();
                break;

            case R.id.action_bar_video:
//                UIUtils.showToast(this, "video");
                dispatchTakeVideoIntent();
                break;

            case R.id.action_bar_qrcode:
                scanQR();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            etEditContent.setText(spokenText);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

//            BitmapUtils bitmapUtils = new BitmapUtils(this);
//            bitmapUtils.display(ivEditPhoto, mCurrentPhotoPath);
            final boolean isCamera;
            if (data == null || (data.getData() == null && data.getClipData() == null)) {
                isCamera = true;
            } else {
                isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
            }

//            System.out.println(isCamera);
            final Uri selectedImageUri;
            if (isCamera) {
                selectedImageUri = Uri.parse(mCurrentPhotoPath);
//                System.out.println("mCurrentPath: " + selectedImageUri.getPath());
                x.image().bind(ivEditPhoto, selectedImageUri.getPath());
            } else {
                selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String path = ImageFilePath.getPath(this, selectedImageUri);
                    mCurrentPhotoPath = path;
//                    System.out.println("path: "+path);
                    x.image().bind(ivEditPhoto, path);
                } else {
                    String path = getPath(selectedImageUri);
                    mCurrentPhotoPath = path;
                    x.image().bind(ivEditPhoto, path);
                }
            }
            ivEditPhoto.setVisibility(View.VISIBLE);
        } else {
            mCurrentPhotoPath = null;
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            mCurrentVideoPath = videoUri.toString();
            vvEditVideo.setVisibility(View.VISIBLE);
            vvEditVideo.setVideoURI(videoUri);
            vvEditVideo.start();
        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            // handle scan result
            String contents = scanResult.getContents();
            etEditContent.setText(contents);
        }

    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }

        return uri.getPath();
    }

    private void scanQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan a QRCode");
//        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onBackPressed() {

        newTitle = etEditTitle.getText().toString();
        newContent = etEditContent.getText().toString();
        String newCategory = acEditCategory.getText().toString();

        if (!(TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newContent))) {
            if (!(newTitle.equals(mNote.getTitle()) && newContent.equals(mNote.getContent()) && TextUtils.equals(newCategory,mNote.getCategory()))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Discard?");
                builder.setMessage("Your changes are not saved. Are you sure you want to discard?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtils.showToast(EditNoteActivity.this, "Changes discarded");
                        finish();
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                builder.show();

            } else {
                finish();
            }
        }
    }

    private boolean saveEdit(Note note) {

        return mDbHelper.update(note);
    }

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
                UIUtils.showToast(this, "Photo save error!");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void takeSelectPicture() {

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            UIUtils.showToast(this, "Photo save error!");
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            // Camera.
            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                cameraIntents.add(intent);
            }

            // Filesystem.
            final Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_PICK);

            // Chooser of filesystem options.
            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

            // Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

            startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
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

    private boolean checkNameInCategory(String name) {
        List<String> list = mDbHelper.queryAllCategory();
        for (String category : list) {
            if (category.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

}
