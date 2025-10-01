/**
 * Main Application Module
 * SonStarMall 메인 페이지 기능 관리
 */

// ==================== Constants ====================
const CONFIG = {
    RECENT_ITEMS_LIMIT: 5,
    HEALTH_CHECK_INTERVAL: 30000, // 30초
    INTERACTION_DEBOUNCE: 300,
    LOCAL_STORAGE_KEY: 'recentItems'
};

const SELECTORS = {
    healthIndicator: '#recommendation-health-indicator',
    aiQuery: '#ai-query',
    aiRecommendList: '#ai-recommend-list',
    cardLinks: '.product-card a',
    cartForms: 'form[action*="/user/cart/add"]',
    wishlistForms: 'form[action*="/wishList/add"]'
};

// ==================== Utility Functions ====================
const Utils = {
    /**
     * 로컬 스토리지에서 안전하게 데이터 가져오기
     */
    getLocalStorage(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.warn(`localStorage 읽기 실패 (${key}):`, error);
            return defaultValue;
        }
    },

    /**
     * 로컬 스토리지에 안전하게 데이터 저장
     */
    setLocalStorage(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
            return true;
        } catch (error) {
            console.warn(`localStorage 저장 실패 (${key}):`, error);
            return false;
        }
    },

    /**
     * 디바운스 함수
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * 숫자 포맷팅 (천단위 구분)
     */
    formatNumber(num) {
        return new Intl.NumberFormat('ko-KR').format(num);
    },

    /**
     * 요소가 존재하는지 확인
     */
    elementExists(selector) {
        return document.querySelector(selector) !== null;
    },

    /**
     * fetch 래퍼 with 에러 핸들링
     */
    async fetchWithErrorHandling(url, options = {}) {
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    ...options.headers
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return response;
        } catch (error) {
            console.error('Fetch 오류:', error);
            throw error;
        }
    }
};

// Sidebar removed - Filter now integrated in collection section

// ==================== Recent Items Module (Removed) ====================
// Recent items functionality removed - no longer needed without sidebar

// ==================== Recommendation Health Module ====================
const RecommendationHealth = {
    indicator: null,
    intervalId: null,

    init() {
        this.indicator = document.querySelector(SELECTORS.healthIndicator);
        if (!this.indicator) return;

        this.check();
        this.startPeriodicCheck();
    },

    async check() {
        try {
            const response = await Utils.fetchWithErrorHandling('/api/recommendations/health');
            const data = await response.json();
            this.updateIndicator(data.recommendation_service);
        } catch (error) {
            console.error('추천 시스템 상태 확인 실패:', error);
            this.updateIndicator('error');
        }
    },

    updateIndicator(status) {
        if (!this.indicator) return;

        const statusMap = {
            true: {
                color: '#4CAF50',
                title: '추천 시스템 정상 작동 중'
            },
            false: {
                color: '#FF9800',
                title: '추천 시스템 일시 중단 (기본 추천 사용 중)'
            },
            error: {
                color: '#F44336',
                title: '추천 시스템 연결 실패'
            }
        };

        const config = statusMap[status] || statusMap.error;
        this.indicator.style.background = config.color;
        this.indicator.title = config.title;
    },

    startPeriodicCheck() {
        this.intervalId = setInterval(() => {
            this.check();
        }, CONFIG.HEALTH_CHECK_INTERVAL);
    },

    stop() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }
};

// ==================== AI Recommendation Module ====================
const AIRecommendation = {
    queryInput: null,
    resultContainer: null,

    init() {
        this.queryInput = document.querySelector(SELECTORS.aiQuery);
        this.resultContainer = document.querySelector(SELECTORS.aiRecommendList);

        if (!this.queryInput || !this.resultContainer) return;

        this.setupEventListeners();
    },

    setupEventListeners() {
        // Enter 키 지원
        this.queryInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.request();
            }
        });
    },

    async request() {
        const query = this.queryInput.value.trim();

        if (!query) {
            alert('검색어를 입력해주세요.');
            return;
        }

        this.showLoading();

        try {
            const response = await fetch(`/recommend/nlp?query=${encodeURIComponent(query)}`);
            const html = await response.text();

            this.resultContainer.innerHTML = html;
            this.resultContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (error) {
            console.error('AI 추천 오류:', error);
            this.showError();
        }
    },

    showLoading() {
        this.resultContainer.style.display = 'block';
        this.resultContainer.innerHTML = `
            <div style="text-align: center; padding: 3rem;">
                <div class="loading">
                    <i class="fa-solid fa-wand-magic-sparkles fa-spin" 
                       style="font-size: 3rem; color: var(--color-primary);"></i>
                    <h3 style="margin-top: 1rem; color: var(--color-text-primary);">
                        AI가 추천 상품을 찾고 있습니다...
                    </h3>
                    <p style="color: var(--color-text-secondary); margin-top: 0.5rem;">
                        잠시만 기다려주세요
                    </p>
                </div>
            </div>
        `;
    },

    showError() {
        this.resultContainer.innerHTML = `
            <div style="text-align: center; padding: 3rem; color: var(--color-text-secondary);">
                <i class="fa-solid fa-exclamation-triangle" 
                   style="font-size: 3rem; margin-bottom: 1rem;"></i>
                <h3>AI 추천 서비스가 일시적으로 중단되었습니다</h3>
                <p>잠시 후 다시 시도해주세요.</p>
                <button onclick="document.getElementById('ai-recommend-list').style.display='none'"
                        style="margin-top: 1rem; padding: 0.5rem 1rem; border: none; 
                               border-radius: 4px; cursor: pointer; background: var(--color-primary);
                               color: white;">
                    닫기
                </button>
            </div>
        `;
    }
};

