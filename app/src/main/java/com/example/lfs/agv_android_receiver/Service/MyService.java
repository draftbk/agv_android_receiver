package com.example.lfs.agv_android_receiver.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.example.lfs.agv_android_receiver.Activity.MainActivity;
import com.example.lfs.agv_android_receiver.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {
    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is;
    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;
    // 接收服务器发送过来的消息
    String response;
    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    OutputStream outputStream;
    // 线程池
    private ExecutorService mThreadPool;
    // Socket变量
    private Socket socket;
    // 接收的text
    private String receText="";
    public MyService() {
    }
    private MySocketBinder mySocketBinder=new MySocketBinder();
    public class MySocketBinder extends Binder{
        public String sendMessage(final String message){
            //如果socket不连接则不执行
            if (!socket.isConnected()){
                return "请连接";
            }
            // 利用线程池直接开启一个线程 & 执行该线程
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 步骤1：从Socket 获得输出流对象OutputStream
                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                        outputStream.write((message+"\n").getBytes("utf-8"));
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            return "正在发送";
        }
        public void startSocket(final String ip, final int port, final Handler handler){

            Log.e("MyService","startSocket");
            // 利用线程池直接开启一个线程 & 执行该线程
                    mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            int tag=0;
                            try {
                                // 创建Socket对象 & 指定服务端的IP 及 端口号
                                socket = new Socket(ip, port);
                                // 判断客户端和服务器是否连接成功
                                Log.e("MyService","connectState: "+socket.isConnected());
                                if(socket!=null&&socket.isConnected()){
                                    Message msg_connect = new Message();
                                    msg_connect.what=2;
                                    msg_connect.obj="connected";
                                    handler.sendMessage(msg_connect);
                                    //吧out也在这里初始化
                                    outputStream = socket.getOutputStream();
                                    // 步骤1：创建输入流对象InputStream
                                    is = socket.getInputStream();
                                    // 步骤2：创建输入流读取器对象 并传入输入流对象
                                    // 该对象作用：获取服务器返回的数据
                                    isr = new InputStreamReader(is);
                                    br = new BufferedReader(isr);
                                    while(true){
                                        tag=1;
                                        System.out.println("这里");
                                        // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                                        response = br.readLine();
                                        // 步骤4:通知主线程,将接收的消息显示到界面
                                        System.out.println("这里2");
                                        Message msg = new Message();
                                        msg.what=0;
                                        msg.obj=response;
                                        handler.sendMessage(msg);
                                    }
                                }

                            } catch (Exception e) {
                                if (tag==0){
                                    Message msg_connect = new Message();
                                    msg_connect.what=2;
                                    msg_connect.obj="unconnected";
                                    handler.sendMessage(msg_connect);
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mySocketBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        init();
        Log.e("MyService","myService onCreate");
        Intent notiIntent =new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivities(this,0, new Intent[]{notiIntent},0);
        Notification notification=new Notification.Builder(this)
                .setContentTitle("CUHKSZ").setContentText("开始监听AGV").setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi).build();
        startForeground(1,notification);
    }

    private void init() {
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MyService","myService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MyService","myService onDestroy");
        if(socket!=null&&socket.isConnected()){
            try {
                // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
                outputStream.close();
                // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
                br.close();
                // 最终关闭整个Socket连接
                socket.close();
                // 判断客户端和服务器是否已经断开连接
                System.out.println(socket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
