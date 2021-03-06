package uk.co.feixie.mynote.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.os.ResultReceiver;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import uk.co.feixie.mynote.service.FetchAddressIntentService;
import uk.co.feixie.mynote.utils.Constants;
import uk.co.feixie.mynote.utils.ImageFilePath;
import uk.co.feixie.mynote.utils.UIUtils;

import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class AddNoteActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText etTitle, etContent;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private String mCurrentPhotoPath;
    private String mCurrentVideoPath;
    private ImageView ivAddPhoto;
    private VideoView vvAddVideo;
    private AutoCompleteTextView acCategory;

    //Google address
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private boolean mAddressRequested;
    private String mAddressOutput;
    private int mAvailable;
    private DbHelper mDbHelper;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        mDbHelper = new DbHelper(this);
        initViews();
        buildGoogleApiClient();
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        AddressResultReceiver resultReceiver = new AddressResultReceiver(null);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAvailable == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();

            // Only start the service to fetch the address if GoogleApiClient is
            // connected.
            if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                startIntentService();
            }
            mAddressRequested = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAvailable == ConnectionResult.SUCCESS) {
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mAvailable = isGooglePlayServicesAvailable(this);
//        System.out.println(mAvailable);
        if (mAvailable == ConnectionResult.SUCCESS) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
                    .addApiIfAvailable(LocationServices.API)
                    .build();

        } else {
            getErrorDialog(mAvailable, this, 123).show();
        }
    }

    private void initViews() {

        x.Ext.init(getApplication());
        x.Ext.setDebug(true);

        Toolbar toolbarAddNote = (Toolbar) findViewById(R.id.toolbarAddNote);
        setSupportActionBar(toolbarAddNote);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_black_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("");
        }


        etTitle = (EditText) findViewById(R.id.etTitle);
        ivAddPhoto = (ImageView) findViewById(R.id.ivAddPhoto);
        vvAddVideo = (VideoView) findViewById(R.id.vvAddVideo);

        etContent = (EditText) findViewById(R.id.etContent);
        etContent.requestFocus();
        etContent.requestFocusFromTouch();

        acCategory = (AutoCompleteTextView) findViewById(R.id.acCategory);
        new Thread() {
            @Override
            public void run() {
                final List<String> listCategory = mDbHelper.queryAllCategory();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new ArrayAdapter<>(AddNoteActivity.this, android.R.layout.simple_dropdown_item_1line, listCategory);
                        acCategory.setAdapter(mAdapter);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                UIUtils.showToast(this, "save");
                if (TextUtils.isEmpty(etTitle.getText().toString()) && TextUtils.isEmpty(etContent.getText().toString())
                        && TextUtils.isEmpty(mCurrentPhotoPath) && TextUtils.isEmpty(mCurrentVideoPath)) {
                    UIUtils.showToast(this, "Cannot save an empty note");
                } else {
                    saveNote();
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

    //save note to database
    private void saveNote() {
        final Note note = new Note();
        String title = etTitle.getText().toString();
        String capital;
        if (!TextUtils.isEmpty(title)) {
            capital = title.subSequence(0, 1).toString().toUpperCase() + title.substring(1);
            note.setTitle(capital);
        } else if (!TextUtils.isEmpty(mAddressOutput)) {
            if (!TextUtils.isEmpty(etContent.getText().toString())) {
                note.setTitle("Note@" + mAddressOutput);
            } else if (!TextUtils.isEmpty(mCurrentPhotoPath) || !TextUtils.isEmpty(mCurrentVideoPath)) {
                note.setTitle("Snapshot@" + mAddressOutput);
            }

        } else {
            note.setTitle(title);
        }


        String category = acCategory.getText().toString();
        note.setCategory(category);


        String content = etContent.getText().toString();
        note.setContent(content);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String time = formatter.format(new Date());
        note.setTime(time);

        if (!TextUtils.isEmpty(mCurrentPhotoPath)) {
//            System.out.println(mCurrentPhotoPath);
            note.setImagePath(mCurrentPhotoPath);
        }

        if (!TextUtils.isEmpty(mCurrentVideoPath)) {
//            System.out.println(mCurrentPhotoPath);
            note.setVideoPath(mCurrentVideoPath);
        }

        if (mLastLocation != null) {
            note.setLatitude(String.valueOf(mLastLocation.getLatitude()));
            note.setLongitude(String.valueOf(mLastLocation.getLongitude()));
        }

        new Thread() {
            @Override
            public void run() {
                mDbHelper.add(note);

                String noteCategory = note.getCategory();
                boolean isInCategory = checkNameInCategory(noteCategory);
                if (!isInCategory && !TextUtils.isEmpty(noteCategory)) {
                    mDbHelper.addCategory(note.getCategory());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }.start();
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


    /**
     * 通过路径获取系统图片
     * <p/>
     * uri
     */
//    private Bitmap getBitmap(Uri uri) {
//        Bitmap pic = null;
//        BitmapFactory.Options op = new BitmapFactory.Options();
//        op.inJustDecodeBounds = true;
//        Display display = getWindowManager().getDefaultDisplay();
//        int dw = display.getWidth();
//        int dh = display.getHeight();
//        try {
//            pic = BitmapFactory.decodeStream(getContentResolver()
//                    .openInputStream(uri), null, op);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        int wRatio = (int) Math.ceil(op.outWidth / (float) dw);
//        int hRatio = (int) Math.ceil(op.outHeight / (float) dh);
//        if (wRatio > 1 && hRatio > 1) {
//            op.inSampleSize = wRatio + hRatio;
//        }
//        op.inJustDecodeBounds = false;
//        try {
//            pic = BitmapFactory.decodeStream(getContentResolver()
//                    .openInputStream(uri), null, op);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return pic;
//    }

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
//            Uri uri = Uri.parse(mCurrentPhotoPath);
//            Bitmap imageBitmap = getBitmap(uri);
//            mImageView.setImageBitmap(imageBitmap);
            //insertIntoEditText(getBitmapMime(imageBitmap,uri));
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
                x.image().bind(ivAddPhoto, selectedImageUri.getPath());
            } else {
                selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String path = ImageFilePath.getPath(this, selectedImageUri);
                    mCurrentPhotoPath = "file:"+path;
//                    System.out.println("path: "+path);
                    x.image().bind(ivAddPhoto, path);
                } else {
                    String path = getPath(selectedImageUri);
                    mCurrentPhotoPath = "file:"+path;
                    x.image().bind(ivAddPhoto, path);
                }
            }
//            BitmapUtils bitmapUtils = new BitmapUtils(this);
//            bitmapUtils.display(ivAddPhoto, URLDecoder.decode(selectedImageUri.toString()));
//            ivAddPhoto.setImageBitmap(imageBitmap);
            ivAddPhoto.setVisibility(View.VISIBLE);

        } else {
            mCurrentPhotoPath = null;
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            mCurrentVideoPath = videoUri.toString();
            vvAddVideo.setVisibility(View.VISIBLE);
            vvAddVideo.setVideoURI(videoUri);
            vvAddVideo.start();

        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            // handle scan result
            String contents = scanResult.getContents();
            etContent.setText(contents);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //add image into edittext
//    private SpannableString getBitmapMime(Bitmap pic, Uri uri) {
////        int imgWidth = pic.getWidth();
////        int imgHeight = pic.getHeight();
////        float scalew = (float) 40 / imgWidth;
////        float scaleh = (float) 40 / imgHeight;
////        Matrix mx = new Matrix();
////        mx.setScale(scalew, scaleh);
////        pic = Bitmap.createBitmap(pic, 0, 0, imgWidth, imgHeight, mx, true);
////        String smile = uri.getPath();
//        String smile = "-";
//        SpannableString ss = new SpannableString(smile + "\n");
//        ImageSpan span = new ImageSpan(this, pic);
//        ss.setSpan(span, 0, smile.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
////        ss.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return ss;
//    }

    /**
     * 这里是重点
     */
//    private void insertIntoEditText(SpannableString ss) {
//        Editable et = etContent.getText();// 先获取Edittext中的内容
//        int start = etContent.getSelectionStart();
//        et.insert(start, ss);// 设置ss要添加的位置
//        etContent.setText(et);// 把et添加到Edittext中
//        etContent.setSelection(start + ss.length());// 设置Edittext中光标在最后面显示
//    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        UIUtils.showToast(this,"back pressed");
        if (TextUtils.isEmpty(etTitle.getText().toString()) && TextUtils.isEmpty(etContent.getText().toString())
                && TextUtils.isEmpty(mCurrentPhotoPath) && TextUtils.isEmpty(mCurrentVideoPath)) {
            UIUtils.showToast(this, "Cannot save an empty note");
        } else {
            saveNote();
        }
    }

    //google service callbacks
    @Override
    public void onConnected(Bundle bundle) {

        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                UIUtils.showToast(this, "No Geocode available");
                return;
            }

            if (mAddressRequested) {
                startIntentService();
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @SuppressLint("ParcelCreator")
    public class AddressResultReceiver extends ResultReceiver {


        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
//            displayAddressOutput();
//            System.out.println("address: "+mAddressOutput);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etTitle.setHint("Note@" + mAddressOutput);
                }
            });


            // Show a toast message if an address was found.
//            if (resultCode == Constants.SUCCESS_RESULT) {
////                UIUtils.showToast(AddNoteActivity.this,"Address found");
//            }

        }
    }
}
