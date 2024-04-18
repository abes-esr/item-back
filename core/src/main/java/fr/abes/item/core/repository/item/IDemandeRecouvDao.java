package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.DemandeRecouv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IDemandeRecouvDao extends JpaRepository<DemandeRecouv, Integer> {
    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 5 order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToProceed();

    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 10 order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToClean();

    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 5 and d.indexRecherche.code != 'DAT' order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToProceedWithoutDAT();

    @Query("select d from DemandeRecouv d where d.iln = :iln and d.etatDemande.numEtat not in (9, 10)")
    List<DemandeRecouv> getActiveDemandesRecouvForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query("select d from DemandeRecouv d where d.iln = :iln and d.etatDemande.numEtat not in (9, 10)")
    List<DemandeRecouv> getAllActiveDemandesRecouvForAdmin(@Param("iln") String iln);
    
    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat not in (9, 10)")
    List<DemandeRecouv> getAllActiveDemandesRecouvForAdminExtended();

    @Query("select d from DemandeRecouv d where d.iln = :iln and d.etatDemande.numEtat = 9")
    List<DemandeRecouv> getAllArchivedDemandesRecouv(@Param("iln") String iln);

    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 9")
    List<DemandeRecouv> getAllArchivedDemandesRecouvExtended();

    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 10 order by d.dateModification asc")
    List<DemandeRecouv> getListDemandesToClean();

    //Même si l'ide signale la requête elle est correcte, demandes en statut terminé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 7 and d.dateModification < current_date - 90 order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 9 and d.dateModification < current_date - 90 order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeRecouv d where d.etatDemande.numEtat = 10 and d.dateModification < current_date - 210 order by d.dateModification asc")
    List<DemandeRecouv> getNextDemandeToDelete();
}
