package fr.abes.item.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Classe gérant l'enregistrement des fichiers
 * - init : va crée le dossier à l'endroit spécifié dans le variable files.upload.path du fichier application.properties
 *      il s'agit d'un chemin absolu, ici /applis/item
 * - changePath : va modifier le dossier de destination du fichier pour stockage lors de l'upload
 * - exist : contrôle si le fichier est déjà présent dans le système de fichiers
 * - store : enregistre le fichier, si un fichier du même nom est déjà présent, renomme le fichier déjà présent en ajoutant
 *      sa date d'archivage et stocke le nouveau (conservation de l'historique des fichiers, rotation)
 * - load : retourne l'emplacement exact du fichier spécifié en paramètre
 * - loadAsResource : permet de retourner l'emplacement physique du fichier sous forme d'URI, nécessaire pour :
 *      pour sauvegarder le fichier, on utilise ensuite une URI pour sauvegarder une à une les lignes du fichier,
 *      on spécifie ensuite l'URI en paramètre de saveFile()
 * - deleteAll : supprime l'ensemble des fichiers du dossiers recursifs
 * - delete : supprimer un fichier dont le nom est spécifié
 */
public interface IStorageService {

	/**
	 * Crée le dossier nécessaire au stockage des fichiers
	 */
    void init();

    /** 
     * modifier le dossier de destination lors de l'upload
     * @param path nouveau chemin de destination
     */
    void changePath(Path path);
    
    /**
     * vérifier si le fichier filename existe déjà dans le rootlocation
     * @param filename nom du fichier à vérifier
     * @return true si fichier existe, false sinon
     */
    boolean exist(String filename);
    
    /**
     * Copie le fichier sur le serveur
     * @param file Fichier récupéré depuis le client
     * @param filename nom du fichier à copier
     */
    void store(MultipartFile file, String filename) throws IOException;

    /**
     * 
     * @return ensemble des fichiers dans le path actuel
     */  
    Stream<Path> loadAll();

    /**
     * permet de retourner le chemin du fichier passé en paramètre
     * @param filename
     * @return chemin du fichier
     */
    Path load(String filename);

    /**
     * permet de charger un fichier en tant que Resource
     * @param filename nom du fichier à charger
     * @return fichier sous forme Resource
     */
    Resource loadAsResource(String filename);

    /**
     * Supprimer l'ensemble des fichiers récursivement
     */
    void deleteAll();

    /**
     * Supprimer un fichier
     */
    void delete(String filename) throws IOException;
}