class Chatbot {
    constructor() {
        this.isOpen = false;
        this.init();
    }

    init() {
        this.createElements();
        this.bindEvents();
        this.addWelcomeMessage();
    }

    createElements() {
        // 플로팅 버튼
        const floatingBtn = document.createElement('button');
        floatingBtn.className = 'chat-floating-btn';
        floatingBtn.innerHTML = `
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
            </svg>
        `;
        document.body.appendChild(floatingBtn);
        this.floatingBtn = floatingBtn;

        // 채팅 컨테이너
        const chatContainer = document.createElement('div');
        chatContainer.className = 'chat-container';
        chatContainer.innerHTML = `
            <div class="chat-header">
                <h3>🛍️ SonStar 쇼핑 도우미</h3>
                <button class="chat-close-btn">&times;</button>
            </div>
            <div class="chat-messages"></div>
            <div class="chat-input-area">
                <input type="text" class="chat-input" placeholder="무엇을 찾으시나요?">
                <button class="chat-send-btn">
                    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
                    </svg>
                </button>
            </div>
        `;
        document.body.appendChild(chatContainer);
        this.chatContainer = chatContainer;
        this.messagesArea = chatContainer.querySelector('.chat-messages');
        this.input = chatContainer.querySelector('.chat-input');
        this.sendBtn = chatContainer.querySelector('.chat-send-btn');
        this.closeBtn = chatContainer.querySelector('.chat-close-btn');
    }

    bindEvents() {
        this.floatingBtn.addEventListener('click', () => this.toggle());
        this.closeBtn.addEventListener('click', () => this.close());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    toggle() {
        this.isOpen ? this.close() : this.open();
    }

    open() {
        this.chatContainer.classList.add('active');
        this.isOpen = true;
        this.input.focus();
    }

    close() {
        this.chatContainer.classList.remove('active');
        this.isOpen = false;
    }

    addWelcomeMessage() {
        this.addMessage('bot', '안녕하세요! SonStar 쇼핑 도우미입니다 😊\n어떤 상품을 찾고 계신가요?');
    }

    addMessage(type, text, products = []) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        messageDiv.textContent = text;

        // 추천 상품이 있으면 카드로 표시
        if (products && products.length > 0) {
            const cardsDiv = document.createElement('div');
            cardsDiv.className = 'product-cards';

            products.forEach(product => {
                const card = document.createElement('div');
                card.className = 'product-card';
                card.innerHTML = `
                    <div class="name">${product.productName}</div>
                    <div class="category">${product.category}</div>
                `;
                card.addEventListener('click', () => {
                    window.location.href = `/item/${product.productId}`;
                });
                cardsDiv.appendChild(card);
            });

            messageDiv.appendChild(cardsDiv);
        }

        this.messagesArea.appendChild(messageDiv);
        this.scrollToBottom();
    }

    showTypingIndicator() {
        const typing = document.createElement('div');
        typing.className = 'typing-indicator';
        typing.id = 'typing-indicator';
        typing.innerHTML = '<span></span><span></span><span></span>';
        this.messagesArea.appendChild(typing);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        const typing = document.getElementById('typing-indicator');
        if (typing) typing.remove();
    }

    scrollToBottom() {
        this.messagesArea.scrollTop = this.messagesArea.scrollHeight;
    }

    async sendMessage() {
        const message = this.input.value.trim();
        if (!message) return;

        // 사용자 메시지 표시
        this.addMessage('user', message);
        this.input.value = '';

        // 로딩 표시
        this.showTypingIndicator();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            const data = await response.json();

            this.hideTypingIndicator();
            this.addMessage('bot', data.answer, data.relatedProducts);

        } catch (error) {
            console.error('Chat error:', error);
            this.hideTypingIndicator();
            this.addMessage('bot', '죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }
}

// 페이지 로드 시 챗봇 초기화
document.addEventListener('DOMContentLoaded', () => {
    new Chatbot();
});