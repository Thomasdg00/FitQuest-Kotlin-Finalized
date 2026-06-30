# FitQuest

FitQuest è un'applicazione Android nativa sviluppata per il tracciamento delle attività sportive, come camminata, corsa e ciclismo. L'app offre funzionalità di live tracking tramite GPS, registrazione dei percorsi su mappa interattiva, e raccolta di metriche avanzate quali altitudine, meteo al momento dell'allenamento e stima delle calorie bruciate. L'obiettivo principale è fornire un ecosistema completo e robusto per monitorare le proprie performance fisiche in modo efficiente e facilmente condivisibile.

## Contesto Universitario

| Informazione | Dettaglio |
| :--- | :--- |
| **Corso** | [Programmazione Mobile] |
| **Anno Accademico** | [2025/2026] |
| **Autore** | [Thomas di Gregorio] |

## Funzionalità Principali (Features)

*   **Live Tracking:** Supporto per il tracciamento in tempo reale di camminata, corsa e ciclismo.
*   **Mappe in Tempo Reale:** Visualizzazione della rotta e della posizione corrente tramite Google Maps SDK.
*   **Background Tracking:** Tracciamento continuo in background gestito tramite un Foreground Service con notifica persistente.
*   **Metriche Avanzate:** Integrazione con i sensori del dispositivo per la lettura dell'altitudine (barometro) e il conteggio dei passi (cadenza), ove disponibili.
*   **Stima delle Calorie:** Calcolo delle calorie consumate basato sull'equivalente metabolico (MET).
*   **Integrazione Meteo:** Acquisizione e salvataggio dei dati meteorologici all'inizio di ogni allenamento tramite le API di OpenMeteo.
*   **Obiettivi e Statistiche:** Impostazione di obiettivi settimanali (es. distanza) e visualizzazione di grafici riassuntivi.
*   **Salvataggio Locale:** Persistenza dei dati (allenamenti, punti del percorso, impostazioni, obiettivi) tramite database Room.
*   **Condivisione:** Generazione di un'immagine condivisibile dell'allenamento con percorso tracciato e statistiche principali.

## Stack Tecnologico

Il progetto è costruito applicando i principi della Clean Architecture (in un'ottica pragmatica) e si avvale delle tecnologie moderne dello sviluppo Android:

*   **Linguaggio:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architettura:** MVVM (Model-View-ViewModel)
*   **Database Locale:** Room
*   **Mappe:** Google Maps SDK / Google Maps Compose
*   **Servizi di Localizzazione:** FusedLocationProviderClient (Google Play Services)
*   **Programmazione Asincrona:** Kotlin Coroutines e Flow
*   **Dependency Injection:** Manuale (tramite `AppContainer`)
*   **API Esterne:** OpenMeteo API (senza necessità di API key)

## Installazione e Configurazione

1.  **Clonare la repository:**
    ```bash
    git clone <URL_DELLA_REPO>
    cd FitQuest
    ```

2.  **Configurazione di Google Maps:**
    Per abilitare la visualizzazione delle mappe, è necessario fornire la chiave API di Google Maps.
    Crea o modifica il file `secrets.properties` (o `local.properties`) nella root del progetto e aggiungi la tua credenziale:
    ```properties
    GOOGLE_MAPS_API_KEY=your_api_key_here
    ```
    Abilita **Maps SDK for Android** in Google Cloud Console. Se limiti la chiave alle app Android, il package name e la SHA-1 devono corrispondere al progetto e al debug keystore in uso.
    *(Nota: Non committare mai la chiave API nel repository di controllo versione).* 

3.  **Configurazione dell'ambiente:**
    Apri il progetto su Android Studio (si raccomanda l'ultima versione stabile).
    Assicurati di aver configurato il JDK corretto (solitamente Java 17 o superiore).

4.  **Sincronizzazione Gradle:**
    Android Studio avvierà automaticamente la sincronizzazione di Gradle per scaricare le dipendenze necessarie. Attendi il completamento del processo.

## Esecuzione

Per avviare il progetto in locale su un emulatore o su un dispositivo fisico collegato:

*   **Tramite Android Studio:**
    Seleziona l'app ("app") e il dispositivo di destinazione, quindi premi il tasto "Run" (Shift + F10).

*   **Tramite Riga di Comando (Gradle):**
    Per compilare ed eseguire una build di debug, puoi usare i comandi forniti dai wrapper Gradle:
    ```bash
    # Su Linux/macOS
    ./gradlew assembleDebug

    # Su Windows
    gradlew.bat assembleDebug
    ```
    *(Opzionale)* Per installare l'APK appena generato direttamente su un dispositivo connesso:
    ```bash
    ./gradlew installDebug
    ```

## Struttura del Progetto

Di seguito viene illustrata la struttura dei package all'interno della directory dei sorgenti per facilitare la comprensione dell'architettura:

```text
com.package.name
├── di/                     # Dependency Injection manuale (AppContainer)
├── ui/                     # Livello di presentazione (Jetpack Compose)
│   ├── screens/            # Schermate dell'applicazione
│   ├── components/         # Componenti UI riutilizzabili
│   └── navigation/         # Gestione della navigazione Compose
├── viewmodel/              # Componenti architetturali ViewModel (gestione stato UI)
├── data/                   # Livello dei dati
│   ├── local/              # Room Database, DAO e Database Entity
│   ├── remote/             # Client di rete e chiamate API (OpenMeteo)
│   └── repository/         # Implementazioni dei repository
├── domain/                 # Regole di business (Core logico)
│   ├── model/              # Entità di dominio
│   └── usecase/            # Casi d'uso isolati per funzionalità specifiche
└── tracking/               # Moduli per la cattura e calcolo dati background
    ├── service/            # Foreground Service
    ├── location/           # Gestione della geolocalizzazione
    ├── sensors/            # Integrazione barometro e pedometro
    └── calories/           # Algoritmi calcolo metriche metaboliche
```
