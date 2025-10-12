# flask-api/recommend_Algorithm.py
import pandas as pd
import numpy as np
import json
import redis
import os
from datetime import datetime, timedelta
import warnings

warnings.filterwarnings('ignore')

# 머신러닝 라이브러리
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.decomposition import TruncatedSVD, NMF
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.cluster import KMeans
from sklearn.neighbors import NearestNeighbors

# 추천 시스템 라이브러리
try:
    from surprise import Dataset, Reader, SVD, NMF as SurpriseMF
    from surprise.model_selection import train_test_split, cross_validate
    SURPRISE_AVAILABLE = True
except ImportError:
    print("⚠️ Surprise 라이브러리가 설치되어 있지 않습니다. 기본 알고리즘을 사용합니다.")
    SURPRISE_AVAILABLE = False

# 딥러닝 (선택적)
try:
    import tensorflow as tf
    from tensorflow.keras.models import Model
    from tensorflow.keras.layers import Input, Embedding, Dot, Add, Flatten, Dense, Dropout
    from tensorflow.keras.optimizers import Adam
    TF_AVAILABLE = True
except ImportError:
    print("⚠️ TensorFlow가 설치되어 있지 않습니다. 딥러닝 기반 추천은 사용할 수 없습니다.")
    TF_AVAILABLE = False


