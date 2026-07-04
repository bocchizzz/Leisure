# 一键启动全部后端服务（H2 默认，零配置）
# 用法：在 server 目录下 powershell -ExecutionPolicy Bypass -File start-all.ps1
# $ErrorActionPreference = 'Continue'
$java = 'C:\Env\TencentKona-25.0.3.b1\bin\java.exe'
# $root = 'C:\Users\neonchen\Desktop\CQ\server'
# $env:JAVA_HOME = 'C:\Env\TencentKona-25.0.3.b1'
# cd $root
# New-Item -ItemType Directory -Force -Path (Join-Path $root 'logs') | Out-Null

# 自动获取 Java 路径
if ($env:JAVA_HOME) {
    $java = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $java = (Get-Command java -ErrorAction SilentlyContinue).Source
}
# $env:JAVA_HOME = Split-Path (Split-Path $java) -Parent

echo $java

# 按依赖顺序启动：先基础/用户，再业务，最后网关
$services = @(
  'bb-iam-service',
  'bb-file-service',
  'bb-message-service',
  'bb-admin-ops-service',
  'bb-court-service',
  'bb-marketplace-service',
  'bb-reputation-service',
  'bb-ai-service',
  'bb-gateway'
)

foreach ($s in $services) {
  $jar = ".\$s\target\$s-0.1.0-SNAPSHOT.jar"
  if (-not (Test-Path $jar)) {
    Write-Output "SKIP (no jar): $s"
    continue
  }
  Start-Process -FilePath $java `
    -ArgumentList '-jar', $jar `
    -WorkingDirectory .\ `
    -RedirectStandardOutput (".\logs\$s.log") `
    -RedirectStandardError  (".\logs\$s.err.log") `
    -WindowStyle Hidden
  Write-Output "started $s"
  Start-Sleep -Seconds 3
}
Write-Output 'ALL STARTED. 等待约10秒后可访问 http://127.0.0.1:8080/api/tasks'
