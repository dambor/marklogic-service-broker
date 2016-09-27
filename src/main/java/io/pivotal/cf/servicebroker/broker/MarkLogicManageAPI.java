package io.pivotal.cf.servicebroker.broker;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Repository
public interface MarkLogicManageAPI {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /manage/v2/databases")
    public String createDatabase(@RequestBody Map<String, Object> m);

    @Headers("Content-Type: application/json")
    @RequestLine("DELETE /manage/v2/databases/{databaseName}")
    public void deleteDatabase(@Param("databaseName") String query);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /manage/v2/forests")
    public String createForest(@RequestBody Map<String, Object> m);

    @Headers("Content-Type: application/json")
    @RequestLine("DELETE /manage/v2/forests/{forestName}?level=full")
    public void deleteForest(@Param("forestName") String query);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /manage/v2/roles")
    public String createRole(@RequestBody Map<String, Object> m);

    @Headers("Content-Type: application/json")
    @RequestLine("POST /manage/v2/users")
    public String createUser(@RequestBody Map<String, Object> m);
}