package io.pivotal.cf.servicebroker.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ServiceInstanceRepository extends PagingAndSortingRepository<ServiceInstance, String> {

    @Query("select max(s.appServerPort) from ServiceInstance s")
    Integer findGreatestAppServerPort();

    @Query("SELECT s.appServerPort FROM ServiceInstance s ORDER BY s.appServerPort DESC")
    ArrayList<Integer> findExistingAppServerPortsDesc();

}