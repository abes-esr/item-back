package fr.abes.item.service.impl;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.*;
import fr.abes.item.service.ILigneFichierExempService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LigneFichierExempService implements ILigneFichierExempService {

    @Getter
    @Autowired
    private DaoProvider dao;

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierExemp ligneFichierExemp = (LigneFichierExemp) ligneFichier;
        return getDao().getLigneFichierExemp().save(ligneFichierExemp);
    }

    @Override
    public LigneFichierExemp findById(Integer id) {
        return getDao().getLigneFichierExemp().findById(id).get();
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
                String indexRecherche = "";
                String valeur = "";
                if (line.lastIndexOf(';') == line.length() - 1) {
                    line += (char)0;
                }
                String [] tabLine = line.split(";");
                for (int i = 0; i < demandeExemp.getIndexRecherche().getIndexZones();i++) {
                    indexRecherche += tabLine[i] + ";";
                }
                //suppression du ; final
                indexRecherche = indexRecherche.substring(0, indexRecherche.length()-1);
                for (int i= demandeExemp.getIndexRecherche().getIndexZones(); i<tabLine.length;i++) {
                    valeur += tabLine[i] + ";";
                }
                //suppression du ; final
                valeur = valeur.substring(0,valeur.length()-1);
                //création de la ligne fichier et enregistrement
                LigneFichierExemp ligneFichierExemp = new LigneFichierExemp(indexRecherche, valeur, 0, position++, "", null, demandeExemp, null);
                getDao().getLigneFichierExemp().save(ligneFichierExemp);
            }
        } catch (
                IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Integer numDemande) {
        List<LigneFichierExemp> ligneFichierExemps = getDao().getLigneFichierExemp().getLigneFichierbyDemande(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierExemps);
        return ligneFichiers;
    }

    @Override
    public int getNbLigneFichierTraiteeByDemande(int numDemande) {
        return getDao().getLigneFichierExemp().getNbLigneFichierTraitee(numDemande);
    }


    @Override
    public List<LigneFichier> getLigneFichierTraitee(Integer numDemande) {
        List<LigneFichierExemp> ligneFichierExemps = getDao().getLigneFichierExemp().getLigneFichierTraitee(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierExemps);
        return ligneFichiers;
    }

    @Override
    public int getNbLigneFichierSuccessByDemande(int numDemande) {
        return getDao().getLigneFichierExemp().getNbLigneFichierSuccessByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierErrorByDemande(int numDemande) {
        return getDao().getLigneFichierExemp().getNbLigneFichierErrorByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierTotalByDemande(int numDemande) {
        return getDao().getLigneFichierExemp().getNbLigneFichierTotalByDemande(numDemande);
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        getDao().getLigneFichierExemp().deleteByDemandeExemp((DemandeExemp) demande);
    }
}
