package tests.detailed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cef.callback.CefAuthCallback;
import org.cef.callback.CefCookieVisitor;
import org.cef.callback.CefURLRequestClient;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.network.CefURLRequest;

import tests.detailed.handler.VerifyCodeResourceHandler;

public class MyURLRequestClient implements CefURLRequestClient {
	private long nativeRef_ = 0;
	VerifyCodeResourceHandler mrHandler = null;
	private CefURLRequest urlRequest_ = null;
	private ByteArrayOutputStream byteStream_ = new ByteArrayOutputStream();
	
	public MyURLRequestClient(VerifyCodeResourceHandler mrHandler){
		this.mrHandler = mrHandler;
	}
	
	public void sendRequest(CefRequest request){
		CefCookieManager cookieManager = CefCookieManager.getGlobalManager();
		cookieManager.visitAllCookies(new CefCookieVisitor() {
			int countAll = 0;
			StringBuilder sb = new StringBuilder();
			@Override
			public boolean visit(CefCookie cookie, int count, int total, BoolRef delete) {
				countAll++;
				sb.append(cookie.name);
				sb.append("=");
				sb.append(cookie.value);
				sb.append(";");
				if(countAll == total){
					String cookieStr = sb.toString().substring(0, sb.length()-1);
					System.out.println("Cookie:" + cookieStr);
					Map<String, String> headerMap = new HashMap<>();
					request.getHeaderMap(headerMap);
					headerMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
					headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
					headerMap.put("Connection", "keep-alive");
					headerMap.put("Cookie", cookieStr);
					headerMap.put("Host", "ceac.state.gov");
					headerMap.put("Referer", "https://ceac.state.gov/genniv/");
					request.setHeaderMap(headerMap);
					System.out.println("Header:" + headerMap.toString());
					urlRequest_ = CefURLRequest.create(request, MyURLRequestClient.this);
				    if (urlRequest_ == null) {
				    	System.out.println("urlRequest_ create is null!");
				    } else {
				    	System.out.println("urlRequest_ create success.");
				    }
				}
				return true;
			}
		});
	}
	
	@Override
	public void setNativeRef(String identifer, long nativeRef) {
		nativeRef_ = nativeRef;
	}

	@Override
	public long getNativeRef(String identifer) {
		return nativeRef_;
	}

	@Override
	public void onRequestComplete(CefURLRequest request) {
		String updateStr = "onRequestCompleted\n\n";
        CefResponse response = request.getResponse();
        boolean isText = response.getHeader("Content-Type").startsWith("text");
        updateStr += response.toString();
        System.out.println(updateStr + isText);
        File imageFile = new File(Configuration.getValue("verify_code_save_path")+"\\code.jpg");
        try {
        	FileOutputStream outStream = new FileOutputStream(imageFile);
			outStream.write(byteStream_.toByteArray());
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        mrHandler.content = byteStream_.toByteArray();
        mrHandler.requestCallBack.Continue();
	}

	@Override
	public void onUploadProgress(CefURLRequest request, int current, int total) {
		System.out.println("onUploadProgress: " + current + "/" + total + " bytes\n");
	}

	@Override
	public void onDownloadProgress(CefURLRequest request, int current, int total) {
		System.out.println("onDownloadProgress: " + current + "/" + total + " bytes\n");
	}

	@Override
	public void onDownloadData(CefURLRequest request, byte[] data, int data_length) {
		byteStream_.write(data, 0, data_length);
		System.out.println("onDownloadData: " + data_length + " bytes\n");
	}

	@Override
	public boolean getAuthCredentials(boolean isProxy, String host, int port, String realm, String scheme,
			CefAuthCallback callback) {
		return false;
	}

}
