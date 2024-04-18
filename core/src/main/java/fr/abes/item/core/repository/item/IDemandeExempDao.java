package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.DemandeExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Service permettant de travailler sur les demandes d'exemplarisation présentes en table DEMANDE_EXEMP
 * méthodes disponibles (CRUD)
 */
@Repository
@ItemConfiguration
public interface IDemandeExempDao extends JpaRepository<DemandeExemp, Integer> {
    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 5 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToProceed();

    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 5 and d.indexRecherche.code != 'DAT' order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToProceedWithoutDAT();

    @Query("select e.libelle from TypeExemp e where e.numTypeExemp in (select d.typeExemp from DemandeExemp d where d.numDemande = :numDemande)")
    String getTypeExemp(@Param("numDemande") Integer numDemande);

    @Query("select d from DemandeExemp d where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10)")
    List<DemandeExemp> getActiveDemandesExempForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query("select d from DemandeExemp d where d.iln = :iln and d.etatDemande.numEtat not in (9, 10)")
    List<DemandeExemp> getAllActiveDemandesExempForAdmin(@Param("iln") String iln);

    @Query("select d from DemandeExemp d where d.etatDemande.numEtat not in (9, 10)")
    List<DemandeExemp> getAllActiveDemandesExempForAdminExtended();

    @Query("select d from DemandeExemp d where d.iln = :iln and d.etatDemande.numEtat = 9")
    List<DemandeExemp> getAllArchivedDemandesExemp(@Param("iln") String iln);

    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 9")
    List<DemandeExemp> getAllArchivedDemandesExempExtended();

    //Même si l'ide signale la requête elle est correcte, demandes en statut terminé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 7 and d.dateModification < current_date - 90 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 9 and d.dateModification < current_date - 90 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 10 and d.dateModification < current_date - 210 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToDelete();
}
