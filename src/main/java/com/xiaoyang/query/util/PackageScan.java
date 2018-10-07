package com.xiaoyang.query.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描包下的java文件
 * 
 * @author dongjiangzheng 2018年1月14日
 *
 */
public class PackageScan {

	List<Class<?>> scan(String _package) {
		URI uri;
		try {
			uri = PackageScan.class.getClassLoader().getResource(_package.replace('.', '/')).toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("code gen dao load url fail", e);
		}
		List<Class<?>> classs = new ArrayList<Class<?>>();
		File dir = new File(uri);
		scanClass(_package, classs, dir);
		return classs;
	}

	/**
	 * 扫描包下的.class文件
	 * 
	 * @param _package
	 * @param classs
	 * @param dir
	 */
	private void scanClass(String _package, List<Class<?>> classs, File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File f : files) {
				if (f.isFile() && f.getName().endsWith(".class")) {
					Class<?> clazz = null;
					try {
						clazz = Class.forName(_package + "." + f.getName().replace(".class", ""));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if (clazz != null) {
						classs.add(clazz);
					}
				} else {
					scanClass(_package, classs, dir);
				}
			}
		}
	}
	
	
	
	
	
}
