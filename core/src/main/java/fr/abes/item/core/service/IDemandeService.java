package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileTypeException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface IDemandeService {
    Demande save(Demande entity);

    Demande findById(Integer id);

    Demande archiverDemande(Demande demande) throws DemandeCheckingException;

    void deleteById(Integer id);

    void initFiles(Demande demande) throws FileTypeException;

    Demande previousState(Demande demande) throws DemandeCheckingException, IOException;

    Demande closeDemande(Demande demande) throws DemandeCheckingException;

    List<Demande> getActiveDemandesForUser(String iln);

    Demande getIdNextDemandeToProceed(int minHour, int maxHour);

    String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    List<Demande> getAllArchivedDemandes(String iln);

    List<Demande> getAllArchivedDemandesAllIln();

    List<Demande> getAllActiveDemandesForAdminExtended();

    List<Demande> getAllActiveDemandesForAdmin(String iln);

    Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException, IOException;

}
