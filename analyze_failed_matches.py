#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
分析正则匹配失败记录的脚本
"""

import csv
import re
import os
from collections import Counter, defaultdict
from pathlib import Path

def analyze_failed_matches(csv_file_path):
    """分析正则匹配失败的记录"""
    
    if not os.path.exists(csv_file_path):
        print(f"文件不存在: {csv_file_path}")
        return
    
    print(f"开始分析文件: {csv_file_path}")
    
    # 统计数据
    total_records = 0
    failed_records = 0
    file_extensions = Counter()
    name_patterns = Counter()
    common_words = Counter()
    
    # 按扩展名分组失败记录
    failed_by_extension = defaultdict(list)
    
    # 正则表达式模式
    movie_patterns = [
        r'.*?(\d{4}).*?',  # 年份
        r'.*?(S\d{1,2}E\d{1,2}).*?',  # S01E01
        r'.*?(\d{1,2}x\d{1,2}).*?',  # 1x01
        r'.*?第[一二三四五六七八九十\d]+季.*?',  # 第X季
        r'.*?第[一二三四五六七八九十\d]+集.*?',  # 第X集
        r'.*?EP\d{1,3}.*?',  # EP001
        r'.*?Episode\s*\d{1,3}.*?',  # Episode 1
        r'.*?Part\s*\d+.*?',  # Part 1
        r'.*?CD\d+.*?',  # CD1
    ]
    
    try:
        with open(csv_file_path, 'r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            
            for row in reader:
                total_records += 1
                
                # 检查是否是正则匹配失败记录
                if 'reg_match_fail' in row.get('status', '').lower():
                    failed_records += 1
                    filename = row.get('filename', '')
                    
                    if filename:
                        # 提取文件扩展名
                        ext = Path(filename).suffix.lower()
                        file_extensions[ext] += 1
                        failed_by_extension[ext].append(filename)
                        
                        # 提取文件名（不含扩展名）
                        name_without_ext = Path(filename).stem
                        name_patterns[name_without_ext] += 1
                        
                        # 提取常见词汇
                        words = re.findall(r'[\w\u4e00-\u9fff]+', name_without_ext)
                        for word in words:
                            if len(word) > 1:  # 忽略单字符
                                common_words[word.lower()] += 1
                        
                        print(f"失败文件: {filename}")
    
    except Exception as e:
        print(f"读取文件时出错: {e}")
        return
    
    print(f"\n=== 分析结果 ===")
    print(f"总记录数: {total_records}")
    print(f"正则匹配失败数: {failed_records}")
    print(f"失败率: {failed_records/total_records*100:.2f}%")
    
    print(f"\n=== 按扩展名统计 ===")
    for ext, count in file_extensions.most_common():
        print(f"{ext}: {count} 个文件")
    
    print(f"\n=== 最常见的失败文件名模式 ===")
    for pattern, count in name_patterns.most_common(10):
        print(f"{pattern}: {count} 次")
    
    print(f"\n=== 最常见的词汇 ===")
    for word, count in common_words.most_common(20):
        print(f"{word}: {count} 次")
    
    # 分析特定扩展名的失败模式
    print(f"\n=== 详细分析特定扩展名的失败模式 ===")
    for ext in ['.mp4', '.mkv', '.avi', '.mov', '.wmv']:
        if ext in failed_by_extension:
            print(f"\n{ext} 文件失败模式:")
            files = failed_by_extension[ext][:10]  # 只显示前10个
            for file in files:
                print(f"  - {file}")
                
                # 尝试用各种模式匹配
                name_without_ext = Path(file).stem
                matched = False
                for i, pattern in enumerate(movie_patterns):
                    if re.search(pattern, name_without_ext, re.IGNORECASE):
                        print(f"    匹配模式 {i+1}: {pattern}")
                        matched = True
                        break
                
                if not matched:
                    print(f"    未匹配任何已知模式")

if __name__ == "__main__":
    csv_file = "analog/result.csv"
    analyze_failed_matches(csv_file)