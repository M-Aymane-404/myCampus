import React, { useState, useEffect, useRef } from 'react';
import { MessageCircle, X, Send, Maximize2, Minimize2, Bot } from 'lucide-react';
import './ChatBot.css';

const ChatBot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState([
        {
            id: 1,
            text: "Bonjour Alae Majjati ! Je suis votre assistant MyCampus. Comment puis-je vous aider aujourd'hui ?",
            sender: 'bot',
            time: '15:13'
        }
    ]);
    const [isExpanded, setIsExpanded] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isOpen]);

    const handleSendMessage = (e) => {
        e.preventDefault();
        if (!message.trim()) return;

        const newMessage = {
            id: messages.length + 1,
            text: message,
            sender: 'user',
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };

        setMessages([...messages, newMessage]);
        setMessage('');

        // Simulate bot response
        setTimeout(() => {
            const botResponse = {
                id: messages.length + 2,
                text: "Merci pour votre message. Je suis là pour vous aider avec MyCampus.",
                sender: 'bot',
                time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            };
            setMessages(prev => [...prev, botResponse]);
        }, 1000);
    };

    const toggleChat = () => setIsOpen(!isOpen);
    const toggleExpand = () => setIsExpanded(!isExpanded);

    return (
        <div className={`chatbot-container ${isOpen ? 'open' : ''} ${isExpanded ? 'expanded' : ''}`}>
            {/* Floating Action Button */}
            <button
                className={`chatbot-fab ${isOpen ? 'hidden' : ''}`}
                onClick={toggleChat}
                aria-label="Ouvrir le chat"
            >
                <div className="fab-icon-container">
                    <MessageCircle size={28} />
                    <span className="notification-badge"></span>
                </div>
            </button>

            {/* Chat Window */}
            <div className="chat-window">
                {/* Header */}
                <div className="chat-header">
                    <div className="header-info">
                        <div className="bot-avatar">
                            <Bot size={20} color="white" />
                        </div>
                        <div className="header-text">
                            <h3>Assistant MyCampus</h3>
                            <span className="online-status">
                                <span className="status-dot"></span>
                                EN LIGNE
                            </span>
                        </div>
                    </div>
                    <div className="header-actions">
                        <button onClick={toggleExpand} className="action-btn">
                            {isExpanded ? <Minimize2 size={18} /> : <Maximize2 size={18} />}
                        </button>
                        <button onClick={toggleChat} className="action-btn close-btn">
                            <X size={20} />
                        </button>
                    </div>
                </div>

                {/* Message List */}
                <div className="chat-messages">
                    {messages.map((msg) => (
                        <div key={msg.id} className={`message-wrapper ${msg.sender}`}>
                            <div className="message-content">
                                <p>{msg.text}</p>
                                <span className="message-time">{msg.time}</span>
                            </div>
                        </div>
                    ))}
                    <div ref={messagesEndRef} />
                </div>

                {/* Footer / Input */}
                <div className="chat-footer">
                    <form className="chat-input-container" onSubmit={handleSendMessage}>
                        <input
                            type="text"
                            placeholder="Posez votre question..."
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                        />
                        <button type="submit" className="send-btn" disabled={!message.trim()}>
                            <Send size={20} />
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ChatBot;
