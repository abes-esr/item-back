# item-api

Le langage utilisé est Java, avec le framework Spring. 

**item-api** est une API permettant de :
1. calculer un taux de recouvrement
2. créer, modifier, supprimer des exemplaires par lot dans des notices
3. récupérer les informations récapitulatives des demandes de recouvrement, de créations, de modification et de suppression
4. récupérer les données des demandes de création, de modification et de suppression sous forme de fichiers

## Architecture de l'API

**item-api** est composée de trois modules : `batch`, `core`, `web` et d'un répertoire `docker`.
Chaque demande envoyée par le client item-client se voit attribuer un numéro de demande et un état. 
Ce dernier changera en fonction de l'avancée de la saisie et du traitement de la demande.

### Module `batch`

Le module `batch` permet d'effectuer les traitements de création, de modification et de suppression permettant 
l'aboutissement des demandes enregistrées dans la base de données PostgreSQL. 
Il se lance à intervals réguliers, qui sont définis dans le fichier `item-api/docker/batch/tasks.tmpl`.

#### Lancement des différents batchs en local :

Dans votre fichier application-localhost.properties, placer la variable suivante :
- pour lancer le batch d'exemplarisation
spring.batch.job.name=traiterLigneFichierExemp
- pour lancer le batch de modification
spring.batch.job.name=traiterLigneFichierModif
- pour lancer le batch de suppression
spring.batch.job.name=traiterLigneFichierSupp
- pour lancer le batch de recouvrement
spring.batch.job.name=traiterLigneFichierRecouv

### Module `core`

Le module `core` permet de mettre à jour les informations de la demande au fur et à mesure que celle-ci est saisie
dans le client **item-client**. Ces informations sont enregistrées dans la base de données PostgreSQL.

Le fichier `core/src/main/java/fr/abes/item/core/constant/Constant.java` contient la plupart des associations 
paramètres/valeurs intangibles qui seront utilisées dans **item-api**. 
On y trouvera notamment les numéros des différents états qu'une demande peut avoir (ex : `ETATDEM_PREPARATION = 1`)
avec la signification de l'état. Il s'y trouve aussi la plupart des messages de complétion ou d'erreur que l'api 
sera amenée à renvoyer, les REGEX et le nombre maximal de lignes par fichier que chaque type de demande peut accepter.

### Module `web`

Le module `web` permet d'exposer les webservices permettant au client **item-client** d'intéragir avec l'API. 
Ces webservices se trouvent dans le package `web/src/main/java/fr/abes/item/web`.
C'est également dans ce module que l'on trouvera le service d'authentification 
(dans le package `web/src/main/java/fr/abes/item/security`) 
Le fichier `web/src/main/resources/application.properties` contient le paramètre spécifiant le nom 
du répertoire de stockage sur le disque des fichiers liés aux demandes (`files.upload.path=/workdir/`)

### Répertoire `docker`

Le répertoire `docker` contient les scripts shell de traitement, d'archivage, d'export de statistiques et les fichiers
***docker-entrypoint.sh*** et ***tasks.tmpl***

## Configuration de l'API

Les fichiers :
* application.properties
* application-dev.properties
* application-prod.properties
* application-test.properties

sont présents dans les dossiers ```src/main/ressources``` des modules :
* web
* batch

Ces fichiers sont non-versionnés. Cela signifie que certains paramètres
sont définis sans variables et qu'il vous appartiendra de les définir vous-même selon vos usages,
notamment concernant les accès aux bases de données.
Si vous avez besoin de plus de précisions, vous pouvez vous adresser à [envoyer un mail à item@abes.fr](item@abes.fr)
