package org.stonerhawk.vkontakte;

import org.oauthtest.mytest.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class CaptchaDialog extends AlertDialog
{
	public interface CaptchaDialogListener 
	{
		public void onFinishEnterCaptcha(String captcha);
		public void onDialogCancel();
	}

	private Context _mContext;
	private CaptchaDialogListener _listener;
	private EditText text;
	private ImageView capthaImage;

	public CaptchaDialog(Context context) 
	{
		super(context);
		_mContext = context;
	}
	
	public void setListener(CaptchaDialogListener listener) 
    { 
    	_listener = listener; 
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
	}

	public Dialog onCreateDialog(int id)
	{
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.captcha_layout,(ViewGroup)findViewById(R.id.captcha_dialog));
		
		text = (EditText)layout.findViewById(R.id.captcha_text); //���� � ��������
		
		capthaImage = (ImageView)layout.findViewById(R.id.captha);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(_mContext);
		builder.setView(layout);
		builder.setMessage("������� ��� � ��������:");
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				_listener.onFinishEnterCaptcha(text.getText().toString());
			}
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				
				dialog.cancel();
				_listener.onDialogCancel();
			}
		});
		
		builder.setCancelable(false);
		return builder.create();
	}
	
}
