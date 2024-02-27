package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.Traitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ItemConfiguration
public interface ITraitementDao extends JpaRepository<Traitement, Integer> {
    /*Retourne un traitement spécifié en paramètre dans la requête*/
    @Query("select t from Traitement t where t.nomMethode = :nomMethode")
    Traitement chercher(@Param("nomMethode") String nomMethode);

    /*Retourne le traitement spécifique à une demande*/
    @Query("select d.traitement.numTraitement from DemandeModif d where d.numDemande = :numDemande")
    Integer findTraitementByDemandeModifId(@Param("numDemande") Integer id);
}
