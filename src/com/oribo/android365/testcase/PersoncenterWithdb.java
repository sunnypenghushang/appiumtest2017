package com.oribo.android365.testcase;

/**
 * “我的”+场景 基本功能用例中需与服务器数据库交互的用例
 */
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import com.oribo.ReadExcelCase.ReadExcel;
import com.oribo.common.AppOperate;
import com.oribo.common.EditText;
import com.oribo.common.HttpRequest;
import com.oribo.common.TestcaseFrame;
import com.oribo.common.ToolFunctions;
import com.oribo.database.DBHelperMysql;
import com.oribo.dataprovider.AppBean;
import com.oribo.dataprovider.Constant;
import com.oribo.log.Log;
import com.oribo.report.TestResultListener;
import com.oribo.utils.FileOperate;
import com.oribo.utils.compareimage;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
@Listeners({TestResultListener.class})
public class PersoncenterWithdb extends TestcaseFrame{
	AndroidDriver<AndroidElement> driver;
	AppBean  appbean = AppBean.getAppBean();
	
	String mac=null;
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{   
		super.testSetUp();
		driver=super.getDriver();
		logging();
		
	}
	
	/**
	 * 个人中心检查我的主机
	 */
	@Test(groups={"个人中心","我的主机12"})
	public  void CheckMyHub() {
		//服务器拉取数据判断当前帐号是否有主机
		boolean flag=false;
		if(ifhashub()!=0)
		{
			Log.logInfo("有主机");
    		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
    		Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/myHost")).isDisplayed());
			
		}
		else
		{   		
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		//如果没有主机的话，APP上不应显示我的主机项
		Assert.assertFalse(driver.getPageSource().contains("我的主机"));  
		}
	
	
	}			
	       

	/**
	 * 
	 * 更多页面检查删除主机
	 */
	@Test(dependsOnMethods={"CheckMyHub"},groups={"个人中心","我的主机"},enabled=false)
	private void ClickMore() {	
		enterMyhost();
		if(ifhashub()>1)
		//我的主机界面点击更多
		AppOperate.swipeToUp(driver, "向上滑屏");
		AndroidElement  	MoreName = (AndroidElement) driver.findElementById("com.orvibo.homemate:id/moreTextView");//主机设备信息“更多”
        AppOperate.click(MoreName, "点击更多");
        //点击删除主机，检查提示框
		AndroidElement  	HostName = (AndroidElement) driver.findElementById("com.orvibo.homemate:id/deleteTextView");//更多页面检查删除主机
	    AppOperate.click(HostName, "点击‘删除主机’");
	    AndroidElement  	noticeText = (AndroidElement) driver.findElementById("com.orvibo.homemate:id/contentTextView");//点击删除主机弹窗提示文案
	    Assert.assertEquals(noticeText.getText(), "确实要删除该主机吗？");
	    //确认删除界面点击‘取消e’
		AndroidElement  	CancelButton = (AndroidElement) driver.findElementById("com.orvibo.homemate:id/rightButton");//点击删除主机弹窗提示文案
        AppOperate.click(CancelButton, "点击‘取消’");
        //再次点击删除主机
        AppOperate.click(HostName, "点击‘删除主机’");
		AndroidElement  	DeleteButton = (AndroidElement) driver.findElementByAndroidUIAutomator("text(\"删除\")");//删除按钮	
		//提示框中确定删除
		AppOperate.click(DeleteButton, "确认删除");
		Assert.assertFalse(HostName.isDisplayed(),"删除主机失败");

	}
	
	/**
	 * 设备信息检查主机Mac地址
	 * 设备信息检查主机ip
	 * 设备信息检查主机固件版本
	 * 设备信息检查主机版本后返回
	 */
	
