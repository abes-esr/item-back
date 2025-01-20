package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.repository.item.ISousZonesAutoriseesDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ToString
@Slf4j
@Getter @Setter
public class FichierEnrichiExemp extends AbstractFichier implements Fichier {
    private final ISousZonesAutoriseesDao sousZonesAutoriseesDao;

    private final IZonesAutoriseesDao zonesAutoriseesDao;

    private final ReferenceService referenceService;

    private int indiceZone;

    private int nbColonnes;

    //compteur indiquant sur quelle ligne du fichier on se trouve, utilisé pour les messages d'erreur
    private int ligneCourantePositionNumber;

    private String zoneCourante;


    private String valeurZones;

    private DemandeExemp demande;

    public FichierEnrichiExemp(@Value("") final String filename, ISousZonesAutoriseesDao sousZonesAutoriseesDao, IZonesAutoriseesDao zonesAutoriseesDao, ReferenceService referenceService) {
        this.sousZonesAutoriseesDao = sousZonesAutoriseesDao;
        this.zonesAutoriseesDao = zonesAutoriseesDao;
        this.referenceService = referenceService;
        this.filename = filename;
        this.ligneCourantePositionNumber = 2;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_ACOMPLETER;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.EXEMP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_ENRICHI_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    /**
     * Méthode IMPORTANTE permettant de vérifier que le contenu du fichier correspond aux
     * spécifications (Controle uniquement, pas d'enrichissement de données)
     *
     * @throws FileCheckingException : {@link IOException}
     * @throws IOException : erreur lecture fichier
     */
    @Override
    public void checkFileContent(Demande demandeExemp) throws FileCheckingException, IOException {
        this.demande = (DemandeExemp) demandeExemp;
        this.nbColonnes = 0;
        ligneCourantePositionNumber = 2;
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            String ligne = Utilitaires.checkBom(bufLecteur.readLine());
            //vérification de l'index de recherche
            checkFirstColumn(ligne, demande.getTypeExemp());

            //une fois qu'on a vérifié l'index de recherche, on l'exclut de la chaine de caractère pour la vérification suivante
            String[] tabLigne = ligne.split(";");
            nbColonnes = StringUtils.countMatches(ligne, ";") + 1;
            StringBuilder newLine = new StringBuilder();

            /*
            indiceZone correspond au pas que l'on fait sur la première ligne du fichier pour analyser la valeur de
            la première zone soit pour Date;Auteur;Titre -> un indice[3] pour analyser la valeur du 4ème emplacement du fichier correspondant à la première zone
            pour les autres, soit PPN, ISBN, etc -> un indice[1] pour analyser la valeur du 2ème emplacement du fichier correspondant à la deuxième zone
            indiceZone est une donnée présente dans la table INDEX_RECHERCHE
             */
            for (int i = indiceZone; i < tabLigne.length; i++) {
                newLine.append(tabLigne[i]).append(";"); //Permet de reconstituer la première ligne complète avec uniquement les zones et sous zones en excluant l'index recherche dans la variable newline, afin de l'anaylser un peu plus bas
            }

            this.valeurZones = newLine.toString(); //ENTETE Reconstitution de l'entête sans l'index de recherche (PPN, ISBN, etc) démarre directement avec la zone et sous zone
            this.checkZones(newLine.toString());//ENTETE vérification des zones saisies par l'utilisateur
            this.checkMandatoryZones(newLine.toString(), demande.getTypeExemp());
            while ((ligne = bufLecteur.readLine()) != null) { //LIGNES EXEMPLAIRES Tant qu'il y a des lignes à lire dans le fichier
                Utilitaires.isValidUtf8(ligne);
                this.checkAnormalLineOfExemplary(ligneCourantePositionNumber, ligne); //Détecte une ligne de données vide
                checkBodyLine(ligne); //controle adequation taille entete taille ligne exemplaire, controle champ vide, controle format de la date pour un index en Date | Auteur | Titre
                ligneCourantePositionNumber++; //pointeur sur ligne en cours d'analyse dans fichier
            }

            //cas où il n'y a que la ligne d'en-tête, lance une erreur (absence des données liées au zones et sous zones)
            if (ligneCourantePositionNumber == 2) {
                throw new FileCheckingException(ligneCourantePositionNumber, Constant.ERR_FILE_NOREQUESTS);
            }

            //cas ou le nombre de lignes du fichier dépassent la limite autorisée
            if ((ligneCourantePositionNumber - 1) > Constant.MAX_LIGNE_FICHIER_INIT_EXEMP) {
                throw new FileCheckingException(ligneCourantePositionNumber, Constant.ERR_FILE_TOOMUCH_EXEMP);
            }
        }
    }

