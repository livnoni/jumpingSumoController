package livnoni.jumpingsumocontroller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;

import java.util.ArrayList;

public class PilotingActivity extends Activity implements ARDeviceControllerListener, ARDeviceControllerStreamListener, RecognitionListener {

    private static String TAG = PilotingActivity.class.getSimpleName();
    private static String SPEECH_COMMAND = "speechCommand";

    public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

    public ARDeviceController deviceController;
    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;

    private Button jumHightBt;
    private Button jumLongBt;

    private Button turnLeftBt;
    private Button turnRightBt;

    private Button forwardBt;
    private Button backBt;

    private TextView batteryLabel;

    private AlertDialog alertDialog;

    private RelativeLayout view;
//    private JpegView frameView;

    private static final int REQUEST_RECORD_PERMISSION = 100;
    private int maxLinesInput = 10;
    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    boolean listening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piloting);

        initIHM();
//        initVideo();

        Intent intent = getIntent();
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        //create the device
        try
        {
            device = new ARDiscoveryDevice();

            ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

            device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
        }
        catch (ARDiscoveryException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getError());
        }

        if (device != null)
        {
            try
            {
                //create the deviceController
                deviceController = new ARDeviceController (device);
                deviceController.addListener (this);
                deviceController.addStreamListener(this);
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }
        }

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);


        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    listening = true;
                    start();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    ActivityCompat.requestPermissions
                            (PilotingActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_PERMISSION);
                } else {
                    listening = false;
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    turnOf();
                }
            }
        });

    }

    private void initIHM ()
    {
        view = (RelativeLayout) findViewById(R.id.piloting_view);

        jumHightBt = (Button) findViewById(R.id.jumHightBt);
        jumHightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        jumLongBt = (Button) findViewById(R.id.jumLongBt);
        jumLongBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });


        turnRightBt = (Button) findViewById(R.id.turnRightBt);
        turnRightBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 10);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        turnLeftBt = (Button) findViewById(R.id.turnLeftBt);
        turnLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) -10);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 100);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -100);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        batteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    int speedLevel;
    boolean isMovingForward = false;
    boolean isMovingBackward = false;

    public void analyzeTextCommander(String text){
        if(text.contains("let go") || text.contains("let's go") || text.contains("lets go") ||text.contains("straight") || text.contains("forward") ||  text.contains("word") ){
            Log.d(SPEECH_COMMAND, "moveRobotForward---->");
            speedLevel = 30;
            isMovingForward = true;
            isMovingBackward = false;
            moveRobotForward(speedLevel);
        }
        if(text.contains("back") || text.contains("beck")  ){
            Log.d(SPEECH_COMMAND, "moveRobotBack---->");
            speedLevel = 30;
            isMovingForward = true;
            isMovingBackward = false;
            moveRobotBackward(speedLevel);
        }
        if(text.contains("fast")|| text.contains("fist") || text.contains("fest") || text.contains("5th") || text.contains("fit") || text.contains("fifth") || text.contains("fest") || text.contains("festival") || text.contains("chest") || text.contains("15") || text.contains("50") ){
            if(isMovingForward){
                speedLevel = speedLevel + 30;
                moveRobotForward(speedLevel);
            }
            if(isMovingBackward){
                speedLevel = speedLevel + 30;
                moveRobotBackward(speedLevel);
            }
        }
        if(text.contains("slow") || text.contains("owl") ){
            if(isMovingForward){
                speedLevel = speedLevel - 30;
                moveRobotForward(speedLevel);
            }
            if(isMovingForward){
                speedLevel = speedLevel - 30;
                moveRobotBackward(speedLevel);
            }
        }
        if(text.contains("stop") || text.contains("stuff") || text.contains("stuck")){
            Log.d(SPEECH_COMMAND, "stopRobot---->");
            isMovingForward = false;
            isMovingBackward = false;
            stopRobot();
        }
        if(text.contains("right") || text.contains("light") || text.contains("termite") || text.contains("price") || text.contains("quite") || text.contains("night") || text.contains("like") || text.contains("dynamite")){
            Log.d(SPEECH_COMMAND, "moveRobotRight---->");
            String [] words = text.split(" ");
            int counter = 1;
            for(int i=0;i<words.length; i++){
                if(words[i].equals("right") || words[i].equals("light")){
                    counter++;
                }
            }
            moveRobot("right", counter);

        }
        if(text.contains("left") || text.contains("lift")){
            Log.d(SPEECH_COMMAND, "moveRobotLeft---->");
            String [] words = text.split(" ");
            int counter = 1;
            for(int i=0;i<words.length; i++){
                if(words[i].equals("left") || text.contains("lift")){
                    counter++;
                }
            }
            moveRobot("left", counter);
        }

    }

    public void moveRobotForward(int speed){
        if (deviceController != null)
        {
            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) speed);
            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
        }
    }

    public void moveRobotBackward(int speed){
        if (deviceController != null)
        {
            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -speed);
            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
        }
    }

    public void stopRobot(){
        if (deviceController != null)
        {
            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);

        }
    }



    public void moveRobot(String direction, int counter){
        if(direction == "right"){
            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 10);
        }
        if(direction == "left"){
            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) -10);
        }
        deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);


        new CountDownTimer(500*counter, 500) {

            public void onTick(long millisUntilFinished) {
                //do nothing            }
            }

            public void onFinish() {
                deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
            }
        }.start();

    }

    public void start(){
        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener((RecognitionListener) this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxLinesInput);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10);

    }

    public void turnOf(){
        speech.stopListening();
        speech.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(PilotingActivity.this, "start talk...", Toast
                            .LENGTH_SHORT).show();
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(PilotingActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

//    @Override
//    protected void onStop() {
//        super.onStop();
////        if (speech != null) {
////            speech.destroy();
////            Log.i(LOG_TAG, "destroy");
////        }
//    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.i(LOG_TAG, "onReadyForSpeech");

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
        if(!listening){
            turnOf();
        }
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.i(LOG_TAG, "onBufferReceived: " + bytes);

    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        Log.i(LOG_TAG, "onResults="+text);

        text = text.toLowerCase();

        analyzeTextCommander(text);

        returnedText.setText(text);
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(LOG_TAG, "onPartialResults");
//        ArrayList<String> matches = bundle
//                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//        String text = "";
//        for (String result : matches)
//            text += result + "\n";
//        Log.i(LOG_TAG, "onPartialResults="+text);

        String[] results = bundle.getStringArray("com.google.android.voicesearch.UNSUPPORTED_PARTIAL_RESULTS");
        Log.i(LOG_TAG, "onPartialResults="+results);

    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onEvent");

    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                turnOf();
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //start the deviceController
        if (deviceController != null)
        {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Connecting ...");
            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ARCONTROLLER_ERROR_ENUM error = deviceController.start();

            if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)
            {
                finish();
            }
        }
    }

    private void stopDeviceController()
    {
        if (deviceController != null)
        {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");
            // show it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ARCONTROLLER_ERROR_ENUM error = deviceController.stop();

                    if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop()
    {
//        if (deviceController != null)
//        {
//            deviceController.stop();
//        }

        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        stopDeviceController();
    }

    public void onUpdateBattery(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });
    }


    @Override
    public void onStateChanged (ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
    {
        Log.i(TAG, "onStateChanged ... newState:" + newState + " error: " + error);

        switch (newState)
        {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                //The deviceController is started
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_RUNNING .....");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                });
                deviceController.getFeatureJumpingSumo().sendMediaStreamingVideoEnable((byte)1);
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                //The deviceController is stoped
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_STOPPED .....");

                deviceController.dispose();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        alertDialog.dismiss();
                        finish();
                    }
                });
                break;

            default:
                break;
        }
    }


    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary)
    {
        if (elementDictionary != null)
        {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED)
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null)
                {
                    Integer batValue = (Integer) args.get("arcontroller_dictionary_key_common_commonstate_batterystatechanged_percent");

                    onUpdateBattery(batValue);
                }
            }
        }
        else
        {
            Log.e(TAG, "elementDictionary is null");
        }
    }

//    @Override
//    public void onFrameReceived(ARDeviceController deviceController, ARFrame frame)
//    {
//        if (frame.isIFrame()) {
//            byte[] data = frame.getByteData();
//            ByteArrayInputStream ins = new ByteArrayInputStream(data);
//            Bitmap bmp = BitmapFactory.decodeStream(ins);
//
//            frameView.setBitmap(bmp);
//
//        FrameDisplay fDisplay = new FrameDisplay(imgView, bmp);
//        fDisplay.execute();
//        }
//    }
//
//    @Override
//    public void onFrameTimeout(ARDeviceController deviceController)
//    {
//        Log.i(TAG, "onFrameTimeout ..... ");
//    }
//
//    public void initVideo()
//    {
//        //imgView = (ImageView) findViewById(R.id.imageView);
//        String deviceModel = Build.DEVICE;
//        Log.d(TAG, "configuring HW video codec for device: [" + deviceModel + "]");
//        frameView = new JpegView(getApplicationContext());
//        frameView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//        view.addView(frameView, 0);
//    }


    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }



    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {
        return null;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        return null;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {

    }


}
