package br.com.tfleet.tfleetqrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    Intent takeVideoIntent;
    VideoView mVideoView;

    private Camera myCamera;
    private MyCameraSurfaceView myCameraSurfaceView;

    private Button btnRecordVideo, parar;
    private MediaRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myCamera = getCameraInstance();
        if(myCamera == null){
            Toast.makeText(MainActivity.this,"Camera FAIL",Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
        FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.videoview);
        myCameraPreview.addView(myCameraSurfaceView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                comeca();
            }
        }, 1000);

        parar = (Button) findViewById(R.id.parar);
        parar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OPBA", "INICIAL");
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
        });

        btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OPBA", "1");
                comeca();
                Log.i("OPBA", "2");

            }
        });
    }

    public void comeca(){

        // record video
        String filename;
        String path;

        Log.i("MI", "MIMI");
        if(myCamera==null)
            myCamera = Camera.open();

        recorder = new MediaRecorder();

        myCamera.lock();
        myCamera.unlock();

        recorder.setCamera(myCamera);

        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

        recorder.setVideoSize(640,480);
        recorder.setVideoEncodingBitRate(3000000);


        path= Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        Date date=new Date();
        filename="/rec"+date.toString().replace(" ", "_").replace(":", "_")+".mp4";
        File file=new File(path,filename);

        recorder.setOutputFile(path+filename);
        Log.i("OPBA", ""+path+filename);

        try {
            recorder.prepare();
            //Thread.sleep(1000);
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakeVideoIntent() {
        takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            mVideoView.setVideoURI(videoUri);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.start();

        }
    }

    private Camera getCameraInstance(){
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /*
 * Recording video
 */





    public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

        private SurfaceHolder mHolder;
        private Camera mCamera;

        public MyCameraSurfaceView(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int weight,
                                   int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // make any resize, rotate or reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                mCamera.setDisplayOrientation(90);

            } catch (Exception e){
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub

        }
    }
}
