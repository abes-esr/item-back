package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class FichierEnrichiModif extends AbstractFichier implements Fichier {
    private int ligneCourante;
    private final String regex = "[9LE]\\d\\d\\$[a-z0-9]";

    @Autowired
    public FichierEnrichiModif(@Value("") final String filename) {
        this.filename = filename;
        this.ligneCourante=2;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_ACOMPLETER;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.MODIF; }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_ENRICHI_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    /**
     * Méthode permettant de vérifier que le contenu du fichier correspond aux
     * spécifications
     *
     * @throws FileCheckingException : erreur dans le format du fichier
     */
    @Override
    public void checkFileContent(Demande demande) throws FileCheckingException, IOException {
        DemandeModif demandeModif = (DemandeModif)demande;
        ligneCourante = 2;
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            if (demandeModif.getTraitement() == null) {
                throw new FileCheckingException(Constant.ERR_FILE_NOTRAIT);
            }
            String ligne = Utilitaires.checkBom(bufLecteur.readLine());
            check3Cols(ligne, 4, Constant.ERR_FILE_3COL_MODIF);
            String tagSubTag = ligne.split(";")[3];
            if (tagSubTag.matches("e\\d{2}\\$a")) {
                throw new FileCheckingException(Constant.ERR_FILE_4COLZONE + tagSubTag);
            }
            if (tagSubTag.startsWith("E")) {
                checkSubfieldCol4ZoneE(tagSubTag, demandeModif.getTraitement().getNomMethode());
            }
            else {
                if (!tagSubTag.startsWith("L")) {
                    checkSubfieldCol4(tagSubTag, demandeModif.getTraitement().getNomMethode());
                }
            }

            while ((ligne = bufLecteur.readLine()) != null) {
                checkBodyLine(ligne, demandeModif);
                ligneCourante++;
            }
            // cas ou il n'y a que la ligne d'en-tête
            if (ligneCourante == 1) {
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_NOREQUESTS);
            }

        }

    }

    /**
     * vérification de la validité de la zone de la quatrième colonne
     * sur la première ligne du fichier enrichi
     *
     * @param subfield zone présente dans la quatrième colonne du fichier
     * @throws FileCheckingException : erreur de format de la zone
     */
    private void checkSubfieldCol4(String subfield, String traitement) throws FileCheckingException {
        String regexSupp = "[9LE]\\d\\d";
        if (traitement.equals("supprimerZone") && (!subfield.matches(regexSupp))){
            throw new FileCheckingException(Constant.ERR_FILE_HEAD4TH);
        }
        if (!traitement.equals("supprimerZone") && (!subfield.matches(regex))){
            throw new FileCheckingException(Constant.ERR_FILE_HEAD4TH);
        }

        if (("930$b").equals(subfield)) {
            throw new FileCheckingException(Constant.ERR_FILE_4COLZONE + "930$b");
        }
        if (subfield.startsWith("955") || subfield.startsWith("956") || subfield.startsWith("957") || subfield.startsWith("959")) {
            throw new FileCheckingException(Constant.ERR_FILE_4COLZONE + subfield.substring(0,3));
        }
    }

    private void checkSubfieldCol4ZoneE(String subfield, String traitement) throws FileCheckingException {
        if (subfield.substring(1).matches(regex)) {
                throw new FileCheckingException(Constant.ERR_FILE_HEAD4TH);
        }
        //TODO : Revoir condition et ajouter TU parce que ça marche pas
        if (traitement.equals("creerNouvelleZone") && ("E856").contains(subfield) || ("E702").contains(subfield) || ("E712").contains(subfield)){
                throw new FileCheckingException(Constant.ERR_FILE_4COLZONE + subfield.substring(0, 4));
        }
    }

    /**
     * Méthode de vérification d'une ligne du corps du fichier enrichi
     *
     * @param ligne ligne du fichier à analyser
     * @throws FileCheckingException : erreur de format de la ligne
     */
    private void checkBodyLine(String ligne, DemandeModif demandeModif) throws FileCheckingException {
        if (ligne.length() < 13) {
            throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante
                    + Constant.ERR_FILE_LINELENGTH);
        }
        try {
            String[] tabligne = ligne.split(";");
            checkRcr(tabligne[1], demandeModif.getRcr(), ligneCourante);
            checkPpn(tabligne[0], ligneCourante);
            checkEpn(tabligne[2], ligneCourante);
            check4cols(tabligne, demandeModif.getTraitement().getNomMethode());
        }catch (IndexOutOfBoundsException e) {
            throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_LINELENGTH);
        }
    }


    /**
     * Méthode permettant de vérifier la valeur de la 4è colonne en fonction du traitement
     *
     * @param ligne : ligne du fichier
     * @param traitement : traitement
     * @throws FileCheckingException : erreur sur la colonne
     */
    private void check4cols(String[] ligne, String traitement) throws FileCheckingException {
        switch (traitement) {
            case "creerNouvelleZone":
            case "ajoutSousZone":
                if (ligne.length != 4) {
                    throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_4COLNONVIDE);
                }
                else {
                    if (ligne[3].contains("$")) {
                        throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_DOLLARFORBID);
                    }
                }
                break;
            case "remplacerSousZone":
                if (ligne.length != 4) {
                    throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_4COLNONVIDE);
                }
                break;
            case "supprimerSousZone":
            case "supprimerZone":
                if (ligne.length != 3) {
                    throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_4COLVIDE);
                }
                break;
            default:
        }
    }

    public String getTagSubtag() throws IOException {
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne = bufLecteur.readLine();
            //on renvoie la 4è colonne de l'en tête et on supprime les espaces éventuels entre la zone et la sous zone
            return ligne.split(";")[3].replace(" ", "");
        }
    }

}
