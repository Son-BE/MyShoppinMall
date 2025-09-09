from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
import torch.nn as nn
from torchvision import transforms, models
from PIL import Image
import io
import logging
from datetime import datetime
import gc
import time

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

class OptimizedFashionClassifier(nn.Module):
    """최적화된 패션 아이템 분류 모델"""

    def __init__(self, num_categories=10, num_genders=3, num_styles=8, num_seasons=4):
        super(OptimizedFashionClassifier, self).__init__()
        self.backbone = models.mobilenet_v3_small(weights='DEFAULT')
        self.feature_extractor = self.backbone.features
        self.avgpool = self.backbone.avgpool
        feature_dim = 576

        self.category_classifier = nn.Sequential(
            nn.Dropout(0.2),
            nn.Linear(feature_dim, num_categories)
        )
        self.gender_classifier = nn.Sequential(
            nn.Dropout(0.2),
            nn.Linear(feature_dim, num_genders)
        )
        self.style_classifier = nn.Sequential(
            nn.Dropout(0.2),
            nn.Linear(feature_dim, num_styles)
        )
        self.season_classifier = nn.Sequential(
            nn.Dropout(0.2),
            nn.Linear(feature_dim, num_seasons)
        )

    def forward(self, x):
        features = self.feature_extractor(x)
        features = self.avgpool(features)
        features = torch.flatten(features, 1)

        return {
            'category': self.category_classifier(features),
            'gender': self.gender_classifier(features),
            'style': self.style_classifier(features),
            'season': self.season_classifier(features)
        }

# 전역 변수
model = None
device = None
transform = None

# AI 라벨 매핑
CATEGORY_LABELS = {
    0: 'TOP', 1: 'BOTTOM', 2: 'DRESS', 3: 'OUTER', 4: 'SHOES',
    5: 'BAG', 6: 'ACCESSORY', 7: 'UNDERWEAR', 8: 'SPORT', 9: 'ETC'
}
GENDER_LABELS = {0: 'MALE', 1: 'FEMALE', 2: 'UNISEX'}
STYLE_LABELS = {0: 'CASUAL', 1: 'FORMAL', 2: 'SPORTY', 3: 'VINTAGE', 4: 'MODERN', 5: 'CLASSIC', 6: 'TRENDY', 7: 'MINIMALIST'}
SEASON_LABELS = {0: 'SPRING', 1: 'SUMMER', 2: 'AUTUMN', 3: 'WINTER'}

def classify_by_filename(filename, user_inputs=None):
    """파일명 기반 우선 분류 (100% 정확도 목표)"""
    filename_lower = filename.lower()

    # 사용자 입력도 함께 분석
    if user_inputs:
        item_name = user_inputs.get('itemName', '').lower()
        item_comment = user_inputs.get('itemComment', '').lower()
        full_text = f"{filename_lower} {item_name} {item_comment}"
    else:
        full_text = filename_lower

    logger.info(f"Analyzing text: '{full_text}'")

    # 성별 분류
    if any(word in full_text for word in ['남성', '남자', '맨즈', 'mens', 'man', 'male']):
        gender = 'MALE'
    elif any(word in full_text for word in ['여성', '여자', '우먼즈', 'womens', 'woman', 'female']):
        gender = 'FEMALE'
    else:
        gender = 'UNISEX'  # 기본값

    # 카테고리 분류 (파일명 우선)
    category = None
    style = 'CASUAL'
    season = 'SPRING'

    if any(word in full_text for word in ['바람막이', '자켓', '점퍼', 'jacket', 'windbreaker', '아우터', 'outer', '코트', 'coat', '패딩', 'padding']):
        category = 'OUTER'
        style = 'SPORTY'
        season = 'AUTUMN'
        logger.info("Detected OUTER category from filename")
    elif any(word in full_text for word in ['티셔츠', 'tshirt', 't-shirt', '셔츠', 'shirt', '블라우스', 'blouse']):
        category = 'TOP'
        style = 'CASUAL'
        logger.info("Detected TOP category from filename")
    elif any(word in full_text for word in ['후드티', '후드', 'hoodie', 'hood', '맨투맨', 'sweatshirt']):
        category = 'TOP'
        style = 'CASUAL'
        logger.info("Detected TOP category from filename")
    elif any(word in full_text for word in ['바지', 'pants', '청바지', 'jeans', 'jean', '슬랙스', 'slacks']):
        category = 'BOTTOM'
        style = 'CASUAL'
        logger.info("Detected BOTTOM category from filename")
    elif any(word in full_text for word in ['원피스', 'dress', '드레스', '치마', 'skirt']):
        category = 'DRESS'
        style = 'FORMAL'
        gender = 'FEMALE'
        logger.info("Detected DRESS category from filename")
    elif any(word in full_text for word in ['신발', 'shoes', '운동화', 'sneakers', '구두', 'boots']):
        category = 'SHOES'
        style = 'SPORTY' if any(word in full_text for word in ['운동화', 'sneakers']) else 'FORMAL'
        logger.info("Detected SHOES category from filename")
    elif any(word in full_text for word in ['가방', 'bag', '백팩', 'backpack']):
        category = 'ACCESSORY'
        logger.info("Detected ACCESSORY category from filename")

    return category, gender, style, season

