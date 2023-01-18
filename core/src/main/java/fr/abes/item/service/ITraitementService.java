package fr.abes.item.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.item.entities.item.Traitement;

import java.util.List;

/**
 * Service permettant à partir d'une notice fournie en paramètre de créer / editer / supprimer des zones
 * - getCbs : permet d'établir la pont avec le cbs
 * - authenticate : se connecter au serveur sudoc avec un login, le mdp étant dans une constante
 * - getNoticeFromEPN : récupèrer le premier exemplaire d'une notice sous forme de chaîne de caractères
 * - creerNouvelleZone : ajoute une zone à un exemplaire
 * - supprimerZone : supprime une zone à un exemplaire
 * - supprimerSousZone : supprime une sous-zone appartenant à une zone d'un exemplaire
 * - creerSousZone : ajoute une sous-zone appartenant à une zone d'un exemplaire
 * - remplacerSousZone : remplace une sous-zone appartenant à une zone d'une exemplaire
 * - saveExemplaire : enregistre un exemplaire en utilisant le cbs
 * - disconnect : deconnecte le cbs
 * - findAll : retourne l'ensemble des traitements disponibles, situés dans la table TRAITEMENT
 *      accessibles via la couche DAO
 */
public interface ITraitementService {

    ProcessCBS getCbs();

    void authenticate(String login) throws CBSException;

    String getNoticeFromEPN(String epn) throws CBSException;

    String creerNouvelleZone(String notice, String tag, String subTag, String valeur) throws ZoneException;

    String supprimerZone(String notice, String tag) throws ZoneException;

    String supprimerSousZone(String notice, String tag, String subTag) throws ZoneException;

    String creerSousZone(String notice, String tag, String subTag, String valeur) throws ZoneException;

    String remplacerSousZone(String notice, String tag, String subTag, String valeur) throws ZoneException;

    String saveExemplaire(String noticeModifiee, String epn) throws CBSException;

    void disconnect() throws CBSException;

    List<Traitement> findAll();

    Integer findTraitementByDemandeId(Integer id);
}
