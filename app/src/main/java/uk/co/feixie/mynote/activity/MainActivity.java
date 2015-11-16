package uk.co.feixie.mynote.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.DateUtils;

public class MainActivity extends AppCompatActivity {

    private ListView lvMainContent;
    private DrawerLayout dlMenu;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Note> mNoteList;
    private MyListAdapter mAdapter;
    private DbHelper mDbHelper;
    private ImageView ivToolbar;
    private BitmapUtils mBitmapUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNoteList = new ArrayList<>();
//        createShortcut();
        initToolbar();
        initFloatingButton();
        initViews();
        initListeners();
    }

    private void initFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        ivToolbar = (ImageView) findViewById(R.id.ivToolbar);
        getCurrentMonthToShow();
    }

    private void initViews() {
        lvMainContent = (ListView) findViewById(R.id.lvMainContent);
        mDbHelper = new DbHelper(this);
        mNoteList = mDbHelper.queryAll();
        sortList(mNoteList);


        mAdapter = new MyListAdapter();
        lvMainContent.setAdapter(mAdapter);
        //lvMainContent.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc"}));

        dlMenu = (DrawerLayout) findViewById(R.id.dlMenu);
        mDrawerToggle = new ActionBarDrawerToggle(this, dlMenu, mToolbar, R.string.drawer_open, R.string.drawer_close);
        dlMenu.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void initListeners() {

        lvMainContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = mNoteList.get(position);
                note.setId(mNoteList.size() - position);
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("note", note);
                startActivity(intent);
            }
        });

    }

    private void getCurrentMonthToShow() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String date = formatter.format(new Date());
        String dates[] = date.split("/");
        int month = Integer.valueOf(dates[1]);
        switch (month) {
            case 1:
                ivToolbar.setImageResource(R.drawable.jan);
                break;
            case 2:
                ivToolbar.setImageResource(R.drawable.feb);
                break;
            case 3:
                ivToolbar.setImageResource(R.drawable.march);
                break;
            case 4:
                ivToolbar.setImageResource(R.drawable.apr);
                break;
            case 5:
                ivToolbar.setImageResource(R.drawable.may);
                break;
            case 6:
                ivToolbar.setImageResource(R.drawable.jun);
                break;
            case 7:
                ivToolbar.setImageResource(R.drawable.july);
                break;
            case 8:
                ivToolbar.setImageResource(R.drawable.aug);
                break;
            case 9:
                ivToolbar.setImageResource(R.drawable.sep);
                break;
            case 10:
                ivToolbar.setImageResource(R.drawable.oct);
                break;
            case 11:
                ivToolbar.setImageResource(R.drawable.nov);
                break;
            case 12:
                ivToolbar.setImageResource(R.drawable.dec);
                break;
        }
    }

    public void sortList(List<Note> noteList) {
        Collections.sort(noteList, new Comparator<Note>() {
            /**
             *
             * @param lhs
             * @param rhs
             * @return an integer < 0 if lhs is less than rhs, 0 if they are
             *         equal, and > 0 if lhs is greater than rhs,比较数据大小时,这里比的是时间
             */
            @Override
            public int compare(Note lhs, Note rhs) {
                Date date1 = DateUtils.stringToDate(lhs.getTime());
                Date date2 = DateUtils.stringToDate(rhs.getTime());
                // 对日期字段进行升序，如果欲降序可采用after方法
                if (date1.getTime() < date2.getTime()) return 1;
                else if (date1.getTime() > date2.getTime()) return -1;
                else return 0;
            }

        });
    }

    //method for create shortcut on android
    //need launcher permission
    private void createShortcut() {

        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        //only one shortcut allowed
        intent.putExtra("duplicate", false);

        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "MyNote");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.launcher));

        Intent intentShortcut = new Intent();
        intentShortcut.setAction("uk.co.fei.shortcut");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intentShortcut);

        sendBroadcast(intent);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mNoteList = mDbHelper.queryAll();
        sortList(mNoteList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


//        if (id == android.R.id.home) {
//            UIUtils.showToast(this, "slide menu");
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNoteList.size();
        }

        @Override
        public Note getItem(int position) {
            return mNoteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(MainActivity.this, R.layout.item_list_main, null);
                holder.tvNoteTitle = (TextView) convertView.findViewById(R.id.tvNoteTitle);
                holder.tvNoteContent = (TextView) convertView.findViewById(R.id.tvNoteContent);
                holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Note note = mNoteList.get(position);
            holder.tvNoteTitle.setText(note.getTitle());
            String time = "[" + note.getTime() + "]";
            String content = note.getContent();
            holder.tvNoteContent.setText(time + content);

            String imagePath = note.getImagePath();
            if (!TextUtils.isEmpty(imagePath)) {
//                Bitmap bitmap = BitmapUtil.getBitmapLocal(MainActivity.this, Uri.parse(imagePath));
//                Bitmap bitmap = null;
                mBitmapUtils = new BitmapUtils(MainActivity.this);
                mBitmapUtils.display(holder.ivPhoto, imagePath);
//                    bitmap = BitmapFactory.decodeStream(getContentResolver()
//                            .openInputStream(Uri.parse(imagePath)));
//                holder.ivPhoto.setImageBitmap(bitmap);
                holder.ivPhoto.setVisibility(View.VISIBLE);
            } else {
                holder.ivPhoto.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvNoteTitle;
        TextView tvNoteContent;
        ImageView ivPhoto;
    }
}
