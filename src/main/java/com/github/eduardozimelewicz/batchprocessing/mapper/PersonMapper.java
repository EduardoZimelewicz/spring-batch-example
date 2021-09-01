package com.github.eduardozimelewicz.batchprocessing.mapper;

import com.github.eduardozimelewicz.batchprocessing.entity.Person;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonMapper implements RowMapper<Person> {

    public static final String ID_COLUMN = "person_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person person = new Person();

        person.setPersonId(rs.getInt(ID_COLUMN));
        person.setFirstName(rs.getString(FIRST_NAME));
        person.setLastName(rs.getString(LAST_NAME));

        return person;
    }
}
