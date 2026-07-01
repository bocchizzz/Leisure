import { expect, test, type Page } from '@playwright/test'

type MockUser = 'admin' | 'client01' | 'hunter01' | 'hunter02' | 'jury01' | 'newbie'

async function asUser(page: Page, username: MockUser) {
  await page.addInitScript((name) => {
    window.localStorage.setItem('cq_token', `mock-token-${name}`)
    window.localStorage.removeItem('cq_user')
  }, username)
}

async function clearAuth(page: Page) {
  await page.addInitScript(() => {
    window.localStorage.removeItem('cq_token')
    window.localStorage.removeItem('cq_user')
  })
}

async function expectNoClientCrash(page: Page) {
  await expect(page.locator('#app')).toBeVisible()
  await expect(page.locator('body')).not.toContainText('Cannot read properties')
  await expect(page.locator('body')).not.toContainText('Unhandled error')
}

async function expectToast(page: Page, text: string) {
  await expect(page.getByText(text).last()).toBeVisible()
}

async function loginByUi(page: Page, username: MockUser) {
  await page.goto('/login')
  await page.getByPlaceholder('输入用户名').fill(username)
  await page.getByPlaceholder('输入密码').fill('123456')
  await page.locator('form').getByRole('button', { name: '登录' }).click()
}

test.describe('赏金布前端角色/权限/操作自动化验收', () => {
  test('公开页面可访问，受保护页面会跳转登录', async ({ page }) => {
    await clearAuth(page)
    await page.goto('/')
    await expect(page).toHaveURL('/')
    await expect(page.locator('body')).toContainText('任务大厅')
    await expectNoClientCrash(page)

    await page.goto('/my-tasks')
    await expect(page).toHaveURL(/\/login\?redirect=\/my-tasks/)
    await expect(page.locator('body')).toContainText('欢迎回来')
    await expectNoClientCrash(page)
  })

  test('登录表单可登录并恢复当前用户', async ({ page }) => {
    await clearAuth(page)
    await loginByUi(page, 'client01')
    await expect(page).toHaveURL('/')
    await expect(page.locator('body')).toContainText('星野委托人')
    await expect(page.evaluate(() => window.localStorage.getItem('cq_token'))).resolves.toBe('mock-token-client01')
    await expectNoClientCrash(page)
  })

  test('未认证用户不能发布委托，普通用户不能进入后台', async ({ page }) => {
    await asUser(page, 'newbie')
    await page.goto('/tasks/publish')
    await expect(page).toHaveURL('/certification')
    await expect(page.locator('body')).toContainText('校园认证')
    await expectNoClientCrash(page)

    const normal = await page.context().newPage()
    await asUser(normal, 'client01')
    await normal.goto('/admin/dashboard')
    await expect(normal).toHaveURL('/')
    await expect(normal.locator('body')).toContainText('任务大厅')
    await expectNoClientCrash(normal)
  })

  test('管理员可访问后台并完成待审任务放行', async ({ page }) => {
    await asUser(page, 'admin')
    await page.goto('/admin/dashboard')
    await expect(page).toHaveURL('/admin/dashboard')
    await expect(page.locator('body')).toContainText('运营看板')

    await page.goto('/admin/tasks')
    await expect(page.locator('body')).toContainText('任务审核')
    await expect(page.locator('body')).toContainText('社团招新海报设计')

    await page.getByRole('row', { name: /社团招新海报设计/ }).getByRole('button', { name: '通过' }).click()
    await expectToast(page, '已通过任务')
    await expectNoClientCrash(page)
  })

  test('委托人可选择猎人生成契约，猎人和委托人可完成种子契约履约', async ({ browser }) => {
    const clientContext = await browser.newContext()
    const clientPage = await clientContext.newPage()
    await asUser(clientPage, 'client01')

    await clientPage.goto('/tasks/101/applications')
    await expect(clientPage.locator('body')).toContainText('申请管理')
    await expect(clientPage.locator('body')).toContainText('安比猎人')
    await clientPage.getByRole('button', { name: '选择该猎人' }).first().click()
    await clientPage.getByRole('button', { name: '确认选择' }).click()
    await expect(clientPage).toHaveURL(/\/contracts\/\d+/)
    await expect(clientPage.locator('body')).toContainText('履约契约')

    const hunterContext = await browser.newContext()
    const hunterPage = await hunterContext.newPage()
    await asUser(hunterPage, 'hunter01')
    await hunterPage.goto('/contracts/301')
    await expect(hunterPage.locator('body')).toContainText('提交履约证据')
    await hunterPage.getByRole('button', { name: '提交履约证据' }).click()
    await hunterPage.getByRole('textbox', { name: '说明' }).fill('E2E 验收：已完成跑腿并拍照留存。')
    await hunterPage.getByRole('button', { name: '提交证据' }).click()
    await expectToast(hunterPage, '证据已提交')
    await hunterPage.getByRole('button', { name: '提交完成' }).click()
    await hunterPage.getByRole('button', { name: '提交', exact: true }).click()
    await expectToast(hunterPage, '已提交，等待委托人确认')

    await clientPage.goto('/contracts/302')
    await expect(clientPage.locator('body')).toContainText('确认完成')
    await clientPage.getByRole('button', { name: '确认完成' }).click()
    await clientPage.getByRole('button', { name: '确认完成' }).last().click()
    await expectToast(clientPage, '已确认完成')

    await expectNoClientCrash(clientPage)
    await expectNoClientCrash(hunterPage)
    await clientContext.close()
    await hunterContext.close()
  })

  test('小法庭陪审员可投票，管理员可打开裁决台', async ({ browser }) => {
    const juryContext = await browser.newContext()
    const juryPage = await juryContext.newPage()
    await asUser(juryPage, 'jury01')
    await juryPage.goto('/court/401')
    await expect(juryPage.locator('body')).toContainText('案件裁决')

    if (await juryPage.getByText('你已投出庄严一票').isVisible().catch(() => false)) {
      await expect(juryPage.locator('body')).toContainText('舆论风向')
    } else {
      await juryPage.getByRole('button', { name: '建议和解' }).click()
      await juryPage.getByRole('button', { name: '投出我的一票' }).click()
      await expectToast(juryPage, '投票成功')
    }

    const adminContext = await browser.newContext()
    const adminPage = await adminContext.newPage()
    await asUser(adminPage, 'admin')
    await adminPage.goto('/admin/court-cases')
    await expect(adminPage.locator('body')).toContainText('案件裁决')
    await expect(adminPage.locator('body')).toContainText('字幕校对交付范围争议')
    await adminPage.getByRole('row', { name: /字幕校对交付范围争议/ }).getByRole('button').first().click()
    await expect(adminPage.locator('.el-drawer')).toContainText('双方陈述')
    await expect(adminPage.locator('.el-drawer')).toContainText('裁决结果')

    await expectNoClientCrash(juryPage)
    await expectNoClientCrash(adminPage)
    await juryContext.close()
    await adminContext.close()
  })
})
