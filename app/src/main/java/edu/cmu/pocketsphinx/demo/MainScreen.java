package edu.cmu.pocketsphinx.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by hayunchong on 1/21/16.
 * Hello
 */
public class MainScreen extends Activity {

    ImageButton cameraButton, helpButton;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainscreen);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        // Register the onClick listener with the implementation above
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                startActivity(new Intent(MainScreen.this, PocketSphinxActivity.class));
            }
        });

        helpButton=(ImageButton) findViewById(R.id.helpButton);
        // Register the onClick listener with the implementation above
        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                startActivity(new Intent(MainScreen.this, helpScreen.class));

            }
        });
    }

}
