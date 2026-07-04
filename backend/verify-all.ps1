# ============================================================
#  赏金布后端 —— 一键验证脚本
#  用法：powershell -ExecutionPolicy Bypass -File verify-all.ps1
#  作用：检查 9 个服务端口监听 + 健康检查 + 经网关的公开接口
# ============================================================

$ErrorActionPreference = 'SilentlyContinue'

$ports = [ordered]@{
  8080 = 'bb-gateway'
  8081 = 'bb-iam-service'
  8082 = 'bb-marketplace-service'
  8083 = 'bb-reputation-service'
  8084 = 'bb-message-service'
  8085 = 'bb-file-service'
  8086 = 'bb-court-service'
  8087 = 'bb-ai-service'
  8088 = 'bb-admin-ops-service'
}

Write-Output '==================== 端口监听检查 ===================='
$listenCount = 0
foreach ($p in $ports.Keys) {
  $c = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
  if ($c) {
    Write-Output ("  [LISTEN] {0}  {1}" -f $p, $ports[$p])
    $listenCount++
  } else {
    Write-Output ("  [ --   ] {0}  {1}  (未启动)" -f $p, $ports[$p])
  }
}
Write-Output ("监听中: {0}/9" -f $listenCount)

Write-Output ''
Write-Output '==================== 健康检查 (actuator/health) ===================='
foreach ($p in $ports.Keys) {
  $body = (curl.exe -s ("http://127.0.0.1:{0}/actuator/health" -f $p))
  if ($body -match '"status"\s*:\s*"UP"') {
    Write-Output ("  [UP    ] {0}  {1}" -f $p, $ports[$p])
  } elseif ($body) {
    Write-Output ("  [?     ] {0}  {1}  -> {2}" -f $p, $ports[$p], $body)
  } else {
    Write-Output ("  [DOWN  ] {0}  {1}  (无响应)" -f $p, $ports[$p])
  }
}

Write-Output ''
Write-Output '==================== 经网关公开接口 (http://127.0.0.1:8080/api) ===================='

function Test-Api($label, $url) {
  $body = (curl.exe -s $url)
  if ($body -match '"code"\s*:\s*200') {
    Write-Output ("  [OK    ] {0}" -f $label)
  } elseif ($body) {
    $preview = if ($body.Length -gt 120) { $body.Substring(0,120) + '...' } else { $body }
    Write-Output ("  [WARN  ] {0}  -> {1}" -f $label, $preview)
  } else {
    Write-Output ("  [FAIL  ] {0}  (无响应，网关或后端未就绪)" -f $label)
  }
}

Test-Api 'GET /api/tasks (任务大厅)'              'http://127.0.0.1:8080/api/tasks'
Test-Api 'GET /api/hunters/leaderboard (榜单)'    'http://127.0.0.1:8080/api/hunters/leaderboard'

Write-Output ''
Write-Output '==================== 内部接口鉴权检查（应被拒绝）===================='
$body = (curl.exe -s 'http://127.0.0.1:8086/internal/court-cases/401')
if ($body -match '403') {
  Write-Output '  [OK    ] court 内部接口无 token 被正确拒绝 (403)'
} else {
  Write-Output ("  [WARN  ] court 内部接口无 token 返回: {0}" -f $body)
}

Write-Output ''
Write-Output '验证完成。若公开接口为 OK，则前端可经 http://127.0.0.1:8080/api 联调。'
