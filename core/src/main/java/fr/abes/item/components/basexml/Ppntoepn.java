package fr.abes.item.components.basexml;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Component
public class Ppntoepn {
	
	@Autowired
	@Qualifier("baseXmlDataSource")
	private DataSource baseXmlDataSource;
	
	@Autowired
	@Qualifier("baseXmlJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public String callFunction(String lesppns, String lercr) {
		StringBuilder requete = new StringBuilder("SELECT AUTORITES.PPNTOEPN_JSON('").append(lesppns).append("', '").append(lercr).append("') from DUAL");
		return jdbcTemplate.queryForObject(requete.toString(), String.class);
	}
}
