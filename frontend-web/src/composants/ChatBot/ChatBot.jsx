import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Bot,
  Maximize2,
  MessageCircle,
  Minimize2,
  Paperclip,
  SendHorizontal,
  X,
} from 'lucide-react';
import { apiAuth } from '../../services/apiAuth';
import './ChatBot.css';

const CHAT_WEBHOOK_URL = 'http://localhost:5679/webhook-test/chat';

const DEFAULT_MESSAGE =
  "Je suis votre assistant MyCampus. Posez-moi une question sur votre contexte académique ou vos documents.";

function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return {};
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) return {};
    const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(base64);
    return JSON.parse(json);
  } catch {
    return {};
  }
}

function normalizeRole(userRole, scope) {
  const raw = (userRole || '').toLowerCase();
  if (raw === 'admin' || raw === 'administrateur') return 'ADMIN';
  if (raw === 'professor' || raw === 'professeur' || raw === 'prof') return 'PROFESSEUR';
  if (raw === 'student' || raw === 'etudiant' || raw === 'etud') return 'ETUDIANT';

  if (typeof scope === 'string') {
    if (scope.includes('ROLE_ADMIN')) return 'ADMIN';
    if (scope.includes('ROLE_PROFESSEUR')) return 'PROFESSEUR';
    if (scope.includes('ROLE_ETUDIANT')) return 'ETUDIANT';
  }

  return 'ETUDIANT';
}

function cleanupMarkdownAndOrganize(text) {
  if (!text || typeof text !== 'string') return '';

  const cleaned = text
    .replace(/\r\n/g, '\n')
    .replace(/```[\s\S]*?```/g, (block) =>
      block
        .replace(/```[a-zA-Z]*/g, '')
        .replace(/```/g, '')
        .trim()
    )
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '$1 ($2)')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/__([^_]+)__/g, '$1')
    .replace(/\*([^*\n]+)\*/g, '$1')
    .replace(/_([^_\n]+)_/g, '$1')
    .replace(/^\s{0,3}#{1,6}\s*/gm, '')
    .replace(/^\s*([-*_]\s*){3,}$/gm, '')
    .replace(/^\s*>\s?/gm, '')
    .replace(/^\s*[-*]\s+/gm, '• ')
    .replace(/^\s*\d+\.\s+/gm, (value) => value.trimStart())
    .replace(/\n{3,}/g, '\n\n')
    .trim();

  const lines = cleaned.split('\n');
  const organized = [];

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line) {
      if (organized.length > 0 && organized[organized.length - 1] !== '') {
        organized.push('');
      }
      continue;
    }

    if (line.endsWith(':') && organized.length > 0 && organized[organized.length - 1] !== '') {
      organized.push('');
    }

    organized.push(line);
  }

  return organized.join('\n').replace(/\n{3,}/g, '\n\n').trim();
}

