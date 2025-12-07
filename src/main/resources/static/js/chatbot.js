/**
 * Hero Section Chatbot
 * AI 기반 상품 추천 챗봇
 */

class HeroChatbot {
    constructor() {
        this.messagesContainer = document.getElementById('hero-chat-messages');
        this.input = document.getElementById('hero-chat-input');
        this.sendButton = document.querySelector('.hero-chat-button');

        this.init();
    }

    init() {
        if (this.input) {
            this.input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.sendMessage();
            });
        }

        if (this.sendButton) {
            this.sendButton.addEventListener('click', () => this.sendMessage());
        }
    }

    async sendMessage() {
        const message = this.input.value.trim();
        if (!message) return;

        this.addMessage(message, 'user');
        this.input.value = '';
        this.setInputState(false);
        this.showTypingIndicator();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });

            const data = await response.json();
            this.hideTypingIndicator();

            // 봇 응답 추가
            this.addMessage(data.answer, 'bot');

            // 상품 카드가 있으면 별도로 추가
            if (data.relatedProducts && data.relatedProducts.length > 0) {
                this.addProductCards(data.relatedProducts);
            }

        } catch (error) {
            console.error('Chat error:', error);
            this.hideTypingIndicator();
            this.addMessage('죄송합니다. 잠시 후 다시 시도해주세요.', 'bot');
        }

        this.setInputState(true);
        this.input.focus();
    }

    addMessage(text, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `${type}-message`;

        if (type === 'bot') {
            messageDiv.innerHTML = `
                <i class="fa-solid fa-robot"></i>
                <span>${this.escapeHtml(text)}</span>
            `;
        } else {
            messageDiv.innerHTML = `<span>${this.escapeHtml(text)}</span>`;
        }

        this.messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    addProductCards(products) {
        const cardsWrapper = document.createElement('div');
        cardsWrapper.className = 'chat-product-cards-wrapper';

        const cards = products.slice(0, 5).map(p => {
            // ✅ API 응답의 imageUrl 사용 (S3 URL)
            const imageUrl = p.imageUrl || '/images/default.png';

            return `
                <a href="/items/detail/${p.productId}" class="chat-product-card">
                    <div class="chat-product-image">
                        <img src="${this.escapeHtml(imageUrl)}" 
                             alt="${this.escapeHtml(p.productName)}" 
                             onerror="this.src='/images/default.png'">
                    </div>
                    <div class="chat-product-info">
                        <p class="chat-product-name">${this.escapeHtml(p.productName)}</p>
                        <p class="chat-product-category">${this.formatCategory(p.category)}</p>
                        <span class="chat-product-link">상품 보기 →</span>
                    </div>
                </a>
            `;
        }).join('');

        cardsWrapper.innerHTML = `
            <div class="chat-product-cards-container">
                ${cards}
            </div>
        `;

        this.messagesContainer.appendChild(cardsWrapper);
        this.scrollToBottom();
    }

    formatCategory(category) {
        const categoryMap = {
            'MENS_TOP': '남성 상의',
            'WOMENS_TOP': '여성 상의',
            'MENS_BOTTOM': '남성 하의',
            'WOMENS_BOTTOM': '여성 하의',
            'MENS_SHOES': '남성 신발',
            'WOMENS_SHOES': '여성 신발',
            'MENS_OUTER': '남성 아우터',
            'WOMENS_OUTER': '여성 아우터',
            'MENS_ACCESSORY': '남성 악세서리',
            'WOMENS_ACCESSORY': '여성 악세서리',
            'ACCESSORIES': '악세서리'
        };
        return categoryMap[category] || category;
    }

    showTypingIndicator() {
        const indicator = document.createElement('div');
        indicator.className = 'bot-message';
        indicator.id = 'typing-indicator';
        indicator.innerHTML = `
            <i class="fa-solid fa-robot"></i>
            <div class="typing-indicator">
                <span></span><span></span><span></span>
            </div>
        `;
        this.messagesContainer.appendChild(indicator);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        const indicator = document.getElementById('typing-indicator');
        if (indicator) indicator.remove();
    }

    setInputState(enabled) {
        this.input.disabled = !enabled;
        this.sendButton.disabled = !enabled;
    }

    scrollToBottom() {
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// DOM 로드 후 초기화
document.addEventListener('DOMContentLoaded', () => {
    window.heroChatbot = new HeroChatbot();
});