import { expect, test } from '@playwright/test'
import {
  asUser,
  clearAuth,
  expectNoClientCrash,
  expectToast,
  expectUrl,
} from './helpers'

test.describe('任务大厅、发布编辑与申请回归', () => {
  test('任务大厅搜索、筛选、排序、AI 搜索、浏览历史和订阅提示可用', async ({ page }) => {
    await clearAuth(page)
    await page.goto('/')
    await expect(page.locator('body')).toContainText('任务大厅')

    await page.getByRole('button', { name: '浏览历史' }).click()
    await expectToast(page, '登录后才能查看浏览历史')
    await expectUrl(page, /\/login$/)

    const logged = await page.context().newPage()
    await asUser(logged, 'client01')
    await logged.goto('/tasks/101')
    await expect(logged.locator('body')).toContainText('帮我取南门咖啡')
    await logged.goto('/')

    await logged.getByRole('button', { name: '浏览历史' }).click()
    await expect(logged.locator('.el-drawer')).toContainText('浏览历史')
    await expect(logged.locator('.el-drawer')).toContainText('帮我取南门咖啡')
    await logged.keyboard.press('Escape')

    const search = logged.locator('input.search__inp')
    await search.fill('高数')
    await search.press('Enter')
    await expect(logged.locator('body')).toContainText('高数期末突击辅导')

    await logged.getByRole('button', { name: 'B' }).click()
    await expect(logged.locator('.advanced__diff.is-active')).toHaveText('B')

    await logged.locator('.advanced__bounty input').first().fill('200')
    await logged.locator('.advanced__bounty input').last().fill('100')
    await logged.locator('.advanced__bounty input').last().dispatchEvent('change')
    await expectToast(logged, '最低赏金不能高于最高赏金')

    await logged.getByRole('button', { name: /清除筛选/ }).click()
    await expect(logged.locator('.advanced__hint')).toContainText('可按难度')

    await logged.locator('select.sort').selectOption('bountyAmount,desc')
    await expectNoClientCrash(logged)

    await logged.locator('.search__ai-btn').click()
    await search.fill('找海报设计')
    await search.press('Enter')
    await expect(logged.locator('.search__ai-btn')).toHaveClass(/search__ai-btn--on/)
    await expectNoClientCrash(logged)

    await logged.locator('.footer__form button').click()
    await expectToast(logged, '邮箱先填上')
    await logged.locator('.footer__inp').fill('bad-email')
    await logged.locator('.footer__form button').click()
    await expectToast(logged, '邮箱格式不对，检查一下')
    await logged.locator('.footer__inp').fill('auto@campus.edu.cn')
    await logged.locator('.footer__form button').click()
    await expectToast(logged, '订阅成功，新委托一来马上通知你。')
  })

  test('任务详情申请、重复申请、申请自己任务和收藏状态正确', async ({ page }) => {
    await asUser(page, 'hunter01')
    await page.goto('/tasks/101')
    await expect(page.locator('body')).toContainText('接取委托')

    await page.getByRole('button', { name: '收藏委托' }).click()
    await expectToast(page, '已收藏委托')
    await expect(page.getByRole('button', { name: '已收藏' })).toBeVisible()
    await page.getByRole('button', { name: '已收藏' }).click()
    await expectToast(page, '已取消收藏')

    await page.getByRole('button', { name: '接取委托' }).click()
    await page.getByPlaceholder('说说你为什么接这单').fill('E2E：我就在南门，可以立刻送达。')
    await page.getByRole('button', { name: '确认接取' }).click()
    await expectToast(page, '申请已发出，等委托人回复就好')

    await page.waitForTimeout(950)
    await page.getByRole('button', { name: '接取委托' }).click()
    await page.getByPlaceholder('说说你为什么接这单').fill('E2E：重复申请应被拦截。')
    await page.getByRole('button', { name: '确认接取' }).click()
    await expectToast(page, '你已申请过该任务')
    await expectNoClientCrash(page)

    const owner = await page.context().newPage()
    await asUser(owner, 'hunter02')
    await owner.goto('/tasks/108')
    await expect(owner.locator('body')).toContainText('维修社团展台灯带接触不良')
    await expect(owner.getByRole('link', { name: '管理申请' })).toBeVisible()
    await expect(owner.getByRole('button', { name: '接取委托' })).toHaveCount(0)
    await expectNoClientCrash(owner)

    const newbie = await page.context().newPage()
    await asUser(newbie, 'newbie')
    await newbie.goto('/tasks/101')
    await newbie.getByRole('button', { name: '接取委托' }).click()
    await expect(newbie.locator('body')).toContainText('完成猎人认证')
    await expect(newbie.getByRole('link', { name: '去认证' })).toBeVisible()
    await expectNoClientCrash(newbie)
  })

  test('发布、AI 辅助、违规拦截、编辑和取消任务可回归', async ({ page }) => {
    await asUser(page, 'client01')
    await page.goto('/tasks/publish')
    await expect(page.locator('body')).toContainText('发布委托')

    await page.getByRole('button', { name: '发布悬赏' }).click()
    await expect(page.locator('body')).toContainText('请输入委托标题')

    await page.getByPlaceholder('例如：明天下午帮我').fill('帮忙把图书馆借的书还掉，顺便取一个快递。')
    await page.getByRole('button', { name: 'AI 一键拆解' }).click()
    await expectToast(page, '拆解完了，核对一下再提交')
    await page.getByLabel('委托描述').fill('帮忙把图书馆借的书还掉，顺便取一个快递。')

    await page.getByRole('button', { name: 'AI 估算' }).click()
    await expect(page.locator('.bounty-ai-tip')).toContainText('AI 建议')
    await page.getByRole('button', { name: '应用最小值' }).click()

    await page.getByRole('button', { name: 'AI 生成封面' }).click()
    await expectToast(page, 'AI 封面已生成')

    await page.getByRole('button', { name: '发布悬赏' }).click()
    await expectToast(page, '悬赏发布成功')
    await expectUrl(page, /\/tasks\/\d+$/)
    await expect(page.locator('body')).toContainText('委托详情')

    await page.goto('/tasks/publish')
    await page.getByLabel('委托标题').fill('代写论文')
    await page.getByLabel('委托描述').fill('请代写论文整篇论文')
    await page.getByRole('button', { name: '发布悬赏' }).click()
    await expectToast(page, '疑似学术不端内容，已被内容安全拦截。')
    await expectUrl(page, '/tasks/publish')

    await page.goto('/tasks/101/edit')
    await expect(page.locator('body')).toContainText('编辑委托')
    await page.getByPlaceholder('一句话说清你要悬赏什么').fill('E2E 编辑后的跑腿委托')
    await page.getByRole('button', { name: '保存修改' }).click()
    await expectToast(page, '委托已更新')
    await expectUrl(page, '/tasks/101')
    await expect(page.locator('body')).toContainText('待审核')

    await page.goto('/tasks/101')
    await page.getByRole('button', { name: '取消任务' }).click()
    await page.getByPlaceholder('取消原因（选填）').fill('E2E 验收取消')
    await page.getByRole('button', { name: '确认取消' }).click()
    await expectToast(page, '任务已取消')
    await expect(page.locator('body')).toContainText('已取消')
    await expectNoClientCrash(page)
  })
})
