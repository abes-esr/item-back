package fr.abes.item.service.impl;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.service.ILigneFichierModifService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

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
public class LigneFichierModifService implements ILigneFichierModifService {

    @Getter
    @Autowired
    private DaoProvider dao;

    @Override
    @Transactional
    @SuppressWarnings("squid:S3776")
    public void saveFile(File file, Demande demande){
        DemandeModif demandeModif = (DemandeModif) demande;
        BufferedReader reader = null;

        try {
            reader = ReaderFactory.createBufferedReader(file);

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
                dao.getLigneFichierModif().save(lf);
            }
        } catch (IOException e){
            log.error(e.getMessage());
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Integer numDemande) {
        List<LigneFichierModif> ligneFichierModifs = getDao().getLigneFichierModif().getLigneFichierbyDemande(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierModifs);
        return ligneFichiers;
    }

    @Override
    public LigneFichierModif findById(Integer id) {
        return getDao().getLigneFichierModif().findById(id).get();
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichier;
        return getDao().getLigneFichierModif().save(ligneFichierModif);
    }

    @Override
    public int getNbLigneFichierTraiteeByDemande(int numDemande) {
        return getDao().getLigneFichierModif().getNbLigneFichierTraitee(numDemande);
    }

    @Override
    public List<LigneFichier> getLigneFichierTraitee(Integer numDemande) {
        List<LigneFichierModif> ligneFichierModifs = getDao().getLigneFichierModif().getLigneFichierTraitee(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierModifs);
        return ligneFichiers;
    }

    @Override
    public int getNbLigneFichierSuccessByDemande(int numDemande) {
        return getDao().getLigneFichierModif().getNbLigneFichierSuccessByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierErrorByDemande(int numDemande) {
        return getDao().getLigneFichierModif().getNbLigneFichierErrorByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierTotalByDemande(int numDemande) {
        return getDao().getLigneFichierModif().getNbLigneFichierTotal(numDemande);
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        getDao().getLigneFichierModif().deleteByDemandeModif((DemandeModif) demande);
    }
}
