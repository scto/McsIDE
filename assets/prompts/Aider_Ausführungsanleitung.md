# So führst du das MCS-Refactoring mit Aider aus
Jetzt ist alles perfekt vorbereitet! Der Ablauf ist dank deiner Projekt-Kontext-Dateien extrem systematisch und einfach. Du arbeitest den Plan quasi wie eine sichere Checkliste ab, ohne dass dir das Token-Limit um die Ohren fliegt.
Hier ist die genaue Schritt-für-Schritt-Anleitung:
## Schritt 1: Aider starten
 1. Öffne dein Terminal (Termux).
 2. Starte dein Skript: ./aider_launcher.sh
 3. Wähle **1) Gemini**, dann **2) Gemini Flash Modelle**, und dort am besten **1) Gemini 2.0 Flash** (oder 3 für Gemini 1.5 Flash 002).
 4. Wähle den **Architect Mode** (Option 2), damit Aider bei den komplexen Übersetzungen und Verschiebungen erst den Plan prüft, bevor es Code ändert.
## Schritt 2: Das "Gedächtnis" laden
Sobald Aider läuft und du die Eingabeaufforderung (aider) siehst, kopierst du einfach den *gesamten Inhalt* deiner .aiderContext.md Datei und fügst ihn direkt in den Chat ein:
```text
/read project-context/projectOverview.md
/read project-context/systemDesign.md
/read project-context/techEnvironment.md
/read project-context/testStrategy.md
/read project-context/uiStrategy.md
/add project-context/activeDevelopment.md
/add project-context/progress.md
/add .aiderRules.md

```
*(Drücke Enter. Aider lädt nun leise alle deine Architektur-Regeln in sein Gedächtnis, liest was bisher passiert ist und weiß sofort Bescheid.)*
## Schritt 3: Den Plan abarbeiten (Beispiel: Schritt 1)
Jetzt nimmst du dir deinen Aider_App_Refactor_Plan.md vor. Du beginnst mit **Schritt 1: Header-Kommentare entfernen**.
Kopiere den "Aider-Aufruf" (den /add Befehl) und den dazugehörigen "Prompt" zusammen und füge sie in den Chat ein:
```text
/add app/src/main/java/com/web/webide/**/*.kt app/src/main/java/com/web/webide/**/*.java

Entferne in allen geladenen Dateien jegliche Header-Kommentare (sowohl Blockkommentare /* ... */ als auch Zeilenkommentare //), die sich ganz oben am Anfang der Datei befinden. 
Die absolute erste Code-Zeile in JEDER Datei muss zwingend das Schlüsselwort `package` sein. Lösche keine Kommentare, die sich innerhalb von Klassen oder Funktionen befinden.

```
*(Drücke Enter. Aider wird nun die Dateien analysieren, die Kommentare löschen und selbstständig einen Git-Commit erstellen. Im Architect-Modus fragt es dich eventuell vorher mit (Y/n), ob du dem Plan zustimmst – drücke einfach Enter für Yes).*
## Schritt 4: ⚠️ WICHTIG - Speicher leeren!
Da du bei Android-Projekten schnell ans Token-Limit (Context Window Exceeded) stößt, musst du nach *jedem* erfolgreich abgeschlossenen und committeten Schritt den Arbeitsspeicher leeren, bevor du den nächsten Teil des Plans lädst.
Tippe nacheinander ein:
```text
/drop
/clear

```
*(Dadurch werden die hunderten bearbeiteten Kotlin/Java-Dateien aus Schritt 1 aus dem aktiven Fenster geworfen. Deine Regeln aus dem project-context behält Aider jedoch sicher im Langzeitgedächtnis!)*
## Schritt 5: Nächster Schritt
Gehe wieder in deinen Refactoring-Plan, kopiere "Schritt 2: Verschieben & Package-Namen anpassen" (wieder den /add Befehl und den Text) und wirf ihn in den Chat.
Warte bis Aider fertig ist, mache wieder /drop und /clear, und mache dann mit Schritt 3 weiter.
### 💡 Dein Workflow-Mantra
Dein kompletter Workflow für die nächste Stunde ist also nur noch dieser Rhythmus:
 1. **Plan-Schritt kopieren & einfügen.**
 2. **Aider zaubern lassen (Y drücken).**
 3. **/drop und /clear tippen.**
 4. **Nächsten Schritt nehmen.**
So arbeitest du dich sicher durch das gesamte Modul, reparierst die Namespaces und übersetzt die App fließend ins Englische, ohne dass die KI jemals überlastet wird!
