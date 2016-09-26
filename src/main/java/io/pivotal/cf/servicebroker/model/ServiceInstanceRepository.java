package io.pivotal.cf.servicebroker.model;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceInstanceRepository extends PagingAndSortingRepository<ServiceInstance, String> {
}