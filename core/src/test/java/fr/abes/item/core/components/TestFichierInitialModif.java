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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test pour FichierInitial")
public class TestFichierInitialModif {



	@DisplayName("checkMaxLines")
	@Test
	public void checkMaxLines() {
		FichierInitial fic = new FichierInitial("maxLines.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		assertThat(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(null)).getMessage().contains(Constant.ERR_FILE_WRONGCONTENT)).isTrue();
	}

	@DisplayName("checkChampTropLong")
	@Test
	public void checkChampTropLong() {
		FichierInitial fic = new FichierInitial("champTropLong.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		assertTrue(assertThrows(FileCheckingException.class, () -> fic.checkFileContent(null))
				.getMessage().contains(Constant.ERR_FILE_WRONGCONTENT));
	}

	@DisplayName("cutFileMoinsde300")
	@Test
	public void cutFileMoinsde300() {
		FichierInitial fic = new FichierInitial("cutFileMoinsde300.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		try {
			List<String> res = fic.cutFile();
			assertThat(res.size() == 1).isTrue();
			ListIterator<String> lesppns = res.listIterator();
			String ppns = lesppns.next();
			assertThat(ppns.endsWith(",")).isFalse();
			assertThat(ppns.matches("((([a-zA-Z0-9]){9},){293})(([a-zA-Z0-9]){9})")).isTrue();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@DisplayName("cutFilePlusde300")
	@Test
	public void cutFilePlusde300() {
		FichierInitial fic = new FichierInitial("cutFilePlusde300.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		try {
			List<String> res = fic.cutFile();
			assertThat(res.size() == 3).isTrue();
			ListIterator<String> lesppns = res.listIterator();
			String ppns = lesppns.next();
			assertThat(ppns.endsWith(",")).isFalse();
			assertThat(ppns.matches("((([a-zA-Z0-9]){9},){299})(([a-zA-Z0-9]){9})")).isTrue();
			String ppns2 = lesppns.next();
			assertThat(ppns2.endsWith(",")).isFalse();
			assertThat(ppns2.matches("((([a-zA-Z0-9]){9},){299})(([a-zA-Z0-9]){9})")).isTrue();
			String ppns3 = lesppns.next();
			assertThat(ppns3.endsWith(",")).isFalse();
			assertThat(ppns3.matches("((([a-zA-Z0-9]){9},){104})(([a-zA-Z0-9]){9})")).isTrue();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void cutFileWithDoublonsMoinsde300() {
		FichierInitial fic = new FichierInitial("cutFileWithDoublonMoinsde300.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		try {
			List<String> res = fic.cutFile();
			assertThat(res.size() == 1).isTrue();
			ListIterator<String> lesppns = res.listIterator();
			String ppns = lesppns.next();
			assertThat(ppns.endsWith(",")).isFalse();
			assertThat(ppns.matches("((([a-zA-Z0-9]){9},){2})(([a-zA-Z0-9]){9})")).isTrue();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void cutFileWithDoublonsPlusde300() {
		FichierInitial fic = new FichierInitial("cutFileWithDoublonPlusde300.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		try {
			List<String> res = fic.cutFile();
			assertThat(res.size() == 1).isTrue();
			ListIterator<String> lesppns = res.listIterator();
			String ppns = lesppns.next();
			assertThat(ppns.endsWith(",")).isFalse();
			assertThat(ppns.matches("((([a-zA-Z0-9]){9},){2})(([a-zA-Z0-9]){9})")).isTrue();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@DisplayName("supprimerRetourChariot")
	@Test
	public void supprimerRetourChariot() {
		FichierInitial fic = new FichierInitial("suppRetourChariot.txt");
		fic.setPath(Paths.get("src/test/resources/fichierInitial"));
		try {
			fic.supprimerRetourChariot();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String line;
		int nbLignes = 0;
		try (FileInputStream fis = new FileInputStream(fic.getPath().resolve(fic.getFilename()).toString());
			 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
			while ((line = reader.readLine()) != null) {
				nbLignes++;
				assertThat(line.trim().isEmpty()).isFalse();
			}
			assertThat(nbLignes).isEqualTo(7);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
