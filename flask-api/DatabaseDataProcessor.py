# # flask-api/DatabaseDataProcessor.py
# import pymysql
# import redis
# import pandas as pd
# from sqlalchemy import create_engine, text
# import json
# import os
# from datetime import datetime, timedelta
# import numpy as np
# from sklearn.preprocessing import LabelEncoder, StandardScaler
# from sklearn.feature_extraction.text import TfidfVectorizer
# from sklearn.decomposition import LatentDirichletAllocation
# import re
# import warnings
# warnings.filterwarnings('ignore')
#
# # NLP 관련 라이브러리
# try:
#     from konlpy.tag import Okt, Mecab
#     KONLPY_AVAILABLE = True
# except ImportError:
#     print("⚠️ KoNLPy가 설치되어 있지 않습니다. 기본 텍스트 처리를 사용합니다.")
#     KONLPY_AVAILABLE = False
#
# try:
#     from wordcloud import WordCloud
#     WORDCLOUD_AVAILABLE = True
# except ImportError:
#     print("⚠️ WordCloud가 설치되어 있지 않습니다. 워드클라우드 기능을 사용할 수 없습니다.")
#     WORDCLOUD_AVAILABLE = False
#
# class SonStarDataProcessor:
#     def __init__(self, mysql_config=None, redis_config=None):
#         # Config 임포트를 여기서 수행 (순환 참조 방지)
#         try:
#             from config import config
#             self.config = config
#         except ImportError:
#             print("⚠️ config 모듈을 불러올 수 없습니다. 기본 설정을 사용합니다.")
#             self.config = None
#
#         # 기본 Docker 환경 설정
#         if mysql_config is None:
#             if self.config:
#                 mysql_config = {
#                     'host': self.config.MYSQL_HOST,
#                     'port': self.config.MYSQL_PORT,
#                     'user': self.config.MYSQL_USER,
#                     'password': self.config.MYSQL_PASSWORD,
#                     'database': self.config.MYSQL_DATABASE,
#                     'charset': 'utf8mb4'
#                 }
#             else:
#                 mysql_config = {
#                     'host': 'localhost',
#                     'port': 3306,
#                     'user': 'root',
#                     'password': 'hi092787!!!',
#                     'database': 'SonStar',
#                     'charset': 'utf8mb4'
#                 }
#
#         if redis_config is None:
#             if self.config:
#                 redis_config = {
#                     'host': self.config.REDIS_HOST,
#                     'port': self.config.REDIS_PORT,
#                     'db': self.config.REDIS_DB,
#                     'decode_responses': True
#                 }
#             else:
#                 redis_config = {
#                     'host': 'localhost',
#                     'port': 6379,
#                     'db': 0,
#                     'decode_responses': True
#                 }
#
#         # MySQL 연결
#         try:
#             self.mysql_engine = create_engine(
#                 f"mysql+pymysql://{mysql_config['user']}:{mysql_config['password']}@"
#                 f"{mysql_config['host']}:{mysql_config['port']}/{mysql_config['database']}"
#             )
#             print("✅ MySQL 연결 성공")
#         except Exception as e:
#             print(f"❌ MySQL 연결 실패: {e}")
#             self.mysql_engine = None
#
#         # Redis 연결
#         try:
#             self.redis_client = redis.Redis(
#                 host=redis_config['host'],
#                 port=redis_config['port'],
#                 db=redis_config['db'],
#                 decode_responses=True
#             )
#             # 연결 테스트
#             self.redis_client.ping()
#             print("✅ Redis 연결 성공")
#         except Exception as e:
#             print(f"❌ Redis 연결 실패: {e}")
#             self.redis_client = None
#
#         # 인코더 초기화
#         self.label_encoders = {}
#         self.scaler = StandardScaler()
#
#         # NLP 관련 초기화
#         self.init_nlp_components()
#
#     def init_nlp_components(self):
#         """NLP 컴포넌트 초기화"""
#         # 한국어 형태소 분석기 초기화
#         if KONLPY_AVAILABLE:
#             try:
#                 self.mecab = Mecab()
#                 self.nlp_engine = 'mecab'
#                 print("✅ Mecab 형태소 분석기 초기화 완료")
#             except:
#                 try:
#                     self.okt = Okt()
#                     self.nlp_engine = 'okt'
#                     print("✅ Okt 형태소 분석기 초기화 완료")
#                 except:
#                     self.nlp_engine = 'basic'
#                     print("⚠️ 형태소 분석기 초기화 실패. 기본 처리를 사용합니다.")
#         else:
#             self.nlp_engine = 'basic'
#
#         # 한국어 불용어 리스트
#         self.korean_stopwords = {
#             '이', '그', '저', '것', '수', '들', '등', '및', '또한', '그리고', '하지만', '그러나',
#             '때문', '위해', '통해', '대해', '있는', '없는', '같은', '다른', '많은', '작은', '큰',
#             '좋은', '나쁜', '새로운', '오래된', '이런', '저런', '그런', '어떤', '모든', '각',
#             '몇', '여러', '다양한', '특별한', '일반적인', '기본적인', '주요', '중요한', '필요한',
#             '가능한', '불가능한', '최고', '최저', '최대', '최소', '항상', '절대', '가끔', '때때로'
#         }
#
#         # 패션/쇼핑 관련 불용어
#         self.fashion_stopwords = {
#             '상품', '제품', '아이템', '옷', '의류', '패션', '스타일', '디자인', '브랜드',
#             '사이즈', '컬러', '색상', '소재', '재질', '원단', '가격', '할인', '세일',
#             '무료', '배송', '당일', '빠른', '즉시', '바로', '신상', '인기', '베스트',
#             '추천', '특가', '한정', '이벤트', '쿠폰', '적립', '혜택', '서비스'
#         }
#
#         self.all_stopwords = self.korean_stopwords.union(self.fashion_stopwords)
#
#         # TF-IDF 벡터라이저 초기화
#         self.tfidf_vectorizer = TfidfVectorizer(
#             max_features=1000,
#             min_df=2,
#             max_df=0.8,
#             ngram_range=(1, 2),
#             stop_words=None  # 한국어는 직접 처리
#         )
#
#     def extract_korean_keywords(self, text, top_k=10):
#         """한국어 텍스트에서 키워드 추출"""
#         if not text or pd.isna(text):
#             return []
#
#         # 텍스트 정제
#         text = str(text).strip()
#         if not text:
#             return []
#
#         # 특수문자 및 숫자 제거 (한글, 영문, 공백만 유지)
#         text = re.sub(r'[^가-힣a-zA-Z\s]', ' ', text)
#         text = re.sub(r'\s+', ' ', text)
#
#         if self.nlp_engine == 'mecab':
#             return self._extract_keywords_mecab(text, top_k)
#         elif self.nlp_engine == 'okt':
#             return self._extract_keywords_okt(text, top_k)
#         else:
#             return self._extract_keywords_basic(text, top_k)
#
#     def _extract_keywords_mecab(self, text, top_k):
#         """Mecab을 사용한 키워드 추출"""
#         try:
#             # 형태소 분석 (명사, 형용사, 영어 단어만 추출)
#             morphs = self.mecab.pos(text)
#             keywords = []
#
#             for word, pos in morphs:
#                 if (pos.startswith('NN') or pos.startswith('VA') or pos.startswith('SL')) and len(word) > 1:
#                     if word.lower() not in self.all_stopwords:
#                         keywords.append(word.lower())
#
#             # 빈도수 계산
#             from collections import Counter
#             word_freq = Counter(keywords)
#             return [word for word, freq in word_freq.most_common(top_k)]
#
#         except Exception as e:
#             print(f"Mecab 키워드 추출 오류: {e}")
#             return self._extract_keywords_basic(text, top_k)
#
#     def _extract_keywords_okt(self, text, top_k):
#         """Okt를 사용한 키워드 추출"""
#         try:
#             # 명사 추출
#             nouns = self.okt.nouns(text)
#             # 형용사 추출
#             adjectives = [word for word, pos in self.okt.pos(text) if pos == 'Adjective']
#
#             keywords = []
#             for word in nouns + adjectives:
#                 if len(word) > 1 and word.lower() not in self.all_stopwords:
#                     keywords.append(word.lower())
#
#             # 빈도수 계산
#             from collections import Counter
#             word_freq = Counter(keywords)
#             return [word for word, freq in word_freq.most_common(top_k)]
#
#         except Exception as e:
#             print(f"Okt 키워드 추출 오류: {e}")
#             return self._extract_keywords_basic(text, top_k)
#
#     def _extract_keywords_basic(self, text, top_k):
#         """기본 키워드 추출 (형태소 분석기 없이)"""
#         # 단순 공백 기반 단어 분리
#         words = text.lower().split()
#         keywords = []
#
#         for word in words:
#             # 길이 2 이상, 불용어가 아닌 단어만
#             if len(word) > 1 and word not in self.all_stopwords:
#                 keywords.append(word)
#
#         # 빈도수 계산
#         from collections import Counter
#         word_freq = Counter(keywords)
#         return [word for word, freq in word_freq.most_common(top_k)]
#
#     def extract_tfidf_keywords(self, texts, top_k=10):
#         """TF-IDF를 사용한 키워드 추출"""
#         try:
#             # 전처리된 텍스트로 TF-IDF 계산
#             processed_texts = []
#             for text in texts:
#                 keywords = self.extract_korean_keywords(text, top_k=50)
#                 processed_texts.append(' '.join(keywords))
#
#             # 빈 텍스트 처리
#             processed_texts = [text if text.strip() else '빈텍스트' for text in processed_texts]
#
#             # TF-IDF 계산
#             tfidf_matrix = self.tfidf_vectorizer.fit_transform(processed_texts)
#             feature_names = self.tfidf_vectorizer.get_feature_names_out()
#
#             # 각 문서별 상위 키워드 추출
#             doc_keywords = []
#             for doc_idx in range(tfidf_matrix.shape[0]):
#                 tfidf_scores = tfidf_matrix[doc_idx].toarray()[0]
#                 word_scores = list(zip(feature_names, tfidf_scores))
#                 word_scores.sort(key=lambda x: x[1], reverse=True)
#
#                 # 상위 키워드만 선택
#                 keywords = [word for word, score in word_scores[:top_k] if score > 0]
#                 doc_keywords.append(keywords)
#
#             return doc_keywords
#
#         except Exception as e:
#             print(f"TF-IDF 키워드 추출 오류: {e}")
#             # 기본 키워드 추출로 fallback
#             return [self.extract_korean_keywords(text, top_k) for text in texts]
#
#     def analyze_category_keywords(self, items_df):
#         """카테고리별 키워드 분석"""
#         print("\n=== 카테고리별 키워드 분석 시작 ===")
#
#         category_keywords = {}
#
#         for category in items_df['category'].unique():
#             if pd.isna(category):
#                 continue
#
#             category_items = items_df[items_df['category'] == category]
#             category_texts = []
#
#             for _, item in category_items.iterrows():
#                 text = f"{item['item_name']} {item.get('description', '')}"
#                 category_texts.append(text)
#
#             # 카테고리별 키워드 추출
#             if category_texts:
#                 keywords = self.extract_tfidf_keywords(category_texts, top_k=15)
#                 # 모든 키워드를 합치고 빈도수 계산
#                 all_keywords = [kw for doc_kws in keywords for kw in doc_kws]
#
#                 from collections import Counter
#                 keyword_freq = Counter(all_keywords)
#                 category_keywords[category] = dict(keyword_freq.most_common(10))
#
#                 print(f"  {category}: {list(keyword_freq.keys())[:5]}")
#
#         return category_keywords
#
#     def extract_search_insights(self, redis_data):
#         """검색 키워드 인사이트 추출"""
#         search_keywords = redis_data.get('search_keywords', [])
#
#         if not search_keywords:
#             return {}
#
#         # 검색 키워드 분석
#         keyword_insights = {
#             'popular_searches': [],
#             'trending_keywords': [],
#             'search_patterns': {}
#         }
#
#         # 인기 검색어 (빈도수 기준)
#         keyword_insights['popular_searches'] = search_keywords[:20]
#
#         # 검색어에서 키워드 추출
#         search_texts = [kw[0] for kw in search_keywords]
#         extracted_keywords = []
#
#         for search_text in search_texts:
#             keywords = self.extract_korean_keywords(search_text, top_k=3)
#             extracted_keywords.extend(keywords)
#
#         # 트렌딩 키워드
#         from collections import Counter
#         keyword_freq = Counter(extracted_keywords)
#         keyword_insights['trending_keywords'] = keyword_freq.most_common(15)
#
#         print(f"✅ 인기 검색어: {len(search_keywords)}개 분석 완료")
#
#         return keyword_insights
#
#     def check_table_schema(self, table_name):
#         """테이블 스키마 확인"""
#         try:
#             with self.mysql_engine.connect() as conn:
#                 result = conn.execute(text(f"DESCRIBE {table_name}"))
#                 columns = [row[0] for row in result.fetchall()]
#                 print(f"📋 {table_name} 테이블 컬럼: {columns}")
#                 return columns
#         except Exception as e:
#             print(f"❌ {table_name} 테이블 스키마 확인 실패: {e}")
#             return []
#
#     def extract_data_from_mysql(self):
#         """MySQL에서 SonStar 쇼핑몰 데이터 추출 (스키마 확인 후)"""
#
#         if self.mysql_engine is None:
#             print("MySQL 연결이 없습니다.")
#             return None, None, None
#
#         print("데이터베이스 스키마 확인 중...")
#
#         # 주요 테이블들의 스키마 확인
#         cart_columns = self.check_table_schema('cart')
#         cart_item_columns = self.check_table_schema('cart_item')
#         orders_columns = self.check_table_schema('orders')
#         wish_list_columns = self.check_table_schema('wish_list')
#
#         # 1. 상품 데이터 (Item 엔티티 기반)
#         items_query = """
#         SELECT
#             i.id as item_id,
#             i.item_name,
#             i.price,
#             i.gender,
#             i.category,
#             i.sub_category,
#             i.age_group,
#             i.style,
#             i.season,
#             i.image_url,
#             i.item_comment as description,
#             i.quantity,
#             i.item_rating,
#             i.review_count,
#             i.view_count,
#             i.order_count,
#             i.cart_count,
#             i.created_at,
#             i.updated_at
#         FROM item i
#         WHERE i.delete_type != 'Y' OR i.delete_type IS NULL
#         """
#
#         # 2. 사용자 데이터 (Member 엔티티 기반)
#         members_query = """
#         SELECT
#             m.id as member_id,
#             m.email,
#             m.name,
#             m.nick_name,
#             m.gender,
#             m.role,
#             m.login_type,
#             m.point,
#             m.used_point,
#             m.created_at as signup_date,
#             m.updated_at as last_activity
#         FROM member m
#         WHERE m.delete_type != 'Y' OR m.delete_type IS NULL
#         """
#
#         # 3. 상호작용 데이터 (스키마에 따라 동적으로 구성)
#         interaction_queries = []
#
#         # 주문 데이터
#         if 'created_at' in orders_columns:
#             orders_query = """
#             SELECT
#                 o.member_id as user_id,
#                 od.item_id as product_id,
#                 'purchase' as action_type,
#                 o.created_at as timestamp,
#                 od.quantity,
#                 od.price as unit_price,
#                 NULL as session_id
#             FROM orders o
#             JOIN order_detail od ON o.id = od.order_id
#             WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
#             """
#             interaction_queries.append(orders_query)
#
#         # 장바구니 데이터 (created_at 컬럼 존재 여부 확인)
#         cart_timestamp_col = None
#         if 'created_at' in cart_columns:
#             cart_timestamp_col = 'c.created_at'
#         elif 'updated_at' in cart_columns:
#             cart_timestamp_col = 'c.updated_at'
#         elif 'created_at' in cart_item_columns:
#             cart_timestamp_col = 'ci.created_at'
#         elif 'updated_at' in cart_item_columns:
#             cart_timestamp_col = 'ci.updated_at'
#         else:
#             cart_timestamp_col = 'NOW()'  # 기본값으로 현재 시간 사용
#
#         cart_query = f"""
#         SELECT
#             c.member_id as user_id,
#             ci.item_id as product_id,
#             'cart' as action_type,
#             {cart_timestamp_col} as timestamp,
#             ci.quantity,
#             NULL as unit_price,
#             NULL as session_id
#         FROM cart c
#         JOIN cart_item ci ON c.id = ci.cart_id
#         """
#         interaction_queries.append(cart_query)
#
#         # 위시리스트 데이터
#         if 'created_at' in wish_list_columns:
#             wish_query = """
#             SELECT
#                 w.member_id as user_id,
#                 w.item_id as product_id,
#                 'like' as action_type,
#                 w.created_at as timestamp,
#                 1 as quantity,
#                 NULL as unit_price,
#                 NULL as session_id
#             FROM wish_list w
#             WHERE w.created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
#             """
#             interaction_queries.append(wish_query)
#
#         # UNION ALL로 합치기
#         interactions_query = " UNION ALL ".join(interaction_queries) if interaction_queries else ""
#
#         print("MySQL에서 데이터 추출 중...")
#         try:
#             items_df = pd.read_sql(items_query, self.mysql_engine)
#             members_df = pd.read_sql(members_query, self.mysql_engine)
#
#             if interactions_query:
#                 interactions_df = pd.read_sql(interactions_query, self.mysql_engine)
#             else:
#                 # 빈 DataFrame 생성
#                 interactions_df = pd.DataFrame(columns=['user_id', 'product_id', 'action_type', 'timestamp', 'quantity', 'unit_price', 'session_id'])
#
#             print(f"✅ 상품 데이터: {len(items_df)}개")
#             print(f"✅ 회원 데이터: {len(members_df)}개")
#             print(f"✅ 상호작용 데이터: {len(interactions_df)}개")
#
#             return items_df, members_df, interactions_df
#
#         except Exception as e:
#             print(f"❌ 데이터 추출 실패: {e}")
#             print(f"실행한 쿼리:")
#             print(f"Items: {items_query}")
#             print(f"Members: {members_query}")
#             if interactions_query:
#                 print(f"Interactions: {interactions_query}")
#             return None, None, None
#
#     def extract_data_from_redis(self):
#         """Redis에서 실시간 행동 데이터 추출"""
#
#         if self.redis_client is None:
#             print("Redis 연결이 없습니다.")
#             return {}
#
#         try:
#             # 최근 조회 상품 (사용자별)
#             recent_views = {}
#             view_keys = self.redis_client.keys("member:*:recent_views")
#
#             for key in view_keys:
#                 member_id = key.split(':')[1]
#                 views = self.redis_client.lrange(key, 0, -1)
#                 if views:
#                     recent_views[member_id] = [json.loads(view) for view in views]
#
#             # 실시간 인기 상품 (조회수 기반)
#             popular_items = self.redis_client.zrevrange("popular_items", 0, 100, withscores=True)
#
#             # 실시간 장바구니 데이터
#             cart_data = {}
#             cart_keys = self.redis_client.keys("cart:member:*")
#
#             for key in cart_keys:
#                 member_id = key.split(':')[2]
#                 cart_items = self.redis_client.hgetall(key)
#                 if cart_items:
#                     cart_data[member_id] = cart_items
#
#             # 검색 키워드 로그
#             search_keywords = self.redis_client.zrevrange("search_keywords", 0, 100, withscores=True)
#
#             print(f"✅ Redis 조회 데이터: {len(recent_views)}명")
#             print(f"✅ 인기 상품: {len(popular_items)}개")
#             print(f"✅ 장바구니 데이터: {len(cart_data)}명")
#
#             return {
#                 'recent_views': recent_views,
#                 'popular_items': popular_items,
#                 'cart_data': cart_data,
#                 'search_keywords': search_keywords
#             }
#
#         except Exception as e:
#             print(f"❌ Redis 데이터 추출 실패: {e}")
#             return {}
#
#     def merge_mysql_redis_data(self, mysql_data, redis_data):
#         """MySQL과 Redis 데이터 통합"""
#         items_df, members_df, interactions_df = mysql_data
#
#         if not redis_data:
#             return items_df, members_df, interactions_df
#
#         # Redis 최근 조회 데이터를 DataFrame으로 변환
#         redis_interactions = []
#         for member_id, views in redis_data.get('recent_views', {}).items():
#             for view in views:
#                 redis_interactions.append({
#                     'user_id': int(member_id),
#                     'product_id': view.get('item_id'),
#                     'action_type': 'view',
#                     'timestamp': datetime.fromtimestamp(view.get('timestamp', 0)),
#                     'quantity': 1,
#                     'unit_price': None,
#                     'session_id': view.get('session_id')
#                 })
#
#         if redis_interactions:
#             redis_df = pd.DataFrame(redis_interactions)
#             # MySQL 데이터와 통합 (중복 제거)
#             interactions_df = pd.concat([interactions_df, redis_df]).drop_duplicates(
#                 subset=['user_id', 'product_id', 'action_type'], keep='last'
#             )
#             print(f"✅ Redis 데이터 통합: {len(redis_interactions)}개 추가")
#
#         return items_df, members_df, interactions_df
#
#     def preprocess_items_data(self, items_df):
#         """상품 데이터 전처리 (NLP 키워드 추출 포함)"""
#         print("\n=== 상품 데이터 전처리 시작 ===")
#
#         if items_df.empty:
#             print("상품 데이터가 비어있습니다.")
#             return items_df
#
#         # 1. 결측값 처리
#         items_df['description'] = items_df['description'].fillna('')
#         items_df['image_url'] = items_df['image_url'].fillna('')
#         items_df['item_rating'] = items_df['item_rating'].fillna(0)
#         items_df['review_count'] = items_df['review_count'].fillna(0)
#         items_df['view_count'] = items_df['view_count'].fillna(0)
#         items_df['order_count'] = items_df['order_count'].fillna(0)
#         items_df['cart_count'] = items_df['cart_count'].fillna(0)
#
#         # 2. 가격 이상치 처리
#         items_df['price'] = items_df['price'].clip(lower=1000, upper=10000000)
#
#         # 3. 인기도 점수 계산
#         items_df['popularity_score'] = (
#                 items_df['view_count'] * 1 +
#                 items_df['cart_count'] * 3 +
#                 items_df['order_count'] * 5 +
#                 items_df['item_rating'] * items_df['review_count'] * 2
#         )
#
#         # 4. 가격대 분류
#         items_df['price_range'] = pd.cut(
#             items_df['price'],
#             bins=[0, 30000, 80000, 150000, float('inf')],
#             labels=['저가', '중가', '고가', '프리미엄']
#         )
#
#         # 5. 카테고리 데이터 인코딩
#         categorical_columns = ['gender', 'category', 'sub_category', 'age_group', 'style', 'season']
#
#         for col in categorical_columns:
#             if col in items_df.columns:
#                 # NULL 값을 'UNKNOWN'으로 처리
#                 items_df[col] = items_df[col].fillna('UNKNOWN').astype(str)
#
#                 le = LabelEncoder()
#                 items_df[f'{col}_encoded'] = le.fit_transform(items_df[col])
#                 self.label_encoders[col] = le
#
#         # 6. NLP 키워드 추출 및 텍스트 특성 처리
#         print(" NLP 키워드 추출 중...")
#
#         # 상품명과 설명 결합
#         items_df['combined_text'] = (
#                 items_df['item_name'].fillna('') + ' ' +
#                 items_df['description'].fillna('')
#         )
#
#         # 개별 상품별 키워드 추출
#         items_df['keywords'] = items_df['combined_text'].apply(
#             lambda x: self.extract_korean_keywords(x, top_k=8)
#         )
#
#         # 키워드를 문자열로 변환 (저장용)
#         items_df['keywords_str'] = items_df['keywords'].apply(
#             lambda x: ','.join(x) if x else ''
#         )
#
#         # TF-IDF 기반 상품별 주요 키워드 추출
#         print("TF-IDF 기반 키워드 분석 중...")
#         tfidf_keywords = self.extract_tfidf_keywords(items_df['combined_text'].tolist(), top_k=5)
#         items_df['tfidf_keywords'] = tfidf_keywords
#         items_df['tfidf_keywords_str'] = items_df['tfidf_keywords'].apply(
#             lambda x: ','.join(x) if x else ''
#         )
#
#         # 텍스트 길이 특성
#         items_df['text_length'] = items_df['combined_text'].str.len()
#         items_df['keyword_count'] = items_df['keywords'].apply(len)
#
#         # 브랜드명 추출 (상품명에서)
#         items_df['brand'] = items_df['item_name'].apply(self._extract_brand_name)
#
#         print(f"✅ NLP 키워드 추출 완료")
#         print(f"상품 데이터 전처리 완료: {len(items_df)}개")
#         return items_df
#
#     def _extract_brand_name(self, item_name):
#         """상품명에서 브랜드명 추출 (간단한 휴리스틱)"""
#         if not item_name or pd.isna(item_name):
#             return 'UNKNOWN'
#
#         # 첫 번째 단어를 브랜드로 가정 (개선 가능)
#         words = str(item_name).split()
#         if words:
#             # 영어로 된 첫 번째 단어나 대문자로 시작하는 단어를 브랜드로 간주
#             for word in words:
#                 if re.match(r'^[A-Z][a-zA-Z]+$', word) and len(word) > 2:
#                     return word
#             # 그 외에는 첫 번째 단어
#             return words[0]
#         return 'UNKNOWN'
#
#     def preprocess_members_data(self, members_df):
#         """회원 데이터 전처리"""
#         print("\n=== 회원 데이터 전처리 시작 ===")
#
#         if members_df.empty:
#             print("회원 데이터가 비어있습니다.")
#             return members_df
#
#         # 1. 결측값 처리
#         members_df['point'] = members_df['point'].fillna(0)
#         members_df['used_point'] = members_df['used_point'].fillna(0)
#
#         # 2. 가입 기간 계산
#         members_df['signup_date'] = pd.to_datetime(members_df['signup_date'])
#         members_df['days_since_signup'] = (
#                 datetime.now() - members_df['signup_date']
#         ).dt.days
#
#         # 3. 사용자 등급 분류
#         members_df['user_grade'] = pd.cut(
#             members_df['used_point'],
#             bins=[0, 10000, 50000, 100000, float('inf')],
#             labels=['브론즈', '실버', '골드', '다이아몬드']
#         )
#
#         # 4. 활동 기간 분류
#         members_df['user_segment'] = pd.cut(
#             members_df['days_since_signup'],
#             bins=[0, 30, 90, 365, float('inf')],
#             labels=['신규', '활성', '기존', '장기']
#         )
#
#         # 5. 카테고리 인코딩
#         categorical_columns = ['gender', 'role', 'login_type', 'user_grade', 'user_segment']
#
#         for col in categorical_columns:
#             if col in members_df.columns:
#                 if pd.api.types.is_categorical_dtype(members_df[col]):
#                     if 'UNKNOWN' not in members_df[col].cat.categories:
#                         members_df[col] = members_df[col].cat.add_categories('UNKNOWN')
#                     members_df[col] = members_df[col].fillna('UNKNOWN')
#                 else:
#                     members_df[col] = members_df[col].fillna('UNKNOWN').astype(str)
#
#                 le = LabelEncoder()
#                 members_df[f'{col}_encoded'] = le.fit_transform(members_df[col])
#                 self.label_encoders[f'member_{col}'] = le
#
#         print(f"회원 데이터 전처리 완료: {len(members_df)}개")
#         return members_df
#
#     def preprocess_interactions_data(self, interactions_df):
#         """상호작용 데이터 전처리"""
#         print("\n=== 상호작용 데이터 전처리 시작 ===")
#
#         if interactions_df.empty:
#             print("상호작용 데이터가 비어있습니다.")
#             # 빈 DataFrame 반환
#             user_item_matrix = pd.DataFrame(columns=['user_id', 'product_id', 'final_score', 'interaction_count'])
#             return interactions_df, user_item_matrix
#
#         # 1. 중복 제거
#         interactions_df = interactions_df.drop_duplicates(
#             subset=['user_id', 'product_id', 'action_type'], keep='last'
#         )
#
#         # 2. 행동 가중치 부여
#         action_weights = {
#             'view': 1,
#             'like': 2,
#             'cart': 3,
#             'purchase': 5
#         }
#         interactions_df['weight'] = interactions_df['action_type'].map(action_weights)
#
#         # 3. 시간 기반 가중치 (최근 행동일수록 높은 가중치)
#         interactions_df['timestamp'] = pd.to_datetime(interactions_df['timestamp'])
#         max_date = interactions_df['timestamp'].max()
#         interactions_df['days_ago'] = (max_date - interactions_df['timestamp']).dt.days
#         interactions_df['time_weight'] = np.exp(-interactions_df['days_ago'] / 30)  # 30일 반감기
#
#         # 4. 구매 금액 고려 (구매 행동의 경우)
#         interactions_df['purchase_value'] = (
#                 interactions_df['quantity'].fillna(1) *
#                 interactions_df['unit_price'].fillna(0)
#         )
#
#         # 5. 최종 점수 계산
#         interactions_df['final_score'] = (
#                 interactions_df['weight'] *
#                 interactions_df['time_weight'] *
#                 (1 + interactions_df['purchase_value'] / 100000)  # 구매금액 보정
#         )
#
#         # 6. 사용자-아이템 매트릭스 생성
#         user_item_matrix = interactions_df.groupby(['user_id', 'product_id']).agg({
#             'final_score': 'sum',
#             'action_type': 'count'
#         }).rename(columns={'action_type': 'interaction_count'}).reset_index()
#
#         print(f"상호작용 데이터 전처리 완료: {len(interactions_df)}개")
#         print(f"사용자-아이템 매트릭스: {len(user_item_matrix)}개")
#
#         return interactions_df, user_item_matrix
#
#     def generate_summary_statistics(self, items_df, members_df, interactions_df):
#         """데이터 요약 통계 생성 (NLP 분석 결과 포함)"""
#         print("\n" + "="*60)
#         print("           SonStar 쇼핑몰 데이터 요약 리포트")
#         print("="*60)
#
#         # 상품 통계
#         print(f"\n[상품 현황]")
#         print(f"  총 상품 수: {len(items_df):,}개")
#         if not items_df.empty:
#             print(f"  평균 가격: {items_df['price'].mean():,.0f}원")
#             print(f"  가격 범위: {items_df['price'].min():,} ~ {items_df['price'].max():,}원")
#             print(f"  카테고리 수: {items_df['category'].nunique()}개")
#             print(f"  평균 평점: {items_df['item_rating'].mean():.2f}")
#
#             # 키워드 분석 결과
#             if 'keyword_count' in items_df.columns:
#                 print(f"  평균 키워드 수: {items_df['keyword_count'].mean():.1f}개")
#                 print(f"  평균 텍스트 길이: {items_df['text_length'].mean():.0f}자")
#
#             # 카테고리별 상품 분포
#             category_dist = items_df['category'].value_counts()
#             print(f"\n[카테고리별 상품 분포]")
#             for cat, count in category_dist.head().items():
#                 print(f"  {cat}: {count}개 ({count/len(items_df)*100:.1f}%)")
#
#         # 회원 통계
#         print(f"\n[회원 현황]")
#         print(f"  총 회원 수: {len(members_df):,}명")
#         if not members_df.empty and 'gender' in members_df.columns:
#             gender_dist = members_df['gender'].value_counts()
#             print(f"  성별 분포: {dict(gender_dist)}")
#
#         # 상호작용 통계
#         print(f"\n[활동 현황]")
#         print(f"  총 상호작용: {len(interactions_df):,}개")
#         if not interactions_df.empty:
#             print(f"  활성 사용자: {interactions_df['user_id'].nunique():,}명")
#             print(f"  상호작용된 상품: {interactions_df['product_id'].nunique():,}개")
#
#             action_dist = interactions_df['action_type'].value_counts()
#             print(f"\n[행동 유형별 분포]")
#             for action, count in action_dist.items():
#                 print(f"  {action}: {count:,}개 ({count/len(interactions_df)*100:.1f}%)")
#
#         # NLP 분석 결과
#         if not items_df.empty and 'keywords' in items_df.columns:
#             print(f"\n[NLP 키워드 분석 결과]")
#
#             # 전체 키워드 빈도 분석
#             all_keywords = []
#             for keywords in items_df['keywords']:
#                 if keywords:
#                     all_keywords.extend(keywords)
#
#             if all_keywords:
#                 from collections import Counter
#                 keyword_freq = Counter(all_keywords)
#                 top_keywords = keyword_freq.most_common(10)
#
#                 print(f"  추출된 총 키워드 수: {len(set(all_keywords))}개")
#                 print(f"  상위 키워드: {[kw for kw, freq in top_keywords[:5]]}")
#
#         # 추천 시스템 준비도 평가
#         print(f"\n[추천 시스템 준비도]")
#
#         if not items_df.empty and not members_df.empty and not interactions_df.empty:
#             # 데이터 희소성
#             total_combinations = len(members_df) * len(items_df)
#             actual_interactions = len(interactions_df.groupby(['user_id', 'product_id']).first())
#             sparsity = 1 - (actual_interactions / total_combinations)
#             print(f"  데이터 희소성: {sparsity:.4f} ({sparsity*100:.2f}%)")
#
#             # 최소 상호작용 기준
#             user_interaction_counts = interactions_df.groupby('user_id').size()
#             item_interaction_counts = interactions_df.groupby('product_id').size()
#
#             active_users = (user_interaction_counts >= 3).sum()
#             popular_items = (item_interaction_counts >= 2).sum()
#
#             print(f"  활성 사용자 (3회 이상): {active_users}/{len(members_df)} ({active_users/len(members_df)*100:.1f}%)")
#             print(f"  인기 상품 (2회 이상): {popular_items}/{len(items_df)} ({popular_items/len(items_df)*100:.1f}%)")
#
#             return {
#                 'items_count': len(items_df),
#                 'members_count': len(members_df),
#                 'interactions_count': len(interactions_df),
#                 'sparsity': sparsity,
#                 'active_users_ratio': active_users/len(members_df),
#                 'popular_items_ratio': popular_items/len(items_df),
#                 'total_keywords': len(set(all_keywords)) if 'all_keywords' in locals() and all_keywords else 0
#             }
#         else:
#             print("  데이터가 부족하여 분석할 수 없습니다.")
#             return {
#                 'items_count': len(items_df),
#                 'members_count': len(members_df),
#                 'interactions_count': len(interactions_df),
#                 'sparsity': 1.0,
#                 'active_users_ratio': 0.0,
#                 'popular_items_ratio': 0.0,
#                 'total_keywords': 0
#             }
#
#     def save_preprocessed_data(self, items_df, members_df, interactions_df, user_item_matrix, category_keywords=None, search_insights=None):
#         """전처리된 데이터 저장 (NLP 결과 포함)"""
#         try:
#             # 파일 경로 설정
#             if self.config:
#                 items_path = self.config.get_file_path('preprocessed_items.csv')
#                 members_path = self.config.get_file_path('preprocessed_members.csv')
#                 interactions_path = self.config.get_file_path('preprocessed_interactions.csv')
#                 matrix_path = self.config.get_file_path('user_item_matrix.csv')
#                 keywords_path = self.config.get_file_path('category_keywords.json')
#                 insights_path = self.config.get_file_path('search_insights.json')
#             else:
#                 items_path = 'preprocessed_items.csv'
#                 members_path = 'preprocessed_members.csv'
#                 interactions_path = 'preprocessed_interactions.csv'
#                 matrix_path = 'user_item_matrix.csv'
#                 keywords_path = 'category_keywords.json'
#                 insights_path = 'search_insights.json'
#
#             # CSV 파일로 저장
#             items_df.to_csv(items_path, index=False, encoding='utf-8')
#             members_df.to_csv(members_path, index=False, encoding='utf-8')
#             interactions_df.to_csv(interactions_path, index=False, encoding='utf-8')
#             user_item_matrix.to_csv(matrix_path, index=False, encoding='utf-8')
#
#             # NLP 분석 결과 저장
#             if category_keywords:
#                 with open(keywords_path, 'w', encoding='utf-8') as f:
#                     json.dump(category_keywords, f, ensure_ascii=False, indent=2)
#
#             if search_insights:
#                 with open(insights_path, 'w', encoding='utf-8') as f:
#                     json.dump(search_insights, f, ensure_ascii=False, indent=2)
#
#             print(f"\n✅ 전처리된 데이터 저장 완료!")
#             print(f"  - {items_path}: {len(items_df)}행")
#             print(f"  - {members_path}: {len(members_df)}행")
#             print(f"  - {interactions_path}: {len(interactions_df)}행")
#             print(f"  - {matrix_path}: {len(user_item_matrix)}행")
#
#             if category_keywords:
#                 print(f"  - {keywords_path}: {len(category_keywords)}개 카테고리")
#             if search_insights:
#                 print(f"  - {insights_path}: 검색 인사이트")
#
#         except Exception as e:
#             print(f"❌ 데이터 저장 실패: {e}")
#
# # 실행 함수
# def run_sonstar_data_preprocessing():
#     """SonStar 쇼핑몰 데이터 전처리 실행 (NLP 기능 포함)"""
#
#     # 1. 프로세서 초기화
#     processor = SonStarDataProcessor()
#
#     # 2. MySQL 데이터 추출
#     mysql_data = processor.extract_data_from_mysql()
#     if mysql_data[0] is None:
#         print("MySQL 데이터 추출 실패. 프로그램을 종료합니다.")
#         return
#
#     # 3. Redis 데이터 추출
#     redis_data = processor.extract_data_from_redis()
#
#     # 4. 데이터 통합
#     items_df, members_df, interactions_df = processor.merge_mysql_redis_data(mysql_data, redis_data)
#
#     # 5. 각 데이터별 전처리 (NLP 포함)
#     items_processed = processor.preprocess_items_data(items_df)
#     members_processed = processor.preprocess_members_data(members_df)
#     interactions_processed, user_item_matrix = processor.preprocess_interactions_data(interactions_df)
#
#     # 6. NLP 분석 수행
#     category_keywords = None
#     search_insights = None
#
#     if not items_processed.empty:
#         category_keywords = processor.analyze_category_keywords(items_processed)
#
#     if redis_data:
#         search_insights = processor.extract_search_insights(redis_data)
#
#     # 7. 요약 통계 생성
#     stats = processor.generate_summary_statistics(items_processed, members_processed, interactions_processed)
#
#     # 8. 전처리된 데이터 저장 (NLP 결과 포함)
#     processor.save_preprocessed_data(
#         items_processed, members_processed, interactions_processed, user_item_matrix,
#         category_keywords, search_insights
#     )
#
#     print(f"\n SonStar 쇼핑몰 데이터 전처리 완료! (NLP 키워드 추출 포함)")
#     print(f" NLP 엔진: {processor.nlp_engine}")
#
#     return items_processed, members_processed, interactions_processed, user_item_matrix, category_keywords, search_insights
#
# # 실행 예시
# if __name__ == "__main__":
#     run_sonstar_data_preprocessing()