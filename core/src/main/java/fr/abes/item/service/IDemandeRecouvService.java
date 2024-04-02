package fr.abes.item.service;

import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.exception.QueryToSudocException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface IDemandeRecouvService extends IDemandeService {
    String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException;

    int launchQueryToSudoc(String codeIndex, String valeurs) throws QueryToSudocException, IOException;

    String getQueryToSudoc(String codeIndex, String[] tabValeurs) throws QueryToSudocException;

    DemandeRecouv creerDemande(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur);

    Demande changeStateCanceled(Demande demande, int etatDemande);

    Demande getIdNextDemandeToClean();

    List<DemandeRecouv> getListDemandesToClean();

    List<DemandeRecouv> getIdNextDemandeToArchive();
    List<DemandeRecouv> getIdNextDemandeToPlaceInDeletedStatus();
    List<DemandeRecouv> getIdNextDemandeToDelete();
}
