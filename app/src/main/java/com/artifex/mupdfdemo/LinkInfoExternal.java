package com.artifex.mupdfdemo;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class LinkInfoExternal extends LinkInfo {
	 public String url;
	public String sourceUrl;

    public static final int  ANNOTATION_TYPE_PAGELINK = 0;
    public static final int  ANNOTATION_TYPE_WEBLINK = 1;
    public static final int  ANNOTATION_TYPE_WEB = 2;
    public static final int  ANNOTATION_TYPE_MAP = 3;

    public static final int  MAP_TYPE_STANDART = 0;
    public static final int  MAP_TYPE_HYBRID = 1;
    public static final int  MAP_TYPE_SATELLITE = 2;

    public int annotationType = -1;
    public boolean isModal = false;
    public boolean isInternal = true;
    public int webViewId = -1;
    public Location location;
    public float zoom;
    public int mapType = 0;

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
            location = new Location("");
            Double lat = new Double(uri.getQueryParameter("lat"));
            Double lon = new Double(uri.getQueryParameter("lon"));
            location.setLatitude(lat);
            location.setLongitude(lon);
            Double zoomValue = new Double(uri.getQueryParameter("slon"));
            float zoomlevel = 12 - (int)(zoomValue / new Double("0.01"));
            zoom = (zoomlevel / 2) + 12;
            if(url.contains("standard")){
                mapType = MAP_TYPE_STANDART;
            }
            else if(url.contains("hybrid")){
                mapType = MAP_TYPE_HYBRID;
            }
            else if(url.contains("satellite")){
                mapType = MAP_TYPE_SATELLITE;
            }
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