class SonStarRecommendationSystem:
    def __init__(self, redis_config=None):
        """SonStar 추천 시스템 초기화"""

        # Config 임포트를 여기서 수행 (순환 참조 방지)
        try:
            from config import config
            self.config = config
        except ImportError:
            print("⚠️ config 모듈을 불러올 수 없습니다. 기본 설정을 사용합니다.")
            self.config = None

        # Redis 연결 (실시간 추천용)
        if redis_config is None:
            if self.config:
                redis_config = {
                    'host': self.config.REDIS_HOST,
                    'port': self.config.REDIS_PORT,
                    'db': self.config.REDIS_DB
                }
            else:
                redis_config = {
                    'host': 'localhost',
                    'port': 6379,
                    'db': 0
                }

        try:
            self.redis_client = redis.Redis(
                host=redis_config['host'],
                port=redis_config['port'],
                db=redis_config['db'],
                decode_responses=True
            )
            self.redis_client.ping()
            print("✅ Redis 연결 성공")
        except Exception as e:
            print(f"❌ Redis 연결 실패: {e}")
            self.redis_client = None

        # 모델 저장소
        self.models = {}
        self.scalers = {}
        self.encoders = {}

        # 데이터 저장소
        self.items_df = None
        self.members_df = None
        self.interactions_df = None
        self.user_item_matrix = None
        self.category_keywords = None

        print("🤖 SonStar 추천 시스템 초기화 완료")

    def load_preprocessed_data(self):
        """전처리된 데이터 로드"""
        try:
            # 파일 경로 설정
            if self.config:
                items_path = self.config.get_file_path('preprocessed_items.csv')
                members_path = self.config.get_file_path('preprocessed_members.csv')
                interactions_path = self.config.get_file_path('preprocessed_interactions.csv')
                matrix_path = self.config.get_file_path('user_item_matrix.csv')
                keywords_path = self.config.get_file_path('category_keywords.json')
            else:
                items_path = 'preprocessed_items.csv'
                members_path = 'preprocessed_members.csv'
                interactions_path = 'preprocessed_interactions.csv'
                matrix_path = 'user_item_matrix.csv'
                keywords_path = 'category_keywords.json'

            self.items_df = pd.read_csv(items_path)
            self.members_df = pd.read_csv(members_path)
            self.interactions_df = pd.read_csv(interactions_path)
            self.user_item_matrix = pd.read_csv(matrix_path)

            # NLP 결과 로드
            try:
                if os.path.exists(keywords_path):
                    with open(keywords_path, 'r', encoding='utf-8') as f:
                        self.category_keywords = json.load(f)
                else:
                    self.category_keywords = {}
            except Exception as e:
                print(f"⚠️ 키워드 파일 로드 실패: {e}")
                self.category_keywords = {}

            print(f"✅ 데이터 로드 완료")
            print(f"  - 상품: {len(self.items_df)}개")
            print(f"  - 회원: {len(self.members_df)}개")
            print(f"  - 상호작용: {len(self.interactions_df)}개")
            print(f"  - 사용자-아이템 매트릭스: {len(self.user_item_matrix)}개")

            return True

        except Exception as e:
            print(f"❌ 데이터 로드 실패: {e}")
            return False

    def build_user_item_matrix(self):
        """사용자-아이템 매트릭스 구축"""
        if self.user_item_matrix is None or self.user_item_matrix.empty:
            print("사용자-아이템 매트릭스가 없습니다.")
            return None

        # 피벗 테이블 생성
        matrix = self.user_item_matrix.pivot_table(
            index='user_id',
            columns='product_id',
            values='final_score',
            fill_value=0
        )

        print(f"✅ 사용자-아이템 매트릭스: {matrix.shape}")
        return matrix

    def collaborative_filtering_user_based(self, target_user_id, n_recommendations=10, n_neighbors=50):
        """사용자 기반 협업 필터링"""
        print(f"\n=== 사용자 기반 협업 필터링 (사용자 ID: {target_user_id}) ===")

        matrix = self.build_user_item_matrix()
        if matrix is None:
            return []

        # 타겟 사용자가 매트릭스에 없는 경우
        if target_user_id not in matrix.index:
            print(f"사용자 {target_user_id}의 상호작용 기록이 없습니다.")
            return self.get_popular_items(n_recommendations)

        # 사용자 간 유사도 계산
        user_similarity = cosine_similarity(matrix)
        user_similarity_df = pd.DataFrame(
            user_similarity,
            index=matrix.index,
            columns=matrix.index
        )

        # 타겟 사용자와 유사한 사용자 찾기
        target_similarities = user_similarity_df[target_user_id].sort_values(ascending=False)
        similar_users = target_similarities.iloc[1:n_neighbors + 1]  # 자기 자신 제외

        # 타겟 사용자가 평가하지 않은 아이템 찾기
        target_user_items = matrix.loc[target_user_id]
        unrated_items = target_user_items[target_user_items == 0].index

        # 유사한 사용자들의 평점 기반으로 추천 점수 계산
        recommendations = {}

        for item_id in unrated_items:
            weighted_score = 0
            similarity_sum = 0

            for user_id, similarity in similar_users.items():
                if matrix.loc[user_id, item_id] > 0:  # 해당 사용자가 이 아이템을 평가했다면
                    weighted_score += similarity * matrix.loc[user_id, item_id]
                    similarity_sum += abs(similarity)

            if similarity_sum > 0:
                recommendations[item_id] = weighted_score / similarity_sum

        # 상위 추천 아이템 선택
        recommended_items = sorted(recommendations.items(), key=lambda x: x[1], reverse=True)[:n_recommendations]

        # 아이템 정보와 함께 반환
        result = []
        for item_id, score in recommended_items:
            item_info = self.get_item_info(item_id)
            if item_info is not None:
                item_info['recommendation_score'] = float(score)
                item_info['recommendation_reason'] = f"유사한 취향의 사용자들이 선호 (유사도: {len(similar_users)}명)"
                result.append(item_info)

        print(f"✅ 사용자 기반 추천 완료: {len(result)}개")
        return result

    def collaborative_filtering_item_based(self, target_user_id, n_recommendations=10, n_neighbors=30):
        """아이템 기반 협업 필터링"""
        print(f"\n=== 아이템 기반 협업 필터링 (사용자 ID: {target_user_id}) ===")

        matrix = self.build_user_item_matrix()
        if matrix is None:
            return []

        # 타겟 사용자가 평가한 아이템들
        if target_user_id not in matrix.index:
            return self.get_popular_items(n_recommendations)

        user_items = matrix.loc[target_user_id]
        rated_items = user_items[user_items > 0]

        if len(rated_items) == 0:
            return self.get_popular_items(n_recommendations)

        # 아이템 간 유사도 계산
        item_similarity = cosine_similarity(matrix.T)
        item_similarity_df = pd.DataFrame(
            item_similarity,
            index=matrix.columns,
            columns=matrix.columns
        )

        # 각 평가된 아이템에 대해 유사한 아이템들 찾기
        recommendations = {}

        for item_id, rating in rated_items.items():
            similar_items = item_similarity_df[item_id].sort_values(ascending=False)

            # 상위 유사 아이템들 (자기 자신 제외)
            for similar_item_id, similarity in similar_items.iloc[1:n_neighbors + 1].items():
                if similar_item_id not in rated_items.index:  # 사용자가 아직 평가하지 않은 아이템
                    if similar_item_id not in recommendations:
                        recommendations[similar_item_id] = 0
                    recommendations[similar_item_id] += similarity * rating

        # 상위 추천 아이템 선택
        recommended_items = sorted(recommendations.items(), key=lambda x: x[1], reverse=True)[:n_recommendations]

        # 아이템 정보와 함께 반환
        result = []
        for item_id, score in recommended_items:
            item_info = self.get_item_info(item_id)
            if item_info is not None:
                item_info['recommendation_score'] = float(score)
                item_info['recommendation_reason'] = "선호 상품과 유사한 특성"
                result.append(item_info)

        print(f"✅ 아이템 기반 추천 완료: {len(result)}개")
        return result

    def matrix_factorization_svd(self, target_user_id, n_recommendations=10, n_components=50):
        """SVD 매트릭스 분해 기반 협업 필터링"""
        print(f"\n=== SVD 매트릭스 분해 추천 (사용자 ID: {target_user_id}) ===")

        matrix = self.build_user_item_matrix()
        if matrix is None:
            return []

        # SVD 분해
        svd = TruncatedSVD(n_components=n_components, random_state=42)
        matrix_reduced = svd.fit_transform(matrix)

        # 복원된 매트릭스
        matrix_reconstructed = svd.inverse_transform(matrix_reduced)
        matrix_reconstructed_df = pd.DataFrame(
            matrix_reconstructed,
            index=matrix.index,
            columns=matrix.columns
        )

        # 타겟 사용자 예측 평점
        if target_user_id not in matrix_reconstructed_df.index:
            return self.get_popular_items(n_recommendations)

        user_predictions = matrix_reconstructed_df.loc[target_user_id]
        original_ratings = matrix.loc[target_user_id]

        # 사용자가 평가하지 않은 아이템들에 대한 예측
        unrated_predictions = user_predictions[original_ratings == 0]
        top_predictions = unrated_predictions.sort_values(ascending=False)[:n_recommendations]

        # 결과 반환
        result = []
        for item_id, predicted_score in top_predictions.items():
            item_info = self.get_item_info(item_id)
            if item_info is not None:
                item_info['recommendation_score'] = float(predicted_score)
                item_info['recommendation_reason'] = "AI 패턴 분석 기반 예측"
                result.append(item_info)

        print(f"✅ SVD 추천 완료: {len(result)}개")
        return result

    def content_based_filtering(self, target_user_id, n_recommendations=10):
        """콘텐츠 기반 필터링 (상품 속성 + NLP 키워드)"""
        print(f"\n=== 콘텐츠 기반 필터링 (사용자 ID: {target_user_id}) ===")

        if self.items_df is None or self.items_df.empty:
            return []

        # 사용자 선호도 프로파일 생성
        user_profile = self.build_user_content_profile(target_user_id)
        if user_profile is None:
            return self.get_popular_items(n_recommendations)

        # 상품 특성 벡터 생성
        item_features = self.build_item_content_features()

        # 사용자 프로파일과 상품 특성 간 유사도 계산
        profile_vector = self.vectorize_user_profile(user_profile, item_features.columns)
        similarities = cosine_similarity([profile_vector], item_features)[0]

        # 상품별 유사도 점수
        item_scores = pd.Series(similarities, index=item_features.index)

        # 사용자가 이미 상호작용한 상품 제외
        user_interactions = self.get_user_interactions(target_user_id)
        if len(user_interactions) > 0:
            item_scores = item_scores.drop(user_interactions, errors='ignore')

        # 상위 추천 아이템
        top_items = item_scores.sort_values(ascending=False)[:n_recommendations]

        # 결과 반환
        result = []
        for item_id, score in top_items.items():
            item_info = self.get_item_info(item_id)
            if item_info is not None:
                item_info['recommendation_score'] = float(score)
                item_info['recommendation_reason'] = "선호 스타일과 유사한 특성"
                result.append(item_info)

        print(f"✅ 콘텐츠 기반 추천 완료: {len(result)}개")
        return result

    def build_user_content_profile(self, user_id):
        """사용자 콘텐츠 선호도 프로파일 생성"""
        if self.interactions_df is None:
            return None

        # 사용자의 상호작용 상품들
        user_interactions = self.interactions_df[self.interactions_df['user_id'] == user_id]

        if len(user_interactions) == 0:
            return None

        # 상호작용한 상품들의 정보
        interacted_items = self.items_df[
            self.items_df['item_id'].isin(user_interactions['product_id'])
        ]

        if len(interacted_items) == 0:
            return None

        # 가중 평균으로 선호도 계산
        profile = {}

        # 카테고리 선호도
        categories = interacted_items['category'].value_counts(normalize=True)
        profile['preferred_categories'] = categories.to_dict()

        # 가격대 선호도
        if 'price_range' in interacted_items.columns:
            price_ranges = interacted_items['price_range'].value_counts(normalize=True)
            profile['preferred_price_ranges'] = price_ranges.to_dict()

        # 성별 선호도
        if 'gender' in interacted_items.columns:
            genders = interacted_items['gender'].value_counts(normalize=True)
            profile['preferred_genders'] = genders.to_dict()

        # 스타일 선호도
        if 'style' in interacted_items.columns:
            styles = interacted_items['style'].value_counts(normalize=True)
            profile['preferred_styles'] = styles.to_dict()

        # 키워드 선호도 (NLP 결과 활용)
        if 'keywords_str' in interacted_items.columns:
            all_keywords = []
            for keywords_str in interacted_items['keywords_str'].dropna():
                if keywords_str:
                    all_keywords.extend(keywords_str.split(','))

            if all_keywords:
                from collections import Counter
                keyword_freq = Counter(all_keywords)
                total_keywords = sum(keyword_freq.values())
                profile['preferred_keywords'] = {
                    k: v / total_keywords for k, v in keyword_freq.most_common(20)
                }

        return profile

    def build_item_content_features(self):
        """상품 콘텐츠 특성 벡터 생성"""
        features_df = pd.DataFrame(index=self.items_df['item_id'])

        # 원-핫 인코딩으로 카테고리 특성 생성
        categorical_columns = ['category', 'gender', 'style', 'season']
        if 'price_range' in self.items_df.columns:
            categorical_columns.append('price_range')

        for col in categorical_columns:
            if col in self.items_df.columns:
                dummies = pd.get_dummies(self.items_df[col], prefix=col)
                dummies.index = self.items_df['item_id']
                features_df = pd.concat([features_df, dummies], axis=1)

        # 수치형 특성 정규화
        numeric_columns = ['price', 'item_rating']
        if 'popularity_score' in self.items_df.columns:
            numeric_columns.append('popularity_score')

        for col in numeric_columns:
            if col in self.items_df.columns:
                scaler = StandardScaler()
                normalized_values = scaler.fit_transform(self.items_df[[col]])
                features_df[f'{col}_normalized'] = normalized_values.flatten()

        # NaN 값을 0으로 채우기
        features_df = features_df.fillna(0)

        return features_df

    def vectorize_user_profile(self, user_profile, feature_columns):
        """사용자 프로파일을 특성 벡터로 변환"""
        profile_vector = np.zeros(len(feature_columns))

        # 카테고리 선호도 반영
        if 'preferred_categories' in user_profile:
            for category, weight in user_profile['preferred_categories'].items():
                matching_cols = [col for col in feature_columns if f'category_{category}' in col]
                for col in matching_cols:
                    col_idx = list(feature_columns).index(col)
                    profile_vector[col_idx] = weight

        # 기타 선호도도 비슷하게 처리
        preference_mappings = {
            'preferred_genders': 'gender',
            'preferred_styles': 'style',
            'preferred_price_ranges': 'price_range'
        }

        for pref_key, prefix in preference_mappings.items():
            if pref_key in user_profile:
                for value, weight in user_profile[pref_key].items():
                    matching_cols = [col for col in feature_columns if f'{prefix}_{value}' in col]
                    for col in matching_cols:
                        col_idx = list(feature_columns).index(col)
                        profile_vector[col_idx] = weight

        return profile_vector

    def hybrid_recommendation(self, target_user_id, n_recommendations=10):
        """하이브리드 추천 (여러 알고리즘 조합)"""
        print(f"\n=== 하이브리드 추천 (사용자 ID: {target_user_id}) ===")

        # 각 알고리즘별 추천 결과
        recommendations = {}

        # 1. 협업 필터링 (가중치: 0.4)
        collab_user = self.collaborative_filtering_user_based(target_user_id, n_recommendations * 2)
        for item in collab_user:
            item_id = item['item_id']
            if item_id not in recommendations:
                recommendations[item_id] = {'score': 0, 'reasons': [], 'item_info': item}
            recommendations[item_id]['score'] += item['recommendation_score'] * 0.4
            recommendations[item_id]['reasons'].append('협업필터링')

        # 2. 아이템 기반 (가중치: 0.3)
        collab_item = self.collaborative_filtering_item_based(target_user_id, n_recommendations * 2)
        for item in collab_item:
            item_id = item['item_id']
            if item_id not in recommendations:
                recommendations[item_id] = {'score': 0, 'reasons': [], 'item_info': item}
            recommendations[item_id]['score'] += item['recommendation_score'] * 0.3
            recommendations[item_id]['reasons'].append('유사상품')

        # 3. 콘텐츠 기반 (가중치: 0.3)
        content_based = self.content_based_filtering(target_user_id, n_recommendations * 2)
        for item in content_based:
            item_id = item['item_id']
            if item_id not in recommendations:
                recommendations[item_id] = {'score': 0, 'reasons': [], 'item_info': item}
            recommendations[item_id]['score'] += item['recommendation_score'] * 0.3
            recommendations[item_id]['reasons'].append('선호스타일')

        # 최종 추천 리스트 생성
        final_recommendations = sorted(
            recommendations.items(),
            key=lambda x: x[1]['score'],
            reverse=True
        )[:n_recommendations]

        # 결과 포맷팅
        result = []
        for item_id, rec_data in final_recommendations:
            item_info = rec_data['item_info']
            item_info['recommendation_score'] = float(rec_data['score'])
            item_info['recommendation_reason'] = ' + '.join(rec_data['reasons'])
            result.append(item_info)

        print(f"✅ 하이브리드 추천 완료: {len(result)}개")
        return result

    def get_popular_items(self, n_recommendations=10):
        """인기 상품 추천 (콜드 스타트 대응)"""
        if self.items_df is None or self.items_df.empty:
            return []

        # 인기도 점수 기반으로 정렬
        popularity_col = 'popularity_score' if 'popularity_score' in self.items_df.columns else 'item_rating'
        popular_items = self.items_df.nlargest(n_recommendations, popularity_col)

        result = []
        for _, item in popular_items.iterrows():
            item_info = {
                'item_id': int(item['item_id']),
                'item_name': str(item['item_name']),
                'price': float(item['price']),
                'category': str(item.get('category', '')),
                'sub_category': str(item.get('sub_category', '') if pd.notna(item.get('sub_category')) else ''),
                'gender': str(item.get('gender', '') if pd.notna(item.get('gender')) else ''),
                'style': str(item.get('style', '') if pd.notna(item.get('style')) else ''),
                'item_rating': float(item.get('item_rating', 0)),
                'popularity_score': float(item.get('popularity_score', item.get('item_rating', 0))),
                'recommendation_score': float(item.get('popularity_score', item.get('item_rating', 0))),
                'recommendation_reason': '인기 상품',
                'image_url': str(item.get('image_url', '') if pd.notna(item.get('image_url')) else ''),
                'description': str(item.get('description', '') if pd.notna(item.get('description')) else '')
            }
            result.append(item_info)

        return result

    def get_item_info(self, item_id):
        """상품 정보 조회"""
        if self.items_df is None:
            return None

        item = self.items_df[self.items_df['item_id'] == item_id]
        if len(item) == 0:
            return None

        item = item.iloc[0]

        return {
            'item_id': int(item['item_id']),  # numpy int64 -> Python int
            'item_name': str(item['item_name']),
            'price': float(item['price']),
            'category': str(item.get('category', '')),
            'sub_category': str(item.get('sub_category', '') if pd.notna(item.get('sub_category')) else ''),
            'gender': str(item.get('gender', '') if pd.notna(item.get('gender')) else ''),
            'style': str(item.get('style', '') if pd.notna(item.get('style')) else ''),
            'item_rating': float(item.get('item_rating', 0)),
            'popularity_score': float(item.get('popularity_score', 0)),
            'image_url': str(item.get('image_url', '') if pd.notna(item.get('image_url')) else ''),
            'description': str(item.get('description', '') if pd.notna(item.get('description')) else '')
        }

    def get_user_interactions(self, user_id):
        """사용자 상호작용 상품 목록"""
        if self.interactions_df is None:
            return []

        user_interactions = self.interactions_df[self.interactions_df['user_id'] == user_id]
        return user_interactions['product_id'].unique().tolist()

    def real_time_recommendation(self, user_id, session_data=None, n_recommendations=5):
        """실시간 추천 (세션 기반)"""
        print(f"\n=== 실시간 추천 (사용자 ID: {user_id}) ===")

        if self.redis_client is None:
            return self.hybrid_recommendation(user_id, n_recommendations)

        try:
            # Redis에서 실시간 데이터 가져오기
            recent_views = self.redis_client.lrange(f"member:{user_id}:recent_views", 0, 9)
            current_cart = self.redis_client.hgetall(f"cart:member:{user_id}")

            # 최근 조회 상품 기반 추천
            if recent_views:
                viewed_items = []
                for view_data in recent_views:
                    try:
                        view_item = json.loads(view_data)
                        viewed_items.append(view_item['item_id'])
                    except:
                        continue

                if viewed_items:
                    # 최근 조회 상품과 유사한 상품 추천
                    similar_recommendations = self.get_similar_items_to_list(
                        viewed_items, n_recommendations
                    )

                    for item in similar_recommendations:
                        item['recommendation_reason'] = '최근 조회 상품과 유사'

                    print(f"✅ 실시간 추천 완료: {len(similar_recommendations)}개")
                    return similar_recommendations

            # 실시간 데이터가 없으면 하이브리드 추천으로 fallback
            return self.hybrid_recommendation(user_id, n_recommendations)

        except Exception as e:
            print(f"❌ 실시간 추천 오류: {e}")
            return self.hybrid_recommendation(user_id, n_recommendations)

    def get_similar_items_to_list(self, item_ids, n_recommendations=5):
        """특정 상품들과 유사한 상품 추천"""
        if not item_ids or self.items_df is None:
            return []

        # 기준 상품들의 특성 가져오기
        base_items = self.items_df[self.items_df['item_id'].isin(item_ids)]

        if len(base_items) == 0:
            return self.get_popular_items(n_recommendations)

        # 콘텐츠 특성 기반 유사도 계산
        item_features = self.build_item_content_features()

        # 기준 상품들의 평균 특성 벡터
        base_features = item_features.loc[base_items['item_id']]
        avg_features = base_features.mean()

        # 모든 상품과의 유사도 계산
        similarities = cosine_similarity([avg_features], item_features)[0]
        similarity_scores = pd.Series(similarities, index=item_features.index)

        # 기준 상품들 제외
        similarity_scores = similarity_scores.drop(item_ids, errors='ignore')

        # 상위 유사 상품들
        top_similar = similarity_scores.sort_values(ascending=False)[:n_recommendations]

        result = []
        for item_id, score in top_similar.items():
            item_info = self.get_item_info(item_id)
            if item_info is not None:
                item_info['recommendation_score'] = float(score)
                result.append(item_info)

        return result

    def category_based_recommendation(self, category, n_recommendations=10):
        """카테고리 기반 추천"""
        if self.items_df is None:
            return []

        category_items = self.items_df[self.items_df['category'] == category]

        if len(category_items) == 0:
            return []

        # 카테고리 내 인기 상품
        popularity_col = 'popularity_score' if 'popularity_score' in category_items.columns else 'item_rating'
        popular_in_category = category_items.nlargest(n_recommendations, popularity_col)

        result = []
        for _, item in popular_in_category.iterrows():
            item_info = self.get_item_info(int(item['item_id']))  # numpy int64 -> Python int
            if item_info is not None:
                item_info['recommendation_reason'] = f'{category} 카테고리 인기 상품'
                result.append(item_info)

        return result

    def save_recommendation_models(self):
        """추천 모델 저장"""
        try:
            # 여기서는 간단히 설정만 저장
            model_config = {
                'algorithm_weights': {
                    'collaborative_user': 0.4,
                    'collaborative_item': 0.3,
                    'content_based': 0.3
                },
                'default_recommendations': 10,
                'similarity_threshold': 0.1
            }

            save_path = self.config.get_file_path(
                'recommendation_models.json') if self.config else 'recommendation_models.json'

            with open(save_path, 'w', encoding='utf-8') as f:
                json.dump(model_config, f, ensure_ascii=False, indent=2)

            print("✅ 추천 모델 설정 저장 완료")

        except Exception as e:
            print(f"❌ 모델 저장 실패: {e}")

    def evaluate_recommendations(self, test_users=None, n_test=10):
        """추천 시스템 성능 평가"""
        print("\n=== 추천 시스템 성능 평가 ===")

        if self.interactions_df is None or len(self.interactions_df) == 0:
            print("평가할 상호작용 데이터가 없습니다.")
            return {}

        if test_users is None:
            # 상호작용이 있는 사용자 중 랜덤 선택
            active_users = self.interactions_df['user_id'].value_counts()
            test_users = active_users[active_users >= 5].index[:n_test].tolist()

        results = {
            'user_based': [],
            'item_based': [],
            'content_based': [],
            'hybrid': []
        }

        for user_id in test_users:
            print(f"평가 중: 사용자 {user_id}")

            # 각 알고리즘별 추천 결과
            user_recs = self.collaborative_filtering_user_based(user_id, 5)
            item_recs = self.collaborative_filtering_item_based(user_id, 5)
            content_recs = self.content_based_filtering(user_id, 5)
            hybrid_recs = self.hybrid_recommendation(user_id, 5)

            results['user_based'].append(len(user_recs))
            results['item_based'].append(len(item_recs))
            results['content_based'].append(len(content_recs))
            results['hybrid'].append(len(hybrid_recs))

        # 평가 결과 요약
        print(f"\n📊 평가 결과 ({len(test_users)}명 대상):")
        for method, scores in results.items():
            avg_recommendations = np.mean(scores) if scores else 0
            print(f"  {method}: 평균 {avg_recommendations:.1f}개 추천")

        return results


