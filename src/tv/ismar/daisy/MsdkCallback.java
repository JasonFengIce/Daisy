package tv.ismar.daisy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.tencent.msdk.api.ADRet;
import com.tencent.msdk.api.CardRet;
import com.tencent.msdk.api.GroupRet;
import com.tencent.msdk.api.LocationRet;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.ShareRet;
import com.tencent.msdk.api.TokenRet;
import com.tencent.msdk.api.WGADObserver;
import com.tencent.msdk.api.WGGroupObserver;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
import com.tencent.msdk.api.WakeupRet;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.TokenType;
import com.tencent.msdk.myapp.autoupdate.WGSaveUpdateObserver;
import com.tencent.msdk.remote.api.RelationRet;
import com.tencent.msdk.tools.Logger;
import com.tencent.tmassistantbase.common.TMAssistantDownloadTaskState;
import com.tencent.tmselfupdatesdk.model.TMSelfUpdateUpdateInfo;

/** 
 * TODO GAME 游戏需要根据自己的逻辑实现自己的MsdkCallback对象。 
 * MSDK通过WGPlatformObserver抽象类中的方法将授权、分享或查询结果回调给游戏。
 * 游戏根据回调结果调整UI等。只有设置回调，游戏才能收到MSDK的响应。
 * 这里是Java层回调(设置了Java层回调会优先调用Java层回调, 如果要使用C++层回调则不能设置Java层回调)
 */
public class MsdkCallback implements WGPlatformObserver { 
    public static BaseActivity mainActivity;
    private static LocalBroadcastManager lbm;
    public static final String LOCAL_ACTION = "com.example.wegame";
    
    public  MsdkCallback(BaseActivity activity) {
    	mainActivity =  activity;
    	lbm = LocalBroadcastManager.getInstance(mainActivity.getApplicationContext());
    }
    
    // 发送结果到结果展示界面
    public static void sendResult(String result) {
    	if(lbm != null) {
    		Intent sendResult = new Intent(LOCAL_ACTION);
            sendResult.putExtra("Result", result);
            Logger.d("send: "+ result);
            lbm.sendBroadcast(sendResult);
    	}
    }
    
    public void OnLoginNotify(LoginRet ret) {
        Logger.d("called");
        Logger.d("ret.flag" + ret.flag);
        switch (ret.flag) {
            case CallbackFlag.eFlag_Succ:
            //	mainActivity.stopWaiting();
                // 登陆成功, 读取各种票据
                String openId = ret.open_id;

                String pf = ret.pf;
                String pfKey = ret.pf_key;
               // MainActivity.platform = ret.platform;
                String qqAccessToken = "";
                long qqAccessTokenExpire = 0;
                String qqRefreshToken = "";
                long qqRefreshTokenExpire = 0;
                for (TokenRet tr : ret.token) {
                    switch (tr.type) {
                        case TokenType.eToken_WX_Access:
                            qqAccessToken = tr.value;
                            qqAccessTokenExpire = tr.expiration;
                            break;
                        case TokenType.eToken_WX_Refresh:
                            qqRefreshToken = tr.value;
                            qqRefreshTokenExpire = tr.expiration;
                            break;
                        case TokenType.eToken_QQ_Access:
                            qqAccessToken = tr.value;
                            qqAccessTokenExpire = tr.expiration;
                            break;
                        case TokenType.eToken_QQ_Pay:
                            qqRefreshToken = tr.value;
                            qqRefreshTokenExpire = tr.expiration;
                            break;
                        default:
                            break;
                    }
                }

                Log.i("pangziinfo","openId="+openId+"//qqAccessToken="+qqAccessToken+"//qqRefreshToken=="+qqRefreshToken+"appid="+"100703379"+"appkey");
                mainActivity.letUserLogin(qqAccessToken,qqRefreshToken,openId);
                break;
            // 游戏逻辑，对登陆失败情况分别进行处理
            case CallbackFlag.eFlag_Login_NetworkErr:
            case CallbackFlag.eFlag_WX_UserCancel:
            case CallbackFlag.eFlag_WX_NotInstall:
            case CallbackFlag.eFlag_WX_NotSupportApi:
            case CallbackFlag.eFlag_WX_LoginFail:
            case CallbackFlag.eFlag_QQ_LoginFail:
            case CallbackFlag.eFlag_Local_Invalid:
            	Logger.d(ret.desc);
            default:
                // 显示登陆界面
               // mainActivity.stopWaiting();
               // mainActivity.letUserLogout();
                break;
        }
    }

