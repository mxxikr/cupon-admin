
import pandas as pd
import os
#!/usr/bin/env python3
"""
Excel 테스트 파일 생성 스크립트
"""

def generate_excel_files():    
    # 작은 테스트 파일 (10명)
    small_customer_ids = [1001 + i for i in range(10)]
    df_small = pd.DataFrame({'customer_id': small_customer_ids})
    df_small.to_excel('customers.xlsx', index=False, sheet_name='Sheet1')
    print("customers.xlsx 생성 완료 (10명)")
    
    # 큰 테스트 파일 (50명)
    large_customer_ids = [1001 + i for i in range(50)]
    df_large = pd.DataFrame({'customer_id': large_customer_ids})
    df_large.to_excel('customers_large.xlsx', index=False, sheet_name='Sheet1')
    print("customers_large.xlsx 생성 완료 (50명)")
    
    # 매우 큰 테스트 파일 (1000명) - 배치 처리 테스트용
    very_large_customer_ids = [1001 + i for i in range(1000)]
    df_very_large = pd.DataFrame({'customer_id': very_large_customer_ids})
    df_very_large.to_excel('customers_very_large.xlsx', index=False, sheet_name='Sheet1')
    print("customers_very_large.xlsx 생성 완료 (1000명)")

if __name__ == '__main__':
    try:
        generate_excel_files()
        print("\n모든 Excel 파일 생성이 완료되었습니다!")
    except ImportError:
        print("오류: pandas와 openpyxl이 필요합니다.")
        print("설치 방법: pip install pandas openpyxl")
    except Exception as e:
        print(f"오류 발생: {e}")