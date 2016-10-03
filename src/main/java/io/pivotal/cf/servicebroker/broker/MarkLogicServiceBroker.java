package io.pivotal.cf.servicebroker.broker;

import com.google.gson.Gson;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example service broker. Can be used as a template for creating custom service brokers
 * by adding your code in the appropriate methods. For more information on the CF service broker
 * lifecycle and API, please see See <a href="https://docs.cloudfoundry.org/services/api.html">here.</a>
 * <p>
 * This class extends DefaultServiceImpl, which has no-op implementations of the methods. This means
 * that if, for instance, your broker does not support binding you can just delete the binding methods below
 * (in other words, you do not need to implement your own no-op implementations).
 */
@Service
@Slf4j
public class MarkLogicServiceBroker extends DefaultServiceImpl {

    @Autowired
    private MarkLogicManageAPI markLogicManageAPI;

    @Autowired
    private Environment env;

    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     * @throws ServiceBrokerException thrown this for any errors during instance creation.
     */
    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
        //ml create content, modules dbs and respective forests

        //Map for storing values
        Map<String, Object> m = new HashMap<>();

        String databaseCreate;
        String forestCreate;
        String contentDBExt = "-content";
        String modulesDBExt = "-modules";
        String forestHostNumber = "001";
        String forestNumber = "1";

        //Create content DB
        databaseCreate = instance.getId() + contentDBExt;
        m.put("database-name", databaseCreate);

        markLogicManageAPI.createDatabase(m);

        //Store the contentDB Name
        instance.getParameters().put("contentDB", databaseCreate);

        m.clear();

        //Create modules DB
        databaseCreate = instance.getId() + modulesDBExt;
        m.put("database-name", databaseCreate);

        markLogicManageAPI.createDatabase(m);

        //Store the modules DB Name
        instance.getParameters().put("modulesDB", databaseCreate);

        m.clear();

        //Create content Forest in MarkLogic, attach to content DB
        forestCreate = instance.getId() + contentDBExt + "-" + forestHostNumber + "-" + forestNumber;
        m.put("forest-name", forestCreate);
        m.put("host", env.getProperty("ML_CLUSTER_NAME"));
        m.put("database", instance.getId() + contentDBExt);

        markLogicManageAPI.createForest(m);

        //Store the content DB Forest Name
        instance.getParameters().put("contentForest", forestCreate);

        m.clear();

        //Create modules Forest in MarkLogic, attach to content DB
        forestCreate = instance.getId() + modulesDBExt + "-" + forestHostNumber + "-" + forestNumber;
        m.put("forest-name", forestCreate);
        m.put("host", env.getProperty("ML_CLUSTER_NAME"));
        m.put("database", instance.getId() + modulesDBExt);

        markLogicManageAPI.createForest(m);

        //Store the content DB Forest Name
        instance.getParameters().put("modulesForest", forestCreate);

    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion.
     */
    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
        //ml db clean up and destroy db and forests

        //delete content DB
        String databaseDelete = instance.getId() + "-content";
        markLogicManageAPI.deleteDatabase(databaseDelete);

        //delete modules DB
        databaseDelete = instance.getId() + "-modules";
        markLogicManageAPI.deleteDatabase(databaseDelete);

        //delete content Forest
        String forestDelete = instance.getId() + "-content-001-1";
        markLogicManageAPI.deleteForest(forestDelete);

