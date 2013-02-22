package com.skyline.application.i18n;

import java.beans.Beans;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private Messages() {
		// do not instantiate
	}

	private static final String BUNDLE_NAME = "com.skyline.application.i18n.messages";
	private static final ResourceBundle RESOURCE_BUNDLE = loadBundle();
	
	private static ResourceBundle loadBundle() {
		return ResourceBundle.getBundle(BUNDLE_NAME);
	}

	public static String getString(String key) {
		try {
			ResourceBundle bundle = Beans.isDesignTime() ? loadBundle() : RESOURCE_BUNDLE;
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		}
	}
	
	public static String getString(@SuppressWarnings("rawtypes") Class cls, String key){
		return getString(cls.getName()+"."+key);
	}
	
	public static String getString(Object o, String key){
		return getString(o.getClass(),key);
	}
}
