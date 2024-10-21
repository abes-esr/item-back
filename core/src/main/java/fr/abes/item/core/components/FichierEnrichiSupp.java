package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class FichierEnrichiSupp extends AbstractFichier implements Fichier {
    private int ligneCourante;

    @Autowired
    public FichierEnrichiSupp(@Value("") final String filename) {
        this.filename = filename;
        this.ligneCourante = 2;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_ACOMPLETER;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_VALIDE_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {
        DemandeSupp demandeSupp = (DemandeSupp) d;
        ligneCourante = 2;
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            String ligne = Utilitaires.checkBom(bufLecteur.readLine());
            check3Cols(ligne);
            while ((ligne = bufLecteur.readLine()) != null) {
                checkBodyLine(ligne, demandeSupp);
                ligneCourante++;
            }
        }
    }

    /**
     * Méthode de vérification de la première partie de la ligne du fichier enrichi.
     * Les trois premières colonnes doivent être : ppn;rcr;epn;
     *
     * @param ligne : ligne à traiter
     * @throws FileCheckingException : erreur dans le format de la ligne
     */
    private void check3Cols(String ligne) throws FileCheckingException {
        if (ligne.split(";").length != 3) {
            throw new FileCheckingException(Constant.ERR_FILE_3COL_SUPP);
        }
        if (ligne.length() < 11) {
            throw new FileCheckingException(Constant.ERR_FILE_3COL_SUPP);
        }
        if (!("ppn;rcr;epn").equalsIgnoreCase(ligne.substring(0, 11))) {
            throw new FileCheckingException(Constant.ERR_FILE_3COL_SUPP);
        }
    }

    /**
     * Méthode de vérification d'une ligne du corps du fichier enrichi
     *
     * @param ligne ligne du fichier à analyser
     * @throws FileCheckingException : erreur de format de la ligne
     */
    private void checkBodyLine(String ligne, DemandeSupp demandeSupp) throws FileCheckingException {
        try {
            // contrôle de la longueur de la ligne
            if (ligne.split(";").length > 3 || StringUtils.countOccurrencesOf(ligne, ";") != 3) {
                throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + " \"" + ligne + "\" " + Constant.ERR_FILE_3COL_SUPP_ANY_LINE);
            }
            String[] tabligne = ligne.split(";");
            // contrôle du ppn
            if (demandeSupp.getTypeSuppression().equals(TYPE_SUPPRESSION.EPN) && tabligne[0] != null) {
                checkPpn(tabligne[0], ligneCourante);
            }
            checkRcr(tabligne[1], demandeSupp.getRcr(), ligneCourante);
            // contrôle de l'epn s'il est renseigné
            if (tabligne.length > 2)
                checkEpn(tabligne[2], ligneCourante);
        } catch (IndexOutOfBoundsException e) {
            throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_LINELENGTH);
        }
    }
}
