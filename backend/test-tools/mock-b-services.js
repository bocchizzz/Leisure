const http = require('http');
const { URL } = require('url');

const INTERNAL_TOKEN = 'bangboo-internal-dev-token';

function ok(res, data) {
  json(res, 200, { code: 200, message: 'OK', data });
}

function fail(res, status, message) {
  json(res, status, { code: status, message, data: null });
}

function json(res, status, body) {
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': '*',
    'Access-Control-Allow-Methods': '*',
  });
  res.end(JSON.stringify(body));
}

function readBody(req) {
  return new Promise((resolve) => {
    const chunks = [];
    req.on('data', chunk => chunks.push(chunk));
    req.on('end', () => {
      const text = Buffer.concat(chunks).toString('utf8');
      if (!text) return resolve({});
      try {
        resolve(JSON.parse(text));
      } catch {
        resolve({});
      }
    });
  });
}

function page(content, number = 0, size = 10) {
  return {
    content,
    totalElements: content.length,
    totalPages: content.length === 0 ? 0 : Math.ceil(content.length / size),
    number,
    size,
    first: number === 0,
    last: true,
  };
}

function userId(req) {
  const raw = req.headers['x-user-id'];
  return raw ? Number(raw) : null;
}

function requireInternal(req, res) {
  if (req.headers['x-internal-token'] !== INTERNAL_TOKEN) {
    fail(res, 401, 'invalid internal token');
    return false;
  }
  return true;
}

const tasks = [
  {
    id: 101,
    title: 'Coffee delivery to library',
    description: 'Please bring one latte from south gate to the library third floor.',
    category: 'ERRAND',
    categoryName: 'Errand',
    difficulty: 'F',
    bountyAmount: 18,
    bountyType: 'POINT',
    location: 'South gate',
    deadline: '2026-07-03T10:00:00Z',
    completionStandard: 'Delivered to the requester.',
    evidenceRequirement: 'Upload handoff photo if needed.',
    status: 'PUBLISHED',
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    applicationCount: 1,
    isFavorited: false,
    viewCount: 88,
    safetyStatus: 'PASS',
    safetyScore: 28,
    createdAt: '2026-07-01T08:00:00Z',
    publishedAt: '2026-07-01T09:00:00Z',
  },
  {
    id: 102,
    title: 'Club recruitment poster design',
    description: 'Design a poster draft for club recruitment.',
    category: 'DESIGN',
    categoryName: 'Design',
    difficulty: 'C',
    bountyAmount: 80,
    bountyType: 'POINT',
    location: 'Online',
    status: 'PENDING_REVIEW',
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    applicationCount: 0,
    isFavorited: false,
    viewCount: 20,
    safetyStatus: 'REVIEW',
    safetyScore: 52,
    createdAt: '2026-07-01T12:00:00Z',
  },
  {
    id: 201,
    title: 'Poster polishing and layout',
    description: 'Existing contract task used by court mock.',
    category: 'DESIGN',
    categoryName: 'Design',
    difficulty: 'B',
    bountyAmount: 80,
    bountyType: 'POINT',
    location: 'Online',
    status: 'COURT_REVIEW',
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    assignedHunterId: 3,
    applicationCount: 1,
    isFavorited: false,
    viewCount: 41,
    safetyStatus: 'PASS',
    createdAt: '2026-07-01T12:00:00Z',
  },
  {
    id: 202,
    title: 'Delivery scope verification',
    description: 'A second contract task used to create a court case.',
    category: 'ONLINE',
    categoryName: 'Online',
    difficulty: 'D',
    bountyAmount: 120,
    bountyType: 'POINT',
    location: 'Online',
    status: 'WAIT_CONFIRM',
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    assignedHunterId: 3,
    applicationCount: 1,
    isFavorited: false,
    viewCount: 36,
    safetyStatus: 'PASS',
    createdAt: '2026-07-01T13:00:00Z',
  },
];

let nextTaskId = 300;
let nextApplicationId = 701;
let nextContractId = 303;
let nextEvidenceId = 901;
let nextReviewId = 1001;

