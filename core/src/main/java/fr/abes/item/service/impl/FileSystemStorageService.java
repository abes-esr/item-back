package fr.abes.item.service.impl;

import fr.abes.item.configuration.StorageProperties;
import fr.abes.item.constant.Constant;
import fr.abes.item.exception.StorageException;
import fr.abes.item.exception.StorageFileNotFoundException;
import fr.abes.item.service.IStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileSystemStorageService implements IStorageService {

    private Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changePath(Path path) {
        this.rootLocation = path;
    }


    /**
     * {@inheritDoc}
     */
    public void store(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new StorageException(Constant.ERR_FILE_STORAGE_EMPTY_FILE + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory " + filename);
            }

            boolean fileExist = exist(filename);
            if (fileExist) {
                // On déplace le fichier à écraser et on l'archive en le
                // renommant avec timestamp
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                Files.move(load(filename),
                        this.rootLocation.resolve(filename + "-archive-" + timestamp.getTime() + ".csv"));
            }

            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            //1er param file.getInputStream() initialement

        } catch (IOException e) {
            throw new StorageException(Constant.ERR_FILE_STORAGE_FILE + filename, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException(Constant.ERR_FILE_STORAGE_FILE_READING, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(Constant.ERR_FILE_READING + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException(Constant.ERR_FILE_READING + filename, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void delete(String filename) throws IOException {
        FileSystemUtils.deleteRecursively(load(filename));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException(Constant.STORAGE_SERVICE_INITIALIZATION_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exist(String filename) {
        Path fileExist = load(filename);
        Resource resource;
        try {
            resource = new UrlResource(fileExist.toUri());
            if (resource.exists()) {
                return true;
            }
        } catch (MalformedURLException e) {
            throw new StorageException(Constant.STORAGE_SERVICE_MALFORMED_URL_FILE_STORED, e);
        }
        return false;
    }


}
