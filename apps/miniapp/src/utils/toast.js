const EVENT = 'snp:toast'

let timer = null

/**
 * JoJo 风自定义 Toast（页面需挂载 <SnpToast />）
 * @param {object|string} opts
 */
export function showToast(opts = {}) {
  const options = typeof opts === 'string' ? { title: opts } : opts || {}
  const title = String(options.title || '').trim()
  if (!title) return

  const payload = {
    title: title.length > 32 ? `${title.slice(0, 32)}…` : title,
    subtitle: options.subtitle || defaultSubtitle(options.type || 'error'),
    type: options.type || 'error',
    duration: options.duration || 3000
  }

  uni.$emit(EVENT, payload)

  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    hideToast()
  }, payload.duration)
}

function defaultSubtitle(type) {
  if (type === 'success') return '操作成功'
  if (type === 'info') return '请留意'
  return '请稍后再试'
}

export function hideToast() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
  uni.$emit(EVENT, { visible: false })
}

export const TOAST_EVENT = EVENT
