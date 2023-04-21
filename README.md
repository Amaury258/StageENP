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

J'ai continué mes recherches sur un moyen d'utilisé l'antivirus BitDefender en ligne de commande ou dans mon programme
java, et j'ai compris qu'il etait impossible de l'utilisé ainsi avec cette version de l'antivirus, mais j'ai aussi 
remarqué autre chose, lorsqu'on connecte la clé USB sur la machine, l'antivirus lance une analyse automatiquement, 
il me manquais plus qu'a trouver le fichier de log.

Ce fichier log est un fichier XML qui contient les informations sur l'analyse comme le nombre de fichiers infecté 
ou alors le fichier qu'on a analyser, ici je vais me servir uniquement du fichier analysé et du nombre de fichiers 
infecté ou dangereux, si il y a au moins un fichier infecté ou dangereux, la clé est éjecté instantanément, sinon
on continue l'execution du programme et créer le TAG.

J'ai ensuite remarqué qu'il y avait un probleme avec l'attente du fichier log, des fois, on attendais pendant longtemps
pour rien, j'ai donc créé un Thread qui vérifie avec une boucle infinie si un fichier à été créer dans un 
certain dossier, si c'est le cas on utilise break; pour sortir de la boucle et fermer le Thread.

J'ai fais la meme chose pour vérifier que la clé a bien été analysé, si on débranche la clé avant ou pendant l'analyse,
cela peut generer une erreur qui risque de fermer l'execution du programme et nous on veux que ce programme 
tourne en boucle, j'ai donc fais un autre Thread qui vérifie la liste des disques, si la clé est toujours connecté, 
on fais rien, sinon on sort de la boucle. Normalement, ce Thread ne devrais jamais etre arreter depuis lui meme mais 
si c'est le cas, ça veux dire que la clé a été retiré avant la fin de l'analyse.

<h2>Semaine 4</h2>

Pour commencer cette semaine, j'ai ajouté la possibilité de détecter les disque dur externe, mais en faisant ça 
j'ai remarqué quelque chose, l'antivirus ne detecte pas les disque dur comme clé USB, ce qui veux dire qu'il n'y a 
pas d'analyse automatique et donc on doit la faire mannuellement, n'ayant pas trouvé de solution qui pourrais faire 
lancer l'analyse automatiquement, j'ai donc décidé de demander l'analyse pour les disque dur externe et les clé USB.

Pour ça j'ai fais une fenetre "Tuto" qui explique comment faire l'analyse d'un périphérique USB qui s'ouvre quand on
detecte un périphérique USB.

Le projet fonctionne en partie, il manque juste la fonctionnalité pour detecter les téléphones et les ejecter mais 
pour ce qui est des fonctionnalités de bases, c'est à dire la détection de périphérique de stockage USB, l'analyse
de celui ci et enfin son ejection, tout fonctionne.

Maintenant il faut que je puisse faire en sorte que le programme java s'execute sous un fichier jar afin d'avoir une
version stable pour l'utiliser sur les machines clientes.

<h2>Semaine 5</h2>

Afin de transformer le projet en fichier executable jar, il faut que je créer un Artifact (on clique sur File, 
Project Structure puis il y a un onglet Artifacts puis il faut choisir quel type d'artifact on veut utiliser, 
ici on choisi JAR from Modules)

Une fois l'Artifact créé, il nous faut le construire, on vas donc dans Build, Build Artifact et ensuite on clique 
sur notre Artifact, et enfin on se retrouve avec notre fichier jar, pour l'utiliser il faut faire 
"java -jar monprojet.jar" ce qui lance le programme java.

Ceci fonctionne si il n'y a pas de librairies ajoutées, dans notre cas ça ne fonctionne pas et en plus, pour une
raison qui m'est inconnue, il manque des fichier pour JavaFX, j'ai trouvé une solution qui consiste à changer la commande
"java -jar" en "java --module-path chemin/vers/mes/modules/javafx --add-modules javafx.controls,javafx.fxml -jar monprojet.jar"
ce qui normalement lance le programme javafx si la bibliotheque JavaFX est bien ajouté à la variable d'environnement PATH.

Ce qui veux dire qu'il faut que je fasse un script qui vérifie si les deux bibliotheque (Java et JavaFX) sont bien
présents sur la machine, sinon il faut ajouter le chemin vers ceux ci dans la variable PATH (les bibliotheques Java et 
JavaFX seront données avec le projet).

J'ai aussi remarqué que USB Disk Ejector, qui me permet d'ejecter les périphériques USB, n'est pas compté comme une
bibliotheque, logique en soit, ce qui veut dire qu'il faut que je trouve un moyen d'y remedier, j'ai donc 
effectué quelque test et j'ai remarqué que l'utilisation de System.getProperty("user.dir") nous donne le dossier 
où se trouve le fichier jar, ce qui veut dire qu'on peut y placer le logiciel USB Disk Ejector dans le meme dossier, 
j'ai donc décidé de le mettre dans un dossier lib donné avec le projet.

Ensuite j'ai eu plusieurs probleme, l'un d'entre eux est le fait que certains morceau de mon code pose probleme apres 
le passage vers le fichier jar, comme par exemple les fichiers temporaires que je stockais dans le dossier tmp 
sur le projet, n'existe pas pour le fichier jar, j'ai donc ajouté une vérification en début de programme qui 
d'abord vérifie si le dossier tmp existe et le crée sinon.

J'ai ensuite remarqué que le programme java depuis le jar ne peut pas acceder aux script batch, ce qui veut dire 
qu'il faut que le script ejecter.cmd doit etre dans le meme dossier que le script qui lance le programme.

