package fr.abes.item.components;

import fr.abes.item.constant.Constant;
import fr.abes.item.utilitaire.Utilitaires;
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

public abstract class AbstractFichier {
    @Getter @Setter
    protected String filename;
    @Getter @Setter
    protected Path path;


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

}
