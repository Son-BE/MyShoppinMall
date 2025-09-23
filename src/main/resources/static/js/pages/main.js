    // 사이드바 토글
    function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('open');
}

    // 최근 본 상품 로드 (기존 함수 유지)
    function loadRecentItems() {
    const recentItemsContainer = document.getElementById('recent-items-container');
    const recentItems = JSON.parse(localStorage.getItem('recentItems') || '[]');

    if (recentItems.length === 0) {
    recentItemsContainer.innerHTML = `
          <div style="text-align: center; padding: 1rem; color: var(--text-secondary); font-size: 0.875rem;">
            최근 본 상품이 없습니다
          </div>
        `;
    return;
}

    const displayItems = recentItems.slice(0, 5);
    recentItemsContainer.innerHTML = displayItems.map(item => `
        <a href="/items/detail/${item.id}" class="recent-item">
          <img src="${item.imagePath}" onerror="this.src='/images/default.png'" alt="${item.itemName}">
          <div class="recent-item-info">
            <h6>${item.itemName}</h6>
            <p>${item.price.toLocaleString()}원</p>
          </div>
        </a>
      `).join('');
}

    // 추천 시스템 상태 확인 (기존 함수 유지)
    function checkRecommendationHealth() {
    fetch('/api/recommendations/health')
        .then(res => res.json())
        .then(data => {
            const indicator = document.getElementById('recommendation-health-indicator');
            if (data.recommendation_service === true) {
                indicator.style.background = '#4CAF50';
                indicator.title = '추천 시스템 정상 작동 중';
            } else {
                indicator.style.background = '#FF9800';
                indicator.title = '추천 시스템 일시 중단 (기본 추천 사용 중)';
            }
        })
        .catch(error => {
            const indicator = document.getElementById('recommendation-health-indicator');
            indicator.style.background = '#F44336';
            indicator.title = '추천 시스템 연결 실패';
        });
}

    // AI 추천 요청 (기존 함수 유지)
    function requestAiRecommend() {
    const query = document.getElementById("ai-query").value;
    const recommendSection = document.getElementById("ai-recommend-list");

    if (!query.trim()) {
    alert('검색어를 입력해주세요.');
    return;
}

    recommendSection.style.display = 'block';
    recommendSection.innerHTML = `
        <div style="text-align: center; padding: 3rem;">
          <div class="loading">
            <i class="fa-solid fa-wand-magic-sparkles fa-spin" style="font-size: 3rem; color: var(--primary-color);"></i>
            <h3 style="margin-top: 1rem; color: var(--text-primary);">AI가 추천 상품을 찾고 있습니다...</h3>
            <p style="color: var(--text-secondary); margin-top: 0.5rem;">잠시만 기다려주세요</p>
          </div>
        </div>
      `;

    fetch(`/recommend/nlp?query=${encodeURIComponent(query)}`)
    .then(res => res.text())
    .then(html => {
    recommendSection.innerHTML = html;
    recommendSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
})
    .catch(error => {
    console.error('AI 추천 오류:', error);
    recommendSection.innerHTML = `
                  <div style="text-align: center; padding: 3rem; color: var(--text-secondary);">
                    <i class="fa-solid fa-exclamation-triangle" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                    <h3>AI 추천 서비스가 일시적으로 중단되었습니다</h3>
                    <p>잠시 후 다시 시도해주세요.</p>
                    <button onclick="this.parentElement.parentElement.style.display='none'"
                            style="margin-top: 1rem; padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer;">
                      닫기
                    </button>
                  </div>
                `;
});
}

    // Enter key support for AI search
    document.getElementById('ai-query')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
    requestAiRecommend();
}
});

    // Confirmation Functions (기존 함수 유지)
    function confirmAddToCart() {
    return confirm("장바구니에 상품을 추가하시겠습니까?");
}

    function confirmAddToWishlist() {
    return confirm("찜 목록에 상품을 추가하시겠습니까?");
}

    // 상품 상호작용 기록 함수 (기존 함수 유지)
    function recordInteraction(itemId, action) {
    const isAuthenticated = /*[[${#authorization.expression('isAuthenticated()')}]]*/ false;

    if (!isAuthenticated) {
    return;
}

    const userId = getUserIdFromSession();

    if (userId) {
    fetch(`/api/recommendations/interactions?userId=${userId}&itemId=${itemId}&action=${action}`, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
}
})
    .then(response => {
    if (response.ok) {
    console.log(`상호작용 기록됨: ${action} on item ${itemId}`);
}
})
    .catch(error => {
    console.warn('상호작용 기록 실패:', error);
});
}
}

    function getUserIdFromSession() {
    const userIdMeta = document.querySelector('meta[name="user-id"]');
    return userIdMeta ? userIdMeta.getAttribute('content') : null;
}

    // 페이지 로드 시 초기화
    document.addEventListener('DOMContentLoaded', function() {
    loadRecentItems();
    checkRecommendationHealth();

    // 30초마다 상태 확인
    setInterval(checkRecommendationHealth, 30000);

    // Add active class to current nav item
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === currentPath ||
    (currentPath === '/' && link.getAttribute('href') === '/')) {
    link.classList.add('active');
}
});

    // 상품 카드 클릭 시 view 이벤트 기록
    document.querySelectorAll('.card a, .product-card a').forEach(link => {
    link.addEventListener('click', function() {
    const href = this.getAttribute('href');
    const itemIdMatch = href.match(/\/items\/detail\/(\d+)/);
    if (itemIdMatch) {
    const itemId = itemIdMatch[1];
    recordInteraction(itemId, 'view');
}
});
});

    // 장바구니 버튼 클릭 시 cart 이벤트 기록
    document.querySelectorAll('form[action*="/user/cart/add"]').forEach(form => {
    form.addEventListener('submit', function(e) {
    const itemIdInput = this.querySelector('input[name="itemId"]');
    if (itemIdInput) {
    recordInteraction(itemIdInput.value, 'cart');
}
});
});

    // 찜하기 버튼 클릭 시 like 이벤트 기록
    document.querySelectorAll('form[action*="/wishList/add"]').forEach(form => {
    form.addEventListener('submit', function(e) {
    const itemIdInput = this.querySelector('input[name="itemId"]');
    if (itemIdInput) {
    recordInteraction(itemIdInput.value, 'like');
}
});
});
});

    // 반응형 사이드바 관리
    window.addEventListener('resize', function() {
    if (window.innerWidth > 1024) {
    document.getElementById('sidebar').classList.remove('open');
}
});

    // 사이드바 외부 클릭 시 닫기
    document.addEventListener('click', function(e) {
    const sidebar = document.getElementById('sidebar');
    const toggle = document.querySelector('.mobile-menu-toggle');

    if (window.innerWidth <= 1024 &&
    !sidebar.contains(e.target) &&
    !toggle.contains(e.target) &&
    sidebar.classList.contains('open')) {
    sidebar.classList.remove('open');
}
});
