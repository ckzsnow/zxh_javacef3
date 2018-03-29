package tests.detailed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cef.browser.CefBrowser;

public class TaskUtils {

	public static Map<String, List<Map<String, String>>> stepMap = new HashMap<>();
	 
	public static int  browserHeaderHeight = 0;
	
	public static CefBrowser browser = null;
	
	public static String currentTaskType = null;
	
	public static int currentTaskStep = 1;
	
	public static boolean currentStepProcessing = false;
	
	public static String jsonData = "";
	
	public static String fileUploadFilePath = "";
	
	public static String fileUploadFileType = "";
	
	public static int fileUploadTotal = 0;
	
	public static int fileUploadCurrentIndex = 0;
	
	public static boolean fileUploadEnterUploadPage = false;
	
	public static String taskId = "1";
	
	public static volatile boolean finished = true;
	
	public static Map<String, String> recordInfoMap = new HashMap<>();
	
	public static List<Map<String, Object>> attachFilesList = new ArrayList<>();
	
	public static Map<String, Object> dataMap = new HashMap<>();
	
	public static int familyMemberIndex = 0;
	
	public static int travelWithMemberIndex = 0;
	
	public static int attachUploadCurrentIndex = 0;
	
	static {
		String rootPath = Configuration.getValue("jscode_path");
		File root = new File(rootPath);
		File[] files = root.listFiles();
		for(File file : files){
			if(file.isDirectory()) {
				Map<String, String> jsMap = new HashMap<>();
				String countryName = file.getName();
				System.out.println(countryName);
				File[] jsFiles = file.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if(name.endsWith(".js"))
							return true;
						else
							return false;
					}
				});
				File[] urlFiles = file.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if(name.endsWith(".txt"))
							return true;
						else
							return false;
					}
				});
				for(File jsFile : jsFiles){
					BufferedReader br;
					StringBuilder sb = new StringBuilder();
					try {
						br = new BufferedReader(new InputStreamReader(
								new FileInputStream(jsFile)));
						String line = null;
						while((line = br.readLine())!=null) {
							line = line.replaceAll("\r", "")
									.replaceAll("\n", "")
									.replaceAll("\t", "");
							sb.append(line);
						}
					} catch (IOException e) {
						System.out.println(e.toString());
					}
					jsMap.put(jsFile.getName().substring(0, jsFile.getName().indexOf(".")), sb.toString());
				}
				List<Map<String, String>> stepDetailList = new ArrayList<>();
				File urlFile = urlFiles[0];
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(urlFile)));
					String line = null;
					while((line = br.readLine())!=null) {
						String[] urlInfos = line.split("=");
						Map<String, String> infoMap = new HashMap<>();
						infoMap.put("url", urlInfos[1]);
						infoMap.put("js", jsMap.get(urlInfos[0]));
						stepDetailList.add(infoMap);
					}
				} catch (IOException e) {
					System.out.println(e.toString());
				}
				stepMap.put(countryName, stepDetailList);
			}
		}
	}
	
	public static void main(String[] args){
		System.out.println(stepMap.toString());
	}
}
