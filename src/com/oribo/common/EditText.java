package com.oribo.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;

import com.oribo.ReadExcelCase.writeExcel;
import com.oribo.android365.testcase.Personcenter;
import com.oribo.log.Log;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

public class EditText {
	 AndroidElement edittext;
	
	//16个汉字
	private  final String MAXCHINA="我是中国人我爱我的祖国我们是一家";
	//32个英文字符
	private  final String MAXENG="qwertyuiopasdfghjklzxcvbnmqwerty";
	private  final String ONECHAR="a";
	//超过32个英文字符
	private  final String GREMAXENG="qwertyuiopasdfghjklzxcvbnmqwertya";
	//超过16个汉字
	private  final String GREMAXCHINA="我是中国人我爱我的祖国我们是一家多";
	private  final String INCLUDENULL="adfa 我们 ￥%……￥%";
	private final String NEWPASSWORD="654321";
	//11位已绑定的手机号
	private final String PHONE="13088884763";
	//11位未绑定的手机号
	private final String PHONE2="13088884760";
	//小11位手机号
	private final String MINNUMBER="130888";
	//大于11位手机号
	private final String MAXNUMBER="1308888476212";
	//不是手机号码的11位数字
	private final String NOTPHONE="56789012345";
	//空格
	private final String NULL="  ";
	//已绑定的邮箱
	private final String EMAIL="15079034630@126.com";
	//未绑定的邮箱
	private final String EMAIL2="15079034631@126.com";
	
	//特殊字符
	private final String SPECIL1="++++++++.-(";
	//5个英文
	private final String SPECIL2="abced";
	String fivechar=null;
	
	public EditText(AndroidElement edittext)
	{
		this.edittext=edittext;
	}
	
   
	/**
	 * 输入旧密码,有隐患，只能从Personcenter中获取
	 */
	
	public void inputoldpassword(AndroidDriver driver)
	{  
		String old=Personcenter.getaccount().getPassWord();
		System.out.println("旧密码是:"+old);
		AppOperate.sendKeys(edittext, "输入旧密码",old );
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(),old);
	}
	/**
	 * 输入登录的账号
	 */
	public void inputaccount(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入正确的账号", Personcenter.getaccount().getAccount());
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(),Personcenter.getaccount().getAccount());
	}
	
	
	/**
	 * 输入16个汉字
	 */
	public  void inputmaxchina(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入16个字汉", MAXCHINA);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), MAXCHINA);
		
	}
	/**
	 * 输入空格
	 * @return
	 */
	public void inputnull(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入空格", NULL);
		AppOperate.submit(edittext, "确认输入",driver);
	}
	
	/**
	 * 输入11位手机号,此手机号已绑定
	 * @return
	 */
	public void inputphonenumber(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入11位手机号", PHONE);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), PHONE);
		
	}
	
	/**
	 * 输入11位手机号,此手机号未绑定
	 * @return
	 */
	public void inputphonenumber2(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入11位手机号", PHONE2);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), PHONE2);
		
	}
	/**
	 * 输入邮箱,此邮箱未绑定
	 */
	public void inputemail(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入邮箱", EMAIL2);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), EMAIL2);
		
	}
	
	
	/**
	 * 输入邮箱,此邮箱已绑定
	 */
	public void inputemailband(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入邮箱", EMAIL);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), EMAIL);
		
	}
	/**
	 * 获取输入的邮箱
	 */
	public String getemail(AndroidDriver driver)
	{
		return EMAIL;
	}
	/**
	 * 输入小11位的手机号
	 * @return
	 */
	public void inputlesphonenumber(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入小11位手机号", MINNUMBER);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), MINNUMBER);
	}
	
	/**
	 * 修改密码
	 * @return
	 */
	public void changepassword(AndroidDriver driver)
	{   
		String str=ToolFunctions.getRandomstring(6);
		AppOperate.sendKeys(edittext, "输入新密码", str);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), str);
		Personcenter.getaccount().setPassWord(str);
		Log.logInfo("修改的新 密码为:"+str);
		
		Map<String,String> map=new HashMap<>();
    	map.put("1", Personcenter.getaccount().getAccount());
    	map.put("2", str);
    	writeExcel.writeexcel(map,System.getProperty("user.dir")+File.separator+"testdata\\personInfo_TestData.xls",1);
	}
	
	/**
	 * 输入大于11位手机号
	 */
	public void inputgrephonenumber(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入大于11位手机号", MAXNUMBER);
		AppOperate.submit(edittext, "确认输入",driver);

	}
	
	/**
	 * 输入特殊字符
	 * @return
	 */
	public void inputSpecialchar(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入特殊字符", SPECIL1);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), SPECIL1);
		
	}
	
	public void inputSpecialchar2(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入特殊字符", SPECIL2);
		AppOperate.submit(edittext, "确认输入",driver);		
	}
	
	public String getSpecialchar2()
	{
		return SPECIL2;
	}
	/**
	 * 输入非手机号码数字
	 * @return
	 */
   public void notphone(AndroidDriver driver)
   {
		AppOperate.sendKeys(edittext, "输入特殊字符", NOTPHONE);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), NOTPHONE);
	   
   }
	public String getmaxchina()
	{
		return MAXCHINA;
	}
	
	public String getphonenumber()
	{
		return PHONE;
	}
	
	
	/**
	 * 输入32个英文
	 */
	public void inputmaxeng(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入32个英文",MAXENG);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertEquals(edittext.getText().trim(), MAXENG);
	}
	public String getmaxeng()
	{
		return MAXENG;
	}
	
	/**
	 * 输入超过32个字符能否再输入
	 */
	public boolean inputgremax(AndroidDriver driver)
	{  
		edittext.sendKeys(GREMAXENG);
		AppOperate.submit(edittext, "确认输入",driver);
		if(edittext.getText().length()>32)
			return false;
		else 
			return true;
	
	}
	
	/**
	 * 输入一个字符
	 * @return
	 */
	public void inputonechar(AndroidDriver driver)
	{
		AppOperate.sendKeys(edittext, "输入1个字符",ONECHAR);
		AppOperate.submit(edittext, "确认输入",driver);
	}
	
	/**
	 * 输入5个英文
	 * @return
	 */
	public void inputfivechar(AndroidDriver driver)
	{   
		fivechar=ToolFunctions.getRandomstring(5);
		AppOperate.sendKeys(edittext, "输入5个字符",fivechar);
		AppOperate.submit(edittext, "确认输入",driver);
		Assert.assertTrue(edittext.getText().equals(fivechar));
	}
	
	/**
	 * 返回输入的5个英文
	 */
	public String getfivechar()
	{
		return fivechar;
	}
	/**
	 * 返回单个字符的内容
	 * @return
	 */
	public String getonechar()
	{
		return ONECHAR;
	}
	public String gettext()
	{
		return edittext.getText().trim();
	}
	
	/**
	 * 清空文本框
	 */
	public void clear()
	{
		AppOperate.clear(edittext, "清空文本框");

	}
	

}
