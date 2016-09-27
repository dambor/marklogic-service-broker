package io.pivotal.cf.servicebroker;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.pivotal.cf.servicebroker.broker.MarkLogicManageAPI;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.CatalogService;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestConfig {

    public static final String SD_ID = "7381b5a5-d4e6-43b1-beac-0304b46c009d";
    public static final String SI_ID = "anID";
    public static final String SB_ID = "12345";
    public static final String PARAM1_NAME = "foo";
    public static final String PARAM1_VAL = "bar";
    public static final String PARAM2_NAME = "bizz";
    public static final String PARAM2_VAL = "bazz";

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private Environment env;

//    @Mock
//    private MarkLogicManageAPI markLogicManageAPI;

    @Bean
    public MarkLogicManageAPI markLogicManageAPI() {
        return Feign
                .builder()
                .encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(env.getProperty("ML_USER"), env.getProperty("ML_PW")))
                .target(MarkLogicManageAPI.class,
                        "http://" + env.getProperty("ML_HOST") + ":" + env.getProperty("ML_PORT"));
    }

    public static String getContents(String fileName) throws Exception {
        URI u = new ClassPathResource(fileName).getURI();
        return new String(Files.readAllBytes(Paths.get(u)));
    }

    private static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PARAM1_NAME, PARAM1_VAL);

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                SD_ID, "pId", "orgId", "spaceId", parameters);
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest(
            ServiceDefinition sd, boolean includeParms) {

        Map<String, Object> parms = new HashMap<>();

        CreateServiceInstanceRequest req = new CreateServiceInstanceRequest(
                sd.getId(), sd.getPlans().get(0).getId(), "testOrgId",
                "testSpaceId", parms);
        req.withServiceInstanceId(SI_ID);
        return req;
    }

    public static ServiceInstance getServiceInstance() {
        return new ServiceInstance(getCreateServiceInstanceRequest());
    }

    private static CreateServiceInstanceBindingRequest getCreateBindingRequest() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PARAM1_NAME, PARAM1_VAL);
        CreateServiceInstanceBindingRequest req = new CreateServiceInstanceBindingRequest("aSdId", "aPlanId", "anAppGuid", null, parameters);
        req.withBindingId(SB_ID);
        return req;
    }

    public static ServiceBinding getServiceInstanceBinding() {
        ServiceBinding sb = new ServiceBinding(getCreateBindingRequest());
        Map<String, Object> creds = new HashMap<>();
        creds.put(PARAM2_NAME, PARAM2_VAL);
        sb.setCredentials(creds);
        return sb;
    }
}