Aider Architektur-Prompt: :core:template Library

Erstelle eine vollständige Kotlin-basierte Android-Bibliothek für das Projekt-Template-Management im Namespace com.scto.mcside. Arbeite nach den Prinzipien der Clean Architecture und SOLID. Verwende ausschließlich Kotlin, Coroutines, Flows (StateFlow/SharedFlow) und injiziere Coroutine Dispatchers über den Konstruktor für maximale Testbarkeit.

1. Projekt-Kontext & Struktur (WICHTIG)
Nutze den beiliegenden Projekt-Tree aus der Datei McsIDE_20260510_221722.md als Referenz für die bestehende Dateistruktur. Lege die neuen Module passend zur bestehenden Architektur im Verzeichnis core/ an. Die Struktur der neuen Module muss exakt so aussehen:

* core/template/api (Paket: com.scto.mcside.core.templates.api)

* core/template/data (Paket: com.scto.mcside.core.templates.data)

* core/template/impl (Paket: com.scto.mcside.core.templates.impl)

Erstelle für jedes Modul eine grundlegende build.gradle.kts Datei. Das impl Modul benötigt Abhängigkeiten zum api und data Modul.

2. API Modul (core/template/api)
Erstelle hier ausschließlich Interfaces, Domain-Models und Exceptions:

* ProjectCreationConfig (Data Class): Enthält appName (String), packageName (String), minSdk (Int), targetSdk (Int), language (Enum: Kotlin/Java), useKotlinDsl (Boolean).

* ProjectLocationProvider (Interface): Liefert den Root-Zielpfad für die Projekterstellung (wird später vom :core:domain Modul injiziert).

* TemplateManager (Interface): Definiert suspend funs/Flows für downloadTemplates(), installTemplates(), updateTemplates(), upgradeTemplates() und clearTemplates().
Metadaten & Abfrage-API für die UI (z.B. WizardScreen):
* TemplateType (Enum): Werte sind ANDROID_NATIVE, CMAKE, FLUTTER, LIBGDX.

* TemplateMetadata (Data Class): id/name, type (TemplateType), thumbnailUri (String: lokaler Pfad/URI für UI-Bilder), description (String), version (String), sourceUrl (String).

* TemplateQueryService (Interface): Funktionen (als Flow/suspend) zum Beziehen der Gesamtanzahl an Templates, Anzahl gefiltert nach Typ, Liste aller TemplateMetadata und Liste von TemplateMetadata gefiltert nach Typ.

3. Data Modul (core/template/data)

* TemplateVersionRepository: Implementiere ein Repository, das die zentrale Versionsverwaltung für generierte Projekte übernimmt. Es hält die Gradle-Version und Versionen aller verwendeten Bibliotheken für die Templates. Es muss Funktionen bieten, um Versionen als Flow/Suspend abzufragen, bestimmte Versionen dynamisch zu überschreiben und zu aktualisieren.

4. Impl Modul (core/template/impl)
Implementiere hier die Geschäftslogik, Dateiverwaltung und das Netzwerk-Handling:

* TemplateDataSource: Lädt Template-ZIP-Archive von einer remote URL (z.B. GitHub/CDN) herunter und entpackt sie on-the-fly mittels ZipInputStream in ein lokales templateStorageDir. CRITICAL SECURITY REQUIREMENT: Implementiere zwingend einen Zip-Slip-Vulnerability-Schutz! Verifiziere via canonicalPath, dass keine Dateien außerhalb des Zielverzeichnisses entpackt werden.

* TemplateManagerImpl: Implementiert das TemplateManager Interface. Orchestriert Downloads, Entpacken und die lokale Verzeichnisverwaltung (templateStorageDir).

* GradleWrapperManager: Lädt eine stabile gradle-wrapper.jar und .properties herunter (die Ziel-Version kommt aus dem TemplateVersionRepository) und platziert sie im generierten Zielprojekt unter gradle/wrapper/.

* ProjectGenerator: Nimmt eine ProjectCreationConfig entgegen und generiert das Projekt am Pfad des ProjectLocationProvider.

   * Erstelle hier die Logik, um dynamisch eine gradle/libs.versions.toml aus den Daten des TemplateVersionRepository zu generieren.

   * Die zu erstellenden build.gradle.kts Dateien der Templates müssen zwingend auf diesen generierten Version Catalog verweisen.

* TemplateQueryServiceImpl: Implementiert das TemplateQueryService Interface. Parst die lokalen entpackten Templates im templateStorageDir, extrahiert Metadaten und löst die lokalen Dateipfade für die thumbnailUri auf. Dies ist wichtig, damit ein externes Feature-Modul (UI) diese Bilder (z.B. via Coil) rendern kann, ohne sie selbst als Assets halten zu müssen.
Bitte setze diese Anforderungen nun schrittweise in Code um. Beginne mit der Modulstruktur und den Build-Dateien und arbeite dich dann von der API über Data zur Implementierung (Impl) vor. Achte auf sauberes Error-Handling!