        //delete modules Forest
        forestDelete = instance.getId() + "-modules-001-1";
        markLogicManageAPI.deleteForest(forestDelete);

    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion. Services that do not support
     *                                updating can through ServiceInstanceUpdateNotSupportedException here.
     */
    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
        //TODO add more forests, nodes, indexes.....
    }

    /**
     * Called during the bind-service process. This is a good time to set up anything on your underlying service specifically
     * needed by an application, such as user accounts, rights and permissions, application-specific environments and connections, etc.
     * <p>
     * Services that do not support binding should set '"bindable": false,' within their catalog.json file. In this case this method
     * can be safely deleted in your implementation.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the bind-service request, which will show up as key value pairs in binding.parameters. Brokers
     *                 can, as part of this method, store any information needed for credentials and unbinding operations as key/value
     *                 pairs in binding.properties
     * @throws ServiceBrokerException thrown this for any errors during binding creation.
     */
    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO create the binding via API... Create App Servers, Roles, Users with those roles and passwords.

        Map<String, Object> m = new HashMap<>();

        //create app server
        //TODO figure out a way to manage the app server ports
        m.put("server-name", instance.getId() + "-app");
        m.put("server-type","http");
        m.put("group-name","Default");
        m.put("root","/");
        m.put("port","6000");
        m.put("content-database", instance.getId() + "-content");
        m.put("modules-database", instance.getId() + "-modules");
        m.put("log-errors", "true");
        m.put("default-error-format", "compatible");
        m.put("error-handler", "/error-handler.xqy");
        m.put("url-rewriter", "/rewriter.xml");
        m.put("rewrite-resolves-globally", "false");

        markLogicManageAPI.createAppServer(m);

        //Save App Server Parameters
        //binding.getParameters().putAll(m);

        m.clear();

        //create admin role
        //TODO add description and add this role to: rest-admin, admin
        m.put("role-name", instance.getId() + "-admin-role");

        markLogicManageAPI.createRole(m);

        binding.getParameters().putAll(m);

        m.clear();

        //create admin user
        String pw = UUID.randomUUID().toString();

        Gson gson = new Gson();
        String[] roleValues = { instance.getId() + "-admin-role" };
        gson.toJson("roleValues");

        m.put("user-name", instance.getId() + "-admin");
        m.put("password", pw);
        m.put("description", instance.getId() + " admin user");
        m.put("role", roleValues );

        markLogicManageAPI.createUser(m);

        binding.getParameters().putAll(m);

        m.clear();

    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during the unbinding creation.
     */
    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO call API to delete stuff

        //delete Admin User
        String userDelete = instance.getId() + "-admin";
        markLogicManageAPI.deleteUser(userDelete);

        //delete Admin Role
        String roleDelete = instance.getId() + "-admin-role";
        markLogicManageAPI.deleteRole(roleDelete);

        //delete App Server
        String appServerDelete = instance.getId() + "-app";
        markLogicManageAPI.deleteAppServer(appServerDelete);

    }

    /**
     * Bind credentials that will be returned as the result of a create-binding process. The format and values of these credentials will
     * depend on the nature of the underlying service. For more information and some examples, see
     * <a href=https://docs.cloudfoundry.org/services/binding-credentials.html>here.</a>
     * <p>
     * This method is called after the create-binding method: any information stored in binding.properties in the createBinding call
     * will be availble here, along with any custom data passed in as json parameters as part of the create-binding process by the client).
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @return credentials, as a series of key/value pairs
     * @throws ServiceBrokerException thrown this for any errors during credential creation.
     */
    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO Put together the VCAP_Services-type variables that are needed. Maybe use the java connection library later.

        Map<String, Object> m = new HashMap<>();

        m.put("host", env.getProperty("ML_HOST"));
        m.put("port", binding.getParameters().get("port"));
        //TODO need to pull back the username and password values- there will be 3 sets of them (admin, contrib, guest)
        m.put("username", binding.getParameters().get("user-name"));
        m.put("password", binding.getParameters().get("password"));
        //Use the instance credentials
        //m.put("username", env.getProperty("ML_USER"));
        //m.put("password", env.getProperty("ML_PW"));
        m.put("database", binding.getParameters().get("content-database"));

        String uri = "marklogic://" + m.get("username") + ":" + m.get("password") + "@" + m.get("host") + ":" + m.get("port") + "/" + m.get("database");

        m.put("uri", uri);

        //maybe something like this? Are the host/port etcs same as we get from the application.props file?
        //or do they come from the backend service somehow?
//        m.put("host", host);
//        m.put("port", port);
//        m.put("database", clusterName);
//
//        String uri = "marklogic://" + m.get("username") + ":" + m.get("password") + "@" + m.get("host") + ":" + m.get("port") + "/" + m.get("database");
//
//        m.put("uri", uri);

        return m;
    }

    @Override
    //TODO deal with async
    public boolean isAsynch() {
        return false;
    }
}