package io.pivotal.cf.servicebroker.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceBindingRepository extends CrudRepository<ServiceBinding, String> {
}