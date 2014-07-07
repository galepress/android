package com.artifex.mupdfdemo;

import android.content.Context;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.net.MalformedURLException;
import java.net.URL;

import ak.detaysoft.galepress.GalePressApplication;

public class LinkInfoExternal extends LinkInfo {
	 public String url;
	public String sourceUrl;

    public static final int  ANNOTATION_TYPE_PAGELINK = 0;
    public static final int  ANNOTATION_TYPE_WEBLINK = 1;
    public static final int  ANNOTATION_TYPE_WEB = 2;
    public static final int  ANNOTATION_TYPE_MAP = 3;

    public int annotationType = -1;
    public boolean isModal = false;
    public boolean isInternal = true;
    public int webViewId = -1;
    public LatLng location;
    public float zoom;

	public LinkInfoExternal(float l, float t, float r, float b, String u) {
		super(l, t, r, b);
		url = u;

        if(url.contains("modal=1")){
            isModal = true;
            if(url.contains("?modal=1")){
                url = url.replace("?modal=1", "");
            }
            else if(url.contains("modal=1")){
                url = url.replace("modal=1", "");
            }
        }
        if(url.substring(0,4).equals("http")){
            annotationType = ANNOTATION_TYPE_WEBLINK;
        }
        else if(url.substring(0,5).equals("ylmap")){
            annotationType = ANNOTATION_TYPE_MAP;
            Uri uri=Uri.parse(url);
            Double lat = new Double(uri.getQueryParameter("lat"));
            Double lon = new Double(uri.getQueryParameter("lon"));
            location = new LatLng(lat,lon);
            Double zoomValue = new Double(uri.getQueryParameter("slon"));
            float zoomlevel = (int)(zoomValue / new Double("0.01"));
            zoom = (zoomlevel * 1.9f)+2.0f;
        }
        else if(url.substring(0,5).equals("ylweb")){
            annotationType = ANNOTATION_TYPE_WEB;
            if (url.substring(0,17).equals("ylweb://localhost")){
                isInternal = true;
                sourceUrl = url.substring(18);
            }
            else{
                isInternal = false;
                sourceUrl = "http://"+url.substring(8);
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
