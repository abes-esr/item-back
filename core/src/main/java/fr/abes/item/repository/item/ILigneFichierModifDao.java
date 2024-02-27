package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.LigneFichierModif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Dao permettant de travailler sur les lignes du fichier que l'utilisateur à charger pour précéder à une
 * demande de modification d'une sous-zone d'une zone se rattachant à un ou plusieurs exemplaires existants
 */
@Repository
@ItemConfiguration
public interface ILigneFichierModifDao extends JpaRepository<LigneFichierModif, Integer> {

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return l'ensemble des lignes du fichier chargé par l'utilisateur rattaché à une demande de modification
     */
    @Query("select lf from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande order by lf.position")
    List<LigneFichierModif> getLigneFichierbyDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @param position le numéro de la ligne (qui représente un exemplaire) du fichier chargé par l'utilisateur
     *                 0 étant le premier exemplaire
     * @return Une ligne complète du fichier chargé, qui correspond à un exemplaire
     */
    @Query("select lf from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande and lf.position = :position")
    LigneFichierModif getLigneFichierbyDemandeEtPos(@Param("numDemande") Integer numDemande, @Param("position") Integer position);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return Une liste contenant toutes les lignes d'exemplaires correspondant à une demande
     *          qui ont été traités (= absence d'echec sur le traitement de ces lignes)
     */
    @Query("select lf from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande and lf.traitee = 1 order by lf.position")
    List<LigneFichierModif> getLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return Une liste contenant toutes les lignes d'exemplaires correspondant à une demande
     *          qui n'ont pas été traités (= absence d'echec sur le traitement de ces lignes)
     */
    @Query("select count(lf) from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande and lf.traitee=0")
    int getNbLigneFichierNonTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param demande le numéro de la demande dont les lignes fichiers vont être supprimées
     */
    void deleteByDemandeModif(DemandeModif demande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande
     */
    @Query("select count(lf) from LigneFichierModif  lf where lf.demandeModif.numDemande = :numDemande and lf.traitee=1")
    int getNbLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc à été
     * positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc = 'Le traitement a été effectué.'")
    int getNbLigneFichierSuccessByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc n'a pas
     * été positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande and traitee=1 and retourSudoc != 'Le traitement a été effectué.'")
    int getNbLigneFichierErrorByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande de modification
     * @return Le nombre de lignes total du fichier
     */
    @Query("select count(lf) from LigneFichierModif lf where lf.demandeModif.numDemande = :numDemande")
    int getNbLigneFichierTotal(@Param("numDemande") Integer numDemande);

    /**Supprime les lignes de fichier de modification associées à une demande
     * @param numDemande numéro de la demande auquel on souhaite supprimer les lignes associées
     */
    @Transactional
    @Modifying
    @Query("delete from LigneFichierModif lf where lf.demandeModif in (select l from DemandeModif l where l.numDemande = :numDemande)")
    void deleteLigneFichierModifByDemandeExempId(@Param("numDemande") Integer numDemande);
}
