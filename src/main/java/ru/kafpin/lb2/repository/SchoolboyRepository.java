package ru.kafpin.lb2.repository;
import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lb2.domain.Schoolboy;

public interface SchoolboyRepository extends CrudRepository<Schoolboy, Long> {
}
