export const formateurs = {
  formaterNombre(nombre) {
    if (nombre === null || nombre === undefined) return '0';
    return nombre.toLocaleString('fr-FR');
  },

  formaterDate(date) {
    if (!date) return '';
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(dateObj.getTime())) return '';
    return dateObj.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  },

  formaterDateHeure(date) {
    if (!date) return '';
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(dateObj.getTime())) return '';
    return dateObj.toLocaleString('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  },

  formaterTemps(secondes) {
    const heures = Math.floor(secondes / 3600);
    const minutes = Math.floor((secondes % 3600) / 60);
    const secs = secondes % 60;

    if (heures > 0) {
      return `${heures}h ${minutes}m ${secs}s`;
    }
    if (minutes > 0) {
      return `${minutes}m ${secs}s`;
    }
    return `${secs}s`;
  },

  formaterPourcentage(valeur, total) {
    if (!total || total === 0) return '0%';
    const pourcentage = (valeur / total) * 100;
    return `${pourcentage.toFixed(1)}%`;
  },
};

