package fr.abes.item.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value("files.upload.path")
    private String location;

    /**
     * Retourne le chemin d'enregistrement des fichiers
     *
     * @return Chemin d'enregistrement des fichiers
     */
    public String getLocation() {
        return location;
    }

    /**
     * Modifie le chemin d'enregistrement des fichiers
     *
     * @param location Nouveau chemin
     */
    public void setLocation(String location) {
        this.location = location;
    }

}
