# Übersicht der Gemini Modelle in Aider

Diese Liste erklärt die verschiedenen Gemini-Modelle, die dir in Aider zur Verfügung stehen. Die Modelle sind nach den Familien **Flash** (schnell & kostengünstig), **Pro** (hochkomplex & intelligent) und **Spezial/Experimentell** geordnet.

*Hinweis zum Token Limit:* Die modernen Gemini 2.x und 3.x Modelle bieten in der Regel ein massives Kontextfenster von **1.000.000 bis 2.000.000 Token**. Gemma-Modelle haben meist ein kleineres Limit (8k - 32k).

## 1. Flash Modelle (Fokus: Geschwindigkeit & Effizienz)
Flash-Modelle sind darauf ausgelegt, Aufgaben rasend schnell und zu sehr geringen Kosten pro Token zu erledigen. Sie sind oft die beste Wahl für alltägliche Programmieraufgaben.

**gemini/gemini-flash-latest**
 * **Beschreibung:** 
Ein Alias (Zeiger), der immer auf das aktuellste, stabile Flash-Modell verweist (derzeit meist 2.5-flash).
 * **Verwendungszweck:** Standard-Modell für schnelle Antworten.
 * **Token Limit:** 1M - 2M Token.
 * **Am günstigsten für:**
Deine Standard-Aider-Sitzung.
Schnelles Schreiben von Kotlin-Boilerplate, einfache Refactorings oder das Erstellen von Tests.

**gemini/gemini-2.0-flash & gemini/gemini-2.0-flash-001**
 * **Beschreibung:**
Die stabile Hauptversion (001) und das generelle Tag der zweiten Flash-Generation.
 * **Verwendungszweck:**
Solide, ausgereifte Code-Generierung, wenn die allerneuesten (aber vielleicht instabilen) Features nicht gebraucht werden.
 * **Token Limit:** 1M Token.
 * **Am günstigsten für:** Zuverlässiges Arbeiten an bestehenden Kotlin-Projekten, wenn du konsistente Ergebnisse ohne Überraschungen willst.

**gemini/gemini-2.5-flash & Preview-Versionen (preview-09-2025)**
 * **Beschreibung:**
Die weiterentwickelte Generation mit verbessertem logischem Denken bei gleichbleibender Geschwindigkeit.
 * **Verwendungszweck:** Fortgeschrittenes Coden mit schnellem Feedback-Loop.
 * **Token Limit:** 1M - 2M Token.
 * **Am günstigsten für:**
Das schnelle Entwickeln von Android-Apps oder Kotlin-Backends (Ktor/Spring), bei denen Aider den Code zügig anpassen muss.
**gemini/gemini-3-flash-preview**
 * **Beschreibung:**
Ein früher Ausblick auf die 3. Generation von Flash.
 * **Verwendungszweck:**
Testen der neuesten KI-Geschwindigkeit und verbesserter Architektur.
 * **Token Limit:** voraussichtlich 2M Token.
 * **Am günstigsten für:** Experimentelles Coden; wenn du sehen willst, wie die neueste KI-Generation mit Kotlin-Syntax umgeht. (Achtung: Previews können fehlerhaft sein).

### Flash "Lite" (Maximaler Speed, geringste Kosten)
**gemini/gemini-flash-lite-latest / gemini-2.0-flash-lite / gemini-2.5-flash-lite / gemini-3.1-flash-lite-preview (inkl. 001 und Datums-Previews)**
 * **Beschreibung:**
Abgespeckte Versionen der Flash-Modelle. Sie sind die schnellsten und billigsten Modelle der API.
 * **Verwendungszweck:** Hochfrequente, sehr einfache Aufgaben; Chat-Bots; Massendatenverarbeitung.
 * **Token Limit:** Meist 1M Token (manchmal künstlich auf 32k-128k limitiert je nach API-Tier).
 * **Am günstigsten für:**
Sehr kleine Aider-Edits, z.B. das Ändern von Variablennamen, das Hinzufügen von Kotlin-Dokumentationskommentaren (KDoc) oder einfache Formatierungen. Nicht empfohlen für komplexe Logik.
### Flash "Native Audio" & "Live"
**gemini/gemini-2.5-flash-native-audio-latest / preview-09-2025 / preview-12-2025 / gemini-3.1-flash-live-preview**
 * **Beschreibung:**
Modelle, die Sprache direkt (ohne Umweg über Text) verarbeiten oder für Echtzeit-Audio/Video-Streams (Live API) optimiert sind.
 * **Verwendungszweck:** Voice-Interfaces, Echtzeit-Übersetzungen, Interaktion über Sprache.
 * **Token Limit:** 1M+ Token.
 * **Am günstigsten für:**
