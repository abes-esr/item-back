package fr.abes.item.core.components;

import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.exception.FileCheckingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test pour FichierEnrichi Suppression")
public class TestFichierEnrichiSupp {
    @DisplayName("checkNok3Cols")
    @Test
    void checkNok3Cols() {
        DemandeSupp demandeSupp = new DemandeSupp("341720001", new Date(), new Date(), TYPE_SUPPRESSION.EPN, "", new EtatDemande(1), new Utilisateur(1));
        FichierEnrichiSupp fic = new FichierEnrichiSupp("Nok3Cols.csv");
        fic.setPath(Paths.get("src/test/resources/fichierEnrichiSupp"));

        assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(demandeSupp)).getMessage().contains("La première ligne du fichier doit contenir 3 colonnes (ppn;rcr;epn)"))
                .isTrue();
    }

    @DisplayName("checkOk3Cols")
    @Test
    void checkOk3Cols() {}

    @DisplayName("checkPpnNonOk")
    @Test
    void checkPpnNonOk() {}

    @DisplayName("checkRcrNonOk")
    @Test
    void checkRcrNonOk() {}

    @DisplayName("checkEpnVide")
    @Test
    void checkEpnVide() {}

    @DisplayName("checkEpnNonVideOk")
    @Test
    void checkEpnNonVideOk() {}

    @DisplayName("checkEpnNonVideNonOk")
    @Test
    void checkEpnNonVideNonOk() {}
}
