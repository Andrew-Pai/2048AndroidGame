package lab4_203_10.uwaterloo.ca.lab4_203_10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;


import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.DOWN;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.LEFT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.NO_MOVEMENT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.RIGHT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.UP;


/**
 * Created by AndrewP on 6/24/2017.
 This app was fully programmed and documented by Andrew Pai
 akpai@edu.uwaterloo.ca
 If this is found on someone elses github, probably Policy 71
 */

public class GameBlock  extends  GameBlockTemplate{//inherits ImageView as the gameblock is an image
    private final float scale= 0.8f; //set scale for block to fit with gameboard
    private boolean hasMerged=false;//hasMerged flag if this block merged with another
    private boolean toDelete=false;//toDelete flag for if this block is merged with another
    private int value;//stores the current value of the block
    private float positionX, positionY,targetX, targetY; //float to hold the current x and y position of the block, and the target position of the block
    private float velocityX, velocityY;//velocity the block initially moves at
    private GameLoopTask.direction currDirection;
    private final float BLOCKSIZE = Resources.getSystem().getDisplayMetrics().widthPixels*0.745f/3 ;
    private final float LEFT_BOUND=0,RIGHT_BOUND= BLOCKSIZE *3; //Set the left and right bound of the block based off of screen size
    private final float UP_BOUND=0,DOWN_BOUND=RIGHT_BOUND;//Set up and down bound of blocks based off left and right bound
    private final float accel=5;//the acceleration of the block
    private boolean moved = false;//moved flag for if this block moved after setting new direction


    RelativeLayout myRL;
    TextView blockNum;
    Random rand = new Random();

    public GameBlock(Context context, RelativeLayout rl, float coordX, float coordY) {//GameBlock constructor
        super(context);
        myRL = rl;
        positionX=coordX;//Set current x position of the block
        positionY=coordY;//Set current y position of the block

        myRL.addView(this);
        this.setImageResource(R.drawable.gameblock);//add block to screen

        blockNum = new TextView(context.getApplicationContext());
        myRL.addView(blockNum);//Add the block number to layout
        //Generate random value, 1 or 2, to assign the block
        value = 2*(rand.nextInt(2)+1);
        //Create textview with value of the block over the block
        Log.d("Created block","With value:"+value);
        blockNum.setText(String.valueOf(value));
        blockNum.setTextColor(Color.RED);
        blockNum.setTextSize(40);
        blockNum.bringToFront();
        blockNum.setX(positionX);
        blockNum.setY(positionY);

        this.setPivotX(0);//since block is scaled set the new pivot to top left of screen
        this.setPivotY(0);//since block is scaled set the new pivot to top left of screen
        this.setScaleX(scale);//Rescale block
        this.setScaleY(scale);//Rescale block

        this.setX(positionX);//Place block at the designated x position
        this.setY(positionY);//Place block at the designated y position
        blockNum.setWidth((int)BLOCKSIZE);
        blockNum.setHeight((int)BLOCKSIZE);
        blockNum.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        
        targetX=coordX;//Set target x position to current pos
        targetY=coordY;//Set target y position to current pos
        resetVelocity();
    }
    //method to reset velocity back to 10
    private void resetVelocity(){
        velocityX=10;
        velocityY=10;
    }


    //Method to change current direction of block
    @Override
    public void setCurrDirection(float xDestination, float yDestination, GameLoopTask.direction nextDirection){

        //If new destination is the same as the current position set moved flag to false
            if(xDestination==positionX&&yDestination==positionY)//
                moved=false;
            else
                moved= true;

        //set the new target destination
            targetX=xDestination;
            targetY=yDestination;
            currDirection=nextDirection;


    }


    /**
     * Created by AndrewP on 6/24/2017.
     This app was fully programmed and documented by Andrew Pai
     akpai@edu.uwaterloo.ca
     If this is found on someone elses github, probably Policy 71
     */
    //Move method that moves the block toward the target position
    @Override
    public void move() {
        //Checks if the block is below or above the target position and adds velocity in the according direction
        //Also accelerate the block by incrementing the velocity
        if ((positionY) < (targetY)) {
            if(currDirection==UP){
                positionY=targetY;
            }else {
                positionY += velocityY;
                velocityY += accel;
            }
        } else if ((positionY) > (targetY)) {
            if(currDirection == DOWN){
                positionY=targetY;
            }else {
                positionY -= velocityY;
                velocityY += accel;
            }
        }
        //Checks if the block is to the left or right the target position and adds velocity in the according direction
        //Also accelerate the block by incrementing the velocity
        if ((positionX) < (targetX)) {
            if(currDirection==LEFT){
                positionX=targetX;
            }else {
                positionX += velocityX;
                velocityX += accel;
            }
        } else if ((positionX) > (targetX)) {
            if(currDirection==RIGHT){
                positionX=targetX;
            }else {
                positionX -= velocityX;
                velocityX += accel;
            }
        }


        //Check if the block went out of bounds and keeps it within the bounds
        if (positionY > DOWN_BOUND) {
            positionY = DOWN_BOUND;
        } else if (positionY < UP_BOUND) {
        }

        if (positionX > RIGHT_BOUND) {
            positionX = RIGHT_BOUND;
        } else if (positionX < LEFT_BOUND) {
            positionX = LEFT_BOUND;
        }

        //If the block has reached it's target position
        if (positionY == targetY && positionX == targetX) {
            if(hasMerged)//if block hasMerged flag was set to true double the value to represent merging
                doubleValue();
            hasMerged=false;//set the hasMerged flag back to false
            resetVelocity();//reset the velocity of the blocks
            if(toDelete)//If the block is to be deleted
                delBlock();//Delete the block
        }

        //Move block to it's next designated position
        this.setX(positionX);
        this.setY(positionY);

        //Moves the textview to next target position
        blockNum.setX(positionX);
        blockNum.setY(positionY);

    }
    //return the x position of block
    public float getPositionX(){
        return positionX;
    }
    //return the target x position of block
    public float getTargetX(){return targetX;}
    //return target y position of block
    public float getTargetY(){return targetY;}
    //return the y position of block
    public float getPositionY(){
        return positionY;
    }
    //doubles the current value of the block
    public void doubleValue(){this.value*=2;blockNum.setText(String.valueOf(this.value));}
    //returns the value of the block
    public int getValue(){return value;}
    //sets the blocks hasMerged flag to true
    public void toMerge(){
        hasMerged=true;
    }
    //returns the blocks hasMerged flag
    public boolean getHasMerged(){return hasMerged;}
    //Round number to 1 decimal place
    private float round( float num){
        return (float) Math.round(num*10)/10;
    }
    //Sets the blocks toDelete flag to true
    public void setToDelete(){
        toDelete=true;
    }
    //Returns the blocks toDelete flag
    public boolean getToDelete(){
        return toDelete;
    }
    //Returns whether the block has moved
    public boolean getMoved(){
        return moved;
    }
    //delBlock method that removes the block from the layout
    public void delBlock(){
        blockNum.setText("Deleted");
        myRL.removeView(blockNum);
        myRL.removeView(this);
    }
}

