package com.oribo.common;

/**
 * 所有测试类应继承此类，一些公共的操作函数都放此，另外SQL查询语句也在这里
 */
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import com.oribo.dataprovider.AppBean;
import com.oribo.dataprovider.Constant;
import com.oribo.dataprovider.UserInfo;
import com.oribo.log.Log;
import com.oribo.ReadExcelCase.DataBean;
import com.oribo.ReadExcelCase.ExcelData;
import com.oribo.ReadExcelCase.ReadExcel;
import com.oribo.database.DBHelperMysql;
import com.oribo.database.Query;
import com.oribo.report.CreateHtmlreport;
import com.oribo.report.MessageOutput;
import com.oribo.report.SendTestReportEMS;
import com.oribo.utils.FileOperate;
import com.oribo.utils.TimeUtils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

public class TestcaseFrame {
	public static  Map<Integer, DataBean>  bean = new HashMap<>();
	Logger mLogger;
	public String appport ;
	public String appudid;
	public String testphone;
	public String appplatformVersion;
	public String appapk;
	public String password;
	public String packagename;
	public static  String picname;
	FileHandler fileHandler;
	static Logger logger;
	AppBean   appbean = AppBean.getAppBean();
	static AndroidDriver<AndroidElement>  driver = null;
	static UserInfo account=UserInfo.getUserInfo();
    
	public static UserInfo getaccount()
	{    
		//从Excel读取账号信息
		account.setAccount(ReadExcel.readsimpledata(1,1,0));
		System.out.println("密码"+ReadExcel.readsimpledata(1,1,1));
		account.setPassWord(ReadExcel.readsimpledata(1,1,1));
		return account;
	}
	
	
	public void testSetUp()
	{
	
		
		File classpathRoot = new File(System.getProperty("user.dir"));//本地的路径
		File appDir = new File(classpathRoot, "apps");//apk 存放的路径
		File app = new File(appDir, appbean.getApk());//apk 的名字	
		//android Appium 的 基础参数的设置
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("deviceName", "Android Emulator");	
		capabilities.setCapability("device", "Selendroid");//测试H5页页
		capabilities.setCapability("platformVersion", appbean.getPlatformVersion() );
		capabilities.setCapability("udid", appbean.getUid());
		capabilities.setCapability("app", app.getAbsolutePath()); 	
		//capabilities.setCapability( "automationName","Selendroid");//这句话设置可以获取toast 消息
		//capabilities.setCapability( "automationName","uiautomator2");
		capabilities.setCapability("appPackage", "com.orvibo.homemate");
		capabilities.setCapability("appActivity", "com.orvibo.homemate.common.launch.LaunchActivity");
		capabilities.setCapability("unicodeKeyboard", "True");  
		capabilities.setCapability("resetKeyboard", "True"); 
		capabilities.setCapability("sessionOverride", true); // 每次启动时覆盖session，否则第二次后运行会报错不能新建session
		//capabilities.setCapability("setWebContentsDebuggingEnabled", "True");	
		//capabilities.setCapability("noSign", "True");
		capabilities.setCapability("noReset", true);//实现app不是每次都安装
		packagename=capabilities.getCapability("appPackage").toString();
		try {
			driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);//全局等待20秒
		
	
	}
	
	/**
	 * 登录
	 */
	public void logging() {
		newSleep(1);
		if(driver.currentActivity().equals(".user.LoginActivity"))
		{
		//登录页面
			List<AndroidElement> edittext = driver.findElementsByClassName("android.widget.EditText"); 
			Assert.assertTrue(edittext.size()>0, "未找到输入框");
			 AppOperate.clear(edittext.get(0), "清除账号框");
			 AppOperate.sendKeys(edittext.get(0), "输入账号", getaccount().getAccount());
			 AppOperate.clear(edittext.get(1), "清除密码框");
			 AppOperate.sendKeys( edittext.get(1), "输入密码", getaccount().getPassWord());
		     AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/login_btn")), "点击登录按扭");
		}

	}
	/**
	 * 获取包名
	 */
	public String getApppackagename()
	{
		return packagename;
	}
	/**
	 * 公共部分
	 * 点击头像后，点击取消
	 * 点击头像后，点击拍照
	 * 刚进入拍照状态，取消
	 * 进入拍照状态，点击拍照
	 * 已拍照，点击拍照页面中间的撤回按钮
	 * 已拍照，点击拍照页面右侧的√
	 * 相片裁剪页面，拉伸或不拉伸方框区域，点击确定
	 * 相片裁剪页面，点击返回或取消按钮
	 * 
	 */
	
