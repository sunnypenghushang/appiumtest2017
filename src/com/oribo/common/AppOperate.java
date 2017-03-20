package com.oribo.common;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.oribo.log.Log;
import com.oribo.log.LoggerUtil;
import com.oribo.log.LoggerUtil.loggerLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by cindy on 16/10/11
 * 基础的操作的封装，拖动点击行等
 */
public abstract class AppOperate {
	private static AndroidDriver driver;

    public AppOperate(AndroidDriver driver) {
        this.driver = driver;
    }


    public abstract void acceptAlert();

    public abstract Boolean IdentifyIsDisplay(String[] TargetText, WebElement element);

    public abstract void backToHomePage(String[] contents);

    public abstract void scrollToUp(String TargetText);

    public abstract void scrollToDown(String TargetText);
    
    
    

	/**
	 * 在某个元素内滑动
	 * @param element
	 * @param driver
	 */
	public static void swipeElement(AndroidElement element,AndroidDriver driver)
	{
		driver.swipe(element.getLocation().getX()+element.getSize().width, element.getLocation().getY()+element.getSize().height,element.getLocation().getX(), element.getLocation().getY(),300);
		
	}
	
	/**
	 * 判断某个元素是否存在，无法识别ID和NAME的情况 
	 * @param element
	 * @param driver
	 */
	public static void exitElement(String element,AndroidDriver driver)
	{   
		 
			 Assert.assertTrue(driver.getPageSource().contains(element));
         
		
	
	}
	
	
/**
 * 通过getPageSource判断元素是否存在
 * @param element
 * @param driver
 * @return
 */
	
	public static boolean ifexitElement(String element,AndroidDriver driver)
	{
		if(driver.getPageSource().contains(element))
			return true;
		else 
			return false;
			
	}
	
/**
 * 删除通知栏信息(小米手机)
 */
	public static void clearNotification(AndroidDriver driver)
	{
		driver.openNotifications();
		driver.findElement(By.id("com.android.systemui:id/clear_all_button")).click();
	    sendKeyEvent(4,"返回",driver);
	}
	
	
	
	/**
	 * 不存在某个元素
	 * @param element
	 * @param driver
	 */
	public static void notExitElement(String element,AndroidDriver driver)
	{  
		Assert.assertFalse(driver.getPageSource().contains(element));
	
	}
	
	/**
	 * 长按某个元素
	 * @param driver
	 * @param element
	 */
	public static void longPress(AndroidDriver driver,WebElement element)
	{
		TouchAction action=new TouchAction(driver);
		action.longPress(element).perform();
		
	}
	/**
	 * 获取界面所有元素
	 * @param driver
	 * @return
	 */
	public static List<AndroidElement> getAllElements(AndroidDriver driver)
	{  
		List<AndroidElement> list=driver.findElementsByXPath("//*");
	    for(int i=0;i<list.size();i++)
	    {   
	    	AndroidElement e=list.get(i);    		
	    	
	    }
		return list;
		
	}
	
	/**
	 * 返回当前界面元素的个数
	 * @param driver
	 * @return
	 */
	public static int elememtNums(AndroidDriver driver)
	{
		return driver.findElementsByXPath("//*").size();
	}
	
	/**
	 * 返回当前界面文本元素的个数
	 * @param driver
	 * @return
	 */
	public static int eleTextNums(AndroidDriver driver)
	{   
		int countbyname=0;
		List<AndroidElement> list=driver.findElementsByXPath("//*");
	    for(int i=0;i<list.size();i++)
	    {   
	    	AndroidElement e=list.get(i);
	    	if(!e.getText().equals(""))
	    		countbyname++;
	    }

		return countbyname;
	}
    
    
    

    /**
     * 模拟轻触操作。
     *
     * @param width  x坐标.
     * @param height y坐标.
     */
    public void tapByCoordinate(double width, double height) {
        Map tap = new HashMap();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        tap.put("tapCount", new Double(2));
        tap.put("touchCount", new Double(2));
        tap.put("duration", new Double(0.5));
        tap.put("x", width);
        tap.put("y", height);
        Log.logInfo("轻触坐标: x = " + width + ", y = " + height);
        js.executeScript("mobile: tap", tap);
    }
    