const applications = [
  {
    id: 601,
    taskId: 101,
    taskTitle: 'Coffee delivery to library',
    hunterId: 3,
    hunterName: 'Hunter One',
    hunterAvatar: 'bangboo:kacha',
    hunterLevel: 4,
    hunterTitle: 'Gold Hunter',
    hunterReputation: 920,
    applyMessage: 'I can deliver it in 20 minutes.',
    expectedFinishTime: '2026-07-03T10:00:00Z',
    status: 'APPLIED',
    createdAt: '2026-07-01T10:00:00Z',
    updatedAt: '2026-07-01T10:00:00Z',
  },
];

const contracts = [
  {
    id: 301,
    contractNo: 'CONTRACT-2026-0301',
    taskId: 201,
    taskTitle: 'Poster polishing and layout',
    applicationId: 600,
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    hunterId: 3,
    hunterName: 'Hunter One',
    hunterAvatar: 'bangboo:kacha',
    bountyAmount: 80,
    bountyType: 'POINT',
    status: 'DISPUTED',
    completionStandard: 'Deliver final poster file.',
    evidenceRequirement: 'Upload final poster and revision record.',
    reviewedByPublisher: false,
    reviewedByHunter: false,
    createdAt: '2026-07-01T14:00:00Z',
    acceptedAt: '2026-07-01T14:00:00Z',
  },
  {
    id: 302,
    contractNo: 'CONTRACT-2026-0302',
    taskId: 202,
    taskTitle: 'Delivery scope verification',
    applicationId: 602,
    publisherId: 2,
    publisherName: 'Client One',
    publisherAvatar: 'bangboo:kacha',
    hunterId: 3,
    hunterName: 'Hunter One',
    hunterAvatar: 'bangboo:kacha',
    bountyAmount: 120,
    bountyType: 'POINT',
    status: 'WAIT_CONFIRM',
    completionStandard: 'Verify scope and submit written conclusion.',
    evidenceRequirement: 'Upload conclusion text.',
    reviewedByPublisher: false,
    reviewedByHunter: false,
    createdAt: '2026-07-01T15:00:00Z',
    acceptedAt: '2026-07-01T15:00:00Z',
  },
];

const evidences = [];
const reviews = [];
const favorites = new Map();
const history = new Map();
const bEvents = {
  markDisputed: [],
  ruleResults: [],
  reviewFlags: [],
};

function now() {
  return new Date().toISOString();
}

function currentUserName(id) {
  const names = { 1: 'Admin', 2: 'Client One', 3: 'Hunter One', 4: 'Hunter Two', 5: 'Jury One', 6: 'Newbie' };
  return names[id] || `User ${id}`;
}

function taskBrief(taskId) {
  return tasks.find(t => t.id === taskId);
}

function contractBrief(contract) {
  return {
    id: contract.id,
    taskId: contract.taskId,
    taskTitle: contract.taskTitle,
    publisherId: contract.publisherId,
    publisherName: contract.publisherName,
    hunterId: contract.hunterId,
    hunterName: contract.hunterName,
    status: contract.status,
    bountyAmount: contract.bountyAmount,
    bountyType: contract.bountyType || 'POINT',
  };
}

