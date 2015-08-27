package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;

import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.eADType;
import com.tencent.msdk.api.eQQScene;
import com.tencent.msdk.api.eWechatScene;
import com.tencent.msdk.notice.NoticeInfo;

public class PlatformTest {
	public static native void WGSendToWeixin(String title, String desc,
			String mediaTagName, byte[] imgData, int imgDataLen,
			String messageExt);

	public static native void WGSendToWeixinWithPhoto(eWechatScene scene,
			String mediaTagName, byte[] imgData, int imgDataLen);

	public static native void WGSendToWeixinWithPhoto(eWechatScene scene,
			String mediaTagName, byte[] imgData, int imgDataLen,
			String messageExt, String messageAction);
	
	public static native void WGSendToWeixinWithPhotoPath(eWechatScene scene, String mediaTagName,
            String imgPath, String messageExt, String mediaAction); 

	public static native void WGSendToWeixinWithMusic(eWechatScene scene,
			String title, String desc, String musicUrl, String musicDataUrl,
			String mediaTagName, byte[] imgData, int imgDataLen,
			String mediaExt, String mediaAction);

	public static native void WGSendToQQ(eQQScene scene, String title,
			String desc, String url, String imgUrl, int imgUrlLen);

	public static native void WGSendToQQWithPhoto(eQQScene scene,
			String imgFilePath);

	public static native void WGSendToQQWithMusic(eQQScene scene, String title,
			String desc, String musicUrl, String musicDataUrl, String imgUrl);

	public static native void WGLogin(int Platform);

	public static native boolean WGSwitchUser(boolean switchToLaunchUser);

	public static native boolean WGLogout();

	public static native int WGGetLoginRecord(LoginRet ret);

	public static native boolean WGFeedBack(String game, String txt);

	public static native void WGFeedBack(String body);

	public static native void throwExp();

	public static native void setObserver(boolean flag);

	public static native void WGEnableCrashReport(boolean bRdmEnable,
			boolean bMtaEnable);

	public static native void WGReportEvent(String name, String body,
			boolean isRealTime);

	public static native void WGReportEvent(String name,
			HashMap<String, String> params, boolean isRealTime);

	public static native void WGTestSpeed(ArrayList<String> addrList);

	public static native String WGGetChannelId();

	public static native String WGGetVersion();

	public static native void WGSetPermission(int permissions);

	public static native boolean WGIsPlatformInstalled(int platform);

	public static native boolean WGIsPlatformSupportApi(int platform);

	public static native String WGGetRegisterChannelId();

	public static native void WGRefreshWXToken();

	public static native String WGGetPf(String gameCustomInfo);

	public static native String WGGetPfKey();

	public static native void WGLogPlatformSDKVersion();

	public static native boolean WGSendToQQGameFriend(int act,
			String friendOpenId, String title, String summary,
			String targetUrl, String imageUrl, String previewText,
			String gameTag);

	public static native boolean WGSendToWXGameFriend(String fopenid,
			String title, String description, String thumb_media_id,
			String extinfo, String media_tag_name);

	public static native boolean WGQueryQQMyInfo();

	public static native boolean WGQueryQQGameFriendsInfo();

	public static native boolean WGQueryWXMyInfo();

	public static native boolean WGQueryWXGameFriendsInfo();

	public static native boolean WGCheckApiSupport(int api);

	public static native void SetActivity(Activity activity);

	public static native Vector<NoticeInfo> WGGetNoticeData(String scene);

	public static native void WGShowNotice(String scene);

	public static native void WGHideScrollNotice();

	public static native void WGOpenUrl(String openUrl);

	public static native void WGOpenAmsCenter(String url);

	public static native void WGLoginWithLocalInfo();

	public static native void WGGetNearbyPersonInfo();

	public static native boolean WGCleanLocation();

	public static native boolean WGGetLocationInfo();

	public static native boolean WGSendMessageToWechatGameCenter();

	public static native void WGStartSaveUpdate(boolean isUseYYB);

	public static native void WGSetSaveUpdateObserver();

	public static native void WGCheckNeedUpdate();

	public static native int WGCheckYYBInstalled();

	public static native void WGSetGameEngineType(String gameEngineInfo);

	public static native void WGSetADObserver();

	public static native void WGShowAD(eADType scene);

	public static native void WGCloseAD(eADType scene);

	public static native void WGJoinQQGroup(String qqGroupKey);

	public static native void WGBindQQGroup(String unionid, String union_name,
			String zoneid, String signature);

	public static native void WGAddGameFriendToQQ(String fopenid, String desc,
			String message);

	public static native void WGSendToWeixinWithUrl(
			eWechatScene wechatsceneTimeline, String title, String desc,
			String url, String mediaTagName, byte[] imgData, int length,
			String messageExt);

	public static native String WGGetPlatformAPPVersion(int platform);

	public static native void WGQueryQQGroupInfo(String unionid, String zoneid);

	public static native void WGUnbindQQGroup(String groupOpenid, String unionid);

	public static native void WGAddCardToWXCardPackage(String fopenid, String desc, String message);

	public static native void WGQueryQQGroupKey(String groupOpenID);
	
	public static native void WGOpenWeiXinDeeplink(String link);
	
}
