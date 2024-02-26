package fr.abes.item.service.impl;

import fr.abes.item.dao.baseXml.ILibProfileDao;
import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.entities.item.Demande;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class DemandeService  {
    protected final ILibProfileDao libProfileDao;

    public DemandeService(ILibProfileDao libProfileDao) {
        this.libProfileDao = libProfileDao;
    }

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
     * Si on à partir du rcr associé à la demande trouvé un établissement dans le la table LibProfile su Shcéma autorités
     * on renseigne l'iln de la demande à partir de l'iln de l'entité issu de la table lib profile
     * on renseigne le shortname également
     * @param demande demande à laquelle attribuer un ILN
     */
    protected void setIlnShortNameOnDemande(Demande demande) {
        Optional<LibProfile> library = libProfileDao.findById(demande.getRcr());
        if (library.orElse(null) != null) {
            demande.setShortname(library.orElse(null).getShortName());
        } else {
            demande.setShortname("non trouvé");
        }
    }

    public void setIlnShortNameOnDemandes(List<Demande> demandeList) {
        List<String> demandesIdListe = new ArrayList<>();

        //Alimentation d'une liste de rcr
        for (Demande demande : demandeList) {
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
        List<LibProfile> listLibProfile = libProfileDao.getShortnameAndIlnFromRcr(demandesIdListe);

        //Alimentation des attributs shortname et iln de chaque entité demande en cas de manquant / vide
        for (Demande demande : demandeList) {
            demande.feedIlnAndShortname(listLibProfile);
        }
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
