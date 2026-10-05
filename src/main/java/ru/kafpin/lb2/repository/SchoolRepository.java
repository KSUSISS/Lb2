package ru.kafpin.lb2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lb2.entity.School;

public interface SchoolRepository
        extends CrudRepository<School, Long> {
}