import psycopg2
from pgvector.psycopg2 import register_vector

conn = psycopg2.connect(
    host="localhost",
    database="chatbot_db",
    user="chatbot",
    password="your_password"
)

register_vector(conn)

cur = conn.cursor()
cur.execute("SELECT * FROM pg_extension WHERE extname = 'vector'")
result = cur.fetchone()

if result:
    print("pgvector 설치 확인 완료!")
else:
    print("pgvector가 설치되지 않았습니다.")

conn.close()