package fr.abes.item.core.components;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Epntoppn {
    private final JdbcTemplate baseXmlJdbcTemplate;

    public Epntoppn(JdbcTemplate baseXmlJdbcTemplate) {
        this.baseXmlJdbcTemplate = baseXmlJdbcTemplate;
    }

    public String callFunction(String lesepns, String lercr) {
        return baseXmlJdbcTemplate.queryForObject("SELECT AUTORITES.EPNTOPPN_JSON('" + lesepns + "', '" + lercr + "') from DUAL", String.class);
    }
}
