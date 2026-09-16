# Tropimon Catch Preview

By FastedCorsi

## Distribution et confidentialité

`build` exécute `privacyCheck` sur les fichiers partageables, le JAR final, le JAR sources et l'archive projet. Le contrôle inspecte aussi les constantes compilées, ressources et archives imbriquées ; ses tests utilisent des données fictives. Toute détection bloque le build sans afficher la valeur trouvée.

Les informations privées connues peuvent compléter la détection via `CATCH_PREVIEW_PRIVATE_TERMS` (variable d'environnement locale, une valeur par ligne, jamais versionnée). Les chemins de compte, e-mails non fictifs, formats courants de secrets et métadonnées auteur sont également contrôlés. Les crédits tiers doivent être examinés et préservés, pas supprimés pour contourner une alerte.

Partager seulement les artefacts de la version validée dans `build/libs` et l'archive projet dans `build/distributions`, jamais le dossier de travail complet. Les instances de test, logs, configurations, captures et anciennes archives locales sont conservés mais ne font pas partie de cette distribution. Le contrôle ne garantit pas de reconnaître un nom civil ou un employeur qui ne lui a pas été fourni.

Pour un environnement différent, définir `TROPIMON_HOME` ou fournir `-PcobblemonJar=<chemin-vers-cobblemon.jar>`. Le script de vérification accepte `-LauncherDirectory`. Aucune installation ou publication n'est exécutée par `build`.

Mod client Fabric 1.21.1 pour Cobblemon 1.7.2. Après une capture réussie, une fiche non bloquante apparaît à droite avec le modèle du Pokémon, son niveau, son sexe, sa nature, son talent et ses six IV.

Les captures directes réalisées sans entrer en combat sont également détectées.

- Une nouvelle capture remplace immédiatement la fiche précédente.
- La fiche se ferme automatiquement après 20 secondes. Chaque nouvelle capture relance ce délai ; une confirmation de relâchement en cours reste ouverte sans limite de temps.
- `Suppr` ferme la fiche pendant le jeu.
- Le bouton `×` est cliquable lorsqu'un écran libère le curseur (inventaire, chat, etc.).
- À proximité d'un PC Cobblemon, le bouton `REL` permet de relâcher le Pokémon après un second clic de confirmation.
- Le bandeau est déplaçable lorsque le curseur est libre (chat, inventaire, etc.). Fermer la fiche ne réinitialise pas sa position : la prochaine capture réutilise la même position, conservée pendant l'exécution du jeu.
- La notification n'ouvre aucun écran et n'interrompt pas les déplacements/combats. Un relâchement depuis le PC peut ouvrir brièvement son interface pour établir le lien serveur officiel.

## Version 0.6.2 — Insignes

L'icône officielle de l'onglet Insignes Cobblemon apparaît à gauche du niveau, réduite à 8 × 8 pixels depuis la version 0.6.6, sans réduire la place du nom du Pokémon. Son survol affiche tous les insignes/rubans possédés avec leur nom et leur description dans la langue du jeu, lorsque le curseur est libre (chat, inventaire…). Sans insigne, l'infobulle indique « Aucun insigne ». Aucun insigne potentiel n'est présenté comme acquis. L'affichage se met à jour si les insignes changent.

## Version 0.6.0

- Cadre Tropimon embarqué sous notre propre namespace, sans dépendance à TropimodClient.
- Aucun accès aux classes, états ou configurations des autres mods Tropimon. Le suivi des paquets de transfert Cobblemon remplace l'ancienne réflexion Team Builder.
- Les UUID des transferts et des synchronisations complètes de boîtes sont mémorisés pour toute la session, sans limite arbitraire. Le critère habituel « UUID inédit ajouté à une case vide après synchronisation initiale » reste inchangé.
- Différence volontaire de la protection : elle ignore précisément les Pokémon transférés, au lieu de masquer toutes les notifications pendant l'application d'une équipe. Une vraie capture simultanée peut donc rester visible.
- La localisation utilisée pour le bouton est mise en cache, y compris les résultats négatifs. Invalidation après les écritures/effacements/déplacements du stockage, les synchronisations complètes, changements de stockage, monde ou connexion.
- Scan PC identique : offsets X/Z de -5 à +5, Y de -3 à +3, coins inclus. Cache de 250 ms, invalidé au déplacement/changement de monde ; un PC disparu n'est plus accepté. Une seule position mutable sert au scan.
- Portrait et textes réutilisés. L'état d'animation reste actif à chaque image, les références visuelles restent vivantes, et les textes dynamiques sont rafraîchis au tick ainsi qu'au changement de Pokémon.
- Au second clic : recherche réelle par UUID, position actuelle, protection du dernier Pokémon de l'équipe et scan PC forcé. Aucun cache d'affichage n'autorise un relâchement.
- Après une ouverture PC différée : mêmes validations, avec contrôle du stockage et de la position confirmée. Si la cible a changé de case, disparu ou quitté le contexte valide, la demande est abandonnée.
- Confirmation sans limite de temps, UUID verrouillé, dernière capture suivante mise en attente. Le délai réseau de 5 secondes ne concerne que l'attente de la réponse d'ouverture du PC.
- Les restrictions serveur Cobblemon restent souveraines : l'envoi d'une demande ne constitue pas un accusé de relâchement.

## Vérification

`build` exécute les tests de détection, transferts, captures successives, confirmation, historique sans éviction, cache, portée PC, dernier Pokémon et déplacement de fenêtre. Les contrats de mixins sont vérifiés contre le bytecode du JAR Cobblemon, et le bytecode produit est contrôlé pour l'absence de dépendances Tropimon externes.

Audit facultatif des paquets réellement utilisés par le Team Builder installé :

```powershell
.\gradlew.bat -p .\TropimonCatchPreview build "-PcoexistenceAuditDir=$env:APPDATA/.tropimon/mods"
```

`tools/VerifyClient.ps1 -Mode standalone` lance le JAR remappé avec seulement Cobblemon, Fabric API et Fabric Language Kotlin dans `build/verify-standalone`.
`-Mode integrations` ajoute Team Builder, DamageCalc et ChatFilter, sans TropimodClient.
`-Mode coexistence` utilise un autre dossier isolé et ajoute les autres JAR Tropimon et leurs dépendances. Ces dépendances de test ne sont jamais ajoutées au mod livré. Le script ne copie aucune configuration ni sauvegarde de l'instance habituelle et ne remplace aucun JAR installé.

Ces lancements vérifient le démarrage et le chargement des ressources ; ils ne remplacent pas une validation de captures et relâchements réels sur serveur. Fermer l'instance de test avant de relancer le script.

Résultats du 30 août 2026 : 22 tests réussis avec l'audit Team Builder activé ; démarrage autonome et ressources chargées, puis même contrôle réussi avec Team Builder 0.59.3, DamageCalc 0.3.34 et ChatFilter 0.1.10. L'essai avec l'ensemble des JAR Tropimon bloque au démarrage de TropimodClient sur `fr/tropimon/tropimoncore/api/communication/PayloadApi$Compressor`, absent du core disponible dans l'environnement isolé. Aucune modification de ces autres mods n'a été effectuée. Les captures/relâchements réels sur serveur restent à valider.

## Compilation

Depuis le dossier parent :

```powershell
.\gradlew.bat -p .\TropimonCatchPreview build
```

Installation locale :

```powershell
.\gradlew.bat -p .\TropimonCatchPreview installTropimonCatchPreviewLocal
```


## Mises à jour automatiques

Le mod vérifie sa propre Release GitHub au démarrage, au maximum une fois toutes les six heures. Lorsqu'une version plus récente est disponible, son JAR et son SHA-256 sont contrôlés, puis la mise à jour est installée après l'arrêt de Minecraft avec sauvegarde de l'ancien JAR. Le launcher peut rester ouvert.

La vérification s'effectue en arrière-plan et n'ajoute aucun travail par tick. Elle peut être désactivée avec "enabled": false dans config/tropimon_catch_preview-updater.json.

