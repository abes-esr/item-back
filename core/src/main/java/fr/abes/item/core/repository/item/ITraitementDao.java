package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.Traitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface ITraitementDao extends JpaRepository<Traitement, Integer> {
    /*Retourne le traitement spécifique à une demande*/
    @Query("select d.traitement.numTraitement from DemandeModif d where d.numDemande = :numDemande")
    Integer findTraitementByDemandeModifId(@Param("numDemande") Integer id);

    List<Traitement> findAllByOrderByNumTraitementAsc();
}
