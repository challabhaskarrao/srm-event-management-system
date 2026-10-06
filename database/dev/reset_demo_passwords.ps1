[CmdletBinding()]
param(
    [switch]$Execute,
    [switch]$ConfirmDevOnly,
    [string]$DbHost = $(if ([string]::IsNullOrWhiteSpace($env:DB_HOST)) { 'localhost' } else { $env:DB_HOST }),
    [int]$DbPort = $(if ([string]::IsNullOrWhiteSpace($env:DB_PORT)) { 3306 } else { [int]$env:DB_PORT }),
    [string]$DbName = $(if ([string]::IsNullOrWhiteSpace($env:DB_NAME)) { 'srm_event_management' } else { $env:DB_NAME }),
    [string]$DbUsername = $(if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) { 'root' } else { $env:DB_USERNAME })
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not $Execute) {
    throw 'This tool is deliberately opt-in. Re-run with -Execute -ConfirmDevOnly after reviewing this file.'
}
if (-not $ConfirmDevOnly) {
    throw 'Refusing to run without -ConfirmDevOnly. This tool is only for the four existing local development accounts.'
}
if ($DbName -ne 'srm_event_management' -or $DbHost -notin @('localhost', '127.0.0.1', '::1')) {
    throw 'Refusing non-local/non-development database target. Expected srm_event_management on localhost, 127.0.0.1, or ::1.'
}

