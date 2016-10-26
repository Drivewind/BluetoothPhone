/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>

#include <stdio.h>


#include "android/log.h"

#include "SerialPort.h"

static const char *TAG = "serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(args...) __android_log_print(ANDROID_LOG_ERROR, TAG,  ##args)

#define IOC_MANGIC		'X'
#define IOC_SETSTATE	_IOW(IOC_MANGIC, 1, int)
#define IOC_GETSTATE	_IOR(IOC_MANGIC, 2, int)

static int GPIOfd;
static int lastfd = -1;

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_anyonavinfo_bluetoothphone_bpservice_imxserial_SerialPort_open
        (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags) {
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            /* TODO: throw an exception */
            LOGE("%s", "Invalid baudrate");
            return NULL;
        }
    }


    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            LOGE("%s", "Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
//Johnson
        else
            lastfd = fd;
    }

    /* Configure device */
    {
        struct termios cfg;
//		LOGD("Configuring serial port");
        if (tcgetattr(fd, &cfg)) {
            LOGE("%s", "tcgetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd, TCSANOW, &cfg)) {
            LOGE("%s", "tcsetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);
    }

    return mFileDescriptor;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_anyonavinfo_bluetoothphone_bpservice_imxserial_SerialPort_close
        (JNIEnv *env, jobject thiz) {
/*
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);
*/
    if (lastfd != -1) {
        LOGD("close(fd = %d)", lastfd);
        close(lastfd);
        lastfd = -1;
    }
}

JNIEXPORT jint JNICALL Java_com_anyonavinfo_bluetoothphone_bpservice_imxserial_SerialPort_getVolumeChannelState(JNIEnv *env, jobject clazz)
{
    int fd = 0;
    int ret;
    int arg;

    fd = open("/dev/volchannel_state",O_RDWR);
    if (fd < 0)
    {
        printf("Open volchannel_state Error!\n");
        LOGE("Open volchannel_state Error!");
        return -1;
    }
    ret = ioctl(fd, IOC_GETSTATE, &arg);
    if(arg != 0 && arg != 1)
        arg = -1;
    close(fd);
    return arg;
}
/**
para: state
1-sethigh
0-setlow
*/
JNIEXPORT jint JNICALL Java_com_anyonavinfo_bluetoothphone_bpservice_imxserial_SerialPort_setVolumeChannelState(JNIEnv *env, jobject clazz, jint state)
{
    int fd = 0;
    int ret;
    int arg;

    fd = open("/dev/volchannel_state",O_RDWR);
    if (fd < 0)
    {
        printf("Open volchannel_state Error!\n");
    }


    if(!state)
    {
        arg = 0;
        ret = ioctl(fd, IOC_SETSTATE, &arg);
    }
    else
    {
        arg = 1;
        ret = ioctl(fd, IOC_SETSTATE, &arg);
    }
    return ret;
}