    public void OnShareNotify(ShareRet ret) {
        Logger.d("called");
        String result = "";
        switch (ret.flag) {
            case CallbackFlag.eFlag_Succ:
                // 分享成功
            	result = "Share success\n" + ret.toString();
                break;
            case CallbackFlag.eFlag_QQ_UserCancel:
            case CallbackFlag.eFlag_WX_UserCancel:
            case CallbackFlag.eFlag_WX_NotInstall:
            case CallbackFlag.eFlag_WX_NotSupportApi:
                
            default:
            	// 分享失败处理
                Logger.d(ret.desc);
                result = "Share faild: \n" + ret.toString();
                break;
        }
        // 发送结果到结果展示界面
        sendResult(result);
    }

    public void OnWakeupNotify(WakeupRet ret) {
        Logger.d("called");
        Logger.d(ret.toString() + ":flag:" + ret.flag);
        Logger.d(ret.toString() + "desc:" + ret.desc);
        Logger.d(ret.toString() + "platform:" + ret.platform);
       // MainActivity.platform = ret.platform;
        // TODO GAME 游戏需要在这里增加处理异账号的逻辑
        if (CallbackFlag.eFlag_Succ == ret.flag
                || CallbackFlag.eFlag_AccountRefresh == ret.flag) {
            //eFlag_AccountRefresh代表 刷新微信票据成功
            Logger.d("login success flag:" + ret.flag);
            //mainActivity.letUserLogin();
        } else if (CallbackFlag.eFlag_UrlLogin == ret.flag) {
            // 用拉起的账号登录，登录结果在OnLoginNotify()中回调
        } else if (ret.flag == CallbackFlag.eFlag_NeedSelectAccount) {
            // 异账号时，游戏需要弹出提示框让用户选择需要登陆的账号
            Logger.d("diff account");
            //mainActivity.showDiffLogin();
        } else if (ret.flag == CallbackFlag.eFlag_NeedLogin) {
            // 没有有效的票据，登出游戏让用户重新登录
            Logger.d("need login");
           // mainActivity.letUserLogout();
        } else {
            Logger.d("logout");
           // mainActivity.letUserLogout();
        }
    }

    @Override
    public void OnRelationNotify(RelationRet relationRet) {
    	String result = relationRet.toString();
        Logger.d("OnRelationNotify" + result);
        // 发送结果到结果展示界面
        sendResult(result);
    }

    @Override
    public void OnFeedbackNotify(int flag, String desc) {
    	String result = String.format(Locale.CHINA, "flag: %d; desc: %s;", flag, desc);
        Logger.d(result);
        sendResult(result);
    }

    @Override
    public String OnCrashExtMessageNotify() {
        // 此处游戏补充crash时上报的额外信息
        Logger.d(String.format(Locale.CHINA, "OnCrashExtMessageNotify called"));
        Date nowTime = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return "new Upload extra crashing message for bugly on " + time.format(nowTime);
    }
    
    @Override
    public void OnLocationNotify(RelationRet relationRet) {
        Logger.d(relationRet);
        sendResult(relationRet.toString());
    }
    
	@Override
	public void OnLocationGotNotify(LocationRet locationRet) {
		Logger.d(locationRet.toString());
		String result = "flag: " + locationRet.flag + "\n" +
				"platform: " + locationRet.platform + "\n" +
				"longitude: " + locationRet.longitude + "\n" +
				"latitude: " + locationRet.latitude;
		sendResult(result);
	}

	@Override
	public void OnAddWXCardNotify(CardRet ret) {
		Logger.d(ret.toString());
		if(CallbackFlag.eFlag_Succ == ret.flag){
			sendResult("插卡请求成功");
			Logger.d(ret.toString());
		}else{
			sendResult("插卡失败，系统其他错误");
			Logger.d(ret.toString());
		}
	}
    
}

class SaveUpdateDemoObserver extends WGSaveUpdateObserver{
    @Override
    public void OnCheckNeedUpdateInfo(long newApkSize, String newFeature, long patchSize,
            final int status, String updateDownloadUrl, final int updateMethod) {
        Logger.d("called");
        String statusDesc = "";
        switch (status) {
            case TMSelfUpdateUpdateInfo.STATUS_OK:
                // 查询更新成功
                statusDesc = "Check success!";
                break;

            case TMSelfUpdateUpdateInfo.STATUS_CHECKUPDATE_RESPONSE_IS_NULL:
                // 查询响应为空
                statusDesc = "Response is null!";
                break;

            case TMSelfUpdateUpdateInfo.STATUS_CHECKUPDATE_FAILURE:
                // 查询更新失败
                statusDesc = "CheckNeedUpdate FAILURE!";
                break;
        }
        if(status == TMSelfUpdateUpdateInfo.STATUS_OK) {
	        switch(updateMethod) {
	        	case TMSelfUpdateUpdateInfo.UpdateMethod_NoUpdate:
	        		// 无更新包
	        		statusDesc += "But no update package.";
	        		break;
	        	case TMSelfUpdateUpdateInfo.UpdateMethod_Normal:
	        		// 有全量更新包
	        		statusDesc += "Common package is available.";
	        		break;
	        	case TMSelfUpdateUpdateInfo.UpdateMethod_ByPatch:
	        		// 有省流量更新包
	        		statusDesc += "Save update package is available.";
	        		break;
	        	default :
	        	    statusDesc += "Happen error!";
	        	    break;
	        }
        }
        Logger.d(statusDesc);
        MsdkCallback.sendResult(statusDesc);
    }

