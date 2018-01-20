package lab4_203_10.uwaterloo.ca.lab4_203_10;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by AndrewP on 7/1/2017.
 */

//Abstract template for creating gameblocks
public abstract class GameBlockTemplate extends ImageView {
    //Constructor method to calls the ImageView constructor to setup the Image
    public GameBlockTemplate(Context context) {
        super(context);
    }
    //SetCurrDirection method that sets the current direction and destination of the gameblock
    public abstract void setCurrDirection(float xDestination, float yDestination, GameLoopTask.direction nextDirection);
    //Move method that moves the block toward it's target
    public abstract void move();
}
