package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.exception.FileCheckingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test pour FichierEnrichi Suppression")
public class TestFichierEnrichiSupp {
    @DisplayName("checkNok3Cols")
    @Test
    void checkNok3Cols() {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkNok3Cols.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));

        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains("La première ligne du fichier doit contenir 3 colonnes (ppn;rcr;epn)"));
    }

    @DisplayName("checkOk3Cols")
    @Test
    void checkOk3Cols() throws IOException, FileCheckingException {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkOk3Cols.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        fic.checkFileContent(demandeSupp);
    }

    @DisplayName("checkPpnNonOk")
    @Test
    void checkPpnNonOk() {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkPpnNonOk.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains(Constant.ERR_FILE_WRONGPPN));
    }

    @DisplayName("checkRcrNonOk")
    @Test
    void checkRcrNonOk() {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkRcrNonOk.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains(Constant.ERR_FILE_WRONGRCR));
    }

    @DisplayName("checkEpnVide")
    @Test
    void checkEpnVide() throws IOException, FileCheckingException {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkEpnVide.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        fic.checkFileContent(demandeSupp);
    }

    @DisplayName("checkEpnNonVideOk")
    @Test
    void checkEpnNonVideOk() throws IOException, FileCheckingException {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkEpnNonVideOk.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        fic.checkFileContent(demandeSupp);
    }

    @DisplayName("checkEpnNonVideNonOk")
    @Test
    void checkEpnNonVideNonOk() {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkEpnNonVideNonOk.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains(Constant.ERR_FILE_WRONGEPN));
    }

    @DisplayName("checkRcrDiffDemande")
    @Test
    void checkRcrDiffDemande() {
        DemandeSupp demandeSupp = new DemandeSupp("341725201", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("checkRcrDiffDemande.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains(Constant.ERR_FILE_WRONGRCR));
    }
}
