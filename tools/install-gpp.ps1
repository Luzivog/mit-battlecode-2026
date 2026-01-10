# Ensure script is running as Administrator
If (-NOT ([Security.Principal.WindowsPrincipal] `
    [Security.Principal.WindowsIdentity]::GetCurrent()
).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator"))
{
    Write-Error "Please run this script as Administrator."
    exit 1
}

Write-Host "Installing MinGW-w64 (g++) via winget..."

winget install --id MSYS2.MSYS2 -e --source winget

# Refresh environment variables
$msysPath = "C:\msys64\mingw64\bin"
if (-not ($env:Path -split ";" | Where-Object { $_ -eq $msysPath })) {
    [Environment]::SetEnvironmentVariable(
        "Path",
        $env:Path + ";" + $msysPath,
        [EnvironmentVariableTarget]::Machine
    )
}

Write-Host "Installation complete."
Write-Host "Please restart your terminal and run: g++ --version"
