package com.mj.exportfile.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * @addtime 2015-07-03 13:50:08
 * @author gl
 */
public class FileUtil {

	/**
	 * 利用FileWriter构造方法中的第二个参数实现内容的追加
	 * @param f 文件
	 * @param context 内容
	 */
	public static void appendFW(File f,String context){
		try{
			if(!f.exists()){
				f.createNewFile();
			}
			FileWriter fw  = new FileWriter(f, true);
				fw.write(context);
				fw.flush();		
				fw.close();				
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void copyFile(String oldPath, String newPath ,String fileName) {
		copyFile(oldPath+fileName , newPath+fileName);
	}
	
	public static void copyFile(String oldFile, String newFile) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int byteread = 0;
			File oldfile = new File(oldFile);
			if (oldfile.exists()) { // 文件存在时
				newFile = newFile.replaceAll("//", "\\\\");
				String newPath = "";
				if(System.getProperties().getProperty("os.name").equals("Mac OS X")){
					newPath = newFile.substring(0,newFile.lastIndexOf("/"));
				}else{
					newFile = newFile.replaceAll("/", "\\\\");
					newPath = newFile.substring(0,newFile.lastIndexOf("\\"));
				}
				
				
				inStream = new FileInputStream(oldfile); // 读入原文件
				if(!new File(newPath).exists()){
					new File(newPath).mkdirs();
				}
				fs = new FileOutputStream(newFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			try {
				inStream.close();
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
