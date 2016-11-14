package com.anyonavinfo.bluetoothphone.bpclient.adapter;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyonavinfo.bluetoothphone.R;
import com.anyonavinfo.bluetoothphone.bpclient.MainActivity;
import com.anyonavinfo.bluetoothphone.bpclient.bean.MyPhoneCall;
import com.anyonavinfo.bluetoothphone.bpservice.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;



public class RecordAdapter extends BaseAdapter {
    private List<MyPhoneCall> list;
    private Context mContext;
    private boolean visibility = false;

    public RecordAdapter(Context mContext, List<MyPhoneCall> list) {
        this.mContext = mContext;
        if (list == null) {
            this.list = new ArrayList<MyPhoneCall>();
        } else {
            this.list = list;
        }
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<MyPhoneCall> list) {
        if (list == null) {
            list = new ArrayList<MyPhoneCall>();
        }
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 调用adapter中的更新的数据
     */
    public List<MyPhoneCall> getData() {
        return list;
    }

    /**
     * 设置checkbox显示或隐藏的方法
     */
    public void setCBVisibility(Boolean visibility) {
        this.visibility = visibility;
        if (visibility) {
            notifyDataSetChanged();
        }
    }


    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        MyPhoneCall phoneCall = list.get(position);
        ViewHolder viewHolder;
        final int checkedIndex = position;/**初始化checkbox的监听位置*/
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_record, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.record_name);
            viewHolder.tvNumb = (TextView) view.findViewById(R.id.record_number);
            viewHolder.tvTime = (TextView) view.findViewById(R.id.record_time);
            viewHolder.tvAddress = (TextView) view.findViewById(R.id.record_address);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.record_checked);
            viewHolder.ivType = (ImageView) view.findViewById(R.id.record_type);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (visibility) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(visibility){
                        list.get(checkedIndex).setChecked(isChecked);
                        int i=0;
                        for(MyPhoneCall call :list){
                            if(call.isChecked()){
                                i++;
                            }
                        }
                        Message msg = new Message();
                        msg.what=0x3001;
                        msg.arg1=i;
                        ((MainActivity) mContext).sendMessage(msg);
                    }
                }
            });
        } else {
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
        }

        if (list.get(position).isChecked()) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }

        //时间显示转换是否完成？
        viewHolder.tvName.setText(phoneCall.getCallName());
        viewHolder.tvNumb.setText(phoneCall.getCallNumber());
        if (phoneCall!=null){
            viewHolder.tvTime.setText(TimeUtils.getRecentTime(phoneCall.getCallTime()));
        }
        if(phoneCall.getCallPlace()!=null){
            viewHolder.tvAddress.setText(phoneCall.getCallPlace());
        }else {
            viewHolder.tvAddress.setText("归属地未知");
        }

        //1 拨打未通,2 拨打成功,3 未接、拒接 4 接听成功
        if(phoneCall.getCallType()==1){
            viewHolder.ivType.setImageResource(R.drawable.huchushibai);
        }else if(phoneCall.getCallType()==2){
            viewHolder.ivType.setImageResource(R.drawable.huchu);
        }else if(phoneCall.getCallType()==3){
            viewHolder.ivType.setImageResource(R.drawable.weijie);
        }else if(phoneCall.getCallType()==4){
            viewHolder.ivType.setImageResource(R.drawable.huru);
        }

        viewHolder.checkBox.setFocusable(false);
        return view;
    }

    final static class ViewHolder {
        TextView tvName;
        TextView tvNumb;
        TextView tvTime;
        TextView tvAddress;
        CheckBox checkBox;
        ImageView ivType;
    }
}