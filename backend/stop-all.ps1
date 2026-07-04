# 一键停止全部后端服务
# 用法：powershell -ExecutionPolicy Bypass -File stop-all.ps1
$ports = 8080,8081,8082,8083,8084,8085,8086,8087,8088
foreach ($p in $ports) {
  $c = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
  if ($c) {
    Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
    Write-Output "stopped $p"
  }
}
Write-Output 'ALL STOPPED'
