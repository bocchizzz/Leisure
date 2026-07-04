# ============================================================
#  赏金布后端 —— 一键使用 MySQL 启动全部服务
#  用法：
#    powershell -ExecutionPolicy Bypass -File start-all-mysql.ps1
#  停止：
#    powershell -ExecutionPolicy Bypass -File stop-all.ps1
# ============================================================

# ---------- MySQL 配置（按你的环境修改这几行即可）----------
$MysqlHost   = '127.0.0.1'
$MysqlPort   = '3306'
$MysqlUser   = 'root'
$MysqlPass   = '12345'          # <<< 改成你本机 MySQL 的 root 密码
$DdlAuto     = 'validate'        # 开发用 update（自动建表）；生产建议 validate
$InitDbFirst = $true           # 首次运行设为 $true 自动建库；之后可改 $false

# ---------- 运行环境 ----------
$JavaHome = 'C:\Program Files\Java\jdk-17\'
$Java     = 'C:\Program Files\Java\jdk-17\bin\java.exe'
$MysqlBin = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'            
$Root     = 'D:\Lessons\2026CQ\CQ\server'

# ---------- 连接串公共参数 ----------
$JdbcOpts = 'useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'

# ---------- 服务清单（顺序 = 启动顺序；gateway 最后；gateway 无数据库）----------
$Services = @(
  @{ name='bb-iam-service';         db='bb_iam' },
  @{ name='bb-file-service';        db='bb_file' },
  @{ name='bb-message-service';     db='bb_message' },
  @{ name='bb-admin-ops-service';   db='bb_admin_ops' },
  @{ name='bb-court-service';       db='bb_court' },
  @{ name='bb-marketplace-service'; db='bb_marketplace' },
  @{ name='bb-reputation-service';  db='bb_reputation' },
  @{ name='bb-ai-service';          db='bb_ai' },
  @{ name='bb-gateway';             db='' }          # 网关无库
)

# ============================================================

$env:JAVA_HOME = $JavaHome
Set-Location $Root
New-Item -ItemType Directory -Force -Path (Join-Path $Root 'logs') | Out-Null

# ---------- Step 1: 建库（仅创建空库，表由 JPA 自动创建）----------
if ($InitDbFirst) {
  Write-Output '== Initialize the database (execute db/init-mysql.sql) =='
  $initSql = Join-Path $Root 'db\init-mysql.sql'
  if (Test-Path $initSql) {
    $args = @('-h', $MysqlHost, '-P', $MysqlPort, '-u', $MysqlUser, "-p$MysqlPass")
    Get-Content $initSql -Raw | & $MysqlBin @args
    if ($LASTEXITCODE -ne 0) {
      Write-Warning 'Database creation failed: Please check if MySQL is running, if the account password is correct, and if the 'mysql' command is in the PATH.'
      Write-Warning 'You can also execute it manually first: mysql -uroot -p < db\init-mysql.sql. Then, change $InitDbFirst in the script to $false and run it again.'
      exit 1
    }
    Write-Output 'Database initialization is complete.'
  } else {
    Write-Warning "The $initSql was not found, so the database creation is skipped."
  }
}

# ---------- Step 2: 依次启动服务 ----------
Write-Output ''
Write-Output '== Start all services (MySQL mode) =='
foreach ($s in $Services) {
  $jar = Join-Path $Root "$($s.name)\target\$($s.name)-0.1.0-SNAPSHOT.jar"
  if (-not (Test-Path $jar)) {
    Write-Warning "Skipping ($s.name): Jar not found (Please execute "mvn clean package -DskipTests" first)."
    continue
  }

  $jvmArgs = @('-jar', $jar)

  # 有数据库的服务：注入 MySQL 连接参数；网关(无 db)不注入
  if ($s.db -ne '') {
    $url = "jdbc:mysql://$MysqlHost`:$MysqlPort/$($s.db)?$JdbcOpts"
    $jvmArgs += "--spring.datasource.url=$url"
    $jvmArgs += "--spring.datasource.username=$MysqlUser"
    $jvmArgs += "--spring.datasource.password=$MysqlPass"
    $jvmArgs += '--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver'
    $jvmArgs += "--spring.jpa.hibernate.ddl-auto=$DdlAuto"
    $jvmArgs += '--spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect'
  }

  Start-Process -FilePath $Java -ArgumentList $jvmArgs `
    -WorkingDirectory $Root `
    -RedirectStandardOutput (Join-Path $Root "logs\$($s.name).log") `
    -RedirectStandardError  (Join-Path $Root "logs\$($s.name).err.log") `
    -WindowStyle Hidden

  Write-Output "started $($s.name)  ->  $(if($s.db){$s.db}else{'(no db)'})"
  Start-Sleep -Seconds 3
}

Write-Output ''
Write-Output 'ALL STARTED (MySQL). After waiting for approximately 10 seconds, you can access http://127.0.0.1:8080/api/tasks'
Write-Output 'Stop: powershell -ExecutionPolicy Bypass -File stop-all.ps1'
