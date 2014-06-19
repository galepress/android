package com.artifex.mupdfdemo;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import ak.detaysoft.galepress.GalePressApplication;

public class LinkInfoExternal extends LinkInfo {
	final public String url;
	public String sourceUrl;

    public static final int  ANNOTATION_TYPE_PAGELINK = 0;
    public static final int  ANNOTATION_TYPE_WEBLINK = 1;
    public static final int  ANNOTATION_TYPE_WEB = 2;
    public static final int  ANNOTATION_TYPE_MAP = 3;

    public int annotationType = -1;
    public boolean isModal = false;
    public boolean isInternal = true;
    public int webViewId = -1;

	public LinkInfoExternal(float l, float t, float r, float b, String u) {
		super(l, t, r, b);
		url = u;

        if(url.contains("modal=1")){
            isModal = true;
        }
        if(url.substring(0,4).equals("http")){
            annotationType = ANNOTATION_TYPE_WEBLINK;
        }
        else if(url.substring(0,5).equals("ylmap")){
            annotationType = ANNOTATION_TYPE_MAP;
        }
        else if(url.substring(0,5).equals("ylweb")){
            annotationType = ANNOTATION_TYPE_WEB;
            if(url.substring(0,11).equals("ylweb://www")){
                isInternal = false;
                sourceUrl = "http://"+url.substring(8);
            }
            else if (url.substring(0,17).equals("ylweb://localhost")){
                isInternal = true;
                sourceUrl = url.substring(18);
            }
        }

	}

    public String getSourceUrlPath(Context context){
        if(isInternal){
            try{
                MuPDFCore core = ((MuPDFActivity)context).core;
                StringBuilder stringBuilder = new StringBuilder("file://");
                stringBuilder.append(context.getFilesDir().getAbsolutePath());
                stringBuilder.append("/");
                stringBuilder.append(core.content.getId().toString());
                stringBuilder.append("/");
                stringBuilder.append(sourceUrl);
                return stringBuilder.toString();
            }
            catch (Exception e){
                e.printStackTrace();
                Log.e("Adem",e.getLocalizedMessage());
                return null;
            }
        }
        else{
            return sourceUrl;
        }
    }

	public void acceptVisitor(LinkInfoVisitor visitor) {
		visitor.visitExternal(this);
	}
}
