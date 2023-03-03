package fr.abes.item.webstats;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NbExemplairesTraitesMapper implements RowMapper<NbExemplairesTraitesDto> {
    @Override
    public NbExemplairesTraitesDto mapRow(ResultSet resultSet, int i) throws SQLException {
        NbExemplairesTraitesDto nbExemp = new NbExemplairesTraitesDto();
        nbExemp.setRcr(resultSet.getString("RCR"));
        nbExemp.setTypeTraitement(resultSet.getInt("DEM_TRAIT_ID"));
        nbExemp.setNbExemplaires(resultSet.getInt("count"));
        return nbExemp;
    }
}
