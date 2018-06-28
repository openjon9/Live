package com.coder.live.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coder.live.Activity.LiveActivity;
import com.coder.live.Class.Person;
import com.coder.live.R;

import java.util.List;

/**
 * Created by Rey on 2018/6/28.
 */

public class MyAdapter3 extends BaseAdapter {


    private List<Person> list;
    private LiveActivity context;
    private MyTag myTag;
    private int mcolor;

    public MyAdapter3(LiveActivity c, List<Person> mlist) {
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

        if (list.get(position).getColor() == 0) {
            myTag.tv1.setTextColor(Color.RED);
            myTag.tv2.setTextColor(Color.RED);
        } else {
            myTag.tv1.setTextColor(Color.YELLOW);
            myTag.tv2.setTextColor(Color.YELLOW);
        }
        myTag.tv1.setText(list.get(position).getName() + ":");
        myTag.tv2.setText(list.get(position).getValue());

        return convertView;
    }


    class MyTag {
        LinearLayout linearLayout;
        TextView tv1;
        TextView tv2;

    }
}
