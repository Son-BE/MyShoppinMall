# Spring Boot Enum 구조에 맞춰 변환
spring_mapping = {
    'TOP': {
        'category': 'MENS_TOP',        # or WOMENS_TOP
        'subCategory': 'M_TSHIRT'      # M_SHIRT, M_KNIT 등
    },
    'BOTTOM': {
        'category': 'MENS_BOTTOM',
        'subCategory': 'M_JEANS'       # M_SLACKS 등
    },
    'OUTER': {
        'category': 'MENS_OUTER',
        'subCategory': 'M_JACKET'      # M_COAT 등
    },
    # ... 기타 카테고리
}

# Gender 기반 동적 매핑
gender_prefix = "WOMENS" if gender == "FEMALE" else "MENS"
category = f'{gender_prefix}_{base_category}'