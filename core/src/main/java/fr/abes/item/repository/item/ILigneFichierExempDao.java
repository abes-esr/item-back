package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.entities.item.LigneFichierExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@ItemConfiguration
public interface ILigneFichierExempDao extends JpaRepository<LigneFichierExemp, Integer> {
    /**
     * @param numDemande le numero de la demande d'exemplarisation
     * @return Une liste de ligne du fichier, ordonné par numéro de ligne croissant, sachant qu'il n'y a qu'une
     * ligne pour un fichier d'exemplarisation
     */
    @Query("select lf from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande order by lf.position")
    List<LigneFichierExemp> getLigneFichierbyDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numero de la demande d'exemplarisation
     * @param position le numero de ligne du fichier
     * @return Une ligne d'un fichier chargé par l'utilisateur pour l'exemplarisation, sachant qu'il n'y a
     * qu'une seule ligne de données d'exemplaires pour une demande d'exemplarisation
     */
    @Query("select lf from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.position = :position")
    LigneFichierExemp getLigneFichierbyDemandeEtPos(@Param("numDemande") Integer numDemande, @Param("position") Integer position);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande
     */
    @Query("select count(lf) from LigneFichierExemp  lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee=1")
    int getNbLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    @Query("select lf from LigneFichierExemp  lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee=1 order by lf.position")
    List<LigneFichierExemp> getLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return Une liste contenant toutes les lignes d'exemplaires correspondant à une demande
     *          qui n'ont pas été traités (= absence d'echec sur le traitement de ces lignes)
     */
    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee=1 order by lf.position")
    int getNbLigneFichierNonTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc à été
     * positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc = '"+ Constant.EXEMPLAIRE_CREE+"'")
    int getNbLigneFichierSuccessByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc n'a pas
     * été positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc != '"+Constant.EXEMPLAIRE_CREE+"'")
    int getNbLigneFichierErrorByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param demande la demande dont les lignes fichiers vont être supprimées
     */
    void deleteByDemandeExemp(DemandeExemp demande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de lignes total du fichier
     */
    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande")
    int getNbLigneFichierTotalByDemande(@Param("numDemande") Integer numDemande);

    @Query("select sum(lf.nbReponse) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande")
    int getNbNoticesTrouveesByDemande(@Param("numDemande") Integer numDemande);

    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.traitee = 1 and lf.nbReponse != 0")
    int getNbReponseTrouveesByDemande(@Param("numDemande") Integer numDemande);

    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.nbReponse=1")
    int getNbUneReponseByDemande(@Param("numDemande") Integer numDemande);

    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.nbReponse=0")
    int getNbZeroReponseByDemande(@Param("numDemande") Integer numDemande);

    @Query("select count(lf) from LigneFichierExemp lf where lf.demandeExemp.numDemande = :numDemande and lf.nbReponse>1")
    int getNbReponseMultipleByDemande(@Param("numDemande") Integer numDemande);

    /**Supprime les lignes de fichier d'exemplaire associées à une demande
     * @param numDemande numéro de la demande auquel on souhaite supprimer les lignes associées
     */
    @Transactional
    @Modifying
    @Query("delete from LigneFichierExemp lf where lf.demandeExemp in (select l from DemandeExemp l where l.numDemande = :numDemande)")
    void deleteLigneFichierExempByDemandeExempId(@Param("numDemande") Integer numDemande);
}
