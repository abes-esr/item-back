package fr.abes.item.service.impl;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.item.ILigneFichierModifDao;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierModif;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.utilitaire.Utilitaires;
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

    public LigneFichierModifService(ILigneFichierModifDao dao) {
        this.dao = dao;
    }

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
                dao.save(lf);
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
}
