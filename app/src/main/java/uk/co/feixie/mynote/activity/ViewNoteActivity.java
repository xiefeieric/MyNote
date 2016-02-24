package uk.co.feixie.mynote.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.lidroid.xutils.BitmapUtils;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.BitmapUtil;
import uk.co.feixie.mynote.utils.UIUtils;

public class ViewNoteActivity extends AppCompatActivity {

    public static final int VIEW_REQUEST_CODE = 123;
    private TextView tvTitle, tvContent;
    private Note mNote;
    private ImageView ivShowPhoto;
    private VideoView vvViewVideo;
    private TextView tvViewCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        Intent intent = getIntent();
        mNote = (Note) intent.getSerializableExtra("note");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_black_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(ViewNoteActivity.this, EditNoteActivity.class);
                intent.putExtra("note", mNote);
                startActivityForResult(intent, VIEW_REQUEST_CODE);
            }
        });

        initViews();
        initListeners();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_REQUEST_CODE && resultCode == RESULT_OK) {
//            Note note = (Note) data.getSerializableExtra("request_note");
            mNote = (Note) data.getSerializableExtra("request_note");
            tvTitle.setText(mNote.getTitle());
            tvContent.setText(mNote.getContent());
            tvViewCategory.setText(mNote.getCategory());
            String imagePath = mNote.getImagePath();
//            System.out.println("imagePath: " + imagePath);
            if (!TextUtils.isEmpty(imagePath)) {
                BitmapUtils bitmapUtils = new BitmapUtils(this);
                bitmapUtils.display(ivShowPhoto, imagePath);
                ivShowPhoto.setVisibility(View.VISIBLE);
            }
            String videoPath = mNote.getVideoPath();
            if (!TextUtils.isEmpty(videoPath)) {
                vvViewVideo.setVideoURI(Uri.parse(videoPath));
                vvViewVideo.setVisibility(View.VISIBLE);
                if (!vvViewVideo.isPlaying()) {
                    vvViewVideo.start();
                }
            }
        }
    }

    private void initViews() {

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(mNote.getTitle());

        tvViewCategory = (TextView) findViewById(R.id.tvViewCategory);
        tvViewCategory.setText(mNote.getCategory());

        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setText(mNote.getContent());

        ivShowPhoto = (ImageView) findViewById(R.id.ivShowPhoto);
        String imagePath = mNote.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
            BitmapUtils bitmapUtils = new BitmapUtils(this);
            bitmapUtils.display(ivShowPhoto, imagePath);
            ivShowPhoto.setVisibility(View.VISIBLE);
        }

        vvViewVideo = (VideoView) findViewById(R.id.vvViewVideo);
        String videoPath = mNote.getVideoPath();
        if (!TextUtils.isEmpty(videoPath)) {
            vvViewVideo.setVideoURI(Uri.parse(videoPath));
            vvViewVideo.setVisibility(View.VISIBLE);
            if (!vvViewVideo.isPlaying()) {
                vvViewVideo.start();
            }
        }
    }

    private void initListeners() {

        vvViewVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (!vvViewVideo.isPlaying()) {
                            vvViewVideo.start();
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_bar_share:
                showShare();
                break;
            case R.id.action_bar_map:
                Intent intent = new Intent(this, MapsNoteActivity.class);
                intent.putExtra("latitude", mNote.getLatitude());
                intent.putExtra("longitude", mNote.getLongitude());
                startActivity(intent);
                break;
            case R.id.action_bar_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to delete?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DbHelper dbHelper = new DbHelper(ViewNoteActivity.this);
                        boolean isDeleted = dbHelper.delete(mNote);
                        if (isDeleted) {
                            finish();
                        } else {
                            Snackbar.make(tvContent, "Delete Failed!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题：微信、QQ（新浪微博不需要标题）
        String title = mNote.getTitle();
        oks.setTitle(title);  //最多30个字符

        // text是分享文本：所有平台都需要这个字段
        String content = mNote.getContent();
        oks.setText(content);  //最多40个字符

        // imagePath是图片的本地路径：除Linked-In以外的平台都支持此参数
        //oks.setImagePath(Environment.getExternalStorageDirectory() + "/meinv.jpg");//确保SDcard下面存在此张图片
        String imagePath = mNote.getImagePath();
        if (!TextUtils.isEmpty(imagePath)) {
            String rawImagePath = Uri.parse(imagePath).getPath();
            oks.setImagePath(rawImagePath);
        }

        //网络图片的url：所有平台
        //oks.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul

        // url：仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl("http://sharesdk.cn");   //网友点进链接后，可以看到分享的详情

        // Url：仅在QQ空间使用
//        oks.setTitleUrl("http://www.baidu.com");  //网友点进链接后，可以看到分享的详情

        // 启动分享GUI
        oks.show(this);
    }
}
