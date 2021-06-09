package greenexSrv2.nvs.com;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshRequest {
	
	public globalData gData ;
	
	public SshRequest(globalData gData) {
		this.gData = gData;
	}
	
	
	public String getSsh(String ip, String user, String password, String strCommand) throws Exception {

		String out = "";

		int exitStatus = -100;

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, ip, 22);
		session.setPassword(password);
		session.setConfig(config);
		session.setTimeout(Integer.valueOf(gData.commonParams.get("SshTimeoutSec")) * 1000);
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(strCommand);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);

		InputStream in = channel.getInputStream();
		channel.connect();
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				out += new String(tmp, 0, i);
			}

			if (channel.isClosed()) {
				exitStatus = channel.getExitStatus();

				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();
		session.disconnect();

		return out;

	}
}
