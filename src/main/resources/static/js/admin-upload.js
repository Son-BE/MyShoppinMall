document.addEventListener('DOMContentLoaded', () => {
    initUpload();
});

// 전역 변수
let selectedFile = null;
let classificationResult = null;

// 서브카테고리 데이터
const subCategories = {
    MENS_TOP: [
        { value: 'M_TSHIRT', label: '티셔츠' },
        { value: 'M_SHIRT', label: '셔츠' },
        { value: 'M_HOODIE', label: '후드티' },
        { value: 'M_SWEATSHIRT', label: '맨투맨' }
    ],
    MENS_BOTTOM: [
        { value: 'M_JEANS', label: '청바지' },
        { value: 'M_SLACKS', label: '슬랙스' },
        { value: 'M_JOGGER_PANTS', label: '조거팬츠' },
        { value: 'M_TRAINING_PANTS', label: '츄리닝' },
        { value: 'M_SHORTS', label: '반바지' }
    ],
    MENS_OUTER: [
        { value: 'M_JACKET', label: '자켓' },
        { value: 'M_COAT', label: '코트' },
        { value: 'M_PADDING', label: '패딩' },
        { value: 'M_WINDBREAKER', label: '바람막이' }
    ],
    MENS_SHOES: [
        { value: 'M_SNEAKERS', label: '스니커즈' },
        { value: 'M_RUNNING_SHOES', label: '운동화' },
        { value: 'M_BOOTS', label: '부츠/구두' }
    ],
    MENS_ACCESSORY: [
        { value: 'M_WATCH', label: '시계' },
        { value: 'M_RING', label: '반지' },
        { value: 'M_NECKLACE', label: '목걸이' }
    ],
    WOMENS_TOP: [
        { value: 'W_TSHIRT', label: '티셔츠' },
        { value: 'W_BLOUSE', label: '블라우스' },
        { value: 'W_HOODIE', label: '후드티' },
        { value: 'W_SWEATSHIRT', label: '맨투맨' }
    ],
    WOMENS_BOTTOM: [
        { value: 'W_JEANS', label: '청바지' },
        { value: 'W_SLACKS', label: '슬랙스' },
        { value: 'W_SKIRT', label: '치마' },
        { value: 'W_JOGGER_PANTS', label: '조거팬츠' },
        { value: 'W_SHORTS', label: '반바지' }
    ],
    WOMENS_OUTER: [
        { value: 'W_JACKET', label: '자켓' },
        { value: 'W_COAT', label: '코트' },
        { value: 'W_PADDING', label: '패딩' },
        { value: 'W_WINDBREAKER', label: '바람막이' }
    ],
    WOMENS_SHOES: [
        { value: 'W_SNEAKERS', label: '스니커즈' },
        { value: 'W_RUNNING_SHOES', label: '운동화' },
        { value: 'W_BOOTS', label: '부츠/구두' }
    ],
    WOMENS_ACCESSORY: [
        { value: 'W_WATCH', label: '시계' },
        { value: 'W_RING', label: '반지' },
        { value: 'W_NECKLACE', label: '목걸이' }
    ]
};

/**
 * 초기화
 */
function initUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const imageInput = document.getElementById('imageInput');
    const analyzeBtn = document.getElementById('analyzeBtn');
    const removeBtn = document.getElementById('removeImage');
    const cancelBtn = document.getElementById('cancelBtn');
    const productForm = document.getElementById('productForm');
    const categorySelect = document.getElementById('category');

    // 클릭 업로드
    uploadArea.addEventListener('click', () => imageInput.click());

    // 파일 선택
    imageInput.addEventListener('change', handleFileSelect);

    // 드래그 앤 드롭
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('drag-over');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('drag-over');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('drag-over');
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFile(files[0]);
        }
    });

    // 이미지 제거
    removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        resetUpload();
    });

    // AI 분석
    analyzeBtn.addEventListener('click', analyzeImage);

    // 취소
    cancelBtn.addEventListener('click', () => {
        if (classificationResult && classificationResult.imagePath) {
            deleteImage(classificationResult.imagePath);
        }
        resetAll();
    });

    // 카테고리 변경 시 서브카테고리 업데이트
    categorySelect.addEventListener('change', updateSubCategories);

    // 폼 제출
    productForm.addEventListener('submit', handleSubmit);
}

