package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.TypeExemp;
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

    @Query("select e from TypeExemp e where e.numTypeExemp in (select d.typeExemp.numTypeExemp from DemandeExemp d where d.numDemande = :numDemande)")
    TypeExemp getTypeExemp(@Param("numDemande") Integer numDemande);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeExemp d JOIN d.ligneFichierExemps l where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10) GROUP BY d")
    List<DemandeDto> getActiveDemandesExempForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeExemp d JOIN d.ligneFichierExemps l where d.iln = :iln and d.etatDemande.numEtat not in (9, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesExempForAdmin(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeExemp d JOIN d.ligneFichierExemps l where d.etatDemande.numEtat not in (9, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesExempForAdminExtended();

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeExemp d JOIN d.ligneFichierExemps l where d.iln = :iln and d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesExemp(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) from DemandeExemp d JOIN d.ligneFichierExemps l where d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesExempExtended();

    //Même si l'ide signale la requête elle est correcte, demandes en statut terminé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 7 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 9 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeExemp d where d.etatDemande.numEtat = 10 and (day(current_date) - day(d.dateModification)) > 210 order by d.dateModification asc")
    List<DemandeExemp> getNextDemandeToDelete();

    boolean existsDemandeExempByEtatDemande_Id(Integer etatDemande);

}
