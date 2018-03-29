package tests.detailed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class MySqlUtils {

	private static final String URL = "jdbc:mysql://117.48.211.42:3306/autofillform?useUnicode=true&characterEncoding=UTF-8";
    private static final String NAME = "root";
    private static final String PASSWORD = "Cckzcbm110";
	
    static {
	    try {  
	        Class.forName("com.mysql.jdbc.Driver");  
	    } catch (ClassNotFoundException e) {  
	        System.out.println(e.toString());
	    }
    }
    
    public static void writeInfoToMysql(Map<String, String> map) {
    	System.out.println("MySqlUtils writeInfoToMysql : " + map.toString());
    	Connection conn;
		try {
			String json = JSON.toJSONString(map);
			conn = DriverManager.getConnection(URL, NAME, PASSWORD);
			Statement stmt = conn.createStatement();  
	        stmt.execute("update task set status='任务已完成', record_data='"+json+"' where id="+TaskUtils.taskId);
	        stmt.close();  
	        conn.close();
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
    }
    
    public static boolean updateTaskRecordStatus(long id, String status){
		boolean updateSuccess = false;
		try {
			Connection conn = DriverManager.getConnection(URL, NAME, PASSWORD);
			Statement stmt = conn.createStatement();
			String sql = "update task set status='"+status+"' where id=" + id;
            int ret = stmt.executeUpdate(sql);
            if(ret > 0) updateSuccess = true;
		} catch (SQLException e) {
			System.out.println("updateTaskRecordStatus" + e.toString());
		}
		return updateSuccess;
	}
    
    public static void main(String[] args){
    	Map<String, String> map = new HashMap<>();
    	map.put("test", "test");
    	map.put("isTrue", "true");
    	writeInfoToMysql(map);
    }
}