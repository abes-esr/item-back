###Notice d'exemplaire

Une notice d’exemplaire correspond à l’exemplaire physique du document. Elle permet de localiser le
document et de fournir des données précises sur sa disponibilité.

Les pratiques en matière de catalogage des données d'exemplaire du Sudoc sont dictées par un double
souci :

- assurer aux utilisateurs une identification précise et univoque de la localisation des différents
exemplaires d'un document saisis dans le système pour une bibliothèque donnée, de façon à faciliter
notamment la fourniture du document.
- permettre aux bibliothèques de saisir tout ou partie des informations descriptives spécifiques à un
exemplaire, du moins celles relevant du catalogue (hors informations de disponibilité immédiate par
exemple) pour tout ou partie de leurs exemplaires d'un document donné.
Ainsi, les mentions minimales requises dans le format de saisie des données d'exemplaire se limitent :
- au n° RCR (n° de la bibliothèque de localisation),
- et à la nature exacte des possibilités de prêt ou de reproduction de l'exemplaire (Code PEB), à l'aide des
valeurs codées correspondantes. Cette information n'a pas vocation à se substituer aux spécifications
éventuellement plus précises qui seraient définies dans le module de prêt du système local.

###Formats d'échanges

[formats d'échanges](http://documentation.abes.fr/sudoc/manuels/echanges/formats_echanges/index.html#DonneesLocales)

###ILN, RCR, PPN, EPN

####Etablissements : RCR, ILN

Chaque bibliothèque, qu’elle soit déployée (réseau Sudoc) ou non (réseau Sudoc -
PS), est identifiée à 2 niveaux :
- à son propre niveau, par un numéro RCR
- au niveau de l’établissement auquel elle appartient, par un numéro ILN
Le numéro RCR correspond à une entité de localisation (une bibliothèque, une
section, un centre de documentation).
Il est attribué par l’ABES, et comprend 9 chiffres (Nomenclature du RCR : les 2
premiers chiffres correspondent au n° du département, les 3 suivants au code INSEE
de la commune, les 2 suivants au Type de bibliothèque *, et les 2 derniers au n°
séquentiel).
C’est avec ce numéro RCR que les bibliothèques sont aussi identifiées dans le
Répertoire des Centres de Ressources du CCFr.
Le numéro ILN (Internal Library Number) correspond à un établissement, qui utilise
pour toutes ses bibliothèques le même SIGB.
Pour les bibliothèques du Sudoc, l’établissement est souvent le SCD, et le numéro
ILN à 3 chiffres sera de forme 0XX ou 1XX ou 4XX (de 001 à 199, de 401 à 499).
Pour les bibliothèques du réseau Sudoc-PS, l’établissement est le Centre Régional,
et le numéro ILN à 3 chiffres sera de forme 2XX (de 200 à 299). 

####Notice : EPN, PPN

EPN : numéro d'exemplaire dans la base Sudoc appelé EPN (numéro d'identification de la notice d'exemplaire, sur 9 positions).

Un numéro pour s ’y retrouver
- 1 PPN par notice (9 caractères)
- 1 EPN par exemplaire (9 caractères)

Présentation des règles de construction des numéros identifiants dans le
Sudoc :
- PPN : 8 chiffres séquentiels + une clé de contrôle chiffre ou X, clé pour les notices
bibliogr. et autorité
- EPN : même principe de construction, on le trouve souvent associé au numéro de
bibliothèque sous la forme RCR:EPN, puisqu’un exemplaire n’a de sens que dans
une localisation donnée.