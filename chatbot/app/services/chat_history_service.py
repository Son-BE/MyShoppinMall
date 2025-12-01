from sqlalchemy import text
from app.database import PostgresSession

class ChatHistoryService:
    def save_message(self, session_id: str, role: str, content: str):
        """메시지 저장"""
        postgres_session = PostgresSession()
        try:
            # 세션이 없으면 생성
            postgres_session.execute(text("""
                INSERT INTO chat_sessions (session_id)
                VALUES (:session_id)
                ON CONFLICT (session_id) DO UPDATE SET updated_at = NOW()
            """), {'session_id': session_id})

            # 메시지 저장
            postgres_session.execute(text("""
                INSERT INTO chat_messages (session_id, role, content)
                VALUES (:session_id, :role, :content)
            """), {
                'session_id': session_id,
                'role': role,
                'content': content
            })

            postgres_session.commit()
        finally:
            postgres_session.close()

    def get_history(self, session_id: str, limit: int = 10) -> list:
        """최근 대화 히스토리 조회"""
        postgres_session = PostgresSession()
        try:
            result = postgres_session.execute(text("""
                SELECT role, content, created_at
                FROM chat_messages
                WHERE session_id = :session_id
                ORDER BY created_at DESC
                LIMIT :limit
            """), {'session_id': session_id, 'limit': limit})

            messages = []
            for row in result.fetchall():
                messages.append({
                    'role': row[0],
                    'content': row[1],
                    'created_at': str(row[2])
                })

            # 시간순 정렬
            return list(reversed(messages))
        finally:
            postgres_session.close()

chat_history_service = ChatHistoryService()