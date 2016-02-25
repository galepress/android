package ak.detaysoft.galepress.custom_models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by p1025 on 21.09.2015.
 */
public class Subscription {

    private String identifier;
    private boolean isActive;
    private int type;
    private String price;
    private boolean isOwned;
    private String marketPrice;

    public final static int WEEK = 0;
    public final static int MONTH = 1;
    public final static int YEAR = 2;


    public Subscription(int type, String identifier, String price, String marketPrice, boolean isActive, boolean isOwned){
        this.identifier = identifier;
        this.isActive = isActive;
        this.type = type;
        this.price = price;
        this.isOwned = isOwned;
        this.marketPrice = marketPrice;
    }

    public Subscription(JSONObject object){
        try {
            this.identifier = object.getString("identifier");
            this.isActive = object.getBoolean("isActive");
            this.type = object.getInt("type");
            this.price = object.getString("price");
            this.isOwned = object.getBoolean("isOwned");
            this.marketPrice = object.getString("marketPrice");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("identifier", identifier);
            obj.put("isActive", isActive);
            obj.put("type", type);
            obj.put("price", price);
            obj.put("isOwned", isOwned);
            obj.put("marketPrice", marketPrice);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIdentifier() {

        return identifier;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public void setOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public boolean isOwned() {

        return isOwned;
    }

    public void setMarketPrice(String marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getMarketPrice() {

        return marketPrice;
    }
}
