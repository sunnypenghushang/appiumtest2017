package com.oribo.android365.testcase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.oribo.ReadExcelCase.ReadExcel;
import com.oribo.common.AppOperate;
import com.oribo.common.EditText;
import com.oribo.dataprovider.Constant;
import com.oribo.common.TestcaseFrame;
import com.oribo.dataprovider.AppBean;
import com.oribo.ReadExcelCase.DataBean;
import com.oribo.ReadExcelCase.ExcelData;
import com.oribo.dataprovider.UserInfo;
import com.oribo.log.LoggerUtil;
import com.oribo.report.CreateHtmlreport;
import com.oribo.report.MessageOutput;
import com.oribo.report.TestResultListener;
import com.oribo.utils.FileOperate;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

@Listeners({TestResultListener.class})
public class PhTestDome extends TestcaseFrame{
	AndroidDriver<AndroidElement> driver=null;
	public String receiver;//测试报告邮件接收人
	AppBean  appbean = AppBean.getAppBean();
	UserInfo account=UserInfo.getUserInfo();
	
	
	/**
	 * 获取被测APP的信息以及APP的登录帐户信息
	 * 设置测试环境
	 * @param port  启动的端口好
	 * @param udid 手机的IDID
	 * @param platformVersion  手机系统
	 * @param apk  测试的apk
	 */
	@BeforeClass(alwaysRun=true)
	@Parameters({ "port","udid","phone","platformVersion", "apk","testaccount","testpassword","reportreceiver"})
	public void beforeSuite(String port, String udid,String phone,String platformVersion ,String apk,String testaccount,String testpassword,String reportreceiver ){
		
		
		//保存app的基础信息		
		appbean.setUid(udid);
		appbean.setPort( port);
		appbean.setPhone(phone);
		appbean.setApk(apk);
		appbean.setPlatformVersion(platformVersion);
		//保存登录账号信息
		UserInfo account=UserInfo.getUserInfo();
		account.setAccount(testaccount);
		account.setPassWord(testpassword);
		FileOperate.delectLogFiles();

	 
	    
		
	}
	
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		ReadExcel  excel =  new ReadExcel();
		ExcelData  excelData =  new ExcelData();
		excelData.setNumerSheet(0);
		excelData.setCaseType(Constant.CASETYPE_ANDROID);
		excel.readXls("personInfo_TestData.xls", excelData,bean);
		FileOperate.delectLogFiles();
		
	}
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{   
		File classpathRoot = new File(System.getProperty("user.dir"));//本地的路径
		File appDir = new File(classpathRoot, "apps");//apk 存放的路径
		File app = new File(appDir, appbean.getApk());//apk 的名字	
		//android Appium 的 基础参数的设置
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("deviceName", "Android Emulator");	
		capabilities.setCapability("platformVersion", appbean.getPlatformVersion() );
		 capabilities.setCapability("udid", "LGD85786654604");
	//	capabilities.setCapability("app", app.getAbsolutePath()); 	
		//capabilities.setCapability( "automationName","Selendroid");//这句话设置可以获取toast 消息
		capabilities.setCapability("appPackage", "com.orvibo.homemate");
		capabilities.setCapability("appActivity", "com.orvibo.homemate.common.launch.LaunchActivity");//
		capabilities.setCapability("unicodeKeyboard", "True");  
		capabilities.setCapability("resetKeyboard", "True");  
		//capabilities.setCapability("setWebContentsDebuggingEnabled", "True");	
		//capabilities.setCapability("noSign", "True");
		capabilities.setCapability("noReset", true);//实现app不是每次都安装
		try {
			driver = new AndroidDriver(new URL("http://127.0.0.1:4724/wd/hub"), capabilities);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);//全局等待5秒

	}
	
	@Test
	public void logging()
	{   
		newSleep(2);
		DataBean  data = bean.get(0);
		String casename=data.getTestCaseName();
		AppOperate.click(driver.findElement(By.name("我的")), "点击'我的'");
		System.out.println("start---");
		System.out.println("end---");
		AppOperate.click(driver.findElement(By.id("")), "点击个人中心的头像");//第二个参数指步骤
		try {
			Assert.assertNotNull(driver.findElementByName("退出登录"));
			//截图
			screenCapresult(driver, casename);
			CreateHtmlreport.writeToLog(data.getTestCaseName(), data.getInstructions(),data.getExpectValue(), "跟期望值一样", "PASS");
				
		} catch (Exception e) {
			// TODO: handle exception
			//CreateHtmlreport.writeToLog(data.getTestCaseName(),data.getInstructions(),data.getExpectValue(), data.getActualValue(), "FAIL");//第二个参数指功能点，用例第一项
		}
		
	}
	
	/**
	 * 输入为空
	 */
	@Test(groups={"个人信息","昵称"})
	public void nickNull()
	{   
		DataBean  data = bean.get(11);
		System.out.println(data.toString());
		enterPersoninfo();
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"昵称\")"), "点击昵称");
     	AndroidElement edittext=(AndroidElement) driver.findElement(By.id("com.orvibo.homemate:id/userNicknameEditText"));
		AppOperate.clear(edittext, "清空昵称文本框");
		Assert.assertFalse(driver.findElement(By.id("com.orvibo.homemate:id/saveButton")).isEnabled(), "昵称为空时保存按扭可点击");
		
	}
	
	/**
	 *输入16个汉字能保存成功
	 */
	@Test(groups={"个人信息","昵称"})
	public void nickmaxchina()
	{
		DataBean  data = bean.get(12);
		System.out.println(data.getTestCaseName());
		enterPersoninfo();
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"昵称\")"), "点击昵称");
		EditText edittext=new EditText((AndroidElement) driver.findElement(By.id("com.orvibo.homemate:id/userNicknameEditText")));
		edittext.inputmaxchina(driver);
		AndroidElement save=(AndroidElement) driver.findElementByAndroidUIAutomator("text(\"保存\")");
		Assert.assertTrue(save.isEnabled());
		AppOperate.confirmButton(driver.findElementByAndroidUIAutomator("text(\"保存\")"), "点击保存按扭");
		//检查是否保存成功
		Assert.assertEquals(driver.findElement(By.id("com.orvibo.homemate:id/userNicknameTextView")).getText().trim(), edittext.getmaxchina());
		

	}
	
	/**
	 * 输入32个英文字母能保存成功
	 */
	@Test(groups={"个人信息","昵称"})
	public void nickmaxeng()
	{
		DataBean  data = bean.get(12);
		System.out.println(data.getTestCaseName());
		enterPersoninfo();
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"昵称\")"), "点击昵称");
		EditText edittext=new EditText((AndroidElement) driver.findElement(By.id("com.orvibo.homemate:id/userNicknameEditText")));
		edittext.inputmaxeng(driver);
		AndroidElement save=(AndroidElement) driver.findElementByAndroidUIAutomator("text(\"保存\")");
		Assert.assertTrue(save.isEnabled());
		AppOperate.confirmButton(driver.findElementByAndroidUIAutomator("text(\"保存\")"), "点击保存按扭");
		//检查是否保存成功
		Assert.assertEquals(driver.findElement(By.id("com.orvibo.homemate:id/userNicknameTextView")).getText().trim(), edittext.getmaxeng());

	}
	
	/**
	 * 修改，输入一个字符
	 */
	@Test(groups={"个人信息","昵称"})
	public void nickmodify()
	{
		DataBean  data = bean.get(13);
		System.out.println(data.getTestCaseName());
		enterPersoninfo();
		AndroidElement nick=(AndroidElement) driver.findElement(By.id("com.orvibo.homemate:id/userNicknameTextView"));
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/userNicknameTextView")), "点击昵称");
		EditText edittext=new EditText((AndroidElement) driver.findElement(By.id("com.orvibo.homemate:id/userNicknameEditText")));
		edittext.inputonechar(driver);
		AndroidElement save=(AndroidElement) driver.findElementByAndroidUIAutomator("text(\"保存\")");
		Assert.assertTrue(save.isEnabled());
		AppOperate.confirmButton(driver.findElementByAndroidUIAutomator("text(\"保存\")"), "点击保存按扭");
		//检查是否保存成功
		Assert.assertEquals(driver.findElement(By.id("com.orvibo.homemate:id/userNicknameTextView")).getText().trim(), edittext.getonechar());
		
		
		
	}
	/**
	 * 邮箱登录，手机号检查、手机登录检查邮箱绑定
	 */
	@Test(groups={"个人信息","账号绑定"},enabled=false)
	public void checkbind()
	{   
		DataBean  data = bean.get(24);
		enterPersoninfo();
		System.out.println("登录类型"+account.getLogingType());
		if(account.getLogingType()==Constant.LOGING_TYPE_EMAIL)
		{   
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"邮箱\")"), "点击进入邮箱绑定界面");
			AppOperate.exitElement("更换邮箱", driver);
			AppOperate.sendKeyEvent(4, "点击手机返回键",driver);
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"手机号\")"), "点击进入手机绑定界面");
			AppOperate.notExitElement("更换手机号", driver);	
			
		}
		else if(account.getLogingType()==Constant.LOGING_TYPE_PHONE)
		{
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"手机号\")"), "点击进入手机绑定界面");
			AppOperate.exitElement("更换手机号", driver);
			AppOperate.sendKeyEvent(4, "点击手机返回键",driver);
			AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"邮箱\")"), "点击进入邮箱绑定界面");
			AppOperate.notExitElement("更换邮箱", driver);		
		}
		
	}
	
	/**
	 * 进入个人信息
	 */
	
	public void enterPersoninfo()
	{   newSleep(2);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElement(By.id("com.orvibo.homemate:id/iv_personal_user_icon")), "点击个人信息头像");
		
	}
	
	
	
	
	
	
	public void enterMyhost()
	{   
		newSleep(2);
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的主机\")"), "点击'我的主机'");
		newSleep(2);
		
	}
	public void enterMore()
	{
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的\")"), "点击'我的'");
		AppOperate.click(driver.findElementByAndroidUIAutomator("text(\"我的主机\")"), "点击'我的主机'");
		AppOperate.swipeToUp(driver, "向上滑屏");
		AppOperate.click((AndroidElement) driver.findElementById("com.orvibo.homemate:id/moreTextView"), "点击更多");
		newSleep(2);
	}
	
	
	@AfterClass
	public void afterClass()
	{
		driver.quit();
		CreateHtmlreport.closeLog();
		
		
	}

}