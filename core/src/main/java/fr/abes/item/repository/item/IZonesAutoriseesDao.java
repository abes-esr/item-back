package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.ZonesAutorisees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IZonesAutoriseesDao extends JpaRepository<ZonesAutorisees, Integer> {
    @Query("select z.labelZone from ZonesAutorisees z join z.zonesTypesExemp t where t.numTypeExemp = :typeExemp")
    List<String> getZonesByTypeExemp(@Param("typeExemp") Integer typeExemp);

    @Query("select z.indicateurs from ZonesAutorisees z where z.labelZone = :labelZone")
    String getIndicateursByTypeExempAndLabelZone(@Param("labelZone") String labelZone);

}
