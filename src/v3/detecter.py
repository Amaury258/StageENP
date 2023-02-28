import os
import time

# Définition des paramètres
clé_usb = "/dev/sda1"  # chemin de la clé USB sous Linux
programme_java = "/chemin/vers/mon/programme.jar"  # chemin du programme Java
timeout = 10  # temps d'attente maximal en secondes

# Boucle principale
while True:
    if os.path.exists(clé_usb):
        # La clé USB est branchée
        print("Clé USB détectée, lancement du programme Java...")
        os.system("java -jar " + programme_java)
        
        # Attente que le programme Java se termine
        while True:
            pids = [pid for pid in os.listdir('/proc') if pid.isdigit()]
            for pid in pids:
                try:
                    cmdline = open(os.path.join('/proc', pid, 'cmdline'), 'rb').read()
                    if cmdline.startswith(b"java") and programme_java in cmdline.decode():
                        # Le programme Java est encore en cours d'exécution
                        break
                except IOError:  # Fichier introuvable ou accès refusé
                    pass
            else:
                # Le programme Java n'est plus en cours d'exécution
                break
            
    # Attente avant la prochaine itération
    time.sleep(1)
