package fr.abes.item.web;

import fr.abes.item.entities.item.TypeExemp;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
public interface ITypeExempRestService {
    @GetMapping(value = "/typeExemp")
    List<TypeExemp> getTypeExemps();
}
