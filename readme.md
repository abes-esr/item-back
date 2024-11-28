# item-Api

[![build-test-pubtodockerhub](https://github.com/abes-esr/item-api/actions/workflows/build-test-pubtodockerhub.yml/badge.svg)](https://github.com/abes-esr/item-api/actions/workflows/build-test-pubtodockerhub.yml) [![Docker Pulls](https://img.shields.io/docker/pulls/abesesr/item.svg)](https://hub.docker.com/r/abesesr/item/)

Vous êtes sur le README usager. Si vous souhaitez accéder au README développement,
veuillez suivre ce lien : [README-developpement](README-developpement.md)

Ce dépôt héberge le code source de l'API de Item.  
Cette API fonctionne avec son interface utilisateur développée en VueJS (front) : https://github.com/abes-esr/item-client/  
L'application Item complète peut être déployée via Docker à l'aide de ce dépôt : https://github.com/abes-esr/item-docker/  

**item-api** est une API permettant de : 
1. calculer un taux de recouvrement
2. créer, modifier, supprimer des exemplaires par lot dans des notices
3. récupérer les informations récapitulatives des demandes de recouvrement, de créations, de modification et de suppression
4. récupérer les données des demandes de création, de modification et de suppression sous forme de fichiers

## Principe général de fonctionnement

### Webservices

Des webservices exposés permettent de récupérer les demandes de recouvrement, créations, modifications et suppressions 
du client web item-client. Ces demandes et leurs informations associées sont stockées dans une base de données PostgreSQL. 

### Traitements

Un processus autonome (batch) se lance à interval régulier pour lire cette base de données et effectuer les traitements
nécessaires à la réalisation des demandes (recouvrement, créations, modifications, suppressions). 
Les données résultant des traitements sont ensuite écrites dans la base de données et des mails récapitulatifs 
sont générés et envoyés à la personne ayant effectué la demande. 

### Récapitulatifs des demandes

**item-api** permet d'accéder aux informations liées à une demande ainsi qu'aux fichiers initiaux (liste de PPN, liste d'EPN, etc.)
qu'elle mettra à disposition du client item-client.