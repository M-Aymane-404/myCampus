import { apiAuth } from '../apiAuth';

const BASE_URL = 'http://localhost:8080/api/professeur';

async function buildHeaders(customHeaders = {}, body = null) {
    const authHeaders = await apiAuth.getAuthHeadersWithRefresh();

    const headers = {
        ...authHeaders,
        ...customHeaders,
    };

    // Ne pas forcer Content-Type quand on envoie un fichier
    if (!(body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

async function extractErrorMessage(response) {
    let message = `Erreur HTTP: ${response.status}`;

    try {
        const errorData = await response.json();
        message =
            errorData.message ||
            errorData.erreur ||
            errorData.error ||
            message;
    } catch {
        // on garde le message par défaut
    }

    return message;
}

export const apiProfesseur = {
    async fetch(endpoint, options = {}) {
        const headers = await buildHeaders(options.headers, options.body);

        const response = await fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            headers,
            credentials: 'include',
        });

        if (!response.ok) {
            const message = await extractErrorMessage(response);
            throw new Error(message);
        }

        if (response.status === 204) {
            return true;
        }

        const contentType = response.headers.get('content-type') || '';

        if (contentType.includes('application/json')) {
            return response.json();
        }

        return response.blob();
    },

    async fetchBlob(endpoint, options = {}) {
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
            const message = await extractErrorMessage(response);
            throw new Error(message);
        }

        const blob = await response.blob();
        const disposition = response.headers.get('content-disposition') || '';
        const match = disposition.match(/filename="?([^"]+)"?/);
        const filename = match ? match[1] : 'fichier';

        return { blob, filename };
    },
};

export default apiProfesseur;