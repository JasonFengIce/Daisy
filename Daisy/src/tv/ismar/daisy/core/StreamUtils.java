package tv.ismar.daisy.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamUtils {

	/**
	 * 把输入流转换成字符数组
	 * 
	 * @param inputStream
	 *            输入流
	 * @return 字符数组
	 * @throws Exception
	 */
	public static byte[] readStream(InputStream inputStream) throws Exception {
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteOutStream.write(buffer, 0, len);
		}
		byteOutStream.close();
		inputStream.close();
		return byteOutStream.toByteArray();
	}
}
