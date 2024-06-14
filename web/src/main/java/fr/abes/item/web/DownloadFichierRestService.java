package fr.abes.item.web;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.core.exception.UserExistException;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	@GetMapping(value="/files/{filename:.+}")
	@Operation(summary = "permet de récupérer les fichiers relatifs à une demande")
	public ResponseEntity<Resource> downloadFile(
			@PathVariable("filename") String filename, @RequestParam("id") Integer numDemande, @RequestParam("type") TYPE_DEMANDE type, HttpServletRequest request
	) throws UserExistException, ForbiddenException {
		checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute("userNum").toString(), type);

		if (numDemande != null && numDemande != 0) {
			storageService.changePath(Paths.get(uploadPath + numDemande));
			storageService.init();
		}
		Resource file = storageService.loadAsResource(filename);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + file.getFilename() + "\"")
				.body(file);

	}
}
