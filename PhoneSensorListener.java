package lab4_203_10.uwaterloo.ca.lab4_203_10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.DOWN;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.LEFT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.NO_MOVEMENT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.RIGHT;
import static lab4_203_10.uwaterloo.ca.lab4_203_10.GameLoopTask.direction.UP;


/**
 * Created by AndrewP on 6/4/2017.
 */


class PhoneSensorListener implements SensorEventListener {
    //1st threshold: minimum slope of the response onset
    //2nd threshold: the maximum response amplitude of the first peak
    //3rd threshold: the maximum response amplitude after settling for 40 samples.
    //for both left right up and down
    //left and right have opposed direction
    //up and down have opposed direction
    private final float threshold_right[] = {0.22f,3.55f,-1.4f};
    private final float threshold_left[]={-0.21f,-3.65f,1.4f};
    private final float threshold_up[] = {0.165f,2.85f,-1.55f};
    private final float threshold_down[]={-0.185f,-3.6f,0.75f};
    FiniteStateMachine fsm_x= new FiniteStateMachine(threshold_right,threshold_left);//Declare and instantiate FiniteStateMachine for determining left or right on x-axis
    FiniteStateMachine fsm_y= new FiniteStateMachine(threshold_up,threshold_down);//Declare and instantiate FiniteStateMachine for determining up or down on y-axis
    TextView state;
    float filterRead[]=new float[3];
    final int constant=15;
    int counter=0;
    GameLoopTask game;


    //constructor create the sensor object with the given value
    public PhoneSensorListener(TextView state,GameLoopTask game ){
        this.state=state;
        this.game=game;


    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        //in the case it could only be sensor for acceleration
        if(event.sensor.getType()== Sensor.TYPE_LINEAR_ACCELERATION){
            //Apply low pass filter algorithm to accel reading in order to smooth out the curves
            for(int x=0;x<filterRead.length;x++){
                filterRead[x]+=(event.values[x]-filterRead[x])/constant;
            }
            //Graph the new points for both x and y axis
            fsm_x.analysis(filterRead[0]);
            fsm_y.analysis(filterRead[1]);
            //display left right up or down according to the result fsm gives
            //Also call the set direction method in gamelooptask to update the new direction for the blocks to move
            if(fsm_x.getMySig()== FiniteStateMachine.signature.LEFT && fsm_y.getMySig()== FiniteStateMachine.signature.UNDETERMINED) {
                state.setText("LEFT");
                game.setDirection(LEFT);
                //set counter back to 0 to count again
                counter=0;
            }else if(fsm_x.getMySig()== FiniteStateMachine.signature.RIGHT && fsm_y.getMySig()== FiniteStateMachine.signature.UNDETERMINED) {
                state.setText("RIGHT");
                game.setDirection(RIGHT);
                counter = 0;
            }else if(fsm_x.getMySig()== FiniteStateMachine.signature.UNDETERMINED && fsm_y.getMySig()== FiniteStateMachine.signature.RIGHT) {
                state.setText("UP");
                game.setDirection(UP);
                counter=0;
            }else if(fsm_x.getMySig()== FiniteStateMachine.signature.UNDETERMINED && fsm_y.getMySig()== FiniteStateMachine.signature.LEFT) {
                state.setText("DOWN");
                game.setDirection(DOWN);
                counter=0;
            }else{
                if(counter++>=40) {
                    state.setText("UNDETERMINED");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Don't care about the accuracy currently
    }
    public void reset(){
        state.setText("UNDETERMINED");
    }
}