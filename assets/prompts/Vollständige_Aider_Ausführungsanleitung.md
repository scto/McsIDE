Aider Ausführungsanleitung: MSC Master Refactoring
Befolge diese Befehle nacheinander, um die Konsolidierung des Projekts abzuschließen.
Phase 1: Build & Namespaces (Schritt 1-3)
1. Zentral: aider build.gradle.kts settings.gradle.kts gradle/libs.versions.toml
2. Core Gradle: aider core/**/build.gradle.kts
3. Feature Gradle: aider feature/**/build.gradle.kts
Phase 2: Feature & Core Refactoring (Schritt 4-5)
Führe für jedes Submodul den /add Befehl aus dem Plan aus und nutze den Prompt:
"Migriere den Quellcode auf den Namespace com.scto.mcs.[core|feature].[name]. Ersetze dabei alle Vorkommen von com.rk, com.srvhive und com.scto.msc."
Phase 3: Ressourcen-Zentralisierung (Schritt 6)
6.1 Verschieben: aider **/*.xml
"Verschiebe alle Ressourcen physisch nach :core:resources. Lösche die Originaldateien erst nach erfolgreichem Move."
6.2 Strings & Code-Fix: aider core/resources/src/main/res/values/*.xml **/*.kt **/*.java
"Merge alle strings.xml. Ersetze hardkodierte Strings im gesamten Projekt durch R.string. Aktualisiere alle Ressourcen-Imports auf com.scto.mcs.core.resources.R."
Phase 4: Extension Manager Integration (Schritt 7)
aider app/src/main/java/**/*.kt feature/**/*.kt core/extension/**/*.kt
"Implementiere das MCSExtension-Interface für Git, Terminal und Editor. Richte die Hilt-Multibindings ein, damit diese Features beim App-Start automatisch im ExtensionManager registriert werden."
Phase 5: Finale (Schritt 8)
aider app/build.gradle.kts app/src/main/AndroidManifest.xml
"Setze den App-Namespace auf com.scto.mcs.app, korrigiere das Manifest und behebe verbleibende Kompilierfehler."