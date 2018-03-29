package tests.detailed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

	private static Map<String, String> configMap = new HashMap<>();
	
	static {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("config.txt")));
			String line = null;
			while((line = br.readLine())!=null) {
				if(line.startsWith("#") || line.isEmpty()) continue;
				String[] args = line.replaceAll(" ", "").split("=");
				configMap.put(args[0], args[1]);
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public static String getValue(String key){
		return configMap.containsKey(key) ? configMap.get(key) : "";
	}
	
	public static void setValue(String key, String value){
		configMap.put(key, value);
	}
	
	public static void main(String[] args){
		System.out.println(Configuration.getValue("test"));
	}
	
}
