package tests.detailed.handler;

import java.nio.ByteBuffer;

import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

public class VerifyCodeResourceHandler extends CefResourceHandlerAdapter {
	private int startPos = 0;
	private int endPos = 0;
	public byte[] content;
	public CefCallback requestCallBack = null;
	
    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
    	if(request.getURL().startsWith("https://ceac.state.gov/GenNIV/BotDetectCaptcha.ashx")){
    		System.out.println("processRequest : " + request.getURL());
    	}
        this.requestCallBack = callback;
        return true;
    }

    @Override
    public void getResponseHeaders(
            CefResponse response, IntRef response_length, StringRef redirectUrl) {
    	response_length.set(content.length);
        response.setMimeType("image/jpeg");
        response.setStatus(200);
    }

    @Override
    public boolean readResponse(
            byte[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
    	System.out.println("...read image...");
    	int length = content.length;
        if (startPos >= length) return false;

        // Extract up to bytes_to_read bytes from the html data
        if(startPos +  bytes_to_read >= length) {
        	endPos = length - 1;
        } else {
        	endPos = startPos + bytes_to_read;
        }
        // Copy extracted bytes into data_out and set the read length
        // to bytes_read.
        ByteBuffer result = ByteBuffer.wrap(data_out);
        int len = endPos-startPos+1;
        result.put(content, startPos, len);
        bytes_read.set(len);

        startPos = endPos;
        return true;
    }

    @Override
    public void cancel() {
       
    }
}
