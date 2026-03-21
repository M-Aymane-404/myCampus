import { Bell, ChevronRight } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { apiNotifications } from '../../services/apiNotifications';
import apiAnnoncesProfesseur from '../../services/professeur/apiAnnoncesProfesseur';
import './NotificationHeart.css';

const ROUTES_PAR_ROLE = {
  admin: 'annonces-admin',
  professor: 'annonces',
  student: 'annonces',
};

const formaterDate = (valeur) => {
  if (!valeur) return '';

  const date = new Date(valeur);
  if (Number.isNaN(date.getTime())) {
    return valeur;
  }

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date);
};

const normaliserNotifications = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  return [];
};

const filtrerNotificationsProfesseur = (notifications, annonceIdsAutorisees) => {
  if (!(annonceIdsAutorisees instanceof Set)) {
    return notifications;
  }

  return notifications.filter((notification) => {
    const referenceType = String(notification?.referenceType || '').toUpperCase();
    if (referenceType !== 'ANNONCE') {
      return true;
    }

    const referenceId = notification?.referenceId;
    if (referenceId == null) {
      return false;
    }

    return annonceIdsAutorisees.has(String(referenceId));
  });
};

const construireDestinationNotification = (currentRole, notification) => {
  const destinationParDefaut = ROUTES_PAR_ROLE[currentRole] || ROUTES_PAR_ROLE.student;
  const referenceType = String(notification?.referenceType || '').toUpperCase();

  if (referenceType === 'ANNONCE' && notification?.referenceId != null) {
    const params = {
      annonceSelectionneeId: notification.referenceId,
    };

    if (currentRole === 'professor') {
      params.annonceVueProfesseur = 'admin';
    }

    return {
      id: destinationParDefaut,
      params,
    };
  }

  return destinationParDefaut;
};

