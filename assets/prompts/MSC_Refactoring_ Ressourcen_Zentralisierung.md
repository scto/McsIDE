MCS Projekt-Refactoring & Ressourcen-Zentralisierungs Plan
Dieses Dokument ist der Master-Plan für die Konsolidierung des Projekts auf den Namespace com.scto.mcs, die Bereinigung der Gradle-Konfigurationen, die Zentralisierung aller Ressourcen und die Integration des Extension-Systems.
Projekt-Kontext
* Ziel-Package: com.scto.mcs
* Struktur: App, Core, Feature.
* Zu ersetzende Namespaces: com.rk, com.srvhive, com.scto.msc.
Schritt 1: Zentrale Build-Konfiguration & Version Catalog
Aider-Aufruf: /add build.gradle.kts settings.gradle.kts gradle/libs.versions.toml
1. Überprüfung: Untersuche libs.versions.toml auf Vollständigkeit (Hilt, KSP, Compose).
2. Inklusion: Stelle sicher, dass settings.gradle.kts alle Module (:core:*, :feature:*) inkludiert.
3. Plugins: Validiere Plugin-Definitionen im Root-Build-Script.
Schritt 2: Audit der Core-Module (Build-Logik)
Aider-Aufruf: /add core/**/build.gradle.kts
1. Namespaces: Setze Namespaces auf com.scto.mcs.core.<modulname>.
2. Hilt/KSP: Standardisiere die Konfiguration über alle Core-Module hinweg.
Schritt 3: Audit der Feature-Module (Build-Logik)
Aider-Aufruf: /add feature/**/build.gradle.kts
1. Namespaces: Setze Namespaces auf com.scto.mcs.feature.<modulname>.
2. Abhängigkeiten: Verknüpfe Features mit benötigten Core-Modulen.
Schritt 4: Quellcode-Refactoring (Features)
Für jeden Punkt: Package-Deklarationen und Imports von com.rk, com.srvhive, com.scto.msc auf com.scto.mcs.feature.<name> ändern.
* 4.1 Editor: /add feature/editor/**/*.kt feature/editor/src/main/AndroidManifest.xml
* 4.2 Git: /add feature/git/**/*.kt feature/git/src/main/AndroidManifest.xml
* 4.3 Settings: /add feature/settings/**/*.kt feature/settings/src/main/AndroidManifest.xml
* 4.4 Terminal: /add feature/terminal/**/*.kt feature/terminal/src/main/AndroidManifest.xml
Schritt 5: Quellcode-Refactoring (Core-Submodule)
Migration auf den Namespace com.scto.mcs.core.<name>.
* 5.1 DI: /add core/di/**/*.kt
* 5.2 Exec: /add core/exec/**/*.kt
* 5.3 Files: /add core/files/**/*.kt
* 5.4 Navigation: /add core/navigation/**/*.kt
* 5.5 Network: /add core/network/**/*.kt
* 5.6 Resources: /add core/resources/**/*.kt
* 5.7 UI: /add core/ui/**/*.kt
* 5.8 Utils: /add core/utils/**/*.kt
* 5.9 Terminal (Logic): /add core/terminal/**/*.kt
* 5.10 Extensions: /add core/extension/**/*.kt (Neu: Extension Manager System)
Schritt 6: Zentralisierung der Ressourcen (Zwei Phasen)
6.1 Ressourcen-Verschiebung (Filesystem)
Aider-Aufruf: /add **/*.xml
1. Verschiebe alle res-Inhalte aus allen Modulen nach :core:resources (core/resources/src/main/res/).
2. Behalte die Unterordnerstruktur bei.
6.2 String-Management & Code-Anpassung
Aider-Aufruf: /add core/resources/src/main/res/values/*.xml **/*.kt **/*.java
1. Führe alle strings.xml zentral zusammen (Duplikate entfernen).
2. Ersetze hardkodierte Strings im Code durch R.string-Referenzen.
3. Biege alle Ressourcen-Imports projektweit auf com.scto.mcs.core.resources.R um.
Schritt 7: Extension System Integration
Aider-Aufruf: aider app/src/main/java/**/*.kt feature/**/*.kt core/extension/**/*.kt
1. Interface: Implementiere MCSExtension in den Modulen Git, Terminal und Editor.
2. Registry: Registriere diese Features beim App-Start im ExtensionManager.
3. DI: Stelle sicher, dass die Erweiterungen über Hilt-Multibindings automatisch geladen werden.
Schritt 8: App-Modul & Finale Integration
Aider-Aufruf: /add app/build.gradle.kts app/src/main/AndroidManifest.xml
1. Finalisiere :app auf com.scto.mcs.app.
2. Validiere Manifest (Pfade, Activities, Permissions).
3. Behebe letzte Kompilierfehler.