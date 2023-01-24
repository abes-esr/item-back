package fr.abes.item.service;

import fr.abes.item.entities.item.Utilisateur;

/**
 * Service permettant d'enregistrer et récupérer les utilisateurs de l'application en base de donnée
 *
 * - findByID : retourne un Utilisateur en allant avec le DAO le chercher dans la table UTILISATEUR à partir de son id
 * - save : sauvegarde un nouvel utilisateur dans la table
 * - deleteByID : supprime un utilisateur de la table à partir de son ID
 * - setIlnOnUtilisateur : attribue un iln à l'utilisateur. Appelé au moment de la recherche d'un utilisateur avec findByID
 */
public interface IUtilisateurService {
    Utilisateur findById(Integer id);

    Utilisateur save(Utilisateur utilisateur);

    void deleteById(Integer id);

    void setIlnOnUtilisateur(Utilisateur utilisateur);

    boolean isAdmin(Utilisateur utilisateur);

    String findRcrById(String id);

    String findEmailById(Integer id);
}
