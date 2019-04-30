package com.videoMaking.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.videoMaking.R;
import com.videoMaking.adapter.ImageLoadAdapter;
import com.videoMaking.adapter.VideoPlayerRecyclerAdapter;
import com.videoMaking.fragment.CommentFragment;
import com.videoMaking.fragment.SharePostFragment;
import com.videoMaking.modal.ImageModal;
import com.videoMaking.modal.VideoInfo;
import com.videoMaking.network.NetworkClient;
import com.videoMaking.playVideo.VideoPlayerRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String API_KEY = "9e5ef71432c64196a16273c85cfb94c1";
    private static final String TAG = "HomeActivity";
    private final String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    @BindView(R.id.like_active)
    ImageView likeActive;
    @BindView(R.id.feed_reuse)
    ImageView feedReuse;
    @BindView(R.id.feed_option)
    ImageView feedOption;
    @BindView(R.id.tv_friend)
    TextView friend;
    @BindView(R.id.tv_popular)
    TextView popular;
    @BindView(R.id.tv_collab)
    TextView collab;
    @BindView(R.id.background_image)
    ImageView backgroundImg;
    @BindView(R.id.recycles_profile)
    RecyclerView recyclerViewNews;
    private VideoPlayerRecyclerAdapter mAdapter;
    // not use butterKnife because  initialization is needed of recyclerView
    private VideoPlayerRecyclerView mRecyclerView;
    private List<VideoInfo> videoInfoList = new ArrayList<>();
    //not use butterKnife because showing only image when permission not granted
    private ImageView permissionImg;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private int isPermissionGrant = 0;
    private boolean isExist = false;
    private BottomSheetDialog bottomSheetDialog;
    private TextView message, whatsapp, instagram, facebook, copyLink, reportVideo, save;
    private Button closeBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setInItId();
        externalStoragePermission();
        // permission code for external storage
        if (isPermissionGrant == 1) {
            ButterKnife.bind(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            getAllVideoList();
            initRecyclerView();
            if (isNetworkConnected()) {// for News Api Result
                callNewsApi();
            } else {
                Toast.makeText(HomeActivity.this, "internet disconnected", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (isPermissionGrant == 0) {
                permissionImg.setVisibility(View.GONE);
                /*because first time video will not play if it recyclerView will not initialize*/
                initRecyclerView();
            }
        }
    }

    // get the all video list from the private app directory
    private List<VideoInfo> getAllVideoList() {
        String INTERNAL_PATH = "/storage/emulated/0/Android/data/com.videoMaking/files";
        String FOLDER_NAME = "/ShortClipVideo";
        File folder = new File(INTERNAL_PATH + FOLDER_NAME);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                VideoInfo videoInfo = new VideoInfo();
                videoInfo.setUrl(listOfFile.getAbsolutePath());
                videoInfo.setCoverUrl("");
                videoInfoList.add(videoInfo);
            }
        } else {
            backgroundImg.setVisibility(View.VISIBLE);
        }
        return videoInfoList;
    }

    private void setInItId() {
        mRecyclerView = findViewById(R.id.recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        permissionImg = findViewById(R.id.permission_image);
        backgroundImg = findViewById(R.id.background_image);
        createBottomSheet();
    }

    // if internet is not available give the toast
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    // setup the recylerview
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setMediaObjects(videoInfoList);
        mAdapter = new VideoPlayerRecyclerAdapter(videoInfoList, initGlide());
        mRecyclerView.setAdapter(mAdapter);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);
    }

    // setup the glide
    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_img)
                .error(R.drawable.white_img);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    /**
     * getting external storage permission
     */
    public void externalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(HomeActivity.this, permissionList)) {
                ActivityCompat.requestPermissions(HomeActivity.this, permissionList, 10);
            } else {
                isPermissionGrant = 1;
            }
        } else {
            isPermissionGrant = 1;
            Toast.makeText(this, "permission automatically granted", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        int count = 0;
        if (context != null && permissions != null) {
            Log.i(TAG, "hasPermissions: " + permissions.length);
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                    Log.i(TAG, "hasPermissions: " + count);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            // Log.i(TAG, "onRequestPermissionsResult: " + permissions);
            if (grantResults[0] == -1) {
                permissionImg.setVisibility(View.VISIBLE);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showMessageOkCancel(getString(R.string.storage_permission),
                            (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                finish();
                            });
                } else {
                    Toast.makeText(this, "Storage Permission not granted", Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults[1] == -1) {
                permissionImg.setVisibility(View.VISIBLE);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    showMessageOkCancel(getString(R.string.audio_permission),
                            (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                finish();
                            });
                } else {
                    Toast.makeText(this, getString(R.string.audio_permission_not_grant), Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults[2] == -1) {
                permissionImg.setVisibility(View.VISIBLE);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.i(TAG, "shouldShowRequestPermissionRationale:");
                    showMessageOkCancel("Camera permission is required to access camera",
                            (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                finish();
                            });
                } else {
                    Toast.makeText(this, "Camera Permission not granted ", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isPermissionGrant == 0) {
                    permissionImg.setVisibility(View.GONE);
                    // code will execute after the permission
                    Log.i(TAG, "permission else part ");
                    ButterKnife.bind(this);
                    bottomNavigationView.setOnNavigationItemSelectedListener(this);
                    if (isNetworkConnected()) {// for News Api Result
                        callNewsApi();
                    } else {
                        Toast.makeText(HomeActivity.this, "internet disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
                //
                // Toast.makeText(this, " Permissions  granted ", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void showMessageOkCancel(String permissionDetail, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(this).setMessage(permissionDetail)
                .setPositiveButton(getString(R.string.ok), onClickListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    // hit the api and get the data
    private void callNewsApi() {
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, true));
        //swipeRefreshLayout.setRefreshing(true);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(NetworkClient.BASE_URL1)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        NetworkClient api = retrofit.create(NetworkClient.class);
        Call<ResponseBody> call = api.getNews("in", API_KEY);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Log.i(TAG, "onResponse: " + response.body());
                try {
                    String result = response.body().string();
                    if (result != null) {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString("status");
                        int totalItem = jsonObject.getInt("totalResults");
                        if (status.equals("ok") && totalItem > 0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("articles");
                            if (jsonArray.length() > 0) {
                                List<ImageModal> imageModalList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonArticle = jsonArray.getJSONObject(i);
                                    String imgUrl = jsonArticle.getString("urlToImage");
                                    ImageModal imageModal = new ImageModal(imgUrl);
//                                Log.i(TAG, "values inside the for loop: " + authorName + "title" + title + "imgUrl" + imgUrl);
                                    imageModalList.add(imageModal);
                                }
                                ImageLoadAdapter imageLoadAdapter = new ImageLoadAdapter(HomeActivity.this, imageModalList);
                                recyclerViewNews.setAdapter(imageLoadAdapter);
                                imageLoadAdapter.notifyDataSetChanged();
                            } else {
                                // Log.i(TAG, "else part: ");
                            }
                        } else {

                            Log.i(TAG, "else part: wrong status code");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(HomeActivity.this, "Internet Issue", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Some Big Issue", Toast.LENGTH_SHORT).show();
                }
                // swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        mRecyclerView.onPausePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoInfoList.clear();
        List<VideoInfo> getVideoList = getAllVideoList();
        if (getVideoList != null && mAdapter != null) {
            backgroundImg.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.onRestartPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecyclerView != null)
            mRecyclerView.releasePlayer();
        super.onDestroy();
    }

    @OnClick({R.id.camera_open, R.id.like_active, R.id.feed_option, R.id.feed_reuse, R.id.tv_collab
            , R.id.tv_popular, R.id.tv_friend})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_friend:
                friend.setTextColor(Color.WHITE);
                popular.setTextColor(Color.GRAY);
                collab.setTextColor(Color.GRAY);
                Toast.makeText(HomeActivity.this, getString(R.string.friend_click), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_popular:
                popular.setTextColor(Color.WHITE);
                friend.setTextColor(Color.GRAY);
                collab.setTextColor(Color.GRAY);
                Toast.makeText(HomeActivity.this, getString(R.string.popular_click), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_collab:
                collab.setTextColor(Color.WHITE);
                popular.setTextColor(Color.GRAY);
                friend.setTextColor(Color.GRAY);
                Toast.makeText(HomeActivity.this, getString(R.string.collab_click), Toast.LENGTH_SHORT).show();
                break;
            case R.id.camera_open:
                Intent openVideoPage = new Intent(HomeActivity.this, VideoRecordActivity.class);
                startActivity(openVideoPage);
                break;
            case R.id.like_active:
                // add fragement
                addLikeRecordFragment();
                break;
            case R.id.feed_reuse:
                // add fragement
                addReuseRecordFragment();
                break;
            case R.id.feed_option:

                showBottomSheetDialog();
                break;
            default:
                Toast.makeText(HomeActivity.this, R.string.wrong_selection, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showBottomSheetDialog() {

        bottomSheetDialog.show();
    }

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null);
            message = view.findViewById(R.id.tv_message);
            whatsapp = view.findViewById(R.id.tv_whatsApp);
            instagram = view.findViewById(R.id.tv_instagram);
            facebook = view.findViewById(R.id.tv_facebook);
            save = view.findViewById(R.id.tv_save);
            reportVideo = view.findViewById(R.id.report_video);
            copyLink = view.findViewById(R.id.tv_copy_link);
            closeBottomSheet = view.findViewById(R.id.btn_cancel);
            message.setOnClickListener(this);
            whatsapp.setOnClickListener(this);
            instagram.setOnClickListener(this);
            facebook.setOnClickListener(this);
            save.setOnClickListener(this);
            reportVideo.setOnClickListener(this);
            copyLink.setOnClickListener(this);
            closeBottomSheet.setOnClickListener(this);
            bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(view);
        }

    }

    private void addLikeRecordFragment() {
        fragmentManager = getSupportFragmentManager();
        CommentFragment commentFragment = new CommentFragment();
        commentFragment.show(fragmentManager, getString(R.string.load_data));
    }

    private void addReuseRecordFragment() {
        fragmentManager = getSupportFragmentManager();
        SharePostFragment sharePostFragment = new SharePostFragment();
        sharePostFragment.show(fragmentManager, "Load data");
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_home:
                Toast.makeText(HomeActivity.this, getString(R.string.home), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_search:
                Toast.makeText(HomeActivity.this, getString(R.string.search), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_add:
                Toast.makeText(HomeActivity.this, getString(R.string.add), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_notification:
                Toast.makeText(HomeActivity.this, getString(R.string.navigation), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_NA:
                Toast.makeText(HomeActivity.this, getString(R.string.profile), Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(HomeActivity.this, getString(R.string.wrong_selection), Toast.LENGTH_SHORT).show();
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (isExist) {
            super.onBackPressed();
            return;
        }
        final int DELAY_TIME = 2000;
        isExist = true;
        Toast.makeText(this, getString(R.string.press_again_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> isExist = false, DELAY_TIME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_message:
                Toast.makeText(HomeActivity.this, getString(R.string.message), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_whatsApp:
                Toast.makeText(HomeActivity.this, getString(R.string.whatsapp), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_instagram:
                Toast.makeText(HomeActivity.this, getString(R.string.instagram), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_copy_link:
                Toast.makeText(HomeActivity.this, getString(R.string.copy_link), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_facebook:
                Toast.makeText(HomeActivity.this, "Facebook", Toast.LENGTH_SHORT).show();
                break;
            case R.id.report_video:
                Toast.makeText(HomeActivity.this, getString(R.string.profile), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_save:
                Toast.makeText(HomeActivity.this, getString(R.string.profile), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_cancel:
                shutBottomSheet();
                break;
            default:
                Toast.makeText(HomeActivity.this, getString(R.string.wrong_selection), Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void shutBottomSheet() {
        bottomSheetDialog.dismiss();

    }
}

