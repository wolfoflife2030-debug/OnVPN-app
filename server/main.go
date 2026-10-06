package main

import (
    "encoding/json"
    "log"
    "net/http"
)

type Server struct {
    ID       string `json:"id"`
    Name     string `json:"name"`
    Region   string `json:"region"`
    Endpoint string `json:"endpoint"`
    Load     int    `json:"load"`
}

func writeJSON(w http.ResponseWriter, status int, value any) {
    w.Header().Set("Content-Type", "application/json; charset=utf-8")
    w.WriteHeader(status)
    _ = json.NewEncoder(w).Encode(value)
}

func newMux() http.Handler {
    mux := http.NewServeMux()
    mux.HandleFunc("/v1/health", func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodGet {
            w.Header().Set("Allow", http.MethodGet)
            http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
            return
        }
        writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
    })
    mux.HandleFunc("/v1/servers", func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodGet {
            w.Header().Set("Allow", http.MethodGet)
            http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
            return
        }
        writeJSON(w, http.StatusOK, []Server{{
            ID: "demo-1", Name: "On Vpn Demo", Region: "test",
            Endpoint: "REPLACE_WITH_SERVER:51820", Load: 0,
        }})
    })
    return mux
}

func main() {
    log.Println("On Vpn API listening on :8080")
    log.Fatal(http.ListenAndServe(":8080", newMux()))
}
