# Tropimon Catch Preview — règle permanente

Appliquer également les consignes du dépôt parent lorsqu'elles sont disponibles.

- Pour toute version, correction, optimisation ou fonctionnalité, l'attribution publique du développeur est exactement « By FastedCorsi » ; métadonnées Fabric : `"authors": ["By FastedCorsi"]`.
- Préserver les crédits/licences tiers, identifiants techniques, URLs publiques nécessaires et données de joueurs. Ne jamais remplacer aveuglément.
- Ne pas ajouter de nom civil, compte système, adresse personnelle/professionnelle, employeur, chemin personnel ou secret du développeur dans les fichiers partagés. Ne pas en recopier dans ces consignes, tests, listes versionnées ou rapports.
- Chemins portables uniquement ; données fictives pour les tests. Garder les configurations, jetons, logs, captures, sauvegardes, .env et .git hors des archives sans supprimer leurs originaux.
- Avant livraison : exécuter `build` et son contrôle `privacyCheck`, examiner toute détection et les nouveaux fichiers, vérifier métadonnées, constantes, ressources et archives imbriquées des JAR finaux. Ne livrer que les artefacts de la version contrôlée, pas tout le dossier build.
- Le contrôle utilise le compte local et, si fournies, les valeurs privées de `CATCH_PREVIEW_PRIVATE_TERMS` (une par ligne, via l'environnement, jamais dans un fichier versionné). Aucun détecteur générique ne remplace la revue des mentions civiles/professionnelles inconnues.
- Si un secret est découvert : masquer sa valeur, indiquer seulement son emplacement et les actions conseillées ; ne pas le tester, le révoquer automatiquement ou le cacher par obfuscation.
- Avant un commit explicitement autorisé : vérifier l'auteur ET le committer effectifs, pseudonyme FastedCorsi et adresse GitHub noreply existante et valide. Ne pas inventer l'adresse ni modifier la configuration Git globale. Sinon ne pas committer.
- Ne pas réécrire l'historique, publier ou pousser sans autorisation correspondante. L'installation locale suit exclusivement la règle de livraison différée ci-dessous, sauf consigne explicite contraire. Signaler séparément les anciennes copies/releases et les traces historiques : le nettoyage actuel ne les efface pas.
- Préserver les captures, caches, rendu, langues, insignes, déplacement de fenêtre, fermeture automatique et sécurité du relâchement. Conserver l'autonomie : aucun accès aux classes, états, configurations ou services internes d'autres mods Tropimon, aucune bibliothèque commune obligatoire.

## Deux livraisons JAR à chaque version

- À chaque livraison d'une version ou d'un changement de code, fournir deux JAR clairement séparés : un JAR local accompagné du système de mise à jour différée de l'instance du launcher, et un JAR prêt à partager. Utiliser deux dossiers ou noms explicites ; ne jamais installer les deux exemplaires simultanément.
- Les deux JAR proviennent de la même version validée et offrent les mêmes fonctionnalités. Ils peuvent être identiques octet pour octet : privilégier un petit script externe pour l'installation locale, sans dupliquer le code du mod ni embarquer ce mécanisme dans le JAR public.
- Le launcher peut rester ouvert : seule l'exécution du jeu Minecraft concerné bloque la mise à jour locale. Attendre l'arrêt du jeu avant de remplacer le JAR dans la bonne instance ; la fermeture du launcher n'est pas requise et ne prouve pas l'arrêt du jeu. Ne jamais forcer l'arrêt du launcher ou du jeu, toucher aux autres mods ni remplacer un fichier utilisé ou verrouillé.
- Cette demande constitue l'autorisation permanente de préparer et d'armer cette installation différée lors d'une livraison, sauf consigne explicite contraire pour la tâche. Une demande de conseil, d'audit ou de mise à jour des règles ne déclenche ni compilation ni installation.
- Réutiliser et adapter les outils locaux existants. Vérifier la cible exacte, l'intégrité du JAR et le résultat de la copie ; conserver une sauvegarde de l'ancien JAR hors du dossier des mods chargés. En cas de cible ambiguë, d'accès impossible ou de verrouillage, conserver le fichier préparé et signaler le blocage sans forcer.
- Le JAR partageable ne contient ni chemin personnel, configuration locale, secret, donnée privée ni outil d'installation spécifique à la machine. Appliquer les contrôles de confidentialité aux deux JAR et aux éventuels fichiers qui les accompagnent. Conserver l'attribution « By FastedCorsi » et les crédits tiers.
- Dans la livraison, indiquer les deux JAR et leur version, les contrôles effectués et l'état réel de l'installation locale : préparée, en attente de fermeture ou installée après vérification. Ne pas annoncer une installation réussie parce qu'un script a seulement été lancé.

## Code simple, lisible et efficace

- Préserver strictement la logique, les fonctionnalités et les protections. Chercher les gains utiles de performance, mémoire et poids sans rendre le code difficile à comprendre.
- Choisir la solution la plus simple qui répond au besoin actuel. Éviter les classes, interfaces, factories, couches de services, méthodes relais et dépendances ajoutées sans utilité concrète ; ne pas bâtir un framework pour un cas isolé.
- Garder des classes cohérentes et des méthodes lisibles quand leur séparation aide réellement. Ne pas tout fusionner dans une classe géante ni compacter le code : moins de fichiers ou de lignes ne garantit pas de meilleures performances.
- Réutiliser ce qui existe dans le mod ; supprimer le code mort seulement après vérification des usages, y compris mixins, réflexion, événements, ressources et compatibilité. Pas de réécriture générale pour une optimisation locale.
- Cibler les coûts identifiés : travail répété par tick ou par frame, scans, allocations, entrées/sorties et caches sans limite. Justifier les gains et vérifier les comportements concernés ; ne pas ajouter de cache, de thread ou d'abstraction préventive sans besoin démontré.
- Chaque mod reste autonome : aucune dépendance aux classes, états ou services internes de nos autres mods. Recréer dans le mod concerné la petite implémentation nécessaire plutôt qu'imposer une bibliothèque commune ; préserver les dépendances officielles nécessaires.

## Publication et mise à jour autonome

- Chaque version livrée est poussée sur le dépôt GitHub public propre à ce mod, puis publiée dans une Release dont le tag correspond exactement à la version.
- La Release contient un seul JAR partageable vérifié et son fichier SHA-256. Les JAR LOCAL, configurations et scripts propres à une machine ne sont jamais publiés.
- Ce mod embarque sa propre implémentation de mise à jour. Elle ne dépend d'aucune classe, bibliothèque ou service interne d'un autre mod Tropimon.
- La mise à jour accepte uniquement la Release officielle de ce dépôt, exige le SHA-256, vérifie l'identifiant et la version de fabric.mod.json, prépare le fichier hors du dossier mods, puis remplace l'ancien JAR seulement après l'arrêt de Minecraft. Elle ne force jamais l'arrêt du jeu ou du launcher et conserve une sauvegarde hors des mods chargés.
- Une évolution de l'updater doit rester légère, asynchrone et sans travail répété par tick ou par frame.

