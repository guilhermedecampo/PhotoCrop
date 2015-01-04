/**
 * PhotoCrop is a modification of Cordova Camera Library for Android and iOS with Cropping utility
 */
package com.sarriaroman.photocrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.soundcloud.android.crop.Crop;

/**
 * This class launches the crop view and returns the captured image.
 */
public class PhotoCrop extends CordovaPlugin {
	private static final int DATA_URL = 0; // Return base64 encoded string
	private static final int FILE_URI = 1; // Return file uri
											// (content://media/external/images/media/2
											// for Android)
	private static final int NATIVE_URI = 2; // On Android, this is the same as
												// FILE_URI

	// -----------------------------------------------------------------------------------------
	private static final int SQUARE_TYPE = 0; 
	private static final int SIZE_TYPE = 1;
	private static final int ASPECT_TYPE = 2;
	// -----------------------------------------------------------------------------------------

	private CallbackContext callbackContext;
	private int targetX;
	private int targetWidth;
	private int targetY;
	private int targetHeight;
	private String fileUri;
	private int destType;
	private int cropType;
	private File destUri;

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback id used when calling back into JavaScript.
	 * @return A PluginResult object with a status and message.
	 */
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;

		if (action.equals("cropPicture")) {
			this.destType = FILE_URI;
			this.targetX = this.targetWidth = 0;
			this.targetY = this.targetHeight = 0;

			this.fileUri = args.getString(0);
			this.destUri = this.createCropFile();
			//this.fileUri = this.fileUri.replaceAll("file://", "");
			
			Log.i(this.cordova.getActivity().getApplication().getPackageName(), "File: " + this.fileUri);
			
			destType = args.getInt(1);
			cropType = args.getInt(2);
			
			this.targetX = this.targetWidth = args.getInt(3);
			this.targetY = this.targetHeight = args.getInt(4);

			try {
				Crop crop = new Crop(Uri.parse(this.fileUri))
					.output(Uri.fromFile(this.destUri));
					
				if( cropType == SQUARE_TYPE ) {
					crop.asSquare();
				} else if( cropType == SIZE_TYPE ) {
					crop.withMaxSize(targetWidth, targetHeight);
				} if( cropType == ASPECT_TYPE ) {
					crop.withAspect(targetX, targetY);
				}
				
				this.cordova.startActivityForResult(this, crop.getIntent(this.cordova.getActivity()), crop.REQUEST_CROP);
			} catch (IllegalArgumentException e) {
				Log.e(this.cordova.getActivity().getApplication().getPackageName(), e.getLocalizedMessage(), e);
				
				callbackContext.error("Illegal Argument Exception");
				PluginResult r = new PluginResult(PluginResult.Status.ERROR);
				callbackContext.sendPluginResult(r);
				return true;
			}

			PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
			r.setKeepCallback(true);
			callbackContext.sendPluginResult(r);

			return true;
		}
		return false;
	}
	
	private String getTempDirectoryPath() {
        File cache = null;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + cordova.getActivity().getPackageName() + "/cache/");
        }
        // Use internal storage
        else {
            cache = cordova.getActivity().getCacheDir();
        }

        // Create the cache directory if it doesn't exist
        cache.mkdirs();
        return cache.getAbsolutePath();
    }
	
	/**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @return a File object pointing to the temporary picture
     */
    private File createCropFile() {
        File photo = null;
        
        String[] fsegs = this.fileUri.split("\\.");
        String encodingType = (fsegs[fsegs.length - 1]).toLowerCase(Locale.getDefault());
        
        if (encodingType.equalsIgnoreCase("jpg") || encodingType.equalsIgnoreCase("jpeg")) {
            photo = new File(getTempDirectoryPath(), ".Crop.jpg");
        } else if (encodingType.equalsIgnoreCase("png")) {
            photo = new File(getTempDirectoryPath(), ".Crop.png");
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
        }
        return photo;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.i(this.cordova.getActivity().getApplication().getPackageName(), "Result: " + requestCode);
    	if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            this.handleCroppedImage();
        }
    }

	private void handleCroppedImage() {
		Bitmap bitmap = null;
        
		if (destType == DATA_URL) {
            bitmap = BitmapFactory.decodeFile(destUri.getAbsolutePath());
            
            if(bitmap == null) {
            	this.failPicture("Error capturing image.");
            }

            this.processPicture(bitmap);
        } else if( destType == NATIVE_URI || destType == FILE_URI ) {
        	this.callbackContext.success( Uri.parse(fileUri).toString() );
        }
	}
	
	public void processPicture(Bitmap bitmap) {
        ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
        try {
            if (bitmap.compress(CompressFormat.JPEG, 100, jpeg_data)) {
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encode(code, Base64.NO_WRAP);
                String js_out = new String(output);
                this.callbackContext.success(js_out);
                js_out = null;
                output = null;
                code = null;
            }
        } catch (Exception e) {
            this.failPicture("Error compressing image.");
        }
        jpeg_data = null;
    }
	
	/**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void failPicture(String err) {
        this.callbackContext.error(err);
    }
}