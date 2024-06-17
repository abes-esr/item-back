# Documentation du fichier de configuration Log4j2

Ce fichier de configuration XML est utilisé pour configurer le framework de journalisation Log4j2. Il définit les propriétés, les appenders et les loggers utilisés pour la gestion des logs de l'application.

## Propriétés

Les propriétés suivantes sont définies dans la section `<Properties>` :

- `logBaseDir` : Le répertoire de base où les fichiers de logs seront stockés. Il est composé de deux parties : `${bundle:application:application.basedir}` et `${bundle:application:log4j2.logdir}`, qui sont des variables définies dans l'application **dans les fichiers de propriétés .properties**.
- `logFileName` : Le nom du fichier de log principal. Il utilise la variable `${bundle:application:application.name}` pour obtenir le nom de l'application.
- `errorFileName` : Le nom du fichier de log dédié aux erreurs. Il est composé du nom de l'application suivi de `_error`.
- `debugFileName` : Le nom du fichier de log dédié au débogage. Il est composé du nom de l'application suivi de `_debug`.
- `sizeTriggerPolicy` : La taille maximale d'un fichier de log avant qu'un nouveau fichier soit créé. Ici, elle est définie à 200 Mo.
- `deleteAgeFile` : L'âge maximal des fichiers de log avant qu'ils soient supprimés. Ici, il est défini à 15 jours.
- `keepMostRecentFile` : Le nombre maximal de fichiers de log récents à conserver. Ici, il est défini à 10 fichiers.
- `keepMostRecentSize` : La taille totale maximale des fichiers de log récents à conserver. Ici, elle est définie à 100 Mo.

## Appenders

Les appenders définissent les destinations où les logs seront écrits. Trois appenders sont configurés dans ce fichier :

### Console

L'appender `Console` est utilisé pour afficher les logs dans la console. Il utilise un `PatternLayout` pour formater les logs avec le modèle suivant :
- `%style{%d{ISO8601}}{black}` : Affiche la date et l'heure au format ISO8601 en noir.
- `%highlight{%-5level }` : Affiche le niveau de log sur 5 caractères, en surbrillance.
- `[%style{%t}{bright,blue}]` : Affiche le nom du thread en bleu vif.
- `%style{%C{1.}}{dark,yellow}` : Affiche le nom de la classe (sans le package) en jaune foncé.
- `%msg%n%throwable` : Affiche le message de log et la stack trace des exceptions, le cas échéant.

### RollingFile

Trois appenders `RollingFile` sont configurés pour écrire les logs dans des fichiers :

1. `LogFile` : Fichier de log principal.
    - `fileName` : Le chemin complet du fichier de log, composé du répertoire de base et du nom du fichier de log principal.
    - `filePattern` : Le modèle de nom de fichier pour les fichiers de log archivés. Il inclut la date et un numéro d'index.
    - `PatternLayout` : Utilise un modèle simple pour formater les logs avec le niveau, la date, le thread, la classe et le message.
    - `Policies` :
        - `OnStartupTriggeringPolicy` : Crée un nouveau fichier de log au démarrage de l'application.
        - `SizeBasedTriggeringPolicy` : Crée un nouveau fichier de log lorsque la taille du fichier actuel atteint la taille définie par `sizeTriggerPolicy`.
        - `TimeBasedTriggeringPolicy` : Crée un nouveau fichier de log à intervalles réguliers (par défaut, chaque jour).
    - `DefaultRolloverStrategy` :
        - `Delete` : Supprime les anciens fichiers de log en fonction des critères suivants :
            - `basePath` : Le répertoire de base des fichiers de log.
            - `maxDepth` : La profondeur maximale des sous-répertoires à parcourir.
            - `IfFileName` : Sélectionne les fichiers correspondant au modèle `*/${logFileName}_*.log.gz`.
            - `IfLastModified` : Sélectionne les fichiers plus anciens que la valeur définie par `deleteAgeFile`.
            - `IfAny` : Supprime les fichiers si l'une des conditions suivantes est remplie :
                - `IfAccumulatedFileCount` : Le nombre de fichiers dépasse la valeur définie par `keepMostRecentFile`.
                - `IfAccumulatedFileSize` : La taille totale des fichiers dépasse la valeur définie par `keepMostRecentSize`.

2. `ErrorFile` : Fichier de log dédié aux erreurs.
    - Utilise une configuration similaire à `LogFile`, mais avec les différences suivantes :
        - `fileName` et `filePattern` utilisent `errorFileName` au lieu de `logFileName`.
        - `LevelRangeFilter` est ajouté pour filtrer uniquement les logs de niveau ERROR.

3. `DebugFile` : Fichier de log dédié au débogage.
    - Utilise une configuration similaire à `LogFile`, mais avec les différences suivantes :
        - `fileName` et `filePattern` utilisent `debugFileName` au lieu de `logFileName`.
        - `LevelRangeFilter` est ajouté pour filtrer les logs de niveau DEBUG à TRACE.

## Loggers

La section `<Loggers>` définit les loggers utilisés dans l'application.

- `<Root>` : Le logger racine qui capture tous les logs.
    - `level` : Le niveau de log minimal capturé par le logger racine. Ici, il est défini à "all", ce qui signifie que tous les niveaux de log seront capturés.
    - `<AppenderRef>` : Les références aux appenders utilisés par le logger racine. Ici, trois appenders sont référencés : `DebugFile`, `LogFile` et `ErrorFile`.

## Options de configuration supplémentaires

- `<LevelRangeFilter>` : Permet de filtrer les logs en fonction d'une plage de niveaux de log.
    - `minLevel` : Le niveau de log minimum inclus dans la plage.
    - `maxLevel` : Le niveau de log maximum inclus dans la plage.
    - `onMatch` : L'action à effectuer lorsqu'un log correspond à la plage (ACCEPT pour inclure le log, DENY pour l'exclure).
    - `onMismatch` : L'action à effectuer lorsqu'un log ne correspond pas à la plage.

- `<PatternLayout>` : Permet de définir le modèle de formatage des logs.
    - `pattern` : Le modèle de formatage des logs, composé de différents éléments tels que la date, le niveau de log, le thread, la classe, le message, etc.

- `<Delete>` : Permet de configurer la suppression des anciens fichiers de log.
    - `basePath` : Le répertoire de base des fichiers de log.
    - `maxDepth` : La profondeur maximale des sous-répertoires à parcourir.
    - `<IfFileName>` : Sélectionne les fichiers correspondant à un modèle spécifique.
        - `glob` : Le modèle de nom de fichier à utiliser pour la sélection.
    - `<IfLastModified>` : Sélectionne les fichiers en fonction de leur date de dernière modification.
        - `age` : L'âge maximal des fichiers à conserver.
    - `<IfAny>` : Supprime les fichiers si l'une des conditions suivantes est remplie :
        - `<IfAccumulatedFileCount>` : Le nombre de fichiers dépasse une valeur spécifiée.
            - `exceeds` : La valeur maximale du nombre de fichiers à conserver.
        - `<IfAccumulatedFileSize>` : La taille totale des fichiers dépasse une valeur spécifiée.
            - `exceeds` : La valeur maximale de la taille totale des fichiers à conserver.
