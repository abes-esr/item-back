package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.DemandeSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IDemandeSuppDao extends JpaRepository<DemandeSupp, Integer> {

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) FROM DemandeSupp d LEFT JOIN d.ligneFichierSupps l WHERE d.etatDemande.numEtat NOT IN (9, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesSuppForAdminExtended();

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) FROM DemandeSupp d LEFT JOIN d.ligneFichierSupps l where d.iln = :iln and d.etatDemande.numEtat not in (9, 10) GROUP BY d")
    List<DemandeDto> getAllActiveDemandesSuppForAdmin(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) FROM DemandeSupp d LEFT JOIN d.ligneFichierSupps l where d.iln = :iln and d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesSupp(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, count(l)) from DemandeSupp d LEFT JOIN d.ligneFichierSupps l where d.iln = :iln and d.etatDemande.numEtat not in (9, 2, 10) GROUP BY d")
    List<DemandeDto> getActiveDemandesSuppForUserExceptedPreparedStatus(@Param("iln") String iln);

    @Query("select new fr.abes.item.core.dto.DemandeDto(d, COUNT(l)) FROM DemandeSupp d LEFT JOIN d.ligneFichierSupps l where d.etatDemande.numEtat = 9 GROUP BY d")
    List<DemandeDto> getAllArchivedDemandesSuppExtended();

    @Query("select d from DemandeSupp d join d.ligneFichierSupps l where d.etatDemande.numEtat = 5 group by d having count(l) > :limite order by d.dateModification")
    List<DemandeSupp> getDemandesEnAttenteGrosVolume(@Param("limite") int limite);

    @Query("select d from DemandeSupp d join d.ligneFichierSupps l where d.etatDemande.numEtat = 5 group by d having count(l) <= :limite order by d.dateModification")
    List<DemandeSupp> getDemandesEnAttentePetitVolume(@Param("limite") int limite);

    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 7 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToArchive();

    //Même si l'ide signale la requête elle est correcte, demandes en statut archivé avec une ancienneté de plus de 90 jours sur la dernière date de modification récupérées
    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 9 and (day(current_date) - day(d.dateModification)) > 90 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToPlaceInDeletedStatus();

    @Query("select d from DemandeSupp d where d.etatDemande.numEtat = 10 and (day(current_date) - day(d.dateModification)) > 210 order by d.dateModification asc")
    List<DemandeSupp> getNextDemandeToDelete();

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc à été
     * positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc = 'exemplaire supprimé'")
    int getNbLigneFichierSuccessByDemande(@Param("numDemande") Integer numDemande);

    boolean existsDemandeSuppByEtatDemande_Id(Integer etatDemande);
}
