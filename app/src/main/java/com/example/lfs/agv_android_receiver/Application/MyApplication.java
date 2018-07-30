package com.example.lfs.agv_android_receiver.Application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by lfs on 2018/6/28.
 */

public class MyApplication extends Application{
    public static String connectIP="192.168.0.1";
    public static int connectPort=8087;
    public static String selfIP="192.168.0.1";
    public static String workerId="000000";
    public static String cancelId="";

    public static void initIp(Context context){
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、取出数据
        String ip = dates.getString("ip","192.168.0.1");
        connectIP=ip;
        String port = dates.getString("port","2000");
        connectPort=Integer.parseInt(port);
    }
    public static String getIp(Context context){
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、取出数据
        String ip = dates.getString("ip",connectIP);
        return ip;
    }
    public static void savePort(Context context, String port){
        connectPort=Integer.parseInt(port);
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、让setting处于编辑状态
        SharedPreferences.Editor editor = dates.edit();
        //3、存放数据
        editor.putString("port",port);
        //4、完成提交
        editor.commit();
    }

    public static void saveIp(Context context, String ip){
        connectIP=ip;
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、让setting处于编辑状态
        SharedPreferences.Editor editor = dates.edit();
        //3、存放数据
        editor.putString("ip",ip);
        //4、完成提交
        editor.commit();
    }

    /***
     * 使用WIFI时，获取本机IP地址
     * @param mContext
     * @return
     */
    public static void getWIFILocalIpAdress(Context mContext) {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        selfIP=ip;
    }
    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }

}
