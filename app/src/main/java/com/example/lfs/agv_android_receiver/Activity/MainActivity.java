package com.example.lfs.agv_android_receiver.Activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lfs.agv_android_receiver.Adapter.TaskAdapter;
import com.example.lfs.agv_android_receiver.Application.MyApplication;
import com.example.lfs.agv_android_receiver.Model.MapPoint;
import com.example.lfs.agv_android_receiver.Model.Task;
import com.example.lfs.agv_android_receiver.R;
import com.example.lfs.agv_android_receiver.Service.MyService;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Switch switchShop;
    private Handler handler;
    private MyService.MySocketBinder mySocketBinder;
    private ServiceConnection connection;
    private TextView textContent,textRemark,textTitle;
    private ListView taskListView;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;
    private int taskMaxNumber=10;
    private int myPosition,tempChoose;
    private List<MapPoint> pointList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
        init();
    }
    private void initService() {
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mySocketBinder= (MyService.MySocketBinder) service;
                mySocketBinder.startSocket(MyApplication.connectIP, MyApplication.connectPort,handler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private void init() {
        //从数据库获得position
        myPosition=MyApplication.getMyPostion(MainActivity.this);
        pointList=new ArrayList<MapPoint>();
        getPointFromSql();
        textTitle=findViewById(R.id.text_title);
        textContent=findViewById(R.id.text_content);
        textRemark=findViewById(R.id.text_remark);
        taskListView = findViewById(R.id.list_task);
        initListView();
        // 初始化handler
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0){
                    showToast("收到信息"+msg.obj.toString());
                    String[] message=msg.obj.toString().split(",");
                    //处理发送任务后的返回信息
                    if (message[0].equals("s20000")){
                        if (taskList.size()>=taskMaxNumber){
                            taskList.remove(taskMaxNumber-1);
                        }
                        Task task=new Task(message[1],message[2]);
                        task.setRemark(message[3]);
                        taskList.add(0,task);
                    }
                    if (taskList.size()>0){
                        textTitle.setText(taskList.get(0).getAgvId()+"号机器人即将到达");
                        textContent.setText(taskList.get(0).getContent());
                        textRemark.setText(taskList.get(0).getRemark());
                    }
                    taskAdapter.notifyDataSetChanged();
                } else if (msg.what==1){
                    switchShop.performClick();
                }else if (msg.what==2){
                    if (msg.obj.toString().equals("connected")){
                        showToast("连接成功");
                        //发送位置和IP信息
                        try {
                            String message="s20001"+","+pointList.get(MyApplication.myPostion).getId()+","+MyApplication.selfIP;
                            message=new String(message.getBytes("UTF-8"));
                            showToast(mySocketBinder.sendMessage(message));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }else if (msg.obj.toString().equals("unconnected")){
                        showToast("连接失败，请重试");
                        switchShop.performClick();
                    }
                }
            }
        };

    }


    private void initListView() {
        initList();
        taskAdapter = new TaskAdapter(MainActivity.this, R.layout.task_item, taskList);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });

    }

    private void initList() {
        taskList=new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }


    private void showToast(String s) {
        Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
    }

    /**
     *创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        switchShop=(Switch) menu.findItem(R.id.connect_switch).getActionView().findViewById(R.id.switchForActionBar);
        switchShop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                if (isChecked) { //开店申请
                    showToast("打开连接");
                    //获取当前自身IP，并存到Application里
                    MyApplication.getWIFILocalIpAdress(MainActivity.this);
                    //页面跳转
                    Intent startService=new Intent(MainActivity.this,MyService.class);
                    startService(startService);
                    Intent bindIntent=new Intent(MainActivity.this,MyService.class);
                    //绑定服务
                    bindService(bindIntent,connection,BIND_AUTO_CREATE);
                } else { //关店申请
//                    showToast("关闭连接");
                    Intent stopService=new Intent(MainActivity.this,MyService.class);
                    stopService(stopService);
                    //解绑service
                    unbindService(connection);
                }
            }
        });
        Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     *菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_connect:
                if (switchShop.isChecked()){
                    showToast("先断开连接再设置ip");
                }else {
                    showSettingDialog();
                }
                break;
            case R.id.menu_point_list:
                if (switchShop.isChecked()){
                    showChoosePositionDialog();
                }else {
                    showToast("先连接再设置");
                }

                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void showSettingDialog() {
    /*@setView 装入一个EditView
     */
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.setting_dialog,null);
        inputDialog.setTitle("输入对应IP地址和端口号");
        inputDialog.setView(dialogView);
        final EditText ipEdit=dialogView.findViewById(R.id.edit_ip);
        ipEdit.setText(MyApplication.connectIP);
        final EditText portEdit=dialogView.findViewById(R.id.edit_port);
        portEdit.setText(MyApplication.connectPort+"");
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputIP=ipEdit.getText().toString();
                        String inputPort=portEdit.getText().toString();
                        Toast.makeText(MainActivity.this,
                                inputIP,
                                Toast.LENGTH_SHORT).show();
                        MyApplication.saveIp(MainActivity.this,inputIP);
                        MyApplication.savePort(MainActivity.this,inputPort);
                    }
                }).show();
    }

    private void showChoosePositionDialog(){

        //获取 items
        final String[] items = new String[pointList.size()];
        for(int i=0;i<pointList.size();i++){
            items[i]=pointList.get(i).getName();
        }
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MainActivity.this);
        singleChoiceDialog.setTitle("选择你当前所在位置");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, myPosition,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tempChoose=which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (tempChoose != -1) {
                            Toast.makeText(MainActivity.this,
                                    "你选择了" + items[tempChoose],
                                    Toast.LENGTH_SHORT).show();
                            myPosition = tempChoose;
                            MyApplication.saveMyPosition(MainActivity.this,myPosition);
                            //发送位置和IP信息
                            try {
                                String message="s20001"+","+pointList.get(MyApplication.myPostion).getId()+","+MyApplication.selfIP;
                                message=new String(message.getBytes("UTF-8"));
                                showToast(mySocketBinder.sendMessage(message));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        singleChoiceDialog.show();
    }
    //从数据库获取点信息
    public void getPointFromSql()
    {
        //在android中操作数据库最好在子线程中执行，否则可能会报异常
        new Thread()
        {
            public void run() {
                try {
                    //注册驱动
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://10.24.4.63:3306/agvsystem";
                    Connection conn = DriverManager.getConnection(url, "root", "19940829");
                    Statement stmt = conn.createStatement();
                    String sql = "select * from point";
                    ResultSet rs = stmt.executeQuery(sql);
                    // 更新 pointList
                    pointList.clear();
                    while (rs.next()) {
                        Log.e("slf", "field1-->"+rs.getInt(1)+"  field2-->"+rs.getString(2)
                                +"  field3-->"+rs.getInt(3));
                        pointList.add(new MapPoint(rs.getInt(1),rs.getInt(3),rs.getString(2)));
                    }
                    rs.close();
                    stmt.close();
                    conn.close();
                    Log.e("slf", "success to connect!");
                }catch(ClassNotFoundException e)
                {
                    Log.e("slf", "fail to connect!"+"  "+e.getMessage());
                } catch (SQLException e)
                {
                    Log.e("slf", "fail to connect!"+"  "+e.getMessage());
                }
            };
        }.start();

    }

}