/**
 * 파일 선택 처리
 */
function handleFileSelect(e) {
    const files = e.target.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
}

/**
 * 파일 처리
 */
function handleFile(file) {
    // 파일 타입 검증
    if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 업로드 가능합니다.');
        return;
    }

    // 파일 크기 검증 (10MB)
    if (file.size > 10 * 1024 * 1024) {
        alert('파일 크기는 10MB 이하여야 합니다.');
        return;
    }

    selectedFile = file;

    // 미리보기 표시
    const reader = new FileReader();
    reader.onload = (e) => {
        document.getElementById('previewImage').src = e.target.result;
        document.getElementById('uploadPlaceholder').style.display = 'none';
        document.getElementById('uploadPreview').style.display = 'block';
        document.getElementById('analyzeBtn').disabled = false;
    };
    reader.readAsDataURL(file);
}

/**
 * AI 분석 요청
 */
async function analyzeImage() {
    if (!selectedFile) return;

    showLoading('AI가 이미지를 분석하고 있습니다...');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        const response = await fetch('/api/admin/products/classify', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('분류 요청 실패');
        }

        classificationResult = await response.json();
        console.log('분류 결과:', classificationResult);

        // 폼에 결과 반영
        fillForm(classificationResult);

        // 결과 섹션 표시
        document.getElementById('resultSection').style.display = 'block';

        // 신뢰도 배지 업데이트
        updateConfidenceBadge(classificationResult.analysisSuccess);

    } catch (error) {
        console.error('분석 오류:', error);
        alert('이미지 분석에 실패했습니다. 다시 시도해주세요.');
    } finally {
        hideLoading();
    }
}

/**
 * 폼에 분류 결과 반영
 */
function fillForm(result) {
    document.getElementById('itemName').value = result.itemName || '';
    document.getElementById('itemComment').value = result.itemComment || '';
    document.getElementById('price').value = result.suggestedPrice || 30000;
    document.getElementById('gender').value = mapGender(result.gender);
    document.getElementById('category').value = result.category || 'MENS_TOP';
    document.getElementById('season').value = mapSeason(result.season);
    document.getElementById('style').value = result.style || 'CASUAL';
    document.getElementById('ageGroup').value = result.ageGroup || 'ADULT';
    document.getElementById('primaryColor').value = result.primaryColor || '';
    document.getElementById('secondaryColor').value = result.secondaryColor || '';
    document.getElementById('imagePath').value = result.imagePath || '';

    // 서브카테고리 업데이트
    updateSubCategories();

    // 서브카테고리 값 설정 (약간의 딜레이 후)
    setTimeout(() => {
        const subCatValue = mapSubCategory(result.subCategory, result.gender);
        document.getElementById('subCategory').value = subCatValue;
    }, 100);
}

/**
 * 성별 매핑
 */
function mapGender(gender) {
    if (!gender) return 'UNISEX';
    const upper = gender.toUpperCase();
    if (upper.includes('MALE') || upper.includes('MEN')) return 'MALE';
    if (upper.includes('FEMALE') || upper.includes('WOMEN')) return 'FEMALE';
    return 'UNISEX';
}

/**
 * 시즌 매핑
 */
function mapSeason(season) {
    if (!season) return 'ALL_SEASON';
    const upper = season.toUpperCase();
    if (upper === 'FALL') return 'AUTUMN';
    return upper;
}

/**
 * 서브카테고리 매핑
 */
function mapSubCategory(subCategory, gender) {
    if (!subCategory) return '';

    const isFemale = gender && (gender.toUpperCase().includes('FEMALE') || gender.toUpperCase().includes('WOMEN'));
    const prefix = isFemale ? 'W_' : 'M_';

    const lower = subCategory.toLowerCase();

    if (lower.includes('tshirt') || lower.includes('shirt')) return prefix + 'TSHIRT';
    if (lower.includes('hoodie')) return prefix + 'HOODIE';
    if (lower.includes('sweatshirt')) return prefix + 'SWEATSHIRT';
    if (lower.includes('jeans')) return prefix + 'JEANS';
    if (lower.includes('jogger')) return prefix + 'JOGGER_PANTS';
    if (lower.includes('training')) return prefix + 'TRAINING_PANTS';
    if (lower.includes('coat')) return prefix + 'COAT';
    if (lower.includes('padding')) return prefix + 'PADDING';
    if (lower.includes('sneaker')) return prefix + 'SNEAKERS';

    return '';
}

