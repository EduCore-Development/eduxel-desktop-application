# Entwickler-Leitfaden: Eduxel Web Dashboard Integration

Dieses Dokument beschreibt die technische Implementierung und den Workflow zur Anbindung des Eduxel Web Dashboards an die zentrale Datenbank und die Eduxel Desktop-App.

## 1. Architektur-Übersicht

Das Eduxel-System besteht aus drei Hauptkomponenten:
1.  **Eduxel Desktop App**: JavaFX-Anwendung zur Verwaltung der Schuldaten.
2.  **MariaDB/MySQL Datenbank**: Zentraler Speicherort für alle Daten.
3.  **Web Dashboard**: Eine externe Web-Anwendung (z.B. PHP/Node.js), die Schülern, Lehrern und Eltern Online-Zugriff ermöglicht.

## 2. Setup-Workflow & Secret-Konzept

Der Prozess zur Aktivierung des Web-Dashboards folgt einer festen Reihenfolge, um maximale Sicherheit zu gewährleisten:

### Schritt 1: Datenbank-Konfiguration (Desktop App)
Zuerst müssen in den Haupteinstellungen der Eduxel-App die Verbindungsparameter zur Datenbank hinterlegt werden (Host, Port, DB-Secret). Erst wenn die Verbindung steht, kann das Web-Dashboard konfiguriert werden.

### Schritt 2: Web-Dashboard Installation & Secret-Generierung
Der Web-Entwickler setzt das Web-Dashboard auf dem Webserver auf.
*   Das Web-Dashboard verbindet sich mit derselben MariaDB-Instanz wie die Desktop-App.
*   Bei der Erstinstallation des Web-Dashboards auf dem Server wird ein **Web Dashboard Secret** generiert (oder vom Entwickler in einer `.env` Datei festgelegt).
*   Dieses Secret dient als kryptografischer Schlüssel zur Validierung von Anfragen und zur Verschlüsselung sensibler Daten zwischen Web und Desktop.

### Schritt 3: Aktivierung in der Desktop-App
Der Benutzer kopiert das generierte Secret vom Web-Dashboard und gibt es in der Eduxel Desktop-App unter dem Reiter **"Web Dashboard"** ein.
*   Die App speichert dieses Secret lokal (verschlüsselt in den Java User-Preferences) ab.
*   In der Datenbank selbst wird das Secret **nicht** gespeichert, um die Angriffsfläche bei einem DB-Leak zu minimieren. Das Secret dient rein als "Shared Secret" zwischen App und Website.
*   Ab diesem Zeitpunkt ist die Kommunikation und Synchronisation autorisiert.

## 3. Datenbank-Struktur (`web_accounts`)

Das Web-Dashboard nutzt eine spezifische Tabelle in der zentralen Datenbank, um Zugänge zu verwalten. Die Tabelle wird von der Desktop-App automatisch via `SchemaBootstrapper` angelegt.

### Tabellendefinition: `web_accounts`
| Spalte | Typ | Beschreibung |
| :--- | :--- | :--- |
| `id` | BIGINT | Primärschlüssel (Auto-Increment) |
| `email` | VARCHAR(512) | E-Mail Adresse (Login-Name, Unique) |
| `password_hash` | VARCHAR(512) | Gehashtes Passwort |
| `type` | VARCHAR(32) | Typ des Accounts (`Schüler`, `Lehrer`, `Eltern`) |
| `reference_id` | BIGINT | Fremdschlüssel auf `students.id` oder `teachers.id` |
| `created_at` | TIMESTAMP | Erstellungszeitpunkt |

### Verknüpfungs-Logik
*   **Schüler-Accounts**: `reference_id` zeigt auf die ID in der Tabelle `students`.
*   **Lehrer-Accounts**: `reference_id` zeigt auf die ID in der Tabelle `teachers`.
*   **Eltern-Accounts**: `reference_id` zeigt aktuell ebenfalls auf die `students.id` (da Eltern dem Schüler zugeordnet sind).

## 4. Hinweise für den Web-Entwickler

1.  **Passwort-Hashing**: Die Desktop-App generiert Passwörter im Klartext für die Erstausgabe. Das Web-Dashboard MUSS diese Passwörter beim ersten Login oder bei der Erstellung sicher hashen (z.B. mit Argon2 oder BCrypt).
2.  **API-Sicherheit**: Wenn das Web-Dashboard eine API bereitstellt, sollte das **Web Dashboard Secret** im Header zur Authentifizierung gegenüber der Desktop-App (falls zukünftig benötigt) verwendet werden.
3.  **Daten-Verschlüsselung**: Sensible Personendaten in der DB (wie Adressen) sind teilweise Base64-verschlüsselt. Das Web-Dashboard benötigt den entsprechenden DB-Schlüssel, um diese Daten im Browser lesbar zu machen.

---
*Dokumentation Stand: 20. Dezember 2025*
*EduCore Development Team*
