package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.DemandeModif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Service permettant de retourner une liste de Demande de modification, variant selon des critères.
 */
@Repository
@ItemConfiguration
public interface IDemandeModifDao extends JpaRepository<DemandeModif, Integer> {
    /**
     * @param iln l'établissement auquel appartient la bibliothèque, une bibliothèque pouvant appartenir à
     *            plusieurs établissements
     * @return les demandes appartenant à l'iln de l'utilisateur (un iln comprenant plusieurs rcr)
     *            et qui sont ni dans l'état préparé, ni dans l'état archivé
     */
    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) FROM DemandeModif d JOIN d.ligneFichierModifs l where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10) GROUP BY d")
    List<DemandeDto> getActiveDemandesModifForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeModif d JOIN d.ligneFichierModifs l where d.iln = :iln and d.etatDemande.numEtat not in (9, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesModifForAdmin(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeModif d JOIN d.ligneFichierModifs l where d.etatDemande.numEtat not in (9, 2, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesModifForAdminExtended();

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeModif d JOIN d.ligneFichierModifs l where d.iln = :iln and d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesModif(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeModif d JOIN d.ligneFichierModifs l where d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesModifExtended();

    @Query("select d from DemandeModif d where d.etatDemande.numEtat = 5 order by d.dateModification asc")
    List<DemandeModif> getNextDemandeToProceed();

    @Query("select d from DemandeModif d where d.etatDemande.numEtat = 10 order by d.dateModification asc")
    List<DemandeModif> getListDemandesToClean();

    //Même si l'ide signale la requête elle est correcte, demandes en statut terminé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeModif d where d.etatDemande.numEtat = 7 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeModif> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeModif d where d.etatDemande.numEtat = 9 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeModif> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeModif d where d.etatDemande.numEtat = 10 and (day(current_date) - day(d.dateModification)) > 210 order by d.dateModification asc")
    List<DemandeModif> getNextDemandeToDelete();

    boolean existsDemandeModifByEtatDemande_Id(Integer etatDemande);

}