/**
 * 서브카테고리 업데이트
 */
function updateSubCategories() {
    const category = document.getElementById('category').value;
    const subCategorySelect = document.getElementById('subCategory');

    subCategorySelect.innerHTML = '<option value="">선택 안함</option>';

    const items = subCategories[category];
    if (items) {
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item.value;
            option.textContent = item.label;
            subCategorySelect.appendChild(option);
        });
    }
}

/**
 * 신뢰도 배지 업데이트
 */
function updateConfidenceBadge(success) {
    const badge = document.getElementById('confidenceBadge');
    const text = document.getElementById('confidenceText');

    if (success) {
        badge.style.background = 'rgba(16, 185, 129, 0.1)';
        badge.style.color = '#10b981';
        text.textContent = 'AI 분석 완료';
    } else {
        badge.style.background = 'rgba(245, 158, 11, 0.1)';
        badge.style.color = '#f59e0b';
        text.textContent = '수동 입력 필요';
    }
}

/**
 * 폼 제출
 */
async function handleSubmit(e) {
    e.preventDefault();

    const formData = {
        itemName: document.getElementById('itemName').value,
        itemComment: document.getElementById('itemComment').value,
        price: parseInt(document.getElementById('price').value),
        quantity: parseInt(document.getElementById('quantity').value) || 100,
        gender: document.getElementById('gender').value,
        category: document.getElementById('category').value,
        subCategory: document.getElementById('subCategory').value,
        season: document.getElementById('season').value,
        style: document.getElementById('style').value,
        ageGroup: document.getElementById('ageGroup').value,
        primaryColor: document.getElementById('primaryColor').value || null,
        secondaryColor: document.getElementById('secondaryColor').value || null,
        imagePath: document.getElementById('imagePath').value
    };

    showLoading('상품을 등록하고 있습니다...');

    try {
        const response = await fetch('/api/admin/products/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.success) {
            showSuccessModal(result.itemName, result.itemId);
        } else {
            alert('상품 등록 실패: ' + result.message);
        }
    } catch (error) {
        console.error('등록 오류:', error);
        alert('상품 등록에 실패했습니다.');
    } finally {
        hideLoading();
    }
}

/**
 * 이미지 삭제 요청
 */
async function deleteImage(imagePath) {
    try {
        await fetch(`/api/admin/products/cancel?imagePath=${encodeURIComponent(imagePath)}`, {
            method: 'DELETE'
        });
    } catch (error) {
        console.error('이미지 삭제 실패:', error);
    }
}

/**
 * 업로드 초기화
 */
function resetUpload() {
    selectedFile = null;
    document.getElementById('imageInput').value = '';
    document.getElementById('previewImage').src = '';
    document.getElementById('uploadPlaceholder').style.display = 'block';
    document.getElementById('uploadPreview').style.display = 'none';
    document.getElementById('analyzeBtn').disabled = true;
}

/**
 * 전체 초기화
 */
function resetAll() {
    resetUpload();
    classificationResult = null;
    document.getElementById('resultSection').style.display = 'none';
    document.getElementById('productForm').reset();
}

/**
 * 로딩 표시
 */
function showLoading(text) {
    document.getElementById('loadingText').textContent = text;
    document.getElementById('loadingOverlay').style.display = 'flex';
}

/**
 * 로딩 숨기기
 */
function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

/**
 * 성공 모달 표시
 */
function showSuccessModal(itemName, itemId) {
    document.getElementById('successMessage').textContent =
        `"${itemName}" 상품이 성공적으로 등록되었습니다. (ID: ${itemId})`;
    document.getElementById('successModal').style.display = 'flex';
}
