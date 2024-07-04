package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.EtatDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Permet de faire des opérations sur les demandes en fonction de leur état, l'état représentant l'avancement
 * du traitement de la demande.
 */
@Repository
@ItemConfiguration
public interface IEtatDemandeDao extends JpaRepository<EtatDemande, Integer> {
    /**
     * @return La liste des états que peux avoir une demande (table ETAT_DEMANDE), excepté l'état préparé
     */
    @Query("select e from EtatDemande e where e.numEtat != 2 order by e.numEtat")
    List<EtatDemande> findAllForDisplay();
}