function marketplaceHandler(req, res, url) {
  if (req.method === 'OPTIONS') return ok(res, null);

  if (req.method === 'GET' && url.pathname === '/actuator/health') return json(res, 200, { status: 'UP' });

  if (url.pathname === '/__mock/events' && req.method === 'GET') return ok(res, bEvents);

  if (url.pathname === '/internal/stats/marketplace' && req.method === 'GET') {
    if (!requireInternal(req, res)) return;
    return ok(res, {
      taskCount: tasks.length,
      recruitingTaskCount: tasks.filter(t => t.status === 'PUBLISHED').length,
      completedTaskCount: contracts.filter(c => c.status === 'COMPLETED').length,
      disputeTaskCount: contracts.filter(c => c.status === 'DISPUTED' || c.status === 'RULED').length,
      contractCount: contracts.length,
    });
  }

  const internalBrief = url.pathname.match(/^\/internal\/contracts\/(\d+)\/brief$/);
  if (internalBrief && req.method === 'GET') {
    if (!requireInternal(req, res)) return;
    const contract = contracts.find(c => c.id === Number(internalBrief[1]));
    return contract ? ok(res, contractBrief(contract)) : fail(res, 404, 'contract not found');
  }

  const markDisputed = url.pathname.match(/^\/internal\/contracts\/(\d+)\/mark-disputed$/);
  if (markDisputed && req.method === 'POST') {
    if (!requireInternal(req, res)) return;
    return readBody(req).then(body => {
      const contract = contracts.find(c => c.id === Number(markDisputed[1]));
      if (!contract) return fail(res, 404, 'contract not found');
      contract.status = 'DISPUTED';
      const task = taskBrief(contract.taskId);
      if (task) task.status = 'COURT_REVIEW';
      bEvents.markDisputed.push({ contractId: contract.id, body });
      ok(res, contractBrief(contract));
    });
  }

  const ruleResult = url.pathname.match(/^\/internal\/contracts\/(\d+)\/rule-result$/);
  if (ruleResult && req.method === 'POST') {
    if (!requireInternal(req, res)) return;
    return readBody(req).then(body => {
      const contract = contracts.find(c => c.id === Number(ruleResult[1]));
      if (!contract) return fail(res, 404, 'contract not found');
      contract.status = 'RULED';
      const task = taskBrief(contract.taskId);
      if (task) task.status = 'RULED';
      bEvents.ruleResults.push({ contractId: contract.id, body });
      ok(res, contractBrief(contract));
    });
  }

  const reviewPermission = url.pathname.match(/^\/internal\/contracts\/(\d+)\/review-permission$/);
  if (reviewPermission && req.method === 'GET') {
    if (!requireInternal(req, res)) return;
    const contract = contracts.find(c => c.id === Number(reviewPermission[1]));
    const queryUserId = Number(url.searchParams.get('userId'));
    if (!contract) return fail(res, 404, 'contract not found');
    const role = queryUserId === contract.publisherId ? 'PUBLISHER_TO_HUNTER' : 'HUNTER_TO_PUBLISHER';
    return ok(res, {
      allowed: queryUserId === contract.publisherId || queryUserId === contract.hunterId,
      taskId: contract.taskId,
      taskTitle: contract.taskTitle,
      reviewerId: queryUserId,
      revieweeId: queryUserId === contract.publisherId ? contract.hunterId : contract.publisherId,
      role,
    });
  }

  const reviewFlags = url.pathname.match(/^\/internal\/contracts\/(\d+)\/review-flags$/);
  if (reviewFlags && req.method === 'POST') {
    if (!requireInternal(req, res)) return;
    return readBody(req).then(body => {
      const contract = contracts.find(c => c.id === Number(reviewFlags[1]));
      if (!contract) return fail(res, 404, 'contract not found');
      Object.assign(contract, body);
      bEvents.reviewFlags.push({ contractId: contract.id, body });
      ok(res, contract);
    });
  }

  if (url.pathname === '/tasks' && req.method === 'GET') return ok(res, page(tasks.filter(t => t.status === 'PUBLISHED')));
  if (url.pathname === '/tasks/recommendations' && req.method === 'GET') return ok(res, tasks.filter(t => t.status === 'PUBLISHED').slice(0, 3));

  if (url.pathname === '/tasks' && req.method === 'POST') {
    return readBody(req).then(body => {
      const uid = userId(req);
      const task = {
        id: nextTaskId++,
        ...body,
        categoryName: body.category || 'OTHER',
        status: 'PENDING_REVIEW',
        publisherId: uid,
        publisherName: currentUserName(uid),
        publisherAvatar: 'bangboo:kacha',
        applicationCount: 0,
        isFavorited: false,
        viewCount: 0,
        safetyStatus: 'REVIEW',
        safetyScore: 50,
        createdAt: now(),
      };
      tasks.unshift(task);
      ok(res, task);
    });
  }

  const taskMatch = url.pathname.match(/^\/tasks\/(\d+)$/);
  if (taskMatch && req.method === 'GET') {
    const task = tasks.find(t => t.id === Number(taskMatch[1]));
    if (!task) return fail(res, 404, 'task not found');
    const uid = userId(req);
    if (uid) {
      history.set(uid, [task.id, ...(history.get(uid) || []).filter(id => id !== task.id)]);
    }
    return ok(res, task);
  }

  if (taskMatch && req.method === 'PUT') {
    return readBody(req).then(body => {
      const task = tasks.find(t => t.id === Number(taskMatch[1]));
      if (!task) return fail(res, 404, 'task not found');
      Object.assign(task, body);
      ok(res, task);
    });
  }

  const taskCancel = url.pathname.match(/^\/tasks\/(\d+)\/cancel$/);
  if (taskCancel && req.method === 'PUT') {
    const task = tasks.find(t => t.id === Number(taskCancel[1]));
    if (!task) return fail(res, 404, 'task not found');
    task.status = 'CANCELLED';
    return ok(res, task);
  }

  if (url.pathname === '/tasks/my-published' && req.method === 'GET') {
    const uid = userId(req);
    return ok(res, page(tasks.filter(t => t.publisherId === uid)));
  }

  if (url.pathname === '/tasks/my-accepted' && req.method === 'GET') {
    const uid = userId(req);
    return ok(res, page(tasks.filter(t => t.assignedHunterId === uid)));
  }

  const taskApps = url.pathname.match(/^\/tasks\/(\d+)\/applications$/);
  if (taskApps && req.method === 'GET') {
    return ok(res, applications.filter(a => a.taskId === Number(taskApps[1])));
  }

  if (taskApps && req.method === 'POST') {
    return readBody(req).then(body => {
      const uid = userId(req);
      const task = taskBrief(Number(taskApps[1]));
      if (!task) return fail(res, 404, 'task not found');
      const app = {
        id: nextApplicationId++,
        taskId: task.id,
        taskTitle: task.title,
        hunterId: uid,
        hunterName: currentUserName(uid),
        hunterAvatar: 'bangboo:kacha',
        hunterLevel: uid === 4 ? 3 : 4,
        hunterTitle: uid === 4 ? 'Silver Hunter' : 'Gold Hunter',
        hunterReputation: uid === 4 ? 875 : 920,
        applyMessage: body.applyMessage || '',
        expectedFinishTime: body.expectedFinishTime,
        status: 'APPLIED',
        createdAt: now(),
        updatedAt: now(),
      };
      applications.unshift(app);
      task.applicationCount = (task.applicationCount || 0) + 1;
      ok(res, app);
    });
  }

  if (url.pathname === '/applications/my' && req.method === 'GET') {
    const uid = userId(req);
    return ok(res, page(applications.filter(a => a.hunterId === uid)));
  }

  const appMatch = url.pathname.match(/^\/applications\/(\d+)$/);
  if (appMatch && req.method === 'PUT') {
    return readBody(req).then(body => {
      const app = applications.find(a => a.id === Number(appMatch[1]));
      if (!app) return fail(res, 404, 'application not found');
      Object.assign(app, body, { updatedAt: now() });
      ok(res, app);
    });
  }

  const appCancel = url.pathname.match(/^\/applications\/(\d+)\/cancel$/);
  if (appCancel && req.method === 'PUT') {
    const app = applications.find(a => a.id === Number(appCancel[1]));
    if (!app) return fail(res, 404, 'application not found');
    app.status = 'CANCELLED';
    app.updatedAt = now();
    return ok(res, app);
  }

  const appReject = url.pathname.match(/^\/applications\/(\d+)\/reject$/);
  if (appReject && req.method === 'PUT') {
    const app = applications.find(a => a.id === Number(appReject[1]));
    if (!app) return fail(res, 404, 'application not found');
    app.status = 'REJECTED';
    app.updatedAt = now();
    return ok(res, app);
  }

  const appAccept = url.pathname.match(/^\/applications\/(\d+)\/accept$/);
  if (appAccept && req.method === 'PUT') {
    const app = applications.find(a => a.id === Number(appAccept[1]));
    if (!app) return fail(res, 404, 'application not found');
    const task = taskBrief(app.taskId);
    app.status = 'ACCEPTED';
    app.updatedAt = now();
    if (task) {
      task.status = 'IN_PROGRESS';
      task.assignedHunterId = app.hunterId;
    }
    const contract = {
      id: nextContractId++,
      contractNo: `CONTRACT-2026-${nextContractId}`,
      taskId: app.taskId,
      taskTitle: app.taskTitle,
      applicationId: app.id,
      publisherId: task ? task.publisherId : 2,
      publisherName: task ? task.publisherName : 'Client One',
      publisherAvatar: 'bangboo:kacha',
      hunterId: app.hunterId,
      hunterName: app.hunterName,
      hunterAvatar: app.hunterAvatar,
      bountyAmount: task ? task.bountyAmount : 0,
      bountyType: task ? task.bountyType : 'POINT',
      status: 'IN_PROGRESS',
      completionStandard: task ? task.completionStandard : '',
      evidenceRequirement: task ? task.evidenceRequirement : '',
      reviewedByPublisher: false,
      reviewedByHunter: false,
      createdAt: now(),
      acceptedAt: now(),
    };
    contracts.unshift(contract);
    return ok(res, contract);
  }

  const contractMatch = url.pathname.match(/^\/contracts\/(\d+)$/);
  if (contractMatch && req.method === 'GET') {
    const contract = contracts.find(c => c.id === Number(contractMatch[1]));
    return contract ? ok(res, contract) : fail(res, 404, 'contract not found');
  }

  if (url.pathname === '/contracts/my-published' && req.method === 'GET') {
    const uid = userId(req);
    return ok(res, page(contracts.filter(c => c.publisherId === uid)));
  }

  if (url.pathname === '/contracts/my-accepted' && req.method === 'GET') {
    const uid = userId(req);
    return ok(res, page(contracts.filter(c => c.hunterId === uid)));
  }

  const contractEvidences = url.pathname.match(/^\/contracts\/(\d+)\/evidences$/);
  if (contractEvidences && req.method === 'GET') {
    return ok(res, evidences.filter(e => e.contractId === Number(contractEvidences[1])));
  }

  if (contractEvidences && req.method === 'POST') {
    return readBody(req).then(body => {
      const contract = contracts.find(c => c.id === Number(contractEvidences[1]));
      if (!contract) return fail(res, 404, 'contract not found');
      const evidence = {
        id: nextEvidenceId++,
        contractId: contract.id,
        taskId: contract.taskId,
        submitterId: userId(req),
        submitterName: currentUserName(userId(req)),
        type: body.type || 'TEXT',
        fileUrl: body.fileUrl,
        content: body.content,
        createdAt: now(),
      };
      evidences.unshift(evidence);
      ok(res, evidence);
    });
  }

  const submitCompletion = url.pathname.match(/^\/contracts\/(\d+)\/submit-completion$/);
  if (submitCompletion && req.method === 'PUT') {
    const contract = contracts.find(c => c.id === Number(submitCompletion[1]));
    if (!contract) return fail(res, 404, 'contract not found');
    contract.status = 'WAIT_CONFIRM';
    contract.submittedAt = now();
    return ok(res, contract);
  }

  const confirmCompletion = url.pathname.match(/^\/contracts\/(\d+)\/confirm-completion$/);
  if (confirmCompletion && req.method === 'PUT') {
    const contract = contracts.find(c => c.id === Number(confirmCompletion[1]));
    if (!contract) return fail(res, 404, 'contract not found');
    contract.status = 'COMPLETED';
    contract.completedAt = now();
    const task = taskBrief(contract.taskId);
    if (task) task.status = 'COMPLETED';
    return ok(res, contract);
  }

  const cancelContract = url.pathname.match(/^\/contracts\/(\d+)\/cancel$/);
  if (cancelContract && req.method === 'PUT') {
    return readBody(req).then(body => {
      const contract = contracts.find(c => c.id === Number(cancelContract[1]));
      if (!contract) return fail(res, 404, 'contract not found');
      contract.status = 'CANCELLED';
      contract.cancelReason = body.cancelReason;
      return ok(res, contract);
    });
  }

  if (url.pathname === '/task-favorites' && req.method === 'GET') {
    const ids = favorites.get(userId(req)) || [];
    return ok(res, page(tasks.filter(t => ids.includes(t.id))));
  }

  const favoriteMatch = url.pathname.match(/^\/task-favorites\/(\d+)$/);
  if (favoriteMatch && req.method === 'POST') {
    const uid = userId(req);
    const ids = favorites.get(uid) || [];
    const id = Number(favoriteMatch[1]);
    if (!ids.includes(id)) ids.push(id);
    favorites.set(uid, ids);
    return ok(res, null);
  }

  if (favoriteMatch && req.method === 'DELETE') {
    const uid = userId(req);
    const id = Number(favoriteMatch[1]);
    favorites.set(uid, (favorites.get(uid) || []).filter(x => x !== id));
    return ok(res, null);
  }

  if (url.pathname === '/task-history' && req.method === 'GET') {
    const ids = history.get(userId(req)) || [];
    return ok(res, page(tasks.filter(t => ids.includes(t.id))));
  }

  if (url.pathname === '/admin/tasks' && req.method === 'GET') return ok(res, page(tasks));

  const reviewTask = url.pathname.match(/^\/admin\/tasks\/(\d+)\/review$/);
  if (reviewTask && req.method === 'PUT') {
    return readBody(req).then(body => {
      const task = tasks.find(t => t.id === Number(reviewTask[1]));
      if (!task) return fail(res, 404, 'task not found');
      task.status = body.approved ? 'PUBLISHED' : 'REMOVED';
      task.reviewComment = body.comment;
      return ok(res, task);
    });
  }

  fail(res, 404, `mock marketplace missing ${req.method} ${url.pathname}`);
}

