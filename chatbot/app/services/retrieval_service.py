from sqlalchemy import text
from app.database import PostgresSession
from app.services.embedding_service import embedding_service

class RetrievalService:
    def search_similar_products(self, query: str, top_k: int = 5):
        """사용자 질문과 유사한 상품 검색"""
        # 질문을 벡터로 변환
        query_embedding = embedding_service.generate_embedding(query)

        postgres_session = PostgresSession()
        try:
            # 벡터를 문자열로 변환
            embedding_str = str(query_embedding)

            # 벡터 유사도 검색 (캐스팅을 SQL에 직접 포함)
            sql = text(f"""
                SELECT 
                    product_id,
                    product_name,
                    category,
                    1 - (embedding <=> '{embedding_str}'::vector) as similarity
                FROM product_embeddings
                ORDER BY embedding <=> '{embedding_str}'::vector
                LIMIT :top_k
            """)

            result = postgres_session.execute(sql, {'top_k': top_k})

            products = []
            for row in result.fetchall():
                products.append({
                    'product_id': row[0],
                    'product_name': row[1],
                    'category': row[2],
                    'similarity': float(row[3])
                })

            return products
        finally:
            postgres_session.close()

retrieval_service = RetrievalService()