$mysqlCommand = Get-Command mysql.exe -ErrorAction SilentlyContinue
if ($null -eq $mysqlCommand) {
    $mysqlPath = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
    if (-not (Test-Path -LiteralPath $mysqlPath)) {
        throw 'mysql.exe was not found. Install/use the MySQL client or add it to PATH.'
    }
} else {
    $mysqlPath = $mysqlCommand.Source
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$pomPath = Join-Path $repoRoot 'backend\pom.xml'
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($null -eq $mavenCommand) {
    $mavenPath = 'C:\maven\apache-maven-3.9.16\bin\mvn.cmd'
    if (-not (Test-Path -LiteralPath $mavenPath)) {
        throw 'mvn.cmd was not found. Maven is required to use the application BCrypt dependency.'
    }
} else {
    $mavenPath = $mavenCommand.Source
}

$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
$javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($null -eq $javaCommand -or $null -eq $javacCommand) {
    throw 'java.exe and javac.exe are required to generate BCrypt hashes with the application dependency.'
}

$oldMysqlPwd = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')
$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ('srm-dev-password-reset-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

function Invoke-MySqlText {
    param([Parameter(Mandatory)][string]$Sql)

    $args = @(
        '--protocol=TCP', '-h', $DbHost, '-P', [string]$DbPort, '-u', $DbUsername,
        '--database', $DbName,
        '--batch', '--raw', '--skip-column-names', '-e', $Sql
    )
    $output = & $mysqlPath @args 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw ('MySQL command failed: ' + (($output | Out-String).Trim()))
    }
    return (($output | Out-String).Trim())
}

function Convert-SecureStringToPlainText {
    param([Parameter(Mandatory)][Security.SecureString]$Value)

    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

function Read-DevPassword {
    param([Parameter(Mandatory)][string]$Label)

    $secure = Read-Host -AsSecureString "Enter the DEV password for $Label"
    return (Convert-SecureStringToPlainText $secure)
}

function Get-BcryptHashes {
    param([Parameter(Mandatory)][string[]]$Passwords)

    $classpathFile = Join-Path $tempRoot 'classpath.txt'
    $javaSourcePath = Join-Path $tempRoot 'DevBcryptHash.java'
    $javaSource = @'
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class DevBcryptHash {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(encoder.encode(line));
        }
    }
}
'@
    [IO.File]::WriteAllText($javaSourcePath, $javaSource, [Text.UTF8Encoding]::new($false))

    & $mavenPath -q -f $pomPath dependency:build-classpath "-Dmdep.outputFile=$classpathFile"
    if ($LASTEXITCODE -ne 0) {
        throw 'Maven could not build the application dependency classpath.'
    }
    $classpath = (Get-Content -LiteralPath $classpathFile -Raw).Trim()
    if ([string]::IsNullOrWhiteSpace($classpath)) {
        throw 'The application dependency classpath was empty.'
    }

    $classesPath = Join-Path $tempRoot 'classes'
    New-Item -ItemType Directory -Path $classesPath | Out-Null
    & $javacCommand.Source '-cp' $classpath '-d' $classesPath $javaSourcePath
    if ($LASTEXITCODE -ne 0) {
        throw 'javac could not compile the temporary BCrypt helper.'
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo.FileName = $javaCommand.Source
    $process.StartInfo.Arguments = '-cp "' + $classesPath + ';' + $classpath + '" DevBcryptHash'
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.CreateNoWindow = $true
    $process.StartInfo.RedirectStandardInput = $true
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.RedirectStandardError = $true
    if (-not $process.Start()) {
        throw 'Could not start the temporary BCrypt helper.'
    }
    foreach ($password in $Passwords) {
        $process.StandardInput.WriteLine($password)
    }
    $process.StandardInput.Close()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) {
        throw ('The temporary BCrypt helper failed: ' + $stderr.Trim())
    }
    $hashes = @($stdout -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    if ($hashes.Count -ne $Passwords.Count -or @($hashes | Where-Object { $_ -notmatch '^\$2[aby]\$12\$[./A-Za-z0-9]{53}$' }).Count -ne 0) {
        throw 'The BCrypt helper did not return the expected number of cost-12 hashes.'
    }
    return $hashes
}

try {
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $dbPassword = Read-Host -AsSecureString 'Enter the MySQL password (input is hidden)'
        $dbPasswordPlain = Convert-SecureStringToPlainText $dbPassword
        [Environment]::SetEnvironmentVariable('MYSQL_PWD', $dbPasswordPlain, 'Process')
    } else {
        [Environment]::SetEnvironmentVariable('MYSQL_PWD', $env:DB_PASSWORD, 'Process')
    }

    Write-Host 'Read-only preflight: locating the four existing DEV accounts and preserving relationships.'
    $preflightSql = @"
SELECT
 (SELECT COUNT(*) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='student-review@example.invalid' AND r.role_name='STUDENT') AS student_user_count,
 (SELECT COALESCE(MIN(u.user_id),0) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='student-review@example.invalid' AND r.role_name='STUDENT') AS student_user_id,
 (SELECT COUNT(*) FROM students s JOIN users u ON u.user_id=s.user_id JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='student-review@example.invalid' AND r.role_name='STUDENT' AND s.register_number='DEV-STUDENT-001') AS student_profile_count,
 (SELECT COUNT(*) FROM student_registry sr JOIN users u ON u.user_id=sr.claimed_user_id WHERE sr.registration_number='DEV-STUDENT-001' AND LOWER(u.email)='student-review@example.invalid') AS student_claim_count,
 (SELECT COUNT(*) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='mentor-review@example.invalid' AND r.role_name='MENTOR') AS mentor_user_count,
 (SELECT COALESCE(MIN(u.user_id),0) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='mentor-review@example.invalid' AND r.role_name='MENTOR') AS mentor_user_id,
 (SELECT COUNT(*) FROM faculty f JOIN users u ON u.user_id=f.user_id JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='mentor-review@example.invalid' AND r.role_name='MENTOR' AND f.faculty_code='DEV-MENTOR-001') AS mentor_faculty_count,
 (SELECT COALESCE(MIN(f.faculty_id),0) FROM faculty f JOIN users u ON u.user_id=f.user_id WHERE LOWER(u.email)='mentor-review@example.invalid' AND f.faculty_code='DEV-MENTOR-001') AS mentor_faculty_id,
 (SELECT COUNT(*) FROM faculty_registry fr JOIN users u ON u.user_id=fr.claimed_user_id WHERE fr.faculty_code='DEV-MENTOR-001' AND LOWER(u.email)='mentor-review@example.invalid') AS mentor_registry_claim_count,
 (SELECT COUNT(*) FROM mentor_assignments ma JOIN faculty f ON f.faculty_id=ma.faculty_id JOIN users u ON u.user_id=f.user_id WHERE LOWER(u.email)='mentor-review@example.invalid' AND f.faculty_code='DEV-MENTOR-001' AND ma.is_active=TRUE AND (ma.active_until IS NULL OR ma.active_until>=CURRENT_DATE)) AS mentor_assignment_count,
 (SELECT COUNT(*) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='hod-review@example.invalid' AND r.role_name='HOD') AS hod_user_count,
 (SELECT COALESCE(MIN(u.user_id),0) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='hod-review@example.invalid' AND r.role_name='HOD') AS hod_user_id,
 (SELECT COUNT(*) FROM faculty f JOIN users u ON u.user_id=f.user_id JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='hod-review@example.invalid' AND r.role_name='HOD' AND f.faculty_code='DEV-HOD-001') AS hod_faculty_count,
 (SELECT COALESCE(MIN(f.faculty_id),0) FROM faculty f JOIN users u ON u.user_id=f.user_id WHERE LOWER(u.email)='hod-review@example.invalid' AND f.faculty_code='DEV-HOD-001') AS hod_faculty_id,
 (SELECT COUNT(*) FROM faculty f JOIN users u ON u.user_id=f.user_id WHERE LOWER(u.email)='hod-review@example.invalid' AND f.faculty_code='DEV-HOD-001' AND f.department_id IS NOT NULL) AS hod_department_count,
 (SELECT COUNT(*) FROM faculty_registry fr JOIN users u ON u.user_id=fr.claimed_user_id WHERE fr.faculty_code='DEV-HOD-001' AND LOWER(u.email)='hod-review@example.invalid') AS hod_registry_claim_count,
 (SELECT COUNT(*) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='admin@srmist.edu.in' AND r.role_name='ADMIN') AS admin_user_count,
 (SELECT COALESCE(MIN(u.user_id),0) FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='admin@srmist.edu.in' AND r.role_name='ADMIN') AS admin_user_id,
 (SELECT COALESCE(MAX(u.email),'') FROM users u JOIN roles r ON r.role_id=u.role_id WHERE LOWER(u.email)='admin@srmist.edu.in' AND r.role_name='ADMIN') AS admin_email;
"@
    $preflight = Invoke-MySqlText $preflightSql
    $fields = @($preflight -split "`t")
    if ($fields.Count -ne 19) {
        throw 'Unexpected preflight output; no password update was attempted.'
    }
    $numbers = @($fields[0..17] | ForEach-Object { [int64]$_ })
    $adminEmail = $fields[18]

    $checks = @{
        student = ($numbers[0] -eq 1 -and $numbers[2] -eq 1 -and $numbers[3] -eq 1)
        mentor  = ($numbers[4] -eq 1 -and $numbers[6] -eq 1 -and $numbers[8] -eq 1 -and $numbers[9] -ge 1)
        hod     = ($numbers[10] -eq 1 -and $numbers[12] -eq 1 -and $numbers[14] -eq 1 -and $numbers[15] -eq 1)
        admin   = ($numbers[16] -eq 1 -and $adminEmail -eq 'admin@srmist.edu.in')
    }
    if ($checks.Values -contains $false) {
        throw ('Preflight failed. Counts were: ' + ($numbers -join ',') + '. No password update was attempted.')
    }

    Write-Host ('Student user_id=' + $numbers[1] + ', registry claim=YES')
    Write-Host ('Mentor user_id=' + $numbers[5] + ', faculty_id=' + $numbers[7] + ', active assignments=' + $numbers[9])
    Write-Host ('HOD user_id=' + $numbers[11] + ', faculty_id=' + $numbers[13] + ', department=YES')
    Write-Host ('Admin email=' + $adminEmail + ', user_id=' + $numbers[17])

    $passwords = @(
        (Read-DevPassword 'student-review@example.invalid'),
        (Read-DevPassword 'mentor-review@example.invalid'),
        (Read-DevPassword 'hod-review@example.invalid'),
        (Read-DevPassword $adminEmail)
    )
    $hashes = @(Get-BcryptHashes $passwords)

    $studentId = $numbers[1]
    $mentorId = $numbers[5]
    $hodId = $numbers[11]
    $adminId = $numbers[17]
    $userIds = @($studentId, $mentorId, $hodId, $adminId)
    $sql = @"
START TRANSACTION;
UPDATE users
SET password_hash = CASE user_id
    WHEN $studentId THEN '$($hashes[0])'
    WHEN $mentorId THEN '$($hashes[1])'
    WHEN $hodId THEN '$($hashes[2])'
    WHEN $adminId THEN '$($hashes[3])'
END,
    session_version = session_version + 1,
    updated_at = CURRENT_TIMESTAMP
WHERE (user_id=$studentId AND LOWER(email)='student-review@example.invalid')
   OR (user_id=$mentorId AND LOWER(email)='mentor-review@example.invalid')
   OR (user_id=$hodId AND LOWER(email)='hod-review@example.invalid')
   OR (user_id=$adminId AND LOWER(email)=LOWER('$adminEmail'));
SELECT ROW_COUNT() AS updated_users;
UPDATE password_reset_tokens SET used_at=CURRENT_TIMESTAMP WHERE user_id IN ($($userIds -join ',')) AND used_at IS NULL;
SELECT ROW_COUNT() AS invalidated_reset_tokens;
COMMIT;
"@
    $updateOutput = Invoke-MySqlText $sql
    $updateLines = @($updateOutput -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    if ($updateLines.Count -lt 2 -or $updateLines[0] -ne '4') {
        throw ('Expected exactly four users to be updated, but MySQL reported: ' + $updateOutput)
    }
    Write-Host 'DEV password reset completed for exactly the four preflight-verified existing users.'
    Write-Host 'Password hashes were generated in memory with BCrypt cost 12 and were not printed.'
    Write-Host ('Reset-token invalidation result: ' + $updateOutput)
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $oldMysqlPwd, 'Process')
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
