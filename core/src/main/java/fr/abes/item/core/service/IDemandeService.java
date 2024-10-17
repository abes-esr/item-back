package fr.abes.item.core.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
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

    List<Demande> getActiveDemandesForUser(String iln);

    Demande getIdNextDemandeToProceed(int minHour, int maxHour);

    String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    Demande changeStateCanceled(Demande demande, int etatDemande);

    List<Demande> getAllArchivedDemandes(String iln);

    List<Demande> getAllArchivedDemandesAllIln();

    List<Demande> getAllActiveDemandesForAdminExtended();

    List<Demande> getAllActiveDemandesForAdmin(String iln);

    Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException;

    String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, ZoneException, IOException;

    List<? extends Demande> getDemandesToArchive();
    List<? extends Demande> getDemandesToPlaceInDeletedStatus();
    List<? extends Demande> getDemandesToDelete();

    String getQueryToSudoc(String code, Integer type, String[] valeurs) throws QueryToSudocException;
}
