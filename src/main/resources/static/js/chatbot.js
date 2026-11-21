document.addEventListener('DOMContentLoaded', () => {
    const widget = document.getElementById('chatbot-widget');

    // El widget solo existe para los roles autorizados en Thymeleaf.
    if (!widget) return;

    const toggle = document.getElementById('chatbot-toggle');
    const panel = document.getElementById('chatbot-panel');
    const close = document.getElementById('chatbot-close');
    const messages = document.getElementById('chatbot-messages');
    const form = document.getElementById('chatbot-form');
    const input = document.getElementById('chatbot-input');
    const send = document.getElementById('chatbot-send');

    const user = widget.dataset.user || 'usuario';
    const storageKey = 'ciruvet_chat_v1_' + encodeURIComponent(user);

    let history = [];

    function scrollToBottom() {
        messages.scrollTop = messages.scrollHeight;
    }

    function showMessage(text, role = 'bot', extraClass = '') {
        const bubble = document.createElement('div');

        bubble.classList.add('chatbot-message', role);

        if (extraClass) {
            bubble.classList.add(extraClass);
        }

        // textContent evita interpretar la respuesta como HTML.
        bubble.textContent = text;
        messages.appendChild(bubble);
        scrollToBottom();

        return bubble;
    }

    function renderHistory() {
        messages.replaceChildren();

        if (history.length === 0) {
            showMessage(
                'Hola. Soy el asistente virtual de CiruVet. ¿En qué puedo ayudarte?'
            );
            return;
        }

        history.forEach(item => {
            showMessage(
                item.content,
                item.role === 'user' ? 'user' : 'bot'
            );
        });
    }

    function saveHistory() {
        try {
            sessionStorage.setItem(
                storageKey,
                JSON.stringify(history)
            );
        } catch (error) {
            console.warn('No se pudo guardar la conversación temporal.');
        }
    }

    // Recuperar la conversación de esta pestaña.
    try {
        const saved = sessionStorage.getItem(storageKey);

        if (saved) {
            const parsed = JSON.parse(saved);

            if (Array.isArray(parsed)) {
                history = parsed
                    .filter(item =>
                        item &&
                        ['user', 'model'].includes(item.role) &&
                        typeof item.content === 'string'
                    )
                    .slice(-40);
            }
        }
    } catch (error) {
        history = [];
    }

    renderHistory();

    function setOpen(open) {
        panel.hidden = !open;
        panel.setAttribute('aria-hidden', String(!open));
        toggle.setAttribute('aria-expanded', String(open));

        if (open) {
            input.focus();
            scrollToBottom();
        }
    }

    toggle.addEventListener('click', () => {
        setOpen(panel.hidden);
    });

    close.addEventListener('click', () => {
        setOpen(false);
    });

    input.addEventListener('keydown', event => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            form.requestSubmit();
        }
    });

    form.addEventListener('submit', async event => {
        event.preventDefault();

        const message = input.value.trim();

        if (!message || send.disabled) return;

        showMessage(message, 'user');

        input.value = '';
        send.disabled = true;
        input.disabled = true;

        const typing = showMessage('Estoy preparando una respuesta...');

        try {
            const response = await fetch('/api/chatbot/message', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    history: history.slice(-10)
                })
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(
                    data.detail ||
                    data.error ||
                    'No se pudo obtener una respuesta. Inténtalo de nuevo.'
                );
            }

            if (typeof data.reply !== 'string' || !data.reply.trim()) {
                throw new Error('El asistente no devolvió una respuesta válida.');
            }

            typing.remove();

            showMessage(data.reply, 'bot');

            history.push(
                { role: 'user', content: message },
                { role: 'model', content: data.reply }
            );

            // Mantener un historial temporal acotado.
            history = history.slice(-40);
            saveHistory();

        } catch (error) {
            typing.remove();

            showMessage(
                error.message || 'Ocurrió un error al consultar el asistente.',
                'bot',
                'error'
            );
        } finally {
            send.disabled = false;
            input.disabled = false;
            input.focus();
        }
    });
});
