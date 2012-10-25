package com.abstractlayers.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/*
 * This class is based Off of CallbackServer, present as part of Cordova 2.1
 * It has been adapted to suit the needs of demo
 */
public class TrimmedDownCallbackServer implements Runnable {

	private ServerSocket waitSocket;
	private List<String> msgQueue;
	private Thread serverThread;
	private Object lock = new Object();
	private int port;

	public int getPort() {
		return port;
	}
	boolean keepRunning;
	static final String digits = "0123456789ABCDEF";

	public TrimmedDownCallbackServer() {
		keepRunning = true;
		msgQueue = new ArrayList<String>();
		this.startServer();
		//port=5000;

	}

	public void startServer() {
		this.serverThread = new Thread(this);
		this.serverThread.start();
		Log.i("TrimmedDownServer", "Server Started");
	}

	public void stopServer() {
		try {
			waitSocket.close();
			keepRunning = false;
		} catch (IOException ignore) {
		}
	}

	public void run() {
		try {
			waitSocket = new ServerSocket(0);
			port = waitSocket.getLocalPort();
			String request;

			while (keepRunning) {
				Socket connection = waitSocket.accept();
				BufferedReader xhrReader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()), 40);
				DataOutputStream output = new DataOutputStream(
						connection.getOutputStream());
				request = xhrReader.readLine();
				String js = null;
				String response = "";
				if (keepRunning) {

					synchronized (lock) {
						while (keepRunning) {
							if (msgQueue.size() > 0) {
								js = msgQueue.remove(0);
								if (js != null && js.length() > 0) {
									Log.i("TrimmedDownServer", "Got Message="+js);
									break;
								}
							}
							try {
								lock.wait(10000);
								break;
							} catch (Exception e) {
								Log.e("TrimmedDownServer", e.getMessage());
							}
						}
					}
					
					 if(js!=null) {
						response = "HTTP/1.1 200 OK\r\n\r\n";
						response += encode(js, "UTF-8");
						Log.i("TrimmedDownServer", " Sent Message:" + js);
						js=null;
					}
					output.writeBytes(response);
					output.flush();
				}
				output.close();
				xhrReader.close();
			}

		} catch (Exception ex) {
			Log.e("TrimmedDownServer", ex.getMessage());
		}
	}

	public void addMessagetoQueue(String message) {
		synchronized (lock) {
			Log.i("TrimmedDownServer", "Adding message to queue="+message);
			msgQueue.add(message);
			lock.notifyAll();
			Log.i("TrimmedDownServer", "Notified");
		}
	}


	
	public static String encode(String s, String enc)
			throws UnsupportedEncodingException {
		if (s == null || enc == null) {
			throw new NullPointerException();
		}
		// check for UnsupportedEncodingException
		"".getBytes(enc);

		// Guess a bit bigger for encoded form
		StringBuilder buf = new StringBuilder(s.length() + 16);
		int start = -1;
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9')
					|| " .-*_'(),<>=?@[]{}:~\"\\/;!".indexOf(ch) > -1) {
				if (start >= 0) {
					convert(s.substring(start, i), buf, enc);
					start = -1;
				}
				if (ch != ' ') {
					buf.append(ch);
				} else {
					buf.append(' ');
				}
			} else {
				if (start < 0) {
					start = i;
				}
			}
		}
		if (start >= 0) {
			convert(s.substring(start, s.length()), buf, enc);
		}
		return buf.toString();
	}

	private static void convert(String s, StringBuilder buf, String enc)
			throws UnsupportedEncodingException {
		byte[] bytes = s.getBytes(enc);
		for (int j = 0; j < bytes.length; j++) {
			buf.append('%');
			buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
			buf.append(digits.charAt(bytes[j] & 0xf));
		}
	}

}