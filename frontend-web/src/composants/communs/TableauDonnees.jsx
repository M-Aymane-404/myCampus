import { Search, Plus, Edit, Trash2, Eye, Upload } from 'lucide-react';
import { useState, useRef } from 'react';
import BoutonAction from './BoutonAction';
import './TableauDonnees.css';

const TableauDonnees = ({
  titre,
  colonnes = [],
  donnees = [],
  surAjouter,
  surModifier,
  surSupprimer,
  surVoir,
  surImporterCSV,
  recherche = true,
  actions = true
}) => {
  const fileInputRef = useRef(null);
  const [termeRecherche, setTermeRecherche] = useState('');
  const [pageCourante, setPageCourante] = useState(1);
  const elementsParPage = 10;

  const donneesFiltrees = donnees.filter(item => {
    if (!termeRecherche) return true;
    return Object.values(item).some(val => 
      String(val).toLowerCase().includes(termeRecherche.toLowerCase())
    );
  });

  const indexDebut = (pageCourante - 1) * elementsParPage;
  const indexFin = indexDebut + elementsParPage;
  const donneesPage = donneesFiltrees.slice(indexDebut, indexFin);
  const totalPages = Math.ceil(donneesFiltrees.length / elementsParPage);

  return (
    <div className="tableau-donnees">
      <div className="tableau-donnees-header">
        <h2 className="tableau-donnees-titre">{titre}</h2>
        <div className="tableau-donnees-actions">
          {recherche && (
            <div className="recherche-container">
              <Search className="recherche-icone" size={18} />
              <input
                type="text"
                className="recherche-input"
                placeholder="Rechercher..."
                value={termeRecherche}
                onChange={(e) => {
                  setTermeRecherche(e.target.value);
                  setPageCourante(1);
                }}
              />
            </div>
          )}
          {surImporterCSV && (
            <>
              <input
                type="file"
                ref={fileInputRef}
                accept=".csv"
                style={{ display: 'none' }}
                onChange={(e) => {
                  const file = e.target.files[0];
                  if (file) {
                    surImporterCSV(file);
                    // Réinitialiser l'input pour permettre de sélectionner le même fichier à nouveau
                    e.target.value = '';
                  }
                }}
              />
              <BoutonAction
                libelle="Importer CSV"
                variante="secondaire"
                taille="moyen"
                icone={Upload}
                onClick={() => fileInputRef.current?.click()}
              />
            </>
          )}
          {surAjouter && (
            <BoutonAction
              libelle="Ajouter"
              variante="primaire"
              taille="moyen"
              icone={Plus}
              onClick={surAjouter}
            />
          )}
        </div>
      </div>

      <div className="tableau-donnees-contenu">
        {donneesPage.length === 0 ? (
          <div className="tableau-donnees-vide">
            <p>Aucune donnée disponible</p>
          </div>
        ) : (
          <table className="tableau-donnees-table">
            <thead>
              <tr>
                {colonnes.map((col, index) => (
                  <th key={index}>{col.label}</th>
                ))}
                {actions && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {donneesPage.map((ligne, indexLigne) => (
                <tr key={indexLigne}>
                  {colonnes.map((col, indexCol) => (
                    <td key={indexCol}>
                      {col.render ? col.render(ligne) : ligne[col.key]}
                    </td>
                  ))}
                  {actions && (
                    <td>
                      <div className="tableau-donnees-boutons-actions">
                        {surVoir && (
                          <button
                            className="bouton-action-icone"
                            onClick={() => surVoir(ligne)}
                            title="Voir"
                          >
                            <Eye size={16} />
                          </button>
                        )}
                        {surModifier && (
                          <button
                            className="bouton-action-icone"
                            onClick={() => surModifier(ligne)}
                            title="Modifier"
                          >
                            <Edit size={16} />
                          </button>
                        )}
                        {surSupprimer && (
                          <button
                            className="bouton-action-icone bouton-action-danger"
                            onClick={() => surSupprimer(ligne)}
                            title="Supprimer"
                          >
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {totalPages > 1 && (
        <div className="tableau-donnees-pagination">
          <button
            className="bouton-pagination"
            onClick={() => setPageCourante(p => Math.max(1, p - 1))}
            disabled={pageCourante === 1}
          >
            Précédent
          </button>
          <span className="pagination-info">
            Page {pageCourante} sur {totalPages}
          </span>
          <button
            className="bouton-pagination"
            onClick={() => setPageCourante(p => Math.min(totalPages, p + 1))}
            disabled={pageCourante === totalPages}
          >
            Suivant
          </button>
        </div>
      )}
    </div>
  );
};

export default TableauDonnees;

