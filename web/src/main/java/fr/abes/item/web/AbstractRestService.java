package fr.abes.item.web;

import fr.abes.item.service.service.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Injecte le service provider qui contient l'ensemble des interfaces
 * pour accéder aux données des différentes tables en base
 */
@RequestMapping("/api/v1")
public class AbstractRestService {
    @Autowired
    private ServiceProvider serviceProvider;

    public ServiceProvider getService() {
        return serviceProvider;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