function formatTime(date) {
  return new Intl.DateTimeFormat('fr-FR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export default function ChatBot() {
  const [isOpen, setIsOpen] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [messages, setMessages] = useState([]);
  const scrollRef = useRef(null);

  const userContext = useMemo(() => {
    const user = apiAuth.getUser() || {};
    const token = apiAuth.getToken();
    const payload = decodeJwtPayload(token);
    const fullName = [user.prenom, user.nom].filter(Boolean).join(' ').trim();
    const displayName = fullName || user.email || 'utilisateur';
    const userIdFromToken = Number(payload.userId);
    const userIdFromUser = Number(user.id);

    return {
      displayName,
      userId: Number.isFinite(userIdFromToken) && userIdFromToken > 0
        ? userIdFromToken
        : Number.isFinite(userIdFromUser) && userIdFromUser > 0
          ? userIdFromUser
          : null,
      role: normalizeRole(user.role, payload.scope),
    };
  }, []);

  useEffect(() => {
    if (messages.length > 0) {
      scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  useEffect(() => {
    if (!isOpen || messages.length > 0) return;
    setMessages([
      {
        id: crypto.randomUUID(),
        sender: 'bot',
        text: `Bonjour ${userContext.displayName} ! ${DEFAULT_MESSAGE}`,
        time: formatTime(new Date()),
      },
    ]);
  }, [isOpen, messages.length, userContext.displayName]);

  const sendMessage = async () => {
    const trimmedMessage = inputValue.trim();
    if (!trimmedMessage || isSending) return;

    if (!userContext.userId) {
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          sender: 'bot',
          text: "Je ne peux pas vous identifier pour le moment. Reconnectez-vous puis réessayez.",
          time: formatTime(new Date()),
        },
      ]);
      return;
    }

    const userMessage = {
      id: crypto.randomUUID(),
      sender: 'user',
      text: trimmedMessage,
      time: formatTime(new Date()),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsSending(true);

    try {
      console.log('ChatBot request URL:', CHAT_WEBHOOK_URL);

      const response = await fetch(CHAT_WEBHOOK_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: trimmedMessage,
          userId: userContext.userId,
          role: userContext.role,
        }),
      });

      const raw = await response.text();
      let data;
      try {
        data = raw ? JSON.parse(raw) : {};
      } catch {
        data = {};
      }

      console.log('ChatBot response status:', response.status);
      console.log('ChatBot response body:', data);

      if (!response.ok || data?.ok === false) {
        throw new Error("Le chatbot n'a pas pu répondre.");
      }

      const reply = typeof data?.reply === 'string' ? data.reply : '';
      const normalizedReply = cleanupMarkdownAndOrganize(reply);

      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          sender: 'bot',
          text: normalizedReply || reply,
          time: formatTime(new Date()),
        },
      ]);
    } catch (error) {
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          sender: 'bot',
          text: "Le service de chat est indisponible pour le moment. Vérifiez que n8n est bien démarré.",
          time: formatTime(new Date()),
        },
      ]);
      console.error('ChatBot error:', error);
    } finally {
      setIsSending(false);
    }
  };

  const onInputKeyDown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="mc-chatbot">
      {isOpen && (
        <section className={`mc-chatbot-panel ${isExpanded ? 'expanded' : ''}`}>
          <header className="mc-chatbot-header">
            <div className="mc-chatbot-title">
              <div className="mc-chatbot-bot-icon">
                <Bot size={16} />
              </div>
              <div>
                <strong>Assistant MyCampus</strong>
                <span>EN LIGNE</span>
              </div>
            </div>
            <div className="mc-chatbot-actions">
              <button
                type="button"
                onClick={() => setIsExpanded((prev) => !prev)}
                aria-label={isExpanded ? 'Réduire la fenêtre' : 'Agrandir la fenêtre'}
              >
                {isExpanded ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
              </button>
              <button type="button" onClick={() => setIsOpen(false)} aria-label="Fermer le chat">
                <X size={16} />
              </button>
            </div>
          </header>

          <div className="mc-chatbot-messages">
            {messages.map((message) => (
              <article key={message.id} className={`mc-chatbot-message ${message.sender}`}>
                <p>{message.text}</p>
                <time>{message.time}</time>
              </article>
            ))}
            {isSending && (
              <article className="mc-chatbot-message bot">
                <p>Assistant MyCampus réfléchit...</p>
                <time>{formatTime(new Date())}</time>
              </article>
            )}
            <div ref={scrollRef} />
          </div>

          <footer className="mc-chatbot-input-zone">
            <textarea
              value={inputValue}
              onChange={(event) => setInputValue(event.target.value)}
              onKeyDown={onInputKeyDown}
              placeholder="Posez votre question..."
              rows={1}
            />
            <button
              type="button"
              className="mc-chatbot-send"
              onClick={sendMessage}
              disabled={isSending || !inputValue.trim()}
              aria-label="Envoyer"
            >
              <SendHorizontal size={18} />
            </button>
          </footer>
        </section>
      )}

      <button
        type="button"
        className="mc-chatbot-trigger"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-label={isOpen ? 'Fermer le chat' : 'Ouvrir le chat'}
      >
        <MessageCircle size={26} />
      </button>
    </div>
  );
}
