package com.oribo.database;

import java.sql.ResultSet;  
import java.sql.SQLException;

import com.mysql.jdbc.Statement;
import com.oribo.common.TestcaseFrame;
import com.oribo.dataprovider.Constant;  
public class Query {
	static String sql = null;  
    static DBHelperMysql db = null;  
   // static DBHelperMysql db = null;  
    static ResultSet ret = null;  
  
    public static void main(String[] args) {  
  /*      sql = "select * from thirdAccount";//SQL语句  
        db= new DBHelperMysql(sql);//创建DBHelper对象    
        db=new DBHelperSqlite(sql);
        try {  
            ret = db.pst.executeQuery();//执行语句，得到结果集  
            while (ret.next()) {  
                String uid = ret.getString(1);  
                String ufname = ret.getString(2);  
                String ulname = ret.getString(3);  
                String udate = ret.getString(4);  
                System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
            }//显示数据  
            ret.close();  
            db.close();//关闭连接  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  */
     String sql="select * from account2 where email='"+TestcaseFrame.getaccount().getAccount()+"' or phone='"+TestcaseFrame.getaccount().getAccount()+"'";
     System.out.println(sql);
     String result=executSql(sql,2,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
  
    }  
    
   public static String executSql(String sql,int findindex,String url,String username,String password)
   {
	   db=new DBHelperMysql(sql,url,username,password);
	   String find = null;
	   try {  
           ret = db.pst.executeQuery();//执行语句，得到结果集  
           while (ret.next()) {  
               find = ret.getString(findindex);  
               System.out.println("查找到了什么"+find);
         
           }//显示数据  
           ret.close();  
           db.close();//关闭连接  
       } catch (SQLException e) {  
           e.printStackTrace();  
       }  
	   return find;
	   
   }
   
  

}