    @Override
    public void OnDownloadAppProgressChanged(final long receiveDataLen, final long totalDataLen) {
    	// 下载游戏进度由此回调，游戏可根据回调的参数做进度表展示
        Logger.d("totalData:" + totalDataLen + "receiveData:" + receiveDataLen);
        MsdkCallback.mainActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//MainActivity.mProgressDialog.setMax((int)(totalDataLen/1024));
	           // MainActivity.mProgressDialog.setProgress((int)(receiveDataLen/1024));
			}
        	
        });
        
    }

    @Override
    public void OnDownloadAppStateChanged(int state, int errorCode, String errorMsg) {
        // 下载状态由此回调
        String result = "";
        switch (state) {
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_SUCCEED:
                // 应用宝内的游戏下载任务完成, 更新完成继续游戏
                result = "state: succeed";
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_DOWNLOADING:
                // 应用宝内的游戏下载中, 游戏提示等待动画或者结合OnDownloadAppProgressChanged显示下载进度
                result = "state: downloading";
                break;
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_WAITING:
                // 应用宝内的游戏下载任务等待中, 提示用户等待
                result = "state: waiting";
                break;
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_PAUSED:
                result = "state: paused";
                break;
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_FAILED:
                // 详细错误码在errorCode中, 错误码定义在TMAssistantDownloadSDKErrorCode中以DownloadSDKErrorCode开头的属性中
                result = "state: failed";
                break;
            case TMAssistantDownloadTaskState.DownloadSDKTaskState_DELETE:
                result = "state: delete";
                break;
            default :
                result = "state: " + state;
        } 
        result += "\nerrorCode:" + errorCode + "\nerrorMsg:" + errorMsg; 
        Logger.d(result);
        if(state != TMAssistantDownloadTaskState.DownloadSDKTaskState_DOWNLOADING &&
                state != TMAssistantDownloadTaskState.DownloadSDKTaskState_WAITING &&
                state != TMAssistantDownloadTaskState.DownloadSDKTaskState_PAUSED)
//            if (MainActivity.mProgressDialog != null && MainActivity.mProgressDialog.isShowing())
//                MainActivity.mProgressDialog.dismiss();
            MsdkCallback.sendResult(result);
    }
    
    /**
     * 省流量更新(WGStartSaveUpdate)，当没有安装应用宝时，会先下载应用宝, 此为下载应用宝包的进度回调
     * @param url 当前任务的url
     * @param receiveDataLen 已经接收的数据长度
     * @param totalDataLen 全部需要接收的数据长度（如果无法获取目标文件的总长度，此参数返回 －1）
     */
    @Override
    public void OnDownloadYYBProgressChanged(String url, final long receiveDataLen, final long totalDataLen) {
    	// 下载应用宝进度由此回调，游戏可根据回调的参数做进度表展示
    	Logger.d("totalData:" + totalDataLen + "receiveData:" + receiveDataLen);
    }
    
    /**
     * @param url 指定任务的url
     * @param state 下载状态: 取值 TMAssistantDownloadSDKTaskState.DownloadSDKTaskState_*
     * @param errorCode 错误码
     * @param errorMsg 错误描述，有可能为null
     */
    @Override
    public void OnDownloadYYBStateChanged(final String url, final int state, final int errorCode, final String errorMsg) {
         Logger.d("called");
         String result = "OnDownloadYYBStateChanged " + "\nstate:" + state + 
         		"\nerrorCode:" + errorCode + "\nerrorMsg:" + errorMsg; 
         Logger.d(result);
         MsdkCallback.sendResult(result);
    }
}

// 点击广告按钮的回调
class MsdkADCallback implements WGADObserver {

	@Override
	public void OnADNotify(ADRet ret) {
		Logger.d("MsdkADCallback OnADNotify:" + ret.toString());
        // TODO GAME 这里增加广告回调的处理
		
		MsdkCallback.sendResult(ret.toString());
	}
	
	// 在广告界面按返回键时，此处会有回调
	@Override
	public void OnADBackPressedNotify(ADRet ret) {
		Logger.d("MsdkADCallback OnADBackPressedNotify:" + ret.toString());
        // TODO GAME 如果按返回键关闭广告的话，则需要调用close方法
        WGPlatform.WGCloseAD(ret.scene);
	}
    
}

