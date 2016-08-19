package cn.itcast.findimage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.EncodedKeySpec;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	protected final int REQUSTSUCESS = 0;
	protected final int REQUSTNOTFOUND = 1;
	protected final int REQUSTERROR = 2;

	private EditText et_url;
	private ImageView iv_show;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case REQUSTSUCESS:
				Bitmap bm = (Bitmap) msg.obj;
				iv_show.setImageBitmap(bm);

				break;
			case REQUSTNOTFOUND:
				Toast.makeText(getApplicationContext(), "找不到网页", 1).show();

				break;
			case REQUSTERROR:
				Toast.makeText(getApplicationContext(), "服务器忙，无法连接", 1).show();

				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		et_url = (EditText) findViewById(R.id.et_url);
		iv_show = (ImageView) findViewById(R.id.iv_show);

	}

	public void click(View v) {
		new Thread() {
			
			public void run() {
				try {String path = et_url.getText().toString().trim();
					File file = new File(getCacheDir(),Base64.encodeToString(path.getBytes(), Base64.DEFAULT));//base64可以加密文件名
				if(file.exists()){
					Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
					Message msg =Message.obtain();
					msg.what=REQUSTSUCESS;
					msg.obj=bitmap;
					handler.sendMessage(msg);
				}
				else{
					
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if (code == 200) {
						InputStream in = conn.getInputStream();
						
						FileOutputStream fos = new FileOutputStream(file);
						int len=-1;
						byte[] buffer=new byte[1024];
						while ((len=in.read(buffer))!=-1) {
							fos.write(buffer, 0, len);
						}
						in.close();
						fos.close();
						
						final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
						// 用new的方法获得message效率比较低用obtain由于会重用消息池所以效率高
						/*Message msg = Message.obtain();
						msg.what = REQUSTSUCESS;
						msg.obj = bitmap;
						handler.sendMessage(msg);*/
						runOnUiThread(new Runnable() {
							public void run() {
							iv_show.setImageBitmap(bitmap);
							}
						});

					} else {
						Message msg = Message.obtain();
						msg.what = REQUSTNOTFOUND;

						handler.sendMessage(msg);

					}}
				} catch (Exception e) {
					e.printStackTrace();

					Message msg = Message.obtain();
					msg.what = REQUSTERROR;

					handler.sendMessage(msg);
				}
			};
		}.start();

	}
}
