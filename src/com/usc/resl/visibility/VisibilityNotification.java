package com.usc.resl.visibility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class VisibilityNotification extends Service{
	private String fname,fname_ftp,tfname;
	private String predictedValue=new String();
	NotificationManager mNotificationManager;
	Notification notification;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		//get a reference to the Notification manager
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		//Instantiate the Notification
		int icon = R.drawable.icon;
		CharSequence tickerText = "Visibility";
		long when = System.currentTimeMillis();
		notification = new Notification(icon, tickerText, when);
		setNotification("Service started. Please wait..");
		mNotificationManager.notify(0,notification);
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				uploadFiles();
			}}).start();
	}
	//File transfer is based on FTP. FTPClient library is used directly to transfer files with images
	//Other options available for file transfer are Flickr, FTP, WebUpload, or similar approach
	//TRY FOR FILE TRANSFERS ON THE GO !!
    
	public void uploadFiles()
    {
		int index;
	
		//ssh
		//get the ssh session
        JSch jsch=new JSch();
        Session session=null;
        Channel channel=null;
        ChannelSftp c=null;
        
		//FTPClient ftpClient = new FTPClient();		
		File listFile = new File(Environment.getExternalStorageDirectory()+"/"+sharedVariables.filesList);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;		
		try{
			fis = new FileInputStream(listFile);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);			
			
			session=jsch.getSession(sharedVariables.ftpUsr, sharedVariables.ftpAddr, 22);
			java.util.Properties config = new java.util.Properties();  
            config.put("StrictHostKeyChecking", "no");  
            session.setConfig(config);  
			session.setPassword(sharedVariables.ftpPass);
			session.connect();
			channel = session.openChannel("sftp");  
            channel.connect();  
            c = (ChannelSftp)channel;
            c.cd("public_html/airQuality/Data");
			while(dis.available() != 0){
				android.util.Log.e("test", "Into while");
				fname = dis.readLine();
				index = fname.lastIndexOf("/");
				tfname =  fname.substring(index+1);
				fname_ftp = tfname;
				
				try {
					BufferedInputStream buffIn = null;
					buffIn = new BufferedInputStream(new FileInputStream(fname));
					File f=new File(fname);
					c.put(new FileInputStream(f),f.getName());
					buffIn.close();
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch(FileNotFoundException e){
					continue;
				}catch (IOException e) {
					e.printStackTrace();
				}
				android.util.Log.e("test", "TRANSFERRED " + fname_ftp);
				
				if(fname_ftp.contains(".tag"))
				{
					index = fname_ftp.lastIndexOf(".");
					String fileName =  fname_ftp.substring(0,index);
					
					String tempURL = sharedVariables.scriptURL+"?"+fileName;
					//String tempURL = sharedVariables.scriptURL;
					try {
						Log.e("TEMP URL", tempURL);
						updateMapFile(tempURL,fileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						android.util.Log.e("test", "Into Update file exception");
						e.printStackTrace();
					}
				}
				File f=new File(fname);
				f.delete();				
			}
			c.disconnect();
			session.disconnect();
			
		}catch(Exception e){
			dis = new DataInputStream(bis);	
			try {
				while(dis.available() != 0){
					fname = dis.readLine();
					File f=new File(fname);
					f.delete();				
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//alertHandler.sendEmptyMessage(1);
			
		}finally{
			//Just to clear contents of the file
			String fname_fileList  = "/sdcard/"+sharedVariables.filesList;
    		try{
    			FileWriter writer = new FileWriter(fname_fileList);
    			writer.flush();
    			writer.close();
    		}catch(IOException e){
    			e.printStackTrace();
    		}
		}
		
    }
	 public void updateMapFile (String urlString,String fileName) throws IOException
	    {
	        int response = -1;
	        InputStream in = null;	
	        URL url = new URL(urlString); 
	        URLConnection conn = url.openConnection();
	                 
	        if (!(conn instanceof HttpURLConnection))                     
	            throw new IOException("Not an HTTP connection");
	        
	        try{
	        	HttpClient httpclient=new DefaultHttpClient();
				HttpGet httpget=new HttpGet(urlString);
				Log.e("RESPONSE","waiting for the response");
				HttpResponse response_new = httpclient.execute(httpget);
				String responsebody=EntityUtils.toString(response_new.getEntity()); 
	        	Log.e("RESPONSE", responsebody);
	        	System.out.println(responsebody);
	        	if (responsebody!=null || !responsebody.trim().equals("")) {
	        		String resp[]=responsebody.split("finalans");
	        		predictedValue=resp[1].trim();
	            	//put an entry into database
	        		try{
	            	String dbScriptURL=sharedVariables.dbScriptURL+"?file="+fileName+"&turbidity="+Float.parseFloat(predictedValue);
	            	url=new URL(dbScriptURL);
            		conn=url.openConnection();
            		if (!(conn instanceof HttpURLConnection))                     
        	            throw new IOException("Not an HTTP connection");
            		httpget=new HttpGet(dbScriptURL);
    				response_new = httpclient.execute(httpget);
    				responsebody=EntityUtils.toString(response_new.getEntity()); 
    				System.out.println(responsebody);
	        		}catch(Exception e){
	        			e.printStackTrace();
	        		}
	            	if(Float.parseFloat(predictedValue)==-3){
	            		setNotification("Unable to model. Please try again.");
	            		mNotificationManager.notify(0,notification);
	            		
	            	}else if(Float.parseFloat(predictedValue)==-1 || Float.parseFloat(predictedValue)==-2){
	            		setNotification("Connection error. Please try again.");
	            		mNotificationManager.notify(0,notification);
	            		
	            	}
	            	else{
	            		setNotification("Visibility in your area is "+predictedValue+" miles");
	            		mNotificationManager.notify(0,notification);
	            	}
	            	//log the error
	            	if(Float.parseFloat(predictedValue)<0){
	            		String error=System.currentTimeMillis()+","+fileName+","+predictedValue;
	            		url=new URL(sharedVariables.errorScriptURL+error);
	            		conn=url.openConnection();
	            		if (!(conn instanceof HttpURLConnection))                     
	        	            throw new IOException("Not an HTTP connection");
	            		httpget=new HttpGet(sharedVariables.errorScriptURL+error);
	    				response_new = httpclient.execute(httpget);
	    				responsebody=EntityUtils.toString(response_new.getEntity()); 
	    			}            		
	            	
	            }else{
	            	setNotification("Server busy. Please try again.");
            		mNotificationManager.notify(0,notification);
            		stopSelf();
	            }
	        	stopSelf();
	        }catch (Exception ex){
	        	Log.e("ERROR", ex.toString());
	            stopSelf();
	        }
	    }
	 public String convertStreamToString(InputStream is) {

	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder(); 
	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            }catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return sb.toString();
	  }
	public void setNotification(String message){		
		//Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		CharSequence contentTitle = "Visibility";
		CharSequence contentText = message;
		Intent notificationIntent = new Intent(this, Pcapture.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				
	}
	
}