def convert_to_spring_format(category, gender, style, season, filename):
    """Spring enum 형식으로 변환"""
    # 성별 접두어
    gender_prefix_category = "MENS" if gender == "MALE" else "WOMENS"
    gender_prefix_sub = "M" if gender == "MALE" else "W"

    # Spring 카테고리 매핑
    if category == 'TOP':
        spring_category = f'{gender_prefix_category}_TOP'
        spring_sub_category = f'{gender_prefix_sub}_TSHIRT'
    elif category == 'BOTTOM':
        spring_category = f'{gender_prefix_category}_BOTTOM'
        spring_sub_category = f'{gender_prefix_sub}_JEANS'
    elif category == 'OUTER':
        spring_category = f'{gender_prefix_category}_OUTER'
        # 파일명으로 더 정확한 서브카테고리 결정
        if '바람막이' in filename.lower() or 'windbreaker' in filename.lower():
            spring_sub_category = f'{gender_prefix_sub}_WINDBREAKER'
        elif '코트' in filename.lower() or 'coat' in filename.lower():
            spring_sub_category = f'{gender_prefix_sub}_COAT'
        elif '패딩' in filename.lower() or 'padding' in filename.lower():
            spring_sub_category = f'{gender_prefix_sub}_PADDING'
        else:
            spring_sub_category = f'{gender_prefix_sub}_WINDBREAKER'  # 기본값
    elif category == 'SHOES':
        spring_category = f'{gender_prefix_category}_SHOES'
        spring_sub_category = f'{gender_prefix_sub}_SNEAKERS'
    elif category == 'DRESS':
        spring_category = 'WOMENS_TOP'  # 드레스는 여성 상의로
        spring_sub_category = 'W_BLOUSE'
    elif category == 'ACCESSORY':
        spring_category = f'{gender_prefix_category}_ACCESSORY'
        spring_sub_category = f'{gender_prefix_sub}_WATCH'
    else:
        # 기본값
        spring_category = f'{gender_prefix_category}_TOP'
        spring_sub_category = f'{gender_prefix_sub}_TSHIRT'

    # Spring 스타일 매핑
    spring_style_mapping = {
        'CASUAL': 'CASUAL',
        'FORMAL': 'FORMAL',
        'SPORTY': 'SPORTY',
        'VINTAGE': 'VINTAGE',
        'MODERN': 'CLEAN',
        'CLASSIC': 'CHIC',
        'TRENDY': 'STREET',
        'MINIMALIST': 'MINIMAL'
    }
    spring_style = spring_style_mapping.get(style, 'CASUAL')

    # 연령대 매핑
    age_group_mapping = {
        'CASUAL': 'TWENTIES',
        'FORMAL': 'THIRTIES',
        'SPORTY': 'TWENTIES',
        'VINTAGE': 'FORTIES',
        'MODERN': 'TWENTIES',
        'CLASSIC': 'THIRTIES',
        'TRENDY': 'TEEN',
        'MINIMALIST': 'THIRTIES'
    }
    spring_age_group = age_group_mapping.get(style, 'TWENTIES')

    return {
        'category': spring_category,
        'subCategory': spring_sub_category,
        'gender': gender,
        'style': spring_style,
        'ageGroup': spring_age_group,
        'season': season.upper()
    }

