package com.videoMaking.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.videoMaking.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;

public class VideoRecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = VideoRecordActivity.class.getName();
    private final String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
    };
    private final String FOLDER_NAME="ShortClipVideo";
    private Camera mCamera;
    private ImageView openCamera;
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder;
    private File mCurrentFile;
    private boolean isPlayVideo = false;
    private VideoView mVideoView;
    private String mOutputFilePath;
    private TextView remainSecond;
    private ImageView playVideo;
    private  CountDownTimer waitTimer;


    public Camera getCameraInstance() {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        init();
        externalStoragePermission();

        // Create our Preview view and set it as the content of our activity.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        openCamera.setOnClickListener(v -> {
            // Create an instance of Camera
            if (checkCameraHardware(this)) {
                if (isPlayVideo) {
                    stopVideoRecording();
                    if (waitTimer != null) {
                        waitTimer.cancel();
                        waitTimer = null;
                        remainSecond.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "stop video ", Toast.LENGTH_SHORT).show();
                } else {
                    startVideoRecording();
                    mOutputFilePath = getCurrentFile().getAbsolutePath();
                }
            }
        });
    }

    private void init() {
        mVideoView = findViewById(R.id.mVideoView);
        remainSecond = findViewById(R.id.remain_seconds);
        mSurfaceView = findViewById(R.id.surfaceView);
        openCamera = findViewById(R.id.button_capture);
        playVideo = findViewById(R.id.mPlayVideo);
        mCamera = getCameraInstance();
    }

    protected File getCurrentFile() {
        return mCurrentFile;
    }

    private void stopVideoRecording() {
        mCamera.stopPreview();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        RelativeLayout relativeLayout = findViewById(R.id.relative_bottom);
        relativeLayout.setVisibility(View.GONE);

        if (mVideoView.getVisibility() == View.GONE) {
            playVideo.setVisibility(View.VISIBLE);
            mVideoView.setVideoPath(mOutputFilePath);
            mVideoView.setOnCompletionListener(mp -> playVideo.setVisibility(View.VISIBLE));
            playVideo.setOnClickListener(v -> {
                mVideoView.setVisibility(View.VISIBLE);
                playVideo.setVisibility(View.GONE);
                MediaController mediaController = new MediaController(this);
                // mediaController.setAnchorView(mVideoView);
                mVideoView.setMediaController(mediaController);
                mVideoView.start();
            });
        }
    }

    private void startVideoRecording() {
        try {
            initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void externalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, permissionList)) {
                ActivityCompat.requestPermissions(this, permissionList, 10);
            } else {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        } else {
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
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

    /*
     * Check if this device has a camera.
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mMediaRecorder == null) {
                mMediaRecorder = new MediaRecorder();
            }
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: in degree " + width + "height" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
    }

    private void initRecorder(Surface surface) throws IOException {

        isPlayVideo = true;
        mCamera.unlock();

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        mMediaRecorder.setPreviewDisplay(surface);
//        setCameraDisplayOrientation();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mCurrentFile = createVideoFile();
        mMediaRecorder.setOutputFile(mCurrentFile.getAbsolutePath());
       // mMediaRecorder.setVideoEncodingBitRate(1280*720);
        mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        //mMediaRecorder.setVideoSize(720, 480);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setMaxDuration(7000);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        startCountDownTimer();
        mMediaRecorder.start();
    }

    private void shutdown() {
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();
        mMediaRecorder = null;
        mCamera = null;
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "MP4" + timeStamp + "_";
        File storageDir = getExternalFilesDir(FOLDER_NAME);
        File img = File.createTempFile(fileName, ".mp4", storageDir);
        //store the current path of the image for later use
        String currentPath = img.getAbsolutePath();
         Log.i(TAG, "createImageFile: " + currentPath);
        return img;
    }

    private void startCountDownTimer() {
         waitTimer = new CountDownTimer(8000, 1000) {

            public void onTick(long millisUntilFinished) {
                remainSecond.setText(String.valueOf(millisUntilFinished / 1000));
                //here you can have your logic to set text to editText
            }

            public void onFinish() {
                remainSecond.setText(getString(R.string.click_save));
                mCamera.stopPreview();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
