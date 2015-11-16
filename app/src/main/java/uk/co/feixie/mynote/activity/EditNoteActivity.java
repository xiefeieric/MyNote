package uk.co.feixie.mynote.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.lidroid.xutils.BitmapUtils;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.BitmapUtil;
import uk.co.feixie.mynote.utils.UIUtils;

public class EditNoteActivity extends AppCompatActivity {

    private Note mNote;
    private EditText etEditTitle,etEditContent;
    private ImageView ivEditPhoto;
    private VideoView vvEditVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        mNote = (Note) getIntent().getSerializableExtra("note");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_black_24dp);
        supportActionBar.setTitle("");

        initViews();
    }

    private void initViews() {
        etEditTitle = (EditText) findViewById(R.id.etEditTitle);
        etEditTitle.setText(mNote.getTitle());
        etEditContent = (EditText) findViewById(R.id.etEditContent);
        etEditContent.setText(mNote.getContent());

        ivEditPhoto = (ImageView) findViewById(R.id.ivEditPhoto);
        String imagePath = mNote.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
//            Bitmap bitmap = BitmapUtil.getBitmapLocal(EditNoteActivity.this, Uri.parse(imagePath));
//            ivEditPhoto.setImageBitmap(bitmap);
            BitmapUtils bitmapUtils = new BitmapUtils(this);
            bitmapUtils.display(ivEditPhoto,imagePath);
            ivEditPhoto.setVisibility(View.VISIBLE);
        }

        vvEditVideo = (VideoView) findViewById(R.id.vvEditVideo);
        String videoPath = mNote.getVideoPath();
        if (!TextUtils.isEmpty(videoPath)){
            vvEditVideo.setVideoURI(Uri.parse(videoPath));
            vvEditVideo.setVisibility(View.VISIBLE);
            vvEditVideo.start();
        }
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

                if (!(newTitle.equals(mNote.getTitle()) && newContent.equals(mNote.getContent()))) {
                    mNote.setTitle(newTitle);
                    mNote.setContent(newContent);
                    System.out.println(mNote.getId());
                    boolean isUpdate = saveEdit(mNote);
                    Intent intent = new Intent();
                    intent.putExtra("request_note", mNote);
                    setResult(RESULT_OK, intent);
//                    UIUtils.showToast(this, "status: "+isUpdate);
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        newTitle = etEditTitle.getText().toString();
        newContent = etEditContent.getText().toString();
        if (!(TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newContent))) {
            if (!(newTitle.equals(mNote.getTitle()) && newContent.equals(mNote.getContent()))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Discard?");
                builder.setMessage("Your changes are not saved. Are you sure you want to discard?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtils.showToast(EditNoteActivity.this,"Changes discarded");
                        finish();
                    }
                });
                builder.setNegativeButton("CANCEL",null);
                builder.show();

            } else {
                finish();
            }
        }



    }

    private boolean saveEdit(Note note) {

        DbHelper dbHelper = new DbHelper(this);
        boolean isUpdate = dbHelper.update(note);
        return isUpdate;
    }
}
