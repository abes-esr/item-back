package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierModif;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.item.ILigneFichierModifDao;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Strategy(type= ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.MODIF})
@Service
public class LigneFichierModifService implements ILigneFichierService {
    private final ILigneFichierModifDao dao;
    private final TraitementService traitementService;

    public LigneFichierModifService(ILigneFichierModifDao dao, TraitementService traitementService) {
        this.dao = dao;
        this.traitementService = traitementService;
    }

    @Override
    @Transactional
    @SuppressWarnings("squid:S3776")
    public void saveFile(File file, Demande demande){
        DemandeModif demandeModif = (DemandeModif) demande;
        try (BufferedReader reader = ReaderFactory.createBufferedReader(file)){
            String line;
            String firstLine = reader.readLine(); //ne pas prendre en compte la première ligne avec les en-tête

            if(firstLine == null){
                log.error(Constant.ERROR_FIRST_LINE_OF_FILE_NULL);
            }

            int position = 0;

            while ((line = reader.readLine()) != null){
                DemandeModif tDemandeModif = new DemandeModif(demandeModif.getNumDemande(), demandeModif.getRcr(), demandeModif.getDateCreation(), demandeModif.getDateModification(), demandeModif.getZone(), demandeModif.getSousZone(), demandeModif.getCommentaire(), demandeModif.getEtatDemande(), demandeModif.getUtilisateur(), demandeModif.getTraitement());
                Pattern regexp = Pattern.compile(Constant.LIGNE_FICHIER_SERVICE_PATTERN);
                Matcher colsFinded = regexp.matcher(line);
                String ppn = "";
                String rcr = "";
                String epn = "";
                String valeur = "";
                while (colsFinded.find()) {
                    if (colsFinded.group("ppn") != null)
                        ppn = Utilitaires.addZeros(colsFinded.group("ppn"), Constant.TAILLEMAX);
                    if (colsFinded.group("rcr") != null)
                        rcr = Utilitaires.addZeros(colsFinded.group("rcr"), Constant.TAILLEMAX);
                    if (colsFinded.group("epn") != null)
                        epn = Utilitaires.addZeros(colsFinded.group("epn"), Constant.TAILLEMAX);
                    if (colsFinded.group("valeur") != null)
                        valeur = colsFinded.group("valeur");
                }
                LigneFichierModif lf = new LigneFichierModif(ppn, rcr, epn, valeur, position++, 0, "", tDemandeModif);
                dao.save(lf);
            }
        } catch (IOException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Demande demande) {
        List<LigneFichierModif> ligneFichierModifs = dao.getLigneFichierbyDemande(demande.getId());
        return new ArrayList<>(ligneFichierModifs);
    }

    @Override
    public LigneFichierModif findById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichier;
        return dao.save(ligneFichierModif);
    }

    @Override
    public int getNbLigneFichierTraiteeByDemande(Demande demande) {
        return dao.getNbLigneFichierTraitee(demande.getId());
    }

    @Override
    public List<LigneFichier> getLigneFichierTraiteeByDemande(Demande demande) {
        List<LigneFichierModif> ligneFichierModifs = dao.getLigneFichierTraitee(demande.getId());
        return new ArrayList<>(ligneFichierModifs);
    }

    @Override
    public LigneFichier getLigneFichierbyDemandeEtPos(Demande demande, Integer numLigne) {
        return dao.getLigneFichierbyDemandeEtPos(demande.getId(), numLigne);
    }

    @Override
    public int getNbLigneFichierNonTraitee(Demande demande) {
        return dao.getNbLigneFichierNonTraitee(demande.getId());
    }

    @Override
    public int getNbLigneFichierSuccessByDemande(Demande demande) {
        return dao.getNbLigneFichierSuccessByDemande(demande.getId());
    }

    @Override
    public int getNbLigneFichierErrorByDemande(Demande demande) {
        return dao.getNbLigneFichierErrorByDemande(demande.getId());
    }

    @Override
    public int getNbLigneFichierTotalByDemande(Demande demande) {
        return dao.getNbLigneFichierTotal(demande.getId());
    }

    @Override
    public int getNbReponseTrouveesByDemande(Demande demande) {
        return 0;
    }

    @Override
    public int getNbZeroReponseByDemande(Demande demande) {
        return 0;
    }

    @Override
    public int getNbUneReponseByDemande(Demande demande) {
        return 0;
    }

    @Override
    public int getNbReponseMultipleByDemande(Demande demande) {
        return 0;
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        dao.deleteByDemandeModif((DemandeModif) demande);
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, IOException, ZoneException {
        LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichier;
        String noticeInit = getNoticeInitiale(demande, ligneFichierModif.getEpn());
        Exemplaire noticeTraitee = getNoticeTraitee(demande, noticeInit, ligneFichier);
        return new String[]{
                traitementService.getCbs().getPpnEncours(),
                noticeInit.replace("\r", "\r\n"),
                noticeTraitee.toString().replace("\r", "\r\n")
        };
    }

    @Override
    public String getQueryToSudoc(String code, Integer type, String[] valeurs) throws QueryToSudocException {
        //not implemented
        return null;
    }

    /**
     * Méthode de récupération d'une notice par son EPN
     *
     * @param demandeModif utilisée pour récupérer le RCR qui servira pour la construction du login Manager CBS
     * @param epn          epn de la notice à chercher
     * @return La notice trouvée dans le CBS
     * @throws CBSException : erreur CBS
     */
    public String getNoticeInitiale(Demande demandeModif, String epn) throws CBSException, IOException {
        try {
            traitementService.authenticate('M' + demandeModif.getRcr());
            // appel getNoticeFromEPN sur EPN récupéré
            String notice = traitementService.getNoticeFromEPN(epn);
            if (notice != null) {
                return notice.substring(1, notice.length() - 1);
            } else {
                return "";
            }
        } finally {
            // déconnexion du CBS après avoir lancé la requête
            traitementService.disconnect();
        }
    }

    /**
     * Méthode de modification d'une notice en fonction du traitement
     *
     * @param demande      permet de récupérer le traitement à lancer sur la notice
     * @param exemplaire   notice récupérée du Sudoc sur laquelle on effectue le traitement
     * @param ligneFichier informations à intégrer à la notice à traiter
     * @return la notice modifiée
     */
    public Exemplaire getNoticeTraitee(Demande demande, String exemplaire, LigneFichier ligneFichier) throws ZoneException {
        DemandeModif demandeModif = (DemandeModif) demande;
        LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichier;
        String exempStr = Utilitaires.getExempFromNotice(exemplaire, ligneFichierModif.getEpn());
        switch (demandeModif.getTraitement().getNomMethode()) {
            case "creerNouvelleZone":
                return traitementService.creerNouvelleZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            case "supprimerZone":
                return traitementService.supprimerZone(exempStr, demandeModif.getZone());
            case "supprimerSousZone":
                return traitementService.supprimerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone());
            case "ajoutSousZone":
                return traitementService.creerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            case "remplacerSousZone":
                return traitementService.remplacerSousZone(exempStr, demandeModif.getZone(), demandeModif.getSousZone(), ligneFichierModif.getValeurZone());
            default:
        }
        return null;
    }
}
