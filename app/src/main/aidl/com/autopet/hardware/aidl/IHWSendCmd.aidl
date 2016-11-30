package com.autopet.hardware.aidl;

import com.autopet.hardware.aidl.IHWCallBack;

interface IHWSendCmd { 
	void sendCommand(String NewCommand);
	String getStatus(String StatusType);
	void registerCallback(IHWCallBack cb);   
    void unregisterCallback(IHWCallBack cb); 	
} 