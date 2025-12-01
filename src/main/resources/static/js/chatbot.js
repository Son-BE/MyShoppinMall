class Chatbot {
    constructor() {
        this.isOpen = false;
        this.tooltipDismissed = false;
        this.init();
    }

    init() {
        this.createElements();
        this.bindEvents();
        this.showTooltipAfterDelay();
    }

    createElements() {
        // 플로팅 컨테이너
        const floatingContainer = document.createElement('div');
        floatingContainer.className = 'chat-floating-container';

        // 안내 말풍선
        const tooltip = document.createElement('div');
        tooltip.className = 'chat-tooltip';
        tooltip.id = 'chat-tooltip';
        tooltip.style.display = 'none';
        tooltip.innerHTML = `
            <button class="chat-tooltip-close">&times;</button>
            <div class="chat-tooltip-title">💬 도움이 필요하신가요?</div>
            <div>AI 쇼핑 도우미가 상품 추천을 도와드려요!</div>
        `;
        floatingContainer.appendChild(tooltip);

        // 플로팅 버튼
        const floatingBtn = document.createElement('button');
        floatingBtn.className = 'chat-floating-btn';
        floatingBtn.innerHTML = `
            <span class="chat-badge">1</span>
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                <circle cx="12" cy="10" r="1.5"/>
                <circle cx="8" cy="10" r="1.5"/>
                <circle cx="16" cy="10" r="1.5"/>
            </svg>
        `;
        floatingContainer.appendChild(floatingBtn);

        document.body.appendChild(floatingContainer);
        this.floatingBtn = floatingBtn;
        this.tooltip = tooltip;
        this.badge = floatingBtn.querySelector('.chat-badge');

        // 채팅 컨테이너
        const chatContainer = document.createElement('div');
        chatContainer.className = 'chat-container';
        chatContainer.innerHTML = `
            <div class="chat-header">
                <div class="chat-header-avatar">🛍️</div>
                <div class="chat-header-info">
                    <div class="chat-header-title">SonStar 쇼핑 도우미</div>
                    <div class="chat-header-status">온라인 · AI가 답변합니다</div>
                </div>
                <button class="chat-close-btn">&times;</button>
            </div>
            <div class="chat-quick-actions">
                <button class="quick-action-btn">👕 오늘의 추천</button>
                <button class="quick-action-btn">🔥 인기 상품</button>
                <button class="quick-action-btn">💰 세일 상품</button>
                <button class="quick-action-btn">❓ 사이즈 문의</button>
            </div>
            <div class="chat-messages"></div>
            <div class="chat-input-area">
                <input type="text" class="chat-input" placeholder="무엇을 찾고 계신가요?">
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
        this.quickActions = chatContainer.querySelectorAll('.quick-action-btn');
    }

    bindEvents() {
        this.floatingBtn.addEventListener('click', () => this.toggle());
        this.closeBtn.addEventListener('click', () => this.close());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });

        // 툴팁 닫기
        this.tooltip.querySelector('.chat-tooltip-close').addEventListener('click', (e) => {
            e.stopPropagation();
            this.hideTooltip();
            this.tooltipDismissed = true;
        });

        // 퀵 액션 버튼
        this.quickActions.forEach(btn => {
            btn.addEventListener('click', () => {
                const text = btn.textContent.trim();
                this.input.value = text;
                this.sendMessage();
            });
        });
    }

    showTooltipAfterDelay() {
        setTimeout(() => {
            if (!this.isOpen && !this.tooltipDismissed) {
                this.tooltip.style.display = 'block';
            }
        }, 3000);
    }

    hideTooltip() {
        this.tooltip.style.display = 'none';
    }

    toggle() {
        this.isOpen ? this.close() : this.open();
    }

    open() {
        this.chatContainer.classList.add('active');
        this.isOpen = true;
        this.hideTooltip();
        this.badge.style.display = 'none';
        this.input.focus();

        // 첫 방문시 웰컴 메시지
        if (this.messagesArea.children.length === 0) {
            this.addWelcomeMessage();
        }
    }

    close() {
        this.chatContainer.classList.remove('active');
        this.isOpen = false;
    }

    addWelcomeMessage() {
        this.addMessage('bot', '안녕하세요! 👋\n\nSonStar 쇼핑 도우미예요.\n어떤 상품을 찾고 계신가요? 취향에 맞는 상품을 추천해 드릴게요!');
    }

    addMessage(type, text, products = []) {
        const messageWrapper = document.createElement('div');

        if (type === 'bot') {
            messageWrapper.className = 'bot-message-wrapper';
            messageWrapper.innerHTML = `
                <div class="bot-avatar">🤖</div>
                <div class="message bot">
                    <div class="message-content">${text.replace(/\n/g, '<br>')}</div>
                    <div class="message-time">${this.getCurrentTime()}</div>
                </div>
            `;

            // 추천 상품 카드
            if (products && products.length > 0) {
                const messageDiv = messageWrapper.querySelector('.message');
                const cardsContainer = document.createElement('div');
                cardsContainer.className = 'product-cards-container';
                cardsContainer.innerHTML = `<div class="cards-title">🛍️ 추천 상품</div>`;

                const cardsDiv = document.createElement('div');
                cardsDiv.className = 'product-cards';

                products.forEach(product => {
                    const card = document.createElement('div');
                    card.className = 'product-card';
                    card.innerHTML = `
                        <div class="product-image">👕</div>
                        <div class="product-info">
                            <div class="name" title="${product.productName}">${product.productName}</div>
                            <div class="category">${product.category}</div>
                            <div class="view-btn">상품 보기 →</div>
                        </div>
                    `;
                    card.addEventListener('click', () => {
                        window.location.href = `/items/${product.productId}`;
                    });
                    cardsDiv.appendChild(card);
                });

                cardsContainer.appendChild(cardsDiv);
                messageDiv.appendChild(cardsContainer);
            }
        } else {
            messageWrapper.className = 'message user';
            messageWrapper.innerHTML = `
                <div class="message-content">${text}</div>
                <div class="message-time">${this.getCurrentTime()}</div>
            `;
        }

        this.messagesArea.appendChild(messageWrapper);
        this.scrollToBottom();
    }

    getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    }

    showTypingIndicator() {
        const wrapper = document.createElement('div');
        wrapper.className = 'bot-message-wrapper';
        wrapper.id = 'typing-indicator';
        wrapper.innerHTML = `
            <div class="bot-avatar">🤖</div>
            <div class="typing-indicator">
                <span></span><span></span><span></span>
            </div>
        `;
        this.messagesArea.appendChild(wrapper);
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

        this.addMessage('user', message);
        this.input.value = '';
        this.showTypingIndicator();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: message })
            });

            const data = await response.json();
            this.hideTypingIndicator();
            this.addMessage('bot', data.answer, data.relatedProducts);

        } catch (error) {
            console.error('Chat error:', error);
            this.hideTypingIndicator();
            this.addMessage('bot', '죄송합니다. 일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요! 🙏');
        }
    }
}

// 페이지 로드 시 챗봇 초기화
document.addEventListener('DOMContentLoaded', () => {
    new Chatbot();
});