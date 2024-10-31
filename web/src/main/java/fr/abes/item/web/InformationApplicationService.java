package fr.abes.item.web;

import fr.abes.item.core.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
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

    @Operation(description = "Connaître : Version du back")
    @GetMapping(value = "/applicationDetails")
    public String getApplicationDetails() {
        return this.applicationVersionBack;
    }

    @SneakyThrows
    @Operation(summary = "Connaitre : Statut base item")
    @GetMapping(value = "/applicationStatutServices")
    public Boolean getStatutServices() {
        return statusService.getItemDataBaseStatus();
    }
}
