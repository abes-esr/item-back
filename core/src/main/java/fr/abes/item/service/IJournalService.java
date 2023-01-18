package fr.abes.item.service;

import fr.abes.item.entities.item.*;

import java.util.List;

/**
 * Service permettant de retourner la journalisation des demandes d'examplarisation
 * findAll() : retourne le journal de l'ensemble des demandes d'exemplarisation
 *      - chaque ligne est une entité JOURNAL_DEMANDE_EXEMP
 *      chaque ligne va contenir une clé primaire relative à la ligne, une référence à une demande, une référence
 *          à un utilisateur, une référence à un etat situé dans la table ETAT_DEMANDE
 *          c'est l'etat de la demande qui va varier le plus souvent dans le journal. Plusieurs lignes pour une
 *          même demande, avec des changements d'Etats
 * andEntreeJournal : ajoute une ligne dans le journal, suite au changement d'Etat d'une demande
 *      - ajoute une novuelle entrée, dans la journal, un changement d'Etat rattaché à la demande d'exmplarisation
 *          ajoute une nouvelle entité JournalDemandeExemp dans la table JOURNAL_DEMANDE_EXEMP
 * findById : retourne une ligne de journal d'une demande par son ID (clé primaire)
 */
public interface IJournalService {

    List<JournalDemandeExemp> findAllExemp();

    void addEntreeJournal(DemandeExemp demande, EtatDemande etat);

    JournalDemandeExemp findByIdExemp(Integer id);

    List<JournalDemandeModif> findAllModif();

    void addEntreeJournal(DemandeModif demandeModif, EtatDemande etat);

    JournalDemandeModif findByIdModif(Integer id);

    void removeEntreesJournal(DemandeModif demandeModif);

    void removeEntreesJournal(DemandeExemp demandeExemp);
}
