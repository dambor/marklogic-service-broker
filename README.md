# marklogic-service-broker
The beginning of a MarkLogic Cloud Foundry Service Broker that provisions MarkLogic running elsewhere.

NOTE: The MarkLogic Manage App Server (Port 8002) must be set to basic authentication for now. The [MarkLogic Documentation](https://docs.marklogic.com/guide/admin/http#id_67435) will direct you to where Application Server authentication is configured in MarkLogic.

##Using marklogic-service-broker
1. The sample-broker requires a mysql datastore for now to manage the ML service credentials.

  ```bash
  cf create-service p-mysql 100mb MLCreds
  ```
2. Edit the [manifest.yml](https://github.com/djdman2000/marklogic-service-broker/blob/master/manifest.yml) file as needed for your MarkLogic environment. Specifically, lines 11-17 where 16-17 are the ports the service broker may use for MarkLogic services provisioned by the broker.
3. The broker makes use of spring-security to protect itself against unauthorized meddling. To set its password edit line 1 of the [application.properties file](https://github.com/djdman2000/marklogic-service-broker/blob/master/src/main/resources/application.properties) (you probably don't want to check this in!)
4. Ensure MarkLogic is running locally. Update the [application.properties](https://github.com/djdman2000/marklogic-service-broker/blob/master/src/test/resources/application.properties) such that it points to your local MarkLogic install.
5. Build the broker:

  ```bash
  cd marklogic-service-broker
  mvn clean install
  ```
6. Push the broker to cf:

  ```bash
  cf push

  ...

  Showing health and status for app marklogic-service-broker-app in org marklogic / space dev as admin...
  OK

  requested state: started
  instances: 1/1
  usage: 512M x 1 instances
  urls: marklogic-service-broker-app.brokerUrl.com
  last uploaded: Tue Dec 20 03:09:38 UTC 2016
  stack: cflinuxfs2
  buildpack: java_buildpack_offline

       state     since                    cpu    memory           disk           details
  #0   running   2016-12-19 10:10:41 PM   0.0%   257.2M of 512M   166.7M of 1G

  ```

8. Register the broker using the url returned in the previous step:

  ```bash
  cf create-service-broker marklogic user the_password_from_application_dot_properties_whose_default_value_is_changeme https://marklogic-service-broker-app.brokerUrl.com
  ```
9. See the broker:

  ```bash
  cf service-brokers
  Getting service brokers as admin...

  name                          url
  ...
  marklogic                     https://marklogic-service-broker-app.brokerUrl.com
  ...

  cf service-access
  Getting service access as admin...
  ...
  broker: marklogic
     service     plan     access   orgs
     marklogic   shared   none
  ...

  cf enable-service-access marklogic
  Enabling access to all plans of service marklogic for all orgs as admin...

  cf service-access
  Getting service access as admin...
  ...
  broker: marklogic
     service     plan     access   orgs
     marklogic   shared   all
  ...

  cf marketplace
  Getting services from marketplace in org your-org / space your-space as you...
  OK

  service               plans                      description
  ...
  marklogic             shared                     A MarkLogic service broker implementation
  ...
  ```
10. Create an instance:

  ```bash
  cf create-service marklogic shared marklogic-service
  ```
11. Look at the broker logs:

  ```bash
  cf logs marklogic-service-broker-app --recent
  Creating service instance marklogic-service in org your-org / space your-space as admin...
  OK
  ...
  2016-12-19T22:26:46.83-0500 [APP/0]      OUT 2016-12-20 03:26:46 [http-nio-8080-exec-6] INFO  i.p.c.s.service.InstanceService - creating service instance: 0dc43d82....
  ...
  ```
12. Bind an app to the service and check the logs again:

  ```bash
  cf bind-service myApp marklogic-service
  Binding service marklogic-service to app myApp in org your-org / space your-space as admin...
  OK
  ...
  2016-12-19T22:29:31.98-0500 [APP/0]      OUT 2016-12-20 03:29:31 [http-nio-8080-exec-2] INFO  i.p.c.s.service.BindingService - creating binding for service instance: 0dc43d82....
  ...
  ```
13. Restage the service you bound to and look at its env: you should see the MarkLogic credentials in the VCAP_SERVICES:

  ```bash
  cf restage myApp
  cf env myApp
  ...
  ```
  ```json
    {
     "VCAP_SERVICES": {
      "marklogic": [
       {
        "credentials": {
         "database": "0dc43d82-3173-4b15-8405-ae94c31ddf3f-content",
         "host": "somehost.com",
         "manageport": "8002",
         "password": "11deb35f-c39e-4357-8f7e-723a5a73aed4",
         "port": 8020,
         "uri": "marklogic://0dc43d82-3173-4b15-8405-ae94c31ddf3f-admin:11deb35f-c39e-4357-8f7e-723a5a73aed4@somehost.com:8020/0dc43d82-3173-4b15-8405-ae94c31ddf3f-content",
         "username": "0dc43d82-3173-4b15-8405-ae94c31ddf3f-admin"
        },
        "label": "marklogic",
        "name": "marklogic-service",
        "plan": "shared",
        "provider": null,
        "syslog_drain_url": null,
        "tags": [
         "MarkLogic"
        ],
        "volume_mounts": []
       }
      ]
     }
    }
  ```
14. Unbind your app from the service and look at the logs:

  ```bash
  cf unbind-service myApp marklogic-service
  Unbinding app myApp from service marklogic-service in org your-org / space your-space as admin...
  OK
  ...
  2016-12-19T22:34:08.72-0500 [APP/0]      OUT 2016-12-20 03:34:08 [http-nio-8080-exec-2] INFO  i.p.c.s.service.BindingService - deleting binding for service instance: 0dc43d82...
  ...
  ```
15. Delete the service (use -f to force deletion without confirmation) and look at the logs:

  ```bash
  cf delete-service marklogic-service

  Really delete the service cookingshow?> y
  Deleting service cookingshow in org your-org / space your-space as admin...
  OK

  ...
  2016-12-19T22:38:06.78-0500 [APP/0]      OUT 2016-12-20 03:38:06 [http-nio-8080-exec-1] INFO  i.p.c.s.service.InstanceService - deleting service instance from repo: 0dc43d82...
  ...
  ```

## To Delete the Service Broker, the Service Broker App and the credentials datastore

1. Unregister and delete the broker:

  ```bash
  cf delete-service-broker marklogic
  cf delete marklogic-cf-service-broker-app
  ```
2. Delete the mysql datastore which manages the ML service credentials:

  ```bash
  cf delete-service MLCreds
  ```

Congrats! You have now fully run the MarkLogic Service Broker end to end. Please file Issues for any additional items you would like to see in this project.