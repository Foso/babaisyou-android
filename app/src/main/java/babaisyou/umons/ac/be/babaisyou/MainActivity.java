package babaisyou.umons.ac.be.babaisyou;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playButton = (Button) findViewById(R.id.playButton);
        Button playOnlineButton = (Button) findViewById(R.id.playOnlineButton);
        Button settingsButton = (Button) findViewById(R.id.settingsButton);

        playButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay();
            }
        });

        settingsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettings();
            }
        });

    }

    private void onPlay() {
        Intent intent = new Intent(this, LevelsListActivity.class);
        startActivity(intent);
    }

    private void onSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
