package fr.abes.item.batch.webstats;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NbExemplairesSuppTraitesMapper implements RowMapper<NbExemplairesSuppTraitesDto> {
    @Override
    public NbExemplairesSuppTraitesDto mapRow(ResultSet resultSet, int i) throws SQLException {
        NbExemplairesSuppTraitesDto nbExemp = new NbExemplairesSuppTraitesDto();
        nbExemp.setRcr(resultSet.getString("RCR"));
        nbExemp.setNbExemplaires(resultSet.getInt("count"));
        return nbExemp;
    }
}