	@Test(dependsOnMethods={"CheckMyHub"},groups={"我的主机"})
	public void myhost()
	{   
		String sql=null;
		//读取主机的model id
		List<String> modelidlist=new ArrayList<>();
		 enterMyhost();
		List<AndroidElement> list=driver.findElements(By.id("com.orvibo.homemate:id/tv_hub_name"));
		modelidlist.add(ReadExcel.readsimpledata(1, 1, 2));
		String hostip=null,hostmac=null,hostfireware=null,hostname=null;
	  	
		for(int i=0;i<modelidlist.size();i++)
		{
		

		//获取主机的主机名称、mac地址、IP地址、固件版本
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		   {
			 sql="select account2.email,gateway.model,gateway.homeName,gateway.uid,gateway.softwareVersion,gateway.localStaticIP "
			 		+ "from account2,gateway,userGatewayBind "
			 		+ "where account2.email='"+TestcaseFrame.getaccount().getAccount()+
			 		"' and account2.userId=userGatewayBind.userId and userGatewayBind.uid=gateway.uid and gateway.model='"+modelidlist.get(i)+"' and account2.delFlag=0 and gateway.delFlag=0 and userGatewayBind.delFlag=0 order by gateway.createTime";
		   }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sql="select account2.email,gateway.model,gateway.homeName,gateway.uid,gateway.softwareVersion,gateway.localStaticIP "
				 		+ "from account2,gateway,userGatewayBind "
				 		+ "where account2.phone='"+TestcaseFrame.getaccount().getAccount()+
				 		"' and account2.userId=userGatewayBind.userId and userGatewayBind.uid=gateway.uid and gateway.model='"+modelidlist.get(i)+"' and account2.delFlag=0 and gateway.delFlag=0 and userGatewayBind.delFlag=0 order by gateway.createTime";
		 }
		 System.out.println(sql);
		 DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集 
			    
			
			    
	           while (ret.next()) {
	        		
	        	 AppOperate.click(list.get(i), "进入我的主机界面");
	        	 newSleep(2);
	 
	        	 //上滑
	  		     AppOperate.swipeToUp(driver, "向上滑屏");
	        	 hostname=ret.getString(3);
	        	 mac=ret.getString(4);
	        	 hostmac=ToolFunctions.toMac(ret.getString(4).replaceAll("202020202020", ""));
	        	 hostfireware=ret.getString(5);
	        	 hostip=ret.getString(6);
	        	 Log.logInfo("主机的名称为:"+hostname+",主机的MAC地址为："+hostmac+"，主机的固件版本为："+hostfireware+",主机的IP地址为："+hostip);
	        	//进入我的主机检查相应信息是否正确
	  		     Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/hubNameTv")).getText().equals(hostname));
	  		     System.out.println("mac地址为:"+driver.findElement(By.id("com.orvibo.homemate:id/info1TextView")).getText());
	  		     Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/info2TextView")).getText().equals(hostmac));
	  		     System.out.println("显示的主机IP是什么"+driver.findElement(By.id("com.orvibo.homemate:id/info2TextView")).getText());
	  		     Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/info2TextView")).getText().equals(hostip));
	  		     Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/info3TextView")).getText().indexOf(hostfireware)!=-1);
	  		     newSleep(2);
	  		     AppOperate.sendKeyEvent(4, "返回到我的主机界面", driver);
	            
	           }//显示数据 
	         
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		}
		   AndroidElement  HostName = (AndroidElement) driver.findElementById("com.orvibo.homemate:id/left_iv");//返回按扭
		   Assert.assertTrue(HostName.isDisplayed(), "未找到返回按钮");
		   AppOperate.click(HostName, "点击返回按扭");;//返回我的页面
		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"我的\")").isDisplayed());
		 
	}

	
	/**
	 * 有WIFI设备时查找节能提醒中是否有设置开关
	 * 无WIFI设备时查找节能提醒中是否有设置开关
	 * 有灯时查找节能提醒中是否有节能提醒设置开关
	 * 无灯时查找节能提醒中是否有节能提醒设置开关
	 * 定时执行提醒中默认开关
	 * 节能提醒中默认开关
	 * 节能提醒中设备数的显示
	 * WIFI和zigbee设备都为空时提醒设置界面显示
	 */
	@Test(groups={"提醒设置","3.30"})
	public void remindersettings()
	{
		//获取当前家庭
		String defaultname=getdefaultfamily();
		//判断当前家庭有无WIFI设备、小方设备,判断默认状态 ，默认为开启
		
		//判断当前家庭有无ZigBee开关，判断默认状态 ，默认为开启
		//判断当前WIFI或小方设备数，全部应展示在定时执行提醒中
		//判断当前ZigBee开关数,全部应展示在节能提醒中
		
		 String sqlzigbee=null,sqlwifi=null;
         int countwifi=0,countzigbee=0;
		 if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		 {
			 sqlwifi="select  distinct userGatewayBind.uid from userGatewayBind,account2 , device "
			 		+ "where  account2.userId=userGatewayBind.userId and "
			 		+ "account2.email='"+TestcaseFrame.getaccount().getAccount()
			 		+"'  and account2.delFlag=0 and "
			 		+ " userGatewayBind.delFlag=0 and userGatewayBind.uid=device.gatewayUID and device.delFlag=0 and device.endpoint=-1";
			 
			 sqlzigbee="select device.deviceName from account2,userGatewayBind,device  "
				 		+ "where account2.userId=userGatewayBind.userId and "
				 		+ "account2.email='"+TestcaseFrame.getaccount().getAccount()
				 		+"' and account2.delFlag=0 "
				 		+ "and userGatewayBind.delFlag=0 and userGatewayBind.uid=device.gatewayUID and "
				 		+ "device.delFlag=0  and (device.deviceType=0 or device.deviceType=1 or device.deviceType=38 or device.deviceType=19 or device.deviceType=82)";
			 
			 
		 }
		 else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		 {
			 sqlwifi="select  distinct userGatewayBind.uid from userGatewayBind,account2 , device "
				 		+ "where  account2.userId=userGatewayBind.userId and "
				 		+ "account2.phone='"+TestcaseFrame.getaccount().getAccount()
				 		+"'  and account2.delFlag=0 and "
				 		+ " userGatewayBind.delFlag=0 and userGatewayBind.uid=device.gatewayUID and device.delFlag=0 and device.endpoint=-1";
			 
			 sqlzigbee="select device.deviceName from account2,userGatewayBind,device  "
			 		+ "where account2.userId=userGatewayBind.userId and "
			 		+ "account2.phone='"+TestcaseFrame.getaccount().getAccount()
			 		+"' and account2.delFlag=0 "
			 		+ "and userGatewayBind.delFlag=0 and userGatewayBind.uid=device.gatewayUID and "
			 		+ "device.delFlag=0  and (device.deviceType=0 or device.deviceType=1 or device.deviceType=38 or device.deviceType=19 or device.deviceType=82)";
			
		 }
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"),"点击我的");
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"提醒设置\")"),"点击提醒 设置");
		 DBHelperMysql dbwifi=new DBHelperMysql(sqlwifi,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		
		   try {  
			   ResultSet  retwifi = dbwifi.pst.executeQuery();//执行语句，得到结果集 
			 
	           while (retwifi.next()) {
	        	  boolean ifcheckbutton=true;
	        	  //WIFI设备不为空，检查提醒 设备中是否有相应开关
	        	  if(ifcheckbutton)
	        	  {
	        		  Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"定时执行提醒\")").isDisplayed());
	        		  ifcheckbutton=false;  
	        	  }
	        	   countwifi++;
	     
	           }//显示数据
	           Log.logInfo("WIFI设备数为:"+countwifi);
	    	   if(countwifi==0)
			   {
				   try{
					  driver.findElementByAndroidUIAutomator("text(\"定时执行提醒\")").isDisplayed();
				   }
				   catch(Exception e)
				   {
					   
				   }
			   }
	    	   else
	    	   {
	     	  AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"定时执行提醒\")"), "进入定时执行提醒界面");
	        	 
        	  //判断开关是否默认打开
        	  Assert.assertTrue(driver.findElements(By.id("com.orvibo.homemate:id/deviceNameTextView")).size()>0);
        	  AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "返回到提醒设置界面");
	    	   }
	         
	           retwifi.close();  
	           dbwifi.close();//关闭连接
	           
	           DBHelperMysql dbzigbee=new DBHelperMysql(sqlzigbee,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
	           ResultSet  retzigbee = dbzigbee.pst.executeQuery();//执行语句，得到结果集
	           while (retzigbee.next()) {
		        	  
	        	   boolean ifcheckbutton=true;
		        	  //灯设备不为空
		        	  if(ifcheckbutton)
		        	  {
		        		  Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"节能提醒\")").isDisplayed());
		        		  ifcheckbutton=false;
		        		  
		        	  }
		        	  countzigbee++;
		        	  
	      	     
	           }//显示数据
	           Log.logInfo("灯设备数为:"+countzigbee);
	           if(countzigbee==0)
	        	   Assert.assertTrue(driver.getPageSource().contains("节能提醒"));
	           else
	           {
	     	      AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"节能提醒\")"), "进入节能提醒界面");
	   
	            //判断当前界面默认开关是否打开，且灯数量应小于等于服务灯数量
	             Assert.assertTrue(driver.findElements(By.id("com.orvibo.homemate:id/deviceName")).size()>0);
	             Assert.assertTrue(driver.findElements(By.id("com.orvibo.homemate:id/deviceName")).size()<=countzigbee); 
	           }
	           retzigbee.close();  
	           dbzigbee.close();
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
		   
		   if(countwifi==0&& countzigbee==0)
		   {
			   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"还没有可设置提醒的设备哟~\")").isDisplayed());
		   }
		
		
	}

	/**
	 * 检查默认家庭在家庭管理主页面的显示
	 */
	
	@Test()
	public void checkdefault()
	{   
		enterhomemanage();
		//获取帐号的名称
		String expectedname=getaccountname();
		//判断默认显示是否正确，如管理员名称是否显示正确
		Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/memberName")).getText().equals(expectedname));
		Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"默认\")").isDisplayed());
		Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/familyCheck")).isDisplayed());
		//判断是否存在家庭管理的标题
		AppOperate.exitElement("com.orvibo.homemate:id/title_tv", driver);
		

	}
	
	/**
	 * 检查新建的家庭在家庭管理主页面的显示
	 */
	@Test(groups={"家庭管理"})
	public void addhome()
	{   
		
		enterhomemanage();
		//判断当前已自创多少个家庭
		int addnumber=driver.findElementsByAndroidUIAutomator("text(\""+getdefaultfamily()+"\")").size();
	
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_iv")), "点击新建家庭按扭");
		Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"创建家庭\")").isDisplayed());
		//输入家庭名称
		EditText familyName=new EditText(driver.findElement(By.id("com.orvibo.homemate:id/familyName")));
		familyName.inputfivechar(driver);
		//截图并保存
		String setbefore=ToolFunctions.getRandomstring(1);
		screenCapCompare(driver, setbefore);
		//设置家庭头像
		AndroidElement familyIcon=driver.findElement(By.id("com.orvibo.homemate:id/familyIcon"));
		//拍照设置头像
		setphotobycamera(familyIcon);
		//查看照片是否设置成功,截图并对比
		String setafter=ToolFunctions.getRandomstring(1);
		screenCapCompare(driver, setafter);
		Assert.assertFalse(compareimage.sameAs(FileOperate.getScreencapFilePath()+File.separator+setbefore+".png", FileOperate.getScreencapFilePath()+File.separator+setafter+".png", 0.97));
		//点击创建家庭
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/familyAddBtn")), "点击创建家庭");
		//判断是否返回到家庭管理界面,并判断是否显示新建的家庭
		Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+familyName.getfivechar()+"\")").isDisplayed());
		
		
	}
	
	/**
	 * 修改默认家庭的家庭名称
	 */
	@Test(groups={"家庭管理"})
	public void edithome()
	{
		enterhomemanage();
		AppOperate.click(driver.findElements(By.id("com.orvibo.homemate:id/familyName")).get(0), "点击进入默认家庭");
		//点击进入默认家庭
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"家庭名称\")"), "点击家庭名称w");
		AndroidElement name=driver.findElement(By.id("com.orvibo.homemate:id/input_family_nickname_edit"));
		//AppOperate.clear(name, "清除名称");
		String editname=ToolFunctions.getRandomstring(5);
		AppOperate.sendKeys(name, "重新输入家庭名称", editname);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"保存\")"), "点击保存按扭");
		Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+editname+"\")").isDisplayed());
		
		
	}
 /**
  * 检查默认家庭的排序
  */
	@Test(groups={"家庭管理","3.30"})
	public void sort()
	{
		enterhomemanage();
		List<AndroidElement> list=driver.findElements(By.id("com.orvibo.homemate:id/familyName"));
		Assert.assertEquals(list.get(0).getText(), getdefaultfamily());
	}
	
	/**
	 *家庭成员页面检查——有成员
	 * 家庭成员页面检查——无成员
	 * 邀请家庭成员页面检查
	 * 家庭邀请手机号
	 * 发起邀请成功
	 * 编辑成员页面检查
	 * 编辑成员页面，删除成员
	 * 用户接受邀请，查看家庭成员界面是否显示成员
	 * 用户拒绝邀请，查看家庭成员界面是否显示成员
	 * 用户未处理邀请，查看家庭成员界面是否显示成员
	 */
	@Test(groups={"家庭管理","3.30"})
	public void memberoffamily()
	{
		enterhomemanage();
		String defaultname=getdefaultfamily();
		//服务器获取默认家庭成员个数
		int familynumbers=getfamilynumbers(defaultname);
		Log.logInfo("当前家庭有"+familynumbers+"个成员");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\""+getdefaultfamily()+"\")"), "点击进入默认家庭");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"家庭成员\")"), "进入家庭成员列表");
		//判断成员列表是否为空
		if(!AppOperate.ifexitElement("com.orvibo.homemate:id/tv_name", driver))
		{
			//为空界面默认应有提示语
			AppOperate.exitElement("com.orvibo.homemate:id/empty_iv", driver);
			Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"你还没有添加家庭成员\")").isDisplayed());
		}
		else{
			//编辑成员，点击任意成员
			AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/tv_name")), "点击任意成员");
			//判断是否有删除按扭
			AppOperate.exitElement("com.orvibo.homemate:id/info_family_delete", driver);
			//设置备注
			AndroidElement edit=driver.findElement(By.id("com.orvibo.homemate:id/userNicknameEditText"));
			String inputname=ToolFunctions.getRandomstring(5);
			edit.sendKeys(inputname);
			//点击保存
			AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_tv")), "点击保存按扭");
			//删除成员
			AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/iv_delete")), "点击删除按扭");
			//判断是否有弹框，有的话点击删除
			Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"提  示\")").isDisplayed());
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"删除\")"), "弹框内点击删除");
			//判断是否删除成功，服务器框默认家庭成员个数减1
			
			Assert.assertEquals(getfamilynumbers(defaultname), familynumbers-1);
		}
		//邀请成员
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_iv")), "点击添加成员按扭");
		
		 //判断“发起邀请”是否可点击
		 Assert.assertFalse(driver.findElementByAndroidUIAutomator("text(\"发起邀请\")").isEnabled(), "发起邀请默认是激活的");
		 //点击联系人选择按扭
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/contactImageView")), "点击添加联系人选择按扭");
		 //判断是否跳 转到联系人选择界面
		Assert.assertTrue( ToolFunctions.cmdmessage("adb shell dumpsys window w|grep name=", "contacts"), "未跳转到联系人选择界面");
		//返回邀请家人界面
		AppOperate.sendKeyEvent(4, "返回家庭管理界面", driver);
		//手动输入号码,分别输入小于11位，11位，大于11位
		EditText edit=new EditText(driver.findElement(By.id("com.orvibo.homemate:id/accountEditText")));
		 //清掉再输入11位未注册的手机号
		 edit.clear();
		 edit.inputphonenumber(driver);
		 String phonenumber=edit.getphonenumber();
		 System.out.println("输入的内容为:"+phonenumber);
		 //如果已注册
		 
		 if(ifaccountregister(phonenumber))
		 {

			 //点击发起邀请
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/send_invite_text")), "点击发起邀请按钮");
			 //判断是否会弹出已发送邀请的提示框
			
			 Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/contentTextView")).getText().contains(phonenumber));
			 //点击确定
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/gotItButton")), "弹框中点击确定");
			 //判定发送邀请的状态
			 int state=inviteState(phonenumber,defaultname);
			 if(state==0)
			 {
				 Log.logInfo("被邀请用户还未处理您的邀请");
				 //重新进入家庭成员界面查看是否显示被邀请的帐号
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "返回到家庭详情界面");
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/info_member_layout")), "重新进入家庭成员界面");
				 //判断是否显示该成员
				Assert.assertFalse(driver.getPageSource().contains(phonenumber));
			
			 }
			 else if(state==1)
			 {
				 
				 Log.logInfo("被邀请用户已接受您的邀请");
				 //重新进入家庭成员界面查看是否显示被邀请的帐号
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "返回到家庭详情界面");
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/info_member_layout")), "重新进入家庭成员界面");
				 //判断是否显示该成员
				 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+phonenumber+"\")").isDisplayed());
			 }
			 else if(state==2)
			 {
				 Log.logInfo("被邀请用户拒绝了您的邀请");
				 //重新进入家庭成员界面查看是否显示被邀请的帐号
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "返回到家庭详情界面");
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/info_member_layout")), "重新进入家庭成员界面");
				 //判断是否显示该成员
				 Assert.assertFalse(driver.getPageSource().contains(phonenumber));
			 }
			 
			 
			 
		 }
		 //如果未注册
		 else
		 {
			Log.logInfo("被邀请的家庭未注册");
			 //点击发起邀请
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/send_invite_text")), "点击发起邀请按钮");
			 //判断是否会弹出未注册的提示框
			 AppOperate.exitElement("该用户尚未注册", driver);
			 AppOperate.exitElement("试试微信快速邀请吧", driver);
			 //点击微信邀请，判断是否会跳 转到微信界面
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/rightButton")), "点击微信邀请");
			 Assert.assertTrue(ToolFunctions.cmdmessage("adb shell dumpsys window w|grep name=", "com.tencent.mm"), "未跳转到微信界面");
			//返回家庭管理界面
			AppOperate.sendKeyEvent(4, "返回家庭管理界面", driver);
		 }


	
	}
	

	/**
	 * 默认家庭数显示
	 * */
 @Test(groups={"家庭管理","3.30"})
 public void checkfamilynumber()
 {
	 int familynumber=familynumbers();
	 System.out.println("家庭数是："+familynumber);
	 enterhomemanage();
	 //判断展示家庭数是否与服务器一致
	 Assert.assertEquals(driver.findElements(By.id("com.orvibo.homemate:id/familyIcon")).size(), familynumber);
	 
	 
 }
	/**
	 * 检查加入的家庭在家庭管理主页面的显示
	 * 默认的家庭切换至新建的家庭
	 * 新建家庭成功，进入家庭详情
	 * 删除默认家庭
	 */
	 @Test(groups={"家庭管理","3.30"})
	 public void familydetails()
	 {     
		 enterhomemanage();
		 String sql=null;
		   int i=1;
		   String usertype=null,familyname=null,showindex=null;
		   
		   if(getaccount().getLogingType()==Constant.LOGING_TYPE_EMAIL)
		   {
			   sql="select f.familyName,fu.userType,f.showIndex from family as f,familyUsers as fu,account2 as a "
				   		+ "where fu.familyId=f.familyId and fu.userId=a.userId and a.email='"+TestcaseFrame.getaccount().getAccount()+
				   		"' and fu.delFlag=0";
		   }
		   else if(getaccount().getLogingType()==Constant.LOGING_TYPE_PHONE)
		   {
			   sql="select f.familyName,fu.userType,f.showIndex from family as f,familyUsers as fu,account2 as a "
				   		+ "where fu.familyId=f.familyId and fu.userId=a.userId and a.phone='"+TestcaseFrame.getaccount().getAccount()+
				   		"' and fu.delFlag=0";
		   }
		  System.out.println(sql);
		   DBHelperMysql db=new DBHelperMysql(sql,Constant.INTERNALURL,Constant.DATABASEACCOUNTINTER,Constant.DATABASEACCOUNTPASSWORD);
		   try {  
			   ResultSet  ret = db.pst.executeQuery();//执行语句，得到结果集 
	           while (ret.next()) { 
	
	        	   familyname=ret.getString(1); 
	        	   AndroidElement familyelement=driver.findElementByAndroidUIAutomator("text(\""+familyname+"\")");
	        	   Log.logInfo("第"+i+"个家庭,家庭名称为:"+familyname);
	        	   AppOperate.click(familyelement, "点击第"+i+"个家庭");
	        	   usertype=ret.getString(2);
	        	   showindex=ret.getString(3);
	        	   System.out.println("showindex为"+showindex);
	        	   //为0则为管理员权限家庭
	        	   if(usertype.equals("0"))
	        	   {   
	        		   
	        		   //showindex为1则为默认家庭
	        		   if(showindex.equals("1"))
	        		   {  
	        			   Log.logInfo(familyelement.getText()+"为默认家庭");
	        			 //权限为管理 员时，家庭详情没有删除或退出家庭按扭
		        		   AppOperate.notExitElement("android.widget.Button", driver);
		        		   AppOperate.sendKeyEvent(4, "返回到家庭管理界面", driver);
		 
	        		   }
	        		   else
	        		   {
	        		   Log.logInfo(familyelement.getText()+"为管理员家庭");
	        		   //非默认家庭且为自创家庭有删除家庭按钮,且权限显示为管理员
	     /*   		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"删除家庭\")").isDisplayed());*/
	        		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"管理员\")").isDisplayed());
	        	/*	   //删除家庭
	        		   AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"删除家庭\")"), "点击删除家庭");
	        		  
	        		   //弹出提示框时点击删除
	        		   AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"删除\")"), "弹出提示框点击删除");
	        		   //查看是否删除成功，界面不再展示
	        		   Assert.assertFalse(driver.getPageSource().contains(familyname));*/
	        		   }
	        	   }
	        	   //为1则为成员家庭
	        	   else if(usertype.equals("1"))
	        	   {   
	        		   Log.logInfo(familyelement.getText()+"为成员家庭");
	        		   //家庭名称不可点击
	        		   AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"家庭名称\")"), "查看成员家庭家庭名称是否可点击");
	        		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"家庭详情\")").isDisplayed());
	        		   //家庭头像不可点击
	        		   AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"家庭头像\")"), "查看成员家庭家庭名称是否可点击");
	        		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"家庭详情\")").isDisplayed());
	        		   
	        		   //查看权限显示
	        		   Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"家庭成员\")").isDisplayed());
	        		   //查看是否有退出家庭的按扭
	        		   AndroidElement exit=driver.findElementByAndroidUIAutomator("text(\"退出家庭\")");
	        		   Assert.assertTrue(exit.isDisplayed());
	        		   //点击退出家庭
	        		   AppOperate.click(exit, "点击退出家庭");
	        		   //弹出提示框后点击退出
	        		   AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"退出\")"), "点击确认退出");

	        		   //查看是否删除成功，界面不再展示
	                   Assert.assertFalse(driver.getPageSource().contains(familyname));
	        	   }
	        
	        	   i++;
	        	   
	        	       
	     
	           }//显示数据  
	           ret.close();  
	           db.close();//关闭连接  
	       } catch (SQLException e) {  
	           e.printStackTrace();  
	       }  
  
	 }
	 /**
	  * 编辑房间
	  */
	 
	 @Test(groups={"房间管理11"})
	 public void defaultroom()
	 {
		 enterroommanage();
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"默认房间\")"), "点击默认房间");
		 //判断默认房间名称是否可编辑
		 Assert.assertFalse(driver.findElement(By.id("com.orvibo.homemate:id/room_name_et")).isEnabled(), "默认房间名称不可编辑");
		 //进入房间编辑界面后，并点击默认图片
		 AndroidElement rompic=driver.findElement(By.id("com.orvibo.homemate:id/room_img_iv"));
		 AppOperate.click(rompic, "点击默认的房间图片");
		 //判断是否有弹框，弹出弹框后点击空白处，查看弹框是否消失
		 List<AndroidElement> menulist=driver.findElements(By.id("com.orvibo.homemate:id/menu_item_text"));
		 Assert.assertEquals(menulist.size(), 3);
		 //进入默认图库
		 AppOperate.click(menulist.get(0), "点击默认图库");
		 //判断默认图库图片不为0
		 //List<AndroidElement> piclist=driver.findElements(By.id("android.widget.ImageView"));
		 List<AndroidElement> piclist=driver.findElements(By.className("android.widget.ImageView"));
		 System.out.println("有多少张图片："+piclist.size());
		 Assert.assertTrue(piclist.size()>1);
		 //选择其中一张图片
		 AppOperate.click(piclist.get(2), "默认图库中选择一张图片");
		 //截图对比是否设置成功
		 String photo=ToolFunctions.getRandomstring(3);
		 screenCapCompare(driver, photo);
		 boolean ifsame=compareimage.sameAs(FileOperate.getScreencapFilePath()+File.separator+photo+".png", FileOperate.getTestDatapFilePath()+File.separator+"editrom.png", 0.90);
		 Assert.assertFalse(ifsame, "头像未设置成功");
		 //点击左上方的返回键返回到编辑房间界面
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "点击左上方的返回键");
		 //再次点击默认房间图片,打开拍照看是否能调用相机
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/tag_tv")), "点击默认房间");
		 AppOperate.click(rompic, "点击默认的房间图片");
		 AppOperate.click(menulist.get(1), "点击选择框中的“拍照”按扭");
		 Assert.assertTrue(ToolFunctions.cmdmessage("adb shell dumpsys window w |grep name=", "camera"));
		 //按返回键
		 AppOperate.sendKeyEvent(4, "点击返回", driver);
		 //点击从相册中选择，判断是否会跳转
		 AppOperate.click(rompic, "点击默认的房间图片");
		 AppOperate.click(menulist.get(2), "点击选择框中的“从相册中选择”按扭");
		 Assert.assertTrue(ToolFunctions.cmdmessage("adb shell dumpsys window w |grep name=", "gallery"));
		 AppOperate.sendKeyEvent(4, "返回到编辑房间界面", driver);
		 //点击保存
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/save_tv")), "房间编辑界面点击保存");
		 //判断是否跳转回房间管理界面
		 Assert.assertTrue(driver.getPageSource().contains("com.orvibo.homemate:id/tag_tv"));
	 }
	 /**
	  * 添加
	  */
	 @Test(groups={"房间管理","3.20"})
	 public void addroom()
	 {
		 //判断当前家庭，并获取当前家庭名称
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 enterroommanage();
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"+添加房间\")"), "点击添加房间按扭");
		 //输入房间名称
		 String roomname1=ToolFunctions.getRandomstring(4);
		 driver.findElement(By.id("com.orvibo.homemate:id/room_name_et")).sendKeys(roomname1);
		 //点击保存
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/save_tv")), "点击保存按扭");
		 //判断跳转回房间管理 界面后，是否会显示已添加的房间
		 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"房间管理\")").isDisplayed());
		 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+roomname1+"\")").isDisplayed());
		 
	 }
	 /**
	  * 删除房间
	  */
	 @Test(groups={"房间管理","3.27"})
	 public void delectroom()
	 {   
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 enterroommanage();
		 int roomnumber1=familyrooms(nowfamilyname);
		 Log.logInfo("房间数为"+roomnumber1);
		 int roomnumber2 = 0;
		 //如果房间数为0，则添加房间
		 if(roomnumber1<=1)
		 {
			 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"+添加房间\")"), "点击添加房间按扭");
			 //输入房间名称
			 String roomname1=ToolFunctions.getRandomstring(4);
			 driver.findElement(By.id("com.orvibo.homemate:id/room_name_et")).sendKeys(roomname1);
			 //点击保存
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/save_tv")), "点击保存按扭");
			 roomnumber1=familyrooms(nowfamilyname);
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/delete_iv")), "删除一个房间");
			 newSleep(2);
			 roomnumber2=familyrooms(nowfamilyname);
			 Log.logInfo("删除后的房间数为:"+roomnumber2);
			 Assert.assertEquals(roomnumber2+1, roomnumber1);
		 }
		 else
		 {
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/delete_iv")), "删除一个房间");
		 newSleep(2);
		 roomnumber2=familyrooms(nowfamilyname);
		 Log.logInfo("删除后的房间数为:"+roomnumber2);
		 Assert.assertEquals(roomnumber2+1, roomnumber1);
		 }
		
	 }
	 
	 
	 /**
	  * 添加楼层
	  * 备注：一个家庭最多添加8个楼层
	  */
	 @Test(groups={"房间管理"})
	 public void addfloor()
	 {   
		 int beforefloor,afterfloor;
		 //判断当前家庭，并获取当前家庭名称
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 Log.logInfo("当前家庭为:"+nowfamilyname);
		 beforefloor=floornumbers(nowfamilyname);
		 Log.logInfo("添加楼层前的楼层数为:"+beforefloor);
		 enterroommanage();
		 //服务器获取楼层数
         
		 int count=3;
		 while(count>0)
		 {
		 try{
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_floor_tv")), "点击添加楼层的按扭");
		 break;
		  }
		 catch(Exception e)
		 {   count--;
			 AppOperate.swipeToUp(driver, "向上滑动查找添加按扭");
		 }
		 }
	
		 //判断当前楼层是否加1
		 afterfloor=floornumbers(nowfamilyname);
		 Assert.assertEquals(afterfloor-1,beforefloor);
	 }
	 
	 /**
	  * 删除楼层
	  */
	 @Test(groups={"房间管理"})
	 public void delectfloor()
	 {   
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 Log.logInfo("当前家庭为:"+nowfamilyname);
		 enterroommanage();
		 List<AndroidElement> floolist,delectlist;
		 //判断是否有楼层
		 try{
		   floolist=driver.findElements(By.id("com.orvibo.homemate:id/floor_name_tv"));
		   delectlist=driver.findElements(By.id("com.orvibo.homemate:id/delete_tv"));
		   for(int i=0;i<floolist.size();i++)
		   {   
			   String floorname=floolist.get(i).getText();
			   //楼层有房间时会弹出提示框
			   if(ifroomonfloor(nowfamilyname,floorname)>0)
			   {
				   AppOperate.click(delectlist.get(i), "删除第"+i+1+"个楼层");
				   //判断是否会弹出提示框
				   AppOperate.exitElement("com.orvibo.homemate:id/contentTextView", driver);
				   //提示框中点击确认删除
				   AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/rightTextView")), "提示框中点击确认删除");
				   newSleep(2);
				   //判断界面不存在 此楼层
				   try{
					   driver.findElementByAndroidUIAutomator("text(\""+floorname+"\")").click();;
				   }
				   catch(Exception e)
				   {
					   Log.logInfo("删除成功");
				   }
				   
			   }
			   else
			   {
				   AppOperate.click(delectlist.get(i), "删除第"+i+1+"个楼层");
				   newSleep(2);
				 //判断界面不存在 此楼层
				   Assert.assertFalse(driver.getPageSource().contains(floorname));
			   }
		   }
		 }
		 catch(Exception e)
		 {
			 Log.logInfo("该房间没有楼层");
		 }
	     
	
		 
	 }
	 
	 /**
	  * 托动楼层排序
	  */
	 @Test(groups={"房间管理2"})
	 public void dragfloor()
	 {   	 
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 Log.logInfo("当前家庭为:"+nowfamilyname);
		 enterroommanage();
/*		 //服务器端拉取数据判断当前楼层个数
		 List<String> floorbefore=floorsort(nowfamilyname);
		 int floornumber=floorbefore.size();
		 Log.logInfo("操作前服务器获取楼层数为："+floornumber);*/
		 int floorsbefore=1;
		 if(driver.getPageSource().contains("删除该楼层"))
		 {
			 floorsbefore=driver.findElements(By.id("com.orvibo.homemate:id/floor_name_tv")).size();
		 }
		 //如果楼层数小于3则添加楼层，大于删除则删除楼层到3层

		 while(floorsbefore<3)
		 {
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_floor_tv")), "楼层数小于3楼时添加楼层");
			 floorsbefore++;
			 
		 }
		 Log.logInfo("操作后前端楼层数为："+floorsbefore);
		 List<AndroidElement> floorlist1=driver.findElements(By.id("com.orvibo.homemate:id/floor_name_fl"));
		 //拖动楼层，三楼托动到一楼
		 new TouchAction(driver).longPress(floorlist1.get(2),5000).moveTo(floorlist1.get(0)).release().perform(); 
	     
		 List<AndroidElement> floorlist2=driver.findElements(By.id("com.orvibo.homemate:id/floor_name_tv"));
		 //托动后判断排序
		 Log.logInfo("托动前第1层楼层为"+floorlist1.get(0).getText());
		 Log.logInfo("托动后第1层楼层为"+floorlist2.get(0).getText());
		 Assert.assertFalse(floorlist1.get(0).getText().equals(floorlist2.get(0).getText()));
	 }
       

	 /**
	  *服务器端场景都否在前端显示
	  */
	 @Test(groups={"情景","3.30"})
	 public void checkscene()
	 {   
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 int secenenumbers=0;
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
		 Log.logInfo("当前家庭为:"+nowfamilyname);
		 //服务器拉取情景个数
		 Map<String,String> scenelist=scenenumbers(nowfamilyname);
		 Set<String> set=scenelist.keySet();
		 Iterator<String> it= set.iterator();
		 Log.logInfo("当前情景数为:"+scenelist.size());
		 //判断当前所有场景是否在前端展示
		 while(it.hasNext())
		 {   
			 
			 String scenename=it.next();
			 int j=3;
			 boolean find = false;
			 while(j>0)
			 {
				 try{
					 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+scenename+"\")").isDisplayed());
					 System.out.println("存在 ");
					 secenenumbers++;
					 find=true;
					 break;
				 }
				 catch(Exception e)
				 {
					 //未找到则向上滑动
					 AppOperate.swipeToUp(driver, "滑动查找场景");
					 find=false;
		
				 }
				 j--;
			 }
				 Assert.assertTrue(find);

			 
		 
		 }
		 Assert.assertEquals(secenenumbers, scenelist.size());
		 
	 }
	 /**
	  * 帐号绑定的灯大于3时检查是否有系统场景
                            检查系统场景是否可编辑

	  */
	 @Test(groups={"情景","3.30"})
	 public void systemscene(){
		 //判断类的个数
		 int lights=lightnumbers();
		 if(lights>=3)
		 {
			 //如果灯的数量大于等于3则检查是否有系统场景
			 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
			 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"灯光全开\")").isDisplayed());
			 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"灯光全关\")").isDisplayed());
			 //点击系统场景不可编辑
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
	         //点击编辑按扭
	         AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑情景\")"), "点击编辑情景");
	         //点击第0，1个编辑按钮，不会进入编辑界面
	         List<AndroidElement> editlist=driver.findElements(By.id("com.orvibo.homemate:id/drag_grid_item_edit_image"));
	         editlist.get(0).click();
	         Assert.assertTrue(driver.getPageSource().contains("场景"));
	         editlist.get(1).click();
	         Assert.assertTrue(driver.getPageSource().contains("场景"));
		 }
		 else
		 {
			 Assert.assertFalse(driver.findElementByAndroidUIAutomator("text(\"灯光全开\")").isDisplayed(),"灯数量小于3时不应有系统场景");
			 Assert.assertFalse(driver.findElementByAndroidUIAutomator("text(\"灯光全关\")").isDisplayed(),"灯数量小于3时不应有系统场景");
		 }
	 }
	 
	 /**
	  * 添加情景
	  */
	 @Test(groups={"情景"})
	 public void addscene()
	 {   
		 int count2=3;
		
		 //判断当前场景数是否为0
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
		 //服务器拉取情景个数
		 Map<String,String> scenelist=scenenumbers(nowfamilyname);
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
		 //如果场景为0则没有编辑按扭
		 Log.logInfo("有多少个场景"+scenelist.size());
		 if(scenelist.size()!=0)
		 {
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"添加情景\")"), "点击添加情景按扭");
			
		 }
		 //添加情景界面，查看默认为称是否为空
		 String scenename=driver.findElement(By.id("com.orvibo.homemate:id/intelligentSceneNameEditText")).getText();
		Assert.assertFalse(scenename.trim().equals(""));
		//点击情景图标
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/iv_scene_icon")), "点击情景图标");
		//选择任意情景图标
		List<AndroidElement> sceneicon=driver.findElements(By.id("com.orvibo.homemate:id/iv_scene_icon"));
		int sceneicons=sceneicon.size();
		AppOperate.click(sceneicon.get(new Random().nextInt(sceneicons)), "随机点击任意一个图标");
		//点击添加执行任务
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/addBindTextView")), "点击添加执行任务");
	
		//判断设备是否为空
		if(AppOperate.ifexitElement("com.orvibo.homemate:id/tvDeviceName", driver))
		{   
			boolean flag=false;
			int count=3;
			String devicename=null;
			List<AndroidElement> list1=driver.findElements(By.id("com.orvibo.homemate:id/tvDeviceName"));
			List<AndroidElement> list2=driver.findElements(By.id("com.orvibo.homemate:id/cbDevice"));
			int index=new Random().nextInt(list1.size());
			while(count>0)
			{
				Log.logInfo("当前页设备数:"+list1.size());
				int size=list1.size();
				if(size==9)
					size=8;
			for(int i=0;i<size;i++)
			{   
				AndroidElement onlinedevice=list1.get(i);
				if(devicestatu(onlinedevice.getText()))
				{   
					AppOperate.click(onlinedevice, "选中任意一个在线设备");
					//判断是否选中
					devicename=list1.get(i).getText();
					Log.logInfo("选中的设备名称为:"+devicename);
					Assert.assertTrue(list2.get(i).getAttribute("checked").equals("true"));
					AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/back_iv")), "点击返回");
					flag=true;
					break;
				}
		
			}
			if(flag)
				break;
			AppOperate.swipeToUp(driver, "向上滑动查找元素");
			count--;
		
		
			}
			if(!flag)
			{
				Log.logInfo("没有在线设备");
			}
			else{
				boolean iffind=false;
				//判断是否显示已添加的设备
				//Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"默认房间 "+devicename+"\")").isDisplayed());
				Assert.assertTrue(driver.findElement(By.id("com.orvibo.homemate:id/tvLocation")).getText().contains(devicename));
				//点击立即，看界面跳 转是否正确 
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/tvTime")), "点击“立即”");
				//返回点击，再点击关闭看界面是否跳转
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "点击取消");
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/tv_action")), "点击关闭");
				Assert.assertFalse(driver.getPageSource().contains("继续添加"));
				//返回后点击保存
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/left_iv")), "点击返回");
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_tv")), "点击保存");
				//盘顿是否会显示添加的情景
				while(count2>0)
				{  
					try{
					Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+scenename+"\")").isDisplayed());
					iffind=true;
					break;
					}
					catch(Exception e)
					{
						AppOperate.swipeToUp(driver, "向上滑动");
					}
					count2--;
					
					
			
				}
				Assert.assertTrue(iffind, "添加的情景未在情景面板显示");
			}
			
		}
		else
		{
			Log.logInfo("设备列表为空");
		}
		

	 }
	 /**
	  * 编辑情景
	  */
	@Test(groups={"情景","3.21"})
	public void editscene()
	{   
		String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		//判断情景是否为空
		Map<String,String> scene=scenenumbers(nowfamilyname);
		Collection<String> set=scene.values();
		int size=scene.size();
		//如果含有一般场景
		if(set.contains(1))
		{
			 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
			 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
	         //点击编辑按扭
	         AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑情景\")"), "点击编辑情景");
	         //点击任意一个编辑按扭进行编辑
	         int index=new Random().nextInt(size);
	         driver.findElements(By.id("com.orvibo.homemate:id/drag_grid_item_edit_image")).get(index).click();
	         //进入情景编辑界面后，判断是否有删除任务的按扭
	         List<AndroidElement> list=driver.findElements(By.id("com.orvibo.homemate:id/ivDelete"));
	         Assert.assertTrue(list.size()>0);
	         //编辑名称
	        AndroidElement edit= driver.findElement(By.id("com.orvibo.homemate:id/intelligentSceneNameEditText"));
	        String inputstr=ToolFunctions.getRandomstring(5);
	        edit.sendKeys(inputstr);
	        //点击保存,看新名称是否显示在场景界面
	        AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_tv")), "点击保存");
	        Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+inputstr+"\")").isDisplayed());
	       
			 
		}
		else
		{   
			if(set.contains(0))
				Log.logInfo("当前只有系统场景,无法做编辑或删除情景的操作");
		}
		
	}
	
	/**
	 * 删除情景
	 */
	@Test(groups={"情景","3.30"})
	public void delectscene()
	{
		String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		//判断情景是否为空
		//判断情景是否为空
		Map<String,String> scene=scenenumbers(nowfamilyname);
		Collection<String> set=scene.values();
		Iterator it=set.iterator();
	    String findname=null;
	    int size=scene.size();
		int index=0;
		if(size>0&&size<3)
		{    
		    AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
			AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
			 //点击编辑按扭
	        AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑情景\")"), "点击编辑情景");
	        
	        while(it.hasNext())
	        {
	        	if(it.next().equals("0"))
	        	{   
	        		Log.logInfo("只含有系统场景，无法做编辑操作");
	        		
	        	}
	        	
	        }


		}
		if(size>=3)
		{
			  AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
				AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
				 //点击编辑按扭
		        AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑情景\")"), "点击编辑情景");
		        while(it.hasNext())
		        {
		        	if(it.next().equals("0"))
		        	{   
		        		index=new Random().nextInt(scene.size());
		        		if(index==0)
		        			index+=2;
		        		
		        		else if(index==1)
		        			index+=1;
		        			
		        		break;
		        	}
		        	 else
			        	 index=new Random().nextInt(scene.size());
		        	
		        }
		        
		      //点击任意一个编辑按扭进行编辑

		         driver.findElements(By.id("com.orvibo.homemate:id/drag_grid_item_edit_image")).get(index).click();
		         //点击删除情景的按扭
		         AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/deleteTextView")), "点击删除情景的按扭");
		         //提示框中点击确定删除
		         AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/rightTextView")), "提示框中点击删除");
		         //跳转回情景界面后，判断是否已删除成功
		         Assert.assertTrue(driver.findElements(By.id("com.orvibo.homemate:id/drag_grid_item_image")).size()+1==size);
		        
		}
		else if(size==0)
			Log.logInfo("当前无场景!");
		
      
		
	}
	 
	 
	 
	/**
	 * 添加联动 
	 */
	 @Test(groups={"联动","3.30"})
	 public void addlink()
	 {   
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 enterscene();
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"联 动\")"), "切换到联动界面");

		 List<String> linklist=linkagenumbers(nowfamilyname);
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
		 System.out.println("联动数是多少"+linklist.size());
		 if(linklist.size()>0)
			 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"添加联动\")"), "点击添加联动按扭");
		 //获取当前联动的名称
		 String linkagename=driver.findElement(By.id("com.orvibo.homemate:id/intelligentSceneNameEditText")).getText();
		 //点击添加启动条件
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/tv_condition_type")), "点击添加启动条件");
		 //判断启动条件是否为空
		 try{
		 List<AndroidElement> list=driver.findElements(By.id("com.orvibo.homemate:id/typeName_tv"));
		 AndroidElement element=list.get(new Random().nextInt(list.size()));
		 String elementname=element.getText();
		 if(list.size()>0)
		 {
			 //点击任意启动条件
			 AppOperate.click(element, "选择任意启动条件"); 
		 }
		 //点击添加执行任务
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/addBindTextView")), "点击添加执行任务");
		 //选择任意设备,判断设备是否为空
		 List<AndroidElement> list2=driver.findElements(By.id("com.orvibo.homemate:id/cbDevice"));
		 AndroidElement element2=list2.get(new Random().nextInt(list2.size()));
		 if(list2.size()>0)
		 {
			 //点击任意启动条件
			 AppOperate.click(element2, "执行任务中点击任意设备"); 
		 }
		 //返回到添加联动界面
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/back_iv")), "点击返回按扭");
		 //判断是否添加成功，有继续添加按扭
		 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"继续添加\")").isDisplayed());
		 //点击保存
		 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_tv")), "点击保存按扭");
		 //判断是否添加成功
		 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+linkagename+"\")").isDisplayed());
		 
		 }
	 
		 catch(Exception e)
		 {
			 
		 }
			
	 }
	 /**
	  * 编辑联动
	  */
	 @Test(groups={"联动","3.16"})
	 public void editlinkage()
	 {
			String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
			//判断联动是否为空
			List<String> link=linkagenumbers(nowfamilyname);
			int size=link.size();
			if(size!=0)
			{
				 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
				 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"联 动\")"), "点击'联动'");
				
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
		         //点击编辑按扭
		         AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑联动\")"), "点击编辑联动");
		         //点击任意一个编辑按扭进行编辑
		         int index=new Random().nextInt(size);
		         driver.findElements(By.id("com.orvibo.homemate:id/item_edit_image_layout")).get(index).click();
		         //进入联动编辑界面后，判断是否有删除任务的按扭
		         List<AndroidElement> list=driver.findElements(By.id("com.orvibo.homemate:id/ivDelete"));
		         Assert.assertTrue(list.size()>0);
		         //编辑名称
		        AndroidElement edit= driver.findElement(By.id("com.orvibo.homemate:id/intelligentSceneNameEditText"));
		        String inputstr=ToolFunctions.getRandomstring(5);
		        edit.sendKeys(inputstr);
		        //点击保存,看新名称是否显示在场景界面
		        AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/right_tv")), "点击保存");
		        Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+inputstr+"\")").isDisplayed());
				 
			}
			else
				Log.logInfo("当前联动数为0,无法做编辑或删除情景的操作");
	 }
	 /**
	  * 删除联动
	  */
	 @Test(groups={"联动","3.20"})
	 public void deletelink()
	 {
			String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
			//判断情景是否为空
			List<String> link=linkagenumbers(nowfamilyname);
			int size=link.size();
			if(size!=0)
			{
				 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
				 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"联 动\")"), "点击'联动'");
				
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/add_iv")), "点击添加按扭");
		         //点击编辑按扭
		         AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"编辑联动\")"), "点击编辑联动");
		         //点击任意一个编辑按扭进行编辑
		         int index=new Random().nextInt(size);
		         driver.findElements(By.id("com.orvibo.homemate:id/item_edit_image_layout")).get(index).click();
		         //点击删除联动的按扭
		         AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/deleteTextView")), "点击删除联动的按扭");
		         //提示框中点击确定删除
		         AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/rightButton")), "提示框中点击删除");
		         //跳转回情景界面后，判断是否已删除成功
		         Assert.assertTrue(driver.findElements(By.id("com.orvibo.homemate:id/nameTextView")).size()+1==size);
		        
				 
			}
			else
				Log.logInfo("当前联动数为0,无法做编辑或删除情景的操作");
	 }
	 
	 
	 /**
	  *服务器联动是否在前端显示
	  */
	 @Test(groups={"联动","3.16"})
	 public void checklinkage()
	 {   
		//查找当前家庭
		 String nowfamilyname=driver.findElement(By.id("com.orvibo.homemate:id/familyTextView")).getText();
		 int linknumbers=0;
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"场景\")"), "点击'场景'");
		 AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"联 动\")"), "点击联动");
		 //服务器拉取联动个数
		 System.out.println("当前家庭为"+nowfamilyname);
		 List<String> linkagelist=linkagenumbers(nowfamilyname);
		 Log.logInfo("当前联动数为:"+linkagelist.size());
		 //判断当前所有场景是否在前端展示
		 for(int i=0;i<linkagelist.size();i++)
		 {
			 String linkname=linkagelist.get(i);
			 int j=3;
			 boolean find = false;
			 while(j>0)
			 {
				 try{
					 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\""+linkname+"\")").isDisplayed());
					 find=true;
					 linknumbers++;
					 break;
				 }
				 catch(Exception e)
				 {
					 //未找到则向上滑动
					 AppOperate.swipeToUp(driver, "滑动查找场景");
			
				 }
				 j--;
				
				
			 }
			 Assert.assertTrue(find,linkname+"联动未在前端显示");
			 Log.logInfo(linkname+"在前端显示");
			 
		 }
		 Assert.assertEquals(linknumbers, linkagelist.size());
		 
	 }
	 
	 /**
	  * 智家优品
	  * 判断该用户是不是广告投放目标者，如果是则有智家优品的选项
	  * 判断该用户是不是广告投放目标者，如果不是则无智家优品的选项
	  * 
	  */
	 
	 @Test(groups={"智家优品"})
	 public void getAdvInfo()
	 {
		 //判断当前用户是不是广告投放目标
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
			   //进入我的界面
				newSleep(1);
				AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
			   //访问接口，查看此用户是否是广告投放目标
			  String target= HttpRequest.sendGet(Constant.HTTPURl, "imageSize=101&userId="+userId);
			  if(target.equals("true"))
			  {   
				  Log.logInfo("服务器返回数据显示该用户是广告投放目标");
				  //判断是否有智家优品的入口,通过名称判断
				 Assert.assertTrue(driver.findElementByAndroidUIAutomator("text(\"智家优品\")").isDisplayed());
				 //点击查看点击智家优品能否跳转
				 AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/iv_personal_store")), "点击智家优品");
				 //判断是否跳转成功,切换至h5
				 int count=15;
				 boolean find=false;
				 while(count>0)
				 { 
					 if(ToolFunctions.cmdmessage("adb shell dumpsys window w|grep name=", "StoreActivity"))
					 {
						 find=true;
						 break;
						 
					 }
				    newSleep(1);
				    count--;
				 }
				 Assert.assertTrue(find, "未能成功跳转至智家优品界面");
			  }
			  else
			  {   
				  Log.logInfo("服务器返回数据显示该用户不是广告投放目标");
				  //判断是否有智家优品的入口,通过适宜优品的图标判断
				  Assert.assertFalse(AppOperate.ifexitElement("com.orvibo.homemate:id/iv_personal_store", driver));
			  }
		 
		 
	 }

	
	public static void main(String args[])
	{
		//System.out.println("家庭数"+familynumbers());
		//sethomemanagename();
		//System.out.println(floornumbers("老家"));
		System.out.println(ifroomonfloor("老家","三楼"));

	}
	

	
	
	/**
	 * 进入家庭管理界面
	 */
	public void enterhomemanage()
	{
		newSleep(2);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"家庭管理\")"), "点击'我的'");
	}
	

	

	@AfterMethod(alwaysRun=true)
	public void tearDown(){
		//关闭appium 资源
		aftertest();
		driver.quit();
		
	}
	

}
