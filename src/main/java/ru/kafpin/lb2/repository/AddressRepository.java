package ru.kafpin.lb2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lb2.entity.Address;

public interface AddressRepository
        extends CrudRepository<Address, Long> {
}