import React from 'react';
import { User, Clock } from 'lucide-react';
import './PlanningCard.css';

const PlanningCard = ({ seance }) => {
    // Categories based on mockup
    const categoryColors = {
        'TRONC COMMUN': '#5C54ED', // Blue
        'TRAVAUX PRATIQUES': '#10B981', // Green
        'EXAMENS': '#EF4444' // Red/Pink
    };

    const type = seance.typeSeance || 'TRONC COMMUN';
    const accentColor = categoryColors[type] || categoryColors['TRONC COMMUN'];

    return (
        <div className="planning-card" style={{ borderLeft: `6px solid ${accentColor}` }}>
            <div className="card-top-row">
                <div className="time-pill">
                    {seance.heureDebut} — {seance.heureFin}
                </div>
                <div className="status-dot" style={{ backgroundColor: accentColor }}></div>
            </div>

            <div className="card-middle">
                <h3 className="subject-name">{seance.matiere || seance.nom}</h3>
                <div className="badges-row">
                    <span className="code-badge">{seance.code || seance.module}</span>
                    <span className="coeff-badge">COEFF: {seance.coefficient || 2}</span>
                </div>
            </div>

            <div className="card-bottom">
                <div className="prof-info">
                    <User size={14} />
                    <span>{seance.professeur || 'Non assigné'}</span>
                </div>
                {seance.salle && (
                    <div className="salle-info">
                        <span>Salle: {seance.salle}</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PlanningCard;
