package uk.co.feixie.mynote.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import java.util.Locale;

import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.db.DbHelper;
import uk.co.feixie.mynote.model.Note;
import uk.co.feixie.mynote.utils.DateUtils;
import uk.co.feixie.mynote.utils.UIUtils;

public class MainActivity extends AppCompatActivity {

    private ListView lvMainContent;
    private ListView lvLeftMenu;
    private DrawerLayout dlMenu;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Note> mNoteList;
    private MyListAdapter mAdapter;
    private DbHelper mDbHelper;
    private ImageView ivToolbar;
    private Note clickedNote;
    private SearchView mSearchView;
    private List<String> mCategoryList;
    private MyCategoryAdapter mCategoryAdapter;
    private String selectedCategory;
    private int sortType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNoteList = new ArrayList<>();
        mCategoryList = new ArrayList<>();
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

                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);

        ivToolbar = (ImageView) findViewById(R.id.ivToolbar);
        getCurrentMonthToShow();
    }

    private void initViews() {

        lvMainContent = (ListView) findViewById(R.id.lvMainContent);
        lvLeftMenu = (ListView) findViewById(R.id.lvLeftMenu);
        dlMenu = (DrawerLayout) findViewById(R.id.dlMenu);

        mDbHelper = new DbHelper(this);
        new Thread() {
            @Override
            public void run() {
                mNoteList = mDbHelper.queryAll();
                sortList(mNoteList);
                mCategoryList = mDbHelper.queryAllCategory();
                Collections.sort(mCategoryList, String.CASE_INSENSITIVE_ORDER);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new MyListAdapter();
                        lvMainContent.setAdapter(mAdapter);

                        mCategoryAdapter = new MyCategoryAdapter();
                        lvLeftMenu.setAdapter(mCategoryAdapter);
                        for (int i = 0; i < mCategoryList.size(); i++) {
                            if (mCategoryList.get(i).equalsIgnoreCase("all notes")){
                                lvLeftMenu.setItemChecked(i, true);
                                return;
                            }
                        }
                    }
                });
            }
        }.start();

        mDrawerToggle = new ActionBarDrawerToggle(this, dlMenu, mToolbar, R.string.drawer_open, R.string.drawer_close);
        dlMenu.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void initListeners() {

        lvMainContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = mNoteList.get(position);
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("note", note);
                startActivity(intent);
            }
        });

        lvMainContent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Snackbar.make(view,"onLongClick",Snackbar.LENGTH_SHORT).show();
                final Note note = mNoteList.get(position);
                setClickedNote(note);
