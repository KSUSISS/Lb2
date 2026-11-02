package ru.kafpin.lb2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.kafpin.lb2.entity.Address;
import ru.kafpin.lb2.entity.School;
import ru.kafpin.lb2.entity.Schoolchild;

import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class SchoolchildRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AddressRepository addressRepository;

    public SchoolchildRepository(JdbcTemplate jdbcTemplate, AddressRepository addressRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.addressRepository = addressRepository;
    }

    // JOIN получает школьника, школу и адрес одним запросом.
    private static final String SELECT_CHILD = """
            SELECT c.*, s.name AS school_name,
                   a.postal_code, a.country, a.region, a.district,
                   a.city, a.street, a.house, a.apartment
            FROM schoolchild c
            JOIN school s ON s.id = c.school_id
            JOIN addresses a ON a.id = c.address_id
            """;

    private static final RowMapper<Schoolchild> CHILD_ROW_MAPPER = (rs, rowNum) -> {
        School school = new School();
        school.setId(rs.getLong("school_id"));
        school.setName(rs.getString("school_name"));

        Address address = new Address();
        address.setId(rs.getLong("address_id"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountry(rs.getString("country"));
        address.setRegion(rs.getString("region"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setApartment(rs.getString("apartment"));

        Schoolchild child = new Schoolchild();
        child.setId(rs.getLong("id"));
        child.setLastName(rs.getString("last_name"));
        child.setFirstName(rs.getString("first_name"));
        child.setMiddleName(rs.getString("middle_name"));
        child.setGender(rs.getString("gender"));
        child.setNationality(rs.getString("nationality"));
        child.setHeight(rs.getInt("height"));
        child.setWeight(rs.getInt("weight"));
        var birthDate = rs.getDate("birth_date");
        child.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
        child.setPhone(rs.getString("phone"));
        child.setSchoolClass(rs.getString("school_class"));
        child.setSchool(school);
        child.setAddress(address);
        return child;
    };

    public List<Schoolchild> findAll() {
        return jdbcTemplate.query(SELECT_CHILD + " ORDER BY c.id", CHILD_ROW_MAPPER);
    }

    public Optional<Schoolchild> findById(Long id) {
        return jdbcTemplate.query(SELECT_CHILD + " WHERE c.id = ?", CHILD_ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<Schoolchild> findByLastNameContainsIgnoreCase(String lastName) {
        return jdbcTemplate.query(SELECT_CHILD
                + " WHERE LOWER(c.last_name) LIKE ? ESCAPE '!' ORDER BY c.id",
                CHILD_ROW_MAPPER, containsPattern(lastName));
    }

    public List<Schoolchild> findBySchoolNameContainsIgnoreCase(String schoolName) {
        return jdbcTemplate.query(SELECT_CHILD
                + " WHERE LOWER(s.name) LIKE ? ESCAPE '!' ORDER BY c.id",
                CHILD_ROW_MAPPER, containsPattern(schoolName));
    }

    public List<Schoolchild> findBySchoolClassContainsIgnoreCase(String schoolClass) {
        return jdbcTemplate.query(SELECT_CHILD
                + " WHERE LOWER(c.school_class) LIKE ? ESCAPE '!' ORDER BY c.id",
                CHILD_ROW_MAPPER, containsPattern(schoolClass));
    }

    private static String containsPattern(String text) {
        return "%" + text.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Transactional
    public Schoolchild save(Schoolchild child) {
        // Сначала сохраняем адрес, чтобы получить его ID для школьника.
        Address address = addressRepository.save(child.getAddress());
        if (child.getId() == 0) {
            var keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement("""
                        INSERT INTO schoolchild
                            (last_name, first_name, middle_name, gender, nationality,
                             height, weight, birth_date, phone, school_class, school_id, address_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, child.getLastName());
                statement.setString(2, child.getFirstName());
                statement.setString(3, child.getMiddleName());
                statement.setString(4, child.getGender());
                statement.setString(5, child.getNationality());
                statement.setInt(6, child.getHeight());
                statement.setInt(7, child.getWeight());
                statement.setObject(8, child.getBirthDate());
                statement.setString(9, child.getPhone());
                statement.setString(10, child.getSchoolClass());
                statement.setLong(11, child.getSchool().getId());
                statement.setLong(12, address.getId());
                return statement;
            }, keyHolder);
            Number id = keyHolder.getKey();
            if (id == null) {
                throw new IllegalStateException("База данных не вернула ID нового школьника");
            }
            child.setId(id.longValue());
        } else {
            jdbcTemplate.update("""
                    UPDATE schoolchild SET last_name = ?, first_name = ?, middle_name = ?,
                        gender = ?, nationality = ?, height = ?, weight = ?, birth_date = ?,
                        phone = ?, school_class = ?, school_id = ?, address_id = ?
                    WHERE id = ?
                    """, child.getLastName(), child.getFirstName(), child.getMiddleName(),
                    child.getGender(), child.getNationality(), child.getHeight(), child.getWeight(),
                    child.getBirthDate(), child.getPhone(), child.getSchoolClass(),
                    child.getSchool().getId(), address.getId(), child.getId());
        }
        return child;
    }

    @Transactional
    public void delete(Schoolchild child) {
        // Сначала удаляем школьника, который ссылается на адрес, затем сам адрес.
        jdbcTemplate.update("DELETE FROM schoolchild WHERE id = ?", child.getId());
        addressRepository.deleteById(child.getAddress().getId());
    }

    @Transactional
    public void deleteAll() {
        for (Schoolchild child : findAll()) {
            delete(child);
        }
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schoolchild", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsById(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM schoolchild WHERE id = ?", Long.class, id);
        return count != null && count > 0;
    }
}