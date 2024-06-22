package se.voxelmanip.apksigner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

	private static final int PICK_FILE_REQUEST_CODE = 1;
	private static final int CREATE_FILE_REQUEST_CODE = 2;
	private Uri pickedFileUri;
	private File tempOutput;
	private static final String TAG = "ApkSigner";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		openFilePicker();
	}

	private void openFilePicker() {
		Toast.makeText(this, "Please select an APK to sign.", Toast.LENGTH_LONG).show();

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/vnd.android.package-archive");
		startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
			if (data != null) {
				pickedFileUri = data.getData();
				File tempFile = copyUriToFile(pickedFileUri);

				signApk(tempFile);

				if (this.tempOutput != null)
					promptUserToSaveFile();
			}
		} else if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
			if (data != null) {
				pickedFileUri = data.getData();
				copyFileToUri(tempOutput, pickedFileUri);

				Toast.makeText(this, "Done!", Toast.LENGTH_LONG).show();
				finishAffinity();
			}
		}
	}

	private void promptUserToSaveFile() {
		Toast.makeText(this, "App has been signed, please select a path to save it to.", Toast.LENGTH_LONG).show();

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/vnd.android.package-archive");
		intent.putExtra(Intent.EXTRA_TITLE, "signed.apk");
		startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
	}

	private File copyUriToFile(Uri uri) {
		try {
			InputStream inputStream = getContentResolver().openInputStream(uri);
			Log.v(TAG, "Cache dir: "+getCacheDir());
			File tempFile = File.createTempFile("temp_apk", ".apk", getCacheDir());
			try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			}
			return tempFile;
		} catch (IOException e) {
			Toast.makeText(this, "Error copying input file", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	private void copyFileToUri(File sourceFile, Uri destinationUri) {
		try (InputStream inputStream = new FileInputStream(sourceFile);
			 OutputStream outputStream = getContentResolver().openOutputStream(destinationUri)) {

			if (outputStream != null) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			}
		} catch (IOException e) {
			Toast.makeText(this, "Error copying output file", Toast.LENGTH_SHORT).show();
		}
	}

	private void signApk(File inputFile) {
		APKSigner signer = new APKSigner(this);
		File tempOutput;
		try {
			tempOutput = File.createTempFile("temp_apk", ".apk", getCacheDir());

			Log.v("ApkSigner", "File output: "+tempOutput.getPath());
			signer.sign(inputFile, new File(tempOutput.getPath()));

			this.tempOutput = tempOutput;

		} catch (Exception e) {
			Toast.makeText(this, "There was an error signing the app.", Toast.LENGTH_LONG).show();

			Log.e(TAG, "Signing error:"+e.getMessage());
		}
	}
}