//                MyDialogFragment dialogFragment = new MyDialogFragment();
//                dialogFragment.show(getSupportFragmentManager(), "dialogFragment");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(new CharSequence[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                            intent.putExtra("note", note);
                            startActivity(intent);
                        }

                        if (which == 1) {
//                            DbHelper dbHelper = new DbHelper(MainActivity.this);
//                            System.out.println(note.getId());
                            new Thread() {
                                @Override
                                public void run() {
                                    boolean delete = mDbHelper.delete(note);
                                    if (delete) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                UIUtils.showToast(MainActivity.this, "Delete Success.");
                                                mNoteList.remove(note);
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                UIUtils.showToast(MainActivity.this, "Delete Fail.");
                                            }
                                        });
                                    }
                                }
                            }.start();
                        }
                    }
                });

                builder.show();

                return true;
            }
        });

        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String category = mCategoryList.get(position);
                selectedCategory = category;
                if (category.equalsIgnoreCase("all notes")) {
                    mNoteList = mDbHelper.queryAll();
                } else {
                    mNoteList = mDbHelper.queryNoteByCategory(category);
                }
                sortOrder();
                mAdapter.notifyDataSetChanged();
                dlMenu.closeDrawers();
            }
        });

        lvLeftMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String category = mCategoryList.get(position);
                if (category.equalsIgnoreCase("all notes")) {
                    UIUtils.showToast(MainActivity.this, "Item can not be deleted");
                } else {
                    mNoteList = mDbHelper.queryNoteByCategory(category);
                    if (mNoteList.size() > 0) {
                        UIUtils.showToast(MainActivity.this, "Please delete all notes under this category first!");
                    } else if (mCategoryList.get(position).equalsIgnoreCase(selectedCategory)) {
                        UIUtils.showToast(MainActivity.this, "Selected category can not be deleted!");
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Are you sure you want to delete the item?");
                        builder.setNegativeButton("Cancel", null);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCategoryList.remove(category);
                                mCategoryAdapter.notifyDataSetChanged();
                                new Thread(){
                                    @Override
                                    public void run() {
                                        mDbHelper.deleteCategory(category);
                                    }
                                }.start();
                            }
                        });
                        builder.show();
                    }
                }
                return true;
            }
        });

    }

    private void getCurrentMonthToShow() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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

    //sort most recent order
    public void sortList(List<Note> noteList) {
        Collections.sort(noteList, new Comparator<Note>() {
            /**
             *
             * param: lhs
             * param: rhs
             * return: an integer < 0 if lhs is less than rhs, 0 if they are
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

    //sort ascending order
    public void sortListAscending(List<Note> noteList) {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note current, Note after) {
                String currentTitle = current.getTitle();
                String afterTitle = after.getTitle();
                int compare = currentTitle.compareToIgnoreCase(afterTitle);
                if (compare>0) return 1;
                else if (compare<0) return -1;
                else return 0;
            }

        });
    }

    //sort descending order
    public void sortListDescending(List<Note> noteList) {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note current, Note after) {
                String currentTitle = current.getTitle();
                String afterTitle = after.getTitle();
                int compare = currentTitle.compareToIgnoreCase(afterTitle);
                if (compare<0) return 1;
                else if (compare>0) return -1;
                else return 0;
            }

        });
    }

    //method for create shortcut on android
    //need launcher permission
//    private void createShortcut() {
//
//        Intent intent = new Intent();
//        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
//        //only one shortcut allowed
//        intent.putExtra("duplicate", false);
//
//        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "MyNote");
//        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.launcher));
//
//        Intent intentShortcut = new Intent();
//        intentShortcut.setAction("uk.co.fei.shortcut");
//        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentShortcut);
//
//        sendBroadcast(intent);
//
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Thread() {
            @Override
            public void run() {

                if (selectedCategory != null) {
                    if (selectedCategory.equalsIgnoreCase("all notes")) {
                        mNoteList = mDbHelper.queryAll();
                    } else {
                        mNoteList = mDbHelper.queryNoteByCategory(selectedCategory);
                    }
                } else {
                    mNoteList = mDbHelper.queryAll();
                }

                sortOrder();

                mCategoryList = mDbHelper.queryAllCategory();
                Collections.sort(mCategoryList,String.CASE_INSENSITIVE_ORDER);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mCategoryAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();

        mSearchView.setQuery("", true);
    }

    private void sortOrder() {
        if (sortType==0) {
            sortList(mNoteList);
        } else if (sortType==1) {
            sortListAscending(mNoteList);
        } else if (sortType==2) {
            sortListDescending(mNoteList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

//                mNoteList = mDbHelper.queryName(query);
//                mAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (TextUtils.isEmpty(newText)) {
                    mNoteList = mDbHelper.queryAll();
                    sortList(mNoteList);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mNoteList = mDbHelper.queryName(newText);
                    sortList(mNoteList);
                    mAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == R.id.action_apps) {
            Intent intent = new Intent(this,AppsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.action_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(new String[]{"Most Recent", "Ascending", "Descending"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sortType = which;
                    if (which==0) {
                        sortList(mNoteList);
                    } else if (which==1) {
                        sortListAscending(mNoteList);
                    } else if (which==2) {
                        sortListDescending(mNoteList);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("CANCEL", null);
            builder.show();
            return true;
        }

//        if (id == android.R.id.home) {
//            UIUtils.showToast(this, "slide menu");
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public Note getClickedNote() {
        return clickedNote;
    }

    public void setClickedNote(Note clickedNote) {
        this.clickedNote = clickedNote;
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
            String time = note.getTime();

            //reformat date to "dd/mm/yyyy"
            Date date = DateUtils.stringToDate(time);
            SimpleDateFormat fomatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String newTime = fomatter.format(date);

            String content = note.getContent();
            String newContent = newTime + " " + content;
            //change time's color in textview
            SpannableStringBuilder style = new SpannableStringBuilder(newContent);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.jikelv)), 0, newTime.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.tvNoteContent.setText(style);

            String imagePath = note.getImagePath();
            if (!TextUtils.isEmpty(imagePath)) {
//                Bitmap bitmap = BitmapUtil.getBitmapLocal(MainActivity.this, Uri.parse(imagePath));
//                Bitmap bitmap = null;
                BitmapUtils bitmapUtils = new BitmapUtils(MainActivity.this);
                bitmapUtils.display(holder.ivPhoto, imagePath);
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


    public class MyCategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCategoryList.size();
        }

        @Override
        public String getItem(int position) {
            return mCategoryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_list_left_menu, null);
            }

            TextView tvLeftMenu = (TextView) convertView.findViewById(R.id.tvLeftMenu);
            tvLeftMenu.setText(mCategoryList.get(position));

            return convertView;
        }
    }
}
