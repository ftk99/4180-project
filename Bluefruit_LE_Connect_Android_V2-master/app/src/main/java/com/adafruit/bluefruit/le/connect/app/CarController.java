package com.adafruit.bluefruit.le.connect.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.adafruit.bluefruit.le.connect.R;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.logging.Logger;

public class CarController extends Fragment implements SensorEventListener {
    // Log
    @SuppressWarnings("unused")
    private final static String TAG = CarController.class.getSimpleName();

    // Config
    private final static float kMinAspectRatio = 1.8f;
    //https://developer.android.com/guide/topics/sensors/sensors_motion#java
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private float currentDegree = 0f;
    private ImageView pointer;

    // UI TextBuffer (refreshing the text buffer is managed with a timer because a lot of changes can arrive really fast and could stall the main thread)
    private Handler mUIRefreshTimerHandler = new Handler();
    private Runnable mUIRefreshTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUITimerRunning) {
                updateTextDataUI();
                // Log.d(TAG, "updateDataUI");
                mUIRefreshTimerHandler.postDelayed(this, 200);
            }
        }
    };
    private boolean isUITimerRunning = false;

    // UI
    private ViewGroup mContentView;
    private EditText mBufferTextView;
    private ViewGroup mRootLayout;
    private View mTopSpacerView;
    private View mBottomSpacerView;

    // Data
    private CarControllerListener mListener;
    private volatile StringBuilder mDataBuffer = new StringBuilder();
    private volatile StringBuilder mTextSpanBuffer = new StringBuilder();
    private int maxPacketsToPaintAsText;
    View.OnTouchListener mPadButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int tag = Integer.valueOf((String) view.getTag());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setPressed(true);
                Log.i("carVal","" +tag + "," + 1);
                mListener.onSendCarCommand(tag, 1);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.setPressed(false);
                Log.i("carVal","" +tag + "," + 0);
                mListener.onSendCarCommand(tag, 0);
                view.performClick();
                return true;
            }
            return false;
        }
    };

    // region Lifecycle
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static CarController newInstance() {
        CarController fragment = new CarController();
        return fragment;
    }

    public CarController() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set title
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.car_title);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // UI
        mRootLayout = view.findViewById(R.id.rootLayout);
        mTopSpacerView = view.findViewById(R.id.topSpacerView);
        mBottomSpacerView = view.findViewById(R.id.bottomSpacerView);

        mContentView = view.findViewById(R.id.contentView);
        mBufferTextView = view.findViewById(R.id.bufferTextView);
        if (mBufferTextView != null) {
            mBufferTextView.setKeyListener(null);     // make it not editable
        }

//        ImageButton upArrowImageButton = view.findViewById(R.id.upArrowImageButton);
//        upArrowImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton leftArrowImageButton = view.findViewById(R.id.leftArrowImageButton);
//        leftArrowImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton rightArrowImageButton = view.findViewById(R.id.rightArrowImageButton);
//        rightArrowImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton bottomArrowImageButton = view.findViewById(R.id.bottomArrowImageButton);
//        bottomArrowImageButton.setOnTouchListener(mPadButtonTouchListener);
//
//        ImageButton button1ImageButton = view.findViewById(R.id.button1ImageButton);
//        button1ImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton button2ImageButton = view.findViewById(R.id.button2ImageButton);
//        button2ImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton button3ImageButton = view.findViewById(R.id.button3ImageButton);
//        button3ImageButton.setOnTouchListener(mPadButtonTouchListener);
//        ImageButton button4ImageButton = view.findViewById(R.id.button4ImageButton);
//        button4ImageButton.setOnTouchListener(mPadButtonTouchListener);
        final Context context = getContext();
        sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        ImageButton acc = view.findViewById(R.id.acc);
        acc.setOnTouchListener(mPadButtonTouchListener);
        ImageButton brake = view.findViewById(R.id.brake);
        brake.setOnTouchListener(mPadButtonTouchListener);
        ImageButton gear = view.findViewById(R.id.gear);
        gear.setOnTouchListener(mPadButtonTouchListener);
        //ImageView pointer = view.findViewById(R.id.point);
        pointer = view.findViewById(R.id.point);

        // Read shared preferences
        maxPacketsToPaintAsText = UartBaseFragment.kDefaultMaxPacketsToPaintAsText; //PreferencesFragment.getUartTextMaxPackets(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
    private void rotateImage(ImageView imageView) {
        Matrix matrix = new Matrix();
        imageView.setScaleType(ImageView.ScaleType.MATRIX); //required
        matrix.postRotate((float) 20, imageView.getDrawable().getBounds().width()/2,    imageView.getDrawable().getBounds().height()/2);
        imageView.setImageMatrix(matrix);
    }
    public void onSensorChanged(SensorEvent event)
    {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

//        final float alpha = 0.8;
//
//        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
//
//        linear_acceleration[0] = event.values[0] - gravity[0];
//        linear_acceleration[1] = event.values[1] - gravity[1];
//        linear_acceleration[2] = event.values[2] - gravity[2];
        //https://www.javacodegeeks.com/2013/09/android-compass-code-example.html
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            //float degree = Math.round(event.values[0]);
            float degree = 0;

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // In landscape
                float preMod = -(90 + (10*event.values[1]));
                degree = Math.round(preMod/10) * 10;
            } else {
                // In portrait
                float preMod = -(90 - (10*event.values[0]));
                degree = Math.round(preMod/10) * 10;
            }

            Log.i("car", ""+    degree);
