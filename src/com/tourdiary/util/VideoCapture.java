package com.tourdiary.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.net.ContentHandler;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
/**
 * className:	    VideoCapture
 * time:	        2017/1/5	11:17
 * desc:	        视频生成类
 *
 * svnVersion:
 * upDateAuthor:    Vincent
 * upDate:          2017/1/5
 * upDateDesc:      TODO
 */


public class VideoCapture {
    private static int     switcher = 1;//录像键
    private static boolean isPaused = false;//暂停键
    private static String  filename = null;
    private static OnFinishListener mFinishListener;

    public static void start(final String path,final List<String> filelist) {

        switcher = 1;
        new Thread() {
            public void run() {

                try {
                    filename = "test.mp4";
                    /*String dirPath = Environment.getExternalStorageDirectory()
                                                .getAbsolutePath() + File.separator + "zheng	" + File.separator + "screenshoot";*/

                    File file1=new File(path,filename);
                    try {
						file1.createNewFile();
					} catch (IOException e) {
//						e.printStackTrace();
					}
                    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file1,
                                                                           1080,
                                                                           1720);
//                    FFmpegFrameRecorder recorder=new FFmpegFrameRecorder(filename, 580, 435);
                    recorder.setFormat("mp4");
                    recorder.setFrameRate(1f);//录像帧率
                    recorder.start();
                    File   file     = new File(path);
                    File[] files    = file.listFiles();
//                    由于生成的文件放在这个目录 图片数量会少一张
                    int    length   = filelist.size();
                    int    position = 0;
                    while (switcher != 0) {
                        if (!isPaused) {

                            if (position < length) {
                                opencv_core.IplImage image = cvLoadImage(new File(filelist.get(position)).getAbsolutePath());
                                recorder.record(image);
                            } else {
                                recorder.stop();
                                mHandler.sendEmptyMessage(0);
                                switcher=0;
                            }
                            
                        }
                        position++;
                    }

                } 
                catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void setFinishListener(OnFinishListener finishListener) {
        mFinishListener = finishListener;
    }

    public static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                        case 0:
                            if (mFinishListener != null){
                                mFinishListener.OnFinish();
                            }
                            break;

                        default:
                            break;
                    }
        }
    };
}
