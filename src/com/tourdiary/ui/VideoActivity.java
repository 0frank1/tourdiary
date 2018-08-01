package com.tourdiary.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.zh.tourdiary.R;
import com.tourdiary.util.Entity;
import com.tourdiary.util.OnFinishListener;
import com.tourdiary.util.VideoCapture;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class VideoActivity extends Activity implements OnFinishListener {

	private Button   mMainBtnStart;
	private GridView gridView;
	public ImageSelectAdapter imgAdapter;
	//
	public List<Entity> imglist=new ArrayList<Entity>();
	public  List<String> filelist=new ArrayList<String>();
	private String str=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zh";//视频文件根目录
	private String str1=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zheng";//图片文件根目录
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		VideoCapture.setFinishListener(this);
		mMainBtnStart=(Button) findViewById(R.id.bt_tovideo);
		gridView=(GridView) findViewById(R.id.gv_tovlist);
		//新建一个适配器。用来适配ListView，显示文件列表
		imgAdapter= new ImageSelectAdapter(this);
		//获取当前日记文件夹
		String diaryname=getDiaryName();
		//显示默认的文件目录
		imgAdapter.scanFiles(str1+File.separator+diaryname);
		imgAdapter.initData();
		//设置适配器，绑定适配器
		gridView.setAdapter(imgAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/**
				 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，来判断是否显示选中效果。
				 * 至于选中效果的规则，下面适配器的代码中会有说明
				 */
		        
				if(imglist.get(position).isSelected()){
					imglist.get(position).setSelected(false);
				}else{
					imglist.get(position).setSelected(true);
				}
				/**
				 * 通知适配器，绑定的数据发生了改变，应当刷新视图
				 */
				imgAdapter.notifyDataSetChanged();
			}
			
		});
		mMainBtnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkFilePath(str);
				mMainBtnStart.setText("进行中");
				//获取当前日记文件夹
				String diaryname=getDiaryName();
				getSelectFile(imglist);
		        VideoCapture.start(Environment.getExternalStorageDirectory()
		                                      .getAbsolutePath() + File.separator + "zh" ,filelist);
//		        + File.separator + diaryname
				
			}
		});
	}
	
	//检查视频储存地址是否存在
    public void checkFilePath(String strImgPath){
    	File path=new File(strImgPath);
		if(!path.exists()){
			path.mkdir();
		}
    }

	//获取被选择的图片地址
	public void getSelectFile(List<Entity> imglist){
		for(Entity e:imglist){
			if(e.isSelected()){
				filelist.add(e.getImageUri());
			}
		}
	}
	@Override
	public void OnFinish() {
		mMainBtnStart.setText("开始");
        Toast.makeText(VideoActivity.this , "生成完成" , Toast.LENGTH_LONG).show();
	}

	public String getDiaryName(){
    	SharedPreferences sp;
    	sp = getSharedPreferences("config",Context.MODE_APPEND);
		String diaryname=sp.getString("cur_diary_name", ""); 
    	return diaryname;
    }
	
	public class ImageSelectAdapter extends BaseAdapter {

		private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
		//创建view时必须要提供context
			public Activity activity;	
			//提供数据源，文件列表
			public List<File> list=new LinkedList<File>();	
			
			//当前列表路径
			public String currPath;	
			
			public int getCount() {

				return list.size();
			}

			public Object getItem(int position) {

				return position;
			}
			
			public long getItemId(int position) {
				return position;
			}

		public View getView(int position, View arg1, ViewGroup arg2) {
			// 申明一个视图装载gridView条目
			if(arg1 == null)
            {
				arg1 = mInflater.inflate(R.layout.imageselect_items, null);
            }
				ImageView img = (ImageView) arg1.findViewById(R.id.imgselectfolder);
				ImageView img1 = (ImageView)arg1.findViewById(R.id.isselected);
				// 获取当前位置
				File f = list.get(position);
				// 显示出文件夹的图片
				img.setImageBitmap(getImageThumbnail(currPath + File.separator + f.getName(), 400, 400));
			if (imglist.get(position).isSelected()) {
				img1.setVisibility(View.VISIBLE);
			} else {
				img1.setVisibility(View.GONE);
			}
			// 肉眼视图
			return arg1;
		}


			//扫描文件夹
			public void scanFiles(String path) {
				
				list.clear();
				File dir=new File(path);
				File[] subFiles=dir.listFiles();
				//生成文件列表
				if(subFiles!=null){
					for(File f:subFiles)
						list.add(f);
				}
				this.notifyDataSetChanged();		
				currPath=path;

			}
			
			/**
			 * 初始化数据
			 */
			public void initData(){
				/**
				 * 这里，我们假设已经从网络或者本地解析好了数据，所以直接在这里模拟了10个实体类，直接装进列表中
				 */
				imglist = new ArrayList<Entity>();
				for(File f:list){
					Entity e=new Entity(currPath+File.separator+f.getName(), false);
					imglist.add(e);
				}
			}
			/** 
		     * 根据指定的图像路径和大小来获取缩略图 
		     * 此方法有两点好处： 
		     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度， 
		     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 
		     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 
		     *        用这个工具生成的图像不会被拉伸。 
		     * @param imagePath 图像的路径 
		     * @param width 指定输出图像的宽度 
		     * @param height 指定输出图像的高度 
		     * @return 生成的缩略图 
		     */  
		    private Bitmap getImageThumbnail(String imagePath, int width, int height) {  
		        Bitmap bitmap = null;  
		        BitmapFactory.Options options = new BitmapFactory.Options();  
		        options.inJustDecodeBounds = true;  
		        // 获取这个图片的宽和高，注意此处的bitmap为null  
		        bitmap = BitmapFactory.decodeFile(imagePath, options);  
		        options.inJustDecodeBounds = false; // 设为 false  
		        // 计算缩放比  
		        int h = options.outHeight;  
		        int w = options.outWidth;  
		        int beWidth = w / width;  
		        int beHeight = h / height;  
		        int be = 1;  
		        if (beWidth < beHeight) {  
		            be = beWidth;  
		        } else {  
		            be = beHeight;  
		        }  
		        if (be <= 0) {  
		            be = 1;  
		        }  
		        options.inSampleSize = be;  
		        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
		        bitmap = BitmapFactory.decodeFile(imagePath, options);  
		        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
		        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
		                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
		        return bitmap;  
		    } 
		    
			public ImageSelectAdapter(Context context){
				
				this.mInflater = LayoutInflater.from(context);
				
			}

	}
}
