package fr.abes.item.service;

import fr.abes.item.entities.item.Demande;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileTypeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface IDemandeService {
    List<Demande> findAll();

    Demande save(Demande entity);

    Demande findById(Integer id);

    Demande archiverDemande(Demande entity) throws DemandeCheckingException;

    void deleteById(Integer id);

    void initFiles(Demande demande) throws FileTypeException;

    void setIlnShortNameOnDemande(Demande demande);

    Demande previousState(Demande demande) throws DemandeCheckingException, IOException;

    Demande closeDemande(Demande demande) throws DemandeCheckingException;

    List<Demande> getActiveDemandesForUser(String iln);

    Demande getIdNextDemandeToProceed();

    String getInfoHeaderFichierResultat(Demande demande, Date dateDebut);

    String getInfoFooterFichierResultat(Demande demande);

    String getSeparationBetweenBlocks(String demandeBrute);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    List<Demande> getAllArchivedDemandes(String iln);

    List<Demande> getAllArchivedDemandesAllIln();

    List<Demande> getAllActiveDemandesForAdminExtended();

    List<Demande> getAllActiveDemandesForAdmin(String iln);

    Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException, IOException;

}
