package com.usc.resl.visibility;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class splash extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		//display the flash screen for 3 seconds
		Timer timer=new Timer();
		TimerTask tt=new RunTimerTask();
		timer.schedule(tt, 6000);
	}
	/*--------------------------------------------------
	  * RunTimerTask Class - Run the task
	  *-------------------------------------------------*/  
	  private class RunTimerTask extends TimerTask
	  {
	    public final void run()
	    {
	      Intent i=new Intent();
	      i.setClass(splash.this, Pcapture.class);
	      startActivity(i);
	    }
	  }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
	
}
