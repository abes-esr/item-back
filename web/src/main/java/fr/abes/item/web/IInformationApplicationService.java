package fr.abes.item.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
public interface IInformationApplicationService {
    @ApiOperation("Connaître : Version du front, back, quel application-localhost.properties est actuellement utilisé, url des serveurs sudoc, item, base xml branchés")
    @GetMapping(value = "/applicationDetails")
    Map<String, String> getApplicationDetails();

    @ApiOperation("Connaitre : Statut base xml, Statut base item, Statut CBS")
    @GetMapping(value = "/applicationStatutServices")
    Map<String, Boolean> getStatutServices();
}
