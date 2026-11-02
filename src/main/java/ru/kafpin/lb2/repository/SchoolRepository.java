package ru.kafpin.lb2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.kafpin.lb2.entity.School;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class SchoolRepository {

    private final JdbcTemplate jdbcTemplate;

    public SchoolRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Превращаем строку результата SQL в объект School.
    private static final RowMapper<School> SCHOOL_ROW_MAPPER = (rs, rowNum) -> {
        School school = new School();
        school.setId(rs.getLong("id"));
        school.setName(rs.getString("name"));
        return school;
    };

    public List<School> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM school ORDER BY id", SCHOOL_ROW_MAPPER);
    }

    public Optional<School> findById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM school WHERE id = ?",
                SCHOOL_ROW_MAPPER, id).stream().findFirst();
    }

    public School save(School school) {
        if (school.getId() == 0) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        "INSERT INTO school (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, school.getName());
                return statement;
            }, keyHolder);
            Number id = keyHolder.getKey();
            if (id == null) {
                throw new IllegalStateException("База данных не вернула ID новой школы");
            }
            school.setId(id.longValue());
        } else {
            jdbcTemplate.update("UPDATE school SET name = ? WHERE id = ?",
                    school.getName(), school.getId());
        }
        return school;
    }

    public boolean hasSchoolchildren(long schoolId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM schoolchild WHERE school_id = ?", Long.class, schoolId);
        return count != null && count > 0;
    }

    public void delete(School school) {
        jdbcTemplate.update("DELETE FROM school WHERE id = ?", school.getId());
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM school");
    }
}
