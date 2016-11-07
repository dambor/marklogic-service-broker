package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
public class ServiceInstanceRepositoryTest {

    @Autowired
    private ServiceInstanceRepository repo;

    @Test
    public void testRepo() throws Exception {
        ServiceInstance si = TestConfig.getServiceInstance();

        repo.save(si);
        ServiceInstance si2 = repo.findOne(si.getId());
        assertNotNull(si2);
        assertEquals(TestConfig.SI_ID, si2.getId());
        Map<String, Object> parameters = si2.getParameters();
        assertNotNull(parameters);
        assertEquals(TestConfig.PARAM1_VAL, parameters.get(TestConfig.PARAM1_NAME));

        repo.delete(si);
        assertNull(repo.findOne(si.getId()));
    }

    @Test
    public void testMaxAppServerPort() throws Exception {

        Integer i = repo.findGreatestAppServerPort();

        assertNull(i);

        CreateServiceInstanceRequest serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz123");

        ServiceInstance si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9001);
        repo.save(si);


        serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz124");

        si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9003);
        repo.save(si);


        serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz125");

        si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9000);
        repo.save(si);


        i = repo.findGreatestAppServerPort();

        assertEquals(9003, i.intValue());

    }

    @Test
    public void testAppServerPortManagement() throws Exception {

        Integer greatestAppServerPort = repo.findGreatestAppServerPort();

        assertNull(greatestAppServerPort);

        CreateServiceInstanceRequest serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz123");

        ServiceInstance si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9001);
        repo.save(si);


        serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz124");

        si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9004);
        repo.save(si);


        serviceInstanceRequest = TestConfig.getCreateServiceInstanceRequest("xyz125");

        si = TestConfig.getServiceInstance(serviceInstanceRequest);

        si.setPortNumber(9002);
        repo.save(si);


        Integer availableAppServerPort;

        ArrayList<Integer> currentUsedAppServerPorts = repo.findExistingAppServerPortsDesc();


        //if there are no recorded app server ports, use the start port specified in manifest.yml - 9000
        if (currentUsedAppServerPorts.isEmpty())
            availableAppServerPort = Integer.parseInt("9000");
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
            if (openPorts.isEmpty()) availableAppServerPort = repo.findGreatestAppServerPort() + 1;
            else availableAppServerPort = openPorts.get(0);

        }

        //assertEquals(Integer.valueOf(9002), availableAppServerPort);
        //assertNotNull(currentUsedAppServerPortsArray[0]);
        //assertEquals(Integer.valueOf(9000), currentUsedAppServerPortsArray[0] );

        assertEquals(Integer.valueOf(9004), availableAppServerPort);

    }

}