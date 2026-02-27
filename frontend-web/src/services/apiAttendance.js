import { apiAuth } from './apiAuth';

const API_BASE_URL = 'http://localhost:8080/api';

export const apiAttendance = {
    /**
     * Fetch student profile to determine inscription year and graduation year.
     * Reuses the endpoint from apiGrades but keeps it independent here.
     */
    fetchStudentProfile: async () => {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const response = await fetch(`${API_BASE_URL}/etudiant/moi`, {
                headers: headers,
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) {
                    console.error("Unauthorized - Session likely expired");
                    // Optionally redirect to login or handle globally
                }
                throw new Error('Failed to fetch profile');
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error fetching student profile:', error);
            throw error;
        }
    },

    /**
     * Fetch all attendance records for the connected student.
     * Can optionally filter by status or date if needed, but we usually want all
     * to populate the calendar.
     */
    fetchAttendance: async (status = null, date = null) => {
        try {
            const params = new URLSearchParams();
            if (status) params.append('status', status);
            if (date) params.append('date', date); // Format: YYYY-MM-DD

            const url = `${API_BASE_URL}/etudiant/moi/presences?${params.toString()}`;

            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const response = await fetch(url, {
                headers: headers,
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Failed to fetch attendance');
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error fetching attendance:', error);
            throw error;
        }
    },

    /**
     * Submit a justification for an absence with optional document upload
     */
    submitJustification: async (presenceId, typeMotif, description = null, file = null) => {
        try {
            // Map frontend type motif to backend enum values
            const backendTypeMotif = mapTypeMotifToBackend(typeMotif);

            // For PUT requests with multipart/form-data, non-file parameters are often 
            // more reliably passed as query parameters in Spring Boot.
            const params = new URLSearchParams();
            params.append('typeMotif', backendTypeMotif);
            if (description) {
                params.append('descriptionDeJustification', description);
            }

            const formData = new FormData();
            if (file) {
                formData.append('motif', file);
            }

            // Get fresh auth headers (includes automatic token refresh if needed)
            const authHeaders = await apiAuth.getAuthHeadersWithRefresh();
            // Don't set Content-Type header for FormData - browser sets it automatically with boundary
            const { 'Content-Type': _, ...headersWithoutContentType } = authHeaders;

            const url = `http://localhost:8080/justification/${presenceId}?${params.toString()}`;

            const response = await fetch(url, {
                method: 'PUT',
                headers: headersWithoutContentType,
                credentials: 'include',
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Failed to submit justification: ${response.status} ${errorText}`);
            }

            return { success: true };
        } catch (error) {
            console.error('Error submitting justification:', error);
            throw error;
        }
    }
};

/**
 * Map frontend type motif strings to backend enum values
 */
const mapTypeMotifToBackend = (frontendType) => {
    const mapping = {
        'Certificat Médical': 'CERTIFICAT_MEDICAL',
        'Raisons Familiales': 'RAISON_FAMILIALE',
        'Autre': 'AUTRE'
    };
    return mapping[frontendType] || 'AUTRE';
};
