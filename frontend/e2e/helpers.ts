import { expect, type Page } from '@playwright/test'

export type MockUser =
  | 'admin'
  | 'client01'
  | 'hunter01'
  | 'hunter02'
  | 'jury01'
  | 'newbie'
  | 'pending01'

export async function clearAuth(page: Page) {
  await page.addInitScript(() => {
    window.localStorage.removeItem('cq_token')
    window.localStorage.removeItem('cq_user')
  })
}

export async function asUser(page: Page, username: MockUser) {
  await page.addInitScript((name) => {
    window.localStorage.setItem('cq_token', `mock-token-${name}`)
    window.localStorage.removeItem('cq_user')
  }, username)
}

export async function loginByUi(page: Page, username: MockUser) {
  await page.goto('/login')
  await page.getByPlaceholder('输入用户名').fill(username)
  await page.getByPlaceholder('输入密码').fill('123456')
  await page.locator('form').getByRole('button', { name: '登录' }).click()
}

export async function expectNoClientCrash(page: Page) {
  await expect(page.locator('#app')).toBeVisible()
  await expect(page.locator('body')).not.toContainText('Cannot read properties')
  await expect(page.locator('body')).not.toContainText('Unhandled error')
}

export async function expectToast(page: Page, text: string | RegExp) {
  await expect(page.getByText(text).last()).toBeVisible()
}

export async function expectUrl(page: Page, pattern: RegExp | string) {
  await expect(page).toHaveURL(pattern)
}