# 실행 함수
def run_sonstar_recommendation_system():
    """SonStar 추천 시스템 실행"""

    # 1. 추천 시스템 초기화
    recommender = SonStarRecommendationSystem()

    # 2. 전처리된 데이터 로드
    if not recommender.load_preprocessed_data():
        print("데이터 로드 실패. 프로그램을 종료합니다.")
        return

    print(f"\n🎯 SonStar 추천 시스템 준비 완료!")

    # 3. 샘플 사용자에 대한 추천 테스트
    if recommender.members_df is not None and len(recommender.members_df) > 0:
        test_user_id = recommender.members_df['member_id'].iloc[0]

        print(f"\n🧪 샘플 추천 테스트 (사용자 ID: {test_user_id})")

        # 각 알고리즘별 추천 결과
        user_based = recommender.collaborative_filtering_user_based(test_user_id, 5)
        item_based = recommender.collaborative_filtering_item_based(test_user_id, 5)
        content_based = recommender.content_based_filtering(test_user_id, 5)
        hybrid = recommender.hybrid_recommendation(test_user_id, 5)
        real_time = recommender.real_time_recommendation(test_user_id, n_recommendations=5)

        print(f"\n📋 추천 결과 요약:")
        print(f"  사용자 기반: {len(user_based)}개")
        print(f"  아이템 기반: {len(item_based)}개")
        print(f"  콘텐츠 기반: {len(content_based)}개")
        print(f"  하이브리드: {len(hybrid)}개")
        print(f"  실시간: {len(real_time)}개")

    # 4. 성능 평가
    recommender.evaluate_recommendations()

    # 5. 모델 저장
    recommender.save_recommendation_models()

    print(f"\n🎉 SonStar 추천 시스템 구축 완료!")
    return recommender


# 사용 예시
if __name__ == "__main__":
    recommender = run_sonstar_recommendation_system()

    # 특정 사용자에 대한 추천 예시
    if recommender and recommender.members_df is not None:
        user_id = 2  # 예시 사용자 ID
        recommendations = recommender.hybrid_recommendation(user_id, 10)

        print(f"\n🎁 사용자 {user_id}에 대한 추천 상품:")
        for i, item in enumerate(recommendations, 1):
            print(f"  {i}. {item['item_name']} - {item['price']:,}원")
            print(f"     이유: {item['recommendation_reason']}")
            print(f"     점수: {item['recommendation_score']:.3f}")
            print()