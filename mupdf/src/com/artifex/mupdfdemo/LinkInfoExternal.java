package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

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
                Log.e("Adem","MuPDFActivity.getInstance().getFilesDir() : "+MuPDFActivity.getInstance().getFilesDir());
                Log.e("Adem","MuPDFActivity.getInstance().getFilesDir() : "+MuPDFActivity.getInstance().content.getId());
                Log.e("Adem","Annotation Url : "+url);

                sourceUrl = url.substring(18);
            }
        }

	}

	public void acceptVisitor(LinkInfoVisitor visitor) {
		visitor.visitExternal(this);
	}
}
