package main

import (
    "net/http"
    "net/http/httptest"
    "testing"
)

func TestHealthEndpoint(t *testing.T) {
    mux := newMux()
    req := httptest.NewRequest(http.MethodGet, "/v1/health", nil)
    rec := httptest.NewRecorder()
    mux.ServeHTTP(rec, req)
    if rec.Code != http.StatusOK {
        t.Fatalf("health status = %d, want %d", rec.Code, http.StatusOK)
    }
}

func TestServersEndpoint(t *testing.T) {
    mux := newMux()
    req := httptest.NewRequest(http.MethodGet, "/v1/servers", nil)
    rec := httptest.NewRecorder()
    mux.ServeHTTP(rec, req)
    if rec.Code != http.StatusOK {
        t.Fatalf("servers status = %d, want %d", rec.Code, http.StatusOK)
    }
}
