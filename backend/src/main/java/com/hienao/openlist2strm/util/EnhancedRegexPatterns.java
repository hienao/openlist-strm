package com.hienao.openlist2strm.util;

import java.util.Arrays;
import java.util.List;

/**
 * 增强正则表达式模式 基于对125,285个文件名的分析，提供更全面的正则表达式匹配
 *
 * @author hienao
 * @since 2024-01-01
 */
public class EnhancedRegexPatterns {

  /** 获取增强的电影正则表达式列表 原有3个 + 新增9个 = 12个 */
  public static List<String> getEnhancedMovieRegexps() {
    return Arrays.asList(
        // 原有正则表达式
        "^(?<title>.+?)[. _]((?<year>19\\d{2}|20\\d{2}))",
        "^(?<title>.+?)[. _]\\[(?<year>19\\d{2}|20\\d{2})\\]",
        "^(?<title>.+?)[. _]\\((?<year>19\\d{2}|20\\d{2})\\)",

        // 新增正则表达式 - 按优先级排序
        "^(?<title>[\\u4e00-\\u9fff]+.*?)[. _](?:19|20)\\d{2}", // 中文电影名 + 年份
        "^(?<title>.+?)[. _](19|20)\\d{2}[. _](?<title2>.*)$", // 年份在中间
        "^(?<title>[^.]+)[. ]+(?:19|20)\\d{2}", // 多点分隔 + 年份
        "^(?<title>.+?)[. _](?:19|20)\\d{2}$", // 年份在最后
        "^(?<title>.+?)[."
            + " _](?:1080p|720p|480p|2160p|4K|BluRay|DVD|HDTV|BDRip|x264|x265)", // 无年份，质量标记
        "^(?<title>[\\u4e00-\\u9fff]+.+?)(?:\\.(?:mp4|mkv|avi|rmvb))?$", // 中文无年份
        "^\\[(?<title>.+?)\\][. _](?:19|20)\\d{2}", // 带方括号 + 年份
        "^(?<title>.+?)[. _]\\[(.+?)\\](?:[. _](?:19|20)\\d{2})?$" // 带内容，可选年份
        );
  }

  /** 获取增强的电视剧目录正则表达式列表 原有3个 + 新增7个 = 10个 */
  public static List<String> getEnhancedTvDirRegexps() {
    return Arrays.asList(
        // 原有正则表达式
        "^(?<title>.+?)[. _]Season[. _](?<season>\\d{1,2})",
        "^(?<title>.+?)[. _]S(?<season>\\d{1,2})",
        "^(?<title>.+)[. _](?<year>19\\d{2}|20\\d{2})",

        // 新增正则表达式 - 按优先级排序
        "^(?<title>[\\u4e00-\\u9fff]+.*?)[. _]第[一二三四五六七八九十]+季", // 中文季格式
        "^(?<title>.+?)[. _]Season[. _](?<season>\\d{1,2})[. _](?:Part|Disc)?\\d?", // 完整Season +
        // 可选后缀
        "^(?<title>.+?)[. _](?:19|20)\\d{2}[. _]Season[. _](?<season>\\d{1,2})", // 带年份 + Season
        "^(?<title>.+?)[. _]S(?<season>\\d{1,2})(?:EP|EPISODE)?\\d?", // 简写S + 可选EP
        "^(?<title>.+?)[. _](?:S|Season)[. _](?<season>\\d{1,2})(?:EP|EPISODE)?\\d?", // 混合格式
        "^(?<title>.+?)[. _]Season[. _](?<season>\\d{1,2})(?:Episode)?[."
            + " _](?:\\d{1,3})?" // 完整Season + 可选Episode
        );
  }

