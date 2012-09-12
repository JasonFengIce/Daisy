package tv.ismar.daisy.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class SystemFileUtil {

	public static void readFile(String filePath, Context context) {

		FileInputStream istream;
		try {
			int len = -1;
			istream = context.openFileInput(filePath);
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			while ((len = istream.read(buffer)) != -1) {
				ostream.write(buffer, 0, len);
			}
			istream.close();

			ostream.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeFile(String content, String filePath,
			Context context) {

		FileOutputStream fos;
		try {
			fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeSDCardFile(String content, String filePath) {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdCardDir = Environment.getExternalStorageDirectory();

			File saveFile = new File(sdCardDir, filePath);

			FileOutputStream outStream;
			try {
				outStream = new FileOutputStream(saveFile);
				outStream.write("Hi,I’m ChaoYu".getBytes());
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}