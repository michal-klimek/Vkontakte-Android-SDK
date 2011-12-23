package org.oauthtest.mytest;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.oauthtest.mytest.VkDialog.VkDialogListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class Vkontakte implements VkDialogListener 
{
    //constants for OAUTH AUTHORIZE in Vkontakte
	public static final String APP_ID ="2440436";
    public static final String CALLBACK_URL = "http://api.vkontakte.ru/blank.html";
    private static final String OAUTH_AUTHORIZE_URL = "http://api.vkontakte.ru/oauth/authorize?client_id=" + APP_ID + "&scope=wall,photos&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token"; 
    private final String PREFS_NAME = "Vk:Captcha";    
    private SharedPreferences _prefs;
    private SharedPreferences.Editor _editor;
    
    private Context _context;
    private VkontakteListener _listener;
    private VkSession _vkSess;
    private boolean _isCaptcha;
    
    private String _accessToken;
    private String _expiresIn;
    private String _userId;
    private long _accessTime;
    
    private String VK_LOGOUT_URL = "http://api.vkontakte.ru/oauth/logout";
    private String VK_API_URL = "https://api.vkontakte.ru/method/";
    //private String VK_POST_TO_WALL_URL = VK_API_URL + "wall.post?";
    
    private volatile static Vkontakte instance;
    
    private Vkontakte(Context context)
    {
		_context = context;
		_vkSess = new VkSession(_context);
		_prefs = _context.getSharedPreferences(PREFS_NAME, 0);
 	   _editor = _prefs.edit();
		CookieSyncManager.createInstance(context); 
	}
    
    private void fillTokenData(String[] params)
    {
		if (params != null) 
		{
			_accessToken = params[0];
			_expiresIn = params[1];
			_userId = params[2];
		}
    }
 
    public static Vkontakte getInstance(Context context) 
    {
        if (instance == null) 
        {
            synchronized (Vkontakte.class) 
            {
                if (instance == null) 
                {
                    instance = new Vkontakte(context);
                }
            }
        }
        return instance;
    }
    
    public void setListener(VkontakteListener listener) 
    { 
    	_listener = listener; 
    }
	
	public void showLoginDialog()
	{
	    new VkDialog(_context,OAUTH_AUTHORIZE_URL, this).show();	
	}
	
	public boolean isAuthorized()
	{
		String[] params = _vkSess.getAccessToken();
		if (params != null) 
		{
			fillTokenData(params);
			_accessTime = Long.parseLong(params[3]); 
			
			long currentTime = System.currentTimeMillis();
			long expireTime = (currentTime - _accessTime) / 1000;
			
			if( _accessToken.equals("") & _expiresIn.equals("") & _userId.equals("") & _accessTime == 0 )
			{  
				return false;
			}
			else if( expireTime >= Long.parseLong(_expiresIn) ) 
			{
				return false;
			}
			else 
			{
				return true;
			}
		}
		return false;
	}
	
	public void logOut()
	{
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(VK_LOGOUT_URL);
        
        try 
        {
             HttpResponse response = client.execute(request);
             HttpEntity entity = response.getEntity();

             //String responseText = EntityUtils.toString(entity);
             CookieManager.getInstance().removeAllCookie();
             resetAccessToken();
        }
        catch(ClientProtocolException cexc)
        {
    	    cexc.printStackTrace();
        }
        catch(IOException ioex)
        {
    	    ioex.printStackTrace();
        }
	}
    
    private String sendRequestWithCaptcha(String request, Boolean captha)
    {
    	if (captha) 
    	{
			String captcha_sid = null;
			String captcha_user = null;
			request = request + "&captcha_sid=" + captcha_sid + "&captcha_key=" + Uri.encode(captcha_user);
		}
    	
    	HttpClient client = new DefaultHttpClient();
        HttpGet httpRequest = new HttpGet(request);

        try 
        {
             HttpResponse response = client.execute(httpRequest);
             HttpEntity entity = response.getEntity();

             String responseText = EntityUtils.toString(entity);
    
             JSONObject jsonObj = new JSONObject(responseText);
             
             if( jsonObj.has("error") ) 
             {
            	 JSONObject errorObj = jsonObj.getJSONObject("error");
                 int errCode = errorObj.getInt("error_code");
                 if( errCode == 14)
                 {
              	   _isCaptcha = true;
              	   
              	   String captcha_sid = errorObj.getString("captcha_sid");
              	   String captcha_img = errorObj.getString("captcha_img");
              	   
              	   _editor.putString("captcha_img", captcha_img);
              	   _editor.putString("captcha_sid", captcha_sid);
              	   _editor.putString("request", request);
              	   _editor.commit();
              	   
              	   getCaptcha();
                 }
             }
       }
       catch(ClientProtocolException cexc)
       {
    	   cexc.printStackTrace();
       }
       catch(IOException ioex)
       {
    	   ioex.printStackTrace();
       }
       catch (JSONException e) 
       {
           e.printStackTrace();
       }
    	
        return null;
    }
    
    private void getCaptcha() 
    {
		String captcha_img = _prefs.getString("captcha_img", "");
	}

	public String[] getAccessToken(String url) 
	{
		String[] query = url.split("#");
		String[] params = query[1].split("&");
		params[0] = params[0].replace("access_token=", "");
		params[1] = params[1].replace("expires_in=", "");
		params[2] = params[2].replace("user_id=", "");
		fillTokenData(params);
		return params;
	}
	
	public void saveAccessToken(String accessToken, String expires, String userId) 
	{
		_vkSess.saveAccessToken(accessToken, expires, userId);
	}
	
	public void resetAccessToken() 
	{ 
		_vkSess.resetAccessToken(); 
	}
    
    public boolean postToWall(String message) 
    {
        String wallPostRequest = VK_API_URL + "wall.post?";
        wallPostRequest += "owner_id=" + _userId + "&access_token="+ _accessToken + "&message="+ Uri.encode(message);
				
		sendRequestWithCaptcha(wallPostRequest, false);
		
		return true;
	}
	
	public interface VkontakteListener 
	{
		void onPostComplete(String result);
		void onPostError(String result);
		void onLoginComplete(String result);
		void onLoginError(String result);
		void didEndGettingUserInfo(String result);
	}

	@Override
	public void onComplete(String url) 
	{
		String[] tokenData = getAccessToken(url);
		saveAccessToken(tokenData[0], tokenData[1], tokenData[2]);
		_listener.onLoginComplete(url);
	}

	@Override
	public void onError(String description) 
	{
		_listener.onLoginError(description);
	}
}
