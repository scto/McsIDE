# NewTerm: Schritt-für-Schritt Implementation Guide (Aider)
Dieser Guide definiert die exakten technischen Schritte. **Warte nach jedem Punkt auf Bestätigung.**
### Phase 1: Fundament & Struktur
 1. [ ] **Projekt-Init:** Erstellung der Multi-Modul-Struktur (:core:* und :feature:*) in Gradle 8.11.1 und Kotlin 2.2.0.
 2. [ ] **Dependency Management:** Zentralisierung aller Abhängigkeiten in libs.versions.toml.
 3. [ ] **Architektur-Vorbereitung:** Erstellung der Base-Klassen für MVI und Clean Architecture in :core:domain.
### Phase 2: Core Utils & Data
 4. [ ] **Architektur-Detektion:** Implementierung von ArchUtils.kt in :core:utils (Erkennung von arm, aarch64, x86_64).
 5. [ ] **Distribution-Modell:** Erstellung der distros.json Beispiel-Datei und der zugehörigen Kotlin-Datenklassen (Serialization).
 6. [ ] **Terminal-Schnittstelle:** Definition des Library-Interfaces für globale Settings (Font, Color, Log-Level) in :core:domain.
### Phase 3: Distribution Management (PRoot)
 7. [ ] **Download-Manager:** Implementierung der Flow-basierten Download-Logik für Distros, Proots und Liballocs.
 8. [ ] **Installations-Logik:** Refactoring der ReTerminal/Termux-Proot Logik zur Unterstützung paralleler Installationen.
 9. [ ] **Maintenance-Features:** Implementierung von Backup/Restore, Reset und Archivierung von Distros.
### Phase 4: UI & Settings (Compose)
 10. [ ] **Settings-Feature:** Implementierung des Settings-Bildschirms in :feature:settings (Material 3).
 11. [ ] **Terminal-UI:** Integration der Terminal-View-Komponente in :feature:terminal.
### Phase 5: Finalisierung
 12. [ ] **Hilt-DI:** Finalisierung der Dependency-Graphen.
 13. [ ] **Build-Test:** Vollständiger Check der Library-Einbindung.
**Bei jedem Fehler: Eintrag in problems.md.**
