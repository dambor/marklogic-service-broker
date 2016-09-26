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
public class ServiceBindingRepositoryTest {

    @Autowired
    private ServiceBindingRepository repo;

    @Test
    public void testRepo() throws Exception {
        ServiceBinding sb = TestConfig.getServiceInstanceBinding();

        repo.save(sb);
        ServiceBinding sb2 = repo.findOne(sb.getId());
        assertNotNull(sb2);
        assertEquals(TestConfig.SB_ID, sb2.getId());
        Map<String, Object> parameters = sb2.getParameters();
        assertNotNull(parameters);
        assertEquals(TestConfig.PARAM1_VAL, parameters.get(TestConfig.PARAM1_NAME));

        Map<String, Object> credentials = sb2.getCredentials();
        assertNotNull(credentials);
        assertEquals(TestConfig.PARAM2_VAL, credentials.get(TestConfig.PARAM2_NAME));

        repo.delete(sb);
        assertNull(repo.findOne(sb.getId()));
    }
}