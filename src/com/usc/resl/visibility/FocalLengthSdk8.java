package com.usc.resl.visibility;

import android.hardware.Camera;

public class FocalLengthSdk8 extends FocalLengthAccessor {

	@Override
	public float getFocalLength() {
		// get the focal length since it is Android Version Froyo or greater
		
		Camera camera=null;		
		try{
			camera=Camera.open();
		}catch(RuntimeException e){
			//just continue trying
			e.printStackTrace();
		}		
		Camera.Parameters p=camera.getParameters();
		float focalLength=p.getFocalLength();
		camera.release();
		focalLength=(float) 4.31;
		return focalLength;
	}

}
