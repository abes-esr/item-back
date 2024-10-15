package fr.abes.item.web;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.core.exception.UserExistException;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.core.utilitaire.Utilitaires;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1")
public class DownloadFichierRestService {
	private final FileSystemStorageService storageService;
	@Value("${files.upload.path}")
	private String uploadPath;

	private final CheckAccessToServices checkAccessToServices;

	public DownloadFichierRestService(FileSystemStorageService storageService, CheckAccessToServices checkAccessToServices) {
		this.storageService = storageService;
		this.checkAccessToServices = checkAccessToServices;
	}

	/**
	 * Webservice de téléchargement d'un fichier en fonction d'une demande
	 * @param filename : nom du fichier à télécharger
	 * @param numDemande : numéro de la demande concernée
	 * @param type : type de la demande concernée
	 * @return : Ressource correspondant au fichier à télécharger
	 */
	@GetMapping(value="/files/{type}/{id}/{filename:.+}")
	@Operation(summary = "permet de récupérer les fichiers relatifs à une demande")
	public ResponseEntity<Resource> downloadFile(
			@PathVariable("filename") String filename, @PathVariable("id") Integer numDemande, @PathVariable("type") TYPE_DEMANDE type, HttpServletRequest request
	) throws UserExistException, ForbiddenException, IOException {
		checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute("userNum").toString(), type);

		if (numDemande != null && numDemande != 0) {
			storageService.changePath(Paths.get(uploadPath + type.toString().toLowerCase() + "/" + numDemande));
			storageService.init();
		}
		Resource file = storageService.loadAsResource(filename);

		Resource bodyFile = Utilitaires.sortFichierCorrespondance(file);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + file.getFilename() + "\"")
				.body(bodyFile);
	}
}
