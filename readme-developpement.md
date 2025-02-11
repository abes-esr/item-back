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

### Cas particulier de la maintenance V2

En v2 les fichiers de propriété doivent contenir :
- spring.jpa.item.database-platform=org.hibernate.dialect.Oracle12cDialect
- spring.jpa.item.properties.hibernate.dialect=org.hibernate.dialect.Oracle12cDialect

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

## En cas de problème avec la base de donnée (en développement et en test) : réinitialiser la base item (postgresql)

Avec un logiciel client, supprimer le schema public, le recréer, puis lancer en local item-api avec cette configuration (en se branchant sur jdbc:postgresql://diplotaxis5-dev.v212.abes.fr:18083/item)

- fichier application-localhost.properties du module web
```xml
spring.jpa.item.generate-ddl=true
spring.jpa.item.show-sql=false
spring.jpa.item.hibernate.ddl-auto=create-drop
spring.sql.item.init.mode=always
spring.hibernate.item.enable_lazy_load_no_trans=false
```

- fichier application-localhost.properties du module batch
```xml
spring.jpa.item.generate-ddl=true
spring.jpa.item.show-sql=false
spring.jpa.item.hibernate.ddl-auto=create
spring.sql.item.init.mode=always
spring.hibernate.item.enable_lazy_load_no_trans=true
spring.batch.jdbc.initialize-schema=always
```

repasser ensuite la configuration des fichiers comme suit

- application-localhost.properties du module web
```xml
spring.jpa.item.generate-ddl=false
spring.jpa.item.show-sql=false
spring.jpa.item.hibernate.ddl-auto=none
spring.sql.item.init.mode=never
spring.hibernate.item.enable_lazy_load_no_trans=false
```

- application-localhost.properties du module batch
```xml
spring.jpa.item.generate-ddl=false
spring.jpa.item.show-sql=false
spring.jpa.item.hibernate.ddl-auto=update
spring.sql.item.init.mode=never
spring.hibernate.item.enable_lazy_load_no_trans=true
spring.batch.jdbc.initialize-schema=never
```

Attention il faudra penser également à lancer les instruction sql suivantes sur la base pour les tables : 
(en cas de violation de contrainte, créer les données dans l'ordre des tables permettant de l'éviter)

table etat_demande
```sql
INSERT INTO public.etat_demande (num_etat,libelle) VALUES
	 (1,'En préparation'),
	 (2,'Préparée'),
	 (3,'A compléter'),
	 (4,'En simulation'),
	 (5,'En attente'),
	 (6,'En cours de traitement'),
	 (7,'Terminé'),
	 (8,'En erreur'),
	 (9,'Archivé'),
	 (10,'Supprimé');
INSERT INTO public.etat_demande (num_etat,libelle) VALUES
	 (11,'Annulé');
```
table index_recherche
```sql
INSERT INTO public.index_recherche (index_zones,num_index_recherche,code,libelle) VALUES
	 (1,1,'ISBN','ISBN'),
	 (1,2,'ISSN','ISSN'),
	 (1,3,'PPN','PPN'),
	 (1,4,'SOU','Numéro Source'),
	 (3,5,'DAT','Date;Auteur;Titre');
```

table index_recherche_type_exemp
```sql
INSERT INTO public.index_recherche_type_exemp (num_index_recherche,num_type_exemp) VALUES
	 (1,1),
	 (1,3),
	 (2,2),
	 (3,1),
	 (3,2),
	 (3,3),
	 (4,1),
	 (4,2),
	 (4,3),
	 (5,3);
```

table role
```sql
INSERT INTO public."role" (num_role,libelle,user_group) VALUES
	 (1,'Admin','ABES'),
	 (2,'Utilisateur','coordinateur');
```

table sous_zones_autorisees
```sql
INSERT INTO public.sous_zones_autorisees (mandatory,num_sous_zone,num_zone,libelle) VALUES
	 (false,1,1,'$a'),
	 (false,2,2,'$c'),
	 (false,3,2,'$d'),
	 (false,4,2,'$e'),
	 (false,5,2,'$a'),
	 (false,6,2,'$i'),
	 (true,7,2,'$j'),
	 (false,8,2,'$v'),
	 (false,9,2,'$2'),
	 (false,10,3,'$a');
INSERT INTO public.sous_zones_autorisees (mandatory,num_sous_zone,num_zone,libelle) VALUES
	 (false,18,5,'$a'),
	 (false,19,6,'$a'),
	 (false,20,7,'$a'),
	 (false,21,7,'$b'),
	 (false,22,7,'$c'),
	 (false,23,7,'$d'),
	 (false,24,7,'$x'),
	 (false,25,8,'$l'),
	 (false,26,8,'$z'),
	 (false,27,8,'$q');
INSERT INTO public.sous_zones_autorisees (mandatory,num_sous_zone,num_zone,libelle) VALUES
	 (false,28,8,'$u'),
	 (false,29,8,'$9'),
	 (false,30,9,'$a'),
	 (false,31,10,'$a'),
	 (false,32,10,'$b'),
	 (true,42,11,'$a'),
	 (false,43,11,'$k'),
	 (false,44,11,'$4'),
	 (false,45,12,'$a'),
	 (false,46,12,'$b');
INSERT INTO public.sous_zones_autorisees (mandatory,num_sous_zone,num_zone,libelle) VALUES
	 (false,47,12,'$c'),
	 (false,48,10,'$f'),
	 (false,49,8,'$b'),
	 (false,50,8,'$y'),
	 (false,51,2,'$l'),
	 (false,52,2,'$k'),
	 (false,53,8,'$x');
```

table traitement
```sql
INSERT INTO public.traitement (num_traitement,libelle,nom_methode) VALUES
	 (1,'Créer une nouvelle zone','creerNouvelleZone'),
	 (2,'Créer une sous-zone','ajoutSousZone'),
	 (3,'Remplacer une sous-zone','remplacerSousZone'),
	 (4,'Supprimer une sous-zone','supprimerSousZone'),
	 (5,'Supprimer une zone','supprimerZone');
```

table type_exemp
```sql
INSERT INTO public.type_exemp (num_type_exemp,libelle) VALUES
	 (1,'Monographies électroniques'),
	 (2,'Périodiques électroniques'),
	 (3,'Autres ressources (monographies imprimées)');
```

table zone_autorisees
```sql
INSERT INTO public.zones_autorisees (num_zone,indicateurs,label_zone) VALUES
	 (1,'##','917'),
	 (2,'##','930'),
	 (3,'##','991'),
	 (5,'##','E316'),
	 (6,'##','E317'),
	 (7,'##','E319'),
	 (8,'4#','E856'),
	 (9,'##','L035'),
	 (10,'##','915'),
	 (11,'41','955');
INSERT INTO public.zones_autorisees (num_zone,indicateurs,label_zone) VALUES
	 (12,'##','920');
```

table zone_autorisees_type_exemp
```sql
INSERT INTO public.zones_autorisees_type_exemp (zonesautorisees_num_zone,zonestypesexemp_num_type_exemp) VALUES
	 (1,1),
	 (2,1),
	 (3,1),
	 (5,1),
	 (6,1),
	 (7,1),
	 (8,1),
	 (9,1),
	 (10,1),
	 (1,2);
INSERT INTO public.zones_autorisees_type_exemp (zonesautorisees_num_zone,zonestypesexemp_num_type_exemp) VALUES
	 (2,2),
	 (3,2),
	 (5,2),
	 (6,2),
	 (7,2),
	 (8,2),
	 (9,2),
	 (11,2),
	 (1,3),
	 (2,3);
INSERT INTO public.zones_autorisees_type_exemp (zonesautorisees_num_zone,zonestypesexemp_num_type_exemp) VALUES
	 (3,3),
	 (5,3),
	 (6,3),
	 (7,3),
	 (9,3),
	 (10,3),
	 (12,3);
```

Ces fichiers sont non-versionnés. Cela signifie que certains paramètres
sont définis sans variables et qu'il vous appartiendra de les définir vous-même selon vos usages,
notamment concernant les accès aux bases de données.
Si vous avez besoin de plus de précisions, vous pouvez vous adresser à [envoyer un mail à item@abes.fr](item@abes.fr)