    private void checkMandatoryZones(String entete, TypeExemp typeExemp) throws FileCheckingException {
        List<SousZonesAutorisees> mandatoryZones = sousZonesAutoriseesDao.getSousZonesAutoriseesMandatory(Optional.ofNullable(typeExemp.getId()));
        for (SousZonesAutorisees ssZone : mandatoryZones) {
            Pattern patternZoneSousZones = Pattern.compile(Constant.REG_EXP_ZONES_SOUS_ZONES);
            boolean trouve = isTrouve(entete, ssZone, patternZoneSousZones);
            if (!trouve) {
                throw new FileCheckingException(Constant.ERR_FILE_MANDATORY_ZONE_MISSING + ssZone.getZone().getLabelZone() + ssZone.getLibelle());
            }
        }
    }

    private static boolean isTrouve(String entete, SousZonesAutorisees ssZone, Pattern patternZoneSousZones) {
        Matcher matcher = patternZoneSousZones.matcher(entete);
        boolean trouve = false;
        while (matcher.find()) {
            if (matcher.group("zone") != null) {
                if (matcher.group("zone").equals(ssZone.getZone().getLabelZone())) {
                    for (int i = 1;i<=10;i++) {
                        String sousZone = matcher.group("sousZone" + i);
                        if (sousZone != null && sousZone.equals(ssZone.getLibelle())) {
                            trouve = true;
                            break;
                        }
                    }
                }
            }
        }
        return trouve;
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

    /**
     * Methode de vérification des premières colonnes de la première ligne du fichier
     * indiceZone correspond à un numéro allant de 1 à 5, correspondant a l'association NUM_INDEX_RECHERCHE / LIBELLE de la table INDEX RECHERCHE
     * checkindexRecherche retourne justement le bon numéro de 1 à 5 en fonction du type d'index de recherche : ISBN, ISSN, PPN, Date;Auteur;Titre, Numéro Source.
     * en analysant le premier emplacement du fichier uploadé
     * <p>
     * si l'indice Zone est egal à 0 c'est que le premier emplacement ne correspond pas à un index de recherche référencé en base.
     *
     * @throws FileCheckingException : erreur dans le format de fichier
     */
    private void checkFirstColumn(String ligne, TypeExemp type) throws FileCheckingException {
        this.indiceZone = checkIndexRecherche(ligne, type);
        if (this.indiceZone == 0) {
            throw new FileCheckingException(1, Constant.ERR_FILE_INDEXINCONNU);
        }
    }

    /**
     * méthode de vérification de la première colonne de l'en tête
     *
     * @param indexLigne : Fragment de la ligne d'en tête contenant le ou les index de recherche
     * @return l'indice de départ des zones à créer en fonction de l'index trouvé, 0 si index non trouvé
     */
    public Integer checkIndexRecherche(String indexLigne, TypeExemp type) throws FileCheckingException {
        int indexZone = 0;
        String[] tabLigne = indexLigne.split(";");

        //on récupère la liste des index possibles dans la BDD en fonction du type d'exemplarisation choisi
        Set<IndexRecherche> index = referenceService.getIndexRechercheFromTypeExemp(type.getId());

        for (IndexRecherche indexCourant : index) {
            //en fonction de l'index, le nombre de zone à examiner change
            indexZone = getIndexZone(indexCourant, tabLigne, indexZone);
            if (tabLigne[0].isEmpty() || tabLigne[0].equalsIgnoreCase(" ")) {
                throw new FileCheckingException(1, Constant.ERR_FILE_NOINDEX);
            }
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
     * Méthode de vérification des indicesZones+n colonnes
     *
     * @param listeZones : liste des zones de la ligne d'entête du fichier
     * @throws FileCheckingException : erreur dans le format du fichier
     */
    public void checkZones(String listeZones) throws FileCheckingException {
        int nbSousZones = 0;
        Pattern patternZoneSousZones = Pattern.compile(Constant.REG_EXP_ZONES_SOUS_ZONES);
        Matcher matcher = patternZoneSousZones.matcher(listeZones);
        while (matcher.find()) {
            //bloc de vérification d'une zone trouvée
            if (matcher.group("zone") != null) {
                nbSousZones = 0;
                zoneCourante = matcher.group("zone");
                List<String> allowedZones = zonesAutoriseesDao.getZonesByTypeExemp(demande.getTypeExemp().getId());

                //contrôle si la zone est bien autorisée,
                //la liste de zones autorisées varie selon le type d'exemplarisation
                checkZone(allowedZones, zoneCourante);
            }
            //vérification des sous zones de la zone
            nbSousZones = checkSousZones(nbSousZones, matcher);
            if (nbSousZones == 0) {
                throw new FileCheckingException(1, Constant.ERR_FILE_ZONEINCOMPLETE);
            }
        }
        if (nbColonnes == 0) {
            throw new FileCheckingException(1, Constant.ERR_FILE_NOZONE);
        }
        if (!matcher.lookingAt()) {
            throw new FileCheckingException(1, Constant.ERR_FILE_CARACTERES);
        }
    }

    private int checkSousZones(int nbSousZones, Matcher matcher) throws FileCheckingException {
        String sousZoneCourante;
        List<String> allowedSousZone = sousZonesAutoriseesDao.getSousZonesAutoriseesByZone(zoneCourante);
        for (int i = 1; i <= 10; i++) {
            if (matcher.group("sousZone" + i) != null) {
                nbSousZones++;
                sousZoneCourante = matcher.group("sousZone" + i);
                if (!allowedSousZone.contains(sousZoneCourante)) {
                    throw new FileCheckingException(1, Constant.ERR_FILE_SOUSZONENONAUTORISEE + sousZoneCourante + " pour la zone " + zoneCourante);
                }
            }
        }
        return nbSousZones;
    }

    private void checkZone(List<String> allowedZones, String zone) throws FileCheckingException {
        if (!allowedZones.contains(zone))
            throw new FileCheckingException(1, Constant.ERR_FILE_ZONENONAUTORISEE + zone);
    }

    /**
     * Méthode de vérification de chaque ligne du fichier
     *
     * @param ligne ligne du fichier à analyser
     */
    public void checkBodyLine(String ligne) throws FileCheckingException {
        String[] tabLigne = ligne.split(";");

        if (Utilitaires.detectsANumberOfDataDifferentFromTheNumberOfHeaderDataOnALine(ligne, nbColonnes)) {
            throw new FileCheckingException(ligneCourantePositionNumber, Constant.ERR_FILE_WRONGNBCOLUMNS);
        }

        List<String> listeChamps = new ArrayList<>(Arrays.asList(tabLigne));

        //analyse de la valeur de la date dans le cas d'une recherche date;auteur;titre
        if ((("DAT").equals(this.indexRecherche.getCode())) && (!listeChamps.get(0).matches("\\d{4}"))) {
            throw new FileCheckingException(ligneCourantePositionNumber, Constant.ERR_FILE_DATENOK);
        }

        if ((("PPN").equals(this.indexRecherche.getCode())) && (!listeChamps.get(0).matches(Constant.PATTERN_INDEX_PPN))) {
            throw new FileCheckingException(ligneCourantePositionNumber, Constant.ERR_FILE_WRONGPPN);
        }
    }
}
