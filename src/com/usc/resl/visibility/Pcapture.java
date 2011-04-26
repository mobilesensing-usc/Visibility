package com.usc.resl.visibility;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.GeomagneticField;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Pcapture extends Activity implements SurfaceHolder.Callback {

	//Public declarations for class
	double lat,lon,altitude;
	private Camera camera;
	private boolean isPreviewRunning = false, spirtCorrect=true;
	private SurfaceView surfaceView;
	private TextView camAdjust,moreImage;	
	private int arrowCheck=0;
	private SurfaceHolder surfaceHolder;
	static private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
	private String tem,hum,vis; //internet tagging data
	private String imageFilePath;
	static public String url;
	private LocationManager lm;
	private float degree_rotate = 0;
    private LocationListener locationListener;
    private SensorManager mSensorManager;
    private int mask,radioData;
    private float Xval,Yval,Zval,camAzim, camElev,camAzimCurrent,camElevCurrent;
    private String X,Y,Z,timeStamp,visibilityRating;
    double elevation;
    double azimuth;
    float myX, myY;
    private boolean mInitialized;
    private Button yesB;
	private Button noB;
	FrameLayout main;
	View vw;
	RelativeLayout drawer;
	private ToggleButton internetB;
	private ToggleButton privacyB;
	private ProgressDialog progressd;
	private String fname;
    public static final String PREFS_NAME = "VisibilityPrefsFile";
    public ImageButton cameraButton;
    View spiritView;
    float focalLength=0;
    final RefreshHandler alertHandler=new RefreshHandler();//handler for showing alert messages
    private String coordinates=new String();
    Thread t;
    private String predictedValue=new String();
    private ImageView arrowRight;
    private ImageView arrowLeft;
    private int screenWidth=0;
	private int screenHeight=0;
	private boolean cameraButtonPressed=false;
	private AlertDialog alertDialog;
    //onCreate method creates surface initializes view and required UI parameters
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        Log.e("Custom Supported",requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)+"");
        setContentView(R.layout.pcapture);
        
        //set custom title bar
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);
        
        //get the screen parameters
        WindowManager wm=getWindowManager();
		Display disp=wm.getDefaultDisplay();
		screenWidth=disp.getWidth();
		screenHeight=disp.getHeight();
		
		//set the title bar text and help button functionality for this view
		TextView titleText=(TextView)findViewById(R.id.titleText);
		titleText.setText("Point the camera towards the sky, parallel to the ground and click a picture");
        Button titleButton=(Button)findViewById(R.id.titleButton);
        titleButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertbox("", "Capture View allows you take a picture of sky. Tap camera icon on screen or camera button on your phone to take a picture.\n\nFollow the directions provided by the indicator at the bottom to take the correct picture. Make sure that indicator remains green.");
				
			}
		});
		
        //get the references for the views
        main = (FrameLayout) findViewById(R.id.main_view);        
        /*yesB = (Button) findViewById(R.id.yesButton);
        noB = (Button) findViewById(R.id.noButton);
        moreImage = (TextView)findViewById(R.id.moreImage);
        */
        cameraButton=(ImageButton)findViewById(R.id.CameraButton);
        
        arrowRight=(ImageView)findViewById(R.id.arrowRight);
        arrowLeft=(ImageView)findViewById(R.id.arrowLeft);
        spiritView = new spiritBall(this, screenWidth/2, screenHeight-70, 7);
        main.addView(spiritView);
   	
        cameraButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				// check if the azimuth and elevation is proper
				if(spirtCorrect==true && cameraButtonPressed==false){
					cameraButtonPressed=true;
					TakePicture();
				}else{
					//alertbox("Error", "Please make sure that Spirit ball is green");
				}				
			}
		});
        //set up the settings view
        
        //set up the settings button
        Button settingsButton=(Button)findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				settingsbox();
				
			}
		});
        //setting up slider
        /*drawer=(RelativeLayout)findViewById(R.id.content);
        drawer.setBackgroundColor(Color.BLACK);
        SlidingDrawer slidingdrawer=(SlidingDrawer)findViewById(R.id.drawer);
        slidingdrawer.bringToFront();
        slidingdrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {			
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				cameraButton.setOnClickListener(null);
				// make arrows invisible
				
			}
		});
        slidingdrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {			
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				// make arrows invisible
				
				
				 cameraButton.setOnClickListener(new View.OnClickListener() {						
						public void onClick(View v) {
							// TODO Auto-generated method stub							
							if(spirtCorrect==true){
								TakePicture();
							}else{
								//alertbox("Error", "Please make sure that Spirit ball is green");
							}								
						}
					});
			}
		});
        //ImageView imgview=(ImageView)findViewById(R.id.handle);
        //imgview.setBackgroundColor(Color.DKGRAY);
       */
        progressd = null;
        
        setScreen(); 
    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
    	cameraButtonPressed=false;
		super.onStart();
	}

	public void setScreen()
    {
	   //Code for GPS location capture. Turn ON GPS and capture location	  
	  lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	  locationListener = new MyLocationListener();
	  lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0,locationListener);
		
	  android.util.Log.e("test", "I AM HERE setScreen");
	  /*yesB.setVisibility(View.INVISIBLE);
      noB.setVisibility(View.INVISIBLE);
      moreImage.setVisibility(View.INVISIBLE);
	  */surfaceView = (SurfaceView)findViewById(R.id.preview_view);
     
      surfaceHolder = surfaceView.getHolder();
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     // camAdjust = (TextView)findViewById(R.id.camAdjust);
     
      mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
      mask |= SensorManager.SENSOR_ORIENTATION_RAW;
      mSensorManager.registerListener(mListener, mask, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
 
      lat = 0;
      lon =0;    
  }
   
    @Override  
	protected void onRestoreInstanceState(Bundle savedInstanceState)  
	{
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	Camera.PictureCallback mPictureCallbackRaw = new Camera.PictureCallback() {
		 public void onPictureTaken(byte[] data, Camera c) {
			 Log.e(getClass().getSimpleName(), "PICTURE CALLBACK RAW: " + data);  
		}
	};
	
	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {  
		 public void onShutter() {
			Log.e(getClass().getSimpleName(), "SHUTTER CALLBACK");
		 }
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		 if (keyCode == KeyEvent.KEYCODE_BACK) {  
			return super.onKeyDown(keyCode, event);  
		 }  		 
		 else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {  
			 if(spirtCorrect==true && cameraButtonPressed==false){
				 	cameraButtonPressed=true;
					TakePicture();
				}else{
					;
				}
			 return true;
		 }else if(keyCode == KeyEvent.KEYCODE_CAMERA){
			 if(spirtCorrect==true && cameraButtonPressed==false){
				 	cameraButtonPressed=true;
					TakePicture();
				}else{
					;
			 }
			 return true;
		 }
	   return false;  
	 }
	
	protected void TakePicture(){
		ImageCaptureCallback iccb = null;
		
		try {  
			 iccb = new ImageCaptureCallback(getContentResolver());  
		 } catch(Exception ex ){  
			 ex.printStackTrace();  
			 Log.e(getClass().getSimpleName(), ex.getMessage(), ex);  
		 }
		timeStamp = timeStampFormat.format(new Date());
  				 
		camera.takePicture(mShutterCallback, mPictureCallbackRaw, iccb);
	}
	
	protected void onResume()  
	{
		Log.e(getClass().getSimpleName(), "onResume");  
		super.onResume();
	}  
	  
	protected void onSaveInstanceState(Bundle outState)  
	{  
		super.onSaveInstanceState(outState);
	}  
	  
	protected void onStop()  
	{  
		Log.e(getClass().getSimpleName(), "onStop");  
		super.onStop();
		try{
		isPreviewRunning=false;
        camera.release();
        surfaceView.destroyDrawingCache();
        lm.removeUpdates(locationListener);
		}catch(Exception e){
			e.printStackTrace();
		}
        System.gc();
	}
	
	protected void onDestroy(){
        super.onDestroy();
        try{
        isPreviewRunning=false;
        camera.release();
        surfaceView.destroyDrawingCache();
        lm.removeUpdates(locationListener);
        }catch(Exception e){
        	e.printStackTrace();
        }
        System.gc();
    }
	
	protected void onPause(){
        super.onPause();
        try
        {
        isPreviewRunning=false;        
        camera.release();
        surfaceView.destroyDrawingCache();
        lm.removeUpdates(locationListener);
        }catch(Exception e){
        	e.printStackTrace();
        }
        System.gc();
    }	  
	
	public void surfaceCreated(SurfaceHolder holder)  
	{  		
		 Log.e(getClass().getSimpleName(), "surfaceCreated"); 	
		 //check if calculate tags and upload files are alread running
		if(t!=null){
			try {
				t.join();				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)  
	{  		
		Log.e(getClass().getSimpleName(), "surfaceChanged");		
		if(isPreviewRunning==true){
		camera.stopPreview();
		}
		try {
			try{
				camera.release();
			}catch(Exception e){
				//e.printStackTrace();
			}
			camera = Camera.open();
			camera.setPreviewDisplay(holder);	
			if (!mInitialized) {
		        mInitialized = true;		        
		        Log.e("SCREEN RESOLUTION",camera.getParameters().getPreviewSize().height+""+camera.getParameters().getPreviewSize().width);
		      }		 
			camera.startPreview(); 
			isPreviewRunning = true;
		} catch (IOException e) {
			Log.e("Surface Created",e.toString());
		} catch(RuntimeException e){
			e.printStackTrace();
			alertbox("Error", "Failed to Connect to Camera Service.");
		}		
	}	 
	
	public void surfaceDestroyed(SurfaceHolder holder)  
	{
		isPreviewRunning=false;
		Log.e(getClass().getSimpleName(), "surfaceDestroyed");  
		try{
			camera.release();
			lm.removeUpdates(locationListener);
		}catch(Exception e){
			e.printStackTrace();
		}				
	}
	
	//calculating solar angle based on lat long and current date and time
	public void solarAngles()
	{
		double dec_angle;
		double hour_angle;
		double sinAttd;
		String tStamp = timeStampFormat.format(new Date());
		int  year1 = Integer.parseInt(tStamp.substring(0, 4));
		int  month1 = Integer.parseInt(tStamp.substring(4, 6));
		int  date1 = Integer.parseInt(tStamp.substring(6, 8));
		int  hour1=0;
		int  minute1=0;
		int  sec1=0;
		System.out.println(month1);
		android.util.Log.e("test", "yyyyMMddHHmmssSS");
		android.util.Log.e("test", "Time STAMP  " + tStamp);
		
		//CODE FOR CALCULATING UTC BIAS set on PHONE
		Time mSavedTime = new Time();
		long now = System.currentTimeMillis();
		mSavedTime.set(now);

		// parse the timezone information from the Time object
		TimeZone mMyTimeZone = TimeZone.getTimeZone(mSavedTime.timezone);

		// transfer the long TimeZone offset into hours unit
		int utc_bias = (mMyTimeZone.getRawOffset() / 3600000);

		// take daylight saving into consideration, however we need the time
		Calendar mCal = Calendar.getInstance(mMyTimeZone);
		mCal.set(year1 , month1-1, date1);

		if(mMyTimeZone.useDaylightTime() && mMyTimeZone.inDaylightTime
		(mCal.getTime()))
		{
			utc_bias = utc_bias + (mMyTimeZone.getDSTSavings() / 3600000);
		} 
		// Calculating UTC
			final int msInMin = 60000;    
		    final int minInHr = 60;    
		    Date date = new Date();    
		    int Hours, Minutes;    
		    DateFormat dateFormat = DateFormat.getDateTimeInstance(     
		       DateFormat.LONG, DateFormat.LONG );  
		    TimeZone zone = dateFormat.getTimeZone();  
		    android.util.Log.e("test", "IST Time: " + dateFormat.format( date ));
		    Minutes =zone.getOffset( date.getTime() ) / msInMin;
		    Hours = Minutes / minInHr;    
		    zone = zone.getTimeZone( "GMT Time" +(Hours>=0?"+":"")+Hours+":"+ Minutes);
		    dateFormat.setTimeZone( zone );
		    String gmtTime = dateFormat.format(date);

		    String[] str = gmtTime.split(" ");
		    
		    String parsingGMT = str[3];
		    android.util.Log.e("test", "UTC: " + gmtTime + " Split String: " + parsingGMT);
		    String[] gmtCurrTime = parsingGMT.split(":");
		    hour1 = Integer.parseInt(gmtCurrTime[0]);
		    minute1 = Integer.parseInt(gmtCurrTime[1]);
		    sec1 = Integer.parseInt(gmtCurrTime[2]);
		    
		    String ampm = str[4];
			
		    android.util.Log.e("test", "UTC time used in equations " + hour1 + ":" + minute1 + ":" + sec1);
			android.util.Log.e("test", "utc_bias " + utc_bias);
			
			hour1 = hour1 + 12;
		
			if(ampm.equalsIgnoreCase("PM"))
			{
				if(hour1 < 12)
				{
					hour1 = hour1 + 12;
				}
			}
			android.util.Log.e("test", "Time used for calculations " + hour1 + ":" + minute1 + ":" + sec1);
			android.util.Log.e("test", "Date used for calculations mmddyyyy " + month1 + ":" + date1 + ":" + year1);
			
		//Now Calculating solar angles
	
	    if ( month1 <= 2 ) // january & february
	    {
	        year1  = year1 - 1;
	        month1 = month1 + 12;
	    }
	
	    double jd = Math.floor( 365.25*(year1 + 4716.0)) + Math.floor( 30.6001*( month1 + 1.0)) + 2.0 - Math.floor( year1/100.0 ) + Math.floor( Math.floor( year1/100.0 )/4.0 ) + date1 - 1524.5 + (hour1 + minute1/60.0 + sec1/3600.0)/24;
		
		//double d = 367*year1 - (7*(year1+(month1+9)))/4 + (275*month1)/9 + date1 - 730530;
		double d = jd-2451543.5;
			
		android.util.Log.e("test", "d " + d);
		
		double w = 282.9404+4.70935e-5*d; 	//(longitude of perihelion degrees)
		double a = 1.000000; 				//(mean distance, a.u.)
		double e = 0.016709-1.151e-9*d;       	//(eccentricity)
		double M = ((356.0470+(0.9856002585*d)) % 360);	//(mean anomaly degrees)
		double L = w + M;                     //(Sun's mean longitude degrees)
		double oblecl = 23.4393-3.563e-7*d;  //(Sun's obliquity of the ecliptic)
		
		android.util.Log.e("test", "M " + M);
		
		double pi = 3.142587;
		
		double E = M+(180/pi)*e*Math.sin(M*(pi/180))*(1+e*Math.cos(M*(pi/180)));

		//rectangular coordinates in the plane of the ecliptic (x axis toward perhilion)
		double x = Math.cos(E*(pi/180))-e;
		double y = Math.sin(E*(pi/180))*Math.sqrt(1-(e*e));

		//find the distance and true anomaly
		double r = Math.sqrt((x*x) + (y*y));
		double v = Math.atan2(y,x)*(180/pi);

		//find the longitude of the sun
		double lon_sun = v + w;

		//compute the ecliptic rectangular coordinates
		double xeclip = r*Math.cos(lon_sun*(pi/180));
		double yeclip = r*Math.sin(lon_sun*(pi/180));
		double zeclip = 0.0;
		
		
		//rotate these coordinates to equatorial rectangular coordinates
		double xequat = xeclip;
		double yequat = yeclip*Math.cos(oblecl*(pi/180))+zeclip*Math.sin(oblecl*(pi/180));
		double zequat = yeclip*Math.sin(23.4406*(pi/180))+zeclip*Math.cos(oblecl*(pi/180));
		double Alt = 0;
		//convert equatorial rectangular coordinates to RA and Decl:
		r = Math.sqrt((xequat*xequat) + (yequat*yequat) + (zequat*zequat))-(Alt/149598000); //roll up the altitude correction
		double RA = Math.atan2(yequat,xequat)*(180/pi);
		double delta = Math.asin(zequat/r)*(180/pi);
		
		//Find the J2000 value
		//double J2000 = jd - 2451545.0;
		double UTH = hour1 + minute1/60.0 + sec1/3600.0;
		android.util.Log.e("test", "UTH " + UTH);
		//Calculate local siderial time
		double GMST0= ((L+180)%360)/15;
		android.util.Log.e("test", "GMST0 " + GMST0);
		double SIDTIME = GMST0 + UTH + lon/15.0;
		android.util.Log.e("test", "RA " + RA);
		android.util.Log.e("test", "SIDTIME " + SIDTIME);

		//Replace RA with hour angle HA
		double HA = (SIDTIME*15 - RA);
		
		android.util.Log.e("test", "HA " + HA);
		//Replace RA with hour angle HA
		//double HA = 15 * (12 - hour1);

		//convert to rectangular coordinate system
		x = Math.cos(HA*(pi/180))*Math.cos(delta*(pi/180));
		y = Math.sin(HA*(pi/180))*Math.cos(delta*(pi/180));
		double z = Math.sin(delta*(pi/180));

		//rotate this along an axis going east-west.
		double xhor = x*Math.cos((90-lat)*(pi/180))-z*Math.sin((90-lat)*(pi/180));
		double yhor = y;
		double zhor = x*Math.sin((90-lat)*(pi/180))+z*Math.cos((90-lat)*(pi/180));
		Log.i("LATITUDE", Double.toString(lat));
		//Find the h and AZ 
		double Az = Math.atan2(yhor,xhor)*(180/pi) + 180;
		double El = Math.asin(zhor)*(180/pi);
		android.util.Log.e("test", "AZ " + Az);
		android.util.Log.e("test", "EL " + El);
		azimuth = Az;
		elevation = El;
		
	}
	
    public void calculateTags()
    {
    	//calculating solar angles
    	try{ 		
    	
    	String filename;
		int index;
		String path="";
		String zipc = "";
		int idBr1=0,idBr2=0,idAdd=0;
		BufferedInputStream inloc;
		String locLine,myloc = null;
		if(lat==0 && lon==0){
			LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
			 // Initialize criteria for location providers              
            Criteria coarse = new Criteria();    
            coarse.setAccuracy(Criteria.ACCURACY_COARSE);
            
			Location currentLocation = locationManager.getLastKnownLocation("network");
			try{
			lat=currentLocation.getLatitude();
			lon=currentLocation.getLongitude();
			altitude=currentLocation.getAltitude();
			}catch(NullPointerException e){
				lat=34;
				lon=-118;	
				altitude=0;
			}			
			Log.i("Location", lat+","+lon);           
		}
		Pcapture.this.solarAngles(); 
		myloc = "http://www.melissadata.com/lookups/latlngzip4.asp?lat="+lat+"&lng="+lon;
		GeomagneticField geoMagnetic=new GeomagneticField((float)lat, (float)lon, (float) altitude, System.currentTimeMillis());
		if(myloc!=null)
		{			
			URL mylocSite = new URL(myloc);
			URLConnection locConn = mylocSite.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) locConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			int response = httpConn.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK)
			{
				inloc = new BufferedInputStream(httpConn.getInputStream());
				DataInputStream dataIn = new DataInputStream(inloc);
				while (true){
					locLine = dataIn.readLine();
					if(locLine.contains("Distance</td>")) break;
				}
				idAdd = locLine.indexOf("Address");
				idBr1 = locLine.indexOf("<br>", idAdd);
				idBr2 = locLine.indexOf("<br>", idBr1+4);
				zipc = locLine.substring(idBr2+4, idBr2+9);
			}
			
			android.util.Log.e("test", "Retrieved Zipcode " + zipc + "Address Index " + idAdd + "BR1 Index " + idBr1 + "BR2 Index " + idBr2);
			
			URL url = new URL("http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query="+zipc);
	        /* Get a SAXParser from the SAXPArserFactory. */
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        SAXParser sp = spf.newSAXParser();

	        /* Get the XMLReader of the SAXParser we created. */
	        XMLReader xr = sp.getXMLReader();
	        /* Create a new ContentHandler and apply it to the XML-Reader*/
	        WeatherXMLHandler weatherXMLHandler=new WeatherXMLHandler();
	        xr.setContentHandler(weatherXMLHandler);
	        
	        /* Parse the xml-data from our URL. */
	        xr.parse(new InputSource(url.openStream()));
	        /* Parsing has finished. */

	        /* Our ExampleHandler now provides the parsed data to us. */
	        XMLParsedDataSet myXmlParsedDataSet=weatherXMLHandler.getParsedData();
			tem=myXmlParsedDataSet.getTemperature();
			hum=myXmlParsedDataSet.getHumidity();
			vis=myXmlParsedDataSet.getVisibility();					
		}
		else
		{
			tem = "Internet_Tag_OFF";
			hum = "Internet_Tag_OFF";
			vis = "Internet_Tag_OFF";
		}
		//Writing gathered information into the file
		if(imageFilePath != null){
			index = imageFilePath.lastIndexOf(".");
			filename =  imageFilePath.substring(0,index);
			path = filename + ".tag";
			android.util.Log.e("test", "Picture taken, filePath is " + path);;
        	try {
        		FileOutputStream buf = new FileOutputStream(path);
        		OutputStreamWriter osw = new OutputStreamWriter(buf);
        		
        		//device id
        		TelephonyManager mTelephonyMgr = (TelephonyManager) 
        		getSystemService(Context.TELEPHONY_SERVICE); 
        		String imei = mTelephonyMgr.getDeviceId(); 
        		String str = imei+" ";
        		osw.write(str);
        		
        		//focal Length
        		camera.release();
        		FocalLengthAccessor fl=FocalLengthAccessor.getInstance();        		
        		str=fl.getFocalLength()+" ";
        		//str="4.31"+" ";
        		osw.write(str);
        		
        		Calendar cd=Calendar.getInstance();
        		Date dt=cd.getTime();
        		//Year
        		str=(1900+dt.getYear())+" ";
        		osw.write(str);
    
        		//Month
        		str=(dt.getMonth()+1)+" ";//+1 because Android Returns value of month one less than the actual
        		osw.write(str);
        		
        		//Day
        		str=dt.getDate()+ " ";
        		osw.write(str);
        		//Hour
        		str=dt.getHours()+" ";
        		osw.write(str);
        		
        		//Minute
        		str=dt.getMinutes()+" ";
        		osw.write(str);
        		
        		//get the privacy preference value
        		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                boolean privacyisChecked=settings.getBoolean("PrivacyFilter", false);
                
        		//Lat
        		str = lat+" ";
        		if(privacyisChecked)
        			str="0 ";        		
        		osw.write(str);
        		
        		//Long
        		str = lon+ " ";
        		if(privacyisChecked)
        			str="0 ";        		
        		osw.write(str);
        		
        		//Azimuth
        		//since camAzim is counter clockwise from east and declination is angle between
				//magnetic north and true north positive towards east. We need to subtract the declination
				//to get the counterclockwise positive angle from true east

	       		 if(camAzim>=0 && camAzim<=geoMagnetic.getDeclination()){
	                	camAzim=(float) (360+camAzim-geoMagnetic.getDeclination());
	                }else{
	                	camAzim=(float) (camAzim-geoMagnetic.getDeclination());
	                }
	       		str =camAzim+" ";
	       		osw.write(str);
        		
        		//Elevation
        		str = Float.toString(camElev)+" ";
        		osw.write(str);     		
        		
        		//Solar Azimuth
        		str = azimuth+" ";
        		osw.write(str);
        		
        		//Solar Elevation
        		str =  elevation+" ";
        		osw.write(str);
        		
        		//if(internetB.isChecked() == true)
				//{
        		//Temp
        			str =tem+" ";
            		osw.write(str);
            		
        		//Humidity
            		str =hum;
            		osw.write(str.replace('%', ' '));
            		
        		//Visibility
            		str = vis+" ";
            		osw.write(str);
				//}
        		//User Visibility Rating
        		str=visibilityRating+" ";
        		osw.write(str);
        		
        		//Co-ordinates
        		String[] rectXY=coordinates.split(",");
        		str=rectXY[0]+" ";
        		osw.write(str);
        		
        		str=rectXY[1]+" ";
        		osw.write(str);
        		
        		str=rectXY[2]+" ";
        		osw.write(str);
        		
        		str=rectXY[3]+" ";
        		osw.write(str);
        		       		
        		str=rectXY[4]+" ";
        		osw.write(str);
        		
        		str=rectXY[5]+" ";
        		osw.write(str);
        		
        		osw.flush();
    			osw.close();
        	}catch(Exception ex) {  
    			ex.printStackTrace();  
    		}
        	//Toast.makeText(Pcapture.this, "Tags Calculated. Thank you for waiting", Toast.LENGTH_SHORT).show();	            	
		}
		//Just to save file name of tag file
		String fname_fileList  = "/sdcard/"+sharedVariables.filesList;
		try{
			FileWriter writer = new FileWriter(fname_fileList,true);
  			writer.write(path+"\n");
			writer.flush();
			writer.close();
		}catch(IOException e){
				e.printStackTrace();
		}   
		//start service for uploading files
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent ii=new Intent(getApplicationContext(), VisibilityNotification.class);
				startService(ii);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						new AlertDialog.Builder(Pcapture.this)  
						   .setMessage("You will receive the visibility value shortly. Please check notifications.") 
						   .setTitle("Visibility")  
						   .setCancelable(true)  
						   .setNeutralButton(android.R.string.ok,  
								   new DialogInterface.OnClickListener() {  
							   public void onClick(DialogInterface dialog, int whichButton){
								  Intent i = new Intent();
								 i.setAction(Intent.ACTION_MAIN);
								 i.addCategory(Intent.CATEGORY_HOME);
								 startActivity(i);
							   }
							   
						   		})  
						   .show();  
					}
				});
				
				
			}
		}).start();
		
			
		
    	 }catch(Exception e){
    		e.printStackTrace();
    		try{
    		File listFile = new File(Environment.getExternalStorageDirectory()+"/"+sharedVariables.filesList);
    		FileInputStream fis = new FileInputStream(listFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);			
    		dis = new DataInputStream(bis);	
			while(dis.available() != 0){
					fname = dis.readLine();
					File f=new File(fname);
					f.delete();				
				}
			//Just to clear contents of the file
			String fname_fileList  = "/sdcard/"+sharedVariables.filesList;
			FileWriter writer = new FileWriter(fname_fileList);	
			writer.flush();
			writer.close();
		 	alertHandler.sendEmptyMessage(1);
    		} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
    	}
    }
    
    
	private final SensorListener mListener = new SensorListener() {
        
        public void onSensorChanged(int sensor, float[] values) {
            //Updating orientation sensor information
        	degree_rotate = values[SensorManager.DATA_X]; // Specific for compass implementation
            Xval = values[SensorManager.RAW_DATA_X];
            Yval = values[SensorManager.RAW_DATA_Y];
            Zval = values[SensorManager.RAW_DATA_Z];
            
            if((Yval <= -170)|| (Yval>=-5 && Yval<=5))
            {
            	//camAdjust.setTextColor(Color.GREEN);
            	main.removeView(spiritView);
     	        spirtCorrect = true;
     	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2), screenHeight-70, 7);
         	    main.addView(spiritView);
         	    arrowImageHandler.sendEmptyMessage(0);
         	    arrowCheck=0;
            }
            else if((Zval <= 5 && Zval >=-5 ))
            {
	           // camAdjust.setTextColor(Color.GREEN);
            	main.removeView(spiritView);
      	        spirtCorrect = true;
      	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2), screenHeight-70, 7);
          	    main.addView(spiritView);
          	    arrowImageHandler.sendEmptyMessage(0);
          	    arrowCheck=0;
            }
            else
            {
            	//camAdjust.setTextColor(Color.RED);
            	if(Yval > 5)
            	{		
	            	if(Yval<20)
	            	{
	            		main.removeView(spiritView);
		      	        spirtCorrect = false;
		      	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2)-Yval,screenHeight-70, 7);
		          	    main.addView(spiritView);
		          	    if(arrowCheck!=1){
		          	    	arrowCheck=1;
		          	    	arrowImageHandler.sendEmptyMessage(0);
		          	    	arrowImageHandler.sendEmptyMessage(1);
		          	    }
	            	}
	            	else
	            	{
	            		main.removeView(spiritView);
		      	        spirtCorrect = false;
		      	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2)-25, screenHeight-70, 7);
		          	    main.addView(spiritView);
		          	    if(arrowCheck!=1){
			          	    arrowCheck=1;
			          	    arrowImageHandler.sendEmptyMessage(0);
			          	    arrowImageHandler.sendEmptyMessage(1);   
		          	    }
	            	}
	          	    
            	}
            	else if (Yval < -5)
            	{
            		if(Yval > -20)
            		{
	            		main.removeView(spiritView);
		      	        spirtCorrect = false;
		      	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2)-Yval, screenHeight-70, 7);
		          	    main.addView(spiritView);
		          	    if(arrowCheck!=-1){
			          	    arrowCheck=-1;
			          	    arrowImageHandler.sendEmptyMessage(0);
			          	    arrowImageHandler.sendEmptyMessage(-1);
		          	    }
            		}
            		else
            		{
            			main.removeView(spiritView);
		      	        spirtCorrect = false;
		      	        spiritView = new spiritBall(Pcapture.this, (screenWidth/2)+25, screenHeight-70, 7);
		          	    main.addView(spiritView);
		          	    if(arrowCheck!=-1){
			          	    arrowCheck=-1;
			          	    arrowImageHandler.sendEmptyMessage(0);
			          	    arrowImageHandler.sendEmptyMessage(-1);
		          	    }
            		}
            	}
            }
           
           if(Yval>-90 && Yval<90){
            	//phone is pointing downwards
                camElevCurrent = -(90+Zval);
                }else if(Yval<-90 || Yval>90){
                	//phone is pointing upwards
                	camElevCurrent=(90+Zval);
                }
            //calculation Camera Azimuth counter clockwise from East
            camAzimCurrent = values[0];//value directly from the compass, clockwise positive from north
            if(camAzimCurrent>=0 && camAzimCurrent<=90){
            	camAzimCurrent=360+camAzimCurrent-90;
            }else{
            	camAzimCurrent = camAzimCurrent-90;//making the value clockwise positive from west
            }
            
            camAzimCurrent = 360-camAzimCurrent;//reverse the conventions, making it counter clockwise from east
            //camAdjust.setText("El: " +camElevCurrent +" 	Az: "+camAzimCurrent);
           
        }
        public void onAccuracyChanged(int sensor, int accuracy) {
            // TODO Auto-generated method stub            
        }
    };
	public Handler arrowImageHandler=new Handler(){
		@Override
	      public void handleMessage(Message msg) {  
	          if(msg.what==0){
	        	  arrowLeft.setVisibility(ImageView.INVISIBLE);
	         	  arrowRight.setVisibility(ImageView.INVISIBLE);
	          }else if(msg.what==1){
	        	  new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								arrowRight.setVisibility(ImageView.VISIBLE);
								
							}
						})	;
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								arrowRight.setVisibility(ImageView.INVISIBLE);
								if(arrowCheck==1){
								
									arrowImageHandler.sendEmptyMessage(1);
								}
							}
						})	;
						}
					}).start();
	          }else if(msg.what==-1){
	        	 
	        	  new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							arrowLeft.setVisibility(ImageView.VISIBLE);
							
						}
					})	;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							arrowLeft.setVisibility(ImageView.INVISIBLE);
							if(arrowCheck==-1){
							
								arrowImageHandler.sendEmptyMessage(-1);
							}
						}
					})	;
					}
				}).start();
	          }
	      }
		
	};
	public class ImageCaptureCallback implements PictureCallback  {  
		private ContentResolver cr;
		private Bitmap bm;
		public ImageCaptureCallback(ContentResolver cr) {  
			this.cr = cr;  
		}  
		
		public void onPictureTaken(byte[] data, Camera camera) {  
			try {				
				Log.i(getClass().getSimpleName(), "onPictureTaken=" + data + " length = " + data.length);
				
				bm = BitmapFactory.decodeByteArray(data,0,data.length );	
				url = Images.Media.insertImage(cr,bm, "weatherSenseTempImage"+"tp", "weatherSenseTempImage");
	            if(url == null){
	            	Toast.makeText(Pcapture.this, "No SD Card detected in slot.", Toast.LENGTH_SHORT).show();
	            	finish();
	            }	            	            
	            // getting FILE PATH FOR Captured Image
	            // Querying using resulted URL in insertImage
	            final Uri imgUri = Uri.parse(url);
	            android.database.Cursor cur = null;
	            cur  = getContentResolver().query(      imgUri, new String[]
	            { MediaStore.Images.ImageColumns.DATA },
	                                            null, null, null);
	            if (cur != null && cur.moveToNext()) {
	                    imageFilePath = cur.getString(0);
	            }
	            cur.close();				
	            //crop an image\
	            camElev=camElevCurrent;
	            camAzim=camAzimCurrent;
	            bm.recycle();
	            Intent cropIntent = new Intent();
	            cropIntent.setClass(getBaseContext(), CropImage.class);
	            cropIntent.putExtra("ImageUrl", imageFilePath);
	            startActivityForResult(cropIntent,1);
	            System.gc();	            
			} catch(Exception ex) {  
				ex.printStackTrace();  
			}  
		}  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1)
		{		
			progressd = ProgressDialog.show(this,"Please Wait","Uploading files..",true);
			File f=new File(imageFilePath);
			f.delete();
			if(data!=null)
			{				
				Bundle extras=data.getExtras();
				String filePath=extras.getString("data");
				visibilityRating=""+extras.getInt("visibilityRating");
				Bitmap bm=(Bitmap)BitmapFactory.decodeFile(filePath);
				coordinates=extras.getString("coordinates");
				f=new File(filePath);
				f.delete();
				url = Images.Media.insertImage(this.getContentResolver(),bm,"tp", "weatherSenseImage");
	            if(url == null){
	            	Toast.makeText(Pcapture.this, "No SD Card detected in slot.", Toast.LENGTH_SHORT).show();
	            	finish();
                }	            
	            // getting FILE PATH FOR Captured Image
	            // Querying using resulted URL in insertImage
	            final Uri imgUri = Uri.parse(url);
	            android.database.Cursor cur = null;
	            cur  = getContentResolver().query(      imgUri, new String[]
	            { MediaStore.Images.ImageColumns.DATA },
	                                            null, null, null);
	            if (cur != null && cur.moveToNext()) {
	                    imageFilePath = cur.getString(0);
	            }
	            android.util.Log.e("test", "Picture taken, filePath is " + imageFilePath);
	            String fname_fileList  = "/sdcard/"+sharedVariables.filesList;
	    		try{
	    			FileWriter writer = new FileWriter(fname_fileList,true);
	      			writer.write(imageFilePath+"\n");
	    			writer.flush();
	    			writer.close();
	    		}catch(IOException e){
	    				e.printStackTrace();
	    		}		
	    		 
	    		android.util.Log.e("test", "Picture taken, now calculating TAGS !!" + X +" "+ Y +" "+Z + " " + visibilityRating + " RadioData : " + radioData);
	    		camera.release();
	    		
	    		t=new Thread(new Runnable(){
					public void run() {
						// TODO Auto-generated method stub
						calculateTags();
						runOnUiThread(new Runnable() {						
							@Override
							public void run() {
								progressd.dismiss();							
							}
						});					    		
					}    			
	    		});
	    		t.start();    		
			}
			else
				progressd.dismiss();
		}
	}
		

	private class RefreshHandler extends Handler {

	    public void handleMessage(Message msg) {
	    
		    if(msg.what==1){
		    	alertbox("Error", "Cannot upload file. Please check your data connection.");
		    }
	    }
	  }	
	
	private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            if (loc != null) {                
            	//getting location information from GPS
            	lat = loc.getLatitude();
            	lon = loc.getLongitude();
            	altitude=loc.getAltitude();
            	//removing location updates : Stopping the GPS
            	lm.removeUpdates(locationListener);
            	//Toast.makeText(Pcapture.this, "GPS Location Calculated", Toast.LENGTH_SHORT).show();
            }
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status,Bundle extras) {
        }
	}
  
    //CODE FOR SPIRIT LEVEL	: class spiritBall that adds spirit level view
	public class spiritBall extends View {
	    private final float x;
	    private final float y;
	    private final int r;
	    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    
	    public spiritBall(Context context, float x, float y, int r) {
	        super(context);
	        mPaint.setColor(Color.YELLOW);
	        this.x = x;
	        this.y = y;
	        this.r = r;
	    }
	    
	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        mPaint.setColor(Color.YELLOW);
	        //set spiritView according to size of the screen
	        RectF rect = new RectF((screenWidth/2)-30,screenHeight-75,(screenWidth/2)+30,screenHeight-65);
	        canvas.drawRect(rect, mPaint); 			
	        mPaint.setColor(Color.BLUE);
	        canvas.drawLine(screenWidth/2-9, screenHeight-75, screenWidth/2-9, screenHeight-64, mPaint);
	        canvas.drawLine(screenWidth/2+9, screenHeight-75, screenWidth/2+9, screenHeight-64, mPaint);	        
	        if(spirtCorrect == true){
	        	mPaint.setColor(Color.GREEN);
	        }else{
	        	mPaint.setColor(Color.RED);
	        }			
			canvas.drawCircle(x, y, r, mPaint);	        
	    }	    
	} 
	
	//Show alert box and display the title and message as passed
	protected void alertbox(String title, String mymessage)  
   {  
	   new AlertDialog.Builder(Pcapture.this)  
	   .setMessage(mymessage)  
	   .setTitle(title)  
	   .setCancelable(true)  
	   .setNeutralButton(android.R.string.ok,  
			   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int whichButton){}  
	   		})  
	   .show();  
   }
	protected void settingsbox(){
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.settings,null
		                               );
		alertDialog=new AlertDialog.Builder(Pcapture.this)
		.setMessage("If the filter is on, GPS Co-ordinates will not be shared.")
		.setTitle("Privacy Settings")
		.setCancelable(true)
		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).setView(layout).show();
		
		privacyB = (ToggleButton) alertDialog.findViewById(R.id.privacyFilter);   
        //setting up the preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean filetransfer=settings.getBoolean("PrivacyFilter", false);
        privacyB.setChecked(filetransfer);        
       
        privacyB.setOnCheckedChangeListener(new OnCheckedChangeListener(){

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		      SharedPreferences.Editor editor = settings.edit();
		      editor.putBoolean("PrivacyFilter", isChecked);
		      // Commit the edits!
		      editor.commit();			
		}});          
	}
}	