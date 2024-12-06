package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.JournalDemandeRecouv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IJournalDemandeRecouvDao extends JpaRepository<JournalDemandeRecouv, Integer> {
    List<JournalDemandeRecouv> findAllByDemandeRecouv_NumDemandeOrderByDateEntreeDesc(Integer numDemande);
}
