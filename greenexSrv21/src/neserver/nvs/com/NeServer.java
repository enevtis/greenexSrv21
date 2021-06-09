package neserver.nvs.com;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.*;

import greenexSrv2.nvs.com.globalData;

public class NeServer extends Thread {

	public SSLSocket socket;
	public globalData gData;

	public NeServer(SSLSocket socket, globalData gData) {
		this.socket = socket;
		this.gData = gData;
	}

	public NeServer(globalData gData) {
		this.gData = gData;

	}

	public void activate() {

		int port = Integer.valueOf(gData.commonParams.get("webServicePort"));

		try {

			SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

			SSLServerSocket sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
			String message = "Https server is listening to port " + port + "";

			System.out.println(message);
			gData.logger.info(message);

			while (true) {
				SSLSocket sslSocket = (SSLSocket) (sslServerSocket.accept());

				(new NeServer(sslSocket, gData)).start();

			}
		} catch (IOException e) {
//				System.out.println(
//					"Exception caught when trying to listen on port " + port + " or listening for a connection");
//				System.out.println(e.getMessage());
		}

	}

	public void run() {

		try {

			List<String> tokens = new ArrayList<>();
			String t = "";
			boolean wasAuthorized = false;
			String[] parts;
			String postData = "";

			int postDataI = -1;

			BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			String line;

			line = in.readLine();
			tokens.add(line);
			while ((line = in.readLine()) != null && (line.length() != 0)) {
				tokens.add(line);
				if (line.indexOf("Content-Length:") > -1) {
					postDataI = Integer.valueOf(line.substring(line.indexOf("Content-Length:") + 16, line.length()));
				}
			}

			// read the post data
			if (postDataI > 0) {
				char[] charArray = new char[postDataI];
				in.read(charArray, 0, postDataI);
				postData = new String(charArray);
			}

			for (int i = 0; i < tokens.size(); i++) {
				t = tokens.get(i);

				if (t.contains("Authorization")) {

					if (checkIsAuthorized(t)) {

						wasAuthorized = true;

						for (int y = 0; y < tokens.size(); y++) {

							if (tokens.get(y).contains("GET")) {
								parts = tokens.get(y).split("\\s+");
								routeResponse(this.socket, parts[1], this.gData);

							} else if (tokens.get(y).contains("POST")) {
								parts = tokens.get(y).split("\\s+");
								routeResponse(this.socket, parts[1], postData, this.gData);

							}

						}

					}

				}
			}

			if (!wasAuthorized) {

				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeBytes("HTTP/1.1 401 Unauthorized\r\n");
				out.writeBytes("WWW-Authenticate: Basic realm=\"NVS\"\r\n");
				out.writeBytes("Content-Type: text/html; charset=UTF-8\r\n");
				out.writeBytes("\r\n");
				out.close();
			}

			in.close();
			this.socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}
	}

	@SuppressWarnings("deprecation")
	public void routeResponse(SSLSocket socket, String requestString, globalData gData) {

		if (requestString.contains("favicon.ico"))
			return;
		if (requestString.isEmpty())
			return;

		try {

			String handlerClassName = gData.router.get(parseHandlerName(requestString));

			Class c = Class.forName(handlerClassName);

			Class<?>[] classes = new Class<?>[1];
			classes[0] = greenexSrv2.nvs.com.globalData.class;
			Constructor cnstr = Class.forName(handlerClassName).getConstructor(classes[0]);

			Object obj = cnstr.newInstance(gData);

			Class[] paramTypes = new Class[] { SSLSocket.class, String.class };

			Method method = c.getMethod("getResponse", paramTypes);

			Object[] args = new Object[] { socket, requestString };

			method.invoke(obj, args);

		} catch (InvocationTargetException e) {

			StringWriter errors = new StringWriter();
			e.getCause().printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** " + errors.toString());

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** " + errors.toString());
		}

	}

	public void routeResponse(SSLSocket socket, String requestString, String postData, globalData gData) {

		if (requestString.contains("favicon.ico"))
			return;
		if (requestString.isEmpty())
			return;

		try {

			String handlerClassName = gData.router.get(parseHandlerName(requestString));

			Class c = Class.forName(handlerClassName);

			Class<?>[] classes = new Class<?>[1];
			classes[0] = greenexSrv2.nvs.com.globalData.class;

			Constructor cnstr = Class.forName(handlerClassName).getConstructor(classes[0]);

			Object obj = cnstr.newInstance(gData);

			Class[] paramTypes = new Class[] { SSLSocket.class, String.class, String.class };

			Method method = c.getMethod("getResponse", paramTypes);

			Object[] args = new Object[] { socket, requestString, postData };

			method.invoke(obj, args);

		} catch (InvocationTargetException e) {

			StringWriter errors = new StringWriter();
			e.getCause().printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** " + errors.toString());

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** " + errors.toString());
		}

	}

	private String parseHandlerName(String requestString) {
		String out = "";
		String[] parts;
		String tempStr = requestString;

		if (tempStr.contains("?")) {
			parts = tempStr.split("\\?");
			out = parts[0];

		} else {
			if (tempStr.startsWith("/img")) {
				out = "/img";
			} else if (tempStr.startsWith("/src")) {
				out = "/src";
			} else {
				out = tempStr;
			}

		}

		return out;
	}

	public boolean checkIsAuthorized(String AuthString) {
		boolean out = false;

		String loginPass = "";
		String parts[] = AuthString.split("\\s+");

		if (parts.length != 3)
			return false;

		byte[] decodedBytes = Base64.getDecoder().decode(parts[2]);
		loginPass = new String(decodedBytes);

		
		String[] loginPassparts = loginPass.split(":");
		if (loginPassparts.length != 2)
			return false;

		String SQL = "select hash from sys_users where user_name='" + loginPassparts[0] + "'";

		String hash = "", password = "";

		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			hash = rec.get("hash");
		}


		
		password = gData.getPasswordFromHash(hash);
		
		if (loginPassparts[1].equals(password)) {

			SQL = "update sys_users set last_login=NOW() " + " where user_name='" + loginPassparts[0] + "'";
			gData.sqlReq.saveResult(SQL);
			gData.commonParams.put("currentUser", loginPassparts[0]);
			
			return true;
		} else {

			gData.logger.info("Login " + loginPassparts[0] + " was unsuccessful.");

		}

		return out;
	}

	String readLine(BufferedInputStream in) {

		InputStreamReader r = new InputStreamReader(in, StandardCharsets.US_ASCII);
		StringBuilder sb = new StringBuilder();
		char c;
		try {
			while ((c = (char) r.read()) >= 0) {
				if (c == '\n')
					break;
				if (c == '\r') {
					c = (char) r.read();
					if ((c < 0) || (c == '\n'))
						break;
					sb.append('\r');
				}
				sb.append(c);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

}
