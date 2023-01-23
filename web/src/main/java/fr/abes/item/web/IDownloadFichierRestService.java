package fr.abes.item.web;

import fr.abes.item.exception.ForbiddenException;
import fr.abes.item.exception.UserExistException;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


public interface IDownloadFichierRestService {
    @GetMapping(value="/files/{filename:.+}")
    @ApiOperation(value = "permet de récupérer les fichiers relatifs à une demandeModif")
    ResponseEntity<Resource> downloadFile(
            @PathVariable String filename, @RequestParam("id") Integer numDemande, HttpServletRequest request
    ) throws UserExistException, ForbiddenException;
}