package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.IndexRecherche;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.repository.item.IIndexRechercheDao;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;


@ToString
@Slf4j
@Component
@Getter
public class FichierEnrichiRecouv extends AbstractFichier implements Fichier {
    private final IIndexRechercheDao indexRechercheDao;

    @Getter @Setter
    private DemandeRecouv demande;

    private int ligneCourante;
    private int indice;


    public FichierEnrichiRecouv(@Value("") final String filename, IIndexRechercheDao indexRechercheDao) {
        this.indexRechercheDao = indexRechercheDao;
        this.filename = filename;
        this.ligneCourante = 2;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_PREPARATION;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.RECOUV;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_ENRICHI_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    @Override
    public void checkFileContent(Demande demandeRecouv) throws FileCheckingException, IOException {
        this.demande = (DemandeRecouv) demandeRecouv;
        ligneCourante = 2;
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne = Utilitaires.checkBom(bufLecteur.readLine());
            //vérification de l'index de recherche
            checkFirstColumn(ligne);

            //une fois qu'on a vérifié l'index de recherche, on l'exclut de la chaine de caractère pour la vérification suivante
            String[] tabLigne = ligne.split(";"); //Un tableau contenant l'index de recherche
            StringBuilder newLine = new StringBuilder(); //Une chaine qui contient les zones et les sous zones

            /*
            indiceZone correspond au pas que l'on fait sur la première ligne du fichier pour analyser la valeur de
            la première zone soit pour Date;Auteur;Titre -> un indice[3] pour analyser la valeur du 4ème emplacement du fichier correspondant à la première zone
            pour les autres, soit PPN, ISBN, etc -> un indice[1] pour analyser la valeur du 2ème emplacement du fichier correspondant à la deuxième zone
            indiceZone est une donnée présente dans la table INDEX_RECHERCHE
             */
            for (int i = indice; i < tabLigne.length; i++) {
                newLine.append(tabLigne[i]).append(";"); //Permet de reconstituer la première ligne complète avec uniquement les zones et sous zones en excluant l'index recherche dans la variable newline
            }

            while ((ligne = bufLecteur.readLine()) != null) { //Tant qu'il y a des lignes à lire dans le fichier
                this.checkAnormalLineOfExemplary(ligneCourante, ligne); //Détecte une ligne de données vide
                //Supprime les éventuels ; que l'utilisateur aurait pu rajouter à la fin des lignes
                ligne = Utilitaires.removeSemicolonFromEndOfLine(ligne);
                this.checkBodyLine(ligne);
                ligneCourante++;
            }

            //cas où il n'y a que la ligne d'en-tête, lance une erreur (absence des données liées au zones et sous zones)
            if (ligneCourante == 2) {
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_NOREQUESTS);
            }

            //cas ou le nombre de lignes du fichier dépassent la limite autorisée
            if ((ligneCourante - 1) > Constant.MAX_LIGNE_FICHIER_INIT_EXEMP) {
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_TOOMUCH_EXEMP);
            }
        }
    }

    private void checkFirstColumn(String ligne) throws FileCheckingException {
        this.indice = checkIndexRecherche(ligne); //Affectation de la taille de l'indice de la ligne d'entête
        if (this.indice == 0) {
            throw new FileCheckingException(1, Constant.ERR_FILE_INDEXINCONNU);
        }
    }

    /**
     * méthode de vérification de la première colonne de l'en tête
     *
     * @param indexLigne : Fragment de la ligne d'en tête contenant le ou les index de recherche
     * @return l'indice de départ des zones à créer en fonction de l'index trouvé, 0 si index non trouvé
     */
    public Integer checkIndexRecherche(String indexLigne) throws FileCheckingException {
        int indexZone = 0;
        String[] tabLigne = indexLigne.split(";");
        //Si l'utilisateur n'a pas renseigné d'index de recherche
        if (tabLigne[0].isEmpty() || tabLigne[0].equalsIgnoreCase(" ")) {
            throw new FileCheckingException(1, Constant.ERR_FILE_NOINDEX);
        }
        //on récupère la liste des index possibles dans la BDD : DAT, ISBN, ISSN, PPN, SOU
        List<IndexRecherche> index = indexRechercheDao.findAll();

        for (IndexRecherche indexCourant : index) {
            //en fonction de l'index, le nombre de zone à examiner change
            indexZone = getIndexZone(indexCourant, tabLigne, indexZone);
        }
        /*A la fin on a un indexZone de :
            3 pour Date;Auteur;Titre, 1 pour les Autres
            si indexZone vaut 0 c'est que les equalsIgnoreCase ont échoué et que la 1ere colonne de la première
            ligne du fichier de l'utilisateur ne correspond pas à une LIBELLE d'index de la table INDEX_RECHERCHE
            présente en base
         */
        return indexZone;
    }

    /**
     * Méthode de vérification de chaque ligne du fichier
     *
     * @param ligne ligne du fichier à analyser
     */
    public void checkBodyLine(String ligne) throws FileCheckingException {
        String[] tabLigne = ligne.split(";");

        /*controle que la taille du tableau correspondant à une ligne de données splitée correspond au nombre exact
        de zone de l'entête du fichier*/
        if (tabLigne.length != indice) {
            if(tabLigne.length == 2 && indice == 3){
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_DATEAUTEURTITRE_TITREMANQUANT);
            }
            throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_WRONGNBCOLUMNS);
        }

        //analyse de la valeur de la date dans le cas d'une recherche date;auteur;titre
        if ((("DAT").equals(this.indexRecherche.getCode())) && (!tabLigne[0].matches(Constant.REG_EXP_DATE_A_4_DECIMALES))) { //Si la date de la ligne en cours n'est pas sur 4 chiffres
            throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_DATENOK);
        }
    }

    /**Methode de vérification d'une ligne qui pourrait être anormale : si l'utilisateur à rentré des espaces
     * vides dans des cellules d'une ligne excel, sans aucune donnée
     * @param lignedExemplaire la ligne d'exemplaire à analyser
     * @throws FileCheckingException une ligne anormale à été détectée, l'utilisateur doit revoir son fichier
     */
    private void checkAnormalLineOfExemplary(Integer ligneCourante, String lignedExemplaire) throws FileCheckingException {
        if (Utilitaires.detectAnormalLine(lignedExemplaire)){
            throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_LIGNE_ANORNALE);
        }
    }
}