J'ai aussi eu quelque probleme avec les commande windows, notament celles qui crée un raccourci, lorsque je faisais 
le raccourci, on ne pouvais l'executer, c'etait juste une erreur dans le nommage du fichier raccourci qui devait avoir 
comme extension .lnk . Il y avait aussi le PATH qui posait probleme, j'ai remarqué sur la machine blanche 
(et je pense que c'est le meme cas sur les machine clientes) qu'il n'y avait pas la jdk de java, j'ai donc du utiliser
setx PATH "chemin/vers/la/jdk" /M pour ajouter la jdk au path du pc, ceci nessecite un redemarrage du pc donc juste apres
je demande avec set /p "input=Redemarrer? (o/n)" ce qui demande à l'utilisateur si il veut redemarrer le pc maintenant 
ou continuer ce qu'il fait.

Maintenant que les problemes trouvés ont été reglé il manque plus qu'a faire passer le programme sur la machine blanche
et cliente, la partie la plus drole sera pour la machine cliente puisqu'il faut que l'ordinateur appelle le 
programme sur le serveur, tant dis que pour la machine blanche il s'agit juste de transferer l'executable jar 
(avec ce qu'il a besoin pour tourner) sur la machine blanche, donc rien de bien compliqué.

<h2>Semaine 6</h2>
<<<<<<< HEAD

Lors de la 6eme Semaine je me suis concentré sur les téléphones puisque l'analyse et l'ejection des périphérique USB 
(clé ou disque dur externe) est presque totalement terminé.

Tout d'abord il faut pouvoir les detecter, j'ai d'abord trouvé une solution qui consiste à utiliser les marques et
les modeles de téléphones, ce qui veux dire que lorsqu'il y a un nouveau modele ou une nouvelle marque de téléphone, 
il faudrat l'ajouter à un fichier ou une base de donnée, ce qui risque d'etre compliqué à gerer vue le nombre de 
nouveau modele, il fallait donc que je trouve une autre solution.

Avec un peu de recherche en ligne de commande j'ai trouvé grace à la commande wmic qu'on pouvait vérifier le service 
d'un périphérique, plus précisement avec la commande "wmic path Win32_PnpEntity get DeviceID,Service" ce 
qui donne le DeviceID et le Service des périphériques connecté au pc.

Pour les téléphones, le service est MTP, il suffit donc de vérifier si le service contient 'MTP', pour ce faire je 
fais comme en SQL, j'utilise l'operateur LIKE comme ceci "wmic path Win32_PnpEntity where "Service like '%mtp%'" get DeviceID"
ce qui nous donne le DeviceID des périphérique qui utilisent le service MTP.

Maintenant qu'on peut les détecter sur l'ordinateur il faut pouvoir les désactiver, j'ai trouvé la commande pnputil 
qui nous permet de manipuler les pilote des périphériques, avec la commande pnputil /disable-device <DeviceID> on peut 
désactiver un périphérique, ce qui veut dire qu'on peut désactiver un téléphone avec cette commande.

Il y a juste un petit probleme avec cette commande, c'est qu'elle a besoin des droit administrateur, ce qui est 
evidemment pas donné à tout le monde dans l'etablissement, ce qui veut dire qu'il faut que trouve une solution pour 
utiliser mon application java en administrateur depuis un compte utilisateur.

<h2>Semaine 7</h2>

Cette semaine j'ai continué mes recherches pour utiliser mon application en administrateur, j'ai d'abord trouvé 
la commande runas qui doit executer une commande en tant qu'un certain utilisateur, ce qui veut dire qu'on peut 
théoriquement executer une commande en administrateur, le probleme c'est qu'il demande le mot de passe du compte à 
l'utilisateur, ce qui veut dire qu'a chaque démarage du pc, l'utilisateur doit entrer le mot de passe administrateur 
pour lancer l'application et comme il ne le connais pas (du moins il est pas sensé le connaitre) il vas annuler 
l'execution et donc l'application servira à rien.

J'ai donc cherché une autre solution, j'ai trouvé un outil qui s'appelle psexec qui pourrais etre utilisé pour executer 
une commande en administrateur comme runas mais avec comme option le mot de passe, mais l'outil ne fonctionnais pas 
meme en ayant les droit administrateur.

J'ai ensuite trouvé les services windows, d'abord on creer le service avec sc create avec les options pour définir 
l'utilisateur comme l'identifiant et le mot de passe et ensuite on le lance avec sc start mais cela ne fonctionne 
toujours pas, j'ai ensuite trouvé les taches planifié windows et j'ai meme essayé d'ajouter manuellement dans le 
Planificateur de taches, malheureusement je suis tombé sur le meme résultat que les service windows.

<h2>Semaine 8</h2>

C'est la derniere semaine, ce qui veut dire qu'on a plus beaucoup de temps pour finir l'application, je décide donc de 
ne pas faire la partie téléphone et de me focaliser sur la partie périphérique de stockage, il fallait que je vérifie 
que tout fonctionne correctement, et j'ai remarqué une erreur dans l'utilisation du logiciel USB_Disk_Ejector, 
le logiciel essayais d'ejecter le disque dur C au lieu d'ejecter le périphérique connecté, apres un peu de réflection et
une relecture du code, j'ai remarqué que lorsqu'on vérifie si le disque est interne ou externe, au lieu 
d'initier le résultat de la méthode à true pour dire qu'il s'agit d'un disque interne, j'ai mis à false et en plus lors 
de la vérification du MediaType, au lieu de vérifier si il ne contenait pas la chaine de caractere "Fixed" je vérifiais 
si il la contenait, apres avoir résolu ces deux problemes, l'application fonctionne correctement.
=======
>>>>>>> 59353e1f3f5f522ac7249baf45320f33b2dfb941

