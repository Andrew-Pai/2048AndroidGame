package lab4_203_10.uwaterloo.ca.lab4_203_10;

import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;

public class Lab4_203_10 extends AppCompatActivity {
    SensorManager snsrMgr;
    SensorEventListener snsrListen;
    Sensor accelSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lab4_203_10);//Set current content view to activity_Lab4_203_10 xml

        //Set reference to relativelayout in xml
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainLayout);
        Log.d("running","run");
        //Set the layout parameters of the width and height to screen widht
        rl.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels;
        rl.getLayoutParams().height= rl.getLayoutParams().width;
        rl.setBackgroundResource(R.drawable.gameboard);//Add gameboard to background screen

        TextView direction = new TextView(getApplicationContext());//Declare and instantiate textview that will display current direction of gesture
        rl.addView(direction);//Add textview to screen
        direction.setTextSize(20);//Increase text size
        direction.setTextColor(Color.RED);//Make colour red
        Log.d("running","run");
        Timer gameTimer = new Timer();//Create a timer that will be used to periodically run the gamelooptask
        GameLoopTask game = new GameLoopTask(this, getApplicationContext(),rl);//Declare and instantiate gamelooptask with the activity, ccontext, and relative layout needed
        Log.d("running","run");
        gameTimer.schedule(game,0,10);//Schedule the gamelooptask to be called every 10ms or 100 fps

        snsrMgr = (SensorManager) getSystemService(SENSOR_SERVICE);//Instantiate a sensor manager that will handle sensors
        accelSensor = snsrMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);//Set accelSensor variable to be of type linear acceleration
        PhoneSensorListener phone = new PhoneSensorListener(direction,game);//
        snsrListen = phone;//Set PhoneSensorLsitener as the class to be called when sensor is changed

        snsrMgr.registerListener(snsrListen,accelSensor,SensorManager.SENSOR_DELAY_GAME);//Register the sensorlistener to lsiten for specified sensor












    }
}
