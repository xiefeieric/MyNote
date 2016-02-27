package uk.co.feixie.mynote.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;
import java.util.ArrayList;
import uk.co.feixie.mynote.R;
import uk.co.feixie.mynote.model.ServerApps;
import uk.co.feixie.mynote.utils.Constants;
import uk.co.feixie.mynote.utils.UIUtils;

public class AppsActivity extends AppCompatActivity {


    private ArrayList<ServerApps.App> mApps;
    private ProgressBar mProgressBar;
    private SharedPreferences mSharedPreferences;
    private AppsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppsActivity.this);
        initToolbar();
        initView();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_keyboard_backspace_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        RecyclerView rvApps = (RecyclerView) findViewById(R.id.rvApps);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setHasFixedSize(true);
        mAdapter = new AppsAdapter();
        rvApps.setAdapter(mAdapter);

    }

    private void initData() {
        x.Ext.init(getApplication());
        x.Ext.setDebug(true);
        loadDataFromWeb(Constants.APPS_URL);
        String apps = mSharedPreferences.getString("apps", "");
        if (!TextUtils.isEmpty(apps)) {
            showApps(apps);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left);
    }

    private void loadDataFromWeb(String url) {
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                String apps = mSharedPreferences.getString("apps", "");
                if (!TextUtils.equals(apps,result)) {
                    mSharedPreferences.edit().putString("apps",result).apply();
                    showApps(result);
                }

                if (TextUtils.isEmpty(apps)) {
                    showApps(result);
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                UIUtils.showToast(AppsActivity.this, ex.getMessage());
//                String apps = mSharedPreferences.getString("apps", "");
//                if (!TextUtils.isEmpty(apps)) {
//                    showApps(apps);
//                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                UIUtils.showToast(AppsActivity.this, cex.getMessage());
//                String apps = mSharedPreferences.getString("apps", "");
//                if (!TextUtils.isEmpty(apps)) {
//                    showApps(apps);
//                }
            }

            @Override
            public void onFinished() {
                mProgressBar.setVisibility(View.GONE);
//                String apps = mSharedPreferences.getString("apps", "");
//                if (!TextUtils.isEmpty(apps)) {
//                    showApps(apps);
//                }
            }
        });
    }

    private void showApps(String result) {

        Gson gson = new Gson();
        ServerApps serverApps = gson.fromJson(result, ServerApps.class);
        mApps = serverApps.apps;
        mAdapter.notifyDataSetChanged();
    }


    public class AppsAdapter extends RecyclerView.Adapter<AppsViewHolder> {

        @Override
        public AppsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_rv_apps, null);
            AppsViewHolder viewHolder = new AppsViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(AppsViewHolder holder, int position) {

            if (!getPackageName().equalsIgnoreCase(mApps.get(position).packageName)) {
                ServerApps.App app = mApps.get(position);
                x.image().bind(holder.ivAppsIcon, app.icon);
                holder.tvAppsTitle.setText(app.title);
                holder.tvAppsStatus.setText(app.status);
                holder.tvAppsDesc.setText(app.desc);
            }
        }

        @Override
        public int getItemCount() {
            if (mApps != null) {
                return mApps.size();
            }
            return 0;
        }
    }

    public class AppsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAppsIcon;
        private TextView tvAppsTitle;
        private TextView tvAppsStatus;
        private TextView tvAppsDesc;
        private LinearLayout llApps;

        public AppsViewHolder(View itemView) {
            super(itemView);

            ivAppsIcon = (ImageView) itemView.findViewById(R.id.ivAppsIcon);
            tvAppsTitle = (TextView) itemView.findViewById(R.id.tvAppsTitle);
            tvAppsStatus = (TextView) itemView.findViewById(R.id.tvAppsStatus);
            tvAppsDesc = (TextView) itemView.findViewById(R.id.tvAppsDesc);

            llApps = (LinearLayout) itemView.findViewById(R.id.llApps);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
            llApps.setLayoutParams(params);
            llApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //open selected app from google play
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mApps.get(getAdapterPosition()).uri)));
                    } catch (android.content.ActivityNotFoundException i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mApps.get(getAdapterPosition()).packageName)));
                    }
                }
            });
        }
    }
}
