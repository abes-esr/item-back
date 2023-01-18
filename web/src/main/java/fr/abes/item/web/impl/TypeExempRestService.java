package fr.abes.item.web.impl;

import fr.abes.item.entities.item.TypeExemp;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.ITypeExempRestService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TypeExempRestService extends AbstractRestService implements ITypeExempRestService {
    @Override
    public List<TypeExemp> getTypeExemps() {
        return getService().getReference().findAllTypeExemp();
    }
}