  /** 获取增强的电视剧文件正则表达式列表 原有3个 + 新增9个 = 12个 */
  public static List<String> getEnhancedTvFileRegexps() {
    return Arrays.asList(
        // 原有正则表达式
        "[._ ]S(?<season>\\d{1,2})E(?<episode>\\d{1,3})",
        "[._ ](?<season>\\d{1,2})x(?<episode>\\d{1,3})",
        "[._ ]Episode[._ ](?<episode>\\d{1,3})",

        // 新增正则表达式 - 按优先级排序
        "[._ ]第[一二三四五六七八九十]+集", // 中文集数格式
        "[._ ]EP(?<episode>\\d{1,3})", // EP格式
        "[._ ]第[一二三四五六七八九十]+集[._ ](?<episode>\\d{1,2})", // 中文集数 + 数字
        "[._ ]S(?<season>\\d{1,2})[._ ]EP?(?<episode>\\d{1,3})", // S + EP格式
        "[._ ]第[一二三四五六七八九十]+季[._ ]第[一二三四五六七八九十]+集", // 中文季 + 集格式
        "[._ ]Season[._ ](?<season>\\d{1,2})[._ ]Episode[._ ](?<episode>\\d{1,3})", // 完整Season +
        // Episode
        "[._ ]Disc[._ ](?<disc>\\d{1,2})[._ ](?<episode>\\d{1,3})", // Disc格式
        "[._ ]S(?<season>\\d{1,2})-(?<episode>\\d{1,3})", // 连字符格式
        "[._ ]S(?<season>\\d{1,2})\\.(?<episode>\\d{1,3})" // 点分隔格式
        );
  }

  /** 获取正则表达式优先级配置 高优先级的正则表达式会优先尝试匹配 */
  public static List<String> getRegexPriorityConfig() {
    return Arrays.asList(
        // 高优先级：中文文件名处理、年份在中间的情况
        "^(?<title>[\\u4e00-\\u9fff]+.*?)[. _](?:19|20)\\d{2}",
        "^(?<title>.+?)[. _](19|20)\\d{2}[. _](?<title2>.*)$",
        "[._ ]第[一二三四五六七八九十]+集",
        "[._ ]第[一二三四五六七八九十]+季",

        // 中优先级：EP格式处理、中文季集格式
        "[._ ]EP(?<episode>\\d{1,3})",
        "^(?<title>[\\u4e00-\\u9fff]+.*?)[. _]第[一二三四五六七八九十]+季",
        "[._ ]第[一二三四五六七八九十]+季[._ ]第[一二三四五六七八九十]+集",

        // 低优先级：复杂的混合格式、Disc/Part格式
        "[._ ]Disc[._ ](?<disc>\\d{1,2})[._ ](?<episode>\\d{1,3})",
        "^(?<title>.+?)[. _]Season[. _](?<season>\\d{1,2})[. _](?:Part|Disc)?\\d?");
  }

  /**
   * 根据优先级对正则表达式进行排序
   *
   * @param originalRegexes 原始正则表达式列表
   * @param priorityConfig 优先级配置
   * @return 排序后的正则表达式列表
   */
  public static List<String> prioritizeRegexes(
      List<String> originalRegexes, List<String> priorityConfig) {
    if (priorityConfig == null || priorityConfig.isEmpty()) {
      return originalRegexes;
    }

    // 创建优先级映射
    java.util.Map<String, Integer> priorityMap = new java.util.HashMap<>();
    for (int i = 0; i < priorityConfig.size(); i++) {
      priorityMap.put(priorityConfig.get(i), i);
    }

    // 创建正则表达式优先级列表
    List<String> prioritizedRegexes = new java.util.ArrayList<>(originalRegexes);

    // 按优先级排序：优先级配置中存在的正则表达式排在前面
    prioritizedRegexes.sort(
        (regex1, regex2) -> {
          Integer priority1 = priorityMap.get(regex1);
          Integer priority2 = priorityMap.get(regex2);

          if (priority1 == null && priority2 == null) {
            return 0; // 都不在优先级配置中，保持原顺序
          } else if (priority1 == null) {
            return 1; // regex1不在优先级配置中，排在后面
          } else if (priority2 == null) {
            return -1; // regex2不在优先级配置中，排在后面
          } else {
            return Integer.compare(priority1, priority2); // 按优先级值排序
          }
        });

    return prioritizedRegexes;
  }
}
