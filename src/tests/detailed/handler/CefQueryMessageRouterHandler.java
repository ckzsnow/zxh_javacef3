// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package tests.detailed.handler;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import com.alibaba.fastjson.JSON;

import tests.detailed.ABBYYOCRUtils;
import tests.detailed.Configuration;
import tests.detailed.MySqlUtils;
import tests.detailed.TaskUtils;

public class CefQueryMessageRouterHandler extends CefMessageRouterHandlerAdapter {
    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent,
            CefQueryCallback callback) {
    	System.out.println("onQuery request : " + request);
        if (request.indexOf("ClickRegion:") == 0) {
        	System.out.println("ClickRegion : " + request);
        	String pos = request.substring(request.indexOf(":")+1);
        	String[] posxy = pos.split("###");
        	boolean clickSuccess = false;
        	try {
        		int posX = Integer.valueOf(posxy[0]);
            	int posY = Integer.valueOf(posxy[1]);
            	int width = Integer.valueOf(posxy[2]);
            	int height = Integer.valueOf(posxy[3]);
            	TaskUtils.fileUploadFilePath = (String)TaskUtils.dataMap.get(posxy[4]);
            	System.out.println("TaskUtils.fileUploadFilePath : " + TaskUtils.fileUploadFilePath);
            	browser.sendMouseEvent(new MouseEvent(browser.getUIComponent(), MouseEvent.MOUSE_PRESSED, 100, 0, posX+(width/2), posY+(height/2), 1, false, MouseEvent.BUTTON1));
    			try {
    				Thread.sleep(500);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			browser.sendMouseEvent(new MouseEvent(browser.getUIComponent(), MouseEvent.MOUSE_RELEASED, 100, 0, posX+(width/2), posY+(height/2), 1, false, MouseEvent.BUTTON1));
    			clickSuccess = true;
        	} catch(Exception ex) {
        		System.out.println(ex.toString());
        	}
			callback.success(new StringBuilder((clickSuccess?"click true":"click false")).toString());
            return true;
        } else if (request.indexOf("AllFinish:") == 0) {
        	TaskUtils.currentTaskStep = 1;
        	TaskUtils.currentTaskType = null;
        	TaskUtils.finished = true;
        	MySqlUtils.writeInfoToMysql(TaskUtils.recordInfoMap);
        	TaskUtils.recordInfoMap.clear();
			callback.success(new StringBuilder(("all finish")).toString());
            return true;
        } else if (request.indexOf("FileUpload:") == 0) {
        	String filePath = request.substring(request.indexOf(":")+1);
        	TaskUtils.fileUploadFilePath = filePath;
			callback.success(new StringBuilder(("ok")).toString());
            return true;
        } else if (request.indexOf("RecordInfo:") == 0) {
        	String infos = request.substring(request.indexOf(":")+1);
        	String[] splitInfos = infos.split(";");
        	for(String splitInfo : splitInfos){
        		String[] detailInfos = splitInfo.split("=");
        		TaskUtils.recordInfoMap.put(detailInfos[0], detailInfos[1]);
        	}
			callback.success(new StringBuilder(("ok")).toString());
            return true;
        } else if (request.indexOf("CurrentStepFinish:") == 0) {
        	TaskUtils.currentStepProcessing = false;
			callback.success(new StringBuilder(("ok")).toString());
            return true;
        } else if (request.indexOf("SkipStep:") == 0) {
        	String steps = request.substring(request.indexOf(":")+1);
        	TaskUtils.currentStepProcessing = true;
        	if(steps == null || steps.isEmpty()) {
        		TaskUtils.currentTaskStep = TaskUtils.currentTaskStep + 1;
        	} else {
        		TaskUtils.currentTaskStep = TaskUtils.currentTaskStep + Integer.valueOf(steps);
        	}
			List<Map<String, String>> list = TaskUtils.stepMap.get(TaskUtils.currentTaskType);
    		System.out.println("TaskUtils step total : " + list.size());
    		System.out.println("TaskUtils.currentTaskStep : " + TaskUtils.currentTaskStep);
    		Map<String, String> map = list.get(TaskUtils.currentTaskStep-1);
    		String jsCode = map.get("js");
    		jsCode = "var data=" + TaskUtils.jsonData + ";" + jsCode;
    		System.out.println("js code : " + jsCode);
    		TaskUtils.currentTaskStep++;
        	TaskUtils.browser.executeJavaScript(jsCode, null, 22222222);
        	callback.success(new StringBuilder(("ok")).toString());
            return true;
        } else if (request.indexOf("AllUploadFinish:") == 0) {
        	TaskUtils.currentTaskStep = TaskUtils.currentTaskStep + 2;
        	TaskUtils.currentStepProcessing = false;
        	callback.success("ok");
            return true;
        } else if(request.indexOf("getUploadInfo:") == 0) {
        	System.out.println("getUploadInfo ======= return attachFilesList");
        	/*if(TaskUtils.attachUploadCurrentIndex >= TaskUtils.attachFilesList.size()) {
        		callback.success("");
        	} else {
        		System.out.println(TaskUtils.attachFilesList.get(TaskUtils.attachUploadCurrentIndex));
        		String uploadBtnInfo = JSON.toJSONString(TaskUtils.attachFilesList.get(TaskUtils.attachUploadCurrentIndex));
        		callback.success(uploadBtnInfo);
        	}*/
        	String uploadInfo = JSON.toJSONString(TaskUtils.attachFilesList);
        	callback.success(uploadInfo);
        	return true;
        } else if(request.indexOf("currentAttachFinish:") == 0) {
        	TaskUtils.attachUploadCurrentIndex ++;
        	TaskUtils.currentTaskStep = TaskUtils.currentTaskStep - 2;
        	TaskUtils.currentStepProcessing = false;
        	callback.success("ok");
        	return true;
        } else if(request.indexOf("getFamilyInfo:") == 0) {
        	System.out.println("TaskUtils familyMemberIndex : " + TaskUtils.familyMemberIndex);
        	List<Map<String, Object>> familyMemberList = (List<Map<String, Object>>) TaskUtils.dataMap.get("family_members");
        	if(familyMemberList == null || familyMemberList.size() == 0) {
        		callback.success("");
        	} else {
        		System.out.println("TaskUtils familyMemberIndex : " + familyMemberList.size());
        		if(TaskUtils.familyMemberIndex >= familyMemberList.size()) {
        			callback.success("");
        		} else {
        			String familyMemberInfo = JSON.toJSONString(familyMemberList.get(TaskUtils.familyMemberIndex));
        			callback.success(familyMemberInfo);
        		}
        	}
        	return true;
        } else if(request.indexOf("addFamilyMemberFinish:") == 0) {
        	TaskUtils.familyMemberIndex ++;
        	TaskUtils.currentTaskStep = TaskUtils.currentTaskStep - 2;
        	TaskUtils.currentStepProcessing = false;
        	callback.success("ok");
        	return true;
        } else if(request.indexOf("getTravelWithMemberInfo:") == 0) {
        	System.out.println("TaskUtils travelWithMemberIndex : " + TaskUtils.travelWithMemberIndex);
        	List<Map<String, Object>> travelWithMemberList = (List<Map<String, Object>>) TaskUtils.dataMap.get("contacts");
        	if(travelWithMemberList == null || travelWithMemberList.size() == 0) {
        		callback.success("");
        	} else {
        		System.out.println("TaskUtils travelWithMemberList : " + travelWithMemberList.size());
        		if(TaskUtils.travelWithMemberIndex >= travelWithMemberList.size()) {
        			callback.success("");
        		} else {
        			String travelWithMemberInfo = JSON.toJSONString(travelWithMemberList.get(TaskUtils.travelWithMemberIndex));
        			callback.success(travelWithMemberInfo);
        		}
        	}
        	return true;
        } else if(request.indexOf("addTravelWithMemberFinish:") == 0) {
        	TaskUtils.travelWithMemberIndex ++;
        	TaskUtils.currentTaskStep = TaskUtils.currentTaskStep - 2;
        	TaskUtils.currentStepProcessing = false;
        	callback.success("ok");
        	return true;
        } else if (request.indexOf("SkipPageStep:") == 0) {
        	String steps = request.substring(request.indexOf(":")+1);
        	TaskUtils.currentStepProcessing = false;
        	TaskUtils.currentTaskStep = TaskUtils.currentTaskStep + Integer.valueOf(steps);
        	callback.success(new StringBuilder(("ok")).toString());
            return true;
        } else if(request.indexOf("GetIndianCode:") == 0) {
        	String ret = ABBYYOCRUtils.ocrImage(Configuration.getValue("verify_code_save_path")+"\\code.jpg");
        	callback.success(ret);
            return true;
        }
        return false;
    }
}
