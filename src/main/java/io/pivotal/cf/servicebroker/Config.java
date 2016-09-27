package io.pivotal.cf.servicebroker;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.pivotal.cf.servicebroker.broker.MarkLogicManageAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@Configuration
@Profile("cloud")
@EnableJpaRepositories("io.pivotal.cf.servicebroker.model")
public class Config {

    @Autowired
    private Environment env;

    @Bean
    public Cloud cloud() {
        return new CloudFactory().getCloud();
    }

    @Bean
    @ConfigurationProperties(DataSourceProperties.PREFIX)
    public DataSource dataSource() {
        return cloud().getSingletonServiceConnector(DataSource.class, null);
    }

    //TODO move to brokeredservice?
    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion("2.9");
    }

    @Bean
    public MarkLogicManageAPI markLogicManageAPI() {
        return Feign
                .builder()
                .encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(env.getProperty("ML_USER"), env.getProperty("ML_PW")))
                .target(MarkLogicManageAPI.class,
                        "http://" + env.getProperty("ML_HOST") + ":" + env.getProperty("ML_PORT"));
    }
}