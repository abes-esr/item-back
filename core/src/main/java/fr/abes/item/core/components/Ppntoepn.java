package fr.abes.item.core.components;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class Ppntoepn {
	private final JdbcTemplate baseXmlJdbcTemplate;

	public Ppntoepn(@Qualifier("baseXmlJdbcTemplate") JdbcTemplate baseXmlJdbcTemplate) {
		this.baseXmlJdbcTemplate = baseXmlJdbcTemplate;
	}


	public String callFunction(String lesppns, String lercr) {
		return baseXmlJdbcTemplate.queryForObject("SELECT AUTORITES.PPNTOEPN_JSON('" + lesppns + "', '" + lercr + "') from DUAL", String.class);
	}
}
