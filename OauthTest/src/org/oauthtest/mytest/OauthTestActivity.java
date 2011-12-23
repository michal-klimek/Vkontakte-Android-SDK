package org.oauthtest.mytest;

import org.oauthtest.mytest.Vkontakte.VkontakteListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class OauthTestActivity extends Activity implements OnClickListener, VkontakteListener
{
	private Vkontakte vk = null;
	
	private ImageView userImage;
	private Button loginButton;
    private Button sendMsg;
    private Button postToWallButton;
    private TextView nameText;
    private TextView surnameText;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        vk = Vkontakte.getInstance(this);
        vk.setListener(this);
        
        userImage = (ImageView)findViewById(R.id.userImage);
        nameText = (TextView)findViewById(R.id.name);
        surnameText = (TextView)findViewById(R.id.surname);
        
        sendMsg = (Button)findViewById(R.id.sendMessage);
        sendMsg.setOnClickListener(this);
        
        postToWallButton = (Button)findViewById(R.id.posttowall);
        postToWallButton.setOnClickListener(this);
        
        loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(this);
        
        configureViewForState(vk.isAuthorized());
    }
    
	private void configureViewForState(boolean authorized) 
	{
		int viewState;
		int loginButtonText;
		if (authorized) 
		{
			viewState = View.VISIBLE;
			loginButtonText = R.string.logout;
		} 
		else 
		{
			viewState = View.GONE;
			loginButtonText = R.string.login;
		}
		
		loginButton.setText(loginButtonText);
		nameText.setVisibility(viewState);
		surnameText.setVisibility(viewState);
	
		sendMsg.setVisibility(viewState);
		sendMsg.setClickable(authorized);
	
		postToWallButton.setVisibility(viewState);
		postToWallButton.setClickable(authorized);
	
		userImage.setVisibility(viewState);
		userImage.setClickable(authorized);
	}

	@Override
	public void onClick(View v) 
	{
		if (v.getId() == R.id.login) 
		{
			if (vk.isAuthorized()) 
			{
				vk.logOut();
			} 
			else 
			{
				vk.showLoginDialog();
			}
		} 
		else if (v.getId() == R.id.posttowall) 
		{
			//vk.postToWall("Test"); //showDialog(IDD_CUSTOM);
			CaptchaDialog d = new CaptchaDialog(this);
			d.show();
		}
	}
	
	@Override
	public void onPostComplete(String result) 
	{
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		String r = result;
	}

	@Override
	public void onPostError(String result) 
	{
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		String r = result;
	}

	@Override
	public void onLoginComplete(String result) 
	{
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		String r = result;
		configureViewForState(vk.isAuthorized());
	}

	@Override
	public void onLoginError(String result) 
	{
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		String r = result;
	}

	@Override
	public void didEndGettingUserInfo(String result) 
	{
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		String r = result;
	}
}