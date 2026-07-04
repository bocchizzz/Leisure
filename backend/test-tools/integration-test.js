const fs = require('fs');
const http = require('http');

const BASE = 'http://127.0.0.1:8080/api';
const ROOT = 'http://127.0.0.1:8080';
const results = [];

function record(name, ok, detail) {
  results.push({ name, ok, detail });
  console.log(`${ok ? 'PASS' : 'FAIL'} ${name}${detail ? ` - ${detail}` : ''}`);
}

async function request(method, path, token, body, headers = {}) {
  const init = { method, headers: { ...headers } };
  if (token) init.headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(body);
  }
  const response = await fetch(`${BASE}${path}`, init);
  const text = await response.text();
  let json = null;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = null;
  }
  return { status: response.status, json, text };
}

async function rootRequest(method, path, token) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${ROOT}${path}`, { method, headers });
  return { status: response.status, text: await response.text() };
}

async function expect(name, promise, predicate, describe) {
  try {
    const result = await promise;
    const ok = Boolean(predicate(result));
    record(name, ok, describe ? describe(result) : JSON.stringify(result.json ?? result.text).slice(0, 160));
    if (!ok) {
      throw new Error(`${name} failed`);
    }
    return result;
  } catch (err) {
    record(name, false, err.message);
    throw err;
  }
}

async function login(username) {
  const result = await request('POST', '/auth/login', null, { username, password: '123456' });
  if (result.status !== 200 || result.json?.code !== 200) {
    throw new Error(`login failed for ${username}: ${result.text}`);
  }
  return result.json.data.token;
}

async function wait(url, timeoutMs = 90000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url);
      if (response.ok) return true;
    } catch {}
    await new Promise(resolve => setTimeout(resolve, 1000));
  }
  throw new Error(`timeout waiting for ${url}`);
}

async function main() {
  await wait(`${ROOT}/actuator/health`);

  const admin = await login('admin');
  const client = await login('client01');
  const hunter = await login('hunter01');
  const jury = await login('jury01');
  const newbie = await login('newbie');

  await expect('frontend authApi.me', request('GET', '/auth/me', client), r => r.status === 200 && r.json.data.username === 'client01');
  await expect('frontend userApi.profile', request('GET', '/users/profile', client), r => r.status === 200 && r.json.data.id === 2);
  await expect('frontend userApi.public user', request('GET', '/users/3', null), r => r.status === 200 && r.json.data.id === 3);

  await expect('frontend fileApi.upload no token blocked', request('POST', '/files/upload', null, {}), r => r.status === 401);

  await expect('frontend taskApi.list via mock marketplace', request('GET', '/tasks', null), r => r.status === 200 && r.json.data.content.length >= 1);
  await expect('frontend taskApi.getById via mock marketplace', request('GET', '/tasks/101', null), r => r.status === 200 && r.json.data.id === 101);
  const createdTask = await expect(
    'frontend taskApi.create via mock marketplace',
    request('POST', '/tasks', client, {
      title: 'Integration task',
      description: 'Created by integration test',
      category: 'ERRAND',
      difficulty: 'F',
      bountyAmount: 20,
      bountyType: 'POINT',
    }),
    r => r.status === 200 && r.json.data.status === 'PENDING_REVIEW',
    r => `id=${r.json?.data?.id}, status=${r.json?.data?.status}`,
  );
  const createdTaskId = createdTask.json.data.id;
  await expect('frontend adminApi.tasks via mock marketplace', request('GET', '/admin/tasks', admin), r => r.status === 200 && r.json.data.totalElements >= 1);
  await expect('frontend adminApi.reviewTask via mock marketplace', request('PUT', `/admin/tasks/${createdTaskId}/review`, admin, { approved: true, comment: 'ok' }), r => r.status === 200 && r.json.data.status === 'PUBLISHED');
  await expect('frontend taskFavoriteApi.add', request('POST', '/task-favorites/101', client), r => r.status === 200);
  await expect('frontend taskFavoriteApi.list', request('GET', '/task-favorites', client), r => r.status === 200 && r.json.data.content.some(t => t.id === 101));
  await expect('frontend taskHistoryApi.list', request('GET', '/task-history', client), r => r.status === 200);

  const app = await expect(
    'frontend taskApi.apply via mock marketplace',
    request('POST', `/tasks/${createdTaskId}/applications`, hunter, { applyMessage: 'I can do it.' }),
    r => r.status === 200 && r.json.data.status === 'APPLIED',
    r => `application=${r.json?.data?.id}`,
  );
  const appId = app.json.data.id;
  await expect('frontend taskApi.applications', request('GET', `/tasks/${createdTaskId}/applications`, client), r => r.status === 200 && Array.isArray(r.json.data));
  await expect('frontend applicationApi.my', request('GET', '/applications/my', hunter), r => r.status === 200 && r.json.data.content.length >= 1);
  await expect('frontend applicationApi.update', request('PUT', `/applications/${appId}`, hunter, { applyMessage: 'Updated.' }), r => r.status === 200 && r.json.data.applyMessage === 'Updated.');
  const contract = await expect('frontend applicationApi.accept', request('PUT', `/applications/${appId}/accept`, client), r => r.status === 200 && r.json.data.status === 'IN_PROGRESS');
  const contractId = contract.json.data.id;
  await expect('frontend contractApi.getById', request('GET', `/contracts/${contractId}`, client), r => r.status === 200 && r.json.data.id === contractId);
  await expect('frontend contractApi.submitEvidence', request('POST', `/contracts/${contractId}/evidences`, hunter, { type: 'TEXT', content: 'Done.' }), r => r.status === 200 && r.json.data.contractId === contractId);
  await expect('frontend contractApi.evidences', request('GET', `/contracts/${contractId}/evidences`, client), r => r.status === 200 && r.json.data.length >= 1);
  await expect('frontend contractApi.submitCompletion', request('PUT', `/contracts/${contractId}/submit-completion`, hunter), r => r.status === 200 && r.json.data.status === 'WAIT_CONFIRM');
  await expect('frontend contractApi.confirmCompletion', request('PUT', `/contracts/${contractId}/confirm-completion`, client), r => r.status === 200 && r.json.data.status === 'COMPLETED');

  await expect('frontend reviewApi.create via mock reputation', request('POST', '/reviews', client, { contractId, rating: 5, tags: ['fast'], content: 'Great.' }), r => r.status === 200 && r.json.data.rating === 5);
  await expect('frontend reviewApi.byContract', request('GET', `/reviews/contract/${contractId}`, client), r => r.status === 200 && r.json.data.publisherToHunter);
  await expect('frontend hunterApi.getById', request('GET', '/hunters/3', null), r => r.status === 200 && r.json.data.userId === 3);
  await expect('frontend hunterApi.me', request('GET', '/hunters/me', hunter), r => r.status === 200 && r.json.data.userId === 3);
  await expect('frontend hunterApi.leaderboard', request('GET', '/hunters/leaderboard', null), r => r.status === 200 && r.json.data.length >= 1);
  await expect('frontend hunterApi.badges', request('GET', '/hunters/3/badges', null), r => r.status === 200 && Array.isArray(r.json.data));
  await expect('frontend hunterApi.creditLogs', request('GET', '/hunters/3/credit-logs', hunter), r => r.status === 200 && r.json.data.content.length >= 1);

  await expect('frontend aiApi.breakdown via mock ai', request('POST', '/ai/tasks/breakdown', client, { rawText: 'bring coffee' }), r => r.status === 200 && r.json.data.steps.length >= 1);
  await expect('frontend aiApi.bountySuggestion via mock ai', request('POST', '/ai/tasks/bounty-suggestion', client, { rawText: 'bring coffee' }), r => r.status === 200 && r.json.data.suggestedBountyMin > 0);
  await expect('frontend aiApi.riskAssessment via mock ai', request('POST', '/ai/tasks/risk-assessment', client, { rawText: 'bring coffee' }), r => r.status === 200 && r.json.data.risks.length >= 1);
  await expect('frontend aiApi.coverImage via mock ai', request('POST', '/ai/tasks/cover-image', client, { title: 'Coffee' }), r => r.status === 200 && r.json.data.imageUrl);
  await expect('frontend aiApi.bangbooAvatar via mock ai', request('POST', '/ai/profile/bangboo-avatar', client, { referenceImageUrl: '/uploads/mock/ref.png' }), r => r.status === 200 && r.json.data.imageUrl);
  await expect('frontend aiApi.smartSearch via mock ai', request('POST', '/ai/tasks/smart-search', client, { rawText: 'coffee' }), r => r.status === 200 && r.json.data.keyword);
  await expect('frontend aiApi.courtSummary via mock ai', request('POST', '/ai/court/summary', client, { caseId: 401, style: 'OBJECTIVE' }), r => r.status === 200 && r.json.data.summary);
  await expect('frontend aiApi.courtRoast via mock ai', request('POST', '/ai/court/roast', client, { caseId: 401, style: 'ROAST' }), r => r.status === 200 && r.json.data.roast);

  await expect('frontend courtApi.getById A service', request('GET', '/court-cases/401', client), r => r.status === 200 && r.json.data.courtCase.id === 401);
  await expect('frontend courtApi.addStatement A service', request('POST', '/court-cases/401/statements', hunter, { content: 'Integration statement.' }), r => r.status === 200 && r.json.data.content === 'Integration statement.');
  await expect('frontend courtApi.addEvidence A service', request('POST', '/court-cases/401/evidences', client, { type: 'TEXT', content: 'Integration evidence.' }), r => r.status === 200 && r.json.data.type === 'TEXT');
  await expect('frontend courtApi.vote party blocked', request('POST', '/court-cases/401/votes', client, { option: 'SUPPORT_PUBLISHER' }), r => r.status === 403 && r.json.code === 403);
  await expect('frontend courtApi.vote jury', request('POST', '/court-cases/401/votes', jury, { option: 'SUGGEST_SETTLEMENT', reason: 'Looks fair.' }), r => r.status === 200 && r.json.data.option === 'SUGGEST_SETTLEMENT');
  await expect('frontend courtApi.vote duplicate blocked', request('POST', '/court-cases/401/votes', jury, { option: 'SUPPORT_HUNTER' }), r => r.status === 409);
  await expect('frontend courtApi.voteStats', request('GET', '/court-cases/401/votes/stats', client), r => r.status === 200 && r.json.data.totalVotes >= 2);
  const newCourtCase = await expect('frontend courtApi.create calls mock B marketplace', request('POST', '/court-cases', client, { contractId: 302, type: 'PERFORMANCE_DISPUTE', caseTitle: 'Integration court case', content: 'Create with mocked B service.' }), r => r.status === 200 && r.json.data.status === 'VOTING');
  const newCaseId = newCourtCase.json.data.id;
  await expect('frontend courtApi.adminList', request('GET', '/admin/court-cases', admin), r => r.status === 200 && r.json.data.totalElements >= 1);
  await expect('frontend courtApi.rule calls mock B rule-result', request('PUT', `/admin/court-cases/${newCaseId}/rule`, admin, { result: 'PARTIAL_HUNTER', bountyReleaseRate: 0.6, publisherCreditDelta: -1, hunterCreditDelta: 2, reason: 'Integration ruling.', archiveAsPrecedent: true }), r => r.status === 200 && r.json.data.status === 'ARCHIVED');
  await expect('frontend courtApi.precedents public', request('GET', '/court-precedents', null), r => r.status === 200 && r.json.data.totalElements >= 1);

  await expect('frontend messageApi.list A service', request('GET', '/messages', client), r => r.status === 200 && Array.isArray(r.json.data.content));
  await expect('frontend messageApi.unreadCount A service', request('GET', '/messages/unread-count', client), r => r.status === 200 && typeof r.json.data === 'number');

  await expect('frontend adminApi.dashboard combines A + mock B stats', request('GET', '/admin/dashboard', admin), r => r.status === 200 && r.json.data.taskCount >= 1 && r.json.data.aiCallCount === 42);
  await expect('frontend adminApi.opsConfig', request('GET', '/admin/ops-config', admin), r => r.status === 200 && r.json.data.juryMinReputation >= 0);
  await expect('frontend adminApi.auditLogs', request('GET', '/admin/audit-logs', admin), r => r.status === 200 && Array.isArray(r.json.data.content));

  await expect('gateway blocks direct internal path', rootRequest('GET', '/internal/contracts/302/brief', null), r => r.status === 403);
  await expect('gateway blocks api internal path', rootRequest('GET', '/api/internal/contracts/302/brief', null), r => r.status === 403);

  const eventsResponse = await fetch('http://127.0.0.1:8082/__mock/events');
  const events = await eventsResponse.json();
  record('mock B observed court mark-disputed', Boolean(events.data.markDisputed.length >= 1), `count=${events.data.markDisputed.length}`);
  record('mock B observed court rule-result', Boolean(events.data.ruleResults.length >= 1), `count=${events.data.ruleResults.length}`);

  const failed = results.filter(r => !r.ok);
  const summary = {
    total: results.length,
    passed: results.length - failed.length,
    failed: failed.length,
    failedNames: failed.map(r => r.name),
  };
  fs.writeFileSync('test-tools/integration-result.json', JSON.stringify({ summary, results }, null, 2));
  console.log(`SUMMARY ${summary.passed}/${summary.total} passed`);
  if (failed.length) process.exit(1);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
