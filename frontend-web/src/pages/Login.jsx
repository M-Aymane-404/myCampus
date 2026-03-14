import { useState } from 'react';
import { GraduationCap, Eye, EyeOff, LayoutDashboard, ChevronRight } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import ReCAPTCHA from 'react-google-recaptcha';
import './Login.css';

const Login = ({ onLogin }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [erreur, setErreur] = useState('');
  const [chargement, setChargement] = useState(false);
  const [recaptchaToken, setRecaptchaToken] = useState(null);

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setErreur('');
  };

  const gererRecaptchaChange = (token) => {
    setRecaptchaToken(token);
    setErreur('');
  };

  const gererSoumission = async (e) => {
    e.preventDefault();

    // Validation basique
    if (!formData.email || !formData.password) {
      setErreur('Veuillez remplir tous les champs');
      return;
    }

    // Validation reCAPTCHA
    if (!recaptchaToken) {
      setErreur('Veuillez valider le reCAPTCHA');
      return;
    }

    setChargement(true);
    setErreur('');

    try {
      // Login with real backend using email, password and recaptcha token
      const data = await apiAuth.login(formData.email, formData.password, recaptchaToken);

      // The role is now extracted from the JWT token in apiAuth.login
      const role = data.user?.role;

      console.log('Login successful with role:', role);

      // Call the onLogin callback with the role from backend
      if (onLogin) {
        onLogin({
          email: formData.email,
          role: role,
          user: data.user
        });
      }
    } catch (error) {
      console.error('Login error:', error);
      // Better error message handling
      let errorMessage = 'Erreur de connexion. Vérifiez vos identifiants.';

      if (error.message) {
        if (error.message.includes('500') || error.message.includes('Erreur interne')) {
          errorMessage = 'Erreur interne du serveur. Veuillez contacter l\'administrateur.';
        } else if (error.message.includes('401') || error.message.includes('Unauthorized')) {
          errorMessage = 'Email ou mot de passe incorrect.';
        } else if (error.message.includes('404') || error.message.includes('Not Found')) {
          errorMessage = 'Service d\'authentification non disponible.';
        } else {
          errorMessage = error.message;
        }
      }

      setErreur(errorMessage);
    } finally {
      setChargement(false);
    }
  };

  return (
      <div className="login-screen">
        <div className="login-main-container">
          {/* Left Side: Brand & Motto */}
          <div className="login-left-pane">
            <div className="brand-header">
              <div className="brand-logo-m">M</div>
              <span className="brand-name">MyCampus</span>
            </div>

            <div className="motto-container">
              <h1 className="motto-text">
                L'excellence<br />
                académique à<br />
                portée de clic.
              </h1>
              <p className="motto-subtext">
                Une plateforme moderne pour connecter professeurs, étudiants et administration.
              </p>
            </div>

            {/* Floating Analytics Card */}
            <div className="analytics-card-floating">
              <div className="analytics-icon-box">
                <LayoutDashboard size={24} />
              </div>
              <div className="analytics-info">
                <h4>Dashboard Analytics</h4>
                <p>Suivez vos progrès en temps réel.</p>
              </div>
            </div>
          </div>

          {/* Right Side: Login Form */}
          <div className="login-right-pane">
            <div className="login-form-wrapper">
              <div className="login-greeting">
                <h2>Bon retour !</h2>
                <p>Connectez-vous pour continuer l'aventure MyCampus.</p>
              </div>

              <form className="modern-login-form" onSubmit={gererSoumission}>
                {erreur && <div className="modern-error-msg">{erreur}</div>}

                <div className="modern-form-group">
                  <label>E-MAIL INSTITUTIONNEL</label>
                  <input
                      type="text"
                      name="email"
                      value={formData.email}
                      onChange={gererChangement}
                      placeholder="demo@mycampus.edu"
                      required
                  />
                </div>

                <div className="modern-form-group">
                  <label>MOT DE PASSE</label>
                  <div className="modern-password-wrapper">
                    <input
                        type={showPassword ? 'text' : 'password'}
                        name="password"
                        value={formData.password}
                        onChange={gererChangement}
                        placeholder="••••••••"
                        required
                    />
                    <button
                        type="button"
                        className="modern-pass-toggle"
                        onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <div className="modern-form-group">
                  <ReCAPTCHA
                      sitekey="6Lf2-H4sAAAAALB3KsGVPQzMHO0_pAhVZmQXBpJC"
                      onChange={gererRecaptchaChange}
                      theme="light"
                      size="normal"
                  />
                </div>

                <button type="submit" className="modern-submit-btn" disabled={chargement}>
                  <span>{chargement ? 'Connexion en cours...' : 'Se connecter'}</span>
                  {!chargement && <ChevronRight size={20} />}
                </button>
              </form>

              <div className="login-help-footer">
                <p>Besoin d'aide ? <a href="#">Accéder au support informatique</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default Login;