const NotificationHeart = ({ currentRole, surChangementElement }) => {
  const [ouvert, setOuvert] = useState(false);
  const [chargement, setChargement] = useState(false);
  const [compteurNonLues, setCompteurNonLues] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [annonceIdsAdminProf, setAnnonceIdsAdminProf] = useState(new Set());
  const conteneurRef = useRef(null);

  const aDesNotificationsNonLues = compteurNonLues > 0;

  const chargerAnnonceIdsAdminProf = async () => {
    if (currentRole !== 'professor') {
      return null;
    }

    try {
      const annonces = await apiAnnoncesProfesseur.listerMesProfAnnonces();
      const ids = new Set(
        (Array.isArray(annonces) ? annonces : [])
          .map((annonce) => annonce?.id)
          .filter((id) => id !== null && id !== undefined)
          .map((id) => String(id))
      );
      setAnnonceIdsAdminProf(ids);
      return ids;
    } catch {
      setAnnonceIdsAdminProf(new Set());
      return new Set();
    }
  };

  const chargerCompteur = async () => {
    try {
      if (currentRole === 'professor') {
        const [page, ids] = await Promise.all([
          apiNotifications.obtenirPage({ page: 0, size: 20 }),
          chargerAnnonceIdsAdminProf(),
        ]);
        const elements = filtrerNotificationsProfesseur(
          normaliserNotifications(page),
          ids
        );
        setCompteurNonLues(elements.filter((notification) => !notification.lu).length);
        return;
      }

      const compteur = await apiNotifications.obtenirCompteurNonLues();
      setCompteurNonLues(compteur);
    } catch {
      setCompteurNonLues(0);
    }
  };

  const chargerNotifications = async () => {
    setChargement(true);
    try {
      let annonceIds = annonceIdsAdminProf;
      if (currentRole === 'professor') {
        annonceIds = await chargerAnnonceIdsAdminProf();
      }

      const page = await apiNotifications.obtenirPage({ page: 0, size: 20 });
      let elements = normaliserNotifications(page);
      if (currentRole === 'professor') {
        elements = filtrerNotificationsProfesseur(elements, annonceIds);
      }

      setNotifications(elements);
      setCompteurNonLues(elements.filter((notification) => !notification.lu).length);
    } catch {
      setNotifications([]);
      setCompteurNonLues(0);
    } finally {
      setChargement(false);
    }
  };

  useEffect(() => {
    chargerCompteur();

    const intervalId = window.setInterval(() => {
      chargerCompteur();
      if (ouvert) {
        chargerNotifications();
      }
    }, 25000);

    const gererFocus = () => {
      chargerCompteur();
      if (ouvert) {
        chargerNotifications();
      }
    };

    window.addEventListener('focus', gererFocus);

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', gererFocus);
    };
  }, [ouvert]);

  useEffect(() => {
    const gererClicExterieur = (event) => {
      if (conteneurRef.current && !conteneurRef.current.contains(event.target)) {
        setOuvert(false);
      }
    };

    if (ouvert) {
      document.addEventListener('mousedown', gererClicExterieur);
    }

    return () => {
      document.removeEventListener('mousedown', gererClicExterieur);
    };
  }, [ouvert]);

  const gererToggle = async () => {
    const prochainEtat = !ouvert;
    setOuvert(prochainEtat);

    if (prochainEtat) {
      await chargerNotifications();
    }
  };

  const marquerCommeLueLocalement = (id) => {
    setNotifications((precedentes) =>
      precedentes.map((notification) =>
        notification.id === id ? { ...notification, lu: true } : notification
      )
    );
    setCompteurNonLues((precedent) => Math.max(0, precedent - 1));
  };

  const gererNotificationClick = async (notification) => {
    if (!notification.lu) {
      marquerCommeLueLocalement(notification.id);
      try {
        await apiNotifications.marquerCommeLue(notification.id);
      } catch {
        await chargerNotifications();
      }
    }

    const destination = construireDestinationNotification(currentRole, notification);
    if (surChangementElement && destination) {
      surChangementElement(destination);
    }

    setOuvert(false);
  };

  const gererToutLire = async () => {
    setNotifications((precedentes) =>
      precedentes.map((notification) => ({ ...notification, lu: true }))
    );
    setCompteurNonLues(0);

    try {
      await apiNotifications.marquerToutesCommeLues();
    } catch {
      await chargerNotifications();
    }
  };

  const titrePanel = useMemo(() => {
    switch (currentRole) {
      case 'admin':
        return 'Notifications administrateur';
      case 'professor':
        return 'Notifications professeur';
      default:
        return 'Notifications etudiant';
    }
  }, [currentRole]);

  return (
    <div className="notification-heart-wrap" ref={conteneurRef}>
      <button
        type="button"
        className="notification-heart-button"
        onClick={gererToggle}
        aria-label="Ouvrir les notifications"
      >
        <Bell size={20} />
        {aDesNotificationsNonLues ? (
          <span className="notification-heart-dot" aria-hidden="true"></span>
        ) : null}
      </button>

      {ouvert ? (
        <div className="notification-panel">
          <div className="notification-panel-header">
            <div>
              <h3>{titrePanel}</h3>
              <p>
                {aDesNotificationsNonLues
                  ? `${compteurNonLues} non lue${compteurNonLues > 1 ? 's' : ''}`
                  : 'Aucune notification non lue'}
              </p>
            </div>

            {aDesNotificationsNonLues ? (
              <button
                type="button"
                className="notification-mark-all"
                onClick={gererToutLire}
              >
                Tout lire
              </button>
            ) : null}
          </div>

          <div className="notification-panel-body">
            {chargement ? (
              <div className="notification-empty-state">Chargement...</div>
            ) : notifications.length === 0 ? (
              <div className="notification-empty-state">
                Aucune notification pour le moment.
              </div>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  type="button"
                  className={`notification-item ${notification.lu ? '' : 'is-unread'}`}
                  onClick={() => gererNotificationClick(notification)}
                >
                  <span className="notification-item-indicator" aria-hidden="true"></span>
                  <div className="notification-item-content">
                    <strong>{notification.titre}</strong>
                    <p>{notification.message}</p>
                    <span>{formaterDate(notification.dateEnvoi)}</span>
                  </div>
                  <ChevronRight size={16} className="notification-item-arrow" />
                </button>
              ))
            )}
          </div>
        </div>
      ) : null}
    </div>
  );
};

export default NotificationHeart;