Für *Aider* als reines Text-Coding-Tool **nicht relevant**. Diese sind für Apps gedacht, die du baust (z.B. wenn du eine Kotlin-App programmierst, die Sprache verarbeitet), aber nicht als Modell *für* das Schreiben von Code.
## 2. Pro Modelle (Fokus: Komplexe Logik & Architektur)
Pro-Modelle sind deutlich intelligenter, machen weniger Syntax-Fehler bei komplexen Aufgaben, sind aber langsamer und teurer als Flash.
**gemini/gemini-pro-latest**
 * **Beschreibung:**
Zeigt immer auf das aktuell beste, stabile Pro-Modell.
 * **Verwendungszweck:** Heavy-Lifting beim Programmieren.
 * **Token Limit:** 2M Token.
 * **Am günstigsten für:**
Komplexe Architektur-Entscheidungen in Kotlin, das Lösen extrem harter Bugs (z.B. komplexe Race-Conditions bei Kotlin Coroutines/Flows) oder das Refactorn riesiger Codebasen auf einmal.
**gemini/gemini-2.5-pro & gemini-2.5-pro-exp-03-25 / preview-05-06 / preview-06-05**
 * **Beschreibung:**
Die Hochleistungsmodelle der 2.5-Generation. Die exp (Experimental) und preview-Modelle sind Schnappschüsse während der Entwicklung.
 * **Verwendungszweck:** Tiefgreifendes logisches Schließen und anspruchsvolle Entwicklungsaufgaben.
 * **Token Limit:** 2M Token.
 * **Am günstigsten für:**
Das Schreiben von fehlerfreiem, komplexem Kotlin-Code von Grund auf, z.B. das Designen kompletter Datenbankschemata oder komplexer Gradle-Build-Skripte.
**gemini/gemini-3-pro-preview / gemini-3.1-pro-preview**
 * **Beschreibung:**
Previews der 3. und 3.1 Generation. Die absoluten Flaggschiffe, derzeit noch im Teststadium.
 * **Verwendungszweck:**
Höchste analytische Fähigkeiten, die Google derzeit bietet.
 * **Token Limit:** 2M Token.
 * **Am günstigsten für:**
Wenn 2.5-Pro an einem harten Programmierproblem scheitert. Gut für konzeptionelle Planung und das Schreiben mathematisch komplexer Algorithmen.
**gemini/gemini-2.5-pro-preview-tts**
 * **Beschreibung:**
Ein Pro-Modell, das speziell auf hochwertige Text-to-Speech (Sprachausgabe) trainiert wurde.
 * **Verwendungszweck:**
Generierung extrem natürlicher Sprache.
 * **Am günstigsten für:**
Nicht für Aider geeignet. Nutze dieses Modell, wenn du eine Kotlin-App schreibst, die Texte vorlesen soll.
**gemini/gemini-3.1-pro-preview-customtools**
 * **Beschreibung:**
Optimiert für "Function Calling". Das Modell weiß extrem gut, wann und wie es externe Werkzeuge aufrufen muss.
 * **Am günstigsten für:**
Für Aider eher experimentell; sehr nützlich, wenn du in Kotlin komplexe Agents programmierst, die APIs ansteuern.
## 3. Spezial- und Experimentelle Modelle (Die restliche Liste)
Diese Modelle solltest du für dein tägliches Aider-Setup normalerweise ignorieren, es sei denn, du arbeitest an einer ganz spezifischen Aufgabe.
 * **gemini/gemini-exp-1114 & gemini-exp-1206:** Frühe experimentelle Dumps von Google (aus November/Dezember). Haben für Coden keinen stabilen Wert mehr.
 * **gemini/gemini-2.5-computer-use-preview-10-2025:** Ein Agenten-Modell, das darauf trainiert ist, Bildschirme anzusehen und Mausklicks/Tastatureingaben auf einem Desktop auszuführen. (In Aider nutzlos, da Aider dateibasiert arbeitet).
 * **gemini/gemini-gemma-2-9b-it / gemini-gemma-2-27b-it / gemini/gemma-3-27b-it:** Googles "Open Weights" Modelle (Gemma). Sie sind viel kleiner (9 oder 27 Milliarden Parameter) und nicht so fähig wie Gemini Pro/Flash. (Token-Limit: meist 8k). *Am günstigsten für:* Wenn du wissen willst, wie sich dein Code mit einem lokalen, offenen Modell verhalten würde, ohne es selbst hosten zu müssen.
 * **gemini/gemini-robotics-er-1.5-preview:** Spezialmodell für Robotik-Frameworks und physische Aktionsplanung.
 * **gemini/learnlm-1.5-pro-experimental:** Ein didaktisches Modell, das darauf trainiert ist, Dinge zu erklären statt sie einfach zu lösen (für Bildungssoftware).
 * **gemini/lyria-3-clip-preview & gemini/lyria-3-pro-preview:** Googles KI-Modelle zur *Musikgenerierung*. Für Aider irrelevant.