//加群加好友回调
class MsdkGroupCallback implements WGGroupObserver {

	@Override
	public void OnQueryGroupInfoNotify(GroupRet groupRet) {
		//TODO GAME 增加查询群信息的回调
		Logger.d("flag:"+ groupRet.flag + ";errorCode："+ groupRet.errorCode + ";desc:" + groupRet.desc);
		if(CallbackFlag.eFlag_Succ == groupRet.flag){
			//游戏可以在会长公会界面显解绑按钮，非工会会长显示进入QQ群按钮
			MsdkCallback.sendResult("查询成功。\n群昵称为："+groupRet.getGroupInfo().groupName 
					+"\n群openID:"+groupRet.getGroupInfo().groupOpenid 
					+"\n加群Key为："+groupRet.getGroupInfo().groupKey);
		}else{
			if(2002 == groupRet.errorCode){
				//游戏可以在会长公会界面显示绑群按钮，非会长显示尚未绑定
				MsdkCallback.sendResult("查询失败，当前公会没有绑定记录！");
			}else if(2003 == groupRet.errorCode){
				//游戏可以在用户公会界面显示加群按钮
				MsdkCallback.sendResult("查询失败，当前用户尚未加入QQ群，请先加入QQ群！");
			}else if(2007 == groupRet.errorCode){
				//游戏可以在用户公会界面显示加群按钮
				MsdkCallback.sendResult("查询失败，QQ群已经解散或者不存在！");
			}else{
				//游戏可以引导用户重试
				MsdkCallback.sendResult("查询失败，系统错误，请重试！");
			}
		}
		
	}

	@Override
	public void OnBindGroupNotify(GroupRet groupRet) {
		//TODO GAME 增加绑定QQ群的回调
		Logger.d("flag:"+ groupRet.flag + ";errorCode："+ groupRet.errorCode + ";desc:" + groupRet.desc);
		if(CallbackFlag.eFlag_Succ == groupRet.flag){
			//游戏可以去查询绑定的公会的相关信息。
			//由于目前手QSDK尚不支持，因此无论绑定是否成功，MSDK都会给游戏一个成功的回调，游戏收到回调以后需要调用查询接口确认绑定是否成功
			MsdkCallback.sendResult("绑定成功。");
		}else{
			//游戏可以引导用户重试
			MsdkCallback.sendResult("绑定失败，系统错误，请重试！");
		}
	}

	@Override
	public void OnUnbindGroupNotify(GroupRet groupRet) {
		//TODO GAME 增加解绑QQ群的回调
		Logger.d("flag:"+ groupRet.flag + ";errorCode："+ groupRet.errorCode + ";desc:" + groupRet.desc);
		if(CallbackFlag.eFlag_Succ == groupRet.flag){
			//解绑成功，游戏可以提示用户解绑成功，并在工会会长界面显示绑群按钮，非会长界面显示尚未绑定按钮
			MsdkCallback.sendResult("解绑成功。");
		}else{
			if(2001 == groupRet.errorCode){
				//解绑用的群openID没有群绑定记录，游戏重新调用查询接口查询绑定情况
				MsdkCallback.sendResult("解绑失败，当前QQ群没有绑定记录！");
			}else if(2003 == groupRet.errorCode){
				//用户登录态过期，重新登陆
				MsdkCallback.sendResult("解绑失败，用户登录态过期，请重新登陆！");
			}else if(2004 == groupRet.errorCode){
				//操作太过频繁，让用户稍后尝试
				MsdkCallback.sendResult("解绑失败，操作太过频繁，让用户稍后尝试！");
			}else if(2005 == groupRet.errorCode){
				//解绑参数错误，游戏重新调用查询接口查询绑定情况
				MsdkCallback.sendResult("解绑失败，操解绑参数错误！");
			}else{
				//游戏可以引导用户重试
				MsdkCallback.sendResult("解绑失败，系统错误，请重试！");
			}
		}
	}

	@Override
	public void OnQueryQQGroupKeyNotify(GroupRet groupRet) {
		//TODO GAME 增加查询群信息的回调
		Logger.d("flag:"+ groupRet.flag + ";errorCode："+ groupRet.errorCode + ";desc:" + groupRet.desc);
		if(CallbackFlag.eFlag_Succ == groupRet.flag){
			//成功获取到加群用的key，可以进一步使用key加入QQ群
			MsdkCallback.sendResult("查询成功。\n加群Key为："+groupRet.getGroupInfo().groupKey);
		}else{
			//游戏可以引导用户重试
			MsdkCallback.sendResult("查询失败，请重试！");
		}
	}
}
