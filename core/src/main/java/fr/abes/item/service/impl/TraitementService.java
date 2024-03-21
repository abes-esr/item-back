package fr.abes.item.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.NoticeConcrete;
import fr.abes.cbs.notices.Zone;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.cbs.utilitaire.Utilitaire;
import fr.abes.item.constant.Constant;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.Traitement;
import fr.abes.item.service.ITraitementService;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraitementService implements ITraitementService {

	@Value("${sudoc.serveur}")
	private String serveurSudoc;

	@Value("${sudoc.port}")
	private String portSudoc;

	@Getter
	private ProcessCBS cbs;

	@Autowired @Getter
	private DaoProvider dao;

	public TraitementService() {
	    cbs = new ProcessCBS();
    }

	@Override
	public void authenticate(String login) throws CBSException, CommException {
		this.cbs.authenticate(serveurSudoc, portSudoc, login, Constant.PASSSUDOC);
		log.debug("je suis authentifié");
	}

	/**
	 * Méthode de recherche d'un EPN et de récupération du premier exemplaire d'une notice
	 *
	 * @param epn : epn à rechercher
	 * @return notice d'exemplaire trouvée
	 * @throws CBSException : Erreur CBS
	 */
	@Override
	public Exemplaire getNoticeFromEPN(String epn) throws CBSException, CommException, ZoneException {
		cbs.search("che EPN " + epn);
		if (cbs.getNbNotices() == 1) {
			NoticeConcrete noticeEpn = cbs.editerNoticeConcrete("1");
			Optional<Exemplaire> exempOpt = noticeEpn.getExemplaires().stream().filter(exemplaire -> exemplaire.findZone("A99", 0).getValeur().equals(epn)).findFirst();
			if (exempOpt.isPresent())
				return exempOpt.get();
			else {
				log.error(epn + " pas trouvé");
				throw new CBSException(Level.ERROR, Constant.ERR_FILE_NOTICE_EPN_NUMBER);
			}
		}
		return null;
	}

	/**
	 * Méthode permettant d'ajouter une zone / sous zone dans une notice d'exemplaire
	 *
	 * @param exemp notice à modifier
	 * @return notice avec nouvelle zone / sous zone préfixée de STR_1F
	 */
	@Override
	public Exemplaire creerNouvelleZone(Exemplaire exemp, String tag, String subTag, String valeur) throws ZoneException {
		exemp.addZone(tag, subTag, valeur);
		exemp = ajout991(exemp);
		return exemp;
	}

	/**
	 * Méthode permettant la suppression d'une zone dans une notice d'exemplaire
	 *
	 * @param exemp notice biblio + exemplaires
	 * @param tag    zone à supprimer
	 * @return chaine de l'exemplaire modifié préfixé par STR_1F
	 */
	@Override
	public Exemplaire supprimerZone(Exemplaire exemp, String tag) throws ZoneException {
		exemp.deleteZone(tag);
		exemp = ajout991(exemp);
		return exemp;
	}

	/**
	 * Méthode permettant la suppression d'une sous-zone dans une notice d'exemplaire
	 *
	 * @param exemp  notice biblio + exemplaires
	 * @param tag    zone qui contient la sous-zone
	 * @param subTag zone à supprimer
	 * @return chaine de l'exemplaire modifié préfixé par STR_1F
	 */
	@Override
	public Exemplaire supprimerSousZone(Exemplaire exemp, String tag, String subTag) throws ZoneException {
		exemp.deleteSousZone(tag, subTag);
		exemp = ajout991(exemp);
		return exemp;
	}


	/**
	 * Méthode permettant la création d'une sous-zone dans une notice d'exemplaire
	 *
	 * @param exemp  notice biblio + exemplaires
	 * @param tag    zone qui contient la sous-zone
	 * @param subTag sous-zone à créer
	 * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
	 * @return l'exemplaire modifié
	 */
	@Override
	public Exemplaire creerSousZone(Exemplaire exemp, String tag, String subTag, String valeur) throws ZoneException {
		exemp.addSousZone(tag, subTag, valeur);
		exemp = ajout991(exemp);
		return exemp;
	}

	/**
	 * Méthode permettant le remplacement d'une sous-zone dans une notice d'exemplaire
	 *
	 * @param exemp  notice biblio + exemplaires
	 * @param tag    zone qui contient la sous-zone
	 * @param subTag sous-zone à remplacer
	 * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
	 * @return l'exemplaire modifié
	 */
	@Override
	public Exemplaire remplacerSousZone(Exemplaire exemp, String tag, String subTag, String valeur) throws ZoneException {
		try {
			exemp.replaceSousZone(tag, subTag, valeur);
			exemp = ajout991(exemp);
		} catch (NullPointerException ex) {
			log.debug("Zone / sous zone absente de la notice à modifier");
		}
		return exemp;
	}

	/**
	 * Ajout d'une zone 991 $a indiquant la modification de la notice par le programme
	 * @param exemp exemplaire à modifier
	 * @return exemplaire modifié
	 */
	public Exemplaire ajout991(Exemplaire exemp) throws ZoneException {
		String datePattern = "dd-MM-yyyy HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
		String date = simpleDateFormat.format(new Date());
		char[] indicateurs = new char[2];
		indicateurs[0] = '#';indicateurs[1] = '#';
		String valeurToInsert = Constant.TEXTE_991_MODIF + " le " + date;
		List<Zone> listZone = exemp.findZoneWithPattern("991", "$a", Constant.TEXTE_991_MODIF);
		if (listZone.isEmpty()) {
			exemp.addZone("991", "$a", valeurToInsert, indicateurs);
		}
		else {
			exemp.replaceSousZoneWithValue("991", "$a", Constant.TEXTE_991_MODIF, valeurToInsert);
		}
		return exemp;
	}

	/**
	 * méthode de validation de la sauvegarde d'un exemplaire
	 * @param noticeModifiee notice à sauvegarder
	 * @return : retour CBS
	 * @throws CBSException : erreur CBS
	 */
	@Override
	public String saveExemplaire(String noticeModifiee, String epn) throws CBSException, CommException {
		String numEx = Utilitaires.getNumExFromExemp(noticeModifiee);
		String noticeModifieeClean = "e" + numEx + Utilitaire.recupEntre(noticeModifiee, 'e' + numEx, Constants.STR_1E);
		log.debug(epn + " sauvegarde exemplaire");
		return cbs.modifierExemp(noticeModifieeClean, numEx);
	}


	/**
	 * Deconnexion du client CBS (sudoc)
	 */
	@Override
    public void disconnect() throws CBSException {
	    cbs.getClientCBS().disconnect();
    }


	/**
	 * Retourner l'ensemble de la liste des traitements disponibles
	 * @return liste de tous les traitements
	 */
	@Override
	public List<Traitement> findAll() {
		return dao.getTraitement().findAll();
	}

	@Override
	public Integer findTraitementByDemandeId(Integer id){
		return dao.getTraitement().findTraitementByDemandeModifId(id);
	}


}
