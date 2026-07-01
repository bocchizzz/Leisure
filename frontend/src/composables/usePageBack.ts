import { computed } from 'vue'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'

export type PageBackTone = 'dark' | 'light'
export type PageBackTarget = string | { name: string; copyParams?: string[] }

interface PageBackOptions {
  label?: () => string | undefined
  target?: () => PageBackTarget | undefined
}

export function usePageBack(options: PageBackOptions = {}) {
  const route = useRoute()
  const router = useRouter()

  const historyBackPath = computed(() => {
    void route.fullPath
    return window.history.state?.back
  })
  const safeBackPath = computed(() => (isSafeHistoryBack(historyBackPath.value) ? historyBackPath.value : ''))
  const label = computed(
    () =>
      options.label?.() ||
      labelForHistoryBack(safeBackPath.value) ||
      (route.meta.backLabel as string | undefined) ||
      '返回上一级',
  )

  function isSafeHistoryBack(backPath: unknown) {
    return (
      typeof backPath === 'string' &&
      backPath.startsWith('/') &&
      !backPath.startsWith('/login') &&
      !backPath.startsWith('/register') &&
      backPath !== route.fullPath
    )
  }

  function labelForHistoryBack(backPath: string) {
    if (!backPath) return ''
    const resolved = router.resolve(backPath)
    const name = String(resolved.name || '')
    const labels: Record<string, string> = {
      'task-hall': '返回任务大厅',
      'task-detail': '返回委托详情',
      'task-edit': '返回编辑委托',
      'application-manage': '返回申请管理',
      'my-tasks': '返回我的任务',
      'contract-detail': '返回契约履约',
      'court-list': '返回小法庭',
      'court-detail': '返回案件详情',
      precedents: '返回判例库',
      'hunter-profile': '返回猎人档案',
      leaderboard: '返回猎人榜',
      profile: '返回我的资料',
      certification: '返回校园认证',
      messages: '返回消息中心',
    }
    if (labels[name]) return labels[name]

    const title = resolved.meta.title
    return typeof title === 'string' ? `返回${title}` : ''
  }

  function resolveBackTarget(): RouteLocationRaw {
    const target = options.target?.() || (route.meta.backTarget as PageBackTarget | undefined) || '/'
    if (typeof target === 'string') return target

    const params: Record<string, string | string[]> = {}
    target.copyParams?.forEach((key) => {
      const value = route.params[key]
      if (typeof value === 'string' || Array.isArray(value)) params[key] = value
    })

    return { name: target.name, params }
  }

  function goBack() {
    if (safeBackPath.value) {
      router.back()
      return
    }

    router.push(resolveBackTarget())
  }

  return {
    label,
    goBack,
    safeBackPath,
    resolveBackTarget,
  }
}
