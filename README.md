# StageENP

<h1>Projet de mon stage de deuxième année</h1>

- Stocker les média usb
- contrôler si le média est un dispositif de stockage : clé, HDD…

- contrôler que le média est bien répertorié dans la base des dispositifs autorisés

Si le média est repertorié dans la base des dispositifs autorisés on envoie le média USB sur l’antivirus 
    si le média contient un potentiel virus, on éjecte le média USB
    sinon, on lance le petit programme qui nous demande ce qu'on veut faire (ouvrir dans l'explorateur, etc...)
sinon
    on éjecte le média USB 


<h2>Semaine 1</h2>


Au début, j'ai juste recherché sur internet comment ça pouvais être fait, j'ai trouvé plusieurs solutions impliquant 
une API USB4Java ou la librairie javax.usb.

J'ai ensuite trouvé une solution qui consiste à prendre les disques du pc en java et mon idée serais de parcourir 
la liste des disques et de prendre un attribut qui serait le type de disque avec la methode 
getSystemTypeDescription(disque) d'un objet FileSystemView.

En supposant que la deuxième solution fonctionne, il faudra d'abord bloquer le média USB, car sinon l'application 
ne sert à rien.

Il faudra vérifier si le périphérique est dans la base des périphériques autorisés, mais surtout à savoir ce que
c'est que cette base.

Et puis bien sûr, faire analyser le média USB sur l'antivirus ce qui nous donnera deux cas possibles :

    -> le périphérique contient un potentiel virus, on doit donc éjecter le média USB
    -> le périphérique ne contient pas de potentiel virus, on doit lancer le programme qui nous demande ce qu'on veux 
       faire avec ce média USB

J'ai ensuite continué de chercher des moyens d'améliorer la détection de clé USB et j'ai finalement utilisé la deuxième 
version de mon programme java.

Et une fois que j'ai fini d'améliorer la détection sur le programme, je suis passé à l'éjection de la clé USB.

J'ai passé beaucoup de temps dessus puisqu'il y avait toujours quelque chose qui ne marchait pas, soit je n'avais pas 
les droits administrateur, soit je n'avais pas l'API qu'il faut, que des petits problèmes.


J'ai d'abord essayé de lancer une commande "devcon" qui pouvait me permettre d'éjecter la clé USB, sauf que "devcon" 
est une commande qui vient du kit de développement windows (WDK) et que je ne pouvais pas l'installé sauf si je suis sur 
Visual Studio et j'étais sur Visual Studio Code

J'ai ensuite essayé une autre commande, cette fois la commande "mountvol" et ça fonctionnait, sauf que la clé devenait 
inutilisable sur le PC et on voulait juste qu'elle soit éjectée.

J'ai ensuite essayé d'utiliser une API java qui s'appelle JNA (Java Native Access) mais quand j'ai écrit le code qui 
me permettait d'éjecter la clé je me retrouvais avec des méthodes et des constantes qui n'existent plus dans la version 
actuelle.

J'ai donc finalement continué sur le lancement d'une commande en java et j'ai fait un petit script qui éjecte la clé 
USB avec un logiciel appelé "USB Disk Ejector" qui éjecte la clé USB et peut être appelé en ligne de commande avec 
"USB_Disk_Eject.exe /REMOVELETTER <lettre de la clé usb>" et ça fonctionne, la clé est éjectée sans aucun souci.

Ensuite j'ai commencé à regarder pour verrouiller la clé USB, la rendre inaccessible pour l'utilisateur le temps
qu'elle soit analyser par l'antivirus, et la déverrouiller ensuite.

J'ai d'abord essayé avec la commande icacls, mais il semblerait qu'elle ne fonctionne pas sur une clé USB alors j'ai 
cherché une alternative, j'ai d'abord trouvé une solution avec la bibliothèque JNA mais comme pour les fonctionnalités 
précédentes, la moitié des méthodes et/ou classes sont introuvables dans les versions récentes.

J'ai alors cherché d'autres bibliothèques et j'ai trouvé usb4java, j'ai trouvé qu'il faut que j'utilise l'id du 
fournisseur (vendor_id) et le l'id du produit (product_id). D'après ce que j'ai compris, je dois d'abord ouvrir une
session USB, envoyer une commande USB pour la verrouiller, faire ce que je veux avec ma clé, c'est-à-dire l'analyser 
avec l'antivirus et surement vérifier si elle figure sur la base des périphériques autorisés si c'est une base de 
données pour stocker les clés USB de l'école, et ensuite la déverrouiller et l'éjecter si elle contient un potentiel 
virus.


<h2>Semaine 2</h2>

Lors de cette semaine, j'ai compris plusieurs chose, tout d'abord, la base des périphériques autorisé ne servais à rien
dans notre contexte et qu'il fallait à la place créer une sorte de TAG qui permettrais de dire si oui ou non la clé usb
a été passé sur la machine blanche qui est la machine qui analysera la clé usb avec l'antivirus.

Ce qui veux dire qu'il faut que je fasse un programme java pour la machine blanche et un autre pour l'utilisateur (ce 
qui veux dire toute les autres machine).

Pour le premier programme, sur la machine blanche :

J'ai donc cherché un moyen de faire le TAG et avec Mr RIGAUD on a trouvé une solution qui consiste à prendre le GUID 
de la clé USB (qui se trouve dans le dossier caché "System Volume Information" sur la clé USB) et ensuite y mettre 
la liste des fichiers de la clé qui sera hashé (en SHA-256), et ensuite chiffrer le tout en AES.

Pour le deuxieme programme, sur la machine utilisateur : 

Si le fichier existe et est chiffré, ça voudra déja dire que la clé est passé sur la machine blanche au moins une fois,
maintenant il faut voir dans le cas où on décide de modifier ce TAG, normalement ça genere une érreur, ensuite on doit
vérifier que le TAG (qu'on créer à partir de la méthode creerTAG() qui genere le tag de base) est identique au TAG 
déchiffré.

J'ai ensuite cherché un moyen de faire l'analyse antivirus avec BitDefender (l'antivirus qu'utilise l'ecole de police)
depuis java et il faut une api spécial de Bitdefender, j'ai ensuite cherché une autre solution qui consiste à utiliser 
une commande (sur un terminal), sans succés, la commande sensé etre dans le dossier de BitDefender n'apparait pas.

<h2>Semaine 3</h2>


