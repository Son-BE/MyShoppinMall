/**
 * SonStarMall Main Page JavaScript
 * Handles interactions, recommendations, and UI functionality
 */

// Global configuration from server
const config = window.SonStarConfig || {};

// Utility functions
const utils = {
    // Format price with commas
    formatPrice: (price) => {
        return new Intl.NumberFormat('ko-KR').format(price);
    },

    // Debounce function for performance
    debounce: (func, wait) => {
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

    // Show notification
    showNotification: (message, type = 'info') => {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        notification.setAttribute('role', 'alert');

        document.body.appendChild(notification);

        // Auto remove after 3 seconds
        setTimeout(() => {
            notification.remove();
        }, 3000);
    },

    // Get user ID from configuration
    getUserId: () => {
        return config.isAuthenticated ? config.userId : null;
    }
};

// Scroll Progress Bar
const scrollProgress = {
    init: () => {
        const progressBar = document.getElementById('scrollProgress');
        if (!progressBar) return;

        const updateProgress = utils.debounce(() => {
            const scrollTop = window.pageYOffset;
            const docHeight = document.documentElement.scrollHeight - window.innerHeight;
            const scrollPercent = Math.min((scrollTop / docHeight) * 100, 100);
            progressBar.style.width = scrollPercent + '%';
            progressBar.setAttribute('aria-valuenow', scrollPercent);
        }, 10);

        window.addEventListener('scroll', updateProgress);
        updateProgress(); // Initial call
    }
};

// User Interaction Tracking
const interactionTracker = {
    // Record user interaction with items
    recordInteraction: async (itemId, action) => {
        if (!config.isAuthenticated || !itemId) return;

        try {
            const response = await fetch('/api/recommendations/interactions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    [config.csrfHeader]: config.csrfToken
                },
                body: JSON.stringify({
                    userId: config.userId,
                    itemId: itemId,
                    action: action
                })
            });

            if (response.ok) {
                console.log(`Interaction recorded: ${action} on item ${itemId}`);
            }
        } catch (error) {
            console.warn('Failed to record interaction:', error);
        }
    },

    // Initialize interaction tracking
    init: () => {
        // Track product views
        document.querySelectorAll('a[href*="/items/detail/"]').forEach(link => {
            link.addEventListener('click', function() {
                const href = this.getAttribute('href');
                const itemIdMatch = href.match(/\/items\/detail\/(\d+)/);
                if (itemIdMatch) {
                    interactionTracker.recordInteraction(itemIdMatch[1], 'view');
                }
            });
        });

        // Track cart additions
        document.querySelectorAll('form[action*="/user/cart/add"]').forEach(form => {
            form.addEventListener('submit', function() {
                const itemIdInput = this.querySelector('input[name="itemId"]');
                if (itemIdInput) {
                    interactionTracker.recordInteraction(itemIdInput.value, 'cart');
                }
            });
        });

        // Track wishlist additions
        document.querySelectorAll('form[action*="/wishList/add"]').forEach(form => {
            form.addEventListener('submit', function() {
                const itemIdInput = this.querySelector('input[name="itemId"]');
                if (itemIdInput) {
                    interactionTracker.recordInteraction(itemIdInput.value, 'like');
                }
            });
        });
    }
};

