package fr.abes.item.dao.item;

import fr.abes.item.entities.item.SousZonesAutorisees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ISousZonesAutoriseesDao extends JpaRepository<SousZonesAutorisees, Integer> {
    @Query("select sz.libelle from SousZonesAutorisees sz join sz.zone z where z.labelZone = :zone")
    List<String> getSousZonesAutoriseesByZone(@Param("zone")String zone);

    @Query("select sz from SousZonesAutorisees sz join sz.zone z join z.zonesTypesExemp t where t.numTypeExemp in :typeExemp and sz.mandatory = true")
    List<SousZonesAutorisees> getSousZonesAutoriseesMandatory(@Param("typeExemp") Optional<Integer> typeExemp);
}
