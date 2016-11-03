package com.anyonavinfo.bluetoothphone.bpclient.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.anyonavinfo.bluetoothphone.bpcallback.CommonData;
import com.anyonavinfo.bluetoothphone.bpclient.MainActivity;
import com.anyonavinfo.bluetoothphone.R;
import com.anyonavinfo.bluetoothphone.bpclient.base.BaseFragment;

import java.lang.reflect.Method;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;
import static android.media.ToneGenerator.TONE_SUP_DIAL;


/**
 * Created by navinfo-21 on 2016/9/8.
 */
public class DialFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private EditText etNumb;
    private ImageButton ibtnDeleteNumb;
    private ImageButton dialNum1;
    private ImageButton dialNum2;
    private ImageButton dialNum3;
    private ImageButton dialNum4;
    private ImageButton dialNum5;
    private ImageButton dialNum6;
    private ImageButton dialNum7;
    private ImageButton dialNum8;
    private ImageButton dialNum9;
    private ImageButton dialx;
    private ImageButton dialNum0;
    private ImageButton dialj;
    private Button dialCall;
    private FragmentTransaction transaction;
    private FragmentManager fm;
    private ConnectingFragment connectingFragment;
    private DialFragment dialFragment;

    private static final int DTMF_DURATION_MS = 120; // 声音的播放时间
    private Object mToneGeneratorLock = new Object(); // 监视器对象锁
    private ToneGenerator mToneGenerator;             // 声音产生器
    private static boolean mDTMFToneEnabled;         // 系统参数“按键操作音”标志位


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            view = View.inflate(getActivity(), R.layout.fragment_dial, null);
            // 获取系统参数“按键操作音”是否开启
            mDTMFToneEnabled = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
            synchronized (mToneGeneratorLock) {
                if (mDTMFToneEnabled && mToneGenerator == null) {
                    mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 80); // 设置声音的大小
                    getActivity().setVolumeControlStream(AudioManager.STREAM_DTMF);
                }
            }
            setViews();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            mDTMFToneEnabled = false;
            mToneGenerator = null;
        }
        return view;
    }

    private void addListener() {
        dialCall.setOnClickListener(this);
        dialNum1.setOnClickListener(this);
        dialNum2.setOnClickListener(this);
        dialNum3.setOnClickListener(this);
        dialNum4.setOnClickListener(this);
        dialNum5.setOnClickListener(this);
        dialNum6.setOnClickListener(this);
        dialNum7.setOnClickListener(this);
        dialNum8.setOnClickListener(this);
        dialNum9.setOnClickListener(this);
        dialNum0.setOnClickListener(this);
        dialx.setOnClickListener(this);
        dialj.setOnClickListener(this);
        ibtnDeleteNumb.setOnClickListener(this);

        ibtnDeleteNumb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                etNumb.setText("");
                return false;
            }
        });


        etNumb.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etNumb.requestFocus();
                if (etNumb.getText().length() > 0) {
                    ibtnDeleteNumb.setVisibility(View.VISIBLE);
                } else {
                    ibtnDeleteNumb.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void setViews() {
        etNumb = (EditText) view.findViewById(R.id.et_numb);
        /** edittext不显示软键盘,要显示光标 */
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            etNumb.setInputType(InputType.TYPE_NULL);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setSoftInputShownOnFocus;
                setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setSoftInputShownOnFocus.setAccessible(true);
                setSoftInputShownOnFocus.invoke(etNumb, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ibtnDeleteNumb = (ImageButton) view.findViewById(R.id.ibtn_delete_numb);

        if (etNumb.getText().length() > 0) {
            ibtnDeleteNumb.setVisibility(View.VISIBLE);
        } else {
            ibtnDeleteNumb.setVisibility(View.INVISIBLE);
        }


        dialNum1 = (ImageButton) view.findViewById(R.id.dialNum1);
        dialNum2 = (ImageButton) view.findViewById(R.id.dialNum2);
        dialNum3 = (ImageButton) view.findViewById(R.id.dialNum3);

        dialNum4 = (ImageButton) view.findViewById(R.id.dialNum4);
        dialNum5 = (ImageButton) view.findViewById(R.id.dialNum5);
        dialNum6 = (ImageButton) view.findViewById(R.id.dialNum6);

        dialNum7 = (ImageButton) view.findViewById(R.id.dialNum7);
        dialNum8 = (ImageButton) view.findViewById(R.id.dialNum8);
        dialNum9 = (ImageButton) view.findViewById(R.id.dialNum9);

        dialx = (ImageButton) view.findViewById(R.id.dialx);
        dialNum0 = (ImageButton) view.findViewById(R.id.dialNum0);
        dialj = (ImageButton) view.findViewById(R.id.dialj);

        dialCall = (Button) view.findViewById(R.id.dialCall);

        fm = getFragmentManager();
        connectingFragment = new ConnectingFragment();
        dialFragment = new DialFragment();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialNum1:
                playTone(ToneGenerator.TONE_DTMF_1);
                keyPressed(KeyEvent.KEYCODE_1);
                break;
            case R.id.dialNum2:
                playTone(ToneGenerator.TONE_DTMF_2);
                keyPressed(KeyEvent.KEYCODE_2);
                break;
            case R.id.dialNum3:
                playTone(ToneGenerator.TONE_DTMF_3);
                keyPressed(KeyEvent.KEYCODE_3);
                break;
            case R.id.dialNum4:
                playTone(ToneGenerator.TONE_DTMF_4);
                keyPressed(KeyEvent.KEYCODE_4);
                break;
            case R.id.dialNum5:
                playTone(ToneGenerator.TONE_DTMF_5);
                keyPressed(KeyEvent.KEYCODE_5);
                break;
            case R.id.dialNum6:
                playTone(ToneGenerator.TONE_DTMF_6);
                keyPressed(KeyEvent.KEYCODE_6);
                break;
            case R.id.dialNum7:
                playTone(ToneGenerator.TONE_DTMF_7);
                keyPressed(KeyEvent.KEYCODE_7);
                break;
            case R.id.dialNum8:
                playTone(ToneGenerator.TONE_DTMF_8);
                keyPressed(KeyEvent.KEYCODE_8);
                break;
            case R.id.dialNum9:
                playTone(ToneGenerator.TONE_DTMF_9);
                keyPressed(KeyEvent.KEYCODE_9);
                break;
            case R.id.dialNum0:
                playTone(ToneGenerator.TONE_DTMF_0);
                keyPressed(KeyEvent.KEYCODE_0);
                break;
            case R.id.dialj:
                playTone(ToneGenerator.TONE_DTMF_P);
                keyPressed(KeyEvent.KEYCODE_POUND);
                break;
            case R.id.dialx:
                playTone(ToneGenerator.TONE_DTMF_S);
                keyPressed(KeyEvent.KEYCODE_STAR);
                break;
            case R.id.ibtn_delete_numb:
                playTone(ToneGenerator.TONE_DTMF_D);
                keyPressed(KeyEvent.KEYCODE_DEL);
                break;
            case R.id.dialCall:
                if (CommonData.hfpStatu < 2) {
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("蓝牙未连接!")
                            .show();
                } else {
                    String number = etNumb.getText().toString();//获取号码待用
                    ((MainActivity) getActivity()).phoneService.phoneDail(number);
                }
                break;
        }

    }

    private void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        etNumb.onKeyDown(keyCode, event);
    }

    @Override
    public void showIcon() {
        ((MainActivity) getActivity()).showfourIcons();
        ((RadioButton) (((MainActivity) getActivity()).rightMenu.getChildAt(1))).setChecked(true);
    }

    /**
     * 播放按键声音
     */
    private void playTone(int tone) {
        if (!mDTMFToneEnabled) {
            return;
        }
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_SILENT
                || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // 静音或者震动时不发出声音
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            mToneGenerator.startTone(tone, DTMF_DURATION_MS);   //发出声音
        }
    }


}
