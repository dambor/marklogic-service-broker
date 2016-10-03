package io.pivotal.cf.servicebroker.broker;

import com.google.gson.Gson;
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
    private MarkLogicManageAPI markLogicManageAPI;

    @Test
    public void testCreateDatabase() throws Exception {

        Map<String, Object> m = new HashMap<>();
        m.put("database-name", "testId" + "-content");

        markLogicManageAPI.createDatabase(m);

    }

//    @Test
//    public void testCreateUser() throws Exception {
//
//        Map<String, Object> m = new HashMap<>();
//
//        String pw = UUID.randomUUID().toString();
//
//        Gson gson = new Gson();
//        String[] roleValues = { "cookingshow-admin-role" };
//        gson.toJson("roleValues");
//
//        //create admin user
//        m.put("user-name", "cookingshowpcf" + "-admin");
//        m.put("password", pw);
//        m.put("description", "cookingshowpcf" + " admin user");
//        m.put("role", roleValues );
//
//        markLogicManageAPI.createUser(m);
//
//    }

}