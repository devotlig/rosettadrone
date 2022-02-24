package sq.rogue.rosettadrone.autolanding;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.lidar_map.my_point_3d.Point3D;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import sq.rogue.rosettadrone.R;
import sq.rogue.rosettadrone.RDApplication;
import sq.rogue.rosettadrone.settings.Tools;

public class TestingActivity extends Activity implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = "TestingActivity";

    private Button targetAndGimbalTestBtn;
    private Button targetTestBtn;
    private Button flightTestBtn;
    private Button targetAndFlightTestBtn;
    private Button fullStartTestBtn;
    private Button exitTestBtn;
    private Button multi1TestBtn;
    private Button multi2TestBtn;
    private ImageView imageView;
    protected TextureView videoSurface = null;
    private SurfaceView drawingSurface = null;
//    private EditText longitudeET;
//    private EditText latitudeET;
//    private EditText altitudeET;
    private EditText pidPET;
    private EditText pidIET;
    private EditText pidDET;


    protected VideoFeeder.VideoDataListener videoDataListener = null;
    protected VideoFeeder.VideoFeed videoFeed = null;
    protected DJICodecManager codecManager = null;

    private FlightController flightController;
    private Thread FCTestThread = null;
    private DrawingLandingPointThread drawingLandingPointThread;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        initFlightControl();
        initUI();

        Camera camera = RDApplication.getProductInstance().getCamera();

        //video read in 1
        videoFeed = VideoFeeder.getInstance().provideTranscodedVideoFeed();
        videoFeed.addVideoDataListener(new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                Log.d(TAG, "Video size:" + size);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //load opencv
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


        BaseProduct product = RDApplication.getProductInstance();

        if(product == null || !product.isConnected()) {

        }else {
            if(videoSurface != null) {
                videoSurface.setSurfaceTextureListener(this);
            }
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initFlightControl() {

        if(RDApplication.getProductInstance() != null) {
            flightController = ((Aircraft) RDApplication.getProductInstance()).getFlightController();
            flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                }
            });

        }

        flightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
    }

    private void initUI() {
        targetAndGimbalTestBtn = findViewById(R.id.targetAndGimbalTestBtn);
        flightTestBtn = findViewById(R.id.flightTestBtn);
        targetTestBtn = findViewById(R.id.targetTestBtn);
        targetAndFlightTestBtn = findViewById(R.id.targetAndFlightTestBtn);
        fullStartTestBtn = findViewById(R.id.fullStartTestBtn);
        exitTestBtn = findViewById(R.id.exitTestBtn);
        multi1TestBtn = findViewById(R.id.multiTest1Btn);
        multi2TestBtn = findViewById(R.id.multiTest2Btn);
//        longitudeET = findViewById(R.id.longitudeET);
//        latitudeET = findViewById(R.id.latitudeET);
//        altitudeET = findViewById(R.id.altitudeET);
        pidPET = findViewById(R.id.pidPET);
        pidIET = findViewById(R.id.pidIET);
        pidDET = findViewById(R.id.pidDET);

        targetAndGimbalTestBtn.setOnClickListener(this);
        flightTestBtn.setOnClickListener(this);
        targetTestBtn.setOnClickListener(this);
        targetAndFlightTestBtn.setOnClickListener(this);
        fullStartTestBtn.setOnClickListener(this);
        exitTestBtn.setOnClickListener(this);
        multi1TestBtn.setOnClickListener(this);
        multi2TestBtn.setOnClickListener(this);

        videoSurface = findViewById(R.id.videoPreviewerSurface);
        if(videoSurface != null) {
            videoSurface.setSurfaceTextureListener(this);
        }
        videoSurface.setVisibility(View.VISIBLE);

        imageView = findViewById(R.id.matshowV);

        drawingSurface = findViewById(R.id.drawingSurface);
        drawingSurface.getHolder().addCallback(TestingActivity.this);
        drawingSurface.setZOrderOnTop(true);
        drawingSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        drawingSurface.setVisibility(View.VISIBLE);
    }

    VisualLanding visualLanding;
    float a=0, b=0, c=0;
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.targetAndGimbalTestBtn:{
                //test for the 'check' before visual landing
                Tools.showToast(this, "Abandoned.");
                break;
            }
            case R.id.targetTestBtn:{
                testTargetDetect();
                break;
            }
            case R.id.flightTestBtn:{

                break;
            }
            case R.id.targetAndFlightTestBtn:{
                FCTestThread = new Thread(new VisualLandingFlightControl(true, codecManager));
                FCTestThread.start();
                break;
            }
            case R.id.fullStartTestBtn:{
                visualLanding = new VisualLanding(codecManager);
                visualLanding.startVisualLanding();
                break;
            }
            case R.id.exitTestBtn:{
//                    ((Aircraft)RDApplication.getProductInstance()).getFlightController()
//                            .setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
//                                @Override
//                                public void onResult(DJIError djiError) {
//                                    if(djiError != null) {
//                                        Tools.showToast(TestingActivity.this, "Exit error: "+djiError.getDescription());
////                                    }
////                                }
////                            });

                    visualLanding.visualLandingFlightControl.endVisualLandingFlightControl();

                    EventBus.getDefault().post(new ExitEvent());
                break;
            }
            case R.id.multiTest1Btn:{
                //pitch pid param adjust
                getDataFromET();
                EventBus.getDefault().post(new PIDParamChangeEvent(a, b, c, 1));
                break;
            }
            case R.id.multiTest2Btn:{
                //roll pid param adjust
                getDataFromET();
                EventBus.getDefault().post(new PIDParamChangeEvent(a, b, c, 2));
                break;
            }
            case R.id.multiTest3Btn:{
                //vertical throttle param adjust
                getDataFromET();
                EventBus.getDefault().post(new PIDParamChangeEvent(a, b, c, 3));
                break;
            }
            default: break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if(codecManager == null) {
            codecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    //---------------test method-------------
    private void testTargetDetect() {
        TargetDetect targetDetect = new TargetDetect(codecManager);
        Mat frame = targetDetect.getTestMat();
        Bitmap bitmap = null;
        if(frame != null) {
            if (frame.cols() > 0 && frame.rows() > 0) {
                bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(frame, bitmap);
            }
        }else {
            Log.e(TAG, "fail to create the bitmap");
        }
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);

        Thread targetDetection = new Thread(targetDetect);
        targetDetection.start();
    }

    private void getDataFromET() {
        if(pidPET.getText().toString() != null){
            a = Float.parseFloat(pidPET.getText().toString());
        }

        if(pidIET.getText().toString() != null) {
            b = Float.parseFloat(pidIET.getText().toString());
        }

        if(pidDET.getText().toString() != null) {
            c = Float.parseFloat(pidDET.getText().toString());
        }
        Tools.showToast(this, "Change the PID params.");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "SurfaceViewStart");
        drawingLandingPointThread = new DrawingLandingPointThread(drawingSurface, videoSurface.getHeight(), videoSurface.getWidth());
        drawingLandingPointThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        drawingLandingPointThread.drawingControl = false;
        drawingLandingPointThread.interrupt();
    }
}

