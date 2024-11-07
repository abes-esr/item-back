package fr.abes.item.core.repository.item;

import fr.abes.item.core.configuration.ItemConfiguration;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichierSupp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Dao permettant de travailler sur les lignes du fichier que l'utilisateur à charger pour précéder à une
 * demande de suppression d'une sous-zone d'une zone se rattachant à un ou plusieurs exemplaires existants
 */
@Repository
@ItemConfiguration
public interface ILigneFichierSuppDao extends JpaRepository<LigneFichierSupp, Integer> {

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return l'ensemble des lignes du fichier chargé par l'utilisateur rattaché à une demande de suppression
     */
    @Query("select lf from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande order by lf.position")
    List<LigneFichierSupp> getLigneFichierbyDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @param position le numéro de la ligne (qui représente un exemplaire) du fichier chargé par l'utilisateur
     *                 0 étant le premier exemplaire
     * @return Une ligne complète du fichier chargé, qui correspond à un exemplaire
     */
    @Query("select lf from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.position = :position")
    LigneFichierSupp getLigneFichierbyDemandeEtPos(@Param("numDemande") Integer numDemande, @Param("position") Integer position);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return Une liste contenant toutes les lignes d'exemplaires correspondant à une demande
     *          qui ont été traités (= absence d'echec sur le traitement de ces lignes)
     */
    @Query("select lf from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee = 1 order by lf.position")
    List<LigneFichierSupp> getLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande qui va aller filtrer les retours sur l'attribut REF_DEMANDE
     * @return Une liste contenant toutes les lignes d'exemplaires correspondant à une demande
     *          qui n'ont pas été traités (= absence d'echec sur le traitement de ces lignes)
     */
    @Query("select count(lf) from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee=0")
    int getNbLigneFichierNonTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param demande le numéro de la demande dont les lignes fichiers vont être supprimées
     */
    void deleteByDemandeSupp(DemandeSupp demande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande
     */
    @Query("select count(lf) from LigneFichierSupp  lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee=1")
    int getNbLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc à été
     * positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc = 'exemplaire supprimé'")
    int getNbLigneFichierSuccessByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc n'a pas
     * été positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc != 'exemplaire supprimé'")
    int getNbLigneFichierErrorByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande de suppression
     * @return Le nombre de lignes total du fichier
     */
    @Query("select count(lf) from LigneFichierSupp lf where lf.demandeSupp.numDemande = :numDemande")
    int getNbLigneFichierTotal(@Param("numDemande") Integer numDemande);

}
