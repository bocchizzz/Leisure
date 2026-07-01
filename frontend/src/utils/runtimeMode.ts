export const isMockApiMode = import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true'

export const usePublicFallback =
  import.meta.env.DEV &&
  (import.meta.env.VITE_USE_MOCK === 'true' || import.meta.env.VITE_PUBLIC_FALLBACK === 'true')
