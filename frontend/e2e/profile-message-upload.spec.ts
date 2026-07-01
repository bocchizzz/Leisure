import { expect, test } from '@playwright/test'
import { asUser, expectNoClientCrash, expectToast } from './helpers'

test.describe('消息中心、资料认证与上传回归', () => {
  test('消息筛选、单条已读、全部已读、可跳转消息和未读数变化正确', async ({ page }) => {
    await asUser(page, 'client01')
    await page.goto('/messages')
    await expect(page.locator('body')).toContainText('消息中心')
    await expect(page.locator('.sig-radio__count')).not.toHaveText('0')

    await page.getByRole('button', { name: '申请' }).click()
    await expect(page.locator('.msg-list')).toContainText('新的接单申请')
    await expectNoClientCrash(page)

    await page.goto('/messages')
    const firstClickable = page.locator('.msg-row.is-clickable').first()
    await expect(firstClickable).toBeVisible()
    await firstClickable.click()
    await expect(page).toHaveURL(/\/(tasks|contracts|court)\//)
    await expectNoClientCrash(page)

    await page.goto('/messages')
    await page.getByRole('button', { name: '全部标记已读' }).click()
    await expectToast(page, '已全部标记为已读')
    await expect(page.locator('.sig-radio__count')).toHaveText('0')
  })

  test('资料修改、昵称长度校验、AI 邦布头像和上传类型拦截可用', async ({ page }) => {
    await asUser(page, 'client01')
    await page.goto('/profile')
    await expect(page.locator('body')).toContainText('我的档案')

    const nicknameInput = page.locator('.pform input[placeholder="请输入昵称"]')
    await expect(nicknameInput).toHaveValue(/.+/)
    await nicknameInput.fill('x'.repeat(31))
    await expect(nicknameInput).toHaveValue('x'.repeat(31))
    await page.getByRole('button', { name: '保存修改' }).click()
    await expectToast(page, '昵称不超过 30 个字符')

    await nicknameInput.fill('E2E 委托人')
    await page.getByRole('button', { name: '咔嚓布' }).click()
    await page.getByRole('button', { name: '保存修改' }).click()
    await expectToast(page, '资料已更新')
    await expect(page.locator('.namecard__name')).toContainText('E2E 委托人')

    const upload = page.locator('.avatar-ai input[type="file"]')
    await upload.setInputFiles({
      name: 'not-image.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('pdf'),
    })
    await expectToast(page, '文件类型不支持，请上传：图片文件')

    await upload.setInputFiles({
      name: 'empty.png',
      mimeType: 'image/png',
      buffer: Buffer.alloc(0),
    })
    await expectToast(page, '请选择有效文件，空文件不能上传')

    await upload.setInputFiles({
      name: 'avatar.png',
      mimeType: 'image/png',
      buffer: Buffer.from('avatar-image'),
    })
    await expectToast(page, '上传成功')
    await page.getByRole('button', { name: '生成邦布头像' }).click()
    await expectToast(page, '邦布头像已生成，请确认使用')
    await page.getByRole('button', { name: '使用生成头像' }).click()
    await expectToast(page, '已切换为 AI 邦布头像')
    await expectNoClientCrash(page)
  })

  test('校园认证必填、材料上传、提交和重复提交状态可见', async ({ page }) => {
    await asUser(page, 'newbie')
    await page.goto('/certification')
    await expect(page.locator('body')).toContainText('校园认证')

    await page.getByRole('button', { name: '提交认证申请' }).click()
    await expect(page.locator('body')).toContainText('请输入真实姓名')
    await expect(page.locator('body')).toContainText('请上传认证材料')

    await page.getByLabel('真实姓名').fill('E2E 新人')
    await page.getByLabel('所在学校').fill('重庆大学')
    await page.getByLabel('学号').fill(`2026${Date.now().toString().slice(-6)}`)
    await page.locator('.form input[type="file"]').setInputFiles({
      name: 'student-card.png',
      mimeType: 'image/png',
      buffer: Buffer.from('student-card'),
    })
    await expectToast(page, '上传成功')
    await page.getByRole('button', { name: '提交认证申请' }).click()
    await expectToast(page, '材料到了，等审核结果就好')
    await expect(page.locator('body')).toContainText('待审核')
    await expect(page.getByRole('button', { name: '提交认证申请' })).toHaveCount(0)
    await expectNoClientCrash(page)
  })
})
