package com.biggestnerd.simplechat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Connection {

	public static final int DEFAULT_PORT = 9002;
	
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	public void connect(String ip, int port, String name, String password) {
		try {
			socket = new Socket(ip, port);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			if(password != null && password.length() > 0) {
				out.writeUTF("PASS:" + password);
			}
			out.writeUTF("NAME:" + name);
			new RecieveThread().start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendMessage(String msg) {
		try {
			out.writeUTF(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseMessage(String msg) {
		if(msg.startsWith("ERR")) {
			String error = msg.split("\\|")[1];
			if(error.equals("INVALID_PASSWORD")) {
				disconnect();
			}
		} else {
			Main.getInstance().addMessage(msg);
		}
	}
	
	public String getServerString() {
		return socket == null ? "" : socket.getInetAddress().toString();
	}
	
	public void disconnect() {
		if(!isConnected()) return;
		try {
			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed();
	}
	
	class RecieveThread extends Thread {
		public void run() {
			String line;
			while(socket.isConnected()) {
				try {
					if((line = in.readUTF()) != null) {
						parseMessage(line);
					}
				} catch (Exception ex) {
					if(ex instanceof SocketException) break;
					ex.printStackTrace();
				}
			}
			try {
				in.close();
				out.close();
			} catch (Exception e) {}
		}
	}
}
