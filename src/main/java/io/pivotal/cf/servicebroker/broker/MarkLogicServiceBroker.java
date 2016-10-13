package io.pivotal.cf.servicebroker.broker;

import com.google.gson.Gson;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceBindingRepository;
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

    @Autowired
    private ServiceBindingRepository repo;

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

        String databaseCreate;
        String forestCreate;
        String contentDBExt = "-content";
        String modulesDBExt = "-modules";
        String forestHostNumber = "001";
        String forestNumber = "1";

        //Create content DB
        Map<String, Object> contentDb = new HashMap<>();
        databaseCreate = instance.getId() + contentDBExt;
        contentDb.put("database-name", databaseCreate);

        markLogicManageAPI.createDatabase(contentDb);

        //Store the contentDB Name
        instance.getParameters().put("contentDB", databaseCreate);

        //Create modules DB
        Map<String, Object> modulesDb = new HashMap<>();
        databaseCreate = instance.getId() + modulesDBExt;
        modulesDb.put("database-name", databaseCreate);

        markLogicManageAPI.createDatabase(modulesDb);

        //Store the modules DB Name
        instance.getParameters().put("modulesDB", databaseCreate);

        //Create content Forest in MarkLogic, attach to content DB
        Map<String, Object> contentDbForest = new HashMap<>();
        forestCreate = instance.getId() + contentDBExt + "-" + forestHostNumber + "-" + forestNumber;
        contentDbForest.put("forest-name", forestCreate);
        contentDbForest.put("host", env.getProperty("ML_CLUSTER_NAME"));
        contentDbForest.put("database", instance.getId() + contentDBExt);

        markLogicManageAPI.createForest(contentDbForest);

        //Store the content DB Forest Name
        instance.getParameters().put("contentForest", forestCreate);

        //Create modules Forest in MarkLogic, attach to content DB
        Map<String, Object> modulesDbForest = new HashMap<>();
        forestCreate = instance.getId() + modulesDBExt + "-" + forestHostNumber + "-" + forestNumber;
        modulesDbForest.put("forest-name", forestCreate);
        modulesDbForest.put("host", env.getProperty("ML_CLUSTER_NAME"));
        modulesDbForest.put("database", instance.getId() + modulesDBExt);

        markLogicManageAPI.createForest(modulesDbForest);

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
        String contentDatabaseDelete = instance.getId() + "-content";
        markLogicManageAPI.deleteDatabase(contentDatabaseDelete);

        //delete modules DB
        String modulesDatabaseDelete = instance.getId() + "-modules";
        markLogicManageAPI.deleteDatabase(modulesDatabaseDelete);

        //delete content Forest
        String contentForestDelete = instance.getId() + "-content-001-1";
        markLogicManageAPI.deleteForest(contentForestDelete);

        //delete modules Forest
        String modulesForestDelete = instance.getId() + "-modules-001-1";
        markLogicManageAPI.deleteForest(modulesForestDelete);

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
        //ml create app server, role, and user

        //create admin role
        Map<String, Object> adminSecRole = new HashMap<>();

        Gson adminSecRoleRolesGson = new Gson();
        String[] adminSecRoleRoles = { "rest-admin", "admin" };
        adminSecRoleRolesGson.toJson("adminSecRoleRoles");

        adminSecRole.put("role-name", instance.getId() + "-admin-role");
        adminSecRole.put("description", instance.getId() + " admin role");
        adminSecRole.put("role", adminSecRoleRoles );

        markLogicManageAPI.createRole(adminSecRole);

        binding.getParameters().putAll(adminSecRole);

        //create admin user
        Map<String, Object> adminSecUser = new HashMap<>();
        String adminPw = UUID.randomUUID().toString();

        Gson adminUserRolesGson = new Gson();
        String[] adminUserRoles = { instance.getId() + "-admin-role" };
        adminUserRolesGson.toJson("adminUserRoles");

        adminSecUser.put("user-name", instance.getId() + "-admin");
        adminSecUser.put("password", adminPw);
        adminSecRole.put("description", instance.getId() + " admin user");
        adminSecUser.put("role", adminUserRoles );

        markLogicManageAPI.createUser(adminSecUser);

        binding.getParameters().putAll(adminSecUser);

        //TODO Create guest role and user
        //create guest role
        Map<String, Object> guestSecRole = new HashMap<>();

        Gson guestSecRoleRolesGson = new Gson();
        //TODO update roles
        String[] guestSecRoleRoles = { "rest-reader" };
        guestSecRoleRolesGson.toJson("guestSecRoleRoles");

        guestSecRole.put("role-name", instance.getId() + "-guest-role");
        guestSecRole.put("description", instance.getId() + " guest role");
        guestSecRole.put("role", guestSecRoleRoles );

        markLogicManageAPI.createRole(guestSecRole);

        binding.getParameters().putAll(guestSecRole);

        //create guest user
        Map<String, Object> guestSecUser = new HashMap<>();
        String guestPw = UUID.randomUUID().toString();

        Gson guestUserRolesGson = new Gson();
        String[] guestUserRoles = { instance.getId() + "-guest-role" };
        guestUserRolesGson.toJson("guestUserRoles");

        guestSecUser.put("user-name", instance.getId() + "-guest");
        guestSecUser.put("password", guestPw);
        guestSecRole.put("description", instance.getId() + " guest user");
        guestSecUser.put("role", guestUserRoles );

        markLogicManageAPI.createUser(guestSecUser);

        binding.getParameters().putAll(guestSecUser);

        //create app server

        //TODO figure out the current highest app server port taking into account the env variable (if not found) and increment by one

        //TODO update to the /v1/rest-apis API

        Map<String, Object> appServer = new HashMap<>();
        appServer.put("server-name", instance.getId() + "-app");
        appServer.put("server-type","http");
        appServer.put("group-name","Default");
        appServer.put("root","/");
        appServer.put("port", env.getProperty("ML_APPSERVER_START_PORT"));
        appServer.put("default-user", instance.getId() + "-guest");
        appServer.put("content-database", instance.getId() + "-content");
        appServer.put("modules-database", instance.getId() + "-modules");
        appServer.put("log-errors", "true");
        appServer.put("default-error-format", "compatible");
        appServer.put("error-handler", "/error-handler.xqy");
        appServer.put("url-rewriter", "/rewriter.xml");
        appServer.put("rewrite-resolves-globally", "false");

        markLogicManageAPI.createAppServer(appServer);

        binding.getParameters().putAll(appServer);


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
        //ml delete user, role, app server

        //delete App Server
        String appServerDelete = instance.getId() + "-app";
        markLogicManageAPI.deleteAppServer(appServerDelete);

        //delete Admin User
        String adminUserDelete = instance.getId() + "-admin";
        markLogicManageAPI.deleteUser(adminUserDelete);

        //delete Admin Role
        String adminRoleDelete = instance.getId() + "-admin-role";
        markLogicManageAPI.deleteRole(adminRoleDelete);

        //delete Guest User
        String guestUserDelete = instance.getId() + "-guest";
        markLogicManageAPI.deleteUser(guestUserDelete);

        //delete Guest Role
        String guestRoleDelete = instance.getId() + "-guest-role";
        markLogicManageAPI.deleteRole(guestRoleDelete);

        //TODO Restart MarkLogic

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
        m.put("username", binding.getParameters().get("user-name"));
        m.put("password", binding.getParameters().get("password"));
        m.put("manageport", env.getProperty("ML_PORT"));
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