package fr.abes.item.utilitaire;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.abes.cbs.utilitaire.Constants;
import fr.abes.item.constant.Constant;
import fr.abes.item.exception.FileCheckingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class testUtilitaires {
    @DisplayName("checkExtensionExe")
    @Test
    void checkExtensionExe() {
        assertThat(assertThrows(FileCheckingException.class, () -> Utilitaires.checkExtension("testfichier.exe")).getMessage().contains(Constant.ERR_FILE_FORMAT)).isTrue();
    }

    @DisplayName("checkExtensionCsv")
    @Test
    void checkExtensionCsv() throws FileCheckingException {
        Utilitaires.checkExtension("testfichier.csv");
    }

    @DisplayName("checkExtensionTxt")
    @Test
    void checkExtensionTxt() throws FileCheckingException {
            Utilitaires.checkExtension("testfichier.txt");
    }

    @DisplayName("checkChaineSansBom")
    @Test
    void checkChaineSansBom() {
        String chaine = "123456";
        assertThat(Utilitaires.checkBom(chaine)).isEqualTo("123456");
    }

    @DisplayName("checkChaineAvecBom")
    @Test
    void checkChaineAvecBom() {
        String chaine = '\ufeff' + "123456";
        assertThat(Utilitaires.checkBom(chaine)).isEqualTo("123456");
    }

    @DisplayName("getNumExFromExempOneEx")
    @Test
    void getNumExFromExempOneEx() {
        String noticeInit =  "e01 $a17-09-18$bx\r" +
                "930 ##$b341720001\r" +
                "A97 17-09-18 10:51:56.000\r" +
                "A98 341720001:17-09-18\r" +
                "A99 618828249\r" +
                Constants.STR_0D + Constants.STR_0D;
        assertThat(Utilitaires.getNumExFromExemp(noticeInit)).isEqualTo("01");
    }

    @DisplayName("getExempFromNotice")
    @Test
    void getExempFromNotice() {
        String epn = "248398830";
        String noticeDeuxExemp = Constants.STR_1E + Constants.VTXTE +
                "01011214:37:13.000" + Constants.STR_1F +
                "e01 $a17-11-05$bx" +
                "930 ##$b341722103$a77 ETH$ju" +
                "A97 17-11-05 14:37:13.000" +
                "A98 341722103:17-11-05" +
                "A99 24719932X" + Constants.STR_0D + Constants.STR_1E + Constants.VTXTE +
                "02016810:01:06.000" + Constants.STR_1F +
                "e02 $a02-12-05$bx" +
                "930 ##$b340322102$a791 ETH$ju" +
                "991 ##$aExemplaire créé automatiquement par l'ABES" +
                "A97 02-12-05 10:01:06.000" +
                "A98 340322102:02-12-05" +
                "A99 248333186" + Constants.STR_0D + Constants.STR_1E + Constants.VTXTE +
                "03011716:22:37.000" + Constants.STR_1F +
                "e03 $a02-12-05$bx" +
                "930 ##$b301892102$a306.485 ETH$ju" +
                "A97 02-12-05 16:22:37.000 " +
                "A98 301892102:02-12-05" +
                "A99 248398830" +
                Constants.STR_1E + Constants.VMC;
        assertThat(Utilitaires.getExempFromNotice(noticeDeuxExemp, epn)).isEqualTo(Constants.STR_1F +
                "e03 $a02-12-05$bx" +
                "930 ##$b301892102$a306.485 ETH$ju" +
                "A97 02-12-05 16:22:37.000 " +
                "A98 301892102:02-12-05" +
                "A99 248398830" + Constants.STR_1E);
    }

    @DisplayName("getAsciiCodeFromTag")
    @Test
    void getAsciiCodeFromTag() {
        String tag = "930";
        assertThat(Utilitaires.getAsciiCodeFromTag(tag)).isEqualTo(156);
    }

    @DisplayName("addZeros")
    @Test
    void addZeros(){
        String str = "12";
        assertThat(Utilitaires.addZeros(str, 9)).isEqualTo("000000012");
    }

    @Test
    void stringToRemove(){
        String string = "230727409;seau;bleu;;;";
        String stringResult = Utilitaires.removeSemicolonFromEndOfLine(string);
        System.out.println(stringResult);
    }

    /**
     * Test de vérification de la méthode supprimant les données locales de la première ligne du fichier
     */
    @Test
    void suppressionZonesL() {
        String header = "930 $a;$j;917 $a;L035 $a;";
        assertThat(Utilitaires.suppressionZonesL(header)).isEqualTo("930 $a;$j;917 $a;");
        header = "L035 $a;930 $a;$j;917 $a";
        assertThat(Utilitaires.suppressionZonesL(header)).isEqualTo("930 $a;$j;917 $a;");
        header = "930 $a;$j;L035 $a;917 $a";
        assertThat(Utilitaires.suppressionZonesL(header)).isEqualTo("930 $a;$j;917 $a;");
        header = "930 $a;$j;L035 $a;$j;917 $a";
        assertThat(Utilitaires.suppressionZonesL(header)).isEqualTo("930 $a;$j;917 $a;");
    }

    @Test
    void isDonneeLocaleTest() {
        String zone = "L035";
        String zonePrecedente = "930";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isTrue();
        zone = "$a";
        zonePrecedente = "930";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isFalse();
        zone = "$a";
        zonePrecedente = "L035";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isTrue();
        zone = "L035";
        zonePrecedente = "$b";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isTrue();
        zone = "930";
        zonePrecedente = "915 $a";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isFalse();
        zone = "930";
        zonePrecedente = "L035 $a";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isFalse();
        zone = "930 $a";
        zonePrecedente = "L035 $a";
        assertThat(Utilitaires.isDonneeLocale(zone, zonePrecedente)).isFalse();
    }

    @Test
    void getXPpnTest() {
        String listeppn;
        int nbToExtract = 3;
        assertThat(Utilitaires.getXPPN(null, nbToExtract)).isEqualTo("");
        listeppn = "123456789,987654321,456789123";
        nbToExtract = 5;
        assertThat(Utilitaires.getXPPN(listeppn, nbToExtract)).isEqualTo("123456789,987654321,456789123");
        nbToExtract = 0;
        assertThat(Utilitaires.getXPPN(listeppn, nbToExtract)).isEqualTo(listeppn);
        listeppn = "123456789,987654321,654987321,789456123,654987321,456789123";
        nbToExtract = 5;
        assertThat(Utilitaires.getXPPN(listeppn, nbToExtract)).isEqualTo("123456789,987654321,654987321,789456123,654987321...");
    }

    @Test
    void testParseJsonOneValue() throws IOException {
        String json = "{\"sudoc\":{\"result\":{\"epn\":\"621956651\",\"ppn\":\"231927401\"}}}";

        Multimap<String, String> map = ArrayListMultimap.create();
        map.put("231927401", "621956651");

        Assertions.assertThat(Utilitaires.parseJson(json)).isEqualTo(map);
    }

    @Test
    void testParseJsonManyValues() throws IOException {
        String json = "{\"sudoc\":{\"result\":[{\"epn\":\"621956651\",\"ppn\":\"231927401\"},{\"epn\":\"999999999\",\"ppn\":\"111111111\"}]}}";

        Multimap<String, String> map = ArrayListMultimap.create();
        map.put("231927401", "621956651");
        map.put("111111111", "999999999");

        Assertions.assertThat(Utilitaires.parseJson(json)).isEqualTo(map);
    }

    @Test
    void replacementOfDiacriticalAccents() {
        String var = "é, è, ê, ë, à, â, ä, î, ï, ô, ö, ù, û, ü, ÿ, æ, œ, ç, ñ, Ø";
        String var2 = Utilitaires.replaceDiacritical(var);
        assertEquals("e, e, e, e, a, a, a, i, i, o, o, u, u, u, y, ae, oe, c, n, oe", var2);
    }
}
