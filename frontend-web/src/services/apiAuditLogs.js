import { apiAuth } from './apiAuth';

const STORAGE_KEY = 'mycampus_audit_logs';
const MAX_LOGS = 2000;

const safeParseJson = (value, fallback) => {
    try {
        return JSON.parse(value);
    } catch {
        return fallback;
    }
};

const generateId = () => {
    try {
        return crypto.randomUUID();
    } catch {
        return `${Date.now()}_${Math.random().toString(16).slice(2)}`;
    }
};

const getStoredLogs = () => {
    const raw = localStorage.getItem(STORAGE_KEY);
    const parsed = raw ? safeParseJson(raw, []) : [];
    return Array.isArray(parsed) ? parsed : [];
};

const setStoredLogs = (logs) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(logs));
};

const normalizeString = (v) => (v ?? '').toString();

const getCurrentUserLabel = () => {
    const user = apiAuth.getUser?.() || null;
    return user?.email || user?.nomUtilisateur || user?.username || '-';
};

const stringifyArg = (arg) => {
    if (arg instanceof Error) {
        return arg.stack || arg.message || 'Error';
    }
    if (typeof arg === 'string') return arg;
    try {
        return JSON.stringify(arg);
    } catch {
        return String(arg);
    }
};

const formatConsoleArgs = (args) => {
    try {
        return args.map(stringifyArg).join(' ');
    } catch {
        return '';
    }
};

const addLogEntry = (partial) => {
    const nowIso = new Date().toISOString();

    const entry = {
        id: partial.id || generateId(),
        horodatage: partial.horodatage || nowIso,
        utilisateur: partial.utilisateur ?? getCurrentUserLabel(),
        action: normalizeString(partial.action) || 'EVENT',
        description: normalizeString(partial.description),
        adresseIp: partial.adresseIp ?? 'local',
        statut: partial.statut || 'SUCCESS',
    };

    const logs = getStoredLogs();
    logs.push(entry);

    if (logs.length > MAX_LOGS) {
        const overflow = logs.length - MAX_LOGS;
        logs.splice(0, overflow);
    }

    setStoredLogs(logs);
    return entry;
};

const applyFilters = (logs, filters = {}) => {
    let result = Array.isArray(logs) ? logs.slice() : [];

    if (filters.status) {
        const status = normalizeString(filters.status).toUpperCase();
        result = result.filter(l => normalizeString(l.statut).toUpperCase() === status);
    }

    if (filters.search) {
        const term = normalizeString(filters.search).toLowerCase();
        result = result.filter((l) => {
            const action = normalizeString(l.action).toLowerCase();
            const description = normalizeString(l.description).toLowerCase();
            const utilisateur = normalizeString(l.utilisateur).toLowerCase();
            const adresseIp = normalizeString(l.adresseIp).toLowerCase();
            return (
                action.includes(term) ||
                description.includes(term) ||
                utilisateur.includes(term) ||
                adresseIp.includes(term)
            );
        });
    }

    if (filters.startDate) {
        const start = new Date(filters.startDate).getTime();
        if (!Number.isNaN(start)) {
            result = result.filter(l => new Date(l.horodatage).getTime() >= start);
        }
    }

    if (filters.endDate) {
        const end = new Date(filters.endDate).getTime();
        if (!Number.isNaN(end)) {
            result = result.filter(l => new Date(l.horodatage).getTime() <= end);
        }
    }

    return result;
};

const sortLogs = (logs, sort) => {
    const s = normalizeString(sort);
    if (s.toLowerCase().startsWith('horodatage')) {
        const desc = s.toLowerCase().includes('desc');
        return logs.sort((a, b) => {
            const ta = new Date(a.horodatage).getTime();
            const tb = new Date(b.horodatage).getTime();
            return desc ? tb - ta : ta - tb;
        });
    }
    return logs.sort((a, b) => {
        const ta = new Date(a.horodatage).getTime();
        const tb = new Date(b.horodatage).getTime();
        return tb - ta;
    });
};

const paginate = (logs, params = {}) => {
    const page = Number.isFinite(params.page) ? params.page : 0;
    const size = Number.isFinite(params.size) ? params.size : 20;
    const totalElements = logs.length;
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    const safePage = Math.min(Math.max(page, 0), totalPages - 1);
    const start = safePage * size;
    const end = start + size;
    const content = logs.slice(start, end);
    return {
        content,
        totalElements,
        totalPages,
        number: safePage,
        size,
    };
};

