from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import pipeline
from PIL import Image
import io
import logging
from datetime import datetime
import time
import numpy as np
import hashlib

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

class ImprovedFashionClassifier:
    def __init__(self):
        self.models = {}
        self.load_models()
        self.processed_images = {}

    def load_models(self):
        """실제 작동하는 모델들 로드"""
        try:
            logger.info("Loading image classification model...")
            self.models['general_classifier'] = pipeline(
                "image-classification",
                model="google/vit-base-patch16-224",
                device=-1
            )
            logger.info("ViT model loaded successfully")

        except Exception as e:
            logger.error(f"Failed to load models: {e}")
            self.models = {}

    def get_image_hash(self, image_data):
        """이미지 데이터의 해시값 계산"""
        return hashlib.md5(image_data).hexdigest()

    def get_image_info(self, image):
        """이미지 상세 정보 추출"""
        try:
            width, height = image.size
            mode = image.mode

            img_array = np.array(image)

            if len(img_array.shape) == 3:
                mean_rgb = np.mean(img_array, axis=(0, 1))
                std_rgb = np.std(img_array, axis=(0, 1))
            else:
                mean_rgb = np.mean(img_array)
                std_rgb = np.std(img_array)

            return {
                'size': f"{width}x{height}",
                'mode': mode,
                'mean_rgb': mean_rgb.tolist() if hasattr(mean_rgb, 'tolist') else mean_rgb,
                'std_rgb': std_rgb.tolist() if hasattr(std_rgb, 'tolist') else std_rgb,
                'total_pixels': width * height
            }
        except Exception as e:
            logger.error(f"Failed to get image info: {e}")
            return {'error': str(e)}

    def classify_fashion_item(self, image, image_hash, filename):
        """패션 아이템 분류"""
        try:
            logger.info(f"=== 새로운 분류 요청 ===")
            logger.info(f"파일명: {filename}")
            logger.info(f"이미지 해시: {image_hash}")

            image_info = self.get_image_info(image)
            logger.info(f"이미지 정보: {image_info}")

            if image_hash in self.processed_images:
                logger.warning(f"동일한 이미지 해시 감지! 이전 처리: {self.processed_images[image_hash]}")
            else:
                self.processed_images[image_hash] = {
                    'filename': filename,
                    'timestamp': datetime.now().isoformat(),
                    'image_info': image_info
                }

            if image is None:
                logger.error("Image is None")
                return self.get_default_classification()

            original_size = image.size
            if image.size[0] > 224 or image.size[1] > 224:
                image = image.resize((224, 224), Image.Resampling.LANCZOS)
                logger.info(f"이미지 크기 조정: {original_size} -> {image.size}")

            if 'general_classifier' in self.models:
                logger.info("AI 모델로 분류 시작...")
                ai_results = self.classify_with_ai(image)
                logger.info(f"AI 분류 완료: {ai_results}")
            else:
                logger.warning("AI 모델 없음, 규칙 기반 분류 사용")
                ai_results = None

            color = self.extract_dominant_color(image)
            logger.info(f"추출된 색상: {color}")

            result = self.process_results(ai_results, color, image, filename)
            logger.info(f"최종 분류 결과: {result}")

            return result

        except Exception as e:
            logger.error(f"Classification failed: {e}")
            return self.get_default_classification()

    def classify_with_ai(self, image):
        """AI 모델로 분류"""
        try:
            results = self.models['general_classifier'](image)

            logger.info(f"AI 모델 원본 결과 (상위 5개):")
            for i, result in enumerate(results[:5]):
                logger.info(f"  {i+1}. {result['label']}: {result['score']:.6f}")

            return results[:3]
        except Exception as e:
            logger.error(f"AI classification failed: {e}")
            return None

    def extract_dominant_color(self, image):
        """이미지에서 주요 색상 추출 - 개선된 버전"""
        try:
            # 원본 이미지를 50x50으로 축소
            small_image = image.resize((50, 50))
            image_array = np.array(small_image)

            logger.info(f"색상 분석용 이미지 크기: {image_array.shape}")

            if len(image_array.shape) == 3:
                # 중앙 영역의 색상을 더 중요하게 고려 (배경 제거 효과)
                center_h, center_w = image_array.shape[:2]
                center_region = image_array[
                                center_h//4:3*center_h//4,
                                center_w//4:3*center_w//4
                                ]

                # 전체 이미지와 중앙 영역의 평균 계산
                full_avg = np.mean(image_array.reshape(-1, 3), axis=0)
                center_avg = np.mean(center_region.reshape(-1, 3), axis=0)

                # 중앙 영역에 가중치 부여 (70% 중앙, 30% 전체)
                weighted_avg = 0.7 * center_avg + 0.3 * full_avg
                r, g, b = weighted_avg.astype(int)

                logger.info(f"전체 평균 RGB: {full_avg.astype(int)}")
                logger.info(f"중앙 평균 RGB: {center_avg.astype(int)}")
                logger.info(f"가중 평균 RGB: R={r}, G={g}, B={b}")

                # 색상 분산도 계산 (단색인지 여러 색상인지 판단)
                color_std = np.std(image_array.reshape(-1, 3), axis=0)
                logger.info(f"색상 표준편차: {color_std}")

                # 가장 많이 나타나는 색상들 분석
                unique_colors, counts = np.unique(
                    image_array.reshape(-1, 3), axis=0, return_counts=True
                )

                # 상위 3개 색상 로깅
                if len(counts) > 0:
                    top_indices = np.argsort(counts)[-min(3, len(counts)):][::-1]
                    logger.info("상위 색상들:")
                    for i, idx in enumerate(top_indices):
                        color_rgb = unique_colors[idx]
                        count = counts[idx]
                        percentage = (count / len(image_array.reshape(-1, 3))) * 100
                        logger.info(f"  {i+1}. RGB{tuple(color_rgb)}: {count}개 ({percentage:.1f}%)")

            else:
                logger.warning("그레이스케일 이미지")
                return "GRAY"

            color_name = self.rgb_to_color_name_improved(r, g, b)
            logger.info(f"최종 색상 결정: RGB({r},{g},{b}) -> {color_name}")

            return color_name

        except Exception as e:
            logger.error(f"Color extraction failed: {e}")
            return "BLACK"

    def rgb_to_color_name_improved(self, r, g, b):
        """개선된 RGB 값을 색상명으로 변환"""

        # 밝기 계산
        brightness = (r + g + b) / 3

        # 최대값과 최소값으로 채도와 명도 계산
        max_val = max(r, g, b)
        min_val = min(r, g, b)
        diff = max_val - min_val

        # 채도 계산 (0~1)
        saturation = 0 if max_val == 0 else diff / max_val

        logger.info(f"색상 분석: 밝기={brightness:.1f}, 채도={saturation:.2f}, 대비={diff}, max={max_val}")

        # 1. 무채색 판단 (채도가 낮거나 대비가 적으면)
        if saturation < 0.15 or diff < 25:
            if brightness > 200:
                return "WHITE"
            elif brightness < 70:  # 더 관대하게 조정
                return "BLACK"
            else:
                return "GRAY"

        # 2. 유채색 판단 - 더 관대한 기준

        # 흰색 (매우 밝고 채도가 낮음)
        if brightness > 220 and saturation < 0.3:
            return "WHITE"

        # 검은색 (매우 어둡고 채도가 낮음)
        if brightness < 50:
            return "BLACK"

        # 주요 색상 채널 기반 판단
        if max_val == r:  # 빨간색 계열이 가장 강함
            if g > b + 30 and g > 100:  # 초록도 강하면
                if g > r * 0.8:  # 거의 비슷하면 노랑
                    return "YELLOW"
                elif r > 150:  # 빨강이 더 강하면 오렌지
                    return "ORANGE"
                else:
                    return "RED"
            elif b > g + 20 and b > 80:  # 파랑이 초록보다 강하면 보라
                return "PURPLE"
            else:  # 순수 빨강 계열
                if r > 180 and max(g, b) < 100:
                    return "RED"
                elif r > 120 and g > 80 and b < 80:
                    return "ORANGE"
                else:
                    return "RED"

        elif max_val == g:  # 초록색 계열이 가장 강함
            if r > b + 30 and r > 80:  # 빨강도 강하면 노랑 계열
                if r > g * 0.8:
                    return "YELLOW"
                else:
                    return "GREEN"
            elif b > r + 20:  # 파랑이 더 강하면 청록
                return "GREEN"  # 또는 CYAN이지만 우리는 GREEN으로 통합
            else:  # 순수 초록
                return "GREEN"

        elif max_val == b:  # 파란색 계열이 가장 강함
            if r > g + 20 and r > 60:  # 빨강이 초록보다 강하면 보라
                return "PURPLE"
            elif g > r + 20:  # 초록이 더 강하면 청록
                return "BLUE"  # 우리는 BLUE로 통합
            else:  # 순수 파랑
                if b > 150 and max(r, g) < 100:
                    return "BLUE"
                elif b > 100 and max(r, g) < 70:
                    return "NAVY"
                else:
                    return "BLUE"

        # 3. 특수 색상들

        # 갈색 (빨강과 초록이 있고 파랑이 적음)
        if r > 80 and g > 50 and b < 80 and r > g and (r + g) > b * 2:
            return "BROWN"

        # 분홍색 (빨강이 강하고 밝음)
        if r > 150 and brightness > 150 and g > 100 and b > 100:
            return "RED"  # 분홍은 RED로 통합

        # 4. 비슷한 값들일 때 (혼합색)
        if abs(r - g) < 40 and abs(g - b) < 40:
            if brightness > 180:
                return "WHITE"
            elif brightness < 80:
                return "BLACK"
            else:
                return "GRAY"

        # 5. 기본 판단 (가장 큰 값 기준)
        if r >= g and r >= b:
            return "RED"
        elif g >= r and g >= b:
            return "GREEN"
        else:
            return "BLUE"

    def map_to_fashion_category(self, ai_label):
        """AI 라벨을 패션 카테고리로 매핑"""
        ai_label = ai_label.lower()

        logger.info(f"카테고리 매핑 시작: '{ai_label}'")

        # 우선순위 기반 매핑 (더 구체적인 것 먼저)

        # 1. 하의 (BOTTOM) - 가장 우선순위
        if any(word in ai_label for word in ['jean', 'denim', 'pants', 'trousers', 'shorts', 'leggings', 'jogger']):
            logger.info(f"BOTTOM으로 분류: {ai_label}")
            return 'BOTTOM'

        # 2. 아우터 (OUTER)
        elif any(word in ai_label for word in ['jacket', 'coat', 'blazer', 'padding', 'puffer', 'cardigan']):
            logger.info(f"OUTER로 분류: {ai_label}")
            return 'OUTER'

        # 3. 원피스 (DRESS)
        elif any(word in ai_label for word in ['dress', 'gown', 'skirt']):
            logger.info(f"DRESS로 분류: {ai_label}")
            return 'DRESS'

        # 4. 신발 (SHOES)
        elif any(word in ai_label for word in ['shoe', 'sneaker', 'boot', 'sandal', 'heel', 'loafer']):
            logger.info(f"SHOES로 분류: {ai_label}")
            return 'SHOES'

        # 5. 액세서리 (ACCESSORY)
        elif any(word in ai_label for word in ['bag', 'handbag', 'purse', 'hat', 'cap', 'belt', 'watch']):
            logger.info(f"ACCESSORY로 분류: {ai_label}")
            return 'ACCESSORY'

        # 6. 상의 (TOP) - 기본값
        else:
            logger.info(f"TOP으로 분류 (기본값): {ai_label}")
            return 'TOP'

    def filename_based_classification(self, filename):
        """파일명 기반 분류"""
        if not filename:
            return 'TOP'

        filename = filename.lower()
        logger.info(f"파일명 기반 분류: '{filename}'")

        # 한국어 + 영어 키워드 모두 지원
        if any(word in filename for word in ['바지', 'pants', 'jean', 'denim', '조거', 'jogger', '슬랙스', 'slacks']):
            logger.info(f"파일명으로 BOTTOM 분류: {filename}")
            return 'BOTTOM'
        elif any(word in filename for word in ['패딩', 'padding', '코트', 'coat', '자켓', 'jacket', '아우터', 'outer']):
            logger.info(f"파일명으로 OUTER 분류: {filename}")
            return 'OUTER'
        elif any(word in filename for word in ['원피스', 'dress', '스커트', 'skirt']):
            logger.info(f"파일명으로 DRESS 분류: {filename}")
            return 'DRESS'
        elif any(word in filename for word in ['티셔츠', 'tshirt', '상의', 'top', '셔츠', 'shirt']):
            logger.info(f"파일명으로 TOP 분류: {filename}")
            return 'TOP'
        else:
            logger.info(f"파일명 분류 실패, TOP으로 기본값: {filename}")
            return 'TOP'

    def enhanced_gender_detection(self, ai_label, filename):
        """AI 라벨과 파일명을 조합한 성별 추정"""
        ai_label = ai_label.lower()
        filename = filename.lower() if filename else ""

        # AI 라벨에서 성별 키워드 찾기
        if any(word in ai_label for word in ['women', 'female', 'ladies', 'woman', 'girl']):
            return 'FEMALE'
        elif any(word in ai_label for word in ['men', 'male', 'mens', 'man', 'boy']):
            return 'MALE'

        # 파일명에서 성별 키워드 찾기
        if any(word in filename for word in ['여자', '여성', 'women', 'female', 'ladies']):
            return 'FEMALE'
        elif any(word in filename for word in ['남자', '남성', 'men', 'male', 'mens']):
            return 'MALE'

        return 'UNISEX'

    def filename_based_gender(self, filename):
        """파일명 기반 성별 추정"""
        if not filename:
            return 'UNISEX'

        filename = filename.lower()

        if any(word in filename for word in ['여자', '여성', 'women', 'female']):
            return 'FEMALE'
        elif any(word in filename for word in ['남자', '남성', 'men', 'male']):
            return 'MALE'
        else:
            return 'UNISEX'

    def process_results(self, ai_results, color, image, filename):
        """결과 처리"""
        try:
            category = 'TOP'
            gender = 'UNISEX'
            confidence = 0.5

            logger.info(f"=== 결과 처리 시작 ===")
            logger.info(f"파일명: {filename}")

            # AI 결과 분석
            if ai_results and len(ai_results) > 0:
                best_label = ai_results[0]['label'].lower()
                confidence = ai_results[0]['score']

                logger.info(f"AI 최고 결과: '{best_label}' (신뢰도: {confidence:.3f})")

                # AI 기반 카테고리 매핑
                ai_category = self.map_to_fashion_category(best_label)
                logger.info(f"AI 기반 카테고리: {ai_category}")

                # 파일명 기반 카테고리 (검증용)
                filename_category = self.filename_based_classification(filename)
                logger.info(f"파일명 기반 카테고리: {filename_category}")

                # 두 결과가 다르면 로그 출력
                if ai_category != filename_category:
                    logger.warning(f"AI와 파일명 분류 결과 다름: AI={ai_category}, 파일명={filename_category}")

                    # 신뢰도가 낮으면 파일명 우선
                    if confidence < 0.6:
                        logger.info(f"신뢰도 낮음({confidence:.3f}), 파일명 분류 사용: {filename_category}")
                        category = filename_category
                    else:
                        logger.info(f"신뢰도 높음({confidence:.3f}), AI 분류 사용: {ai_category}")
                        category = ai_category
                else:
                    logger.info(f"AI와 파일명 분류 일치: {ai_category}")
                    category = ai_category

                # 성별 추정
                gender = self.enhanced_gender_detection(best_label, filename)

                logger.info(f"최종 매핑 결과: 카테고리={category}, 성별={gender}")

            else:
                logger.warning("AI 결과 없음, 파일명 기반 분류만 사용")
                category = self.filename_based_classification(filename)
                gender = self.filename_based_gender(filename)

            return self.convert_to_spring_format(category, gender, confidence, color, filename)

        except Exception as e:
            logger.error(f"Result processing failed: {e}")
            return self.get_default_classification()

    def convert_to_spring_format(self, category, gender, confidence, color, filename):
        """Spring enum 형식으로 변환"""
        try:
            logger.info(f"Spring 변환 시작: 카테고리={category}, 성별={gender}")

            if gender == 'UNISEX':
                # 카테고리별 기본 성별 설정
                if category == 'DRESS':
                    gender = 'FEMALE'
                else:
                    gender = 'MALE'  # 기본값
                logger.info(f"UNISEX -> {gender}로 변경")

            gender_prefix = "WOMENS" if gender == "FEMALE" else "MENS"
            gender_sub = "W" if gender == "FEMALE" else "M"

            # 카테고리별 정확한 서브카테고리 매핑
            spring_mapping = {
                'TOP': {
                    'category': f'{gender_prefix}_TOP',
                    'subCategory': f'{gender_sub}_TSHIRT'
                },
                'BOTTOM': {
                    'category': f'{gender_prefix}_BOTTOM',
                    'subCategory': f'{gender_sub}_JEANS'  # 바지는 JEANS로
                },
                'OUTER': {
                    'category': f'{gender_prefix}_OUTER',
                    'subCategory': f'{gender_sub}_JACKET'
                },
                'DRESS': {
                    'category': 'WOMENS_TOP',
                    'subCategory': 'W_DRESS'
                },
                'SHOES': {
                    'category': f'{gender_prefix}_SHOES',
                    'subCategory': f'{gender_sub}_SNEAKERS'
                },
                'ACCESSORY': {
                    'category': f'{gender_prefix}_ACCESSORY',
                    'subCategory': f'{gender_sub}_BAG'
                }
            }

            if category not in spring_mapping:
                logger.warning(f"알 수 없는 카테고리: {category}, TOP으로 기본값 사용")
                category = 'TOP'

            spring_data = spring_mapping[category]
            logger.info(f"Spring 매핑: {category} -> {spring_data}")

            result = {
                'category': spring_data['category'],
                'subCategory': spring_data['subCategory'],
                'gender': gender,
                'style': 'CASUAL',
                'ageGroup': 'TEEN',
                'season': 'SPRING',
                'confidence': round(confidence, 3),
                'color': color,
                'debug_info': {
                    'filename': filename,
                    'detected_category': category,
                    'ai_confidence': confidence,
                    'final_mapping': spring_data
                }
            }

            logger.info(f"최종 Spring 형식 결과: {result}")
            return result

        except Exception as e:
            logger.error(f"Spring format conversion failed: {e}")
            return self.get_default_classification()

    def get_default_classification(self):
        """기본 분류 결과"""
        return {
            'category': 'MENS_TOP',
            'subCategory': 'M_TSHIRT',
            'gender': 'UNISEX',
            'style': 'CASUAL',
            'ageGroup': 'TEEN',
            'season': 'SPRING',
            'confidence': 0.1,
            'color': 'BLACK',
            'debug_info': {'method': 'default'}
        }