	public void setphoto(AndroidElement photo)
	{
		       //点击头像按扭，查看是否有图片选择
				//判断是否弹出选择框
				List<AndroidElement> list=driver.findElementsByClassName("android.widget.TextView");
				Assert.assertEquals(list.size(), 3);
				AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"取消\")"), "点击选择框中的“取消”按扭");
				Assert.assertFalse(driver.getPageSource().contains("拍照"), "点击取消选择框未消失");
				//再次操作，点击拍照
				AppOperate.click(photo, "个人信息中点击“头像”");
				AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"拍照\")"), "点击选择框中的“拍照”按扭");
				//进入拍照界面点击取消
				AppOperate.click(driver.findElement(By.id("com.android.camera:id/v6_btn_cancel")), "拍照界面点击取消");
				//重新进入拍照界面
			    entercamera(photo);
				//点击拍照按扭
				AppOperate.click(driver.findElement(By.id("com.android.camera:id/v6_shutter_button_internal")), "点击拍照按扭");
				AppOperate.click(driver.findElement(By.id("com.android.camera:id/v6_btn_done")), "点击选择按扭");
				//裁剪界面点击取消
				AppOperate.click(driver.findElement(By.id("com.miui.gallery:id/cancel")), "裁剪界面点击“取消”");
				//判断是否回到个人信息界面
				AppOperate.exitElement("个人信息", driver);
				
				//重新点击头像进入 拍照
				setphotobycamera(photo);
	}
	
	/**
	 * 点击头像后弹框中点击进入拍照界面
	 */
	
	public void entercamera(AndroidElement photo)
	{
		//AndroidElement photo=driver.findElement(By.id("com.orvibo.homemate:id/userPicImageView"));
		AppOperate.click(photo, "点击“头像”");
		//判断是否弹出选择框
		List<AndroidElement> list=driver.findElementsByClassName("android.widget.TextView");
		Assert.assertEquals(list.size(), 3);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"拍照\")"), "点击选择框中的“拍照”按扭");

	}
	/**
	 * 点击头像后拍照设置头像
	 */
	public void setphotobycamera(AndroidElement photo)
	{
		entercamera(photo);
		AppOperate.click(driver.findElement(By.id("com.android.camera:id/v6_shutter_button_internal")), "点击拍照按扭");
		AppOperate.click(driver.findElement(By.id("com.android.camera:id/v6_btn_done")), "点击选择按扭");
		//裁剪界面点击确定
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"确定\")"), "裁剪界面点击确定");
		
	}
	
	/**
	 * 进入个人信息
	 */
	
	public void enterPersoninfo()
	{   
		newSleep(1);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/iv_personal_user_icon")), "点击个人信息头像");

		
	}
	
	
	
	
	
	
	public void enterMyhost()
	{   
		newSleep(1);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的主机\")"), "点击'我的主机'");
		
		
	}
	public void enterscene()
	{
		 newSleep(1);
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
	}
	public void enterMore()
	{   
		newSleep(1);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的主机\")"), "点击'我的主机'");
		AppOperate.swipeToUp(driver, "向上滑屏");
		AppOperate.click((AndroidElement) driver.findElementById("com.orvibo.homemate:id/moreTextView"), "点击更多");
		
	}
	public void enterroommanage()
	{
		newSleep(1);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"房间管理\")"), "点击'房间管理'");
	}

	
	public static AndroidDriver<AndroidElement> getDriver()
	{
		return driver;
	}
	
	
	/**
	 * 判断是否登录账号
	 * @return
	 */
	public boolean isLogin()
	{
		return true;
	}
	
	
	//睡眠
	public static void newSleep(int p_time)
	{
		try
		{
			Thread.sleep(p_time * 1000);
		} catch (InterruptedException e)
		{

		}

	}
	
	/**
	 * 按钮不能点击返回期望结果
	 * @param btn
	 * @param bean
	 * @param logge
	 * @return
	 */
	public String  btnunenanled(AndroidElement  btn,DataBean bean,Logger  logge){
		try {
			if (!btn.isEnabled()) {
				return bean.getExpectValue();
			} else{
				return btn.getText()+"--能点击"	;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return e.toString();
		}	

	}
	
	/**
	 * 按钮能点击返回期望结果
	 * @param btn
	 * @param bean
	 * @param logge
	 * @return
	 */
	public String  btniSenanled(AndroidElement  btn,DataBean bean,Logger  logge){
		try {
			if (btn.isEnabled()) {
				return bean.getExpectValue();
			} else{
				return btn.getText()+"--不能点击"	;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return e.toString();
		}
	}
	
	/**
	 * 封装AssertEquals方法，判断是否与预期结果一致，如果正确actualValue传DataBean.getExpectValue()值，错误则传实际值
	 * @param driver
	 * @param info：验证信息说明
	 * @param expectedValue：期望结果
	 * @param actualValue：实际结果
	 */
	/*public static String newAssertEquals(AppiumDriver driver,DataBean  bean,Object actualValue,Logger mlogger,String loginfo)
	{

		bean.setActualValue(String.valueOf(actualValue));
		if (bean.getExpectValue().equals(bean.getActualValue()))
		{
			LoggerUtil.logOutput(mlogger, loggerLevel.LEVLEINFO, bean.getInstructions() +"用例结果  ：      "+MessageOutput.mActualResult+ MessageOutput.mExpectedResult + MessageOutput.mMatch);
			//mlogger.(loginfo+MessageOutput.mActualResult+ MessageOutput.mExpectedResult + MessageOutput.mMatch);
			CreateHtmlreport.writeToLog(bean.getTestCaseName(), bean.getInstructions(),bean.getExpectValue(), "跟期望值一样", "PASS");
			return MessageOutput.SUCCESS;
		} else
		{			
			
			LoggerUtil.logOutput(mlogger, loggerLevel.LEVELSERVER, bean.getInstructions() +"用例结果  ：      "+actualValue);
			mlogger.severe(bean.getInstructions() +"用例结果  ：      "+loginfo+MessageOutput.mActualResult+ MessageOutput.mExpectedResult + MessageOutput.mNot
					+ MessageOutput.mMatch);
			CreateHtmlreport.writeToLog(bean.getTestCaseName(),bean.getInstructions(),bean.getExpectValue(), bean.getActualValue(), "FAIL");
			screenShort(driver, bean.getTestCaseName() + MessageOutput.mActualResult+ MessageOutput.mExpectedResult 
					+ MessageOutput.mNot+ MessageOutput.mMatch + "__截图",bean.getInstructions());
			//driver.closeApp();
			//driver.quit();// Assert结果为false，脚本结束运行
			return MessageOutput.FAIL;
		}
	}*/
	
	/**
	 * 封装verifyEquals方法
	 * @param driver
	 * @param info：验证信息说明
	 * @param expectedValue：期望结果
	 * @param actualValue：实际结果
	 */
	public static String newVerifyEquals (AppiumDriver driver, String info,Object instrut,Object expectedValue, Object actualValue)
	{
		if (expectedValue != null & expectedValue.equals(actualValue))
		{
			logger.info(MessageOutput.mActualResult+ MessageOutput.mExpectedResult + MessageOutput.mMatch);
			CreateHtmlreport.writeToLog(info,instrut, expectedValue, actualValue, "PASS");
			return "通过";
		} else
		{
			logger.severe(MessageOutput.mActualResult+ MessageOutput.mExpectedResult + MessageOutput.mNot
					+ MessageOutput.mMatch);
			CreateHtmlreport.writeToLog(info, instrut,expectedValue, actualValue, "FAIL");
			screenShort(driver, info + MessageOutput.mActualResult+ MessageOutput.mExpectedResult
					+ MessageOutput.mNot+ MessageOutput.mMatch + "__截图",instrut.toString());
			return "失败";
		}
	}

	
	
	
	/**
	 *  自动截图并保存，参数指定某个具体的driver和图片的名称
	 *    调用截图方法需要将浏览器的语言手动设置成中文；
	 *    filename为测试用例名称，可通过databean获取
	 * @param driver2
	 * @param captureName
	 */
	public static void screenShort(AppiumDriver driver2, String captureName,String filname)
	{   
		File screenShortFile = driver2.getScreenshotAs(OutputType.FILE);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy_MM_dd_hh_mm_ss");
		String dateStr = dateFormat.format(new Date());
		try
		{
			//此FileUtils 为org.apache.commons.io下面的类，进行文件的操作，复制文件
			FileUtils.copyFile(screenShortFile, FileOperate.getPicturePath(filname+"_"+AppBean.getAppBean().getPhone()+"_"+AppBean.getAppBean().getApk(), dateStr, captureName));

		} catch (IOException e)
		{
			throw new RuntimeException("截图失败：" + e);
		}
	}
	/**
	 * 测试结果截图，第二个参数传递测试用例名称
	 * @author sunnypeng
	 * @param driver
	 * @param addinfo
	 */
	
	
	public static void screenCapresult(AndroidDriver driver,String addinfo)
	{   
		String path=FileOperate.getScreencapFilePath();
		File screenShot = driver.getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(screenShot,new File(path,addinfo+TimeUtils.gettime()+".png"));
			picname=addinfo+TimeUtils.gettime()+".png";
		} catch (IOException e) {
			
			e.printStackTrace();
		}	

	}
	
	
	/**
	 * 测试对比截图
	 * @author sunnypeng
	 * @param driver
	 * @param addinfo
	 */
	
	
	public static void screenCapCompare(AndroidDriver driver,String filename)
	{   
		String path=FileOperate.getScreencapFilePath();
		File screenShot = driver.getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(screenShot,new File(path,filename+".png"));
			picname=filename+".png";
			Log.logInfo("已截图,图片名称为:"+picname);
		} catch (IOException e) {
			
			e.printStackTrace();
		}	

	}
	
	 
	 /**
	  * 查询某个设备的状态
	  */
	 public boolean devicestatu(String devicename)
	 {     
		 String sql=null;
		 boolean ifonline=false;
		 
			if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
			{
				sql="select deviceStatus.Online,device.deviceName from account2,deviceStatus,device,userGatewayBind "
						+ "where account2.userId=userGatewayBind.userId and userGatewayBind.uid=deviceStatus.uid "
						+ "and device.deviceId=deviceStatus.deviceId "
						+ "and account2.email='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and device.delFlag=0 and userGatewayBind.delFlag=0 "
						+ "and device.deviceName='"+devicename+"'";
			   }
			else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
			   {
				sql="select deviceStatus.Online,device.deviceName from account2,deviceStatus,device,userGatewayBind "
						+ "where account2.userId=userGatewayBind.userId and userGatewayBind.uid=deviceStatus.uid "
						+ "and device.deviceId=deviceStatus.deviceId "
						+ "and account2.phone='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and device.delFlag=0 and userGatewayBind.delFlag=0 "
						+ "and device.deviceName='"+devicename+"'";
			   }
			 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
			   try {  
				   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
		           while (ret.next()) {
		        	   if(ret.getInt(1)!=0)
		        	   {
		        		   ifonline=true;
		        		   Log.logInfo(devicename+"在线");
		        	   }
		        	   
		                   
		           }//显示数据 
		         
		           ret.close();  
		           db.close();//关闭连接  
		       } catch (SQLException e) {  
		           e.printStackTrace();  
		       }  
			   return ifonline;
	 }

	 /**
	  * 某个家庭有哪些情景,key为0代表系统场景，1为一般场景，系统场景不可编辑
	  */
	 public Map<String,String> scenenumbers(String familyname)
	 {     
		 String sql=null;
	//	 List<String> list=new ArrayList();
		 Map<String,String> map=new HashMap<>();
			if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
			{
				sql="select account2.email,scene.sceneName,scene.familyId,family.familyName from account2,scene,family"
						+ " where account2.userId=scene.userId and account2.email='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and scene.delFlag=0 and family.familyId=scene.familyId and"
						+ " family.familyName='"+familyname+"'";
			   }
			else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
			   {
				sql="select account2.email,scene.sceneName,scene.familyId,family.familyName from account2,scene,family"
						+ " where account2.userId=scene.userId and account2.phone='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and scene.delFlag=0 and family.familyId=scene.familyId and"
						+ " family.familyName='"+familyname+"'";
			   }
			System.out.println(sql);
			 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
			   try {  
				   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
		           while (ret.next()) {
		        	   String s=ret.getString(2);
		        	   if(s.contains("模式"))
		        	   {   
		        		   
		       	        s= s.substring(0,s.lastIndexOf("模"));
		       	        map.put(s, "0");
		       
		        	   }
		        	   else
		        	   {
		        	 //  list.add(s);
		        	   map.put(s, "1");
		        	   }
		                   
		           }//显示数据 
		         
		           ret.close();  
		           db.close();//关闭连接  
		       } catch (SQLException e) {  
		           e.printStackTrace();  
		       }  
			return map;
	 }
	 
	 /**
	  * 某个家庭有哪些联动
	  */
	 public List<String> linkagenumbers(String familyname)
	 {     
		 String sql=null;
		 List<String> list=new ArrayList();
			if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
			{
				sql="select account2.email,linkage.linkageName,linkage.familyId,family.familyName from account2,linkage,family"
						+ " where account2.userId=linkage.userId and account2.email='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and linkage.delFlag=0 and family.familyId=linkage.familyId and"
						+ " family.familyName='"+familyname+"'";
			   }
			else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
			   {
				sql="select account2.email,linkage.linkageName,linkage.familyId,family.familyName from account2,linkage,family"
						+ " where account2.userId=linkage.userId and account2.phone='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and linkage.delFlag=0 and family.familyId=linkage.familyId and"
						+ " family.familyName='"+familyname+"'";
			   }
			 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
			   try {  
				   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
		           while (ret.next()) {
		        	   System.out.println(ret.getString(2));
		        	   list.add(ret.getString(2));
		                   
		           }//显示数据 
		         
		           ret.close();  
		           db.close();//关闭连接  
		       } catch (SQLException e) {  
		           e.printStackTrace();  
		       }  
			return list;
	 }
	 
	 
	 /**
	  * 判断是否有主机
	  */
		public  int  ifhashub() {
			String sql=null;
			List<String> modelidlist=new ArrayList<>();
			String modelid=ReadExcel.readsimpledata(1, 1, 2);
		
			int count=0;
			boolean flag=false;
	
			//服务器查询该账号是否有主机
			if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
			{
				sql="select count(*) from account2,gateway,userGatewayBind where account2.email='"+TestcaseFrame.getaccount().getAccount()+
					   		"'  and account2.userId=userGatewayBind.userId and userGatewayBind.uid=gateway.uid and gateway.model='"+modelid+"' and account2.delFlag=0 and gateway.delFlag=0 and userGatewayBind.delFlag=0";
			   }
			else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
			   {
				sql="select count(*) from account2,gateway,userGatewayBind where account2.phone='"+TestcaseFrame.getaccount().getAccount()+
				   		"'  and account2.userId=userGatewayBind.userId and userGatewayBind.uid=gateway.uid and gateway.model='"+modelid+"' and account2.delFlag=0 and gateway.delFlag=0 and userGatewayBind.delFlag=0";
			   }
			System.out.println(sql);
			 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
			   try {  
				   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
		           while (ret.next()) {
		        	  //不为0代表有主机，检查我的界面是否显示我的主机
		        	count=ret.getInt(1);
		        	           
		           }//显示数据 
		         
		           ret.close();  
		           db.close();//关闭连接  
		       } catch (SQLException e) {  
		           e.printStackTrace();  
		       }  
			
		return count;
		
		}
	 
	 /**
	  * 楼层排序
	  */
	 public static  List<String> floorsort(String familyname)
	 {
		 String sql=null;
		 List<String> list = new ArrayList();
		
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select distinct floor.floorName,family.familyName  from floor,account2,family where account2.userId=floor.userId and floor.delFlag=0 and account2.email='"+TestcaseFrame.getaccount().getAccount()
			 		+"'  and family.familyId=floor.familyId and family.delFlag=0 and account2.delFlag=0 and family.familyName='"+familyname+"' order by floor.createTime";
			 System.out.println(sql);
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select distinct floor.floorName,family.familyName from floor,account2,family where account2.userId=floor.userId and floor.delFlag=0 and account2.phone='"+TestcaseFrame.getaccount().getAccount()
				 		+"'  and family.familyId=floor.familyId and family.delFlag=0 and account2.delFlag=0 and family.familyName='"+familyname+"' order by floor.createTime";
		 }
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) {
	        	   list.add(ret.getString(2))  ;
	        	   
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		   return list;
	 }
	 
   /**
    * 某个家庭有多少个房间
    */
	 public int familyrooms(String familyname)
	 {
		 String sql=null;
		 int count=0;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select  count(*) from account2,floor,family,room "
			 		+ "where account2.userId=floor.userId and account2.email='"+TestcaseFrame.getaccount().getAccount()
			 		+"' and floor.delFlag=0 and family.familyId=floor.familyId"
			 		+ " and room.floorId=floor.floorId and room.delFlag=0 and family.familyName='"+familyname+"'";
			 
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select  count(*) from account2,floor,family,room "
				 		+ "where account2.userId=floor.userId and account2.phone='"+TestcaseFrame.getaccount().getAccount()
				 		+"' and floor.delFlag=0 and family.familyId=floor.familyId"
				 		+ " and room.floorId=floor.floorId and room.delFlag=0 and family.familyName='"+familyname+"'";
			 
		 }
		 Log.logInfo(sql);
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   count=ret.getInt(1) ;   
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		 return count;
	 }
	 
	 /**
	  * 判断某个楼层有没有房间
	  */
	 public  static int ifroomonfloor(String familyname,String floorname)
	 {  
		 String sql=null;
		 int count=0;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select  count(*) from account2,floor,family,room "
			 		+ "where account2.userId=floor.userId and account2.email='"+TestcaseFrame.getaccount().getAccount()
			 		+"' and floor.delFlag=0 and family.familyId=floor.familyId"
			 		+ " and room.floorId=floor.floorId and room.delFlag=0 and family.familyName='"+familyname+"' and floor.floorName='"+floorname+"'";
			 
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select  count(*) from account2,floor,family,room "
				 		+ "where account2.userId=floor.userId and account2.phone='"+TestcaseFrame.getaccount().getAccount()
				 		+"' and floor.delFlag=0 and family.familyId=floor.familyId"
				 		+ " and room.floorId=floor.floorId and room.delFlag=0 and family.familyName='"+familyname+"' and floor.floorName='"+floorname+"'";
			 
		 }
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   count=ret.getInt(1) ;   
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		 return count;
	 }
	 
	 /**
	  * 某个家庭有几个楼层
	  */
	public  int floornumbers(String familyname)
	{   
		String sql=null;
		int count=0;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select family.familyName,floor.floorName,account2.email from floor,account2,family "
			 		+ "where account2.userId=floor.userId and floor.delFlag=0 and account2.email='"+TestcaseFrame.getaccount().getAccount()+
			 		"' and family.familyId=floor.familyId and family.delFlag=0 and family.familyName='"+familyname+"'";
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select family.familyName,floor.floorName,account2.email from floor,account2,family "
				 		+ "where account2.userId=floor.userId and floor.delFlag=0 and account2.phone='"+TestcaseFrame.getaccount().getAccount()+
				 		"' and family.familyId=floor.familyId and family.delFlag=0 and family.familyName='"+familyname+"'";
		 }
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   count++;     
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		
		return count;
	}

	 
	 /**
	  * 判断当前账号有几个家庭
	  */
	 public static int familynumbers()
	 {   
		 int count=0;
		 String sql = null;
		 //判断登录的什么类型的账号
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql=" select f.familyName from family as f "
						+ "where familyId in"
						+ "("
						+ "select fu.familyId from account2 as a,familyUsers as fu "
						+ "where fu.userId=a.userId and a.email='"+TestcaseFrame.getaccount().getAccount()+"' and fu.delFlag=0"
						+")";
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql=" select f.familyName from family as f "
						+ "where familyId in"
						+ "("
						+ "select fu.familyId from account2 as a,familyUsers as fu "
						+ "where fu.userId=a.userId and a.phone='"+TestcaseFrame.getaccount().getAccount()+"' and fu.delFlag=0"
						+")";
		 }
		Log.logInfo(sql);

	//	 System.out.println("查询语句是:"+sql);
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   count++;     
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
	   
		   return count;
		 
	 }
	 /**
	  * 判断第一个家庭的类型,1的话是子成员，0为管理 员
	  */
	 public String familytype()
	 {
	   String type=null;
	   String sql="select f.userType from account2 as a,familyUsers as f where f.userId=a.userId and "
			 		+ "email='"+TestcaseFrame.getaccount().getAccount()+"' or phone='"+TestcaseFrame.getaccount().getAccount()+"' and f.delFlag=0";
	   DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
	   try {  
		   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
          while (ret.next()) { 
       	   type=ret.getString(1);
       	       break;
    
          }//显示数据  
          ret.close();  
          db.close();//关闭连接  
      } catch (SQLException e) {  
          e.printStackTrace();  
      }  
		return type;
	 }
	 
	 /**
	  * 获取默认家庭的名称
	  */
	 public  String getdefaultfamily()
	 {  
		 String sql=null;
		 String defaultname=null;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			sql="select f.familyName,fu.userType,f.showIndex from family as f,familyUsers as fu,account2 as a "
					+ "where fu.familyId=f.familyId and fu.userId=a.userId "
					+ "and a.email='"+TestcaseFrame.getaccount().getAccount()+"' and fu.delFlag=0 and f.showIndex=1";
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
				sql="select f.familyName,fu.userType,f.showIndex from family as f,familyUsers as fu,account2 as a "
						+ "where fu.familyId=f.familyId and fu.userId=a.userId "
						+ "and a.phone='"+TestcaseFrame.getaccount().getAccount()+"' and fu.delFlag=0 and f.showIndex=1";
		 }
		 
		   DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   defaultname=ret.getString(1);
	     
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		 
		 return defaultname;
		 
	 }
	/**
	 * 获取家庭成员个数
	 * @return
	 */
	 public static int getfamilynumbers(String familyname)
	 {   
		 int number=0;
		 String sql="select count(*)  from family as f left join familyUsers as fu  "
		 		+ "on fu.familyId=f.familyId,account2 as a  where  f.familyName='"+familyname+
		 		"' and fu.delFlag=0 and f.delFlag=0 and fu.userId=a.userId";
		   DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   number=ret.getInt(1);
	     
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		   return number-1;
		 
	 }
	 /**
	  * 获取登录的帐户名称
	  * @return
	  */
	public  String getaccountname()
	{
		String homename;
		//获取帐号的名称,如果userName为空则显示账号名称
		String sql1=null,sql2=null;
		
		
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql1="select userName from account2 where email='"+TestcaseFrame.getaccount().getAccount()+"'";
	         sql2="select * from account2 where email='"+TestcaseFrame.getaccount().getAccount()+"'";
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql1="select userName from account2 where phone='"+TestcaseFrame.getaccount().getAccount()+"'";
			 sql2="select * from account2 where phone='"+TestcaseFrame.getaccount().getAccount()+"'";
		 }
		 
		 //name为从数据库查询到的名称
		 String name=Query.executSql(sql1,1,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		if(name.trim().equals(""))
		{
		
		String expectedname=Query.executSql(sql2,1,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		homename=expectedname;
		}
		else 
			homename=name;
		return homename;
		
	}
	
	/**
	 * 判断当前的账号是否是广告投放目标
	 * @param args
	 */
	public boolean ifadvtarget()
	{   
		String sql=null;
		String userId=null;
		//获取当前账号的userID
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select account2.userId from account2 where email='"+TestcaseFrame.getaccount().getAccount()+"'";
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select account2.userId from account2 where phone='"+TestcaseFrame.getaccount().getAccount()+"'";
		
		 }
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   userId=ret.getString(1);
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		   
		   //访问接口，查看此用户是否是广告投放目标
		  String target= HttpRequest.sendGet(Constant.HTTPURl, "imageSize=101&userId="+userId);
		  if(target.equals("true"))
		  {   
			  Log.logInfo("该用户是广告投放目标");
			  return true;
		  }
		  else
		  {   
			  Log.logInfo("该用户不是广告投放目标");
			  return false;
		  }
	}
	
	/**
	 * 查询当前账号传感器设备
	 * @param args
	 */
	public List<String> checksensor()
	{
		String sql=null;
		int devicetype;
		List<String> list=new ArrayList();
		//获取当前账号的userID
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select device.deviceName,device.deviceType from "
			 		+ "device,account2,userGatewayBind where account2.userId=userGatewayBind.userId "
			 		+ "and userGatewayBind.uid=device.gatewayUID and userGatewayBind.delFlag=0 and device.delFlag=0 and"
			 		+ " account2.email='"+TestcaseFrame.getaccount().getAccount()+
			 		"' and device.deviceType in (18,22,23,24,25,26,27,46,47,48)";

		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select device.deviceName,device.deviceType from "
				 		+ "device,account2,userGatewayBind where account2.userId=userGatewayBind.userId "
				 		+ "and userGatewayBind.uid=device.gatewayUID and userGatewayBind.delFlag=0 and device.delFlag=0 and"
				 		+ " account2.phone='"+TestcaseFrame.getaccount().getAccount()+
				 		"' and device.deviceType in (18,22,23,24,25,26,27,46,47,48)";
		
		 }
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	   list.add(ret.getString(3));

	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		   return list;
		
	}
	
	/**
	 * 判断某帐号是否已注册
	 * @return
	 */
	public boolean  ifaccountregister(String account)
	{
		String sql=null;
		boolean flag=false;
		//获取当前账号的userID
		 if(account.contains("@"))
		 {
			 sql="select count(*) from account2 where email='"+account+"'";

		 }
		 else
		 {
			 sql="select count(*) from account2 where phone='"+account+"'";
		 }
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) { 
	        	 if( ret.getInt(1)!=0)
	        		 {
	        		 flag=true;
	        		 }

	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		return flag;
	}
	/**
	 * 删除帐号
	 */
	public void deleteaccount(String account)
	{
		String sql=null;
		int state=0;
		//获取当前账号的userID
		 if(account.contains("@"))
		 {
			 sql="delete from account2 where email='"+account+"'";

		 }
		 else
		 {
			 sql="delete from account2 where phone='"+account+"'";
		 }
		
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			  db.pst.executeUpdate();//执行语句，得到结果集 
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }
		
	}
	
	/**
	 * 判断某个被邀请者是否接受邀请,0:未处理  1:已接受 2:已拒绝
	 * @return
	 */
	public int  inviteState(String inviteaccount,String familyname)
	{
		String sql=null;
		int state=0;
		//获取当前账号的userID
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select familyInvite.status from familyInvite "
			 		+ "inner join family on family.familyId=familyInvite.familyId "
			 		+ "and family.familyName='"+familyname+"' "
			 				+ "and  familyInvite.receiveUserId in (select account2.userId from account2 where email='"+inviteaccount+"')";

		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select familyInvite.status from familyInvite "
				 		+ "inner join family on family.familyId=familyInvite.familyId "
				 		+ "and family.familyName='"+familyname+"' "
				 				+ "and  familyInvite.receiveUserId in (select account2.userId from account2 where phone='"+inviteaccount+"')";
		 }
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) {
	        	   state=ret.getInt(1);
	        	  
	        	

	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }
		   return state;
	}
	
	/**
	 * 判断帐号授权了哪些三方,返回为Map类型，包括帐户名和注册类型，
	 *  0：注册用户；
		1：微信授权用户；
		2：qq授权用户；
		3：新浪微博授权用户；

	 * @return 
	 * @return
	 */
	public Map<String, String> authorthirdaccount()
	{
		String sql=null;
		Map<String,String> map=new HashMap<>();
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select account2.email,thirdAccount.thirdUserName,thirdAccount.registerType  from account2 inner "
			 		+ "join thirdAccount on account2.userId=thirdAccount.userId and account2.delFlag=0 and"
			 		+ " account2.email='"+TestcaseFrame.getaccount().getAccount()+"' and thirdAccount.delFlag=0";

		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select account2.email,thirdAccount.thirdUserName,thirdAccount.registerType  from account2 inner "
				 		+ "join thirdAccount on account2.userId=thirdAccount.userId and account2.delFlag=0 and"
				 		+ " account2.phone='"+TestcaseFrame.getaccount().getAccount()+"' and thirdAccount.delFlag=0";
	
		 }
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) {
	        	   map.put(ret.getString(2), ret.getString(3));
	
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }
		   return map;
		  
	}
   
	/**
	 * 当前帐号有几个灯
	 * @return
	 */
	public int lightnumbers()
	{
		String sql=null;
		int count=0;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sql="select device.deviceName,device.deviceType from account2,userGatewayBind,device "
			 		+ "where account2.userId=userGatewayBind.userId and "
			 		+ "account2.email='"+TestcaseFrame.getaccount().getAccount()+"'  "
			 		+ "and account2.delFlag=0 and  userGatewayBind.delFlag=0 "
			 		+ "and userGatewayBind.uid=device.gatewayUID and device.delFlag=0  and device.deviceType in (0,1,19,38,82)";

		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select device.deviceName,device.deviceType from account2,userGatewayBind,device "
				 		+ "where account2.userId=userGatewayBind.userId and "
				 		+ "account2.phone='"+TestcaseFrame.getaccount().getAccount()+"'  "
				 		+ "and account2.delFlag=0 and  userGatewayBind.delFlag=0 "
				 		+ "and userGatewayBind.uid=device.gatewayUID and device.delFlag=0  and device.deviceType in (0,1,19,38,82)";
	
		 }
		 
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集  
	           while (ret.next()) {
	        	   count++;
	        	   
	
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }
		   return count;
		
	}
	/**
	 * 删除三方帐号
	 * @return
	 */
	public void deletethridaccout(String accountname)
	{
		String sql=null;
		int state=0;
		
	    sql="delete from thirdAccount where thirdUserName='"+accountname+"'";
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			  db.pst.executeUpdate();//执行语句，得到结果集 
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }
		
	}
	public static String scrennfilename()
	{   
		System.out.println("截图文件名为:"+picname);
		return picname;
	}
	
	/**
	 * 发送测试报告
	 */
	public static void sendReport(String reportname,String receiver)
	{   
		String logpath=System.getProperty("user.dir")+File.separator+"Log";
		String logzip=System.getProperty("user.dir")+File.separator+"Log.zip";
		String reportpath=System.getProperty("user.dir")+File.separator+"test-output";
		String reportzip=System.getProperty("user.dir")+File.separator+"report.zip";
		
		//重命名测试报告名称
		FileOperate.copyFile(System.getProperty("user.dir")+File.separator+"test-output\\index.html",System.getProperty("user.dir")+File.separator+"test-output\\"+reportname+".html");
		
		//压缩测试报告
		FileOperate.zip1(reportzip, new File(reportpath));
		//压缩LOG
		FileOperate.zip1(logzip, new File(logpath));
		
		SendTestReportEMS ems=new SendTestReportEMS();
		ems.setAddress("15079034630@126.com", receiver, reportname+"测试报告");
		ems.setAffix(logzip, "Log.zip");
		ems.send("smtp.126.com", "15079034630@126.com", "penghong1987","本次测试已完成，附件为测试日志，测试报告路径为："+reportpath+File.separator+reportname+".html");
		Log.logInfo("测试报告发送完成");
	}
	
	/**
	 * 每个测试方法结束后要做操作
	 */
	public void aftertest()
	{
		Log.logInfo("******************************************************");
		System.out.println(System.getProperty("line.separator"));
	}

}
