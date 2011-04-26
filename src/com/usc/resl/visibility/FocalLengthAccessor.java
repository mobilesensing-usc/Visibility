package com.usc.resl.visibility;

import android.content.Intent;
import android.os.Build;


public abstract class FocalLengthAccessor {
	 private static FocalLengthAccessor sInstance;
	 	//load the correct version of the code based on the Android Version
	    public static FocalLengthAccessor getInstance() {
	        if (sInstance == null) {
	            String className;

	            @SuppressWarnings("deprecation")
	            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);      
	            if (sdkVersion < Build.VERSION_CODES.FROYO) {
	                className = "com.usc.resl.visibility.FocalLengthSdkGen";
	            } else {
	                className = "com.usc.resl.visibility.FocalLengthSdk8";
	            }

	            /*
	             * Find the required class by name and instantiate it.
	             */
	            try {
	                Class<? extends FocalLengthAccessor> clazz =
	                        Class.forName(className).asSubclass(FocalLengthAccessor.class);
	                sInstance = clazz.newInstance();
	            } catch (Exception e) {
	                throw new IllegalStateException(e);
	            }
	        }

	        return sInstance;
	    }

	   
	    public abstract float getFocalLength();

	  
}
