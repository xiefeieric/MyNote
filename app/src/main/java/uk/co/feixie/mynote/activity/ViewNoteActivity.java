package uk.co.feixie.mynote.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.BitmapUtils;

public class ViewNoteActivity extends AppCompatActivity {

    public static final int VIEW_REQUEST_CODE = 123;
    private TextView tvTitle,tvContent;
    private Note mNote;
    private ImageView ivShowPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        Intent intent = getIntent();
        mNote = (Note) intent.getSerializableExtra("note");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_black_24dp);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle("");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(ViewNoteActivity.this,EditNoteActivity.class);
                intent.putExtra("note",mNote);
                startActivityForResult(intent, VIEW_REQUEST_CODE);
            }
        });

        initViews();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==VIEW_REQUEST_CODE && resultCode==RESULT_OK) {
            Note note = (Note) data.getSerializableExtra("request_note");
            tvTitle.setText(note.getTitle());
            tvContent.setText(note.getContent());
        }
    }

    private void initViews() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(mNote.getTitle());
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setText(mNote.getContent());
        ivShowPhoto = (ImageView) findViewById(R.id.ivShowPhoto);

        String imagePath = mNote.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bitmap = BitmapUtils.getBitmapLocal(ViewNoteActivity.this, Uri.parse(imagePath));
            ivShowPhoto.setImageBitmap(bitmap);
            ivShowPhoto.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_note,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
