package fr.abes.item.dao.impl;

import fr.abes.item.dao.baseXml.ILibProfileDao;
import fr.abes.item.dao.baseXml.IUserProfileDao;
import fr.abes.item.dao.item.*;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Le provider central qui contient l'ensemble des interfaces dao pour travailler sur les tables en base de donnée,
 * au travers des entités. Chaque entité est présente dans une interface :
 * JpaRepository<[NOM_ENTITE], [CLE_PRIMAIRE_ENTITE]>
 *
 *     Les méthodes de base que fournissent JpaRepository
 *     https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html
 *
 *     Retrouver l'usage des différentes interfaces Dao
 *     Faire dans le projet une recherche CTRL+MAJ+F (intellij) avec :
 *     getDao().get[nom de l'attribut dao de cette classe avec une majuscule]
 *     ex : getDao().getLibProfile()
 */
@Service
@Getter
public class DaoProvider {
    /**
    * Table DEMANDE_MODIF : les demandes de modification d'exemplaires
    */
    @Resource
    private IDemandeModifDao demandeModif;

    /**
    * Table DEMANDE_EXEMP : les demandes d'ajout d'exemplaires
    */
    @Resource
    private IDemandeExempDao demandeExemp;

    /**
     * Table DEMANDE_RECOUV : les demandes de taux de recouvrement
     */
    @Resource
    private IDemandeRecouvDao demandeRecouv;

    /**
    * Table LIB_PROFILE (schema AUTORITES) : les différents établissements physiques (bibliothèques),
    * identifiables par un attribut RCR et rattaché à un groupe d'etablissement ILN
    */
    @Resource
    private ILibProfileDao libProfile;

    /**
    * Table USER_PROFILE (schema AUTORITES) : les différents utilisateurs physiques autorisés à se loguer
    * à l'application : catalogueurs, coordinateurs, personnel de l'ABES
    */
    @Resource
    private IUserProfileDao userProfile;

    /**
    * Table ETAT_DEMANDE : l'état d'avancement du traitement d'une demande, qui est associé à une demande
    */
    @Resource
    private IEtatDemandeDao etatDemande; //

    /**
    * Table JOURNAL_DEMANDE_MODIF : le journal de la modification des états d'une demande
    * de modification d'exemplaires en masse : quand une demande
    * a changé d'état d'avancement
    */
    @Resource
    private IJournalDemandeModifDao journalDemandeModif;

    /**
    * Table JOURNAL_DEMANDE_EXEMP : le journal de la modification des états d'une demande
    * d'exemplarisation d'un exemplaire : quand une demande
    * a changé d'état d'avancement
    */
    @Resource
    private IJournalDemandeExempDao journalDemandeExemp;

    /**
    * Table LIGNE_FICHIER_MODIF : une ligne du fichier chargé par l'utilisateur pour procéder à
    * une demande de modification de plusieurs exemplaires sur une sous-zone d'une zone
    */
    @Resource
    private ILigneFichierModifDao ligneFichierModif;

    /**
     * Table LIGNE_FICHIER_EXEMP : la ligne du fichier chargé par l'utilisateur contenant les données qui vont
     * être exemplarisées
     */
    @Resource
    private ILigneFichierExempDao ligneFichierExemp;

    @Resource
    private ILigneFichierRecouvDao ligneFichierRecouv;

    /**
    * Table ROLE : en 1 l'utilisateur est un admnistrateur (droits étendus)
    * en 2 l'utilisateur est un coordinateur (pour faire un controle sur les droits à posteriori)
    */
    @Resource
    private IRoleDao role;

    /**
    * Table TRAITEMENT : contient les différents traitements disponible pour une demande de modification
    * d'exemplaires en masse (créer, modifier, supprimer)
     */
    @Resource
    private ITraitementDao traitement; //

    /**
    * Table UTILISATEUR : les utilisateurs de l'application
     */
    @Resource
    private IUtilisateurDao utilisateur;

    /**
    * Table INDEX_RECHERCHE : contient les codes recherche et les libellés de recherche
    * soit les différents type de recherche possibles
     */
    @Resource
    private IIndexRechercheDao indexRecherche; //

    /**
    * Table TYPE_EXEMP : les types d'exemplarisation disponibles
     */
    @Resource
    private ITypeExempDao typeExemp;

    /**
     * Table ZONES_AUTORISEES : liste des zones autorisées pour l'exemplarisation
     */
    @Resource
    private IZonesAutoriseesDao zonesAutorisees;

    /**
     * Table SOUS_ZONES_AUTORISEES : liste des sous zones autorisées pour une zone donnée dans l'exemplarisation
     */
    @Resource
    private ISousZonesAutoriseesDao sousZonesAutorisees;


}