package lab4_203_10.uwaterloo.ca.lab4_203_10;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

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

public class GameLoopTask extends TimerTask {//GameLoopTask inerhits TimerTask as it needs to runs periodically
    private Activity myActivity;//type Activity to store the current activity
    private Context myContext;//type Context to store current status of the app
    private RelativeLayout myRL;//type RelativeLayout to store reference to layout of app
    public static final float BLOCKSIZE = Resources.getSystem().getDisplayMetrics().widthPixels*0.745f/3f;//Takes the current screen size and divides it between ~4 blocks
    //endGame boolean for when it is Game Over, win or lose
    //toCreate boolean that keeps track of when a block needs to be created
    //moved boolean that keeps track if any blocks have moved
    //simulation boolean that makes sure the blocks don't change when simulation=true
    boolean endGame=false, toCreate=false,moved=false, simulation =false;
    enum direction {UP,DOWN,LEFT,RIGHT,NO_MOVEMENT};//directon enum to store direction of the gameblock
    private direction currDirection;//stores the current direction of the game blcoks
    LinkedList<GameBlock> myGB =new LinkedList<GameBlock>();//Linked list of type GameBlock that will store references to each of the current GameBlocks

    //GameLoopTask constructor
    //Passes activity so it runs its task on the UI thread
    //Passes context to get current status of app and add new views
    //Passes RelativeLayout to be able to call addView
    public GameLoopTask(Activity myActivity, Context myContext, RelativeLayout myRL){
        this.myActivity = myActivity;
        this.myContext=myContext;
        this.myRL= myRL;
        createBlock();//Calls createBlock method to add the first block to the gameboard
    }
    @Override
    public void run() {//Run method that updates the gameblocks so they move
        myActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(!endGame || !reachedTarget()) {//Checks if game is over or if the blocks have reached their target location
                            for (GameBlock block : myGB) {//Loop through each gameblock in the linked list
                                block.move();//Call the move method in each gameblock to move the block closer toward it's target
                                //Check if any of the blocks have reached 2048
                                if(block.getValue()==2048){
                                    TextView output = new TextView(myContext.getApplicationContext());
                                    myRL.addView(output);
                                    output.setY(BLOCKSIZE * 3.5f);
                                    output.setTextColor(Color.GREEN);
                                    output.setText("CONGRATS!!!");
                                    output.setTextSize(50);
                                    output.bringToFront();
                                    Log.wtf("END OF GAME","CONGRATS YOU WON");
                                    endGame=true;
                                    return;
                                }
                            }


                            if(reachedTarget()) {//Once the blocks have reached the target
                                for (int x = myGB.size() - 1; x >= 0; x--) {//Loop through the linked list
                                    if (myGB.get(x).getToDelete()) {//If the block has been marked to delete
                                        myGB.get(x).delBlock();//call the delBlock method in each block to remove it from the layout
                                        myGB.remove(x);//Dereference the gameblock from the linked list for garbage collection
                                    }
                                }
                                if (toCreate) {//If the toCreate boolean is true
                                    createBlock();//Create a new block
                                    toCreate = false;//Set the toCreate boolean back to false
                                }
                            }
                        }
                    }
                }
        );
    }
    //CreateBlock method that locates an open spot on the board for a new lbock
    private void createBlock(){//Creates an instance of the GameBlock and adds it to the view
        Random rand = new Random();//Declares a Random object to generate a random number
        float x=0.0f,y=0.0f;//float for the x,y location of the new block
        boolean empty=false;//empty boolean if it's an empty spot

        while (!empty) {//Keep looping until a unique spot is found
            x = (float) rand.nextInt(4);//Choose one of 4 random blocks in x directin
            y = (float) rand.nextInt(4);//Choose one of 4 random blocks in y direction
            x *= BLOCKSIZE;//Change x to the position on screen
            y *= BLOCKSIZE;//Change y to position on screen
            if (myGB.isEmpty())//If there are currently no blocks exit the loop
                break;
            if (isOccupied(x, y))//Check if the block is occupied
                empty = false;
            else
                empty = true;
        }

        //Declare and instantiate a new block of type Gameblock with the target coordinate
        GameBlock block = new GameBlock(myContext,myRL, x,y);
        //Add the newly declared gameblock to the linkedlist
        myGB.add(block);

        //If there is a full board
        if(myGB.size()==16){
            Log.d("No moves","Checking if there are moves left");
            //Check if there are any possible moves left
            if(noMoves()) {
                //If there are no more moves, set endGame flag to true and display a Game Over message
                TextView output = new TextView(myContext.getApplicationContext());
                myRL.addView(output);
                output.setY(BLOCKSIZE * 3.5f);
                output.setTextColor(Color.GREEN);
                output.setText("GAME OVER");
                output.setTextSize(50);
                output.bringToFront();
                Log.wtf("END OF GAME", "LOSER");
                endGame = true;
            }
        }
    }

    //setDirection method that is called from the sensor listener/finite state machine to update the gameblocks
    public void setDirection(direction nextDirection){//Changes current direction of the blocks and also calls the gameblock to update their direction
        Log.d("new direction",nextDirection.toString());
        float xTarget=-1,yTarget=-1;
        int emptySlot []=new int[myGB.size()];//Keeps track of the number of empty slots for each gameblock
        int value[][]= new int[myGB.size()][3];//Keeps track of the other values in the direction of movement for each gameblock
        //blockCount keeps track of the number of blocks between the boundary and the current block position
        //slotCount keeps track of the number of board spaces between the boundary and current block position
        //counter keeps track of the current index being looked at in the linked list
        //blockCounter that will keep track of the value of blocks that are
        int blockCount=0, slotCount=0,counter =0, blockCounter=2;

        //If it is gameover don't do anything
        if(endGame)
           return;

        //Only set new direction for the blocks when the current blocks have reached their target
        if(reachedTarget()) {
            currDirection = nextDirection;//Set current direction to the next direction
            moved=false;//Set moved boolean back to false
            for (GameBlock block : myGB) {//Loop through each gameblock
                emptySlot[counter] = 0;//Set the # of empty slots back to 0
                switch (currDirection) {//Depending on the next direction of the blocks set the target game slots that need to be checked
                    //Algorithm that determines the # of block slots between the current block and boundary -> -1 means block is at the edge and 2 is at the other edge
                    //If direction is up then set the y target to the top boundary and x target as the current x position
                    case UP:
                        xTarget = block.getPositionX();
                        yTarget = 0.0f;
                        blockCounter = (int) (block.getPositionY() / BLOCKSIZE) - 1;
                        break;
                    //If direction is down then set the y target to the bottom boundary and x target as the current x position
                    case DOWN:
                        xTarget = block.getPositionX();
                        yTarget = BLOCKSIZE * 3.0f;
                        blockCounter = 2 - (int) (block.getPositionY() / BLOCKSIZE);
                        break;
                    //If direction is left then set the x target to the left boundary and y target as the current x position
                    case LEFT:
                        xTarget = 0.0f;
                        yTarget = block.getPositionY();
                        blockCounter = (int) (block.getPositionX() / BLOCKSIZE) - 1;
                        break;
                    //If direction is right then set the x target to the right boundary and y target as the current x position
                    case RIGHT:
                        xTarget = BLOCKSIZE * 3.0f;
                        yTarget = block.getPositionY();
                        blockCounter = 2 - (int) (block.getPositionX() / BLOCKSIZE);
                        break;
                    default:
                        break;
                }

                //Keep looping from boundary until the blocks current position
                while (round(xTarget) != round(block.getPositionX()) || round(yTarget) != round(block.getPositionY())) {
                    //Get the value of the block at blockCounter distance away
                    value[counter][blockCounter]=blockValue(xTarget,yTarget);
                    if (isOccupied(xTarget, yTarget)) {//If the current position being observed has a block
                        blockCount++;//Increment the blockCount counter
                    }
                    //Increment the number of slots between the boundary and current block
                    slotCount++;
                    //Based off direction look at the block spaces between current block and target
                    switch (currDirection) {
                        case UP:
                            yTarget += BLOCKSIZE;//Move a blockspace closer to the block
                            if (yTarget >= block.getPositionY())//If the target position overshoots, sets it to the current position of the block
                                yTarget = block.getPositionY();
                            break;
                        case DOWN:
                            yTarget -= BLOCKSIZE;//Move a blockspace closer to the block
                            if (yTarget <= block.getPositionY())//If the target position overshoots, sets it to the current position of the block
                                yTarget = block.getPositionY();
                            break;
                        case LEFT:
                            xTarget += BLOCKSIZE;//Move a blockspace closer to the block
                            if (xTarget >= block.getPositionX())//If the target position overshoots, sets it to the current position of the block
                                xTarget = block.getPositionX();
                            break;
                        case RIGHT:
                            xTarget -= BLOCKSIZE;//Move a blockspace closer to the block
                            if (xTarget <= block.getPositionX())//If the target position overshoots, sets it to the current position of the block
                                xTarget = block.getPositionX();
                            break;
                        default:
                            xTarget = block.getPositionX();
                            yTarget = block.getPositionY();
                            break;
                    }
                    blockCounter--;
                }//End of While loop
                emptySlot[counter] = slotCount - blockCount;//Number of game slots subract number of blocks to get the number of empty blocks
                counter++;
                slotCount = 0;
                blockCount = 0;


            }//End of For each loop of gameblock
            counter = 0;
            //Loop through each gameblock to determine it's final target location
            for (GameBlock block : myGB) {
                //Call block analysis method to check if block will merge
                emptySlot[counter] = blockAnalysis(block, counter, emptySlot[counter], value[counter]);
                    xTarget = block.getPositionX();
                    yTarget = block.getPositionY();
                    //Based off the current direction of movement
                    //Move the block to fill in the empty slots
                    switch (currDirection) {
                        case UP:
                            yTarget -= emptySlot[counter] * BLOCKSIZE;
                            break;
                        case DOWN:
                            yTarget += emptySlot[counter] * BLOCKSIZE;
                            break;
                        case LEFT:
                            xTarget -= emptySlot[counter] * BLOCKSIZE;
                            break;
                        case RIGHT:
                            xTarget += emptySlot[counter] * BLOCKSIZE;
                            break;
                        default:
                            break;
                    }
                    //Increment counter for the next index
                    counter++;
                //Call the setCurrDirection in the gameblock to set the calculated target position
                    block.setCurrDirection(xTarget, yTarget, currDirection);
                    if(block.getMoved())//If the block has moved from original position, set moved to true
                        moved=true;
            }
            if(moved && !simulation)//If a block has moved and it's not a simulation, set the toCreate flag to true
                toCreate=true;

        }//End of If(targetreached)
    }

    //isOccupied method that takes in x and y coordinate parameters to check if there is a block at the location
    public boolean isOccupied (float x, float y){
        for(GameBlock block:myGB) {//Loop through all other game blocks to make sure no block is currently in that spot
            if(round(x) == round(block.getPositionX())&& round(y) == round(block.getPositionY())) {//compare x and y coordinates to see if they match
                return true;//return true if a block is found at the location
            }
        }
        return false;//otherwise return false
    }

    //Get the value of the block and the x y coordinate
    public int blockValue(float x, float y){
        for(GameBlock block:myGB) {//Loop through all the gameblocks
            if(round(x) == round(block.getPositionX())&& round(y) == round(block.getPositionY())) {//Once the block at coordinates are found
                return block.getValue();//return the value of the block
            }
        }
        return 0;//otherwise return 0 for empty block
    }

    //BlockAnalysis method incoporates the merge algorithm to determine the updated number of empty slots the block will have
    private int blockAnalysis(GameBlock block, int counter, int emptySlot,int value[]){//Calculate where to move the block
        int updatedSlots=emptySlot;

        //Compares current block value with next block value, while giving priority to furthest block
        if(!block.getHasMerged()){//If current block has not merged yet
            //Now check if this block will merge

            //current block has same value as next block, and next block won't merge as it is different value or the 2 blocks in front merge instead
            if(block.getValue()==value[0]&&(value[0]!=value[1]||value[1]==value[2])){
                updatedSlots++;//Since the block will merge, increment the number of empty slots
                blockMerge(block,1);//Merge the block
                if(value[1]==value[2]&&value[1]!=0)
                    updatedSlots++;//Since the block will merge, increment the number of empty slots
                return updatedSlots;//return the new number of empty slots
            }else if(block.getValue()==value[1]&&value[0]==0&&value[1]!=value[2]){//Merges with a block 2 gamespaces away
                updatedSlots++;//Since the block will merge, increment the number of empty slots
                blockMerge(block,2);//Merge the block
                return updatedSlots;//return the new number of empty slots
            }else if (block.getValue()==value[2]&&value[0]==0&&value[1]==0){//Merges with block 3 gamespaces away
                updatedSlots++;//Since the block will merge, increment the number of empty slots
                blockMerge(block,3);//Merge the block
                return updatedSlots;//return the new number of empty slots
            }

        }
        //otherwise this block doesn't merge :(
        //Check if any of the blocks in front merge
        if((value[2]==value[1]&&value[1]!=0)||(value[1]==value[0]&&value[1]!=0))
            updatedSlots++;

        return updatedSlots;//return the new number of empty slots
    }

    //blockMerge method for merging two blocks
    private void blockMerge(GameBlock block, int distance){//Merges given block with block at distance away
        float x=0,y=0;
        if(!simulation)//If it is not a simulation set current blocks delete flag to true
            block.setToDelete();
        //Based off direction of movement and distance away the block is get the x,y coordinates of the block to be merged
        switch (currDirection) {
            case UP:
                x = block.getPositionX();
                y = block.getPositionY()-distance * BLOCKSIZE;
                break;
            case DOWN:
                x=block.getPositionX();
                y = block.getPositionY()+distance * BLOCKSIZE;
                break;
            case LEFT:
                y = block.getPositionY();
                x = block.getPositionX()-distance * BLOCKSIZE;
                break;
            case RIGHT:
                y = block.getPositionY();
                x = block.getPositionX()+distance * BLOCKSIZE;
                break;
            default:
                break;
        }
        //Set the target blocks toMerge flag to true
       for(GameBlock target : myGB){
           if(round(x) == round(target.getPositionX())&& round(y) == round(target.getPositionY())) {//Iterate through gameblock list to find block at xy coord
               if(!simulation)//If it isn't a simulation
                target.toMerge();
               return;
           }
       }
    }

    /*
    This app was fully programmed and documented by Andrew Pai
    akpai@edu.uwaterloo.ca
    If this is found on someone elses github, probably Policy 71
     */

    //reachedTarget method that checks  if all the blocks have reached their target position
    private boolean reachedTarget(){
        for(GameBlock block:myGB)
            if(block.getPositionX() != block.getTargetX() || block.getPositionY()!=block.getTargetY())
                return false;
        return true;
    }
    //noMoves method that checks if there are any possible moves remaining
    private boolean noMoves(){
        simulation =true;//set simulation boolean to true so no blocks change
        setDirection(UP);//send up direction to test if blocks can move up
        if(!moved){//if block can't move up
            Log.d("No moves","Can't move up");
            moved=false;
            setDirection(DOWN);//send up direction to test if blocks can move down
            if(!moved) {//if blocks can't move down
                Log.d("No moves","Can't move down");
                moved = false;
                setDirection(LEFT);//send up direction to test if blocks can move left
                if(!moved) {//if blocks can't move left
                    Log.d("No moves","Can't move left");
                    moved = false;
                    setDirection(RIGHT);//send up direction to test if blocks can move right
                    if(!moved) {//if blocks can't move right
                        Log.d("No moves","Can't move right");
                        return true;//if blocks cant move in any direction, return no more moves
                    }
                }
            }
        }
        for(GameBlock block: myGB){
            block.setCurrDirection(block.getPositionX(),block.getPositionY(),RIGHT);//reset the targest positions of each block
        }
        simulation=false;
        moved=false;
        return false;//return false if there are possible moves
    }

    private float round(float number){
        return (float)Math.round(number*10)/10;
    }//method that rounds the inputted number to 1 decimal place
}
