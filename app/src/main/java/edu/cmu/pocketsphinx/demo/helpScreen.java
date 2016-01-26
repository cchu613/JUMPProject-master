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
 */
public class helpScreen extends Activity {

    ImageButton returnButton;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.help);
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);

        returnButton = (ImageButton) findViewById(R.id.returnButton);
        // Register the onClick listener with the implementation above
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                startActivity(new Intent(helpScreen.this, MainScreen.class));
            }
        });

    }

}
