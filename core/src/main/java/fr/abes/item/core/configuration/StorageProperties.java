package fr.abes.item.core.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value("files.upload.path")
    private String location;



}
