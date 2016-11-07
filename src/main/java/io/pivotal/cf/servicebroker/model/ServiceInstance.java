package io.pivotal.cf.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.cloud.servicebroker.model.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "service_instance")
public class ServiceInstance implements Serializable {

    public static final long serialVersionUID = 1L;

    @JsonSerialize
    @JsonProperty("id")
    @Id
    private String id;

    @JsonSerialize
    @JsonProperty("organization_guid")
    private String organizationGuid;

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceId;

    @JsonSerialize
    @JsonProperty("space_guid")
    private String spaceGuid;

    @JsonSerialize
    @JsonProperty("parameters")
    @Column(length = 1000)
    @Convert(converter = MapConverter.class)
    private Map<String, Object> parameters = new HashMap<>();

    @JsonSerialize
    @JsonProperty("accepts_incomplete")
    private boolean acceptsIncomplete;

    // added to manage MarkLogic app server ports
    @JsonSerialize
    @JsonProperty("app_server_port")
    private Integer appServerPort;

    public ServiceInstance() {
        super();
    }

    //TODO deal with stuff in response bodies
    public ServiceInstance(CreateServiceInstanceRequest request) {
        this();
        this.id = request.getServiceInstanceId();
        this.organizationGuid = request.getOrganizationGuid();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
        this.spaceGuid = request.getSpaceGuid();

        if (request.getParameters() != null) {
            parameters.putAll(request.getParameters());
        }
    }

    public String getId() {
        return id;
    }

    public Integer getPortNumber() {
        return appServerPort;
    }

    public void setPortNumber(Integer i) {
        appServerPort = i;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public ServiceInstance(DeleteServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
        this.serviceId = request.getServiceDefinitionId();
    }

    public ServiceInstance(UpdateServiceInstanceRequest request) {
        this.id = request.getServiceInstanceId();
        this.planId = request.getPlanId();
    }

    public CreateServiceInstanceResponse getCreateResponse() {
        CreateServiceInstanceResponse resp = new CreateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        if (parameters.containsKey("dashboard_url")) {
            resp.withDashboardUrl(parameters.get("dashboard_url").toString());
        }
        return resp;
    }

    public DeleteServiceInstanceResponse getDeleteResponse() {
        DeleteServiceInstanceResponse resp = new DeleteServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        return resp;
    }

    public UpdateServiceInstanceResponse getUpdateResponse() {
        UpdateServiceInstanceResponse resp = new UpdateServiceInstanceResponse();
        resp.withAsync(this.acceptsIncomplete);
        return resp;
    }
}