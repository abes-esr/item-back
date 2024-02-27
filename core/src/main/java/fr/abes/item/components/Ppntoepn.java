package fr.abes.item.components;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class Ppntoepn {
	private final JdbcTemplate baseXmlJdbcTemplate;

	public Ppntoepn(JdbcTemplate baseXmlJdbcTemplate) {
		this.baseXmlJdbcTemplate = baseXmlJdbcTemplate;
	}


	public String callFunction(String lesppns, String lercr) {
		return baseXmlJdbcTemplate.queryForObject("SELECT AUTORITES.PPNTOEPN_JSON('" + lesppns + "', '" + lercr + "') from DUAL", String.class);
	}
}
