import { useState } from 'react';
import { GraduationCap, Eye, EyeOff } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import './Login.css';

const Login = ({ onLogin }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    role: 'admin' // Default role for mock login
  });
  const [showPassword, setShowPassword] = useState(false);
  const [erreur, setErreur] = useState('');
  const [chargement, setChargement] = useState(false);

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setErreur('');
  };

  const gererSoumission = async (e) => {
    e.preventDefault();
    
    // Validation basique
    if (!formData.email || !formData.password) {
      setErreur('Veuillez remplir tous les champs');
      return;
    }

    setChargement(true);
    setErreur('');

    try {
      // MOCK LOGIN MODE: Use role from dropdown, bypass backend authentication
      // Call mock login which accepts any credentials
      const data = await apiAuth.mockLogin(formData.email, formData.password, formData.role);
      
      // Use the role selected in the dropdown (not from backend)
      const role = formData.role;
      
      console.log('Login successful with role:', role);
      
      // Call the onLogin callback with the selected role
      if (onLogin) {
        onLogin({
          email: formData.email,
          role: role,
          user: data.user || { email: formData.email, role: role }
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
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <div className="login-logo-icon">
              <GraduationCap size={40} />
            </div>
            <h1 className="login-title">MyCampus</h1>
            <p className="login-subtitle">University Management System</p>
          </div>
        </div>

        <form className="login-form" onSubmit={gererSoumission}>
          {erreur && (
            <div className="login-erreur">
              {erreur}
            </div>
          )}

          <div className="login-form-group">
            <label htmlFor="email" className="login-label">
              Email / Username
            </label>
            <input
              type="text"
              id="email"
              name="email"
              className="login-input"
              value={formData.email}
              onChange={gererChangement}
              placeholder="email@univ.ma"
              required
            />
          </div>

          <div className="login-form-group">
            <label htmlFor="password" className="login-label">
              Password
            </label>
            <div className="login-password-container">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                className="login-input"
                value={formData.password}
                onChange={gererChangement}
                placeholder="Enter your password"
                required
              />
              <button
                type="button"
                className="login-password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                aria-label="Toggle password visibility"
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
          </div>

          <div className="login-form-group">
            <label htmlFor="role" className="login-label">
              Role
            </label>
            <select
              id="role"
              name="role"
              className="login-input"
              value={formData.role}
              onChange={gererChangement}
              required
            >
              <option value="admin">Administrator</option>
              <option value="professor">Professor</option>
              <option value="student">Student</option>
            </select>
          </div>

          <button type="submit" className="login-button" disabled={chargement}>
            {chargement ? 'Connexion...' : 'Sign In'}
          </button>
        </form>

        <div className="login-footer">
          <p className="login-footer-text">
            Forgot your password? <a href="#" className="login-link">Contact IT Support</a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
