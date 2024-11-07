package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.exception.FileCheckingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test pour FichierInitial")
public class TestFichierInitialModif {


    @DisplayName("checkMaxLines")
    @Test
    public void checkMaxLines() {
        FichierInitial fic = new FichierInitial("maxLines.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(null)).getMessage().contains(Constant.ERR_FILE_TOOMUCH_START));
    }

    @DisplayName("checkChampTropLong")
    @Test
    public void checkChampTropLong() {
        FichierInitial fic = new FichierInitial("champTropLong.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(null))
                .getMessage().contains("la ligne ne doit contenir qu'un ppn"));
    }

    @DisplayName("cutFileMoinsde300")
    @Test
    public void cutFileMoinsde300() throws IOException {
        FichierInitial fic = new FichierInitial("cutFileMoinsde300.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        List<String> res = fic.cutFile();
        assertThat(res.size() == 1).isTrue();
        ListIterator<String> lesppns = res.listIterator();
        String ppns = lesppns.next();
        assertFalse(ppns.endsWith(","));
        assertTrue(ppns.matches("((([a-zA-Z0-9]){9},){293})(([a-zA-Z0-9]){9})"));
    }

    @DisplayName("cutFilePlusde300")
    @Test
    public void cutFilePlusde300() throws IOException {
        FichierInitial fic = new FichierInitial("cutFilePlusde300.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        List<String> res = fic.cutFile();
        assertEquals(3, res.size());
        ListIterator<String> lesppns = res.listIterator();
        String ppns = lesppns.next();
        assertFalse(ppns.endsWith(","));
        assertTrue(ppns.matches("((([a-zA-Z0-9]){9},){299})(([a-zA-Z0-9]){9})"));
        String ppns2 = lesppns.next();
        assertFalse(ppns2.endsWith(","));
        assertTrue(ppns2.matches("((([a-zA-Z0-9]){9},){299})(([a-zA-Z0-9]){9})"));
        String ppns3 = lesppns.next();
        assertFalse(ppns3.endsWith(","));
        assertTrue(ppns3.matches("((([a-zA-Z0-9]){9},){104})(([a-zA-Z0-9]){9})"));
    }

    @Test
    public void cutFileWithDoublonsMoinsde300() throws IOException {
        FichierInitial fic = new FichierInitial("cutFileWithDoublonMoinsde300.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        List<String> res = fic.cutFile();
        assertEquals(1, res.size());
        ListIterator<String> lesppns = res.listIterator();
        String ppns = lesppns.next();
        assertFalse(ppns.endsWith(","));
        assertTrue(ppns.matches("((([a-zA-Z0-9]){9},){2})(([a-zA-Z0-9]){9})"));
    }

    @Test
    public void cutFileWithDoublonsPlusde300() throws IOException {
        FichierInitial fic = new FichierInitial("cutFileWithDoublonPlusde300.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        List<String> res = fic.cutFile();
        assertEquals(1, res.size());
        ListIterator<String> lesppns = res.listIterator();
        String ppns = lesppns.next();
        assertFalse(ppns.endsWith(","));
        assertTrue(ppns.matches("((([a-zA-Z0-9]){9},){2})(([a-zA-Z0-9]){9})"));

    }

    @DisplayName("supprimerRetourChariot")
    @Test
    public void supprimerRetourChariot() throws IOException {
        FichierInitial fic = new FichierInitial("suppRetourChariot.txt");
        fic.setPath(Paths.get("src/test/resources/fichierInitial"));
        fic.supprimerRetourChariot();

        String line;
        int nbLignes = 0;
        FileInputStream fis = new FileInputStream(fic.getPath().resolve(fic.getFilename()).toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        while ((line = reader.readLine()) != null) {
            nbLignes++;
            assertFalse(line.trim().isEmpty());
        }
        assertEquals(7, nbLignes);
    }
}
