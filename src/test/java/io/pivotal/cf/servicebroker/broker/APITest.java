package io.pivotal.cf.servicebroker.broker;

import io.pivotal.cf.servicebroker.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
@SpringApplicationConfiguration(classes = {Application.class})
public class APITest {

    @Autowired
    private Environment env;

    @Autowired
    private MarkLogicManageAPI api;

    @Test
    public void testAPI() throws Exception {

        Map<String, Object> m = new HashMap<>();
        m.put("database-name", "testId" + "-content");

        api.createDatabase(m);

    }

}