// AI Recommendation System
const aiRecommendation = {
    // Request AI recommendations
    requestAiRecommend: async () => {
        const queryInput = document.getElementById('ai-query');
        const recommendSection = document.getElementById('ai-recommend-list');

        if (!queryInput || !recommendSection) return;

        const query = queryInput.value.trim();

        if (!query) {
            utils.showNotification('검색어를 입력해주세요.', 'warning');
            queryInput.focus();
            return;
        }

        // Show loading state
        recommendSection.style.display = 'block';
        recommendSection.innerHTML = aiRecommendation.getLoadingHTML();
        recommendSection.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(`/recommend/nlp?query=${encodeURIComponent(query)}`);

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const html = await response.text();
            recommendSection.innerHTML = html;
            recommendSection.setAttribute('aria-busy', 'false');

            // Scroll to results
            recommendSection.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });

            utils.showNotification('AI 추천 결과를 불러왔습니다.', 'success');

        } catch (error) {
            console.error('AI recommendation error:', error);
            recommendSection.innerHTML = aiRecommendation.getErrorHTML();
            recommendSection.setAttribute('aria-busy', 'false');
            utils.showNotification('AI 추천 서비스가 일시적으로 중단되었습니다.', 'error');
        }
    },

    // Get loading HTML
    getLoadingHTML: () => {
        return `
            <div class="ai-loading" role="status" aria-live="polite">
                <div class="loading-content">
                    <i class="fa-solid fa-wand-magic-sparkles fa-spin" aria-hidden="true"></i>
                    <h3>AI가 추천 상품을 찾고 있습니다...</h3>
                    <p>잠시만 기다려주세요</p>
                </div>
            </div>
        `;
    },

    // Get error HTML
    getErrorHTML: () => {
        return `
            <div class="ai-error" role="alert">
                <div class="error-content">
                    <i class="fa-solid fa-exclamation-triangle" aria-hidden="true"></i>
                    <h3>AI 추천 서비스가 일시적으로 중단되었습니다</h3>
                    <p>잠시 후 다시 시도해주세요.</p>
                    <button onclick="this.closest('#ai-recommend-list').style.display='none'" 
                            class="close-button">
                        닫기
                    </button>
                </div>
            </div>
        `;
    },

    // Initialize AI search functionality
    init: () => {
        const queryInput = document.getElementById('ai-query');
        const searchButton = document.querySelector('.ai-search-button');

        // Enter key support
        if (queryInput) {
            queryInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    aiRecommendation.requestAiRecommend();
                }
            });
        }

        // Button click
        if (searchButton) {
            searchButton.addEventListener('click', aiRecommendation.requestAiRecommend);
        }

        // Expose to global scope for backward compatibility
        window.requestAiRecommend = aiRecommendation.requestAiRecommend;
    }
};

// Recommendation System Health Monitor
const healthMonitor = {
    checkHealth: async () => {
        const indicator = document.getElementById('recommendation-health-indicator');
        if (!indicator) return;

        try {
            const response = await fetch('/api/recommendations/health');
            const data = await response.json();

            if (data.recommendation_service === true) {
                indicator.className = 'status-indicator status-healthy';
                indicator.title = '추천 시스템 정상 작동 중';
                indicator.setAttribute('aria-label', '추천 시스템 정상 작동 중');
            } else {
                indicator.className = 'status-indicator status-warning';
                indicator.title = '추천 시스템 일시 중단 (기본 추천 사용 중)';
                indicator.setAttribute('aria-label', '추천 시스템 일시 중단');
            }
        } catch (error) {
            const indicator = document.getElementById('recommendation-health-indicator');
            if (indicator) {
                indicator.className = 'status-indicator status-error';
                indicator.title = '추천 시스템 연결 실패';
                indicator.setAttribute('aria-label', '추천 시스템 연결 실패');
            }
        }
    },

    init: () => {
        // Check on load
        healthMonitor.checkHealth();

        // Check every 30 seconds
        setInterval(healthMonitor.checkHealth, 30000);
    }
};

