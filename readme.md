# Item-Api

[![build-test-pubtodockerhub](https://github.com/abes-esr/item-api/actions/workflows/build-test-pubtodockerhub.yml/badge.svg)](https://github.com/abes-esr/item-api/actions/workflows/build-test-pubtodockerhub.yml) [![Docker Pulls](https://img.shields.io/docker/pulls/abesesr/item.svg)](https://hub.docker.com/r/abesesr/item/)

Ce dépôt héberge le code source de l'API de Item.  
Cette API fonctionne avec son interface utilisateur développée en VueJS (front) : https://github.com/abes-esr/item-client/  
Et l'application Item complète peut être déployée via Docker à l'aide de ce dépôt : https://github.com/abes-esr/item-docker/  

## Partie serveur de l'application item - Server part of the item application

### Note à l'attention des développeurs - Note to developers

Les fichiers 
* applications.properties
* application-PROD.properties
* application-TEST.properties

présents dans le dossier ressources du (main) dans les modules
* web
* batch

présents dans le dossier ressources du test dans les modules
* web
* core

sont non-versionnés. Cela signifie que vous devrez avoir 
votre propre base de donnée pour faire fonctionner l'application
en local des variables indiquées dans le dossier documentation.
Si besoin de précisions, vous adressez à item@abes.fr

---

The files 
* applications.properties
* application-PROD.properties
* application-TEST.properties

present in the resources' folder of the hand in modules
* web
* batch

present in the test resources' folder in the modules
* web
* core

are not versioned. This means that you will need to have 
your own database to run the application
locally of the variables indicated in the documentation file.
If you need further information, please contact item@abes.fr.
