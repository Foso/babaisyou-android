package babaisyou.umons.ac.be.babaisyou;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.support.v7.widget.GridLayout;
import android.widget.ImageView;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.HashMap;

import be.ac.umons.babaisyou.exceptions.WrongFileFormatException;
import be.ac.umons.babaisyou.game.BlockType;
import be.ac.umons.babaisyou.game.Level;

public class LevelActivity extends AppCompatActivity {

    private static final String TAG = "BBIY_DEBUG";

    Level level;
    LevelsListActivity.LevelPack levelPack;
    GridLayout grid;

    private HashMap<String, Bitmap> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        grid = (GridLayout) findViewById(R.id.level_grid);

        //Il faut envoyer le pack de niveau et le niveau actuel

        Intent intent = getIntent();
        String currlvl = intent.getStringExtra("level");


        levelPack = LevelsListActivity.getInstance().getLevelPack(); //There is a level pack because the player tapped a level
        levelPack.setFirstLevel(currlvl);

        try {
            level = Level.load(getAssets().open("levels/" + currlvl));
        } catch (WrongFileFormatException e) {
            //TODO
            e.printStackTrace();
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }

        grid.setRowCount(level.getHeight() + 2 );
        grid.setColumnCount(level.getWidth() + 2);

        //précharger toutes les images
        images = new HashMap<>();
        for(BlockType type : BlockType.values()) {
            Log.w(TAG, type.getId());
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getAssets().open("images/"+type.getId()+".png"));
            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }
            images.put(type.getId(), bitmap);
        }

        update();

    }



    private void update() {
        //Remove all images
        grid.removeAllViews();

        int width = level.getWidth();
        int height = level.getHeight();

        for (int i=-1; i<= width; i++) {
            for (int j=-1; j<= height; j++) {

                int blockSize = getBlockSize();

                FrameLayout frameLayout = new FrameLayout(this);

                if (i == -1 || i == height || j == -1 || j == width) {
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(images.get("border"));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(blockSize, blockSize);
                    imageView.setLayoutParams(layoutParams);

                    frameLayout.addView(imageView);
                } else {

                    String[] blocks = level.getToId(j,i);
                    for (String block : blocks) {
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(images.get(block));
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(blockSize, blockSize);
                        imageView.setLayoutParams(layoutParams);
                        frameLayout.addView(imageView);
                    }

                }

                grid.addView(frameLayout);

            }
        }

    }

    private int getBlockSize() {
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();
        int height = d.getHeight();

        return Math.min(width, height) / Math.max(level.getWidth() + 2, level.getHeight() + 2);
    }
}
