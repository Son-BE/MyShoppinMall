import pandas as pd
import numpy as np
import json
from typing import Any, Dict, List, Union

def clean_data_for_api(data: Any) -> Any:
    """
    pandas/numpy 데이터를 FastAPI/JSON 호환 형태로 변환
    """
    if isinstance(data, dict):
        return {key: clean_data_for_api(value) for key, value in data.items()}
    elif isinstance(data, list):
        return [clean_data_for_api(item) for item in data]
    elif isinstance(data, np.integer):
        return int(data)
    elif isinstance(data, np.floating):
        return float(data) if not np.isnan(data) else None
    elif isinstance(data, np.ndarray):
        return data.tolist()
    elif pd.isna(data):
        return None
    elif isinstance(data, (pd.Timestamp, pd.DatetimeIndex)):
        return str(data)
    else:
        return data

def safe_get_item_info(items_df: pd.DataFrame, item_id: int) -> Dict:
    """
    안전한 상품 정보 조회 (NaN 처리 포함)
    """
    if items_df is None or items_df.empty:
        return None

    item = items_df[items_df['item_id'] == item_id]
    if len(item) == 0:
        return None

    item = item.iloc[0]

    # 기본 정보 추출 (NaN 값 처리)
    item_info = {
        'item_id': int(item['item_id']),
        'item_name': str(item['item_name']) if pd.notna(item['item_name']) else '',
        'price': float(item['price']) if pd.notna(item['price']) else 0.0,
        'category': str(item.get('category', '')) if pd.notna(item.get('category')) else '',
        'sub_category': str(item.get('sub_category', '')) if pd.notna(item.get('sub_category')) else '',
        'gender': str(item.get('gender', '')) if pd.notna(item.get('gender')) else '',
        'style': str(item.get('style', '')) if pd.notna(item.get('style')) else '',
        'item_rating': float(item.get('item_rating', 0)) if pd.notna(item.get('item_rating')) else 0.0,
        'popularity_score': float(item.get('popularity_score', 0)) if pd.notna(item.get('popularity_score')) else 0.0,
        'image_url': str(item.get('image_url', '')) if pd.notna(item.get('image_url')) else '',
        'description': str(item.get('description', '')) if pd.notna(item.get('description')) else ''
    }

    return item_info

def safe_build_recommendation_response(recommendations: List[Dict], algorithm: str = "unknown") -> List[Dict]:
    """
    추천 결과를 안전하게 API 응답 형태로 변환
    """
    safe_recommendations = []

    for rec in recommendations:
        try:
            # 모든 값을 안전하게 변환
            safe_rec = clean_data_for_api(rec)

            # 필수 필드 검증 및 기본값 설정
            safe_rec.setdefault('item_id', 0)
            safe_rec.setdefault('item_name', '')
            safe_rec.setdefault('price', 0.0)
            safe_rec.setdefault('category', '')
            safe_rec.setdefault('sub_category', '')
            safe_rec.setdefault('gender', '')
            safe_rec.setdefault('style', '')
            safe_rec.setdefault('item_rating', 0.0)
            safe_rec.setdefault('popularity_score', 0.0)
            safe_rec.setdefault('recommendation_score', 0.0)
            safe_rec.setdefault('recommendation_reason', algorithm)
            safe_rec.setdefault('image_url', '')
            safe_rec.setdefault('description', '')

            # None 값들을 적절한 기본값으로 변환
            if safe_rec['image_url'] is None:
                safe_rec['image_url'] = ''
            if safe_rec['description'] is None:
                safe_rec['description'] = ''

            safe_recommendations.append(safe_rec)

        except Exception as e:
            print(f"추천 항목 변환 오류: {e}, 항목: {rec}")
            continue

    return safe_recommendations