function reputationHandler(req, res, url) {
  if (req.method === 'OPTIONS') return ok(res, null);
  if (req.method === 'GET' && url.pathname === '/actuator/health') return json(res, 200, { status: 'UP' });

  if (url.pathname === '/internal/stats/reputation' && req.method === 'GET') {
    if (!requireInternal(req, res)) return;
    return ok(res, { reviewCount: reviews.length, creditLogCount: 3 });
  }

  if (url.pathname === '/reviews' && req.method === 'POST') {
    return readBody(req).then(body => {
      const uid = userId(req);
      const contract = contracts.find(c => c.id === body.contractId);
      if (!contract) return fail(res, 404, 'contract not found');
      const role = uid === contract.publisherId ? 'PUBLISHER_TO_HUNTER' : 'HUNTER_TO_PUBLISHER';
      const revieweeId = uid === contract.publisherId ? contract.hunterId : contract.publisherId;
      const review = {
        id: nextReviewId++,
        contractId: contract.id,
        taskId: contract.taskId,
        taskTitle: contract.taskTitle,
        reviewerId: uid,
        reviewerName: currentUserName(uid),
        reviewerAvatar: 'bangboo:kacha',
        revieweeId,
        revieweeName: currentUserName(revieweeId),
        role,
        rating: body.rating,
        tags: body.tags || [],
        content: body.content || '',
        createdAt: now(),
      };
      reviews.unshift(review);
      if (role === 'PUBLISHER_TO_HUNTER') contract.reviewedByPublisher = true;
      if (role === 'HUNTER_TO_PUBLISHER') contract.reviewedByHunter = true;
      return ok(res, review);
    });
  }

  const reviewsByUser = url.pathname.match(/^\/reviews\/user\/(\d+)$/);
  if (reviewsByUser && req.method === 'GET') {
    return ok(res, reviews.filter(r => r.revieweeId === Number(reviewsByUser[1])));
  }

  const reviewsByTask = url.pathname.match(/^\/reviews\/task\/(\d+)$/);
  if (reviewsByTask && req.method === 'GET') {
    return ok(res, reviews.filter(r => r.taskId === Number(reviewsByTask[1])));
  }

  const reviewsByContract = url.pathname.match(/^\/reviews\/contract\/(\d+)$/);
  if (reviewsByContract && req.method === 'GET') {
    const contractId = Number(reviewsByContract[1]);
    return ok(res, {
      publisherToHunter: reviews.find(r => r.contractId === contractId && r.role === 'PUBLISHER_TO_HUNTER') || null,
      hunterToPublisher: reviews.find(r => r.contractId === contractId && r.role === 'HUNTER_TO_PUBLISHER') || null,
    });
  }

  const hunter = url.pathname.match(/^\/hunters\/(\d+)$/);
  if (hunter && req.method === 'GET') return ok(res, hunterProfile(Number(hunter[1])));

  if (url.pathname === '/hunters/me' && req.method === 'GET') return ok(res, hunterProfile(userId(req)));

  if (url.pathname === '/hunters/leaderboard' && req.method === 'GET') {
    return ok(res, [hunterProfile(3), hunterProfile(4), hunterProfile(5)].map((h, index) => ({ ...h, rank: index + 1 })));
  }

  const badges = url.pathname.match(/^\/hunters\/(\d+)\/badges$/);
  if (badges && req.method === 'GET') return ok(res, ['ON_TIME', 'GOOD_COMMUNICATION', 'COURT_JUROR']);

  const creditLogs = url.pathname.match(/^\/hunters\/(\d+)\/credit-logs$/);
  if (creditLogs && req.method === 'GET') {
    const id = Number(creditLogs[1]);
    return ok(res, page([
      { id: 1, userId: id, delta: 8, reason: 'Completed a task', sourceType: 'CONTRACT', sourceId: 302, createdAt: now() },
      { id: 2, userId: id, delta: -1, reason: 'Court adjustment', sourceType: 'COURT_RULING', sourceId: 401, createdAt: now() },
    ]));
  }

  fail(res, 404, `mock reputation missing ${req.method} ${url.pathname}`);
}

