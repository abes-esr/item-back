package fr.abes.item.service;

import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.entities.item.LigneFichierExemp;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.exception.QueryToSudocException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface IDemandeService {
    List<Demande> findAll();

    Demande save(Demande entity);

    Demande findById(Integer id);

    Demande archiverDemande(Demande demande) throws DemandeCheckingException;

    void deleteById(Integer id);

    void initFiles(Demande demande) throws FileTypeException;

    Demande previousState(Demande demande) throws DemandeCheckingException, IOException;

    Demande closeDemande(Demande demande) throws DemandeCheckingException;

    List<Demande> getActiveDemandesForUser(String iln);

    Demande getIdNextDemandeToProceed(int minHour, int maxHour);

    String getInfoHeaderFichierResultat(Demande demande, Date dateDebut);


    String getSeparationBetweenBlocks(String demandeBrute);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    List<Demande> getAllArchivedDemandes(String iln);

    List<Demande> getAllArchivedDemandesAllIln();

    List<Demande> getAllActiveDemandesForAdminExtended();

    List<Demande> getAllActiveDemandesForAdmin(String iln);

    Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException, IOException;

}
