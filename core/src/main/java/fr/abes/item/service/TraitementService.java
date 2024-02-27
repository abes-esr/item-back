package fr.abes.item.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.cbs.utilitaire.Utilitaire;
import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.Traitement;
import fr.abes.item.repository.item.ITraitementDao;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TraitementService {
	private final ITraitementDao traitementDao;

	@Value("${sudoc.serveur}")
	private String serveurSudoc;

	@Value("${sudoc.port}")
	private String portSudoc;

	@Getter
	private ProcessCBS cbs;


	public TraitementService(ITraitementDao traitementDao) {
		this.traitementDao = traitementDao;
		cbs = new ProcessCBS();
    }

	public void authenticate(String login) throws CBSException {
		this.cbs.authenticate(serveurSudoc, portSudoc, login, Constant.PASSSUDOC);
	}

	/**
	 * Méthode de recherche d'un EPN et de récupération du premier exemplaire d'une notice
	 * @param epn : epn à rechercher
	 * @return notice d'exemplaire trouvée
	 * @throws CBSException : Erreur CBS
	 */
	public String getNoticeFromEPN(String epn) throws CBSException {
		cbs.search("che EPN " + epn);
		if (cbs.getNbNotices() == 1) {
			String noticeEpn = cbs.getClientCBS().mod("1", String.valueOf(cbs.getLotEncours()));
			String numEx = Utilitaires.getNumExFromExemp(Utilitaires.getExempFromNotice(noticeEpn, epn));
			String resu = cbs.getClientCBS().modE(numEx, String.valueOf(cbs.getLotEncours()));
			cbs.back();
			String resu2 = Utilitaire.recupEntre(resu, Constants.VTXTE, Constants.STR_0D + Constants.STR_0D + Constants.STR_1E);
			return Constants.STR_1F + resu2.substring(resu2.indexOf("e" + numEx)) + Constants.STR_0D + Constants.STR_1E;
		}
		else
			throw new CBSException(Constant.CBS_PREFIX + Constants.VERROR, Constant.ERR_FILE_NOTICE_EPN_NUMBER);
	}

	/**
	 * Méthode permettant d'ajouter une zone / sous zone dans une notice d'exemplaire
	 * @param notice notice à modifier
	 * @return notice avec nouvelle zone / sous zone préfixée de STR_1F
	 */
	public String creerNouvelleZone(String notice, String tag, String subTag, String valeur) throws ZoneException {
		Exemplaire exemp = new Exemplaire(notice);
		exemp.addZone(tag, subTag, valeur);
		exemp = ajout991(exemp);
		return exemp.toString();
	}

	/**
	 * Méthode permettant la suppression d'une zone dans une notice d'exemplaire
	 * @param notice notice biblio + exemplaires
	 * @param tag zone à supprimer
	 * @return chaine de l'exemplaire modifié préfixé par STR_1F
	 */
	public String supprimerZone(String notice, String tag) throws ZoneException {
		Exemplaire exemp = new Exemplaire(notice);
		exemp.deleteZone(tag);
		exemp = ajout991(exemp);
		return exemp.toString();
	}

	/**
	 * Méthode permettant la suppression d'une sous-zone dans une notice d'exemplaire
	 * @param notice notice biblio + exemplaires
	 * @param tag zone qui contient la sous-zone
	 * @param subTag zone à supprimer
	 * @return chaine de l'exemplaire modifié préfixé par STR_1F
	 */
	public String supprimerSousZone(String notice, String tag, String subTag) throws ZoneException {
		Exemplaire exemp = new Exemplaire(notice);
		exemp.deleteSousZone(tag, subTag);
		exemp = ajout991(exemp);
		return exemp.toString();
	}


	/**
	 * Méthode permettant la création d'une sous-zone dans une notice d'exemplaire
	 * @param notice notice biblio + exemplaires
	 * @param tag zone qui contient la sous-zone
	 * @param subTag sous-zone à créer
	 * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
	 * @return l'exemplaire avec la zone ajoutée
	 */
	public String creerSousZone(String notice, String tag, String subTag, String valeur) throws ZoneException {
		Exemplaire exemp = new Exemplaire(notice);
		exemp.addSousZone(tag, subTag, valeur);
		exemp = ajout991(exemp);
		return exemp.toString();
	}

	/**
	 * Méthode permettant le remplacement d'une sous-zone dans une notice d'exemplaire
	 * @param notice notice biblio + exemplaires
	 * @param tag zone qui contient la sous-zone
	 * @param subTag sous-zone à remplacer
	 * @param valeur valeur associée à la sous zone (la sous-zone est la clé)
	 * @return l'exemplaire avec la sous zone remplacée
	 */
	public String remplacerSousZone(String notice, String tag, String subTag, String valeur) throws ZoneException {
		Exemplaire exemp = new Exemplaire(notice);
		try {
			exemp.replaceSousZone(tag, subTag, valeur);
			exemp = ajout991(exemp);
		} catch (NullPointerException ex) {
			log.debug("Zone / sous zone absente de la notice à modifier");
		}
		return exemp.toString();
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
	public String saveExemplaire(String noticeModifiee) throws CBSException {
		String numEx = Utilitaires.getNumExFromExemp(noticeModifiee);
		String noticeModifieeClean = "e" + numEx + Utilitaire.recupEntre(noticeModifiee, 'e' + numEx, Constants.STR_1E);
		return cbs.modifierExemp(noticeModifieeClean, numEx);
	}


	/**
	 * Deconnexion du client CBS (sudoc)
	 */
    public void disconnect() throws CBSException {
	    cbs.getClientCBS().disconnect();
    }


	/**
	 * Retourner l'ensemble de la liste des traitements disponibles
	 * @return l'ensemble des traitements disponibles en base
	 */
	public List<Traitement> findAll() {
		return traitementDao.findAll();
	}

	public Integer findTraitementByDemandeId(Integer id){
		return traitementDao.findTraitementByDemandeModifId(id);
	}


}
