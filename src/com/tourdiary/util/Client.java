package com.tourdiary.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	static Socket socket;
	static void connectToHost(final String address, final int port){
		new Thread(){
			public void run(){
				try {
					socket=new Socket(address,port);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
}
