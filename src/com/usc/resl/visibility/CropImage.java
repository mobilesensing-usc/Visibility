package com.usc.resl.visibility;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class CropImage extends Activity{

	private ImageView cropImageView; //ImageView to be Displayed
	private Button btnSave; //Save Button
	//private Button btnDiscard; //Discard Button
	private Bitmap image = null; //Reference to the image passed
	private RelativeLayout main; //Layout on which buttons and image is displayed
	private Ball b; //rectangle view displayed for cropping the image
	private int X; 
	private int Y;
	private String imageFilePath; //filePath passed from previous intent
	private Rect cropRect; //reference to the cropping rectangle co-ordinates
	private int adjust; 
	private int initX=0;
	private int initY=0;
	private int finalX=0;
	private int finalY=0;
	private int h=150; //height of the cropping rectangle
	private int w=350; //widht of the cropping rectangle
	private int imgHeight=0;
	private int imgWidth=0;
	private SeekBar seekBar;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.e("Custom Supported",requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)+"");
		setContentView(R.layout.cropimage);
		
		//set custom title bar
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);
        TextView titleText=(TextView)findViewById(R.id.titleText);
		titleText.setText("Resize the box so that it only contains the sky pixels");
		Button titleButton=(Button)findViewById(R.id.titleButton);
        titleButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertbox("", "This View allows you to crop the sky pixels from picture taken."+
						"Tap in the center of rectangle to move it on screen. Tap and drag on the sides " +
						"to resize the rectangle.\n\n Slider on the bottom left allows you to rate visibility.\n\n" +
						"Clicking on the calculate visibility button will send the image to backend server for analysis and will return you the visibility value.");
			}
		});
		//setTitle("Resize the box so that it only contains the sky pixels");
		//get the view references
		cropImageView=(ImageView)findViewById(R.id.CropImageView);
		btnSave=(Button)findViewById(R.id.Upload);
		//btnDiscard=(Button)findViewById(R.id.Cancel);
		main=(RelativeLayout)findViewById(R.id.main);
		seekBar=(SeekBar)findViewById(R.id.seekbar);
		
		seekBar.setMax(10);
		//set the listeners for the buttons
		btnSave.setOnClickListener(saveButtonListener);		
		//btnDiscard.setOnClickListener(discardButtonListener);
		
		//get the filepath of the image to be displayed on the image view
		Bundle extras=getIntent().getExtras();
		imageFilePath=extras.getString("ImageUrl");
		//decode the file and set to the image view
		image=BitmapFactory.decodeFile(imageFilePath);
		cropImageView.setImageBitmap(image);
		imgHeight=image.getHeight();
		imgWidth=image.getWidth();
		main.setOnTouchListener(onTouch);		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//set the screen for the rectangle view and image view
		WindowManager wm=getWindowManager();
		Display disp=wm.getDefaultDisplay();
		X=disp.getWidth()/2;
		Y=(disp.getHeight()-80)/2;
		Log.i("OnStart", X+","+Y);
		b=new Ball(this,X,Y,h,w);
		main.addView(b);
		cropImageView.setScaleType(ScaleType.FIT_XY);		
	}

	private OnTouchListener onTouch=new OnTouchListener(){

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			
		//get the co-ordinates of touch as soon as the screen is touched
		switch(arg1.getAction()){
		case MotionEvent.ACTION_DOWN:					
								initX=(int) arg1.getX();
								initY=(int) arg1.getY();
								Log.i("Init", initX+","+initY);
								break;
		}
		
		if(adjust==1 && arg1.getX()>X-(w/2)-(2*w) && arg1.getX()<X+(w/2)+(2*w) && arg1.getY()<Y+(h/2)+(2*h) && arg1.getY()>Y-(h/2)-(2*h)){
			//user is trying to resize the rectangle	
			switch(arg1.getAction()){
				
				case MotionEvent.ACTION_MOVE:
										finalX=(int) arg1.getX();
										finalY=(int) arg1.getY();
										break;
				case MotionEvent.ACTION_UP:
										if(initX!=0 && initY!=0){
											Log.i("Final", finalX+","+finalY);
											int tempX=Math.abs(initX-finalX);
											int tempY=Math.abs(initY-finalY);
											if(tempX>tempY){
												if(initX>finalX){
													Log.i("Motion Event", "Left");
													if(Math.abs(X+w/2-initX)>Math.abs(X-w/2-initX)){
														Log.i("Motion Event","Left Increase");														
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height(),cropRect.width()+tempX);
														h=cropRect.height();
														w=cropRect.width()+tempX;
														main.addView(b);
													}else if(cropRect.width()-tempX>=100){
														Log.i("Motion Event","Left Decrease");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height(),cropRect.width()-tempX);
														h=cropRect.height();
														w=cropRect.width()-tempX;
														main.addView(b);
													}
												}else if(initX<finalX){
													Log.i("Motion Event", "Right");
													if(Math.abs(X+w/2-initX)<=Math.abs(X-w/2-initX)){
														Log.i("Motion Event","Right Increase");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height(),cropRect.width()+tempX);
														h=cropRect.height();
														w=cropRect.width()+tempX;
														main.addView(b);
													}
													else if(Math.abs(X+w/2-initX)>Math.abs(X-w/2-initX) && cropRect.width()-tempX>=100){
														Log.i("Motion Event","Right Decrease");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height(),cropRect.width()-tempX);
														h=cropRect.height();
														w=cropRect.width()-tempX;
														main.addView(b);
													}
												}
											}else{										
												if(initY<finalY){
													Log.i("Motion Event","Down");
													if(Math.abs(Y+h/2-initY)<=Math.abs(Y-h/2-initY)){
														Log.i("Motion Event","Down Increase");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height()+tempY,cropRect.width());
														h=cropRect.height()+tempY;
														w=cropRect.width();
														main.addView(b);
													}
													else if(Math.abs(Y+h/2-initY)>Math.abs(Y-h/2-initY) && Math.abs(cropRect.height())>=100){
														Log.i("Motion Event","Down Decrease");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height()-tempY,cropRect.width());
														h=cropRect.height()-tempY;
														w=cropRect.width();
														main.addView(b);
													}
												}else if(initY>finalY){
													Log.i("Motion Event", "Up");
													if(Math.abs(Y+h/2-initY)>Math.abs(Y-h/2-initY)){
														Log.i("Motion Event","Up Increase");
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height()+tempY,cropRect.width());
														h=cropRect.height()+tempY;
														w=cropRect.width();
														main.addView(b);
													}else if(Math.abs(cropRect.height())>=100){
														Log.i("Motion Event","Up Decrease"+tempY);
														main.removeView(b);
														b=new Ball(arg0.getContext(),X,Y,cropRect.height()-tempY,cropRect.width());
														h=cropRect.height()-tempY;
														w=cropRect.width();
														main.addView(b);
													}
												}
											}
										}
										adjust=0;
										break;								
				}
			}else{
				//user has moved the rectangle to a new co-ordinate
				main.removeView(b);
				h=Math.abs(h);
				b=new Ball(arg0.getContext(),arg1.getX(),arg1.getY(),h,w);
				main.addView(b);
			}		
			return true;
		}		
	};
	public class Ball extends View {
	    private final int x;
	    private final int y;
	    private int h;
	    private final int w;
	    
	    private final Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
	    public Ball(Context context, float x, float y, int h,int w) {
	        super(context);
	        mPaint.setColor(Color.YELLOW);
	        mPaint.setStyle(Style.STROKE);
	        mPaint.setStrokeWidth(3);
	        this.x =(int) x;
	        this.y = (int)y;
	        this.h=(int)h;
	        this.w=(int)w;
	    }
	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        //set the drawable for the images displayed when resizing the rectangle
	        Drawable mCropHeight=getApplicationContext().getResources().getDrawable(R.drawable.cameraheight);
	        Drawable mCropWidth=getApplicationContext().getResources().getDrawable(R.drawable.camera_crop_width);
	      
	        adjust=0;
	      
	        Log.i("On TOuch", x+","+y+","+X+","+w+","+Y+","+this.h);
	        if((x<X-(w/2)+(0.2*w) && x>X-(w/2)-(0.2*w)) || (x>X+(w/2)-(0.2*w) && x<X+(w/2)+(0.2*w)) || (y>Y+(h/2)-(0.2*h) && y<Y+(h/2)+(0.2*h)) || (y<Y-(h/2)+(0.2*h) && y>Y-(h/2)-(0.2*h))){
	        	//set the image bounds to be displayed on the rectangle, since the user has clicked on the edge of a rectangle
	        	/*mCropHeight.setBounds((int)(X-(0.1*w)), (int)(Y+(h/2)-(0.1*h)), (int)(X+(0.1*w)), (int)(Y+(h/2)+(0.1*h)));
		        mCropHeight.draw(canvas);
		        mCropHeight.setBounds((int)(X-(0.1*w)),(int)(Y-(h/2)-(0.1*h)),(int)(X+(0.1*w)),(int)(Y-(h/2)+(0.1*h)));
		        mCropHeight.draw(canvas);		        
		        mCropWidth.setBounds((int)((X-(w/2)-(0.1*w))), (int)(Y-(0.1*h)), (int)(X-(w/2)+(0.1*w)),(int)(Y+(0.1*h)));
		        mCropWidth.draw(canvas);
		        mCropWidth.setBounds((int)(X+(w/2)-(0.1*w)), (int)(Y-(0.1*h)), (int)(X+(w/2)+(0.1*w)),(int)(Y+(0.1*h)));
		        mCropWidth.draw(canvas);*/
	        	
		        adjust=1;
	        }
	        if(adjust==1){
	        	cropRect=new Rect((int)X-(w/2),(int)Y-(h/2),(int)X+(w/2),(int)Y+(h/2));
	        }else{
	        	//user has clicked on just another point
	        	X=(int)x;
		        Y=(int)y;
	        	cropRect=new Rect((int)x-(w/2),(int)y-(h/2),(int)x+(w/2),(int)y+(h/2));
	        	//cropRect=new Rect(190,85,290,185);
	       	}
	        mCropHeight.setBounds((int)(X-(15)), (int)(Y+(h/2)-(15)), (int)(X+(15)), (int)(Y+(h/2)+(15)));
	        mCropHeight.draw(canvas);
	        mCropHeight.setBounds((int)(X-(15)),(int)(Y-(h/2)-(15)),(int)(X+(15)),(int)(Y-(h/2)+(15)));
	        mCropHeight.draw(canvas);		        
	        mCropWidth.setBounds((int)((X-(w/2)-(15))), (int)(Y-(15)), (int)(X-(w/2)+(15)),(int)(Y+(15)));
	        mCropWidth.draw(canvas);
	        mCropWidth.setBounds((int)(X+(w/2)-(15)), (int)(Y-(15)), (int)(X+(w/2)+(15)),(int)(Y+(15)));
	        mCropWidth.draw(canvas);
	        canvas.drawRect(cropRect, mPaint);
	        System.gc();
		    Log.i("cropRect", x+","+w+","+y+","+h);       
	    }
	}
	OnClickListener saveButtonListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			//show a progress dialog while the image is cropped and sent back to the previous intent
			final ProgressDialog progressd = ProgressDialog.show(v.getContext(),"Please Wait","Calculating tags..",true);
			Thread t=new Thread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//calculate the scaling factors
					float scaleX=(float)image.getWidth()/cropImageView.getWidth();
					float scaleY=(float)image.getHeight()/cropImageView.getHeight();
					
					//adjust the co-ordinates if the rectangle is out of the screen
					int top=cropRect.top;
					int rectHeight=cropRect.height();
					Log.i("Save Button", top+","+rectHeight);
					if(cropRect.bottom>cropImageView.getHeight()){
						rectHeight=rectHeight-cropRect.bottom+cropImageView.getHeight();				
					}
					if(top<0){
						rectHeight=rectHeight+top;
						top=0;
					}
					
					int left=cropRect.left;
					int rectWidth=cropRect.width();
					if(left+rectWidth>=cropImageView.getWidth()){
						rectWidth=cropImageView.getWidth()-left;
					}
					if(left<0){
						rectWidth=rectWidth+left;
						left=0;
					}
					//crop the image and save and return data
					Bitmap bm=Bitmap.createBitmap(image, (int)(left*scaleX), (int)(top*scaleY), (int)(rectWidth*scaleX), (int)(rectHeight*scaleY));
					String url = Images.Media.insertImage(getContentResolver(),bm, "weatherSenseTempImage"+"tp", "weatherSenseTempImage");
		            if(url == null){
		            	Toast.makeText(CropImage.this, "No SD Card detected in slot.", Toast.LENGTH_SHORT).show();
		            	finish();
		            }	            
		            String filePath=null;
		            // getting FILE PATH FOR Captured Image
		            // Querying using resulted URL in insertImage
		            final Uri imgUri = Uri.parse(url);
		            android.database.Cursor cur = null;
		            cur  = getContentResolver().query(      imgUri, new String[]
		            { MediaStore.Images.ImageColumns.DATA },
		                                            null, null, null);
		            if (cur != null && cur.moveToNext()) {
		                    filePath = cur.getString(0);
		            }
		            cur.close();
		            //return the data
					Bundle extras=new Bundle();
					extras.putString("data", filePath);
					
					int right=(int)(left*scaleX)+(int)(rectWidth*scaleX);
					int bottom=(int)(top*scaleY)+(int)(rectHeight*scaleY);
					left=(int) (left*scaleX);
					top=(int)(top*scaleY);
					extras.putString("coordinates", left+","+bottom+","+right+","+top+","+imgHeight+","+imgWidth);
					extras.putInt("visibilityRating", seekBar.getProgress());
					setResult(RESULT_OK,(new Intent()).setAction("inline-data").putExtras(extras));
					//free the heap for further use and finish the activity
					bm.recycle();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							cropImageView.setImageBitmap(null);
							image.recycle();
							progressd.dismiss();
							finish();
						}
					});
									
				}
				
			});
			t.start();
			
			
		}
	};
	OnClickListener discardButtonListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// delete the file and free the heap memory before exiting 
			File f=new File(imageFilePath);
			f.delete();
			cropImageView.setImageBitmap(null);
			image.recycle();
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		File f=new File(imageFilePath);
		f.delete();
		cropImageView.setImageBitmap(null);
		image.recycle();
		super.onDestroy();
	}
	//Show alert box and display the title and message as passed
	protected void alertbox(String title, String mymessage)  
   {  
	   new AlertDialog.Builder(CropImage.this)  
	   .setMessage(mymessage)  
	   .setTitle(title)  
	   .setCancelable(true)  
	   .setNeutralButton(android.R.string.ok,  
			   new DialogInterface.OnClickListener() {  
		   public void onClick(DialogInterface dialog, int whichButton){}  
	   		})  
	   .show();  
   }
}
