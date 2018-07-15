package babaisyou.umons.ac.be.babaisyou;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.support.v7.widget.GridLayout;
import android.widget.ImageView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import be.ac.umons.babaisyou.exceptions.GamedCompletedException;
import be.ac.umons.babaisyou.exceptions.WrongFileFormatException;
import be.ac.umons.babaisyou.game.BlockType;
import be.ac.umons.babaisyou.game.Direction;
import be.ac.umons.babaisyou.game.Level;

public class LevelActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "BBIY_DEBUG";

    private GestureDetectorCompat gestureDetector;

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

        setTitle(currlvl);

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


        gestureDetector = new GestureDetectorCompat(this, this);
        gestureDetector.setOnDoubleTapListener(this);

        update();

    }



    private int getBlockSize() {
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();
        int height = d.getHeight();

        return Math.min(width, height) / Math.max(level.getWidth() + 2, level.getHeight() + 2);
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


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // Grab two events located on the plane at e1=(x1, y1) and e2=(x2, y2)
        // Let e1 be the initial event
        // e2 can be located at 4 different positions, consider the following diagram
        // (Assume that lines are separated by 90 degrees.)
        //
        //
        //         \ A  /
        //          \  /
        //       D   e1   B
        //          /  \
        //         / C  \
        //
        // So if (x2,y2) falls in region:
        //  A => it's an UP swipe
        //  B => it's a RIGHT swipe
        //  C => it's a DOWN swipe
        //  D => it's a LEFT swipe
        //

        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        Direction direction = getDirection(x1,y1,x2,y2);
        return onSwipe(direction);
    }


    /** Override this method. The Direction enum will tell you how the user swiped. */
    public boolean onSwipe(Direction direction){
        Log.w(TAG, direction.toString());
        switch (direction) {
            case up: level.move(be.ac.umons.babaisyou.game.Direction.UP); break;
            case down: level.move(be.ac.umons.babaisyou.game.Direction.DOWN); break;
            case right: level.move(be.ac.umons.babaisyou.game.Direction.RIGHT); break;
            case left: level.move(be.ac.umons.babaisyou.game.Direction.LEFT); break;
        }
        // if gagne changer titre et map
        if (level.hasWon()) {
            try {
                levelPack.nextLevel();
            } catch (GamedCompletedException e1) {
                //Revenir au menu principal
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            try{
                level = levelPack.getCurrentLevel();
            } catch (WrongFileFormatException e1) {
                // Si le niveau est corrompu, afficher scene d'erreur
                Log.w(TAG, "Could not load Level");
                level = null;
            }
        }

        update();
        return true;
    }

    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    public Direction getDirection(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get(angle);
    }

    /**
     *
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    public double getAngle(float x1, float y1, float x2, float y2) {

        double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }




    public enum Direction{
        up,
        down,
        left,
        right;

        /**
         * Returns a direction given an angle.
         * Directions are defined as follows:
         *
         * Up: [45, 135]
         * Right: [0,45] and [315, 360]
         * Down: [225, 315]
         * Left: [135, 225]
         *
         * @param angle an angle from 0 to 360 - e
         * @return the direction of an angle
         */
        public static Direction get(double angle){
            if(inRange(angle, 45, 135)){
                return Direction.up;
            }
            else if(inRange(angle, 0,45) || inRange(angle, 315, 360)){
                return Direction.right;
            }
            else if(inRange(angle, 225, 315)){
                return Direction.down;
            }
            else{
                return Direction.left;
            }

        }

        /**
         * @param angle an angle
         * @param init the initial bound
         * @param end the final bound
         * @return returns true if the given angle is in the interval [init, end).
         */
        private static boolean inRange(double angle, float init, float end){
            return (angle >= init) && (angle < end);
        }
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


}
