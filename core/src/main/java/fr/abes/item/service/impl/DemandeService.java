package fr.abes.item.service.impl;

import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DemandeService  {
    @Autowired
    @Getter
    protected DaoProvider dao;

    @Autowired
    @Getter
    protected ServiceProvider service;

    @Value("${batch.min.hour}")
    protected int minHour;
    @Value("${batch.max.hour}")
    protected int maxHour;
    /**
     * Va mettre à jour la liste de demandeModifs préalablement récupérée interroge
     * la base XML pour completer les demandeModifs avec l'ILN et intitulé du RCR, le code RCR étant dans la table
     *
     * @param demandeList La liste de demande
     */
    public void setIlnShortNameOnList(List<Demande> demandeList) {
        setIlnShortNameOnDemandes(demandeList);
    }

    /**
     * Effectue une recherche pour retournée une entité LibProfile qui correspond à un établissement à partir
     * du rcr associé au numéro de la demande
     *
     * Si on à partir du rcr associé à la demande trouvé un établissement dans le la table LibProfile su Shcéma autorités
     * on renseigne l'iln de la demande à partir de l'iln de l'entité issu de la table lib profile
     * on renseigne le shortname également
     * @param demande
     */
    public void setIlnShortNameOnDemande(Demande demande) {
        Optional<LibProfile> library = this.getDao().getLibProfile().findById(demande.getRcr());
        if (library.orElse(null) != null) {
            demande.setShortname(library.orElse(null).getShortName());
        } else {
            demande.setShortname("non trouvé");
        }
    }

    public void setIlnShortNameOnDemandes(List<Demande> demandeList) {
        List<String> demandesIdListe = new ArrayList<>();

        //Alimentation d'une liste de rcr
        Iterator<Demande> it = demandeList.iterator();
        while (it.hasNext()) {
            Demande demande = it.next();
            demandesIdListe.add(demande.getRcr());
        }

        //Dédoublonnage de la liste de rcr
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Iterator<String> i = demandesIdListe.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            }
            else {
                set.add(s);
            }
        }

        //Récupération d'une liste d'entités LibProfile avec une liste rcr passée en paramètre
        List<LibProfile> listLibProfile = this.getDao().getLibProfile().getShortnameAndIlnFromRcr(demandesIdListe);

        //Alimentation des attributs shortname et iln de chaque entité demande en cas de manquant / vide
        Iterator<Demande> dem = demandeList.iterator();
        while (dem.hasNext()) {
            Demande demande = dem.next();
            demande.feedIlnAndShortname(listLibProfile);
        }
    }

    /**
     * Permet de savoir si il existe des demandes dans une liste avec un iln nul, ou un shortname nul
     * @param demandeList liste de demandes
     * @return true si demandes avec iln nul présent, ou avec shortname présent
     */
    private boolean isThereAtLeastOneDemandeWithIlnOrShortnameEmpty(List<Demande> demandeList) {
        Iterator<Demande> it = demandeList.iterator();
        while (it.hasNext()) {
            Demande demande = it.next();
            if(Objects.equals(demande.getIln(), "") || demande.getIln() == null || demande.getShortname() == null || Objects.equals(demande.getShortname(), "")){
                return true;
            }
        }
        return false;
    }

    /**
     * Va ajouter des espaces entre les zones systèmes, les zones locales et les zones d'exmplaire dans la phase de visualisation
     *
     * @param demandeBrute la demande qui va être matchée via des expressions régulières pour détecter les zones
     * @return une string qui contient la demande accompagnée des espacements transmise au webservice pour visualisation
     */
    public String getSeparationBetweenBlocks(String demandeBrute) {
        /*construire expression reguliere detection premiere zone systeme*/
        Pattern systemZonePattern = Pattern.compile("A\\d{2}");
        Matcher systemZoneMatcher = systemZonePattern.matcher(demandeBrute);

        systemZoneMatcher.find();/*obligatoire sinon le matcher lancera une exception*/
        String premiereZoneSysteme = systemZoneMatcher.group(0); /*recupère uniquement la première occurence*/

        demandeBrute = demandeBrute.replace(premiereZoneSysteme, "\r" + premiereZoneSysteme);

        /*construire expression reguliere detection premiere zone d'une notice d'exemplaire*/
        Pattern firstExempZonePattern = Pattern.compile("e\\d{2}");
        Matcher firstExempZoneMatcher = firstExempZonePattern.matcher(demandeBrute);

        firstExempZoneMatcher.find();/*obligatoire sinon le matcher lancera une exception*/

        String premiereZoneExemplaire = firstExempZoneMatcher.group(0);

        demandeBrute = demandeBrute.replace(premiereZoneExemplaire,  premiereZoneExemplaire);

        return demandeBrute;
    }
}