    /**
     * 模拟轻触操作。
     *
     * @param width      x坐标.
     * @param height     y坐标.
     * @param tapCount   轻触手指数.
     * @param touchCount 轻触次数.
     * @param duration   持续时间
     */
    public void tapByCoordinate(double width, double height, double tapCount, double touchCount, double duration,Logger logger) {
        Map tap = new HashMap();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        tap.put("tapCount", tapCount);
        tap.put("touchCount", touchCount);
        tap.put("duration", duration);
        tap.put("x", width);
        tap.put("y", height);
        Log.logInfo("轻触坐标: x = " + width + ", y = " + height);
        js.executeScript("mobile: tap", tap);
    }
    /**
     * 座标点击
     */
    public static void clickbycoordinate(AndroidDriver driver,int x,int y){
    	int screenheight=driver.manage().window().getSize().height;
    	int screenwidth=driver.manage().window().getSize().width;
    	int x_click=x*screenwidth/1080;
    	int y_click=y*screenheight/1920;
    	driver.tap(1, x_click, y_click, 0);
    	 Log.logInfo("轻触坐标: x = " + x + ", y = " + y);
    	 newSleep(1);
   
    	//1080x1920
    }
    
    
    /**
     * 通过adb命令点击屏幕
     */
    public static void tapByadb(int x,int y)
    {   
    
    	String str;
    	int width = 0,height = 0;
		try {
			Process process=Runtime.getRuntime().exec("adb shell dumpsys window displays |grep init");
			BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((str=br.readLine())!=null)
			{   
				if(ToolFunctions.getMatch(str, "init="))
				{   
					String x1=str.split(" ")[4].split("=")[1].split("x")[0];
					String y1=str.split(" ")[4].split("=")[1].split("x")[1];
					width=Integer.parseInt(x1);
					height=Integer.parseInt(y1);
		
					
				/*	for(int i=0;i<list.length;i++)
					{
						System.out.println(i+":"+list[i]);
					}*/
				
					break;
				}

			}
			Log.logInfo("X为:"+width+"Y为："+height);
			br.close();
			int x_click=x*width/1080;
	    	int y_click=y*height/1920;
	    	Runtime.getRuntime().exec("adb shell input tap "+x_click+" "+y_click);
	    	newSleep(1);
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }
    
    public static void main(String[] args)
    {
    	tapByadb(201,1060);
    }
    
    /**
     * 向上滑屏
     * @param driver
     * @param during
     */
  	public static void swipeToUp(AndroidDriver driver,String logtext)
  	{     
  		Log.logStep("[" + logtext + "] ");
  		  int width = driver.manage().window().getSize().width;
          int height = driver.manage().window().getSize().height;
         // driver.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 3);
          try {
			Runtime.getRuntime().exec("adb shell input swipe "+width / 2+" "+height * 3 / 4+" "+width / 2+" "+ height / 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  	}
  	
  /**
   * 向下滑屏
   * @param driver
   * @param during
   */
  	public static void swipeToDown(AndroidDriver driver,int logtext)
  	{   
  		Log.logStep("[" + logtext + "] ");
  		int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        driver.swipe(width / 2, height / 4, width / 2, height * 3 / 4, 2);
  	}
  	
	/**
	 * override original method
	 * 按下指定的键,在原生方法执行时添加日志。
	 *
	 * @param keyEvent the element to be found.
	 * @param LogText  input log text.
	 */
	public static void sendKeyEvent(int keyEvent, String LogText,AndroidDriver driver) {
		Log.logStep("[" + LogText + "] ");
		driver.pressKeyCode(keyEvent);
		newSleep(1);
		
		
	}
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
	 * 向左滑屏
	 */
	public boolean swipeRightToLeft() {
		boolean isToHomePage = false;
		try {
			sendKeyEvent(AndroidKeyCode.KEYCODE_DPAD_RIGHT, "滑动引导页",driver);
			isToHomePage = true;
		} catch (Exception e) {
			Log.logInfo(e.getStackTrace());
		}
		return isToHomePage;
	}
	
    
    
    
    

    /**
     * 如果目标元素不响应,可尝试重复点击。
     *
     * @param element       尝试点击的元素.
     * @param find          等待消失的文本,可以设置多个.
     * @param maxRetryTimes 重试次数
     * @return boolean
     */
    public boolean clickToRetry(WebElement element, String[] find, int maxRetryTimes) {
        int retryTimes = 1;
        while (retryTimes <= maxRetryTimes) {
            Log.logInfo("第" + retryTimes + "次尝试点击......");
            try {
             //   click(element, "点击按钮");
            } catch (Exception e) {
                Log.logError("org.openqa.selenium.remote.SessionNotFoundException!");
            }

            if (!waitForText(driver,2, find)) {
                break;
            } else {
                retryTimes++;
            }
        }

        if (retryTimes > maxRetryTimes) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 如果目标元素不响应,可尝试重复点击。
     *
     * @param element       尝试点击的元素.
     * @param maxRetryTimes 重试次数
     * @return boolean
     */
    public boolean clickToRetry(WebElement element, int maxRetryTimes) {
        int retryTimes = 1;
        boolean unClickable = true;

        while (unClickable) {
            Log.logInfo("第" + retryTimes + "次点击......");
            try {
                click(element, "点击目标按钮");
                unClickable = false;
            } catch (Exception e) {
                e.printStackTrace();
                Log.logError(e.getStackTrace());
            }

            retryTimes++;

            if (retryTimes > maxRetryTimes)
                break;
        }

        return !unClickable;
    }

    /**
     * wait for the text present in timeout setting
     * 在指定时间内等待，直到控件出现在页面上。pageSource.contains方式判断
     *
     * @param timeoutInSeconds 设置等待时间,单位:秒.
     * @param TargetText       等待出现的文本,可以设置多个.
     * @return boolean
     */
   public static boolean waitForText(AndroidDriver driver,int timeoutInSeconds, String... TargetText) {
        Log.logStep("[Wait For Text : " + Arrays.toString(TargetText) + "] ");
        Log.logInfo("[Wait For Text : " + Arrays.toString(TargetText) + "] ");
        Boolean flag = false;
        String pageSource = null;
        long currentTime = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(3 * 1000);
                if (driver != null)
                    pageSource = driver.getPageSource();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (null != pageSource) {
                for (int i = 0; i < TargetText.length; i++) {
                    flag = pageSource.contains(TargetText[i]);
                }
            }

            if (System.currentTimeMillis() - currentTime >= timeoutInSeconds * 1000 || flag) {
                break;
            }
        }

        return flag;
    }

    /**
     * wait for the text present in timeout setting
     * 在指定时间内等待，直到文本消失在页面上。
     *
     * @param timeoutInSeconds 设置等待时间,单位:秒.
     * @param TargetText       等待消失的文本,可以设置多个.
     * @return boolean
     */
    public static void waitForTextDisappear(int timeoutInSeconds, String TargetText,AndroidDriver driver) {
        Log.logStep("[Wait For Text Disappear : " + TargetText + "] ");
        Boolean flag = true;
        long currentTime = System.currentTimeMillis();
        while (flag) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String pageSource = driver.getPageSource();

            
                flag = (pageSource.contains(TargetText));
            
            if (System.currentTimeMillis() - currentTime >= timeoutInSeconds * 1000 || !flag) {
                break;
            }
            
        }
      
    }

    /**
     * override original method
     * 模拟点击,在原生方法执行时添加日志。
     *
     * @param element the element to be found.
     * @param LogText input log text.
     */
    public static void click(final WebElement element, String LogText) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[点击 " + element.toString().substring(element.toString().indexOf("->")));
        element.click();
        newSleep(1);
    }

    /**
     * override original method
     * 模拟输入,在原生方法执行时添加日志。
     *
     * @param element       the element to be found.
     * @param LogText       input log text.
     * @param charSequences sendKeys content, support for multiple strings.
     */
    public static void sendKeys(WebElement element, String LogText, String charSequences) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[输入字符 " + element.toString().substring(element.toString().indexOf("->")));
        element.sendKeys(charSequences);
    }

    /**
     * override original method
     * 模拟清除,在原生方法执行时添加日志。
     *
     * @param element the element to be found.
     * @param LogText input log text.
     */
    public static void clear(final WebElement element, String LogText) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[清空数据 " + element.toString().substring(element.toString().indexOf("->")));
        element.clear();
    }
    
    
	/**
	 * 确认输入
	 */
	public static void submit(AndroidElement element,String LogText,AndroidDriver driver)
	{   
		Log.logStep("[" + LogText + "] ");
	    Log.logInfo("[输入后回车 " + element.toString().substring(element.toString().indexOf("->")));
	    driver.pressKeyCode(66);
		 
	}
	
	/**
	 * 点击确认或保存按扭
	 */
	public static void confirmButton(WebElement webElement,String LogText)
	{
		Log.logStep("[" + LogText + "] ");
	    Log.logInfo("[点击按扭 " + webElement.toString().substring(webElement.toString().indexOf("->")));
	    webElement.click();
	}

    /**
     * override original method
     * 获取文本,在原生方法执行时添加日志。
     *
     * @param element the element to be found.
     * @param LogText input log text.
     * @return String
     */
    public String getText(final WebElement element, String LogText) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[获取文本 " + element.toString().substring(element.toString().indexOf("->")));
        return element.getText();
    }

    /**
     * override original method
     * 获取属性,在原生方法执行时添加日志。
     *
     * @param element the element to be found.
     * @param LogText input log text.
     * @return String
     */
    public String getAttribute(final WebElement element, String attribute, String LogText) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[获取文本 " + element.toString().substring(element.toString().indexOf("->")));
        return element.getAttribute(attribute);
    }

    /**
     * override original method
     * 滚动到目标文本的位置,在原生方法执行时添加日志。
     *
     * @param targetText the target text to be scrollToExact.
     * @param LogText    input log text.
     */
    public void scroll_TargetText(String targetText, String LogText) {
        Log.logStep("[滑动屏幕定位目标文字:" + targetText + LogText + "] ");
    }

    /**
     * override original method
     * 模拟手指在目标元素上持续轻触一段时间,在原生方法执行时添加日志。
     *
     * @param fingers  the number of fingers.
     * @param element  the element to be found.
     * @param duration duration.
     * @param LogText  input log text.
     */
    public void tab(int fingers, WebElement element, int duration, String LogText) {
        Log.logStep("[" + LogText + "] ");
        Log.logInfo("[轻触 " + element.toString().substring(element.toString().indexOf("->")));
        ((AppiumDriver) driver).tap(fingers, element, duration);
    }

    /**
     * override original method
     * 锁屏指定时间后解锁,在原生方法执行时添加日志。
     *
     * @param LogText          input log text.
     * @param timeoutInSeconds time for waiting, unit: second.
     */
    public void lockScreen(String LogText, int timeoutInSeconds) {
        Log.logStep("[" + LogText + "] ");
      // ((AppiumDriver) driver).lockScreen(timeoutInSeconds);
    }

    /**
     * 长按源元素移至目标元素后释放。
     *
     * @param elementA the source element.
     * @param elementB the target element.
     * @param LogText  input log text.
     */
    public void swipe_elementA_TO_elementB(WebElement elementA, WebElement elementB, String LogText) {
        Log.logStep("[" + LogText + "] ");
        new TouchAction(((AppiumDriver) driver)).longPress(elementA)
                .moveTo(elementB).release().perform();
    }

    /**
     * 设置手机的网络连接状态,可以开关蓝牙、wifi、数据流量。
     *
     * @param LogText      input log text.
     * @param airplaneMode airplaneMode switch.
     * @param wifi         wifi switch.
     * @param data         data switch.
     */
    public void setNetworkConnection(String LogText, Boolean airplaneMode, Boolean wifi, Boolean data) {
        Log.logStep("[" + LogText + "] ");
     //   ((AndroidDriver) driver).setNetworkConnection(new NetworkConnectionSetting(airplaneMode, wifi, data));
        
    }

    /**
     * Js模拟向下滚动至目标元素。
     *
     * @param element the element to be found.
     * MyHost测试用例5，修改该函数，增加了static变量
     */
    public void scrollDown(WebElement element) {
        // java
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", "down");
        scrollObject.put("element", ((RemoteWebElement) element).getId());
        js.executeScript("mobile: scroll", scrollObject);
    }

    /**
     * Js模拟向上滚动至目标元素。
     *
     * @param element the element to be found.
     */
    public void scrollUP(WebElement element) {
        // java
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", "up");
        scrollObject.put("element", ((RemoteWebElement) element).getId());
        js.executeScript("mobile: scroll", scrollObject);
    }

    /**
     * Js模拟滚动至目标文本的位置。
     *
     * @param TargetText the target text to be found.
     */
    public void scrollTo(String TargetText) {
        Log.logStep("[滑动页面以发现'" + TargetText + "'] ");
   //     ((AndroidDriver) driver).scrollTo(TargetText);
        
    }

    /**
     * 模拟手势密码。
     *
     * @param webElements 手势密码元素对象.
     */
    public void GesturePWD(List<WebElement> webElements) {
        int x = webElements.get(0).getSize().width / 2;
        int y = webElements.get(0).getSize().height / 2;
        TouchAction touchAction = new TouchAction((AppiumDriver) driver);
        touchAction.press(webElements.get(0).getLocation().x + x, webElements.get(0).getLocation().y + y);
        for (int i = 1; i < webElements.size(); i++) {
            touchAction.waitAction(500);
            touchAction.moveTo(webElements.get(i).getLocation().x - webElements.get(i - 1).getLocation().x, webElements.get(i).getLocation().y - webElements.get(i - 1).getLocation().y);
        }
        touchAction.release();
        touchAction.perform();
    }

    /**
     * 模拟手势密码。
     *
     * @param GestureContainer 手势密码盘元素对象.
     * @param password         手势密码数组
     */
    public void GesturePWD(List<WebElement> GestureContainer, int[] password) {
        List<WebElement> webElements = new ArrayList<WebElement>();
        for (int i = 0; i < password.length; i++) {
            webElements.add(GestureContainer.get(password[i] - 1));
        }
        int x = webElements.get(0).getSize().width / 2;
        int y = webElements.get(0).getSize().height / 2;
        TouchAction touchAction = new TouchAction((AppiumDriver) driver);
        touchAction.press(webElements.get(0).getLocation().x + x, webElements.get(0).getLocation().y + y);
        for (int i = 1; i < webElements.size(); i++) {
            touchAction.waitAction(500);
            touchAction.moveTo(webElements.get(i).getLocation().x - webElements.get(i - 1).getLocation().x, webElements.get(i).getLocation().y - webElements.get(i - 1).getLocation().y);
        }
        touchAction.release();
        touchAction.perform();
    }

    /**
     * This Method for swipe up
     *
     * @param during wait for page loading
     * @author quqing
     */
    public void swipeToDown(int during) {
        int width = ((AppiumDriver) driver).manage().window().getSize().width;
        int height = ((AppiumDriver) driver).manage().window().getSize().height;
        ((AppiumDriver) driver).swipe(width / 2, height / 6, during, width / 2, height * 3 / 4);
    }

    /**
     * This Method for swipe down
     *
     * @param during wait for page loading
     * @author quqing
     */
    public void swipeToDown(int during, int startXDenominator, int startYDenominator, int endXDenominator, int endYDenominator) {
        int width = ((AppiumDriver) driver).manage().window().getSize().width;
        int height = ((AppiumDriver) driver).manage().window().getSize().height;
        ((AppiumDriver) driver).swipe(width / startXDenominator, height / startYDenominator, width / endXDenominator, height / endYDenominator, during);
    }

    /**
     * This Method for swipe up
     *
     * @param during wait for page loading
     * @author quqing
     */
    public void swipeToUp(int during) {
        int width = ((AppiumDriver) driver).manage().window().getSize().width;
        int height = ((AppiumDriver) driver).manage().window().getSize().height;
        ((AppiumDriver) driver).swipe(width / 2, height * 3 / 4, width / 2, height / 6, during);
    }

    public void swipeToUp(int startX, int startY, int endX, int endY, int during) {
        int width = ((AppiumDriver) driver).manage().window().getSize().width;
        int height = ((AppiumDriver) driver).manage().window().getSize().height;
        ((AppiumDriver) driver).swipe(startX, startY, endX, endY, during);
    }

    /**
     * Description: set element locate timeout.
     * 内容描述：设置对象查找超时时间.
     *
     * @param time The amount of time to wait.
     * @author quqing
     */
    public static void setElementLocateTimeout(long time) {
        driver.manage().timeouts().implicitlyWait(time, TimeUnit.SECONDS);
    }

    /**
     * wait for the element clickable in timeout setting
     * 在指定时间内等待，直到对象能够被点击。
     *
     * @param by               the element locator By
     * @param timeOutInSeconds The timeout in seconds when an expectation is called
     * @param sleepInMillis    The duration in milliseconds to sleep between polls.
     * @return boolean
     * @author quqing
     */
    public boolean waitForElementClickable(By by, long timeOutInSeconds, long sleepInMillis) {
        try {
            setElementLocateTimeout(timeOutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds - 5, sleepInMillis);
            return wait.until(ExpectedConditions.elementToBeClickable(by)) != null;
        } finally {
            setElementLocateTimeout(timeOutInSeconds);
        }
    }

    /**
     * judge if the element is present in specified seconds
     * 在指定的时间内判断指定的对象是否存在,兼容AndroidDriver&IOSDriver。
     *
     * @param by      the element locator By
     * @param seconds timeout in seconds
     * @return boolean
     */
    public boolean elementExists(int seconds, By... by) {
        boolean exists = false;
        setElementLocateTimeout(1);
        long start = System.currentTimeMillis();
        while (!exists && ((System.currentTimeMillis() - start) < seconds * 1000)) {
            try {
                if (driver instanceof AndroidDriver) {
                    exists = driver.findElements(by[0]).size() > 0;
                } else {
                    exists = driver.findElements(by[1]).size() > 0;
                }
            } catch (NoSuchElementException ne) {
                exists = false;
            }
        }
        setElementLocateTimeout(30);
        return exists;
    }
    
    /**
     * 等待元素出现,通过name获取
     */
    
    public static void waitelementexit(WebDriver driver,String text,int seconds)
    {   
    	boolean flag=true;
    	long start = System.currentTimeMillis();
    	while(flag&&((System.currentTimeMillis() - start) < seconds * 1000))
    	{
    		
    		try{
    		if(((AndroidElement) driver).findElementByAndroidUIAutomator("text(\""+text+"\")").isDisplayed())
    			flag=false;
    		}
    		catch(Exception e)
    		{
    			TestcaseFrame.newSleep(1);
    		}
    	}
    }

    /**
     * judge if the element is present in specified seconds
     * 在指定的时间内判断指定的对象是否存在。
     *
     * @param by       the element locator By
     * @param duration the time during which something continues.
     * @return boolean
     */
    public static boolean elementExists(int duration, By by) {
        boolean exists = false;
        setElementLocateTimeout(1);
        long start = System.currentTimeMillis();
        while (!exists && ((System.currentTimeMillis() - start) <= duration * 1000)) {
            try {
                exists = (driver.findElements(by).size() > 0) ? true : false;
            } catch (NoSuchElementException ne) {
                exists = false;
            }
        }
        setElementLocateTimeout(30);
        return exists;
    }

    /**
     * judge if the element is present in specified seconds
     * 在指定的时间内判断指定的对象是否存在。
     *
     * @param webElement the element
     * @param duration   the time during which something continues.
     * @return boolean
     */
    public boolean elementExists(int duration, WebElement webElement) {
        boolean exists = false;
        setElementLocateTimeout(1);
        long start = System.currentTimeMillis();
        while (!exists && ((System.currentTimeMillis() - start) <= duration * 1000)) {
            try {
                exists = (null != webElement) ? true : false;
            } catch (NoSuchElementException ne) {
                exists = false;
            }
        }
        setElementLocateTimeout(30);
        return exists;
    }

    /**
     * 在指定时间内等待，直到对象能够被点击.
     *
     * @param androidBy
     * @param iosBy
     * @param element
     * @param timeOutInSeconds
     * @param sleepInMillis
     * @param desc
     * @return boolean
     */
    public boolean clickClickableElement(By androidBy, By iosBy, WebElement element, long timeOutInSeconds, long sleepInMillis, String... desc) {
        boolean clickable = false;

        try {
            if (driver instanceof AndroidDriver) {
                clickable = waitForElementClickable(androidBy, timeOutInSeconds, sleepInMillis);
            } else {
                clickable = waitForElementClickable(iosBy, timeOutInSeconds, sleepInMillis);
            }

            if (clickable) {
                if (null == desc) {
                    element.click();
                } else {
                    click(element, desc[0]);
                }
            }
        } catch (Exception e) {
            Log.logError(e.getStackTrace());
            e.printStackTrace();
        } finally {
            return clickable;
        }
    }

}

