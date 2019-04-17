const request = require("request");

const credentials = $config.params.username + ":" + $config.params.password;
const ms_endpoint = $config.params.server + "/service/" + $config.params.microservice + "/health";

const options = {
    "url" : ms_endpoint,
    "headers" : {
      "Authorization" : "Basic " + Buffer.from(credentials).toString("base64"),
      "Accept" : "application/json"
    }
};

request(options, function (error, response, body) {
    
    let status = JSON.parse(body).status;

    if (status === undefined || status !== "UP") {
        $export(null, { microservice : $config.params.microservice, healthy : false });
    }
    else {
        $export(null, { microservice : $config.params.microservice, healthy : true });
    }

});
