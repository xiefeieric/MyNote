package uk.co.feixie.mynote.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DB;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.BitmapUtils;
import uk.co.feixie.mynote.utils.DateUtils;
import uk.co.feixie.mynote.utils.UIUtils;

public class MainActivity extends AppCompatActivity {

    private ListView lvMainContent;
    private DrawerLayout dlMenu;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Note> mNoteList;
    private MyListAdapter mAdapter;
    private DbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNoteList = new ArrayList<>();


        initToolbar();
        initFloatingButton();
//        initDatabase();
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
        actionBar.setTitle("All Notes");
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

//    private void initDatabase() {
//        DB db = new DB(this);
//        SQLiteDatabase writableDatabase = db.getWritableDatabase();
//
//    }

    private void initViews() {
        lvMainContent = (ListView) findViewById(R.id.lvMainContent);
        mDbHelper = new DbHelper(this);
        mNoteList = mDbHelper.queryAll();
        sortList(mNoteList);


        mAdapter = new MyListAdapter();
        lvMainContent.setAdapter(mAdapter);
        //lvMainContent.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc", "abc"}));

        dlMenu = (DrawerLayout) findViewById(R.id.dlMenu);
        mDrawerToggle = new ActionBarDrawerToggle(this,dlMenu,mToolbar, R.string.drawer_open,R.string.drawer_close);
        dlMenu.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void initListeners() {

        lvMainContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = mNoteList.get(position);
                note.setId(mNoteList.size()-position);
                Intent intent = new Intent(MainActivity.this,ViewNoteActivity.class);
                intent.putExtra("note",note);
                startActivity(intent);
            }
        });

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
                if(date1.getTime() < date2.getTime()) return 1;
                else if(date1.getTime() > date2.getTime()) return -1;
                else return 0;
            }

        });
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
            if (convertView==null) {
                holder = new ViewHolder();
                convertView = View.inflate(MainActivity.this,R.layout.item_list_main,null);
                holder.tvNoteTitle = (TextView) convertView.findViewById(R.id.tvNoteTitle);
                holder.tvNoteContent = (TextView) convertView.findViewById(R.id.tvNoteContent);
                holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Note note = mNoteList.get(position);
            holder.tvNoteTitle.setText(note.getTitle());
            String time = "["+note.getTime()+"]";
            String content = note.getContent();
            holder.tvNoteContent.setText(time+content);

            String imagePath = note.getImagePath();
            if (!TextUtils.isEmpty(imagePath)) {
//                Bitmap bitmap = BitmapUtils.getBitmapLocal(MainActivity.this, Uri.parse(imagePath));
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver()
                            .openInputStream(Uri.parse(imagePath)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                holder.ivPhoto.setImageBitmap(bitmap);
                holder.ivPhoto.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        public class ViewHolder {
            TextView tvNoteTitle;
            TextView tvNoteContent;
            ImageView ivPhoto;
        }
    }
}
