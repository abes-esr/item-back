package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.JournalDemandeModif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ItemConfiguration
public interface IJournalDemandeModifDao extends JpaRepository<JournalDemandeModif, Integer> {
    @Query("delete from JournalDemandeModif where demandeModif.numDemande = :demandeModif")
    void deleteAllLinesJournalDemandeModifByDemandeId(@Param("demandeModif") Integer demandeModif);
}
