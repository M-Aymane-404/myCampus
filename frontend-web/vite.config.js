import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    cors: false,
    proxy: {
      '/webhook': {
        // Default for local dev (Vite on host): n8n is exposed on http://127.0.0.1:5679
        // When running via docker-compose, `N8N_PROXY_TARGET` is set to http://n8n:5678.
        target: process.env.N8N_PROXY_TARGET || 'http://127.0.0.1:5679',
        changeOrigin: true,
        secure: false,
      },
      '/webhook-test': {
        target: process.env.N8N_PROXY_TARGET || 'http://127.0.0.1:5679',
        changeOrigin: true,
        secure: false,
      },
    },
    fs: {
      strict: true,
      deny: ['**/.env', '**/.env.*', '**/*.{crt,pem,key}'],
    },
  },
  preview: {
    host: '127.0.0.1',
    port: 4173,
    strictPort: true,
    cors: false,
  },
})
