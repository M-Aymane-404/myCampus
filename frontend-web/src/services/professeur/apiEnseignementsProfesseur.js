import apiProfesseur from './apiProfesseur';

export const apiEnseignementsProfesseur = {
    async listerEnseignements() {
        return apiProfesseur.fetch('/enseignements', { method: 'GET' });
    },
};

export default apiEnseignementsProfesseur;