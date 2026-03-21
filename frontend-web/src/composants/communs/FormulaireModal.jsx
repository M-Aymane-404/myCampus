import { X } from 'lucide-react';
import { useEffect } from 'react';
import BoutonAction from './BoutonAction';
import './FormulaireModal.css';

const FormulaireModal = ({
  titre,
  estOuvert,
  surFermer,
  children,
  surSauvegarder,
  texteBoutonSauvegarder = 'Sauvegarder',
  texteBoutonAnnuler = 'Annuler',
  chargement = false,
  messageErreur = ''
}) => {
  useEffect(() => {
    if (estOuvert) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [estOuvert]);

  if (!estOuvert) return null;

  return (
    <div className="formulaire-modal-overlay" onClick={surFermer}>
      <div className="formulaire-modal-contenu" onClick={(e) => e.stopPropagation()}>
        <div className="formulaire-modal-header">
          <h3 className="formulaire-modal-titre">{titre}</h3>
          <button
            className="formulaire-modal-fermer"
            onClick={surFermer}
            aria-label="Fermer"
          >
            <X size={20} />
          </button>
        </div>
        {messageErreur ? (
          <div className="formulaire-modal-alert formulaire-modal-alert-erreur" role="alert">
            {messageErreur}
          </div>
        ) : null}
        <div className="formulaire-modal-body">
          {children}
        </div>
        <div className="formulaire-modal-footer">
          <BoutonAction
            libelle={texteBoutonAnnuler}
            variante="secondaire"
            taille="moyen"
            onClick={surFermer}
            disabled={chargement}
          />
          {surSauvegarder && (
            <BoutonAction
              libelle={texteBoutonSauvegarder}
              variante="primaire"
              taille="moyen"
              onClick={surSauvegarder}
              disabled={chargement}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default FormulaireModal;

