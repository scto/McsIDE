# Aider Refactoring Plan: App Modul (Namespace, Headers & Translation)
Dieser Plan konzentriert sich **ausschließlich auf das :app Modul**. Er dient dazu, den veralteten Namespace com.web.webide auf com.scto.mcs zu migrieren, alle Header-Kommentare zu entfernen und eine vollständige Übersetzung von Chinesisch nach Englisch durchzuführen.
## Schritt 1: Header-Kommentare entfernen
**Aider-Aufruf:** /add app/src/main/java/com/web/webide/**/*.kt app/src/main/java/com/web/webide/**/*.java
**Prompt für Aider:**
```text
Entferne in allen geladenen Dateien jegliche Header-Kommentare (sowohl Blockkommentare /* ... */ als auch Zeilenkommentare //), die sich ganz oben am Anfang der Datei befinden. 
Die absolute erste Code-Zeile in JEDER Datei muss zwingend das Schlüsselwort `package` sein. Lösche keine Kommentare, die sich innerhalb von Klassen oder Funktionen befinden.

```
## Schritt 2: Verschieben & Package-Namen anpassen
**Aider-Aufruf:** /add app/src/main/java/com/web/webide/**/*.kt app/src/main/java/com/web/webide/**/*.java
**Prompt für Aider:**
```text
Führe folgende Migration für das gesamte App-Modul durch:
1. Verschiebe alle Dateien physisch von `app/src/main/java/com/web/webide/` nach `app/src/main/java/com/scto/mcs/`. Achte darauf, dass die Unterordner-Struktur erhalten bleibt.
2. Ändere in allen verschobenen Dateien die Package-Deklaration von `package com.web.webide...` zu `package com.scto.mcs...`.
3. Passe alle internen Imports an, sodass sie auf den neuen Namespace `com.scto.mcs` verweisen.

```
## Schritt 3: Manifest & Gradle Konfiguration updaten
**Aider-Aufruf:** /add app/build.gradle.kts app/src/main/AndroidManifest.xml
**Prompt für Aider:**
```text
Aktualisiere die Build- und Manifest-Dateien auf den neuen Namespace:
1. In `app/build.gradle.kts`: Ändere `namespace` und (falls vorhanden) `applicationId` von `com.web.webide` auf `com.scto.mcs`.
2. In `AndroidManifest.xml`: Ersetze alle Vorkommen von `com.web.webide` durch `com.scto.mcs` (insbesondere bei den `android:name` Attributen der Activities, Services und Receiver).
3. Stelle sicher, dass auch Provider-Authorities (z.B. `com.web.webide.fileprovider`) auf `com.scto.mcs.fileprovider` geändert werden.

```
## Schritt 4: XML String-Ressourcen übersetzen (Chinesisch -> Englisch)
**Aider-Aufruf:** /add app/src/main/res/values/strings.xml app/src/main/res/values-zh/**/*.xml *(und füge ggf. weitere value-Ordner hinzu, falls vorhanden)*
**Prompt für Aider:**
```text
Deine Aufgabe ist es, die App vollständig ins Englische zu übersetzen:
1. Gehe durch alle geladenen XML-Ressourcen-Dateien (insbesondere `strings.xml`).
2. Identifiziere alle Texte innerhalb der `<string>` Tags, die chinesische Zeichen enthalten.
3. Übersetze diese chinesischen Texte in passendes, flüssiges und entwicklerfreundliches Englisch. 
4. Behalte Formatierungszeichen (wie `%s`, `%d`, `\n`) exakt bei.

```
## Schritt 5: Hardkodierte Texte im Quellcode übersetzen (Chinesisch -> Englisch)
**Aider-Aufruf:** /add app/src/main/java/com/scto/mcs/**/*.kt app/src/main/java/com/scto/mcs/**/*.java
**Prompt für Aider:**
```text
1. Scanne alle geladenen Kotlin- und Java-Dateien im App-Modul nach hardkodierten Texten (Strings in Anführungszeichen `""`), die chinesische Zeichen enthalten (z.B. in Toasts, Logs, UI-Texten oder Konstanten).
2. Übersetze all diese chinesischen Texte in kontextuell passendes Englisch.
3. Fasse den restlichen Code nicht an, ändere nur die String-Inhalte.

```
## Schritt 6: Aufräumen & Cleanup
**Aider-Aufruf:** /run rm -rf app/src/main/java/com/web
**Prompt für Aider:**
```text
Bitte bestätige, dass der veraltete, nun leere Ordner `com/web/webide` erfolgreich gelöscht wurde. 
Führe einen kurzen visuellen Check durch und stelle sicher, dass keine `com.web.webide` Imports mehr in den aktiven Dateien des App-Moduls übrig geblieben sind.

```
