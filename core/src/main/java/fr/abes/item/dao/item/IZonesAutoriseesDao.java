package fr.abes.item.dao.item;

import fr.abes.item.entities.item.ZonesAutorisees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IZonesAutoriseesDao extends JpaRepository<ZonesAutorisees, Integer> {
    @Query("select z.labelZone from ZonesAutorisees z join z.zonesTypesExemp t where t.numTypeExemp = :typeExemp")
    List<String> getZonesByTypeExemp(@Param("typeExemp") Integer typeExemp);

    @Query("select z.indicateurs from ZonesAutorisees z where z.labelZone = :labelZone")
    String getIndicateursByTypeExempAndLabelZone(@Param("labelZone") String labelZone);

}
