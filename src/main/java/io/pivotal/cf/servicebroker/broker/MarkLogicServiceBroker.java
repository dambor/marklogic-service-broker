package io.pivotal.cf.servicebroker.broker;

import com.google.gson.Gson;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceBindingRepository;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.model.ServiceInstanceRepository;
import io.pivotal.cf.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    private ServiceInstanceRepository instanceRepo;

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
        contentDb.put("uri-lexicon", "true");

        markLogicManageAPI.createDatabase(contentDb);

        //Store the contentDB Name
        instance.getParameters().put("contentDB", databaseCreate);

        //Create modules DB
        Map<String, Object> modulesDb = new HashMap<>();
        databaseCreate = instance.getId() + modulesDBExt;
        modulesDb.put("database-name", databaseCreate);
        contentDb.put("uri-lexicon", "true");

        markLogicManageAPI.createDatabase(modulesDb);

        //Store the modules DB Name
        instance.getParameters().put("modulesDB", databaseCreate);

        //TODO Track Forests similar to how it is done for the app server ports with forestHostNumber and forestNumber

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

        //Following lines moved from Create Binding
        //ml create app server, role, and user

        //TODO decide if Gson is really required for roles...

        //create guest role
        Map<String, Object> guestSecRole = new HashMap<>();

        Gson guestSecRoleRolesGson = new Gson();
        String[] guestSecRoleRoles = { "rest-reader" };
        guestSecRoleRolesGson.toJson("guestSecRoleRoles");

        guestSecRole.put("role-name", instance.getId() + "-role");
        guestSecRole.put("description", instance.getId() + " guest role");
        guestSecRole.put("role", guestSecRoleRoles );

        markLogicManageAPI.createRole(guestSecRole);

        instance.getParameters().putAll(guestSecRole);

        //create guest user
        Map<String, Object> guestSecUser = new HashMap<>();
        String guestPw = UUID.randomUUID().toString();

        Gson guestUserRolesGson = new Gson();
        String[] guestUserRoles = { instance.getId() + "-role" };
        guestUserRolesGson.toJson("guestUserRoles");

        guestSecUser.put("user-name", instance.getId() + "-guest");
        guestSecUser.put("password", guestPw);
        guestSecRole.put("description", instance.getId() + " guest user");
        guestSecUser.put("role", guestUserRoles );

        markLogicManageAPI.createUser(guestSecUser);

        instance.getParameters().putAll(guestSecUser);


        //create contributor role
        Map<String, Object> contributorSecRole = new HashMap<>();

        Gson contributorSecRoleRolesGson = new Gson();
        String[] contributorSecRoleRoles = { "rest-writer", instance.getId() + "-role" };
        contributorSecRoleRolesGson.toJson("contributorSecRoleRoles");

        contributorSecRole.put("role-name", instance.getId() + "-contributor-role");
        contributorSecRole.put("description", instance.getId() + " contributor role");
        contributorSecRole.put("role", contributorSecRoleRoles );

        markLogicManageAPI.createRole(contributorSecRole);

        instance.getParameters().putAll(contributorSecRole);

        //create contributor user
        Map<String, Object> contributorSecUser = new HashMap<>();
        String contributorPw = UUID.randomUUID().toString();

        Gson contributorUserRolesGson = new Gson();
        String[] contributorUserRoles = { instance.getId() + "-contributor-role" };
        contributorUserRolesGson.toJson("contributorUserRoles");

        contributorSecUser.put("user-name", instance.getId() + "-contributor");
        contributorSecUser.put("password", contributorPw);
        contributorSecRole.put("description", instance.getId() + " contributor user");
        contributorSecUser.put("role", contributorUserRoles );

        markLogicManageAPI.createUser(contributorSecUser);

        instance.getParameters().putAll(contributorSecUser);


        //create admin role
        Map<String, Object> adminSecRole = new HashMap<>();

        Gson adminSecRoleRolesGson = new Gson();
        String[] adminSecRoleRoles = { "admin", instance.getId() + "-contributor-role", "rest-admin" };
        adminSecRoleRolesGson.toJson("adminSecRoleRoles");

        adminSecRole.put("role-name", instance.getId() + "-admin-role");
        adminSecRole.put("description", instance.getId() + " admin role");
        adminSecRole.put("role", adminSecRoleRoles );

        markLogicManageAPI.createRole(adminSecRole);

        instance.getParameters().putAll(adminSecRole);

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

        instance.getParameters().putAll(adminSecUser);


        //determine the next available app server port to use for this app server
        Integer availableAppServerPort;

        ArrayList<Integer> currentUsedAppServerPorts = instanceRepo.findExistingAppServerPortsDesc();

        //if there are no recorded app server ports, use the start port specified in manifest.yml - 9000
        if (currentUsedAppServerPorts.isEmpty())
            availableAppServerPort = Integer.parseInt(env.getProperty("ML_APPSERVER_START_PORT"));
        else {

            //parse any open ports in the currentUsedAppServerPorts array
            Integer[] currentUsedAppServerPortsArray = currentUsedAppServerPorts.toArray(new Integer[0]);
            Arrays.sort(currentUsedAppServerPortsArray);

            ArrayList<Integer> openPorts = new ArrayList<>();

            int j = currentUsedAppServerPortsArray[0];
            for (int i=0;i<currentUsedAppServerPortsArray.length;i++)
            {
                if (j==currentUsedAppServerPortsArray[i])
                {
                    j++;
                }
                else
                {
                    openPorts.add(j);
                    i--;
                    j++;
                }
            }

            //if no open ports in the array, get the highest port and increase by one otherwise use the first available open port
            if (openPorts.isEmpty()) availableAppServerPort = instanceRepo.findGreatestAppServerPort() + 1;
            else availableAppServerPort = openPorts.get(0);

        }

        //create app server
        Map<String, Object> restServer = new HashMap<>();
        restServer.put("name", instance.getId() + "-app");
        restServer.put("group","Default");
        restServer.put("database", instance.getId() + "-content");
        restServer.put("modules-database", instance.getId() + "-modules");
        restServer.put("port", availableAppServerPort);
        restServer.put("xdbc-enabled","true");
        restServer.put("forests-per-host","3");
        restServer.put("error-format","json");

        Map<String, Object> restApi = new HashMap<>();
        restApi.put("rest-api",restServer);

        markLogicManageAPI.createRestServer(restApi);

        instance.setPortNumber(availableAppServerPort);

        instance.getParameters().putAll(restServer);

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

        //ml delete user, role, app server

        //delete Admin User
        String adminUserDelete = instance.getId() + "-admin";
        markLogicManageAPI.deleteUser(adminUserDelete);

        //delete Admin Role
        String adminRoleDelete = instance.getId() + "-admin-role";
        markLogicManageAPI.deleteRole(adminRoleDelete);

        //delete contributor User
        String contributorUserDelete = instance.getId() + "-contributor";
        markLogicManageAPI.deleteUser(contributorUserDelete);

        //delete contributor Role
        String contributorRoleDelete = instance.getId() + "-contributor-role";
        markLogicManageAPI.deleteRole(contributorRoleDelete);

        //delete Guest User
        String guestUserDelete = instance.getId() + "-guest";
        markLogicManageAPI.deleteUser(guestUserDelete);

        //delete Guest Role
        String guestRoleDelete = instance.getId() + "-guest-role";
        markLogicManageAPI.deleteRole(guestRoleDelete);

        //delete App Server
        String restServerDelete = instance.getId() + "-app";
        markLogicManageAPI.deleteRestServer(restServerDelete);

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

        //TODO: individual credentials for the different apps or anything else on a per app basis.

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

        //TODO: delete individual credentials for the different apps or anything else on a per app basis.

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

        Map<String, Object> m = new HashMap<>();

        m.put("host", env.getProperty("ML_HOST"));
        m.put("port", instance.getParameters().get("port"));
        m.put("username", instance.getParameters().get("user-name"));
        m.put("password", instance.getParameters().get("password"));
        m.put("manageport", env.getProperty("ML_PORT"));
        //Use the instance credentials
        //m.put("username", env.getProperty("ML_USER"));
        //m.put("password", env.getProperty("ML_PW"));
        m.put("database", instance.getParameters().get("database"));

        String uri = "marklogic://" + m.get("username") + ":" + m.get("password") + "@" + m.get("host") + ":" + m.get("port") + "/" + m.get("database");

        m.put("uri", uri);

        return m;
    }

    @Override
    //TODO deal with async
    public boolean isAsynch() {
        return false;
    }
}