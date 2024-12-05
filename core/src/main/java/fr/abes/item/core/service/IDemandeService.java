package fr.abes.item.core.service;

import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface IDemandeService {
    Demande save(Demande entity);

    Demande findById(Integer id);

    Demande creerDemande(String rcr, Integer userNum);

    void modifierShortNameDemande(Demande demande);

    Demande archiverDemande(Demande demande) throws DemandeCheckingException;

    void deleteById(Integer id);

    void initFiles(Demande demande) throws FileTypeException;

    void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException;

    Demande previousState(Demande demande) throws DemandeCheckingException, IOException;

    Demande closeDemande(Demande demande) throws DemandeCheckingException;

    void cleanLignesFichierDemande(Demande demande);

    List<DemandeDto> getActiveDemandesForUser(String iln);

    Demande getIdNextDemandeToProceed(int minHour, int maxHour);

    String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    Demande changeStateCanceled(Demande demande, int etatDemande);

    List<DemandeDto> getAllArchivedDemandes(String iln);

    List<DemandeDto> getAllArchivedDemandesAllIln();

    List<DemandeDto> getAllActiveDemandesForAdminExtended();

    List<DemandeDto> getAllActiveDemandesForAdmin(String iln);

    Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException;

    List<? extends Demande> getDemandesToArchive();
    List<? extends Demande> getDemandesToPlaceInDeletedStatus();
    List<? extends Demande> getDemandesToDelete();

    Demande restaurerDemande(Demande demande) throws DemandeCheckingException;

    void refreshEntity(Demande demande);

}
