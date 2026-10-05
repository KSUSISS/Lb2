package ru.kafpin.lb2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lb2.entity.Schoolchild;

import java.util.List;

public interface SchoolchildRepository
        extends CrudRepository<Schoolchild, Long> {

    List<Schoolchild> findByLastNameContainsIgnoreCase(String lastName);

    List<Schoolchild> findBySchoolNameContainsIgnoreCase(String schoolName);

    List<Schoolchild> findBySchoolClassContainsIgnoreCase(String schoolClass);
}