// Recent Items Management
const recentItems = {
    // Load recent items from localStorage
    loadRecentItems: () => {
        const container = document.getElementById('recent-items-container');
        if (!container) return;

        const recentItems = JSON.parse(localStorage.getItem('recentItems') || '[]');

        if (recentItems.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <p>최근 본 상품이 없습니다</p>
                </div>
            `;
            return;
        }

        const displayItems = recentItems.slice(0, 5);
        container.innerHTML = displayItems.map(item => `
            <article class="recent-item" role="listitem">
                <a href="/items/detail/${item.id}" aria-label="${item.itemName} 상품 상세보기">
                    <img src="${item.imagePath}" 
                         alt="${item.itemName}" 
                         loading="lazy"
                         onerror="this.src='/images/default.png'">
                    <div class="recent-item-info">
                        <h5>${item.itemName}</h5>
                        <p>${utils.formatPrice(item.price)}원</p>
                    </div>
                </a>
            </article>
        `).join('');
    },

    // Add item to recent items
    addRecentItem: (item) => {
        const recentItems = JSON.parse(localStorage.getItem('recentItems') || '[]');

        // Remove if already exists
        const filteredItems = recentItems.filter(recentItem => recentItem.id !== item.id);

        // Add to beginning
        filteredItems.unshift(item);

        // Keep only last 10 items
        const limitedItems = filteredItems.slice(0, 10);

        localStorage.setItem('recentItems', JSON.stringify(limitedItems));
        recentItems.loadRecentItems();
    },

    init: () => {
        recentItems.loadRecentItems();
    }
};

// Floating Recommendations Panel
const floatingRecommendations = {
    init: () => {
        const toggleButton = document.getElementById('toggle-recommend');
        const panel = document.getElementById('recommend-products');

        if (!toggleButton || !panel) return;

        toggleButton.addEventListener('click', function() {
            const isExpanded = this.getAttribute('aria-expanded') === 'true';

            if (!isExpanded) {
                panel.style.display = 'block';
                this.innerHTML = '<i class="fa-solid fa-times" aria-hidden="true"></i> 추천 닫기';
                this.setAttribute('aria-expanded', 'true');
                panel.style.animation = 'slideInLeft 0.3s ease';
            } else {
                panel.style.animation = 'slideOutLeft 0.3s ease';
                setTimeout(() => {
                    panel.style.display = 'none';
                    this.innerHTML = '<i class="fa-solid fa-sparkles" aria-hidden="true"></i> 추천 상품';
                    this.setAttribute('aria-expanded', 'false');
                }, 300);
            }
        });
    }
};

// Navigation Enhancement
const navigation = {
    init: () => {
        // Add active class to current nav item
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-link');

        navLinks.forEach(link => {
            const href = link.getAttribute('href');
            if (href === currentPath || (currentPath === '/' && href === '/')) {
                link.classList.add('active');
                link.setAttribute('aria-current', 'page');
            }
        });
    }
};

// Form Confirmation Functions
const formConfirmations = {
    confirmAddToCart: () => {
        return confirm("장바구니에 상품을 추가하시겠습니까?");
    },

    confirmAddToWishlist: () => {
        return confirm("찜 목록에 상품을 추가하시겠습니까?");
    },

    init: () => {
        // Expose to global scope for backward compatibility
        window.confirmAddToCart = formConfirmations.confirmAddToCart;
        window.confirmAddToWishlist = formConfirmations.confirmAddToWishlist;
    }
};

// Hero Section Animations
const heroAnimations = {
    init: () => {
        const heroElements = document.querySelectorAll('.hero-content > *');

        heroElements.forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';

            setTimeout(() => {
                el.style.transition = 'all 0.6s ease';
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, index * 200);
        });
    }
};

// Smooth Scrolling for Anchor Links
const smoothScrolling = {
    init: () => {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));

                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }
};

// Initialize all modules when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all modules
    scrollProgress.init();
    interactionTracker.init();
    aiRecommendation.init();
    healthMonitor.init();
    recentItems.init();
    floatingRecommendations.init();
    navigation.init();
    formConfirmations.init();
    heroAnimations.init();
    smoothScrolling.init();

    console.log('SonStarMall main page initialized');
});

// Handle page visibility change for health monitoring
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        // Check health when page becomes visible
        healthMonitor.checkHealth();
    }
});

// Export for testing or external use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        utils,
        scrollProgress,
        interactionTracker,
        aiRecommendation,
        healthMonitor,
        recentItems,
        floatingRecommendations,
        navigation,
        formConfirmations,
        heroAnimations,
        smoothScrolling
    };
}