//            Log.i("carx", "" + event.values[0]);
//            Log.i("cary", "" + event.values[1]);
//            Log.i("carz", "" + event.values[2]);
            //degree = -10;
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            pointer.startAnimation(ra);
            currentDegree = -degree;
            //Log.i("carVal","" +4 + "," + 1);
            mListener.onSendCarCommand(4, Math.max(0,((int)currentDegree )));
            //Log.d("card", "" + (int)currentDegree ));

        }
    }
    @Override
    public void onAttach(@NonNull Context context) {
        Log.d("car","" + context);
        super.onAttach(context);
        Log.d("car","" + context);
        Log.d("car", "" + context.getClass());
        Log.d("car", "" + getTargetFragment().getClass());
        if (context instanceof CarControllerListener) {
            mListener = (CarControllerListener) context;
        } else if (getTargetFragment() instanceof CarControllerListener) {
            mListener = (CarControllerListener) getTargetFragment();
        } else {
            throw new RuntimeException(context.toString() + " must implement CarControllerListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewTreeObserver observer = mRootLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                adjustAspectRatio();
                mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Refresh timer
        isUITimerRunning = true;
        mUIRefreshTimerHandler.postDelayed(mUIRefreshTimerRunnable, 0);
    }

    @Override
    public void onPause() {
        isUITimerRunning = false;
        mUIRefreshTimerHandler.removeCallbacksAndMessages(mUIRefreshTimerRunnable);

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_help, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentActivity activity = getActivity();

        switch (item.getItemId()) {
            case R.id.action_help:
                if (activity != null) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    if (fragmentManager != null) {
                        CommonHelpFragment helpFragment = CommonHelpFragment.newInstance(getString(R.string.controlpad_help_title), getString(R.string.controlpad_help_text));
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                                .replace(R.id.contentLayout, helpFragment, "Help");
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // endregion

    // region UI

    private void adjustAspectRatio() {
        ViewGroup rootLayout = mContentView;
        final int mainWidth = rootLayout.getWidth();

        if (mainWidth > 0) {
            final int mainHeight = rootLayout.getHeight() - mTopSpacerView.getLayoutParams().height - mBottomSpacerView.getLayoutParams().height;
            if (mainHeight > 0) {
                // Add black bars if aspect ratio is below min
                final float aspectRatio = mainWidth / (float) mainHeight;
                if (aspectRatio < kMinAspectRatio) {
                    final int spacerHeight = Math.round(mainHeight - mainWidth / kMinAspectRatio);
                    ViewGroup.LayoutParams topLayoutParams = mTopSpacerView.getLayoutParams();
                    topLayoutParams.height = spacerHeight / 2;
                    mTopSpacerView.setLayoutParams(topLayoutParams);

                    ViewGroup.LayoutParams bottomLayoutParams = mBottomSpacerView.getLayoutParams();
                    bottomLayoutParams.height = spacerHeight / 2;
                    mBottomSpacerView.setLayoutParams(bottomLayoutParams);
                }
            }
        }
    }

    public synchronized void addText(String text) {
        mDataBuffer.append(text);
    }


    private int mDataBufferLastSize = 0;

    private synchronized void updateTextDataUI() {

        final int bufferSize = mDataBuffer.length();
        if (mDataBufferLastSize != bufferSize) {

            if (bufferSize > maxPacketsToPaintAsText) {
                mDataBufferLastSize = bufferSize - maxPacketsToPaintAsText;
                mTextSpanBuffer.setLength(0);
                mTextSpanBuffer.append(getString(R.string.uart_text_dataomitted)).append("\n");
                mDataBuffer.replace(0, mDataBufferLastSize, "");
                mTextSpanBuffer.append(mDataBuffer);

            } else {
                mTextSpanBuffer.append(mDataBuffer.substring(mDataBufferLastSize, bufferSize));
            }

            mDataBufferLastSize = mDataBuffer.length();
            mBufferTextView.setText(mTextSpanBuffer);
            mBufferTextView.setSelection(0, mTextSpanBuffer.length());        // to automatically scroll to the end
        }
    }
    // endregion

    // region ControllerPadFragmentListener
    public interface CarControllerListener {
        void onSendCarCommand(int tag, int value);
    }
    // endregion
}
