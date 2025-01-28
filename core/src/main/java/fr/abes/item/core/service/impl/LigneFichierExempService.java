package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.DonneeLocale;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.cbs.notices.ZoneEtatColl;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierExemp;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.item.ILigneFichierExempDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Strategy(type= ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.EXEMP})
@Service
public class LigneFichierExempService implements ILigneFichierService {
    private final ILigneFichierExempDao dao;
    private final TraitementService traitementService;
    private final IZonesAutoriseesDao zonesAutoriseesDao;
    @Getter
    private String exemplairesExistants;
    @Getter
    private String donneeLocaleExistante;
    @Getter
    private int nbReponses;
    private final ReentrantLock lock = new ReentrantLock();


    public LigneFichierExempService(ILigneFichierExempDao dao, TraitementService traitementService, IZonesAutoriseesDao zonesAutoriseesDao) {
        this.dao = dao;
        this.traitementService = traitementService;
        this.zonesAutoriseesDao = zonesAutoriseesDao;
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierExemp ligneFichierExemp = (LigneFichierExemp) ligneFichier;
        return dao.save(ligneFichierExemp);
    }

    @Override
    public LigneFichierExemp findById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    //Construction des lignes d'exemplaires
    @Override
    @Transactional
    public void saveFile(File file, Demande demande) {
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        try (BufferedReader reader = ReaderFactory.createBufferedReader(file)) {
            String line;
            //lecture à vide de la première ligne du fichier
            reader.readLine();
            int position = 0;

            while ((line = reader.readLine()) != null) {
                StringBuilder indexRecherche = new StringBuilder();
                StringBuilder valeur = new StringBuilder();
                if (line.lastIndexOf(';') == line.length() - 1) {
                    line += (char)0;
                }
                String [] tabLine = line.split(";");
                for (int i = 0; i < demandeExemp.getIndexRecherche().getIndexZones();i++) {
                    indexRecherche.append(tabLine[i]).append(";");
                }
                //suppression du ; final
                indexRecherche = new StringBuilder(indexRecherche.substring(0, indexRecherche.length() - 1));
                for (int i= demandeExemp.getIndexRecherche().getIndexZones(); i<tabLine.length;i++) {
                    valeur.append(tabLine[i]).append(";");
                }
                //suppression du ; final
                valeur = new StringBuilder(valeur.substring(0, valeur.length() - 1));
                //création de la ligne fichier et enregistrement
                LigneFichierExemp ligneFichierExemp = new LigneFichierExemp(indexRecherche.toString(), valeur.toString(), 0, position++, "", null, demandeExemp, null);
                dao.save(ligneFichierExemp);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Demande demande) {
        List<LigneFichierExemp> ligneFichierExemps = dao.getLigneFichierbyDemande(demande.getId());
        return new ArrayList<>(ligneFichierExemps);
    }

    @Override
    public int getNbLigneFichierTraiteeByDemande(Demande demande) {
        return dao.getNbLigneFichierTraitee(demande.getId());
    }


    @Override
    public List<LigneFichier> getLigneFichierTraiteeByDemande(Demande demande) {
        List<LigneFichierExemp> ligneFichierExemps = dao.getLigneFichierTraitee(demande.getId());
        return new ArrayList<>(ligneFichierExemps);
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
        return dao.getNbLigneFichierTotalByDemande(demande.getId());
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        dao.deleteByDemandeExemp((DemandeExemp) demande);
    }

    @Override
    public int getNbReponseTrouveesByDemande(Demande demande) {
        return dao.getNbReponseTrouveesByDemande(demande.getId());
    }

    @Override
    public int getNbUneReponseByDemande(Demande demande) {
        return dao.getNbUneReponseByDemande(demande.getId());
    }

    @Override
    public int getNbZeroReponseByDemande(Demande demande) {
        return dao.getNbZeroReponseByDemande(demande.getId());
    }

    @Override
    public int getNbReponseMultipleByDemande(Demande demande) {
        return dao.getNbReponseMultipleByDemande(demande.getId());
    }

    /**
     * Méthode permettant d'interroger le Sudoc, de créer un exemplaire et de le retourner pour la simulation
     *
     * @param demandeExemp      demande d'exemplarisation concernée
     * @param ligneFichierExemp ligneFichier à traiter
     * @return la chaine de l'exemplaire construit, ou message d'erreur
     */
    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demandeExemp, LigneFichier ligneFichierExemp) throws CBSException, IOException, ZoneException {
        try {
            DemandeExemp demande = (DemandeExemp) demandeExemp;
            LigneFichierExemp ligneFichier = (LigneFichierExemp) ligneFichierExemp;
            lock.lock();
            traitementService.authenticate("M" + demande.getRcr());
            String numEx = launchQueryToSudoc(demande, ligneFichier.getIndexRecherche());
            //Retourne le tableau exemplaires existants / Exemplaire à créer
            return new String[]{
                    //L'indice 0 retourne le PPN de la notice
                    traitementService.getCbs().getPpnEncours(),
                    //L'indice 1 retourne les données locales et les exemplaires existants tous ensemble sous forme d'une chaine
                    Utilitaires.removeNonPrintableCharacters(donneeLocaleExistante).replace("\r", "\r\n") + "\r\n" + exemplairesExistants.replace("\r", "\r\n"), //2*r\n\ comptent pour un saut de ligne
                    //L'indice 2 retourne le bloc de données locales et l'exemplaire à créer
                    creerDonneesLocalesFromHeaderEtValeur(demande.getListeZones(), ligneFichier.getValeurZone()).replace("\r", "\r\n") + "\r\n" +
                            creerExemplaireFromHeaderEtValeur(demande.getListeZones(), ligneFichier.getValeurZone(), demande.getRcr(), numEx).replace("\r", "\r\n"),
            };
            //Si l'utilisateur n'a pas autorisé la création d'exemplaires multiples sur les notices de cette demande associée à ce RCR en cas d'exemplaires déjà présents

        } catch (QueryToSudocException e) {
            throw new CBSException(Level.ERROR, e.getMessage());
        } finally {
            traitementService.disconnect();
            lock.unlock();
        }
    }

    /**
     * @param demande : la demande à partir de laquelle on va construire la requête
     * @param valeurs : tableau des valeurs des index de recherche
     * @return le numéro du prochain exemplaire à créer dans la notice au format "xx"
     */
    public String launchQueryToSudoc(DemandeExemp demande, String valeurs) throws CBSException, QueryToSudocException, IOException {
        String[] tabvaleurs = valeurs.split(";");
        String query = getQueryToSudoc(demande.getIndexRecherche().getCode(), demande.getTypeExemp().getNumTypeExemp(), tabvaleurs);

        if (!query.isEmpty()) {
            try {
                traitementService.getCbs().search(query);
                nbReponses = traitementService.getCbs().getNbNotices();
            } catch (IOException e) {
                nbReponses = 0;
            }
            switch (nbReponses) {
                //Le sudoc n'a pas trouvé de notice correspondant au PPN ou autre critère de recherche
                case 0:
                    throw new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND);
                case 1:
                    //Le sudoc à trouvé une notice correspondant au critère
                    String notice = traitementService.getCbs().getClientCBS().mod("1", String.valueOf(traitementService.getCbs().getLotEncours()));
                    String numExStr = Utilitaires.getLastNumExempFromNotice(notice);
                    //On controle ici pour la notice trouvée dans le sudoc le nombre d'exemplaires déjà présents sur ce RCR
                    donneeLocaleExistante = Utilitaires.getDonneeLocaleExistante(notice);
                    exemplairesExistants = Utilitaires.getExemplairesExistants(notice);
                    int numEx = Integer.parseInt(numExStr);
                    numEx++;
                    return (numEx < 10) ? "0" + numEx : Integer.toString(numEx); //On retourne le numero d'exemplaire ou sera enregistré le nouvel exemplaire
                default:
                    throw new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + traitementService.getCbs().getListePpn());
            }
        } else {
            throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_NOT_COMPLIANT);
        }
    }

    /**
     * Méthode de création des données locales à partir de l'en tête du fichier et des valeurs associées
     *
     * @param header : header du fichier
     * @param valeurZones : valeurs des zones à ajouter dans les données locales
     * @return les données locales de la notice
     */
    public String creerDonneesLocalesFromHeaderEtValeur(String header, String valeurZones) throws ZoneException {
        String[] listeHeader = header.split(";");
        String[] listeValeur = valeurZones.split(";");
        String zonePrecedente = "";
        DonneeLocale donneeLocale = new DonneeLocale(Constants.STR_1F + this.donneeLocaleExistante + Constants.STR_1E);
        for (int i = 0; i < listeHeader.length; i++) {
            if (Utilitaires.isDonneeLocale(listeHeader[i], zonePrecedente)) {
                String headerEnCours = listeHeader[i];
                String valeurEnCours = listeValeur[i];
                Pattern patternHeader = Pattern.compile(Constant.REG_EXP_DONNEELOCALE);
                Matcher matcher = patternHeader.matcher(headerEnCours);

                if (matcher.find()) {
                    donneeLocale.addZone(matcher.group("zone"), matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(matcher.group("zone")).toCharArray());
                    zonePrecedente = matcher.group("zone");
                } else {
                    donneeLocale.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                }
            }
        }

        //replaceAll -> retire les caractères ASCII Printables sur des plages définies
        return donneeLocale.toString();
    }

    /**
     * Méthode de construction d'un exemplaire à partir de l'en tête du fichier et des valeurs associées
     *
     * @param header      : chaine contenant la liste des zones à créer (séparées par des ;)
     * @param valeurZones : chaine contenant les valeurs des zones à créer (séparées par des ;)
     * @return l'exemplaire sous forme de chaine
     */
    public String creerExemplaireFromHeaderEtValeur(String header, String valeurZones, String rcr, String numExemp) throws ZoneException {
        String[] listeHeader = header.split(";");
        String[] listeValeur = valeurZones.split(";");
        String zonePrecedente = "";
        //variable permettant de déterminer si une 930 a été ajoutée dans l'exemplaire
        boolean added930 = false;
        boolean zonePrecedenteVide = false;
        //Création d'un exemplaire vide
        Exemplaire exemp = new Exemplaire();
        //le fichier ayant été vérifié les deux tableaux ont la même taille
        for (int i = 0; i < listeHeader.length; i++) {
            String headerEnCours = listeHeader[i]; //entête en cours (une zone, une zone avec sous zone, ou une sous zone)
            String valeurEnCours = listeValeur[i]; //valeur en cours, associée à l'entête juste avant
            if (!Utilitaires.isDonneeLocale(listeHeader[i], zonePrecedente)) {
                Pattern patternHeader = Pattern.compile(Constant.REG_EXP_ZONE_SOUS_ZONE);
                Matcher matcher = patternHeader.matcher(headerEnCours);
                if (matcher.find()) {
                    //cas où on ajoute une zone + sous zone
                    String labelZone = matcher.group("zone");
                    if (labelZone.equals("930") && !("").equals(valeurEnCours)) {
                        added930 = true;
                    }
                    if (Utilitaires.isEtatCollection(labelZone)) {
                        //cas d'une zone d'état de collection
                        if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                            exemp.addZoneEtatCollection(labelZone, matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(labelZone).toCharArray());
                        } else {
                            zonePrecedenteVide = true;
                        }
                    } else {
                        //cas ou le header en cours est une zone + indicateur + sous zone classique
                        if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                            exemp.addZone(labelZone, matcher.group("sousZone"), valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(labelZone).toCharArray());
                        } else {
                            zonePrecedenteVide = true;
                        }
                    }
                    zonePrecedente = matcher.group("zone");
                } else {
                    //cas où on ajoute une sous zone à une zone déjà insérée
                    if (zonePrecedenteVide) {
                        if (Utilitaires.isEtatCollection(zonePrecedente)) {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                exemp.addZoneEtatCollection(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                            }
                        } else {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                if (!added930)
                                    exemp.addZone(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                else
                                    exemp.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                                if (zonePrecedente.equals("930")) {
                                    added930 = true;
                                }
                            }
                        }
                        zonePrecedenteVide = false;
                    } else {
                        if (Utilitaires.isEtatCollection(zonePrecedente)) {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                List<Zone> listezone = exemp.findZones(zonePrecedente);
                                if (!listezone.isEmpty()) {
                                    ZoneEtatColl zone = (ZoneEtatColl) listezone.get(0);
                                    zone.addSousZone(headerEnCours, valeurEnCours, 0);
                                } else {
                                    if (headerEnCours.equals("$4")) {
                                        //cas où on essaie d'ajouter une $4 seule dans la 955
                                        exemp.addZoneEtatCollection(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                        ZoneEtatColl zone = (ZoneEtatColl) exemp.findZones(zonePrecedente).get(0);
                                        zone.addSousZone(headerEnCours, valeurEnCours, 0);
                                    }
                                }
                            }
                        } else {
                            if (!((("").equals(valeurEnCours)) || (valeurEnCours.charAt(0) == (char) 0))) {
                                //cas ou le header en cours est une sous zone seule
                                if (!exemp.findZones(zonePrecedente).isEmpty()) {
                                    exemp.addSousZone(zonePrecedente, headerEnCours, valeurEnCours);
                                } else {
                                    exemp.addZone(zonePrecedente, headerEnCours, valeurEnCours, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zonePrecedente).toCharArray());
                                    if (zonePrecedente.equals("930")) {
                                        added930 = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (added930) {
            //une 930 a été rajoutée, on rajoute juste une sous zone $b
            exemp.addSousZone("930", "$b", rcr);
        } else {
            //pas de 930 ajoutée par l'utilisateur, on la crée avec une $b
            exemp.addZone("930", "$b", rcr, zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone("930").toCharArray());
        }
        //ajout de la exx
        exemp.addZone("e" + numExemp, "$b", "x");
        //ajout de la 991 $a
        ajout991(exemp);
        return exemp.toString();
    }


    /**
     * Méthode d'ajout d'une zone 991 prédéfinie dans l'exemplaire
     *
     * @param exemp : exemplaire sur lequel rajouter la 991
     */
    private void ajout991(Exemplaire exemp) throws ZoneException {
        String datePattern = "dd-MM-yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        String date = simpleDateFormat.format(Calendar.getInstance().getTime());
        String valeur991 = Constant.TEXTE_991_CREA + " le " + date;
        char[] indicateurs = {'#', '#'};
        exemp.addZone("991", "$a", valeur991, indicateurs);
    }

    /**
     * Méthode construisant la requête che en fonction des paramètres d'une demande d'exemplarisation
     *
     * @param codeIndex code de l'index de la recherche
     * @param valeur    tableau des valeurs utilisées pour construire la requête
     * @return requête che prête à être lancée vers le CBS
     */
    @Override
    public String getQueryToSudoc(String codeIndex, Integer typeExemp, String[] valeur) throws QueryToSudocException {
        switch (typeExemp) {
            case Constant.TYPEEXEMP_MONOELEC:
                switch (codeIndex) {
                    case "ISBN":
                        return "tno t; tdo o; che isb " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo o; che sou " + valeur[0];
                }
            case Constant.TYPEEXEMP_PERIO:
                switch (codeIndex) {
                    case "ISSN":
                        return "tno t; tdo t; che isn " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo t; che sou " + valeur[0];
                }
            case Constant.TYPEEXEMP_AUTRE:
                switch (codeIndex) {
                    case "ISBN":
                        return "tno t; tdo b; che isb " + valeur[0];
                    case "PPN":
                        return "che ppn " + valeur[0];
                    case "SOU":
                        return "tno t; tdo b; che sou " + valeur[0];
                    case "DAT":
                        if (valeur[1].isEmpty()) {
                            return "tno t; tdo b; apu " + valeur[0] + "; che mti " + Utilitaires.replaceDiacritical(valeur[2]);
                        }
                        return "tno t; tdo b; apu " + valeur[0] + "; che aut " + Utilitaires.replaceDiacritical(valeur[1]) + " et mti " + Utilitaires.replaceDiacritical(valeur[2]);
                }
            default:
                throw new QueryToSudocException(Constant.ERR_FILE_SEARCH_INDEX_NOT_RECOGNIZED_FOR_DEMANDE);
        }
    }

    public boolean hasDonneeLocaleExistante() {
        return !donneeLocaleExistante.isEmpty();
    }
}
