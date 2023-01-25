package fr.abes.item.web.impl;

import fr.abes.item.exception.ForbiddenException;
import fr.abes.item.exception.UserExistException;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.IDownloadFichierRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Paths;

@RestController
public class DownloadFichierRestService extends AbstractRestService implements IDownloadFichierRestService {

	@Value("${files.upload.path}")
	private String uploadPath;

	@Autowired
	CheckAccessToServices checkAccessToServices;

	/**
	 * Webservice de téléchargement d'un fichier en fonction d'une demande
	 * @param filename : nom du fichier à télécharger
	 * @param numDemande : numéro de la demande concernée
	 * @return : Resource correspondant au fichier à télécharger
	 */
	@Override
	@GetMapping(value="/files/{filename:.+}")
	public ResponseEntity<Resource> downloadFile(
			@PathVariable String filename, @RequestParam("id") Integer numDemande, HttpServletRequest request
	) throws UserExistException, ForbiddenException {

		checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute("userNum").toString());

		if (numDemande != null && numDemande != 0) {
			getService().getStorage().changePath(Paths.get(uploadPath + numDemande));
			getService().getStorage().init();
		}
		Resource file = getService().getStorage().loadAsResource(filename);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + file.getFilename() + "\"")
				.body(file);

	}
}
