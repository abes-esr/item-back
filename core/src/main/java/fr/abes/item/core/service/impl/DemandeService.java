package fr.abes.item.core.service.impl;

import fr.abes.item.core.entities.baseXml.LibProfile;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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

}
