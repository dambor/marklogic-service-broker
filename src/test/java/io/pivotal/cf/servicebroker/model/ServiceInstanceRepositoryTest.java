package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
}