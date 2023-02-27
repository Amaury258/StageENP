# StageENP

<h1>Projet de mon stage de deuxième année</h1>

- Stocker les média usb
- contrôler si le média est un dispositif de stockage : clé, HDD…

- contrôler que le média est bien répertorié dans la base des dispositifs autorisés

si le média est repertorié dans la base des dispositifs autorisés
    on envoi le media USB sur l’antivirus 
    si le média contient un potentiel virus, on ejecte le media USB
    sinon, on lance le piti programme qui nous demande ce qu'on veux faire (ouvrir dans l'explorateur, etc...)
sinon
    on ejecte le media USB 


<h2>Jour 1</h2>


Au début j'ai juste recherché sur internet comment ça pouvais etre fait, j'ai trouvé plusieurs solutions impliquant une API USB4Java ou la librairie javax.usb

J'ai ensuite trouvé une solution qui consiste à prendre les disque du pc en java (pour l'instant j'ai que le disque C puisque je n'ai pas de clé USB sous la main) et mon idée serais de parcourir la liste des disques et de prendre un attribut qui serait le type de disque avec la methode getSystemTypeDescription(disque) d'un objet FileSystemView.

En supposant que la deuxieme solution fonctionne, il faudra d'abord bloquer le média USB, car sinon l'application ne sert à rien

Il faudra vérifier si le périphérique est dans la base des périphériques autorisés, mais surtout à savoir ce que c'est que cette base.

Et puis bien sur, faire analyser le média USB sur l'anti-virus ce qui nous donnera deux cas possibles:

    -> le périphérique contient un potentiel virus, on doit donc éjecter le média USB
    -> le périphérique ne contient pas de potentiel virus, on doit lancer le programme qui nous demande ce qu'on veux faire avec ce média USB

J'ai ensuite passé le reste du temps à refaire les commits car j'ai remarqué que j'avais oublié certains trucs dans certains commits