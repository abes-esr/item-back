package fr.abes.item.dao.item;

import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.LigneFichierRecouv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILigneFichierRecouvDao extends JpaRepository<LigneFichierRecouv, Integer> {
    /**
     * @param numDemande le numero de la demande de recouvrement
     * @return Une liste de ligne du fichier, ordonné par numéro de ligne croissant, sachant qu'il n'y a qu'une
     * ligne pour un fichier de recouvrement
     */
    @Query("select lf from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande order by lf.position")
    List<LigneFichierRecouv> getLigneFichierbyDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numero de la demande de recouvrement
     * @param position le numero de ligne du fichier
     * @return Une ligne d'un fichier chargé par l'utilisateur pour le recouvrement, sachant qu'il n'y a
     * qu'une seule ligne de données pour une demande de recouvrement
     */
    @Query("select lf from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.position = :position")
    LigneFichierRecouv getLigneFichierbyDemandeEtPos(@Param("numDemande") Integer numDemande, @Param("position") Integer position);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande
     */
    @Query("select count(lf) from LigneFichierRecouv  lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee=1")
    int getNbLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    @Query("select lf from LigneFichierRecouv  lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee=1 order by lf.position")
    List<LigneFichierRecouv> getLigneFichierTraitee(@Param("numDemande") Integer numDemande);

    /**
     * @param demande le numéro de la demande dont les lignes fichiers vont être supprimées
     */
    void deleteByDemandeRecouv(DemandeRecouv demande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc à été
     * positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee=1 and lf.retourSudoc = 'Le traitement a été effectué.'")
    int getNbLigneFichierSuccessByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de ligne du fichier qui ont été traitées sur cette demande et dont le retour du sudoc n'a pas
     * été positif pour le traitement
     */
    @Query("select count(lf) from LigneFichierRecouv lf where demandeRecouv.numDemande = :numDemande and traitee=1 and retourSudoc != 'Le traitement a été effectué.'")
    int getNbLigneFichierErrorByDemande(@Param("numDemande") Integer numDemande);

    /**
     * @param numDemande le numéro de la demande
     * @return Le nombre de lignes total du fichier
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande")
    int getNbLigneFichierTotalByDemande(@Param("numDemande") Integer numDemande);

    /**
     * Méthode permettant pour une demande donnée de récupérer le nombre de recherche effectuées
     * @param numDemande : le numéro de la demande
     * @return retourne le nombre de ligne du fichier pour lesquelles la requête à retourné au moins 1 résultat
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee = 1 and lf.nbReponses != 0")
    int getNbReponseTrouveesByDemande(@Param("numDemande") Integer numDemande);

    /**
     * Méthode permettant pour une demande donnée de récupérer le nombre de recherche ayant retournée 0 réponse
     * @param numDemande : le numéro de la demande
     * @return nombre de requêtes ayant retourné 0 réponse
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee = 1 and lf.nbReponses = 0")
    int getNbZeroReponseByDemande(@Param("numDemande") Integer numDemande);

    /**
     * Méthode permettant pour une demande donnée de récupérer le nombre de recherche ayant retournée une réponse
     * @param numDemande : numéro de la demande
     * @return nombre de requête ayant retourné 1 seule réponse
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee = 1 and lf.nbReponses = 1")
    int getNbUneReponseByDemande(@Param("numDemande") Integer numDemande);

    /**
     * Méthode permettant pour une demande donnée de récupérer le nombre de recherche ayant retournée plus d'une réponse
     * @param numDemande : numéro de la demande
     * @return nombre de requêtes ayant retourné plusieurs réponses
     */
    @Query("select count(lf) from LigneFichierRecouv lf where lf.demandeRecouv.numDemande = :numDemande and lf.traitee = 1 and lf.nbReponses > 1")
    int getNbReponseMultipleByDemande(@Param("numDemande") Integer numDemande);
}
