/**
 * useScrollReveal — ZZZ DesignSpec v3.0 § 9.3
 * IntersectionObserver 驱动的 scroll reveal composable
 *
 * 用法：
 *   import { useScrollReveal } from '@/composables/useScrollReveal'
 *   // 页面内带 .scroll-reveal 的元素会自动进入视口时加 .is-visible
 *   useScrollReveal()
 *
 *   // 可选：指定选择器 / 自定义 IO 参数
 *   useScrollReveal('.scroll-reveal', { threshold: 0.15 })
 *
 *   // 可选：带 stagger 延迟，列表场景用
 *   const refreshReveal = useScrollReveal('.picks__slide', {}, { stagger: 60 })
 *   // 异步列表渲染后可手动刷新
 *   refreshReveal()
 */

import { onMounted, onUnmounted } from 'vue'

interface IoOptions {
  threshold?: number
  rootMargin?: string
  [key: string]: unknown
}

interface ExtraOptions {
  stagger?: number
}

/**
 * @param selector    CSS 选择器，默认 '.scroll-reveal'
 * @param ioOptions   IntersectionObserver 选项覆盖
 * @param extra       扩展选项
 * @param extra.stagger  每个子元素的延迟步长（ms），0 = 不启用
 */
export function useScrollReveal(
  selector = '.scroll-reveal',
  ioOptions: IoOptions = {},
  extra: ExtraOptions = {}
): () => void {
  const defaults: IntersectionObserverInit = {
    threshold:   0.12,
    rootMargin:  '0px 0px -40px 0px',
    ...ioOptions,
  }

  const { stagger = 0 } = extra

  let observer: IntersectionObserver | null = null
  let mutationObserver: MutationObserver | null = null
  const observed = new WeakSet<Element>()
  const settled = new WeakSet<Element>()

  function toMs(value: string) {
    const part = value.trim()
    if (!part) return 0
    if (part.endsWith('ms')) return Number(part.slice(0, -2)) || 0
    if (part.endsWith('s')) return (Number(part.slice(0, -1)) || 0) * 1000
    return Number(part) || 0
  }

  function transitionTotalMs(node: HTMLElement) {
    const style = window.getComputedStyle(node)
    const durations = style.transitionDuration.split(',').map(toMs)
    const delays = style.transitionDelay.split(',').map(toMs)
    const totals = durations.map((duration, index) => duration + (delays[index] ?? delays[0] ?? 0))
    return Math.max(0, ...totals)
  }

  function settle(node: HTMLElement) {
    if (settled.has(node)) return
    settled.add(node)

    const delay = Math.min(Math.max(transitionTotalMs(node) + 80, 480), 1200)
    window.setTimeout(() => {
      node.style.opacity = '1'
      node.style.transitionDelay = ''
    }, delay)
  }

  function show(el: Element) {
    const node = el as HTMLElement
    node.classList.add('is-visible')
    settle(node)
  }

  function applyStagger(el: Element) {
    if (stagger <= 0) return
    const node = el as HTMLElement
    if (node.style.transitionDelay) return
    const siblings = Array.from(node.parentElement?.children ?? [])
    const idx = siblings.indexOf(node)
    if (idx >= 0) {
      node.style.transitionDelay = `${Math.min(idx * stagger, 360)}ms`
    }
  }

  function observe(el: Element) {
    if (observed.has(el)) return
    observed.add(el)

    if (!observer) {
      show(el)
      return
    }

    observer.observe(el)
  }

  function refresh() {
    document.querySelectorAll(selector).forEach(observe)
  }

  onMounted(() => {
    if (typeof IntersectionObserver === 'undefined') {
      refresh()
      return
    }

    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (!entry.isIntersecting) return

        const el = entry.target as HTMLElement
        applyStagger(el)
        show(el)
        // 一次性：进入后不再观察
        observer!.unobserve(el)
      })
    }, defaults)

    refresh()

    if (typeof MutationObserver !== 'undefined') {
      mutationObserver = new MutationObserver(() => refresh())
      mutationObserver.observe(document.body, { childList: true, subtree: true })
    }
  })

  onUnmounted(() => {
    observer?.disconnect()
    mutationObserver?.disconnect()
    observer = null
    mutationObserver = null
  })

  return refresh
}
