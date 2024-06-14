package fr.abes.item.core.service.impl;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierExemp;
import fr.abes.item.core.repository.item.ILigneFichierExempDao;
import fr.abes.item.core.service.ILigneFichierService;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Strategy(type= ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.EXEMP})
@Service
public class LigneFichierExempService implements ILigneFichierService {
    private final ILigneFichierExempDao dao;

    public LigneFichierExempService(ILigneFichierExempDao dao) {
        this.dao = dao;
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
        } catch (
                IOException e) {
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
}
