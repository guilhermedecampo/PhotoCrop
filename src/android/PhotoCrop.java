/**
 * PhotoCrop is a modification of Cordova Camera Library for Android and iOS with Cropping utility
 */
package com.sarriaroman.photocrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

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
	private Object targetX;
	private int targetWidth;
	private Object targetY;
	private int targetHeight;
	private String fileUri;
	private int destType;

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

		if (action.equals("takePicture")) {
			this.destType = FILE_URI;
			this.targetX = this.targetWidth = 0;
			this.targetY = this.targetHeight = 0;

			this.fileUri = args.getString(0);
			
			destType = args.getInt(1);
			this.targetX = this.targetWidth = args.getInt(3);
			this.targetHeight = args.getInt(4);
			this.targetY = this.targetHeight = args.getInt(5);

			// If the user specifies a 0 or smaller width/height
			// make it -1 so later comparisons succeed
			if (this.targetWidth < 1) {
				this.targetWidth = -1;
			}
			if (this.targetHeight < 1) {
				this.targetHeight = -1;
			}

			try {
				new Crop(this.fileUri).output(outputUri).asSquare().start((CordovaPlugin) this);
			} catch (IllegalArgumentException e) {
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
    private File createCropFile(int encodingType) {
        File photo = null;
        if (encodingType == JPEG) {
            photo = new File(getTempDirectoryPath(), ".Pic.jpg");
        } else if (encodingType == PNG) {
            photo = new File(getTempDirectoryPath(), ".Pic.png");
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
        }
        return photo;
    }
}
