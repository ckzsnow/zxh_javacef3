package tests.detailed.handler;

import java.util.Vector;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.CefDialogHandler;

import tests.detailed.TaskUtils;

public class FileDialogHandler implements CefDialogHandler {

	@Override
	public boolean onFileDialog(CefBrowser arg0, FileDialogMode arg1, String arg2, String arg3, Vector<String> arg4,
			int arg5, CefFileDialogCallback callBack) {
		Vector<String> filePaths = new Vector<String>();
		System.out.println("onFileDialog called, filePath : " + TaskUtils.fileUploadFilePath);
		filePaths.add(TaskUtils.fileUploadFilePath);
        callBack.Continue(0, filePaths);
        return true;
	}	
	
}
