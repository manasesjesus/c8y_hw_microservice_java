var request = require("request");

var credentials  = $config.params.username + ":" + $config.params.password;
var headerOption = {
    "url" : $config.params.health_endpoint,
    "headers" : {
      "Authorization" : "Basic " + new Buffer(credentials).toString("base64"),
      "Accept" : "application/json"
    }
};

request(headerOption, function (error, response, body) {
    
    let status = JSON.parse(body).status;

    if (status === undefined || status !== "UP") {
        console.log("[ERROR]: The microservice is not up and running...");
        $export(null, { healthy : false });
    }
    else {
        $export(null, { healthy : true });
    }

});
