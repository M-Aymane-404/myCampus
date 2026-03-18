import { apiAuth } from './apiAuth';

const API_BASE_URL = 'http://localhost:8080/api';

const parseAttendanceDate = (dateVal) => {
    if (!dateVal) return null;
    if (Array.isArray(dateVal)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = dateVal;
        return new Date(year, month - 1, day, hour, minute, second);
    }

    const parsed = new Date(dateVal);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const getAttendanceTimestamp = (record) => {
    const parsed = parseAttendanceDate(record?.dateDeclaration)
        || parseAttendanceDate(record?.seance?.date);
    return parsed ? parsed.getTime() : 0;
};

const getAttendanceLogicalKey = (record) => {
    if (record?.seance?.id != null) {
        return `seance-${record.seance.id}`;
    }
    if (record?.id != null) {
        return `presence-${record.id}`;
    }

    return [
        record?.seance?.date || 'no-date',
        record?.seance?.codeMatiere || 'no-code',
        record?.status || 'no-status'
    ].join('|');
};

const dedupeAttendanceRecords = (records) => {
    if (!Array.isArray(records)) {
        return [];
    }

    const recordsByKey = new Map();
    records.forEach((record) => {
        if (!record) return;

        const key = getAttendanceLogicalKey(record);
        const existing = recordsByKey.get(key);
        if (!existing || getAttendanceTimestamp(record) >= getAttendanceTimestamp(existing)) {
            recordsByKey.set(key, record);
        }
    });

    return Array.from(recordsByKey.values()).sort(
        (left, right) => getAttendanceTimestamp(right) - getAttendanceTimestamp(left)
    );
};

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
                    console.error('Unauthorized - Session likely expired');
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
            if (date) params.append('date', date);

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
            return dedupeAttendanceRecords(data);
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
            const backendTypeMotif = mapTypeMotifToBackend(typeMotif);

            const params = new URLSearchParams();
            params.append('typeMotif', backendTypeMotif);
            if (description) {
                params.append('descriptionDeJustification', description);
            }

            const formData = new FormData();
            if (file) {
                formData.append('motif', file);
            }

            const authHeaders = await apiAuth.getAuthHeadersWithRefresh();
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

const mapTypeMotifToBackend = (frontendType) => {
    const mapping = {
        'Certificat Medical': 'CERTIFICAT_MEDICAL',
        'Raisons Familiales': 'RAISON_FAMILIALE',
        'Autre': 'AUTRE'
    };
    return mapping[frontendType] || 'AUTRE';
};
