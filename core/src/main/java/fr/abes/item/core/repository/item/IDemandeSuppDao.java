package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.DemandeSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IDemandeSuppDao extends JpaRepository<DemandeSupp, Integer> {
    @Query("select d from DemandeSupp d where d.etatDemande.numEtat not in (9, 10)")
    List<DemandeSupp> getAllActiveDemandesSuppForAdminExtended();
    @Query("select d from DemandeSupp d where d.iln = :iln and d.etatDemande.numEtat not in (9, 10)")
    List<DemandeSupp> getAllActiveDemandesSuppForAdmin(@Param("iln") String iln);
    @Query("select d from DemandeSupp d where d.iln = :iln and d.etatDemande.numEtat = 9")
    List<DemandeSupp> getAllArchivedDemandesSupp(@Param("iln") String iln);
    @Query("select d from DemandeSupp d where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10)")

    List<DemandeSupp> getActiveDemandesSuppForUserExceptedPreparedStatus(@Param("iln") String iln);
    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 9")
    List<DemandeSupp> getAllArchivedDemandesSuppExtended();
    List<DemandeSupp> findDemandeSuppsByEtatDemande_IdOrderByDateModificationAsc(Integer id);

    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 7 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 9 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 10 and (day(current_date) - day(d.dateModification)) > 210 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToDelete();
}
