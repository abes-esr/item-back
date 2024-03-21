package fr.abes.item.utilitaire;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.cbs.utilitaire.Utilitaire;
import fr.abes.item.constant.Constant;
import fr.abes.item.exception.FileCheckingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utilitaires {

    private Utilitaires() {
        //nothing to initialize
    }

    public static Multimap<String, String> parseJson(String input) throws IOException {
        //la correspondance pouvant retourner plusieurs fois un ppn, on crée une multimap pour récupérer le résultat
        Multimap<String, String> resMap = ArrayListMultimap.create();
        //parse de l'input json
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode sudocnode = objectMapper.readTree(input);
        JsonNode resultnode = sudocnode.findParent("result");
        if (null != resultnode) {
            Iterator<JsonNode> elements = resultnode.elements();
            while (elements.hasNext()) {
                JsonNode liste = elements.next();
                Iterator<JsonNode> ppnepn = liste.elements();
                while (ppnepn.hasNext()) {
                    JsonNode record;
                    JsonNode ppn;
                    JsonNode epn;
                    //si on a plusieurs valeurs dans le json elles sont sous forme de tableau
                    if (ppnepn.getClass().toString().contains("ArrayList")) {
                        record = ppnepn.next();
                        resMap.put(record.path("ppn").asText(), record.path("epn").asText());
                    } else {
                        //cas avec une seule valeur, on injecte l'unique valeur dans la map et on sort
                        resMap.put(liste.path("ppn").asText(), liste.path("epn").asText());
                        return resMap;
                    }
                }
            }
        }
        return resMap;
    }

    /**
     * Méthode de vérification de l'extension du fichier
     * doit être .txt
     */
    public static void checkExtension(String filename) throws FileCheckingException {
        String extension = filename.substring(filename.length() - 4);
        if (!((".txt").equals(extension) || (".csv").equals(extension))) {
            throw new FileCheckingException(Constant.ERR_FILE_FORMAT);
        }
    }

    public static String checkBom(String chaine) {
        if (chaine != null && !("").equals(chaine)) {
            if (chaine.codePointAt(0) == 0xfeff) {
                return chaine.substring(1);
            }
            return chaine;
        }
        return null;
    }

    /**
     * Récupère le numéro d'un exemplaire dans une notice d'exemplaire
     *
     * @param exemp ensemble des exemplaires de la notice
     * @return numéro d'exemplaire dans la notice correspondant à l'epn (format xx sans le e)
     */
    public static String getNumExFromExemp(String exemp) {
        return exemp.substring(1, 3);
    }

    /**
     * Recupère un exemplaire d'une notice correspondant à un epn
     *
     * @param notice notice biblio + ensemble des exemplaires
     * @param epn    epn de l'exemplaire à retrouver
     * @return exemplaire trouvé ou rien si inconnu
     */
    public static String getExempFromNotice(String notice, String epn) {
        String[] listExemps = notice.split(Constants.STR_1E + Constants.VTXTE);
        for (int i = 0; i < listExemps.length; i++) {
            if (listExemps[i].contains("A99 " + epn)) {
                //la fin de la chaine est différente en fonction de si l'exemplaire trouvé est le dernier ou non, on adapte le retour en fonction
                return (listExemps[i].contains(Constants.STR_1E + Constants.VMC))
                        ? listExemps[i].substring(listExemps[i].indexOf(Constants.STR_1F) + 1, listExemps[i].indexOf(Constants.STR_1E + Constants.VMC)) + Constants.STR_1E
                        : listExemps[i].substring(listExemps[i].indexOf(Constants.STR_1F) + 1);
            }
        }
        return "";
    }

    /**
     * Récupère le numéro du dernier exemplaire d'une notice
     *
     * @param notice notice à analyser
     * @return numéro du dernier exemplaire de la notice
     */
    public static String getLastNumExempFromNotice(String notice) {
        String[] listeExemps = notice.split(Constants.STR_1E + Constants.VTXTE);

        //récupération du dernier exemplaire
        String lastExemp = listeExemps[listeExemps.length - 1];
        Pattern pattern = Pattern.compile(Constant.REG_EXP_ZONE_EXX);
        Matcher matcher = pattern.matcher(lastExemp);
        if (matcher.find()) {
            return matcher.group("numEx");
        }
        return "0";
    }

    public static String getExemplairesExistants(String notice) {
        String[] listeExemps = notice.split(Constants.STR_1E + Constants.VTXTE);
        StringBuilder exemplairesExistants = new StringBuilder();
        for (int i = 1; i < listeExemps.length; i++) {
            Pattern pattern = Pattern.compile(Constant.REG_EXP_EXEMPLAIRE);
            Matcher matcher = pattern.matcher(listeExemps[i]);
            if (matcher.find())
                exemplairesExistants.append(matcher.group("exemplaire")).append("\r");
        }
        return exemplairesExistants.toString();
    }

    public static String getDonneeLocaleExistante(String notice) {
        String noticeLocale = Utilitaire.recupEntre(notice, "VTXTLOK", Constants.STR_0D + Constants.STR_1E);
        return (noticeLocale.length() > 1) ? Utilitaire.recupEntre(noticeLocale, Constants.STR_1F, "").substring(1) : "";
    }

    /**
     * Méthode permettant d'obtenir une valeur entière correspondant à la sommes des codes ascii décimaux des caractères d'une zone
     *
     * @param tag zone sur laquelle effectuer le calcul
     * @return somme des codes ascii des caractères composant la zone
     */
    public static int getAsciiCodeFromTag(String tag) {
        int returnCode = 0;
        if (tag.length() == 3) {
            Iterator listChars = tag.chars().iterator();
            while (listChars.hasNext()) {
                returnCode += ((PrimitiveIterator.OfInt) listChars).next();
            }
        }
        return returnCode;
    }

    /**
     * Méthode permettant de compléter une chaine avec des 0 à gauche jusqu'à une taille maximale
     *
     * @param str       chaine à modifier
     * @param tailleMax taille maximale de la chaine
     * @return chaine modifiée
     */
    public static String addZeros(String str, int tailleMax) {
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = str.length(); i < tailleMax; i++) {
            strBuilder.insert(0, "0");
        }
        return strBuilder.toString();
    }

    public static boolean detectsANumberOfDataDifferentFromTheNumberOfHeaderDataOnALine(String currentDataLine, int numberOfHeaderColumns, int currentDataLinePositionNumber) throws FileCheckingException {
        return (StringUtils.countMatches(currentDataLine, ";") != numberOfHeaderColumns);
    }

    public static String addNecessarySemiColonFromEndOfLine(String lineToAddSemiColons, Integer nbColonnes){
        int initialySemiColonFinded = 0;

        Pattern patternLine = Pattern.compile(";");
        Matcher matcherLine = patternLine.matcher(lineToAddSemiColons);

        while (matcherLine.find()) {
            initialySemiColonFinded++;
        }

        if(nbColonnes - initialySemiColonFinded == 2){
            lineToAddSemiColons = Utilitaires.addCharactersToEndOfString(lineToAddSemiColons, new StringBuilder().append((char)0).append(';'), 1);
            return lineToAddSemiColons;
        }

        return lineToAddSemiColons;
    }

    public static <T> String addCharactersToEndOfString(String lineToAddCharacters, T characterToAdd, Integer numberOfCharactersToAdd){
        String lineToReturn = lineToAddCharacters;

        for (int i = 1; i <= numberOfCharactersToAdd; i++) {
                lineToReturn = lineToReturn + characterToAdd + (char)0;
        }

        return lineToReturn;
    }

    /**
     * @param ligneANettoyer ligne de données d'exemplaires ou l'on veut supprimer l'ensemble des ; qui auraient été insérés en fin de ligne
     * @return une ligne de données d'exemplaires ou il n'y aura pas de ; à la fin
     */
    public static String removeSemicolonFromEndOfLine(String ligneANettoyer) {
        Pattern patternLine = Pattern.compile(";");
        Matcher matcherLine = patternLine.matcher(ligneANettoyer);
        int count = 0;
        while (matcherLine.find()) {
            count++;
        }

        while (count > 0) {
            Pattern pattern = Pattern.compile(";$");
            Matcher matcher = pattern.matcher(ligneANettoyer);
            while (matcher.find()) {
                ligneANettoyer = ligneANettoyer.substring(0, ligneANettoyer.length() - 1);
            }
            count--;
        }
        return ligneANettoyer;
    }

    public static String removeSemicolonFromEndOfLine(String lineToClean, int zonesNumber) {
        Pattern patternLine = Pattern.compile(";");
        Matcher matcherLine = patternLine.matcher(lineToClean);
        int count = 0;
        while (matcherLine.find()) {
            count++;
        }

        while (count > zonesNumber) {
            Pattern pattern = Pattern.compile(";$");
            Matcher matcher = pattern.matcher(lineToClean);
            while (matcher.find()) {
                lineToClean = lineToClean.substring(0, lineToClean.length() - 1);
            }
            count--;
        }
        return lineToClean;
    }


    /**Methode permettant de détecter si une ligne est anormale
     * - c'est à dire ne contenant aucune données d'exemplaires (si l'utilisateur à mis des espaces dans des cellules
     * sans aucune données
     * @param ligneDexemplaire ligne d'exemplaire que l'on va controler (exemple de ligne suspecte ;; ;;;;;)
     * @return false si la ligne est valide; true si la ligne est anormale
     */
    public static boolean detectAnormalLine(String ligneDexemplaire) {
        Pattern pattern = Pattern.compile(Constant.REG_EXP_LIGNE_ANORMALE);
        Matcher matcher = pattern.matcher(ligneDexemplaire);
        return matcher.matches();
    }

    /**
     * Méthode permettant de supprimer les zones L de la première ligne du fichier, ainsi que les sous zones associées
     *
     * @param header ligne de l'en tête du fichier
     * @return ligne sans les zones L et les sous zones associées
     */
    public static String suppressionZonesL(String header) {
        String[] tabHeader = header.split(";");
        List<String> headerListe = new ArrayList<>();
        StringBuilder headerARetourner = new StringBuilder();
        String zonePrecedente = "";
        for (String zone : tabHeader) {
            if (!zone.startsWith("L") && !zone.startsWith("$")) {
                //la valeur n'est pas une zone L, on la rajoute dans une liste
                headerListe.add(zone);
                if (!zone.startsWith("$")) {
                    zonePrecedente = zone;
                }
            } else {
                if (zone.startsWith("$") && !zonePrecedente.startsWith("L")) {
                    //la valeur est une sous zone, et n'est pas rattachée à une zone L précédente
                    headerListe.add(zone);
                } else {
                    if (!zone.startsWith("$")) {
                        zonePrecedente = zone;
                    }
                }

            }
        }
        Iterator iterator = headerListe.iterator();
        int i = 0;
        //on reconstruit la ligne avec les ; entre chaque champ
        while (iterator.hasNext()) {
            headerARetourner.append(iterator.next());
            headerARetourner.append(";");
        }
        return headerARetourner.toString();
    }

    /**
     * Méthode permettant de vérifier qu'une zone en paramètre est une zone de donnée locale
     *
     * @param zone ligne de l'en tête du fichier
     * @return ligne sans les zones L et les sous zones associées
     */
    public static boolean isDonneeLocale(String zone, String zonePrecedente) {
        if (zone.startsWith("L")) {
            return true;
        }
        if (zone.startsWith("$") && zonePrecedente.startsWith("L")) {
            return true;
        }
        return false;
    }

    public static boolean isEtatCollection(String zone) {
        if (("955").equals(zone) || ("956").equals(zone) || ("957").equals(zone) || ("959").equals(zone)) {
            return true;
        }
        return false;
    }

    /**Methode permettant de supprimer les caractères ASCII problématiques
     * https://fr.wikibooks.org/wiki/Les_ASCII_de_0_%C3%A0_127/La_table_ASCII
     * @param text Chaine à nettoyer des caractères ACSII
     * @return chaine avec les caractères ASCII supprimés
     */
    public static String removeNonPrintableCharacters(String text) {
        text = text.replaceAll("[\\x1E]", "");
        text = text.replaceAll("[\\x1F]", "");
        return text;
    }


    /**
     * Méthode permettant d'extraire X PPN d'une chaine de ppn séparés par des ;
     * @param listePpn liste de PPN séparés par des ;
     * @param nbToExtract nombre de ppn à extraire de cette liste (0 si tous)
     * @return le chaine correspondant au nombre de ppn suivie de ...
     */
    public static String getXPPN(String listePpn, Integer nbToExtract) {
        if (listePpn == null || listePpn.isEmpty())
            return "";
        if (nbToExtract == 0) {
            return listePpn;
        }
        String[] tabPpn = listePpn.split(",");
        StringBuilder strToReturn = new StringBuilder();
        //extraction du nombre de ppn requis de la chaine
        for (int i = 0;i<nbToExtract;i++) {
            if (i < tabPpn.length)
                strToReturn.append(tabPpn[i]).append(",");
        }
        //suppression de la , finale
        if (strToReturn.charAt(strToReturn.length() - 1) == ',')
            strToReturn.deleteCharAt(strToReturn.length() - 1);
        //ajout de ... en fin de chaine si celle-ci a été tronquée
        if (tabPpn.length > nbToExtract) {
            strToReturn.append("...");
        }
        return strToReturn.toString();
    }

    public static String replaceDiacritical(String stringFirst){
        return stringFirst
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ä", "a")
                .replace("î", "i")
                .replace("ï", "i")
                .replace("ô", "o")
                .replace("ö", "o")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ü", "u")
                .replace("ÿ", "y")
                .replace("æ", "ae")
                .replace("œ", "oe")
                .replace("ç", "c")
                .replace("ñ", "n")
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace("Ë", "E")
                .replace("À", "A")
                .replace("Â", "A")
                .replace("Ä", "A")
                .replace("Î", "I")
                .replace("Ï", "I")
                .replace("Ô", "O")
                .replace("Ö", "O")
                .replace("Ú", "U")
                .replace("Ù", "U")
                .replace("Ü", "U")
                .replace("Û", "U")
                .replace("Ÿ", "Y")
                .replace("Æ", "AE")
                .replace("Œ", "OE")
                .replace("Ç", "C")
                .replace("Ñ", "N")
                .replace("Ø","oe");
    }
}
