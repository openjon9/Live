package com.coder.live.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coder.live.Activity.JoinLiveGroupActivity;
import com.coder.live.Class.Person;
import com.coder.live.R;

import java.util.List;

/**
 * Created by Rey on 2018/6/28.
 */

public class MyAdapter2 extends BaseAdapter {


    private  Context context;
    private List<Person> list;

    public MyAdapter2(Context c, List<Person> mlist) {
        list = mlist;
        context = c;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyTag myTag = null;
        if (convertView == null) {
            myTag = new MyTag();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitem, null);
            myTag.linearLayout = (LinearLayout) convertView.findViewById(R.id.linear);
            myTag.tv1 = (TextView) convertView.findViewById(R.id.listtext1);
            myTag.tv2 = (TextView) convertView.findViewById(R.id.listtext2);
            convertView.setTag(myTag);
        } else {
            myTag = (MyTag) convertView.getTag();
        }
        myTag.tv1.setText(list.get(position).getName()+":");
        myTag.tv2.setText(list.get(position).getValue());


        return convertView;
    }

    class MyTag {
        LinearLayout linearLayout;
        TextView tv1;
        TextView tv2;

    }

}