def load_model():
    """모델 로드"""
    global model, device, transform

    try:
        device = torch.device('cpu')
        torch.set_num_threads(1)
        logger.info(f"Using device: {device}")

        model = OptimizedFashionClassifier()

        try:
            model_path = '/app/models/fashion_classifier.pth'
            checkpoint = torch.load(model_path, map_location=device)
            model.load_state_dict(checkpoint['model_state_dict'])
            logger.info("Loaded pre-trained model")
        except:
            logger.warning("Using default pretrained MobileNetV3")

        model.to(device)
        model.eval()

        transform = transforms.Compose([
            transforms.Resize((160, 160)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])

        logger.info("Model loaded successfully")
        return True

    except Exception as e:
        logger.error(f"Failed to load model: {e}")
        return False

@app.route('/health', methods=['GET'])
def health_check():
    """헬스 체크"""
    return jsonify({
        'status': 'healthy',
        'model_loaded': model is not None,
        'device': str(device) if device else 'unknown',
        'timestamp': datetime.now().isoformat()
    })

@app.route('/classify-image', methods=['POST'])
def classify_image():
    """파일명 우선 이미지 분류"""
    start_time = time.time()

    try:
        # 요청 검증
        if 'image' not in request.files:
            return jsonify({'success': False, 'message': 'No image file provided'}), 400

        image_file = request.files['image']
        if image_file.filename == '':
            return jsonify({'success': False, 'message': 'Empty filename'}), 400

        # 파일 크기 확인
        image_file.seek(0, 2)
        file_size = image_file.tell()
        image_file.seek(0)

        if file_size > 10 * 1024 * 1024:
            return jsonify({'success': False, 'message': 'File too large'}), 413

        # 사용자 입력 수집
        user_inputs = {
            'itemName': request.form.get('itemName', ''),
            'itemComment': request.form.get('itemComment', ''),
        }

        logger.info(f"Processing image: {image_file.filename} ({file_size/1024:.1f}KB)")
        logger.info(f"User inputs: {user_inputs}")

        # 1단계: 파일명 기반 분류 (우선 적용)
        category, gender, style, season = classify_by_filename(image_file.filename, user_inputs)

        classification_method = "filename_based"
        confidence = 0.9 if category else 0.3

        # 2단계: 파일명으로 분류 실패시 AI 모델 사용
        if not category and model:
            try:
                logger.info("Using AI model for classification")
                image = Image.open(io.BytesIO(image_file.read())).convert('RGB')
                image_tensor = transform(image).unsqueeze(0).to(device)

                with torch.no_grad():
                    outputs = model(image_tensor)

                # AI 결과 처리
                probs = torch.softmax(outputs['category'], dim=1)
                conf, pred_idx = torch.max(probs, 1)

                category = CATEGORY_LABELS[pred_idx.item()]
                gender = GENDER_LABELS[torch.argmax(torch.softmax(outputs['gender'], dim=1)).item()]
                style = STYLE_LABELS[torch.argmax(torch.softmax(outputs['style'], dim=1)).item()]
                season = SEASON_LABELS[torch.argmax(torch.softmax(outputs['season'], dim=1)).item()]

                confidence = conf.item()
                classification_method = "ai_model"

                del image_tensor
                gc.collect()

            except Exception as e:
                logger.error(f"AI classification failed: {e}")
                # 기본값 사용
                category = 'TOP'
                gender = 'UNISEX'
                style = 'CASUAL'
                season = 'SPRING'
                confidence = 0.1
                classification_method = "default"

        # 파일명 분류 실패하고 AI도 없으면 기본값
        if not category:
            category = 'TOP'
            gender = 'UNISEX'
            style = 'CASUAL'
            season = 'SPRING'
            classification_method = "default"

        # Spring 형식으로 변환
        spring_result = convert_to_spring_format(category, gender, style, season, image_file.filename)

        total_time = time.time() - start_time

        # 최종 응답
        result = {
            'success': True,
            'message': 'Classification completed successfully',
            'data': {
                'category': spring_result['category'],
                'gender': spring_result['gender'],
                'style': spring_result['style'],
                'season': spring_result['season'],
                'subCategory': spring_result['subCategory'],
                'ageGroup': spring_result['ageGroup'],
                'confidence_scores': {
                    'overall': round(confidence, 3)
                },
                'classification_method': classification_method,
                'processing_time': round(total_time, 2),
                'original_predictions': {
                    'category': category,
                    'gender': gender,
                    'style': style,
                    'season': season
                }
            },
            'timestamp': datetime.now().isoformat()
        }

        logger.info(f"Classification completed ({classification_method}): {spring_result['category']}/{spring_result['subCategory']} in {total_time:.2f}s")
        return jsonify(result)

    except Exception as e:
        logger.error(f"Classification error: {str(e)}")
        return jsonify({
            'success': False,
            'message': f'Classification failed: {str(e)}'
        }), 500

@app.route('/model-info', methods=['GET'])
def model_info():
    """모델 정보 조회"""
    return jsonify({
        'model_architecture': 'Filename-Priority + MobileNetV3 Hybrid Classifier',
        'classification_priority': ['filename_based', 'ai_model', 'default'],
        'spring_compatibility': True,
        'device': str(device) if device else 'unknown'
    })

def initialize_app():
    """앱 초기화"""
    logger.info("Initializing filename-priority Flask app...")

    if not load_model():
        logger.warning("Model loading failed, using filename-only classification")

    app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024
    logger.info("Filename-priority Flask app initialization completed")

if __name__ == '__main__':
    initialize_app()
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)
else:
    initialize_app()