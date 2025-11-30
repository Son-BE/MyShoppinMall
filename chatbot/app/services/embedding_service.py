from sentence_transformers import SentenceTransformer
from sqlalchemy import text
from app.database import MySQLSession, PostgresSession
import numpy as np

class EmbeddingService:
    def __init__(self):
        # 한국어 지원 임베딩 모델
        self.model = SentenceTransformer('jhgan/ko-sroberta-multitask')

    def create_item_text(self, item: dict) -> str:
        """상품 정보를 임베딩용 텍스트로 변환"""
        parts = [
            item.get('item_name',''),
            item.get('category',''),
            item.get('sub_category',''),
            item.get('gender',''),
            item.get('age_group',''),
            item.get('style',''),
            item.get('season',''),
            item.get('primary_color',''),
            item.get('secondary_color',''),
            item.get('item_comment', '') or ''
        ]
        return ' '.join(filter(None, parts))

    def generate_embedding(self, text: str) -> list:
        """텍스트를 벡터로 변환"""
        embedding = self.model.encode(text)
        return embedding.tolist()

    def sync_all_items(self):
        """MySQL의 모든 상품을 PostgresSQL에 임베딩으로 저장"""
        mysql_session = MySQLSession()
        postgres_session = PostgresSession()

        try:
            # MySQL에서 상품 조회
            result = mysql_session.execute(text("""
                SELECT id, item_name, category, sub_category, gender, 
                       age_group, style, season, primary_color, 
                       secondary_color, item_comment
                FROM item
                WHERE delete_type != 'Y' OR delete_type IS NULL
                """))

            items = result.fetchall()
            colums = result.keys()

            synced_count = 0
            for row in items:
                item = dict(zip(colums, row))

                # 임베딩 생성
                item_text = self.create_item_text(item)
                embedding = self.generate_embedding(item_text)

                # PostgresSQL에 저장
                postgres_session.execute(text("""
                    INSERT INTO product_embeddings 
                        (product_id, product_name, category, embedding)
                    VALUES 
                        (:product_id, :product_name, :category, :embedding)
                    ON CONFLICT (product_id) 
                    DO UPDATE SET 
                        product_name = :product_name,
                        category = :category,
                        embedding = :embedding,
                        updated_at = NOW()
                """), {
                    'product_id': item['id'],
                    'product_name': item['item_name'],
                    'category': item['category'],
                    'embedding': str(embedding)
                })

                synced_count += 1
                if synced_count % 100 == 0:
                    print(f"{synced_count}개 상품 처리 완료")

            postgres_session.commit()
            print(f"총 {synced_count}개 상품 임베딩 완료!")
            return synced_count

        finally:
            mysql_session.close()
            postgres_session.close()

embedding_service = EmbeddingService()


