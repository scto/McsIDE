# McsIDE ![Stone Badge](https://stone.professorlee.work/api/stone/h465855hgg/WebIDE)

![Version](https://img.shields.io/badge/version-0.0.1-blue?style=flat-square)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue?style=flat-square)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose-green?style=flat-square)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-GPLv3-orange?style=flat-square)](LICENSE)


[ [**German**] ] | [ [Englisch](README_EN.md) ]

McsIDE ist eine native, Android-basierte integrierte Entwicklungsumgebung (IDE) für die Entwicklung nativer Android Applikationen auf dem Smartphone. Das Projekt wurde vollständig mit Jetpack Compose entwickelt und implementiert einen vollständigen Workflow – von der Code-Bearbeitung bis hin zur Erstellung von APKs direkt auf dem Smartphone.

Dies ist ein experimentelles Projekt, dessen Kernarchitektur und Codellogik in Zusammenarbeit mit verschiedenen KI-Modellen (Claude, Gemini, DeepSeek) erstellt wurden.

## Screenshots

<div align="center">
  <img src="https://github.com/user-attachments/assets/2eac6ea4-25a1-4a02-b814-2925ffb2092e" width="45%" />
  <img src="https://github.com/user-attachments/assets/7999b42a-af56-4aea-b705-920e7e168844" width="45%" />
</div>

## Analyse der Projektstruktur

Der Hauptcode befindet sich unter app/src/main/java/com/scto/mcside/. Die Verzeichnisstruktur ist wie folgt gegliedert:

```text
com.scto.mcside
├── build/              # Custom APK build system
│   ├── ApkBuilder.java # Core logic for compiling and packaging APKs
│   ├── ApkInstaller.kt # Handles APK installation
│   └── ...             # Encryptor, ZipAligner
├── core/               # App-specific core infrastructure
│   └── utils/          # Utilities (Backup, CodeFormatter, WorkspaceManager, etc.)
├── files/              # File system module
│   ├── FileIcons.kt    # Icon resource mapping
│   └── FileTree.kt     # File explorer UI and logic
├── ui/                 # Interface layer (Jetpack Compose)
│   ├── components/     # Shared UI components
│   ├── editor/         # Code editor screen
│   ├── preview/        # Web preview screen
│   ├── settings/       # Application settings and about screens
│   ├── terminal/       # Terminal emulator (Alpine Linux integration)
│   ├── theme/          # Design system (Colors, Typography)
│   └── welcome/        # Welcome/Onboarding screen
```

**Wichtige Ressourcen (app/src/main/assets/)**:

*   `textmate/`: TextMate-Grammatiken und Konfigurationen für die Syntax-Hervorhebung.
*   `queries/`: Abfragen für den Syntaxbaum.
*   `init-host.sh`, `init.sh`, `proot`, `rootfs.bin`: Dateien für die eingebettete Alpine Linux-Umgebung.


## Funktionen und Merkmale

*  **Syntax-Hervorhebung**: Basierend auf TextMate-Grammatikdateien; unterstützt HTML, CSS, JavaScript und JSON perfekt.
*  **Projektverwaltung**: Vollständiger Zugriff auf das Dateisystem, unterstützt das Erstellen und Verwalten von Webprojekten mit mehreren Dateien.
*  **Echtzeit-Vorschau**: Integrierte WebView-Vorschauumgebung zur Testung von JavaScript-Interaktionen.
*  **Moderne Benutzeroberfläche**: Zu 100 % in Kotlin und Jetpack Compose geschrieben, unterstützt dynamische Themes.
*  **Git-Integration**: Integrierte Git-Versionskontrolle mit visualisiertem Commit-Verlauf (Graph), Unterstützung für Clone, Commit, Push, Pull und Branch-Management. Sensible Dateien und Build-Artefakte werden automatisch ignoriert.


## Ausblick und Herausforderungen

Wir arbeiten aktiv an der Verbesserung von McsIDE. Dies sind unsere Hauptpläne und aktuellen Herausforderungen:

*  **Mehrsprachigkeit**: Native Unterstützung für den Wechsel zwischen Deutsch und Englisch innerhalb der App (und zukünftig weiteren Sprachen).
*  **Benutzerdefiniertes Code-Highlighting**: Benutzern ermöglichen, eigene TextMate-Grammatiken und Farbschemata zu importieren.
*  **Cloud-Ressourcen und Größenoptimierung**: Die größte Herausforderung ist derzeit die Größe der APK aufgrund der eingebetteten Linux-Umgebung. Wir planen, große Ressourcen wie rootfs.bin in die Cloud auszulagern, um sie bei Bedarf herunterzuladen. Dies wird die initiale Installationsgröße erheblich reduzieren.


## Diskussion und Community

* QQ-Gruppe: 1050254184
* Telegram-Kanal: Android_For_WebIDE

## Mitwirkende

<a href="https://github.com/scto/McsIDE/graphs/contributors">
<img src="https://contrib.rocks/image?repo=scto/McsIDE" />
</a>


## Lizenz

```
McsIDE - Eine leistungsstarke IDE für die App unter Android.
Copyright (C) 2025  scto  <tschmid35@gmail.com>

Dieses Programm ist freie Software: Sie können es unter den Bedingungen der 
GNU General Public License, wie von der Free Software Foundation veröffentlicht, 
weiterverteilen und/oder modifizieren; entweder gemäß Version 3 der Lizenz oder 
(nach Ihrer Option) jeder späteren Version.

Dieses Programm wird in der Hoffnung verteilt, dass es nützlich sein wird, 
jedoch OHNE JEDE GEWÄHRLEISTUNG; auch ohne die implizite Gewährleistung der 
MARKTGÄNGIGKEIT oder der EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. 
Siehe die GNU General Public License für weitere Details.

Sie sollten eine Kopie der GNU General Public License zusammen mit diesem 
Programm erhalten haben. Wenn nicht, siehe [https://www.gnu.org/licenses/](https://www.gnu.org/licenses/).
```


[![Star History Chart](https://api.star-history.com/svg?repos=scto/McsIDE&type=Date)](https://star-history.com/#scto/McsIDE&Date)