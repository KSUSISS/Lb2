package ru.kafpin.lb2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.kafpin.lb2.entity.Address;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepository {

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Одна строка таблицы addresses превращается в объект Address.
    private static final RowMapper<Address> ADDRESS_ROW_MAPPER = (rs, rowNum) -> {
        Address address = new Address();
        address.setId(rs.getLong("id"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountry(rs.getString("country"));
        address.setRegion(rs.getString("region"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setApartment(rs.getString("apartment"));
        return address;
    };

    public List<Address> findAll() {
        return jdbcTemplate.query("SELECT * FROM addresses ORDER BY id", ADDRESS_ROW_MAPPER);
    }

    public Optional<Address> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM addresses WHERE id = ?",
                ADDRESS_ROW_MAPPER, id).stream().findFirst();
    }

    public Address save(Address address) {
        if (address.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement("""
                        INSERT INTO addresses
                            (postal_code, country, region, district, city, street, house, apartment)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, address.getPostalCode());
                statement.setString(2, address.getCountry());
                statement.setString(3, address.getRegion());
                statement.setString(4, address.getDistrict());
                statement.setString(5, address.getCity());
                statement.setString(6, address.getStreet());
                statement.setString(7, address.getHouse());
                statement.setString(8, address.getApartment());
                return statement;
            }, keyHolder);
            Number id = keyHolder.getKey();
            if (id == null) {
                throw new IllegalStateException("База данных не вернула ID нового адреса");
            }
            address.setId(id.longValue());
        } else {
            jdbcTemplate.update("""
                    UPDATE addresses SET postal_code = ?, country = ?, region = ?, district = ?,
                        city = ?, street = ?, house = ?, apartment = ?
                    WHERE id = ?
                    """, address.getPostalCode(), address.getCountry(), address.getRegion(),
                    address.getDistrict(), address.getCity(), address.getStreet(),
                    address.getHouse(), address.getApartment(), address.getId());
        }
        return address;
    }

    public boolean existsById(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM addresses WHERE id = ?", Long.class, id);
        return count != null && count > 0;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM addresses", Long.class);
        return count == null ? 0 : count;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM addresses WHERE id = ?", id);
    }

    public void delete(Address address) {
        deleteById(address.getId());
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM addresses");
    }
}