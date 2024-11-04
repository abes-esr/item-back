package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.entities.item.IndexRecherche;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class AbstractFichier {

    protected String filename;

    protected Path path;

    protected IndexRecherche indexRecherche;


    /**
     * Méthode de découpage du fichier en lot de taille définie
     *
     * @return un tableau contenant les lots de lignes du fichier
     */
    public List<String> cutFile() throws IOException {
        ArrayList<String> tabResult = new ArrayList<>();
        int cptLigne = 0;
        //variable du nombre de coupes successives dans le fichier
        StringBuilder ligneainserer = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = Utilitaires.checkBom(bufLecteur.readLine())) != null) {
                if (!ligneainserer.toString().contains(ligne)) {
                    if (cptLigne < Constant.MAX_LIGNE_APPELWS) {
                        cptLigne++;
                        //concaténation de la ligne du fichier
                        ligneainserer.append(ligne).append(",");
                    } else {
                        //insertion de la ligne dans la liste sans la dernière ,
                        tabResult.add(ligneainserer.substring(0, ligneainserer.length() - 1));
                        ligneainserer = new StringBuilder(ligne).append(",");
                        cptLigne = 1;
                    }
                }
            }
            tabResult.add(ligneainserer.substring(0, ligneainserer.length() - 1));
        }

        return tabResult;

    }

    protected int getIndexZone(IndexRecherche indexCourant, String[] tabLigne, int indexZone) {
        if (indexCourant.getIndexZones() == 3) {
            //cas date / auteur / titre : on vérifie les 3 premières colonnes dans le fichier Date;Auteur;Titre qui doivent correspondre
            if ((tabLigne.length >= 3) && (tabLigne[0].concat(";").concat(tabLigne[1]).concat(";").concat(tabLigne[2]).equalsIgnoreCase(indexCourant.getLibelle()))) {
                indexZone = indexCourant.getIndexZones();
                this.indexRecherche = indexCourant;
            }
            //Si l'utilisateur n'a pas renseigné d'index de recherche
        } else {
            //autre cas : on ne vérifie que la première colonne
            if (tabLigne[0].equalsIgnoreCase(indexCourant.getLibelle())) {
                indexZone = indexCourant.getIndexZones();
                this.indexRecherche = indexCourant;
            }
            //Si l'utilisateur n'a pas renseigné d'index de recherche
        }
        return indexZone;
    }


    /**
     * Méthode permettant de vérifier que la valeur de la seconde colonne correspond au RCR de la demande
     * @param rcrFichier : ligne du fichier
     * @param rcr : rcr de la demande
     * @throws FileCheckingException : erreur de format de fichier
     */
    protected void checkRcr(String rcrFichier, String rcr, int ligneCourante) throws FileCheckingException {
        if (!rcrFichier.equals(rcr)) {
            throw new FileCheckingException(Constant.ERR_FILE_WRONGCONTENT);
        }
    }

    /**
     * Méthode de vérification de la forme d'un ppn
     * @param ppn ppn à vérifier
     * @throws FileCheckingException : erreur de format de fichier
     */
    protected void checkPpn(String ppn, int ligneCourante) throws FileCheckingException {
        if (!ppn.matches("^(\\d{8}[0-9X])?$")){
            throw new FileCheckingException(Constant.ERR_FILE_WRONGCONTENT);
        }
    }

    /**
     * Méthode de vérification de la forme d'un epn
     * @param epn epn à vérifier
     * @throws FileCheckingException: erreur de format de l'epn
     */
    protected void checkEpn(String epn, int ligneCourante) throws FileCheckingException {
        if (!epn.matches("^(\\d{8}[0-9X])?$")) {
            throw new FileCheckingException(Constant.ERR_FILE_WRONGCONTENT);
        }
    }

}