# 전역 변수
classifier = None

def load_classifier():
    """분류기 로드"""
    global classifier
    try:
        classifier = ImprovedFashionClassifier()
        logger.info("Improved Fashion Classifier loaded successfully")
        return True
    except Exception as e:
        logger.error(f"Failed to load classifier: {e}")
        return False

@app.route('/health', methods=['GET'])
def health_check():
    """헬스 체크"""
    models_loaded = 0
    processed_count = 0

    if classifier:
        if classifier.models:
            models_loaded = len(classifier.models)
        processed_count = len(classifier.processed_images)

    return jsonify({
        'status': 'healthy',
        'classifier_loaded': classifier is not None,
        'models_available': models_loaded,
        'processed_images_count': processed_count,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/classify-image', methods=['POST'])
def classify_image():
    """이미지 기반 패션 아이템 분류"""
    start_time = time.time()

    try:
        # 요청 검증
        if 'image' not in request.files:
            return jsonify({'success': False, 'message': 'No image file provided'}), 400

        image_file = request.files['image']
        filename = image_file.filename or "unknown"

        if filename == '':
            return jsonify({'success': False, 'message': 'Empty filename'}), 400

        logger.info(f"===== 새로운 이미지 분류 요청 =====")
        logger.info(f"파일명: {filename}")

        # 이미지 로드 및 해시 계산
        try:
            image_data = image_file.read()
            if len(image_data) == 0:
                return jsonify({'success': False, 'message': 'Empty image file'}), 400

            # 이미지 해시 계산
            image_hash = hashlib.md5(image_data).hexdigest()
            logger.info(f"이미지 해시: {image_hash}")
            logger.info(f"이미지 크기: {len(image_data)} bytes")

            image = Image.open(io.BytesIO(image_data)).convert('RGB')
            logger.info(f"PIL 이미지 크기: {image.size}")

        except Exception as e:
            logger.error(f"Image loading failed: {e}")
            return jsonify({'success': False, 'message': f'Invalid image file: {str(e)}'}), 400

        # 분류 실행
        if classifier:
            result_data = classifier.classify_fashion_item(image, image_hash, filename)
            classification_method = "improved_enhanced"
        else:
            result_data = ImprovedFashionClassifier().get_default_classification()
            classification_method = "default"

        total_time = time.time() - start_time

        # 최종 응답
        result = {
            'success': True,
            'message': 'Classification completed successfully',
            'data': {
                **result_data,
                'classification_method': classification_method,
                'processing_time': round(total_time, 2),
                'image_hash': image_hash
            },
            'timestamp': datetime.now().isoformat()
        }

        logger.info(f"===== 분류 완료: {result_data['category']} in {total_time:.2f}s =====")
        return jsonify(result)

    except Exception as e:
        logger.error(f"Classification error: {str(e)}")
        return jsonify({
            'success': False,
            'message': f'Classification failed: {str(e)}'
        }), 500

@app.route('/debug/processed-images', methods=['GET'])
def get_processed_images():
    """처리된 이미지 목록 조회"""
    if classifier:
        return jsonify({
            'success': True,
            'processed_images': classifier.processed_images,
            'count': len(classifier.processed_images)
        })
    else:
        return jsonify({'success': False, 'message': 'Classifier not loaded'})

@app.route('/model-info', methods=['GET'])
def model_info():
    """모델 정보 조회"""
    models_loaded = []
    if classifier and classifier.models:
        models_loaded = list(classifier.models.keys())

    return jsonify({
        'model_architecture': 'ViT + Improved Color + Enhanced Rule-based Classification',
        'models_loaded': models_loaded,
        'classification_method': 'hybrid_improved',
        'spring_compatibility': True,
        'supported_categories': ['TOP', 'BOTTOM', 'OUTER', 'DRESS', 'SHOES', 'ACCESSORY'],
        'supported_colors': ['BLACK', 'WHITE', 'RED', 'BLUE', 'GREEN', 'YELLOW', 'ORANGE', 'PURPLE', 'BROWN', 'GRAY', 'NAVY']
    })

def initialize_app():
    """앱 초기화"""
    logger.info("Initializing Improved Fashion Classifier...")

    if not load_classifier():
        logger.warning("Classifier loading failed, using default classification")

    app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024
    logger.info("Improved Fashion Classifier initialization completed")

if __name__ == '__main__':
    initialize_app()
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)
else:
    initialize_app()