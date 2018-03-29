// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package tests.detailed;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cef.CefApp;
import org.cef.CefApp.CefVersion;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefRequestContext;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefRequestContextHandlerAdapter;
import org.cef.network.CefCookieManager;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import tests.detailed.dialog.DownloadDialog;
import tests.detailed.handler.AppHandler;
import tests.detailed.handler.CefQueryMessageRouterHandler;
import tests.detailed.handler.ContextMenuHandler;
import tests.detailed.handler.DragHandler;
import tests.detailed.handler.FileDialogHandler;
import tests.detailed.handler.GeolocationHandler;
import tests.detailed.handler.JSDialogHandler;
import tests.detailed.handler.KeyboardHandler;
import tests.detailed.handler.RequestHandler;
import tests.detailed.ui.ControlPanel;
import tests.detailed.ui.MenuBar;
import tests.detailed.ui.StatusPanel;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = -2295538706810864538L;
    public static void main(String[] args) {
        // OSR mode is enabled by default on Linux.
        // and disabled by default on Windows and Mac OS X.
        boolean osrEnabledArg = OS.isLinux();
        boolean transparentPaintingEnabledArg = false;
        String cookiePath = null;
        for (String arg : args) {
            arg = arg.toLowerCase();
            if (!OS.isLinux() && arg.equals("--off-screen-rendering-enabled")) {
                osrEnabledArg = true;
            } else if (arg.equals("--transparent-painting-enabled")) {
                transparentPaintingEnabledArg = true;
            } else if (arg.startsWith("--cookie-path=")) {
                cookiePath = arg.substring("--cookie-path=".length());
                File testPath = new File(cookiePath);
                if (!testPath.isDirectory() || !testPath.canWrite()) {
                    System.out.println("Can't use " + cookiePath
                            + " as cookie directory. Check if it exists and if it is writable");
                    cookiePath = null;
                } else {
                    System.out.println("Storing cookies in " + cookiePath);
                }
            }
        }

        System.out.println("Offscreen rendering " + (osrEnabledArg ? "enabled" : "disabled"));

        // MainFrame keeps all the knowledge to display the embedded browser
        // frame.
        final MainFrame frame =
                new MainFrame(osrEnabledArg, transparentPaintingEnabledArg, cookiePath, args);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                frame.dispose();
            }
        });

        frame.setSize(1200, 600);
        frame.setVisible(true);
    }

    private final CefClient client_;
    private String errorMsg_ = "";
    private final CefBrowser browser_;
    private ControlPanel control_pane_;
    private StatusPanel status_panel_;
    private final CefCookieManager cookieManager_;

    public MainFrame(boolean osrEnabled, boolean transparentPaintingEnabled, String cookiePath,
            String[] args) {
        // 1) CefApp is the entry point for JCEF. You can pass
        //    application arguments to it, if you want to handle any
        //    chromium or CEF related switches/attributes in
        //    the native world.
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = osrEnabled;
        // try to load URL "about:blank" to see the background color
        settings.background_color = settings.new ColorType(100, 255, 242, 211);
        CefApp myApp = CefApp.getInstance(args, settings);
        CefVersion version = myApp.getVersion();
        System.out.println("Using:\n" + version);

        //    We're registering our own AppHandler because we want to
        //    add an own schemes (search:// and client://) and its corresponding
        //    protocol handlers. So if you enter "search:something on the web", your
        //    search request "something on the web" is forwarded to www.google.com
        CefApp.addAppHandler(new AppHandler(args));

        //    By calling the method createClient() the native part
        //    of JCEF/CEF will be initialized and an  instance of
        //    CefClient will be created. You can create one to many
        //    instances of CefClient.
        client_ = myApp.createClient();

        // 2) You have the ability to pass different handlers to your
        //    instance of CefClient. Each handler is responsible to
        //    deal with different informations (e.g. keyboard input).
        //
        //    For each handler (with more than one method) adapter
        //    classes exists. So you don't need to override methods
        //    you're not interested in.
        DownloadDialog downloadDialog = new DownloadDialog(this);
        client_.addContextMenuHandler(new ContextMenuHandler(this));
        client_.addDownloadHandler(downloadDialog);
        client_.addDragHandler(new DragHandler());
        client_.addGeolocationHandler(new GeolocationHandler(this));
        client_.addJSDialogHandler(new JSDialogHandler());
        client_.addDialogHandler(new FileDialogHandler());
        client_.addKeyboardHandler(new KeyboardHandler());
        client_.addRequestHandler(new RequestHandler(this));

        //    Beside the normal handler instances, we're registering a MessageRouter
        //    as well. That gives us the opportunity to reply to JavaScript method
        //    calls (JavaScript binding). We're using the default configuration, so
        //    that the JavaScript binding methods "cefQuery" and "cefQueryCancel"
        //    are used.
        CefMessageRouter msgRouter = CefMessageRouter.create();
        /*msgRouter.addHandler(new MessageRouterHandler(), true);
        msgRouter.addHandler(new MessageRouterHandlerEx(client_), false);*/
        msgRouter.addHandler(new CefQueryMessageRouterHandler(), true);
        client_.addMessageRouter(msgRouter);

        // 2.1) We're overriding CefDisplayHandler as nested anonymous class
        //      to update our address-field, the title of the panel as well
        //      as for updating the status-bar on the bottom of the browser
        client_.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                control_pane_.setAddress(browser, url);
            }
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                setTitle(title);
            }
            @Override
            public void onStatusMessage(CefBrowser browser, String value) {
                status_panel_.setStatusText(value);
            }
        });

        // 2.2) To disable/enable navigation buttons and to display a prgress bar
        //      which indicates the load state of our website, we're overloading
        //      the CefLoadHandler as nested anonymous class. Beside this, the
        //      load handler is responsible to deal with (load) errors as well.
        //      For example if you navigate to a URL which does not exist, the
        //      browser will show up an error message.
        client_.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                    boolean canGoBack, boolean canGoForward) {
                control_pane_.update(browser, isLoading, canGoBack, canGoForward);
                status_panel_.setIsInProgress(isLoading);

                if (!isLoading && !errorMsg_.isEmpty()) {
                    browser.loadString(errorMsg_, control_pane_.getAddress());
                    errorMsg_ = "";
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                    String errorText, String failedUrl) {
                if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED) {
                    errorMsg_ = "<html><head>";
                    errorMsg_ += "<title>Error while loading</title>";
                    errorMsg_ += "</head><body>";
                    errorMsg_ += "<h1>" + errorCode + "</h1>";
                    errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
                    errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
                    errorMsg_ += "</body></html>";
                    browser.stopLoad();
                }
            }
            
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
            	System.out.println("onLoadEnd httpStatusCode : " + httpStatusCode);
            	String url = browser.getURL();
            	if(url.indexOf("https://www.baidu.com") != -1){
            		//browser.executeJavaScript("function getTop(e){   var offset=e.offsetTop;   if(e.offsetParent!=null){     offset+=getTop(e.offsetParent);   }            return offset;}function getLeft(e){   var offset=e.offsetLeft;   if(e.offsetParent!=null){      offset+=getLeft(e.offsetParent);   }    return offset;}var interval = setInterval(function(){if(document.querySelector('input[value=\"百度一下\"]') != null){console.log('find a');clearInterval(interval);var x = getLeft(document.querySelector('input[value=\"百度一下\"]'));var y = getTop(document.querySelector('input[value=\"百度一下\"]'));var w = document.querySelector('input[value=\"百度一下\"]').offsetWidth;var h = document.querySelector('input[value=\"百度一下\"]').offsetHeight;window.cefQuery({request: 'ClickRegion:'+x+'###'+y+'###'+w+'###'+h,onSuccess: function(response) {},onFailure: function(error_code, error_message) {console.log('ClickRegion error : ' + error_message);}});}}, 3000);", null, 99999);
            	}
            	if(TaskUtils.currentTaskType != null && !TaskUtils.currentStepProcessing) {
            		TaskUtils.currentStepProcessing = true;
            		/*Jedis jedis = RedisPool.pool.getResource();
            		jedis.hset(TaskUtils.taskId, "current", String.valueOf(TaskUtils.currentTaskStep));
		    		jedis.close();*/
            		List<Map<String, String>> list = TaskUtils.stepMap.get(TaskUtils.currentTaskType);
            		System.out.println("TaskUtils step total : " + list.size());
            		System.out.println("TaskUtils.currentTaskStep : " + TaskUtils.currentTaskStep);
            		Map<String, String> map = list.get(TaskUtils.currentTaskStep-1);
            		String jsCode = map.get("js");
            		String targetUrl = map.get("url");
            		if(url.indexOf(targetUrl) != -1) {
            			jsCode = "var data=" + TaskUtils.jsonData + ";" + jsCode;
            			System.out.println("js code : " + jsCode);
            			TaskUtils.currentTaskStep++;
                		browser.executeJavaScript(jsCode, null, 99999);
            		} else {
            			TaskUtils.currentStepProcessing = false;
            		}
            	}
			}
        });

        // 3) Before we can display any content, we require an instance of
        //    CefBrowser itself by calling createBrowser() on the CefClient.
        //    You can create one to many browser instances per CefClient.
        //
        //    If the user has specified the application parameter "--cookie-path="
        //    we provide our own cookie manager which persists cookies in a directory.
        CefRequestContext requestContext = null;
        if (cookiePath != null) {
            cookieManager_ = CefCookieManager.createManager(cookiePath, false);
            requestContext = CefRequestContext.createContext(new CefRequestContextHandlerAdapter() {
                @Override
                public CefCookieManager getCookieManager() {
                    return cookieManager_;
                }
            });
        } else {
            cookieManager_ = CefCookieManager.getGlobalManager();
        }
        browser_ = client_.createBrowser(
                "", osrEnabled, transparentPaintingEnabled, requestContext);

        //Last but not least we're setting up the UI for this example implementation.
        getContentPane().add(createContentPanel(), BorderLayout.CENTER);
        MenuBar menuBar =
                new MenuBar(this, browser_, control_pane_, downloadDialog, cookieManager_);

        menuBar.addBookmark("Binding Test", "client://tests/binding_test.html");
        menuBar.addBookmark("Binding Test 2", "client://tests/binding_test2.html");
        menuBar.addBookmark("Download Test", "http://cefbuilds.com");
        menuBar.addBookmark("Geolocation Test", "http://slides.html5rocks.com/#geolocation");
        menuBar.addBookmark("Login Test (username:pumpkin, password:pie)",
                "http://www.colostate.edu/~ric/protect/your.html");
        menuBar.addBookmark("Certificate-error Test", "https://www.k2go.de");
        menuBar.addBookmark("Resource-Handler Test", "http://www.foo.bar/");
        menuBar.addBookmark("Resource-Handler Set Error Test", "http://seterror.test/");
        menuBar.addBookmark(
                "Scheme-Handler Test 1: (scheme \"client\")", "client://tests/handler.html");
        menuBar.addBookmark(
                "Scheme-Handler Test 2: (scheme \"search\")", "search://do a barrel roll/");
        menuBar.addBookmark("Spellcheck Test", "client://tests/spellcheck.html");
        menuBar.addBookmark("LocalStorage Test", "client://tests/localstorage.html");
        menuBar.addBookmark("Transparency Test", "client://tests/transparency.html");
        menuBar.addBookmarkSeparator();
        menuBar.addBookmark(
                "javachromiumembedded", "https://bitbucket.org/chromiumembedded/java-cef");
        menuBar.addBookmark("chromiumembedded", "https://bitbucket.org/chromiumembedded/cef");
        setJMenuBar(menuBar);
        TaskUtils.browser = browser_;
        new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					System.out.println("Exception : " + e1.toString());
				}
		        System.out.println("menuBar height : " + menuBar.getHeight());
		        System.out.println("control_pane_ height : " + control_pane_.getHeight());
		        Insets set = getInsets();
		        int titlebarH=set.top;
		        System.out.println("frame header height : " + titlebarH);
		        TaskUtils.browserHeaderHeight = menuBar.getHeight() + control_pane_.getHeight() + titlebarH;
				String redisTaskQueueName = Configuration.getValue("redis_task_queue");
				long beginTime = System.currentTimeMillis();
			    while(true) {
			    	try{
			    		if(!TaskUtils.finished) {
				    		System.out.println("Task status : last task is executing.");
				    		System.out.println("last task has cost time : " + 
				    				(System.currentTimeMillis() - beginTime)/1000 + "s.");
				    		if((System.currentTimeMillis() - beginTime)/1000 > Integer.valueOf(Configuration.getValue(TaskUtils.currentTaskType + "_timeout"))) {
				    			TaskUtils.browser.stopLoad();
				    			TaskUtils.currentTaskStep = 1;
				            	TaskUtils.currentTaskType = null;
				            	TaskUtils.finished = true;
				            	TaskUtils.currentStepProcessing = false;
				            	TaskUtils.recordInfoMap.clear();
				            	TaskUtils.browser.loadURL("client://tests/binding_test.html");
								Map<String, Object> taskMap = JSON.parseObject(TaskUtils.jsonData, Map.class);
				            	MySqlUtils.updateTaskRecordStatus(Integer.valueOf((String)taskMap.get("task_id")), "任务超时，已被终止");
				    		}
				    		Thread.sleep(10000);
				    		continue;
				    	}
				    	String task = null;
				    	Jedis jedis = RedisPool.pool.getResource();
			    		task = jedis.lpop(redisTaskQueueName);
			    		jedis.close();
			    		if(task != null) {
			    			System.out.println("current task : " + task);
							Map<String, Object> taskMap = JSON.parseObject(task, Map.class);
							String task_id = (String)taskMap.get("task_id");
							String task_type = (String)taskMap.get("task_type");
							TaskUtils.jsonData = task;
							TaskUtils.finished = false;
							TaskUtils.taskId = task_id;
							TaskUtils.currentTaskType = task_type;
							if("australia".equals(task_type)) {
								TaskUtils.attachFilesList = (List<Map<String, Object>>) taskMap.get("attachFilesList");
								System.out.println("TaskUtils attachFileList : " + TaskUtils.attachFilesList.toString());
								TaskUtils.dataMap = taskMap;
								System.out.println("TaskUtils dataMap : " + TaskUtils.dataMap.toString());
							}
							TaskUtils.browser.loadURL(TaskUtils.stepMap.get(TaskUtils.currentTaskType).get(TaskUtils.currentTaskStep-1).get("url"));
							beginTime = System.currentTimeMillis();
							MySqlUtils.updateTaskRecordStatus(Integer.valueOf(task_id), "正在进行自动化填表");
						} else {
							System.out.println("no task, sleep 5s...");
							Thread.sleep(5000);
						}
			    	} catch(Exception e) {
			    		System.out.println("Exception : " + e.toString());
			    	}					
			    }				
			}
		}).start();
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        control_pane_ = new ControlPanel(browser_);
        status_panel_ = new StatusPanel();
        contentPanel.add(control_pane_, BorderLayout.NORTH);

        // 4) By calling getUIComponen() on the CefBrowser instance, we receive
        //    an displayable UI component which we can add to our application.
        contentPanel.add(browser_.getUIComponent(), BorderLayout.CENTER);

        contentPanel.add(status_panel_, BorderLayout.SOUTH);
        return contentPanel;
    }
}
