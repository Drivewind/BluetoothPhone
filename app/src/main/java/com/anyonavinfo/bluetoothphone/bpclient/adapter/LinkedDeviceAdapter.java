package com.anyonavinfo.bluetoothphone.bpclient.adapter;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyonavinfo.bluetoothphone.R;
import com.anyonavinfo.bluetoothphone.bpcallback.CommonData;
import com.anyonavinfo.bluetoothphone.bpclient.MainActivity;
import com.anyonavinfo.bluetoothphone.bpclient.bean.DeviceBean;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by shijj on 2016/9/21.
 */
public class LinkedDeviceAdapter extends BaseAdapter {
    private ArrayList<DeviceBean> deviceList;
    private Context mContext;
    private LayoutInflater inflater;
    public SweetAlertDialog sweetAlertDialog;

    public LinkedDeviceAdapter(Context context, ArrayList<DeviceBean> data) {
        mContext = context;
        if (data == null) {
            deviceList = new ArrayList<DeviceBean>();
        } else {
            deviceList = data;
        }
        inflater = LayoutInflater.from(context);

    }

    public void setData(ArrayList<DeviceBean> data) {
        if (data != null) {
            deviceList = data;
        }
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DeviceBean deviceBean = deviceList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.connectedevice_item, null);
            holder = new ViewHolder();
            holder.pic_phone = (ImageView) convertView.findViewById(R.id.pic_phone);
            holder.device_name = (TextView) convertView.findViewById(R.id.linked_device);
            holder.device_status = (TextView) convertView.findViewById(R.id.linked_status);
            holder.delete = (Button) convertView.findViewById(R.id.delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (deviceBean.getDeviceState() == 0) {
            holder.device_status.setText("未连接");
        } else if (deviceBean.getDeviceState() == 1) {
            holder.device_status.setText("已连接");
        } else if (deviceBean.getDeviceState() == 2) {
            holder.device_status.setText("连接中");
        } else if (deviceBean.getDeviceState() == 3) {
            holder.device_status.setText("断开中");
        }
        holder.pic_phone.setImageResource(R.drawable.phone);
        holder.delete.setBackgroundResource(R.drawable.delete);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitleText("是否删除该设备！");
                sweetAlertDialog.setCancelText("不");
                sweetAlertDialog.setConfirmText("是");
                sweetAlertDialog.showCancelButton(true);
                sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.cancel();

                    }
                });
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        if (deviceBean.getDeviceState() == 1) {
                            ((MainActivity) mContext).phoneService.disconnect();
                        }
                        ((MainActivity) mContext).phoneService.deletePair(deviceBean.getDeviceAddr());
                        deviceList.remove(deviceBean);
                        LinkedDeviceAdapter.this.notifyDataSetChanged();
                        sDialog.cancel();
                    }
                });
                sweetAlertDialog.show();


            }
        });
        holder.device_name.setText(deviceBean.getDeviceName());
        convertView.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Message msg = new Message();
                msg.arg2 = position;
                if (deviceBean.getDeviceState() == 0) {
                    msg.what = 0x3003;
                    //连接
                    for (DeviceBean bean : deviceList) {
                        if (bean.getDeviceState() == 2 || bean.getDeviceState() == 3)
                            return;//若已有设备正在连接则忽略
                    }
                    if (CommonData.hfpStatu >= 2) {

                        msg.arg1 = 2;//断开再连
                        ((MainActivity) mContext).sendMessage(msg);
                        ((MainActivity) mContext).postDelayedRunnable(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) mContext).phoneService.disconnect();
                            }
                        }, 1000);
                    } else {
                        msg.arg1 = 1;//直接连接
                        ((MainActivity) mContext).sendMessage(msg);
                    }

//                    ((MainActivity) mContext).postDelayedRunnable(new Runnable() {
//                        @Override
//                        public void run() {
//                            ((MainActivity) mContext).phoneService.connect(deviceBean.getDeviceAddr());
//                            deviceBean.setDeviceState(2);
//                            setData(deviceList);
//                            LinkedDeviceAdapter.this.notifyDataSetChanged();
//                        }
//                    }, time);


                } else if (deviceBean.getDeviceState() == 1) {

                    msg.what = 0x3004;
                    ((MainActivity) mContext).sendMessage(msg);
                    ((MainActivity) mContext).postDelayedRunnable(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) mContext).phoneService.disconnect();
                        }
                    }, 1000);
                }
            }
        });
        return convertView;
    }


    class ViewHolder {
        ImageView pic_phone;
        TextView device_name;
        TextView device_status;
        Button delete;
    }

}
