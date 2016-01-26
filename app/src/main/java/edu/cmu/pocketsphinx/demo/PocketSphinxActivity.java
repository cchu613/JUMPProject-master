

package edu.cmu.pocketsphinx.demo;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;


import java.util.Properties;
import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener, OnInitListener {

   // Button slideButton;
    //SlidingDrawer slidingDrawer;

    ImageButton returnButton;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private TextToSpeech tts;
    private static final String COMMANDS_SEARCH = "commands";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "take picture";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);


        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(COMMANDS_SEARCH, R.string.commands_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");


        tts = new TextToSpeech(this, this);

        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        returnButton = (ImageButton)findViewById(R.id.returnButton);
        // Register the onClick listener with the implementation above
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                v.startAnimation(animAlpha);

                recognizer.cancel();
                recognizer.shutdown();
                startActivity(new Intent(PocketSphinxActivity.this, MainScreen.class));
            }
        });

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    public static String executeRemoteCommand(String username,String password,String hostname,int port, int degree)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand("python3 stepper.py "+degree);
        channelssh.connect();
        channelssh.disconnect();

        return baos.toString();
    }
    @Override
    public void onInit(int code){
        if(code==TextToSpeech.SUCCESS)
        {
            tts.setLanguage(Locale.getDefault());
        }
        else {
            tts = null;
            Toast.makeText(this, "Failed to initialize TTS engine.",

                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        recognizer.cancel();
        recognizer.shutdown();
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
    
    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
    	    return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            switchSearch(COMMANDS_SEARCH);
        }
        else {
            ((TextView) findViewById(R.id.result_text)).setText(text);
        }
    }

    //Translate word commands to degrees
    public void takePic(String text)
    {
        int degree = commandToDegree(text);
        rotateCamera(degree); //send signal to rotate camera and take picture
    }
    private int commandToDegree(String text)
    {
        int degree = -1;
        switch(text)
        {
            case "right side":
                degree = 90;
                break;
            case "left side":
                degree = -90;
                break;
            case "front side":
                degree = 0;
                break;
            case "back side":
                degree = 180;
                break;
            case "front right":
                degree = 45;
                break;
            case "front left":
                degree = -45;
                break;
            case "back right":
                degree = 135;
                break;
            case "back left":
                degree = -135;
                break;
            /*
            case "one o clock":
                degree = 30;
                break;
            case "two o clock":
                degree = 60;
                break;
            case "three o clock":
                degree = 90;
                break;
            case "four o clock":
                degree = 120;
                break;
            case "five o clock":
                degree = 150;
                break;
            case "six o clock":
                degree = 180;
                break;
            case "seven o clock":
                degree = -150;
                break;
            case "eight o clock":
                degree = -120;
                break;
            case "nine o clock":
                degree = -90;
                break;
            case "ten o clock":
                degree = -60;
                break;
            case "eleven o clock":
                degree = -30;
                break;
            case "twelve o clock":
                degree = 0;
                break;
                */
        }
        return degree;
    }

    public void rotateCamera(final int degree)
    {
        if(degree != -1) {
            //Rotate camera by degree
            new AsyncTask<Integer, Void, Void>(){
                @Override
                protected Void doInBackground(Integer... params) {
                    try {
                        executeRemoteCommand("pi","raspberry","192.168.225.166", 22, degree);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(1);
            Log.i("TAKE PICTURE", "Rotate by this degree: " + degree);
            if (!tts.isSpeaking()) {
                tts.speak("Taking picture at "+degree+" degrees", TextToSpeech.QUEUE_FLUSH, null);
            }
        }

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            takePic(text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else {
            recognizer.startListening(searchName, 50000);
        }

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                //.setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setDictionary(new File(assetsDir, "commandsShort.dict"))
                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)
                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)
                
                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)
                
                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File commandsGrammar = new File(assetsDir, "commands.gram");
        recognizer.addGrammarSearch(COMMANDS_SEARCH, commandsGrammar);


    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);

    }

}
