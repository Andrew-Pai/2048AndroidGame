package lab4_203_10.uwaterloo.ca.lab4_203_10;


/**
 * Created by Andrew Pai on 6/4/2017.
 */

public class FiniteStateMachine {
    //create four states with the type enum
    enum states{WAIT,RISE,FALL,DETERMINED};
    private states myState;
    //create signatures that records what is going to be the output
    enum signature{LEFT,RIGHT,UNDETERMINED};
    private signature mySig;
    //create prediction to record temporary change in states to avoid saving issue when stating myStates
    private signature prediction;
    //1st threshold: minimum slope of the response onset
    //2nd threshold: the maximum response amplitude of the first peak
    //3rd threshold: the maximum response amplitude after settling for 40 samples.
    //for both left and right
    private  float []threshold_right=new float[3];//={0.3f,4f,-1.8f};
    private  float []threshold_left=new float[3];//={-0.28f,-4.2f,1.75f};
    //set field to save previous value, max and min value of acceleration
    private float prevAccel, maxAccel=0, minAccel=0;
    //counter to keep track of when to reset the whole thing
    private int counter;
    //we set a final constant to be the value the counter equals to after resetting
    final private int defaultCounter =30;


    //constructor
    //set default value before start analyzing
    public FiniteStateMachine(float threshA[], float threshB[]){
        //set states to wait
        myState= states.WAIT;
        //dont show anything of the gesture yet
        mySig= signature.UNDETERMINED;
        //can't predict the movement yet
        prediction= signature.UNDETERMINED;
        //counter set to default constant
        counter=defaultCounter;
        //copies the given value to the local private field
        //whether it is for left right or up down threshold
        for(int x =0; x<threshA.length;x++){
            threshold_right[x]=threshA[x];
        }
        for(int x =0; x<threshB.length;x++){
            threshold_left[x]=threshB[x];
        }
    }
    //the function that put every value back to where constructor has put them
    public void reset(){
        myState= states.WAIT;
        mySig= signature.UNDETERMINED;
        prediction= signature.UNDETERMINED;
        counter=defaultCounter;
        //clear the max and min value
        maxAccel=0;
        minAccel=0;
    }
    //function that returns the output private field of signature
    public signature getMySig(){
        return mySig;
    }
    //start analyzing
    public void analysis(float accelInput){
        //when the analyzing has been done over 30 times reset all the value
        if(counter-- <=0){
            reset();
        }
        //each time make the slope equals the new input minus the last value
        //the last value is either the previous max value or the min value
        float accelSlope = accelInput-prevAccel;
        //when its larger than the max value it becomes the new max
        if(accelInput > maxAccel)
            maxAccel=accelInput;
        //when its smaller than the min value it becomes the new min
        if (accelInput< minAccel)
            minAccel=accelInput;

        switch (myState){
            case WAIT:
                //Waiting until accel reading begins rising or falling past a certain threshold
                if(accelSlope>=threshold_right[0]){//If slope is greater than threshold that means rising
                    myState = states.RISE;
                    //we predict that will be the rising for the right gesture
                    prediction= signature.RIGHT;
                    //if the slope is less the left threshold that means it is falling
                }else if (accelSlope<=threshold_left[0]){
                    myState= states.FALL;
                    //we predict that its going left
                    prediction= signature.LEFT;
                }
                break;
            case RISE:
                if(accelSlope <=0){//Once slope is no longer rising and is now falling, check max accel
                    if(prediction== signature.RIGHT)
                        if(maxAccel >= threshold_right[1]){//Check if max accel reading was greater than threshold
                            myState = states.FALL;
                        }else{
                            myState= states.DETERMINED;
                            //it doesn't fit the left gesture or the right gesture so it is unknown
                            mySig= signature.UNDETERMINED;
                        }
                    if(prediction== signature.LEFT)
                        //if after settling for 40 samples it is still larger than the max value
                        if(maxAccel>=threshold_left[2]){
                            //we can determine that the gesture is going left
                            myState = states.DETERMINED;
                            mySig= signature.LEFT;
                        }else{
                            //it doesn't fit the left gesture or the right gesture so it is unknown
                            myState= states.DETERMINED;
                            mySig= signature.UNDETERMINED;
                        }
                }
                break;
            case FALL:
                //Once slope is no longer falling and is now rising, check max accel after 40 samples
                if(accelSlope >=0){
                    if(prediction== signature.RIGHT)
                        //our minimum accerl is less than the value after setting for 40 samples
                        if(minAccel<=threshold_right[2]){
                            //we know that it is a right gesture
                            myState = states.DETERMINED;
                            //gives right as displaying message
                            mySig= signature.RIGHT;
                        }else{
                            //it doesn't fit the left gesture or the right gesture so it is unknown
                            myState= states.DETERMINED;
                            mySig= signature.UNDETERMINED;
                        }
                    if(prediction== signature.LEFT)
                        //if the minimum value is less than peak value it starts to rise
                        if (minAccel<=threshold_left[1]){
                            //state changes to rise
                            myState= states.RISE;
                        }else{
                            //it doesn't fit the left gesture or the right gesture so it is unknown
                            myState= states.DETERMINED;
                            mySig= signature.UNDETERMINED;
                        }
                }
                break;
            //once the output is clear whether it is left right or undetermined
            //clear the value and reset the value
            case DETERMINED:
                reset();
                break;
            default:
                reset();
                break;
        }
        //make the input for this round to be the previous input for the next round
        prevAccel=accelInput;
    }
}