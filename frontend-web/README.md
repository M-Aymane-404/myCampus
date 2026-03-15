# React + Vite

## MyCampus Chatbot (n8n RAG)

Le frontend inclut un composant `ChatBot` connecté au webhook n8n.

Configuration recommandée:

1. Créer un fichier `.env` dans `frontend-web/`
2. Ajouter:

```bash
VITE_N8N_CHAT_WEBHOOK_URL=http://localhost:5679/webhook/chat
```

Payload envoyé au webhook:

```json
{
  "message": "Question de l'utilisateur",
  "userId": 123,
  "role": "ETUDIANT"
}
```

Le chatbot est injecté dans l'application après connexion et s'ouvre via le bouton flottant en bas à droite.

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) (or [oxc](https://oxc.rs) when used in [rolldown-vite](https://vite.dev/guide/rolldown)) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is currently not compatible with SWC. See [this issue](https://github.com/vitejs/vite-plugin-react/issues/428) for tracking the progress.

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
