package com.example.lfs.agv_android_receiver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.lfs.agv_android_receiver.Model.Task;
import com.example.lfs.agv_android_receiver.R;

import java.util.List;

/**
 * Created by lfs on 2018/7/16.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    private int resourceId;

    public TaskAdapter(Context context, int textViewResourceId,
                       List<Task> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    /*  由系统调用，获取一个View对象，作为ListView的条目，屏幕上能显示多少个条目，getView方法就会被调用多少次
     *  position：代表该条目在整个ListView中所处的位置，从0开始
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //重写适配器的getItem()方法
        Task task = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) { //若没有缓存布局，则加载
            //首先获取布局填充器，然后使用布局填充器填充布局文件
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            //存储子项布局中子控件对象
            viewHolder.taskContent = (TextView) view.findViewById(R.id.text_content);
            viewHolder.agvId = (TextView) view.findViewById(R.id.text_id);
            // 将内部类对象存储到View对象中
            view.setTag(viewHolder);
        } else { //若有缓存布局，则直接用缓存（利用的是缓存的布局，利用的不是缓存布局中的数据）
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.taskContent.setText(task.getContent());
        viewHolder.agvId.setText(task.getAgvId());

        return view;
    }

    //内部类，用于存储ListView子项布局中的控件对象
    class ViewHolder {
        TextView taskContent;
        TextView agvId;

    }

}
