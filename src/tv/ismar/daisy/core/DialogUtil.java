package tv.ismar.daisy.core;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 对话框工具类
 * 
 * @author YJ
 */
public class DialogUtil {
	/**
	 * 对话框没有标题
	 */
	public static final int NO_TITLE = -1;
	/**
	 * 对话框默认标题
	 */
	public static final int DEFALUT_TITLE = 0;
	/**
	 * 进度对话框对象
	 */
	private static ProgressDialog pDialog;

	/**
	 * 显示进度对话框
	 * 
	 * @param context
	 *            当前应用上下文
	 * @param msg
	 *            提示消息
	 */
	public static void showProgressDialog(Context context, String msg) {
		pDialog = new ProgressDialog(context);
		pDialog.setMessage(msg);
		pDialog.show();
	}

	/**
	 * 显示进度对话框
	 * 
	 * @param context
	 *            当前应用上下文
	 * @param resId
	 *            提示消息字符串资源ID
	 */
	public static void showProgressDialog(Context context, int resId) {
		String msg = context.getResources().getString(resId);
		showProgressDialog(context, msg);
	}

	public static void cancelProgressDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
		}
	}

	/**
	 * 确认提示框
	 * 
	 * @param context
	 *            当前应用上下文
	 * @param title
	 *            标题
	 * @param msg
	 *            提示消息
	 * @param okListener
	 *            确定选项监听器对象
	 * @param cancelListener
	 *            取消选项监听器对象
	 */
	public static void showConfirmDialog(final Context context, String title, String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
		AlertDialog.Builder builder = new Builder(context);
		if (null != title && !"".equals(title)) {
			builder.setTitle(title);
		}
		builder.setMessage(msg);
		builder.create().show();
	}

}
