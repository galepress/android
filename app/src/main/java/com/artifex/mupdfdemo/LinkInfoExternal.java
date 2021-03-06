package com.artifex.mupdfdemo;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class LinkInfoExternal extends LinkInfo {
	 public String url;
	public String sourceUrl;

    public static final int COMPONENT_TYPE_ID_VIDEO 		=1;
    public static final int COMPONENT_TYPE_ID_SES 			=2;
    public static final int COMPONENT_TYPE_ID_HARİTA 		=3;
    public static final int COMPONENT_TYPE_ID_WEBLINK       =4;
    public static final int COMPONENT_TYPE_ID_WEB			=5;
    public static final int COMPONENT_TYPE_ID_TOOLTIP		=6;
    public static final int COMPONENT_TYPE_ID_SCROLLER		=7;
    public static final int COMPONENT_TYPE_ID_SLIDESHOW		=8;
    public static final int COMPONENT_TYPE_ID_360			=9;
    public static final int COMPONENT_TYPE_ID_BOOKMARK		=10;
    public static final int COMPONENT_TYPE_ID_ANIMATION		=11;

    public static final int  MAP_TYPE_STANDART = 0;
    public static final int  MAP_TYPE_HYBRID = 1;
    public static final int  MAP_TYPE_SATELLITE = 2;

    public int componentAnnotationTypeId = -1;
    public boolean isModal = false;
    public boolean isInternal = true;
    public int webViewId = -1;
    public Location location;
    public float zoom;
    public int mapType = 0;
    public boolean isMailto = false;
    public boolean isSuitabale = true;

	public LinkInfoExternal(float l, float t, float r, float b, String u) {
		super(l, t, r, b);
		url = u;
        Uri uri = Uri.parse(url);
        Log.e("urlllllllllll", ""+url);

        if(uri.isHierarchical()) {

            String modalQueryParameterValue = uri.getQueryParameter("modal");
            if(modalQueryParameterValue!=null && !modalQueryParameterValue.isEmpty()){
                int modalValue = Integer.parseInt(modalQueryParameterValue);
                if(modalValue == 1){
                    isModal = true;
                }
                removeQueryParameter("modal", modalQueryParameterValue);
            }

            String componentTypeQueryParameterValue = uri.getQueryParameter("componentTypeID");
            if(componentTypeQueryParameterValue!=null && !componentTypeQueryParameterValue.isEmpty()){
                componentAnnotationTypeId = Integer.parseInt(componentTypeQueryParameterValue);
                removeQueryParameter("componentTypeID", componentTypeQueryParameterValue);
                isSuitabale = true;
            } else {
                if(url.contains("www") || url.contains("http://")) {
                    componentAnnotationTypeId = COMPONENT_TYPE_ID_WEBLINK;
                } else if(url.contains("@")){
                    componentAnnotationTypeId = COMPONENT_TYPE_ID_WEBLINK;
                    isMailto = true;
                } else {
                    isSuitabale = false;
                    return;
                }
            }


            if(componentAnnotationTypeId == COMPONENT_TYPE_ID_HARİTA){
                uri = Uri.parse(url);
                location = new Location("");

                Double lat = 41.0053215;
                if(uri.getQueryParameter("lat") != null && !uri.getQueryParameter("lat").isEmpty())
                    lat = new Double(uri.getQueryParameter("lat"));

                Double lon = 29.0121795;
                if(uri.getQueryParameter("lon") != null && !uri.getQueryParameter("lon").isEmpty())
                    lon = new Double(uri.getQueryParameter("lon"));
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
            else if(isWebAnnotation()){
                if(url.length() == 8){ // Eger servisten bos url gelmisse "ylweb://"
                    isInternal = false;
                    sourceUrl = "";
                } else {
                    try{
                        if (url.substring(0,17).equals("ylweb://localhost")){
                            isInternal = true;
                            sourceUrl = url.substring(18);
                        }
                        else{
                            isInternal = false;
                            sourceUrl = "http://"+url.substring(8);
                        }
                    } catch (Exception e){ //Url hatalı
                        isInternal = false;
                        sourceUrl = "";
                    }

                }

            }

        } else {
            if(url.startsWith("mailto:")) {
                isMailto = true;
                componentAnnotationTypeId = COMPONENT_TYPE_ID_WEBLINK;
            } else {
                isSuitabale = false;
            }
        }

	}

    public boolean isWebAnnotation(){
        switch (componentAnnotationTypeId){
            case COMPONENT_TYPE_ID_HARİTA:
                return false;
            case COMPONENT_TYPE_ID_WEBLINK:
                return false;
            default:
                return true;
        }
    }

    private void removeQueryParameter(String paramName, String paramValue) {
        String temp = paramName+"="+paramValue;
        if(url.contains(temp)){
            if(url.contains("?"+temp+"&")){
                url = url.replace("?"+temp+"&", "?");
            }
            else if(url.contains("&"+temp+"&")){
                url = url.replace("&"+temp+"&", "&");
            }
            else if(url.contains("?"+temp)){
                url = url.replace("?"+temp, "");
            }
            else if(url.contains("&"+temp)){
                url = url.replace("&"+temp, "");
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

    public boolean mustHorizontalScrollLock() {
        if(
                componentAnnotationTypeId == COMPONENT_TYPE_ID_HARİTA ||
                componentAnnotationTypeId == COMPONENT_TYPE_ID_360 ||
                componentAnnotationTypeId == COMPONENT_TYPE_ID_SLIDESHOW ||
                componentAnnotationTypeId == COMPONENT_TYPE_ID_VIDEO

                ){
            return false;
        }
        return true;
    }
}
