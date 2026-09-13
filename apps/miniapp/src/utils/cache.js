/**
 * Lightweight in-memory cache for tab pages (reduce onShow refetch lag).
 */
const store = Object.create(null)

export function getCache(key, maxAgeMs = 30000) {
  const hit = store[key]
  if (!hit) return null
  if (Date.now() - hit.at > maxAgeMs) return null
  return hit.data
}

export function setCache(key, data) {
  store[key] = { data, at: Date.now() }
  return data
}

/**
 * Return cache immediately when fresh; refresh in background and optionally patch UI.
 * @param {string} key
 * @param {() => Promise<any>} fetcher
 * @param {number} maxAgeMs
 * @param {(fresh: any) => void} [onFresh] called when background refresh finishes
 */
export async function staleWhileRevalidate(key, fetcher, maxAgeMs = 30000, onFresh) {
  const cached = getCache(key, maxAgeMs)
  if (cached != null) {
    Promise.resolve()
      .then(fetcher)
      .then((fresh) => {
        setCache(key, fresh)
        if (typeof onFresh === 'function') onFresh(fresh)
      })
      .catch(() => {})
    return { data: cached, fromCache: true }
  }
  const fresh = await fetcher()
  setCache(key, fresh)
  return { data: fresh, fromCache: false }
}

export function invalidateCache(key) {
  if (key) delete store[key]
  else Object.keys(store).forEach((k) => delete store[k])
}
