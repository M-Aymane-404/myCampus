import './ChampFormulaire.css';

const ChampFormulaire = ({
  label,
  nom,
  type = 'text',
  valeur,
  onChange,
  placeholder,
  requis = false,
  erreur,
  options = [],
  multiple = false
}) => {
  return (
    <div className="champ-formulaire">
      <label htmlFor={nom} className="champ-formulaire-label">
        {label}
        {requis && <span className="champ-formulaire-requis">*</span>}
      </label>
      {type === 'select' ? (
        <select
          id={nom}
          name={nom}
          className={`champ-formulaire-input ${erreur ? 'champ-formulaire-erreur' : ''}`}
          value={multiple ? (valeur || []) : (valeur || '')}
          onChange={onChange}
          multiple={multiple}
        >
          {!multiple && <option value="">Sélectionner...</option>}
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : type === 'textarea' ? (
        <textarea
          id={nom}
          name={nom}
          className={`champ-formulaire-input champ-formulaire-textarea ${erreur ? 'champ-formulaire-erreur' : ''}`}
          value={valeur || ''}
          onChange={onChange}
          placeholder={placeholder}
          rows={4}
        />
      ) : (
        <input
          id={nom}
          name={nom}
          type={type}
          className={`champ-formulaire-input ${erreur ? 'champ-formulaire-erreur' : ''}`}
          value={valeur || ''}
          onChange={onChange}
          placeholder={placeholder}
          required={requis}
        />
      )}
      {erreur && <span className="champ-formulaire-message-erreur">{erreur}</span>}
    </div>
  );
};

export default ChampFormulaire;

