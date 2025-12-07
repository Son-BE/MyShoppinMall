from sqlalchemy import text
from app.database import PostgresSession, MySQLSession
from app.services.embedding_service import embedding_service


class RetrievalService:
    def search_similar_products(self, query: str, top_k: int = 5):
        """사용자 질문과 유사한 상품 검색"""
        # 질문을 벡터로 변환
        query_embedding = embedding_service.generate_embedding(query)

        postgres_session = PostgresSession()
        mysql_session = MySQLSession()

        try:
            # 벡터를 문자열로 변환
            embedding_str = str(query_embedding)

            # 1. PostgreSQL에서 유사 상품 검색
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
            product_ids = []

            for row in result.fetchall():
                products.append({
                    'product_id': row[0],
                    'product_name': row[1],
                    'category': row[2],
                    'similarity': float(row[3]),
                    'image_url': None
                })
                product_ids.append(row[0])

            # 2. MySQL에서 이미지 URL 조회
            if product_ids:
                ids_str = ','.join(map(str, product_ids))
                mysql_sql = text(f"""
                    SELECT id, image_url 
                    FROM item 
                    WHERE id IN ({ids_str})
                """)
                mysql_result = mysql_session.execute(mysql_sql)

                # 이미지 URL 매핑
                image_map = {row[0]: row[1] for row in mysql_result.fetchall()}

                for product in products:
                    product['image_url'] = image_map.get(product['product_id'])

            return products

        finally:
            postgres_session.close()
            mysql_session.close()


retrieval_service = RetrievalService()