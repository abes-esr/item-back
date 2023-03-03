package fr.abes.item.webstats;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NbDemandesTraiteesMapper implements RowMapper<NbDemandesTraiteesDto> {
    public NbDemandesTraiteesDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NbDemandesTraiteesDto nbDemandes = new NbDemandesTraiteesDto();
        nbDemandes.setRcr(rs.getString("RCR"));
        nbDemandes.setNbDemandesTraitees(rs.getInt("count"));
        return nbDemandes;
    }
}
