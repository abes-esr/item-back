package fr.abes.item.web;

import fr.abes.item.core.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class InformationApplicationService {
    private final StatusService statusService;
    @Value("${app.version}")
    private String applicationVersionBack;

    public InformationApplicationService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Operation(description = "Connaître : Version du front, back, quel application-localhost.properties est actuellement utilisé, url des serveurs sudoc, item, base xml branchés")
    @GetMapping(value = "/applicationDetails")
    public Map<String, String> getApplicationDetails() {
        Map<String, String> map = new HashMap<>();
        map.put("BACKVERSION", this.applicationVersionBack);
        return map;
    }

    @Operation(summary = "Connaitre : Statut base xml, Statut base item, Statut CBS")
    @GetMapping(value = "/applicationStatutServices")
    public Map<String, Boolean> getStatutServices() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("STATUT CBS", statusService.getCbsConnectionStatus());
        map.put("STATUT BASE_XML", statusService.getXmlConnectionStatus());
        map.put("STATUT BASE_ITEM", statusService.getItemDataBaseStatus());
        return map;
    }
}
