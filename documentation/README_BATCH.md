# ITEM

##Pouvoir lancer le batch en developement :

Avoir une copie propre du projet en local.
1. supprimer le dossier .m2 dans le dossier du user local
1. executer la command suivante dans le terminal
`mvn dependency:purge-local-repository clean install`

En cas de problème au lancement du batch

1. dans intellij faire CTRL + MAJ + ALT + S
1. Dans project structure > librairies supprimer les librairies suivantes
* log4j-to-slf4j
* log4j-slf4j-impl