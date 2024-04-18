package fr.abes.item.web;

import fr.abes.item.core.entities.item.TypeExemp;
import fr.abes.item.core.service.ReferenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TypeExempRestService {
    private final ReferenceService referenceService;

    public TypeExempRestService(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping(value = "/typeExemp")
    public List<TypeExemp> getTypeExemps() {
        return referenceService.findAllTypeExemp();
    }
}
