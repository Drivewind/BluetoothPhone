package com.anyonavinfo.bluetoothphone.bpclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.anyonavinfo.bluetoothphone.R;
import com.anyonavinfo.bluetoothphone.bpcallback.CommonData;
import com.anyonavinfo.bluetoothphone.bpcallback.IBPCallbackImpl;
import com.anyonavinfo.bluetoothphone.bpclient.base.BaseFragment;
import com.anyonavinfo.bluetoothphone.bpclient.base.BaseFragmentActivity;
import com.anyonavinfo.bluetoothphone.bpservice.imxserial.SerialPort;
import com.anyonavinfo.bluetoothphone.bpservice.service.BluetoothPhoneService;
import com.anyonavinfo.bluetoothphone.bpservice.utils.LogcatHelper;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.CallerIDsFragment;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.ConnectingFragment;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.DialFragment;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.LinkmanFragment;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.RecordFragment;
import com.anyonavinfo.bluetoothphone.bpclient.fragment.SetFragment;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseFragmentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    public static final String FRAGMENT_CALLERIDS = "callerIDsFragment";
    public static final String FRAGMENT_CONNECTING = "connectingFragment";
    public static final String FRAGMENT_DIAL = "dialFragment";
    public static final String FRAGMENT_LINKMAN = "linkmanFragment";
    public static final String FRAGMENT_RECORD = "recordFragment";
    public static final String FRAGMENT_SET = "setFragment";

    private static LinearLayout mThreeIcons;
    private ImageButton ibtnExit;
    private CheckBox call_dial;
    private CheckBox call_mute;
    private CheckBox call_switch;
    public RadioGroup rightMenu;
    private CallerIDsFragment callerIDsFragment;
    private DialFragment dialFragment;
    private ConnectingFragment connectingFragment;
    private LinkmanFragment linkmanFragment;
    public RecordFragment recordFragment;
    private SetFragment setFragment;
    private FragmentTransaction transaction;
    private FragmentManager fm;
    private long exitTime = 0;
    public Handler mHandler;
    public BluetoothPhoneService phoneService;
    private BaseFragment preFragment, curFragment;
    private boolean isFristOn;
    public SweetAlertDialog sweetAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setPackage("com.anyonavinfo.bluetoothphone");
        intent.setAction("android.intent.action.service.BluetoothPhoneService");
        setViews();
        LogcatHelper.getInstance(this).start();
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlerNewIntent(intent);
    }

    private void handlerNewIntent(Intent intent) {
        if (intent.getAction().equals("PHONE_INCOMING")) {
            transformCallIDsFragment();
            callerIDsFragment.setCallData(CommonData.talkingContact);
            phoneService.phoneTransferToBluetooth();
            call_dial.setClickable(false);
            call_mute.setClickable(false);
            call_switch.setClickable(false);
            if (sweetAlertDialog != null) {
                sweetAlertDialog.cancel();
            }
        }
    }

    /**
     * 绑定service
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    MyConn conn = new MyConn();

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handlerMessage(msg);
            }
        };
        IBPCallbackImpl.getCallback(this).setHandler(mHandler);
    }

    private void collectHandler() {
        IBPCallbackImpl.getCallback(this).setHandler(null);
        mHandler = null;
    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            String action = getIntent().getAction();
            if (action.equals("PHONE_INCOMING")) {
                initFragment(4);
                preFragment = null;
                isFristOn = true;
            } else if (getIntent().getAction().equals("android.intent.action.MAIN")) {
                if (CommonData.hfpStatu <= 2) {
                    initFragment(0);
                } else if (CommonData.hfpStatu == 3) {
                    initFragment(3);
                } else if (CommonData.hfpStatu == 5) {
                    initFragment(5);
                }
            }
            phoneService = ((BluetoothPhoneService.MyBinder) service).getService();
            if (action.equals("PHONE_INCOMING")) {
                phoneService.phoneTransferToBluetooth();
            }
            initHandler();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            phoneService = null;
        }
    }

    /**
     * 关联fragment
     */
    private void initFragment(int hfpStatu) {
        fm = getSupportFragmentManager();
        setFragment = new SetFragment();
        setFragment.setOnUiReadyListener(new SetFragment.OnUiReady() {
            @Override
            public void uiIsReady() {
                if (CommonData.hfpStatu >= 2) {
                    setFragment.aSwitch.setChecked(true);
                    setFragment.updateDeviceState(CommonData.curDeviceAddr, 1);
                } else {
                    setFragment.updateDeviceState(CommonData.curDeviceAddr, 0);
                }
            }
        });
        dialFragment = new DialFragment();
        linkmanFragment = new LinkmanFragment();
        linkmanFragment.setOnUiReadyListener(new LinkmanFragment.OnUiReady() {
            @Override
            public void uiIsReady() {
                if (CommonData.hfpStatu >= 2) {
                    linkmanFragment.updatePhoneBookView(phoneService.getPhoneBookList());
                } else {
                    linkmanFragment.updatePhoneBookView(null);
                }
            }
        });
        recordFragment = new RecordFragment();
        recordFragment.setOnUiReadyListener(new RecordFragment.OnUiReady() {
            @Override
            public void uiIsReady() {
                if (CommonData.hfpStatu >= 2) {
                    recordFragment.updatePhoneCallView(phoneService.getPhoneCallList(0));
                } else {
                    recordFragment.updatePhoneCallView(null);
                }
            }
        });
        connectingFragment = new ConnectingFragment();
        connectingFragment.setOnUiReadyListener(new ConnectingFragment.OnUiReady() {
            @Override
            public void uiIsReady() {
                connectingFragment.setCallData(CommonData.talkingContact);
            }
        });
        callerIDsFragment = new CallerIDsFragment();
        callerIDsFragment.setOnUiReadyListener(new CallerIDsFragment.OnUiReady() {
            @Override
            public void uiIsReady() {
                callerIDsFragment.setCallData(CommonData.talkingContact);
            }
        });
        curFragment = setFragment;

        transaction = fm.beginTransaction();
        transaction.add(R.id.frameLayout, setFragment, FRAGMENT_SET)
                .add(R.id.frameLayout, dialFragment, FRAGMENT_DIAL)
                .add(R.id.frameLayout, linkmanFragment, FRAGMENT_LINKMAN)
                .add(R.id.frameLayout, recordFragment, FRAGMENT_RECORD)
                .add(R.id.frameLayout, connectingFragment, FRAGMENT_CONNECTING)
                .add(R.id.frameLayout, callerIDsFragment, FRAGMENT_CALLERIDS)
                .hide(dialFragment)
                .hide(linkmanFragment)
                .hide(recordFragment)
                .hide(connectingFragment)
                .hide(setFragment)
                .hide(callerIDsFragment);
        if (hfpStatu == 0) {
            transaction.show(setFragment);
        } else if (hfpStatu == 4) {
            transaction.show(callerIDsFragment);
            showThreeIcons();
        } else if (hfpStatu == 3) {
            transaction.show(connectingFragment);
            showThreeIcons();
        } else if (hfpStatu == 5) {
            transaction.show(connectingFragment);
            showThreeIcons();
        }
        transaction.commitAllowingStateLoss();
    }

    private void setViews() {
        ibtnExit = (ImageButton) findViewById(R.id.ibtn_exit);
        rightMenu = (RadioGroup) findViewById(R.id.rg_medu);
        mThreeIcons = (LinearLayout) findViewById(R.id.ll_call);
        call_dial = (CheckBox) findViewById(R.id.rbtn_call_dial);
        call_mute = (CheckBox) findViewById(R.id.rbtn_call_mute);
        call_switch = (CheckBox) findViewById(R.id.rbtn_call_switch);

        rightMenu.getChildAt(0).setOnClickListener(this);
        rightMenu.getChildAt(1).setOnClickListener(this);
        rightMenu.getChildAt(2).setOnClickListener(this);
        rightMenu.getChildAt(3).setOnClickListener(this);

        call_dial.setOnCheckedChangeListener(this);
        call_mute.setOnCheckedChangeListener(this);
        call_switch.setOnCheckedChangeListener(this);

        call_mute.setOnClickListener(this);
        call_switch.setOnClickListener(this);

        ibtnExit.setOnClickListener(this);

    }

    /**
     * 根据需求判断显示隐藏fragment
     */
    public void toFragment(String tag) {
        FragmentTransaction ft = fm.beginTransaction();
        for (Fragment fragment : fm.getFragments()) {
            if (!fragment.getTag().equals(tag)) {
                ft.hide(fragment);
            } else {
                preFragment = curFragment;
                curFragment = (BaseFragment) fragment;
                if (curFragment.equals(callerIDsFragment)) {
                    curFragment = preFragment;
                }
            }
        }
        ft.show(fm.findFragmentByTag(tag)).commitAllowingStateLoss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    private void toTalkingFragment() {
        if (!curFragment.equals(connectingFragment)) {
            transformConnectFragment();
        } else {

        }
        connectingFragment.setCallData(CommonData.talkingContact);
    }

    private void toDialingFragment() {
        transformConnectFragment();
        connectingFragment.setCallData(CommonData.talkingContact);
        connectingFragment.call_connect.setText("拨号中...");
    }

    private void toInCommingFragment() {
        transformCallIDsFragment();
        callerIDsFragment.setCallData(CommonData.talkingContact);
    }

    private void switchCallFragment() {
        if (CommonData.hfpStatu <= 2) {
            toFragment(FRAGMENT_DIAL);
        } else if (CommonData.hfpStatu == 3) {
            toDialingFragment();
        } else if (CommonData.hfpStatu == 4) {
            toInCommingFragment();
        } else if (CommonData.hfpStatu == 5) {
            toTalkingFragment();
        }
    }

    @Override
    public void onClick(View v) {
        int checkedid = v.getId();
        switch (checkedid) {
            case R.id.rbtn_medu_setting:
                toFragment(FRAGMENT_SET);
                break;
            case R.id.rbtn_medu_dial:
                switchCallFragment();
                break;
            case R.id.rbtn_medu_linkman:
                toFragment(FRAGMENT_LINKMAN);
                break;
            case R.id.rbtn_medu_record:
                toFragment(FRAGMENT_RECORD);
                break;
            case R.id.rbtn_call_mute:
                if (call_mute.isChecked()) {
//                    phoneService.setVolume(CommonData.avVolume, CommonData.hfpVolume);
                    phoneService.unMute();
                } else {
//                    phoneService.setVolume(0, CommonData.hfpVolume);
                    phoneService.mute();
                }
                break;
            case R.id.rbtn_call_switch:
                phoneService.phoneTransfer();
                break;
            case R.id.ibtn_exit:
                finish();
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rbtn_call_dial:
                if (isChecked) {
                    //connectingFragment.showCallLayoutIn();
                } else {
                    //connectingFragment.showCallLayoutOut();
                }
                break;
            case R.id.rbtn_call_mute:
//                if(isChecked){
//                    phoneService.unMute();
//                }else{
//                    phoneService.mute();
//                }
                break;
            case R.id.rbtn_call_switch:
//                phoneService.phoneTransfer();
                break;
        }
    }

    /**
     * 左侧3个隐藏按钮的开和关
     */
    public void showThreeIcons() {
        rightMenu.setVisibility(View.INVISIBLE);
        mThreeIcons.setVisibility(View.VISIBLE);
    }


    /**
     * 左侧4个隐藏按钮的开和关
     */
    public void showfourIcons() {
        rightMenu.setVisibility(View.VISIBLE);
        mThreeIcons.setVisibility(View.INVISIBLE);
    }


    /**
     * 加载来电显示界面
     */
    public void transformCallIDsFragment() {
        showThreeIcons();
        toFragment(FRAGMENT_CALLERIDS);
    }

    /**
     * 从dialfragment加载connectingFragment
     */
    public void transformConnectFragment() {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//fragment打开动画
        toFragment(FRAGMENT_CONNECTING);
        call_dial.setChecked(false);
        showThreeIcons();
    }

    /**
     * 从connectingFragment加载dialfragment
     */
    public void transformDialFragment() {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//fragment打开动画
        toFragment(FRAGMENT_DIAL);
    }

    public void transformRecordFragment() {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//fragment打开动画
        toFragment(FRAGMENT_RECORD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(connectRunnable);
        collectHandler();
        unbindService(conn);
        LogcatHelper.getInstance(this).stop();
    }

    /**
     * 物理键退出APP
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //蓝牙模块主动连接手机，发出的消息name 和 addr有3秒左右延迟，故用runnable做处理
    private int connectCount;
    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            connectCount++;
            if (CommonData.curDeviceAddr != null) {
                setFragment.updateDeviceData();
                setFragment.updateDeviceState(CommonData.curDeviceAddr, 1);
                recordFragment.updatePhoneCallView(phoneService.getPhoneCallList(0));
                linkmanFragment.updatePhoneBookView(phoneService.getPhoneBookList());
                connectCount = 0;
            } else {
                if (connectCount < 16)
                    postDelayedRunnable(connectRunnable, 300);
            }
        }
    };

    private void handlerMessage(Message msg) {
        switch (msg.what) {
            case CommonData.HFP_CONNECTED:
//                postDelayedRunnable(connectRunnable, 300);
                break;
            case CommonData.HFP_DISCONNECTED:
                postDelayedRunnable(new Runnable() {
                    @Override
                    public void run() {
                        setFragment.updateDeviceData();
                        setFragment.updateDeviceState(null, 0);
                        recordFragment.updatePhoneCallView(null);
                        linkmanFragment.updatePhoneBookView(null);
                    }
                }, 500);
                break;
            case CommonData.HFP_CONNECTING:
                break;
            case CommonData.A2DP_CONNECTED:
                break;
            case CommonData.A2DP_DISCONNECTED:
                break;

            case CommonData.A2DP_CONNECTING:
                break;
            case CommonData.AVRCP_CONNECTED:
                break;
            case CommonData.AVRCP_DISCONNECTED:
                break;
            case CommonData.AVRCP_CONNECTING:

                break;
            case CommonData.HFP_STATU:
                if (msg.arg1 >= 2) {
                    postDelayedRunnable(connectRunnable, 300);
                }
                break;
            case CommonData.A2DP_STATU:
                break;
            case CommonData.AVRCP_STATU:
                break;
            case CommonData.BLUETOOTH_STATUS:
                break;
            case CommonData.PHONE_DIALING:
                toDialingFragment();
                connectingFragment.call_connect.setText("拨号中...");
                phoneService.phoneTransferToBluetooth();
                if (sweetAlertDialog != null) {
                    sweetAlertDialog.cancel();
                }
                call_dial.setClickable(false);
                call_mute.setClickable(false);
                call_switch.setClickable(false);
                break;
            case CommonData.PHONE_TALKING:
                break;
            case CommonData.PHONE_HANGUP:
                if (preFragment == null || isFristOn) {
                    finish();
                } else {
                    toFragment(preFragment.getTag());
                    curFragment.showIcon();
                }
                break;
            case CommonData.PHONE_CALL_SUCCESSED:
                toTalkingFragment();
                if (sweetAlertDialog != null) {
                    sweetAlertDialog.cancel();
                }
                call_dial.setClickable(true);
                call_mute.setClickable(true);
                call_switch.setClickable(true);
                break;
            case CommonData.VOICE_CONNECTED:
                call_switch.setChecked(true);
                break;
            case CommonData.VOICE_DISCONNECTED:
                call_switch.setChecked(false);
                break;
            case CommonData.CURRENT_DEVICE_NAME:
                break;
            case CommonData.CURRENT_DEVICE_ADDR:
                postDelayedRunnable(connectRunnable, 300);
                break;
            case CommonData.CURRENT_BLUETOOTH_NAME:
                setFragment.ed_dev_name.setText(msg.getData().getString("name"));
                break;
            case CommonData.PHONEBOOK_DOWNLOAD_START:
                onShow();
                break;
            case CommonData.PHONEBOOK:
                break;
            case CommonData.PHONEBOOK_DOWNLOAD_DONE:
                linkmanFragment.updatePhoneBookView(phoneService.getPhoneBookList());
                recordFragment.updatePhoneCallView(phoneService.getPhoneCallList(0));
                sweetAlertDialog.cancel();
                sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                sweetAlertDialog.setTitleText("同步完成!");
                sweetAlertDialog.show();
                break;
            case CommonData.PHONECALL:
                recordFragment.updatePhoneCallView(phoneService.getPhoneCallList(0));
                break;
            case CommonData.VOLUME:
                break;
            case CommonData.VOLUME_MUTE:
                call_mute.setChecked(false);
                break;
            case CommonData.VOLUME_UNMUTE:
                call_mute.setChecked(true);
                break;
            case CommonData.PHONEOPERATOR_SUCCESSED:
                if (CommonData.hfpStatu == 3 || CommonData.hfpStatu == 5) {
                    if (!TextUtils.isEmpty(CommonData.talkingContact.getPbplace()))
                        connectingFragment.call_dist.setText(CommonData.talkingContact.getPbplace());
                } else if (CommonData.hfpStatu == 4) {
                    if (!TextUtils.isEmpty(CommonData.talkingContact.getPbplace()))
                        callerIDsFragment.caller_dist.setText(CommonData.talkingContact.getPbplace());
                }
                recordFragment.updatePhoneCallView(phoneService.getPhoneCallList(0));
                linkmanFragment.updatePhoneBookView(phoneService.getPhoneBookList());
                break;
            case CommonData.UPDATE_TALKING_TIME:
                StringBuilder sb = new StringBuilder();
                int talkingTime = msg.arg1;
                int hour = talkingTime / 3600;
                if (hour < 10) {
                    sb.append("0");
                }
                sb.append(hour).append(":");
                int min = talkingTime % 3600 / 60;
                if (min < 10) {
                    sb.append("0");
                }
                sb.append(min).append(":");
                int sec = talkingTime % 60;
                if (sec < 10) {
                    sb.append("0");
                }
                sb.append(sec);
                connectingFragment.call_connect.setText(sb.toString());
                //connectingFragment.call_time.setText(sb.toString());
                break;
            case 0x3001:
                recordFragment.btnRecordDelete.setText("删除（" + msg.arg1 + "）");
                break;
            case 0x3002:
                linkmanFragment.btnDeleteLinkman.setText("删除（" + msg.arg1 + "）");
                break;
            default:
                break;

        }

    }

    public void postDelayedRunnable(Runnable runnable, int millsec) {
        if (mHandler != null) {
            mHandler.postDelayed(runnable, millsec);
        }
    }

    public void sendMessage(Message msg) {
        if (mHandler != null) {
            mHandler.sendMessage(msg);
        }
    }

    public void onShow() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    public void onDiss() {
        if (sweetAlertDialog != null) {
            sweetAlertDialog.cancel();
        }
    }

    public void showProgressDialog() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitleText("蓝牙电话请求访问您的通讯录！");
        sweetAlertDialog.setCancelText("拒绝");
        sweetAlertDialog.setConfirmText("同意");
        sweetAlertDialog.showCancelButton(true);
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.cancel();

            }
        });
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                phoneService.phoneBookStartUpdate();
                sDialog.cancel();
            }
        });
        sweetAlertDialog.show();

    }

}
