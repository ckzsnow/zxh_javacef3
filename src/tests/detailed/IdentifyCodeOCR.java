package tests.detailed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import com.alibaba.fastjson.JSONObject;

public class IdentifyCodeOCR {
	
	public final static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String httpRequestData(String url, String param)
			throws IOException {
		URL u;
		HttpURLConnection con = null;
		OutputStreamWriter osw;
		StringBuffer buffer = new StringBuffer();

		u = new URL(url);
		con = (HttpURLConnection) u.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		osw.write(param);
		osw.flush();
		osw.close();

		BufferedReader br = new BufferedReader(new InputStreamReader(con
				.getInputStream(), "UTF-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buffer.append(temp);
			buffer.append("\n");
		}

		return buffer.toString();
	}

	public static String GetScore(String username, String password) {
		String param = String.format("user=%s&pass=%s", username, password);
		String result;
		try {
			result = IdentifyCodeOCR.httpRequestData(
					"http://upload.chaojiying.net/Upload/GetScore.php", param);
		} catch (IOException e) {
			result = "未知问题";
		}
		return result;
	}
	
	public static String UserReg(String username, String password) {
		String param = String.format("user=%s&pass=%s", username, password);
		String result;
		try {
			result = IdentifyCodeOCR.httpRequestData(
					"http://upload.chaojiying.net/Upload/UserReg.php", param);
		} catch (IOException e) {
			result = "未知问题";
		}
		return result;
	}

	public static String UserPay(String username, String card) {

		String param = String.format("user=%s&card=%s", username, card);
		String result;
		try {
			result = IdentifyCodeOCR.httpRequestData(
					"http://upload.chaojiying.net/Upload/UserPay.php", param);
		} catch (IOException e) {
			result = "未知问题";
		}
		return result;
	}
	
	public static String ReportError(String username, String password, String softid, String id) {
		
		String param = String
		.format(
				"user=%s&pass=%s&softid=%s&id=%s",
				username, password, softid, id);
		String result;
		try {
			result = IdentifyCodeOCR.httpRequestData(
					"http://upload.chaojiying.net/Upload/ReportError.php", param);
		} catch (IOException e) {
			result = "未知问题";
		}
		
		return result;
	}

	public static String httpPostImage(String param, byte[] data) throws IOException {
		long time = (new Date()).getTime();
		URL u = null;
		HttpURLConnection con = null;
		String boundary = "----------" + MD5(String.valueOf(time));
		String boundarybytesString = "\r\n--" + boundary + "\r\n";
		OutputStream out = null;
		
		u = new URL("http://upload.chaojiying.net/Upload/Processing.php");
		
		con = (HttpURLConnection) u.openConnection();
		con.setRequestMethod("POST");
		//con.setReadTimeout(60000);   
		con.setConnectTimeout(60000);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(true);
		con.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		
		out = con.getOutputStream();
			
		for (String paramValue : param.split("[&]")) {
			out.write(boundarybytesString.getBytes("UTF-8"));
			String paramString = "Content-Disposition: form-data; name=\""
					+ paramValue.split("[=]")[0] + "\"\r\n\r\n" + paramValue.split("[=]")[1];
			out.write(paramString.getBytes("UTF-8"));
		}
		out.write(boundarybytesString.getBytes("UTF-8"));

		String paramString = "Content-Disposition: form-data; name=\"userfile\"; filename=\""
				+ "chaojiying_java.gif" + "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
		out.write(paramString.getBytes("UTF-8"));
		
		out.write(data);
		
		String tailer = "\r\n--" + boundary + "--\r\n";
		out.write(tailer.getBytes("UTF-8"));

		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(con
					.getInputStream(), "UTF-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buffer.append(temp);
			buffer.append("\n");
		}

		return buffer.toString();
	}	
	
	public static String PostPic(String username, String password, String softid, String codetype, String len_min, String filePath) {
		String result = "";
		String param = String
		.format(
				"user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s", username, password, softid, codetype, len_min);
		try {
			File f = new File(filePath);
			if (null != f) {
				int size = (int) f.length();
				byte[] data = new byte[size];
				FileInputStream fis = new FileInputStream(f);
				fis.read(data, 0, size);
				if(null != fis) fis.close();
				
				if (data.length > 0)	result = IdentifyCodeOCR.httpPostImage(param, data);
			}
		} catch(Exception e) {
			result = "-1";
		}
		
		
		return result;
	}

	public static String PostPic(String username, String password, String softid, String codetype, String len_min, byte[] byteArr) {
		String result = "";
		String param = String
		.format(
				"user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s", username, password, softid, codetype, len_min);
		try {
			result = IdentifyCodeOCR.httpPostImage(param, byteArr);
		} catch(Exception e) {
			result = "-1";
		}
		
		
		return result;
	}
	
	public static String verifyCode(String type, String filePath){
		String result = PostPic("ckzsnow", "ckzcbm110", "7be4cf00befd8236ac8171945592da36", type, "4", filePath);
		if(("-1").equals(result)){
			return "";
		} else {
			JSONObject json = JSONObject.parseObject(result);
			if(json.containsKey("pic_str")){
				return json.getString("pic_str");
			} else {
				return "";
			}
		}
	}
	
	public static String verifyCode(String type, byte[] byteArr){
		String result = PostPic("ckzsnow", "ckzcbm110", "7be4cf00befd8236ac8171945592da36", type, "4", byteArr);
		if(("-1").equals(result)){
			return "";
		} else {
			JSONObject json = JSONObject.parseObject(result);
			if(json.containsKey("pic_str")){
				return json.getString("pic_str");
			} else {
				return "";
			}
		}
	}
	
	public static void main(String[] args){
		System.out.println(verifyCode("4008", "C:\\Users\\Administrator\\Desktop\\code.jpg"));
	}

}
