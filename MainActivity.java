package com.example.choosepictest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static final int SELECT_PICTURE = 1;
	private static final int CROP_PHOTO = 2;


	private Button takePhoto;

	private Button chooseFromAlbum;

	private ImageView picture;

	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		takePhoto = (Button) findViewById(R.id.take_photo);
		chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
		picture = (ImageView) findViewById(R.id.picture);
		takePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Determine Uri of camera image to save.
				final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
				root.mkdirs();
				final String fname = "this_picture";
				final File sdImageMainDirectory = new File(root, fname);
				imageUri = Uri.fromFile(sdImageMainDirectory);

				// Camera.
				final List<Intent> cameraIntents = new ArrayList<Intent>();
				final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				final PackageManager packageManager = getPackageManager();
				final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
				for(ResolveInfo res : listCam) {
					final String packageName = res.activityInfo.packageName;
					final Intent intent = new Intent(captureIntent);
					intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
					intent.setPackage(packageName);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					cameraIntents.add(intent);
				}

				// Filesystem.
				final Intent galleryIntent = new Intent();
				galleryIntent.setType("image/*");
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

				// Chooser of filesystem options.
				final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

				// Add the camera options.
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

				startActivityForResult(chooserIntent, SELECT_PICTURE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				final boolean isCamera;
				if (data == null) {
					isCamera = true;
				} else {
					final String action = data.getAction();
					isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
				}

				Uri selectedImageUri;
				if (isCamera) {
					selectedImageUri = imageUri;
				} else {
					selectedImageUri = data.getData();
				}

				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(selectedImageUri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, CROP_PHOTO);
			}
			else if (requestCode == CROP_PHOTO){
				try {
					Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
							.openInputStream(imageUri));
					picture.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
