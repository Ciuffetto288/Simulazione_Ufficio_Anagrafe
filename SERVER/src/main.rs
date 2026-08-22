use axum::{
    extract::{Query, State},
    http::{Method, StatusCode},
    response::{IntoResponse, Json},
    routing::get,
    Router,
};
use csv::StringRecord;
use serde::{Deserialize, Serialize};
use std::{
    env,
    io::{Read, Write},
    net::SocketAddr,
    path::{Path, PathBuf},
    sync::Arc,
};
use tower_http::cors::{Any, CorsLayer};

#[derive(Clone)]
struct AppState {
    data_file: Arc<PathBuf>,
}

#[derive(Clone, Deserialize, Serialize)]
struct Cittadino {
    nome: String,
    cognome: String,
    #[serde(rename = "codiceFiscale")]
    codice_fiscale: String,
    comune: String,
    provincia: String,
    #[serde(rename = "dataNascita")]
    data_nascita: String,
}

#[derive(Deserialize)]
struct SearchQuery {
    query: Option<String>,
}

#[derive(Deserialize)]
struct PendingPayload {
    cittadino: Option<Cittadino>,
}

#[tokio::main]
async fn main() {
    let native_host = true;
    let state = AppState {
        data_file: Arc::new(data_file()),
    };

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods([Method::GET])
        .allow_headers(Any);
    let app = Router::new()
        .route("/api", get(health))
        .route("/api/cittadini", get(search_cittadini))
        .with_state(state)
        .layer(cors);
    let address = SocketAddr::from(([127, 0, 0, 1], 8080));
    if native_host {
        eprintln!("Anagr@Fe server in ascolto su http://{address}");
    } else {
        println!("Anagr@Fe server in ascolto su http://{address}");
    }
    let listener = tokio::net::TcpListener::bind(address).await.unwrap();
    if native_host {
        let (shutdown_sender, shutdown_receiver) = tokio::sync::oneshot::channel();
        std::thread::spawn(move || native_message_loop(shutdown_sender));
        axum::serve(listener, app)
            .with_graceful_shutdown(async { let _ = shutdown_receiver.await; })
            .await
            .unwrap();
    } else {
        axum::serve(listener, app).await.unwrap();
    }
}

fn native_message_loop(shutdown_sender: tokio::sync::oneshot::Sender<()>) {
    let ready = br#"{"status":"ready"}"#;
    let length = (ready.len() as u32).to_le_bytes();
    let mut stdout = std::io::stdout().lock();
    if stdout.write_all(&length).and_then(|_| stdout.write_all(ready)).and_then(|_| stdout.flush()).is_err() {
        let _ = shutdown_sender.send(());
        return;
    }
    drop(stdout);

    let mut stdin = std::io::stdin().lock();
    loop {
        let mut length = [0u8; 4];
        if stdin.read_exact(&mut length).is_err() {
            break;
        }
        let message_length = u32::from_le_bytes(length) as usize;
        if message_length > 1024 * 1024 {
            break;
        }
        let mut message = vec![0u8; message_length];
        if stdin.read_exact(&mut message).is_err() {
            break;
        }
    }
    let _ = shutdown_sender.send(());
}

async fn health() -> Json<serde_json::Value> {
    Json(serde_json::json!({ "status": "ok", "service": "anagrafe-server" }))
}

async fn search_cittadini(
    State(state): State<AppState>,
    Query(params): Query<SearchQuery>,
) -> Result<Json<Vec<Cittadino>>, ApiError> {
    let query = params.query.unwrap_or_default().to_lowercase();
    let mut cittadini = load_cittadini(&state.data_file)?;
    if let Some(pending) = fetch_pending_citizen().await {
        if !cittadini.iter().any(|citizen| citizen.codice_fiscale == pending.codice_fiscale) {
            cittadini.push(pending);
        }
    }
    let mut results: Vec<Cittadino> = cittadini
        .into_iter()
        .filter(|cittadino| {
            query.is_empty()
                || cittadino.nome.to_lowercase().contains(&query)
                || cittadino.cognome.to_lowercase().contains(&query)
                || cittadino.codice_fiscale.to_lowercase().contains(&query)
        })
        .collect();
    results.sort_by(|left, right| {
        left.cognome
            .to_lowercase()
            .cmp(&right.cognome.to_lowercase())
            .then_with(|| left.nome.to_lowercase().cmp(&right.nome.to_lowercase()))
            .then_with(|| left.codice_fiscale.cmp(&right.codice_fiscale))
    });
    Ok(Json(results))
}

async fn fetch_pending_citizen() -> Option<Cittadino> {
    let response = reqwest::get("http://127.0.0.1:8090/api/stato").await.ok()?;
    if !response.status().is_success() {
        return None;
    }
    response.json::<PendingPayload>().await.ok()?.cittadino
}

#[derive(Debug)]
struct ApiError(String);

impl IntoResponse for ApiError {
    fn into_response(self) -> axum::response::Response {
        (
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(serde_json::json!({ "error": self.0 })),
        )
            .into_response()
    }
}

fn data_file() -> PathBuf {
    if let Ok(directory) = env::var("ANAGRAFE_DATA_DIR") {
        return PathBuf::from(directory).join("cittadini.csv");
    }

    let home = env::var_os("HOME").map(PathBuf::from).unwrap_or_default();
    if cfg!(target_os = "windows") {
        let app_data = env::var_os("APPDATA").map(PathBuf::from).unwrap_or(home);
        app_data.join("Anagr_fe").join("data").join("cittadini.csv")
    } else if cfg!(target_os = "macos") {
        home.join("Library").join("Anagr_fe").join("data").join("cittadini.csv")
    } else {
        home.join(".config").join("Anagr_fe").join("data").join("cittadini.csv")
    }
}

fn load_cittadini(path: &Path) -> Result<Vec<Cittadino>, ApiError> {
    if !path.exists() {
        return Ok(Vec::new());
    }

    let mut reader = csv::ReaderBuilder::new()
        .delimiter(b';')
        .trim(csv::Trim::Fields)
        .from_path(path)
        .map_err(|error| ApiError(format!("Impossibile leggere l'archivio: {error}")))?;
    let mut cittadini = Vec::new();

    for record in reader.records() {
        match record {
            Ok(record) => {
                if let Some(cittadino) = parse_cittadino(&record) {
                    cittadini.push(cittadino);
                }
            }
            Err(error) => eprintln!("Riga CSV ignorata: {error}"),
        }
    }

    Ok(cittadini)
}

fn parse_cittadino(record: &StringRecord) -> Option<Cittadino> {
    if record.len() < 8 || (record.get(0)?.is_empty() && record.get(1)?.is_empty()) {
        return None;
    }

    Some(Cittadino {
        nome: record.get(0)?.to_owned(),
        cognome: record.get(1)?.to_owned(),
        data_nascita: record.get(2)?.to_owned(),
        comune: record.get(4)?.to_owned(),
        provincia: record.get(5)?.to_owned(),
        codice_fiscale: record.get(7)?.to_owned(),
    })
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_java_storage_record() {
        let record = StringRecord::from(vec![
            "Mario", "Rossi", "1980-01-01", "M", "Roma", "RM", "H501", "RSSMRA80A01H501Z",
        ]);
        let citizen = parse_cittadino(&record).expect("record valido");
        assert_eq!(citizen.codice_fiscale, "RSSMRA80A01H501Z");
        assert_eq!(citizen.comune, "Roma");
        assert_eq!(citizen.provincia, "RM");
    }
}