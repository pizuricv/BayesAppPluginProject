// take first runtime from global, that way you don't need to define a node that has raw data.
// if you have more than one node that produces geo data, you should not do this
var lat1 = options.rawData.GLOBAL["latitude"];
var lon1 = options.rawData.GLOBAL["longitude"];
if(options.requiredProperties.node){
    var nodeName = options.requiredProperties.node;
    lat1 = options.rawData[nodeName].latitude;
    lon1 = options.rawData[nodeName].longitude; 
} 

if(options.requiredProperties.distance && lat1 && lon1) {
    var radlat1 = Math.PI * lat1/180;
    var radlat2 = Math.PI * options.requiredProperties.latitude/180;
    var radlon1 = Math.PI * lon1/180;
    var radlon2 = Math.PI * options.requiredProperties.longitude/180;
    var theta = lon1-options.requiredProperties.longitude;
    
    var radtheta = Math.PI * theta/180;

    var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
    dist = Math.acos(dist);
    dist = dist * 180/Math.PI;
    dist = dist * 60 * 1.1515;
    dist = dist * 1.609344;
    var value = {
        observedState: dist > options.requiredProperties.distance ? "Out" : "In",
        rawData: {  
            distance: dist, 
            distanceConfigured: options.requiredProperties.distance,
            latitude: lat1, 
            longitude: lon1,  
            latitudeConfigured:  options.requiredProperties.latitude, 
            longitudeConfigured: options.requiredProperties.longitude
            
        }
    };
    send(null, value);
}else{
  send(new Error("Missing properties"));
}

