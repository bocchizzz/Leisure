import { expect, test } from '@playwright/test'
import {
  asUser,
  clearAuth,
  expectNoClientCrash,
  expectToast,
  expectUrl,
  loginByUi,
} from './helpers'

test.describe('认证与路由权限回归', () => {
  test('公开页面、404 与受保护路由门禁稳定', async ({ page }) => {
    await clearAuth(page)

    await page.goto('/')
    await expectUrl(page, '/')
    await expect(page.locator('body')).toContainText('任务大厅')
    await expectNoClientCrash(page)

    await page.goto('/tasks/101')
    await expect(page.locator('body')).toContainText('委托详情')
    await expect(page.locator('body')).toContainText('帮我取南门咖啡')
    await expectNoClientCrash(page)

    await page.goto('/leaderboard')
    await expect(page.locator('body')).toContainText('公会悬赏榜')
    await expectNoClientCrash(page)

    await page.goto('/precedents')
    await expect(page.locator('body')).toContainText('校园判例库')
    await expectNoClientCrash(page)

    await page.goto('/route-that-does-not-exist')
    await expect(page.locator('body')).toContainText('404')
    await expect(page.locator('body')).toContainText('返回任务大厅')
    await expectNoClientCrash(page)

    await page.goto('/my-tasks')
    await expectUrl(page, /\/login\?redirect=\/my-tasks/)
    await expect(page.locator('body')).toContainText('欢迎回来')
    await expectNoClientCrash(page)
  })

  test('登录失败、登录成功、退出和过期 token 处理正确', async ({ page }) => {
    await clearAuth(page)

    await page.goto('/login')
    await page.getByPlaceholder('输入用户名').fill('client01')
    await page.getByPlaceholder('输入密码').fill('bad-password')
    await page.locator('form').getByRole('button', { name: '登录' }).click()
    await expectToast(page, '账号或密码错误')
    await expectUrl(page, '/login')

    await loginByUi(page, 'client01')
    await expectUrl(page, '/')
    await expect(page.locator('body')).toContainText('星野委托人')
    await expect(page.evaluate(() => window.localStorage.getItem('cq_token'))).resolves.toBe('mock-token-client01')
    await expectNoClientCrash(page)

    await page.locator('.cb-topnav__user').click()
    await page.getByRole('menuitem', { name: '退出' }).click()
    await expectUrl(page, '/login')
    await expect(page.evaluate(() => window.localStorage.getItem('cq_token'))).resolves.toBeNull()

    await page.addInitScript(() => {
      window.localStorage.setItem('cq_token', 'mock-token-ghost')
      window.localStorage.removeItem('cq_user')
    })
    await page.goto('/messages')
    await expectUrl(page, /\/login\?redirect=\/messages/)
    await expectToast(page, '登录已过期，请重新登录')
    await expectNoClientCrash(page)
  })

  test('注册成功、重复注册和认证门禁可预期', async ({ page }) => {
    await clearAuth(page)
    const username = `pwuser${Date.now()}`

    await page.goto('/register')
    await page.getByPlaceholder('3-20 位字母或数字').fill(username)
    await page.getByPlaceholder('至少 6 位').fill('123456')
    await page.getByPlaceholder('再输一次').fill('123456')
    await page.getByPlaceholder('显示在委托板上的名字').fill('自动化新人')
    await page.getByRole('button', { name: /注册并去认证/ }).click()
    await expectUrl(page, '/certification')
    await expectToast(page, '注册成功，先去认证')
    await expect(page.locator('body')).toContainText('校园认证')
    await expectNoClientCrash(page)

    await page.locator('.cb-topnav__user').click()
    await page.getByRole('menuitem', { name: '退出' }).click()

    await page.goto('/register')
    await page.getByPlaceholder('3-20 位字母或数字').fill('client01')
    await page.getByPlaceholder('至少 6 位').fill('123456')
    await page.getByPlaceholder('再输一次').fill('123456')
    await page.getByPlaceholder('显示在委托板上的名字').fill('重复账号')
    await page.getByRole('button', { name: /注册并去认证/ }).click()
    await expectToast(page, '用户名已存在')
    await expectUrl(page, '/register')

    const newbie = await page.context().newPage()
    await asUser(newbie, 'newbie')
    await newbie.goto('/tasks/publish')
    await expectUrl(newbie, '/certification')
    await expect(newbie.locator('body')).toContainText('校园认证')
    await expectNoClientCrash(newbie)

    const normal = await page.context().newPage()
    await asUser(normal, 'client01')
    await normal.goto('/admin/dashboard')
    await expectUrl(normal, '/')
    await expectToast(normal, '需要管理员权限')
    await expect(normal.locator('body')).toContainText('任务大厅')
    await expectNoClientCrash(normal)
  })
})