// ==================== User Interaction Module ====================
const UserInteraction = {
    userId: null,
    isAuthenticated: false,

    init() {
        this.userId = this.getUserId();
        this.isAuthenticated = this.checkAuth();

        if (!this.isAuthenticated) return;

        this.setupEventListeners();
    },

    setupEventListeners() {
        // 상품 카드 클릭 (view)
        document.querySelectorAll(SELECTORS.cardLinks).forEach(link => {
            link.addEventListener('click', (e) => {
                this.handleViewInteraction(link);
            });
        });

        // 장바구니 추가 (cart)
        document.querySelectorAll(SELECTORS.cartForms).forEach(form => {
            form.addEventListener('submit', (e) => {
                this.handleCartInteraction(form);
            });
        });

        // 찜하기 (like)
        document.querySelectorAll(SELECTORS.wishlistForms).forEach(form => {
            form.addEventListener('submit', (e) => {
                this.handleWishlistInteraction(form);
            });
        });
    },

    handleViewInteraction(link) {
        const href = link.getAttribute('href');
        const itemIdMatch = href?.match(/\/items\/detail\/(\d+)/);

        if (itemIdMatch) {
            const itemId = itemIdMatch[1];
            this.recordInteraction(itemId, 'view');
        }
    },

    handleCartInteraction(form) {
        const itemIdInput = form.querySelector('input[name="itemId"]');
        if (itemIdInput) {
            this.recordInteraction(itemIdInput.value, 'cart');
        }
    },

    handleWishlistInteraction(form) {
        const itemIdInput = form.querySelector('input[name="itemId"]');
        if (itemIdInput) {
            this.recordInteraction(itemIdInput.value, 'like');
        }
    },

    async recordInteraction(itemId, action) {
        if (!this.userId || !itemId) return;

        try {
            const url = `/api/recommendations/interactions?userId=${this.userId}&itemId=${itemId}&action=${action}`;
            const response = await Utils.fetchWithErrorHandling(url, { method: 'POST' });

            if (response.ok) {
                console.log(`상호작용 기록됨: ${action} on item ${itemId}`);
            }
        } catch (error) {
            console.warn('상호작용 기록 실패:', error);
        }
    },

    getUserId() {
        const userIdMeta = document.querySelector('meta[name="user-id"]');
        return userIdMeta ? userIdMeta.getAttribute('content') : null;
    },

    checkAuth() {
        // Thymeleaf에서 주입된 값 확인
        return document.body.hasAttribute('data-authenticated');
    }
};

// ==================== Navigation Module (Removed) ====================
// Navigation highlighting removed - not needed in new layout

// Global functions for HTML onclick handlers
window.confirmAddToCart = function() {
    return confirm("장바구니에 상품을 추가하시겠습니까?");
};

window.confirmAddToWishlist = function() {
    return confirm("찜 목록에 상품을 추가하시겠습니까?");
};

window.requestAiRecommend = function() {
    AIRecommendation.request();
};

// ==================== Application Initialization ====================
const App = {
    init() {
        // DOM이 로드되면 모든 모듈 초기화
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.initModules());
        } else {
            this.initModules();
        }
    },

    initModules() {
        console.log('SonStarMall 초기화 중...');

        // 모듈 초기화 (사이드바 관련 제거됨)
        RecommendationHealth.init();
        AIRecommendation.init();
        UserInteraction.init();

        console.log('SonStarMall 초기화 완료');
    },

    cleanup() {
        // 페이지 언로드 시 정리
        RecommendationHealth.stop();
    }
};

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', () => {
    App.cleanup();
});

// 앱 시작
App.init();