function hunterProfile(id) {
  const rep = id === 3 ? 920 : id === 4 ? 875 : 890;
  return {
    userId: id,
    username: id === 3 ? 'hunter01' : id === 4 ? 'hunter02' : 'jury01',
    nickname: currentUserName(id),
    avatarUrl: 'bangboo:kacha',
    reputation: rep,
    reputationLevel: rep >= 900 ? 'Gold' : 'Silver',
    hunterLevel: id === 3 ? 4 : 3,
    hunterTitle: id === 3 ? 'Gold Hunter' : 'Silver Hunter',
    completedTaskCount: id === 3 ? 12 : 5,
    averageRating: 4.8,
    badges: ['ON_TIME', 'GOOD_COMMUNICATION'],
  };
}

function aiHandler(req, res, url) {
  if (req.method === 'OPTIONS') return ok(res, null);
  if (req.method === 'GET' && url.pathname === '/actuator/health') return json(res, 200, { status: 'UP' });

  if (url.pathname === '/internal/stats/ai' && req.method === 'GET') {
    if (!requireInternal(req, res)) return;
    return ok(res, { aiCallCount: 42 });
  }

  if (url.pathname === '/ai/chat' && req.method === 'POST') return ok(res, { reply: 'Mock AI is online.', action: 'NONE', data: null });
  if (url.pathname === '/ai/tasks/breakdown' && req.method === 'POST') return ok(res, {
    title: 'Mock task title',
    category: 'ERRAND',
    difficulty: 'F',
    suggestedBountyMin: 10,
    suggestedBountyMax: 30,
    steps: ['Confirm requirement', 'Execute task', 'Submit evidence'],
    riskTips: ['Confirm delivery location'],
  });
  if (url.pathname === '/ai/tasks/bounty-suggestion' && req.method === 'POST') return ok(res, {
    suggestedBountyMin: 20,
    suggestedBountyMax: 60,
    reason: 'Mock suggestion based on category and difficulty.',
  });
  if (url.pathname === '/ai/tasks/risk-assessment' && req.method === 'POST') return ok(res, {
    risks: ['Scope may be unclear'],
    suggestions: ['Add completion standard'],
  });
  if (url.pathname === '/ai/tasks/cover-image' && req.method === 'POST') return ok(res, {
    imageUrl: '/uploads/mock/task-cover.png',
    prompt: 'mock cover prompt',
    source: 'mock-b-service',
  });
  if (url.pathname === '/ai/profile/bangboo-avatar' && req.method === 'POST') return ok(res, {
    imageUrl: '/uploads/mock/bangboo-avatar.png',
    prompt: 'mock avatar prompt',
    source: 'mock-b-service',
    mascotKey: 'kacha',
  });
  if (url.pathname === '/ai/tasks/smart-search' && req.method === 'POST') return ok(res, {
    keyword: 'coffee',
    category: 'ERRAND',
    tips: ['Try nearby tasks'],
  });
  if (url.pathname === '/ai/court/summary' && req.method === 'POST') return ok(res, {
    summary: 'Mock court summary.',
    focusPoints: ['scope', 'evidence', 'revision count'],
    evidenceAnalysis: 'Evidence is sufficient for a preliminary decision.',
    suggestion: 'Recommend partial support.',
  });
  if (url.pathname === '/ai/court/roast' && req.method === 'POST') return ok(res, {
    roast: 'Mock roast: keep the receipts and the scope clear.',
    style: 'ROAST',
  });

  fail(res, 404, `mock ai missing ${req.method} ${url.pathname}`);
}

function start(port, handler, label) {
  const server = http.createServer((req, res) => {
    const url = new URL(req.url, `http://127.0.0.1:${port}`);
    Promise.resolve(handler(req, res, url)).catch(err => {
      console.error(`[${label}]`, err);
      fail(res, 500, err.message || 'mock error');
    });
  });
  server.listen(port, '127.0.0.1', () => console.log(`${label} mock listening on ${port}`));
}

start(8082, marketplaceHandler, 'marketplace');
start(8083, reputationHandler, 'reputation');
start(8087, aiHandler, 'ai');
