# Projekt-Status & Anerkennung: Initialisierung der Template-Bibliothek

Diese Dokumentation dient als Bestätigung und Leitfaden für den aktuellen Refactoring-Status im Projekt **McsIDE**. Es hält die getroffenen Entscheidungen und die spezifische Ausführungslogik für den ersten Meilenstein der Template-Library fest.

## 1. Aktueller System-Status
 * **Arbeitsverzeichnis:** core/ (Eingeschränkt via --subtree-only)
 * **Kontext-Ladestatus:** * project-overview.md, system-design.md, tech-environment.md (Read-only geladen)
   * McsIDE_20260510_221722.md (Projekt-Struktur Map geladen)
 * **Ziel:** Implementierung einer modularen Template-Engine nach Clean Architecture Prinzipien.

## 2. Definierte Architektur-Vorgaben (:core:template)
Die Bibliothek wird in drei Teilmodule unterteilt, um eine strikte Trennung von Belangen (Separation of Concerns) zu gewährleisten:
 1. **:api**: Enthält nur Interfaces, Domain-Modelle und Exceptions. Keine Android-Abhängigkeiten, wenn möglich.
 2. **:data**: Beinhaltet Datenquellen, Repositories und lokale/remote Datenstrukturen.
 3. **:impl**: Beherbergt die konkrete Geschäftslogik (Manager-Klassen, Generatoren) und verknüpft API mit Daten.

## 3. Anerkennung von Schritt 1: Modul-Setup
Der Fokus liegt auf der Erstellung der physischen Infrastruktur.
### Pfad-Korrektur (Subtree-Spezifisch)
Da Aider innerhalb des core-Verzeichnisses operiert, werden die Module wie folgt angelegt:
 * ✅ template/api (Namespace: com.scto.mcside.core.templates.api)
 * ✅ template/data (Namespace: com.scto.mcside.core.templates.data)
 * ✅ template/impl (Namespace: com.scto.mcside.core.templates.impl)
### Gradle-Konfiguration
 * Verwendung von build.gradle.kts (Kotlin DSL).
 * Einbindung des Version Catalogs (libs.versions.toml).
 * Herstellung der Abhängigkeits-Hierarchie (impl -> api & data).

## 4. Handlungsanweisung für die KI (Aider)
Um die Stabilität des Projekts zu gewährleisten, erfolgt die Ausführung streng nach folgendem Protokoll:
 1. **Isolation:** Es werden nur Ordner und Gradle-Dateien erstellt.
 2. **Commit:** Nach der Erstellung erfolgt ein atomarer Git-Commit.
 3. **Meldung:** Die KI unterbricht die Arbeit nach Schritt 1 und wartet auf die Validierung.
**Status:** *Bereit zur Ausführung von Schritt 1.*

