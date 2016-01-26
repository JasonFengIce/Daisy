package com.qiyi.video.utils;

import android.util.Log;

//import android.util.Log;

public class LogUtils
{
	public static boolean mIsDebug = true;

    public LogUtils()
    {
    }

    public static void setDebug(boolean isDebug)
    {
    	mIsDebug = isDebug;
    }

    public static void d(String s, Object object)
    {
    	Log.v(s, "contentaaaaa :"+object.toString());
    }

    public static void i(String s, Object object)
    {
    	Log.v(s, "content :"+object.toString());
    }

    public static void w(String tag, Object object)
    {
    	System.out.println("tag="+tag+"****"+object.toString());
    }

    public static void e(String tag, Object object)
    {
    	System.out.println("tag="+tag+"****"+object.toString());
    }

    public static void d(String s, Object object, Throwable t)
    {
    	System.out.println("s="+s+"****"+object.toString());
    }

    public static void i(String s, Object object, Throwable t)
    {
    	Log.v(s, "content :"+object.toString());
    }

    public static void w(String tag, Object object, Throwable t)
    {
    	System.out.println("tag="+tag+"****"+object.toString());
    }

    public static void e(String tag, Object object, Throwable t)
    {
    	System.out.println("tag="+tag+"****"+object.toString());
    }

}