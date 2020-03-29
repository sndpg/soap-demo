package org.psc.soap.soapdemo.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.psc.soap.soapdemo.configuration.WebServiceConfiguration;
import org.psc.soap.soapdemo.schema.GetStatusRequest;
import org.psc.soap.soapdemo.schema.GetStatusResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.annotation.PostConstruct;

@Slf4j
@Endpoint
public class WorkerEndpoint {

    @PostConstruct
    public void postConstruct() {
        log.info("WorkerEndpoint constructed");
    }

    @ResponsePayload
    @PayloadRoot(namespace = WebServiceConfiguration.TARGET_NAMESPACE, localPart = "getStatusRequest")
    public GetStatusResponse getStatus(@RequestPayload GetStatusRequest request) {
        log.info("getStatus called");
        if (request != null) {
            request.getKeyValue().forEach(keyValue -> log.info("{}: {}", keyValue.getKey(), keyValue.getValue()));
        }
        GetStatusResponse getStatusResponse = new GetStatusResponse();
        getStatusResponse.setStatus("OK");
        return getStatusResponse;
    }

}
