param(
    [string]$BindAddress = "127.0.0.1"
)

$ErrorActionPreference = "Stop"

# Oracle JDK's Windows selector uses a Unix-domain loopback socket. Keeping its
# socket files on a short path avoids Windows path-length failures in synced or
# deeply nested workspaces.
$javaSocketDirectory = Join-Path $env:SystemDrive "srm-java-sockets"
New-Item -ItemType Directory -Path $javaSocketDirectory -Force | Out-Null

$jvmArguments = "-Djdk.net.unixdomain.tmpdir=$javaSocketDirectory"
$applicationArguments = "--server.address=$BindAddress"

mvn spring-boot:run `
    "-Dspring-boot.run.jvmArguments=$jvmArguments" `
    "-Dspring-boot.run.arguments=$applicationArguments"
