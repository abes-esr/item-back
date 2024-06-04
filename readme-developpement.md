# Utilisation des filtres de kibana-prod sur item pour travailler sur les logs

## Guide pour voir les logs d'item de traitement des demandes en production pour comprendre en cas d'incident ou de bug pourquoi ça n'a pas fonctionné

- se connecter sur https://kibana-prod.abes.fr/ avec vos identifiants

![img.png](documentation/img.png)

- selectionner son espace qui a été préalablement crée par le SIRE (aller voir le SIRE pour créer son espace)

![img.png](documentation/imgr.png)

- dans son espace, cliquer sur les elements suivants pour permettre d'avoir une visualisation des logs qui suivent un élément qu'on recherche dans le champ message
- dans **message** : on retrouve les logs qui sont dans item-batch

![img_1.png](documentation/img_1.png)

- prendre item-batch

![img_7.png](documentation/img_7.png)

![img_3.png](documentation/img_3.png)

![img_4.png](documentation/img_4.png)

![img_6.png](documentation/img_6.png)

- le + à coté du timestamp va permettre de partir de l'heure d'origine du premier message recherché qui correspond au match effectué dans le champ de la recherche

![img_8.png](documentation/img_8.png)

- sort old new pour les avoir dans l'ordre chronologique

![img_9.png](documentation/img_9.png)

- editer le filtre pour créer une plage de temps de recherche

![img_10.png](documentation/img_10.png)

![img_11.png](documentation/img_11.png)

- supprimer le filtre de recherche et refresh

![img_12.png](documentation/img_12.png)

- replacer le filtre du timestamp en chronologique

![img_13.png](documentation/img_13.png)

- on peut maintenant avoir toutes les lignes qui suivent la recherche par filtre effectuée (soit toutes les lignes qui suivent le traitement d'une demande)

## Pour retrouver les lignes traitées par le batch sur une demande spécifique comprise dans un plage (exemple le filtre dbeaver sur la demande 11548382)

![img.png](img.png)

- faire ensuite un filtre dans kibana avec en paramètre le num_lignefichier pour retrouver ce qui s'est passé