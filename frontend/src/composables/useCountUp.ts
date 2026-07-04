/**
 * useCountUp — ZZZ DesignSpec v3.0 § 9.7 数字/赏金跳动
 *
 * 数字从 0 滚动到目标值的入场动画，配合 IntersectionObserver
 * 在元素进入视口时触发一次。用于赏金金额、统计数字、EXP 等。
 *
 * 设计依据：ZZZ 官网数据区块的数字都有「点火」上扬动效，
 * 强化「实时信号」「赏金累积」的叙事感——不是纯装饰，
 * 而是把静态数字变成有节奏的信息揭示。
 *
 * 用法：
 *   const reward = ref(0)
 *   const rewardEl = useCountUp(rewardRef, 12800)         // 单值
 *   // 模板：<span ref="rewardRef">{{ displayed }}</span>
 *
 * 或直接对 DOM 文本操作（推荐，无需额外 ref 绑定）：
 *   import { animateCountUp } from '@/composables/useCountUp'
 *   animateCountUp(el, 0, 12800, { duration: 900, format: v => v.toLocaleString() })
 */

import { onMounted, onUnmounted, type Ref } from 'vue'

const EASE_OUT = (t: number): number => 1 - Math.pow(1 - t, 3)

const prefersReducedMotion = (): boolean =>
  typeof window !== 'undefined' &&
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

interface CountOptions {
  duration?: number
  from?: number
  format?: (v: number) => string
  decimals?: number
}

/**
 * 直接对一个 DOM 元素做数字滚动（一次性）。
 * 返回一个 cancel 函数。
 */
export function animateCountUp(
  el: HTMLElement,
  to: number,
  opts: CountOptions = {}
): () => void {
  const { duration = 850, from = 0, format, decimals = 0 } = opts

  const render = (v: number): void => {
    el.textContent = format
      ? format(v)
      : v.toFixed(decimals)
  }

  if (prefersReducedMotion()) {
    render(to)
    return () => {}
  }

  let raf = 0
  const start = performance.now()
  const delta = to - from

  const tick = (now: number): void => {
    const p = Math.min((now - start) / duration, 1)
    const v = from + delta * EASE_OUT(p)
    render(Math.round(v * 10 ** decimals) / 10 ** decimals)
    if (p < 1) raf = requestAnimationFrame(tick)
  }
  raf = requestAnimationFrame(tick)

  return () => cancelAnimationFrame(raf)
}

/**
 * Composable：元素进入视口时触发数字滚动（一次）。
 * @param elRef  绑定到目标 DOM 的 template ref
 * @param to     目标值
 */
export function useCountUp(
  elRef: Ref<HTMLElement | null>,
  to: number,
  opts: CountOptions = {}
): void {
  let observer: IntersectionObserver | null = null
  let cancel: (() => void) | null = null

  onMounted(() => {
    const el = elRef.value
    if (!el) return

    if (typeof IntersectionObserver === 'undefined') {
      cancel = animateCountUp(el, to, opts)
      return
    }

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return
          cancel = animateCountUp(entry.target as HTMLElement, to, opts)
          observer!.unobserve(entry.target)
        })
      },
      { threshold: 0.4 }
    )
    observer.observe(el)
  })

  onUnmounted(() => {
    observer?.disconnect()
    cancel?.()
  })
}

/**
 * 批量：对一组带 data-count-to 属性的元素做滚动。
 * 元素需带 `data-count-to="12800"`，可选 `data-count-format="currency"`.
 * 进入视口时各自触发。适合页面级一次性接管所有统计数字。
 *
 * 用法（页面 onMounted 后调用，或返回的 refresh 手动刷新）：
 *   const refresh = useCountUpAll()
 */
export function useCountUpAll(
  selector = '[data-count-to]'
): () => void {
  let observer: IntersectionObserver | null = null
  const done = new WeakSet<Element>()

  const trigger = (el: HTMLElement): void => {
    const to = Number(el.dataset.countTo ?? '0')
    if (Number.isNaN(to)) return
    const fmt = el.dataset.countFormat
    const decimals = Number(el.dataset.countDecimals ?? '0')
    animateCountUp(el, to, {
      decimals,
      format: fmt === 'currency' ? (v) => v.toLocaleString() : undefined,
    })
  }

  const refresh = (): void => {
    document.querySelectorAll<HTMLElement>(selector).forEach((el) => {
      if (done.has(el) || !observer) return
      observer.observe(el)
    })
  }

  onMounted(() => {
    if (typeof IntersectionObserver === 'undefined') {
      document.querySelectorAll<HTMLElement>(selector).forEach(trigger)
      return
    }
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting || done.has(entry.target)) return
          done.add(entry.target)
          trigger(entry.target as HTMLElement)
          observer!.unobserve(entry.target)
        })
      },
      { threshold: 0.4 }
    )
    refresh()
  })

  onUnmounted(() => {
    observer?.disconnect()
    observer = null
  })

  return refresh
}
