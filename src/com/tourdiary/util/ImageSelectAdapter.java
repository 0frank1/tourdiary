package com.tourdiary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.lbsapi.panoramaview.PanoramaView.ImageDefinition;
import com.zh.tourdiary.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageSelectAdapter extends BaseAdapter {

	//创建view时必须要提供context
		public Activity activity;	
		//提供数据源，文件列表
		public List<File> list=new LinkedList<File>();	
		//
		public List<Entity> imglist=new ArrayList<Entity>();
		//当前列表路径
		public String currPath;	
		private Bitmap bmp_folder;
		
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
		ImageView img1 = null;
		if (arg1 == null) {
			arg1 = View.inflate(activity, R.layout.imageselect_items, null);

			// TextView imgName=(TextView) arg1.findViewById(R.id.img_name);
			ImageView img = (ImageView) arg1.findViewById(R.id.imgselectfolder);
			img1 = (ImageView) arg1.findViewById(R.id.isselected);
			// 获取当前位置
			File f = list.get(position);
			// 获取文件名
			// imgName.setText(f.getName());
			// 显示出文件夹的图片
			img.setImageBitmap(getImageThumbnail(currPath + File.separator + f.getName(), 400, 400));
			// img.setImageBitmap(BitmapFactory.decodeFile(currPath+File.separator+f.getName()));

		} else {
			img1=(ImageView) arg1;
		}
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
				Entity e=new Entity(currPath+f.getName(), false);
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
	    
		public ImageSelectAdapter(Activity activity){
			
			this.activity=activity;
			
		}

}