const escapeCsvValue = (value) => {
    const s = normalizeString(value);
    const escaped = s.replace(/"/g, '""');
    return `"${escaped}"`;
};

const buildCsv = (logs) => {
    const header = ['horodatage', 'utilisateur', 'action', 'description', 'adresseIp', 'statut'];
    const lines = [header.join(',')];
    for (const l of logs) {
        lines.push([
            escapeCsvValue(l.horodatage),
            escapeCsvValue(l.utilisateur),
            escapeCsvValue(l.action),
            escapeCsvValue(l.description),
            escapeCsvValue(l.adresseIp),
            escapeCsvValue(l.statut),
        ].join(','));
    }
    return lines.join('\n');
};

export const apiAuditLogs = {
    /**
     * Fetch all audit logs with pagination
     * @param {Object} params - { page, size, sort }
     */
    async getAllLogs(params = {}) {
        try {
            const logs = getStoredLogs();
            const filtered = applyFilters(logs, {});
            const sorted = sortLogs(filtered, params.sort || 'horodatage,desc');
            return paginate(sorted, params);
        } catch (error) {
            console.error('API Error (getAllLogs):', error);
            throw error;
        }
    },

    /**
     * Fetch logs by status
     * @param {string} status - SUCCESS, WARNING, ERROR
     * @param {Object} params - pagination params
     */
    async getLogsByStatus(status, params = {}) {
        try {
            const logs = getStoredLogs();
            const filtered = applyFilters(logs, { status });
            const sorted = sortLogs(filtered, params.sort || 'horodatage,desc');
            return paginate(sorted, params);
        } catch (error) {
            console.error('API Error (getLogsByStatus):', error);
            throw error;
        }
    },

    /**
     * Search logs by text
     * @param {string} searchTerm - search term
     * @param {Object} params - pagination params
     */
    async searchLogs(searchTerm, params = {}) {
        try {
            const logs = getStoredLogs();
            const filtered = applyFilters(logs, { search: searchTerm });
            const sorted = sortLogs(filtered, params.sort || 'horodatage,desc');
            return paginate(sorted, params);
        } catch (error) {
            console.error('API Error (searchLogs):', error);
            throw error;
        }
    },

    /**
     * Export logs to CSV
     * @param {Object} filters - optional filters { status, search, startDate, endDate }
     */
    async exportToCSV(filters = {}) {
        try {
            const logs = getStoredLogs();
            const filtered = applyFilters(logs, filters);
            const sorted = sortLogs(filtered, 'horodatage,desc');
            const csv = buildCsv(sorted);
            const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
            
            // Create download link
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            
            // Generate filename with timestamp
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
            link.download = `audit-logs-${timestamp}.csv`;
            
            // Trigger download
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);
            
            return { success: true };
        } catch (error) {
            console.error('API Error (exportToCSV):', error);
            throw error;
        }
    },

    /**
     * Get logs statistics
     */
    async getStatistics() {
        try {
            const logs = getStoredLogs();
            const stats = {
                total: logs.length,
                SUCCESS: 0,
                WARNING: 0,
                ERROR: 0,
            };

            for (const l of logs) {
                const s = normalizeString(l.statut).toUpperCase();
                if (s === 'SUCCESS' || s === 'WARNING' || s === 'ERROR') {
                    stats[s] += 1;
                }
            }

            return stats;
        } catch (error) {
            console.error('API Error (getStatistics):', error);
            throw error;
        }
    },

    initClientCapture() {
        if (typeof window === 'undefined') return;
        if (window.__mycampusAuditCaptureInitialized) return;

        window.__mycampusAuditCaptureInitialized = true;

        const original = {
            log: console.log,
            info: console.info,
            warn: console.warn,
            error: console.error,
        };

        const record = (statut, action, args) => {
            try {
                addLogEntry({
                    statut,
                    action,
                    description: formatConsoleArgs(args),
                });
            } catch {
                // ignore
            }
        };

        console.log = (...args) => {
            original.log(...args);
            record('SUCCESS', 'console.log', args);
        };

        console.info = (...args) => {
            original.info(...args);
            record('SUCCESS', 'console.info', args);
        };

        console.warn = (...args) => {
            original.warn(...args);
            record('WARNING', 'console.warn', args);
        };

        console.error = (...args) => {
            original.error(...args);
            record('ERROR', 'console.error', args);
        };

        window.addEventListener('error', (event) => {
            record('ERROR', 'window.error', [event?.message, event?.filename, event?.lineno, event?.colno]);
        });

        window.addEventListener('unhandledrejection', (event) => {
            record('ERROR', 'window.unhandledrejection', [event?.reason]);
        });
    },

    addLog(entry) {
        return addLogEntry(entry);
    },

    clearLogs() {
        localStorage.removeItem(STORAGE_KEY);
    }
};
