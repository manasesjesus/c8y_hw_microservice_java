const request = require("request");

const credentials = $config.params.username + ":" + $config.params.password;
const ms_endpoint = $config.params.server + "/service/" + $config.params.microservice + "/health";

const headerOption = {
    "url" : ms_endpoint,
    "headers" : {
      "Authorization" : "Basic " + new Buffer(credentials).toString("base64"),
      "Accept" : "application/json"
    }
};

request(headerOption, function (error, response, body) {
    
    let status = JSON.parse(body).status;

    if (status === undefined || status !== "UP") {
        console.log("[ERROR]: The microservice is not up and running...");
        $export(null, { microservice : $config.params.microservice, healthy : false });
    }
    else {
        $export(null, { microservice : $config.params.microservice, healthy : true });
    }

});
