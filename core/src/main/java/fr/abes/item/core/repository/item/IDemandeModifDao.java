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
    @Query(value = "select d, (SELECT count(num_lignefichier) FROM ligne_fichier_modif WHERE ref_demande = demande_modif.num_demande) as nb_lignefichier from DemandeModif d where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10)", nativeQuery = true)
    List<DemandeDto> getActiveDemandesModifForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query(value = "select d, (SELECT count(num_lignefichier) FROM ligne_fichier_modif WHERE ref_demande = demande_modif.num_demande) as nb_lignefichier from DemandeModif d where d.iln = :iln and d.etatDemande.numEtat not in (9, 10)", nativeQuery = true)
    List<DemandeDto> getAllActiveDemandesModifForAdmin(@Param("iln") String iln);

    @Query(value = "select d, (SELECT count(num_lignefichier) FROM ligne_fichier_modif WHERE ref_demande = demande_modif.num_demande) as nb_lignefichier from DemandeModif d where d.etatDemande.numEtat not in (9, 2, 10)", nativeQuery = true)
    List<DemandeDto> getAllActiveDemandesModifForAdminExtended();

    @Query(value = "select d, (SELECT count(num_lignefichier) FROM ligne_fichier_modif WHERE ref_demande = demande_modif.num_demande) as nb_lignefichier from DemandeModif d where d.iln = :iln and d.etatDemande.numEtat = 9", nativeQuery = true)
    List<DemandeDto> getAllArchivedDemandesModif(@Param("iln") String iln);

    @Query(value = "select d, (SELECT count(num_lignefichier) FROM ligne_fichier_modif WHERE ref_demande = demande_modif.num_demande) as nb_lignefichier from DemandeModif d where d.etatDemande.numEtat = 9", nativeQuery = true)
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
}
