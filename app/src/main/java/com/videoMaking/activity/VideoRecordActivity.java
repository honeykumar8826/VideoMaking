package com.videoMaking.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class VideoRecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = VideoRecordActivity.class.getName();
    private final String FOLDER_NAME = "ShortClipVideo";
    @BindView(R.id.button_capture)
    ImageView openCamera;
    @BindView(R.id.surfaceView)
    SurfaceView mSurfaceView;
    @BindView(R.id.mVideoView)
    VideoView mVideoView;
    @BindView(R.id.remain_seconds)
    TextView remainSecond;
    @BindView(R.id.mPlayVideo)
    ImageView playVideo;
    int currentCameraId = 0;
    private Camera mCamera;
    private ImageView switchCam;
    private SurfaceHolder mHolder;
    private MediaRecorder mMediaRecorder;
    private File mCurrentFile;
    private boolean isPlayVideo = false;
    private String mOutputFilePath;
    private CountDownTimer waitTimer;
    private int i = 0;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        // Create our Preview view and set it as the content of our activity.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    private void init() {
        ButterKnife.bind(this);
        switchCam = findViewById(R.id.switch_camera);
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
        switchCam.setVisibility(View.GONE);
        if (mVideoView.getVisibility() == View.GONE) {
            playVideo.setVisibility(View.VISIBLE);
            mVideoView.setVideoPath(mOutputFilePath);
            mVideoView.setOnCompletionListener(mp -> playVideo.setVisibility(View.VISIBLE));
            playVideo.setOnClickListener(v -> {
                mVideoView.setVisibility(View.VISIBLE);
                playVideo.setVisibility(View.GONE);
                MediaController mediaController = new MediaController(this);
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

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            Toast.makeText(context, getString(R.string.camera_check), Toast.LENGTH_SHORT).show();
            // no camera on this device
            return false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
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
        mMediaRecorder.setCamera(mCamera);
        if (currentCameraId == 1) {
            mMediaRecorder.setOrientationHint(270);
        } else {
            mMediaRecorder.setOrientationHint(90);
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mCurrentFile = createVideoFile();
        mMediaRecorder.setOutputFile(mCurrentFile.getAbsolutePath());
        mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
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
        File videoFile = File.createTempFile(fileName, ".mp4", storageDir);
        return videoFile;
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

    @OnClick({R.id.button_capture, R.id.camera_flash, R.id.open_gallery, R.id.switch_camera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_capture:
                // Create an instance of Camera
                if (checkCameraHardware(this)) {
                    if (isPlayVideo) {
                        stopVideoRecording();
                        if (waitTimer != null) {
                            waitTimer.cancel();
                            waitTimer = null;
                            remainSecond.setVisibility(View.GONE);
                        }
                    } else {
                        startVideoRecording();
                        mOutputFilePath = getCurrentFile().getAbsolutePath();
                    }
                }
                break;
            case R.id.camera_flash:
                if (i == 0) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                    i = -1;
                } else if (i == -1) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    i = 0;
                }
                break;
            case R.id.open_gallery:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivity(intent);
                break;
            case R.id.switch_camera:
                switchCamera();
                break;
            default:
                Toast.makeText(VideoRecordActivity.this, R.string.wrong_selection, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void switchCamera() {

        mCamera.stopPreview();
        mCamera.release();

        //swap the id of the camera to be used
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = Camera.open(currentCameraId);
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }
}
