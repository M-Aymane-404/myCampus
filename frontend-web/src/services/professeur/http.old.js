

import { apiAuth } from '../apiAuth';

export const BASE_URL = 'http://localhost:8080/api/professeur';

// ─── Construction des headers ────────────────────────────────────────────────

async function buildHeaders(customHeaders = {}, body = null) {
    const authHeaders = await apiAuth.getAuthHeadersWithRefresh();

    const headers = {
        ...authHeaders,
        ...customHeaders,
    };

    // Ne pas forcer Content-Type pour FormData → le browser gère le boundary
    if (!(body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

// ─── Traitement de la réponse ────────────────────────────────────────────────

async function handleResponse(response) {
    if (!response.ok) {
        let message = `Erreur HTTP: ${response.status}`;
        try {
            const errorData = await response.json();
            message = errorData.message || errorData.erreur || errorData.error || message;
        } catch {
            // pas de corps JSON → message par défaut
        }
        throw new Error(message);
    }

    // 204 No Content (DELETE, publier…)
    if (response.status === 204) return true;

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) return response.json();

    // Fichier binaire (PDF, image…)
    return response.blob();
}

// ─── Fonction principale ─────────────────────────────────────────────────────

/**
 * Appel HTTP standard (JSON ou FormData)
 * @param {string} endpoint   ex: "/notes/classes/ILCS1-A/examens/8/feuille"
 * @param {Object} options    même API que fetch() : method, body, headers…
 * @returns {Promise<any>}    JSON parsé | true (204) | Blob
 */
export async function fetchProfesseur(endpoint, options = {}) {
    const headers = await buildHeaders(options.headers, options.body);

    const response = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers,
        credentials: 'include',
    });

    return handleResponse(response);
}

// ─── Fonction pour les téléchargements PDF ───────────────────────────────────

/**
 * Téléchargement d'un fichier binaire (PDF, image)
 * Retourne { blob, filename } pour déclencher le téléchargement côté client
 *
 * @param {string} endpoint   ex: "/examens/8/corrige"
 * @param {Object} options    options fetch optionnelles
 * @returns {Promise<{ blob: Blob, filename: string }>}
 *
 * @example
 *   const { blob, filename } = await fetchProfesseurBlob('/examens/8/corrige');
 *   const url = URL.createObjectURL(blob);
 *   const a   = document.createElement('a');
 *   a.href     = url;
 *   a.download = filename;
 *   a.click();
 */
export async function fetchProfesseurBlob(endpoint, options = {}) {
    const authHeaders = await apiAuth.getAuthHeadersWithRefresh();

    const response = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers: {
            ...authHeaders,
            ...(options.headers || {}),
        },
        credentials: 'include',
    });

    if (!response.ok) {
        let message = `Erreur HTTP: ${response.status}`;
        try {
            const errorData = await response.json();
            message = errorData.message || errorData.erreur || errorData.error || message;
        } catch {
            // pas de corps JSON
        }
        throw new Error(message);
    }

    const blob = await response.blob();

    // Récupère le nom du fichier depuis l'entête Content-Disposition
    // ex: 'attachment; filename="corrige_examen_8.pdf"'
    const disposition = response.headers.get('content-disposition') || '';
    const match       = disposition.match(/filename="?([^"]+)"?/);
    const filename    = match ? match[1] : 'fichier';

    return { blob, filename };
}