package com.anyonavinfo.bluetoothphone.bpservice.service;

import com.anyonavinfo.bluetoothphone.bpcallback.CommonData;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by Drive on 2016/11/30.
 */

public class TBOXServiceMediator {

    public static final int UPDATE_HFP_STATUS = 0x1001;
    public static final int GET_PAD_CONNECTION=0X1002;

    private OnJsonOutput onJsonOutput;
    private OnStatusOutput onStatusOutput;

    public void excuteCommand(int... Command) {
        String CtrlString = "", ParaString = "";
        switch (Command[0]) {
            case UPDATE_HFP_STATUS:
                String hfpStatu = Command[1] + "";
                try {
                    ParaString = new JSONStringer().object().key("HFPSTATU").value(hfpStatu)
                            .endObject().toString();
                    CtrlString = new JSONStringer().object().key("UPDATEHFPSTATU")
                            .value(ParaString).endObject().toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                outputJsonCommand(CtrlString);
                break;
            case GET_PAD_CONNECTION:
                try {
                    ParaString ="";
                    CtrlString = new JSONStringer().object().key("GETPADCONNECTION")
                            .value(ParaString).endObject().toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                outputJsonCommand(CtrlString);
                break;
        }

    }

    public void getJsonStatus(String json) {
        String JsonID = "";
        JSONObject jsonparas;
        try {
            JSONObject jsonobject = new JSONObject(json);
            JsonID = jsonobject.names().getString(0);
            if(JsonID.endsWith("ISPADCONNECT")){
                jsonparas = new JSONObject(jsonobject.getString("ISPADCONNECT"));
                int isPadConnected = Integer.parseInt(jsonparas.getString("isPadConnect"));
                if(this.onStatusOutput!=null){
                    this.onStatusOutput.outputPadConnectionStatu(isPadConnected);
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void outputJsonCommand(String json) {
        if (this.onJsonOutput != null) {
            this.onJsonOutput.outputJson(json);
        }
    }

    public void setOnJsonOutput(OnJsonOutput onJsonOutput) {
        this.onJsonOutput = onJsonOutput;
    }

    public interface OnJsonOutput {
        void outputJson(String json);
    }

    public void setOnStatusOutput(OnStatusOutput onStatusOutput){
        this.onStatusOutput=onStatusOutput;
    }
    public interface OnStatusOutput{
        void outputPadConnectionStatu(int statu);
    }
}
