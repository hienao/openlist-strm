/**
 * 前端日志收集服务
 * 收集console日志并发送到后端
 */

import { authenticatedApiCall, apiCall } from '~/utils/api.js'

class Logger {
  constructor() {
    this.logQueue = []
    this.isProcessing = false
    this.maxQueueSize = 100
    this.batchSize = 10
    this.flushInterval = 5000 // 5秒
    
    // 启动定时发送
    this.startPeriodicFlush()
    
    // 页面卸载时发送剩余日志
    if (process.client) {
      window.addEventListener('beforeunload', () => {
        this.flush(true)
      })
    }
  }

  /**
   * 添加日志到队列
   */
  addLog(level, message, extra = {}) {
    const logEntry = {
      timestamp: Date.now(), // 使用数字时间戳而不是ISO字符串
      level,
      message: typeof message === 'string' ? message : JSON.stringify(message),
      url: process.client ? window.location.href : '',
      userAgent: process.client ? navigator.userAgent : '',
      ...extra
    }

    this.logQueue.push(logEntry)

    // 如果队列过大，移除最旧的日志
    if (this.logQueue.length > this.maxQueueSize) {
      this.logQueue.shift()
    }

    // 如果是错误级别，立即发送
    if (level === 'error') {
      this.flush()
    }
  }

  /**
   * 发送日志到后端
   */
  async flush(sync = false) {
    if (this.isProcessing || this.logQueue.length === 0) {
      return
    }

    this.isProcessing = true

    try {
      // 取出要发送的日志
      const logsToSend = this.logQueue.splice(0, this.batchSize)
      
      // 为每个日志条目添加会话ID
      const enrichedLogs = logsToSend.map(log => ({
        ...log,
        sessionId: this.getSessionId()
      }))
      
      if (sync && navigator.sendBeacon) {
        // 同步发送（页面卸载时）
        const data = JSON.stringify({ logs: enrichedLogs })
        navigator.sendBeacon('/api/logs/frontend', data)
      } else {
        // 异步发送 - 使用apiCall而不是authenticatedApiCall，因为日志API不需要认证
        // API路径为'/logs/frontend'
        await apiCall('/logs/frontend', {
          method: 'POST',
          body: { logs: enrichedLogs }
        })
      }
    } catch (error) {
      console.error('发送前端日志失败:', error)
      // 发送失败时，将日志重新加入队列
      // this.logQueue.unshift(...logsToSend)
    } finally {
      this.isProcessing = false
    }
  }

  /**
   * 获取或生成会话ID
   */
  getSessionId() {
    let sessionId = sessionStorage.getItem('logSessionId')
    if (!sessionId) {
      sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      sessionStorage.setItem('logSessionId', sessionId)
    }
    return sessionId
  }

  /**
   * 启动定时发送
   */
  startPeriodicFlush() {
    if (process.client) {
      setInterval(() => {
        this.flush()
      }, this.flushInterval)
    }
  }

  /**
   * 记录信息日志
   */
  info(message, extra = {}) {
    this.addLog('info', message, extra)
  }

  /**
   * 记录警告日志
   */
  warn(message, extra = {}) {
    this.addLog('warn', message, extra)
  }

  /**
   * 记录错误日志
   */
  error(message, extra = {}) {
    this.addLog('error', message, extra)
  }

  /**
   * 记录调试日志
   */
  debug(message, extra = {}) {
    this.addLog('debug', message, extra)
  }
}

// 创建全局日志实例
const logger = new Logger()

/**
 * 拦截原生console方法
 */
if (process.client) {
  const originalConsole = {
    log: console.log,
    info: console.info,
    warn: console.warn,
    error: console.error,
    debug: console.debug
  }

  // 重写console.log
  console.log = function(...args) {
    originalConsole.log.apply(console, args)
    logger.info(args.join(' '))
  }

  // 重写console.info
  console.info = function(...args) {
    originalConsole.info.apply(console, args)
    logger.info(args.join(' '))
  }

  // 重写console.warn
  console.warn = function(...args) {
    originalConsole.warn.apply(console, args)
    logger.warn(args.join(' '))
  }

  // 重写console.error
  console.error = function(...args) {
    originalConsole.error.apply(console, args)
    logger.error(args.join(' '))
  }

  // 重写console.debug
  console.debug = function(...args) {
    originalConsole.debug.apply(console, args)
    logger.debug(args.join(' '))
  }

  // 捕获未处理的错误
  window.addEventListener('error', (event) => {
    logger.error(`未捕获的错误: ${event.message}`, {
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno,
      stack: event.error?.stack
    })
  })

  // 捕获未处理的Promise拒绝
  window.addEventListener('unhandledrejection', (event) => {
    logger.error(`未处理的Promise拒绝: ${event.reason}`, {
      stack: event.reason?.stack
    })
  })
}

export default logger