package io.pivotal.cf.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "service_binding")
public class ServiceBinding implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    @Id
    private String id;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceId;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("app_guid")
    private String appGuid;

    @JsonSerialize
    @JsonProperty("bind_resource")
    @Convert(converter = MapConverter.class)
    private Map<String, Object> bindResource = new HashMap<>();

    @JsonSerialize
    @JsonProperty("parameters")
    @Column(length = 1000)
    @Convert(converter = MapConverter.class)
    private Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("credentials")
    @Column(length = 500)
    @Convert(converter = MapConverter.class)
    private Map<String, Object> credentials = new HashMap<>();

    // added to manage MarkLogic app server ports
    @JsonSerialize
    @JsonProperty("app_server_port")
    private int appServerPort;

    public ServiceBinding() {
        super();
    }

    //TODO deal with stuff in response bodies
    public ServiceBinding(CreateServiceInstanceBindingRequest request) {
        this();
        this.id = request.getBindingId();
        this.serviceId = request.getServiceDefinitionId();
        this.planId = request.getPlanId();
        this.appGuid = request.getBoundAppGuid();
        this.bindResource = request.getBindResource();
        if (request.getParameters() != null) {
            getParameters().putAll(request.getParameters());
        }
        //added to save the app port value
        this.appServerPort = Integer.valueOf((String) request.getParameters().get("port"));
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setCredentials(Map<String, Object> creds) {
        this.credentials = creds;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public CreateServiceInstanceAppBindingResponse getCreateResponse() {
        CreateServiceInstanceAppBindingResponse resp = new CreateServiceInstanceAppBindingResponse();
        resp.withCredentials(credentials);
        return resp;
    }
}