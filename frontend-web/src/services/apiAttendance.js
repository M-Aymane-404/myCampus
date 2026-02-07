import { apiAuth } from './apiAuth';

const API_BASE_URL = 'http://localhost:8080/api';

export const apiAttendance = {
    /**
     * Fetch student profile to determine inscription year and graduation year.
     * Reuses the endpoint from apiGrades but keeps it independent here.
     */
    fetchStudentProfile: async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/etudiant/moi`, {
                headers: apiAuth.getAuthHeaders()
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

            const response = await fetch(url, {
                headers: apiAuth.getAuthHeaders()
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
    }
};
