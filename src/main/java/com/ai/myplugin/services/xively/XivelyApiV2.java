package com.ai.myplugin.services.xively;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit.http.*;

/**
 * https://xively.com/dev/docs/api/
 */
public interface XivelyApiV2 {

//    page	Integer indicating which page of results you are requesting. Starts from 1.	https://api.xively.com/v2/products/
//    PRODUCT_ID/devices?page=2
//    per_page	Integer defining how many results to return per page (1 to 1000).	https://api.xively.com/v2/products/
//    PRODUCT_ID/devices?per_page=5
//    serial	Filter returned devices by serial, by passing either an exact or partial serial string	https://api.xively.com/v2/products/
//    PRODUCT_ID/devices?serial=1827b
//    activated	Possible values (‘true’, ‘false’, ‘all’). Whether to return serials that have been activated, not yet activated, or all of them. Omitting this parameter returns ‘all’.	https://api.xively.com/v2/products/
//    PRODUCT_ID/devices?activated=false

    static final String ACTIVATED_ALL = "all";
    static final String ACTIVATED_TRUE = "true";
    static final String ACTIVATED_FALSE = "false";

    /**
     * https://xively.com/dev/docs/api/product_management/devices/list_all_devices/
     */
    @GET("/products/{PRODUCT_ID}/devices")
    DeviceList listDevices(
            @Path("PRODUCT_ID") String productId,
            @Query("page") Long page,
            @Query("per_page") Long perPage,
            @Query("serial") String serial,
            @Query("activated") String activated);

    /**
     * https://xively.com/dev/docs/api/product_management/devices/create_device/
     * @param productId
     * @param serialNumber
     */
    @GET("/products/{PRODUCT_ID}/devices/{SERIAL_NUMBER}")
    DeviceResponse getDevice(
            @Path("PRODUCT_ID") String productId,
            @Path("SERIAL_NUMBER") String serialNumber);

    /**
     * https://xively.com/dev/docs/api/product_management/devices/create_device/
     * @param productId
     * @param devices
     */
    @POST("/products/{PRODUCT_ID}/devices")
    void createDevice(
            @Path("PRODUCT_ID") String productId,
            @Body Devices devices);

    @GET("/feeds.json")
    public JsonElement readFeeds();

    @GET("/feeds/{FEED_ID}")
    public JsonObject readFeed(
            @Path("FEED_ID") long feedId);

    @GET("/feeds/{FEED_ID}/datastreams/{DATASTREAM_ID}")
    public JsonObject readDatastream(
            @Path("FEED_ID") long feedId,
            @Path("DATASTREAM_ID") String datastreamId);


}
